"""Pure trace post-processing: parse the generated CSP artefact and consolidate
all per-stage traces into the end-to-end ``trace_full.json`` structure.

No file I/O beyond reading the paths handed to ``parse_csp_trace``; ``consolidate``
is a pure dict -> dict function so it is trivially unit-testable. ``bridge.py``
wires these to the filesystem.

Trace shape produced by ``consolidate``::

    {
      "traces": [ {robochart_element, robochart_type, [source/target/trigger],
                   rct_line_*, dafny{...}, requirement_ids, java_*}, ... ],
      "csp":      {"assertions": [{process, property, line}, ...]},   # whole-machine
      "isabelle": {"deadlock_free": {...}, "lemmas": [...], "zoperations": [...]}
    }
"""
from __future__ import annotations

import re
from pathlib import Path
from typing import Any, Optional

_ASSERT_RE = re.compile(r"^\s*assert\s+(?P<proc>\S+)\s*:\s*\[(?P<prop>[^\]]+)\]")


def parse_csp_trace(csp_file: Optional[Path]) -> dict:
    """Map each CSP process name -> list of ``{property, line}`` from an
    assertions (``*_coreassertions.csp``) file. Lines are 1-based.
    Returns ``{}`` if the file is missing or ``None``.

    FDR4 evidence is whole-machine behavioural (deadlock/divergence freedom), so
    the right granularity is the assertion, keyed by its CSP process.
    """
    if csp_file is None:
        return {}
    csp_file = Path(csp_file)
    if not csp_file.exists():
        return {}
    out: dict[str, list] = {}
    for i, line in enumerate(csp_file.read_text(encoding="utf-8").splitlines(), start=1):
        m = _ASSERT_RE.match(line)
        if not m:
            continue
        out.setdefault(m.group("proc"), []).append(
            {"property": m.group("prop").strip(), "line": i})
    return out


_ZOP_RE = re.compile(r"^\s*zoperation\s+(\w+)")
_LEMMA_RE = re.compile(r"^\s*lemma\s+(\w+)\s*(?:\[|:)")


def parse_isabelle_trace(thy_file: Optional[Path]) -> Optional[dict]:
    """Parse a Z-Machine ``.thy`` into the same shape ``IsabellePhase`` emits:
    ``{"mappings": [{isabelle_element, isabelle_type, thy_line_start}, ...]}``.

    The live pipeline gets this JSON from the Java ``IsabellePhase``; this Python
    parser exists to **back-fill archived runs** (which predate that producer) by
    reading their retained ``.thy``. Same regexes as the Java side: ``zoperation``
    declarations and named ``lemma``s. Returns ``None`` if the file is missing.
    """
    if thy_file is None:
        return None
    thy_file = Path(thy_file)
    if not thy_file.exists():
        return None
    mappings = []
    for i, line in enumerate(thy_file.read_text(encoding="utf-8").splitlines(), start=1):
        m = _ZOP_RE.match(line)
        if m:
            mappings.append({"isabelle_element": m.group(1),
                             "isabelle_type": "zoperation", "thy_line_start": i})
            continue
        m = _LEMMA_RE.match(line)
        if m:
            mappings.append({"isabelle_element": m.group(1),
                             "isabelle_type": "lemma", "thy_line_start": i})
    return {"mappings": mappings}


