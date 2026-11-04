# ITER_SUMMARY: lre, pass 1

No Java was present at the start.

## What I wrote

15 files under `java.generated.project/src/main/java/lre/`:

- `RoboChartType`
- `LreConstants`: the DM4 thresholds, plus a named constant for every other number the requirements fix
- sealed `InputEvent` (six records) and `OutputEvent` (`AdvVel`, `AdvHdng`)
- `Obstacle`, `ObstacleRegister`, `Sensor`
- `Actuator.apply(OutputEvent)`
- the operations `CalcVel`, `CalcCStc`, `CalcCDyn`, `CheckOPEZ` and `CalcCPA`, which hold Var1–8
- `LreMode`, and `LreController`, whose `step` runs the operations, declares named predicates and has a two-level if-else covering Beh1–19

Also `result_codegen.json` (all 51 requirements, all 15 files) and `post_codegen.{md,json}` (12 issues).

## Decisions the requirements did not settle

1. cstc and cdyn are naturals, but the requirements use −1 for "none". The sensor returns an index that is never registered (`Integer.MAX_VALUE`). The controller never tests it.
2. The distance reported for an absent obstacle is 100000.0 m.
3. CPA: the relative velocity is the obstacle's minus the AUV's, and tcpa = −p·v/(|v|²+ε). cda includes the vertical separation `vdist(cdyn)`, so DM5's zero accessors do not give cda = 0, and a spurious CAM, when there is no dynamic obstacle.
4. ε = 0.000001, which rounds up to 1 in the model's constants. The sign of tcpa is unaffected.
5. CheckOPEZ and CalcCPA call the sensor's closest-index functions directly (same value as cstc/cdyn; inside the profile).
6. HCM advises 0 m/s (FR3), not 0.1 m/s (system description).
7. Priority within a mode: reqOCM, then inOpez, then collision, then the other events, then Beh9, Beh11, Beh10. Beh10 comes last because otherwise Beh11 could never fire.
8. Beh4's "1" values are their own constants. Initial readings and advice are 0.0. vvel is signed. `step(null)` means no input this cycle.

## Unresolved

CAM's evasive manoeuvre (FR4) is not specified, so nothing is output on entering CAM.
