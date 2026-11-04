# LRE run 3 — iteration 1

**What I wrote.** Package `lre` (15 files): sealed `InputEvent`/`OutputEvent`; `LreMode` (OCM first); `LreConstants`; the `Obstacle` record, `ObstacleRegister` and `Sensor` (SF1–3 distance functions, field accessors, closest-index selection); an `Actuator.apply(OutputEvent)`; the five operations CalcVel, CalcCStc, CalcCDyn, CheckOPEZ and CalcCPA; and `LreController.step`. Each step runs the five operations and then a two-level if-else over the named predicates. Each Beh requirement has exactly one branch. The trace covers all 51 requirements.

**Decisions the requirements left open:**
- **"−1 = none":** int maps to nat, so the no-obstacle index is a sentinel that the register refuses (`Integer.MAX_VALUE`). Sensor functions return their safe default for it (large distance 1e6 m, zero fields).
- **Index source:** an operation may read only the sensor and its own fields. CheckOPEZ and CalcCPA therefore call the sensor's selection functions directly, which give the same value as cstc/cdyn within the step.
- **CPA:** closing velocity is AUV minus obstacle; tcpa = p·c/|c|²; cda = √(|p−c·tcpa|² + vdist²). The vdist term stops the zero-distance defaults from creating a false collision when there is no obstacle.
- **HCM advice:** 0 m/s (FR3), not 0.1 m/s (system description).
- **Literal 1 thresholds** (Beh4/9/11): separate constants, not minSafeDist.
- **Priority within a mode:** reqOCM, then inOpez (so Beh5/16's guard is inOpez alone), then collision, then endTask/reqHCM, then Beh9, Beh11, Beh10. Later branches conjoin the negations of earlier guard-only branches. Beh11 comes before Beh10 so that it stays reachable.
- **vvel:** the signed rate of climb.
- **Unhandled inputs and CAM:** inputs a mode doesn't handle are ignored; CAM issues no evasive command (none specified).

**Unresolved:** none so far. Details are in post_codegen.md.
