# LRE run 5 — iteration 1 (cold codegen)

## What I did
`java.generated.project/src/main/java/` was empty (Gradle scaffold only), so
**nothing was deleted** — no stale tree from another study. Wrote 15 Java files
under `src/main/java/lre/` (`annotation`, `mode`, `constants`, `sensor`,
`event`, `actuator`, `operation`, `controller`) plus `result_codegen.json`
(59 entries; all 51 requirement ids covered, every file claimed by ≥1 entry)
and `post_codegen.{md,json}`. `./gradlew build` is clean.

Shape: `step(InputEvent)` calls the five operations, declares 11 named boolean
predicates, then a two-level if-else with one outer block per `currentMode`.
Every mode has an unguarded event branch (reqVel in OCM, reqOCM elsewhere), so
no mode is a sink; there is no Final state.

## Decisions the requirements did not settle
1. **`cda` with no dynamic obstacle.** LRE-DM5 zeroes `nsRelDist`/`ewRelDist`
   when no obstacle exists, which via the textbook CPA formula gives `cda = 0`
   and a spurious MOM→CAM. LRE-OP5 instead promises "large-distance defaults".
   I followed LRE-OP5: `cda = sqrt(hdist(cdyn)² − (r·v)²/|v|²)` — algebraically
   the same for a real obstacle, but degenerating to `hdist(cdyn)` (the safe
   large distance) when there is none.
2. **Safe large distance = 1000.0 m** — invented; LRE-DM5 fixes no value.
3. **CAM emits no output.** LRE-FR4 says "evasive manoeuvres"; LRE-DM7 allows
   only advVel/advHdng and names no value, so I emitted nothing rather than
   invent a number.
4. **LRE-Var1..8 live on the operation classes**, surfaced into `step()` as
   named predicates (the trace rules explicitly permit operation-class fields).

## Unresolved
`staticObsVertDist` and `staticObsDfltVertDist` are distinct constants (both
default 1), so LRE-Beh10's and LRE-Beh13's guards are not complementary once the
extractor turns each predicate into an independent boolean. A MOM→HCM→MOM tau
cycle is therefore representable in the model though impossible in the Java.
I implemented both requirements literally for iteration 1; if FDR4 reports a
divergence I will conjoin `!vdistCstcAtMostDfltVert` into the HCM→MOM guard.
