# Iteration 1 — cold codegen (chemical_detector, run 2, condition E)

**What changed.** The tree started empty, so there was nothing to delete. I wrote 19 files under `chemical_detector/`:
- Two mode-nested controllers, `GasAnalysisController` and `MovementController`, in `controller`. They communicate only through `turn`, `stop` and `resume` (sealed `InputEvent`/`OutputEvent`).
- Data enums and the `GasSensor` record in `data`.
- `GasSensorArray` (analysis, intensity, location, goreq) and `OdometerSensor` in `sensor`.
- `Vehicle` (move, randomWalk, shortRandomWalk, changeDirection, pause) and an `Actuator` output port in `actuator`.
- `Clock`, `DetectorConstants`, `RoboChartType`, and a `ChemicalDetector` class that wires the two controllers together.

Also written: `result_codegen.json` (81/81 requirements) and `post_codegen.{md,json}`. Compiles (javac `--release 17`); a scratchpad smoke test passed every path.

**Decisions the spec left open:**
- **No final states.** Gas analysis ends in `Concluded`, whose only transition is a gas self-loop that keeps the reading unanalysed. `Found` self-loops on `obstacle`, recording `l`, with no motion. Every mode needs an outgoing transition.
- **Analysis guards.** They are `sts == noGas` and its negation, which equals `gasD` because Status has two values.
- **Chem** is the enum {TARGET, OTHER}. `analysis` returns gasD iff some entry is TARGET with i > 0.
- **`angle(x)`** maps positions 1–4 to Front, Right, Back, Left, cyclically. Ties go to the lowest position.
- **Invented values.** Constants: THR 2.0, LV 1.0, EVADE_TIME 1, STUCK_PERIOD 2, STUCK_DIST 1.0, OUT_PERIOD 1. Initial values: a and anl Front, l front.
- **Odometer** is read as a sensor query, not an event.
- **`changeDirection`** sits on `Vehicle`, layered on `move`.
- **Autonomous transitions** fire on `step(null)`.

**Unresolved or ambiguous:**
- CD-Fn2/Fn3 take the maximum over all entries regardless of chemical.
- CD-MV-Var4 says `l` updates on every obstacle, but Waiting, Avoiding, AvoidingAgain and GettingOut accept no obstacle.
- The requirement names still say "Final".
