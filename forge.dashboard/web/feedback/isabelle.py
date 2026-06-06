"""Isabelle/UTP Z-Machine ``isabelle build`` feedback."""
from __future__ import annotations

import re
from pathlib import Path

from .base import (
    Issue,
    Feedback,
    TraceRef,
    collect_files_to_review,
)


_ISABELLE_STAR_LINE_RE = re.compile(r"^\*\*\*\s*(.+?)\s*$", re.MULTILINE)
_ISABELLE_LINE_OF_RE = re.compile(
    r"\(line\s+(\d+)\s+of\s+\"([^\"]+)\"\)"
)
_ISABELLE_FINISHED_RE = re.compile(
    r"^Finished\s+(\S+)\s+\(([\d.]+)s",
    re.MULTILINE,
)

# Markers Isabelle prints in verbose mode (-v) BEFORE a proof step. We
# scan backward from `*** Timeout` / `*** Interrupt` to attach a lemma
# or theory name to otherwise context-less error messages.
#
# Patterns covered:
#   "lemma <name>:"                        — source line echo (rare in build mode)
#   "theorem <name>:"                      — same, for `theorem` declarations
#   'proving lemma "<name>"'               — older verbose form (Isabelle < 2022)
#   'Loading theory "<name>"'              — theory loader (rare in build mode)
#   "<session>: theory <session>.<theory>" — Isabelle 2023 `build -v` marker
#                                            (the actual format observed; both
#                                            -v and -v -v emit this and *only*
#                                            this for theory boundaries)
# `lemma\b` excludes `lemmas`.
_ISABELLE_CONTEXT_MARKER_RE = re.compile(
    r'(?:^\s*(?:lemma|theorem)\b\s+(?P<lemma1>\w+)\s*:)'
    r'|(?:proving\s+lemma\s+"(?P<lemma2>[^"]+)")'
    r'|(?:Loading\s+theory\s+"(?P<theory1>[^"]+)")'
    r'|(?:^\s*\S+:\s+theory\s+\S+\.(?P<theory2>\w+)\s*$)',
    re.MULTILINE,
)
_LEMMA_DECL_RE = re.compile(r'^\s*(?:lemma|theorem)\b\s+(\w+)')


def _find_preceding_context(raw_output: str, before_offset: int) -> dict:
    """Walk backward from ``before_offset`` to find the most recent
    lemma/theory marker.

    Returns ``{'lemma': str | None, 'theory': str | None}``. Used to
    enrich Timeout/Interrupt issues with the name of the goal Isabelle
    was working on when the tactic stalled.
    """
    prefix = raw_output[:before_offset]
    last_lemma: str | None = None
    last_theory: str | None = None
    for m in _ISABELLE_CONTEXT_MARKER_RE.finditer(prefix):
        name = m.group('lemma1') or m.group('lemma2')
        if name:
            last_lemma = name
            continue
        theory_name = m.group('theory1') or m.group('theory2')
        if theory_name:
            last_theory = theory_name
    return {'lemma': last_lemma, 'theory': last_theory}


