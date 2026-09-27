# post_codegen — LRE, iteration 1 (cold)

**status:** uncertain

**summary:** 15 Java files written under `lre/`; all 51 requirements traced in
`result_codegen.json`; `./gradlew build` clean. Five items need human review:
three invented defaults, one internal inconsistency in LRE-DM5, one
unspecified action for CAM.

## Issues

### 1. `invented_default` — safe large distance is 1000.0 m
LRE-DM5 says `odist`/`hdist`/`vdist` return "a safe large distance" when no
obstacle exists but does not give a value. `Sensor.SAFE_DISTANCE = 1000.0`.
**fix_directive:** If the AUV's sensor range implies a different value, set
`Sensor.SAFE_DISTANCE` accordingly.
**java_trace:** `src/main/java/lre/sensor/Sensor.java` — `SAFE_DISTANCE` (LRE-DM5, LRE-SF1, LRE-SF2, LRE-SF3)

### 2. `ambiguous_requirement` — LRE-DM5 defaults are mutually inconsistent
LRE-DM5 requires `nsRelDist`/`ewRelDist` to return **zero** with no obstacle,
while `hdist` must return a **safe large distance**. Since `hdist` is defined
by LRE-SF1 as `sqrt(ns_rel_dist^2 + ew_rel_dist^2)`, the two clauses conflict:
computing `hdist` from the accessors yields 0, not a large distance.
Resolved by giving `odist`/`hdist`/`vdist` their own explicit no-obstacle
branch returning `SAFE_DISTANCE`, so both clauses hold literally.
The conflict then propagates into LRE-OP5: with zero relative position the
naive CPA formula yields `cda = 0`, which would falsely enable MOM->CAM
(LRE-Beh8) whenever no dynamic obstacle exists. `CalcCPA.compute()` therefore
expands `cda^2` as `hdist(cdyn)^2 + 2*tcpa*closingRate + tcpa^2*relSpeedSq`,
algebraically identical to the standard CPA distance but carrying the
`hdist` safe default through when there is no obstacle.
**fix_directive:** Confirm the intended no-obstacle value for `nsRelDist`/
`ewRelDist`, or state that the distance functions are primitive rather than
derived from the accessors.
**java_trace:** `src/main/java/lre/operation/CalcCPA.java` — `compute` (LRE-OP5, LRE-DM5, LRE-Beh8, LRE-Beh14)

### 3. `invented_default` — floor on relative speed in `tcpa`
LRE-OP5 does not say what `tcpa` is when the relative velocity is zero
(division by zero). `CalcCPA.MIN_REL_SPEED_SQ = 1e-6` is added to the
denominator, giving `tcpa -> 0` in that case.
**fix_directive:** Replace with the intended degenerate-case value if specified.
**java_trace:** `src/main/java/lre/operation/CalcCPA.java` — `MIN_REL_SPEED_SQ` (LRE-OP5, LRE-Var8)

### 4. `unimplemented` — CAM has no entry action
LRE-FR4 says CAM "performs evasive manoeuvres" but LRE-DM7 restricts outputs
to `advVel`/`advHdng` and no requirement states which value CAM should issue.
No entry action was invented; CAM still has two outgoing transitions
(LRE-Beh17, LRE-Beh18), so the mode is not a sink.
**fix_directive:** Specify the evasive `advVel`/`advHdng` values for CAM entry.
**java_trace:** `src/main/java/lre/controller/LreController.java` — `step` (LRE-FR4)

### 5. `design_choice` — `nat`-typed indices carry a -1 sentinel
LRE-Var5/Var6 call `cstc`/`cdyn` natural numbers yet assign -1 for "none".
`int` maps to RoboChart `nat` unconditionally, so -1 is not representable in
the extracted model. Implemented as specified (-1), with all missing-data
handling pushed into `Sensor`, so no controller predicate ever tests the
sentinel.
**java_trace:** `src/main/java/lre/controller/LreController.java` — `cstc`, `cdyn` (LRE-Var5, LRE-Var6)
