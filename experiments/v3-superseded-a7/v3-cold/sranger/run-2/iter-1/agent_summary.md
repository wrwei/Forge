# ITER_SUMMARY — sranger, pass 2

**Written.** Nine Java files in package `sranger`: `RoboChartType`; `InputEvent` (Obstacle, Tick, EndTask); `OutputEvent` (Move(lv, av)); `SRangerMode` (MOVING initial, TURNING, FINAL); `SRangerConstants`; `Sensor` (distance, update); `Clock` (now); `Actuator` (apply, last velocities); `SRangerController`. `step` declares `obstacleDetected` and `turnDurationElapsed` (`timer.now() - clockResetTime >= TURN_DURATION`), then a two-level if-else with six transitions, one per SR-Beh2–7; SR-Beh1 is the mode field's initialiser. Final is terminal. Also `result_codegen.json` (66 entries; all 23 requirements, all 9 files) and `post_codegen.{md,json}` (status uncertain, 10 items). Compiles with `javac --release 17 -Xlint:all`, no warnings; a scratchpad smoke test of every transition passed.

**Decisions the requirements did not settle.**
- Moving→Turning: trigger `obstacle` and guard `obstacleDetected` together (Beh2 names the event, GP1 the guard).
- Turning order: endTask, timed return, then tick guarded by `!turnDurationElapsed`. Ticks arrive every cycle, so tick-first would starve the timed return.
- endTask is the first branch in Moving and Turning.
- Tick self-loops do nothing; Turning's entry actions (clock reset, Move(0, turnVel)) are inlined only on Moving→Turning.
- Power-up Move(moveVel, 0) is issued in the constructor.
- Move is one record with two real components (one event, two values).
- Sensor distance with no reading: 100.0 m (invented); negative or NaN readings are ignored.
- clockResetTime starts at 0.0; actuator starts at (0, 0) (invented).
- Clock: monotonic seconds since creation; the controller field is `timer` because `clock` is reserved.

**Unresolved.** OBSTACLE_THRESHOLD = 0.5 is rounded up to 1 in the model's constants interface, so the model's guard reads `distance() <= 1`; the Java keeps 0.5. Whether the extraction carries the constructor's power-up command and the two-component Move is unknown until the run.
