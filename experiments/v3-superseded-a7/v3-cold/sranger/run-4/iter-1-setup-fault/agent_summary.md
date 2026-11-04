# SRanger, pass 4, iteration 1

No Java was present at the start, so nothing was deleted.

## What was written

Nine files under `sranger/`:
- `annotation.RoboChartType`
- `event.InputEvent` (Obstacle, Tick, EndTask)
- `event.OutputEvent` (`Move(lv, av)`)
- `sensor.Sensor` (`distance()`)
- `actuator.Actuator`
- `controller.SRangerMode` (MOVING, TURNING, FINAL)
- `controller.SRangerConstants`
- `controller.Clock`
- `controller.SRangerController`

`step` has two predicates, `obstacleDetected` and `turnDurationElapsed`, and one branch for each of SR-Beh2 to SR-Beh7. SR-Beh1 is the initial value of the mode field, so SR-DC1 holds by construction. Also written: `result_codegen.json` (62 entries, every requirement and file) and `post_codegen.{md,json}`. The code compiles with `javac --release 17`, and a scratch behaviour check passed.

## Decisions the requirements did not settle

- **No-reading distance**: 1000.0 m. Negative or NaN readings are ignored.
- **Actuator before the first command**: (0, 0). Clock time starts at 0.
- **Initial `Move(moveVel, 0)`**: issued by the constructor, because power-up has no `step` branch.
- **Moving → Turning**: the obstacle event plus the `obstacleDetected` guard (SR-Beh2 together with SR-GP1). The guard uses `<=` as SR-GP1 states, not the strict "crosses below" of the description.
- **Turning branch order**: endTask, then the timed return to Moving, then tick with `!turnDurationElapsed`. This stops a tick on every cycle from holding the robot in Turning.
- **`Move`**: a record with two real components, as SR-DM4 gives it.
- **Clock field**: named `timer`, because `clock` is reserved.

## Unresolved

- `obstacleThreshold` = 0.5 rounds up to 1 in the model's constants interface, so the model's guard differs from the Java one.
- An obstacle event that arrives in Turning is ignored, including one in the cycle where the turn ends. No requirement covers this.
- The extracted model may not show the initial Move.
