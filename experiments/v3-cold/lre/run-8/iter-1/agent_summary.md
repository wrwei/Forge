# LRE, pass 8, iteration 1

## What was written
- 15 Java files in package `lre`: the annotation, events, sensor (Obstacle, ObstacleRegister, Sensor), actuator, five operations, constants, and the mode enum and controller. One `step()`; the operations run before the guards.
- `result_codegen.json`, covering all 51 requirements.
- `post_codegen.{md,json}`.
- No Java existed beforehand. Compiled with javac (release 17); a scratch smoke test passed.

## Decisions the requirements left open
- **"No obstacle" index.** The requirements give −1, which the nat-only profile forbids. Index 0 means "none", and obstacles are numbered from 1.
- **Safe large distance.** Set to 1000000.0 m.
- **CPA.** tcpa = (r·u)/|u|² and cda = √(|r−u·tcpa|² + vdist²), with vertical separation held constant. Through vdist, OP5's large default reaches cda despite DM5's zero accessors.
- **Index source.** CheckOPEZ and CalcCPA call the same sensor selection function that CalcCStc and CalcCDyn store, because compute() may only use sensor calls and its own fields.
- **Variables.** They live in the operations that compute them.
- **Constants.** VEL_LIMIT, MOM_VELOCITY, HCM_VELOCITY and STOP_VELOCITY were added. Beh4's distance "1" uses MIN_SAFE_DIST.
- **Priority within a mode.** reqOCM, other events, inOpez, collision, then Beh9, Beh11, Beh10, each negating earlier guards. Beh11 precedes Beh10, which would otherwise make it unsatisfiable.
- **HCM velocity.** Advised as 0 per FR3, not the 0.1 in the description.
- **vvel.** The signed rate of climb, as OP1 states.
- **CAM.** Issues nothing on entry.
- **step(null).** Means no operator input this cycle.

## Unresolved
- With zero relative velocity, tcpa is 0/0 (NaN in Java). This blocks CAM entry and allows CAM exit. A verifier may flag the division.
- DC1's uniqueness rule cannot strictly apply to transitions that have no trigger.
