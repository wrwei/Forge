# ITER_SUMMARY: lre, pass 6, iteration 1

## What was written
15 Java files under `lre/`: annotation; event (sealed `InputEvent` with 6 records, `OutputEvent` with 2); sensor (`Obstacle`, `ObstacleRegister`, `Sensor`); actuator; operation (CalcVel, CalcCStc, CalcCDyn, CheckOPEZ, CalcCPA); controller (`LreMode`, `LreConstants`, `LreController`). `step` runs the five operations, declares named predicates (including `inOpez`, GP1) and holds 17 transitions, one per Beh2–18. Each variable (Var1–8) lives in its operation and is read through a getter of the same name. `result_codegen.json` maps all 51 requirements and 15 files. The code compiles under javac `-Xlint:all`, and a jshell walk-through of every transition behaved as specified.

## Decisions the requirements did not settle
- **"None" index:** cstc/cdyn are nat, not -1. Index 0 means "no obstacle"; registered indices start at 1.
- **Safe large distance:** 1000000.0 m.
- **cda/tcpa:** the AUV's velocity minus the obstacle's (obstacle velocities are absolute: static means zero velocity). cda adds the vertical separation, so with no dynamic obstacle it is large, not 0. `CPA_EPSILON` = 0.000001 keeps tcpa defined; the model rounds it to 1.
- **HCM advice:** 0 m/s, per FR3 (the description says 0.1).
- **CAM entry:** no action (no evasive output specified).
- **Priority:** operator events, then inOpez, CAM, the HCM guards. Beh11 precedes Beh10 so it stays reachable; later branches conjoin the negations of earlier autonomous guards.
- **No operator input:** `step(null)`.
- **Literal 1 thresholds:** Beh4's literal 1s and the 1 m/s limits are separate constants.

## Unresolved (in post_codegen)
- **CAM exit:** Beh18 ignores tcpa, so after a close pass CAM can persist until reqOCM.
- **MOM/HCM oscillation:** possible without an event if staticObsDfltVertDist exceeded staticObsVertDist.
- **Descent:** vvel is the signed rate of climb, so a fast descent does not trigger Beh11.
