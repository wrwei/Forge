# LRE Cold Baseline Run-7 — Agent Summary

## Design decisions

- **Package layout**: `lre.annotation` (RoboChartType), `lre.mode` (LreMode enum), `lre.constants`, `lre.sensor` (Obstacle record, ObstacleRegister, Sensor), `lre.event` (sealed InputEvent / OutputEvent with records), `lre.actuator`, `lre.operation` (one class per LRE-OP requirement), `lre.controller` (LreController).
- **State machine**: single-method `step(InputEvent event)` with pure two-level mode-nested if-else. Named boolean predicates declared before the switch. High-priority safety guards (`inOpez`) duplicated as first inner branches in MOM and HCM.
- **Operations**: each `compute()` uses only `this.field = expression` assignments — no local variables. `CheckOPEZ` reads `cstc` from `CalcCStc` (per LRE-OP2). `CalcCPA` uses two field assignments only, with a tiny `+1e-9` term to avoid division-by-zero when relative obstacle velocity is zero (the case where sensor sentinel defaults yield safe values).
- **Sensor safe defaults**: distance functions return `Double.MAX_VALUE` when `index == -1` or missing; field accessors return `0.0`. This keeps controller predicates simple comparisons without sentinel checks.
- **Transitions**: each LRE-Beh1..18 maps to exactly one branch. MOM-to-HCM has four ordered branches: reqHCM (operator), then horizontal-velocity, default-vertical, vertical-velocity guards. CAM exits via reqOCM or cda >= minSafeDist (Beh18 fires advVel(0)).
- **Annotations**: `@RoboChartType("nat")` on natural-number indices, `@RoboChartType("real")` on doubles on fields and method signatures only — never on locals or type arguments.
