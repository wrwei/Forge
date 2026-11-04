# post_codegen

**status:** uncertain

**summary:** 15 Java files under lre.* implement all 51 LRE requirements (one controller with 4 modes and 18 transition branches, 5 operations, sensor, actuator, events, constants); result_codegen.json traces every requirement and every public element. 13 points the requirements leave open are flagged: 6 ambiguous requirements, 3 design choices, 3 invented defaults, 1 scope question. None blocks compilation or the profile as written.

## Issues

### 1. [ambiguous_requirement] "-1 means no obstacle" conflicts with the natural-number type of cstc/cdyn

**raw:** LRE-Var5 and LRE-Var6 declare cstc and cdyn as natural numbers but say -1 means no obstacle; LRE-OP3, LRE-OP4 and LRE-DM5 say closestStaticIndex()/closestDynamicIndex() return -1 when none exists. The Java profile maps int to nat and forbids negative values and negative 'none' sentinels.

**fix_directive:** Resolved as: index 0 (ObstacleRegister.NO_OBSTACLE) means 'no obstacle'. The register rejects identifiers below 1, so 0 never names an obstacle and every sensor function reads it as absent (large distance, zero fields). Confirm, or state the intended sentinel.

**java_trace:**

- `src/main/java/lre/sensor/ObstacleRegister.java` lines 20-33, `NO_OBSTACLE`; requirement_ids: LRE-Var5, LRE-Var6, LRE-DM3
- `src/main/java/lre/sensor/Sensor.java` lines 137-164, `closestStaticIndex`; requirement_ids: LRE-OP3, LRE-OP4, LRE-DM5

### 2. [ambiguous_requirement] No-dynamic-obstacle defaults: zero relative position (DM5) vs large-distance default (OP5)

**raw:** LRE-DM5 has nsRelDist/ewRelDist return zero when there is no obstacle; LRE-OP5 says the sensor yields 'zero-velocity and large-distance defaults' for CalcCPA. With a zero relative position the textbook cda = |p + v*tcpa| is 0, which would send MOM/HCM to CAM when there is no dynamic obstacle at all.

**fix_directive:** Resolved by computing cda in the equivalent form sqrt(odist^2 - tcpa^2*|w|^2), so the large odist default governs: with no dynamic obstacle cda = 100000 and tcpa = 0, and neither MOM->CAM nor HCM->CAM fires. Confirm.

**java_trace:**

- `src/main/java/lre/operation/CalcCPA.java` lines 36-44, `compute`; requirement_ids: LRE-OP5, LRE-Var7, LRE-Var8
- `src/main/java/lre/sensor/Sensor.java` lines 87-130, `odist`; requirement_ids: LRE-DM5, LRE-SF3

### 3. [design_choice] CPA geometry and sign conventions

**raw:** The requirements give neither the CPA formula, nor the sign convention of ns_rel_dist/ew_rel_dist, nor whether obstacle velocities are absolute. Chosen: relative distances are obstacle minus AUV; obstacle velocities are absolute (consistent with 'static iff its velocities are zero'); relative motion is horizontal (the sensor exposes no obs_roc or signed vertical offset) with the vertical separation held constant, so cda is an overall distance comparable with minSafeDist; tcpa = (p.w)/|w|^2 with w the AUV's velocity relative to the obstacle. cda is the unclamped closest distance on the relative track (standard DCPA): a receding obstacle whose track passed within minSafeDist keeps the LRE in CAM until the relative motion changes or the operator sends reqOCM.

**fix_directive:** Confirm the conventions, or specify the CPA formula (and whether cda should be clamped to the current distance when tcpa < 0).

**java_trace:**

- `src/main/java/lre/operation/CalcCPA.java` lines 36-44, `compute`; requirement_ids: LRE-OP5, LRE-DM2, LRE-Var7, LRE-Var8, LRE-Beh18

### 4. [invented_default] CPA_EPSILON = 0.000001 avoids division by zero; the model rounds it up to 1

