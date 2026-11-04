# post_codegen

**status:** uncertain

**summary:** All 51 LRE requirements are implemented and traced in java.generated.project/result_codegen.json. Eleven decisions the requirements do not settle are listed below: 2 invented defaults, 5 design choices, 3 ambiguous requirements and 1 scope question. None is unimplemented.

## Issues

### 1. [design_choice] 'No obstacle' index is a non-negative sentinel, not -1

- **raw:** LRE-Var5/Var6/DM5/OP3/OP4 say cstc/cdyn = -1 means no obstacle, but the same requirements type them as natural numbers and the Java profile maps int to nat (never negative).
- **fix_directive:** Kept: closestStaticIndex()/closestDynamicIndex() return ObstacleRegister.NO_INDEX (Integer.MAX_VALUE), an index put() refuses to register. The controller never tests it: every sensor function returns its no-obstacle default (large distance, zero field) for an empty index.
- **java_trace:**
  - `src/main/java/lre/sensor/ObstacleRegister.java` lines 15-37, `NO_INDEX` — LRE-Var5, LRE-Var6, LRE-DM3
  - `src/main/java/lre/sensor/Sensor.java` lines 126-163, `closestStaticIndex` — LRE-DM5, LRE-OP3, LRE-OP4

### 2. [invented_default] 'Safe large distance' is 1000000.0 m

- **raw:** LRE-DM5: when no obstacle exists, odist, hdist and vdist return a safe large distance (value not given).
- **fix_directive:** Kept as a private sensor constant SAFE_LARGE_DIST = 1000000.0, far above every threshold (all 1 m).
- **java_trace:**
  - `src/main/java/lre/sensor/Sensor.java` lines 13-14, `SAFE_LARGE_DIST` — LRE-DM5, LRE-OP2, LRE-OP5

### 3. [invented_default] Actuator holds 0.0 velocity and heading before any advice

- **raw:** LRE-DM8: the actuator stores the latest advVel/advHdng values; no initial value is given.
- **fix_directive:** Kept: advisedVelocity and advisedHeading start at 0.0.
- **java_trace:**
  - `src/main/java/lre/actuator/Actuator.java` lines 12-13, `Actuator` — LRE-DM8

### 4. [design_choice] CheckOPEZ and CalcCPA take the closest index from the sensor themselves

- **raw:** LRE-OP2: CheckOPEZ uses the index computed by CalcCStc. LRE-OP5: CalcCPA uses cdyn. The profile allows an operation's compute() to use only sensor calls and its own fields.
- **fix_directive:** Kept: CheckOPEZ.opezIndex and CalcCPA.cpaIndex are assigned from sensor.closestStaticIndex()/closestDynamicIndex(), the same call CalcCStc/CalcCDyn make on the same sensor snapshot in the same step, so the values are equal to cstc/cdyn.
- **java_trace:**
  - `src/main/java/lre/operation/CheckOPEZ.java` lines 27-31, `compute` — LRE-OP2, LRE-Var5
  - `src/main/java/lre/operation/CalcCPA.java` lines 33-45, `compute` — LRE-OP5, LRE-Var6

### 5. [design_choice] CPA formula: horizontal CPA plus constant vertical separation

- **raw:** LRE-OP5 gives no formula for cda/tcpa. LRE-DM5 says nsRelDist/ewRelDist return 0 with no obstacle, which would make a position-only CPA report cda = 0 (a false collision) whenever the AUV moves and no dynamic obstacle exists.
- **fix_directive:** Kept: closing velocity c = AUV velocity - obstacle velocity; tcpa = (p.c)/|c|^2; cda = sqrt(|p - c*tcpa|^2 + vdist^2). vdist's large default makes cda large when no dynamic obstacle exists. obs_roc is not used (the sensor has no accessor for it). When |c| = 0, tcpa and cda are NaN: no collision is predicted, and CAM's exit guard !(cda < minSafeDist) holds.
- **java_trace:**
  - `src/main/java/lre/operation/CalcCPA.java` lines 25-45, `compute` — LRE-OP5, LRE-Var7, LRE-Var8
  - `src/main/java/lre/controller/LreController.java` lines 59-61, `step` — LRE-Beh8, LRE-Beh14, LRE-Beh18