def _classify_tactic_timeout(context: dict) -> tuple[str, str, str]:
    """Classify ``*** Timeout`` using preceding lemma context.

    Special-cased for ``*_deadlock_free`` lemmas because that's the
    most common cause and the fix is structural (Java side), not tactic
    tuning.
    """
    lemma = context.get('lemma')

    if lemma and lemma.endswith('_deadlock_free'):
        title = f"Tactic timeout in {lemma} (deadlock_free)"
        fix = (
            "Z-Machine `deadlock_free` proof timed out — typically on the "
            "`by (metis St.exhaust_disc)` step. That tactic requires every "
            "state in the `St` enum to have at least one bare-precondition "
            "operation (i.e. a `zoperation` with `pre \"st = X\"` and no "
            "extra conjuncts). If any mode block in the controller has "
            "only guarded transitions, the residual disjunction leaves a "
            "`st = X ∧ <guard>` case that `St.exhaust_disc` cannot match, "
            "so `metis` searches indefinitely.\n\n"
            "Fix: inspect every mode block in the controller's step() "
            "method. Confirm at least one inner branch is either (a) an "
            "event with no extra guard (e.g. `if (event instanceof X)`), "
            "or (b) the explicit `else { /* stay */ }` fallback. See "
            "docs/fixes/I1_deadlock_free_proof.md for the proof tactic "
            "rationale and the residual-goal shape."
        )
        return ("isabelle_tactic_timeout", title, fix)

    if lemma:
        title = f"Tactic timeout in {lemma}"
        fix = (
            f"An Isabelle tactic on lemma `{lemma}` exceeded the per-goal "
            "timeout. If the lemma's tactic includes `auto`, `metis`, or "
            "`smt`, that proof method ran out of search budget. Either "
            "tighten the goal so the auto tactic can dispatch (often a "
            "Java-side change), hand-write a more targeted proof script "
            "in the Z-Machine session theory, or raise "
            "`phases.isabelle_verify.proof_timeout` in pipeline.yaml."
        )
    else:
        title = "Tactic timeout (lemma name unknown)"
        fix = (
            "An Isabelle tactic exceeded the per-goal timeout, and no "
            "preceding `lemma` / `proving lemma` / `Loading theory` "
            "marker was found in the captured output to identify which "
            "goal stalled. Either re-run with a higher Isabelle verbosity "
            "(`-v -v`) to surface lemma markers, or inspect the .thy file "
            "manually to locate `auto` / `metis` / `smt` calls."
        )
    return ("isabelle_tactic_timeout", title, fix)


def _classify_tactic_interrupt(context: dict) -> tuple[str, str, str]:
    """Classify ``*** Interrupt`` — usually a cascade after a Timeout."""
    lemma = context.get('lemma')
    title = (f"Tactic interrupt in {lemma}" if lemma
             else "Tactic interrupt (lemma name unknown)")
    fix = (
        "Isabelle reported an interrupt. This is usually the cascade of "
        "a preceding `*** Timeout` rather than an independent failure — "
        "address the timeout above and this should resolve."
    )
    return ("isabelle_tactic_timeout", title, fix)


def _classify_isabelle_error(message: str, context: dict | None = None) -> tuple[str, str, str]:
    """Return (kind, title, fix_directive) for an Isabelle '*** ...' line.

    ``context`` (optional) is the result of ``_find_preceding_context()``
    for this error's location in the raw output. Used to attach a lemma
    name to otherwise context-less messages like ``*** Timeout`` and
    ``*** Interrupt``; unused for messages that carry their own location.
    """
    lowered = message.lower()
    ctx = context or {}

    # Tactic timeout / interrupt — these are the bare strings Isabelle
    # emits without a lemma or line hint. Without preceding-context
    # enrichment they classify as 'isabelle_other' and provide no path
    # to a fix.
    if lowered.startswith("timeout"):
        return _classify_tactic_timeout(ctx)
    if lowered.startswith("interrupt"):
        return _classify_tactic_interrupt(ctx)

    if "failed to finish proof" in lowered:
        return (
            "isabelle_proof_failed",
            "Proof did not close",
            "Isabelle could not discharge a proof obligation. The lemma "
            "specified in the message above did not close via the chosen "
            "tactic (`zpog_full`, `auto`, `deadlock_free`, etc.). "
            "Inspect the linked Java element: a guard or invariant may "
            "have changed in a way that the automated tactic can no "
            "longer dispatch. Hand-write a proof script in the Z-Machine "
            "session theory if the auto tactics genuinely cannot reach "
            "this proof.",
        )
    if "undefined fact" in lowered or "no facts named" in lowered:
        return (
            "isabelle_undefined_ref",
            "Undefined fact / lemma reference",
            "A theory references a lemma or fact that is not declared. "
            "Most likely the EGL emitted a reference to something the "
            "current ETL output no longer produces (e.g. a renamed "
            "operation). Re-run phase 5c (Isabelle Theory Generation) "
            "after the underlying Java change so the theory references "
            "match the current state machine.",
        )
    if "type unification" in lowered or "type mismatch" in lowered:
        return (
            "isabelle_type_mismatch",
            "Type unification failed",
            "An expression in the generated theory has incompatible "
            "types. Often this is because the auto-emitted `consts` "
            "signature (`unit \\<Rightarrow> integer`) is too narrow for "
            "an actual call site. Refine the signature manually in the "
            "session theory, or extend phase 5c to infer it from the "
            "Java method's parameters.",
        )
    if "outer syntax error" in lowered or "bad theory" in lowered or "malformed" in lowered:
        return (
            "isabelle_parse_error",
            "Theory will not parse",
            "Isabelle's outer syntax parser rejected the generated "
            ".thy file. The line number above points at the offending "
            "construct. This is usually a template-side bug — see "
            "docs/fixes/I2_template_fork.md for known classes.",
        )
    if "session" in lowered and "fail" in lowered:
        return (
            "isabelle_session_failed",
            "Session FAILED",
            "The session-level build failed. Check earlier '*** ...' "
            "lines for the originating cause.",
        )
    if "no parent session" in lowered or "missing session" in lowered:
        return (
            "isabelle_heap_missing",
            "Parent session heap not built",
            "The `Z_Machine` parent heap has not been built. One-time "
            "command in WSL: `isabelle build -b Z_Machine`. See "
            "docs/design/isabelle_wsl_setup.md.",
        )
    return (
        "isabelle_other",
        f"Isabelle error: {message[:80]}",
        "See the verbatim message above. If it points at a specific "
        "lemma in the Z-Machine theory, treat it like a proof failure "
        "and inspect the originating Java element.",
    )


