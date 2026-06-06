# SRanger cold baseline — run 6 summary

Single-shot generation of the SRanger reactive controller per the case-study spec.

## Layout (under `src/main/java/sranger/`)

- `annotation/` — `RoboChartType`, `Clock`, `SensorService`.
- `mode/` — `SRangerMode` enum (`Moving`, `Turning`, `Final`; initial: `Moving`).
- `constants/` — `SRangerConstants` (`moveVel`, `turnVel`, `obstacleThreshold`, `turnDuration`).
- `event/` — sealed `InputEvent` (`obstacle`, `tick`, `endTask`) and sealed `OutputEvent` (`Move(lv, av)`).
- `sensor/` — `Sensor` with `distance()` returning the latest IR reading (large default until updated).
- `actuator/` — `Actuator` recording the last `Move(lv, av)`.
- `clock/` — `Clock` exposing `nowMs()`.
- `controller/` — `SRangerController` (single-method `step(InputEvent)`).

## Controller shape

Mode-nested if-else over `mode == X` with named boolean predicates declared up front:
`obstacleDetected = sensor.distance() <= obstacleThreshold`,
`turnDurationElapsed = clock.nowMs()/1000.0 - clockResetTime >= turnDuration`.

Per-mode inner branches encode SR-Beh2..SR-Beh7. The `endTask` override is duplicated as the
highest-priority branch in both `Moving` and `Turning`. Entry actions are inlined:
Moving constructor issues `Move(moveVel, 0)`; Moving→Turning issues `Move(0, turnVel)` and
sets `clockResetTime`; Turning→Moving issues `Move(moveVel, 0)`; *→Final issues `Move(0, 0)`.
`Final` is terminal (no outgoing branches). No lambdas/streams/ternaries; traditional
`instanceof` + cast only.
