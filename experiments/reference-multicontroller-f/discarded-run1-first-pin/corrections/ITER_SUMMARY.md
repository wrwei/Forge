# Iteration 1 — chemical_detector, condition F, run 1 (cold codegen)

**What I changed.** The tree started cold, with no Java present, so nothing was deleted. I wrote 20 files under `chemical_detector/`, plus `result_codegen.json` (81/81 requirements, every file traced) and `post_codegen.{md,json}`. There are two controllers in one package, `GasAnalysis` and `Movement`, each with a mode-nested `step()`. Turn, stop and resume go from GasAnalysis to Movement. Flag goes to the Vehicle. `ChemicalDetector` wires the two together. I checked compilation with `javac` in the scratchpad only.

**Decisions the spec did not settle:**
- I added a `Tick` input (the cycle with no event). Terminal modes `CONCLUDED` (not "Final") and `FOUND` self-loop on Tick only, so the flag is sent once.
- The odometer (CD-Evt3) is a sensor query sampled into d0/d1, not an event.
- Guard pairs are explicit complements `p`/`!p`.
- Intensity is a `double`, so `thr` is numeric. Chem is a record.
- A reading indicates gas iff it has the target Chem with i > 0. `angle(x)` maps positions 1..4 to Left, Right, Back, Front.
- I invented all constant values (1) and all initial values.

**Outcome: instrument defect, run halted.** Compile and coverage passed. Every Gradle-backed phase (preflight, t2m, m2m, m2t, dafny_gen, isabelle_gen) failed before running, with `PermissionError: [Errno 13] Permission denied: forge.transformations/gradlew`. The pinned SHA 6b78ebe8 stores that file as mode 100644, not executable (the java.generated.project wrapper is 100755). All later phases failed only for lack of input, so they measured nothing about the Java. The fix (`chmod +x`) lies inside `forge.transformations/`, which I must not touch, and no Java change can work around it. I did not run iteration 2: it would fail identically and waste the cap. This is for the operator to repair before the run continues.
