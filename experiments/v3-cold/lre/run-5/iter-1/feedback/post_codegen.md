# post_codegen

**status:** `uncertain`

**summary:** All 51 LRE requirements are implemented (15 Java files under lre/, 4 modes, 18 transitions, 5 operations) and the project compiles. Eleven decisions were not settled by the requirements and are listed for review; none is known to break the profile.

## Issues

### 1. [ambiguous_requirement] '-1 means no obstacle' conflicts with natural-number cstc/cdyn; index 0 used instead

- **kind:** `ambiguous_requirement`
- **raw:** LRE-DM5, LRE-OP3, LRE-OP4 say closestStaticIndex()/closestDynamicIndex() return -1 when none exists; LRE-Var5/Var6 declare cstc/cdyn natural numbers with -1 meaning none. The Java profile maps int to nat and forbids negative values or negative 'none' markers.
- **fix_directive:** Kept cstc/cdyn non-negative: index 0 (ObstacleRegister.NO_OBSTACLE) means 'no obstacle', and the register rejects identifiers below 1, so 0 is never a real obstacle. The sensor's distance functions and field accessors already return their no-obstacle defaults for index 0. If obstacle identifiers must include 0, choose another reserved non-negative sentinel in ObstacleRegister.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/sensor/ObstacleRegister.java` lines 22-37, element `NO_OBSTACLE / put`, requirement_ids: LRE-DM3, LRE-DM5, LRE-Var5, LRE-Var6
  - `java.generated.project/src/main/java/lre/sensor/Sensor.java` lines 134-162, element `closestStaticIndex / closestDynamicIndex`, requirement_ids: LRE-DM5, LRE-OP3, LRE-OP4

### 2. [invented_default] Value of the 'safe large distance' for absent obstacles: 1,000,000 m

- **kind:** `invented_default`
- **raw:** LRE-DM5: 'When no obstacle exists, distance functions odist, hdist, and vdist return a safe large distance.' No value is given.
- **fix_directive:** Sensor.SAFE_LARGE_DISTANCE = 1000000.0 m, far above every 1 m threshold. It is private to the sensor, not a model constant. Change it there if a different value is required.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/sensor/Sensor.java` lines 18-19, element `SAFE_LARGE_DISTANCE`, requirement_ids: LRE-DM5, LRE-SF1, LRE-SF2, LRE-SF3, LRE-OP2, LRE-OP5

### 3. [invented_default] Tie-break of the closest-obstacle selection: lowest index

- **kind:** `invented_default`
- **raw:** LRE-OP3/OP4 select the obstacle with minimum odist but do not say which wins when two are equally close.
- **fix_directive:** Sensor.closestOf iterates identifiers in ascending order with a strict '<', so the lowest identifier wins a tie.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/sensor/Sensor.java` lines 152-162, element `closestOf`, requirement_ids: LRE-OP3, LRE-OP4

### 4. [invented_default] Actuator's advised velocity and heading before any advice: 0

- **kind:** `invented_default`
- **raw:** LRE-DM8: the Actuator stores the latest advVel/advHdng values; no initial value is given.
- **fix_directive:** Both start at 0.0. They are only observable before the first output event.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/actuator/Actuator.java` lines 12-13, element `advisedVelocity / advisedHeading`, requirement_ids: LRE-DM8

### 5. [design_choice] CPA formulas for cda and tcpa

- **kind:** `design_choice`
- **raw:** LRE-OP5 names cda and tcpa and says CalcCPA uses the obstacle's relative position and velocity from the Sensor, with zero-velocity and large-distance defaults when there is no dynamic obstacle; it gives no formulas.
- **fix_directive:** Horizontal CPA with the AUV's velocity relative to the obstacle, w = (nsVel - obsNsVel, ewVel - obsEwVel): tcpa = (p.w)/|w|^2, horizontal miss = p - w*tcpa. cda = sqrt(miss^2 + vdist^2), taking the vertical separation as constant. Including vdist means the no-obstacle default (zero position, large vdist) gives a large cda rather than 0, which would otherwise force CAM. Limitation: when |w| = 0, Java yields NaN, so collisionCourse is false and CAM's safe exit (Beh18) is enabled. This happens when there is no dynamic obstacle and the AUV is horizontally still, or the velocities match exactly. No epsilon was added, to keep fractional literals out of the model.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/operation/CalcCPA.java` lines 32-43, element `compute`, requirement_ids: LRE-OP5, LRE-Var7, LRE-Var8

### 6. [design_choice] State variables live in the operation classes; the controller reads them through getters

- **kind:** `design_choice`
- **raw:** LRE-Var1..Var8 say 'the controller maintains' each variable; the rules allow variables to be fields of the operation that computes them.
- **fix_directive:** hvel/vvel/vel (CalcVel), inOpez (CheckOPEZ), cstc (CalcCStc), cdyn (CalcCDyn), cda/tcpa (CalcCPA) are operation fields with same-named getters. The controller owns the operations and calls compute() on each at the top of step(), before the guards. The controller's operation fields are named calcVelOp etc. so they do not collide with the class names when case is ignored.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/controller/LreController.java` lines 53-58, element `step (operation calls)`, requirement_ids: LRE-ARCH2, LRE-OP1, LRE-OP2, LRE-OP3, LRE-OP4, LRE-OP5, LRE-Var1, LRE-Var2, LRE-Var3, LRE-Var4, LRE-Var5, LRE-Var6, LRE-Var7, LRE-Var8

