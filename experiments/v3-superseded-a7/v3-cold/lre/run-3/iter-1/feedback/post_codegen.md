# post_codegen

**status:** uncertain

**summary:** All 51 LRE requirements are implemented in 15 Java files (package lre) and traced in result_codegen.json; the project compiles. Fourteen points the requirements did not settle are recorded below: the -1 'no obstacle' index versus nat, the HCM advised velocity, the CPA formula and its defaults, operation data flow, transition priority, CAM behaviour, and naming.

## Issues

### 1. [ambiguous_requirement] '-1 means no obstacle' conflicts with cstc/cdyn being natural numbers

- **raw:** LRE-Var5/Var6 declare cstc and cdyn as natural numbers whose value -1 means none; LRE-DM5, OP3, OP4 say closestStaticIndex()/closestDynamicIndex() return -1 when none. The Java profile maps int to nat and forbids negative values and negative 'none' markers.
- **fix_directive:** Index 0 is reserved as ObstacleRegister.NO_OBSTACLE: the register refuses index 0, and the closest-index functions return 0 when there is no candidate. Every distance function returns the safe large distance for an absent index, so guards over cstc/cdyn behave as the requirements intend. Change only if obstacle id 0 must be usable.
- **java_trace:**
  - `src/main/java/lre/sensor/ObstacleRegister.java` lines 19-37, `NO_OBSTACLE / put`, requirement_ids: LRE-DM3, LRE-Var5, LRE-Var6
  - `src/main/java/lre/sensor/Sensor.java` lines 116-146, `closestStaticIndex / closestDynamicIndex`, requirement_ids: LRE-DM5, LRE-OP3, LRE-OP4

### 2. [ambiguous_requirement] HCM advised velocity: 0 m/s (LRE-FR3) versus 0.1 m/s (system description)

- **raw:** LRE-FR3: on entering HCM advise 0 m/s. System description: HCM is reduced speed (0.1 m/s).
- **fix_directive:** Followed LRE-FR3 (requirements are authoritative): every transition into HCM advises 0.0. Note that with the AUV stopped the HCM->MOM condition (LRE-Beh13) may never become true by the AUV's own motion; the operator's reqOCM remains available.
- **java_trace:**
  - `src/main/java/lre/controller/LreController.java` lines 107-125, `step (MOM->HCM branches)`, requirement_ids: LRE-FR3, LRE-Beh9, LRE-Beh10, LRE-Beh11, LRE-Beh12

### 3. [design_choice] CPA formula: closing velocity, horizontal plane plus current vertical separation

- **raw:** LRE-OP5 names cda and tcpa but gives no formula, and the Sensor exposes no obstacle rate-of-climb accessor.
- **fix_directive:** Closing velocity w = AUV velocity - obstacle velocity (obs_*_vel are earth-frame, since 'static' means zero); p = (nsRelDist, ewRelDist); tcpa = (p.w)/(|w|^2 + 0.000001); cda = sqrt(|p - w*tcpa|^2 + vdist^2), vertical separation held constant. With no dynamic obstacle, p = 0 and vdist is large, so cda is large (safe).
- **java_trace:**
  - `src/main/java/lre/operation/CalcCPA.java` lines 30-42, `compute`, requirement_ids: LRE-OP5, LRE-Var7, LRE-Var8

### 4. [invented_default] CPA denominator term 0.000001 (m/s)^2

- **raw:** tcpa divides by |w|^2, which is zero when the closing velocity is zero (including the no-dynamic-obstacle case).
- **fix_directive:** Added 0.000001 inline (not in LreConstants, whose fractional constants the model rounds up to 1). With w = 0, tcpa = 0 and cda equals the current distance.
- **java_trace:**
  - `src/main/java/lre/operation/CalcCPA.java` lines 33-35, `compute (tcpa)`, requirement_ids: LRE-OP5, LRE-Var8

### 5. [invented_default] Safe large distance for an absent obstacle: 100000.0 m

- **raw:** LRE-DM5 says the distance functions return 'a safe large distance' when no obstacle exists, without a value.
- **fix_directive:** Sensor.NO_OBSTACLE_DISTANCE = 100000.0 m, far beyond every threshold (1 m).
- **java_trace:**
  - `src/main/java/lre/sensor/Sensor.java` lines 17-17, `NO_OBSTACLE_DISTANCE`, requirement_ids: LRE-DM5, LRE-OP2, LRE-OP5

### 6. [design_choice] CheckOPEZ and CalcCPA read the closest index from the Sensor, not from CalcCStc/CalcCDyn

- **raw:** LRE-OP2: CheckOPEZ uses the index computed by CalcCStc; LRE-OP5: CalcCPA uses cdyn. The operation profile limits compute() to sensor calls, Math.sqrt, arithmetic and the class's own fields.
- **fix_directive:** Both call sensor.closestStaticIndex()/closestDynamicIndex(), the same function CalcCStc/CalcCDyn call; the controller runs all operations in one step on unchanged sensor data, so the index is identical.
- **java_trace:**
  - `src/main/java/lre/operation/CheckOPEZ.java` lines 25-28, `compute`, requirement_ids: LRE-OP2, LRE-Var1
  - `src/main/java/lre/operation/CalcCPA.java` lines 30-42, `compute`, requirement_ids: LRE-OP5

