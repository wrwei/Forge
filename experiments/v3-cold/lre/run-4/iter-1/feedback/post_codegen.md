# post_codegen

**status:** uncertain

**summary:** Java for all 51 LRE requirements written under lre/ (controller, 5 operations, sensor, register, actuator, events, constants); compiles with javac --release 17. 13 points the requirements leave open are listed below; none blocks the pipeline.

## Issues

### 1. [ambiguous_requirement] "-1 means no obstacle" conflicts with natural-number indices

- **raw:** LRE-Var5/Var6 declare cstc and cdyn as natural numbers yet say -1 means no obstacle; LRE-DM5/OP3/OP4 have closestStaticIndex()/closestDynamicIndex() return -1 when none exists. Java int becomes nat in the model and may never be negative.
- **fix_directive:** Kept the indices natural: ObstacleRegister.NO_OBSTACLE = 0 is the 'none' value, and register identifiers start at 1 (put rejects index 0). The distance functions return the safe large distance for index 0, so the guards behave as the requirements intend for -1. Confirm 1-based identifiers are acceptable.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/sensor/ObstacleRegister.java` L19-32 `NO_OBSTACLE, put` (LRE-DM3, LRE-Var5, LRE-Var6)
  - `java.generated.project/src/main/java/lre/sensor/Sensor.java` L131-152 `closestStaticIndex, closestDynamicIndex` (LRE-DM5, LRE-OP3, LRE-OP4)

### 2. [invented_default] Value of the 'safe large distance'

- **raw:** LRE-DM5 requires odist/hdist/vdist to return 'a safe large distance' when no obstacle exists, without giving a value.
- **fix_directive:** Chose 1000.0 m (Sensor.NO_OBSTACLE_DIST, private to the sensor), far above every 1 m threshold. Field accessors return 0.0 as DM5 states.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/sensor/Sensor.java` L16-16 `NO_OBSTACLE_DIST` (LRE-DM5, LRE-OP2, LRE-OP5)

### 3. [ambiguous_requirement] HCM advised velocity: 0 m/s (LRE-FR3) vs 0.1 m/s (system description)

