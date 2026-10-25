r"""Environment-model-aware satisfiability (2026-10-20).

`check_precondition_unsat` leaves `offered` free, so it could not see an
operation whose precondition is false under the theory's OWN environment
model. These controls pin the new check:

POSITIVE CONTROLS (must fire; fix the check, never the control):
  * model A (`offered = listens`), frozen LRE run-5 theory: the nine
    guard-driven operations out of MOM/HCM/CAM are dead, incl. MOMToCAM.
  * model A, synthetic two-state machine: the absence-guarded branch is dead.
  * model B, synthetic: a branch needing two events in one step is dead.
NEGATIVE CONTROLS (must NOT fire):
  * model B, the LRE theory the current generator emits: no dead operation,
    and Idle_X fires under the empty offer.
  * pre-split encoding (no `offered` lens): the check is silent.
"""
from pathlib import Path

from web import vacuity
from web import vacuity_sat as vsat

FX = Path(__file__).resolve().parent / "fixtures" / "envmodel"

LRE_DEAD_UNDER_A = {"MOMToOCM_2", "MOMToCAM", "MOMToHCM_1", "MOMToHCM_2",
                    "MOMToHCM_3", "HCMToOCM_1", "HCMToCAM", "HCMToMOM", "CAMToOCM_1"}


def _dead(text: str, tmp_path: Path):
    thy = tmp_path / "X_Beh.thy"
    thy.write_text(text)
    m = vsat.parse_theory(text)
    fs = vacuity.check_precondition_unsat_env(m, thy, tmp_path)
    return m, {f.title.split("zoperation ")[1].split(" is")[0] for f in fs}, fs


def test_model_a_frozen_lre_nine_dead_operations(tmp_path):
    m, dead, fs = _dead((FX / "LreController_Beh.modelA.thy").read_text(), tmp_path)
    assert vsat.environment_model(m) == "A"
    assert dead == LRE_DEAD_UNDER_A
    assert all(f.kind == "precondition_unsat_env" and "INSTRUMENT" in f.fix_directive for f in fs)


def test_model_b_generated_lre_no_dead_operation(tmp_path):
    m, dead, _ = _dead((FX / "LreController_Beh.modelB.thy").read_text(), tmp_path)
    assert vsat.environment_model(m) == "B"
    assert dead == set()
    assert vsat.allowed_offers(m, "MOM") == [frozenset(), frozenset({"endTask"}),
                                            frozenset({"reqOCM"}), frozenset({"reqHCM"})]
    idle = next(o for o in m.operations if o.name == "Idle_MOM")
    env = vsat.Env(checked=False)
    assert vsat.op_sat_at(idle, "MOM", m, env, fixed_offered=frozenset()).verdict != vsat.UNSAT


A_THY = r"""
enumtype St = S | T | initial

enumtype Evt = go | stop

zstore F =
  st::"St"
  tr :: "(St, Evt) tag list"
  listens:: "Evt set"
  offered:: "Evt set"
  x :: "bool"
  where inv:
    "tr \<noteq> []
                      \<and> (st = S \<longrightarrow> listens = {go}) \<and> offered = listens"

zoperation SToT =
  over F
  pre "st= S \<and> go \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= T
         ,tr\<Zprime> =tr @ [Event go] @ [State T]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"

zoperation SToS =
  over F
  pre "st= S \<and> x \<and> \<not>(go \<in> offered) \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= S
         ,tr\<Zprime> =tr @ [State S]
         ,listens\<Zprime> = {go}
         ,offered\<Zprime> = {go}
         ]"

definition Init :: "F subst" where
  [z_defs]:
  "Init =
  [st\<leadsto> initial
  ,tr\<leadsto> [State initial]
  ,listens\<leadsto> {}
  ,offered\<leadsto> {}
  ]"
"""

B_THY = A_THY.replace(
    r"\<and> offered = listens",
    r"\<and> (st = S \<longrightarrow> (offered = {} \<or> offered = {go} \<or> offered = {stop}))"
).replace(
    r'pre "st= S \<and> x \<and> \<not>(go \<in> offered) \<and> offered \<subseteq> listens"',
    r'pre "st= S \<and> go \<in> offered \<and> stop \<in> offered"',
)


