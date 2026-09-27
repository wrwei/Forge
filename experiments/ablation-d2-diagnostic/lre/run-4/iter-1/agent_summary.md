# LRE run 6 — iteration 1 (cold codegen)

## What I did

Wrote the LRE controller from nothing: 15 Java files under
`java.generated.project/src/main/java/lre/` (`annotation`, `mode`, `constants`,
`event`, `sensor`, `actuator`, `operation`, `controller`), plus
`result_codegen.json` (99 entries; all 51 requirements traced, all 15 files
claimed) and `post_codegen.{md,json}`. `./gradlew build` is clean.

**Nothing deleted** — preflight confirmed the tree was already cold.
`build.gradle` still carries `group = 'chemdetector'`; left alone as instructed.

Shape: one `LreController.step(InputEvent event)` invoking the five operations
at the top, refreshing the eight LRE-Var state fields from them, declaring ten
named boolean predicates, then a four-block mode-nested if-else
(OCM/MOM/HCM/CAM) with operator-triggered transitions first in each block.
Every mode has at least one unguarded event-triggered transition.

## Decisions the requirements did not settle

1. **LRE-DM5 is internally inconsistent.** It requires `nsRelDist`/`ewRelDist`
   to return zero with no obstacle, while `hdist` must return a safe large
   distance — but LRE-SF1 defines `hdist` from exactly those accessors. I gave
   `odist`/`hdist`/`vdist` their own no-obstacle branch returning
   `SAFE_DISTANCE`, so both clauses hold literally. The conflict reaches
   LRE-OP5: zero relative position makes the textbook CPA formula give
   `cda = 0`, falsely firing MOM→CAM whenever no dynamic obstacle exists. I
   expanded `cda²` as `hdist(cdyn)² + 2·tcpa·closingRate + tcpa²·relSpeedSq` —
   algebraically identical, but it carries the safe default through. Spec
   defect, flagged for review, not edited.
2. **Invented defaults:** `SAFE_DISTANCE = 1000.0` m; `MIN_REL_SPEED_SQ = 1e-6`
   in the `tcpa` denominator (LRE-OP5 is silent at zero relative velocity).
3. **LRE-Beh4's bare "1"** reads as `MIN_SAFE_DIST` for the distance terms; the
   `vel <= 1` term uses the literal, as no velocity constant exists.
4. **CAM has no entry action** — LRE-FR4 names no output value. Invented nothing.

## Unresolved

`cstc`/`cdyn` are called natural numbers yet carry a `-1` sentinel, and `int`
maps to RoboChart `nat` unconditionally. Implemented as specified, with all
missing-data handling in `Sensor` so no predicate tests the sentinel.
