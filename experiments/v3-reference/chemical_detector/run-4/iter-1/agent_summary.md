# ITER_SUMMARY: chemical_detector, run 4

## Iteration 1

**What I wrote.** 22 files in `chemical_detector`. There are two controllers, matching CD-ARCH2. `GasAnalysisController` has the modes Reading, Analysis, NoGas, GasDetected and Concluded. `MovementController` has the seven movement states. Turn, stop and resume are `OutputEvent` records of the gas-analysis subsystem and `InputEvent` records of the movement subsystem, so the module shares them. `GasSensorArray` holds analysis, intensity, location and goreq. `VehicleSensor` holds the odometer. `Clock` drives the evasion timer. `Move`, `RandomWalk`, `ShortRandomWalk` and `ChangeDirection` are the operation classes.

**Decisions the requirements left open:**
- Constant values: THR=3.0, LV=1.0, EVADE_TIME=2, STUCK_PERIOD=4, STUCK_DIST=1.0, OUT_PERIOD=2. All are whole numbers, so rounding changes nothing.
- Variables start at noGas, 0.0, Front and front. Every one is overwritten before it is read.
- In analysis, a sample "indicates" the target when it is the target Chem (a constructor argument) with positive intensity. angle(x) follows the Angle declaration order, cycling.
- The odometer is a sampled sensor method, not an event record.
- Waiting's during action randomWalk() runs on every transition into Waiting, but not on the initial entry.
- Found and Concluded are terminal modes. CD-MV-Beh9 is met by Found having no outgoing transition.
- The GasSensor record is named `GasSample`, to keep "sensor" out of its name. Intensity is real; Chem is a record with a nat id.
- The waits use `MovementActuator.pause`.
- No Java class connects the two controllers.

**Unresolved.** Taken literally, CD-Fn2 takes the maximum over every chemical, not just the target. All 13 points are in `post_codegen.md`.
