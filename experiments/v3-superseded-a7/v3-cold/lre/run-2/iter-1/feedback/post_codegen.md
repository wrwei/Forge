# post_codegen — code-generation self-review

**status:** `uncertain`

**summary:** All 51 LRE requirements are implemented in 15 Java files under src/main/java/lre (controller, sensor, actuator, event, operation, annotation); the project compiles and result_codegen.json maps every requirement and every public element. Sixteen points are flagged: one requirement value that the profile cannot express (index -1), three invented defaults, the CPA formulation, transition priority, and requirement gaps or inconsistencies (CAM manoeuvre, HCM speed, signed vvel, leaving CAM).

## Issues (16)

### 1. [ambiguous_requirement] The index -1 for 'no obstacle' is not a natural number

- **raw:** LRE-Var5/LRE-Var6 declare cstc and cdyn as natural numbers but say -1 means none; LRE-DM5 says closestStaticIndex()/closestDynamicIndex() return -1. Java integers become nat in the model and may not be negative.
- **fix_directive:** Implemented: index 0 means 'no obstacle'. ObstacleRegister.put rejects index 0, so obstacles are numbered from 1; the selection functions return 0 when there is no candidate and break ties by the lowest index; cstc and cdyn start at 0. If the operator wants another sentinel, change ObstacleRegister.NO_OBSTACLE.
- **java_trace:**
  - `src/main/java/lre/sensor/ObstacleRegister.java` lines 17–30 — `NO_OBSTACLE, put` — LRE-DM3, LRE-Var5, LRE-Var6
  - `src/main/java/lre/sensor/Sensor.java` lines 132–158 — `closestStaticIndex, closestDynamicIndex` — LRE-DM5, LRE-OP3, LRE-OP4

### 2. [invented_default] Safe large distance for an index with no obstacle is 1000000.0 m

- **raw:** LRE-DM5: 'When no obstacle exists, distance functions odist, hdist, and vdist return a safe large distance' -- no value given.
- **fix_directive:** Sensor.NO_OBSTACLE_DIST = 1000000.0 (private to Sensor, used only in its function bodies). Replace if the requirements fix a value.
- **java_trace:**
  - `src/main/java/lre/sensor/Sensor.java` lines 15–15 — `NO_OBSTACLE_DIST` — LRE-DM5
  - `src/main/java/lre/sensor/Sensor.java` lines 62–94 — `hdist, vdist, odist` — LRE-SF1, LRE-SF2, LRE-SF3

### 3. [invented_default] CPA_EPSILON = 0.000001 (m/s)^2 keeps the tcpa division defined

- **raw:** LRE-OP5 gives no formula for tcpa/cda; the standard tcpa = (r . w)/|w|^2 divides by zero when the relative velocity is zero (always so with no dynamic obstacle and a stationary AUV).
- **fix_directive:** The denominator is |w|^2 + CPA_EPSILON. The model's constants interface rounds this fractional constant up to 1, so the model's tcpa and cda differ numerically from Java's; the guards that read them (LRE-Beh8, LRE-Beh14, LRE-Beh18) are affected only in value, not in structure.
- **java_trace:**
  - `src/main/java/lre/controller/LreConstants.java` lines 54–54 — `CPA_EPSILON` — LRE-OP5
  - `src/main/java/lre/operation/CalcCPA.java` lines 31–51 — `compute` — LRE-OP5, LRE-Var7, LRE-Var8

### 4. [design_choice] CPA geometry: relative velocity, horizontal plane, current vertical separation

- **raw:** LRE-OP5: 'computes cda and tcpa to the closest dynamic obstacle ... accesses the obstacle's relative position and velocity from the Sensor.' The obstacle velocities are ground-referenced (LRE-DM2: static means both are zero), and the Sensor offers no vertical-velocity accessor for an obstacle.
- **fix_directive:** Implemented: w = (nsVel - obsNsVel(i), ewVel - obsEwVel(i)) is the AUV's velocity relative to the obstacle; tcpa = (r . w)/(|w|^2 + CPA_EPSILON); cda = sqrt(|r - w*tcpa|^2 + vdist(i)^2), holding the vertical separation at its current value. The vdist term also makes the no-dynamic-obstacle case safe: cda is then the large distance and MOM/HCM -> CAM cannot fire. If cda is meant to be horizontal only, the no-obstacle case needs another guard (zero position defaults would give cda = 0).
- **java_trace:**
  - `src/main/java/lre/operation/CalcCPA.java` lines 22–51 — `compute` — LRE-OP5, LRE-Var7, LRE-Var8, LRE-DM2

### 5. [ambiguous_requirement] No-obstacle defaults of the position accessors: zero (DM5) or large (OP5)

