# ITER_SUMMARY — sranger, run 1, iteration 1

## What was written
Nine files under `sranger/` (Java 17): `annotation/RoboChartType`; `event/InputEvent` (Obstacle, Tick, EndTask) and `event/OutputEvent` (Move(lv, av)); `sensor/Sensor` (`distance()`); `actuator/Actuator` (`apply`, last-command getters); `controller/SRangerMode` (MOVING first, TURNING, FINAL), `SRangerConstants`, `Clock`, and `SRangerController`. `step()` is a two-level if-else with two named predicates, `obstacleDetected` and `turnDurationElapsed`. `clockResetTime` is assigned from `timer.now()`, so the timed guard becomes `since`. The trace covers all 23 requirements and all 9 files. Compilation and a smoke test of every transition pass.

## Decisions the requirements did not settle
- **Moving → Turning** is triggered by the Obstacle event and guarded by obstacleDetected (SR-Beh2 and SR-GP1 together). An obstacle event while the reading is above the threshold is ignored.
- **Turning priority**: endTask first, then the autonomous timed exit, then tick guarded by `!turnDurationElapsed`, so that the timed exit can fire on a tick cycle.
- **endTask** is the first branch in both Moving and Turning.
- **Initial entry**: the constructor issues Move(moveVel, 0) at power-up (SR-FR1, SR-Beh1).
- **Invented defaults**:
  - The sensor returns 1000.0 m when it has no reading; a negative or NaN reading counts as no reading.
  - The actuator holds Move(0, 0) before the first command.
  - clockResetTime starts at 0.0.
  - The clock is monotonic `System.nanoTime` seconds.
- **Final** is terminal: its mode block is empty and has no transitions.

## Open points
- In the model, obstacleThreshold (0.5) is rounded up to 1, so the model's obstacle guard is coarser than the Java's.
- The constructor's initial Move may not appear in the extracted model.
- No pipeline result yet (iteration 1 not run).
