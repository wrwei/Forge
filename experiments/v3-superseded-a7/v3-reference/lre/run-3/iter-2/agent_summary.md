# LRE run 3 — iteration 2

**What I wrote.** Package `lre` (15 files): sealed `InputEvent`/`OutputEvent`; `LreMode` (OCM first); `LreConstants`; the `Obstacle` record, `ObstacleRegister` and `Sensor` (SF1–3 distance functions, field accessors, closest-index selection); an `Actuator`; the five operations; and `LreController.step`, which runs the operations and then a two-level if-else over named predicates, with one branch per Beh requirement. The trace covers all 51 requirements.

**Iteration 2 fixes (from iteration 1):**
- Preflight error: annotated `Sensor.SAFE_LARGE_DIST` with `@RoboChartType("real")`.
- Dafny `unresolved identifier: MAX_VALUE`: `NO_INDEX` is now the literal 2147483647 (`nat`).
- Preflight rule8/rule6 warnings: in MOM, HCM and CAM every triggerless branch now comes first. Operator-event branches follow, conjoining the negation of every triggerless guard. RoboChart has no event-absence guard.

**Decisions the requirements left open:**
- **"−1 = none":** a non-negative sentinel, because int maps to nat. Sensor functions return defaults for it (1e6 m distance, zero fields).
- **Index source:** CheckOPEZ and CalcCPA take the closest index from the sensor, since an operation reads only the sensor and its own fields. The value equals cstc/cdyn within the step.
- **CPA:** tcpa = p·c/|c|²; cda = √(|p−c·tcpa|² + vdist²), with closing velocity c = AUV − obstacle. The vdist default prevents a false collision when there is no obstacle.
- **HCM advice:** 0 m/s (FR3), not 0.1 m/s (system description).
- **Literal 1 thresholds** (Beh4/9/11): own constants.
- **Priority:** autonomous transitions take precedence over operator requests in the same cycle. Beh11 comes before Beh10 so it stays reachable.
- **vvel:** the signed rate of climb.
- **Unhandled inputs and CAM:** inputs a mode doesn't handle are ignored; CAM issues no evasive command.

**Unresolved:** none. Details are in post_codegen.md.
