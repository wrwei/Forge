"""Schema, trace loading, diff, serialisation, and the common writer.

Per-phase modules (``fdr4``, ``dafny``, ``isabelle``, ``gradle``) build
``Issue`` / ``Feedback`` records and hand them to ``write_feedback`` here.
"""
from __future__ import annotations

import json
import logging
from dataclasses import asdict, dataclass, field
from pathlib import Path

logger = logging.getLogger(__name__)


# ---------------------------------------------------------------------------
# Schema
# ---------------------------------------------------------------------------


@dataclass
class TraceRef:
    """A single Java source reference derived from trace_full.json."""
    file: str = ""
    line_start: int | None = None
    line_end: int | None = None
    element: str = ""
    robochart_type: str = ""
    robochart_element: str = ""
    requirement_ids: list[str] = field(default_factory=list)


@dataclass
class Issue:
    """One actionable issue surfaced by a phase.

    ``seen_in_runs`` is the diff-aware counter: 1 = first time this run,
    2+ = the same issue recurred across that many consecutive runs. A
    counter >= ``THRASHING_THRESHOLD`` means the agent's fix strategy is
    not working and a different approach is needed.
    """
    kind: str
    title: str
    raw: str = ""
    fix_directive: str = ""
    java_trace: list[TraceRef] = field(default_factory=list)
    seen_in_runs: int = 1


# When an issue has recurred this many runs in a row, surface a prominent
# warning so the agent doesn't thrash with the same fix.
THRASHING_THRESHOLD = 3


@dataclass
class Feedback:
    """Full feedback record for a phase run."""
    phase: str
    label: str
    status: str
    summary: str = ""
    issues: list[Issue] = field(default_factory=list)
    files_to_review: list[dict] = field(default_factory=list)
    next_step: str = ""
    # Diff against the previous run (populated by ``write_feedback``).
    new_count: int = 0
    recurring_count: int = 0
    resolved_count: int = 0
    resolved_issue_titles: list[str] = field(default_factory=list)


# ---------------------------------------------------------------------------
# Trace loading and lookup
# ---------------------------------------------------------------------------


def load_trace(t2m_output: Path) -> dict | None:
    """Load ``trace_full.json`` from the T2M output directory."""
    trace_path = t2m_output / "trace_full.json"
    if not trace_path.exists():
        return None
    try:
        return json.loads(trace_path.read_text(encoding="utf-8"))
    except (json.JSONDecodeError, OSError) as exc:
        logger.warning("Failed to load %s: %s", trace_path, exc)
        return None


def load_dafny_trace(t2m_output: Path) -> list | None:
    """Load ``trace_dafny.json`` mappings."""
    trace_path = t2m_output / "trace_dafny.json"
    if not trace_path.exists():
        return None
    try:
        data = json.loads(trace_path.read_text(encoding="utf-8"))
        return data.get("mappings", [])
    except (json.JSONDecodeError, OSError) as exc:
        logger.warning("Failed to load %s: %s", trace_path, exc)
        return None


def entry_to_trace_ref(entry: dict) -> TraceRef:
    """Convert a ``trace_full.json`` entry into a TraceRef."""
    return TraceRef(
        file=entry.get("java_file") or entry.get("java_source_file", ""),
        line_start=entry.get("java_line_start"),
        line_end=entry.get("java_line_end"),
        element=entry.get("java_element", ""),
        robochart_type=entry.get("robochart_type", ""),
        robochart_element=entry.get("robochart_element", ""),
        requirement_ids=list(entry.get("requirement_ids", [])),
    )


def dafny_entry_to_trace_ref(entry: dict) -> TraceRef:
    return TraceRef(
        file=entry.get("java_file", ""),
        line_start=entry.get("java_line_start"),
        line_end=entry.get("java_line_end"),
        element=entry.get("dafny_element", ""),
        robochart_type=entry.get("dafny_type", ""),
        robochart_element=entry.get("dafny_element", ""),
    )


def find_dafny_entry(dafny_line: int, mappings: list) -> dict | None:
    """Find the narrowest enclosing Dafny-trace entry for a line."""
    best = None
    best_span = float("inf")
    for entry in mappings:
        start, end = entry.get("dafny_line_start"), entry.get("dafny_line_end")
        if start is None or end is None:
            continue
        start, end = int(start), int(end)
        if start <= dafny_line <= end:
            span = end - start
            if span < best_span:
                best, best_span = entry, span
    return best


