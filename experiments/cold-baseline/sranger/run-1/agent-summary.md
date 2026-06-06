# SRanger cold baseline — run 1

Single-shot Java implementation generated from the SRanger system
description and `requirement_all.json`, with no prior session state and
no verifier feedback.

## Structure

Packages under `sranger.*`:
- `annotation` — `@RoboChartType`, `@Clock`.
- `mode` — `SRangerMode` enum (Moving, Turning, Final).
- `event` — sealed `InputEvent` (Obstacle, Tick, EndTask) and sealed
  `OutputEvent` with the single `Move(lv, av)` record.
- `constants` — `SRangerConstants` with moveVel, turnVel,
  obstacleThreshold, turnDuration.
- `sensor` — `Sensor.distance()` with a large default when uninitialised.
- `actuator` — `Actuator` storing the latest `Move`.
- `clock` — `Clock` (class name auto-detected by the M2M).
- `controller` — `SRangerController` with a single `step(InputEvent)`
  method.

## State machine

Mode-nested pure two-level if-else. Named boolean predicates
`obstacleDetected` (SR-GP1) and `turnDurationElapsed` (SR-GP2) declared
before the chain. Initial Moving entry action `Move(moveVel, 0)` issued
in the constructor. EndTask handled as the highest-priority branch in
both Moving and Turning. Turning → Moving is the autonomous branch
guarded by `turnDurationElapsed`; clockResetTime is captured on entry
to Turning via `clock.now()`. Final has no outgoing transitions.

No lambdas, streams, ternaries, or pattern-matching instanceof — all
casts are on separate lines.
