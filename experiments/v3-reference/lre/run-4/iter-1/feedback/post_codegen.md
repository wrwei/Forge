# post_codegen — uncertain

**Status:** `uncertain`

## Summary

All 51 LRE requirements are implemented in 15 Java files under java.generated.project/src/main/java/lre and traced in result_codegen.json; the project compiles. Thirteen points the requirements did not settle are recorded below: a conflict between the natural-number index variables and the -1 'none' value, a conflict between the system description and LRE-FR3 on the HCM velocity, invented defaults (safe large distance, CPA regulariser, actuator start values, tie-breaking), the CPA formula, the signed vertical velocity, the priority order of transitions within each mode, and three scope or interpretation questions.

## Issues (13)

### 1. [ambiguous_requirement] cstc/cdyn are natural numbers, but -1 is specified to mean 'no obstacle'

**Raw:** LRE-Var5/Var6 declare cstc and cdyn as natural numbers with 'Value -1 means no ... obstacle exists'; LRE-DM5/OP3/OP4 say closestStaticIndex()/closestDynamicIndex() return -1 when none exists. A Java int is a nat in the model and may never be negative.

**Fix directive:** Resolved as: Sensor.NO_OBSTACLE = 0 means 'none'; ObstacleRegister rejects identifier 0, so stored obstacle identifiers start at 1. The distance functions and field accessors give the no-obstacle defaults for any index the register does not hold. Confirm, or give a nat-compatible convention.

**Java trace:**

- `src/main/java/lre/sensor/Sensor.java` lines 13–15, `NO_OBSTACLE` — LRE-Var5, LRE-Var6, LRE-DM5
- `src/main/java/lre/sensor/ObstacleRegister.java` lines 19–28, `put` — LRE-DM3
- `src/main/java/lre/sensor/Sensor.java` lines 134–158, `closestStaticIndex` — LRE-OP3, LRE-OP4, LRE-DM5

### 2. [ambiguous_requirement] HCM velocity: system description says 0.1 m/s, LRE-FR3 says 0 m/s

**Raw:** system_description.txt: 'HCM (High Caution Mode): Reduced speed (0.1 m/s)'. LRE-FR3: 'On entering HCM, the LRE advises a velocity of 0 m/s'.

**Fix directive:** Implemented LRE-FR3 (the requirements file is the authority): every transition into HCM advises advVel(0.0). Change the literal in the four MOM-to-HCM branches if 0.1 is meant.

**Java trace:**

- `src/main/java/lre/controller/LreController.java` lines 98–115, `step` — LRE-FR3, LRE-Beh9, LRE-Beh10, LRE-Beh11, LRE-Beh12

### 3. [invented_default] Value of the 'safe large distance' returned when no obstacle exists

**Raw:** LRE-DM5 says odist, hdist and vdist return 'a safe large distance' when no obstacle exists, without a value.

**Fix directive:** Chose 1000000.0 m (private SAFE_LARGE_DISTANCE in Sensor), far above every threshold (1 m). Supply a value if the system has one.

**Java trace:**

- `src/main/java/lre/sensor/Sensor.java` lines 17–18, `Sensor` — LRE-DM5, LRE-OP2, LRE-OP5

### 4. [design_choice] CPA formula: horizontal closing motion, vertical separation held, regularised denominator

**Raw:** LRE-OP5 names cda and tcpa but gives no formula. The sensor exposes only horizontal relative position and obstacle velocity (nsRelDist, ewRelDist, obsNsVel, obsEwVel).

**Fix directive:** Implemented: p = (nsRelDist, ewRelDist) of cdyn, closing velocity w = (nsVel - obsNsVel, ewVel - obsEwVel); tcpa = (p.w)/(|w|^2 + CPA_EPSILON); cda = sqrt(|p - w*tcpa|^2 + vdist(cdyn)^2). CPA_EPSILON = 0.000001 (an invented value) keeps the division defined when the closing speed is zero (then tcpa = 0 and cda is the current distance). With no dynamic obstacle p = 0 and vdist is large, so cda is large and CAM is not entered. Obstacle and AUV vertical rates are ignored. The model's constants interface rounds CPA_EPSILON up to 1, which changes tcpa/cda in the model only.

**Java trace:**

- `src/main/java/lre/operation/CalcCPA.java` lines 33–45, `compute` — LRE-OP5, LRE-Var7, LRE-Var8
- `src/main/java/lre/LreConstants.java` lines 22–23, `CPA_EPSILON` — LRE-OP5

### 5. [ambiguous_requirement] vvel is the signed rate of climb, so LRE-Beh11 fires only when climbing

**Raw:** LRE-OP1: 'vvel is the rate of climb.' LRE-Beh11: 'vvel is at or above 1 m/s AND vdist <= staticObsVertDist'.

**Fix directive:** Implemented literally (vvel = rateOfClimb, signed), so a fast descent toward a static obstacle does not trigger LRE-Beh11 (LRE-Beh10 still triggers on vdist alone). If the magnitude is meant, vvel should be |rate_of_climb|.

**Java trace:**

- `src/main/java/lre/operation/CalcVel.java` lines 23–27, `compute` — LRE-OP1, LRE-Var3, LRE-Beh11

### 6. [design_choice] Priority of transitions within each mode

**Raw:** All LRE-Beh requirements have priority 8; the order among transitions leaving one mode is not given.

