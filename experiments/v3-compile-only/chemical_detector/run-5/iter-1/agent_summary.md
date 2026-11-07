# ITER_SUMMARY — chemical_detector, run 5

## Iteration 1

**What I wrote.** There are two controllers, one per reasoning subsystem.
`GasAnalysisController` has the states Reading, Analysis, NoGas, GasDetected
and a terminal CONCLUDED. `MovementController` has Waiting, Going, Found,
Avoiding, TryingAgain, AvoidingAgain and GettingOut. Around them are the
domain types (Status, Angle, Loc, Chem, GasSample), the sealed
`InputEvent`/`OutputEvent` hierarchies, `ChemicalDetectorConstants`, a `Clock`
(clock T is `since(evasionStart)`, and `pause` gives the waits), two sensors
(the gas-analysis functions and the odometer) and two actuators. turn, stop
and resume are declared as outputs of gas analysis and inputs of movement,
with the same names, so the module connects them.

**Decisions the requirements left open:**
- Constant values: THR 2, LV 1, EVADE_TIME 1, STUCK_PERIOD 2, STUCK_DIST 1,
  OUT_PERIOD 1. All are integral, so rounding is harmless.
- A value "indicates" the target when its chemical equals a configured target
  and its intensity is above 0.
- Sensor positions 1–4 map to Left/Right/Back/Front and wrap round. A tie goes
  to the lowest position.
- Intensity is real and Chem is a record with a nat id. The GasSensor record is
  named GasSample, to avoid the sensor-name convention.
- move, randomWalk, shortRandomWalk and changeDirection are Vehicle actuator
  calls. An argument-less `compute()` cannot pass lv, a or l.
- randomWalk runs on entry to Waiting, because the profile has no during
  actions.
- NoGas→Reading is guarded by `sts == noGas`, which always holds there.
- Found is terminal; no Found→Final step.
- The odometer is a sensor read, not an event.

**Unresolved.** Whether evadeTime should count towards stuckPeriod. There is
no Java runtime wiring between the subsystems.

Details: `post_codegen.md`.
