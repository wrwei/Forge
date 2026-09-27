# ITER_SUMMARY — chemical_detector run 3, iteration 1 (cold codegen)

**What changed.** `java.generated.project/src/main/java/` started empty, so nothing was deleted. I wrote 19 Java files under `chemical_detector/` (annotation, constants, domain, event, mode, sensor, actuator, operation, controller), plus `result_codegen.json`, which traces all 81 requirements and names every file. There are two mode-nested controllers:

- `GasAnalysisController`: READING, ANALYSIS, NO_GAS, GAS_DETECTED, SOURCE_FOUND, CONCLUDED.
- `MovementController`: WAITING, GOING, AVOIDING, TRYING_AGAIN, AVOIDING_AGAIN, GETTING_OUT, FOUND, HALTED.

They communicate only through `MovementBus.turn/stop/resume`, whose calls extract to the same `turn`/`stop`/`resume` events that the movement controller triggers on. A small `ChemicalDetector` driver composes the two.

**Decisions the requirements did not settle.**
- **No `Final` mode.** Per CLAUDE.md, the controller whose theory is generated must not have one, and every mode needs an outgoing transition. The spec's terminal steps therefore become an emitting state followed by an idle sink: SOURCE_FOUND→CONCLUDED absorbs later gas readings, and FOUND→HALTED has a stop self-loop. A self-loop on the emitting state itself would re-run its entry action and re-send `stop`/`flag`.
- **Analysis and AvoidingAgain.** The second outgoing branch of each is an `else`, so the pair of guards is an explicit P / ¬P total cover. This is equivalent to the spec because Status has two values and "stuck" is exactly ¬progress.
- **Threshold guard.** It is written `ins >= THR` rather than as a `goreq(...)` call, so the guard stays transparent in the model. `goreq` is still implemented and used by `intensity`/`location`.
- **Invented defaults.** Constant values; "indicates target" means target chem id with intensity > 0; the sensor-position→Angle mapping; empty-reading defaults.

**Unresolved or ambiguous.** `intensity()` takes the peak over all sensors, as the spec states, not only the target chemical. A `turn` delivered while in AvoidingAgain is dropped in Java. Details are in `post_codegen.md` (12 items).
