# SRanger — cold pass 7, iteration 1

## What was written

Nine files under `sranger/`: the `RoboChartType` annotation; sealed `InputEvent`
(`Obstacle`, `Tick`, `EndTask`) and `OutputEvent` (`Move(lv, av)`); `Sensor`
(`distance()`); `Actuator` (`apply`, last linear/angular velocity); `Clock`
(`now()`, `advance()`); and, in `controller`, `SRangerMode`
(`MOVING`, `TURNING`, `FINAL`), `SRangerConstants` and `SRangerController`.
`step()` is the profile's two-level if-else with two predicates,
`obstacleDetected` and `turnDurationElapsed`. Also written:
`result_codegen.json` (54 entries; all 23 requirements, all 9 files) and
`post_codegen.{md,json}`. No Java was present beforehand.

## Decisions the requirements left open

- Moving → Turning is `Obstacle && obstacleDetected` (SR-Beh2 trigger,
  SR-GP1 guard). An obstacle event above the threshold is ignored.
- Turning priority: endTask, then autonomous `turnDurationElapsed`, then
  `Tick && !turnDurationElapsed`. A per-cycle tick must not starve the return
  to Moving.
- endTask is the first branch in both Moving and Turning. Final is terminal,
  with an empty block.
- Move is one two-component record sent via
  `actuator.apply(new OutputEvent.Move(...))`.
- The power-up Move(MOVE_VEL, 0) is issued in the constructor.
- Clock is framework-advanced from 0.0 and monotonic. The field is `timer`
  because `clock` is reserved, and `clockResetTime = timer.now()` follows the
  clock convention.
- Invented defaults: no-reading distance 1000.0 m (negative or NaN readings
  count as no reading); the actuator starts at Move(0, 0); `clockResetTime`
  starts at 0.0.

## Unresolved

- The model rounds `OBSTACLE_THRESHOLD` 0.5 up to 1, so the model's guard is
  `distance() <= 1`.
- Whether the extractor types a two-component event, and whether it carries
  the constructor's initial Move, is unknown to me.
