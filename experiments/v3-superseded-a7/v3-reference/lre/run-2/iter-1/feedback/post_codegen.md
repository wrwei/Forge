# post_codegen

**status:** uncertain

**summary:** All 51 requirements are implemented in 15 Java files under src/main/java/lre and traced in result_codegen.json. 13 points the requirements did not settle are recorded below (invented defaults, design choices, ambiguities, one scope question); none is unimplemented.

## Issues

### 1. [ambiguous_requirement] cstc/cdyn use -1 for 'no obstacle', but integers are natural numbers in the model

- **raw:** LRE-Var5/Var6 and LRE-OP3/OP4 say -1 means no static/dynamic obstacle, while the same requirements call cstc and cdyn natural numbers and the Java profile forbids negative integers.
- **fix_directive:** Index 0 denotes 'no obstacle': ObstacleRegister rejects identifiers below 1, and closestStaticIndex()/closestDynamicIndex() return 0 when there is no candidate. The sensor's distance functions and field accessors treat 0 like any absent index (large distance, zero fields). Confirm that 1-based obstacle identifiers are acceptable.
- **java_trace:**
  - `src/main/java/lre/sensor/ObstacleRegister.java` lines 18-31, `NO_OBSTACLE` (LRE-DM3, LRE-Var5, LRE-Var6)
  - `src/main/java/lre/sensor/Sensor.java` lines 131-155, `closestStaticIndex` (LRE-DM5, LRE-OP3, LRE-OP4)

### 2. [invented_default] Value of the 'safe large distance' for an absent obstacle

- **raw:** LRE-DM5 says odist, hdist and vdist return a safe large distance when no obstacle exists, without a value.
- **fix_directive:** Sensor.SAFE_LARGE_DISTANCE = 100000.0 m, far above every threshold (all 1 m). Replace if the platform defines a sensor range.
- **java_trace:**
  - `src/main/java/lre/sensor/Sensor.java` lines 15-16, `SAFE_LARGE_DISTANCE` (LRE-DM5, LRE-SF1, LRE-SF2, LRE-SF3)

### 3. [invented_default] CPA_EPSILON regularises the tcpa division

- **raw:** LRE-OP5 does not say what tcpa is when the AUV and the obstacle do not close on each other (zero closing speed), where the textbook formula divides by zero.
- **fix_directive:** tcpa = (p.w) / (|w|^2 + CPA_EPSILON) with CPA_EPSILON = 0.000001 (m/s)^2, so tcpa = 0 and cda = current distance when nothing closes. The model's constants interface rounds CPA_EPSILON up to 1; because the denominator stays positive, the sign of tcpa (all the guards test) is the same in the model and in Java, and only the magnitude of tcpa and cda differs.
- **java_trace:**
  - `src/main/java/lre/LreConstants.java` lines 21-26, `CPA_EPSILON` (LRE-OP5)
  - `src/main/java/lre/operation/CalcCPA.java` lines 43-46, `compute` (LRE-OP5, LRE-Var8)

### 4. [design_choice] How CalcCPA computes cda and tcpa

- **raw:** LRE-OP5 names cda and tcpa but gives no formula. LRE-DM5 makes the field accessors return zero for an absent obstacle, while LRE-OP5 speaks of 'large-distance defaults'; a plain 2-D CPA on zero relative position gives cda = 0, i.e. a phantom collision whenever there is no dynamic obstacle.
- **fix_directive:** Relative position = (ns_rel_dist, ew_rel_dist), taken as obstacle minus AUV; closing velocity w = AUV horizontal velocity minus obstacle velocity; tcpa minimises the horizontal separation (negative once the obstacle has passed). cda is the separation at max(tcpa, 0) with the current vertical separation vdist added (constant vertical separation: no obs_roc accessor exists in LRE-DM5). With no dynamic obstacle vdist is the safe large distance, so cda is large and MOM/HCM never go to CAM spuriously; clamping at 0 lets CAM end (LRE-Beh18) once the obstacle has passed. Confirm the sign convention of ns_rel_dist/ew_rel_dist.
- **java_trace:**
  - `src/main/java/lre/operation/CalcCPA.java` lines 8-52, `CalcCPA` (LRE-OP5, LRE-Var7, LRE-Var8, LRE-Beh8, LRE-Beh14, LRE-Beh18)

### 5. [ambiguous_requirement] HCM advised velocity: 0 m/s (LRE-FR3) or 0.1 m/s (system description)

- **raw:** The system description says HCM has 'reduced speed (0.1 m/s)'; LRE-FR3 says on entering HCM the LRE advises 0 m/s.
- **fix_directive:** Implemented LRE-FR3 (advVel(0.0) on every transition into HCM), since the requirements file is the source of requirements.
- **java_trace:**
  - `src/main/java/lre/controller/LreController.java` lines 115-134, `step` (LRE-FR3, LRE-Beh9, LRE-Beh10, LRE-Beh11, LRE-Beh12)

### 6. [design_choice] Priority of the transitions out of each mode

- **raw:** All LRE-Beh requirements have the same priority, and several can be enabled together (e.g. inOpez and a collision risk in MOM).
- **fix_directive:** In MOM, HCM and CAM the operator's reqOCM comes first (an override shared by three modes), then inOpez -> OCM, then collision risk -> CAM, then endTask and reqHCM, then the autonomous HCM entries in the order Beh9, Beh11, Beh10. Each branch below a guard-only branch conjoins the negation of that guard. Beh11 precedes Beh10 because STATIC_OBS_VERT_DIST equals STATIC_OBS_DFLT_VERT_DIST: under the opposite order, Beh11's guard conjoined with not(Beh10) would be unsatisfiable. If OPEZ should not override collision avoidance, swap the two branches in MOM and HCM.
- **java_trace:**
  - `src/main/java/lre/controller/LreController.java` lines 97-165, `step` (LRE-Beh5, LRE-Beh6, LRE-Beh7, LRE-Beh8, LRE-Beh9, LRE-Beh10, LRE-Beh11, LRE-Beh12, LRE-Beh13, LRE-Beh14, LRE-Beh15, LRE-Beh16, LRE-Beh17, LRE-Beh18, LRE-DC1)

