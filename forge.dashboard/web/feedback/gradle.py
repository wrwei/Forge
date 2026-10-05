"""Gradle-driven phase feedback (T2M / M2M / M2T / dafny_gen / isabelle_gen)
plus the structural-linter preflight."""
from __future__ import annotations

import json
import re
import xml.etree.ElementTree as ET
from pathlib import Path

from .base import (
    Issue,
    Feedback,
    TraceRef,
    collect_files_to_review,
)


# Path resolution: gradle.py lives at
#   <repo>/forge.dashboard/web/feedback/gradle.py
# so the repo root is parents[3] and the model XMI is at a fixed offset.
_REPO_ROOT = Path(__file__).resolve().parents[3]
_MODEL_XMI = _REPO_ROOT / "forge.transformations" / "output" / "robochart_model.xmi"
_GENERATED_PROJECT = _REPO_ROOT / "java.generated.project"


def _summarise_robochart_model(xmi_path: Path) -> dict | None:
    """Return {state_count, transition_count, machine_name, states} from the
    first ``<machines>`` element in the RoboChart model XMI, or None on
    parse error / missing file."""
    if not xmi_path.exists():
        return None
    try:
        tree = ET.parse(xmi_path)
    except (ET.ParseError, OSError):
        return None
    root = tree.getroot()
    # The XMI uses default namespaces and an xsi:type attribute on each node.
    # We search for <machines> elements regardless of namespace.
    xsi_type = "{http://www.w3.org/2001/XMLSchema-instance}type"
    machines = [e for e in root.iter() if e.tag.endswith("machines")]
    if not machines:
        return None
    machine = machines[0]
    states: list[str] = []
    transition_count = 0
    for child in machine:
        local = child.tag.rsplit("}", 1)[-1]
        if local == "nodes" and child.get(xsi_type, "").endswith(":State"):
            name = child.get("name")
            if name:
                states.append(name)
        elif local == "transitions":
            transition_count += 1
    # The XMI ETL emits exactly one t_init transition (initial → first state).
    # Subtract it so the count reflects user-defined transitions.
    user_transition_count = max(0, transition_count - 1)
    return {
        "machine_name": machine.get("name", "?"),
        "state_count": len(states),
        "states": states,
        "transition_count_raw": transition_count,
        "transition_count_user": user_transition_count,
    }


def _resolve_controller_path(
    model_summary: dict | None,
) -> Path | None:
    """Locate the controller .java file generically.

    Strategy: if the RoboChart model summary names a state machine,
    look for ``<MachineName>.java`` under the generated source tree.
    Otherwise fall back to any ``*Controller.java`` (sorted for
    deterministic selection across runs).
    """
    src_root = _GENERATED_PROJECT / "src" / "main" / "java"
    if not src_root.exists():
        return None
    if model_summary and model_summary.get("machine_name"):
        name = f"{model_summary['machine_name']}.java"
        matches = sorted(src_root.rglob(name))
        if matches:
            return matches[0]
    fallback = sorted(src_root.rglob("*Controller.java"))
    return fallback[0] if fallback else None


def _find_step_method_lines(controller_path: Path) -> tuple[int, int] | None:
    """Locate the ``step(...)`` method's line range in the controller.

    Best-effort: brace-counts from the line containing the signature.
    Returns None if the file is missing or no signature is found.
    """
    if not controller_path.exists():
        return None
    try:
        text = controller_path.read_text(encoding="utf-8")
    except OSError:
        return None
    sig = re.search(r"public\s+void\s+step\s*\(", text)
    if not sig:
        return None
    start_line = text.count("\n", 0, sig.start()) + 1
    # Walk forward, brace-counting from the first '{' after the signature.
    open_brace = text.find("{", sig.end())
    if open_brace == -1:
        return start_line, start_line
    depth = 0
    end = open_brace
    for i, ch in enumerate(text[open_brace:], start=open_brace):
        if ch == "{":
            depth += 1
        elif ch == "}":
            depth -= 1
            if depth == 0:
                end = i
                break
    end_line = text.count("\n", 0, end) + 1
    return start_line, end_line