def build_isabelle_issues(raw_output: str) -> list[Issue]:
    """Parse Isabelle build output into Issue records.

    Strategy: first collect every ``*** ...`` line, then for each one
    look in the same line and the next two lines for an optional
    ``(line N of "thy/path")`` continuation. Some Isabelle versions
    inline the location, others put it on a follow-up ``*** (line ...)``
    line. Lines that are ONLY the location continuation are skipped as
    primary error messages.
    """
    issues: list[Issue] = []
    seen: set[tuple[str, str]] = set()

    star_matches = list(_ISABELLE_STAR_LINE_RE.finditer(raw_output))
    for idx, m in enumerate(star_matches):
        message = m.group(1).strip()
        if not message:
            continue
        # Skip pure location continuations like `*** (line 380 of "x.thy")`
        if message.startswith("(line ") and message.endswith(")"):
            continue

        # Look for an inline location hint, or one in the next 2 *** lines.
        line_num: int | None = None
        thy_path = ""
        loc_match = _ISABELLE_LINE_OF_RE.search(message)
        if loc_match:
            line_num, thy_path = int(loc_match.group(1)), loc_match.group(2)
        else:
            for nxt in star_matches[idx + 1: idx + 3]:
                nxt_msg = nxt.group(1).strip()
                lm = _ISABELLE_LINE_OF_RE.search(nxt_msg)
                if lm:
                    line_num, thy_path = int(lm.group(1)), lm.group(2)
                    break

        key = (message[:80], thy_path)
        if key in seen:
            continue
        seen.add(key)

        # Look backward for the most recent `lemma X:` / `Loading theory "Y"`
        # marker so context-less messages (Timeout, Interrupt) can be tied
        # to the goal Isabelle was processing.
        context = _find_preceding_context(raw_output, m.start())

        kind, title, fix = _classify_isabelle_error(message, context=context)
        raw_lines = [f"*** {message}"]
        if thy_path:
            loc = thy_path + (f":{line_num}" if line_num else "")
            raw_lines.append(f"At: {loc}")
        else:
            if context.get('lemma'):
                raw_lines.append(f"Preceding lemma: {context['lemma']}")
            if context.get('theory'):
                raw_lines.append(f"In theory: {context['theory']}")

        java_trace: list[TraceRef] = []
        if thy_path:
            java_trace.append(TraceRef(file=thy_path, line_start=line_num))
        elif context.get('lemma'):
            # No inline location, but we know the lemma name. The .thy
            # file path + line is filled in later by _enrich_thy_locations
            # if build_isabelle_feedback was passed a session_dir.
            java_trace.append(TraceRef(
                file=context.get('theory', "") or "",
                element=context['lemma'],
            ))
        elif context.get('theory'):
            # Only theory is known (Isabelle's `build -v` emits theory-
            # level markers but no per-lemma echo). _enrich_thy_locations
            # may upgrade this with a deadlock-free heuristic if the .thy
            # contains a `*_deadlock_free` lemma.
            java_trace.append(TraceRef(
                file=context['theory'],
                element="",
            ))

        issues.append(Issue(
            kind=kind,
            title=title,
            raw="\n".join(raw_lines),
            fix_directive=fix,
            java_trace=java_trace,
        ))
    return issues


