# post_codegen — LRE, iteration 1

**status:** uncertain

**summary:** 15 Java files written from scratch under `src/main/java/lre/`;
all 51 requirements traced. Build clean. Five items need human review.

## Issues

### 1. `invented_default` — safe-distance default (LRE-DM5, LRE-OP2, LRE-OP5)
LRE-DM5 says distance functions return "a safe large distance" when no obstacle
exists but fixes no value. `Sensor.SAFE_DISTANCE = 1000.0` m was invented.
*fix_directive:* if the spec intends a specific default, set `Sensor.SAFE_DISTANCE`.

### 2. `ambiguous_requirement` — cda when no dynamic obstacle (LRE-OP5 vs LRE-DM5)
LRE-DM5 makes `nsRelDist`/`ewRelDist` return **zero** with no obstacle, which via
the direct CPA formula yields `cda = 0` and would force a spurious MOM→CAM.
LRE-OP5 instead promises "large-distance defaults". Resolved by computing
`cda = sqrt(hdist(cdyn)^2 - (r.v)^2/|v|^2)`, algebraically identical for a real
obstacle but degenerating to `hdist(cdyn)` (= SAFE_DISTANCE) when none exists.
*fix_directive:* confirm this reading of LRE-OP5 over the literal LRE-DM5 zero.

### 3. `design_choice` — CAM has no entry action (LRE-FR4)
LRE-FR4 says the AUV "performs evasive manoeuvres" but LRE-DM7 permits only
advVel/advHdng and names no value. No output is emitted on entering CAM.
*fix_directive:* if CAM should advise a velocity, state it in LRE-FR4.

### 4. `design_choice` — state variables live on the operation classes
LRE-Var1..8 say "the controller maintains" the variables; they are fields of the
operation classes that compute them, surfaced into `step()` as named boolean
predicates (which the ETL lifts into the machine State interface). This follows
`codegen_trace_rules.txt`, which allows operation-class fields for `variable`.

### 5. `scope_question` — MOM↔HCM guard complementarity (LRE-Beh10 vs LRE-Beh13)
`staticObsVertDist` and `staticObsDfltVertDist` are distinct constants (both
default 1). Implemented literally, so `vdistCstcAtMostDfltVert` and
`vdistCstcAtMostVert` are independent booleans in the extracted model, and a
valuation making both MOM→HCM (Beh10) and HCM→MOM (Beh13) enabled is
representable — a potential tau-cycle divergence in FDR4 that the Java itself
cannot exhibit. Left literal for iteration 1.
*fix_directive:* if FDR4 reports divergence, conjoin `!vdistCstcAtMostDfltVert`
into the HCM→MOM guard or merge the two thresholds.

## next_step
Run the pipeline; feed any phase failure back through `/fix-from-feedback`.
