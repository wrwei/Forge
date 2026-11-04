# ITER_SUMMARY: lre, pass 9

**Written.** 15 Java files in `lre`: `LreController` (modes OCM/MOM/HCM/CAM; `step` runs the five operations, declares named predicates, then a two-level if-else), `LreMode`, `LreConstants`, sealed `InputEvent`/`OutputEvent`, `Sensor` + `ObstacleRegister` + `Obstacle`, `Actuator.apply(OutputEvent)`, operations `CalcVel`, `CalcCStc`, `CalcCDyn`, `CheckOPEZ`, `CalcCPA`. Plus `result_codegen.json` (51/51 requirements) and `post_codegen.{md,json}`. No Java existed beforehand. Compiles under Java 17; a scratch smoke test of the Beh scenarios passed.

**Decisions the requirements left open:**
- "No obstacle" index is 0, not -1 (ints are nat); obstacle ids start at 1.
- DM5 (zero relative distance) contradicts OP5 (large distance): followed OP5. Zeros would force CAM with no dynamic obstacle. Large distance = 1000000.0 m.
- CPA: horizontal; obstacle velocity used as the relative velocity, so OP5's defaults yield a large cda. The denominator has `CPA_EPSILON` = 0.000001 (the model rounds it up to 1). tcpa is not clamped.
- CheckOPEZ/CalcCPA call `sensor.closest*Index()` themselves (profile limits compute() to sensor calls); same index as CalcCStc/CalcCDyn.
- HCM advice 0 m/s (FR3) over the description's 0.1 m/s.
- Literal thresholds of 1 are `VEL_THRESHOLD` and `ENTRY_CLEARANCE_DIST`, not minSafeDist.
- Order (equal priorities). MOM: reqOCM, endTask, inOpez, CAM, reqHCM, Beh9, Beh11, Beh10. HCM: reqOCM, inOpez, CAM, Beh13. Lower branches conjoin negated guard-only predicates. Beh11 precedes Beh10, or Beh11 would never be enabled.
- `step(null)` = no operator input; actuator starts at 0; distance ties go to the lowest index.

**Unresolved:**
- CAM's evasive manoeuvre (FR4) is unspecified; entering CAM issues nothing.
- CAM exits only on cda ≥ minSafeDist. A passed obstacle that came within that distance keeps CAM until reqOCM.
- vvel is the signed rate of climb, so Beh11 ignores fast descent.
- Beh13 undoes an operator reqHCM at once when no static obstacle is near.
