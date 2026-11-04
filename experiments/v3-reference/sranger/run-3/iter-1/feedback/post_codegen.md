# post_codegen

**status:** uncertain

**summary:** All 23 SR requirements implemented in 9 Java files under sranger/; 7 points the requirements did not settle are recorded below.

## Issues

### 1. [invented_default] Sensor no-reading distance is 1000.0 m

- **raw:** SR-DM5 requires a 'large default value' when no reading is available but gives no number. Chosen: 1000.0 m (Sensor.NO_READING_DISTANCE), far above obstacleThreshold. A negative or NaN measurement passed to update() is also treated as 'no reading'.
- **fix_directive:** None unless the requirement fixes a value; change NO_READING_DISTANCE if so.
- **java_trace:**
  - `java.generated.project/src/main/java/sranger/sensor/Sensor.java` lines 12-28, `Sensor.NO_READING_DISTANCE / update`, requirement_ids: SR-DM5, SR-SF1

### 2. [invented_default] Initial values of clockResetTime and the actuator's stored command

- **raw:** clockResetTime starts at 0.0 (it is always reset on entry to Turning before the Turning guard reads it). The Actuator's stored lv/av start at 0.0 before the first command; the constructor immediately overwrites them with Move(moveVel, 0).
- **fix_directive:** None; the values are never observed by a guard before being set.
- **java_trace:**
  - `java.generated.project/src/main/java/sranger/controller/SRangerController.java` lines 20-20, `clockResetTime`, requirement_ids: SR-Var1
  - `java.generated.project/src/main/java/sranger/actuator/Actuator.java` lines 11-12, `lastLinearVelocity / lastAngularVelocity`, requirement_ids: SR-DM6

### 3. [design_choice] Power-up entry action of Moving is issued in the constructor

- **raw:** SR-FR1/SR-Beh1: Moving is initial and its entry issues Move(moveVel, 0). With entry actions written inline on each branch of step(), the power-up entry has no branch; the constructor issues it. The extracted model's initial transition may therefore carry no action.
- **fix_directive:** None unless the model must show the initial Move.
- **java_trace:**
  - `java.generated.project/src/main/java/sranger/controller/SRangerController.java` lines 32-39, `SRangerController(Sensor, Actuator, Clock)`, requirement_ids: SR-FR1, SR-Beh1

### 4. [design_choice] Moving -> Turning needs the obstacle event and obstacleDetected

- **raw:** SR-Beh2 names the obstacle event as trigger; SR-GP1 names obstacleDetected (distance <= obstacleThreshold) as the guard of the same transition. Both are used: trigger obstacle, condition obstacleDetected.
- **fix_directive:** None.
- **java_trace:**
  - `java.generated.project/src/main/java/sranger/controller/SRangerController.java` lines 53-66, `step / obstacleDetected`, requirement_ids: SR-Beh2, SR-GP1

### 5. [design_choice] Tick self-loops perform no action; Turning priority endTask > timeout > tick

- **raw:** SR-Beh4/SR-Beh7 say the tick self-loops perform no action, so the branches do not re-issue Move. In Turning, endTask is first (override), the autonomous turnDurationElapsed transition second, and the tick self-loop last with '&& !turnDurationElapsed' so that the timeout is not masked by the per-cycle tick.
- **fix_directive:** None.
- **java_trace:**
  - `java.generated.project/src/main/java/sranger/controller/SRangerController.java` lines 67-84, `step`, requirement_ids: SR-Beh4, SR-Beh5, SR-Beh6, SR-Beh7

### 6. [ambiguous_requirement] obstacleThreshold 0.5 is fractional; the model constant rounds it up to 1

- **raw:** SR-DM2 fixes obstacleThreshold = 0.5 m. The model's constants interface rounds fractional constants up to an integer, so the model guard reads distance() <= 1 while the Java tests distance() <= 0.5. moveVel, turnVel and turnDuration are whole numbers and unaffected.
- **fix_directive:** The exact value matters to the SR-GP1 guard; the verified model over-approximates the obstacle condition.
- **java_trace:**
  - `java.generated.project/src/main/java/sranger/controller/SRangerConstants.java` lines 14-14, `OBSTACLE_THRESHOLD`, requirement_ids: SR-DM2, SR-GP1

### 7. [scope_question] Final ignores all events

- **raw:** SR-FR3 makes Final terminal and no requirement gives it a transition, so its mode block is empty (time still passes in the model). The Clock is a separate class (field 'timer', since 'clock' is reserved) advanced by the framework.
- **fix_directive:** None.
- **java_trace:**
  - `java.generated.project/src/main/java/sranger/controller/SRangerController.java` lines 86-88, `step (FINAL block)`, requirement_ids: SR-FR3, SR-DM1

## next_step

Run the pipeline (compile, coverage, profile check, transformations, FDR4, Dafny, Isabelle, vacuity).
