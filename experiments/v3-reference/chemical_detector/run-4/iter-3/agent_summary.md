# ITER_SUMMARY: chemical_detector, run 4

**What I wrote.** 22 files in `chemical_detector`, with two controllers (CD-ARCH2). `GasAnalysisController` has the modes Reading, Analysis, NoGas, GasDetected and Concluded. `MovementController` has the seven movement states. Turn, stop and resume are `OutputEvent` records of the gas-analysis subsystem and `InputEvent` records of the movement subsystem (shared events). `GasSensorArray` holds analysis, intensity, location and goreq. `VehicleSensor` holds the odometer. `Clock` drives the evasion timer.

**Decisions the requirements left open:**
- Constants: THR=3.0, LV=1.0, EVADE_TIME=2, STUCK_PERIOD=4, STUCK_DIST=1.0, OUT_PERIOD=2.
- Variables start at noGas, 0.0, Front and front. Each is overwritten before it is read.
- In analysis, a sample "indicates" the target when it is the target Chem with positive intensity. angle(x) follows the Angle declaration order.
- The odometer is a sampled sensor method, not an event.
- Waiting's during action randomWalk runs on each transition into Waiting, but not on the initial entry.
- Found and Concluded are terminal modes.
- The GasSensor record is named `GasSample`, to keep "sensor" out of its name.
- The waits use `MovementActuator.pause`.
- No Java class connects the two controllers.

**Iteration 2.** FDR4 failed to parse and Isabelle failed on type coercion. The cause was the operation calls in `step`: the pipeline named all four operations after their method, `compute`, merged their bodies, and turned their parameters into untyped constants. I believe this is a pipeline limitation. As a workaround, the controller sends the Vehicle commands as output events (`move!a`, `halt`, `randomWalk`, `shortRandomWalk`, `changeDirection!l`). FDR4 then passed.

**Iteration 3.** Isabelle still injected the bodies of operations with parameters into every controller's Init. Every `compute()` is now parameterless, and `MovementActuator` records the commanded velocity and heading.

**Unresolved.** Taken literally, CD-Fn2 takes the maximum over all chemicals. All points are in `post_codegen.md`.
