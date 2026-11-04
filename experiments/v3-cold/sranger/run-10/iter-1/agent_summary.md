# ITER_SUMMARY — sranger, pass 10, iteration 1

No Java was present at the start, so nothing was deleted.

## What was written

Nine Java files under `sranger/`:
- `annotation/RoboChartType`;
- `event/InputEvent` (`Obstacle`, `Tick`, `EndTask`) and `event/OutputEvent` (`Move(lv, av)`);
- `controller/SRangerMode` (MOVING first), `SRangerConstants`, `Clock` and `SRangerController`;
- `sensor/Sensor` (`distance()`);
- `actuator/Actuator`.

`step()` is a two-level if-else with two predicates, `obstacleDetected` and `turnDurationElapsed`, the latter written as `timer.now() - clockResetTime >= TURN_DURATION`. Each branch assigns the mode and performs the target's entry action inline. All 23 requirements are traced in `result_codegen.json`. The review is in `post_codegen.{md,json}`.

## Decisions the requirements did not settle

- **Sensor no-reading default:** 1000.0 m. Negative, NaN or out-of-range readings also count as "no reading".
- **Actuator before the first command:** (0, 0).
- **Move:** one record with two real components, emitted via `actuator.apply(new OutputEvent.Move(...))`.
- **Power-up entry:** `Move(moveVel, 0)` is issued in the constructor, since `step()` carries transitions only.
- **Moving → Turning:** the `Obstacle` trigger *and* the `obstacleDetected` guard, because SR-Beh2 and SR-GP1 each state one of them.
- **Turning priority:** endTask, then the timed return, then `tick && !turnDurationElapsed`. Otherwise a tick every cycle would block the return to Moving. Tick self-loops perform no action and do not reset the clock.
- **Clock:** a framework-advanced clock. The controller field is named `timer` because `clock` is reserved.
- **Unhandled events:** obstacle in Turning and every event in Final are ignored. Final is terminal, and no transitions were invented.

## Unresolved

- `OBSTACLE_THRESHOLD = 0.5` will be rounded up to 1 in the model's constants interface. The extracted guard is therefore coarser than the Java.
- I don't know whether the extractor types a two-component event record, or keeps an output call made in the constructor. If either fails, the fallbacks are listed in `post_codegen`.
