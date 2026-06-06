# SRanger Baseline Run 3 — Agent Summary

Single-shot cold-baseline implementation of the SRanger reactive controller
from `pipeline.assets/case-studies/sranger/`.

## Files produced (under `src/main/java/sranger/`)

- `annotation/RoboChartType.java`, `annotation/Clock.java` — pipeline annotations
- `mode/SRangerMode.java` — `Moving` (initial), `Turning`, `Final`
- `event/InputEvent.java` — sealed: `Obstacle`, `Tick`, `EndTask` (signals)
- `event/OutputEvent.java` — sealed: `Move(lv, av)` (real, real)
- `constants/SRangerConstants.java` — `moveVel=1.0`, `turnVel=2.0`, `obstacleThreshold=0.5`, `turnDuration=2.0`
- `sensor/Sensor.java` — `distance()` with safe default
- `actuator/Actuator.java` — stores last `Move`
- `clock/Clock.java` — `nowMs()` source for the timed transition
- `controller/SRangerController.java` — mode-nested if-else state machine

## Design notes

- Single-method controller with named predicates `obstacleDetected` and
  `turnDurationElapsed` declared before the if-else chain.
- `endTask` is the highest priority in each non-terminal mode, duplicated.
- `Moving -> Turning` records `clockResetTime = clock.nowMs()` and emits
  `Move(0, turnVel)`; `Turning -> Moving` (autonomous) emits `Move(moveVel, 0)`.
- Entry actions inline at each transition; initial `Move(moveVel, 0)` issued
  from the controller constructor (SR-FR1 + SR-Beh1).
- `Final` carries an unconditional `tick` self-loop so the state retains
  bare-precondition cover for the Isabelle `deadlock_free` tactic.
- Traditional `instanceof` + explicit cast; no ternaries, lambdas, or streams.
