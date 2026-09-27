# Iteration 1 — chemical_detector run 4 (cold codegen)

**Changed:** Wrote a new tree under `java.generated.project/src/main/java/chemical_detector/` (21 files) and `result_codegen.json` (102 rows, all 81 requirements). No Java existed beforehand, so nothing was deleted.

- `GasAnalysisController` has the modes Reading, Analysis, NoGas, GasDetected and Concluded. `MovementController` has the seven specified modes.
- Both use a single `step(InputEvent)` method: mode-nested if-else with named predicates declared first.
- Gas analysis emits turn, stop and resume through an `OutputPort`. `system.MovementLink` delivers them to movement. The Vehicle receives flag.
- The functions and `odometer` are on a `@SensorService` `Sensor`. Vehicle operations are Vehicle methods, and `pause` gives the waits. `ChangeDirection` is in `operation`. The clock `T` is reset from `Clock.nowMs()`.
- A scratch harness drove search, homing, avoidance, stuck recovery and found/halt. It passed.

**Decisions the spec left open:**
- *Terminal modes.* Every mode needs an outgoing transition. Concluded accepts later gas readings without analysing them. Found self-loops on obstacle and commands nothing. flag plus halt run on the stop transitions, so flag is emitted once.
- *Guards are exact complements* (`p` / `!p`). This keeps the else-if priority true once transitions are unordered.
- *Odometer* is a sampled sensor value, not a trigger.
- *Intensity* is a real compared through goreq. *Chem* is a record with a nat id.
- *"Indicates the target"* means an entry for the target Chem with i > 0. The target is a startup parameter.
- *Sensor position → Angle* follows Left, Right, Back, Front order.
- *Constants and initial values* were invented and are listed in `post_codegen.md`.

**Unresolved:**
- CD-Fn2/Fn3 take the peak over all chemicals, while CD-Fn1 considers only the target. Both implemented literally.
- AvoidingAgain has no specified entry behaviour.
- I cannot yet tell whether the Dafny/Isabelle invariants will be non-vacuous.