def collect_files_to_review(trace_data: dict | None) -> list[dict]:
    if not trace_data:
        return []
    file_reqs: dict[str, set] = {}
    for entry in trace_data.get("traces", []):
        jf = entry.get("java_file", "")
        if not jf:
            continue
        short = Path(jf).name
        file_reqs.setdefault(short, set()).update(entry.get("requirement_ids", []))
    return [
        {"file": f, "requirement_ids": sorted(reqs)}
        for f, reqs in sorted(file_reqs.items())
    ]


# ---------------------------------------------------------------------------
# Requirement-trace amplification (Fix 1)
#
# The codegen agent emits result_codegen.json mapping requirement IDs to
# Java files / elements. The downstream verifiers (FDR4, Dafny, Isabelle)
# produce failures pinned to Java files / lines. Joining the two lets a
# verification fix-directive identify the requirement(s) the failure
# violates, which is the single biggest improvement to feedback richness
# we can make without changing the LLM workflow itself.
# ---------------------------------------------------------------------------


def load_codegen_trace(generated_project: Path) -> list[dict] | None:
    """Load ``result_codegen.json`` mappings from the generated project.

    Returns a list of {requirement_gid, java_file, java_element,
    element_type} dicts, or ``None`` if the artefact is absent / malformed.
    """
    trace_path = generated_project / "result_codegen.json"
    if not trace_path.exists():
        return None
    try:
        data = json.loads(trace_path.read_text(encoding="utf-8"))
        return data.get("codegen_trace", [])
    except (json.JSONDecodeError, OSError) as exc:
        logger.warning("Failed to load %s: %s", trace_path, exc)
        return None


def load_requirements(case_study_dir: Path) -> dict[str, dict] | None:
    """Load ``requirement_all.json`` keyed by requirement ID.

    Returns a dict mapping each requirement's ``id`` (or ``gid``) to the
    full requirement record (incl. ``name``, ``description``, ``types``,
    ``priority``), or ``None`` if the file is absent / malformed.
    """
    req_path = case_study_dir / "requirements" / "requirement_all.json"
    if not req_path.exists():
        return None
    try:
        data = json.loads(req_path.read_text(encoding="utf-8"))
    except (json.JSONDecodeError, OSError) as exc:
        logger.warning("Failed to load %s: %s", req_path, exc)
        return None
    reqs = data if isinstance(data, list) else data.get("requirements", [])
    out: dict[str, dict] = {}
    for r in reqs:
        if not isinstance(r, dict):
            continue
        rid = r.get("id") or r.get("gid")
        if rid:
            out[rid] = r
    return out


def lookup_requirements_for_java(
    java_file: str,
    java_element: str | None,
    codegen_trace: list[dict] | None,
) -> list[str]:
    """Reverse-map a (java_file, element) pair to requirement IDs.

    The match is on the filename component (so absolute vs.\\ relative
    paths interoperate) plus, when ``java_element`` is given, exact name.
    Returns an alphabetically-sorted list with duplicates removed.
    """
    if not codegen_trace or not java_file:
        return []
    target_name = Path(java_file).name.lower()
    found: set[str] = set()
    for entry in codegen_trace:
        ef = entry.get("java_file", "")
        if not ef:
            continue
        if Path(ef).name.lower() != target_name:
            continue
        if java_element and entry.get("java_element") != java_element:
            continue
        rid = entry.get("requirement_gid")
        if rid:
            found.add(rid)
    return sorted(found)


def amplify_directive_with_requirements(
    fix_directive: str,
    requirement_ids: list[str],
    requirements: dict[str, dict] | None,
    max_lines: int = 4,
) -> str:
    """Append a ``Related requirements`` block to ``fix_directive``.

    For each requirement ID, looks up its description from
    ``requirements`` (if loaded) and includes a one-line restatement.
    Truncates to ``max_lines`` to keep the directive scannable.
    """
    if not requirement_ids:
        return fix_directive
    lines = ["", "Related requirements:"]
    for rid in requirement_ids[:max_lines]:
        if requirements and rid in requirements:
            r = requirements[rid]
            name = r.get("name", "")
            desc = (r.get("description") or "").strip().splitlines()[0]
            if len(desc) > 160:
                desc = desc[:157] + "..."
            label = f"{rid}" + (f" ({name})" if name else "")
            lines.append(f"  - {label}: {desc}" if desc else f"  - {label}")
        else:
            lines.append(f"  - {rid}")
    if len(requirement_ids) > max_lines:
        lines.append(f"  - ... ({len(requirement_ids) - max_lines} more)")
    return fix_directive.rstrip() + "\n" + "\n".join(lines)


