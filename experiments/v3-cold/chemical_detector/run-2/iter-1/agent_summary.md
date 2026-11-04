# Iteration 1 — chemical_detector, pass 2

## What was written
There were 18 Java files under `chemical_detector/`. No Java was present beforehand.
- **Controllers.** `GasAnalysisController` (READING, ANALYSING, GAS_ABSENT, GAS_DETECTED, CONCLUDED) and `MovementController` (WAITING, GOING, FOUND, AVOIDING, TRYING_AGAIN, AVOIDING_AGAIN, GETTING_OUT). Each has one two-level `step`. Stop and resume come first wherever they apply.
- **Supporting classes.** Domain types, sealed `InputEvent`/`OutputEvent`, sensors `GasSensorArray` (analysis, intensity, location, goreq) and `OdometerSensor`, a `Clock` (now, pause), actuators `Vehicle` and `EventBus`, constants, and the annotation.
- **Trace.** `result_codegen.json` covers all 81 requirements and all 18 files. `post_codegen.{md,json}` is the Layer 5 review.

## Decisions the requirements did not settle
- **Names.** The GasSensor record is named `GasSample`, because the profile treats names containing "sensor" as sensor services. NoGas and Analysis become GAS_ABSENT and ANALYSING, to avoid clashes that differ only in capitalisation.
- **Operations.** move, randomWalk, shortRandomWalk and changeDirection are `Vehicle` methods, not `compute()` classes. They compute nothing.
- **Odometer.** It is a sensor method sampled into d0 and d1, not an event.
- **Shared events.** Turn, Stop and Resume have the same names in both hierarchies.
- **Waiting.** Its during action, randomWalk, is issued on every transition into WAITING. The Vehicle starts already random-walking.
- **Types.** Intensity is real; Chem wraps a nat.
- **Invented values.**
  - Constants: thr=10, lv=1, evadeTime=1, stuckPeriod=3, stuckDist=1, outPeriod=2.
  - Initial variable values.
  - "Indicates" means target chemical with i > 0.
  - Sensor k faces the k-th Angle.
  - Empty readings return 0.0 and Front.

## Unresolved
- GAS_ABSENT → READING has no guard and no trigger. I wrote it as a bare mode assignment; the profile shows no example of that form.
- Peak intensity and location range over all chemicals, as written.
- Stuck detection treats less elapsed time as progress, as written.
