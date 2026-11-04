# ITER_SUMMARY: chemical_detector, run 2

## What was written

The package is `chemical_detector`. It has two profile controllers:

- `GasAnalysisController`: Reading, Analysis, NoGas, GasDetected, Concluded.
- `MovementController`: Waiting, Going, Found, Avoiding, TryingAgain, AvoidingAgain, GettingOut.

Turn, stop and resume are shared `OutputEvent`/`InputEvent` records.

- `ChemicalSensor`: analysis, intensity, location, goreq.
- `VehicleSensor.odometer()`.
- `Clock`, which provides `evasionStart`/since.
- `VehicleActuator`: move, randomWalk, shortRandomWalk, changeDirection, pause (the wait) and flag.
- `ChemicalDetectorSystem`: the wiring.

## Decisions the requirements left open

- **Constants:** THR=3, LV=1.0, EVADE_TIME=1, STUCK_PERIOD=3, STUCK_DIST=1.0, OUT_PERIOD=2. STUCK_PERIOD > EVADE_TIME keeps both AvoidingAgain outcomes reachable.
- **Initial values:** sts=noGas, ins=0, anl=a=Front, l=front, d0=d1=0.
- **Target chemical:** a constructor argument. A reading indicates it when some entry has that chemical with i > 0.
- **Empty readings:** intensity returns 0 and location returns Front.
- **Sensor positions:** position k maps to Front/Right/Back/Left, repeating. On a tie, the first sensor wins.
- **Types:** Chem is a record of a nat. Intensity is nat.
- **Operations:** the operations are actuations taking controller variables, so they are actuator calls, not `compute()` classes.
- **Odometer:** a sampled sensor value.
- **Waiting's during randomWalk():** issued on every entry and once at start-up.
- **Final states:** gas analysis ends in Concluded. Found is terminal (CD-MV-Beh9 has no transition of its own).
- **Avoiding entry:** restarts the clock and re-samples d0.

## Iteration 2

- **Preflight error:** `GasSensor.i` was reported as a double without `@RoboChartType("real")`, although the record component carries it. I think this is a pipeline defect: the lint misses annotations on record components. Workaround: Intensity (GasSensor.i, ins, intensity, goreq, THR) is now nat.
- **Rule8 warnings (non-blocking):** these concern stop/resume before the autonomous branches of AvoidingAgain. Kept. CD-MV-FR6 requires all four transitions, and moving the autonomous ones first would make stop and resume unreachable.

## Unresolved

None.
