# ITER_SUMMARY — lre, run 1, iteration 1

No Java was present at the start.

**Written** (package `lre`, 15 files): the `RoboChartType` annotation; sealed `InputEvent` (six records) and `OutputEvent` (`AdvVel`, `AdvHdng`); `Obstacle` record, `ObstacleRegister`, `Sensor`; `Actuator` (receives OutputEvents via `apply`); operations `CalcVel`, `CalcCStc`, `CheckOPEZ`, `CalcCDyn`, `CalcCPA`; `LreMode`, `LreConstants`, `LreController`. `step` runs the five operations, declares named predicates (including `inOpez`), then a mode-nested if-else with 17 transitions, one per Beh2–Beh18. `result_codegen.json` traces all 51 requirements.

**Decisions the requirements did not settle:**
- "No obstacle" is index 0, not -1, because nat cannot be negative. Obstacle identifiers start at 1.
- The safe large distance is 1000000.0 (private to `Sensor`).
- The CPA is horizontal with constant vertical separation: cda includes vdist, so cda is large when there is no dynamic obstacle. MIN_REL_SPEED_SQ = 0.000001 keeps the division defined; the model rounds it up to 1.
- CheckOPEZ and CalcCPA call the same sensor selection functions as CalcCStc and CalcCDyn instead of reading those operations, because the profile limits compute() right-hand sides.
- HCM advises 0 m/s (FR3), not the 0.1 m/s in the system description.
- Priorities: reqOCM first, then endTask, then inOpez, then collision, then the static-obstacle transitions, with reqHCM last. Beh11 comes before Beh10 because their thresholds are equal and Beh11 would otherwise never fire.
- CAM issues no command of its own (no output is specified).
- `step(null)` means a cycle with no operator input.

**Unresolved:** none blocking. All open points are listed in `post_codegen.md`.