def _enrich_thy_locations(issues: list[Issue], session_dir: Path) -> None:
    """Enrich Isabelle issues with .thy file context.

    Two enrichment paths:

    1. **Lemma known, location not.** Scan .thy files for the named
       lemma and fill in ``file`` + ``line_start`` on the TraceRef.

    2. **Theory known, lemma unknown.** Isabelle's ``build -v`` doesn't
       echo per-lemma markers, so timeout/interrupt issues often arrive
       with only a theory name. If the theory contains a
       ``*_deadlock_free`` lemma — overwhelmingly the most common
       timeout cause in this template — promote that as the suspected
       culprit and upgrade the issue's title + fix directive to the
       deadlock-free-specific diagnosis.

    Mutates ``issues`` in place.
    """
    try:
        # Sort so the cross-file fallback lookup below picks a
        # deterministic .thy when a lemma name appears in multiple files.
        thy_files = sorted(session_dir.glob("*.thy"))
    except OSError:
        return
    if not thy_files:
        return

    # lemmas_by_thy[thy_stem] = [(lemma_name, line_num), ...]
    lemmas_by_thy: dict[str, list[tuple[Path, str, int]]] = {}
    flat_index: dict[tuple[str, str], tuple[Path, int]] = {}
    for thy in thy_files:
        try:
            entries: list[tuple[Path, str, int]] = []
            for i, line in enumerate(
                thy.read_text(encoding='utf-8').splitlines(), 1
            ):
                m = _LEMMA_DECL_RE.match(line)
                if m:
                    name = m.group(1)
                    entries.append((thy, name, i))
                    flat_index[(thy.name, name)] = (thy, i)
                    flat_index[(thy.stem, name)] = (thy, i)
            lemmas_by_thy[thy.stem] = entries
        except OSError:
            continue

    for issue in issues:
        for ref in issue.java_trace:
            if ref.line_start is not None:
                continue  # already located

            if ref.element:
                # Path 1: lemma known, find its line.
                hit = (flat_index.get((ref.file, ref.element))
                       or flat_index.get((f"{ref.file}.thy", ref.element)))
                if not hit:
                    # Fall back to any .thy that has this lemma.
                    for (_thy, lemma_name), pair in flat_index.items():
                        if lemma_name == ref.element:
                            hit = pair
                            break
                if hit:
                    thy_path, line_num = hit
                    ref.file = str(thy_path)
                    ref.line_start = line_num
            elif ref.file and issue.kind == "isabelle_tactic_timeout":
                # Path 2: theory known but no lemma. Look for a
                # `*_deadlock_free` lemma in that theory and promote it
                # — but only for the Timeout issue itself. Interrupts
                # are cascades; their generic "address the timeout above"
                # message is more accurate than upgrading them too.
                is_timeout = issue.raw.lstrip().lower().startswith("*** timeout")
                entries = lemmas_by_thy.get(ref.file, [])
                deadlock_free = [
                    (thy, name, ln) for thy, name, ln in entries
                    if name.endswith('_deadlock_free')
                ]
                if deadlock_free:
                    thy_path, lemma_name, line_num = deadlock_free[0]
                    ref.file = str(thy_path)
                    ref.line_start = line_num
                    ref.element = lemma_name
                    if is_timeout:
                        _upgrade_timeout_to_deadlock_free(issue, lemma_name)
                    else:
                        # Interrupt: refine the title so it agrees with
                        # the enriched trace, but keep the cascade-from-
                        # timeout fix directive untouched.
                        issue.title = (f"Tactic interrupt — cascade from "
                                       f"timeout in {lemma_name}")