### 7. [design_choice] CheckOPEZ and CalcCPA select the obstacle themselves through the same sensor function

- **kind:** `design_choice`
- **raw:** LRE-OP2 says CheckOPEZ uses the index computed by CalcCStc; LRE-OP5 uses cdyn. The profile limits compute() to sensor calls and the class's own fields.
- **fix_directive:** CheckOPEZ evaluates sensor.odist(sensor.closestStaticIndex()), and CalcCPA stores sensor.closestDynamicIndex() in its own field dynamicIndex. Within a step these are the same values CalcCStc/CalcCDyn compute.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/operation/CheckOPEZ.java` lines 24-27, element `compute`, requirement_ids: LRE-OP2, LRE-Var1
  - `java.generated.project/src/main/java/lre/operation/CalcCPA.java` lines 33-33, element `dynamicIndex`, requirement_ids: LRE-OP5, LRE-Var6

### 8. [design_choice] Transition priority within MOM and HCM (all Beh requirements share priority 8)

- **kind:** `design_choice`
- **raw:** The requirements give no relative priority between transitions leaving the same mode.
- **fix_directive:** MOM: reqOCM, then inOpez, then CAM (Beh8), then HCM via Beh9, Beh11, Beh10, then endTask (Beh7), then reqHCM (Beh12). HCM: reqOCM, inOpez, CAM, then MOM (Beh13). CAM: reqOCM, then the safe exit (Beh18). Each branch below a guard-only branch conjoins its negation. Beh11 is placed before Beh10 because with staticObsVertDist <= staticObsDfltVertDist (both 1), Beh11 below Beh10 could never be enabled. With the events last, endTask/reqHCM only take effect when no autonomous safety transition is enabled. reqVel/reqHdng are ignored outside OCM (Beh19).
- **java_trace:**
  - `java.generated.project/src/main/java/lre/controller/LreController.java` lines 95-156, element `step (MOM, HCM, CAM blocks)`, requirement_ids: LRE-Beh5, LRE-Beh6, LRE-Beh7, LRE-Beh8, LRE-Beh9, LRE-Beh10, LRE-Beh11, LRE-Beh12, LRE-Beh13, LRE-Beh14, LRE-Beh15, LRE-Beh16, LRE-Beh17, LRE-Beh18, LRE-Beh19, LRE-DC1

### 9. [ambiguous_requirement] HCM advised velocity: 0 m/s (LRE-FR3) versus 0.1 m/s (system description)

- **kind:** `ambiguous_requirement`
- **raw:** system_description.txt: 'HCM: Reduced speed (0.1 m/s)'. LRE-FR3: 'On entering HCM, the LRE advises a velocity of 0 m/s'.
- **fix_directive:** Implemented advVel(0.0) on every entry to HCM, following the requirement (the only source of requirements).
- **java_trace:**
  - `java.generated.project/src/main/java/lre/controller/LreController.java` lines 105-128, element `step (MOM->HCM branches)`, requirement_ids: LRE-FR3, LRE-Beh9, LRE-Beh10, LRE-Beh11, LRE-Beh12

### 10. [ambiguous_requirement] Literal thresholds of 1 in Beh4, Beh9 and Beh11 versus the named constants

- **kind:** `ambiguous_requirement`
- **raw:** LRE-Beh4: vel <= 1, odist(cdyn) > 1, odist(cstc) > 1. LRE-Beh9/Beh11: hvel/vvel >= 1 m/s. These may be meant as minSafeDist and the MOM speed, but the text gives the number 1.
- **fix_directive:** Implemented as the literal 1.0, as written. If the distance tests in Beh4 are meant to track minSafeDist, replace 1.0 in cdynOdistAboveOne/cstcOdistAboveOne with LreConstants.MIN_SAFE_DIST. Beh11 compares the signed rate of climb (vvel >= 1), so a fast descent does not trigger it, as written.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/controller/LreController.java` lines 62-68, element `velAtMostOne / cdynOdistAboveOne / cstcOdistAboveOne / hvelAtLeastOne / vvelAtLeastOne`, requirement_ids: LRE-Beh4, LRE-Beh9, LRE-Beh11

### 11. [scope_question] CAM's evasive manoeuvre has no specified output; separation of HCM and MOM guards depends on the constants

- **kind:** `scope_question`
- **raw:** LRE-FR4: in CAM the AUV performs evasive manoeuvres, but no advVel/advHdng value is specified. LRE-Beh10 uses staticObsDfltVertDist while LRE-Beh13 uses staticObsVertDist.
- **fix_directive:** CAM entry issues no output event. The MOM->HCM and HCM->MOM guards are mutually exclusive only while staticObsDfltVertDist <= staticObsVertDist (equal with the default 1). If the defaults change so that this fails, the two guard-only transitions can alternate without an event.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/controller/LreController.java` lines 102-104, element `step (MOM->CAM)`, requirement_ids: LRE-FR4, LRE-Beh8
  - `java.generated.project/src/main/java/lre/LreConstants.java` lines 16-20, element `STATIC_OBS_VERT_DIST / STATIC_OBS_DFLT_VERT_DIST`, requirement_ids: LRE-DM4, LRE-Beh10, LRE-Beh13

## next_step

Run the pipeline (compile, coverage, profile check, transformations, FDR4, Dafny, Isabelle, vacuity) on java.generated.project.
