# LRE — cold pass 3, iteration 1

**Written.** 15 Java files under `src/main/java/lre`:
- `LreController.step`: four modes and 18 transitions, with named predicates.
- `Sensor` with an `ObstacleRegister` and the `Obstacle` record.
- `Actuator.apply(OutputEvent)`.
- Five operations: `CalcVel`, `CalcCStc`, `CalcCDyn`, `CheckOPEZ`, `CalcCPA`.
- Sealed `InputEvent`/`OutputEvent`, the `LreMode` enum, `LreConstants` and the `RoboChartType` annotation.

Also written: `result_codegen.json` (all 51 requirements; every public element traced) and `post_codegen.{md,json}` (13 issues). No Java existed beforehand. The code compiles under Java 17 with no lint warnings, and a scratch run of the main transitions behaved as specified.

**Decisions the requirements did not settle:**
- **No obstacle.** The requirements use −1, but ints are nat. I reserved index 0 (`NO_OBSTACLE`); identifiers start at 1.
- **Missing obstacle.** Distances return 1000000.0 m and field accessors return 0.
- **CPA.** Horizontal extrapolation at the closing velocity (AUV minus obstacle), keeping the current vertical separation. A divide-by-zero guard of 0.000001 rounds to 1 in the model; tcpa's sign is unaffected.
- **Operation inputs.** CheckOPEZ and CalcCPA call the Sensor's closest-index functions directly, because the profile forbids reading another operation.
- **Priorities.** reqOCM, then OPEZ, then collision, then the rest. Beh11 comes before Beh10, since otherwise Beh10's guard subsumes it and Beh11 is dead.
- **Literal values.** Named as constants (MOM_SPEED, HALT_SPEED, APPROACH_SPEED, MOM_ENTRY_CLEARANCE).
- **HCM speed.** 0 m/s per FR3; the system description says 0.1 m/s.
- **vvel.** The signed rate of climb, as OP1 states.
- **DC1.** Read as applying to event triggers. Several trigger-less transitions per mode are kept, one per requirement.

**Unresolved.** CAM's "evasive manoeuvre" (FR4) has no specified output, so CAM issues no advice. The Sensor's initial depth of 0 means inOpez holds until the first reading.