def _build_success_body(success_stats: dict | None,
                        session_dir: Path | None) -> str:
    """Render the informational body for a passing Isabelle run.

    Lists exactly which lemmas were proven, categorised by their
    semantic role (deadlock-freedom, invariant preservation, other).
    Falls back to scanning .thy files in ``session_dir`` if the runner
    didn't pre-compute ``success_stats['lemma_stats']``.
    """
    lines: list[str] = []

    elapsed = (success_stats or {}).get("elapsed") or ""
    peak_mb = (success_stats or {}).get("peak_memory_mb")
    lemma_stats = (success_stats or {}).get("lemma_stats") or {}

    if elapsed or peak_mb is not None:
        meta_bits: list[str] = []
        if elapsed:
            meta_bits.append(f"elapsed **{elapsed}**")
        if peak_mb is not None:
            meta_bits.append(f"peak memory **{peak_mb} MB**")
        lines.append("Runtime: " + ", ".join(meta_bits) + ".")
        lines.append("")

    # Per-theory lemma listing — names, not just counts.
    by_theory = lemma_stats.get("by_theory") or []
    # Fallback: scan session_dir directly if the runner didn't pre-scan.
    if not by_theory and session_dir is not None:
        try:
            thy_files = sorted(session_dir.glob("*.thy"))
        except OSError:
            thy_files = []
        for thy in thy_files:
            try:
                text = thy.read_text(encoding="utf-8")
            except OSError:
                continue
            names = [
                m.group(1) for line in text.splitlines()
                for m in [_LEMMA_DECL_RE.match(line)] if m
            ]
            if not names:
                continue
            by_theory.append({
                "theory": thy.stem,
                "lemmas": names,
                "deadlock_free": [n for n in names if n.endswith("_deadlock_free")],
                "invariant_count": sum(1 for n in names if n.endswith("_inv")),
            })

    if by_theory:
        total = sum(len(t.get("lemmas") or []) for t in by_theory)
        lines.append(f"**Theories built ({len(by_theory)}), "
                     f"{total} lemma(s) proven total:**")
        for thy in by_theory:
            theory_name = thy.get("theory", "?")
            lemmas = thy.get("lemmas") or []
            deadlock_free = thy.get("deadlock_free") or []
            inv_count = thy.get("invariant_count", 0)
            other = [
                n for n in lemmas
                if n not in deadlock_free and not n.endswith("_inv")
            ]
            lines.append("")
            lines.append(f"**`{theory_name}.thy`** — {len(lemmas)} lemmas:")
            if deadlock_free:
                lines.append("- *Deadlock-freedom* ({} lemma{}):".format(
                    len(deadlock_free),
                    "" if len(deadlock_free) == 1 else "s",
                ))
                for n in deadlock_free:
                    lines.append(f"  - `{n}`")
            if inv_count:
                inv_names = [n for n in lemmas if n.endswith("_inv")]
                lines.append(
                    f"- *Invariant preservation* ({inv_count} lemmas): "
                    + ", ".join(f"`{n}`" for n in inv_names)
                )
            if other:
                lines.append(
                    f"- *Other* ({len(other)} lemma{'' if len(other) == 1 else 's'}): "
                    + ", ".join(f"`{n}`" for n in other)
                )

    return "\n".join(lines).rstrip()


