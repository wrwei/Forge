# post_codegen

- **status**: uncertain
- **summary**: LRE implemented in 15 files: controller with a mode-nested step(), five operations, sensor with obstacle register, actuator, sealed events, constants. Every requirement is traced. 14 issues record invented defaults, design choices and ambiguities; none blocks the pipeline.

## Issues

### 1. [ambiguous_requirement] cstc and cdyn are natural numbers, but 'no obstacle' is given as -1

- **raw**: LRE-Var5/Var6 type cstc and cdyn as natural numbers and say -1 means no obstacle; LRE-DM5, OP3 and OP4 have closestStaticIndex()/closestDynamicIndex() return -1. The profile maps int to nat and forbids negative values.
- **fix_directive**: Resolved: 'no obstacle' is the reserved natural index ObstacleRegister.NO_OBSTACLE (2147483647), which put() refuses, so every sensor function treats it as the no-obstacle case (safe large distance, zero fields). The controller never compares against the sentinel. Confirm, or name the value to use.
- **java_trace**:
  - `src/main/java/lre/sensor/ObstacleRegister.java` lines 18-41, `NO_OBSTACLE`, requirements: LRE-DM3, LRE-DM5, LRE-Var5, LRE-Var6
  - `src/main/java/lre/sensor/Sensor.java` lines 203-241, `closestStaticIndex`, requirements: LRE-DM5, LRE-OP3, LRE-OP4

### 2. [ambiguous_requirement] No-dynamic-obstacle defaults: DM5 says zero fields, OP5 says large-distance defaults

- **raw**: LRE-DM5: nsRelDist/ewRelDist/obsNsVel/obsEwVel return zero when no obstacle exists. LRE-OP5: the sensor returns zero-velocity and large-distance defaults. With zero relative position the textbook CPA gives cda = 0 and tcpa = 0, a false collision (MOM->CAM) whenever there is no dynamic obstacle.
- **fix_directive**: Resolved in CalcCPA: tcpa = (p.w)/(|w|^2 + CPA_EPSILON) and cda = sqrt(hdist(cdyn)^2 - tcpa*(p.w)), where p = (nsRelDist, ewRelDist) and w = AUV velocity minus obstacle velocity. This equals |p - w*tcpa| when an obstacle exists, and gives tcpa = 0 and cda = the safe large distance when none does. Checked with a scratch harness.
- **java_trace**:
  - `src/main/java/lre/operation/CalcCPA.java` lines 44-59, `compute`, requirements: LRE-OP5, LRE-DM5, LRE-Var7, LRE-Var8

### 3. [invented_default] Safe large distance for a missing obstacle = 1000000.0 m

- **raw**: LRE-DM5 requires 'a safe large distance' without a value.
- **fix_directive**: Sensor.SAFE_LARGE_DIST = 1000000.0 (private to the sensor). Confirm or supply the value.
- **java_trace**:
  - `src/main/java/lre/sensor/Sensor.java` lines 13-15, `SAFE_LARGE_DIST`, requirements: LRE-DM5

### 4. [invented_default] CPA_EPSILON = 0.000001 m^2/s^2 keeps tcpa defined at zero relative velocity

- **raw**: LRE-OP5 does not say what tcpa is when the relative velocity is zero (0/0). Operations may not branch, so the denominator gets a small positive term. The model's constants interface rounds this fractional constant up to 1, so the model's tcpa and cda magnitudes differ from the Java (the sign of tcpa is unchanged).
- **fix_directive**: Confirm the epsilon, or specify tcpa/cda for zero relative velocity.
- **java_trace**:
  - `src/main/java/lre/controller/LreConstants.java` lines 45-50, `CPA_EPSILON`, requirements: LRE-OP5

### 5. [design_choice] CPA computed in the horizontal plane

- **raw**: LRE-OP5 says CalcCPA uses the obstacle's relative position and velocity from the sensor; the sensor's only accessors are horizontal (nsRelDist, ewRelDist, obsNsVel, obsEwVel). Vertical separation and rate of climb are not used, which is conservative.
- **fix_directive**: None unless a 3-D CPA is intended.
- **java_trace**:
  - `src/main/java/lre/operation/CalcCPA.java` lines 44-59, `compute`, requirements: LRE-OP5

### 6. [design_choice] Numeric values in the requirements become named constants

- **raw**: FR2's 1 m/s and the 1 m/s bounds in Beh4, Beh9 and Beh11 share MOM_VEL. Beh4's 'odist > 1' uses MIN_SAFE_DIST, so with that threshold the cstc conjunct is implied by !inOpez. Also STOP_VEL = 0 (FR3, Beh7, Beh18), SURFACE_DEPTH = 0 (OP2) and MIN_TCPA = 0 (Beh8, Beh14).
- **fix_directive**: Confirm that the velocity bounds track the MOM speed, and that Beh4's distance bound is minSafeDist.
- **java_trace**:
  - `src/main/java/lre/controller/LreConstants.java` lines 26-43, `LreConstants`, requirements: LRE-FR2, LRE-FR3, LRE-Beh4, LRE-Beh9, LRE-Beh11, LRE-OP2, LRE-Beh8