- **raw:** LRE-DM5: field accessors nsRelDist, ewRelDist, obsNsVel, obsEwVel return zero when no obstacle exists. LRE-OP5: the Sensor 'returns zero-velocity and large-distance defaults when no dynamic obstacle exists.'
- **fix_directive:** Followed LRE-DM5 (all four accessors return 0); the large distance reaches CalcCPA through vdist, which returns the safe large distance (see the CPA geometry issue).
- **java_trace:**
  - `src/main/java/lre/sensor/Sensor.java` lines 96–130 — `nsRelDist, ewRelDist, obsNsVel, obsEwVel` — LRE-DM5, LRE-OP5

### 6. [design_choice] CheckOPEZ and CalcCPA obtain the closest indices through the sensor

- **raw:** LRE-OP2: CheckOPEZ uses the index computed by CalcCStc; LRE-OP5: CalcCPA uses cdyn. The profile allows compute() to read only sensor calls, constants and the class's own fields.
- **fix_directive:** CheckOPEZ calls sensor.odist(sensor.closestStaticIndex()) and CalcCPA calls sensor.closestDynamicIndex() -- the same sensor functions CalcCStc and CalcCDyn assign, so the values are identical within a step. The controller still runs CalcCStc and CalcCDyn first and its guards read cstc/cdyn from them.
- **java_trace:**
  - `src/main/java/lre/operation/CheckOPEZ.java` lines 23–26 — `compute` — LRE-OP2, LRE-OP3
  - `src/main/java/lre/operation/CalcCPA.java` lines 31–51 — `compute` — LRE-OP5, LRE-OP4

### 7. [design_choice] Variables LRE-Var1 to LRE-Var8 are fields of the operation classes

- **raw:** Each LRE-Var* says 'the controller maintains' the variable; the profile allows a variable to live in the operation that computes it.
- **fix_directive:** inOpez, hvel, vvel, vel, cstc, cdyn, cda, tcpa are fields of CheckOPEZ, CalcVel, CalcCStc, CalcCDyn and CalcCPA, read by the controller's predicates through same-named getters. They are not copied into the controller, which would give two model elements the same name.
- **java_trace:**
  - `src/main/java/lre/operation/CalcVel.java` lines 12–14 — `hvel, vvel, vel` — LRE-Var2, LRE-Var3, LRE-Var4
  - `src/main/java/lre/operation/CheckOPEZ.java` lines 12–12 — `inOpez` — LRE-Var1
  - `src/main/java/lre/operation/CalcCStc.java` lines 12–12 — `cstc` — LRE-Var5
  - `src/main/java/lre/operation/CalcCDyn.java` lines 12–12 — `cdyn` — LRE-Var6
  - `src/main/java/lre/operation/CalcCPA.java` lines 14–15 — `tcpa, cda` — LRE-Var7, LRE-Var8

### 8. [design_choice] Priority of the transitions within each mode

- **raw:** The requirements give every LRE-Beh* the same priority and do not order the transitions of a mode.
- **fix_directive:** MOM: reqOCM, inOpez, collision course (CAM), endTask, Beh9, Beh11, Beh10, reqHCM. HCM: reqOCM, inOpez, collision course (CAM), Beh13. CAM: reqOCM, Beh18. reqOCM is the operator override; inOpez is first among the guard-only branches so its guard is 'inOpez alone' as LRE-Beh5/Beh16 state. Every branch below a guard-only branch conjoins the negations of the earlier guards, so the guards of Beh7-Beh14 carry extra !inOpez / !collisionCourse / ... conjuncts. Beh11 comes before Beh10 so that it is not subsumed when staticObsVertDist <= staticObsDfltVertDist (all defaults are 1).
- **java_trace:**
  - `src/main/java/lre/controller/LreController.java` lines 98–157 — `step` — LRE-Beh5, LRE-Beh6, LRE-Beh7, LRE-Beh8, LRE-Beh9, LRE-Beh10, LRE-Beh11, LRE-Beh12, LRE-Beh13, LRE-Beh14, LRE-Beh15, LRE-Beh16, LRE-Beh17, LRE-Beh18

### 9. [design_choice] LRE-DC1 with several guard-only transitions from MOM

- **raw:** LRE-DC1: 'no duplicate transitions for the same source mode and trigger event combination', yet Beh5, Beh8, Beh9, Beh10 and Beh11 all leave MOM with no trigger event.
- **fix_directive:** Read as 'no transition is duplicated': each LRE-Beh2..18 is exactly one branch of step, and the guard-only branches of a mode are mutually exclusive through the negated earlier guards.
- **java_trace:**
  - `src/main/java/lre/controller/LreController.java` lines 48–158 — `step` — LRE-DC1

### 10. [ambiguous_requirement] LRE-Beh11 tests the signed vertical velocity

