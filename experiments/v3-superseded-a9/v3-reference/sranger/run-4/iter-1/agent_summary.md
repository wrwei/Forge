# ITER_SUMMARY — sranger, run 4

## Iteration 1

**Written.** Nine files under `sranger/`. `annotation.RoboChartType` and `event.InputEvent` (signals `Obstacle`, `Tick`, `EndTask`). `event.OutputEvent` (one record, `Move(lv, av)`). `sensor.Sensor` (`distance()`), `actuator.Actuator` (stores the last Move) and `time.Clock` (`now()`, in seconds). Under `controller`: `SRangerMode {MOVING, TURNING, FINAL}`, `SRangerConstants` (the four real constants) and `SRangerController`, which has a single `step`. The trace lists all 23 requirements and covers every file.

**Controller.**
- Moving: endTask goes to Final with Move(0,0). obstacle together with `obstacleDetected` goes to Turning: it resets `clockResetTime` and sends Move(0,turnVel). tick is a self-loop.
- Turning: endTask goes to Final. `turnDurationElapsed` goes to Moving with Move(moveVel,0). tick together with `!turnDurationElapsed` is a self-loop.
- Final has no transitions.

**Decisions the requirements did not settle:**
- SR-Beh2 triggers on the obstacle event and SR-GP1 names `obstacleDetected` as the guard of the same transition, so the code uses both.
- Turning priority is endTask, then the timed return, then tick. Otherwise ticks arriving every cycle could keep the robot turning forever.
- The constructor issues Moving's entry command at power-up (SR-FR1).
- With no reading the sensor returns 1000.0 m (invented). Negative or NaN readings are ignored.
- The actuator reads 0/0 before the first command.
- The clock field is named `timer` because `clock` is reserved.

**Unresolved or flagged:**
- OBSTACLE_THRESHOLD = 0.5 is rounded up to 1 in the model's constants, so the model checks the guard against 1 m.
- `forge.assets/corrections/csp_overrides.csp` comes with the pinned commit and is labelled for LRE. Its `core_clock_type = {0..1}` may not reach turnDuration = 2, if the pipeline applies it to this study. I left it unchanged; it is instrument material.

Full detail: `post_codegen.md`.
