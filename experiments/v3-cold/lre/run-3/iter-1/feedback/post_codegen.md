# post_codegen

**status:** uncertain

## Summary

All 51 LRE requirements are implemented in 15 Java files under src/main/java/lre: one controller (LreController.step, four modes, 18 transitions), a Sensor with an ObstacleRegister, an Actuator, five operation classes and the constants, events and mode enum. It compiles under Java 17. Thirteen points the requirements leave open are listed below: invented values, design choices, ambiguities and one scope question. None of them blocks the pipeline.

## Issues (13)

### 1. [ambiguous_requirement] The no-obstacle index -1 is not a natural number

**raw:** LRE-DM5, LRE-OP3 and LRE-OP4 say the selection functions return -1 when no obstacle exists, and LRE-Var5/LRE-Var6 say -1 means none. The same requirements type cstc and cdyn as natural numbers, and the Java profile maps int to nat, so it can never be negative.

**fix_directive:** Implemented: index 0 is ObstacleRegister.NO_OBSTACLE and obstacle identifiers start at 1 (put rejects identifiers <= 0). A missing index gets the safe large distance and zero fields, so every guard behaves as the -1 convention intends. Confirm that identifier 0 may be reserved; if the register must accept 0, choose another non-negative sentinel.

**java_trace:**

- `src/main/java/lre/sensor/ObstacleRegister.java` lines 18-30 — NO_OBSTACLE, put — LRE-DM3, LRE-DM5, LRE-Var5, LRE-Var6
- `src/main/java/lre/sensor/Sensor.java` lines 119-139 — closestStaticIndex, closestDynamicIndex — LRE-DM5, LRE-OP3, LRE-OP4

### 2. [invented_default] The safe large distance is 1000000.0 m

**raw:** LRE-DM5 says odist, hdist and vdist return 'a safe large distance' when no obstacle exists, but gives no value.

**fix_directive:** Implemented as Sensor.SAFE_LARGE_DIST = 1000000.0 m, far beyond every 1 m threshold. Replace it if the system has a defined sensing range.

**java_trace:**

- `src/main/java/lre/sensor/Sensor.java` lines 15 — SAFE_LARGE_DIST — LRE-DM5, LRE-OP2, LRE-OP5

### 3. [ambiguous_requirement] HCM speed: 0.1 m/s in the system description, 0 m/s in LRE-FR3

**raw:** The system description says HCM uses a 'reduced speed (0.1 m/s)'. LRE-FR3 says the LRE advises 0 m/s on entering HCM.

**fix_directive:** Implemented with the requirement: every transition into HCM issues advVel(HALT_SPEED = 0.0). If 0.1 m/s is intended, change the advised value on the HCM-entry branches.

**java_trace:**

- `src/main/java/lre/LreConstants.java` lines 27 — HALT_SPEED — LRE-FR3
- `src/main/java/lre/controller/LreController.java` lines 102-116 — step (MOM to HCM branches) — LRE-FR3, LRE-Beh9, LRE-Beh10, LRE-Beh11, LRE-Beh12

### 4. [design_choice] The CPA formula in CalcCPA

**raw:** LRE-OP5 says what cda and tcpa mean, but gives no formula.

**fix_directive:** Implemented as follows. The closing velocity c is the AUV's horizontal velocity minus the obstacle's (ns, ew). With p the obstacle's relative position (nsRelDist, ewRelDist), tcpa = (p.c) / (|c|^2 + MIN_REL_SPEED_SQ), and cda is the distance at tcpa, sqrt(|p - c*tcpa|^2 + vdist^2). Horizontal motion is extrapolated and the current vertical separation is kept. obs_roc is not used, because LRE-DM5 provides no accessor for it. With no dynamic obstacle, tcpa = 0 and cda is the large default, so CAM is never entered spuriously. Confirm, or supply the intended formula.

**java_trace:**

