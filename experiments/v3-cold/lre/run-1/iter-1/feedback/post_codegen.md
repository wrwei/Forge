# Codegen review (Layer 5)

**status:** uncertain

**summary:** LRE controller written in five layers: 15 Java files under lre/ (annotation, constants, events, sensor with obstacle register, actuator, five operations, mode enum, controller). All 51 requirements are traced; the code compiles under --release 17. 12 points the requirements leave open are listed below, none of which blocks compilation.

## Issues

### 1. [ambiguous_requirement] The no-obstacle index is a non-negative sentinel, not -1

- **raw:** LRE-Var5 and LRE-Var6 declare cstc and cdyn natural numbers but say -1 means no obstacle; LRE-DM5, LRE-OP3 and LRE-OP4 say closestStaticIndex() and closestDynamicIndex() return -1. The profile maps int to nat and forbids a negative value meaning 'none'.
- **fix_directive:** Resolved in code: both selection functions return ObstacleRegister.NO_INDEX (Integer.MAX_VALUE), which the register refuses as a key, so every distance and field query at that index takes the sensor's no-obstacle defaults. The controller never tests the sentinel. If an observable -1 is needed, the requirement must give cstc/cdyn a signed type.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/sensor/ObstacleRegister.java` lines 14-14, `NO_INDEX`: LRE-Var5, LRE-Var6, LRE-DM3
  - `java.generated.project/src/main/java/lre/sensor/Sensor.java` lines 127-157, `closestStaticIndex, closestDynamicIndex`: LRE-DM5, LRE-OP3, LRE-OP4

### 2. [invented_default] Safe large distance for an absent obstacle is 100000.0 m

- **raw:** LRE-DM5, LRE-OP2 and LRE-OP5 require odist, hdist and vdist to return 'a safe large distance' when no obstacle exists, without a value.
- **fix_directive:** Chosen 100000.0 m (private to Sensor). It exceeds every threshold (all 1 m), so no guard fires on an absent obstacle. Replace it if the requirements give a value.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/sensor/Sensor.java` lines 14-14, `NO_OBSTACLE_DIST`: LRE-DM5
  - `java.generated.project/src/main/java/lre/sensor/Sensor.java` lines 60-90, `hdist, vdist, odist`: LRE-DM5, LRE-SF1, LRE-SF2, LRE-SF3

### 3. [design_choice] Closest-point-of-approach formula for cda and tcpa

- **raw:** LRE-OP5 gives no formula. LRE-DM5 makes the field accessors return zero when there is no dynamic obstacle, so a purely horizontal CPA would give cda = 0 and an unconditional MOM->CAM transition whenever no dynamic obstacle exists.
- **fix_directive:** Chosen: p = (nsRelDist, ewRelDist) of cdyn (obstacle minus AUV), v = obstacle velocity minus AUV velocity; tcpa = -(p.v) / (|v|^2 + CPA_SPEED_EPSILON); cda = sqrt(|p + v*tcpa|^2 + vdist(cdyn)^2), i.e. the vertical separation is held constant (DM5 exposes no obstacle rate-of-climb accessor). With no dynamic obstacle, tcpa = 0 and cda = the large distance. cda is evaluated at tcpa even when tcpa < 0, so after a close pass the LRE stays in CAM until the geometry changes or reqOCM arrives (LRE-Beh18 tests cda only). Confirm, or give the intended formula.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/operation/CalcCPA.java` lines 27-47, `compute`: LRE-OP5, LRE-Var7, LRE-Var8, LRE-Beh8, LRE-Beh14, LRE-Beh18

### 4. [invented_default] CPA_SPEED_EPSILON = 0.000001 m^2/s^2 rounds up to 1 in the model

- **raw:** tcpa divides by the squared relative speed, which is zero whenever the obstacle (or, with no obstacle, the AUV) is not moving relative to the other. A small positive term keeps the division defined without a conditional, which compute() may not contain.
- **fix_directive:** The exact value matters numerically: the model's constants interface rounds this fractional constant up to 1, so in the model tcpa's magnitude, and hence cda, differ from the Java. tcpa's sign, which the guards test, is unaffected. Accept the difference, or have the requirements specify how a zero relative speed is handled.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/LreConstants.java` lines 34-35, `CPA_SPEED_EPSILON`: LRE-OP5

### 5. [design_choice] CheckOPEZ and CalcCPA read the closest index from the sensor, not from CalcCStc/CalcCDyn

- **raw:** LRE-OP2 says CheckOPEZ uses the index computed by CalcCStc; LRE-OP5 says CalcCPA uses cdyn. The operation profile allows only sensor calls, Math.sqrt, arithmetic, comparisons and the class's own fields in compute(), and a second field named cstc/cdyn would duplicate a model name.
- **fix_directive:** Both operations call sensor.closestStaticIndex() / sensor.closestDynamicIndex() directly. In the same step, on the same sensor snapshot, this is the value CalcCStc/CalcCDyn assign. The controller runs CalcCStc and CalcCDyn before CheckOPEZ and CalcCPA.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/operation/CheckOPEZ.java` lines 19-22, `compute`: LRE-OP2, LRE-Var1
  - `java.generated.project/src/main/java/lre/operation/CalcCPA.java` lines 27-47, `compute`: LRE-OP5

### 6. [ambiguous_requirement] HCM velocity: 0 m/s (LRE-FR3) or 0.1 m/s (system description)

- **raw:** The system description says HCM runs at reduced speed 0.1 m/s; LRE-FR3 says the LRE advises 0 m/s on entering HCM.
- **fix_directive:** Followed LRE-FR3 (the requirements file is the source of truth): HCM_ADVISED_VEL = 0.0. Change the constant if 0.1 m/s is meant.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/LreConstants.java` lines 19-20, `HCM_ADVISED_VEL`: LRE-FR3

