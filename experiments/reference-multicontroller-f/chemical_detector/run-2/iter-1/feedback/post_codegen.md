# post_codegen — chemical_detector (iteration 1)

**Status:** uncertain

The initial Java has two controllers, `GasAnalysisController` and `MovementController`, which communicate through turn/stop/resume. All 81 requirements are traced in `result_codegen.json`.

## Issues

1. **design_choice:** GA's Final pseudo-state is realised as the terminal mode `Concluded`, which has a Tick-triggered self-loop (`GasAnalysisController.java:76-80`; CD-GA-Beh6). This follows the codegen rule that every mode needs an outgoing transition.
2. **design_choice:** Found "stays halted" (CD-MV-Beh9) is realised as a Tick self-loop. `Tick` is an input event that the specification does not define (`MovementController.java:93-97`, `InputEvent.java:30`).
3. **design_choice:** The odometer is modelled as the sampled sensor value `Sensor.odometer()`, not as an input event (CD-Evt3).
4. **design_choice:** Complementary guards are written as the negation of one named predicate: `stsIsNoGas`/`!stsIsNoGas` and `insAtOrAboveThr`/`!insAtOrAboveThr`. The threshold check is `ins >= THR`; `goreq` is the same order and is used inside `intensity` and `location` (CD-Fn4).
5. **invented_default:** Constant values: thr=2.0, lv=1.0, evadeTime=1, stuckPeriod=3, stuckDist=1.0, outPeriod=1 (CD-Const1..6).
6. **invented_default:** `Chem = {target, other}`. `analysis` returns gasD iff some sensor reports `target`. Sensor position k maps to Left/Right/Back/Front cyclically. Intensity is a `double`.
7. **ambiguous_requirement:** `intensity` takes the maximum over all sensors, not only target-chemical ones (CD-Fn2 followed literally).
8. **design_choice:** Waiting's during-action `randomWalk()` sits at the head of the Waiting mode block.

**Next step:** run the pipeline.
