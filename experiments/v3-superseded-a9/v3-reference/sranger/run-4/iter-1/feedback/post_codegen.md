# post_codegen

**status:** uncertain

**summary:** All 23 requirements implemented in 9 Java files (one controller, 3 modes, 6 transitions plus the initial state). Eight decisions the requirements leave open are listed below; none blocks the pipeline.

## Issues

### 1. [invented_default] Sensor no-reading distance is 1000.0 m

- **raw:** SR-DM5 requires 'a large default value' when no IR reading is available but gives no number.
- **fix_directive:** Chose 1000.0 m (far above obstacleThreshold 0.5 m). Replace if the platform specifies a sentinel.
- **java_trace:** `src/main/java/sranger/sensor/Sensor.java` lines 11-13, element `NO_READING_DISTANCE`, requirement_ids SR-DM5, SR-SF1

### 2. [design_choice] Invalid IR readings (negative or NaN) are ignored

- **raw:** SR-DM5/SR-SF1 state distance() is non-negative; nothing says what to do with a negative measurement.
- **fix_directive:** Sensor.update keeps the previous value when reading < 0 or NaN; clearReading() restores the no-reading default.
- **java_trace:** `src/main/java/sranger/sensor/Sensor.java` lines 21-30, element `update`, requirement_ids SR-DM5

### 3. [ambiguous_requirement] Moving -> Turning: obstacle event and obstacleDetected guard

- **raw:** SR-Beh2 triggers the transition on the obstacle event; SR-GP1 says obstacleDetected (distance <= obstacleThreshold) is the guard on the same transition.
- **fix_directive:** Implemented both: trigger obstacle, condition obstacleDetected. An obstacle event while the distance reads above the threshold is ignored.
- **java_trace:** `src/main/java/sranger/controller/SRangerController.java` lines 52-62, element `step`, requirement_ids SR-Beh2, SR-GP1

### 4. [design_choice] Turning priority: endTask, then timed return, then tick

- **raw:** Requirements give no priority between SR-Beh5 (autonomous), SR-Beh6 (endTask) and SR-Beh7 (tick).
- **fix_directive:** endTask first (stop override); the autonomous turnDurationElapsed transition before tick so a tick each cycle cannot hold the robot in Turning; tick self-loop guarded by !turnDurationElapsed.
- **java_trace:** `src/main/java/sranger/controller/SRangerController.java` lines 67-76, element `step`, requirement_ids SR-Beh5, SR-Beh6, SR-Beh7

### 5. [design_choice] Initial Move(moveVel, 0) issued in the constructor

- **raw:** SR-FR1 requires Moving's entry command on power-up (SR-Beh1); step() has no initial branch.
- **fix_directive:** The constructor issues Move(MOVE_VEL, 0.0) after wiring; every branch entering Moving issues it inline.
- **java_trace:** `src/main/java/sranger/controller/SRangerController.java` lines 33-38, element `SRangerController`, requirement_ids SR-FR1, SR-Beh1

### 6. [scope_question] OBSTACLE_THRESHOLD = 0.5 is rounded up to 1 in the model's constants

- **raw:** The rules state a fractional constant is rounded up in the constants interface. obstacleThreshold is the only fractional constant and its exact value matters to the obstacleDetected guard.
- **fix_directive:** Kept 0.5 as the requirement fixes it. The model compares distance against 1, not 0.5; verification of SR-GP1 is therefore at integer granularity.
- **java_trace:** `src/main/java/sranger/controller/SRangerConstants.java` lines 17, element `OBSTACLE_THRESHOLD`, requirement_ids SR-DM2, SR-GP1

### 7. [design_choice] Clock source and naming

- **raw:** SR-Var1 needs 'the current time'. 'clock' is a reserved word.
- **fix_directive:** Clock (package sranger.time) returns monotonic seconds since construction; the controller field is named timer; clockResetTime = timer.now() and timer.now() - clockResetTime >= TURN_DURATION follow the profile's clock convention.
- **java_trace:** `src/main/java/sranger/time/Clock.java` lines 8-26, element `Clock`, requirement_ids SR-Var1, SR-GP2

### 8. [invented_default] Actuator reports 0.0 / 0.0 before any command

- **raw:** SR-DM6 does not define the stored command before the first Move.
- **fix_directive:** Initialised to 0.0 (stopped); overwritten immediately by the constructor's Move(moveVel, 0).
- **java_trace:** `src/main/java/sranger/actuator/Actuator.java` lines 12-13, element `Actuator`, requirement_ids SR-DM6

## next_step

Run the pipeline; revisit an issue only if a phase reports it.
