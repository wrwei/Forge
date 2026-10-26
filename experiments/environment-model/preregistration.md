# Pre-registration — the environment model of the Isabelle chain

Date: 18 October 2026, before any theory was modified or any proof run.
Addresses the finding that the pinned environment model made guard-driven operations unsatisfiable.

## The finding, restated from the records

The Isabelle generator (`thy_generation_rule.egl`, generator change of 2026-09-16) pins
`offered = listens` as an invariant conjunct: each step, the environment offers
exactly the events the current state listens for. Under that conjunct

- every C2 trigger conjunct `e ∈ offered` is implied by the state alone;
- every operation whose precondition contains an absence conjunct
  `¬(e ∈ offered)` for a listened `e` is unsatisfiable (proof-dead).

Dead operations per current-extractor theory, counted today from the shipped
Forge theories (`handoff/dead_ops_all.json`):

All 64 theories in Forge that carry the `offered = listens` conjunct (the ladder copies under
`steam-boiler/.../isabelle-ladder-arm1` excluded as duplicates of one theory):

| folder / machine | theories affected | dead operations where affected |
|---|---|---|
| convergence-v3-frozen / LRE | 6 of 10 (runs 2–5; run-1's Java emitted no absence conjuncts) | 9 of 18 — every guard-driven transition out of MOM, HCM, CAM, incl. `MOMToCAM` |
| ablation-d2-diagnostic / LRE | 10 of 15 | 9 of 18 (same nine) |
| convergence-v3-frozen / SRanger | 4 of 5 | 1 of 8 (`TURNINGToMOVING`) |
| ablation-d2-diagnostic / SRanger | 7 of 7 | 1 of 8 |
| multicontroller-e / Movement(Controller) | 8 of 8 | 2 of 23--28 (`AvoidingAgainToAvoiding`, `AvoidingAgainToGettingOut`) |
| GasAnalysis(Controller), all folders | 0 of 18 | — |
| steam-boiler / BoilerController | 0 of 1 | — |
| **total** | **35 of 64** | |

## Why the fix is a model choice, not a sentence

The generated Java handles **one input event per step, possibly none the state
listens for** (`step(InputEvent event)`, verified in
`convergence-v3-frozen/lre/run-5/iter-1/java/controller/LreController.java`):
event-triggered branches are tested first, guard-only branches (OPEZ, collision,
obstacle) only when no event branch matched, and an implicit `else` leaves the
mode unchanged. The faithful environment assumption is therefore

    offered ⊆ listens  ∧  (offered = {} ∨ ∃e. offered = {e})        (model B)

i.e. *at most one listened event per step, the empty offer allowed*. A
**non-empty** singleton model was considered and is ruled out by inspection: a
guard-driven transition's precondition requires every higher-priority event to
be absent, which under a non-empty offer from the listen set is impossible — the
same operations would stay dead.

Under model B the empty offer is exactly the case the 2026-09-16 record calls
"refuted on chem": a state waiting for an event has no enabled operation when
no event arrives. In the Java that is the implicit `else` (an idle step), which
the Z-machine encoding never had. Model B therefore also emits, per state `X`,
an operation

    Idle_X :  pre  st = X ∧ ¬P₁ ∧ … ∧ ¬Pₙ        (Pᵢ = the other operations'
              update  offered' ∈ {{}} ∪ {{e} | e ∈ listens(X)}   preconditions at X,
                                                      minus the `st = X` conjunct)

which is the extracted form of Java's `else`. The environment's next offer is a
nondeterministic operation parameter drawn from that set, for every operation.

## What deadlock-freedom means under each model (to be stated in Section 5.5)

- **Model A** (`offered = listens`, current): deadlock-freedom is proved for an
  environment that always offers every listened event at once; guard-driven
  transitions guarded by event absence are unreachable in the verified theory.
- **Model B**: deadlock-freedom is a **totality check** on the extracted step
  relation — in every reachable state and under every allowed offer, some
  operation (possibly `Idle_X`) is enabled. It is not a progress property. The
  behavioural content of the Isabelle chain is the invariant-preservation
  lemmas, which hold or fail independently of the model.
- In both models the tock-CSP/FDR4 analysis is a separate semantics and its
  deadlock-freedom result is not interchangeable with the Isabelle one.

## The probe

1. Rewrite the five probe theories from model A to model B mechanically
   (script `envmodel_b_rewrite.py`; the generator is patched only if B is
   adopted). Probe set: LRE run-5/iter-1 (the audit's witness, 9 dead ops),
   SRanger run-1/iter-1, GasAnalysis run-1/iter-1, Movement run-1/iter-1,
   Steam Boiler rerun-2026-09-28.
2. For each theory build four arms in the Docker Isabelle used by the pipeline
   (proof timeout 1800 s per build):
   - `NoDlf` — all invariant lemmas, deadlock-freedom lemma removed;
   - `M` — deadlock-freedom closed by `metis St.exhaust_disc Evt.exhaust_disc
     insertCI` (+ used-enum facts, as in the 2026-09-16 tactic);
   - `Au` — closed by `using St.exhaust_disc Evt.exhaust_disc by auto`;
   - `Bl` — closed by `by blast`.
3. Record per arm: pass / named failing goal / timeout / memory kill.
   Timeouts and kills are environment outcomes, not verdicts.

## Decision rule (fixed now)

- **Adopt B** if, for all five theories, `NoDlf` passes and at least one of
  `M`/`Au`/`Bl` passes. Then: patch the generator, regenerate, rerun the
  Isabelle column of the 186-mutant campaign and the tierB scoring under B,
  rewrite Section 5.5 (C2 paragraph and the deadlock-freedom statement), and
  disclose the model-A dead-operation counts above as the state of the frozen
  runs.
- **Fall back to A** if any `NoDlf` fails under B (an invariant lemma that
  depends on the environment model is itself a finding and is reported), or if
  deadlock-freedom closes in no arm for some theory after the budget. Then:
  keep `offered = listens`, disclose in Section 5.5 that deadlock-freedom is
  conditional on maximal cooperation and that the operations listed above are
  unreachable in the verified theories, and re-score tierB under model A so the
  harness and the proofs share one environment.
- **Either way**: the Phase 6d vacuity audit and the pin-consistency regression
  test gain a per-operation satisfiability check of `pre ∧ inv`; the probe
  scripts and outputs are shipped in the artefact repository.

## Expected outcome (stated before running)

`NoDlf` is expected to pass everywhere: the new conjunct is re-established by
every update by construction (offered' is drawn from the target's listen set),
and `Idle_X` changes no other field. The deadlock-freedom arms are the open
question: the residual goal is propositional over the guard atoms once the
`Idle_X` complement is in place, but the LRE preconditions carry long real
arithmetic terms and `metis` may not terminate; `auto` is the likelier closer.

## Status (20 October 2026)

Probe kit: `scripts/envmodel_b/` in the research repository (rewriter, runner,
`results/`). Two runs so far, both cut short by Docker Desktop quitting:

| study | A0 | NoDlf | Au | M | Bl |
|---|---|---|---|---|---|
| LRE (run-5/iter-1) | PASS 165 s | **PASS 225 s** | TIMEOUT 1800 s | unmeasured (Docker quit) | stack exhaustion 443 s |
| SRanger, Gas, Movement, Boiler | not reached | | | | |

The invariant half of the decision rule holds on the LRE under model B. The
totality goal does not close with generic automation on the LRE; `metis` is
unmeasured. Neither outcome triggers the fall-back yet (the rule requires the
budget to be exhausted in every arm). Next step before further machine time: a
propositional tautology lemma over abstracted atoms, or a per-state split of the
goal, so the closer never looks inside the arithmetic.

## Probe iteration 2 — encoding B2 (20 October 2026, after run 3)

Run 3 (`results/2026-10-20-run3/`) reached three studies:

| study | A0 | NoDlf | Au | M | Bl |
|---|---|---|---|---|---|
| LRE | PASS | PASS | TIMEOUT 1800 s | TIMEOUT 1837 s | stack exhaustion 367 s |
| SRanger | PASS 50 s | **PASS 70 s** | TIMEOUT 1824 s | **FAIL, goal printed** (60 s) | stack exhaustion 216 s |
| Gas | PASS 60 s | **PASS 100 s** | — | **FAIL, goal printed** (105 s) | FAIL, 2 goals printed (180 s) |

The invariant lemmas close on every study reached. The printed residual goals
were checked by hand and are **valid** — for every state and every allowed
offer one disjunct holds (on SRanger, `TURNINGToMOVING`, dead under model A,
is the disjunct that fires for `TURNING`/`tick` once the turn has elapsed). The
closers fail on the *form* of the hypothesis: `offered ⊆ listens ∧ ∃e. offered
= {e}` requires subset/singleton reasoning before a case split is available;
`metis` lacks the lemmas and `blast` recurses into the stack limit.

Encoding B2 states the same model as a per-state literal disjunction,
`st = X ⟶ (offered = {} ∨ offered = {e₁} ∨ …)` for every `St` literal
(unpinned states: `offered = {}`), so the case split is in the hypothesis and
membership reduces to enum distinctness. The rewriter asserts that every
update's `listens'` literal equals the target state's pin (held on all five
theories). Model, Idle construction and decision rule are unchanged; theories
and sessions are renamed `_B2_` so no run-1–3 row can be reused for them
(A0 rows are reused). Tactic order is now `auto`, `metis` (+ `Evt.distinct
singletonD`), `blast`.

## Probe iteration 3 — encoding B3 (20 October 2026, after run 4)

Run 4 (B2, `results/2026-10-20-run4/`): LRE A0 PASS 186 s, NoDlf PASS 321 s,
Au TIMEOUT 1837 s, M TIMEOUT 1823 s. Diagnosis from the goal structure
(`handoff/lre_goal_structure.json`): with `Idle_X` written as the negation of
every other precondition, the totality disjunction for LRE/MOM (8 operations,
13 guard atoms of up to 720 characters) has at least 179,200 CNF clauses and
≈5⁸ tableau branches; SRanger's worst state has 18 clauses, which is why it
produced a goal and LRE did not. The Boiler would have been 3.6×10⁷.

B3 writes `Idle_X` in priority-chain form, `¬G₁ ∧ … ∧ ¬Gₙ` over the
operations' raw guards with the C1 negations stripped — the Java `else`
branch literally — which is logically the same operation (the rewriter checks
`⋀¬Pᵢ ⟺ ⋀¬Gᵢ` propositionally, exhaustively up to 18 atoms) but gives the
totality goal polynomial branching. LRE `Idle_MOM` shrinks from 3,300 to 1,107
characters (24 redundant negations removed). Two states turn out to need no
`Idle` at all because their chain already ends in an unconditional branch:
Gas `GasDetected` (an if/else pair) and every Boiler state (explicit
self-loops) — the Boiler's step relation is total by construction. Model,
parameters and decision rule unchanged; sessions renamed `_B3_`.

## Probe iteration 4 — structured closers (20 October 2026, after run 5)

Run 5 (B3): LRE A0 PASS 191 s, NoDlf PASS 257 s, Au TIMEOUT, M TIMEOUT,
Bl stack exhaustion 332 s; SRanger A0 PASS 45 s, NoDlf PASS 66 s (run still
going when read). The chain form was necessary but not sufficient on the LRE.

Cause: `deadlock_free` leaves **one** goal for the whole machine with the
state pins as implications (`st = MOM ⟶ …`) in the hypotheses, so a single
generic closer must do the 5-way state split, the ≤4-way offer split per
state, the event-membership evaluation and the guard tautology in one search,
carrying the 720-character atoms through all of it.

Iteration 4 adds arms that make the splits explicit before any closer runs:
`case_tac st; simp; elim disjE; simp` (membership atoms reduce to True/False by
enum distinctness; other states' implications vanish), then `blast+` (S1) or
`metis …+` (S2) on the remaining per-branch guard-chain tautology. A diagnostic
arm `Dg` (`apply deadlock_free` then `oops`) times the method alone and never
counts as a pass. Decision rule otherwise unchanged: adopt B iff every study
has NoDlf = PASS and one of S1/S2/Au/M/Bl = PASS.

## Decision (20 October 2026, run 6)

| study | A0 (shipped, model A) | NoDlf (model B invariants) | S1 (model B totality) |
|---|---|---|---|
| LRE | PASS 166 s | PASS 211 s | PASS 267 s |
| SRanger | PASS 40 s | PASS 71 s | PASS 75 s |
| Gas | PASS 56 s | PASS 85 s | PASS 76 s |
| Movement | PASS 236 s | PASS 734 s | PASS 296 s |
| Steam Boiler | PASS 1240 s | PASS 858 s | PASS 1793 s |

Every study: all invariant lemmas close under model B, and the totality lemma
closes with the explicit-split closer (S1). Verified from the build logs:
`Finished` on all ten builds, lemma present, no `oops`, no warnings. The
diagnostic arm shows the `deadlock_free` method itself dominates (LRE 186 s of
267 s; Boiler 1200 s of 1793 s); the split-and-close step is cheap.

**Outcome under the pre-registered rule: ADOPT model B.** Evidence:
`scripts/envmodel_b/results/2026-10-20-run6-decision/` (summary, build logs,
and the ten proved theories).

Follow-up, in order: (1) generator patch — the 2026-09-16 sites (invariant conjunct,
`nxt` parameter and `offered' = nxt`, Init) plus chain-form `Idle_X` emission
and the S1 closing script; (2) regenerate the five theories from the pipeline
and check they match the rewriter's output and verdicts; (3) tierB re-score
under model B and the Isabelle column of the 186-mutant campaign; (4) Section
5.5 rewrite — environment model stated, C2 paragraph, deadlock-freedom as a
totality check, model-A dead-operation counts disclosed for the frozen runs;
(5) per-operation `pre ∧ inv` satisfiability check in the Phase 6d audit.

## Generator patch verified (20 October 2026, run 7)

The patched generator's output for all five frozen-run models (seven theories:
the two multi-controller runs each produce a second controller) was proved in
the Docker Isabelle (`results/2026-10-20-run7-regenerated/`): LRE 47 lemmas
256 s, SRanger 25 lemmas 60 s, GasAnalysisController 23 lemmas 70 s,
MovementController 61 lemmas 432 s, GasAnalysis 23 lemmas 65 s, Movement 63
lemmas 422 s, BoilerController 77 lemmas 1041 s. Every build `Finished`, no
warnings, no open proof. The generator reproduces the probe result; the
Phase 6d check `precondition_unsat_env` reports nothing on these theories.

## Follow-up 3 — guard-level conformance and the mutation campaign's Isabelle column under model B (20 October 2026)

**Guard-level enumeration (tierB).** Re-run on theories the repaired generator
extracts from the reference-run Java (`experiments/environment-model/tierB-modelB/`):
100% on all three studies, as under model A; and in every one of the 7,328
cells exactly one operation is enabled (the `Idle_X` operation in the 2,412
cells where no Java branch applies). The archived `prefix` and model-A `fixed`
rows are reproduced byte-identically by the extended harness.

**Mutation campaign, Isabelle column — pre-registration, written before the
run.** The 186 archived per-mutant `robochart_model.xmi` were regenerated with
today's generator under both encodings. Theory visibility is identical under A
and B (125 of 186 mutants change at least one `_Beh.thy`; the archived
campaign counted 86 because its Isabelle arm covered only the Gas-analysis
controller for the Chemical Detector and the generator has since gained the
data-variable updates — 13 mutants differ from the archived flag for those two
reasons, none because of the environment model). The rerun builds the 131
changed theories plus the three baselines (`scripts/envmodel_b/mutation-modelB/`).

Expectations, stated before running:
1. The three baselines verify.
2. The 11 archived Isabelle timeouts resolve to verdicts: their `metis` hang was
   the dead-operation goal, which the explicit-split closer does not face.
3. Kills come from invariant lemmas only: the totality lemma holds by
   construction (`Idle_X` is the complement of the chain), so a mutant that
   survives every invariant lemma verifies.
4. Kills are at least the archived 1 and may rise, since the nine LRE
   operations whose lemmas were vacuous under model A are live under B.

Reporting rule: the archived column stays as collected (Table 10); the model-B
column is reported beside it as the repaired-generator result. `timeout`,
`MEMKILL`, `HARDKILL` rows are environment outcomes, rerun before any count.

**Outcome (21 October 2026).** All four expectations held. 131 of 131 builds
reached a verdict: 130 verified, 1 ill-typed theory (`chemical_detector_trigger_swap_000`,
the archived kill, same cause). The 11 archived timeouts all verify. No verdict
changed in either direction. Evidence: `experiments/environment-model/mutation-modelB/`.
Environment outcomes during the run (session-name hyphen; Docker OOM kills from an
unread heap ceiling) are recorded there and were rebuilt, never counted.
