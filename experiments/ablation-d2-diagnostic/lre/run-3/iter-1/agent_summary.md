# LRE run 5 — iteration 1 (cold codegen)

## What I did

Wrote the LRE controller from scratch: 15 Java files under
`java.generated.project/src/main/java/lre/` (`annotation`, `mode`, `constants`,
`event`, `sensor`, `actuator`, `operation`, `controller`), plus
`result_codegen.json` tracing all 51 requirements (117 entries; every file named
by at least one entry). `./gradlew build` is clean. Nothing was deleted — `src/`
held no Java. `build.gradle` still says `group = 'chemdetector'`, left alone.

Shape: single `step(InputEvent)`, four-mode outer if-else on `currentMode`
(OCM/MOM/HCM/CAM), named boolean predicates ahead of the chain, operation
`compute()` bodies restricted to direct `this.field = expr` assignments. All
missing-data handling lives in `Sensor` (large distance for
`odist`/`hdist`/`vdist`, zero for field accessors), so no controller predicate
carries a sentinel check.

## Decisions the requirements did not settle

- **Transition priority.** None given. Unconditional operator transitions
  (Beh6, Beh7, Beh12, Beh15, Beh17) go first in each mode; the other order would
  make Beh6/Beh15/Beh17 conditional, which is not what they state.
- **CAM entry action.** LRE-FR4 says "evasive manoeuvres" but names no value, and
  LRE-DM7 permits no other output. I invented nothing; CAM has no entry action.
- **Zero relative velocity.** Uncovered by LRE-OP5; tcpa divides by squared
  relative speed. Floored that denominator at 1e-6 in `Sensor.relSpeedSq`.
- **No-obstacle distance.** LRE-DM5 gives no value; used 1000.0 m.
- **cstc/cdyn in operations.** `compute()` allows no locals or calls on other
  operations, so `CheckOPEZ`/`CalcCPA` re-read the same Sensor selection
  functions `CalcCStc`/`CalcCDyn` wrap; values coincide within a step.

## Unresolved

`cstc`/`cdyn` are Java `int` holding a -1 sentinel, but int extracts as RoboChart
`nat`. No guard tests the sentinel, so nothing should surface.

All seven judgement calls are in `post_codegen.{md,json}`.
