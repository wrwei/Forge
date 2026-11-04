# ITER_SUMMARY — sranger, pass 8, iteration 1

No Java was present at the start, so nothing was deleted.

## What was written
Nine files under `sranger/`:
- `annotation/RoboChartType`
- `event/InputEvent` {Obstacle, Tick, EndTask} and `event/OutputEvent` {Move(lv, av)}
- `sensor/Sensor` (distance, update) and `sensor/Clock` (now)
- `actuator/Actuator` (apply, lastCommand)
- `controller/SRangerMode` {MOVING, TURNING, FINAL}, `controller/SRangerConstants` and `controller/SRangerController`

`step()` has two named predicates (obstacleDetected, turnDurationElapsed) and three mode blocks. The initial mode and the six branches give seven transitions, one per SR-Beh requirement (SR-DC1). `result_codegen.json` traces all 23 requirements and all 9 files. `post_codegen.{md,json}` lists 8 items.

## Decisions the requirements left open
- **Moving→Turning**: the trigger is the obstacle event and the guard is obstacleDetected (SR-Beh2 and SR-GP1 together). A stale obstacle event above the threshold is ignored.
- **Turning priority**: EndTask, then the timed exit, then `Tick && !turnDurationElapsed`. Tick arrives every cycle and would otherwise starve the timed exit.
- **Power-up**: the constructor issues Move(MOVE_VEL, 0), the entry action of Moving at power-up (SR-FR1).
- **Time**: a `Clock` class, monotonic seconds since construction. The clock is reset before Move(0, turnVel) on entering Turning.
- **Invented defaults**: the no-reading distance is 1000.0 m (negative or NaN readings count as no reading). The actuator holds Move(0, 0) before the first command.
- **Final** is terminal with an empty block. Obstacle in Turning and all events in Final are ignored, and no transitions were invented.

## Unresolved
- OBSTACLE_THRESHOLD = 0.5 will be rounded up to 1 in the model's constants interface, so the model's guard is weaker than the Java's. The units were kept in metres, so this remains a fidelity note.
- I could not confirm from the rules alone that the extractor accepts a two-component output event (`Move(lv, av)`) or an actuator call in the constructor.
