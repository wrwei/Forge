# Iteration 1 — chemical_detector, run 5 (cold codegen)

**What changed.** The tree was empty (no stale Java; nothing deleted). I wrote 19 Java files under `chemical_detector/` plus `result_codegen.json` (111 rows; all 81 requirements traced, every file claimed). There are two mode-nested controllers: `GasAnalysisController` (READING, ANALYSIS, NO_GAS, GAS_DETECTED, FINAL) and `MovementController` (WAITING, GOING, FOUND, AVOIDING, TRYING_AGAIN, AVOIDING_AGAIN, GETTING_OUT). They communicate only through `MovementCommands` (turn/stop/resume); `ChemicalDetector` wires them to the Vehicle. The project compiles.

**Decisions the spec left open:**
- **Terminal modes.** FINAL and FOUND have no outgoing transition in the spec. The rule files require a tick-triggered self-loop on each, so I added a `Tick` event. Rerouting GA's terminal transition to a live state would break CD-GA-Beh6. flag and halt run once, on the transitions into FOUND.
- **Guards.** The extractor can see them all. GasDetected: `ins >= THR` (which is `goreq`) and else. Analysis: `sts == noGas` and `sts == gasD`. AvoidingAgain: progress and else. Each state's guards cover every case.
- **Types.** Intensity is a `double`. Chem is the enum {TARGET, OTHER}; a record or String would be extracted as `real` or leak into Sensors.
- **Other modelling choices.** The odometer is a sampled sensor read (`d0 = odometer`). `changeDirection(l)` is extracted as an output event, not an operation. Waiting's `randomWalk()` is a top-of-block statement.
- **Invented defaults.** All constants are 1, inside the verifier type ranges. `analysis` means TARGET with i > 0. Sensor positions map to directions as 1 Front, 2 Right, 3 Back, 4 Left.

**Unresolved.**
- CD-MV-Beh9 is named "Found_to_Final", but its description only says the subsystem stays halted.
- CLAUDE.md says the theory-generated controller must have no Final state; `java_codegen_rules.txt` requires a tick self-loop instead. I followed the rules file.

Details are in `post_codegen.md`.
