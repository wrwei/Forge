# post_codegen

- **status**: uncertain
- **summary**: All 51 requirements implemented and traced (15 Java files, 148 trace entries). Thirteen points are not settled by the requirements: the -1 'none' index (encoded as 0), defaults and formula for CPA, the HCM velocity conflict, the unspecified CAM manoeuvre, and transition ordering.

## Issues

### 1. [ambiguous_requirement] "-1 means no obstacle" conflicts with the natural-number type of cstc/cdyn

- **raw**: LRE-Var5/LRE-Var6 call cstc and cdyn natural numbers yet use -1 for 'none'; LRE-DM5/OP3/OP4 have closestStaticIndex()/closestDynamicIndex() return -1. The Java profile maps int to nat and forbids negative sentinels.
- **fix_directive**: Implemented NO_OBSTACLE = 0 as the 'none' index; ObstacleRegister/Sensor.putObstacle reject index 0, so registered obstacles are indexed from 1. Confirm 0-as-none is acceptable or give another encoding.
- **java_trace**:
  - `java.generated.project/src/main/java/lre/controller/LreConstants.java` lines 47-48, `NO_OBSTACLE` — LRE-Var5, LRE-Var6, LRE-DM5
  - `java.generated.project/src/main/java/lre/sensor/ObstacleRegister.java` lines 21-29, `put` — LRE-DM3
  - `java.generated.project/src/main/java/lre/sensor/Sensor.java` lines 150-174, `closestStaticIndex/closestDynamicIndex` — LRE-OP3, LRE-OP4, LRE-DM5

### 2. [invented_default] Value of the 'safe large distance' for absent obstacles

- **raw**: LRE-DM5 (and OP2, OP5) say the distance functions return 'a safe large distance' when no obstacle exists, without a value.
- **fix_directive**: Chose NO_OBSTACLE_DIST = 1000000.0 m, returned by hdist/vdist/odist for any index with no obstacle. Confirm or supply a value.
- **java_trace**:
  - `java.generated.project/src/main/java/lre/controller/LreConstants.java` lines 49-50, `NO_OBSTACLE_DIST` — LRE-DM5, LRE-OP2, LRE-OP5

### 3. [ambiguous_requirement] HCM advised velocity: 0 m/s (LRE-FR3) vs 0.1 m/s (system description)

- **raw**: LRE-FR3: 'On entering HCM, the LRE advises a velocity of 0 m/s'. System description: 'HCM: Reduced speed (0.1 m/s)'.
- **fix_directive**: Followed the requirement: HCM_VEL = 0.0. If 0.1 m/s is intended, note that the model's constants interface rounds fractional constants up (0.1 -> 1).
- **java_trace**:
  - `java.generated.project/src/main/java/lre/controller/LreConstants.java` lines 25-26, `HCM_VEL` — LRE-FR3

### 4. [design_choice] CPA formula for cda and tcpa

- **raw**: LRE-OP5 names cda and tcpa but gives no formula. Accessors exist only for horizontal position and velocity.
- **fix_directive**: Horizontal-plane CPA. p = (nsRelDist, ewRelDist), closing velocity c = AUV velocity - obstacle velocity; tcpa = (p.c)/(|c|^2 + CPA_EPSILON); cda = sqrt(hdist^2 - tcpa*(p.c)). Using hdist (not |p|) makes the sensor's large-distance default give cda = large and tcpa = 0 when there is no dynamic obstacle, so no CAM entry. Vertical separation and obs_roc are not used. Confirm the formula and whether CPA should be 3-D.
- **java_trace**:
  - `java.generated.project/src/main/java/lre/operation/CalcCPA.java` lines 32-47, `compute` — LRE-OP5, LRE-Var7, LRE-Var8

### 5. [invented_default] CPA_EPSILON keeps the CPA denominator non-zero; it is rounded to 1 in the model

- **raw**: compute() bodies admit no branching, so a zero closing velocity (relatively stationary obstacle, or no dynamic obstacle with the AUV stopped) would divide by zero.
- **fix_directive**: Added CPA_EPSILON = 0.000000000001 (m/s)^2 to the denominator; in Java it inflates cda^2 by about eps*hdist^2/|c|^2 (negligible). The constants interface rounds it up to 1, so in the model tcpa keeps its sign but cda is larger than in Java. Guards Beh8/Beh14/Beh18 depend on cda; review whether this model deviation matters.
- **java_trace**:
  - `java.generated.project/src/main/java/lre/controller/LreConstants.java` lines 51-55, `CPA_EPSILON` — LRE-OP5, LRE-Beh8, LRE-Beh14, LRE-Beh18

### 6. [design_choice] CheckOPEZ and CalcCPA read the closest index from the sensor, not from CalcCStc/CalcCDyn

- **raw**: LRE-OP2 says CheckOPEZ uses the index computed by CalcCStc; LRE-OP5 says CalcCPA uses cdyn. The operation profile lets compute() use only sensor calls and its own fields.
- **fix_directive**: CheckOPEZ uses sensor.odist(sensor.closestStaticIndex()); CalcCPA uses sensor.closestDynamicIndex(). Within one step these equal cstc and cdyn (same sensor snapshot, deterministic selection).
- **java_trace**:
  - `java.generated.project/src/main/java/lre/operation/CheckOPEZ.java` lines 27-30, `compute` — LRE-OP2, LRE-OP3
  - `java.generated.project/src/main/java/lre/operation/CalcCPA.java` lines 32-47, `compute` — LRE-OP5, LRE-OP4

### 7. [design_choice] Transition priority within each mode

