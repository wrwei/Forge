# Iteration summary — chemical_detector, run 4

## Iteration 1

**Written.** 19 Java files in `chemical_detector`. There are two controllers, `GasAnalysisController` and `MovementController`, because the requirements describe two subsystems that talk only through turn, stop and resume. Each requirement state is one mode, and gas analysis has a terminal Concluded mode. Gas analysis sends `OutputEvent.Turn/Stop/Resume` and movement receives the `InputEvent` records with the same names, so they become shared events. The rest of the code: domain types; `GasAnalysisSensor` (analysis, intensity, location, goreq); `OdometerSensor`; a `Clock` for the evasion timer and the waits; `VehicleActuator`; and the Java wiring in `ChemicalDetectorSystem`. The trace has 148 entries, covering all 81 requirements and all 19 files.

**Decisions the requirements left open** (details in `post_codegen.md`):
- Constants: thr 1.0, lv 1.0, evadeTime 1, stuckPeriod 3, stuckDist 1.0, outPeriod 1. The reals are whole numbers, so the model's rounding does not change them.
- Initial values of the variables.
- A reading indicates the target when its chemical equals the configured target and its intensity is above 0.
- Direction by sensor position: 1 Left, 2 Right, 3 Back, 4 Front, then repeating. Ties go to the first sensor.
- The odometer is a sensor variable, not an event, because d0 and d1 are sampled inside actions.
- Waiting's randomWalk runs on every transition into Waiting. The Vehicle starts out random-walking.
- The Vehicle operations are actuator methods, not operation classes.
- NoGas → Reading is guarded by `sts == noGas`. That guard always holds in NoGas, and the profile cannot express a single unguarded transition.
- Found and Concluded are terminal.

**Unresolved.** Intensity and location range over every chemical in a reading (CD-Fn2 read literally). The NoGas guard and the case-only-different Angle/Loc literals may need changing if verifiers object.
