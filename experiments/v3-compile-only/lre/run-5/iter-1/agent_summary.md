# LRE run 5: iteration 1

**What I wrote.** 15 files under `lre/`:
- `annotation`: `RoboChartType`
- `event`: sealed `InputEvent` (6 records) and `OutputEvent` (`AdvVel`, `AdvHdng`)
- `sensor`: `Obstacle` record, `ObstacleRegister` (a TreeMap), `Sensor` (distance functions, field accessors, closest-index selection)
- `actuator`: `Actuator.apply(OutputEvent)`
- `operation`: five classes made only of assignments (`CalcVel`, `CalcCStc`, `CalcCDyn`, `CheckOPEZ`, `CalcCPA`)
- `controller`: `LreMode`, `LreConstants`, `LreController`

`step()` runs the five operations, declares the named predicates, then runs the two-level if-else. Operator events come first in each mode (reqOCM first); the autonomous guards come after, each with the negations of the guards above it. Variables Var1–8 are fields of the operations that compute them, so no model name is duplicated. All 51 requirements are traced.

**Decisions the requirements left open:**
- "No obstacle" is index 0, not -1 (the indices are nat); register identifiers start at 1.
- The safe large distance is 1000000.0 m.
- CPA is computed in the horizontal plane: tcpa = r·c/(|c|²+0.000001), cda = sqrt(hdist² − tcpa²|c|²). With no dynamic obstacle this gives cda = the large distance.
- CheckOPEZ and CalcCPA call the sensor's closest-index selection themselves, because compute() may only use sensor calls.
- MOM's HCM branches are ordered Beh9, Beh11, Beh10. Putting Beh10 first would make Beh11 unsatisfiable.
- HCM advises 0 m/s, as LRE-FR3 says; the system description says 0.1.
- The Beh4 thresholds are the literal 1.
- vvel is the signed rate of climb.
- Entering CAM issues no output (the evasive manoeuvre is not specified).
- A cycle with no operator input calls `step(null)`.

**Unresolved.** Nothing so far. Details are in `post_codegen.md`.
