"""FDR4 (CSP refinement) feedback: classifier, prescriptive directives,
counterexample → trace resolution, and the public ``build_fdr4_feedback``.
"""
from __future__ import annotations

import re
from pathlib import Path

from .base import (
    Issue,
    Feedback,
    TraceRef,
    collect_files_to_review,
    entry_to_trace_ref,
)


# ---------------------------------------------------------------------------
# CSP-line / element-name → trace lookup
# ---------------------------------------------------------------------------


def _enrich_csp_error(csp_line_text: str, csp_line: int | None,
                     trace_data: dict | None) -> list[TraceRef]:
    """Find trace entries that match a CSP-M error.

    Strategy 1: direct CSP line range match.
    Strategy 2: match element names referenced in the CSP line text.
    """
    if not trace_data:
        return []
    traces = trace_data.get("traces", [])

    matches: list[dict] = []
    if csp_line is not None:
        matches = [
            e for e in traces
            if e.get("csp_line_start") is not None
            and e.get("csp_line_end") is not None
            and e["csp_line_start"] <= csp_line <= e["csp_line_end"]
        ]

    if not matches and csp_line_text:
        element_names = {
            e["robochart_element"] for e in traces if "robochart_element" in e
        }
        referenced = [
            name for name in element_names
            if re.search(r"\b" + re.escape(name) + r"\b", csp_line_text)
        ]
        if referenced:
            matches = [
                e for e in traces if e.get("robochart_element") in referenced
            ]

    return [entry_to_trace_ref(e) for e in matches]


def _enrich_assertion(assert_str: str, counterexamples: list,
                     trace_data: dict | None) -> list[TraceRef]:
    """Extract state/event names from an assertion + counterexamples and look
    them up in the trace data."""
    if not trace_data:
        return []
    traces = trace_data.get("traces", [])
    element_names = {
        e["robochart_element"] for e in traces if "robochart_element" in e
    }

    referenced: set[str] = set()
    for ce in counterexamples:
        for ev in ce.get("trace", []):
            ev_str = str(ev)
            for name in element_names:
                if name in ev_str:
                    referenced.add(name)
    for name in element_names:
        if name in assert_str:
            referenced.add(name)

    matches = [e for e in traces if e.get("robochart_element") in referenced]
    return [entry_to_trace_ref(e) for e in matches]


# ---------------------------------------------------------------------------
# Counterexample → event/transition resolution
# ---------------------------------------------------------------------------


def _resolve_trace_events(ce: dict, event_map: dict[str, str]) -> list[str]:
    """Resolve a counterexample's event-ID trace to event names.

    FDR4 framed_json wraps the trace under ``implementation_behaviour``
    or ``specification_behaviour`` (integer IDs), or sometimes provides
    a top-level ``trace`` with string names. Resolve via event_map if
    possible; otherwise return strings as-is.
    """
    impl = ce.get("implementation_behaviour", {}) or {}
    spec = ce.get("specification_behaviour", {}) or {}
    raw_trace = impl.get("trace") or spec.get("trace") or ce.get("trace", [])
    resolved: list[str] = []
    for ev in raw_trace:
        name = event_map.get(str(ev), str(ev)) if event_map else str(ev)
        resolved.append(name)
    return resolved


def _resolve_error_event(ce: dict, event_map: dict[str, str]) -> str:
    """Extract the (FDR4-identified) single 'culprit' event from a
    counterexample, resolved to a name."""
    impl = ce.get("implementation_behaviour", {}) or {}
    eid = impl.get("error_event")
    if eid is None:
        return ""
    return event_map.get(str(eid), str(eid))


def _transitions_on_event(event_name: str,
                         trace_data: dict | None) -> list[dict]:
    """Return Transition entries in trace_data whose ``trigger_event``
    matches the given event name."""
    if not trace_data or not event_name:
        return []
    out = []
    for entry in trace_data.get("traces", []):
        if entry.get("robochart_type") != "Transition":
            continue
        trig = entry.get("trigger_event", "") or ""
        if trig == event_name or event_name.endswith("." + trig) or trig.endswith("." + event_name):
            out.append(entry)
    return out


def _autonomous_transitions(trace_data: dict | None) -> list[dict]:
    """Return Transition entries with no trigger event (guard-only)."""
    if not trace_data:
        return []
    return [
        e for e in trace_data.get("traces", [])
        if e.get("robochart_type") == "Transition"
        and not (e.get("trigger_event") or "").strip()
    ]