- `src/main/java/lre/operation/CalcCPA.java` lines 31-42 — compute — LRE-OP5, LRE-Var7, LRE-Var8

### 5. [invented_default] The divide-by-zero guard MIN_REL_SPEED_SQ = 0.000001

**raw:** tcpa divides by the squared relative speed, which is zero when the obstacle moves with the AUV. Profile section 5 allows no conditional inside compute().

**fix_directive:** Implemented as a small additive constant, 0.000001 m^2/s^2. The model's constants interface rounds it up to 1. That changes tcpa's magnitude in the model but not its sign, since the denominator stays positive, so the tcpa >= 0 guard of LRE-Beh8 and LRE-Beh14 is unaffected. cda in the model differs from the Java value.

**java_trace:**

- `src/main/java/lre/LreConstants.java` lines 36 — MIN_REL_SPEED_SQ — LRE-OP5, LRE-Beh8, LRE-Beh14

### 6. [design_choice] CheckOPEZ and CalcCPA re-read the Sensor's selection functions

**raw:** LRE-OP2 says CheckOPEZ uses the index computed by CalcCStc, and LRE-OP5 says CalcCPA uses cdyn. Profile section 5 limits compute() right-hand sides to sensor calls and the class's own fields.

**fix_directive:** Implemented: CheckOPEZ calls sensor.odist(sensor.closestStaticIndex()), and CalcCPA calls the accessors with sensor.closestDynamicIndex(). These are the functions CalcCStc and CalcCDyn call, so within a step they give the same index. Neither class reads the other operations' getters.

**java_trace:**

- `src/main/java/lre/operation/CheckOPEZ.java` lines 22-25 — compute — LRE-OP2, LRE-OP3
- `src/main/java/lre/operation/CalcCPA.java` lines 31-42 — compute — LRE-OP5, LRE-OP4

### 7. [design_choice] Transition priorities within each mode

**raw:** Every transition requirement has priority 8, so the requirements do not order transitions that can be enabled together.

**fix_directive:** Implemented order. MOM: reqOCM, inOpez, collision course (CAM), endTask, reqHCM, Beh9, Beh11, Beh10. HCM: reqOCM, inOpez, collision course, Beh13. CAM: reqOCM, Beh18. OCM: reqVel, reqHdng, reqMOM. Overrides that apply in several modes come first, and OPEZ precedes collision avoidance. Beh11 precedes Beh10 because with equal thresholds Beh10's guard subsumes Beh11's; the reverse order would make Beh11 unreachable. Lower branches conjoin the negations of the guard-only branches above them, so endTask and reqHCM are not taken while inOpez or a collision course holds.

**java_trace:**

- `src/main/java/lre/controller/LreController.java` lines 92-136 — step — LRE-Beh5, LRE-Beh6, LRE-Beh7, LRE-Beh8, LRE-Beh9, LRE-Beh10, LRE-Beh11, LRE-Beh12, LRE-Beh13, LRE-Beh14, LRE-Beh15, LRE-Beh16, LRE-Beh17, LRE-Beh18

### 8. [ambiguous_requirement] LRE-DC1 versus several trigger-less transitions from one mode

**raw:** LRE-DC1 forbids duplicate transitions for the same source mode and trigger event. MOM has five transitions without a trigger event (Beh5, Beh8, Beh9, Beh10, Beh11) and HCM has three (Beh13, Beh14, Beh16), while DC1 also requires one transition per requirement.

**fix_directive:** Interpreted DC1 as applying to event triggers. Each behavioural requirement is one branch, and the trigger-less branches differ by guard. No event appears twice as a trigger from the same mode.

**java_trace:**

- `src/main/java/lre/controller/LreController.java` lines 92-117 — step — LRE-DC1, LRE-Beh9, LRE-Beh10, LRE-Beh11

### 9. [ambiguous_requirement] vvel is the signed rate of climb

