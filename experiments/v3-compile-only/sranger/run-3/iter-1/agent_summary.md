# SRanger — run 3, iteration 1

## What was written

Nine Java files in package `sranger` (no Java was present beforehand):
`annotation/RoboChartType`; `event/InputEvent` (Obstacle, Tick, EndTask) and
`event/OutputEvent` (Move(lv, av)); `controller/SRangerMode` (MOVING first,
TURNING, FINAL), `SRangerConstants` (the four SR-DM2 reals), `Clock`,
`SRangerController`; `sensor/Sensor` (`distance()`); `actuator/Actuator`
(`apply`, last lv/av). `step()` has two predicates (`obstacleDetected`,
`turnDurationElapsed = timer.now() - clockResetTime >= TURN_DURATION`) and
the profile's two-level if-else with one branch per SR-Beh2..7. The trace
covers all 23 requirements and all 9 files.

## Decisions the requirements did not settle

- Moving→Turning uses trigger `obstacle` **and** guard `obstacleDetected`
  (SR-Beh2 names the event; SR-GP1 names the guard on the same transition).
- Turning order: endTask, autonomous turnDurationElapsed, then
  `tick && !turnDurationElapsed`. A tick branch above the autonomous one would
  starve Turning→Moving in Java.
- Power-up `Move(moveVel, 0)` (SR-FR1 entry of the initial mode) is issued in
  the constructor.
- Clock field named `timer` (`clock` is reserved); the clock is advanced by the
  framework, which keeps the controller deterministic.
- Invented defaults: no-reading distance 1000.0 m; actuator reports 0/0 before
  any command; clockResetTime starts at 0.0.
- Final has no outgoing transitions (terminal), as SR-FR3 implies.

## Unresolved

- The model's constants interface rounds OBSTACLE_THRESHOLD 0.5 up to 1. I kept
  metres, as the requirements state them, and flagged it in post_codegen.
- Whether the extractor carries the constructor's initial Move into the model
  is unknown until the pipeline runs.
