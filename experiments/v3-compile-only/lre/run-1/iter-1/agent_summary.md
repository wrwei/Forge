# ITER_SUMMARY — lre, run 1, iteration 1

## What I wrote
Java under `lre/` (no Java existed beforehand). Packages:
- `annotation`: `RoboChartType`
- `LreConstants`: the DM4 thresholds plus named constants for every other fixed value, so guards contain no real literals
- `domain`: `Obstacle`, `ObstacleRegister`
- `sensor`: `Sensor`
- `event`: sealed `InputEvent`, `OutputEvent`
- `actuator`: `Actuator`
- `operation`: the five operations
- `controller`: `LreMode`, `LreController`

`step` runs the operations, declares named predicates, then a two-level if-else. All 51 requirements and all 15 files are traced. The Layer 5 review is in `post_codegen.{md,json}`.

## Decisions the requirements did not settle
1. **"-1 = none" versus nat indices:** `ObstacleRegister.NO_OBSTACLE = Integer.MAX_VALUE`, never stored.
2. **CPA:** w = AUV velocity minus obstacle velocity.
   - tcpa = r·w/(|w|²+cpaEpsilon)
   - cda = sqrt(|r−w·tcpa|² + vdist²)
   - vdist settles the DM5/OP5 clash about no-obstacle defaults: with no dynamic obstacle, cda is the large default.
   - cpaEpsilon = 0.000001 is invented; the model rounds it to 1.
3. **Invented defaults:** safe distance 1000 m; readings and advice start at 0.
4. **Priority:** operator events first. Then inOpez→OCM, collision→CAM, and HCM triggers in the order Beh9, Beh11, Beh10 (keeps Beh11 satisfiable). Earlier autonomous guards are negated.
5. **HCM speed:** 0 m/s (FR3), not 0.1 m/s (system description).
6. **CAM manoeuvre:** unspecified; entering CAM emits nothing.
7. **vvel:** the signed rate of climb, taken literally.
8. **Shared indices:** CheckOPEZ and CalcCPA call the sensor's closest-index functions directly, since compute() may use only sensor calls.
9. **Inputs outside OCM:** no extra transitions, so reqVel and reqHdng are refused.

## Unresolved
- The CAM manoeuvre.
- Whether vvel should be a magnitude.
- MOM/HCM avoid alternating without events only while staticObsDfltVertDist ≤ staticObsVertDist.
