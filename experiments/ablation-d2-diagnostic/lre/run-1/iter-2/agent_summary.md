# Iteration 1 — LRE, run 3 (cold codegen)

## What I did

Wrote the LRE controller from scratch: 15 Java files under
`java.generated.project/src/main/java/lre/`, in the packages CLAUDE.md
prescribes. Nothing pre-existing was deleted — the tree was empty, as the
preflight confirmed. `./gradlew build` passes. `result_codegen.json` has 109
entries covering all 51 requirement ids, every source file cited at least once.

Shape: one `step(InputEvent)` that invokes the five operations, refreshes the
eight LRE-Var state fields from them, declares ten named boolean predicates,
then a two-level `currentMode == X` if-else carrying the 18 transitions of
LRE-Beh2..18. Entry actions inline: `advVel(1)` on every transition into MOM
(FR2), `advVel(0)` into HCM (FR3). Every mode has an unguarded event-triggered
exit (reqVel in OCM, reqOCM in MOM/HCM/CAM), so no mode relies solely on
guarded transitions for progress.

## Decisions the requirements did not settle

1. **DM5 vs OP5 conflict.** DM5 says the relative-position accessors return
   zero with no obstacle; OP5 says "large-distance defaults". Literally, DM5
   puts the phantom obstacle on the AUV, giving `cda = 0`, firing MOM→CAM
   whenever no dynamic obstacle exists. I kept DM5 literal and changed the CPA
   formula instead: `cda = sqrt(hdist(cdyn)² − (r·v)²/|v|²)`, which reduces to
   the safe large distance when absent because DM5 defines `hdist` that way.
   Flagged for human review, not a spec edit.
2. **Obstacle velocity read as absolute**, so relative velocity subtracts the
   AUV's own. DM2 says "obstacle velocity"; OP5 says "relative".
3. **`SAFE_LARGE_DIST = 1000.0`** and **`EPSILON = 1.0E-9`** invented — DM5
   gives no magnitude, and `compute()` may not contain a zero test.
4. **CAM emits no entry action.** FR4 wants evasive manoeuvres; DM7 permits
   only advVel/advHdng and no requirement gives a value.

## Unresolved

Whether `cstc`/`cdyn`, which Var5/6 define with a −1 sentinel but which the
extractor maps to `nat`, survive extraction. No `-1` literal reaches extracted
code. All items are also in `post_codegen.{md,json}`.
