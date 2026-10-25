# Iteration 1 — chemical_detector, condition F, run 2 (cold codegen)

**What I changed.** I wrote 19 Java files under `chemical_detector/`. There was no pre-existing Java to delete. The packages are annotation, types, constants, event, sensor, actuator, operation, timing, mode and controller. There are two controllers, `GasAnalysisController` and `MovementController`; each has a single `step(InputEvent)`. The gas-analysis controller emits turn/stop/resume through `Actuator.apply(new OutputEvent.X(..))`, and the movement controller consumes them as `InputEvent` records with the same names and payloads. I also wrote `result_codegen.json` (113 rows; every requirement and every file covered) and `post_codegen.{md,json}`.

**Decisions the requirements did not settle.**
- The GA Final pseudo-state became the terminal mode `Concluded`, and Found's "stays halted" became a self-loop. Both are Tick-triggered, following the codegen rules. `Tick` is therefore an input event the spec does not define.
- The odometer is a sampled sensor value (`Sensor.odometer()`), not an event, because d0/d1 are sampled inside actions.
- Complementary guards are negations of one predicate (`!stsIsNoGas`, `!insAtOrAboveThr`, and `!within && !advanced` for the stuck case), so each mode's guards cover every case.
- The threshold check is `ins >= THR`; `goreq` implements the same order inside `intensity` and `location`.
- Invented defaults: thr=2.0, lv=1.0, evadeTime=1, stuckPeriod=3, stuckDist=1.0, outPeriod=1; `Chem = {target, other}`; sensor position k maps to Left/Right/Back/Front cyclically; Intensity is a `double`.
- Waiting's during-action `randomWalk()` is placed at the head of the Waiting mode block.

**Unresolved.**
- CD-Fn2 asks for the maximum over all sensors, not only target-chemical ones. I followed it literally and flagged it as ambiguous.
- Instrument note: `CLAUDE.md` in this worktree carries two "ABLATION (condition D2)" passages, but the condition is F. The preflight checks only the rule files, which are the full set. I did not compile or run any phase outside the runner.
