# ITER_SUMMARY: lre, pass 3, iteration 1

**What I wrote.** Fifteen Java files in package `lre`:
- the sealed events `InputEvent` (six records) and `OutputEvent` (`AdvVel`, `AdvHdng`);
- `Obstacle` and `ObstacleRegister`;
- `Sensor` (the DM5/SF functions) and `Actuator`;
- the five operations;
- `LreConstants`, `LreMode`, and `LreController`, whose mode-nested `step` holds named predicates.

Also written: `result_codegen.json` (all 51 requirements and all 15 files traced) and `post_codegen.{md,json}`. No Java existed beforehand. The project compiles, and a scratch smoke test of 23 scenarios passed.

**Decisions the requirements left open:**
- "-1 means none" versus `nat`: index 0 is reserved as `NO_OBSTACLE`, and the register refuses it. Absent indices give a distance of 100000.0 m and field values of 0.
- HCM advice: 0 m/s (FR3), not the description's 0.1 m/s.
- CPA: uses the closing velocity (AUV minus obstacle) in the horizontal plane plus the current vertical separation. The `tcpa` denominator has +0.000001 added.
- `CheckOPEZ` and `CalcCPA` call the sensor's closest-index functions themselves, because the operation profile allows only sensor calls. Within a step this gives the same index `CalcCStc`/`CalcCDyn` compute.
- Priority in MOM: reqOCM, endTask, inOpez, CAM, then the static-caution triggers (Beh9, Beh11, Beh10), then reqHCM. In HCM: reqOCM, inOpez, CAM, then MOM. Each branch below a predicate-only guard conjoins that guard's negation.
- `vvel` is the signed rate of climb. Thresholds of 1 are literals. Distance ties go to the lowest index. The actuator starts at 0.0. `step(null)` means no input.
- Variables live in their operation classes. `Obstacle` keeps DM2's snake_case names, which avoids clashing with sensor accessor names.

**Unresolved.** The requirements don't specify CAM's evasive manoeuvre, so CAM has no entry action. With 0 m/s advised in HCM, the autonomous exit (Beh13) may never become true without operator action.
