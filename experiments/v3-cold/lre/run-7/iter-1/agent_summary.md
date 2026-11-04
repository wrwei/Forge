# LRE cold pass 7: iteration 1 summary

No Java was present beforehand, so nothing was deleted.

## What was written

Fifteen files in `lre`:
- `annotation`: `RoboChartType`
- `event`: sealed `InputEvent` (6 records), `OutputEvent` (2 records)
- `sensor`: `Sensor`, `Obstacle`, `ObstacleRegister`
- `actuator`: `Actuator.apply(OutputEvent)`
- `operation`: CalcVel, CalcCStc, CalcCDyn, CheckOPEZ, CalcCPA
- `controller`: `LreMode`, `LreConstants`, `LreController`

`step()` runs the operations, declares named predicates (including `inOpez`), then a four-mode if-else. Also written: `result_codegen.json` (51/51 requirements) and `post_codegen.{md,json}`. The code compiles with javac and Gradle, and a scratch smoke test passed.

## Decisions the requirements did not settle

- **"No obstacle" index:** the spec says -1, but `nat` can't be negative. Index 0 is used instead; obstacle IDs start at 1.
- **CalcCPA:**
  - It uses the AUV's velocity relative to the obstacle.
  - `cda` includes `vdist`, so with no dynamic obstacle it takes the sensor's large-distance default. Zero field accessors would otherwise trigger CAM falsely.
  - `cda` is taken at max(tcpa, 0), so CAM can exit once the obstacle has passed.
  - A 0.000001 offset prevents division by zero.
- **Index source:** CheckOPEZ and CalcCPA query the sensor's selection functions, because `compute()` may not read another operation.
- **Invented values:** safe distance 100000.0 m; actuator starts at 0.
- **MOM priority:** events first, then inOpez, collision, Beh9, Beh11, Beh10. Beh11 sits above Beh10, which would otherwise make it unreachable. Lower autonomous guards negate the ones above.
- **Kept as written:**
  - Beh4 uses the literal 1, not minSafeDist.
  - `vvel` is the signed rate of climb.
  - HCM entry is advVel(0) (FR3), not 0.1 m/s.
  - Unlisted events are ignored.

## Unresolved

- No evasive output is specified for CAM.
- If staticObsDfltVertDist exceeded staticObsVertDist, MOM and HCM could alternate every step.
