# ITER_SUMMARY — lre, pass 7, iteration 1

No Java was present beforehand. I wrote 15 files under `lre/`:

- `annotation`: RoboChartType.
- `event`: sealed InputEvent (6 records) and OutputEvent (AdvVel, AdvHdng).
- `sensor`: Obstacle, ObstacleRegister, Sensor.
- `actuator`: Actuator, which takes events through `apply(OutputEvent)`.
- `operation`: CalcVel, CalcCStc, CheckOPEZ, CalcCDyn, CalcCPA.
- `controller`: LreMode, LreConstants, LreController.

`step()` calls the five operations, evaluates named predicates, then runs a two-level if-else over the four modes. Each of Beh2–Beh18 is one branch. The variables live in the operations and are read through getters of the same name. I also wrote `result_codegen.json` (130 entries, all 51 requirements) and `post_codegen.{md,json}`.

Decisions the requirements left open:

- **"None" index:** cstc/cdyn are natural numbers, so instead of -1 they use a reserved index (2147483647) that the register refuses to store.
- **Safe large distance:** 1,000,000 m (invented).
- **HCM velocity:** 0 m/s per FR3, not the description's 0.1 m/s.
- **CAM manoeuvre:** no output, because none is specified.
- **Priority:** reqOCM, then collision course, then inOpez, then other events, then autonomous HCM entries. Lower branches negate earlier guard-only ones. Beh11 precedes Beh10 so it can still fire.
- **CPA:** horizontal relative motion, constant vertical separation, and a 1e-9 term keeping the divisor non-zero. cda is taken at max(tcpa, 0), so a receding obstacle cannot hold the LRE in CAM.
- **vvel:** the signed rate of climb.
- **Beh4 thresholds:** the literal 1.
- **No input:** `step(null)` is a cycle without operator input.
- **Ties:** the lowest index wins. Static means velocities exactly 0.

Unresolved:

- If staticObsDfltVertDist exceeded staticObsVertDist, MOM and HCM could alternate without bound.
- DC1's "no duplicates" conflicts with Beh9–11, three event-less MOM→HCM transitions. I kept all three.
- I could not confirm how the extractor treats the `compute()` calls before the predicates.
