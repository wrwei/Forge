"""Coverage feedback: missing-implementation and over-implementation
detection on the bidirectional trace between requirements and Java code.

Inputs
------
- requirements file (typically ``requirement_all.json``) — set of all
  requirement IDs the developer is implementing
- ``result_codegen.json`` — produced by ``/gen-trace``, mapping each
  requirement ID to one or more Java elements
- Java source root — scanned for public types and methods to detect
  Java elements that aren't mentioned in the trace

Issue kinds
-----------
- ``missing_implementation`` — requirement has no entry in result_codegen.json
- ``over_implementation`` — public Java element not mentioned in any trace entry
- ``missing_codegen_trace`` — result_codegen.json itself is missing
"""
from __future__ import annotations

import json
import logging
import re
from pathlib import Path

from .base import (
    Feedback,
    Issue,
    TraceRef,
    collect_files_to_review,
)

logger = logging.getLogger(__name__)


# Public type declaration: class, interface, record, enum, sealed/non-sealed.
_PUBLIC_TYPE_RE = re.compile(
    r"^\s*public\s+(?:static\s+|final\s+|abstract\s+|sealed\s+|non-sealed\s+)*"
    r"(class|interface|record|enum|@interface)\s+(\w+)",
    re.MULTILINE,
)

# Public method: a leading 'public' modifier, optional generics, return type,
# method name, then '(' on the same line. Skips constructors (no return type
# pattern) since constructor identity is the class name and is already covered.
# The negative lookahead rejects type-declaration keywords as a "return type" —
# without it, `public record Foo()` would match with "record" as the return
# type and "Foo" as the method name, double-counting the type as a method.
_PUBLIC_METHOD_RE = re.compile(
    r"^\s*public\s+(?:static\s+|final\s+|synchronized\s+|abstract\s+|default\s+|native\s+)*"
    r"(?!record\s|class\s|interface\s|enum\s|@interface\s)"
    r"(?:<[^>]+>\s+)?"
    r"(?:[\w.$]+(?:<[^>]*>)?(?:\[\])?\s+)"
    r"(\w+)\s*\(",
    re.MULTILINE,
)

# Names whose absence from result_codegen.json should NOT count as
# over-implementation. The annotation infrastructure is generated boilerplate
# defined by CLAUDE.md, not by any one requirement.
_FRAMEWORK_ELEMENT_NAMES = frozenset({
    # ETL-recognised annotation infrastructure (see CLAUDE.md "Naming
    # conventions consumed by the M2M (ETL)" table). These are
    # case-study-independent marker types that exist purely to let the
    # M2M classify Java classes / methods / fields when their names
    # don't follow the default conventions. They are defined by the
    # pipeline, not by any specific requirement, so they should not
    # be flagged as over-implementation.
    "RoboChartType",
    "Clock",
    "SensorService",
    "RoboChartWait",
    # Java built-ins / generated convenience that needn't be requirement-traced:
    "main",
    "toString",
    "hashCode",
    "equals",
})


def _load_requirements(req_path: Path) -> dict[str, dict]:
    """Load requirement_all.json into a dict keyed by requirement id."""
    try:
        data = json.loads(req_path.read_text(encoding="utf-8"))
    except (json.JSONDecodeError, OSError) as exc:
        logger.warning("Failed to load %s: %s", req_path, exc)
        return {}

    if isinstance(data, list):
        items = data
    elif isinstance(data, dict):
        for key in ("requirements", "requirement_all"):
            if key in data and isinstance(data[key], list):
                items = data[key]
                break
        else:
            return {}
    else:
        return {}

    return {req["id"]: req for req in items if isinstance(req, dict) and "id" in req}


def _load_codegen_trace(trace_path: Path) -> list[dict]:
    """Load result_codegen.json's codegen_trace list."""
    if not trace_path.exists():
        return []
    try:
        data = json.loads(trace_path.read_text(encoding="utf-8"))
    except (json.JSONDecodeError, OSError) as exc:
        logger.warning("Failed to load %s: %s", trace_path, exc)
        return []
    return data.get("codegen_trace", [])


