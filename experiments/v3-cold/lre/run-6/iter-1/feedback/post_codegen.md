# post_codegen

- **status**: uncertain
- **summary**: All 51 requirements are implemented (15 Java files, trace in result_codegen.json). The issues below record defaults and choices the requirements leave open; none blocks the pipeline.

## Issues

### 1. cstc/cdyn are natural numbers but the requirements use -1 for 'no obstacle'

- **kind**: `ambiguous_requirement`
- **raw**: LRE-Var5/Var6 declare cstc and cdyn as natural numbers with 'Value -1 means no ... obstacle exists'; LRE-DM5, LRE-OP3 and LRE-OP4 have closestStaticIndex()/closestDynamicIndex() return -1 when none exists. The Java profile maps int to nat and forbids negative values and negative 'none' markers.
- **fix_directive**: Index 0 stands for 'no obstacle': ObstacleRegister.NO_OBSTACLE = 0, registered indices start at 1 (put rejects 0), and the closest-index functions return 0 when there is no candidate. Every sensor function treats an unregistered index (including 0) as 'no obstacle'. Confirm the encoding.
- **java_trace**:
  - `src/main/java/lre/sensor/ObstacleRegister.java` lines 14-26, `ObstacleRegister`, requirements: LRE-DM3, LRE-Var5, LRE-Var6
  - `src/main/java/lre/sensor/Sensor.java` lines 136-160, `closestStaticIndex`, requirements: LRE-DM5, LRE-OP3, LRE-OP4

### 2. Value of the 'safe large distance' returned when no obstacle exists

- **kind**: `invented_default`
- **raw**: LRE-DM5: 'When no obstacle exists, distance functions odist, hdist, and vdist return a safe large distance' (no value given).
- **fix_directive**: Sensor.SAFE_LARGE_DISTANCE = 1000000.0 m, used for hdist, vdist and odist of an unregistered index. Field accessors return 0.0 as LRE-DM5 states.
- **java_trace**:
  - `src/main/java/lre/sensor/Sensor.java` lines 14-15, `SAFE_LARGE_DISTANCE`, requirements: LRE-DM5, LRE-OP2, LRE-OP5

### 3. CPA formula for cda and tcpa

- **kind**: `design_choice`
- **raw**: LRE-OP5 names cda and tcpa but gives no formula; DM2 calls obs_ns_vel/obs_ew_vel 'obstacle velocity' (static means zero velocity, so they are absolute), and DM5's zero field defaults would make a purely horizontal cda equal 0 when there is no dynamic obstacle.
- **fix_directive**: Relative velocity w = AUV velocity - obstacle velocity (horizontal); r = obstacle position relative to the AUV. tcpa = (r.w)/(|w|^2 + CPA_EPSILON); cda = sqrt(|r - w*tcpa|^2 + vdist^2), i.e. the vertical separation is taken as constant (obs_roc and rate_of_climb are not used). With no dynamic obstacle this gives tcpa = 0 and cda = the safe large distance. tcpa is not clamped, so it is negative once the obstacle has passed.
- **java_trace**:
  - `src/main/java/lre/operation/CalcCPA.java` lines 33-53, `compute`, requirements: LRE-OP5, LRE-Var7, LRE-Var8, LRE-DM2

### 4. CPA_EPSILON keeps tcpa defined at zero relative velocity; it rounds up to 1 in the model

- **kind**: `invented_default`
- **raw**: tcpa divides by the squared relative speed, which is 0 when the AUV and obstacle move together (or the AUV is still and there is no dynamic obstacle).
- **fix_directive**: LreConstants.CPA_EPSILON = 0.000001 m^2/s^2 is added to the divisor. The model's constants interface rounds it up to 1, so the model's tcpa/cda values (and hence the cda/tcpa guards of LRE-Beh8, Beh14, Beh18) differ numerically from the Java when relative speeds are small; signs are unchanged.
- **java_trace**:
  - `src/main/java/lre/controller/LreConstants.java` lines 50-55, `CPA_EPSILON`, requirements: LRE-OP5, LRE-Beh8, LRE-Beh14, LRE-Beh18

### 5. HCM advised velocity: 0 m/s (LRE-FR3) versus 0.1 m/s (system description)

- **kind**: `ambiguous_requirement`
- **raw**: LRE-FR3: 'On entering HCM, the LRE advises a velocity of 0 m/s'. The system description says HCM is 'Reduced speed (0.1 m/s)'.
- **fix_directive**: Implemented the requirement: HCM_VELOCITY = 0.0, advised on every transition into HCM.
- **java_trace**:
  - `src/main/java/lre/controller/LreConstants.java` lines 26-27, `HCM_VELOCITY`, requirements: LRE-FR3

### 6. CAM evasive manoeuvre has no specified output

- **kind**: `ambiguous_requirement`
- **raw**: LRE-FR4: 'the AUV performs evasive manoeuvres' with no advised velocity or heading; LRE-DM7 allows only advVel and advHdng.
- **fix_directive**: Transitions into CAM (LRE-Beh8, LRE-Beh14) carry no action; no evasive advice is invented.
- **java_trace**:
  - `src/main/java/lre/controller/LreController.java` lines 115-116, `step`, requirements: LRE-FR4, LRE-Beh8, LRE-Beh14

