# SRanger — pass 9, iteration 1

No Java was present before writing.

## What I wrote

Nine files under `sranger/`: the `RoboChartType` annotation; sealed `InputEvent` (`Obstacle`, `Tick`, `EndTask`) and `OutputEvent` (`Move(lv, av)`); `Sensor.distance()`; `Actuator.apply()` storing the last lv/av; a framework-advanced `Clock`; `SRangerMode` (MOVING, TURNING, FINAL); `SRangerConstants` (1.0, 2.0, 0.5, 2.0); and `SRangerController`, whose single `step()` has two predicates (`obstacleDetected`, `turnDurationElapsed`) and a two-level if-else with seven transitions, one per SR-Beh requirement. I also wrote the trace (`result_codegen.json`) and the review (`post_codegen.{md,json}`, 11 issues).

## Decisions the requirements left open

- Moving→Turning needs both the obstacle event and `obstacleDetected` (SR-Beh2 plus SR-GP1).
- Priority: endTask comes first in both modes. In Turning the timeout outranks tick, and the tick branch conjoins `!turnDurationElapsed`, so regular ticks cannot keep the robot turning forever.
- The tick self-loops do nothing (SR-Beh4/7): no Move is reissued and the timer is not restarted.
- The power-up Move(moveVel, 0) is issued in the constructor, since no transition enters the initial mode.
- No-reading default: 1000.0 m. Negative or NaN readings count as missing.
- Before any command, the actuator reports (0, 0).
- Move has two real components, not one datatype.
- The clock field is named `timer` (`clock` is reserved).
- Final ignores all events.

## Unresolved

- `obstacleThreshold` = 0.5 is rounded up to 1 in the model's constants, so the model's obstacle guard is looser than the Java's.
- In RoboChart a self-transition re-runs the state's entry action. If the extraction puts Turning's clock reset into its entry action, a tick restarts the timer in the model but not in the Java.
- The constructor-time initial Move may not appear in the extracted model.
