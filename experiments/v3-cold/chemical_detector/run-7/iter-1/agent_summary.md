# Iteration 1 — chemical_detector, cold pass 7

No Java was present at the start, so nothing was deleted.

## What was written

There are 18 files under `chemical_detector/`. Two controllers follow the profile's two-level `step()`:

- `GasAnalysisController`: Reading → Analysing → NoGas | GasDetected → Reading | Concluded. It emits turn, stop and resume through `CommandActuator`.
- `MovementController`: Waiting, Going, Found, Avoiding, TryingAgain, AvoidingAgain, GettingOut and Halted. It consumes the same-named input events, flags the Vehicle and drives `VehicleActuator`.

The functions are `GasSensorArray` methods and the odometer is a `VehicleSensor` method. A `Clock` field provides the evasion clock, and `Clock.delay` the waits. `result_codegen.json` covers all 81 requirements and every file. `post_codegen.{md,json}` lists 12 points.

## Decisions the requirements did not settle

- **Placeholder constants:** THR=2.0, LV=1.0, EVADE_TIME=1, STUCK_PERIOD=3, STUCK_DIST=1.0, OUT_PERIOD=2.
- **Initial values:** sts=noGas, ins=0, anl=a=Front, d0=d1=0, l=front.
- **Target chemical:** a `GasSensorArray` constructor argument; a sample "indicates" it when ids match and intensity > 0. angle(x) maps positions 1..4 to Left, Right, Back, Front.
- **Types:** Chem is a record with a nat id; Intensity is real. GasSensor was renamed GasMeasurement, since classes named "…sensor…" become sensor services.
- **Operations:** move, randomWalk, shortRandomWalk and changeDirection are `VehicleActuator` methods, not operation classes, because they take controller variables as arguments.
- **During action:** Waiting's during randomWalk() runs as an entry action, including at construction.
- **Unguarded transitions:** NoGas→Reading and Found→Halted are guarded by a constant-true predicate. Halted was added so that CD-MV-Beh9 is a transition.
- **Mode names:** ANALYSING and NO_GAS avoid clashing with `analysis` and `noGas`.

## Unresolved

- Whether the extractor accepts two-argument actuator calls, a `true` guard and a constructor call.
- The operation-class rule was not followed (see above).
- Routing CommandActuator's events to MovementController is left to the platform.