def populate_trace_refs_with_requirements(
    refs: list[TraceRef],
    codegen_trace: list[dict] | None,
) -> None:
    """Mutate each ``TraceRef`` to fill its ``requirement_ids`` field.

    Match strategy: filename + element name, falling back to filename
    only when the element isn't recorded.
    """
    if not codegen_trace:
        return
    for ref in refs:
        if ref.requirement_ids:
            continue  # already set upstream
        ids = lookup_requirements_for_java(ref.file, ref.element or None, codegen_trace)
        if not ids and ref.element:
            ids = lookup_requirements_for_java(ref.file, None, codegen_trace)
        ref.requirement_ids = ids


# ---------------------------------------------------------------------------
# Serialisation, diff, rendering, writer
# ---------------------------------------------------------------------------


def _ref_to_dict(ref: TraceRef) -> dict:
    return asdict(ref)


def _issue_to_dict(issue: Issue) -> dict:
    return {
        "kind": issue.kind,
        "title": issue.title,
        "raw": issue.raw,
        "fix_directive": issue.fix_directive,
        "java_trace": [_ref_to_dict(r) for r in issue.java_trace],
        "seen_in_runs": issue.seen_in_runs,
    }


def _issue_key(issue: Issue) -> str:
    """Stable identity for an issue across runs.

    Excludes line numbers so a fix that shifts lines doesn't mask a
    recurring issue. Uses (kind, title, file + element) when trace
    data is present; falls back to the raw message otherwise.
    """
    base = f"{issue.kind}|{issue.title}"
    if issue.java_trace:
        ref = issue.java_trace[0]
        base += f"|{ref.file}|{ref.element}"
    else:
        base += f"|{issue.raw[:200]}"
    return base


def _load_previous_feedback(json_path: Path) -> dict | None:
    """Load the previous run's feedback JSON, if any."""
    if not json_path.exists():
        return None
    try:
        return json.loads(json_path.read_text(encoding="utf-8"))
    except (json.JSONDecodeError, OSError):
        return None


def _apply_diff(fb: Feedback, previous: dict | None) -> None:
    """Compute new/recurring/resolved sets in-place on ``fb`` by comparing
    against the previous run's feedback dict.

    Mutates ``fb.issues`` (bumps ``seen_in_runs`` on recurring issues) and
    populates ``fb.new_count``, ``fb.recurring_count``, ``fb.resolved_count``,
    ``fb.resolved_issue_titles``.
    """
    if previous is None:
        fb.new_count = len(fb.issues)
        fb.recurring_count = 0
        fb.resolved_count = 0
        return

    prev_issues_by_key: dict[str, dict] = {}
    for raw in previous.get("issues", []):
        # Reconstruct a minimal Issue just to call _issue_key
        trace_refs = [
            TraceRef(**{k: v for k, v in tr.items()
                        if k in TraceRef.__dataclass_fields__})
            for tr in raw.get("java_trace", [])
        ]
        prev = Issue(
            kind=raw.get("kind", ""),
            title=raw.get("title", ""),
            raw=raw.get("raw", ""),
            java_trace=trace_refs,
            seen_in_runs=int(raw.get("seen_in_runs", 1)),
        )
        prev_issues_by_key[_issue_key(prev)] = raw

    current_keys: set[str] = set()
    new_count = 0
    recurring_count = 0
    for issue in fb.issues:
        key = _issue_key(issue)
        current_keys.add(key)
        if key in prev_issues_by_key:
            prev_raw = prev_issues_by_key[key]
            issue.seen_in_runs = int(prev_raw.get("seen_in_runs", 1)) + 1
            recurring_count += 1
        else:
            issue.seen_in_runs = 1
            new_count += 1

    resolved_titles: list[str] = []
    for key, prev_raw in prev_issues_by_key.items():
        if key not in current_keys:
            title = prev_raw.get("title", "")
            if title:
                resolved_titles.append(title)

    fb.new_count = new_count
    fb.recurring_count = recurring_count
    fb.resolved_count = len(resolved_titles)
    fb.resolved_issue_titles = resolved_titles


def _format_trace_ref_md(ref: TraceRef) -> str:
    parts = []
    if ref.robochart_type or ref.robochart_element:
        parts.append(
            f"  - RoboChart: {ref.robochart_type} `{ref.robochart_element}`"
        )
    if ref.file:
        short = Path(ref.file).name
        loc = f"`{short}`"
        if ref.line_start and ref.line_end:
            loc += f":{ref.line_start}-{ref.line_end}"
        elif ref.line_start:
            loc += f":{ref.line_start}"
        elem = f" ({ref.element})" if ref.element else ""
        parts.append(f"  - Java: {loc}{elem}")
    if ref.requirement_ids:
        parts.append(
            f"  - Requirement(s): {', '.join(ref.requirement_ids)}"
        )
    return "\n".join(parts)


