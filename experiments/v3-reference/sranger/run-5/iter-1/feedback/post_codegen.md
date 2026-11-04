# post_codegen

**status:** uncertain

**summary:** All 23 requirements implemented in 9 Java files (base package sranger). One invented default and several design choices are recorded below; nothing is unimplemented. SR-DC1 holds by construction: one branch per (mode, trigger) pair.

## Issues

### 1. [invented_default] Sensor 'no reading' distance is 1000.0 m

- **raw:** SR-DM5 requires a 'large default value' when no IR reading is available but gives no number.
- **fix_directive:** None needed unless the specification fixes a value; change Sensor.NO_READING_DISTANCE if it does. Negative or NaN readings are also treated as 'no reading'.
- **java_trace:**
  - `src/main/java/sranger/sensor/Sensor.java` lines 12-29, element `NO_READING_DISTANCE / update`, requirement_ids: SR-DM5, SR-SF1

### 2. [design_choice] Obstacle transition requires both the obstacle event and obstacleDetected

- **raw:** SR-Beh2 triggers Moving->Turning on the obstacle event; SR-GP1 says obstacleDetected (distance <= obstacleThreshold) is the guard on that transition. Both are used: trigger Obstacle, condition obstacleDetected.
- **fix_directive:** If the guard is meant to be implied by the event only, drop '&& obstacleDetected' from the Moving block.
- **java_trace:**
  - `src/main/java/sranger/controller/SRangerController.java` lines 50-62, element `step / obstacleDetected`, requirement_ids: SR-Beh2, SR-GP1

### 3. [design_choice] Turning: autonomous Turning->Moving outranks the tick self-transition

- **raw:** The framework delivers tick every cycle; if the tick self-loop (SR-Beh7) came first, SR-Beh5 could never fire. Order: endTask, turnDurationElapsed, tick && !turnDurationElapsed.
- **fix_directive:** None.
- **java_trace:**
  - `src/main/java/sranger/controller/SRangerController.java` lines 66-77, element `step`, requirement_ids: SR-Beh5, SR-Beh6, SR-Beh7

### 4. [design_choice] endTask is the highest-priority branch in Moving and Turning

- **raw:** endTask is the operator shutdown (SR-DM3); written first in each mode block as an override.
- **fix_directive:** None.
- **java_trace:**
  - `src/main/java/sranger/controller/SRangerController.java` lines 55-69, element `step`, requirement_ids: SR-Beh3, SR-Beh6

### 5. [design_choice] Initial Moving entry command issued in the constructor

- **raw:** SR-FR1 issues Move(moveVel, 0) on entering Moving, including at power-up (SR-Beh1). Java issues it in the constructor; the extracted model's initial transition may not carry this action.
- **fix_directive:** None in Java; note that the model may omit the power-up Move.
- **java_trace:**
  - `src/main/java/sranger/controller/SRangerController.java` lines 31-36, element `SRangerController`, requirement_ids: SR-FR1, SR-Beh1

### 6. [ambiguous_requirement] obstacleThreshold = 0.5 is rounded up to 1 in the model's constants interface

- **raw:** The constants interface rounds fractional constants up; the obstacleDetected guard compares against it, so the model uses distance() <= 1 while Java uses 0.5. distance() is an uninterpreted sensor function, so deadlock/divergence results are unaffected.
- **fix_directive:** None in Java; the Java value is exact.
- **java_trace:**
  - `src/main/java/sranger/controller/SRangerConstants.java` lines 17-17, element `OBSTACLE_THRESHOLD`, requirement_ids: SR-DM2, SR-GP1

### 7. [design_choice] Time source is a settable Clock class read via now()

- **raw:** SR-Var1 needs 'the current time'; a Clock class (field named timer, since 'clock' is reserved) supplies it so clockResetTime becomes a RoboChart clock and turnDurationElapsed becomes since(clockResetTime) >= TURN_DURATION.
- **fix_directive:** None.
- **java_trace:**
  - `src/main/java/sranger/timing/Clock.java` lines 9-30, element `Clock`, requirement_ids: SR-Var1, SR-GP2

## next_step

Run the pipeline; act on verifier feedback.
