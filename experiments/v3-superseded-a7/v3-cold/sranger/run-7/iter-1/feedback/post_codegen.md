# post_codegen

**status:** uncertain

**summary:** All 23 requirements implemented in 9 Java files (package sranger); compiles with javac --release 17 without warnings. 10 items flagged: 2 invented defaults, 6 design choices, 1 ambiguous requirement, 1 scope question. None is unimplemented.

## Issues

### 1. [invented_default] IR distance when no reading is available: 100.0 m

**raw:** SR-DM5 requires distance() to return 'a large default value' when no reading is available but gives no number. Chosen: 100.0 m (200x obstacleThreshold), held in Sensor.NO_READING_DISTANCE and used as the initial reading.

**fix_directive:** If the system specifies a sensor range or a sentinel, replace NO_READING_DISTANCE with it; it must stay above OBSTACLE_THRESHOLD.

**java_trace:**

- `src/main/java/sranger/sensor/Sensor.java` lines 12-14, `NO_READING_DISTANCE / latestDistance`: SR-DM5, SR-SF1

### 2. [invented_default] Initial values of clockResetTime, the clock and the actuator

**raw:** Not given by the requirements. clockResetTime starts at 0.0 (it is only read in Turning, after being set on entry). Clock starts at 0.0 and is advanced monotonically by the framework (advanceTo ignores earlier times). Actuator stores Move(0, 0) until the first command; the controller constructor issues Move(moveVel, 0) immediately.

**fix_directive:** None needed unless the platform defines other start-up values.

**java_trace:**

- `src/main/java/sranger/controller/SRangerController.java` lines 18-18, `clockResetTime`: SR-Var1
- `src/main/java/sranger/controller/Clock.java` lines 10-17, `Clock`: SR-Var1, SR-GP2
- `src/main/java/sranger/actuator/Actuator.java` lines 11-12, `Actuator`: SR-DM6

### 3. [design_choice] Move is one output event with two real components

**raw:** SR-DM4 calls for a single output event Move carrying a linear and an angular velocity. Implemented as record OutputEvent.Move(double lv, double av), emitted by actuator.apply(new OutputEvent.Move(lv, av)). The alternatives (two events, or a one-component event typed by a velocity record) contradict SR-DM4's 'single event' or its Move(lv, av) signature.

**fix_directive:** If the toolchain cannot type an event by two components, wrap lv and av in a record datatype and give Move a single component of that type.

**java_trace:**

- `src/main/java/sranger/event/OutputEvent.java` lines 16-16, `Move`: SR-DM4, SR-DM6
- `src/main/java/sranger/actuator/Actuator.java` lines 15-21, `apply`: SR-DM6

### 4. [ambiguous_requirement] Moving -> Turning: obstacle event and obstacleDetected guard

**raw:** SR-Beh2 says the transition fires 'when the obstacle event is received'; SR-GP1 says obstacleDetected (distance <= obstacleThreshold) 'is used as the guard on the Moving -> Turning transition'. Implemented as both: trigger Obstacle, condition obstacleDetected. An obstacle event while the sensor reads above the threshold is ignored.

**fix_directive:** If the event alone should suffice, remove '&& obstacleDetected' from the Moving block's Obstacle branch (SR-GP1 would then be unused).

**java_trace:**

- `src/main/java/sranger/controller/SRangerController.java` lines 44-44, `obstacleDetected`: SR-GP1
- `src/main/java/sranger/controller/SRangerController.java` lines 51-54, `step`: SR-Beh2, SR-GP1, SR-FR2, SR-Var1

### 5. [design_choice] Priority in Turning: endTask, then timeout, then tick

**raw:** No priority is given among SR-Beh5, SR-Beh6 and SR-Beh7. endTask (stop) is first. The autonomous timeout is placed above the tick self-loop, and the tick branch conjoins !turnDurationElapsed: since tick arrives every control cycle, the opposite order would keep the Java controller in Turning forever.

**fix_directive:** None, unless the requirements state another priority.

**java_trace:**

- `src/main/java/sranger/controller/SRangerController.java` lines 60-70, `step`: SR-Beh5, SR-Beh6, SR-Beh7, SR-GP2

### 6. [design_choice] Moving's entry command on power-up is issued by the constructor

**raw:** SR-FR1 says entering Moving issues Move(moveVel, 0) and SR-Beh1 says the controller starts in Moving. The power-up entry is not a branch of step(), so the constructor issues the command. If the extractor ignores constructor statements, the model's initial transition carries no action; the Turning -> Moving branch still issues it.

**fix_directive:** If the model must show the initial action, report it as a pipeline limitation rather than moving the call into step().

**java_trace:**

- `src/main/java/sranger/controller/SRangerController.java` lines 27-32, `SRangerController`: SR-FR1, SR-Beh1

### 7. [scope_question] obstacleThreshold = 0.5 is rounded up to 1 in the model

**raw:** The model's constants interface rounds fractional constants up, so the SR-GP1 guard becomes distance() <= 1 in the model while the Java keeps 0.5. moveVel (1.0), turnVel (2.0) and turnDuration (2.0) are integral and unaffected; the timed guard since(clockResetTime) >= turnDuration is exact.

**fix_directive:** Treat verification results about the obstacle guard as holding for the rounded threshold; the Java is not changed.

**java_trace:**

- `src/main/java/sranger/controller/SRangerConstants.java` lines 17-17, `OBSTACLE_THRESHOLD`: SR-DM2, SR-GP1

### 8. [design_choice] Negative or non-numeric IR readings are treated as no reading

**raw:** SR-DM5 requires distance() to be non-negative but says nothing about invalid measurements. Sensor.update stores a reading only if it is >= 0 (which also rejects NaN); otherwise it falls back to the no-reading default, so invalid data never triggers obstacle detection.

**fix_directive:** None.

**java_trace:**

- `src/main/java/sranger/sensor/Sensor.java` lines 20-26, `update`: SR-DM5

### 9. [design_choice] Final is terminal; tick self-loops perform no action

**raw:** SR-FR3 gives Final no outgoing transition, so every event is ignored there (an empty FINAL block). Per SR-Beh4 and SR-Beh7 the tick self-loops only re-assign the mode: they do not re-issue Move or reset clockResetTime. SR-DC1 holds: each (mode, trigger) pair has exactly one branch.

**fix_directive:** None.

**java_trace:**

- `src/main/java/sranger/controller/SRangerController.java` lines 47-74, `step`: SR-FR3, SR-Beh4, SR-Beh7, SR-DC1

### 10. [design_choice] Clock service is a class named Clock held in field 'timer'

**raw:** 'clock' is a reserved word, so the controller's Clock field is named timer. clockResetTime = timer.now() on the Moving -> Turning branch resets the clock, and turnDurationElapsed is timer.now() - clockResetTime >= TURN_DURATION, the since(clockResetTime) shape.

**fix_directive:** None.

**java_trace:**

- `src/main/java/sranger/controller/SRangerController.java` lines 45-45, `turnDurationElapsed`: SR-GP2, SR-Var1
- `src/main/java/sranger/controller/SRangerController.java` lines 53-53, `clockResetTime`: SR-Var1

## next_step

Run the pipeline (compile, coverage, profile check, transformations, FDR4, Dafny, Isabelle, vacuity) and act on its reports.
