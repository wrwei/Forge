"""P1 severity split (VAC-1) — regression tests.

Background. `dead_offered_pairs` reports (state, offered) pairs from which no
operation can fire. A single BLOCKING signal used to cover two situations:

  (state, {})        the environment offers nothing. A reactive controller is
                     WAITING; a specified terminal mode is ABSORBING. Neither
                     is a defect.
  (state, non-empty) the state declares it listens for these events yet no
                     operation can act on them. A genuine gap.

Because BLOCKING prevents convergence, the conflation made a correct
implementation unconvergeable — the only way to clear a (state, {}) pair is to
add a transition, and with no requirement behind it that is an unjustified edit
made solely to satisfy the checker. (The archived v1 SRanger carries exactly
such an edge: a tick self-loop on its terminal mode, added per its own source
comment to give the model checker "a visible event out of Final".)

These tests pin the split. If someone re-merges the kinds, or moves
`dead_state_no_offer` out of ADVISORY_KINDS, the second test fails.
"""

from pathlib import Path

from web import vacuity
from web import vacuity_sat as vsat


REPO = Path(__file__).resolve().parents[2]


# A terminal state T with an empty pin: absorbing by design, nothing offered,
# no operation can fire. This must be ADVISORY.
TERMINAL_THY = r"""
enumtype St = A | T | initial

enumtype Evt = go | stop

zstore M =
  st::"St"
  tr :: "(St, Evt) tag list"
  listens :: "Evt set"
  offered :: "Evt set"
  where inv:
    "tr \<noteq> []
                      \<and> (st = A \<longrightarrow> listens = {go, stop})
                      \<and> (st = T \<longrightarrow> listens = {})"

zoperation InitialToA =
  over M
  pre "st= initial"
  update "[st\<Zprime>= A
         ,tr\<Zprime> =tr @ [State A]
         ,listens\<Zprime> = {go, stop}
         ]"

zoperation AToT =
  over M
  pre "st= A \<and> stop \<in> offered \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= T
         ,tr\<Zprime> =tr @ [State T]
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


# Same machine, but T advertises `go` while no operation at T can act on it.
# (T, {go}) is a dead pair with a NON-EMPTY offer. This must stay BLOCKING.
ADVERTISED_THY = TERMINAL_THY.replace(
    r"\<and> (st = T \<longrightarrow> listens = {})",
    r"\<and> (st = T \<longrightarrow> listens = {go})",
).replace(
    """  update "[st\\<Zprime>= T
         ,tr\\<Zprime> =tr @ [State T]
         ,listens\\<Zprime> = {}
         ]\"""",
    """  update "[st\\<Zprime>= T
         ,tr\\<Zprime> =tr @ [State T]
         ,listens\\<Zprime> = {go}
         ]\"""",
)


def _pairs(thy_text):
    """Run the P1 pair computation on a theory fixture.

    `vsat.parse_theory` takes the theory TEXT (not a path), as the existing
    semantic tests do — no temp file needed.
    """
    model = vsat.parse_theory(thy_text)
    env = vsat.Env(checked=False)
    reach = vsat.reachability(model, env).reachable
    return model, vsat.dead_offered_pairs(model, reach, env)


def test_severity_table_splits_the_two_p1_kinds():
    """The classifier, not just the message text, must distinguish them."""
    assert vacuity.classify_severity("P1", "dead_state_no_offer") == "advisory"
    assert vacuity.classify_severity("P1", "dead_state_offered_pair") == "blocking"
    assert "dead_state_no_offer" in vacuity.ADVISORY_KINDS
    # the blocking half must NOT have been swept in with it
    assert "dead_state_offered_pair" not in vacuity.ADVISORY_KINDS


def test_empty_offer_pair_is_detected_at_a_terminal_state():
    """Detection is unchanged: the pair is still found, just not blocking."""
    model, dead = _pairs(TERMINAL_THY)
    assert "T" in model.enumtypes.get("St", []), model.enumtypes
    empty = [(s, off) for s, off in dead if not off]
    assert any(s == "T" for s, _ in empty), dead
    # and it is the empty-offer kind, so advisory
    assert vacuity.classify_severity("P1", "dead_state_no_offer") == "advisory"


def test_nonempty_offer_pair_still_blocks():
    """POSITIVE CONTROL: a state advertising an event it cannot act on.

    If this stops firing, the split has gone too far and the check is blind to
    the situation it exists to catch.
    """
    _, dead = _pairs(ADVERTISED_THY)
    nonempty = [(s, off) for s, off in dead if off]
    assert nonempty, (
        "no non-empty-offer dead pair found; the positive control is broken "
        f"(pairs seen: {dead})")
    assert any(s == "T" and "go" in off for s, off in nonempty), dead
    assert vacuity.classify_severity("P1", "dead_state_offered_pair") == "blocking"


# ---------------------------------------------------------------------------
# VAC-2 (2026-09-14): the tier split within the non-empty half.
#
# A guard `x > 1` with x ranging over the CHECKED domain {0..1} is dead at the
# instantiation FDR checks but fires symbolically — that is the deliberate
# narrowing (disclosed), not a Java gap, and must be ADVISORY
# (dead_state_offered_pair_checked). A pair dead in BOTH tiers (no operation
# consumes the advertised event at all) stays BLOCKING
# (dead_state_offered_pair). Observed live: LRE run-1's (OCM, {reqMOM}) —
# odist(cdyn) > 1 unsatisfiable at core_real={0..1} with stubbed odist.
#
# The machine below: state A listens for {go}; the only consuming operation
# has guard `x > 1`. Under the checked env x is narrowed to {0..1} -> dead;
# symbolically x is an unbounded int -> fires. Expect ADVISORY, phase passes.
NARROWED_THY = r"""
enumtype St = A | initial

