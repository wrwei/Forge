# SRanger cold baseline — run 8 summary

Single-shot Java implementation of SRanger per the case-study system
description and `requirement_all.json`. No verifier feedback consumed.

## Layout

- `sranger.annotation` — `@RoboChartType`, `@Clock`, `@SensorService`.
- `sranger.event` — `InputEvent` (sealed: `Obstacle`, `Tick`, `EndTask`)
  and `OutputEvent` (sealed: `Move(lv, av)`).
- `sranger.mode` — `SRangerMode` enum (`Moving`, `Turning`, `Final`).
- `sranger.constants` — `SRangerConstants` (`moveVel=1.0`, `turnVel=2.0`,
  `obstacleThreshold=0.5`, `turnDuration=2.0`).
- `sranger.sensor` — `Sensor` exposing `distance()` with a large default
  for un-initialised state.
- `sranger.actuator` — `Actuator` storing the last issued `Move`.
- `sranger.clock` — `Clock` providing `nowSeconds()` / `nowMs()`.
- `sranger.controller` — `SRangerController` with a single-method
  mode-nested if-else state machine.

## Design notes

- Initial mode `Moving`; the constructor issues `Move(moveVel, 0)` as the
  initial-entry action of Moving.
- `endTask` is duplicated as the highest-priority inner branch in both
  Moving and Turning.
- Tick self-loops in Moving and Turning provide event-triggered
  bare-precondition cover for the Isabelle deadlock-free proof
  (avoids the τ-self-loop nondeterminism trap).
- Guard predicates `obstacleDetected` and `turnDurationElapsed` are
  declared as named booleans before the outer chain, as simple
  comparisons only.
