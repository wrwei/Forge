# SRanger run-7 cold baseline — agent summary

Single-shot Java implementation of the SRanger reactive controller from
`requirement_all.json` and `system_description.txt`.

## Files produced
- `annotation/RoboChartType.java` — model-extraction type hints (`nat`, `real`).
- `event/InputEvent.java` — sealed iface with `Obstacle`, `Tick`, `EndTask` signals (SR-DM3).
- `event/OutputEvent.java` — sealed iface with `Move(lv, av)` (SR-DM4).
- `mode/SRangerMode.java` — `Moving`, `Turning`, `Final` (SR-DM1).
- `constants/SRangerConstants.java` — `moveVel`, `turnVel`, `obstacleThreshold`, `turnDuration` (SR-DM2).
- `sensor/Sensor.java` — `distance()` with a large default when uninitialised (SR-DM5, SR-SF1).
- `actuator/Actuator.java` — stores last `(lv, av)` (SR-DM6).
- `clock/Clock.java` — `nowMs()` for `clockResetTime` arithmetic.
- `controller/SRangerController.java` — single-method `step(InputEvent)` with mode-nested if-else.

## Design choices
- Two named predicates: `obstacleDetected`, `turnDurationElapsed`
  (`clock.nowMs() - clockResetTime >= turnDuration`, expecting M2M `since`-rewrite).
- `EndTask` duplicated as the highest-priority branch in both `Moving` and `Turning`
  (SR-Beh3, SR-Beh6) per the cross-mode-override rule.
- `Tick` self-loops in `Moving`/`Turning` provide event-triggered bare-precondition
  cover (proof pattern (b)) instead of `else { mode = same; }` to avoid FDR4
  determinism failures.
- `Final` has no outgoing transitions per SR-FR3.
