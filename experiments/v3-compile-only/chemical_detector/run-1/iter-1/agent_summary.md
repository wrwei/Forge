# Iteration 1 — chemical_detector

**What I wrote.** Base package `chemical_detector`. There are two controllers, each with an enum mode and a two-level `step(InputEvent)`:
- `GasAnalysisController`: Reading, Analysis, NoGas, GasDetected, Concluded.
- `MovementController`: Waiting, Going, Found, Avoiding, TryingAgain, AvoidingAgain, GettingOut, Halted.

Supporting code:
- Domain enums `Status`, `Angle`, `Loc` and the record `GasSample(c, i)`.
- The sealed `InputEvent` and `OutputEvent` interfaces.
- `ChemicalDetectorConstants`.
- Sensors: `ChemicalSensor` (analysis, intensity, location, goreq) and `OdometerSensor`.
- `Clock`, with `now()` and `pause(n)`.
- Actuators: `VehicleActuator` and `GasAnalysisActuator`.
- `ChemicalDetectorSystem`, which wires the two controllers together.

No Java was present beforehand.

**Decisions the requirements left open:**
- Constants: THR=2.0, LV=1.0, EVADE_TIME=1, STUCK_PERIOD=2, STUCK_DIST=1.0, OUT_PERIOD=1; whole numbers, so rounding changes nothing.
- Variables start at noGas/0.0/Front/front/0; gs starts empty.
- Chem is a nat id and Intensity is real. The record is named `GasSample` so the "contains sensor" rule cannot make it a sensor service.
- A sample indicates the target when c equals the target id (a sensor constructor argument) and i > 0.
- angle(x): positions 1–4 map to Left, Right, Back, Front, then repeat. On ties the first position wins. Empty readings give 0.0 and Front.
- move, randomWalk, shortRandomWalk and changeDirection are Vehicle commands, because the parameterless `compute()` profile cannot express them.
- Waiting's during action, randomWalk(), runs on every transition into Waiting, so no divergent self-loop is needed.
- The two Final states are the terminal modes Concluded and Halted. Found→Halted realises CD-MV-Beh9.
- turn, stop and resume are OutputEvent records in gas analysis and InputEvent records of the same names in movement.
- The odometer is a zero-argument sensor method; the clock T is `evasionStart`, restarted on every entry to Avoiding.

**Unresolved.** Obstacle events outside Going and TryingAgain are ignored, because no requirement covers them. Details are in `post_codegen.md`.