# ---------------------------------------------------------------------------
# Prescriptive directives per assertion class
# ---------------------------------------------------------------------------


def _render_trace_story(trace_events: list[str]) -> str:
    """Fix 2: turn a CSP-M event sequence into a one-line natural-language
    story for the fix directive.

    Domain events render as ``operator/system event \`<name>\```; the
    distinguished ``tock`` is rendered as ``one clock tick passes``.
    Consecutive ``tock``s are collapsed.
    """
    if not trace_events:
        return ""
    parts: list[str] = []
    tock_run = 0
    for ev in trace_events:
        if ev == "tock":
            tock_run += 1
            continue
        if tock_run:
            parts.append(
                f"{tock_run} clock tick{'s pass' if tock_run > 1 else ' passes'}"
            )
            tock_run = 0
        parts.append(f"event `{ev}` fires")
    if tock_run:
        parts.append(
            f"{tock_run} clock tick{'s pass' if tock_run > 1 else ' passes'}"
        )
    if not parts:
        return ""
    if len(parts) == 1:
        return f"Trace: {parts[0]}."
    return "Trace: " + "; then ".join(parts) + "."


def _prescriptive_deadlock(counterexamples: list,
                          event_map: dict[str, str],
                          trace_data: dict | None) -> str:
    """Produce a counterexample-specific deadlock directive, or the
    generic one if we can't narrow further."""
    if not counterexamples:
        return ""
    # Last event in the first counterexample's trace = probable trigger
    # that led to the deadlocked state.
    trace = _resolve_trace_events(counterexamples[0], event_map)
    if not trace:
        return ""
    story = _render_trace_story(trace)
    last = trace[-1]
    handlers = _transitions_on_event(last, trace_data)
    if not handlers:
        body = (
            f"The counterexample ends with event `{last}`. "
            f"No transition in the RoboChart model handles `{last}` — "
            f"some mode reaches a state where this event occurs without "
            f"a matching branch. Add an `instanceof` check for the "
            f"event type in the corresponding mode block of step()."
        )
        return (story + "\n\n" + body) if story else body
    states = sorted({h.get("source_state", "?") for h in handlers})
    body = (
        f"The counterexample ends with event `{last}`. Transitions "
        f"handling `{last}` exist (from state(s): {', '.join(states)}), "
        f"but the counterexample reaches a state where none of them "
        f"fire. Check whether the reached state is one of these "
        f"source states and, if so, whether the guards block every "
        f"branch; otherwise add a branch for `{last}` in the reached "
        f"state."
    )
    return (story + "\n\n" + body) if story else body


def _prescriptive_nondeterminism(counterexamples: list,
                                 event_map: dict[str, str],
                                 trace_data: dict | None) -> str:
    """Narrow the nondeterminism directive by identifying the ambiguous
    event and the transitions it simultaneously enables."""
    if not counterexamples:
        return ""
    err_event = _resolve_error_event(counterexamples[0], event_map)
    if not err_event:
        # Fallback: last event of the trace.
        trace = _resolve_trace_events(counterexamples[0], event_map)
        err_event = trace[-1] if trace else ""
    if not err_event:
        return ""

    handlers = _transitions_on_event(err_event, trace_data)
    by_source: dict[str, list[dict]] = {}
    for h in handlers:
        src = h.get("source_state", "?")
        by_source.setdefault(src, []).append(h)
    ambiguous = [(src, hs) for src, hs in by_source.items() if len(hs) > 1]
    if not ambiguous:
        return ""
    parts = []
    for src, hs in ambiguous:
        tgts = ", ".join(
            f"{h.get('robochart_element', '?')}→{h.get('target_state', '?')}"
            for h in hs
        )
        parts.append(f"state `{src}`: {tgts}")
    return (
        f"Event `{err_event}` is ambiguous — at "
        + "; ".join(parts)
        + f". These transitions all fire on `{err_event}` with "
        "overlapping (or no) guards. Make the guards mutually "
        "exclusive, or reorder the if-else so the higher-priority "
        "branch is first."
    )