**raw:** compute() may contain only assignments, so the zero-relative-velocity case cannot be special-cased; 0.000001 m^2/s^2 is added to |w|^2. The model's constants interface rounds a fractional constant up, so CPA_EPSILON is 1 in the model: tcpa keeps its sign (the guard tcpa >= 0 is unaffected) and cda stays well defined, but the model's cda and tcpa values differ numerically from the Java's.

**fix_directive:** Confirm the epsilon, or supply a conditional-free formulation that the model can express exactly.

**java_trace:**

- `src/main/java/lre/LreConstants.java` lines 41-42, `CPA_EPSILON`; requirement_ids: LRE-OP5

### 5. [invented_default] The 'safe large distance' is 100000.0 m

**raw:** LRE-DM5 asks for a safe large distance when no obstacle exists without giving a value. Sensor.NO_OBSTACLE_DISTANCE = 100000.0 (private to the sensor, not model-visible).

**fix_directive:** Confirm the value, or give the sensing range to use.

**java_trace:**

- `src/main/java/lre/sensor/Sensor.java` lines 16-18, `NO_OBSTACLE_DISTANCE`; requirement_ids: LRE-DM5, LRE-OP2, LRE-OP5

### 6. [ambiguous_requirement] HCM velocity: 0.1 m/s (system description) vs 0 m/s (LRE-FR3)

**raw:** The system description says HCM runs at reduced speed 0.1 m/s; LRE-FR3 says the LRE advises 0 m/s on entering HCM. The requirements were followed: HCM_VEL = 0.0.

**fix_directive:** Confirm 0 m/s. If 0.1 is intended, note that the model would round it up to 1.

**java_trace:**

- `src/main/java/lre/LreConstants.java` lines 26-27, `HCM_VEL`; requirement_ids: LRE-FR3, LRE-Beh9, LRE-Beh10, LRE-Beh11, LRE-Beh12

### 7. [ambiguous_requirement] The CAM evasive manoeuvre is unspecified

**raw:** LRE-FR4 says the AUV performs evasive manoeuvres in CAM, but no requirement gives the velocity or heading to advise, and LRE-DM7 restricts the outputs to advVel/advHdng. Entering CAM issues no output (the last advice stands).

**fix_directive:** Specify CAM's advice (entry action or per-cycle output) if one is intended.

**java_trace:**

- `src/main/java/lre/controller/LreController.java` lines 100-102, `step`; requirement_ids: LRE-FR4, LRE-Beh8, LRE-Beh14, LRE-DM7

### 8. [design_choice] Priority among transitions that can be enabled together

**raw:** All transition requirements share priority 8. Order chosen. MOM: reqOCM, collision (->CAM), inOpez (->OCM), endTask, reqHCM, Beh9, Beh11, Beh10. HCM: reqOCM, collision, inOpez, Beh13. CAM: reqOCM, cda >= minSafeDist. Rationale: reqOCM is the operator override (accepted even in CAM, per Beh17); CAM ranks above the OPEZ handover because CAM has no OPEZ exit; operator events come before autonomous HCM entries. Beh11 precedes Beh10 because, with the default thresholds (both 1.0), Beh11 placed after Beh10 could never be enabled. Lower branches conjoin the negations of earlier guard-only branches, so endTask and reqHCM are refused while OPEZ or a collision risk holds.

**fix_directive:** Confirm the order, or give explicit priorities.

**java_trace:**

- `src/main/java/lre/controller/LreController.java` lines 96-143, `step`; requirement_ids: LRE-Beh5, LRE-Beh6, LRE-Beh7, LRE-Beh8, LRE-Beh9, LRE-Beh10, LRE-Beh11, LRE-Beh12, LRE-Beh13, LRE-Beh14, LRE-Beh15, LRE-Beh16

### 9. [design_choice] CheckOPEZ and CalcCPA re-read the closest-obstacle index from the sensor

