# SRanger cold baseline — run 5

Single-shot Java implementation of the SRanger reactive controller, generated without prior verifier feedback.

## Package layout

- `sranger.annotation.RoboChartType` — model-extraction annotation
- `sranger.event.InputEvent` — sealed: `Obstacle`, `Tick`, `EndTask`
- `sranger.event.OutputEvent` — sealed: `Move(lv, av)`
- `sranger.mode.SRangerMode` — `Moving`, `Turning`, `Final`
- `sranger.constants.SRangerConstants` — `moveVel`, `turnVel`, `obstacleThreshold`, `turnDuration`
- `sranger.sensor.Sensor` — `distance()` with `Double.MAX_VALUE` safe-default
- `sranger.actuator.Actuator` — stores last `Move(lv, av)` command
- `sranger.clock.Clock` — `nowMs()` for time-based transitions
- `sranger.controller.SRangerController` — single-method, mode-nested if-else state machine
- `sranger.Main` — demo driver

## Modelling choices

- Time predicate `clock.nowMs() - clockResetTime >= turnDuration` follows the documented M2M rewrite to `since(clockResetTime) >= turnDuration`.
- `endTask` is the highest-priority transition in both Moving and Turning.
- Moving and Turning each have a `tick` self-loop with bare precondition to satisfy Isabelle deadlock-freedom; Final has a `tick` self-loop for the same reason.
- Initial-state Moving entry action `Move(moveVel, 0)` issued from the controller constructor.
- All seven SR-Beh transitions implemented exactly once (SR-DC1).

No streams, lambdas, ternaries, or pattern-matching instanceof.
