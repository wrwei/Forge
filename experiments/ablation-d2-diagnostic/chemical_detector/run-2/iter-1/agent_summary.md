# Iteration 1 — chemical_detector, run 2 (D2, cold codegen)

**What changed.** The worktree started cold, with no Java present, so nothing was deleted. I wrote 21 files under
`chemical_detector/`:
- `data`: Status, Angle, Loc, Chem, GasSample.
- `event`: sealed InputEvent and OutputEvent, whose records carry the spec's lowercase event names.
- `mode`, `constants`, `timing.Clock`.
- `sensor`: GasSensorArray (analysis, intensity, location, goreq) and OdometerSensor.
- `actuator`: Vehicle and EventActuator.
- `operation.ChangeDirection`.
- Two mode-nested controllers, GasAnalysisController and MovementController.
- A wiring class, `system.ChemicalDetectorSystem`.

I also wrote `result_codegen.json`, which traces all 81 requirements and all 21 files, and `post_codegen.{md,json}`.

**Decisions the spec did not settle:**
- Terminal states. The gas-analysis "final state" becomes `Concluded`, which self-loops on `gas`. It keeps the reading but does not analyse it. Movement's `Found` self-loops on `stop` with no action. `flag` and `move(0, Front)` sit on the incoming stop transitions, so flag is emitted only once. I did this because the rules require every mode to have an outgoing transition.
- Guards come in complementary p / !p pairs: `stsIsNoGas`, `insAtOrAboveThr`, and progress vs stuck.
- The odometer is a sampled sensor (`odometer()` into d0/d1), not an input event.
- Waiting's during `randomWalk()` runs at the head of its mode block.
- Constant values are invented: THR=3, LV=1, EVADE_TIME=1, STUCK_PERIOD=3, STUCK_DIST=1, OUT_PERIOD=2.
- "Indicates target" is defined as same Chem with intensity > 0.
- Sensor position maps to Angle by declaration order.
- The GasSensor record is named `GasSample`, so the extractor won't take it for a sensor-service class.
- The platform operations are Vehicle methods. changeDirection is an operation class with no `compute()`.

**Unresolved:**
- CD-Fn2 makes the peak intensity a maximum over all chemicals, not just the target. I implemented it literally; this needs human review.
- CD-DC1 says "exactly one transition per behaviour", but the extra Concluded self-loop has no behaviour requirement of its own.
- I did not compile locally, because the protocol allows only the runner, so compile status is first known from phase 2a.