- **raw**: All LRE-Beh requirements have priority 8; no order is given.
- **fix_directive**: Order used: reqOCM first (override in MOM/HCM/CAM), then inOpez, endTask, collision (CAM), HCM conditions Beh9, Beh11, Beh10, then reqHCM; in HCM: reqOCM, inOpez, CAM, back to MOM. Later branches conjoin the negation of earlier guard-only branches. Beh11 is placed before Beh10 because with staticObsVertDist = staticObsDfltVertDist = 1, Beh10 subsumes Beh11 and Beh11 would never be enabled.
- **java_trace**:
  - `java.generated.project/src/main/java/lre/controller/LreController.java` lines 98-145, `step` — LRE-Beh5, LRE-Beh6, LRE-Beh7, LRE-Beh8, LRE-Beh9, LRE-Beh10, LRE-Beh11, LRE-Beh12, LRE-Beh13, LRE-Beh14, LRE-Beh15, LRE-Beh16, LRE-DC1

### 8. [unimplemented] CAM's evasive manoeuvre is not specified

- **raw**: LRE-FR4: 'the AUV performs evasive manoeuvres' with no advised velocity or heading.
- **fix_directive**: Entering CAM issues no output; the last advice stays in force until CAM is left (Beh17, or Beh18 with advVel(0)). Specify the CAM outputs if any are required.
- **java_trace**:
  - `java.generated.project/src/main/java/lre/controller/LreController.java` lines 108-111, `step` — LRE-FR4, LRE-Beh8
  - `java.generated.project/src/main/java/lre/controller/LreController.java` lines 137-140, `step` — LRE-FR4, LRE-Beh14

### 9. [ambiguous_requirement] vvel is the signed rate of climb

- **raw**: LRE-OP1: 'vvel is the rate of climb'; LRE-Beh11 tests vvel >= 1 m/s.
- **fix_directive**: Followed literally: vvel = rate_of_climb (signed), so Beh11 fires only when climbing. A fast descent towards a static obstacle is caught only by Beh10 (vdist <= staticObsDfltVertDist). Confirm, or use |rate_of_climb|.
- **java_trace**:
  - `java.generated.project/src/main/java/lre/operation/CalcVel.java` lines 23-27, `compute` — LRE-OP1, LRE-Var3, LRE-Beh11

### 10. [design_choice] Thresholds written as literals in the requirements became named constants

- **raw**: LRE-Beh4 uses 'vel <= 1' and 'odist > 1'; LRE-Beh9/Beh11 use '1 m/s'; LRE-Beh7/Beh18 'advVel(0)'; LRE-OP2 'depth at or below zero'; LRE-Beh8/Beh14 'tcpa non-negative'.
- **fix_directive**: Introduced MOM_ENTRY_MAX_VEL, MOM_ENTRY_MIN_DIST, HIGH_HORIZ_VEL, HIGH_VERT_VEL, STOP_VEL, SURFACE_DEPTH, MIN_TCPA rather than reusing MIN_SAFE_DIST or MOM_VEL, since the requirements state bare numbers.
- **java_trace**:
  - `java.generated.project/src/main/java/lre/controller/LreConstants.java` lines 20-43, `LreConstants` — LRE-Beh4, LRE-Beh7, LRE-Beh8, LRE-Beh9, LRE-Beh11, LRE-Beh14, LRE-Beh18, LRE-OP2

### 11. [design_choice] Variables live in the operation classes; operations run at the top of step()

- **raw**: LRE-Var1..Var8 say 'the controller maintains' each variable; LRE-OP1..OP5 say the controller invokes each operation before evaluating guards.
- **fix_directive**: Each variable is a field of the operation that computes it, read through a getter of the same name; step() calls CalcVel, CalcCStc, CheckOPEZ, CalcCDyn, CalcCPA compute() before declaring the predicates.
- **java_trace**:
  - `java.generated.project/src/main/java/lre/controller/LreController.java` lines 52-77, `step` — LRE-Var1, LRE-Var2, LRE-Var3, LRE-Var4, LRE-Var5, LRE-Var6, LRE-Var7, LRE-Var8, LRE-GP1, LRE-ARCH2

### 12. [scope_question] Events without a transition in the current mode are ignored

- **raw**: LRE-Beh19 settles OCM; the requirements do not say what endTask or reqHCM do in HCM/CAM, or reqMOM outside OCM. LRE-DC1 forbids duplicate transitions per mode and trigger, yet several trigger-less transitions leave MOM.
- **fix_directive**: Unhandled events are ignored (no branch). step(null) means no operator input this cycle. DC1 read as one branch per behavioural requirement; the trigger-less MOM transitions differ by guard.
- **java_trace**:
  - `java.generated.project/src/main/java/lre/controller/LreController.java` lines 80-156, `step` — LRE-Beh19, LRE-DC1

### 13. [design_choice] Closest-obstacle ties and record component names

- **raw**: LRE-DM5 does not say which index wins a tie in closestStaticIndex/closestDynamicIndex.
- **fix_directive**: Ties go to the lowest index. The Obstacle record keeps the requirement's names (ns_rel_dist, ...) so that its fields do not share names with the Sensor accessors (nsRelDist, ...).
- **java_trace**:
  - `java.generated.project/src/main/java/lre/sensor/Sensor.java` lines 162-174, `closestOf` — LRE-DM5
  - `java.generated.project/src/main/java/lre/sensor/Obstacle.java` lines 17-25, `Obstacle` — LRE-DM2

## next_step

Run the pipeline (compile, coverage, profile check, transformations, FDR4/Dafny/Isabelle, vacuity) and review the uncertain items above alongside its reports.