**raw:** LRE-OP2 says CheckOPEZ uses the cstc computed by CalcCStc, and LRE-OP5 says CalcCPA uses cdyn. The profile gives an operation only its sensors and constants, so CheckOPEZ and CalcCPA call sensor.closestStaticIndex()/closestDynamicIndex() into private fields (opezTarget, cpaTarget). It is the same function on the same readings within one step, so the value equals cstc/cdyn. The variables LRE-Var1..Var8 live in the operation classes and the controller reads them through getters; they are not duplicated as controller fields.

**fix_directive:** None needed unless operations may take other operations as inputs.

**java_trace:**

- `src/main/java/lre/operation/CheckOPEZ.java` lines 25-28, `compute`; requirement_ids: LRE-OP2, LRE-OP3, LRE-Var1
- `src/main/java/lre/operation/CalcCPA.java` lines 36-44, `compute`; requirement_ids: LRE-OP5, LRE-OP4

### 10. [ambiguous_requirement] The numeric thresholds of Beh4, Beh9 and Beh11 are bare literals

**raw:** LRE-Beh4 uses '1' for vel and for the odist bounds; LRE-Beh9 and LRE-Beh11 use '1 m/s'. They are not named as minSafeDist or the MOM speed, so MOM_ENTRY_MAX_VEL, MOM_ENTRY_MIN_DIST and CAUTION_VEL (all 1.0) were introduced. With the defaults, Beh4's odist(cstc) > 1 duplicates part of !inOpez; it is kept as written.

**fix_directive:** Confirm whether these should track minSafeDist and the MOM speed.

**java_trace:**

- `src/main/java/lre/LreConstants.java` lines 32-39, `MOM_ENTRY_MAX_VEL`; requirement_ids: LRE-Beh4, LRE-Beh9, LRE-Beh11

### 11. [ambiguous_requirement] vvel is the signed rate of climb

**raw:** LRE-OP1 sets vvel to the rate of climb, so LRE-Beh11 (vvel >= 1 m/s) reacts to climbing only, not to descending at 1 m/s or more towards an obstacle below. LRE-Beh10 still catches vertical proximity whatever the velocity.

**fix_directive:** Confirm, or use the magnitude of the rate of climb.

**java_trace:**

- `src/main/java/lre/operation/CalcVel.java` lines 23-27, `compute`; requirement_ids: LRE-OP1, LRE-Var3, LRE-Beh11

### 12. [invented_default] Initial readings, no-event cycles and tie-breaking

**raw:** Sensor readings are 0.0 until the first update(), so depth 0 makes inOpez true and MOM is refused until a depth is reported. The actuator's stored advice starts at 0.0. step(null) is a cycle with no operator input (autonomous transitions only). When two obstacles are equally close, the lower identifier is chosen.

**fix_directive:** Confirm.

**java_trace:**

- `src/main/java/lre/sensor/Sensor.java` lines 32-38, `update`; requirement_ids: LRE-DM5
- `src/main/java/lre/sensor/Sensor.java` lines 151-164, `closestOf`; requirement_ids: LRE-OP3, LRE-OP4
- `src/main/java/lre/actuator/Actuator.java` lines 16-24, `apply`; requirement_ids: LRE-DM8

### 13. [scope_question] Reading of LRE-DC1 when one mode has several trigger-less transitions

**raw:** MOM has five trigger-less transitions (Beh5, Beh8, Beh9, Beh10, Beh11) and HCM three (Beh13, Beh14, Beh16). DC1 was read as: one transition per requirement, and no two transitions with the same source, trigger and guard. Each of Beh2..Beh18 is exactly one branch of step(); Beh1 is the initial value of currentMode; Beh19 holds because OCM has branches only for reqVel, reqHdng and reqMOM, and no other mode handles reqVel or reqHdng.

**fix_directive:** Confirm the reading of DC1.

**java_trace:**

- `src/main/java/lre/controller/LreController.java` lines 53-154, `step`; requirement_ids: LRE-DC1, LRE-Beh1, LRE-Beh19

## next_step

Run the pipeline (compile, coverage, profile check, T2M, M2M, M2T, FDR4, Dafny, Isabelle, vacuity).
