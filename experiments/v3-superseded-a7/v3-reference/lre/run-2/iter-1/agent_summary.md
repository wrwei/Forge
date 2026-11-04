# LRE run 2 — iteration 1

## What was written

There are 15 files under `lre/`:
- `RoboChartType`
- `LreConstants`: the DM4 thresholds and `CPA_EPSILON`
- sealed `InputEvent` and `OutputEvent`
- `Actuator.apply(OutputEvent)`
- `Obstacle`, `ObstacleRegister` and `Sensor`
- five operations, each holding its variables behind getters
- `LreMode` and `LreController`

`step()` runs the five `compute()` calls, declares the guard predicates and then works through the four mode blocks. The trace covers all 51 requirements.

## Decisions the requirements left open

- **"−1 = none" against nat.** Index 0 means "no obstacle", and identifiers start at 1.
- **Large distance for absent obstacles.** Invented as 100000 m.
- **CPA.** Closing velocity is the AUV's minus the obstacle's (horizontal). tcpa = p·w/(|w|²+ε). cda is evaluated at max(tcpa,0) and adds the vertical separation `vdist`. The DM5 accessors return 0 for an absent obstacle, which would otherwise give cda = 0 and a false CAM. The model rounds ε up to 1, but the sign of tcpa is unchanged.
- **Priorities.** The order is `reqOCM`, inOpez, collision, `endTask`/`reqHCM`, then Beh9, Beh11, Beh10. Beh11 precedes Beh10 because the two vertical thresholds are equal, so the other order would make Beh11 dead. Later branches conjoin the negations of earlier guard-only branches.
- **HCM velocity.** The description says 0.1 m/s; FR3 says 0. I used 0.
- **vvel.** It is the signed rate of climb (OP1).
- **CAM.** It has no entry action, because no evasive advice is specified.
- **Operation inputs.** CheckOPEZ and CalcCPA re-read the closest index from the sensor.
- **No input.** `step(null)` is a cycle without input.

All of these are listed in `post_codegen.md`.

## Unresolved

- Nothing is unimplemented.
- The sign convention of `ns_rel_dist`/`ew_rel_dist` (obstacle minus AUV) is assumed.
