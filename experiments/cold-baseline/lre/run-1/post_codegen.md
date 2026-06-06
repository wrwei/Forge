# Post-codegen review — SRanger (cold iter-1)

**Status**: uncertain

**Summary**: Cold codegen produced 9 Java source files implementing the SRanger
three-mode controller (Moving / Turning / Final) per the requirement set. The
build is clean. Four design choices and one M2M-driven structural deviation
require user review.

## Issues

### 1. Move event split into MoveLv + MoveAv records

- **Kind**: `design_choice`
- **Affected requirements**: SR-DM4, SR-FR1, SR-FR2, SR-FR3, SR-Beh2, SR-Beh3,
  SR-Beh5, SR-Beh6
- **fix_directive**: The spec (SR-DM4) defines a single output event type
  `Move(lv : real, av : real)`. The Java/M2M pipeline extracts at most the
  FIRST argument of an `OutputEvent` constructor (see
  `forge.transformations/src/main/resources/transformations/java2robochart.etl`
  `extractOutputEventInfo`, line 3577ff. and `buildCommunication`, line 3640ff.).
  A single `Move(double lv, double av)` record would silently drop `av`. To
  preserve both semantic channels, `OutputEvent` was split into two
  single-payload variants `MoveLv(lv)` and `MoveAv(av)`. Each transition's
  entry action issues both events in sequence (lv then av). The corresponding
  RoboChart `.rct` will declare two events `moveLv : real`, `moveAv : real`
  instead of a single tuple-typed `move`. If the user prefers a unified
  `move` channel, the workaround is to define a payload record (e.g.
  `record MoveCmd(double lv, double av)`) and extend the M2M to extract
  multi-field constructor arguments — out of scope for cold iter-1.
- **java_trace**:
  - `src/main/java/sranger/event/OutputEvent.java:7-12` — `OutputEvent.MoveLv`, `OutputEvent.MoveAv` (`requirement_ids`: ["SR-DM4"])
  - `src/main/java/sranger/controller/SRangerController.java:42-67` — pair-call pattern in step() (`requirement_ids`: ["SR-FR1", "SR-FR2", "SR-FR3", "SR-Beh2", "SR-Beh3", "SR-Beh5", "SR-Beh6"])

### 2. turnDuration stored as TURN_DURATION_MS (milliseconds), not seconds

- **Kind**: `invented_default`
- **Affected requirements**: SR-DM2, SR-Var1, SR-GP2, SR-Beh5
- **fix_directive**: The spec (SR-DM2) defines `turnDuration : real = 2.0
  seconds`. The constant in `SRangerConstants` was renamed
  `TURN_DURATION_MS = 2000.0` (milliseconds). Reason: the canonical M2M clock
  pattern is `clock.nowMs()` (see CLAUDE.md "Generated RoboChart structure"
  and ETL line 2587ff.), so the elapsed-time guard
  `clock.nowMs() - clockResetTime >= TURN_DURATION_MS` must compare like
  with like. Both values are in ms. The semantics are equivalent (2.0 s =
  2000 ms). If the user prefers seconds, the alternative is `now()` returning
  seconds — but that diverges from the M2M's hard-coded `nowMs()` matcher.
- **java_trace**:
  - `src/main/java/sranger/constants/SRangerConstants.java:16-17` — `TURN_DURATION_MS` (`requirement_ids`: ["SR-DM2"])
  - `src/main/java/sranger/controller/SRangerController.java:36` — `turnDurationElapsed` predicate (`requirement_ids`: ["SR-GP2", "SR-Beh5"])

### 3. Final state has no outgoing transitions

- **Kind**: `design_choice`
- **Affected requirements**: SR-FR3, SR-DC1
- **fix_directive**: Final is a terminal mode per SR-FR3 ("the controller
  issues a stop command... terminal"). The Java mode block for `Final`
  contains no transitions, by spec. Implications:
  - The M2M's deadlock-lint (java2robochart.etl line 686) explicitly skips
    states named exactly `Final` — confirmed; my enum literal uses the
    matching case (`Final`, not `FINAL`).
  - The Isabelle Z-machine `deadlock_free` proof handles absorbing Final
    states via `St.exhaust_disc` (per ETL comment at line 684), so the
    proof should still close.
  - FDR4 will see `STOP` for this state, which is the intended terminal
    behaviour, not a verification failure.
  - If a strictly-deadlock-free CSP is required, add an event-triggered
    self-loop (e.g. `if (event instanceof InputEvent.Tick) { currentMode =
    SRangerMode.Final; }`) to give Final a bare-precondition operation
    without changing observable behaviour.
- **java_trace**:
  - `src/main/java/sranger/controller/SRangerController.java:65-69` — Final mode block (`requirement_ids`: ["SR-FR3", "SR-DC1"])

### 4. obstacleDetected guard conjoined with InputEvent.Obstacle

- **Kind**: `design_choice`
- **Affected requirements**: SR-GP1, SR-Beh2
- **fix_directive**: SR-Beh2 says the Moving -> Turning transition fires "when
  the obstacle event is received." SR-GP1 says the predicate
  `obstacleDetected = (distance <= obstacleThreshold)` is "used as the guard
  on the Moving -> Turning transition." The implementation conjoins both:
  `event instanceof InputEvent.Obstacle && obstacleDetected`. This is
  defensive - the obstacle event already implies the threshold was crossed
  (per the system description), so the guard re-check is structurally
  redundant. If the user wants the transition to fire on the event alone
  (and drop the predicate), remove the `&& obstacleDetected` conjunct.
- **java_trace**:
  - `src/main/java/sranger/controller/SRangerController.java:34` — `obstacleDetected` predicate (`requirement_ids`: ["SR-GP1"])
  - `src/main/java/sranger/controller/SRangerController.java:45` — Moving -> Turning branch (`requirement_ids`: ["SR-Beh2"])

### 5. Initial-state entry action (Move(moveVel, 0)) not explicitly emitted

- **Kind**: `scope_question`
- **Affected requirements**: SR-FR1, SR-Beh1
- **fix_directive**: SR-FR1 says "on entering Moving, the controller issues
  Move(moveVel, 0)". On power-up (SR-Beh1), the controller is in Moving but
  no `step()` has run, so no actuator call has been issued yet. The
  implementation relies on the M2M's "common incoming action" lifting
  heuristic (EGL template) to promote the `MoveLv(moveVel) ; MoveAv(0)` pair
  from the Turning -> Moving transition into Moving's `entry` action - which
  then fires on first state entry per RoboChart semantics. If the heuristic
  doesn't lift it (e.g., because there's only one incoming transition into
  Moving and the EGL needs more evidence), the initial Move command will not
  be issued and the actuator's lastLv/lastAv will read 0.0 on power-up
  instead of 1.0/0.0. Verify by inspecting the generated `.rct` for
  `state Moving { entry moveLv ! 1; moveAv ! 0 }`. If absent, add an explicit
  `init()` method or constructor-time actuator call.
- **java_trace**:
  - `src/main/java/sranger/controller/SRangerController.java:14` — `currentMode = SRangerMode.Moving` initialiser (`requirement_ids`: ["SR-Beh1"])
  - `src/main/java/sranger/controller/SRangerController.java:58-59` — Turning -> Moving entry pair (`requirement_ids`: ["SR-FR1"])

## Next step

User to run dashboard phases (compile -> coverage -> preflight -> t2m -> m2m
-> m2t -> fdr4 -> dafny_gen -> dafny_verify -> isabelle_gen ->
isabelle_verify) and feed any failures back via `/fix-from-feedback`.