def _render_markdown(fb: Feedback) -> str:
    status_badge = {
        "passed": "PASSED",
        "failed": "FAILED",
        "stopped": "STOPPED",
    }.get(fb.status, fb.status.upper())

    lines = [
        f"# {fb.label} — {status_badge}",
        "",
        "## Summary",
        fb.summary or "(no summary)",
    ]

    has_diff = (fb.new_count + fb.recurring_count + fb.resolved_count) > 0
    thrashing_issues = [
        i for i in fb.issues if i.seen_in_runs >= THRASHING_THRESHOLD
    ]

    if has_diff or thrashing_issues:
        lines.append("")
        lines.append("## Run history")
        if thrashing_issues:
            titles = ", ".join(f"`{i.title}`" for i in thrashing_issues)
            lines.append(
                f"**WARNING — thrashing detected.** "
                f"{len(thrashing_issues)} issue(s) have recurred "
                f"{THRASHING_THRESHOLD}+ runs in a row despite fixes: "
                f"{titles}. Previous fix strategies are not working — "
                "change approach (e.g. re-read the originating "
                "requirement, or escalate to the user)."
            )
            lines.append("")
        lines.append(
            f"- New this run: {fb.new_count}"
        )
        lines.append(
            f"- Recurring from previous run: {fb.recurring_count}"
        )
        lines.append(
            f"- Resolved since previous run: {fb.resolved_count}"
        )
        if fb.resolved_issue_titles:
            lines.append("")
            lines.append("**Resolved issue titles**")
            for t in fb.resolved_issue_titles:
                lines.append(f"- {t}")

    lines.append("")
    lines.append("## Issues")

    if not fb.issues:
        lines.append("None.")
        lines.append("")
    else:
        for i, issue in enumerate(fb.issues, 1):
            history_badge = ""
            if issue.seen_in_runs >= THRASHING_THRESHOLD:
                history_badge = (
                    f" [RECURRING x{issue.seen_in_runs} — fix strategy "
                    "failing]"
                )
            elif issue.seen_in_runs >= 2:
                history_badge = f" [recurring x{issue.seen_in_runs}]"
            lines.append(
                f"### Issue {i}: {issue.kind} — {issue.title}{history_badge}"
            )
            if issue.raw:
                lines.append("")
                lines.append("**Raw**")
                lines.append("```")
                lines.append(issue.raw.strip())
                lines.append("```")
            if issue.java_trace:
                lines.append("")
                lines.append("**Java trace**")
                for ref in issue.java_trace:
                    rendered = _format_trace_ref_md(ref)
                    if rendered:
                        lines.append(rendered)
            if issue.fix_directive:
                lines.append("")
                lines.append("**Fix directive**")
                lines.append(issue.fix_directive)
            lines.append("")

    lines.append("## Files to review")
    if not fb.files_to_review:
        lines.append("(none identified)")
    else:
        for entry in fb.files_to_review:
            reqs = entry.get("requirement_ids", [])
            req_str = f" (requirements: {', '.join(reqs)})" if reqs else ""
            lines.append(f"- {entry['file']}{req_str}")

    lines.append("")
    lines.append("## Next step")
    lines.append(fb.next_step or "(no recommendation)")
    lines.append("")
    return "\n".join(lines)


def write_feedback(corrections_dir: Path, fb: Feedback) -> tuple[Path, Path]:
    """Write ``post_<phase>.md`` and ``post_<phase>.json`` into
    ``corrections_dir`` and return the two paths.

    Reads the previous JSON (if any) before writing to compute
    diff-awareness: issues that recur have their ``seen_in_runs``
    counter bumped; new and resolved sets are recorded on ``fb``.
    """
    corrections_dir.mkdir(parents=True, exist_ok=True)
    md_path = corrections_dir / f"post_{fb.phase}.md"
    json_path = corrections_dir / f"post_{fb.phase}.json"

    previous = _load_previous_feedback(json_path)
    _apply_diff(fb, previous)

    md_path.write_text(_render_markdown(fb), encoding="utf-8")

    payload = {
        "phase": fb.phase,
        "label": fb.label,
        "status": fb.status,
        "summary": fb.summary,
        "new_count": fb.new_count,
        "recurring_count": fb.recurring_count,
        "resolved_count": fb.resolved_count,
        "resolved_issue_titles": fb.resolved_issue_titles,
        "issues": [_issue_to_dict(i) for i in fb.issues],
        "files_to_review": fb.files_to_review,
        "next_step": fb.next_step,
    }
    json_path.write_text(
        json.dumps(payload, indent=2, ensure_ascii=False),
        encoding="utf-8",
    )
    logger.info("Wrote feedback: %s, %s", md_path, json_path)
    return md_path, json_path
