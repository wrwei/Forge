# SRanger — iteration 1

## What was written and why
There are nine files under `sranger/`:
- `annotation/RoboChartType`
- `event/InputEvent` (Obstacle, Tick, EndTask) and `event/OutputEvent` (Move(lv, av))
- `sensor/Sensor` (`distance()`)
- `actuator/Actuator` (`apply`, last lv/av)
- `time/Clock`
- `controller/SRangerMode` (MOVING, TURNING, FINAL)
- `controller/SRangerConstants`
- `controller/SRangerController`

`step()` is the profile's two-level if-else:
- Moving: endTask, then obstacle && obstacleDetected, then tick.
- Turning: endTask, then the autonomous turnDurationElapsed, then tick && !turnDurationElapsed.
- Final: empty (terminal).

Every branch performs the target mode's entry actions inline. The trace covers all 23 requirements and every file. No Java was present beforehand.

## Decisions the requirements did not settle
- No IR reading: `distance()` returns 100.0 m. A negative or NaN reading counts as no reading.
- Before any command, the actuator reports (0, 0).
- At power-up, the constructor issues Move(moveVel, 0), the entry command of Moving.
- SR-Beh2 (obstacle event) and SR-GP1 (guard) describe the same transition, so both are implemented as event and guard. An obstacle event with distance above the threshold is ignored.
- In Turning, the timeout ranks above tick. Otherwise the per-cycle tick would hold the robot in Turning forever.
- Time source: a `Clock` field named `timer`, because `clock` is reserved. clockResetTime is assigned from `timer.now()`, so it should be extracted as a RoboChart clock with `since()`.
- The constant `obstacleThreshold` is 0.5. The model rounds it up to 1, which affects the obstacle guard.

## Unresolved
None.
