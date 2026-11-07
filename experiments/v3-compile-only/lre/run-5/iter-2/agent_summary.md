# LRE run 5: iteration 2

**What I wrote.** 15 files under `lre/`:
- `annotation`: `RoboChartType`
- `event`: sealed `InputEvent` (6 records) and `OutputEvent` (`AdvVel`, `AdvHdng`)
- `sensor`: `Obstacle`, `ObstacleRegister`, `Sensor`
- `actuator`: `Actuator.apply(OutputEvent)`
- `operation`: five classes made only of assignments
- `controller`: `LreMode`, `LreConstants`, `LreController`

`step()` runs the operations, then the named predicates, then the two-level if-else. Variables Var1–8 are fields of the operations that compute them. All 51 requirements are traced.

**Iteration 2 changes (preflight).**
- Error fixed: the annotation on `Sensor.NO_OBSTACLE_DIST` was missing.
- rule8 warnings: in MOM, HCM and CAM the branches with no event now come first. Each operator-event branch is conjoined with the negation of every guard above it. RoboChart has no guard for an absent event, so this is the only way the model and the Java can agree. As a consequence, autonomous safety transitions now take precedence over reqOCM, endTask and reqHCM.
- rule6 warning: every guard now fits on one line.

**Decisions the requirements left open:**
- "No obstacle" is index 0, not -1 (nat); register identifiers start at 1.
- The safe large distance is 1000000.0 m.
- CPA is computed in the horizontal plane, with an epsilon in the denominator.
- CheckOPEZ and CalcCPA call the sensor's closest-index selection themselves.
- MOM's HCM branches are ordered Beh9, Beh11, Beh10, which keeps Beh11 satisfiable.
- HCM advises 0 m/s (LRE-FR3); the system description says 0.1.
- The Beh4 thresholds are the literal 1.
- vvel is the signed rate of climb.
- Entering CAM issues no output.
- A cycle with no operator input calls `step(null)`.

**Unresolved.** Nothing. Details are in `post_codegen.md`.
