# LRE Cold-Baseline Run 3 — Agent Summary

## Layout
Package root `lre.*` with sub-packages `annotation`, `mode`, `constants`, `event`, `sensor`, `actuator`, `operation`, `controller`. One class per file.

## Key design decisions
- **Sentinel handling in `Sensor`**: `odist/hdist/vdist` return `Double.MAX_VALUE` and `nsRelDist/ewRelDist/obsNsVel/obsEwVel` return `0.0` for index `-1` or unknown index, so the controller's named predicates can call sensor methods directly with no ternaries / no existence-check predicates (per the codegen rules).
- **`CalcCPA.compute()`** uses two field assignments only: `tcpa` via the standard `-(r.v)/|v|^2` formula and `cda` via `|r + v*tcpa|`. To avoid division-by-zero when no dynamic obstacle exists (Sensor returns zero velocities), I add a tiny `+0.000001` epsilon to the denominator. Documented as an invented default.
- **`CheckOPEZ.compute()`** is a single boolean assignment combining `odist(cstc) <= minSafeDist` with `depth <= 0.0` via `||`, reading `cstc` from `CalcCStc` per LRE-OP2.
- **Controller**: pure two-level if-else; all guards extracted as named booleans declared before the chain. Each non-Final mode has at least one bare-precondition event-triggered branch (`ReqOCM` in MOM/HCM/CAM; `ReqVel`/`ReqHdng` are no-cost loops in OCM) to satisfy the Z-machine deadlock-freedom proof tactic without introducing `else { mode = Same }` τ-self-loops.
- **Transition priorities in MOM**: `reqOCM > endTask > reqHCM > CAM trigger > OPEZ > HCM-by-velocity > HCM-by-default-vert`. Event-triggered overrides come first; safety-critical CAM-trigger is placed before OPEZ since collision risk dominates.
- **`@RoboChartType("nat")`** on indices and counts; `("real")` on doubles. Parameter names avoid RoboChart-reserved words (`value`, not `event`).
- No lambdas/streams/ternaries/pattern-instanceof anywhere; traditional `instanceof` + explicit cast.
