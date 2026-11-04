# chemical_detector, pass 10, iteration 1

**Written.** 19 Java files under `chemical_detector/`. Two controllers, `GasAnalysisController` (Reading, Analysis, NoGas, GasDetected, SourceLocated) and `MovementController` (Waiting, Going, Found, Avoiding, TryingAgain, AvoidingAgain, GettingOut), each a two-level if-else `step` with named predicates. Also: domain enums and records, `InputEvent`/`OutputEvent`, `DetectorConstants`, sensors (`GasSensorArray` functions, `OdometerSensor`, `Clock`), and actuators (`VehicleActuator`, `SteeringActuator`, `MovementLinkActuator`). The trace covers all 81 requirements and 19 files. It compiles; a smoke run passed.

**Decisions the requirements did not settle:**
- Operations: move, randomWalk and shortRandomWalk are `VehicleActuator` methods, and changeDirection is `SteeringActuator.changeDirection(l)`. They take controller arguments, which the profile's parameterless `compute()` cannot.
- The record is named `GasSample`, not `GasSensor`, because names containing "sensor" mark sensor services.
- Intensity is real. Chem is a record with a species id, and the target chemical is set in `GasSensorArray`.
- The odometer is a zero-argument sensor method, sampled into d0 and d1.
- randomWalk() (during Waiting) is issued on every transition into Waiting. The Vehicle starts in a random walk.
- NoGas→Reading uses a constant-true predicate, because the profile forbids a transition with no event and no guard.
- turn, stop and resume are `OutputEvent` records in gas analysis and same-named `InputEvent` records in movement.
- Terminal modes are SourceLocated and Found. The clock T is the field `evasionStart`.
- Invented constants: THR 1.0, LV 1.0, EVADE_TIME 1, STUCK_PERIOD 2, STUCK_DIST 1.0, OUT_PERIOD 1.
- angle(x): sensor position k faces the k-th Angle value (Left, Right, Back, Front).
- "Indicates the target" means the target chemical with positive intensity.

**Unresolved:** I cannot tell from the rules how the extractor models actuator calls with two arguments (`move(lv, a)`), or whether same-named input and output records become one shared event. Both are recorded in `post_codegen`.