def _upgrade_timeout_to_deadlock_free(issue: Issue, lemma_name: str) -> None:
    """Promote a generic-timeout issue to the deadlock-free-specific
    diagnosis when ``.thy`` introspection identified ``lemma_name`` as
    a ``*_deadlock_free`` lemma. Used when Isabelle's build output gave
    us theory-level granularity but the .thy reveals the prime suspect.
    """
    issue.title = (f"Tactic timeout, suspected in {lemma_name} "
                   "(deadlock_free — inferred from .thy)")
    issue.fix_directive = (
        f"Z-Machine `deadlock_free` proof timed out — most likely on the "
        f"`by (metis St.exhaust_disc)` step of `{lemma_name}`. (Isabelle's "
        f"build output only provides theory-level granularity, so the "
        f"lemma name is inferred from the .thy file.) That tactic requires "
        f"every state in the `St` enum to have at least one bare-"
        f"precondition operation. If any mode block in the controller has "
        f"only guarded transitions, the residual disjunction leaves a "
        f"`st = X ∧ <guard>` case that `St.exhaust_disc` cannot match, so "
        f"`metis` searches indefinitely."
        f"\n\n"
        f"Fix: inspect every mode block in the controller's step() method. "
        f"Confirm at least one inner branch is either (a) an event with "
        f"no extra guard (e.g. `if (event instanceof X)`), or (b) the "
        f"explicit `else {{ /* stay */ }}` fallback. See "
        f"docs/fixes/I1_deadlock_free_proof.md for the proof tactic "
        f"rationale and residual-goal shape."
    )


def build_isabelle_feedback(
    label: str,
    status: str,
    summary: str,
    raw_output: str,
    trace_data: dict | None,
    session_dir: Path | None = None,
    success_stats: dict | None = None,
) -> Feedback:
    """Build feedback for the isabelle_verify phase.

    ``session_dir`` (optional) is the directory holding the generated
    .thy files. When provided, issues that name a lemma but lack a line
    number (typically from Timeout/Interrupt) have their java_trace
    entries enriched with the actual .thy path + line. Without it the
    issues still carry the lemma name, just not the location.

    ``success_stats`` (optional) is a dict from the runner with
    ``finished`` (list of (session, secs) tuples), ``elapsed`` (string),
    and ``peak_memory_mb`` (int). On the success path it's used to
    enrich the summary with per-theory durations, total lemma count
    discovered from the .thy files, and total wall time.
    """
    issues: list[Issue] = []
    if status != "passed":
        issues = build_isabelle_issues(raw_output)
        if session_dir is not None:
            _enrich_thy_locations(issues, session_dir)
        # If the parser found nothing but we know it failed, surface a
        # generic catch-all so the user sees something actionable.
        if not issues and raw_output.strip():
            issues.append(Issue(
                kind="isabelle_other",
                title="Isabelle reported a failure but no error lines parsed",
                raw=raw_output[:1500],
                fix_directive=(
                    "The runner exited non-zero but the output didn't "
                    "match any known error pattern. Read the raw text "
                    "above; consider extending the classifier in "
                    "forge.dashboard/web/feedback/isabelle.py if this "
                    "becomes common."
                ),
            ))

    if status == "passed":
        next_step = (
            "Z-Machine deadlock-freedom (and any R-series lemmas) verified. "
            "Continue refinement in Phase 1; Isabelle is the slowest "
            "backend, so re-run only on milestones."
        )
        # Surface a rich informational summary as a single issue so the
        # Feedback panel doesn't just say "Issues: None". The Issue
        # struct is reused as the carrier; consumers that filter by
        # `kind == "isabelle_summary"` can treat it as non-actionable.
        body = _build_success_body(success_stats, session_dir)
        if body:
            issues.append(Issue(
                kind="isabelle_summary",
                title="Verification summary",
                raw=body,
                fix_directive="No action — informational.",
            ))
    else:
        next_step = (
            "Fix the issues above. If a specific lemma did not close, "
            "either change the Java code so the auto tactic can dispatch, "
            "or hand-write a proof script in the Z-Machine session theory."
        )

    # Only surface "Files to review" when there are issues to act on.
    # On a passed run the list is misleading (nothing to fix).
    files_to_review = (
        collect_files_to_review(trace_data) if status != "passed" else []
    )

    return Feedback(
        phase="isabelle_verify",
        label=label,
        status=status,
        summary=summary,
        issues=issues,
        files_to_review=files_to_review,
        next_step=next_step,
    )
