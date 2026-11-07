# ITER_SUMMARY — sranger, run 2

## Iteration 1

No Java was present at the start, so nothing was deleted.

**What I wrote (package `sranger`, 9 files):** the `RoboChartType` annotation; sealed `InputEvent` (`Obstacle`, `Tick`, `EndTask`) and `OutputEvent` (`Move(lv, av)`); `Sensor.distance()`; `Actuator.apply(OutputEvent)`, which stores the last lv/av; the `SRangerMode` enum (MOVING first, TURNING, FINAL); `SRangerConstants`; a `Clock`; and `SRangerController.step`. `step` has two predicates, `obstacleDetected` and `turnDurationElapsed`, and one block per mode.

**Decisions the requirements did not settle:**
- Moving→Turning is triggered by `obstacle` and guarded by `obstacleDetected` (SR-Beh2 and SR-GP1 together).
- In Turning, the timed transition comes before `tick`, and the `tick` self-loop is guarded by `!turnDurationElapsed`. Tick arrives every cycle, so otherwise Turning would never end.
- `endTask` is the first branch in Moving and Turning.
- The initial Move(moveVel, 0) of Moving is issued in the constructor.
- The clock field is named `timer`, because `clock` is reserved.
- Invented defaults: the sensor returns 100.0 m when it has no reading or a negative one; the actuator stores (0, 0) before the first command; time starts at 0.
- Final is terminal, and `obstacle` in Turning is ignored.

**Not resolved:** `obstacleThreshold = 0.5` becomes 1 in the model's constants interface, so the model's guard is looser than the Java's.

All of these are recorded in `post_codegen.md`.
