"""Tests for the R2-4 extension of the vacuity audit (Phase 6d):
satisfiability, reachability, and constant-consistency checks.

POSITIVE CONTROLS (must fire — if one stops firing the check is broken;
fix the check, never the control):
  * C6  — LRE odist(index)=0 stub makes MOM/HCM/CAM unreachable under the
          checked CSP instantiation (R2).
  * U4  — sranger obstacleThreshold: Java 0.5 vs .rct/CSP 1 (K1).
  * pre-split trigger encoding: presence + absence conjunct over the
          same pinned static set is contradictory (S1).

NEGATIVE CONTROLS: the archived reference-run artefacts must produce NO
S1/A1/N1 findings (they are believed clean of those classes); their known
R2/T2/K1 findings are pinned exactly.
"""
import json
from pathlib import Path

import pytest

from web import vacuity
from web import vacuity_sat as vsat

REPO = Path(__file__).resolve().parents[2]
REF = REPO / "reference-runs"

pytestmark = pytest.mark.skipif(not REF.exists(),
                                reason="reference-runs not present")


# ---------------------------------------------------------------------------
# Parser unit tests
# ---------------------------------------------------------------------------

MINI_THY = r"""
enumtype St = A | B | initial

enumtype Evt = e1 | e2

zstore M =
  flag :: "bool"
  x :: "real"
  st::"St"
  tr :: "(St, Evt) tag list"
  listens :: "Evt set"
  offered :: "Evt set"
  where inv:
    "tr \<noteq> []
                      \<and> (st = A \<longrightarrow> listens = {e1})"

zoperation InitialToA =
  over M
  pre "st= initial"
  update "[st\<Zprime>= A
         ,tr\<Zprime> =tr @ [State A]
         ,listens\<Zprime> = {e1}
         ]"

zoperation AToB =
  over M
  pre "st= A \<and> x>1.0 \<and> e1 \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= B
         ,tr\<Zprime> =tr @ [State B]
         ,listens\<Zprime> = {}
         ]"

definition Init :: "M subst" where
  [z_defs]:
  "Init =
  [st\<leadsto> initial
  ,tr\<leadsto> [State initial]
  ,listens\<leadsto> {}
  ]"
"""


def test_parse_theory_shape():
    m = vsat.parse_theory(MINI_THY)
    assert m.name == "M"
    assert m.enumtypes["St"] == ["A", "B", "initial"]
    assert m.pins == {"A": frozenset({"e1"})}
    assert m.pin_lens == "listens"
    assert [op.name for op in m.operations] == ["InitialToA", "AToB"]
    assert m.operations[1].source_state == "A"
    assert m.operations[1].target_state == "B"
    assert m.init["st"] == "initial"


def test_sat_conjunction_basic():
    m = vsat.parse_theory(MINI_THY)
    env = vsat.Env(checked=False)
    ok = vsat.parse_conjunction(r"st= A \<and> x>1.0")
    assert vsat.sat_conjunction(ok, m, env).verdict == vsat.SAT
    bad = vsat.parse_conjunction(r"x>1.0 \<and> x<0.5")
    assert vsat.sat_conjunction(bad, m, env).verdict == vsat.UNSAT
    boolclash = vsat.parse_conjunction(r"flag \<and> \<not>flag")
    assert vsat.sat_conjunction(boolclash, m, env).verdict == vsat.UNSAT


def test_sat_member_against_pin():
    m = vsat.parse_theory(MINI_THY)
    env = vsat.Env(checked=False)
    # e2 IN offered with offered ⊆ listens = {e1} at st=A → UNSAT
    atoms = vsat.parse_conjunction(
        r"st= A \<and> e2 \<in> offered \<and> offered \<subseteq> listens")
    assert vsat.sat_conjunction(atoms, m, env).verdict == vsat.UNSAT
    # e1 is fine
    atoms = vsat.parse_conjunction(
        r"st= A \<and> e1 \<in> offered \<and> offered \<subseteq> listens")
    assert vsat.sat_conjunction(atoms, m, env).verdict == vsat.SAT


def test_unknown_never_silent_sat():
    m = vsat.parse_theory(MINI_THY)
    env = vsat.Env(checked=False)
    atoms = vsat.parse_conjunction(r"x>1.0 \<or> flag")
    r = vsat.sat_conjunction(atoms, m, env)
    assert r.verdict == vsat.UNKNOWN
    assert r.unknown_atoms


