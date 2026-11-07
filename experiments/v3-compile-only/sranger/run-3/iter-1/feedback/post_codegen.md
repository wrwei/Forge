# post_codegen

**status:** uncertain

**summary:** All 23 SRanger requirements are implemented in 9 Java files (package sranger). One controller (SRangerController) with modes MOVING (initial), TURNING, FINAL; six transitions in step() plus the initial mode; Clock-based clockResetTime with a since-style guard. Seven points are flagged: three invented defaults, four design choices (one concerns the model's rounding of the 0.5 m threshold).

## Issues

### 1. invented_default — Sensor default distance when no reading is available is 1000.0 m
- **raw:** SR-DM5 requires a 'large default value' when no IR reading is available but does not give the value.
- **fix_directive:** None needed unless the requirements fix a value; any value above OBSTACLE_THRESHOLD (0.5 m) satisfies SR-DM5. Negative or NaN readings passed to update() are treated as 'no reading' and also yield the default.
- **java_trace:** `src/main/java/sranger/sensor/Sensor.java` lines 16–35, NO_READING_DISTANCE / distance / update — SR-DM5, SR-SF1

### 2. invented_default — Actuator reports 0.0 / 0.0 before any command
- **raw:** SR-DM6 says the Actuator stores the latest command but not what it reports before the first one.
- **fix_directive:** None needed; the controller issues Move(moveVel, 0) at power-up, so the default is only observable before the controller is constructed.
- **java_trace:** `src/main/java/sranger/actuator/Actuator.java` lines 12–13, lastLinearVelocity / lastAngularVelocity — SR-DM6

### 3. invented_default — clockResetTime starts at 0.0
- **raw:** SR-Var1 defines clockResetTime but not its initial value; it is only read in Turning, after Moving -> Turning has set it.
- **fix_directive:** None needed; the initial value never influences a transition.
- **java_trace:** `src/main/java/sranger/controller/SRangerController.java` line 20, clockResetTime — SR-Var1

### 4. design_choice — Moving -> Turning is triggered by obstacle AND guarded by obstacleDetected
- **raw:** SR-Beh2 says the transition fires when the obstacle event is received; SR-GP1 says obstacleDetected (distance <= obstacleThreshold) is the guard on the same transition. Both were combined: trigger obstacle, condition obstacleDetected.
- **fix_directive:** If the guard is meant to replace the event (an autonomous transition), drop the instanceof test and conjoin no negations (Moving has no other guard-only branch).
- **java_trace:** `src/main/java/sranger/controller/SRangerController.java` lines 41–53, step / obstacleDetected — SR-Beh2, SR-GP1

### 5. design_choice — Turning branch order: endTask, then autonomous turnDurationElapsed, then tick && !turnDurationElapsed
- **raw:** SR-Beh5/6/7 give no priority. endTask is placed first as the stop override. The autonomous Turning -> Moving branch is placed above tick: a tick arrives every cycle, so a tick branch above it would starve Turning -> Moving in Java. The tick branch conjoins !turnDurationElapsed as the profile requires, so the model's transitions are disjoint.
- **fix_directive:** None needed unless the requirements give a different priority.
- **java_trace:** `src/main/java/sranger/controller/SRangerController.java` lines 59–71, step — SR-Beh5, SR-Beh6, SR-Beh7, SR-GP2

### 6. design_choice — Power-up Move(moveVel, 0) is issued in the constructor
- **raw:** SR-FR1 requires Move(moveVel, 0) on entering Moving, and SR-Beh1 makes Moving the initial mode, so the command is issued once at construction. The extracted model may or may not carry this as an action of the initial transition.
- **fix_directive:** If the model must show the initial entry action and the extractor does not lift it from the constructor, report it; do not move the call into step(), which would add a transition no requirement names.
- **java_trace:** `src/main/java/sranger/controller/SRangerController.java` lines 26–31, SRangerController — SR-FR1, SR-Beh1

### 7. design_choice — Clock is advanced by the framework; obstacleThreshold 0.5 rounds up to 1 in the model
- **raw:** Time is a Clock (field 'timer', since 'clock' is reserved) advanced by the system framework via advance(seconds), which makes the controller deterministic and testable. Separately, OBSTACLE_THRESHOLD = 0.5 m is kept in metres as SR-DM2 states; the model's constants interface rounds it up to 1, so the model's obstacleDetected guard reads distance() <= 1. The exact value matters to the guard in Java; the model over-approximates it.
- **fix_directive:** None in the Java; units were kept as the requirements give them rather than rescaled to avoid rounding.
- **java_trace:**
  - `src/main/java/sranger/controller/Clock.java` lines 9–24, Clock — SR-Var1, SR-GP2
  - `src/main/java/sranger/controller/SRangerConstants.java` line 17, OBSTACLE_THRESHOLD — SR-DM2, SR-GP1

## next_step

Run the pipeline (compile, coverage, profile check, transformations, verifiers) and address any failures in the Java.