_CASE_NOT_TREATED_RE = re.compile(r"case not treated:?\s*(.*)", re.IGNORECASE)
_COMPILE_ERROR_RE = re.compile(r"error:\s*(.+)", re.IGNORECASE)
# Epsilon EGL/EOL traceback frames look like:
#   at (path/to/template.egl@<line>:<col>-<line>:<col>)
# Their presence means the failure is a runtime error inside an Epsilon
# template, NOT a Java compile error.
_EGL_FRAME_RE = re.compile(r"\.(egl|eol|etl|epl)@\d+:\d+", re.IGNORECASE)
_RANGE_OOB_RE = re.compile(
    r"Range\s+\[\s*\d+\s*,\s*-?\d+\s*\)\s+out of bounds for length 0",
    re.IGNORECASE,
)


def build_gradle_issues(phase_id: str, error_summary: str) -> list[Issue]:
    """Classify a gradle-phase stderr dump into Issue(s).

    Each gradle-based phase (t2m, m2m, m2t, dafny_gen) receives only raw
    stderr today. This classifier produces a single issue with a directive
    pointing at the most likely Java-side root cause.
    """
    if not error_summary.strip():
        return []

    m = _CASE_NOT_TREATED_RE.search(error_summary)
    if m:
        construct = m.group(1).strip() or "unknown construct"
        directive = (
            f"The ETL/EGL transformation does not handle the construct "
            f"`{construct}`. This is a CLAUDE.md rule violation: the Java "
            "code uses a language feature that the formal model "
            "extraction cannot represent. See "
            "forge.assets/prompts/java_codegen_rules.txt — common "
            "culprits: lambdas, streams, ternary expressions, "
            "pattern-matching instanceof, local variables in compute()."
        )
        return [Issue(
            kind="etl_unsupported_construct",
            title=f"ETL cannot handle: {construct}",
            raw=error_summary,
            fix_directive=directive,
        )]

    # EGL/EOL template runtime error — recognised by the .egl@line:col
    # traceback frames. Distinguish empty-list indexing (state machine has
    # too few states/transitions) from generic template errors.
    if _EGL_FRAME_RE.search(error_summary):
        # Only label as "empty state machine" if the Range OOB pattern is
        # present AND we can verify (via the XMI) that the model actually
        # has 0 user-defined transitions. If the XMI shows transitions
        # exist, the OOB came from a different empty list and the empty-
        # state-machine directive would be misleading — fall through.
        model_summary = _summarise_robochart_model(_MODEL_XMI)
        is_range_oob = bool(_RANGE_OOB_RE.search(error_summary))
        # Treat the empty-state-machine case as confirmed when:
        #   - the OOB pattern is present, AND
        #   - either the XMI is unavailable (best-effort guess),
        #     or the XMI shows 0 user-defined transitions.
        empty_state_machine = is_range_oob and (
            model_summary is None
            or model_summary["transition_count_user"] == 0
        )
        if empty_state_machine:
            # Resolve the controller file generically: prefer the
            # machine name from the model summary, fall back to any
            # *Controller.java under the generated source tree.
            controller = _resolve_controller_path(model_summary)
            step_lines = _find_step_method_lines(controller) if controller else None
            diag_lines = []
            if model_summary:
                diag_lines.append(
                    f"Model inspection ({_MODEL_XMI.name}):"
                )
                diag_lines.append(
                    f"  - Machine: {model_summary['machine_name']}"
                )
                diag_lines.append(
                    f"  - States: {model_summary['state_count']} "
                    f"({', '.join(model_summary['states']) or '—'})"
                )
                diag_lines.append(
                    f"  - User-defined transitions: "
                    f"{model_summary['transition_count_user']}"
                )
                diag_lines.append("")
            else:
                diag_lines.append(
                    f"Model inspection: {_MODEL_XMI.name} not found — "
                    "diagnosis is best-effort based on the error pattern."
                )
                diag_lines.append("")
            diag_lines.append(error_summary)
            raw_with_diag = "\n".join(diag_lines)

            directive = (
                "The Isabelle/EGL template tried to index into the controller's "
                "transitions list, but it is empty. "
            )
            if model_summary and model_summary["transition_count_user"] == 0:
                directive += (
                    f"The RoboChart model has {model_summary['state_count']} "
                    "states but 0 user-defined transitions. "
                )
            directive += (
                "The Java controller's step() method body is empty (Layer 2 "
                "skeleton). Fill in step() with the mode-nested if-else state "
                "machine (Layer 4) so each state has at least one outgoing "
                "transition, then re-run this phase."
            )

            java_trace: list[TraceRef] = []
            if controller and step_lines:
                java_trace.append(TraceRef(
                    file=str(controller.relative_to(_REPO_ROOT)).replace("\\", "/"),
                    line_start=step_lines[0],
                    line_end=step_lines[1],
                    element="step",
                ))

            return [Issue(
                kind="egl_empty_state_machine",
                title="EGL template indexed an empty states/transitions list",
                raw=raw_with_diag,
                fix_directive=directive,
                java_trace=java_trace,
            )]

        # Fall-through: any other EGL template runtime error (including
        # Range OOB cases where the XMI shows transitions exist).
        diag_lines = []
        if model_summary:
            diag_lines.append(
                f"Model inspection ({_MODEL_XMI.name}): "
                f"{model_summary['state_count']} states, "
                f"{model_summary['transition_count_user']} user-defined "
                "transitions."
            )
            diag_lines.append("")
        diag_lines.append(error_summary)
        return [Issue(
            kind="egl_template_error",
            title="EGL template runtime error",
            raw="\n".join(diag_lines),
            fix_directive=(
                "The EGL template raised a runtime error while generating "
                "output from the RoboChart model. This is not a Java compile "
                "error. Inspect the .egl@<line> reference in the trace and "
                "the model element at that point to identify the missing or "
                "malformed structure."
            ),
        )]

    m = _COMPILE_ERROR_RE.search(error_summary)
    if m:
        return [Issue(
            kind="java_compile_error",
            title="Java compilation failed",
            raw=error_summary,
            fix_directive=(
                "Fix the Java compile error. The pipeline cannot extract "
                "a model from code that does not compile."
            ),
        )]

    label_by_phase = {
        "compile": "gradle build of java.generated.project",
        "t2m": "Spoon discovery",
        "m2m": "ETL (Java EMF → RoboChart)",
        "m2t": "EGL / RCT / RoboChart CSP generator",
        "dafny_gen": "Dafny generator",
    }
    phase_label = label_by_phase.get(phase_id, phase_id)
    return [Issue(
        kind=f"{phase_id}_failure",
        title=f"{phase_label} failed",
        raw=error_summary,
        fix_directive=(
            f"{phase_label} raised an error. Inspect the raw message "
            "above and correlate with the Java code. If the message "
            "references a Spoon/EMF construct, the Java source may use "
            "a feature banned by java_codegen_rules.txt."
        ),
    )]


