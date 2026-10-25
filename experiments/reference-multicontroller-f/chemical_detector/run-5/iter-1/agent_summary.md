# Iteration 1 — chemical_detector, run 5 (condition F), cold codegen

**What I wrote.** A fresh tree in `java.generated.project/src/main/java/chemical_detector/` (20 files) plus `result_codegen.json` (110 rows; all 81 requirements covered, every file cited). Nothing was deleted: `src/` did not exist.

There are two mode-nested controllers:
- `GasAnalysis`: READING, ANALYSIS, NO_GAS, GAS_DETECTED, CONCLUDED.
- `Movement`: WAITING, GOING, FOUND, AVOIDING, TRYING_AGAIN, AVOIDING_AGAIN, GETTING_OUT.

They communicate only through turn/stop/resume, which GasAnalysis emits as `OutputEvent` records and Movement consumes as `InputEvent` records. The functions are on `@SensorService VehicleSensors`. move, randomWalk, shortRandomWalk and pause (for wait) are on `Vehicle`. changeDirection is an operation class. Clock `T` is reset on both entries to AVOIDING, and the stuck guards are complementary.

**Decisions the spec left open:**
- The terminal modes CONCLUDED (the spec's Final) and FOUND each self-loop on an invented `Tick` event, per `java_codegen_rules.txt`. flag and the halt sit on the stop transitions, so they are not re-emitted.
- Analysis and GasDetected guards are complementary pairs. The threshold guard is `ins >= THR` rather than a `goreq` call.
- The odometer is a sensor sample, not an input event.
- Constants and initial values are placeholders; the constants are small integers so the timed model stays tractable.
- `angle(x)` cycles Left, Right, Back, Front. "Target present" means the target chemical with intensity > 0.

**Unresolved:**
- The spec takes the intensity/location peak over all chemicals, not just the target. Implemented as written and flagged in `post_codegen.md`.
- `java_codegen_rules.txt` warns that Isabelle deadlock-freedom may be false for trigger-only states, and calls this a generator matter. If that still holds at this SHA, no Java can fix it.
- I only compile-checked and smoke-tested in the scratchpad. I ran no pipeline phase myself.
