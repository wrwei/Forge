# post_codegen

**status:** uncertain

**summary:** All 51 requirements are implemented (15 Java files, compiled with javac --release 17). 12 points the requirements leave open are recorded below: a sentinel for 'no obstacle', two sensor defaults, the CPA formula and its regulariser, how operations obtain the closest index, the transition priority order, and four requirement ambiguities.

## Issues

### 1. "-1 means none" for cstc/cdyn conflicts with natural-number indices

- **kind:** `ambiguous_requirement`
- **raw:** LRE-DM5/OP3/OP4 say closestStaticIndex()/closestDynamicIndex() return -1 when no obstacle exists; LRE-Var5/Var6 declare cstc/cdyn as natural numbers, and the Java profile maps int to nat (never negative).
- **fix_directive:** Implemented a non-negative sentinel: ObstacleRegister.NO_OBSTACLE = 0, and obstacle identifiers start at 1 (put rejects index 0). Every sensor function treats an index with no obstacle as 'none' (large distance, zero fields), so the sentinel never needs to be tested by the controller. Ties in the closest-obstacle search go to the lowest index. Confirm or supply the intended encoding.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/sensor/ObstacleRegister.java` lines 17-30, `NO_OBSTACLE, put` (LRE-DM3, LRE-DM5, LRE-Var5, LRE-Var6)
  - `java.generated.project/src/main/java/lre/sensor/Sensor.java` lines 129-159, `closestStaticIndex, closestDynamicIndex, closestOf` (LRE-DM5, LRE-OP3, LRE-OP4)

### 2. Value of the sensor's "safe large distance"

- **kind:** `invented_default`
- **raw:** LRE-DM5: when no obstacle exists, odist, hdist and vdist return a safe large distance; no value is given.
- **fix_directive:** Sensor.NO_OBSTACLE_DIST = 1000000.0 m (far above every 1 m threshold). The same value is returned by hdist, vdist and odist.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/sensor/Sensor.java` lines 14-15, `NO_OBSTACLE_DIST` (LRE-DM5)

### 3. No-dynamic-obstacle defaults: DM5 (fields zero) vs OP5 ("large-distance defaults")

- **kind:** `ambiguous_requirement`
- **raw:** LRE-DM5 says nsRelDist/ewRelDist/obsNsVel/obsEwVel return zero with no obstacle; LRE-OP5 says the sensor returns zero-velocity and large-distance defaults. With a zero relative position the CPA would be 0 m and MOM would enter CAM whenever no dynamic obstacle exists.
- **fix_directive:** Followed DM5 for the field accessors and brought the large default into CalcCPA through the distance function vdist(cdyn), which is part of cda (see the CPA design choice). With no dynamic obstacle cda = 1000000 m, so Beh8/Beh14 cannot fire and Beh18 can.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/operation/CalcCPA.java` lines 36-50, `compute` (LRE-OP5, LRE-DM5, LRE-Var7)

### 4. CPA geometry for cda and tcpa

- **kind:** `design_choice`
- **raw:** LRE-OP5 names cda and tcpa but gives no formula; the sensor exposes horizontal relative position and horizontal velocities only.
- **fix_directive:** Horizontal CPA with closing velocity w = AUV velocity - obstacle velocity and relative position p: tcpa = (p.w)/(|w|^2 + CPA_SPEED_SQ_EPS); miss = p - w*tcpa; cda = sqrt(|miss|^2 + vdist(cdyn)^2), i.e. the vertical separation is taken as constant over the encounter (no obstacle vertical-rate accessor is specified).
- **java_trace:**
  - `java.generated.project/src/main/java/lre/operation/CalcCPA.java` lines 36-50, `compute` (LRE-OP5, LRE-Var7, LRE-Var8)

### 5. CPA_SPEED_SQ_EPS regulariser (0.000001 m^2/s^2)

- **kind:** `invented_default`
- **raw:** With zero relative velocity the CPA formula divides by zero (Java NaN would block Beh18). Operation bodies cannot branch, so a small constant is added to the denominator.
- **fix_directive:** LreConstants.CPA_SPEED_SQ_EPS = 0.000001. It does not change the sign of tcpa (Beh8/Beh14 'tcpa >= 0'), and changes cda negligibly for real closing speeds. The model's constants interface rounds it up to 1, so in the model cda and tcpa differ in magnitude from the Java when the closing speed is small; this matters to the guard 'cda < minSafeDist' only in value, not in structure.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/controller/LreConstants.java` lines 47-51, `CPA_SPEED_SQ_EPS` (LRE-OP5)

### 6. CheckOPEZ and CalcCPA re-query the closest index from the sensor

