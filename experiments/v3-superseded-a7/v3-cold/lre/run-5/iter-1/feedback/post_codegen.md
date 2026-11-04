# post_codegen

**status:** uncertain

**summary:** All 51 LRE requirements are implemented in 15 Java files under java.generated.project/src/main/java/lre and traced in result_codegen.json; 15 points need confirmation: 3 invented defaults, 5 ambiguous requirements, 6 design choices, 1 scope question.

## Issues

### 1. [ambiguous_requirement] "No obstacle" index: the requirements say -1, the profile makes integers natural numbers

- **raw:** LRE-DM5/OP3/OP4: closestStaticIndex()/closestDynamicIndex() return -1 if none exists; LRE-Var5/Var6: cstc/cdyn are natural number variables, value -1 means none. Profile: int becomes nat, never use a negative number to mean none.
- **fix_directive:** Resolved with ObstacleRegister.NO_OBSTACLE = 2147483647 (Integer.MAX_VALUE), a natural-number index the register refuses to store; the sensor returns it when no static/dynamic obstacle exists and treats it like any absent index (safe large distance, zero fields). Ties on odist go to the lowest index. Confirm, or name the sentinel the requirements intend.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/sensor/ObstacleRegister.java` lines 16-20, `NO_OBSTACLE`, requirements LRE-DM3, LRE-Var5, LRE-Var6
  - `java.generated.project/src/main/java/lre/sensor/Sensor.java` lines 131-157, `closestStaticIndex/closestDynamicIndex`, requirements LRE-DM5, LRE-OP3, LRE-OP4

### 2. [invented_default] Value of the "safe large distance" returned when no obstacle exists

- **raw:** LRE-DM5: when no obstacle exists, odist, hdist and vdist return a safe large distance (value not given).
- **fix_directive:** Used 1000000.0 m (Sensor.NO_OBSTACLE_DIST, private), far above every 1 m threshold. Supply the intended value if it differs.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/sensor/Sensor.java` lines 12-13, `NO_OBSTACLE_DIST`, requirements LRE-DM5, LRE-SF1, LRE-SF2, LRE-SF3, LRE-OP2, LRE-OP5

### 3. [invented_default] CPA_EPSILON regularises the tcpa division; fractional, so rounded up to 1 in the model

- **raw:** LRE-OP5 does not say what tcpa is when the AUV and the obstacle have the same velocity (closing speed 0, division by zero).
- **fix_directive:** Added LreConstants.CPA_EPSILON = 0.000001 (m/s)^2 to the squared closing speed, so tcpa = 0 and cda = current horizontal distance at zero closing speed. The model's constants interface rounds it up to 1, which changes cda/tcpa values in the model but not which guards are reachable. Confirm or supply the intended treatment.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/LreConstants.java` lines 40-43, `CPA_EPSILON`, requirements LRE-OP5
  - `java.generated.project/src/main/java/lre/operation/CalcCPA.java` lines 34-44, `compute`, requirements LRE-OP5, LRE-Var7, LRE-Var8

### 4. [design_choice] CPA formulation: horizontal plane, closing velocity, |p| taken from hdist

- **raw:** LRE-OP5 names cda and tcpa and the inputs (relative position and velocity from the Sensor) but no formula.
- **fix_directive:** With p = (nsRelDist, ewRelDist) of the closest dynamic obstacle (assumed obstacle minus AUV) and w = AUV velocity minus obstacle velocity: tcpa = p.w / (|w|^2 + e), cda = sqrt(hdist^2 - tcpa^2 (|w|^2 + e)). Vertical motion is ignored (the Sensor exposes no obstacle depth or rate-of-climb accessor). |p| comes from hdist so that with no dynamic obstacle (p = 0) cda is the safe large distance and CAM is not entered. Confirm the sign convention of ns_rel_dist/ew_rel_dist.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/operation/CalcCPA.java` lines 34-44, `compute`, requirements LRE-OP5, LRE-Var7, LRE-Var8, LRE-DM5

### 5. [design_choice] CheckOPEZ and CalcCPA obtain the closest index from the Sensor, not from CalcCStc/CalcCDyn

- **raw:** LRE-OP2: CheckOPEZ uses the closest-static-obstacle index computed by CalcCStc; LRE-OP5: CalcCPA uses cdyn. Profile: compute() may use only sensor calls, Math.sqrt, arithmetic and its own fields.
- **fix_directive:** Both call sensor.closestStaticIndex()/closestDynamicIndex() inside compute(), the same call CalcCStc/CalcCDyn make, so the value is identical within a step. Accept, or say how operations should share results.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/operation/CheckOPEZ.java` lines 22-25, `compute`, requirements LRE-OP2, LRE-OP3
  - `java.generated.project/src/main/java/lre/operation/CalcCPA.java` lines 34-44, `compute`, requirements LRE-OP5, LRE-OP4

