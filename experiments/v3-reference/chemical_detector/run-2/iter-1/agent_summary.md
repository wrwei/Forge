# ITER_SUMMARY: chemical_detector, run 2

## Iteration 1: what was written and why

The package is `chemical_detector`. It has two controllers in the profile's two-level if-else shape:

- `GasAnalysisController`, with modes Reading, Analysis, NoGas, GasDetected and Concluded.
- `MovementController`, with modes Waiting, Going, Found, Avoiding, TryingAgain, AvoidingAgain and GettingOut.

Gas analysis sends `OutputEvent` Turn, Stop and Resume. Movement receives them as `InputEvent` records of the same names, so they become the shared events.

Other parts:

- `ChemicalSensor`: the functions analysis, intensity, location and goreq.
- `VehicleSensor.odometer()`: a sampled distance.
- `Clock`: the evasion timer (`evasionStart`, compared with `since`).
- `VehicleActuator`: move, randomWalk, shortRandomWalk, changeDirection, `pause` (the RoboChart wait) and flag.
- `ChemicalDetectorSystem`: the Java wiring.

## Decisions the requirements left open

- **Constants:** THR=3.0, LV=1.0, EVADE_TIME=1, STUCK_PERIOD=3, STUCK_DIST=1.0, OUT_PERIOD=2. All are whole numbers, and STUCK_PERIOD > EVADE_TIME so both AvoidingAgain outcomes are reachable.
- **Initial values:** sts=noGas, ins=0, anl=a=Front, l=front, d0=d1=0.
- **Target chemical:** a constructor argument. A reading indicates it when some entry has that chemical with i > 0.
- **Empty readings:** intensity returns 0 and location returns Front.
- **Sensor positions:** position k maps to Front/Right/Back/Left, clockwise and repeating. On a tie, the first sensor wins.
- **Types:** Chem is a record of a nat. Intensity is real.
- **Operations:** the operations are actuations whose arguments are controller variables, so they are actuator calls, not `compute()` classes.
- **Odometer:** read as a sensor value, not received as an event.
- **Waiting's during randomWalk():** issued on every entry to Waiting and once at start-up.
- **Final states:** gas analysis ends in a terminal mode, Concluded. Found is terminal, so CD-MV-Beh9 has no transition of its own.
- **Avoiding entry:** restarts the clock and re-samples d0.

## Unresolved

Nothing yet. The full review is in `post_codegen.md`.
