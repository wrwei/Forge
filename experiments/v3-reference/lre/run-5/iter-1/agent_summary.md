# ITER_SUMMARY — lre, run 5

## Iteration 1

**What was written.** Package `lre` contains 15 files. `annotation.RoboChartType`; sealed `InputEvent` (ReqVel, ReqHdng, ReqOCM, ReqMOM, ReqHCM, EndTask) and `OutputEvent` (AdvVel, AdvHdng); `controller.LreMode` (OCM first), `LreConstants`, `LreController`; `sensor.Obstacle`, `ObstacleRegister`, `Sensor`; `actuator.Actuator`; and five operations (CalcVel, CheckOPEZ, CalcCStc, CalcCDyn, CalcCPA). `step` runs the five `compute()` calls, then the named predicates, then the mode-nested if-else: one branch per LRE-Beh transition.

**Decisions the requirements did not settle:**
- "-1 = none" is impossible for a `nat`, so the sentinel is `NO_OBSTACLE = 0` and the register rejects index 0.
- The safe large distance is 1,000,000 m.
- DM5 has the field accessors return zero, but OP5 expects large-distance defaults. CalcCPA adds `hdist(cdyn) - sqrt(ns²+ew²)` to cda. That term is 0 for a real obstacle and large when there is none.
- CPA is computed in the horizontal plane, with closing velocity = AUV velocity − obstacle velocity and `CPA_EPSILON` in the denominator (no NaN). The model rounds it up to 1.
- HCM advises 0 m/s, following FR3; the system description says 0.1.
- Priority within MOM and HCM: reqOCM, then OPEZ, then collision, then the other transitions. Beh11 comes before Beh10, so that Beh11 is not dead.
- CheckOPEZ and CalcCPA call `sensor.closestStaticIndex()`/`closestDynamicIndex()` themselves, because an operation may not read another operation's getter.
- vvel is the signed rate of climb.
- Entering CAM issues no output.
- Initial readings and advised values are 0.
- `step(null)` means a cycle with no operator input.

All of these are recorded in `post_codegen.md`.

**Unresolved:** none blocking. A scratch smoke test (outside the project) passed 26/26, covering the CPA edge cases and every mode transition.

**Java found at start:** none.