**raw:** LRE-OP1 says vvel is the rate of climb, and LRE-Beh11 tests vvel >= 1. Read literally, only a climb of 1 m/s or more triggers Beh11; a fast descent toward a static obstacle below does not.

**fix_directive:** Implemented literally, vvel = rateOfClimb. A descent is still covered by Beh10 (vdist <= staticObsDfltVertDist, regardless of velocity). If the magnitude was intended, CalcVel would need an absolute value, which profile section 5 does not offer.

**java_trace:**

- `src/main/java/lre/operation/CalcVel.java` lines 21 — compute — LRE-OP1, LRE-Var3, LRE-Beh11

### 10. [scope_question] CAM's evasive manoeuvre has no specified output

**raw:** LRE-FR4 says the AUV performs evasive manoeuvres in CAM. No requirement gives the advice to issue, and LRE-DM7 limits outputs to advVel and advHdng.

**fix_directive:** Implemented: no advice is issued on entering CAM, and CAM has no entry action. Specify the advVel/advHdng values for CAM if the LRE should command the manoeuvre.

**java_trace:**

- `src/main/java/lre/controller/LreController.java` lines 97-98 — step (MOM to CAM) — LRE-FR4, LRE-Beh8, LRE-Beh14

### 11. [design_choice] Named constants for the literal speeds and the Beh4 clearance

**raw:** LRE-FR2, LRE-FR3, LRE-Beh4, LRE-Beh7, LRE-Beh9, LRE-Beh11 and LRE-Beh18 state literal values (1 m/s, 0 m/s, 1 m) without naming them.

**fix_directive:** Implemented as LreConstants fields. MOM_SPEED = 1.0 is the advised velocity in MOM and the Beh4 vel ceiling. APPROACH_SPEED = 1.0 is the Beh9/Beh11 velocity threshold. HALT_SPEED = 0.0. MOM_ENTRY_CLEARANCE = 1.0 is the Beh4 odist bound, kept separate from MIN_SAFE_DIST although the two are equal. The surface depth and the tcpa bound (0) are literals.

**java_trace:**

- `src/main/java/lre/LreConstants.java` lines 24-33 — MOM_SPEED, HALT_SPEED, APPROACH_SPEED, MOM_ENTRY_CLEARANCE — LRE-FR2, LRE-FR3, LRE-Beh4, LRE-Beh9, LRE-Beh11

### 12. [invented_default] Initial sensor and actuator values; null as 'no event'

**raw:** The requirements give no initial readings and no initial advice, and do not say how a cycle without an operator command is presented.

**fix_directive:** Implemented: the Sensor's raw data are 0 until the first update(). Depth 0 makes inOpez true, so MOM cannot be entered before a reading below the surface. The Actuator's latest advice starts at 0. step(null) is a cycle with no operator command, in which only autonomous transitions can fire.

**java_trace:**

- `src/main/java/lre/sensor/Sensor.java` lines 18-21 — depth, nsVel, ewVel, rateOfClimb — LRE-DM5, LRE-OP2
- `src/main/java/lre/actuator/Actuator.java` lines 12-13 — latestVelocity, latestHeading — LRE-DM8

### 13. [design_choice] Obstacle field names and tie-breaking

**raw:** LRE-DM2 names the Obstacle fields in snake_case. LRE-OP3 and LRE-OP4 do not say which obstacle is chosen when two are equally close.

**fix_directive:** Implemented: the record components keep the requirement's snake_case names (ns_rel_dist and so on), which also keeps them distinct from the Sensor accessors nsRelDist and so on. When two obstacles are equally close, the lowest identifier is chosen.

**java_trace:**

- `src/main/java/lre/sensor/Obstacle.java` lines 10-16 — Obstacle — LRE-DM2
- `src/main/java/lre/sensor/Sensor.java` lines 128-139 — closestOf — LRE-OP3, LRE-OP4

## Next step

Run the pipeline (compile, coverage, profile check, transformations, verifiers). The issues above are open questions for the requirements owner, not blockers.
