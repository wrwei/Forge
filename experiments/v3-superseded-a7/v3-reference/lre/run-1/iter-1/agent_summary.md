# Iteration 1 — LRE

**Written.** 15 Java files under `lre/`: annotation; sealed `InputEvent` (6 records) and `OutputEvent` (`AdvVel`, `AdvHdng`); `Obstacle`, `ObstacleRegister`, `Sensor`; `Actuator`; five operations (`CalcVel`, `CalcCStc`, `CheckOPEZ`, `CalcCDyn`, `CalcCPA`), each with one `compute()` made of assignments; `LreMode` (OCM first), `LreConstants`, and `LreController`. `step()` runs the five operations, declares named predicates, then a mode-nested if-else with one branch per Beh requirement. Trace: 162 entries covering all 51 requirements and all files.

**Decisions the requirements left open:**
- "−1 = none" can't be a nat. Sentinel `NO_OBSTACLE = 0`; obstacle identifiers start at 1.
- Safe large distance = 1000000 m. Field accessors return 0 when there is no obstacle (DM5).
- CPA is horizontal, using the closing velocity, plus a constant vertical separation `vdist(cdyn)`. That separation is what keeps "no dynamic obstacle" from producing cda = 0 (DM5 and OP5 conflict on this).
- `CPA_SPEED_SQ_EPS = 0.000001` is added to the denominator, so cda/tcpa stay defined at zero relative velocity. The model rounds it up to 1.
- CheckOPEZ and CalcCPA re-query `closestStaticIndex()`/`closestDynamicIndex()`, because compute() may use only the sensor. The value is the same as CalcCStc/CalcCDyn give in that step.
- Priority order: reqOCM first in MOM, HCM and CAM. MOM order: endTask, inOpez, CAM, Beh9, Beh11, Beh10, reqHCM. Beh11 comes before Beh10 so it isn't subsumed. Lower predicate-only branches negate the guards above them.
- HCM advises 0 m/s (FR3), not 0.1 m/s as the system description says.
- vvel is signed (rate of climb), and Beh11 is taken literally.
- `step(null)` means no operator input in that cycle.

**Unresolved.** CAM's evasive manoeuvre has no specified output, so none is issued on entering CAM. All 12 points are in `post_codegen.md`.