**Fix directive:** Order chosen: operator events first (reqOCM, then endTask, then reqHCM in MOM), so their guards are exactly the event as specified; then inOpez -> OCM, which keeps its guard 'inOpez alone' (LRE-Beh5, LRE-Beh16); then collision course -> CAM; then the static-obstacle transitions. In MOM those are ordered Beh9, Beh11, Beh10 because Beh11's condition implies Beh10's when staticObsVertDist = staticObsDfltVertDist, so Beh11 placed after Beh10 could never be taken. Lower guard-only branches conjoin the negations of the earlier guard-only guards, as the profile requires.

**Java trace:**

- `src/main/java/lre/controller/LreController.java` lines 91–136, `step` — LRE-Beh5, LRE-Beh6, LRE-Beh7, LRE-Beh8, LRE-Beh9, LRE-Beh10, LRE-Beh11, LRE-Beh12, LRE-Beh13, LRE-Beh14, LRE-Beh15, LRE-Beh16, LRE-Beh17, LRE-Beh18, LRE-GP1

### 7. [design_choice] CheckOPEZ and CalcCPA obtain the closest-obstacle index from the sensor

**Raw:** LRE-OP2: 'CheckOPEZ uses the closest-static-obstacle index computed by CalcCStc'; LRE-OP5 uses cdyn. The profile lets an operation take only sensors and constants and use only sensor calls and its own fields.

**Fix directive:** CheckOPEZ and CalcCPA call sensor.closestStaticIndex()/closestDynamicIndex(), the same functions CalcCStc and CalcCDyn assign to cstc/cdyn in the same step. The index is therefore identical, without one operation reading another.

**Java trace:**

- `src/main/java/lre/operation/CheckOPEZ.java` lines 23–26, `compute` — LRE-OP2
- `src/main/java/lre/operation/CalcCPA.java` lines 33–37, `compute` — LRE-OP5

### 8. [scope_question] CAM evasive manoeuvre has no specified output; operator inputs outside OCM

**Raw:** LRE-FR4: 'the AUV performs evasive manoeuvres' (no advised velocity or heading given). LRE-Beh19: 'In all other modes, operator velocity and heading inputs are overridden by the LRE.'

**Fix directive:** CAM issues no advice on entry, because no value is specified. reqVel/reqHdng are ignored outside OCM, and in OCM reqOCM, reqHCM and endTask are ignored. Specify the CAM advice if one is intended.

**Java trace:**

- `src/main/java/lre/controller/LreController.java` lines 76–136, `step` — LRE-FR4, LRE-Beh19

### 9. [ambiguous_requirement] LRE-Beh10 and LRE-Beh13 use different vertical thresholds

**Raw:** MOM -> HCM (LRE-Beh10) uses vdist <= staticObsDfltVertDist; HCM -> MOM (LRE-Beh13) requires vdist > staticObsVertDist.

**Fix directive:** Implemented as written. With the given defaults (both 1) the two guards are disjoint. If staticObsDfltVertDist were set above staticObsVertDist, MOM and HCM could alternate on guards alone.

**Java trace:**

- `src/main/java/lre/controller/LreController.java` lines 111–127, `step` — LRE-DM4, LRE-Beh10, LRE-Beh13

### 10. [ambiguous_requirement] LRE-DC1 and several guard-only transitions from one mode

**Raw:** LRE-DC1: 'no duplicate transitions for the same source mode and trigger event combination'. LRE-Beh9, Beh10 and Beh11 are three MOM -> HCM transitions with no trigger event.

**Fix directive:** Read DC1 as: no two transitions with the same source, trigger and guard. Each Beh requirement is one branch; Beh9, Beh10 and Beh11 stay separate branches with distinct guards.

**Java trace:**

- `src/main/java/lre/controller/LreController.java` lines 105–115, `step` — LRE-DC1, LRE-Beh9, LRE-Beh10, LRE-Beh11

### 11. [design_choice] Literal thresholds of 1 kept as literals

**Raw:** LRE-Beh4 (vel <= 1, odist(cdyn) > 1, odist(cstc) > 1), LRE-Beh9/Beh11 (>= 1 m/s) and LRE-FR2 (advise 1 m/s) state the number 1, not a named constant.

**Fix directive:** Written as the literal 1.0 as stated, not as MIN_SAFE_DIST, although that constant's default is also 1.

**Java trace:**

- `src/main/java/lre/controller/LreController.java` lines 58–74, `step` — LRE-Beh4, LRE-Beh9, LRE-Beh11, LRE-FR2

### 12. [design_choice] A cycle without operator input is step(null)

**Raw:** The LRE takes autonomous transitions each step whether or not an operator input arrived; the requirements define no 'no input' event.

**Fix directive:** step accepts null for a cycle with no operator input. Autonomous transitions are evaluated in every call; an event a mode does not handle does not block them.

**Java trace:**

- `src/main/java/lre/controller/LreController.java` lines 46–52, `step` — LRE-ARCH2

### 13. [invented_default] Actuator start values and closest-obstacle ties

**Raw:** LRE-DM8 does not give the advised values before the first output; LRE-OP3/OP4 do not say which obstacle is closest when two are at the same odist.

**Fix directive:** The Actuator starts at advised velocity 0.0 and heading 0.0. On a tie, the lowest identifier is selected.

**Java trace:**

- `src/main/java/lre/actuator/Actuator.java` lines 12–13, `Actuator` — LRE-DM8
- `src/main/java/lre/sensor/Sensor.java` lines 146–158, `closestDynamicIndex` — LRE-OP3, LRE-OP4

## Next step

Run the pipeline (compile, coverage, profile check, transformations, FDR4, Dafny, Isabelle, vacuity) and fix the Java from its reports; the points above need a decision from the requirements owner, not a code change.