### 7. [design_choice] Transition priority within each mode

- **raw:** The behavioural requirements all have priority 8 and give no order between transitions of one mode.
- **fix_directive:** Order: operator reqOCM/endTask (exits to OCM), then inOpez, then collision (CAM), then static-obstacle caution (Beh9, Beh11, Beh10: most specific first so Beh11 stays satisfiable after earlier guards are negated, all thresholds being 1), then reqHCM. HCM: reqOCM, inOpez, CAM, then clear->MOM. CAM: reqOCM, then cda >= minSafeDist. Lower predicate branches conjoin the negations of earlier predicate-only guards.
- **java_trace:**
  - `src/main/java/lre/controller/LreController.java` lines 93-151, `step`, requirement_ids: LRE-Beh5, LRE-Beh6, LRE-Beh7, LRE-Beh8, LRE-Beh9, LRE-Beh10, LRE-Beh11, LRE-Beh12, LRE-Beh13, LRE-Beh14, LRE-Beh15, LRE-Beh16, LRE-Beh17, LRE-Beh18, LRE-DC1

### 8. [scope_question] CAM evasive manoeuvre is not specified

- **raw:** LRE-FR4: in CAM the AUV performs evasive manoeuvres. No output, value or algorithm is given, and the LRE's only outputs are advVel and advHdng.
- **fix_directive:** CAM has no entry action; the last advice stays in force until CAM exits. An evasive advVel/advHdng would need a requirement.
- **java_trace:**
  - `src/main/java/lre/controller/LreController.java` lines 104-106, `step (MOM->CAM)`, requirement_ids: LRE-FR4, LRE-Beh8, LRE-Beh14

### 9. [ambiguous_requirement] vvel is the signed rate of climb

- **raw:** LRE-OP1: vvel is the rate of climb. LRE-Beh11: vvel at or above 1 m/s.
- **fix_directive:** vvel = rateOfClimb() (signed), as written; Beh11 therefore fires when climbing at >= 1 m/s, not when descending. Use the magnitude if descent should count.
- **java_trace:**
  - `src/main/java/lre/operation/CalcVel.java` lines 24-24, `compute (vvel)`, requirement_ids: LRE-OP1, LRE-Var3, LRE-Beh11

### 10. [design_choice] Thresholds of 1 written as literals

- **raw:** LRE-Beh4 (vel <= 1, odist > 1), LRE-Beh9/Beh11 (hvel/vvel >= 1 m/s) and the MOM advice (1 m/s) are stated as numbers, not as the DM4 constants.
- **fix_directive:** Kept as literals 1.0, not tied to minSafeDist, so changing a DM4 constant does not change them.
- **java_trace:**
  - `src/main/java/lre/controller/LreController.java` lines 59-72, `step (predicates)`, requirement_ids: LRE-Beh4, LRE-Beh9, LRE-Beh11, LRE-FR2

### 11. [design_choice] Controller variables live in the operation classes; predicate inOpez shares the variable's name

- **raw:** LRE-Var1..Var8 say the controller maintains the variables; LRE-GP1 names the predicate 'inOpez'.
- **fix_directive:** Each variable is a field of the operation that computes it, read through a getter of the same name. The step() predicate is named inOpez as LRE-GP1 requires, the same name as CheckOPEZ.inOpez.
- **java_trace:**
  - `src/main/java/lre/controller/LreController.java` lines 59-59, `step (inOpez)`, requirement_ids: LRE-GP1, LRE-Var1

### 12. [invented_default] Actuator's advised velocity and heading start at 0.0

- **raw:** LRE-DM8 does not say what the actuator holds before the first advice.
- **fix_directive:** Both start at 0.0.
- **java_trace:**
  - `src/main/java/lre/actuator/Actuator.java` lines 12-13, `advisedVelocity / advisedHeading`, requirement_ids: LRE-DM8

### 13. [design_choice] Closest-obstacle tie-break and the no-input cycle

- **raw:** LRE-DM5 does not say which obstacle wins a distance tie; LRE-ARCH2 says the controller evaluates transitions every step, including steps with no operator input.
- **fix_directive:** Ties go to the lowest index. step(null) runs a cycle with no operator input (autonomous transitions only).
- **java_trace:**
  - `src/main/java/lre/sensor/Sensor.java` lines 134-146, `closestOf`, requirement_ids: LRE-DM5, LRE-OP3, LRE-OP4
  - `src/main/java/lre/controller/LreController.java` lines 50-50, `step`, requirement_ids: LRE-ARCH2

### 14. [design_choice] Obstacle record components keep the requirement's snake_case names

- **raw:** LRE-DM2 names the fields ns_rel_dist, obs_ns_vel, ...; camel-casing them would give nsRelDist and obsNsVel, the names of LRE-DM5's sensor accessors.
- **fix_directive:** Kept snake_case so no model element shares a name with a sensor function.
- **java_trace:**
  - `src/main/java/lre/sensor/Obstacle.java` lines 9-15, `Obstacle`, requirement_ids: LRE-DM2

## next_step

Run the pipeline (compile, coverage, profile check, transformations, verifiers). None of the points above blocks it; the first two (nat sentinel, HCM velocity) are the ones a requirements owner should confirm.
