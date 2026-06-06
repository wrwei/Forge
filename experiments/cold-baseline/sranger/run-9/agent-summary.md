# SRanger Cold Baseline — Run 9 Agent Summary

Single-shot implementation of the SRanger reactive ground-robot controller
under `sranger.*` packages.

## Files written

- `annotation/RoboChartType.java`, `Clock.java`, `SensorService.java` — model-extraction annotations.
- `event/InputEvent.java` — sealed interface with `Obstacle`, `Tick`, `EndTask` signal records.
- `event/OutputEvent.java` — sealed interface with `Move(lv, av)` only.
- `mode/SRangerMode.java` — enum `{Moving, Turning, Final}`; Moving is initial.
- `constants/SRangerConstants.java` — `moveVel=1.0`, `turnVel=2.0`, `obstacleThreshold=0.5`, `turnDuration=2.0`.
- `clock/Clock.java` — convention-named Clock dependency exposing `nowSeconds()` / `nowMs()`.
- `sensor/Sensor.java` — `distance()` returns IR distance; defaults to a large value when no reading.
- `actuator/Actuator.java` — stores last `Move(lv, av)` (uses traditional `instanceof` + cast).
- `controller/SRangerController.java` — single-method `step(InputEvent)` with mode-nested if-else.

## Design choices

- Named boolean predicates `obstacleDetected` and `turnDurationElapsed` declared before the if-else.
- `EndTask` is the first inner branch in both `Moving` and `Turning` blocks (highest priority duplicated).
- Tick self-loops provide bare-precondition coverage in each mode for the Isabelle deadlock-freedom proof.
- Initial-mode entry action (`Move(moveVel, 0)`) is issued from the constructor.
- No lambdas/streams/ternaries; all predicate RHS are simple comparisons.