def _scan_public_elements(java_root: Path) -> list[tuple[str, str, str]]:
    """Scan ``.java`` files under ``java_root`` for public types and
    methods. Returns ``[(rel_path, element_name, kind), ...]`` where
    ``kind`` is one of ``type`` or ``method``.
    """
    out: list[tuple[str, str, str]] = []
    if not java_root.exists():
        return out
    for jf in java_root.rglob("*.java"):
        try:
            text = jf.read_text(encoding="utf-8")
        except (OSError, UnicodeDecodeError):
            continue
        rel = jf.relative_to(java_root).as_posix()
        for m in _PUBLIC_TYPE_RE.finditer(text):
            out.append((rel, m.group(2), "type"))
        for m in _PUBLIC_METHOD_RE.finditer(text):
            out.append((rel, m.group(1), "method"))
    return out


def build_coverage_issues(
    requirements_path: Path,
    codegen_trace_path: Path,
    java_source_root: Path,
) -> list[Issue]:
    """Compute the deterministic coverage issues. Pure logic — used both
    by the dashboard phase and by tests.
    """
    if not requirements_path.exists():
        return [Issue(
            kind="coverage_input_missing",
            title="Requirements file not found",
            raw=f"Expected: {requirements_path}",
            fix_directive=(
                "Verify the requirements path in dashboard config.yaml. "
                "Without a requirements file the coverage check cannot run."
            ),
        )]

    if not codegen_trace_path.exists():
        return [Issue(
            kind="missing_codegen_trace",
            title="result_codegen.json not found",
            raw=f"Expected: {codegen_trace_path}",
            fix_directive=(
                "Run /gen-trace to produce result_codegen.json before this "
                "phase. The skill consumes the requirements file + Java "
                "source and writes the requirement → Java mapping."
            ),
        )]

    requirements = _load_requirements(requirements_path)
    if not requirements:
        return [Issue(
            kind="coverage_input_invalid",
            title="Requirements file is empty or malformed",
            raw=f"Loaded 0 requirements from {requirements_path}",
            fix_directive=(
                "The requirements file did not yield any entries. Check the "
                "JSON shape — coverage expects either a top-level list of "
                "requirements or a `requirements` / `requirement_all` key."
            ),
        )]

    trace = _load_codegen_trace(codegen_trace_path)

    # Index trace by requirement id, and collect the set of element
    # names + file paths that ANY trace entry references. We use the
    # file-level set to suppress noise from methods inside types that
    # are already traced — if a record `Obstacle` is mapped to
    # `LRE-DM2`, its component methods aren't separately interesting.
    by_req: dict[str, list[dict]] = {}
    traced_element_names: set[str] = set()
    traced_file_basenames: set[str] = set()
    for entry in trace:
        rid = entry.get("requirement_gid", "")
        by_req.setdefault(rid, []).append(entry)
        elem = entry.get("java_element", "")
        if elem:
            traced_element_names.add(elem)
        java_file = entry.get("java_file", "")
        if java_file:
            traced_file_basenames.add(Path(java_file).name)

    issues: list[Issue] = []

    # 1) missing_implementation
    for rid, req in requirements.items():
        if rid in by_req:
            continue
        name = req.get("name", "")
        desc = (req.get("description") or "")[:240]
        types = req.get("types", []) or []
        type_hint = ", ".join(types) if types else "(no types)"
        issues.append(Issue(
            kind="missing_implementation",
            title=f"Requirement {rid} has no implementation trace",
            raw=f"{rid} ({name}) — types: {type_hint}\n{desc}",
            fix_directive=(
                f"Either implement requirement {rid} ({name}) and re-run "
                f"/gen-trace, or — if the implementation already exists — "
                f"add the requirement to result_codegen.json by re-running "
                f"/gen-trace. The mapping must be reachable for downstream "
                f"verification feedback to attribute failures correctly."
            ),
            java_trace=[TraceRef(
                file="",
                element=name,
                requirement_ids=[rid],
            )],
        ))

    # 2) over_implementation — public Java elements not in the trace.
    # Skip elements inside files that already contain traced things.
    # Two patterns this rule handles:
    #   (a) methods inside a class whose own mapping is traced — the
    #       class-level requirement is understood to cover its members
    #       (e.g. Sensor traced to LRE-DM5 covers Sensor.hdist).
    #   (b) container types (enums, constants holders) whose component
    #       fields / enum values are individually traced — the type
    #       itself is implicitly traced through its members (e.g.
    #       GasAnalysisMode's four enum values trace to CD-GA-FR1..4;
    #       the enum type itself has no separate requirement).
    public_elements = _scan_public_elements(java_source_root)
    for rel_path, elem_name, kind in public_elements:
        if elem_name in _FRAMEWORK_ELEMENT_NAMES:
            continue
        if elem_name in traced_element_names:
            continue
        file_basename = Path(rel_path).name
        if file_basename in traced_file_basenames:
            # The file already contributes traced elements; the
            # containing type's mapping is implicit.
            continue
        issues.append(Issue(
            kind="over_implementation",
            title=f"Public {kind} '{elem_name}' has no requirement trace",
            raw=f"{rel_path}: public {kind} {elem_name}",
            fix_directive=(
                f"The public {kind} '{elem_name}' in {rel_path} is not "
                f"mapped to any requirement in result_codegen.json. Either: "
                f"(a) re-run /gen-trace if the mapping was missed, "
                f"(b) the element implements an existing requirement that "
                f"isn't yet captured — add it to /gen-trace's output, or "
                f"(c) the element isn't required and should be removed."
            ),
            java_trace=[TraceRef(file=rel_path, element=elem_name)],
        ))

    return issues


