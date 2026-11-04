# LRE pass 6 — iteration 1 summary

**Written.** 15 Java files under `lre`:
- the `RoboChartType` annotation, `LreConstants` (four thresholds, each 1.0), and sealed `InputEvent` (6 records) and `OutputEvent` (2);
- `Obstacle`, `ObstacleRegister` and `Sensor` (distance functions, field accessors, closest-index selection);
- `Actuator.apply`;
- five operations (CalcVel, CalcCStc, CheckOPEZ, CalcCDyn, CalcCPA), whose fields hold Var1–Var8;
- `LreMode` and `LreController.step`. `step` runs the five `compute()` calls, then 15 named predicates, then one block per mode with 18 transitions.

Also written: `result_codegen.json` (all 51 requirements) and `post_codegen.{md,json}`. Compiles cleanly; a scratch harness outside the project checked transitions and CPA arithmetic. No Java existed beforehand.

**Decisions the requirements did not settle**
- "No obstacle" is index 0, not −1, because nat cannot be negative. Obstacles are stored from index 1.
- The safe large distance is 1000000.0 m. Sensor readings and actuator outputs start at 0.0.
- DM5 (accessors return zero) contradicts OP5 ("large-distance defaults"). Taken literally, a zero position would give a phantom CAM. Resolved by including vdist (large by default) in cda.
- CPA: horizontal relative motion with constant vertical separation. Obstacle velocities are read as world-frame. A 0.000001 term in the denominator avoids division by zero.
- CheckOPEZ and CalcCPA call the sensor's closest-index functions directly, because the profile does not let `compute()` read another operation.
- Priority: operator inputs first, then inOpez, collision, hvel, vvel, default-vertical. Beh11 precedes Beh10, whose guard contains Beh11's.
- The HCM entry speed is 0 m/s (FR3), not the description's 0.1 m/s.
- Beh4's "> 1" and the 1 m/s thresholds are literals.
- vvel is the signed rate of climb.

**Unresolved.** CAM issues no evasive output: FR4 names none. DC1 is read as "one transition per Beh requirement", although MOM has five transitions with no trigger.
