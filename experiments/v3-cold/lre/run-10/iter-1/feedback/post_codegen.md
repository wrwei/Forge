# post_codegen

- **phase**: codegen
- **status**: uncertain

## Summary

All 51 LRE requirements are implemented in 15 Java files under src/main/java/lre (one controller, five operations, sensor with obstacle register, actuator, sealed input/output events, mode enum, constants). The project compiles (javac --release 17 and Gradle). The issues below record values the requirements do not give, choices between valid options, and places where the requirements conflict with the Java profile or with each other.

## Issues (18)

### 1. [invented_default] 'No obstacle' index is ObstacleRegister.NO_OBSTACLE (Integer.MAX_VALUE), not -1

- **kind**: invented_default
- **raw**: LRE-DM5/Var5/Var6 say closestStaticIndex()/closestDynamicIndex() return -1 when no obstacle exists, but also call cstc and cdyn natural numbers; the profile maps int to nat and forbids negative values meaning 'none'.
- **fix_directive**: If a different sentinel is wanted, change NO_OBSTACLE; put() already rejects it as a key, and every Sensor accessor treats an unregistered index as 'no obstacle', so nothing else depends on its value.
- **java_trace**:
  - `src/main/java/lre/sensor/ObstacleRegister.java` lines 17-17, element `NO_OBSTACLE`, requirement_ids: LRE-DM5, LRE-Var5, LRE-Var6
  - `src/main/java/lre/sensor/Sensor.java` lines 116-140, element `closestStaticIndex`, requirement_ids: LRE-OP3, LRE-OP4

### 2. [invented_default] 'Safe large distance' for a missing obstacle is 1000000.0 m

- **kind**: invented_default
- **raw**: LRE-DM5: when no obstacle exists, odist, hdist and vdist return 'a safe large distance'; no value is given.
- **fix_directive**: Change Sensor.NO_OBSTACLE_DISTANCE; any value well above every threshold (all 1 m) keeps the behaviour.
- **java_trace**:
  - `src/main/java/lre/sensor/Sensor.java` lines 15-15, element `NO_OBSTACLE_DISTANCE`, requirement_ids: LRE-DM5
  - `src/main/java/lre/sensor/Sensor.java` lines 50-78, element `hdist`, requirement_ids: LRE-SF1, LRE-SF2, LRE-SF3

### 3. [design_choice] CPA formula: AUV velocity relative to the obstacle, horizontal plane, vertical separation constant

- **kind**: design_choice
- **raw**: LRE-OP5 names cda and tcpa but gives no formula. DM2 defines 'static' as zero obstacle velocity, so obs_ns_vel/obs_ew_vel are read as absolute and the relative velocity is r = (nsVel - obsNsVel, ewVel - obsEwVel). ns_rel_dist/ew_rel_dist are read as obstacle position minus AUV position. tcpa = (p . r) / |r|^2; cda = sqrt(|p - r*tcpa|^2 + vdist^2). obs_roc and rate_of_climb are not used (the Sensor has no obstacle-depth-rate accessor).
- **fix_directive**: If the obstacle velocities are already relative, drop the sensor.nsVel()/ewVel() terms in relNsVel/relEwVel (and negate them). If cda should be horizontal only, drop the vdist term (but see the no-obstacle issue).
- **java_trace**:
  - `src/main/java/lre/operation/CalcCPA.java` lines 37-49, element `compute`, requirement_ids: LRE-OP5, LRE-Var7, LRE-Var8, LRE-DM2

### 4. [ambiguous_requirement] No-dynamic-obstacle defaults would give cda = 0 with a position-only CPA