def build_gradle_phase_feedback(
    phase_id: str,
    label: str,
    status: str,
    summary: str,
    error_summary: str,
    trace_data: dict | None,
    lint_lines: list[str] | None = None,
) -> Feedback:
    if status == "passed":
        issues = []
        # Surface ETL `[deadlock-lint]` lines as advisory issues without
        # flipping status to failed. Each line is of the form
        #   [deadlock-lint] <stm>.<state>: <message>
        # We emit one Issue per line; the dashboard renders them as
        # warnings under a passed phase, and the user can act on them
        # (or ignore) before running the Isabelle proof.
        for raw in (lint_lines or []):
            after_prefix = raw.split("[deadlock-lint]", 1)[1].strip() if "[deadlock-lint]" in raw else raw
            state_loc = after_prefix.split(":", 1)[0].strip() if ":" in after_prefix else after_prefix
            issues.append(Issue(
                kind="deadlock_lint_warning",
                title=f"State without unconditional fallback: {state_loc}",
                raw=raw,
                fix_directive=(
                    "Add an `else { mode = <SameMode>; }` clause to the inner "
                    "if-else chain of this mode block so the state has at "
                    "least one bare-precondition outgoing transition. "
                    "Without it, the Isabelle deadlock_free proof will fail "
                    "(see CLAUDE.md \"Isabelle/UTP Z-Machine `deadlock_free` "
                    "proof\" gotcha)."
                ),
            ))
        next_step_by_phase = {
            "compile": "Java compiles. Proceed to Phase 2b (Coverage Check) or Phase 2c (Preflight).",
            "t2m": "Proceed to Phase 4 (M2M — ETL Transformation).",
            "m2m": "Proceed to Phase 5a (CSP Generation) and/or 5b (Dafny Generation).",
            "m2t": "Proceed to Phase 6a (FDR4 Verification).",
            "dafny_gen": "Proceed to Phase 6b (Dafny Verification).",
        }
        next_step = next_step_by_phase.get(phase_id, "Proceed to the next phase.")
        # No files to review on a clean pass — the list is derived from
        # trace_data (last T2M run) and can be stale after a case-study
        # switch. Same convention applied across all phases.
        files_to_review = []
    else:
        issues = build_gradle_issues(phase_id, error_summary)
        next_step = (
            "Read the issue above, follow the fix directive, edit the "
            "Java source, and re-run this phase."
        )
        files_to_review = collect_files_to_review(trace_data)
        # When the only issue is the empty-state-machine one, the trace-derived
        # file list (operation files, etc.) is misleading — the fix sits in
        # LreController.step(). Replace it with just the controller.
        if issues and all(i.kind == "egl_empty_state_machine" for i in issues):
            controller_files = sorted({
                ref.file
                for issue in issues
                for ref in issue.java_trace
                if ref.file
            })
            files_to_review = [
                {"file": Path(f).name, "requirement_ids": []}
                for f in controller_files
            ]

    return Feedback(
        phase=phase_id,
        label=label,
        status=status,
        summary=summary,
        issues=issues,
        files_to_review=files_to_review,
        next_step=next_step,
    )


