# Iteration 1 — chemical_detector, condition F, run 1 (cold codegen)

**Changed.** Wrote 18 Java files under `chemical_detector/`. No earlier Java existed, so nothing was deleted.
- Mode-nested controllers: `GasAnalysisController` and `MovementController`.
- Turn, Stop and Resume share names across `InputEvent` and `OutputEvent`, so they unify as inter-controller events.
- Functions and the odometer are on `@SensorService ChemSensors`. Operations are on `Vehicle`. Clock T is `evasionStart`, reset from `Clock.nowMs()`.
- `ChemicalDetector` wires the two controllers together.

Also wrote `result_codegen.json` (81/81 requirements) and `post_codegen.{md,json}`. Checked with `javac` and a scratch harness, not via any phase.

**Decisions the spec left open:**
- The spec has no tick event. GA FINAL self-loops on `gas` (keeps `gs`, no analysis). MV FOUND self-loops on `obstacle` (records `l`, stays halted, no re-flag).
- Autonomous decisions use complementary guards: `sts == noGas` and its negation, `ins >= THR` and its negation, progress and its De Morgan complement.
- Odometer is a zero-argument sensor method.
- Invented: constants (THR=2, LV=1, EVADE_TIME=1, STUCK_PERIOD=2, STUCK_DIST=1, OUT_PERIOD=1), TARGET_CHEM=1, "indicates" means c==target and i>0, and the position→Angle map.

**Outcome.** 11/12 phases passed (FDR4 9/9, Dafny both controllers, vacuity 0 blocking). `isabelle_verify` failed outside my theories: building the upstream `ITrees` session's document hit `lualatex: command not found` / `Failed to build document "document"`. This is an environment or instrument defect. Java cannot address it, so I made no change and **stopped to report** rather than spend iterations on identical reruns. Fix: install a TeX distribution or prebuild the `ITrees`/`Z_Machine` heaps, then resume at iteration 2.

**For human review:** CD-Fn2's peak ignores the chemical's identity, and CD-Evt3 is typed as an event but describes sampling.
