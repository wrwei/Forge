# ITER_SUMMARY — lre, run 3

## Iteration 1

**Written.** 14 files under `lre/`: the annotation; the `Obstacle` record and `ObstacleRegister`; sealed `InputEvent` (6 records) and `OutputEvent` (2); `Sensor` (raw readings, hdist/vdist/odist, field accessors, closest-index selection); `Actuator` (`apply`, latest advVel/advHdng); five operations (CalcVel, CalcCStc, CalcCDyn, CheckOPEZ, CalcCPA) holding Var1–8; `LreMode`, `LreConstants` (all 1.0) and `LreController`. `step` runs the operations, declares named predicates (including `inOpez`), then runs a two-level if-else with the 18 transitions. Entry actions are advVel(1) into MOM and advVel(0) into HCM; Beh7 and Beh18 advise advVel(0).

**Decisions the requirements left open:**
- "−1 = none" conflicts with nat-typed cstc/cdyn. Obstacle indices start at 1 and 0 means none.
- Safe large distance = 100000.0 m.
- CPA is computed in the horizontal plane with closing velocity (AUV − obstacle) and `+0.001` in the denominator against 0/0. Using hdist(i) for |p| makes cda large and tcpa 0 when no dynamic obstacle exists.
- CheckOPEZ and CalcCPA call `sensor.closestStaticIndex()`/`closestDynamicIndex()` themselves (the profile allows only sensor calls in `compute()`). The value is the same as CalcCStc/CalcCDyn's.
- Priority: reqOCM, then inOpez, collision, endTask, HCM hazards, reqHCM. Later branches conjoin the negations of earlier guard-only branches. Beh11 is placed before Beh10 because, with equal thresholds, it would otherwise never fire.
- vvel ≥ 1 is signed (taken literally). Beh4 uses the literal 1. HCM advises 0 per FR3, not the 0.1 in the description.
- CAM has no specified output; unhandled inputs are ignored; `step(null)` = no input.

**Unresolved.** The evasive manoeuvre (FR4) has no defined output.
