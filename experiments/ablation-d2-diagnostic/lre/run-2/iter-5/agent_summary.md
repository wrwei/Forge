# LRE run 4 — iteration 1 (cold codegen)

## What I wrote

Nothing pre-existing was under `java.generated.project/src` (preflight
confirmed 0 Java files), so nothing was deleted. I wrote 15 new files under
`src/main/java/lre/`: `annotation/RoboChartType`, `mode/LreMode`,
`constants/LreConstants`, `event/{InputEvent,OutputEvent}`,
`sensor/{Obstacle,ObstacleRegister,Sensor}`,
`operation/{CalcVel,CalcCStc,CalcCDyn,CheckOPEZ,CalcCPA}`,
`actuator/Actuator`, `controller/LreController`, plus
`result_codegen.json` (51/51 requirements traced, every file cited at least
once). `gradlew build` is green. No `Clock` class and no `Final` mode exist.

`step()` runs the five operations, latches them into the eight LRE-Var
controller fields, declares ten named predicates, then selects one transition
in a two-level `currentMode == X` if-else. Every mode has an unguarded
event-triggered exit (OCM: reqVel/reqHdng; MOM/HCM/CAM: reqOCM).

## Decisions the requirements did not settle

1. **CPA formulation (LRE-OP5).** The spec names cda/tcpa but gives no
   formula. I used the standard horizontal CPA projection from relative
   position and velocity, with the *current vertical separation* folded into
   cda. That is deliberate: LRE-DM5 fixes the field accessors at zero when no
   obstacle exists, so a purely horizontal cda would be 0 with no dynamic
   obstacle and would spuriously fire MOM→CAM. Including `vdist(cdyn)` —
   which returns the safe large distance — makes that case safe while keeping
   DM5 literal.
2. **Degenerate relative velocity.** `relSpeedSq` carries a 1e-9 floor so
   tcpa stays total without a ternary or a sentinel branch.
3. **HCM entry velocity.** `system_description.txt` says HCM runs at 0.1 m/s;
   LRE-FR3 says 0 m/s. I followed FR3 and flag the contradiction for human
   review; I did not edit the spec.
4. **Naming.** `LreConstants` uses the spec's camelCase; the safe large
   distance (1e6) is a private Sensor constant, not a LRE-DM4 threshold.

## Unresolved

Nothing blocking. MOM↔HCM guards are mutually exclusive at any one valuation,
so I expect no guard-only tau cycle; that is the property I am least sure
survives abstraction into CSP.
