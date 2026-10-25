# Iteration 1 — chemical_detector, condition F, run 1 (cold codegen)

**What I changed.** The tree started cold, with no Java present, so nothing was deleted. I wrote 20 files under `chemical_detector/`, plus `result_codegen.json` (81/81 requirements, every file traced) and `post_codegen.{md,json}`. There are two controllers in one package, `GasAnalysis` and `Movement`, each with a mode-nested `step()`. Turn, stop and resume go from GasAnalysis to Movement (same-named OutputEvent/InputEvent records). Flag goes to the Vehicle. `ChemicalDetector` wires the two together. I checked compilation with plain `javac` and ran a smoke driver, both in the scratchpad only. No pipeline phase was run outside the runner.

**Decisions the spec did not settle:**
- *Terminal modes.* I added a `Tick` input, which is the cycle with no event. `CONCLUDED` (used instead of a state named "Final") and `FOUND` self-loop on Tick only, so the flag is sent once.
- *Odometer (CD-Evt3).* I made it a sensor query, `Sensor.odometer()`, sampled into d0/d1 inside the actions, not an event.
- *Guard pairs.* Analysis, GasDetected and AvoidingAgain each use explicit complements `p`/`!p`, so the pair always covers every case. The gasD branch is written `!(sts == noGas)`.
- *Types.* Intensity is a `double`, so `thr` is numeric. Chem is a record with a nat id.
- *analysis.* A reading indicates gas iff it has the target Chem with i > 0. `intensity` and `location` take the maximum over all values. `angle(x)` maps positions 1..4 to Left, Right, Back, Front.
- *Defaults I invented.* All constants are 1, all reals and positions start at 0/Front, and all durations are in Clock units.

**Unresolved.** The spec gives no constant values. CD-Fn1 does not define what it means for a reading to "indicate" the target. These are listed in `post_codegen.md` for human review.
