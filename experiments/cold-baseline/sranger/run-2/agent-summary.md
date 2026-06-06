# SRanger cold-baseline run 2 — agent summary

Single-shot implementation of the SRanger reactive controller from the case-study
description and `requirement_all.json`. No prior session, no verifier feedback.

## Package layout (`sranger.*`)

- `annotation.RoboChartType`, `annotation.Clock` — source-retention markers.
- `constants.SRangerConstants` — `moveVel=1.0`, `turnVel=2.0`,
  `obstacleThreshold=0.5`, `turnDuration=2.0` (SR-DM2).
- `mode.SRangerMode` — `Moving`, `Turning`, `Final` (SR-DM1).
- `event.InputEvent` — sealed: `Obstacle`, `Tick`, `EndTask` (SR-DM3).
- `event.OutputEvent` — sealed: `Move(lv, av)` (SR-DM4).
- `sensor.Sensor` — `distance()` with a large default for missing data
  (SR-DM5, SR-SF1).
- `actuator.Actuator` — stores last `lv`/`av` (SR-DM6).
- `clock.Clock` — `nowSec()` real-time source for `turnDurationElapsed`.
- `controller.SRangerController` — single-method mode-nested `step(InputEvent)`.

## Controller shape

Named predicates `obstacleDetected = distance() <= obstacleThreshold`
(SR-GP1) and `turnDurationElapsed = nowSec() - clockResetTime >= turnDuration`
(SR-GP2). Outer chain is pure `currentMode == X`. `EndTask` is the
highest-priority inner branch in both Moving and Turning (SR-Beh3, SR-Beh6).
Transitions SR-Beh2/4/5/7 implemented with entry actions inline; Final
self-loops to preserve deadlock-freedom.

Initial entry action `Move(moveVel, 0)` issued from the constructor (SR-FR1,
SR-Beh1). No lambdas, streams, ternaries, or pattern-matching instanceof.
