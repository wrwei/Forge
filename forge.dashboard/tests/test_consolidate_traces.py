import json
from pathlib import Path

from web.trace_postprocess import consolidate, parse_csp_trace

FIX = Path(__file__).parent / "fixtures" / "lre"


def _load(name):
    return json.loads((FIX / name).read_text(encoding="utf-8"))


def _consolidate(**over):
    args = dict(codegen=_load("result_codegen.json"), t2m=None,
                m2m=_load("trace_m2m.json"), m2t_rct=_load("trace_m2t_rct.json"),
                dafny=None, csp=None, isa=None)
    args.update(over)
    return consolidate(**args)


def _by_name(out):
    return {t["robochart_element"]: t for t in out["traces"]}


def test_keeps_m2m_spine_and_rct_lines():
    bn = _by_name(_consolidate())
    assert bn["OCM"]["robochart_type"] == "State"
    assert bn["OCM"]["rct_line_start"] == 134
    assert bn["t1"]["source_state"] == "OCM"


def test_attaches_dafny_to_transition_by_source_state():
    t1 = _by_name(_consolidate(dafny=_load("trace_dafny.json")))["t1"]
    assert "dafny" in t1
    assert t1["dafny"]["dafny_line_start"]


def test_attaches_csp_and_isabelle_blocks():
    csp = parse_csp_trace(FIX / "LreController_coreassertions.csp")
    out = _consolidate(csp=csp, isa=_load("trace_isabelle.json"))
    assert any(a["property"] == "deadlock free" for a in out["csp"]["assertions"])
    assert out["isabelle"]["deadlock_free"]["thy_line_start"] == 182


def test_transition_inherits_controller_requirements():
    t1 = _by_name(_consolidate())["t1"]
    assert t1.get("requirement_ids"), "t1 should inherit LreController requirements via class fallback"


def test_trace_full_is_complete_for_a_transition():
    csp = parse_csp_trace(FIX / "LreController_coreassertions.csp")
    out = _consolidate(dafny=_load("trace_dafny.json"), csp=csp, isa=_load("trace_isabelle.json"))
    t1 = _by_name(out)["t1"]
    assert t1.get("requirement_ids")          # requirement
    assert t1.get("rct_line_start")           # RoboChart syntax
    assert t1.get("dafny")                    # Dafny evidence
    assert out["csp"]["assertions"]           # FDR4 behavioural evidence
    assert out["isabelle"]["deadlock_free"]   # Isabelle structural evidence
