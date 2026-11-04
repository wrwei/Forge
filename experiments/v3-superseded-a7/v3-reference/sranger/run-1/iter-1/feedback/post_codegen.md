# post_codegen

**status:** uncertain

**summary:** SRanger controller implemented: 3 modes, 7 transitions (SR-Beh1..7), 2 named predicates, Sensor/Clock/Actuator leaves, 23/23 requirements traced. 8 items flagged: 2 invented defaults, 4 design choices, 1 scope question, 1 ambiguity. Nothing unimplemented.

## Issues

### 1. [invented_default] No-reading IR distance is 1000.0 m

- **raw:** SR-DM5 requires a 'large default value' when no reading is available but gives no number.
- **fix_directive:** Sensor returns NO_READING_DISTANCE = 1000.0 m before the first valid reading and after an invalid (negative or NaN) one, far above obstacleThreshold = 0.5 m. Change the constant if the platform defines a sentinel.
- **java_trace:**
  - `src/main/java/sranger/sensor/Sensor.java` lines 11-27, Sensor.update / NO_READING_DISTANCE, requirements SR-DM5, SR-SF1

### 2. [invented_default] Actuator holds Move(0, 0) before the first command

- **raw:** SR-DM6 does not say what the actuator reports before any command is issued.
- **fix_directive:** The actuator starts at (0.0, 0.0) (stationary). The controller constructor overwrites it straight away with Move(moveVel, 0), so no observable cycle uses it.
- **java_trace:**
  - `src/main/java/sranger/actuator/Actuator.java` lines 11-12, Actuator.lastLinearVelocity / lastAngularVelocity, requirements SR-DM6

### 3. [design_choice] Moving -> Turning needs both the obstacle event and obstacleDetected

- **raw:** SR-Beh2 triggers on the obstacle event; SR-GP1 says obstacleDetected (distance <= obstacleThreshold) is the guard of the same transition.
- **fix_directive:** Implemented as trigger obstacle with condition obstacleDetected. An obstacle event without a reading at or below the threshold is ignored in Moving.
- **java_trace:**
  - `src/main/java/sranger/controller/SRangerController.java` lines 55-66, step: obstacleDetected, MOVING obstacle branch, requirements SR-Beh2, SR-GP1

### 4. [design_choice] Turning priority: endTask, then the autonomous timeout, then tick

- **raw:** SR-Beh5/6/7 all have priority 1. tick is delivered every cycle, so if tick came before the timeout Turning would never be left.
- **fix_directive:** endTask is first (override); the autonomous turnDurationElapsed branch comes next; the tick self-loop is guarded by !turnDurationElapsed so the two are mutually exclusive in the model.
- **java_trace:**
  - `src/main/java/sranger/controller/SRangerController.java` lines 71-82, step: TURNING block, requirements SR-Beh5, SR-Beh6, SR-Beh7, SR-DC1

### 5. [design_choice] Initial Move(moveVel, 0) is issued in the constructor

- **raw:** SR-FR1 requires Move(moveVel, 0) on entering Moving, and SR-Beh1 says the controller starts in Moving; no transition into the initial mode exists in step().
- **fix_directive:** The constructor issues Move(moveVel, 0) once. Every transition into Moving inside step() issues it too. The extracted model may not include this power-up action, because the profile places entry actions only inside step() branches.
- **java_trace:**
  - `src/main/java/sranger/controller/SRangerController.java` lines 32-37, SRangerController constructor, requirements SR-FR1, SR-Beh1

### 6. [design_choice] Clock is a framework-advanced time source in seconds

- **raw:** SR-Var1 requires 'the current time' but does not name a time source.
- **fix_directive:** Clock.now() returns seconds since start-up; the system framework calls Clock.advance(dt). clockResetTime = timer.now() on Moving -> Turning; turnDurationElapsed is timer.now() - clockResetTime >= turnDuration (extracted as since(clockResetTime)).
- **java_trace:**
  - `src/main/java/sranger/sensor/Clock.java` lines 11-32, Clock, requirements SR-Var1, SR-GP2
  - `src/main/java/sranger/controller/SRangerController.java` lines 56-65, step: turnDurationElapsed, clockResetTime reset, requirements SR-Var1, SR-GP2

### 7. [scope_question] obstacleThreshold = 0.5 is rounded up to 1 in the model

- **raw:** The model's constants interface rounds a fractional constant up to an integer; obstacleThreshold is used in the obstacleDetected guard.
- **fix_directive:** The Java guard uses 0.5 m; the verified model checks distance() <= 1. Deadlock and divergence results do not depend on the value, but any property that depends on the exact threshold does not carry over. The other three constants are integral.
- **java_trace:**
  - `src/main/java/sranger/controller/SRangerConstants.java` lines 17-17, OBSTACLE_THRESHOLD, requirements SR-DM2, SR-GP1

### 8. [ambiguous_requirement] When the timeout fires, and what Turning/Final do with other events

- **raw:** SR-Beh5 says the transition fires when turnDurationElapsed 'becomes true'; nothing says what obstacle does in Turning or what any event does in Final.
- **fix_directive:** In Java the timeout is evaluated at each step() call, so it fires on the first cycle at or after turnDuration. obstacle in Turning, and every event in Final, is ignored. No transitions were invented for them.
- **java_trace:**
  - `src/main/java/sranger/controller/SRangerController.java` lines 71-85, step: TURNING and FINAL blocks, requirements SR-Beh5, SR-FR3

## next_step

Run the pipeline (compile, coverage, profile check, transformations, FDR4, Dafny, Isabelle, vacuity) and fix the Java from the failing phase reports.
