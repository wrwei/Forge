# ITER_SUMMARY — chemical_detector, run 5

## Iteration 1

**What I wrote** (no Java was present to delete):
- `GasAnalysisController` and `MovementController`, each with an enum mode and a two-level `step()`;
- enums `Status`, `Angle` and `Loc`, and records `Chem` and `GasSensor`;
- sealed `InputEvent` and `OutputEvent`. `Turn`, `Stop` and `Resume` are on both sides, so they become shared events;
- `GasSensorArray` (analysis, intensity, location, goreq, angle) and `OdometerSensor`;
- `Clock` (its `delay` becomes a wait), the actuators `Vehicle` and `MovementLink`, and the constants.

**Open decisions:**
- **Constants:** thr = 1.0, lv = 1.0, evadeTime = 2, stuckPeriod = 5, stuckDist = 1.0, outPeriod = 3. All are whole numbers, so the model's rounding has no effect.
- **analysis:** a value indicates the target chemical (a constructor argument) when c equals the target and i > 0.
- **angle(x):** 1 Front, 2 Right, 3 Back, 4 Left. On an empty reading, intensity returns 0.0 and location returns Front.
- **Types:** Chem is a record with a nat id; Intensity is a real.
- **Operations:** move, randomWalk, shortRandomWalk and changeDirection are `Vehicle` methods, not `compute()` classes, because they take run-time arguments or issue commands. changeDirection(l) calls move(lv, the direction away from l).
- **Odometer:** read as a sensor quantity, not an event, because it never triggers a transition.
- **Waiting's during action:** randomWalk() is issued when Waiting is entered, in the constructor and on each resume.
- **Terminal modes:** gas analysis ends in `Concluded`. Found is terminal, with no separate final transition.
- **Evasion clock:** the field `evasionStart`, reset on each entry to Avoiding.
- **Initial values:** sts = noGas, a = anl = Front, l = front, reals 0.

**Unresolved:** the Java has no glue delivering `MovementLink` output to `MovementController`; the model's module connects the shared events.