def test_checked_tier_narrows_and_stubs():
    m = vsat.parse_theory(MINI_THY)
    grid = (vsat.Fraction(0), vsat.Fraction(1))
    env = vsat.Env(checked=True, grid=grid, const_map={"f": vsat.Fraction(0)})
    # x > 1.0 has no solution on {0,1}
    atoms = vsat.parse_conjunction(r"x>1.0")
    assert vsat.sat_conjunction(atoms, m, env).verdict == vsat.UNSAT
    # x >= 1.0 does
    atoms = vsat.parse_conjunction(r"x\<ge>1.0")
    assert vsat.sat_conjunction(atoms, m, env).verdict == vsat.SAT
    # stubbed call: f(y) > 1.0 with f ↦ 0 is UNSAT
    atoms = vsat.parse_conjunction(r"f(y)>1.0")
    assert vsat.sat_conjunction(atoms, m, env).verdict == vsat.UNSAT


# ---------------------------------------------------------------------------
# POSITIVE CONTROL — pre-split contradictory trigger conjuncts (S1)
# ---------------------------------------------------------------------------

F1_THY = r"""
enumtype St = S | T | initial

enumtype Evt = go | stop

zstore F =
  st::"St"
  tr :: "(St, Evt) tag list"
  triggers:: "Evt set"
  where inv:
    "tr \<noteq> []
                      \<and> (st = S \<longrightarrow> triggers = {go, stop})"

zoperation InitialToS =
  over F
  pre "st= initial"
  update "[st\<Zprime>= S
         ,tr\<Zprime> =tr @ [State S]
         ,triggers\<Zprime> = {go, stop}
         ]"

zoperation SToT =
  over F
  pre "st= S \<and> go \<in> triggers \<and> \<not>(stop \<in> triggers)"
  update "[st\<Zprime>= T
         ,tr\<Zprime> =tr @ [State T]
         ,triggers\<Zprime> = {}
         ]"

definition Init :: "F subst" where
  [z_defs]:
  "Init =
  [st\<leadsto> initial
  ,tr\<leadsto> [State initial]
  ,triggers\<leadsto> {}
  ]"
"""


def test_f1_positive_control_precondition_unsat(tmp_path):
    r"""The earlier encoding read the STATIC pinned set for both
    presence and absence conjuncts; with triggers pinned to {go, stop} at
    S, `\<not>(stop \<in> triggers)` is false — S1 must fire."""
    thy = tmp_path / "F_Beh.thy"
    thy.write_text(F1_THY)
    m = vsat.parse_theory(F1_THY)
    findings = vacuity.check_precondition_unsat(m, thy, tmp_path)
    assert [f.signal for f in findings] == ["S1"]
    assert "SToT" in findings[0].title


# ---------------------------------------------------------------------------
# POSITIVE CONTROL — C6: LRE modes unreachable under checked instantiation
# ---------------------------------------------------------------------------

def test_c6_positive_control_lre_modes_unreachable_checked():
    root = REF / "lre" / "formal-artefacts"
    r = vacuity.audit(root, REPO, java_root=REF / "lre" / "java")
    r2 = [f for f in r.findings if f.signal == "R2"]
    assert len(r2) == 1, [f"{f.signal}:{f.title}" for f in r.findings]
    for mode in ("MOM", "HCM", "CAM"):
        assert mode in r2[0].title
    # and the root cause is visible in the T2 detail
    t2 = [f for f in r.findings if f.signal == "T2"]
    assert t2 and "odist" in t2[0].raw


def test_c6_symbolic_reachability_is_full():
    """Same theory, unnarrowed model: every mode reachable — the defect is
    the instantiation, not the automaton."""
    text = (REF / "lre" / "formal-artefacts" / "isabelle"
            / "LreController_Beh.thy").read_text()
    m = vsat.parse_theory(text)
    reach = vsat.reachability(m, vsat.Env(checked=False))
    assert reach.reachable == {"initial", "OCM", "MOM", "HCM", "CAM"}


# ---------------------------------------------------------------------------
# POSITIVE CONTROL — U4: sranger constant divergence (K1)
# ---------------------------------------------------------------------------

def test_u4_positive_control_sranger_constant_divergence():
    root = REF / "sranger" / "formal-artefacts"
    r = vacuity.audit(root, REPO, java_root=REF / "sranger" / "java")
    k1 = [f for f in r.findings if f.signal == "K1"]
    assert len(k1) == 1, [f"{f.signal}:{f.title}" for f in r.findings]
    assert "obstaclethreshold" in k1[0].title
    assert "0.5" in k1[0].raw and ": 1" in k1[0].raw


# ---------------------------------------------------------------------------
# Synthetic positives for the remaining checks
# ---------------------------------------------------------------------------

def test_antecedent_vacuity_fires(tmp_path):
    thy_text = MINI_THY.replace(
        r'"tr \<noteq> []',
        r'"tr \<noteq> [] \<and> (x>1.0 \<and> x<0.5 \<longrightarrow> flag)')
    thy = tmp_path / "M_Beh.thy"
    thy.write_text(thy_text)
    m = vsat.parse_theory(thy_text)
    findings = vacuity.check_antecedent_vacuity(m, thy, tmp_path)
    assert [f.signal for f in findings] == ["A1"]


