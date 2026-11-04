# post_codegen

**status:** uncertain

**summary:** LRE controller generated: 15 Java files, all 51 requirements traced, 17 transitions (one per requirement LRE-Beh2..Beh18). Flagged 11 points the requirements left open; none blocks the pipeline.

## Issues

### 1. [ambiguous_requirement] 'No obstacle' index is 0, not -1

- **raw:** LRE-Var5/Var6 declare cstc and cdyn as natural numbers but say -1 means 'no obstacle'; LRE-DM5, OP3 and OP4 return -1 when there is none. The profile maps int to nat and forbids negative values and negative 'none' markers.
- **fix_directive:** Kept the nat type and used ObstacleRegister.NO_OBSTACLE = 0 as the 'none' index. The register rejects identifier 0, so real obstacles are numbered from 1. Every sensor function treats index 0 as 'no obstacle' and returns the defaults DM5 gives for that case.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/sensor/ObstacleRegister.java` lines 19-19, `NO_OBSTACLE` — LRE-DM3, LRE-Var5, LRE-Var6
  - `java.generated.project/src/main/java/lre/sensor/ObstacleRegister.java` lines 24-32, `put` — LRE-DM3
  - `java.generated.project/src/main/java/lre/sensor/Sensor.java` lines 133-158, `closestStaticIndex/closestDynamicIndex` — LRE-DM5, LRE-OP3, LRE-OP4

### 2. [invented_default] Safe large distance is 1000000.0 m

- **raw:** LRE-DM5 says odist, hdist and vdist return 'a safe large distance' when there is no obstacle, but gives no value.
- **fix_directive:** Used a private sensor constant SAFE_DISTANCE = 1000000.0. It stays inside Sensor, so it does not appear in the model.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/sensor/Sensor.java` lines 14-14, `SAFE_DISTANCE` — LRE-DM5, LRE-OP2, LRE-OP5

### 3. [design_choice] How CalcCPA computes cda and tcpa

- **raw:** LRE-OP5 does not give a formula. It says the sensor returns 'zero-velocity and large-distance defaults' when there is no dynamic obstacle, while LRE-DM5 says the field accessors return zero. A pure horizontal CPA built from zero defaults would give cda = 0 and enter CAM when there is no obstacle at all.
- **fix_directive:** With closing velocity c = (AUV velocity - obstacle velocity): tcpa = (p.c)/(|c|^2 + MIN_REL_SPEED_SQ) and cda = sqrt(|p - c*tcpa|^2 + vdist^2). This is the horizontal CPA, assuming the vertical separation stays constant. Without a dynamic obstacle, vdist returns the safe large distance, so cda is large. MIN_REL_SPEED_SQ = 0.000001 is an invented constant that keeps the division defined. Note that the model's constants interface rounds it up to 1, so the model's cda and tcpa values differ slightly from Java's.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/operation/CalcCPA.java` lines 31-45, `compute` — LRE-OP5, LRE-Var7, LRE-Var8
  - `java.generated.project/src/main/java/lre/controller/LreConstants.java` lines 45-45, `MIN_REL_SPEED_SQ` — LRE-OP5

### 4. [design_choice] CheckOPEZ and CalcCPA get the closest index from the sensor themselves

- **raw:** LRE-OP2 says CheckOPEZ uses the index CalcCStc computes, and LRE-OP5 says CalcCPA uses cdyn. The profile allows a compute() right-hand side to use only sensor calls, constants, arithmetic and the class's own fields, not another operation's getter.
- **fix_directive:** CheckOPEZ calls sensor.odist(sensor.closestStaticIndex()), and CalcCPA calls sensor.closestDynamicIndex(). These are the same selection functions CalcCStc and CalcCDyn call, evaluated in the same step, so the value is identical. The controller runs CalcCStc before CheckOPEZ and CalcCDyn before CalcCPA.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/operation/CheckOPEZ.java` lines 24-27, `compute` — LRE-OP2, LRE-OP3
  - `java.generated.project/src/main/java/lre/operation/CalcCPA.java` lines 31-33, `compute` — LRE-OP4, LRE-OP5

### 5. [ambiguous_requirement] Velocity advised in HCM: 0 or 0.1 m/s

- **raw:** The system description gives HCM a reduced speed of 0.1 m/s; LRE-FR3 says entering HCM advises 0 m/s.
- **fix_directive:** Followed the requirement: HCM_ADVISED_VEL = 0.0, issued on every transition into HCM.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/controller/LreConstants.java` lines 33-33, `HCM_ADVISED_VEL` — LRE-FR3
  - `java.generated.project/src/main/java/lre/controller/LreController.java` lines 108-124, `step` — LRE-Beh9, LRE-Beh10, LRE-Beh11, LRE-Beh12

