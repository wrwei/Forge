# LRE — pass 4, iteration 1

## What was written

15 Java files under `lre/`:
- `LreController`, with a mode-nested `step()` and 16 named predicates;
- `LreMode`, with OCM first;
- the five operations named in the requirements;
- `Sensor`, `Obstacle` and `ObstacleRegister`;
- `Actuator`, which receives the `OutputEvent` records;
- the sealed `InputEvent`/`OutputEvent` hierarchies;
- `LreConstants` (13 whole-number constants);
- the `@RoboChartType` annotation.

Also written: `result_codegen.json` (141 entries; all 51 requirements, every file) and `post_codegen.{md,json}`. `step()` runs the operations in dependency order before the guards. The state variables are operation fields exposed by getters.

## Unsettled decisions

- **No obstacle:** "−1" conflicts with natural-number indices. Index 0 stands for "no obstacle" and register identifiers start at 1.
- **Safe large distance:** 1000 m.
- **HCM velocity:** 0 m/s, following FR3, not the description's 0.1 m/s.
- **CPA:** uses the closing velocity (AUV minus obstacle) for horizontal motion and a constant vertical separation. cda is a sum of squares. Zero closing velocity gives NaN in Java, which reads as "no collision course".
- **Priority:**
  - In MOM and HCM: reqOCM, then OPEZ, then collision, then other events, then the HCM triggers in the order Beh9, Beh11, Beh10.
  - Lower branches conjoin the negations of earlier guard-only branches.
- **Operation inputs:** CheckOPEZ and CalcCPA read the closest indices from the sensor, as the profile restricts `compute()`.
- **Thresholds:** the literal thresholds in Beh4, Beh9, Beh11 and OP2 have their own constants.
- **DC1:** read as one transition per requirement.

## Unresolved

- CAM's "evasive manoeuvre" has no specified output, so CAM issues no advice.
- If `staticObsDfltVertDist` > `staticObsVertDist`, MOM and HCM could cycle without an event. The required defaults (both 1) exclude this.
- CalcCPA divides by a value that can be zero if a verifier interprets the division.
