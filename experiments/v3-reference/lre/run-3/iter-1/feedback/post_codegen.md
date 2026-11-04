# post_codegen

**status:** uncertain

**summary:** LRE implemented in 15 Java files (controller, 5 operations, sensor, register, actuator, events, constants). All 51 requirements are traced. 10 issues flagged: 2 invented defaults, 3 ambiguities, 4 design choices, 1 scope question.

## Issues

### 1. [ambiguous_requirement] cstc/cdyn are natural numbers but 'none' is specified as -1

- **raw:** LRE-Var5/Var6 declare cstc and cdyn as natural numbers with -1 meaning no obstacle; LRE-DM5/OP3/OP4 have closestStaticIndex()/closestDynamicIndex() return -1. A nat cannot be -1.
- **fix_directive:** Implemented 'none' as the index 2147483647 (Sensor.NO_OBSTACLE_INDEX), which ObstacleRegister never stores (MAX_INDEX = 2147483646). Every distance function returns the safe large distance for it and every field accessor returns 0, so no guard needs to test for 'none'. Confirm or supply the intended encoding.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/sensor/Sensor.java` lines 16-16, element `NO_OBSTACLE_INDEX`, requirements LRE-Var5, LRE-Var6, LRE-DM5, LRE-OP3, LRE-OP4
  - `java.generated.project/src/main/java/lre/sensor/ObstacleRegister.java` lines 15-15, element `MAX_INDEX`, requirements LRE-DM3

### 2. [invented_default] Safe large distance for a missing obstacle = 1000000.0 m

- **raw:** LRE-DM5 requires odist/hdist/vdist to return 'a safe large distance' when no obstacle exists, without giving a value.
- **fix_directive:** Used 1000000.0 m (Sensor.NO_OBSTACLE_DIST). Any value well above every threshold (all 1 m) works.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/sensor/Sensor.java` lines 13-13, element `NO_OBSTACLE_DIST`, requirements LRE-DM5, LRE-OP2, LRE-OP5

### 3. [design_choice] CPA geometry: sign conventions, horizontal approach, constant vertical separation

- **raw:** LRE-OP5 does not give the CPA formula. ns_rel_dist/ew_rel_dist are taken as obstacle position minus AUV position; obs_ns_vel/obs_ew_vel as absolute velocities (LRE-DM2 calls an obstacle with zero velocity static).
- **fix_directive:** tcpa = (p.w)/(|w|^2 + CPA_EPSILON), where p is the relative position and w = AUV velocity - obstacle velocity (horizontal). cda = sqrt(|p - w*tcpa|^2 + vdist^2): the vertical separation is taken as constant, so obs_roc and rate_of_climb are not used. With no dynamic obstacle, p = 0 and vdist is large, so cda is large and CAM is not entered. This also reconciles OP5's 'large-distance defaults' with DM5's zero-valued accessors.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/operation/CalcCPA.java` lines 26-38, element `compute`, requirements LRE-OP5, LRE-Var7, LRE-Var8

### 4. [invented_default] CPA_EPSILON = 0.000001 keeps the tcpa denominator non-zero

- **raw:** tcpa divides by the squared relative speed, which is zero when the AUV and the obstacle move together. The compute() profile allows no if-else.
- **fix_directive:** Added CPA_EPSILON = 0.000001 to the denominator. The model's constants interface rounds it up to 1. The denominator stays positive, so the sign of tcpa (the only thing the guards test) is unchanged; the model's cda value is only approximate.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/LreConstants.java` lines 42-42, element `CPA_EPSILON`, requirements LRE-OP5

### 5. [ambiguous_requirement] HCM advised velocity: 0 m/s (LRE-FR3) vs 0.1 m/s (system description)

- **raw:** The system description says HCM runs at reduced speed (0.1 m/s); LRE-FR3 says entering HCM advises 0 m/s.
- **fix_directive:** Followed LRE-FR3: HCM_ADVISED_VEL = 0.0.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/LreConstants.java` lines 27-27, element `HCM_ADVISED_VEL`, requirements LRE-FR3

### 6. [ambiguous_requirement] vvel is the signed rate of climb

- **raw:** LRE-OP1 says 'vvel is the rate of climb'; LRE-Beh11 tests vvel >= 1 m/s, so a fast descent does not trigger Beh11.
- **fix_directive:** Kept the literal reading (vvel = rate_of_climb, signed). A descent is still covered by Beh10, which does not depend on velocity.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/operation/CalcVel.java` lines 19-19, element `compute`, requirements LRE-OP1, LRE-Var3, LRE-Beh11

### 7. [design_choice] Transition priority within each mode

- **raw:** All LRE-Beh requirements share priority 8; the requirements give no relative order.
- **fix_directive:** MOM: reqOCM, endTask, inOpez, collision course, Beh9, Beh11, Beh10, reqHCM. HCM: reqOCM, inOpez, collision course, clear (Beh13). CAM: reqOCM, cda safe. OCM: reqVel, reqHdng, reqMOM. Every branch below a guard-only branch conjoins the negation of the guards above it. Beh11 is placed before Beh10: with equal thresholds (1 m), Beh10 placed first would make Beh11's guard unsatisfiable.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/controller/LreController.java` lines 70-128, element `step`, requirements LRE-Beh5, LRE-Beh6, LRE-Beh7, LRE-Beh8, LRE-Beh9, LRE-Beh10, LRE-Beh11, LRE-Beh12, LRE-Beh13, LRE-Beh14, LRE-Beh15, LRE-Beh16, LRE-Beh17, LRE-Beh18, LRE-DC1

### 8. [design_choice] State variables are held by the operations

- **raw:** LRE-Var1..Var8 say 'the controller maintains' each variable; the profile lets an operation own the variables it computes.
- **fix_directive:** Each variable is a field of the operation that computes it, exposed through a getter of the same name. The controller's guards read the getters. CheckOPEZ and CalcCPA read the closest index from the sensor again (fields staticIndex/dynIndex), because compute() may use only sensor calls and its own fields. The value is the same as cstc/cdyn within a step.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/operation/CheckOPEZ.java` lines 19-22, element `compute`, requirements LRE-OP2, LRE-Var1
  - `java.generated.project/src/main/java/lre/operation/CalcCPA.java` lines 27-27, element `compute`, requirements LRE-OP5

### 9. [design_choice] Literal thresholds of Beh4/Beh9/Beh11 named as constants

- **raw:** LRE-Beh4 (vel <= 1, odist > 1) and LRE-Beh9/Beh11 (>= 1 m/s) give literal values that are not among the four DM4 constants.
- **fix_directive:** Added MOM_ENTRY_MAX_VEL, MOM_ENTRY_MIN_DIST and CAUTION_VEL (each 1.0), and MOM_ADVISED_VEL / STOP_VEL for the advised velocities. Beh4's distance bound was not identified with minSafeDist.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/LreConstants.java` lines 33-39, element `LreConstants`, requirements LRE-Beh4, LRE-Beh9, LRE-Beh11

### 10. [scope_question] CAM evasive manoeuvre is not specified

- **raw:** LRE-FR4 says the AUV performs evasive manoeuvres in CAM, but no requirement gives the advice to issue (only advVel/advHdng exist).
- **fix_directive:** Entering CAM issues no advice; the controller only leaves CAM through Beh17/Beh18. Supply the manoeuvre if one is intended.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/controller/LreController.java` lines 121-128, element `step`, requirements LRE-FR4, LRE-Beh8, LRE-Beh14

## next_step

Run the pipeline; revisit these choices only if the verifiers or the operator object.
