# LRE run 1 — iteration 4

## What changed and why
Iteration 3 reached 11/12; only `isabelle_verify` failed, with
`*** Timeout` on `LreController_deadlock_free` (620s against the 600s budget).
Iteration 1 had *passed* the same proof in 464s, so the margin was already thin
and iteration 2's change consumed it.

Reading the generated theory showed why, and it is a property of my Java design
rather than a marginal timing wobble. The extractor **inlines an operation
field's defining expression at every read**. `CalcCPA` had five intermediate
fields — `relNs`, `relEw`, `relNsVel`, `relEwVel`, `closingSpeedSq` — which
exist only because `compute()` may not declare locals. Each read expanded in
place, so `cda` became a ~1200-character nested term, `relNsVel` appearing four
times fully expanded inside it. That term was replicated in the `update` block
of all 18 zoperations *and* inlined into the MOM→CAM / HCM→CAM preconditions and
their negations in every sibling branch.

Fix: moved the closest-point-of-approach arithmetic behind two Sensor functions,
`tcpaTo(index)` and `cdaTo(index)` — the same shape `odist`/`hdist`/`vdist`
already have under LRE-DM5/SF1-3. Sensor methods may use locals, so the
intermediates are now genuinely local and never reach the model. `CalcCPA` keeps
three fields (`cdyn`, `tcpa`, `cda`) and its `compute()` is three assignments.
The theory should now carry `cda' = cdaTo((cdyn))` and a precondition
`cdaTo((cdyn)) < minsafedist()` in place of the giant terms, and the zstore drops
from 13 state variables to 8. `safeClosingSpeedSq` is now private, so it is no
longer lifted as a RoboChart function. `./gradlew build` clean; trace refreshed.

Names were chosen to avoid the collision that broke iteration 2: a Sensor method
must not share a name with an operation field, because both land in the same
RoboChart namespace — hence `cdaTo`/`tcpaTo`, not `cda`/`tcpa`.

## Decision this forced
LRE-OP5 says CalcCPA "accesses the obstacle's relative position and velocity
from the Sensor", which reads as putting the CPA formulas in CalcCPA. The
verifier budget makes that untenable, so the formulas now live in the Sensor
alongside the other derived distance functions and CalcCPA consumes them.
CalcCPA still owns and publishes `cda` and `tcpa`. Flagged for review.

## Other open judgements
Unchanged, in `post_codegen.{md,json}`: HCM entry velocity 0 vs 0.1 m/s;
`nsRelDist`/`ewRelDist` no-obstacle default (DM5 zero vs OP5 large-distance —
followed OP5); `SAFE_LARGE_DISTANCE = 1000.0`; transition priority; dense-list
`ObstacleRegister`. The vacuity advisory `dead_state_no_offer (OCM, {})` is
non-blocking by the audit's own classification.