### 7. [design_choice] Where the state variables live, and how CheckOPEZ and CalcCPA get the closest indices

- **raw:** LRE-Var1..Var8 say 'the controller maintains' each variable and that the operations set them; LRE-OP2/OP5 say CheckOPEZ and CalcCPA use the index computed by CalcCStc/CalcCDyn.
- **fix_directive:** Each variable is a field of the operation that computes it, exposed by a getter of the same name; the controller runs the five compute() calls at the start of step() and its guard predicates read the getters. CheckOPEZ and CalcCPA read the closest index from the sensor themselves (closestStaticIndex()/closestDynamicIndex(), the same call CalcCStc/CalcCDyn make on the same sensor state), because compute() may only use sensor calls and fields of its own class.
- **java_trace:**
  - `src/main/java/lre/controller/LreController.java` lines 52-76, `step` (LRE-ARCH2, LRE-OP1, LRE-OP2, LRE-OP3, LRE-OP4, LRE-OP5, LRE-GP1)
  - `src/main/java/lre/operation/CheckOPEZ.java` lines 26-29, `compute` (LRE-OP2, LRE-Var1)
  - `src/main/java/lre/operation/CalcCPA.java` lines 39-40, `compute` (LRE-OP5)

### 8. [ambiguous_requirement] vvel is the signed rate of climb

- **raw:** LRE-OP1 defines vvel as the rate of climb; LRE-Beh11 tests vvel >= 1 m/s. A fast descent towards a static obstacle below therefore never satisfies Beh11.
- **fix_directive:** Implemented literally (vvel = rateOfClimb()); Beh10 still moves MOM to HCM near a static obstacle regardless of velocity. If the magnitude was meant, use the absolute rate of climb in CalcVel.
- **java_trace:**
  - `src/main/java/lre/operation/CalcVel.java` lines 22-26, `compute` (LRE-OP1, LRE-Var3, LRE-Beh11)

### 9. [scope_question] CAM issues no advice of its own

- **raw:** LRE-FR4 says the AUV performs evasive manoeuvres in CAM but fixes no output; LRE-DM7 allows only advVel and advHdng.
- **fix_directive:** CAM has no entry action and, as LRE-Beh19 requires outside OCM, ignores reqVel/reqHdng. The evasive manoeuvre is left to the autopilot. Add an entry action if a specific advice is required.
- **java_trace:**
  - `src/main/java/lre/controller/LreController.java` lines 155-165, `step` (LRE-FR4, LRE-Beh19)

### 10. [design_choice] Literal thresholds outside LRE-DM4

- **raw:** LRE-Beh4 (vel <= 1, odist > 1), LRE-Beh9/Beh11 (>= 1 m/s), LRE-FR2 (1 m/s), LRE-FR3/Beh7/Beh18 (0 m/s) give numbers that LRE-DM4 does not name as constants.
- **fix_directive:** Written as literals (1.0, 0.0) in the guard predicates and outputs; only the four DM4 thresholds (and CPA_EPSILON) are named constants. Beh4's distance threshold 1 is kept literal rather than tied to MIN_SAFE_DIST.
- **java_trace:**
  - `src/main/java/lre/controller/LreController.java` lines 61-76, `step` (LRE-Beh4, LRE-Beh9, LRE-Beh11, LRE-FR2, LRE-FR3)

### 11. [design_choice] No input in a cycle, and one transition per cycle

- **raw:** The requirements do not say how a cycle without an operator input is presented to the controller.
- **fix_directive:** step(null) is a cycle without input: only the autonomous transitions can fire. Each step takes at most one transition.
- **java_trace:**
  - `src/main/java/lre/controller/LreController.java` lines 46-52, `step` (LRE-ARCH2)

### 12. [invented_default] Minor defaults: tie-breaking and initial actuator values

- **raw:** LRE-OP3/OP4 do not say which obstacle is closest when two are equidistant; LRE-DM8 does not give the actuator's values before the first advice.
- **fix_directive:** The lowest index wins a tie; the actuator reports 0.0 velocity and heading before any advice.
- **java_trace:**
  - `src/main/java/lre/sensor/Sensor.java` lines 143-155, `closestOf` (LRE-OP3, LRE-OP4)
  - `src/main/java/lre/actuator/Actuator.java` lines 12-13, `Actuator` (LRE-DM8)

### 13. [design_choice] Obstacle record keeps the requirement's snake_case field names

- **raw:** LRE-DM2 names the fields ns_rel_dist, ew_rel_dist, obs_depth, obs_ns_vel, obs_ew_vel, obs_roc; LRE-DM5 names the sensor accessors nsRelDist, ewRelDist, obsNsVel, obsEwVel.
- **fix_directive:** The record components use the snake_case names verbatim, so that no datatype field shares a name (case-insensitively) with a sensor function in the model.
- **java_trace:**
  - `src/main/java/lre/sensor/Obstacle.java` lines 5-21, `Obstacle` (LRE-DM2)

## next_step

Run the pipeline (compile, coverage, profile check, transformations, FDR4, Dafny, Isabelle, vacuity) and fix the Java from the reports.
