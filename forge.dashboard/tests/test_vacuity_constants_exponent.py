"""K1 constant check: Dafny constants with an exponent, and the plain-decimal
form the Dafny generator now emits (2026-10-28)."""
from fractions import Fraction
from pathlib import Path
import json

from web import vacuity, vacuity_sat as vs


def test_dafny_reader_takes_the_exponent():
    got = vs.parse_dafny_consts("const EPS: real := 1.0E-9\nconst BIG: real := 2.5e3\n")
    assert got == {"EPS": Fraction(1, 10**9), "BIG": Fraction(2500)}


def test_dafny_reader_plain_decimals_unchanged():
    got = vs.parse_dafny_consts("const EPS: real := 0.000000001\nconst T: real := 2.0\n")
    assert got == {"EPS": Fraction(1, 10**9), "T": Fraction(2)}


def _check(tmp_path: Path, dfy_text: str):
    cd = tmp_path / "constant_defaults.json"
    cd.write_text(json.dumps({"EPS": 1e-09}))
    dfy = tmp_path / "C.dfy"
    dfy.write_text(dfy_text)
    return vacuity.check_constant_consistency(None, None, [dfy], [], cd, tmp_path)


def test_no_divergence_for_matching_exponent_or_plain(tmp_path):
    assert _check(tmp_path, "const EPS: real := 1.0E-9\n") == []
    assert _check(tmp_path, "const EPS: real := 0.000000001\n") == []


def test_divergence_still_reported_for_a_real_mismatch(tmp_path):
    fs = _check(tmp_path, "const EPS: real := 0.001\n")
    assert [f.kind for f in fs] == ["constant_divergence"]
