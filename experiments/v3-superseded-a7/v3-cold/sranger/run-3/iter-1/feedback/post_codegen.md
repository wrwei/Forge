# post_codegen

**status:** uncertain

**summary:** All 23 requirements are implemented (9 Java files, single controller SRangerController with modes MOVING, TURNING, FINAL). No requirement is unimplemented. Two invented defaults, four design choices, one ambiguity and one model-fidelity note are listed below.

## Issues

### 1. [invented_default] Distance reported while no IR reading is available: 1000.0 m

**raw:** SR-DM5 asks for 'a large default value' when no reading is available but gives no number. Sensor.distance() returns NO_READING_DISTANCE = 1000.0 m before the first reading, after clearReading(), and after an invalid (negative or NaN) update. The only requirement on the value is that it exceeds obstacleThreshold (0.5 m), which it does.

**fix_directive:** If the system has a specified maximum IR range or sentinel, replace the constant in Sensor; otherwise keep it.

**java_trace:**

- `src/main/java/sranger/sensor/Sensor.java` lines 10-26, `NO_READING_DISTANCE`, requirements SR-DM5, SR-SF1, SR-GP1

### 2. [invented_default] Initial values: actuator reports Move(0, 0) before any command; clockResetTime starts at 0.0

**raw:** SR-DM6 does not say what the actuator holds before the first command; it starts at lv = 0.0, av = 0.0 (stopped), and the controller's constructor immediately overwrites it with Move(moveVel, 0). SR-Var1 gives no initial value for clockResetTime; it starts at 0.0, the clock's start time, and is only read in Turning, which always resets it on entry.

**fix_directive:** None needed unless the requirements fix other initial values.

**java_trace:**

- `src/main/java/sranger/actuator/Actuator.java` lines 13-14, `Actuator`, requirements SR-DM6
- `src/main/java/sranger/controller/SRangerController.java` lines 16-16, `clockResetTime`, requirements SR-Var1

### 3. [design_choice] Move is a record with two real components (lv, av)

**raw:** SR-DM4 and the system description give Move(lv : real, av : real): one event carrying two reals. The profile describes a valued event as a record with one component; it does not say how a two-valued event is written. OutputEvent.Move has two @RoboChartType("real") components, mirroring the requirement, rather than wrapping them in an invented datatype.

**fix_directive:** If the transformation phases reject a two-component event record, introduce a record type (e.g. a velocity pair with lv and av) and give Move one component of that type.

**java_trace:**

- `src/main/java/sranger/event/OutputEvent.java` lines 16-16, `Move`, requirements SR-DM4

### 4. [design_choice] Power-up entry action of Moving is issued in the constructor

**raw:** SR-FR1 says entering Moving issues Move(moveVel, 0), and SR-Beh1 makes Moving the initial mode, so the Java issues the command once, in the constructor. step() is the only place transitions are written; the initial transition of the extracted model may therefore not show this action, though the Java does perform it.

**fix_directive:** None in the Java; when reading the model, expect the forward command on the Turning -> Moving transition but possibly not on the initial transition.

**java_trace:**

- `src/main/java/sranger/controller/SRangerController.java` lines 21-27, `SRangerController`, requirements SR-FR1, SR-Beh1

### 5. [design_choice] In Turning, the timed transition ranks above tick; the tick self-loop is guarded by !turnDurationElapsed

**raw:** Tick arrives every control cycle (SR-DM3). If the tick self-loop (SR-Beh7) ranked above the autonomous Turning -> Moving transition (SR-Beh5), a tick cycle would always keep the robot in Turning. So Turning lists endTask, then turnDurationElapsed, then tick; following the profile, the tick branch conjoins !turnDurationElapsed. SR-Beh7 states no guard; the added guard only removes the overlap with SR-Beh5.

**fix_directive:** None.

**java_trace:**

- `src/main/java/sranger/controller/SRangerController.java` lines 53-64, `step`, requirements SR-Beh5, SR-Beh6, SR-Beh7, SR-GP2, SR-DC1

### 6. [design_choice] Tick self-loops perform no action

**raw:** The profile says every branch performs the target mode's entry actions. SR-Beh4 and SR-Beh7 say explicitly that the tick self-loops perform no action, so those two branches only assign the (unchanged) mode and do not re-issue Move.

**fix_directive:** None.

**java_trace:**

- `src/main/java/sranger/controller/SRangerController.java` lines 49-50, `step`, requirements SR-Beh4
- `src/main/java/sranger/controller/SRangerController.java` lines 62-63, `step`, requirements SR-Beh7

### 7. [ambiguous_requirement] Moving -> Turning needs both the obstacle event and obstacleDetected; 'below' vs 'at or below'

**raw:** SR-Beh2 triggers the transition on the obstacle event; SR-GP1 says obstacleDetected (distance <= obstacleThreshold) is its guard. Both are used: trigger obstacle, guard obstacleDetected. An obstacle event while the reading is above the threshold is ignored. SR-DM3 says the event is emitted when the reading crosses strictly below the threshold, SR-GP1 says at or below; the guard follows SR-GP1 (<=).

**fix_directive:** If the event alone should suffice, drop obstacleDetected from the Moving -> Turning branch.

**java_trace:**

- `src/main/java/sranger/controller/SRangerController.java` lines 37-48, `obstacleDetected`, requirements SR-Beh2, SR-GP1, SR-DM3

### 8. [scope_question] obstacleThreshold = 0.5 is fractional and is rounded up to 1 in the model's constants

**raw:** The constants interface of the model rounds a fractional constant up to an integer, so the guard obstacleDetected is checked in the model as distance() <= 1 rather than <= 0.5. The other three constants (1.0, 2.0, 2.0) are integral and unaffected. The Java keeps 0.5.

**fix_directive:** None in the Java; read verifier results about obstacleDetected with this rounding in mind.

**java_trace:**

- `src/main/java/sranger/controller/SRangerConstants.java` lines 17-17, `OBSTACLE_THRESHOLD`, requirements SR-DM2, SR-GP1
- `src/main/java/sranger/controller/SRangerController.java` lines 37-37, `obstacleDetected`, requirements SR-GP1

## Next step

Run the pipeline (compile, coverage, profile check, transformations, verifiers).