enumtype Evt = go

zstore M =
  st::"St"
  x :: "int"
  tr :: "(St, Evt) tag list"
  listens :: "Evt set"
  offered :: "Evt set"
  where inv:
    "tr \<noteq> []
                      \<and> (st = A \<longrightarrow> listens = {go})"

zoperation InitialToA =
  over M
  pre "st= initial"
  update "[st\<Zprime>= A
         ,tr\<Zprime> =tr @ [State A]
         ,listens\<Zprime> = {go}
         ]"

zoperation AGo =
  over M
  pre "st= A \<and> go \<in> offered \<and> x > 1 \<and> offered \<subseteq> listens"
  update "[st\<Zprime>= A
         ,tr\<Zprime> =tr @ [State A]
         ]"

definition Init :: "M subst" where
  [z_defs]:
  "Init =
  [st\<leadsto> initial
  ,x\<leadsto> 0
  ,tr\<leadsto> [State initial]
  ,listens\<leadsto> {}
  ]"
"""


def _dead_pairs_two_tiers(thy_text):
    """Detection in both tiers, same surfaces the phase uses."""
    model = vsat.parse_theory(thy_text)
    out = {}
    for label, env in (("sym", vsat.Env(checked=False)),
                       ("chk", vsat.Env(checked=True))):
        reach = vsat.reachability(model, env).reachable
        out[label] = {(s, frozenset(o))
                      for s, o in vsat.dead_offered_pairs(model, reach, env)}
    return out


def test_vac2_narrowing_dead_pair_classified_checked_only():
    """x>1 at checked {0..1} is dead; symbolically it fires. The pair must be
    checked-only — the input condition for dead_state_offered_pair_checked."""
    tiers = _dead_pairs_two_tiers(NARROWED_THY)
    pair = ("A", frozenset({"go"}))
    assert pair in tiers["chk"], tiers
    assert pair not in tiers["sym"], tiers


def test_vac2_both_tier_dead_pair_detected_in_both():
    """An advertised-but-unconsumed event is dead in BOTH tiers — the input
    condition for the BLOCKING dead_state_offered_pair."""
    tiers = _dead_pairs_two_tiers(ADVERTISED_THY)
    pair = ("T", frozenset({"go"}))
    assert pair in tiers["chk"], tiers
    assert pair in tiers["sym"], tiers


def test_vac2_checked_kind_registered_advisory():
    """The registry itself: a re-merge or de-registration must fail here."""
    assert "dead_state_offered_pair_checked" in vacuity.ADVISORY_KINDS
    assert vacuity.classify_severity(
        "P1", "dead_state_offered_pair_checked") == "advisory"
    assert vacuity.classify_severity(
        "P1", "dead_state_offered_pair") == "blocking"
