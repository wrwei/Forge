# Iteration summary: chemical_detector, pass 5

## What I wrote

There was no Java to remove. I wrote 18 files under `chemical_detector/`:

- `GasAnalysisController` (READING, ANALYSING, NO_GAS, GAS_DETECTED, CONCLUDED) and `MovementController` (the seven specified modes);
- domain enums and records, the `InputEvent`/`OutputEvent` events, and the constants;
- `ChemicalSensor` (analysis, intensity, location, goreq), `OdometerSensor` and `Clock`;
- `VehicleActuator` and `AnalysisActuator`.

`result_codegen.json` traces all 81 requirements. `post_codegen.{md,json}` lists 11 issues.

## Decisions the requirements left open

- **Operations:** CD-OP1..4 are VehicleActuator methods. Profile `compute()` classes cannot take the controller's `a` or `l`, and changeDirection is conditional.
- **Random walk:** Waiting's `during randomWalk()` is issued on every entry to Waiting, and in the constructor.
- **Odometer:** a zero-argument sensor method, not an event.
- **Final states:** GA's final state is the terminal mode CONCLUDED; FOUND is itself terminal.
- **Shared events:** turn, stop and resume are both OutputEvent and InputEvent records with the same names; AnalysisActuator delivers them synchronously.
- **Types:** Intensity is `real`, Chem a record holding a `nat`, a reading `List<GasSensor>`.
- **Constants:** THR=1.0, LV=1.0, EVADE_TIME=1, STUCK_PERIOD=2, STUCK_DIST=1.0, OUT_PERIOD=1.
- **Initial values:** chosen; each is overwritten before it is read.
- **Gas detection:** a value for the target Chem with i > 0. Position p faces the p-th Angle. An empty reading gives 0.0 and Front.
- **Names:** ANALYSING and `species` avoid clashes with `analysis` and Isabelle's `id`.

## Unresolved

- Peak intensity and location span all chemicals, as written.
- Terminal modes may count as deadlock in the Isabelle check.
- `gas` carries a sequence, which may be costly for FDR.
- An event that arrives in the same `step()` call as an autonomous transition is not handled.

Compiled cleanly with `javac --release 17 -Xlint:all`. A scenario harness outside the project passed all 14 checks.
