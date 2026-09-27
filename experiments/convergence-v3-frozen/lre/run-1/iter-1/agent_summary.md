# LRE run 1 — iteration 1 (cold codegen)

## What I did
`java.generated.project/src/main/java/` was empty — nothing was deleted. Wrote
15 new files under `lre/`: `annotation/RoboChartType`, `mode/LreMode`,
`constants/LreConstants`, `sensor/{Obstacle,ObstacleRegister,Sensor}`,
`event/{InputEvent,OutputEvent}`, `actuator/Actuator`,
`operation/{CalcVel,CalcCStc,CalcCDyn,CheckOPEZ,CalcCPA}`,
`controller/LreController`. `./gradlew build` is clean. All 50 requirements are
traced in `result_codegen.json` (65 entries; every file cited at least once).

Event records are named exactly as the spec names the channels (`reqVel`,
`advVel`, …) and outputs go through `actuator.apply(new OutputEvent.advVel(x))`,
the pattern the ETL's `extractOutputEventInfo` recognises. Constants keep the
spec names (`minSafeDist`, …) so the generated `Constants` interface matches the
requirement vocabulary. All eight LRE-Var quantities are controller fields, so
they surface as `LreController_State`. `advVel(1.0)` is the first action on both
transitions into MOM and `advVel(0.0)` on all four into HCM, so the EGL can lift
them to entry actions (LRE-FR2/FR3). No `Final` mode; every mode has an
unguarded event-triggered branch (`reqVel` in OCM, `reqOCM` elsewhere) giving a
bare precondition without a tau self-loop.

## Judgements the requirements did not settle
1. **HCM entry velocity.** LRE-FR3 says advise 0 m/s; the system description says
   0.1 m/s. Followed LRE-FR3 (requirement_all.json is canonical).
2. **No-dynamic-obstacle defaults.** LRE-DM5 says `nsRelDist`/`ewRelDist` return
   zero; LRE-OP5 says the Sensor returns "large-distance" defaults. DM5 taken
   literally gives `cda = 0` and a spurious, permanent CAM, so I followed OP5.
3. **Invented defaults.** `SAFE_LARGE_DISTANCE = 1000.0`; CalcCPA's denominator
   carries `MIN_CLOSING_SPEED_SQ = 1e-9` because `compute()` may not branch.
4. **Transition priority.** Unspecified. Autonomous safety guards first, then
   remaining autonomous guards, then event-triggered branches — which is also the
   shape preflight rule8 asks for. Each triggerless branch textually negates its
   predecessors (rule6).
5. **ObstacleRegister** is a dense immutable `List`, not a partial map: the ETL
   maps `List` to `Seq`, while `Map` silently degrades to `real`.

## Unresolved
Nothing blocking. Items 1 and 2 are genuine spec conflicts for human review; both
are recorded in `post_codegen.{md,json}`.
