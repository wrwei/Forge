# post_codegen

**status:** uncertain

**summary:** All 23 requirements implemented (9 Java files). No requirement unimplemented; 1 invented default, 1 ambiguity and 6 design choices recorded.

## Issues

### 1. [invented_default] No-reading distance is 1000.0 m

- **raw:** SR-DM5 asks for 'a large default value' when no reading is available but gives no number.
- **fix_directive:** If the requirements fix a value, replace NO_READING_DISTANCE in Sensor; any value well above OBSTACLE_THRESHOLD keeps obstacleDetected false on missing data.
- **java_trace:**
  - `src/main/java/sranger/sensor/Sensor.java` lines 10-14, `NO_READING_DISTANCE` — SR-DM5, SR-SF1

### 2. [design_choice] Negative or NaN IR readings are treated as 'no reading'

- **raw:** SR-DM5/SR-SF1 require distance() to be non-negative but do not say how an invalid measurement is handled. Sensor.update maps it to the no-reading default (the robot keeps moving) rather than clamping to 0 (which would trigger a turn).
- **fix_directive:** Change Sensor.update if invalid readings must instead be clamped or rejected.
- **java_trace:**
  - `src/main/java/sranger/sensor/Sensor.java` lines 16-26, `update` — SR-DM5, SR-SF1

### 3. [design_choice] obstacleThreshold = 0.5 m is fractional and is rounded up in the model

- **raw:** The model's constants interface rounds fractional constants up, so the guard obstacleDetected (distance() <= obstacleThreshold) is checked in the model against 1, not 0.5. The Java keeps metres and 0.5 as SR-DM2 states; units were not rescaled.
- **fix_directive:** None in Java; read verification results for SR-GP1 with the rounded threshold in mind.
- **java_trace:**
  - `src/main/java/sranger/controller/SRangerConstants.java` lines 17-17, `OBSTACLE_THRESHOLD` — SR-DM2, SR-GP1
  - `src/main/java/sranger/controller/SRangerController.java` lines 44-44, `obstacleDetected` — SR-GP1

### 4. [ambiguous_requirement] Moving -> Turning: trigger event and guard both stated

- **raw:** SR-Beh2 fires the transition on the obstacle event; SR-GP1 says obstacleDetected is the guard of the same transition. Implemented as trigger Obstacle with condition obstacleDetected (both).
- **fix_directive:** Drop '&& obstacleDetected' if the event alone must trigger the transition.
- **java_trace:**
  - `src/main/java/sranger/controller/SRangerController.java` lines 51-54, `step` — SR-Beh2, SR-GP1, SR-FR2

### 5. [design_choice] Power-up entry command issued in the constructor

- **raw:** SR-FR1 says Move(moveVel, 0) is issued on entering Moving; Moving is entered at power-up (SR-Beh1) without a transition, so the constructor issues it.
- **fix_directive:** None unless the power-up command should not be issued.
- **java_trace:**
  - `src/main/java/sranger/controller/SRangerController.java` lines 30-35, `SRangerController` — SR-FR1, SR-Beh1

### 6. [design_choice] Clock source and clock reset order

- **raw:** The current time comes from a Clock (monotonic System.nanoTime, seconds since creation); the controller field is named 'timer' because 'clock' is reserved. On Moving -> Turning, clockResetTime is set before Move(0, turnVel) is issued.
- **fix_directive:** None.
- **java_trace:**
  - `src/main/java/sranger/time/Clock.java` lines 8-17, `Clock` — SR-Var1, SR-GP2
  - `src/main/java/sranger/controller/SRangerController.java` lines 52-54, `step` — SR-Var1, SR-FR2

### 7. [design_choice] Priority in Turning and ignored events

- **raw:** Turning: endTask first, then the autonomous turnDurationElapsed transition, then tick (conjoined with !turnDurationElapsed). Obstacle in Turning and every event in Final are ignored (no transition), as no requirement defines them.
- **fix_directive:** None.
- **java_trace:**
  - `src/main/java/sranger/controller/SRangerController.java` lines 59-72, `step` — SR-Beh5, SR-Beh6, SR-Beh7, SR-DC1, SR-FR3

### 8. [design_choice] Sensor and Actuator are concrete classes

- **raw:** SR-ARCH2 calls them interfaces; they are final classes (the profile recognises sensor classes by name), each with one implementation.
- **fix_directive:** None.
- **java_trace:**
  - `src/main/java/sranger/sensor/Sensor.java` lines 8-36, `Sensor` — SR-ARCH2, SR-DM5
  - `src/main/java/sranger/actuator/Actuator.java` lines 9-34, `Actuator` — SR-ARCH2, SR-DM6

## next_step

Run the pipeline; no Java change is required by this review.
