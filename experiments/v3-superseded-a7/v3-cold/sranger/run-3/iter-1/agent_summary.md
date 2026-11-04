# SRanger, pass 3, iteration 1

## What was written

Nine Java files under `sranger/`:
- `annotation`: `RoboChartType`.
- `event`: `InputEvent` (`Obstacle`, `Tick`, `EndTask`) and `OutputEvent` (`Move(lv, av)`).
- `sensor`: `Sensor` (`distance()`). `timing`: `Clock` (`now()`).
- `actuator`: `Actuator`, which stores the last command.
- `controller`: `SRangerMode` (MOVING, TURNING, FINAL), `SRangerConstants`, `SRangerController`.

`step()` declares `obstacleDetected` and `turnDurationElapsed`, then has one block per mode:
- Moving: endTask, then obstacle with obstacleDetected, then tick.
- Turning: endTask, then timed return, then tick.
- Final: terminal, no transitions.

Entering Turning resets `clockResetTime` from the clock. `result_codegen.json` maps all 23 requirements and all 9 files. `post_codegen.{md,json}` lists 8 issues.

## Decisions the requirements did not settle

- **Timed return outranks tick.** In Turning, the tick self-loop also requires `!turnDurationElapsed`. Tick arrives every cycle, so it would otherwise keep the robot turning forever.
- **Obstacle needs event and guard.** Moving→Turning needs the obstacle event and `distance <= obstacleThreshold`. SR-DM3 says the event fires below the threshold, SR-GP1 says at or below. I followed SR-GP1.
- **Tick self-loops do nothing.** They only re-assign the mode (SR-Beh4, SR-Beh7).
- **Startup command.** The constructor issues the forward Move at power-up. The model's initial transition may not show it.
- **Move shape.** Move has two real components, as the requirement states. The profile only describes events with a single value.
- **Invented defaults.**
  - The sensor returns 1000.0 m when no reading is available.
  - The actuator holds (0, 0) before the first command.
  - `clockResetTime` starts at 0.0.
- **Names.** The clock field is `timer` (`clock` is reserved). `Clock` sits outside the `sensor` package.

## Not resolved

- obstacleThreshold = 0.5 is rounded up to 1 in the model's constants.
- It is unknown whether the extractor accepts a two-component event record.