def _prescriptive_divergence(trace_data: dict | None) -> str:
    """Identify autonomous (guard-only) transitions that may form the
    divergent cycle."""
    autos = _autonomous_transitions(trace_data)
    if not autos:
        return ""
    names = [t.get("robochart_element", "?") for t in autos]
    # Find chains: target_state of one = source_state of another (both autos)
    by_source = {t.get("source_state", "?"): t for t in autos}
    chains = []
    for t in autos:
        tgt = t.get("target_state", "")
        if tgt in by_source and by_source[tgt] is not t:
            chains.append((t.get("robochart_element", "?"),
                          by_source[tgt].get("robochart_element", "?")))
    if chains:
        chain_desc = "; ".join(f"{a} → {b}" for a, b in chains)
        return (
            f"Guard-only transitions found that can chain: {chain_desc}. "
            "Break the cycle by adding an event trigger on at least one "
            "link, or strengthen the guards so the chain cannot re-enter."
        )
    return (
        f"Autonomous (guard-only) transitions exist: {', '.join(names)}. "
        "If any of them form a cycle, divergence results. Add an event "
        "trigger to at least one transition in the cycle."
    )


# ---------------------------------------------------------------------------
# Issue builders
# ---------------------------------------------------------------------------


def _fdr4_assertion_issue(assertion: dict,
                          trace_data: dict | None,
                          event_map: dict[str, str] | None = None) -> Issue:
    """Classify an FDR4 failed assertion and build an Issue.

    If ``event_map`` is provided, the directive is enriched with
    counterexample-specific information (which event is the culprit,
    which transitions are ambiguous, etc.). Falls back to the generic
    directive when counterexample data is insufficient.
    """
    assert_str = assertion.get("assertion_string", "unknown")
    counterexamples = assertion.get("counterexamples", [])
    event_map = event_map or {}

    # Build a human-readable trace description (resolved to event names).
    traces_list = []
    for ce in counterexamples:
        resolved = _resolve_trace_events(ce, event_map)
        if resolved:
            traces_list.append(" -> ".join(resolved))
    trace_desc = "; ".join(traces_list) if traces_list else "(no counterexample trace)"

    lowered = assert_str.lower()
    base_fix = ""
    prescriptive = ""
    if "deadlock" in lowered:
        kind, title = "deadlock", "Deadlock-freedom violated"
        base_fix = (
            "One or more states have no reachable outgoing transition. "
            "Every mode in the controller's step() method must have "
            "at least one reachable branch out."
        )
        prescriptive = _prescriptive_deadlock(
            counterexamples, event_map, trace_data)
    elif "divergence" in lowered:
        kind, title = "divergence", "Divergence-freedom violated"
        base_fix = (
            "The state machine has an infinite internal loop. Guard-only "
            "(autonomous) transitions must not form a cycle with no "
            "event-triggered edge."
        )
        prescriptive = _prescriptive_divergence(trace_data)
    elif "deterministic" in lowered:
        kind, title = "nondeterminism", "Determinism violated"
        base_fix = (
            "Multiple transitions from the same mode are enabled by the "
            "same event/guard combination. NOTE: nondeterminism from "
            "overlapping guards is often EXPECTED "
            "(see forge.assets/prompts/fdr4_system.txt) — only fix "
            "if the assertion is not in config.phases.fdr4.expected_failures."
        )
        prescriptive = _prescriptive_nondeterminism(
            counterexamples, event_map, trace_data)
    else:
        kind, title = "assertion_failed", f"Assertion failed: {assert_str}"
        base_fix = (
            "Inspect the counterexample trace and compare against the "
            "intended mode transitions in the controller."
        )

    if prescriptive:
        fix = prescriptive + "\n\n" + base_fix
    else:
        fix = base_fix

    raw = f"{assert_str}\nCounterexample: {trace_desc}"
    java_trace = _enrich_assertion(assert_str, counterexamples, trace_data)
    return Issue(kind=kind, title=title, raw=raw,
                 fix_directive=fix, java_trace=java_trace)


