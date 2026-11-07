# ITER_SUMMARY — sranger, run 2

No Java was present at the start, so nothing was deleted.

**What I wrote (package `sranger`, 9 files):** the `RoboChartType` annotation; sealed `InputEvent` (`Obstacle`, `Tick`, `EndTask`) and `OutputEvent` (`Move(lv, av)`); `Sensor.distance()`; an `Actuator` that stores the last lv/av; the `SRangerMode` enum (MOVING first); `SRangerConstants`; a `Clock`; and `SRangerController.step`. `step` has the predicates `obstacleDetected` and `turnDurationElapsed` and one block per mode.

**Decisions the requirements did not settle:**
- Moving→Turning is triggered by `obstacle` and guarded by `obstacleDetected` (SR-Beh2 and SR-GP1).
- In Turning, the timed transition comes before `tick`, and `tick` is guarded by `!turnDurationElapsed`. Tick arrives every cycle, so otherwise Turning would never end.
- `endTask` is the first branch in Moving and Turning.
- The initial Move(moveVel, 0) is issued in the constructor.
- The clock field is named `timer`, because `clock` is reserved.
- Invented defaults: the sensor returns 100.0 m when it has no reading or a negative one; the actuator stores (0, 0) before the first command; time starts at 0.
- Final is terminal, and `obstacle` in Turning is ignored.

**Not resolved:** `obstacleThreshold = 0.5` becomes 1 in the model's constants interface.

**Iteration 2:** preflight error: annotated `Sensor.NO_READING_DISTANCE` with `@RoboChartType("real")`. I accepted the rule-8 warning (the timed branch comes after `endTask`). Putting it first would make the Java lose an `endTask` that arrives in the same cycle the turn time runs out. The model allows both outcomes, so it covers the Java's behaviour.

Details are in `post_codegen.md`.