def test_empty_param_domain_fires(tmp_path):
    thy_text = MINI_THY.replace(
        'zoperation AToB =\n  over M\n  pre',
        'zoperation AToB =\n  over M\n  params g \\<in> "EmptySet" \n  pre'
    )
    thy_text = ('definition EmptySet :: "real set" where [simp]: '
                '"EmptySet = {}"\n' + thy_text)
    thy = tmp_path / "M_Beh.thy"
    thy.write_text(thy_text)
    m = vsat.parse_theory(thy_text)
    findings = vacuity.check_empty_domain(m, thy, tmp_path)
    kinds = [f.kind for f in findings]
    assert "empty_param_domain" in kinds
    # and the operation is UNSAT via op_sat_at too
    op = [o for o in m.operations if o.name == "AToB"][0]
    assert vsat.op_sat_at(op, "A", m, vsat.Env(checked=False)).verdict == vsat.UNSAT


def test_singleton_domain_only_flagged_when_compared(tmp_path):
    # Loc is singleton but never compared → not flagged
    thy_text = MINI_THY + "\nenumtype Loc = left\n"
    thy = tmp_path / "M_Beh.thy"
    thy.write_text(thy_text)
    m = vsat.parse_theory(thy_text)
    findings = vacuity.check_empty_domain(m, thy, tmp_path)
    assert findings == []
    # compared singleton → flagged
    thy_text2 = thy_text.replace(
        'pre "st= A \\<and> x>1.0',
        'pre "st= A \\<and> loc= (left) \\<and> x>1.0')
    thy_text2 = thy_text2.replace('  x :: "real"', '  x :: "real"\n  loc :: "Loc"')
    m2 = vsat.parse_theory(thy_text2)
    findings2 = vacuity.check_empty_domain(m2, thy, tmp_path)
    assert [f.kind for f in findings2] == ["singleton_domain"]


def test_init_inconsistency_fires(tmp_path):
    # Init pins st to A but sets listens = {} while inv pins listens = {e1} at A
    thy_text = MINI_THY.replace("st\\<leadsto> initial", "st\\<leadsto> A")
    thy = tmp_path / "M_Beh.thy"
    thy.write_text(thy_text)
    m = vsat.parse_theory(thy_text)
    findings = vacuity.check_initial_state_consistency(m, thy, tmp_path)
    assert [f.signal for f in findings] == ["N1"]


def test_init_consistent_is_clean(tmp_path):
    thy = tmp_path / "M_Beh.thy"
    thy.write_text(MINI_THY)
    m = vsat.parse_theory(MINI_THY)
    assert vacuity.check_initial_state_consistency(m, thy, tmp_path) == []


def test_dead_offered_pairs_found():
    """Post-split fixture: at st=A with offered={} the only outgoing op
    requires e1 ∈ offered → (A, {}) is a dead pair."""
    m = vsat.parse_theory(MINI_THY)
    env = vsat.Env(checked=False)
    reach = vsat.reachability(m, env)
    dead = vsat.dead_offered_pairs(m, reach.reachable, env)
    assert ("A", frozenset()) in dead


# ---------------------------------------------------------------------------
# NEGATIVE CONTROLS — archived artefacts, pinned expected findings
# ---------------------------------------------------------------------------

ARCHIVED_EXPECTED = {
    # study -> exact multiset of firing signals
    "lre": ["R2", "T2"],
    "sranger": ["K1"],
    "chemical_detector": ["R2", "T2"],
}


@pytest.mark.parametrize("study", sorted(ARCHIVED_EXPECTED))
def test_archived_artefacts_pinned_findings(study):
    root = REF / study / "formal-artefacts"
    r = vacuity.audit(root, REPO, java_root=REF / study / "java")
    got = sorted(f.signal for f in r.findings)
    assert got == sorted(ARCHIVED_EXPECTED[study]), \
        [f"{f.signal}: {f.title} | {f.raw[:120]}" for f in r.findings]


@pytest.mark.parametrize("study", sorted(ARCHIVED_EXPECTED))
def test_archived_artefacts_no_s1_a1_n1(study):
    """The archived theories are believed clean of contradictory
    preconditions, vacuous antecedents, and Init/invariant clashes."""
    root = REF / study / "formal-artefacts"
    r = vacuity.audit(root, REPO, java_root=REF / study / "java")
    bad = [f for f in r.findings if f.signal in ("S1", "A1", "N1", "E1")]
    assert bad == [], [f"{f.signal}: {f.title}" for f in bad]


def test_verdicts_are_closed_vocabulary():
    root = REF / "lre" / "formal-artefacts"
    r = vacuity.audit(root, REPO, java_root=REF / "lre" / "java")
    for d in r.details.values():
        for table in ("op_fireable_symbolic", "op_fireable_checked"):
            for v in d.get(table, {}).values():
                assert v in (vsat.SAT, vsat.UNSAT, vsat.UNKNOWN)
