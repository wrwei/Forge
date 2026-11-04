# ITER_SUMMARY: lre, pass 10

**Written.** No Java was present at the start; 15 files under `java.generated.project/src/main/java/lre/`:
- sealed `InputEvent` (six records) and `OutputEvent` (`AdvVel`, `AdvHdng`);
- `LreMode` (OCM first) and `LreConstants`;
- `Obstacle`, `ObstacleRegister`, `Sensor` and `Actuator`;
- five operation classes;
- `LreController.step`: operations, named predicates, one block per mode, one branch per LRE-Beh.

It compiles (javac, Gradle); the trace covers all 51 requirements and all 15 files.

**Decisions the requirements did not settle.**
- No-obstacle index: `NO_OBSTACLE = Integer.MAX_VALUE` instead of -1, because integers become nat.
- The safe large distance is 1,000,000 m.
- CPA:
  - relative velocity = AUV velocity minus obstacle velocity (DM2 makes obstacle velocities absolute);
  - tcpa = p.r / (|r|^2 + eps);
  - cda = sqrt(|p - r*tcpa|^2 + vdist^2), so cda stays large when there is no dynamic obstacle (DM5's zero-position defaults would otherwise force CAM);
  - eps = 1e-9 (rounded to 1 in the model) avoids division by zero.
- CheckOPEZ and CalcCPA call the Sensor index functions directly.
- Var1-Var8 live in the operation classes.
- Priority: reqOCM, inOpez, collision course, then endTask, reqHCM, Beh9, Beh11, Beh10. Beh11 comes before Beh10 so that it is not dead code.
- HCM advises 0 m/s (FR3); the description says 0.1.
- The literal 1s in Beh4 are their own constants.
- A cycle with no operator input calls `step(null)`.

**Unresolved.**
- CAM's evasive manoeuvre is not specified, so CAM sends no output.
- CAM exits on cda alone, so a near-miss that has already passed keeps CAM active.
- vvel is signed, so Beh11 ignores fast descent.
- MOM and HCM would alternate if staticObsDfltVertDist > staticObsVertDist.
- I read DC1 as "one branch per requirement".

Details are in `post_codegen.md`.
