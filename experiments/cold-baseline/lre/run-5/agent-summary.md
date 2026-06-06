# LRE Cold Baseline — Run 5 — Agent Summary

## Design Decisions

- **Package layout.** `lre.{annotation, mode, constants, sensor, event, actuator, operation, controller}` per the project convention.
- **Modes.** `LreMode {OCM, MOM, HCM, CAM}` (LRE-DM1). Controller field `currentMode` initialised to `OCM`.
- **Data types.** `Obstacle` as a Java `record` with six real fields plus `isStatic()`/`isDynamic()` helpers (zero-velocity test). `ObstacleRegister` is an immutable copy-on-write `Map<Integer, Obstacle>` exposing `staticIds()` / `dynamicIds()`.
- **Sensor.** Single class storing raw scalars + a `ObstacleRegister`. All `hdist/vdist/odist/*RelDist/obs*Vel` methods return safe defaults (`Double.MAX_VALUE` for distances, `0.0` for field accessors) when called with `-1`, per LRE-DM5. `closestStaticIndex` / `closestDynamicIndex` iterate the register and return `-1` if empty.
- **Operations.** Five classes (`CalcVel`, `CalcCStc`, `CalcCDyn`, `CheckOPEZ`, `CalcCPA`), each with a single `compute()` of direct `this.field = expression` assignments and no local variables. `CheckOPEZ` reads `cstc` from `CalcCStc` (explicit dependency). `CalcCPA` inlines the standard CPA formula in two field assignments (tcpa first, then cda).
- **Controller.** Pure two-level if-else with named-boolean predicates declared before the chain; each mode's first event-triggered branch is bare-precondition (no extra guard) for Isabelle `deadlock_free`. High-priority `inOpez` and CPA-collision branches duplicated in MOM and HCM. Entry actions (`advVel(1.0)` / `advVel(0.0)`) emitted inline at the transition target.
