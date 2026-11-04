# SRanger — pass 6, iteration 1

## What was written

No Java was present beforehand; nothing was deleted. Nine files under `sranger/`:

- `annotation/RoboChartType`;
- `event/InputEvent` (Obstacle, Tick, EndTask) and `event/OutputEvent` (Move(lv, av));
- `sensor/Sensor` (`distance()`), `actuator/Actuator` (`apply`, last lv/av);
- `controller/` SRangerMode {MOVING, TURNING, FINAL}, SRangerConstants, Clock and SRangerController.

The `step()` method has two predicates (`obstacleDetected`, `turnDurationElapsed`) and the seven required transitions; Final is terminal. Also written: `result_codegen.json` (all 23 requirements, all 9 files) and `post_codegen.{md,json}`. The code compiles cleanly, and a scratch harness exercised every transition.

## Decisions the requirements did not settle

1. **Obstacle trigger and guard.** SR-Beh2 names the obstacle event; SR-GP1 makes obstacleDetected the guard of the same transition. Both are used: `obstacle && obstacleDetected`.
2. **Two-component Move.** Move is one record with two real components, not a new velocity datatype.
3. **Initial forward command.** It is issued in the constructor, because no `step()` branch covers power-up. The model's initial transition will probably lack it.
4. **Turning priority.** endTask comes first, then the timed transition, then `tick && !turnDurationElapsed`. Tick self-loops do nothing, so they do not reset the clock.
5. **Time source.** A framework-advanced `Clock` class, held in the field `timer`, because `clock` is reserved.
6. **Invented defaults.** Distance with no reading: 1000.0 m; negative readings are ignored. The actuator's last command before any command is (0, 0).
7. **Unhandled events.** Obstacle in Turning, and every event in Final, are ignored; no transitions were added beyond SR-Beh1–7.

## Unresolved

- obstacleThreshold = 0.5 is rounded up to 1 in the model, which changes the obstacleDetected guard there.
- I could not confirm whether the extractor supports an event with two components.
- I could not confirm whether the Isabelle Z-Machine treats the terminal Final mode as a deadlock.