def consolidate(codegen, t2m, m2m, m2t_rct, dafny=None, csp=None, isa=None) -> dict:
    """Pure merge of per-stage trace dicts into the trace_full structure.

    Parameters are the parsed JSON of each per-stage trace (or ``None``):
    ``codegen`` (result_codegen.json), ``t2m``, ``m2m``, ``m2t_rct``, ``dafny``
    (trace_dafny.json), ``isa`` (trace_isabelle.json); ``csp`` is the dict from
    :func:`parse_csp_trace`. Returns ``{"traces": []}`` if ``m2m`` is missing.
    """
    if not m2m:
        return {"traces": []}

    codegen_by_name: dict[str, list[dict]] = {}
    if codegen:
        for entry in codegen.get("codegen_trace", []):
            codegen_by_name.setdefault(entry.get("java_element", ""), []).append(entry)

    t2m_by_suffix: dict[str, list] = {}
    if t2m:
        for entry in t2m.get("source_positions", []):
            qn = entry.get("qualified_name", "")
            simple = qn.rsplit(".", 1)[-1] if "." in qn else qn
            t2m_by_suffix.setdefault(simple, []).append(entry)

    rct_by_name: dict[str, dict] = {}
    if m2t_rct:
        for entry in m2t_rct.get("mappings", []):
            rct_by_name[entry.get("robochart_element", "")] = entry

    # Dafny TransitionMethods, keyed by dafny_element (== the source-state name).
    dafny_tm: dict[str, dict] = {}
    if dafny:
        for entry in dafny.get("mappings", []):
            if entry.get("dafny_type") == "TransitionMethod":
                dafny_tm[entry.get("dafny_element", "")] = entry

    def _reqs_for(rc_name: str, m2m_java_elem: str) -> list:
        """Requirement IDs via the element name, its m2m java_element, and the
        enclosing class (so a transition keyed 'LreController.step' inherits the
        controller's requirements)."""
        keys = [rc_name, m2m_java_elem]
        if m2m_java_elem and "." in m2m_java_elem:
            keys.append(m2m_java_elem.split(".", 1)[0])
        ids = []
        for k in keys:
            for e in codegen_by_name.get(k, []):
                if e.get("requirement_gid"):
                    ids.append(e["requirement_gid"])
        return sorted(set(ids))

    traces = []
    for m2m_entry in m2m.get("mappings", []):
        rc_type = m2m_entry.get("robochart_type", "")
        rc_name = m2m_entry.get("robochart_element", "")
        m2m_java_elem = m2m_entry.get("java_element", "")
        trace: dict[str, Any] = {"robochart_type": rc_type, "robochart_element": rc_name}

        if rc_type == "Transition":
            for key in ("source_state", "target_state", "trigger_event"):
                if key in m2m_entry:
                    trace[key] = m2m_entry[key]

        if rc_name in rct_by_name:
            r = rct_by_name[rc_name]
            trace["rct_line_start"] = r.get("rct_line_start")
            trace["rct_line_end"] = r.get("rct_line_end")

        # Dafny evidence: a transition's contract method is keyed by its source state.
        if rc_type == "Transition":
            d = dafny_tm.get(m2m_entry.get("source_state"))
            if d:
                trace["dafny"] = {
                    "dafny_element": d.get("dafny_element"),
                    "dafny_line_start": d.get("dafny_line_start"),
                    "dafny_line_end": d.get("dafny_line_end"),
                }

        reqs = _reqs_for(rc_name, m2m_java_elem)
        if reqs:
            trace["requirement_ids"] = reqs

        cg = codegen_by_name.get(rc_name) or codegen_by_name.get(m2m_java_elem)
        if cg and cg[0].get("java_file"):
            trace.setdefault("java_file", cg[0]["java_file"])
        if "java_file" not in trace and m2m_entry.get("java_file"):
            trace["java_file"] = m2m_entry["java_file"]
        if "java_line_start" not in trace and m2m_entry.get("java_line_start"):
            trace["java_line_start"] = m2m_entry["java_line_start"]
            trace["java_line_end"] = m2m_entry.get("java_line_end")
        t2m_entries = t2m_by_suffix.get(rc_name) or t2m_by_suffix.get(m2m_java_elem)
        if t2m_entries and "java_line_start" not in trace:
            trace["java_line_start"] = t2m_entries[0].get("line_start")
            trace["java_line_end"] = t2m_entries[0].get("line_end")
            trace["java_source_file"] = t2m_entries[0].get("file", "")

        traces.append(trace)

    result: dict[str, Any] = {"traces": traces}

    # CSP: whole-machine behavioural evidence (flatten assertions across processes).
    if csp:
        result["csp"] = {"assertions": [
            {"process": proc, **a} for proc, lst in csp.items() for a in lst]}

    # Isabelle: structural evidence (deadlock_free + invariant lemmas + zoperations).
    if isa:
        mappings = isa.get("mappings", []) if isinstance(isa, dict) else []
        deadlock_free = next(
            (m for m in mappings
             if m.get("isabelle_type") == "lemma"
             and str(m.get("isabelle_element", "")).endswith("deadlock_free")), None)
        result["isabelle"] = {
            "deadlock_free": deadlock_free,
            "lemmas": [m for m in mappings if m.get("isabelle_type") == "lemma"],
            "zoperations": [m for m in mappings if m.get("isabelle_type") == "zoperation"],
        }
    return result
