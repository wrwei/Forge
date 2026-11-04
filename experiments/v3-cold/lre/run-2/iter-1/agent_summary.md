# ITER_SUMMARY: lre, pass 2

**Written.** No Java was present at the start, so nothing was deleted. I wrote 15 files under `lre/`:
- `annotation` and `event` (sealed `InputEvent`/`OutputEvent`);
- `sensor` (`Obstacle`, `ObstacleRegister`, `Sensor`) and `actuator` (`apply(OutputEvent)`);
- five `operation` classes (CalcVel, CalcCStc, CheckOPEZ, CalcCDyn, CalcCPA), which hold Var1–8;
- `controller` (`LreMode`, `LreConstants`, `LreController`).

`step` runs the five `compute()` calls, then named predicates, then a two-level if-else with one branch per Beh2–18. Also written: `result_codegen.json` (51/51 requirements, 15/15 files) and `post_codegen.{md,json}` (15 items). javac and a scratch smoke test pass.

**Decisions the requirements left open:**
- "No obstacle" is index 0, not −1, because nat cannot be negative; obstacle IDs are positive.
- DM5's zero defaults would give cda = 0 and permanent CAM. So cda adds the vertical separation `vdist(cdyn)`, which is large when no obstacle exists.
- CPA: relative velocity = AUV − obstacle, horizontal; tcpa divides by `|w|²+CPA_EPSILON` (0.000001; the model rounds it to 1).
- Absent-obstacle distance = 1000 m. Initial readings and advice = 0.0 (depth 0 ⇒ OPEZ until the first update, which is fail-safe).
- HCM advises 0 m/s (FR3), not 0.1 (system description).
- The "1" thresholds: speeds use NORMAL_SPEED, Beh4 distances use MIN_SAFE_DIST.
- Priority: reqOCM/endTask > inOpez > collision > reqHCM > Beh9 > Beh11 > Beh10. Beh11 precedes Beh10, which subsumes it; the reverse order would leave Beh11 dead.
- CheckOPEZ/CalcCPA call `closestStaticIndex()`/`closestDynamicIndex()` directly, because one operation cannot read another's field.
- vvel is signed (OP1), so Beh11 fires only when climbing.

**Unresolved:**
- CAM issues no evasive advice; none is specified.
- MOM↔HCM could cycle if dfltVert > vert. With the given equal defaults, it cannot.
