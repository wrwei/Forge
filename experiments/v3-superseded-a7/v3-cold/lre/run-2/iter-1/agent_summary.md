# ITER_SUMMARY — lre, pass 2, iteration 1

No Java existed beforehand. Wrote 15 files under `java.generated.project/src/main/java/lre/` in the five layers, plus `result_codegen.json` (172 entries; every requirement, file and public element mapped) and `post_codegen.{md,json}`.

**Structure.** `LreController.step` runs CalcVel, CalcCStc, CalcCDyn, CheckOPEZ and CalcCPA, declares named predicates (including `inOpez`), then a two-level if-else over OCM/MOM/HCM/CAM. `Sensor` holds an `ObstacleRegister` of `Obstacle` records; `Actuator.apply` receives `AdvVel`/`AdvHdng`. Var1–8 are operation fields read through same-named getters, not duplicated in the controller.

**Decisions the requirements did not settle**
- Index -1 ("none") is impossible for nat: index 0 means none, obstacles are numbered from 1, ties go to the lowest index.
- Safe large distance: 1000000.0 m.
- CPA: relative velocity (obstacle minus AUV); tcpa = r·w/(|w|²+ε), ε = 0.000001 (rounded to 1 in the model); cda adds the current vertical separation, which keeps the no-obstacle case safe. Position accessors return 0 as DM5 says (OP5 says "large").
- CheckOPEZ and CalcCPA call the same sensor selection functions as CalcCStc/CalcCDyn, because compute() may read only the sensor.
- Priority: reqOCM, inOpez, collision→CAM, then endTask, Beh9, Beh11, Beh10, reqHCM in MOM; Beh13 last in HCM. Lower branches conjoin negations of earlier guard-only guards.
- Requirement literals (1, 0) are named constants; HCM advises 0 m/s (FR3), not the description's 0.1.
- Readings and advice start at zero.

**Unresolved**
- CAM's evasive manoeuvre has no specified output; CAM has no entry action.
- Beh18 leaves CAM on cda alone: after an obstacle passes on a near line, CAM persists until reqOCM.
- Beh11 tests signed vvel, so a fast descent never triggers it.
- DC1 read as "no duplicated transition": MOM has five guard-only transitions.