### 7. CAM exit ignores tcpa, so a passed near-miss can hold CAM

- **kind**: `ambiguous_requirement`
- **raw**: LRE-Beh18 leaves CAM only when cda >= minSafeDist. cda is the miss distance of the whole relative track, so after a close pass with unchanged relative motion cda stays below minSafeDist although tcpa < 0.
- **fix_directive**: Implemented as stated (guard !cdaBelowMinSafe). CAM is then left by reqOCM (LRE-Beh17), a change in relative motion, or another dynamic obstacle becoming the closest. Confirm, or add tcpa < 0 to the exit guard.
- **java_trace**:
  - `src/main/java/lre/controller/LreController.java` lines 145-151, `step`, requirements: LRE-Beh17, LRE-Beh18, LRE-Var7

### 8. Transition priority within MOM and HCM

- **kind**: `design_choice`
- **raw**: All LRE-Beh requirements have the same priority; java_codegen_rules 4b requires an ordered inner chain with later branches conjoining the negation of earlier predicate-only guards.
- **fix_directive**: MOM: reqOCM, endTask, inOpez, collision course (CAM), Beh9, Beh11, Beh10, reqHCM. HCM: reqOCM, inOpez, collision course (CAM), Beh13. Beh11 precedes Beh10 because with equal thresholds Beh11's guard implies Beh10's, and the opposite order would make Beh11 unreachable. reqHCM is last and conjoins the negations of all earlier autonomous guards.
- **java_trace**:
  - `src/main/java/lre/controller/LreController.java` lines 107-143, `step`, requirements: LRE-Beh5, LRE-Beh6, LRE-Beh7, LRE-Beh8, LRE-Beh9, LRE-Beh10, LRE-Beh11, LRE-Beh12, LRE-Beh13, LRE-Beh14, LRE-Beh15, LRE-Beh16, LRE-DC1

### 9. MOM/HCM autonomous guards are disjoint only while staticObsDfltVertDist <= staticObsVertDist

- **kind**: `scope_question`
- **raw**: LRE-Beh10 enters HCM when vdist <= staticObsDfltVertDist; LRE-Beh13 returns to MOM when vdist > staticObsVertDist (and hdist > staticObsHorizDist). With the default values (all 1) they cannot both hold.
- **fix_directive**: Kept the guards as stated. If staticObsDfltVertDist were raised above staticObsVertDist, MOM and HCM could alternate without an event; Beh13 would then need vdist > staticObsDfltVertDist as well.
- **java_trace**:
  - `src/main/java/lre/controller/LreController.java` lines 123-142, `step`, requirements: LRE-Beh10, LRE-Beh13, LRE-DM4

### 10. vvel is the signed rate of climb

- **kind**: `ambiguous_requirement`
- **raw**: LRE-OP1: 'vvel is the rate of climb'. LRE-Beh11 tests vvel >= 1, so a fast descent toward an obstacle below does not trigger it.
- **fix_directive**: Implemented literally (vvel = rateOfClimb()). LRE-Beh10 still forces HCM within staticObsDfltVertDist regardless of velocity.
- **java_trace**:
  - `src/main/java/lre/operation/CalcVel.java` lines 24-28, `compute`, requirements: LRE-OP1, LRE-Var3, LRE-Beh11

### 11. Operations re-read the closest index from the sensor

- **kind**: `design_choice`
- **raw**: LRE-OP2 and LRE-OP5 use the index computed by CalcCStc/CalcCDyn, but an operation takes only sensors in its constructor and assigns only its own fields (java_codegen_rules 5).
- **fix_directive**: CheckOPEZ and CalcCPA call sensor.closestStaticIndex()/closestDynamicIndex() themselves; within a step this is the same value as cstc/cdyn. Variables live in the operation that computes them and the controller reads them through the getters.
- **java_trace**:
  - `src/main/java/lre/operation/CheckOPEZ.java` lines 24-27, `compute`, requirements: LRE-OP2, LRE-Var1
  - `src/main/java/lre/operation/CalcCPA.java` lines 33-53, `compute`, requirements: LRE-OP5

### 12. A cycle with no operator input is step(null)

- **kind**: `design_choice`
- **raw**: LRE-DM6 lists six input events and no 'no event' value; autonomous transitions must still be evaluated every cycle.
- **fix_directive**: step accepts null for a cycle without operator input; no extra event type is introduced.
- **java_trace**:
  - `src/main/java/lre/controller/LreController.java` lines 69-92, `step`, requirements: LRE-ARCH2, LRE-DM6

### 13. Initial values before the first step

- **kind**: `invented_default`
- **raw**: The requirements give no initial advised velocity/heading or initial variable values.
- **fix_directive**: Actuator velocity and heading start at 0.0; hvel, vvel, vel, cda, tcpa start at 0.0, cstc and cdyn at 0 (none), inOpez false. Every step recomputes the variables before any guard reads them.
- **java_trace**:
  - `src/main/java/lre/actuator/Actuator.java` lines 12-13, `Actuator`, requirements: LRE-DM8
  - `src/main/java/lre/operation/CalcCPA.java` lines 24-25, `CalcCPA`, requirements: LRE-Var7, LRE-Var8

## next_step

Run the pipeline (compile, coverage, profile check, transformations, verifiers) and confirm or redirect the choices listed here.
