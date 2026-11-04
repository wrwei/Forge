# ITER_SUMMARY — sranger, run 3

## Iteration 1

No Java was present before writing. Nine files under `sranger/`:

- `annotation.RoboChartType`
- `event.InputEvent` (Obstacle, Tick, EndTask)
- `event.OutputEvent` (Move(lv, av))
- `controller.SRangerMode` (MOVING first)
- `controller.SRangerConstants`
- `controller.SRangerController`
- `sensor.Sensor` (distance)
- `sensor.Clock` (now)
- `actuator.Actuator` (apply)

`step` has two predicates, `obstacleDetected` and `turnDurationElapsed` (`timer.now() - clockResetTime >= TURN_DURATION`, so it becomes a `since` guard), and one block per mode. The clock field is named `timer` because `clock` is reserved.

Decisions the requirements did not settle:

- Moving→Turning is triggered by obstacle, guarded by `obstacleDetected` (SR-Beh2 plus SR-GP1).
- Turning priority: endTask, then the autonomous timeout, then tick with `!turnDurationElapsed`. Ticks arrive every cycle, so tick-first would block the timeout.
- Tick self-loops do nothing (SR-Beh4/7). Repeating Turning's entry would reset the clock every cycle.
- The power-up Move(moveVel, 0) is issued in the constructor (SR-FR1/Beh1). Otherwise the robot never starts.
- clockResetTime is reset before Move(0, turnVel), in SR-Beh2's order.
- Invented: no-reading distance is 1000.0 m, and negative or NaN readings count as no reading. The Actuator starts at (0, 0).
- Events with no specified transition are ignored; Final is terminal.
- OBSTACLE_THRESHOLD 0.5 rounds up to 1 in the model; the Java keeps 0.5.

Details: `post_codegen.md`.

Unresolved: none so far.
