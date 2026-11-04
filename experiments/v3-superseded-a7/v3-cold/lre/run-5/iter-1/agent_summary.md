# ITER_SUMMARY: LRE, pass 5, iteration 1

**Written.** 15 Java files under `java.generated.project/src/main/java/lre/`: annotation, events, `LreMode`, `LreConstants`, `Obstacle`/`ObstacleRegister`/`Sensor`, `Actuator`, five operations, `LreController`. Also `result_codegen.json` (138 entries, all 51 requirements, every public element) and `post_codegen.{md,json}`. No Java existed beforehand. Compiles with javac and Gradle; a scratch harness outside the project exercised all 18 transitions.

**Structure.** `step()` runs the five operations, declares named predicates (including `inOpez`, GP1), then one block per mode. Var1–8 are operation fields with same-named getters, not duplicated on the controller.

**Decisions the requirements did not settle.**
- "No obstacle" = -1 conflicts with natural-number integers: used a reserved index, 2147483647 (`ObstacleRegister.NO_OBSTACLE`).
- Safe large distance: 1000000.0 m.
- CPA: horizontal plane, closing velocity, |p| from `hdist` (no obstacle gives a large cda); `CPA_EPSILON = 0.000001` prevents division by zero (rounds to 1 in the model).
- CheckOPEZ/CalcCPA call the same sensor index functions as CalcCStc/CalcCDyn, since operations may use only sensor calls.
- HCM advises 0 m/s (FR3), not the description's 0.1 m/s.
- vvel is the signed rate of climb, so Beh11 fires only when climbing.
- Priority: reqOCM, inOpez, collision course, then the rest; lower branches negate higher guard-only guards. Beh11 precedes Beh10, otherwise equal thresholds make it a dead transition.
- Ignored inputs: reqVel/reqHdng outside OCM; reqOCM/reqHCM/endTask in OCM.
- Numeric values from transition requirements are named constants; the actuator starts at 0.0.

**Unresolved.** CAM's evasive manoeuvre is unspecified, so nothing is advised in CAM. Beh13 ignores staticObsDfltVertDist (MOM/HCM could oscillate if thresholds differ). Beh18 ignores tcpa, so CAM persists after a close pass until reqOCM.