def test_model_a_synthetic_absence_branch_dead(tmp_path):
    m, dead, _ = _dead(A_THY, tmp_path)
    assert vsat.environment_model(m) == "A"
    assert dead == {"SToS"}


def test_model_b_synthetic_two_events_dead(tmp_path):
    m, dead, fs = _dead(B_THY, tmp_path)
    assert vsat.environment_model(m) == "B"
    assert dead == {"SToS"}
    assert "two events" in fs[0].fix_directive


def test_pre_split_encoding_is_silent(tmp_path):
    pre_split = A_THY.replace('  offered:: "Evt set"\n', "").replace(" \\<and> offered = listens", "")
    pre_split = pre_split.replace(" \\<and> offered \\<subseteq> listens", "").replace("offered", "listens")
    pre_split = pre_split.replace("         ,listens\\<Zprime> = {}\n         ,listens\\<Zprime> = {}\n", "         ,listens\\<Zprime> = {}\n")
    pre_split = pre_split.replace("         ,listens\\<Zprime> = {go}\n         ,listens\\<Zprime> = {go}\n", "         ,listens\\<Zprime> = {go}\n")
    pre_split = pre_split.replace("  ,listens\\<leadsto> {}\n  ,listens\\<leadsto> {}\n", "  ,listens\\<leadsto> {}\n")
    m, dead, _ = _dead(pre_split, tmp_path)
    assert vsat.environment_model(m) == "free"
    assert dead == set()


GAS_EXHAUSTIVE_THY = r"""
enumtype St = Analysis | NoGas | GasDetected | initial

enumtype Evt = tick | gas

enumtype Status = noGas | gasD

zstore F =
  st::"St"
  tr :: "(St, Evt) tag list"
  listens:: "Evt set"
  offered:: "Evt set"
  sts :: "Status"
  where inv:
    "tr \<noteq> []
                      \<and> (st = Analysis \<longrightarrow> listens = {tick})
                      \<and> (st = Analysis \<longrightarrow> (offered = {} \<or> offered = {tick}))"

zoperation AnalysisToNoGas =
  over F
  pre "st= Analysis \<and> sts= (noGas)"
  update "[st\<Zprime>= NoGas
         ,tr\<Zprime> =tr @ [State NoGas]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"

zoperation AnalysisToGasDetected =
  over F
  pre "st= Analysis \<and> sts= (gasD)"
  update "[st\<Zprime>= GasDetected
         ,tr\<Zprime> =tr @ [State GasDetected]
         ,listens\<Zprime> = {}
         ,offered\<Zprime> = {}
         ]"

zoperation Idle_Analysis =
  over F
  params nxt \<in> "{{}, {tick}}"
  pre "st= Analysis \<and> \<not>(sts= (noGas)) \<and> \<not>(sts= (gasD)) \<and> \<not>(tick \<in> offered)"
  update "[st\<Zprime>= Analysis
         ,tr\<Zprime> = tr
         ,offered\<Zprime> = nxt
         ]"

definition Init :: "F subst" where
  [z_defs]:
  "Init =
  [st\<leadsto> initial
  ,tr\<leadsto> [State initial]
  ,listens\<leadsto> {}
  ,offered\<leadsto> {}
  ]"
"""


def test_model_b_dead_idle_not_blocked_by_plain_check(tmp_path):
    # The symbolic S1 check must not report the exhaustive-mode idle operation as
    # blocking; only the environment-aware check reports it, as advisory.
    thy = tmp_path / "G_Beh.thy"
    thy.write_text(GAS_EXHAUSTIVE_THY)
    m = vsat.parse_theory(GAS_EXHAUSTIVE_THY)
    assert [f.kind for f in vacuity.check_precondition_unsat(m, thy, tmp_path)] == []


def test_model_b_dead_idle_is_advisory(tmp_path):
    thy = tmp_path / "G_Beh.thy"
    thy.write_text(GAS_EXHAUSTIVE_THY)
    m = vsat.parse_theory(GAS_EXHAUSTIVE_THY)
    fs = vacuity.check_precondition_unsat_env(m, thy, tmp_path)
    assert [f.kind for f in fs] == ["idle_unreachable"]
    assert vacuity.classify_severity(fs[0].signal, fs[0].kind) == "advisory"