def build_coverage_feedback(
    label: str,
    requirements_path: Path,
    codegen_trace_path: Path,
    java_source_root: Path,
    trace_data: dict | None = None,
) -> Feedback:
    """Build the coverage feedback record. Status is 'passed' when no
    issues are found, 'failed' otherwise."""
    issues = build_coverage_issues(
        requirements_path, codegen_trace_path, java_source_root,
    )

    missing = sum(1 for i in issues if i.kind == "missing_implementation")
    over = sum(1 for i in issues if i.kind == "over_implementation")
    blockers = sum(
        1 for i in issues
        if i.kind in ("coverage_input_missing", "missing_codegen_trace",
                      "coverage_input_invalid")
    )

    if blockers:
        status = "failed"
        summary = "Coverage check could not run — input artifacts missing."
        next_step = (
            "Resolve the input issue above (most commonly: run /gen-trace "
            "to produce result_codegen.json), then re-run this phase."
        )
    elif issues:
        status = "failed"
        parts = []
        if missing:
            parts.append(f"{missing} missing implementation(s)")
        if over:
            parts.append(f"{over} over-implementation(s)")
        summary = "; ".join(parts)
        next_step = (
            "Address each missing_implementation by implementing or "
            "tracing the requirement. For each over_implementation, "
            "decide whether the Java element should be removed or a "
            "requirement should be added. Use /fix-from-feedback."
        )
    else:
        status = "passed"
        summary = (
            "Every requirement has an implementation trace; every public "
            "Java element is justified."
        )
        next_step = (
            "Coverage clean. Proceed to Phase 2c (Preflight) or directly "
            "to Phase 3 (T2M)."
        )

    # Only surface "Files to review" when there are issues to act on.
    # On a clean pass the list is noise (and worse: it's derived from
    # trace_data which is the last-run trace_full.json, so after a case-
    # study switch it can show stale filenames from the previous study
    # until Phase 3/T2M re-runs).
    files_to_review = (
        collect_files_to_review(trace_data) if status != "passed" else []
    )

    return Feedback(
        phase="coverage",
        label=label,
        status=status,
        summary=summary,
        issues=issues,
        files_to_review=files_to_review,
        next_step=next_step,
    )
