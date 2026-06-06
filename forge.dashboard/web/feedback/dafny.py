"""Dafny (design-by-contract via Z3) feedback: classifier + builders."""
from __future__ import annotations

import re

from .base import (
    Issue,
    Feedback,
    TraceRef,
    collect_files_to_review,
    dafny_entry_to_trace_ref,
    find_dafny_entry,
)


# e.g. "LreController.dfy(42,15): Error: a postcondition could not be proved"
_DAFNY_ERROR_RE = re.compile(
    r"([^\s()]+\.dfy)\((\d+),\d+\):\s*(Error|Warning|Info):?\s*(.*)"
)


def _classify_dafny_error(message: str) -> tuple[str, str, str]:
    """Return (kind, title, fix_directive) for a Dafny error message."""
    lowered = message.lower()
    if "postcondition" in lowered and "could not" in lowered:
        return (
            "dafny_postcondition",
            "Postcondition could not be proved",
            "The method body cannot be shown to satisfy its `ensures` "
            "clause(s). Inspect the Java method linked below: either the "
            "body is missing a case, or the postcondition is too strong "
            "given the body's actual behaviour.",
        )
    if "precondition" in lowered and (
        "might not hold" in lowered or "could not" in lowered
    ):
        return (
            "dafny_precondition",
            "Precondition might not hold at call site",
            "A call violates the callee's `requires`. Inspect the call "
            "site in the linked Java method: add a guard before the call "
            "so the precondition is guaranteed, or weaken the callee's "
            "precondition to match reality.",
        )
    if "assertion" in lowered and "might not" in lowered:
        return (
            "dafny_assertion",
            "Assertion might not hold",
            "An inline assertion is not provable from the surrounding "
            "state. Strengthen the preceding guards/invariants, or remove "
            "the assertion if it encodes an assumption the model does "
            "not guarantee.",
        )
    if "index out of range" in lowered or "out of bounds" in lowered:
        return (
            "dafny_bounds",
            "Index/array access not within bounds",
            "An array or sequence access in the linked Java method lacks "
            "a bounds check. Add an explicit guard on the index before "
            "the access.",
        )
    if "division" in lowered and "zero" in lowered:
        return (
            "dafny_div_zero",
            "Possible division by zero",
            "A division in the linked Java method is not guarded against "
            "a zero divisor. Add an explicit guard or return a safe "
            "default when the divisor could be zero.",
        )
    if "decreases" in lowered:
        return (
            "dafny_termination",
            "Termination (decreases) could not be proved",
            "A loop or recursive call lacks a provable decreasing "
            "measure. Rewrite the loop so a bounded natural decreases "
            "each iteration.",
        )
    return (
        "dafny_other",
        f"Dafny error: {message[:80]}",
        "Inspect the linked Java code; the generated Dafny contract "
        "could not be verified. The exact verifier message above is the "
        "most specific guide.",
    )


def build_dafny_issues(raw_output: str,
                       dafny_mappings: list | None) -> list[Issue]:
    """Parse Dafny verifier output into Issue records with Java traceability."""
    issues: list[Issue] = []
    seen: set[tuple[str, int]] = set()
    for m in _DAFNY_ERROR_RE.finditer(raw_output):
        if m.group(3) != "Error":
            continue
        dfy_file, dafny_line, _, message = m.group(1), int(m.group(2)), m.group(3), m.group(4).strip()
        key = (dfy_file, dafny_line)
        if key in seen:
            continue
        seen.add(key)

        kind, title, fix = _classify_dafny_error(message)

        java_trace: list[TraceRef] = []
        if dafny_mappings:
            entry = find_dafny_entry(dafny_line, dafny_mappings)
            if entry:
                java_trace.append(dafny_entry_to_trace_ref(entry))

        raw = f"{dfy_file}({dafny_line},?): Error: {message}"
        issues.append(Issue(kind=kind, title=title, raw=raw,
                            fix_directive=fix, java_trace=java_trace))
    return issues


def build_dafny_feedback(
    label: str,
    status: str,
    summary: str,
    raw_output: str,
    dafny_mappings: list | None,
    trace_data: dict | None,
) -> Feedback:
    issues = build_dafny_issues(raw_output, dafny_mappings)

    if status == "passed":
        next_step = (
            "Dafny design-by-contract obligations are discharged. "
            "Continue refinement in Phase 1."
        )
    else:
        next_step = (
            "Read each issue above, follow the fix directive, edit the "
            "linked Java file, re-run Phase 5b (Dafny Generation), then "
            "Phase 6b (Dafny Verification)."
        )

    return Feedback(
        phase="dafny_verify",
        label=label,
        status=status,
        summary=summary,
        issues=issues,
        files_to_review=(
            collect_files_to_review(trace_data) if status != "passed" else []
        ),
        next_step=next_step,
    )
