# post_codegen — LRE, iteration 1 (cold)

status: uncertain

## Summary

15 Java files written under `java.generated.project/src/main/java/lre/`
implementing all 51 LRE requirements. `./gradlew build` is clean.
`result_codegen.json` has 109 entries; every requirement id is covered and
every source file is cited at least once.

Five items need human review: one requirement conflict, two invented
defaults and two design choices.

## Issues

### 1. ambiguous_requirement — LRE-DM5 vs LRE-OP5 disagree on the no-obstacle default

LRE-DM5 says the field accessors `nsRelDist`, `ewRelDist`, `obsNsVel`,
`obsEwVel` return **zero** when no obstacle exists, while the distance
functions return "a safe large distance". LRE-OP5 says the no-dynamic-obstacle
case is handled by the Sensor returning "zero-velocity **and large-distance**
defaults".

Taken literally, DM5 makes `nsRelDist(-1) = ewRelDist(-1) = 0`, i.e. the
phantom obstacle sits exactly on the AUV. A CPA computed from those relative
components yields `cda = 0`, which fires MOM→CAM (LRE-Beh8) whenever no
dynamic obstacle exists — the opposite of safe.

Resolved in favour of DM5's literal wording by changing the CPA formula
rather than the accessors: `CalcCPA` derives the closest distance of approach
from `hdist(cdyn)` (which DM5 does define as the safe large distance when the
obstacle is absent) as `cda = sqrt(hdist^2 - (r.v)^2/|v|^2)`, the standard
perpendicular-distance form. With no dynamic obstacle this gives
`cda = 1000`, so LRE-Beh8 stays disabled. No accessor deviates from DM5.

fix_directive: Confirm the intended no-obstacle default for the relative
position accessors, or state that the CPA must be computed from `hdist`.

java_trace: src/main/java/lre/operation/CalcCPA.java (compute) —
requirement_ids: LRE-OP5, LRE-DM5, LRE-Beh8, LRE-Beh14

### 2. invented_default — magnitude of the "safe large distance"

LRE-DM5 requires a "safe large distance" but does not give a value.
`Sensor.SAFE_LARGE_DIST = 1000.0` m. Any value strictly above every
threshold in LRE-DM4 (all 1.0 m) would do; 1000 was chosen to stay well
clear of the OCM→MOM `> 1` guards too.

fix_directive: Replace with the operationally intended sensor range if one exists.

java_trace: src/main/java/lre/sensor/Sensor.java (SAFE_LARGE_DIST) —
requirement_ids: LRE-DM5

### 3. invented_default — division guard in the CPA computation

`CalcCPA.EPSILON = 1.0E-9` is added to the relative-speed denominator so that
a zero relative velocity does not divide by zero. The codegen rules forbid
local variables, ternaries and conditional logic in `compute()`, so an
explicit zero test was not available. With zero relative velocity the result
degenerates correctly: `tcpa = 0` and `cda = hdist(cdyn)`.

fix_directive: Confirm the degenerate-case semantics for tcpa when the
relative velocity is zero.

java_trace: src/main/java/lre/operation/CalcCPA.java (EPSILON) —
requirement_ids: LRE-OP5, LRE-Var7, LRE-Var8

### 4. design_choice — relative velocity subtracts the AUV's own velocity

LRE-DM2 defines `obs_ns_vel` / `obs_ew_vel` as the obstacle's velocity, while
LRE-OP5 speaks of "relative position and velocity". Read together, position is
already relative and velocity is not, so `CalcCPA` uses
`obsNsVel(cdyn) - nsVel()` as the relative component. If `obs_ns_vel` is in
fact already AUV-relative, the subtraction must be removed.

fix_directive: State whether the obstacle velocity fields are absolute or
AUV-relative.

java_trace: src/main/java/lre/operation/CalcCPA.java (compute) —
requirement_ids: LRE-OP5, LRE-DM2

### 5. design_choice — CAM has no entry action

LRE-FR4 says CAM "performs evasive manoeuvres" but LRE-DM7 restricts the LRE's
outputs to `advVel` and `advHdng`, and no requirement gives a concrete evasive
value. No entry action is emitted on entering CAM; only the exit transitions
LRE-Beh17 and LRE-Beh18 produce output. MOM and HCM do get the entry actions
LRE-FR2 and LRE-FR3 specify (`advVel(1)` and `advVel(0)` on every incoming
transition).

fix_directive: Supply the advised velocity and/or heading for CAM, or confirm
that CAM issues no output.

java_trace: src/main/java/lre/controller/LreController.java (step) —
requirement_ids: LRE-FR4, LRE-DM7

## next_step

Run the pipeline (compile -> coverage -> preflight -> T2M -> M2M -> M2T ->
verifiers).
