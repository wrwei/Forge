# SRanger — cold pass 1

No Java was present before this pass; nothing was deleted.

## What was written

Nine files under `sranger/`:

- `annotation/RoboChartType`
- the sealed events `InputEvent` (`Obstacle`, `Tick`, `EndTask`) and `OutputEvent` (`Move(lv, av)`)
- `sensor/Sensor` (`distance()`)
- `actuator/Actuator` (stores the last Move)
- `time/Clock` (`now()`)
- in `controller/`: `SRangerMode` (MOVING first), `SRangerConstants` and `SRangerController`

`step()` declares `obstacleDetected` and `turnDurationElapsed`, then runs one block per mode. Each of SR-Beh2–7 is exactly one branch. `clockResetTime = timer.now()` on entering Turning gives the clock reset. `result_codegen.json` traces all 23 requirements and all nine files. `post_codegen.{md,json}` lists the points below.

## Decisions the requirements left open

- **No-reading distance:** 1000.0 m (invented). Invalid readings count as no reading.
- **clockResetTime:** starts at 0.0 (invented).
- **Move:** one record with two real components (SR-DM4).
- **Moving → Turning:** trigger `obstacle` plus guard `obstacleDetected` (`<=`), reconciling SR-Beh2 with SR-GP1.
- **Initial forward command:** issued in the constructor; no transition leads into the initial mode.
- **Priority:** `endTask` first in both modes. In Turning, the timed exit is above the `tick` self-loop, which also requires `!turnDurationElapsed`. Unhandled events are ignored.
- **Clock:** monotonic seconds, advanced by the framework. The field is `timer` because `clock` is reserved.
- **Sensor and Actuator:** concrete classes.

## Unresolved

- **Move's two components:** the profile only shows events with at most one; untested whether the transformation accepts two.
- **obstacleThreshold:** 0.5 becomes 1 in the model's constants. Deadlock and divergence results do not depend on it.

## Checked before the run

`javac --release 17` compiles with no warnings. A scratch smoke test confirmed power-up Move(1, 0), obstacle turn Move(0, 2), return to Moving after 2 s, and stop Move(0, 0).
