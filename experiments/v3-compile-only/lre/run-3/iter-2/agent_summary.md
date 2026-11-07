# ITER_SUMMARY — lre, run 3

## Iteration 1

15 files under `lre/`: the annotation; `Obstacle` and `ObstacleRegister`; sealed `InputEvent` (6 records) and `OutputEvent` (2); `Sensor`; `Actuator`; five operations holding Var1–8; `LreMode`, `LreConstants` (all 1.0) and `LreController`. `step` runs the operations, declares named predicates (including `inOpez`), then runs the 18 transitions as a two-level if-else. MOM entry advises advVel(1) and HCM entry advVel(0); Beh7 and Beh18 also advise advVel(0).

**Open decisions:**
- "−1 = none" conflicts with nat-typed cstc/cdyn. Indices start at 1 and 0 means none.
- Safe large distance = 100000.0 m.
- CPA is computed in the horizontal plane with closing velocity and `+0.001` against 0/0. hdist gives |p|, so with no dynamic obstacle cda is large and tcpa is 0.
- CheckOPEZ and CalcCPA call `closestStaticIndex()`/`closestDynamicIndex()` themselves; the profile allows only sensor calls. The values are the same as CalcCStc/CalcCDyn's.
- vvel ≥ 1 is signed. Beh4 uses the literal 1. HCM advises 0 (FR3), not 0.1.
- CAM has no output; unhandled inputs are ignored; `step(null)` = no input.

## Iteration 2

Preflight error: `SAFE_LARGE_DIST` lacked `@RoboChartType("real")`; added. Rule-8 warnings: guard-only branches came after `ReqOCM`. Each mode now lists its guard-only transitions first and its event transitions after, with the negations conjoined. Safety transitions therefore take precedence over operator requests. Beh11 precedes Beh10 so it stays reachable. Positive predicates (`staticObsClear`, `cdaAtLeastMinSafe`) keep the negations plain.

**Suspected lint defect:** rule 6 reported `!vertVelHazard` missing where it is present. Guards with three or more chained negations seem to be mis-parsed. Warnings may remain for MOM's last branches.

**Unresolved:** the evasive manoeuvre (FR4).
