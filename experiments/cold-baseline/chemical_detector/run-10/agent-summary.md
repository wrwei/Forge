# Chemical Detector — Cold Baseline Run 10

Single-shot generation. No verifier feedback consulted.

## Implementation summary

Two controllers in `chemdetector.controller`:

- `GasAnalysisController` covers CD-GA-FR1..4 / CD-GA-Beh1..7. Modes:
  `Reading -> Analysis -> {NoGas, GasDetected} -> {Reading, Final}`.
  Emits `Turn`/`Stop`/`Resume` via an `EventBus`.
- `MovementController` covers CD-MV-FR1..7 / CD-MV-Beh1..23. Modes:
  `Waiting / Going / Avoiding / TryingAgain / AvoidingAgain /
  GettingOut / Found / Final`. Stuck detection uses a `Clock` named
  `T` plus distance deltas (`d1 - d0`).

Supporting layout:

- `datatype/` — `Status`, `Angle`, `Loc`, `Chem`, `Intensity` (with
  `goreq`), `GasSensor` record.
- `event/` — sealed `GasAnalysisInputEvent`, `MovementInputEvent`,
  `GasAnalysisOutputEvent`, `MovementOutputEvent`.
- `sensor/Sensor` — pure derived-quantity functions for CD-Fn1..4.
- `vehicle/Vehicle` — move / randomWalk / shortRandomWalk / pause
  (`@RoboChartWait`).
- `operation/ChangeDirection` — CD-OP4 with `compute()` of direct
  field assignments only.
- `annotation/` — `@RoboChartType`, `@Clock`, `@SensorService`,
  `@RoboChartWait`.

## Conventions applied

- Mode-nested if-else step methods, named boolean predicates declared
  before the chain (no ternaries, no sentinel checks).
- High-priority `Stop`/`Resume` transitions duplicated into every
  applicable movement-mode block.
- `@RoboChartType` on `nat` / `real` fields and parameters.
- Avoided RoboChart reserved-word collisions: clock field named `T`,
  wait method named `pause`, no `event`/`state` identifiers in
  unsafe positions.
