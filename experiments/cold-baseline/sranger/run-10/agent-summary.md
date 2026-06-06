# SRanger Cold Baseline — Run 10

Single-shot generation. No prior session, memory, or verifier feedback.

## What was generated

Package layout under `sranger.*`:

- `sranger.annotation` — `RoboChartType`, `Clock`, `SensorService`
- `sranger.event` — sealed `InputEvent` (Obstacle, Tick, EndTask), sealed `OutputEvent` (Move)
- `sranger.mode` — enum `SRangerMode { Moving, Turning, Final }`
- `sranger.constants` — `SRangerConstants` (moveVel, turnVel, obstacleThreshold, turnDuration)
- `sranger.clock` — `Clock` (advance / now)
- `sranger.sensor` — `Sensor` (distance() with large default)
- `sranger.actuator` — `Actuator` (records last Move)
- `sranger.controller` — `SRangerController` (single-method mode-nested step)

## Design choices

- Initial Move(moveVel, 0) issued from the controller constructor as the Moving entry action on power-up (SR-FR1, SR-Beh1).
- `endTask` modelled with highest priority within each non-terminal mode block (Beh3, Beh6 duplicated as first inner branch).
- Turn-duration guard expressed as `clock.now() - clockResetTime >= turnDuration` so the ETL can rewrite it to `since(clockResetTime) < turnDuration`-style RoboChart timing where applicable.
- Final has no outgoing transitions (terminal mode by SR-FR3); the controller leaves it as a sink — no self-loop, since `else if` chains for Final fall through.
- Tick transitions explicitly assign the same mode to satisfy SR-Beh4/SR-Beh7 with no other action.
- Named boolean predicates (`obstacleDetected`, `turnDurationElapsed`) declared before the if-else chain per codegen rules.