- **kind:** `design_choice`
- **raw:** LRE-OP2 says CheckOPEZ uses the index computed by CalcCStc; LRE-OP5 says CalcCPA uses cdyn. The profile lets compute() use only sensor calls and fields of its own class.
- **fix_directive:** CheckOPEZ calls sensor.closestStaticIndex() and CalcCPA calls sensor.closestDynamicIndex(): the same deterministic query CalcCStc/CalcCDyn make in the same step, so the index is identical. step() computes CalcCStc before CheckOPEZ and CalcCDyn before CalcCPA.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/operation/CheckOPEZ.java` lines 22-26, `compute` (LRE-OP2, LRE-OP3)
  - `java.generated.project/src/main/java/lre/operation/CalcCPA.java` lines 36-38, `compute` (LRE-OP5, LRE-OP4)
  - `java.generated.project/src/main/java/lre/controller/LreController.java` lines 51-56, `step` (LRE-OP1, LRE-OP2, LRE-OP3, LRE-OP4, LRE-OP5)

### 7. Priority order of transitions within a mode

- **kind:** `design_choice`
- **raw:** All transition requirements have priority 8; the code needs an order, and lower predicate-only branches must negate the guards above them.
- **fix_directive:** reqOCM (operator override) first in MOM, HCM and CAM. MOM: reqOCM, endTask, inOpez, collision (CAM), Beh9, Beh11, Beh10, reqHCM. Beh11 is placed before Beh10 because, with equal vertical thresholds, Beh11's guard implies Beh10's and would otherwise be unsatisfiable after negation. endTask precedes inOpez so that endTask always advises 0 m/s. reqHCM is last so that a collision risk or a nearby static obstacle is never masked by an operator request. HCM: reqOCM, inOpez, collision (CAM), Beh13. CAM: reqOCM, Beh18. step(null) means no operator input this cycle.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/controller/LreController.java` lines 94-126, `step (MOM block)` (LRE-Beh5, LRE-Beh6, LRE-Beh7, LRE-Beh8, LRE-Beh9, LRE-Beh10, LRE-Beh11, LRE-Beh12, LRE-DC1)
  - `java.generated.project/src/main/java/lre/controller/LreController.java` lines 128-154, `step (HCM, CAM blocks)` (LRE-Beh13, LRE-Beh14, LRE-Beh15, LRE-Beh16, LRE-Beh17, LRE-Beh18)

### 8. HCM velocity: 0.1 m/s (system description) vs 0 m/s (LRE-FR3)

- **kind:** `ambiguous_requirement`
- **raw:** The system description gives HCM a reduced speed of 0.1 m/s; LRE-FR3 says the LRE advises 0 m/s on entering HCM.
- **fix_directive:** Followed the requirement: HCM_ADVISED_VEL = 0.0, advised on every transition into HCM.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/controller/LreConstants.java` lines 26-27, `HCM_ADVISED_VEL` (LRE-FR3)

### 9. Sign of vvel in LRE-Beh11

- **kind:** `ambiguous_requirement`
- **raw:** LRE-OP1 defines vvel as the rate of climb (signed); LRE-Beh11 tests 'vvel at or above 1 m/s'.
- **fix_directive:** Implemented literally: vvel = rateOfClimb() and the guard vvel >= 1.0, so a fast descent does not trigger Beh11 (Beh10 still applies within the default vertical distance). If |vvel| was intended, change CalcVel.vvel or the predicate.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/operation/CalcVel.java` lines 22-26, `compute` (LRE-OP1, LRE-Var3)
  - `java.generated.project/src/main/java/lre/controller/LreController.java` lines 68-73, `vvelAtOrAboveLimit, fastVertNearStatic` (LRE-Beh11)

### 10. CAM evasive manoeuvre has no specified output

- **kind:** `scope_question`
- **raw:** LRE-FR4 says the AUV performs evasive manoeuvres in CAM, but no requirement gives the advice issued, and LRE-DM7 restricts outputs to advVel and advHdng.
- **fix_directive:** No advice is issued on entering CAM; the advice in force is kept. Specify the CAM velocity and/or heading if one is intended.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/controller/LreController.java` lines 105-107, `step (MOM->CAM)` (LRE-FR4, LRE-Beh8)
  - `java.generated.project/src/main/java/lre/controller/LreController.java` lines 134-136, `step (HCM->CAM)` (LRE-FR4, LRE-Beh14)

### 11. Literal thresholds in LRE-Beh4 and the effect of reqHCM

- **kind:** `ambiguous_requirement`
- **raw:** LRE-Beh4 uses the literal 1 (vel <= 1, odist > 1) rather than minSafeDist. LRE-Beh12 enters HCM on request, and LRE-Beh13 returns to MOM as soon as the closest static obstacle is beyond both thresholds.
- **fix_directive:** Introduced MOM_ENTRY_MAX_VEL and MOM_ENTRY_MIN_OBS_DIST (both 1.0); with these values the odist(cstc) > 1 conjunct is implied by !inOpez, but it is kept as stated. A reqHCM with no static obstacle nearby lasts one cycle (HCM then MOM), as the requirements imply.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/controller/LreConstants.java` lines 32-36, `MOM_ENTRY_MAX_VEL, MOM_ENTRY_MIN_OBS_DIST` (LRE-Beh4)
  - `java.generated.project/src/main/java/lre/controller/LreController.java` lines 137-141, `step (HCM->MOM)` (LRE-Beh12, LRE-Beh13)

### 12. Actuator values before the first advice

- **kind:** `invented_default`
- **raw:** LRE-DM8: the actuator stores the latest advised values; none exist before the first output.
- **fix_directive:** Both advisedVelocity and advisedHeading start at 0.0.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/actuator/Actuator.java` lines 12-13, `advisedVelocity, advisedHeading` (LRE-DM8)

## next_step

Run the pipeline (compile, coverage, profile check, transformations, verifiers) and fix any reported failure in the Java.