- **kind**: ambiguous_requirement
- **raw**: LRE-DM5 makes nsRelDist/ewRelDist return zero when no obstacle exists, while LRE-OP5 relies on the Sensor's 'zero-velocity and large-distance defaults'. A CPA built only from the relative position would give cda = 0 < minSafeDist and tcpa = 0, forcing MOM/HCM into CAM with no obstacle present.
- **fix_directive**: Resolved by including vdist(cdyn)^2 in cda: with no obstacle vdist is the large default, so cda is large and tcpa is 0. Revisit if the accessors are meant to return large distances instead.
- **java_trace**:
  - `src/main/java/lre/operation/CalcCPA.java` lines 45-48, element `compute`, requirement_ids: LRE-OP5, LRE-DM5

### 5. [invented_default] CPA_SPEED_EPS = 0.000000001 keeps the tcpa denominator positive

- **kind**: invented_default
- **raw**: tcpa divides by the squared relative speed, which is zero when the AUV and the obstacle move together, or when there is no obstacle and the AUV is stationary. compute() may contain only assignments, so the zero case cannot be branched on. The model's constants interface rounds this constant up to 1, so the model's tcpa differs in magnitude from the Java (not in sign; cda's no-obstacle value is unaffected).
- **fix_directive**: If the verifiers object to the division or the rounded constant, the alternative is a Sensor-side helper that returns the guarded quotient; that adds a sensor function the requirements do not list.
- **java_trace**:
  - `src/main/java/lre/controller/LreConstants.java` lines 42-42, element `CPA_SPEED_EPS`, requirement_ids: LRE-OP5
  - `src/main/java/lre/operation/CalcCPA.java` lines 44-44, element `compute`, requirement_ids: LRE-OP5, LRE-Var8

### 6. [design_choice] CheckOPEZ and CalcCPA read the closest-obstacle index from the Sensor, not from CalcCStc/CalcCDyn

- **kind**: design_choice
- **raw**: LRE-OP2 says CheckOPEZ uses the index computed by CalcCStc (and OP5 the cdyn of CalcCDyn). The operation profile allows only sensor calls and the class's own fields in compute(), so each calls the same Sensor function (closestStaticIndex / closestDynamicIndex) that CalcCStc/CalcCDyn call in the same step; the values are identical.
- **fix_directive**: None needed unless the extractor supports one operation reading another's result.
- **java_trace**:
  - `src/main/java/lre/operation/CheckOPEZ.java` lines 25-28, element `compute`, requirement_ids: LRE-OP2, LRE-OP3
  - `src/main/java/lre/operation/CalcCPA.java` lines 38-38, element `compute`, requirement_ids: LRE-OP5, LRE-OP4

### 7. [design_choice] Variables Var1-Var8 are fields of the operation classes that compute them

- **kind**: design_choice
- **raw**: LRE-Var1..Var8 say 'the controller maintains' each variable; they are also 'set by' an operation each step. They are private fields with same-named getters in CalcVel, CheckOPEZ, CalcCStc, CalcCDyn and CalcCPA, read by the controller's predicates, rather than duplicated as controller fields.
- **fix_directive**: If controller-level state variables are required, add controller fields and assign them from the getters before the predicates.
- **java_trace**:
  - `src/main/java/lre/controller/LreController.java` lines 48-68, element `step`, requirement_ids: LRE-Var1, LRE-Var2, LRE-Var3, LRE-Var4, LRE-Var5, LRE-Var6, LRE-Var7, LRE-Var8

### 8. [design_choice] Transition priority: reqOCM, then inOpez, then collision course, then mode-specific transitions

- **kind**: design_choice
- **raw**: All LRE-Beh requirements have the same priority. Cross-mode transitions come first in each mode: operator override reqOCM, then inOpez (MOM, HCM), then collision course (MOM, HCM). In MOM the mode-specific branches follow in the order endTask, reqHCM, Beh9, Beh11, Beh10. Beh11 precedes Beh10 because, with equal vertical thresholds, Beh11 placed after Beh10 would need vdist <= 1 and vdist > 1 and could never be enabled. Lower branches conjoin the negation of earlier guard-only branches.
- **fix_directive**: Reorder the inner branches if a different priority is wanted, keeping the negated earlier guards consistent.
- **java_trace**:
  - `src/main/java/lre/controller/LreController.java` lines 90-147, element `step`, requirement_ids: LRE-Beh5, LRE-Beh6, LRE-Beh7, LRE-Beh8, LRE-Beh9, LRE-Beh10, LRE-Beh11, LRE-Beh12, LRE-Beh13, LRE-Beh14, LRE-Beh15, LRE-Beh16, LRE-Beh17, LRE-Beh18

### 9. [ambiguous_requirement] HCM advised velocity: requirement says 0 m/s, system description says 0.1 m/s

- **kind**: ambiguous_requirement
- **raw**: LRE-FR3: 'On entering HCM, the LRE advises a velocity of 0 m/s'. system_description.txt: 'HCM: Reduced speed (0.1 m/s)'. The requirement was followed.
- **fix_directive**: Set HCM_ADVISED_VEL = 0.1 if the system description is authoritative (the model would round it up to 1).
- **java_trace**:
  - `src/main/java/lre/controller/LreConstants.java` lines 24-24, element `HCM_ADVISED_VEL`, requirement_ids: LRE-FR3

### 10. [ambiguous_requirement] OCM-to-MOM thresholds '1' implemented as their own constants

- **kind**: ambiguous_requirement
- **raw**: LRE-Beh4 uses literal 1 for vel and for odist to cdyn and cstc. These may be meant as the MOM speed and minSafeDist. They are ENGAGE_MAX_VEL and ENGAGE_MIN_DIST (both 1.0), equal to the DM4 and FR2 values under the defaults.
- **fix_directive**: Replace ENGAGE_MIN_DIST with MIN_SAFE_DIST in the two predicates if the threshold should follow minSafeDist.
- **java_trace**:
  - `src/main/java/lre/controller/LreConstants.java` lines 30-32, element `ENGAGE_MIN_DIST`, requirement_ids: LRE-Beh4, LRE-DM4
  - `src/main/java/lre/controller/LreController.java` lines 56-58, element `step`, requirement_ids: LRE-Beh4

### 11. [ambiguous_requirement] vvel is the signed rate of climb, so fast descent does not trigger LRE-Beh11

- **kind**: ambiguous_requirement
- **raw**: LRE-OP1: 'vvel is the rate of climb'. LRE-Beh11 enters HCM when vvel >= 1 m/s, so only climbing at 1 m/s or more counts; descending towards a static obstacle below does not (Beh10 still applies on vertical distance alone).
- **fix_directive**: If the magnitude is intended, the vertical speed needs an absolute value, which compute() cannot express without a Sensor helper.
- **java_trace**:
  - `src/main/java/lre/operation/CalcVel.java` lines 24-24, element `compute`, requirement_ids: LRE-OP1, LRE-Var3, LRE-Beh11

### 12. [scope_question] CAM's evasive manoeuvre is not specified, so CAM issues no output

- **kind**: scope_question
- **raw**: LRE-FR4: in CAM 'the AUV performs evasive manoeuvres'. No velocity, heading or entry action is given, and the only outputs are advVel/advHdng (LRE-DM7). CAM therefore leaves the last advised values in place and only waits for reqOCM or a safe cda.
- **fix_directive**: Add an entry action on the branches into CAM once the manoeuvre (advised velocity and/or heading) is specified.
- **java_trace**:
  - `src/main/java/lre/controller/LreController.java` lines 97-99, element `step`, requirement_ids: LRE-FR4, LRE-Beh8
  - `src/main/java/lre/controller/LreController.java` lines 130-132, element `step`, requirement_ids: LRE-FR4, LRE-Beh14

### 13. [ambiguous_requirement] CAM exit ignores tcpa, so a passed near-miss keeps CAM until reqOCM

- **kind**: ambiguous_requirement
- **raw**: LRE-Beh18 leaves CAM when cda >= minSafeDist. cda is the unclamped closest-approach distance (consistent with Beh8/Beh14 testing tcpa >= 0 separately), so an obstacle that has already passed on a near-collision course keeps cda below minSafeDist and CAM stays active until reqOCM or the geometry changes.
- **fix_directive**: If CAM should end once the approach is in the past, make Beh18's guard !cdaBelowMinSafe || !tcpaNonNegative.
- **java_trace**:
  - `src/main/java/lre/controller/LreController.java` lines 143-146, element `step`, requirement_ids: LRE-Beh18

### 14. [ambiguous_requirement] MOM/HCM would alternate if staticObsDfltVertDist exceeded staticObsVertDist

- **kind**: ambiguous_requirement
- **raw**: Beh10 (MOM->HCM: vdist <= staticObsDfltVertDist) and Beh13 (HCM->MOM: hdist > staticObsHorizDist and vdist > staticObsVertDist) are mutually exclusive only while staticObsDfltVertDist <= staticObsVertDist. With the default values (all 1) they are exclusive; with a larger default vertical threshold the two guard-only transitions would form a cycle (a divergence in the model).
- **fix_directive**: If the thresholds may differ, add !staticVdistWithinDfltVert to the Beh13 guard.
- **java_trace**:
  - `src/main/java/lre/controller/LreController.java` lines 116-121, element `step`, requirement_ids: LRE-Beh10, LRE-DM4
  - `src/main/java/lre/controller/LreController.java` lines 133-137, element `step`, requirement_ids: LRE-Beh13

### 15. [ambiguous_requirement] LRE-DC1 'no duplicate transitions for the same source mode and trigger' versus several triggerless MOM transitions

- **kind**: ambiguous_requirement
- **raw**: MOM has five guard-only transitions (Beh5, Beh8, Beh9, Beh10, Beh11), so read literally DC1 cannot hold. It was read as: each Beh requirement is exactly one branch and no branch is duplicated.
- **fix_directive**: Merge Beh9, Beh10 and Beh11 into one MOM->HCM branch with a disjunctive guard if DC1 is meant literally for the same target.
- **java_trace**:
  - `src/main/java/lre/controller/LreController.java` lines 90-122, element `step`, requirement_ids: LRE-DC1

### 16. [design_choice] A cycle without operator input calls step(null)

- **kind**: design_choice
- **raw**: LRE-DM6 lists exactly six input events and none for 'no input', so no tick event was added. step(null) matches no instanceof test and evaluates only the guard-only transitions.
- **fix_directive**: None, unless the pipeline needs an explicit event for autonomous cycles.
- **java_trace**:
  - `src/main/java/lre/controller/LreController.java` lines 41-46, element `step`, requirement_ids: LRE-DM6, LRE-ARCH2

### 17. [design_choice] Obstacle record components keep the requirement's snake_case names

- **kind**: design_choice
- **raw**: LRE-DM2 names the fields ns_rel_dist, ..., obs_roc. camelCase components (nsRelDist, obsNsVel, ...) would share names with the Sensor accessors of LRE-DM5, and model names must be unique.
- **fix_directive**: None.
- **java_trace**:
  - `src/main/java/lre/sensor/Obstacle.java` lines 9-15, element `Obstacle`, requirement_ids: LRE-DM2, LRE-DM5

### 18. [invented_default] Tie-breaking and actuator start values

- **kind**: invented_default
- **raw**: Equal odist among candidates: closestStaticIndex/closestDynamicIndex return the lowest index (LRE-OP3/OP4 do not say). Before any output the Actuator reports advised velocity and heading 0.0 (LRE-DM8 does not say).
- **fix_directive**: Change Sensor.closestOf or the Actuator field initialisers if other conventions are wanted.
- **java_trace**:
  - `src/main/java/lre/sensor/Sensor.java` lines 128-140, element `closestOf`, requirement_ids: LRE-OP3, LRE-OP4
  - `src/main/java/lre/actuator/Actuator.java` lines 13-14, element `Actuator`, requirement_ids: LRE-DM8

## Next step

Run the pipeline (compile, coverage, profile check, T2M, M2M, M2T, FDR4, Dafny, Isabelle, vacuity) on java.generated.project.
