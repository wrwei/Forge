# SRanger — pass 4, iteration 1

## What was written

Nine Java files under `sranger/`:

- `annotation/RoboChartType`
- `event/InputEvent`, holding `Obstacle`, `Tick` and `EndTask`
- `event/OutputEvent`, holding `Move(lv, av)`
- `controller/SRangerMode`, with the modes `MOVING`, `TURNING` and `FINAL`
- `controller/SRangerConstants`
- `controller/SRangerController`
- `sensor/Sensor`
- `sensor/Clock`
- `actuator/Actuator`

`step()` has one block per mode and two named predicates:

- `obstacleDetected`: `distance() <= OBSTACLE_THRESHOLD`
- `turnDurationElapsed`: `timer.now() - clockResetTime >= TURN_DURATION`

Also written: `result_codegen.json` (54 entries; all 23 requirements and 9 files covered) and `post_codegen.{md,json}` (11 issues, `uncertain`). The Java compiles under `--release 17`; a scratch smoke test exercised all seven transitions.

## Decisions the requirements did not settle

- **Moving → Turning** fires on the `Obstacle` event *and* `obstacleDetected` (SR-Beh2 with SR-GP1).
- **Turning priority:** `EndTask`, then the timed exit, then `Tick && !turnDurationElapsed`. Tick arrives every cycle and would otherwise block the timed exit.
- **Tick self-loops perform no action** (SR-Beh4/7), so Moving's entry command is not re-issued on tick.
- **The power-up `Move(moveVel, 0)`** is issued in the constructor.
- **`Move` carries two real components.** The profile describes single-component events.
- **Time source:** a `Clock` class (monotonic seconds); the field is named `timer` because `clock` is reserved.
- **Final** has no outgoing transitions.
- **Invented defaults:**
  - the sensor distance is 1000.0 m when there is no reading, and a negative or NaN reading counts as none;
  - the Actuator starts at (0, 0);
  - `clockResetTime` starts at 0.0.
- **Comparison:** `<=` (SR-GP1) is used, not the strict "crosses below".

## Unresolved

- **Fractional threshold:** the model rounds `obstacleThreshold` 0.5 up to 1, so the modelled guard is `distance <= 1`.
- **Initial entry action:** the model may not show it; the profile has no branch for the initial transition.
- **Two-component `Move`:** extractor support is unknown.
