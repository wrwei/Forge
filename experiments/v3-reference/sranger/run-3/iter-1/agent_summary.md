# ITER_SUMMARY — sranger, run 3

## Iteration 1

**What I wrote.** I wrote nine files under `sranger/`:

- `annotation/RoboChartType`;
- the sealed `InputEvent` (`Obstacle`, `Tick`, `EndTask`) and `OutputEvent` (`Move(lv, av)`);
- `Sensor.distance()`;
- `Actuator.apply(OutputEvent)`, which stores the last lv and av;
- in `controller/`: `SRangerMode {MOVING, TURNING, FINAL}`, `SRangerConstants`, `Clock` and `SRangerController`.

`step` declares two predicates, `obstacleDetected` and `turnDurationElapsed` (`timer.now() - clockResetTime >= TURN_DURATION`), then runs one block per mode.

- **Moving:** endTask goes to Final and issues Move(0,0). obstacle together with `obstacleDetected` goes to Turning, resets the clock and issues Move(0,turnVel). tick is a self-loop.
- **Turning:** endTask goes to Final. `turnDurationElapsed` (autonomous) goes to Moving and issues Move(moveVel,0). tick together with `!turnDurationElapsed` is a self-loop.
- **Final:** empty (terminal).

I also wrote `result_codegen.json`: 56 entries, covering all 23 requirements and all 9 files.

**Decisions the requirements left open:**

1. The no-reading distance is 1000.0 m. A negative or NaN reading counts as no reading.
2. The constructor issues Move(moveVel,0), because Moving is entered at power-up and no `step` branch covers that entry.
3. The Moving→Turning transition needs both the obstacle event (SR-Beh2) and the `obstacleDetected` guard (SR-GP1).
4. The tick self-loops issue no Move (SR-Beh4/7). In Turning the priority is endTask, then timeout, then tick, so the per-cycle tick cannot block the timeout.
5. `clockResetTime` and the actuator's stored values start at 0.0.
6. The clock field is named `timer` because `clock` is reserved.
7. Final accepts no events.

**Unresolved:** `obstacleThreshold` = 0.5 is rounded up to 1 in the model's constants, so the verified guard is `distance() <= 1`. The Java guard uses 0.5.
