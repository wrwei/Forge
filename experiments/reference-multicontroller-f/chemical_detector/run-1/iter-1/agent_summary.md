# Iteration 1 — chemical_detector, condition F, run 1 (cold codegen)

**Changed.** Wrote 18 Java files under `chemical_detector/`. No earlier Java existed, so nothing was deleted.
- Two mode-nested controllers: `GasAnalysisController` (READING, ANALYSIS, NO_GAS, GAS_DETECTED, FINAL) and `MovementController` (WAITING, GOING, FOUND, AVOIDING, TRYING_AGAIN, AVOIDING_AGAIN, GETTING_OUT).
- Turn, Stop and Resume use the same names in `InputEvent` and `OutputEvent`, so they unify as inter-controller events.
- The functions (analysis, intensity, location, goreq) and the odometer live on `@SensorService ChemSensors`.
- The operations are on `Vehicle`. Clock T is `evasionStart`, reset from `Clock.nowMs()`.
- `ChemicalDetector` wires gas-analysis outputs into the movement controller.

Wrote `result_codegen.json` (81 requirements, every file cited) and `post_codegen.{md,json}`. Compiled with `javac` and ran a scenario harness in scratch, not via any pipeline phase.

**Decisions the spec left open:**
- **Sink states.** The spec has no tick event. GA FINAL self-loops on `gas` (keeps `gs`, never analyses it). MV FOUND self-loops on `obstacle` (records `l`, stays halted, flag not re-sent). This satisfies the every-mode-has-an-outgoing-transition rule using domain events.
- **Guards.** Every autonomous decision has complementary guards: `sts == noGas` and its negation, `ins >= THR` and its negation, progress and its De Morgan complement. The threshold uses `>=` directly. `goreq` exists but is not called in a guard.
- **Odometer.** Modelled as a zero-argument sensor method, not an input event.
- **Invented values.** Constants THR=2, LV=1, EVADE_TIME=1, STUCK_PERIOD=2, STUCK_DIST=1, OUT_PERIOD=1. TARGET_CHEM=1. "Indicates target" means c==target and i>0. Sensor position maps to Front/Right/Back/Left.

**Unresolved / for human review.**
- CD-Fn2 takes the peak over all chemicals, so a strong non-target reading can trigger "found".
- CD-Evt3 is typed as an event but describes sampling.
- NO_GAS→READING is a bare `currentMode = READING` (no guard, no trigger, as specified). I do not know whether the extractor reads that as a transition.
