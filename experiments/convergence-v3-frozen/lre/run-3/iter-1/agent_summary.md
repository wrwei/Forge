# LRE run 3 — iteration 1

## What I wrote

`src/main/java/` was empty — no stale tree from another study, nothing deleted.
Wrote 15 new files under `lre/`:
`annotation/RoboChartType`, `mode/LreMode`, `constants/LreConstants`,
`event/*`, `sensor/{Obstacle,ObstacleRegister,Sensor}`, `actuator/Actuator`,
`operation/{CalcVel,CalcCStc,CalcCDyn,CalcCPA,CheckOPEZ}`,
`controller/LreController`, plus `result_codegen.json` (113 entries; all 51
requirements covered, every file claimed). `gradlew build` is clean.

The controller is a single `step(InputEvent)` with the five `compute()` calls,
the eight LRE-Var fields refreshed from the operations, ten named boolean
predicates, then a pure two-level if-else over the four modes. No `Final` state.
Every mode has an event-triggered bare-precondition transition (OCM: the
reqVel/reqHdng self-loops of LRE-Beh2/3; MOM, HCM, CAM: reqOCM), so no
artificial self-loop and no unconditional `else` fallback exists. Autonomous
guard pairs are mutually exclusive (MOM<->HCM on hdist/vdist; MOM/HCM->CAM vs
CAM->OCM on cda), so no tau-cycle should arise.

## Judgements the spec did not settle

1. **HCM entry velocity.** LRE-FR3 says 0 m/s; `system_description.txt` says
   0.1 m/s. Implemented 0.0 — the structured requirement is canonical. Flagged.
2. **CAM emits nothing on entry.** LRE-FR4 names no advisory value and LRE-DM7
   allows only advVel/advHdng, so I invented none.
3. **`SAFE_LARGE_DIST = 1000.0`** for absent obstacles (LRE-DM5 says "a safe
   large distance" without a value).
4. **`MIN_REL_SPEED_SQ = 1e-9`** added to the squared relative speed in
   CalcCPA, so tcpa is finite rather than NaN at zero relative speed — the case
   LRE-OP5's own zero defaults create. `compute()` admits no locals and no
   conditionals, so a floor was the only in-rule option.
5. **cstc/cdyn are `@RoboChartType("nat")` yet initialise to -1**, because
   LRE-Var5/6 say both. No guard tests for -1; the Sensor absorbs it.
6. **LRE-Beh4's bare "greater than 1"** is a literal `1.0`, not `MIN_SAFE_DIST`;
   thresholds are named constants only where a requirement names them.

Unresolved: nothing blocking; items 1 and 2 need a human ruling on the spec.
