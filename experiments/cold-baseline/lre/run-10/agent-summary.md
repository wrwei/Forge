# LRE Cold Baseline run-10 — Agent Summary

Single-shot implementation generated from `requirement_all.json` and the
case-study system description; no prior runs consulted.

## Design decisions

- **Package layout**: `lre.{annotation, mode, constants, sensor, event, actuator, operation, controller}`.
- **Modes**: enum `LreMode {OCM, MOM, HCM, CAM}` with initial `OCM`.
- **Sensor**: stores `depth`, `ns_vel`, `ew_vel`, `rate_of_climb` and an `ObstacleRegister`. Distance functions return `Double.MAX_VALUE` and field accessors return `0.0` when index is `-1` or absent, per LRE-DM5.
- **ObstacleRegister**: immutable copy-on-write `Map<Integer,Obstacle>` with `staticIndices()` / `dynamicIndices()` filtering. `Obstacle.isStatic()` checks both horizontal velocities are zero.
- **Operations**: five operation classes (`CalcVel`, `CalcCStc`, `CalcCDyn`, `CheckOPEZ`, `CalcCPA`), each with a single `compute()` containing only direct `this.field = expression` assignments. `CheckOPEZ` reads `cstc` from `CalcCStc`; `CalcCPA` reads `cdyn` from `CalcCDyn`. Order of invocation: `CalcVel`, `CalcCStc`, `CalcCDyn`, `CheckOPEZ`, `CalcCPA`.
- **Controller**: single-method `step(InputEvent)` with mode-nested if-else. All guard conditions are extracted as named boolean predicates declared before the if-else chain — no ternary, no sentinel checks (Sensor returns safe defaults).
- **Bare-precondition transitions**: every mode has at least one event-triggered branch with no extra guard (OCM: `reqVel`/`reqHdng`; MOM/HCM/CAM: `reqOCM`) — required for Isabelle `deadlock_free` proof.
- **Entry actions**: `advVel(1.0)` on entry to MOM; `advVel(0.0)` on entry to HCM and on `endTask`/`CAM safe` exits.
- **@RoboChartType**: `nat` for index ints, `real` for double fields/parameters.
