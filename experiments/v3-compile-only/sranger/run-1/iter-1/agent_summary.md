# SRanger — iteration 1 summary

No Java was present before writing.

## What was written (`sranger.*`)
- `annotation.RoboChartType`
- `event.InputEvent` (Obstacle, Tick, EndTask) and `event.OutputEvent` (Move(lv, av))
- `sensor.Sensor` (distance())
- `actuator.Actuator` (apply, last lv/av)
- `time.Clock` (now())
- `controller.SRangerMode` (MOVING first, TURNING, FINAL)
- `controller.SRangerConstants`
- `controller.SRangerController`: one `step` with the two predicates `obstacleDetected` and `turnDurationElapsed`, and the seven transitions

Every transition is a single branch (SR-DC1). The trace and `post_codegen` are written.

## Decisions the requirements left open
- **No-reading distance**: 1000.0 m (invented). A negative reading counts as no reading.
- **Moving → Turning**: needs both the Obstacle event (SR-Beh2) and `obstacleDetected` (SR-GP1). An obstacle event that arrives while the distance is above the threshold is ignored.
- **Priority**:
  - endTask comes first in Moving and Turning.
  - In Turning, the timed return comes before tick, and the tick branch carries `!turnDurationElapsed`. Otherwise ticks every cycle would keep the robot turning forever.
  - The tick self-loops do nothing and do not reset the clock.
- **Clock**: the field is called `timer` because `clock` is reserved. `clockResetTime = timer.now()` makes `clockResetTime` the RoboChart clock, and the guard becomes `since(clockResetTime) >= turnDuration`.
- **Power-up**: the initial Move(moveVel, 0) is issued in the constructor. The model may not show it.
- **Move**: keeps the two real components given in SR-DM4.
- **Ignored events**: Obstacle in Turning and all events in Final have no transition.

## Unresolved
- OBSTACLE_THRESHOLD = 0.5 becomes 1 in the model, because the model rounds fractional constants up. The requirements fix both the value and the metre unit, so it is kept as specified.