### 7. [design_choice] Transition priority within MOM and HCM

- **raw**: The requirements give no relative priority. Order used: reqOCM, inOpez, collision course (each applies in several modes), then endTask and reqHCM, then Beh9, Beh11, Beh10. Lower branches conjoin the negation of every guard-only branch above them. Beh11 comes before Beh10 because, with staticObsVertDist = staticObsDfltVertDist, Beh11 below Beh10 could never fire.
- **fix_directive**: Confirm the priority order.
- **java_trace**:
  - `src/main/java/lre/controller/LreController.java` lines 97-132, `step`, requirements: LRE-Beh5, LRE-Beh6, LRE-Beh7, LRE-Beh8, LRE-Beh9, LRE-Beh10, LRE-Beh11, LRE-Beh12, LRE-Beh13, LRE-Beh14, LRE-Beh15, LRE-Beh16, LRE-DC1

### 8. [design_choice] CheckOPEZ and CalcCPA read the closest-obstacle index from the sensor

- **raw**: Operations take only sensors. CheckOPEZ and CalcCPA call sensor.closestStaticIndex() and sensor.closestDynamicIndex(), the same calls CalcCStc and CalcCDyn make. On the same readings within one step these return the same index as cstc and cdyn.
- **fix_directive**: None unless operations should depend on each other.
- **java_trace**:
  - `src/main/java/lre/operation/CheckOPEZ.java` lines 30-33, `compute`, requirements: LRE-OP2, LRE-OP3
  - `src/main/java/lre/operation/CalcCPA.java` lines 44-59, `compute`, requirements: LRE-OP5, LRE-OP4

### 9. [design_choice] Cycles without operator input and events a mode does not handle

- **raw**: step(null) runs a cycle with no operator input. Events a mode does not list are ignored: reqVel/reqHdng outside OCM (Beh19); reqOCM in OCM; reqMOM outside OCM; endTask outside MOM; reqHCM outside MOM.
- **fix_directive**: None.
- **java_trace**:
  - `src/main/java/lre/controller/LreController.java` lines 60-142, `step`, requirements: LRE-Beh19, LRE-ARCH2

### 10. [ambiguous_requirement] HCM speed: 0.1 m/s (system description) vs advVel(0) (FR3)

- **raw**: The system description says HCM runs at 0.1 m/s; LRE-FR3 says the LRE advises 0 m/s on entering HCM.
- **fix_directive**: Followed FR3 (STOP_VEL = 0.0).
- **java_trace**:
  - `src/main/java/lre/controller/LreController.java` lines 107-120, `step`, requirements: LRE-FR3

### 11. [ambiguous_requirement] vvel is the signed rate of climb

- **raw**: LRE-OP1: vvel is the rate of climb, so Beh11 (vvel >= 1) fires only when climbing. A fast descent toward an obstacle below is caught only by Beh10 (vdist <= staticObsDfltVertDist).
- **fix_directive**: Implemented as written. Say if Beh11 should use |vvel|.
- **java_trace**:
  - `src/main/java/lre/operation/CalcVel.java` lines 33-37, `compute`, requirements: LRE-OP1, LRE-Var3, LRE-Beh11

### 12. [scope_question] CAM: no evasive manoeuvre is specified, and the exit depends on unconstrained cda

- **raw**: LRE-FR4 says the AUV performs evasive manoeuvres in CAM but specifies no advice, so none is issued on entering CAM. Beh18 leaves CAM only when cda >= minSafeDist; cda is the closest approach over all time, so an obstacle that passed close and is receding (tcpa < 0) keeps the LRE in CAM until reqOCM (Beh17).
- **fix_directive**: Specify the CAM advice, and whether Beh18 should also accept tcpa < 0.
- **java_trace**:
  - `src/main/java/lre/controller/LreController.java` lines 134-141, `step`, requirements: LRE-FR4, LRE-Beh17, LRE-Beh18

### 13. [invented_default] Actuator's stored advice before the first output = 0.0

- **raw**: LRE-DM8 does not give initial stored values.
- **fix_directive**: Confirm.
- **java_trace**:
  - `src/main/java/lre/actuator/Actuator.java` lines 12-16, `Actuator`, requirements: LRE-DM8

### 14. [design_choice] The GP1 predicate 'inOpez' has the same name as the CheckOPEZ variable it reads

- **raw**: LRE-GP1 names the predicate 'inOpez'; LRE-Var1 names the variable inOpez. The predicate is a local in step() assigned from checkOPEZ.inOpez().
- **fix_directive**: None unless a later phase reports a name clash.
- **java_trace**:
  - `src/main/java/lre/controller/LreController.java` lines 67, `step`, requirements: LRE-GP1, LRE-Var1

## next_step

Run the pipeline (compile, coverage, profile check, transformations, FDR4, Dafny, Isabelle, vacuity) and fix the Java from its reports.
