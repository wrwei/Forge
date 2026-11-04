# post_codegen

**status:** `uncertain`

**summary:** All 51 requirements are implemented and traced; the project compiles. 12 points the requirements leave open are recorded below, each with the resolution used.

## Issues

### 1. [ambiguous_requirement] cstc/cdyn are natural numbers but -1 is specified for 'none'

- **raw:** LRE-Var5/Var6 declare cstc and cdyn natural numbers and LRE-DM5/OP3/OP4 make -1 mean 'no obstacle'; the Java profile maps int to nat, which cannot be negative.
- **fix_directive:** Resolved: sentinel LreConstants.NO_OBSTACLE = 0; ObstacleRegister.put rejects index 0, so register indices start at 1. No guard tests the sentinel; it only reaches the sensor functions, which return their 'no obstacle' defaults for it.
- **java_trace:**
  - `src/main/java/lre/controller/LreConstants.java` lines 25-29, `NO_OBSTACLE` — LRE-Var5, LRE-Var6, LRE-DM5, LRE-OP3, LRE-OP4
  - `src/main/java/lre/sensor/ObstacleRegister.java` lines 21-30, `put` — LRE-DM3

### 2. [invented_default] Value of the 'safe large distance'

- **raw:** LRE-DM5 says odist/hdist/vdist return 'a safe large distance' when no obstacle exists, without a value.
- **fix_directive:** Chose LreConstants.SAFE_LARGE_DIST = 1000000.0 m (integral, so not changed by the model's rounding of constants).
- **java_trace:**
  - `src/main/java/lre/controller/LreConstants.java` lines 22-23, `SAFE_LARGE_DIST` — LRE-DM5, LRE-OP2, LRE-OP5

### 3. [ambiguous_requirement] No-dynamic-obstacle defaults for CalcCPA: zero (DM5) or large distance (OP5)

- **raw:** LRE-DM5: field accessors nsRelDist/ewRelDist return zero when no obstacle exists. LRE-OP5: the Sensor returns 'zero-velocity and large-distance defaults'. With zero relative distances a naive CPA gives cda = 0 and tcpa = 0, i.e. a spurious collision.
- **fix_directive:** Kept DM5's zero accessors. CalcCPA adds (hdist(cdyn) - sqrt(ns^2 + ew^2)) to cda: exactly 0 for a registered obstacle, SAFE_LARGE_DIST when there is none, so no spurious CAM.
- **java_trace:**
  - `src/main/java/lre/operation/CalcCPA.java` lines 36-50, `compute` — LRE-OP5, LRE-DM5, LRE-Var7

### 4. [design_choice] CPA formula: horizontal plane, closing velocity, epsilon

- **raw:** LRE-OP5 does not give the formula for cda and tcpa.
- **fix_directive:** Horizontal plane only (the four accessors the Sensor provides). Closing velocity w = AUV velocity - obstacle velocity, since a static obstacle has zero velocity (ground frame). tcpa = (p.w)/(|w|^2 + CPA_EPSILON); cda = |p - w*tcpa|. CPA_EPSILON = 0.000001 avoids division by zero (no NaN). The model's constants interface rounds CPA_EPSILON up to 1, so the model's cda/tcpa values differ numerically from Java's; no guard compares against CPA_EPSILON directly.
- **java_trace:**
  - `src/main/java/lre/operation/CalcCPA.java` lines 36-50, `compute` — LRE-OP5, LRE-Var7, LRE-Var8
  - `src/main/java/lre/controller/LreConstants.java` lines 31-32, `CPA_EPSILON` — LRE-OP5

### 5. [ambiguous_requirement] HCM advised velocity: 0 m/s (FR3) versus 0.1 m/s (system description)

- **raw:** system_description.txt: HCM 'Reduced speed (0.1 m/s)'. LRE-FR3: 'On entering HCM, the LRE advises a velocity of 0 m/s'.
- **fix_directive:** Followed the requirement: every transition into HCM issues advVel(0.0).
- **java_trace:**
  - `src/main/java/lre/controller/LreController.java` lines 95-127, `step` — LRE-FR3, LRE-Beh9, LRE-Beh10, LRE-Beh11, LRE-Beh12

### 6. [design_choice] Transition priority within MOM and HCM

- **raw:** All LRE-Beh requirements have the same priority (8); the order of overlapping transitions is not given.
- **fix_directive:** Order: reqOCM (operator override, first in MOM, HCM and CAM), then inOpez -> OCM, then collision course -> CAM, then endTask/reqHCM, then the static-obstacle HCM entries. Every branch below a guard-only branch conjoins the negation of that guard. Beh11 (vvel >= 1 and vdist <= staticObsVertDist) is placed before Beh10 (vdist <= staticObsDfltVertDist): with the default thresholds equal, Beh10 subsumes Beh11, so the reverse order would make Beh11 a dead transition. Consequence: an endTask or reqHCM received while in OPEZ or on a collision course is not taken; the safety transition is taken instead.
- **java_trace:**
  - `src/main/java/lre/controller/LreController.java` lines 95-143, `step` — LRE-Beh5, LRE-Beh6, LRE-Beh7, LRE-Beh8, LRE-Beh9, LRE-Beh10, LRE-Beh11, LRE-Beh12, LRE-Beh13, LRE-Beh14, LRE-Beh15, LRE-Beh16, LRE-DC1

### 7. [ambiguous_requirement] LRE-DC1 'no duplicate transitions for the same source mode and trigger event'

- **raw:** MOM has five trigger-less transitions (Beh5, Beh8, Beh9, Beh10, Beh11), HCM three (Beh13, Beh14, Beh16), as the requirements demand.
- **fix_directive:** Read DC1 as: one transition per Beh requirement and no two transitions on the same input event from the same mode. That holds; the trigger-less transitions are distinguished by their guards.
- **java_trace:**
  - `src/main/java/lre/controller/LreController.java` lines 77-154, `step` — LRE-DC1

### 8. [design_choice] CheckOPEZ and CalcCPA obtain the closest indices from the Sensor

- **raw:** LRE-OP2 says CheckOPEZ uses the index computed by CalcCStc; OP5 says CalcCPA uses cdyn. The operation profile only allows sensor calls and the operation's own fields in compute().
- **fix_directive:** CheckOPEZ calls sensor.closestStaticIndex() and CalcCPA calls sensor.closestDynamicIndex(): the same functions CalcCStc and CalcCDyn call, so within a cycle they get the same index.
- **java_trace:**
  - `src/main/java/lre/operation/CheckOPEZ.java` lines 23-26, `compute` — LRE-OP2, LRE-Var1
  - `src/main/java/lre/operation/CalcCPA.java` lines 36-50, `compute` — LRE-OP5

### 9. [ambiguous_requirement] vvel is the signed rate of climb

- **raw:** LRE-OP1: 'vvel is the rate of climb'. LRE-Beh11: 'vvel is at or above 1 m/s'.
- **fix_directive:** vvel = rateOfClimb (signed), so Beh11 fires only when climbing at >= 1 m/s, not when descending fast. Vertical proximity at any speed is still covered by Beh10.
- **java_trace:**
  - `src/main/java/lre/operation/CalcVel.java` lines 22-26, `compute` — LRE-OP1, LRE-Var3, LRE-Beh11

### 10. [scope_question] CAM evasive manoeuvre has no specified output

- **raw:** LRE-FR4: in CAM 'the AUV performs evasive manoeuvres'; no velocity or heading is given, and LRE-DM7 allows only advVel and advHdng.
- **fix_directive:** Entering CAM issues no output. The manoeuvre is left to the autopilot; CAM ends on reqOCM or when cda >= minSafeDist (advVel(0)).
- **java_trace:**
  - `src/main/java/lre/controller/LreController.java` lines 144-153, `step` — LRE-FR4, LRE-Beh17, LRE-Beh18

### 11. [invented_default] Initial sensor readings and advised values

- **raw:** No requirement gives values before the first sensor reading or the first advice.
- **fix_directive:** Sensor raw readings start at 0.0 (depth 0 puts the AUV in OPEZ, so reqMOM is refused until a real depth arrives); Actuator advised velocity and heading start at 0.0.
- **java_trace:**
  - `src/main/java/lre/sensor/Sensor.java` lines 15-18, `Sensor` — LRE-DM5
  - `src/main/java/lre/actuator/Actuator.java` lines 12-13, `Actuator` — LRE-DM8

### 12. [design_choice] Literal thresholds and naming

- **raw:** LRE-Beh4/Beh9/Beh11 give literal thresholds of 1; LRE-DM2 names obstacle fields in snake_case.
- **fix_directive:** Literal 1.0 used where the requirement gives a number (not tied to minSafeDist). Obstacle components keep the requirement's snake_case names, so they cannot collide with the Sensor accessors nsRelDist etc. step(null) means a cycle with no operator input. On a distance tie, closestStaticIndex/closestDynamicIndex return the lowest index.
- **java_trace:**
  - `src/main/java/lre/controller/LreController.java` lines 59-74, `step` — LRE-Beh4, LRE-Beh9, LRE-Beh11
  - `src/main/java/lre/sensor/Obstacle.java` lines 10-17, `Obstacle` — LRE-DM2

## next_step

Run the pipeline (compile, coverage, profile check, transformations, verifiers). No issue blocks it.
