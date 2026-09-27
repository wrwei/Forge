from pathlib import Path

from web.trace_postprocess import parse_csp_trace

FIX = Path(__file__).parent / "fixtures" / "lre"


def test_parse_csp_trace_finds_assertions_with_lines():
    result = parse_csp_trace(FIX / "LreController_coreassertions.csp")
    assert "P_LreController_Module" in result
    props = {a["property"]: a["line"] for a in result["P_LreController_Module"]}
    assert props["deadlock free"] == 4
    assert props["divergence free"] == 5
    assert any(a["property"] == "deadlock free" for a in result["P_LreController"])


def test_parse_csp_trace_missing_or_none_returns_empty():
    assert parse_csp_trace(FIX / "does_not_exist.csp") == {}
    assert parse_csp_trace(None) == {}
