# Iteration 1 — lre, run 7 (condition D2, cold codegen)

**What I did.** Wrote the LRE from nothing: 15 Java files under
`java.generated.project/src/main/java/lre/`, plus `result_codegen.json` (109
entries; all 51 requirements covered, all 15 files claimed) and
`post_codegen.{md,json}`. The tree was already empty — nothing was deleted.
`build.gradle` still says `group = 'chemdetector'`; left alone as instructed.
`./gradlew build` is clean.

**Shape.** Single-method mode-nested `step(InputEvent event)`, one outer block
per mode, eleven named predicates before the chain, negations as `!predicate`.
Five operation classes with direct `this.field = expr` assignments only. No
lambdas, streams, ternaries, pattern-matching `instanceof`, `switch` or
`Optional`. Every mode has an unguarded event transition (OCM `reqVel`/
`reqHdng`; MOM `reqOCM`/`endTask`/`reqHCM`; HCM and CAM `reqOCM`), so no mode
is reachable-but-stuck. There is no FINAL mode.

**Decisions the requirements did not settle.**
1. LRE-DM5 and LRE-OP5 contradict each other on the no-dynamic-obstacle case:
   DM5 zeroes the position accessors, OP5 promises "large-distance defaults".
   Taken literally, `cda` would be 0 and the LRE would enter CAM whenever no
   dynamic obstacle exists. I took `hdist(cdyn)` as the relative-position
   magnitude so the large safe default propagates into `cda`.
2. LRE-OP5 gives no CPA formula. Used standard constant-velocity CPA with
   `|v|^2` floored by `1.0e-9`, so a non-closing pair gives `tcpa = 0` and
   `cda` = present horizontal distance rather than NaN.
3. LRE-FR4 asks for "evasive manoeuvres" in CAM but LRE-DM7 permits only
   `advVel`/`advHdng` and no value is given. CAM entry emits nothing.
4. `-1` as the no-obstacle index is specified, but `int` maps to `nat`
   unconditionally. Kept `-1` in Java; no predicate tests it.
5. `inOpez` lives on `CheckOPEZ`, not the controller, so LRE-GP1's named
   predicate is not shadowed. The other seven LRE-Var quantities are
   controller fields.

**Unresolved.** Items 1 and 3 are requirement defects, not code defects; they
need the requirement owner, and I have not edited the immutable spec.