### 6. [design_choice] Priority of transitions within a mode

- **raw:** The requirements give every transition the same priority (8).
- **fix_directive:** Order chosen, highest first. MOM: reqOCM, endTask, inOpez, collision (CAM), Beh9, Beh11, Beh10, reqHCM. HCM: reqOCM, inOpez, collision, Beh13. CAM: reqOCM, then Beh18. Each branch below a guard-only branch has the negations of the guards above it added. Beh11 comes before Beh10 because STATIC_OBS_VERT_DIST = STATIC_OBS_DFLT_VERT_DIST; in the other order Beh11 could never fire. reqHCM is last, so an autonomous safety transition always takes precedence over it.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/controller/LreController.java` lines 94-152, `step` — LRE-Beh5, LRE-Beh6, LRE-Beh7, LRE-Beh8, LRE-Beh9, LRE-Beh10, LRE-Beh11, LRE-Beh12, LRE-Beh13, LRE-Beh14, LRE-Beh15, LRE-Beh16, LRE-Beh17, LRE-Beh18, LRE-DC1

### 7. [scope_question] CAM issues no evasive command

- **raw:** LRE-FR4 says the AUV performs evasive manoeuvres in CAM, but specifies no output; LRE-DM7 allows only advVel and advHdng.
- **fix_directive:** CAM has no entry action. The LRE only leaves CAM on reqOCM (Beh17) or once cda >= minSafeDist (Beh18, which advises 0).
- **java_trace:**
  - `java.generated.project/src/main/java/lre/controller/LreController.java` lines 105-107, `step` — LRE-FR4, LRE-Beh8
  - `java.generated.project/src/main/java/lre/controller/LreController.java` lines 143-152, `step` — LRE-FR4, LRE-Beh17, LRE-Beh18

### 8. [ambiguous_requirement] vvel is the signed rate of climb

- **raw:** LRE-OP1 says vvel is the rate of climb. LRE-Beh11 tests vvel >= 1 m/s, so it fires only when climbing fast, not when descending fast.
- **fix_directive:** Implemented exactly as written (vvel = rateOfClimb, signed). Beh10, which ignores velocity, still covers a static obstacle that is vertically close.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/operation/CalcVel.java` lines 23-27, `compute` — LRE-OP1, LRE-Var3, LRE-Beh11

### 9. [design_choice] Literal thresholds in Beh4, Beh9 and Beh11 are separate constants

- **raw:** Beh4 says vel <= 1 and odist > 1; Beh9 and Beh11 say >= 1 m/s. These are not the DM4 thresholds.
- **fix_directive:** Named them VEL_LIMIT = 1.0 and OBS_CLEARANCE = 1.0, separate from MIN_SAFE_DIST. Zero values are named too: STOP_VEL, SURFACE_DEPTH and MIN_TCPA.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/controller/LreConstants.java` lines 24-42, `LreConstants` — LRE-Beh4, LRE-Beh9, LRE-Beh11, LRE-Beh7, LRE-Beh18, LRE-OP2, LRE-Beh8, LRE-Beh14

### 10. [invented_default] step(null) means no operator input; actuator values start at 0

- **raw:** The requirements define no 'no input' event and no initial advice.
- **fix_directive:** Calling step with null runs only the autonomous transitions; no extra event was added (DM6 lists exactly six inputs). The actuator reports 0 velocity and 0 heading until the first advice.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/controller/LreController.java` lines 51-76, `step` — LRE-DM6, LRE-ARCH2
  - `java.generated.project/src/main/java/lre/actuator/Actuator.java` lines 12-13, `advisedVelocity/advisedHeading` — LRE-DM8

### 11. [ambiguous_requirement] DC1 and trigger-less transitions

- **raw:** LRE-DC1 forbids duplicate transitions for the same source mode and trigger. MOM has five transitions with no trigger event (Beh5, Beh8, Beh9, Beh10, Beh11), three of them to HCM.
- **fix_directive:** Read DC1 as one transition per requirement. Each of those transitions has a distinct guard, and the negated guards make them mutually exclusive.
- **java_trace:**
  - `java.generated.project/src/main/java/lre/controller/LreController.java` lines 102-119, `step` — LRE-DC1, LRE-Beh5, LRE-Beh8, LRE-Beh9, LRE-Beh10, LRE-Beh11

## next_step

Run the pipeline. If a verifier disagrees with one of these choices, revisit the corresponding issue.