- **raw:** The system description says HCM runs at reduced speed 0.1 m/s; LRE-FR3 says on entering HCM the LRE advises 0 m/s.
- **fix_directive:** Followed the requirement: HCM_VEL = 0.0. (0.1 would also round up to 1 in the model's constants, equal to MOM_VEL.)
- **java_trace:**
  - `java.generated.project/src/main/java/lre/LreConstants.java` L27-27 `HCM_VEL` (LRE-FR3)

### 4. [design_choice] CPA formulation in CalcCPA

- **raw:** LRE-OP5 names the inputs (relative position, obstacle velocity) but not the formula.
- **fix_directive:** Linear horizontal motion with closing velocity = AUV velocity - obstacle velocity; tcpa = (p.w)/(w.w); cda = sqrt of the squared horizontal miss distance plus vdist^2 (vertical separation taken as constant, obs_roc unused). A sum of squares, so never sqrt of a negative; with no dynamic obstacle vdist is the large default, so cda is large. If the closing velocity is exactly zero, tcpa and cda are NaN in Java: Beh8/Beh14 are false and Beh18 is true (no closing motion, no collision course). The division has no provably non-zero divisor if a verifier interprets it.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/operation/CalcCPA.java` L31-43 `compute` (LRE-OP5, LRE-Var7, LRE-Var8, LRE-Beh8, LRE-Beh14, LRE-Beh18)

### 5. [design_choice] Operations read the closest indices from the sensor, not from CalcCStc/CalcCDyn

- **raw:** LRE-OP2 and LRE-OP5 use the index computed by CalcCStc/CalcCDyn; the profile lets compute() use only sensor calls and the operation's own fields.
- **fix_directive:** CheckOPEZ and CalcCPA call sensor.closestStaticIndex()/closestDynamicIndex(), the same functions CalcCStc/CalcCDyn assign, so within one step the index is identical. step() runs CalcVel, CalcCStc, CheckOPEZ, CalcCDyn, CalcCPA before the guards.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/operation/CheckOPEZ.java` L23-26 `compute` (LRE-OP2)
  - `java.generated.project/src/main/java/lre/controller/LreController.java` L53-57 `step` (LRE-OP1, LRE-OP2, LRE-OP3, LRE-OP4, LRE-OP5)

### 6. [design_choice] State variables live in the operation classes

- **raw:** LRE-Var1..8 say 'the controller maintains' each variable; each is also 'set by' an operation.
- **fix_directive:** Each variable is a field of the operation that computes it, exposed by a getter of the same name; the controller owns the operations and its guards read the getters. No duplicate controller fields.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/controller/LreController.java` L60-75 `step (predicates)` (LRE-Var1, LRE-Var2, LRE-Var3, LRE-Var4, LRE-Var5, LRE-Var6, LRE-Var7, LRE-Var8, LRE-GP1)

### 7. [design_choice] Transition priority within MOM and HCM

- **raw:** All transition requirements share priority 8; none orders transitions that can be enabled together.
- **fix_directive:** Order: reqOCM (operator override) first; then inOpez -> OCM; then collision course -> CAM; then endTask / reqHCM; then the autonomous HCM triggers. Lower branches conjoin the negations of earlier guard-only branches. Among the HCM triggers Beh9, Beh11, Beh10 are ordered so that Beh11 stays satisfiable while staticObsVertDist = staticObsDfltVertDist; all three have the same target and action.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/controller/LreController.java` L96-143 `step` (LRE-Beh5, LRE-Beh6, LRE-Beh7, LRE-Beh8, LRE-Beh9, LRE-Beh10, LRE-Beh11, LRE-Beh12, LRE-Beh13, LRE-Beh14, LRE-Beh15, LRE-Beh16)

### 8. [ambiguous_requirement] Beh9/10/11 vs LRE-DC1

- **raw:** LRE-DC1 forbids duplicate transitions for the same source mode and trigger, while Beh9, Beh10 and Beh11 are three triggerless MOM -> HCM transitions.
- **fix_directive:** Read DC1 as one transition per requirement: three separate branches, made mutually exclusive by negated guards.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/controller/LreController.java` L114-127 `step` (LRE-DC1, LRE-Beh9, LRE-Beh10, LRE-Beh11)

### 9. [scope_question] HCM -> MOM oscillation if staticObsDfltVertDist > staticObsVertDist

- **raw:** Beh13 (HCM -> MOM) needs vdist > staticObsVertDist; Beh10 (MOM -> HCM) needs vdist <= staticObsDfltVertDist. Both autonomous.
- **fix_directive:** With the required defaults (both 1.0) the guards exclude each other. If the default vertical threshold were set larger, vdist between the two thresholds would cycle MOM <-> HCM without an event. Not guarded further, to keep Beh13 as written.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/LreConstants.java` L18-21 `STATIC_OBS_VERT_DIST, STATIC_OBS_DFLT_VERT_DIST` (LRE-DM4, LRE-Beh10, LRE-Beh13)

### 10. [design_choice] Literal thresholds as their own constants

- **raw:** LRE-Beh4 uses literal 1 (vel <= 1, odist > 1); Beh9/Beh11 use 1 m/s; Beh8/14 use tcpa >= 0; OP2 uses depth <= 0.
- **fix_directive:** Each is a separate constant (MOM_ENTRY_MAX_VEL, MOM_ENTRY_MIN_DIST, HIGH_HORIZ_VEL, HIGH_VERT_VEL, MIN_TCPA, SURFACE_DEPTH) rather than reusing minSafeDist; values as written. All constants are whole numbers, so the model's rounding does not change them.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/LreConstants.java` L33-48 `LreConstants` (LRE-Beh4, LRE-Beh8, LRE-Beh9, LRE-Beh11, LRE-Beh14, LRE-OP2)

### 11. [ambiguous_requirement] vvel is the signed rate of climb

- **raw:** LRE-OP1: vvel is the rate of climb. LRE-Beh11 triggers on vvel >= 1 m/s.
- **fix_directive:** Implemented literally: only climbing at 1 m/s or more triggers Beh11; descending fast towards an obstacle below does not (Beh10 still covers vdist <= staticObsDfltVertDist).
- **java_trace:**
  - `java.generated.project/src/main/java/lre/operation/CalcVel.java` L24-24 `compute` (LRE-OP1, LRE-Var3, LRE-Beh11)

### 12. [unimplemented] CAM evasive manoeuvre has no output

- **raw:** LRE-FR4: in CAM the AUV performs evasive manoeuvres. LRE-DM7 allows only advVel and advHdng and no requirement says what to advise in CAM.
- **fix_directive:** CAM issues no advice; its only behaviour is the exits Beh17/Beh18. An evasive velocity/heading needs a requirement.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/controller/LreController.java` L145-153 `step` (LRE-FR4, LRE-Beh8, LRE-Beh14)

### 13. [invented_default] Initial values and the no-input cycle

- **raw:** No requirement gives initial sensor, actuator or operation values, nor how a cycle without operator input is presented.
- **fix_directive:** Sensor readings and actuator advice start at 0.0 (depth 0 means at the surface, hence in OPEZ until the first reading). Operation results start at 0/false; step() recomputes them before any guard, so they are never observed. step(null) is a cycle with no operator event.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/sensor/Sensor.java` L19-22 `depth, nsVel, ewVel, rateOfClimb` (LRE-DM5)
  - `java.generated.project/src/main/java/lre/actuator/Actuator.java` L12-13 `lastVelocity, lastHeading` (LRE-DM8)

## Next step

Run the pipeline (compile, coverage, profile check, transformations, verifiers) and review these choices against its feedback.
