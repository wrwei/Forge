# LRE run 4 — iteration 1

No Java was present at the start, so nothing was deleted.

## What was written

15 files under `lre/`: `LreController` (mode-nested `step`), `LreMode`, `LreConstants`, `Sensor` (with `Obstacle` and `ObstacleRegister`), `Actuator`, the operations `CalcVel`, `CalcCStc`, `CheckOPEZ`, `CalcCDyn` and `CalcCPA`, the sealed events `InputEvent` and `OutputEvent`, and `RoboChartType`. The requirement variables are fields of the operations that compute them. `step` runs the five operations, then evaluates named predicates, then takes at most one transition. The trace covers all 51 requirements and all 15 files.

## Decisions the requirements left open

- **No-obstacle index**: 0, not -1, because the model's integers are nat. The register rejects identifier 0.
- **Safe large distance**: 1000000 m.
- **CPA**: horizontal, using velocity relative to the AUV. `CPA_EPSILON` (1e-9) keeps the division defined. A term `hdist(cdyn) - |r|`, which is exactly 0 when the obstacle exists, makes `cda` large when there is no dynamic obstacle. With DM5's zero defaults alone there would be a phantom collision.
- `CheckOPEZ` and `CalcCPA` read `closestStaticIndex()` / `closestDynamicIndex()` from the sensor, because the profile does not let one operation read another.
- **Priority**: `reqOCM` comes first, then `inOpez`, then `endTask`, collision, Beh9, Beh11, Beh10, `reqHCM`. Beh11 comes before Beh10 because the reverse order would make Beh11 unsatisfiable when the two vertical thresholds are equal. Lower branches conjoin the negations of earlier guard-only branches.
- **Inline values**: the 1s and 0s fixed inline by the requirements are named constants.
- **HCM velocity**: 0 m/s (FR3), not the description's 0.1 m/s.
- `vvel` is the signed rate of climb.
- `step(null)` means no operator input.

## Unresolved

- CAM's evasive manoeuvre is unspecified, so CAM issues no advice.

Details are in `post_codegen.md`.
