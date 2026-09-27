# LRE run 4 — iteration 1 (cold codegen)

## What I did

`java.generated.project/src/main/java/` was empty — no stale tree from another
case study, so nothing was deleted. Wrote 15 new Java files under
`src/main/java/lre/` (annotation, mode, constants, sensor, event, actuator,
operation, controller) plus `result_codegen.json` and `post_codegen.{md,json}`.
`./gradlew build` is clean.

Shape: `LreController.step(InputEvent)` is a single-method, mode-nested
if-else over OCM/MOM/HCM/CAM. The five operations (CalcVel, CalcCStc,
CalcCDyn, CheckOPEZ, CalcCPA) run first, their results are copied into the
eight LRE-Var state fields, then ten named boolean predicates are declared,
then the four `currentMode == X` blocks. No `Final` mode. Every mode has an
event-triggered bare-precondition transition (OCM: reqVel; MOM/HCM/CAM:
reqOCM), so no `else`/tau self-loop was needed anywhere. The MOM↔HCM guards
are mutually exclusive (hdist>1 ∧ vdist>1 negates all three MOM→HCM
disjuncts), and MOM→CAM→OCM needs contradictory cda, so no autonomous cycle.

## Decisions the requirements did not settle

1. **LRE-DM5 vs LRE-OP5 conflict.** DM5 says `nsRelDist`/`ewRelDist` return
   zero when no obstacle exists; OP5 says the no-dynamic-obstacle case uses
   "large-distance defaults". Zero gives cda = 0, which spuriously fires
   MOM→CAM whenever there is no dynamic obstacle. I followed OP5: relative
   *distances* default to a large value, *velocities* to zero.
2. **HCM speed.** `system_description.txt` says 0.1 m/s; LRE-FR3 says advise
   0 m/s. I implemented FR3 — `requirement_all.json` is canonical.
3. **Invented defaults.** `SAFE_LARGE_DIST = 1000.0` m; the CPA formula
   (tcpa = −(r·v)/|v|², cda = |r + v·tcpa|) with 1e-6 added to |v|² to make
   the division total.
4. `cstc`/`cdyn` annotated `nat` per LRE-Var5/6 wording even though −1 is the
   sentinel; no guard tests it, per the no-sentinel-predicate rule.

## Unresolved

Nothing blocking. The two conflicts above are flagged in `post_codegen.md`
for human review — I did not edit the requirements.
