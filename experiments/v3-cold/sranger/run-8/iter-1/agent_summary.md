# ITER_SUMMARY: sranger, pass 8, iteration 1

There was no Java in the project before writing, so nothing was deleted.

**What was written.** Nine files under `sranger/`:
- `annotation/RoboChartType`
- `event/InputEvent` (Obstacle, Tick, EndTask) and `event/OutputEvent` (Move(lv, av))
- `sensor/Sensor` (distance()) and `sensor/Clock` (now(), advance())
- `actuator/Actuator` (apply(Move), last velocities)
- `controller/SRangerMode` (MOVING, TURNING, FINAL), `SRangerConstants` (the four real constants) and `SRangerController` (step())

Also written: `result_codegen.json` (covers 23/23 requirements and all 9 files) and `post_codegen.{md,json}`. The code compiles with javac and with Gradle. A scratch smoke run exercised all seven transitions.

**Decisions the requirements did not settle.**
1. Moving→Turning needs the Obstacle event and obstacleDetected together (SR-Beh2 with SR-GP1).
2. Turning's branch order is endTask, then the timed return, then tick guarded by `!turnDurationElapsed`. A tick arrives every cycle, so putting tick first would stop the timed return from ever firing in Java.
3. The power-up Move(moveVel, 0) is issued in the constructor (SR-FR1).
4. The framework advances the Clock. The field is named `timer` because `clock` is reserved. clockResetTime is reset on Moving→Turning, before Move(0, turnVel) is issued.
5. The no-reading distance is 100.0 m, an invented value. Negative and NaN readings are ignored.
6. obstacleDetected uses `<=` (SR-GP1), although SR-DM3 says "crosses below".
7. endTask is the first branch in Moving and in Turning. Final has no transitions and an empty block.

**Not resolved.**
- obstacleThreshold = 0.5 rounds up to 1 in the model's constants, which weakens the guard as modelled. The requirement fixes 0.5, so the Java keeps it.
- I do not know whether the extractor keeps the constructor's initial command, or how it types Move's two real components.
