"""Back-fill trace_full.json across archived convergence runs.

Re-runs ONLY the consolidation (no codegen / LLM / verifier re-run): for each
``experiments/convergence/**/traces/`` dir, load the per-stage JSONs already
present, parse the retained ``.csp``/``.thy`` under the sibling
``formal-artefacts/``, and rewrite ``trace_full.json`` with the complete schema
(Dafny + CSP + Isabelle evidence + requirement_ids).

Usage (from forge.dashboard):  python scripts/backfill_traces.py [<convergence_dir>]
"""
from __future__ import annotations

import json
import sys
from pathlib import Path

# Make `web` importable when run as a plain script from forge.dashboard.
_DASH = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(_DASH))
from web.trace_postprocess import consolidate, parse_csp_trace, parse_isabelle_trace  # noqa: E402

DEFAULT_CONV = _DASH.parent / "experiments" / "convergence"


def _load(p: Path):
    return json.loads(p.read_text(encoding="utf-8")) if p.exists() else None


def _first(*globs):
    for g in globs:
        for p in sorted(g):
            return p
    return None


def backfill(conv_dir: Path) -> dict:
    s = {"runs": 0, "with_req": 0, "with_dafny": 0, "with_csp": 0, "with_isa": 0,
         "no_m2m": 0, "errors": 0}
    for traces in sorted(conv_dir.rglob("traces")):
        if not traces.is_dir():
            continue
        m2m = _load(traces / "trace_m2m.json")
        if not m2m:
            s["no_m2m"] += 1
            continue
        fa = traces.parent / "formal-artefacts"
        csp_file = thy_file = None
        if fa.exists():
            csp_file = _first((fa / "csp").rglob("*_Module_coreassertions.csp"),
                              (fa / "csp").rglob("*_coreassertions.csp"))
            thy_file = _first((fa / "isabelle").rglob("*_Beh.thy"))
        try:
            out = consolidate(
                codegen=_load(traces / "result_codegen.json"),
                t2m=_load(traces / "trace_t2m.json"),
                m2m=m2m,
                m2t_rct=_load(traces / "trace_m2t_rct.json"),
                dafny=_load(traces / "trace_dafny.json"),
                csp=parse_csp_trace(csp_file),
                isa=parse_isabelle_trace(thy_file),
            )
            (traces / "trace_full.json").write_text(
                json.dumps(out, indent=2, ensure_ascii=False), encoding="utf-8")
        except Exception as e:  # noqa: BLE001
            s["errors"] += 1
            print(f"  ERROR {traces}: {e}")
            continue
        s["runs"] += 1
        ts = out["traces"]
        if any("requirement_ids" in t for t in ts):
            s["with_req"] += 1
        if any("dafny" in t for t in ts):
            s["with_dafny"] += 1
        if "csp" in out:
            s["with_csp"] += 1
        if "isabelle" in out:
            s["with_isa"] += 1
    return s


if __name__ == "__main__":
    conv = Path(sys.argv[1]) if len(sys.argv) > 1 else DEFAULT_CONV
    print(f"Back-filling trace_full.json under: {conv}")
    st = backfill(conv)
    print("\n=== back-fill coverage ===")
    print(f"runs back-filled:        {st['runs']}")
    print(f"  with requirement_ids:  {st['with_req']}")
    print(f"  with dafny blocks:     {st['with_dafny']}")
    print(f"  with csp block:        {st['with_csp']}")
    print(f"  with isabelle block:   {st['with_isa']}")
    print(f"skipped (no trace_m2m):  {st['no_m2m']}")
    print(f"errors:                  {st['errors']}")