### 6. [design_choice] Variables Var1-Var8 are fields of the operations that compute them

- **raw:** LRE-Var1..Var8: the controller maintains inOpez, hvel, vvel, vel, cstc, cdyn, cda, tcpa, each set by an operation.
- **fix_directive:** Each variable is a field of its operation with a getter of the same name; step() calls the five compute() methods first and reads the getters in its predicates. They are not duplicated as controller fields, to keep model names unique. The predicate required by LRE-GP1 is named inOpez.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/controller/LreController.java` lines 51-72, `step`, requirements LRE-Var1, LRE-Var2, LRE-Var3, LRE-Var4, LRE-Var5, LRE-Var6, LRE-Var7, LRE-Var8, LRE-GP1

### 7. [ambiguous_requirement] HCM advised velocity: 0 m/s (LRE-FR3) vs 0.1 m/s (system description)

- **raw:** LRE-FR3: on entering HCM, the LRE advises a velocity of 0 m/s. System description: HCM, reduced speed (0.1 m/s).
- **fix_directive:** Followed the requirement: HCM_ADV_VEL = 0.0, advised on every transition into HCM. Change the constant if 0.1 m/s is intended (0.1 would round up to 1 in the model).
- **java_trace:**
  - `java.generated.project/src/main/java/lre/LreConstants.java` lines 23-24, `HCM_ADV_VEL`, requirements LRE-FR3
  - `java.generated.project/src/main/java/lre/controller/LreController.java` lines 101-117, `step`, requirements LRE-FR3, LRE-Beh9, LRE-Beh10, LRE-Beh11, LRE-Beh12

### 8. [ambiguous_requirement] vvel is the signed rate of climb, so LRE-Beh11 fires only when climbing

- **raw:** LRE-OP1: vvel is the rate of climb. LRE-Beh11: MOM to HCM when vvel is at or above 1 m/s and vdist(cstc) <= staticObsVertDist.
- **fix_directive:** Implemented literally (vvel = rateOfClimb): a fast descent towards a static obstacle below does not trigger Beh11 (Beh10 still applies regardless of velocity). Use the magnitude instead if a vertical speed was meant.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/operation/CalcVel.java` lines 22-26, `compute`, requirements LRE-OP1, LRE-Var3
  - `java.generated.project/src/main/java/lre/controller/LreController.java` lines 104-108, `step`, requirements LRE-Beh11

### 9. [scope_question] CAM evasive manoeuvre is not specified

- **raw:** LRE-FR4: in CAM the AUV performs evasive manoeuvres. No requirement gives the advised velocity or heading in CAM; LRE-DM7 allows only advVel and advHdng.
- **fix_directive:** Nothing is advised on entering CAM; CAM only leaves on reqOCM (Beh17) or cda >= minSafeDist (Beh18). Specify the manoeuvre (as advVel/advHdng values) if one is expected.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/controller/LreController.java` lines 96-97, `step`, requirements LRE-FR4, LRE-Beh8
  - `java.generated.project/src/main/java/lre/controller/LreController.java` lines 125-126, `step`, requirements LRE-FR4, LRE-Beh14
  - `java.generated.project/src/main/java/lre/controller/LreController.java` lines 133-139, `step`, requirements LRE-FR4, LRE-Beh17, LRE-Beh18

### 10. [design_choice] Transition priority within MOM and HCM

- **raw:** No requirement orders the transitions of a mode; LRE-DC1 requires one transition per Beh requirement.
- **fix_directive:** Order: reqOCM (operator override), inOpez, collision course, endTask, Beh9, Beh11, Beh10, reqHCM in MOM; reqOCM, inOpez, collision, Beh13 in HCM. Lower branches conjoin the negation of higher guard-only branches, so endTask/reqHCM/HCM entries are not taken while inOpez or a collision is predicted. Beh11 precedes Beh10 because, with equal vertical thresholds, Beh11 placed after Beh10 would have an unsatisfiable guard (a dead transition). Reorder if a different priority is intended.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/controller/LreController.java` lines 90-131, `step`, requirements LRE-Beh5, LRE-Beh6, LRE-Beh7, LRE-Beh8, LRE-Beh9, LRE-Beh10, LRE-Beh11, LRE-Beh12, LRE-Beh13, LRE-Beh14, LRE-Beh15, LRE-Beh16, LRE-DC1

