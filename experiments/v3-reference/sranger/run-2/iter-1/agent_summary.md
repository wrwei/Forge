# SRanger run 2 — iteration 1

No Java was present at the start.

## What I wrote

Nine files under `sranger/`:

- `annotation/RoboChartType`
- `event/InputEvent` (`Obstacle`, `Tick`, `EndTask`) and `event/OutputEvent` (`Move(lv, av)`)
- `controller/SRangerMode` (`MOVING` first), `SRangerConstants` and `SRangerController`
- `sensor/Sensor` (`distance()`) and `sensor/Clock` (`now()`)
- `actuator/Actuator` (`apply`, which records the last command)

`step()` has two predicates:

- `obstacleDetected`: `distance() <= OBSTACLE_THRESHOLD`
- `turnDurationElapsed`: `timer.now() - clockResetTime >= TURN_DURATION`

Each SR-Beh requirement gets one branch: Moving has 3, Turning has 3 and Final has none (SR-DC1). `result_codegen.json` maps all 23 requirements.

## Decisions the requirements did not settle

- **Moving → Turning** needs the obstacle event *and* the `obstacleDetected` guard (SR-Beh2 plus SR-GP1).
- **Turning priority** is endTask, then the autonomous return, then the tick self-loop. The tick branch conjoins `!turnDurationElapsed`, so ticks cannot hold the robot in Turning.
- **Entering Turning** resets `clockResetTime` before issuing `Move(0, turnVel)`, following SR-Beh2's order (SR-FR2 lists the two the other way round).
- **The power-up `Move(moveVel, 0)`** is issued by the constructor.
- **No-reading distance** is 100.0 m (an invented default). A negative or NaN reading counts as no reading.
- **The actuator starts at `Move(0, 0)`.**
- **`Move` has two real components** rather than a wrapper datatype.
- **The clock field is named `timer`**, because `clock` is a reserved word.

## Unresolved

- `obstacleThreshold` (0.5) rounds up to 1 in the model's constants interface, so the model's guard is looser than the Java's.

Full details are in `post_codegen.md`.