### 6. [ambiguous_requirement] HCM velocity: 0.1 m/s (system description) vs 0 m/s (LRE-FR3)

- **raw:** system_description.txt: 'HCM: Reduced speed (0.1 m/s)'. LRE-FR3: 'On entering HCM, the LRE advises a velocity of 0 m/s'.
- **fix_directive:** Implemented LRE-FR3 (HCM_ADVISED_VEL = 0.0); the requirements are the source of truth.
- **java_trace:**
  - `src/main/java/lre/controller/LreConstants.java` lines 31-32, `HCM_ADVISED_VEL` — LRE-FR3

### 7. [ambiguous_requirement] Literal '1' thresholds in LRE-Beh4/Beh9/Beh11

- **raw:** LRE-Beh4: vel <= 1, odist(cdyn) > 1, odist(cstc) > 1. LRE-Beh9/Beh11: hvel/vvel at or above 1 m/s. None names minSafeDist.
- **fix_directive:** Kept as the requirements state them, as named constants VEL_LIMIT = 1.0 and MOM_ENTRY_CLEARANCE = 1.0 independent of MIN_SAFE_DIST.
- **java_trace:**
  - `src/main/java/lre/controller/LreConstants.java` lines 22-26, `VEL_LIMIT` — LRE-Beh4, LRE-Beh9, LRE-Beh11
  - `src/main/java/lre/controller/LreController.java` lines 62-67, `step` — LRE-Beh4

### 8. [design_choice] Transition priority within each mode

- **raw:** All LRE-Beh requirements have priority 8; their relative order within a mode is not given.
- **fix_directive:** Kept: reqOCM first (override valid in MOM/HCM/CAM), then inOpez (so LRE-Beh5/Beh16 guard is inOpez alone), then collision -> CAM, then endTask/reqHCM, then Beh9, Beh11, Beh10. Beh11 precedes Beh10 because with equal thresholds Beh10's guard contains Beh11's, so the other order would make Beh11 unreachable. Each branch below a guard-only branch conjoins its negation.
- **java_trace:**
  - `src/main/java/lre/controller/LreController.java` lines 94-162, `step` — LRE-Beh5, LRE-Beh6, LRE-Beh7, LRE-Beh8, LRE-Beh9, LRE-Beh10, LRE-Beh11, LRE-Beh12, LRE-Beh13, LRE-Beh14, LRE-Beh15, LRE-Beh16, LRE-Beh17, LRE-Beh18, LRE-DC1

### 9. [ambiguous_requirement] vvel is the signed rate of climb

- **raw:** LRE-OP1: 'vvel is the rate of climb'. LRE-Beh11: vvel at or above 1 m/s.
- **fix_directive:** Kept literally: vvel = rateOfClimb (signed), so Beh11 fires on climbing at >= 1 m/s, not on descending. Beh10 covers vertical proximity regardless of velocity.
- **java_trace:**
  - `src/main/java/lre/operation/CalcVel.java` lines 23-27, `compute` — LRE-OP1, LRE-Var3, LRE-Beh11

### 10. [scope_question] Inputs with no transition are ignored; CAM issues no evasive command

- **raw:** LRE-Beh19: in modes other than OCM, operator velocity/heading inputs are overridden. LRE-FR4: CAM performs evasive manoeuvres (no output given). LRE-DM7: advVel/advHdng are the only outputs.
- **fix_directive:** Kept: reqVel/reqHdng are ignored outside OCM; reqOCM, reqHCM and endTask are ignored in OCM; reqMOM, reqHCM and endTask are ignored in HCM and CAM. CAM has no entry action because no evasive command is specified. A cycle with no operator input is step(null).
- **java_trace:**
  - `src/main/java/lre/controller/LreController.java` lines 51-164, `step` — LRE-Beh19, LRE-FR4, LRE-DM7

### 11. [design_choice] Ties between equally close obstacles go to the lowest index

- **raw:** LRE-OP3/OP4: return the index of the obstacle with the minimum odist.
- **fix_directive:** Kept: strict '<' over ascending indices.
- **java_trace:**
  - `src/main/java/lre/sensor/Sensor.java` lines 126-163, `closestStaticIndex` — LRE-OP3, LRE-OP4

## next_step

Run the pipeline. None of these items blocks it; revisit them only if a verifier or the requirement owner objects.