### 11. [ambiguous_requirement] LRE-Beh13 ignores staticObsDfltVertDist: MOM and HCM could oscillate if the thresholds differ

- **raw:** LRE-Beh10: MOM to HCM when vdist(cstc) <= staticObsDfltVertDist. LRE-Beh13: HCM to MOM when hdist(cstc) > staticObsHorizDist and vdist(cstc) > staticObsVertDist.
- **fix_directive:** Implemented literally. With the default thresholds (all 1.0) the MOM-to-HCM and HCM-to-MOM guards exclude each other; if staticObsDfltVertDist were set above staticObsVertDist, an autonomous MOM/HCM cycle would be possible. Confirm the thresholds stay equal or add the default threshold to Beh13.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/controller/LreController.java` lines 109-112, `step`, requirements LRE-Beh10
  - `java.generated.project/src/main/java/lre/controller/LreController.java` lines 127-131, `step`, requirements LRE-Beh13, LRE-DM4

### 12. [ambiguous_requirement] LRE-Beh18 does not consider tcpa: CAM persists after a close pass

- **raw:** LRE-Beh18: CAM to OCM when cda >= minSafeDist.
- **fix_directive:** Implemented literally. For an obstacle on a straight course that has already passed within minSafeDist (tcpa < 0), cda stays at that past minimum, so CAM is left only by reqOCM. Confirm, or add a tcpa < 0 exit.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/controller/LreController.java` lines 136-139, `step`, requirements LRE-Beh18, LRE-OP5

### 13. [design_choice] Operator inputs outside their modes are ignored

- **raw:** LRE-Beh19: OCM handles exactly reqVel, reqHdng and reqMOM; in all other modes operator velocity and heading inputs are overridden by the LRE.
- **fix_directive:** No branch handles reqVel/reqHdng in MOM, HCM or CAM, or reqOCM/reqHCM/endTask in OCM; such inputs leave the mode and the outputs unchanged (in the model they are not accepted in those modes).
- **java_trace:**
  - `java.generated.project/src/main/java/lre/controller/LreController.java` lines 75-88, `step`, requirements LRE-Beh19, LRE-Beh2, LRE-Beh3, LRE-Beh4

### 14. [design_choice] Numeric values in transition requirements are named constants

- **raw:** LRE-FR2 (1 m/s), LRE-FR3 (0 m/s), LRE-Beh4 (vel <= 1, odist > 1), LRE-Beh7/Beh18 (advVel(0)), LRE-Beh9/Beh11 (1 m/s).
- **fix_directive:** Each value is its own LreConstants field (MOM_ADV_VEL, HCM_ADV_VEL, STOP_ADV_VEL, MOM_ENTRY_MAX_VEL, MOM_ENTRY_MIN_OBS_DIST, HIGH_HORIZ_VEL, HIGH_VERT_VEL); Beh4's distance 1 is not tied to minSafeDist. Sign tests (depth <= 0, tcpa >= 0) use the literal 0.0. All are whole numbers, so none is changed by the model's rounding.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/LreConstants.java` lines 20-38, `LreConstants`, requirements LRE-FR2, LRE-FR3, LRE-Beh4, LRE-Beh7, LRE-Beh9, LRE-Beh11, LRE-Beh18

### 15. [invented_default] Actuator values before the first advice

- **raw:** LRE-DM8: the Actuator stores the latest advised velocity and heading; no initial value is given.
- **fix_directive:** Both start at 0.0 until the LRE issues advVel/advHdng. Supply initial values if the autopilot needs others.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/actuator/Actuator.java` lines 12-13, `advisedVelocity/advisedHeading`, requirements LRE-DM8

## next_step

Run the pipeline (compile, coverage, profile check, transformations, verifiers). The flagged resolutions stand unless the operator overrides them.