def build_preflight_feedback(
    label: str,
    status: str,
    summary: str,
    lint_report_path: Path,
    error_summary: str,
    trace_data: dict | None,
) -> Feedback:
    """Build feedback from a ``lint_report.json`` produced by the
    structural linter. Parses violations and emits one Issue per rule
    hit, carrying the linter's directive verbatim.

    If ``status == "failed"`` but ``lint_report_path`` does not exist,
    falls back to the gradle classifier on ``error_summary`` so that a
    broken linter invocation still produces meaningful feedback.
    """
    issues: list[Issue] = []

    if lint_report_path and lint_report_path.exists():
        try:
            data = json.loads(lint_report_path.read_text(encoding="utf-8"))
            for v in data.get("violations", []):
                file_path = v.get("file", "") or ""
                line_start = v.get("line_start") or None
                line_end = v.get("line_end") or None
                trace_ref = TraceRef(
                    file=file_path,
                    line_start=line_start if line_start else None,
                    line_end=line_end if line_end else None,
                )
                raw_lines = [
                    f"Rule: {v.get('rule', '?')}",
                    f"Severity: {v.get('severity', '?')}",
                    f"Message: {v.get('message', '')}",
                ]
                if file_path and line_start:
                    raw_lines.append(f"Location: {file_path}:{line_start}")
                issues.append(Issue(
                    kind=f"lint_{v.get('rule', 'unknown')}",
                    title=v.get("message", "Lint violation"),
                    raw="\n".join(raw_lines),
                    fix_directive=v.get("directive", ""),
                    java_trace=[trace_ref] if file_path else [],
                ))
        except (json.JSONDecodeError, OSError) as exc:
            issues.append(Issue(
                kind="preflight_report_unreadable",
                title="Failed to read lint_report.json",
                raw=str(exc),
                fix_directive=(
                    "The preflight phase ran but its report file is "
                    "missing or malformed. Re-run the phase; if the "
                    "problem persists, check StructuralLinter output."
                ),
            ))
    elif status == "failed":
        # Linter itself crashed — fall back to classifier on stderr.
        issues = build_gradle_issues("preflight", error_summary)

    if status == "passed":
        next_step = "Proceed to Phase 3 (T2M — Spoon Discovery)."
    else:
        has_errors = any(
            v.kind.startswith("lint_")
            and v.raw.find("Severity: error") != -1
            for v in issues
        )
        if has_errors:
            next_step = (
                "Preflight found structural rule violations (errors). "
                "Fix them before continuing — downstream phases would "
                "either crash or produce a silently-wrong formal model."
            )
        else:
            next_step = (
                "Preflight found only warnings. You may continue to "
                "Phase 3 (T2M), but address the warnings at the next "
                "convenient point."
            )

    return Feedback(
        phase="preflight",
        label=label,
        status=status,
        summary=summary,
        issues=issues,
        files_to_review=(
            collect_files_to_review(trace_data) if status != "passed" else []
        ),
        next_step=next_step,
    )