### 7. [unimplemented] CAM's evasive manoeuvre is not specified

- **raw:** LRE-FR4 says the AUV performs evasive manoeuvres in CAM, but no requirement says what the LRE advises (LRE-DM7 allows only advVel and advHdng).
- **fix_directive:** CAM has no entry action. In CAM the LRE ignores operator velocity and heading (LRE-Beh19), so the autopilot keeps the last advice (0 m/s if CAM was entered from HCM, 1 m/s if from MOM). Give the CAM entry advice to complete it.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/controller/LreController.java` lines 96-98, `step (MOM->CAM)`: LRE-FR4, LRE-Beh8
  - `java.generated.project/src/main/java/lre/controller/LreController.java` lines 129-131, `step (HCM->CAM)`: LRE-FR4, LRE-Beh14

### 8. [design_choice] Transition priority within each mode

- **raw:** All behaviour requirements have priority 8, so the order of a mode's transitions is not given.
- **fix_directive:** Order chosen: reqOCM first in MOM, HCM and CAM (operator override); then inOpez->OCM; then collision course->CAM; then the events endTask and reqHCM; then the autonomous MOM->HCM transitions in the order Beh9, Beh11, Beh10. With both vertical thresholds at 1, Beh10 (vdist <= dfltVert) subsumes Beh11 (vvel >= 1 and vdist <= vert), so Beh10 placed first would leave Beh11 unreachable once earlier guards are negated. Every branch below a predicate-only branch conjoins that guard's negation. LRE-DC1 is read as 'no two transitions with the same source, trigger and guard': Beh9, Beh10 and Beh11 are three triggerless MOM->HCM transitions with distinct guards.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/controller/LreController.java` lines 89-147, `step`: LRE-Beh5, LRE-Beh6, LRE-Beh7, LRE-Beh8, LRE-Beh9, LRE-Beh10, LRE-Beh11, LRE-Beh12, LRE-Beh13, LRE-Beh14, LRE-Beh15, LRE-Beh16, LRE-Beh17, LRE-Beh18, LRE-DC1

### 9. [ambiguous_requirement] LRE-Beh4's thresholds '1' are kept as their own constants

- **raw:** LRE-Beh4 requires vel <= 1 and odist(cdyn), odist(cstc) > 1 rather than naming minSafeDist or the MOM speed.
- **fix_directive:** MOM_ENTRY_MAX_VEL = 1.0 and MOM_ENTRY_MIN_DIST = 1.0, so the values aren't tied to other thresholds. With these values, !inOpez already implies odist(cstc) > 1. Point them at MIN_SAFE_DIST / MOM_ADVISED_VEL if that is the intent.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/LreConstants.java` lines 24-27, `MOM_ENTRY_MAX_VEL, MOM_ENTRY_MIN_DIST`: LRE-Beh4
  - `java.generated.project/src/main/java/lre/controller/LreController.java` lines 72-76, `step (OCM->MOM)`: LRE-Beh4

### 10. [invented_default] Initial sensor readings and actuator advice are 0.0

- **raw:** No requirement gives the readings before the first sensor update or the advice before the first output.
- **fix_directive:** All start at 0.0. Before the first update(), depth = 0, so inOpez holds (depth <= 0) and reqMOM is refused until a positive depth is reported, which is the safe behaviour at the surface.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/sensor/Sensor.java` lines 17-20, `depth, nsVel, ewVel, rateOfClimb`: LRE-DM5, LRE-OP2
  - `java.generated.project/src/main/java/lre/actuator/Actuator.java` lines 8-9, `advisedVelocity, advisedHeading`: LRE-DM8

### 11. [scope_question] vvel is the signed rate of climb

- **raw:** LRE-OP1 says vvel is the rate of climb and LRE-Beh11 tests vvel >= 1, so a fast descent toward an obstacle below does not trigger LRE-Beh11.
- **fix_directive:** Implemented literally (vvel = rate_of_climb). LRE-Beh10 still forces HCM when vdist <= staticObsDfltVertDist regardless of velocity. Using |rate_of_climb| would change LRE-OP1.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/operation/CalcVel.java` lines 18-22, `compute`: LRE-OP1, LRE-Var3, LRE-Beh11

### 12. [design_choice] Classification, tie-break, frame and no-event conventions

- **raw:** LRE-DM2, LRE-DM3, LRE-DM5 and LRE-ARCH2 leave these details open.
- **fix_directive:** An obstacle is static iff obs_ns_vel and obs_ew_vel are exactly 0.0 (no tolerance). On equal odist the lowest index wins. Relative distances are the obstacle's position minus the AUV's. step(null) means no operator input this cycle, so only autonomous transitions can fire. The Obstacle components keep DM2's snake_case names so they stay distinct from the camelCase sensor accessors.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/sensor/Obstacle.java` lines 17-20, `isStatic`: LRE-DM2
  - `java.generated.project/src/main/java/lre/sensor/Sensor.java` lines 147-157, `closestOf`: LRE-DM5, LRE-DM3
  - `java.generated.project/src/main/java/lre/controller/LreController.java` lines 40-45, `step`: LRE-ARCH2

## next_step

Run the pipeline. Issues 3, 4 and 7 (CPA formula, epsilon rounding, CAM advice) are the ones most likely to need an operator decision.