- **raw:** LRE-OP1: vvel is the rate of climb (signed). LRE-Beh11: 'vvel is at or above 1 m/s'. A descent at 1 m/s or more never satisfies it.
- **fix_directive:** Implemented literally (vvel >= VERT_VEL_LIMIT). If a magnitude is meant, the predicate vvelAtOrAboveLimit should test |vvel|.
- **java_trace:**
  - `src/main/java/lre/controller/LreController.java` lines 71–71 — `vvelAtOrAboveLimit` — LRE-Beh11, LRE-OP1, LRE-Var3

### 11. [ambiguous_requirement] HCM velocity: 0.1 m/s (system description) or 0 m/s (LRE-FR3)

- **raw:** system_description.txt: 'HCM: Reduced speed (0.1 m/s)'. LRE-FR3: 'On entering HCM, the LRE advises a velocity of 0 m/s'.
- **fix_directive:** Implemented the requirement: HCM_ADVISED_VEL = 0.0.
- **java_trace:**
  - `src/main/java/lre/controller/LreConstants.java` lines 26–26 — `HCM_ADVISED_VEL` — LRE-FR3

### 12. [unimplemented] The CAM evasive manoeuvre has no specified output

- **raw:** LRE-FR4: in CAM 'the AUV performs evasive manoeuvres'. No requirement says what the LRE advises in CAM, and LRE-DM7 limits outputs to advVel and advHdng.
- **fix_directive:** CAM has no entry action: the autopilot keeps the last advice (1 m/s from MOM, 0 m/s from HCM) until the LRE leaves CAM. A manoeuvre needs a requirement giving the advVel/advHdng values to send on entering CAM.
- **java_trace:**
  - `src/main/java/lre/controller/LreController.java` lines 105–107 — `step (MOM -> CAM)` — LRE-FR4, LRE-Beh8
  - `src/main/java/lre/controller/LreController.java` lines 139–141 — `step (HCM -> CAM)` — LRE-FR4, LRE-Beh14

### 13. [ambiguous_requirement] Leaving CAM ignores tcpa

- **raw:** LRE-Beh18: CAM -> OCM when cda >= minSafeDist. Once a dynamic obstacle has passed on a near line, tcpa < 0 but cda stays below minSafeDist.
- **fix_directive:** Implemented literally: CAM is then left only on reqOCM or when the geometry changes. If 'safe' should include 'obstacle has passed', the CAM guard would be !cdaBelowMinSafeDist || !tcpaNonNegative.
- **java_trace:**
  - `src/main/java/lre/controller/LreController.java` lines 148–157 — `step (CAM)` — LRE-Beh18, LRE-Beh17

### 14. [design_choice] Every number the transitions fix is a named constant

- **raw:** LRE-Beh4 states '> 1' and '<= 1' rather than minSafeDist; LRE-Beh9/Beh11, LRE-FR2/FR3, LRE-Beh7/Beh18, LRE-OP2 and LRE-Beh8/Beh14 fix 1 m/s, 0 m/s, depth 0 and tcpa 0.
- **fix_directive:** MOM_ENTRY_MIN_DIST and MOM_ENTRY_MAX_VEL are kept distinct from MIN_SAFE_DIST although equal by default; the other values are HORIZ_VEL_LIMIT, VERT_VEL_LIMIT, MOM_ADVISED_VEL, HCM_ADVISED_VEL, STOP_ADVISED_VEL, SURFACE_DEPTH and MIN_TCPA. All are integral, so the model's rounding does not change them.
- **java_trace:**
  - `src/main/java/lre/controller/LreConstants.java` lines 22–49 — `LreConstants` — LRE-Beh4, LRE-Beh9, LRE-Beh11, LRE-FR2, LRE-FR3, LRE-Beh7, LRE-Beh18, LRE-OP2, LRE-Beh8, LRE-Beh14

### 15. [invented_default] Values before the first sensor reading

- **raw:** No requirement gives the sensor readings, the advice or the derived variables before the first reading.
- **fix_directive:** All start at zero (false for inOpez; index 0 = no obstacle for cstc/cdyn). Depth 0 means in OPEZ, so reqMOM is refused until a reading with depth > 0 arrives.
- **java_trace:**
  - `src/main/java/lre/sensor/Sensor.java` lines 18–21 — `depth, nsVel, ewVel, rateOfClimb` — LRE-DM5
  - `src/main/java/lre/actuator/Actuator.java` lines 11–12 — `advisedVelocity, advisedHeading` — LRE-DM8

### 16. [scope_question] ObstacleRegister has put/remove, which LRE-DM3 does not list

- **raw:** LRE-DM3 lists lookup by index, iteration and static/dynamic filtering only.
- **fix_directive:** put and remove were added so the register can be populated; neither is used by the controller.
- **java_trace:**
  - `src/main/java/lre/sensor/ObstacleRegister.java` lines 21–35 — `put, remove` — LRE-DM3

## next_step

Run the pipeline (compile, coverage, profile check, transformations, FDR4, Dafny, Isabelle, vacuity).