def _fdr4_parse_error_issue(error_msg: str, csp_file: Path | None,
                            trace_data: dict | None) -> Issue:
    """Build an Issue for a CSP-M parse/type error."""
    csp_line = None
    m = re.search(r"\.csp:(\d+):", error_msg)
    if m:
        csp_line = int(m.group(1))

    csp_line_text = ""
    if csp_line is not None and csp_file and csp_file.exists():
        try:
            lines = csp_file.read_text(encoding="utf-8").splitlines()
            if 1 <= csp_line <= len(lines):
                csp_line_text = lines[csp_line - 1]
        except OSError:
            pass

    java_trace = _enrich_csp_error(csp_line_text, csp_line, trace_data)
    fix = (
        "CSP-M source generated by the pipeline failed to parse. This is "
        "usually an EGL template issue or a RoboChart model shape the "
        "CSP generator does not support. Inspect the offending .rct / "
        ".csp lines and correlate with the Java element above."
    )
    return Issue(
        kind="parse_error",
        title="CSP-M parse error",
        raw=error_msg.strip(),
        fix_directive=fix,
        java_trace=java_trace,
    )


def _analyse_csp_structure(csp_file: Path | None) -> list[Issue]:
    """Scan the generated CSP-M for structural smells (empty channels,
    STOP states) that usually indicate missing controller logic."""
    if not csp_file or not csp_file.exists():
        return []
    try:
        text = csp_file.read_text(encoding="utf-8")
    except OSError:
        return []

    issues: list[Issue] = []
    stop_states: list[str] = []
    empty_channels = False

    for line in text.splitlines():
        stripped = line.strip()
        if stripped == "channel":
            empty_channels = True
        m = re.match(r"^(\w+)\s*=\s*STOP\s*$", stripped)
        if m:
            stop_states.append(m.group(1))

    if empty_channels:
        issues.append(Issue(
            kind="structure_empty_channels",
            title="Generated state machine declares no events",
            raw="channel declaration in CSP-M is empty",
            fix_directive=(
                "The controller defines no event types that trigger mode "
                "transitions. Add a sealed InputEvent hierarchy with at "
                "least one record per triggering event, and use "
                "instanceof checks in step()."
            ),
        ))
    if stop_states:
        issues.append(Issue(
            kind="structure_stop_states",
            title=f"Deadlocked states in generated CSP-M: {', '.join(stop_states)}",
            raw="Process(es) equal to STOP with no outgoing transitions",
            fix_directive=(
                "Each listed mode has no transition logic. In the "
                "controller's step() method, add the inner if-else "
                "branches that transition out of these modes."
            ),
        ))
    return issues


# ---------------------------------------------------------------------------
# Public builder
# ---------------------------------------------------------------------------


def build_fdr4_feedback(
    label: str,
    status: str,
    summary: str,
    failed_assertions: list,
    errors: list,
    csp_file: Path | None,
    trace_data: dict | None,
    expected_failures: list[dict],
    event_map: dict[str, str] | None = None,
) -> Feedback:
    issues: list[Issue] = []

    for err in errors:
        if isinstance(err, dict) and err.get("kind") and err.get("title"):
            issues.append(Issue(
                kind=err["kind"],
                title=err["title"],
                raw=err.get("raw", "") or err.get("error", ""),
                fix_directive=err.get("fix_directive", "") or err.get("error", ""),
            ))
            continue
        msg = err.get("error") if isinstance(err, dict) else str(err)
        if msg:
            issues.append(_fdr4_parse_error_issue(msg, csp_file, trace_data))

    for assertion in failed_assertions:
        issues.append(
            _fdr4_assertion_issue(assertion, trace_data, event_map)
        )

    if expected_failures:
        note = (
            "The following failures are marked EXPECTED in "
            "config.phases.fdr4.expected_failures and are reported for "
            "information only: "
            + "; ".join(a.get("assertion_string", "?") for a in expected_failures)
            + "."
        )
        issues.append(Issue(
            kind="expected_failure",
            title="Expected failure (informational)",
            raw=note,
            fix_directive="No action required.",
        ))

    if status == "passed" and not errors and not failed_assertions:
        issues.extend(_analyse_csp_structure(csp_file))

    if status == "passed":
        next_step = (
            "Proceed to Phase 6b (Dafny Verification). On a clean FDR4 pass "
            "you may continue refining features in Phase 1."
        )
    else:
        next_step = (
            "Read each issue above, follow the fix directive, edit the "
            "linked Java file, and re-run Phase 6a (FDR4 Verification)."
        )

    return Feedback(
        phase="fdr4",
        label=label,
        status=status,
        summary=summary,
        issues=issues,
        files_to_review=(
            collect_files_to_review(trace_data) if status != "passed" else []
        ),
        next_step=next_step,
    )
