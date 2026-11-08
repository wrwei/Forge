"""Amendment 21: D2 and I2 report the generator's placeholder predicates.

D2: `Valid() { true }` with behavioural ensures present (D1 stays silent) is
reported as advisory. I2: `zexpr R1 is "True"` is reported as advisory with
the number of lemmas that establish or preserve it. Neither blocks.
"""
from pathlib import Path

from forge_pipeline import audit as vacuity


DAFNY = """
class C {
    var mode: int
    ghost predicate Valid()
        reads this
    {
        true  // Extend with domain invariants
    }
    method transitionFromA()
        modifies this
        ensures Valid()
        ensures old(mode == 1) ==> mode == 2
    {
        mode := 2;
    }
}
"""

THY = """
theory T imports Z_Machines begin
zstore S =
  st :: "nat"
  where inv: "st \\<le> 3"
lemma Init_inv: "Init establishes S_inv" by zpog_full
lemma A_inv: "A preserves S_inv" by zpog_full
lemma "S_deadlock_free" by zpog_full
zexpr R1 is "True"
lemma  "Init establishes R1" by zpog_full
lemma "A preserves R1 under S_inv" by (zpog_full; auto)
lemma "B preserves R1 under S_inv" by (zpog_full; auto)
end
"""


def test_d2_reports_placeholder_valid_when_ensures_carry_the_load(tmp_path: Path):
    d = tmp_path / "C.dfy"
    d.write_text(DAFNY)
    f = vacuity.check_dafny_valid_vacuous(d, tmp_path)
    assert f is not None and f.signal == "D2"
    assert vacuity.classify_severity(f.signal, f.kind) == "advisory"
    assert "1 behavioural ensures" in f.raw


def test_d1_still_fires_when_nothing_carries_the_load(tmp_path: Path):
    d = tmp_path / "C.dfy"
    d.write_text(DAFNY.replace("ensures old(mode == 1) ==> mode == 2", ""))
    f = vacuity.check_dafny_valid_vacuous(d, tmp_path)
    assert f is not None and f.signal == "D1"
    assert vacuity.classify_severity(f.signal, f.kind) == "blocking"


def test_i2_counts_lemmas_on_the_placeholder_and_is_advisory(tmp_path: Path):
    t = tmp_path / "T.thy"
    t.write_text(THY)
    fs = vacuity.check_isabelle_requirement_placeholder(t, tmp_path)
    assert len(fs) == 1 and fs[0].signal == "I2"
    assert "3 of 6 lemmas" in fs[0].title
    assert vacuity.classify_severity(fs[0].signal, fs[0].kind) == "advisory"
    # I1 does not fire: the store invariant is not literally True
    assert vacuity.check_isabelle_inv_vacuous(t, tmp_path) is None


def test_i2_silent_when_the_requirement_has_content(tmp_path: Path):
    t = tmp_path / "T.thy"
    t.write_text(THY.replace('zexpr R1 is "True"', 'zexpr R1 is "st \\<noteq> 3"'))
    assert vacuity.check_isabelle_requirement_placeholder(t, tmp_path) == []


def test_report_with_only_placeholders_passes(tmp_path: Path):
    r = vacuity.Report()
    d = tmp_path / "C.dfy"; d.write_text(DAFNY)
    t = tmp_path / "T.thy"; t.write_text(THY)
    r.add(vacuity.check_dafny_valid_vacuous(d, tmp_path))
    for f in vacuity.check_isabelle_requirement_placeholder(t, tmp_path):
        r.add(f)
    assert r.status == "passed" and len(r.advisory) == 2 and r.blocking == []
