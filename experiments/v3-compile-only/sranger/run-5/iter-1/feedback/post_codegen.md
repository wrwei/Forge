# post_codegen

**status:** uncertain

**summary:** All 23 SRanger requirements are implemented in 9 Java files (one controller, three modes, six step() transitions plus the initial mode). No requirement is unimplemented. Three invented defaults, five design choices and one model-fidelity question are listed for review.

## Issues

### 1. [invented_default] Sensor no-reading value is 1000.0 m

- **raw:** SR-DM5 asks for 'a large default value' when no IR reading is available but gives no number.
- **fix_directive:** None needed unless the specification fixes a value; 1000.0 m is far above OBSTACLE_THRESHOLD, so missing data can never satisfy obstacleDetected.
- **java_trace:**
  - `java.generated.project/src/main/java/sranger/sensor/Sensor.java` lines 11-15, `NO_READING_DISTANCE`, requirements SR-DM5, SR-SF1

### 2. [invented_default] Negative or NaN readings are treated as 'no reading'

- **raw:** SR-DM5 / SR-SF1 say distance() is non-negative but do not say what to do with an invalid measurement.
- **fix_directive:** None needed; Sensor.update stores the no-reading default for any reading that is not >= 0.0, so distance() is always non-negative.
- **java_trace:**
  - `java.generated.project/src/main/java/sranger/sensor/Sensor.java` lines 17-27, `update`, requirements SR-DM5, SR-SF1

### 3. [invented_default] Initial values of clockResetTime and of the actuator's last command

- **raw:** SR-Var1 does not give clockResetTime a value before Turning is first entered; SR-DM6 does not say what the actuator holds before the first command.
- **fix_directive:** None needed; both start at 0.0. clockResetTime is only read in Turning, which always sets it first; the actuator's 0.0/0.0 is overwritten by the constructor's initial Move.
- **java_trace:**
  - `java.generated.project/src/main/java/sranger/controller/SRangerController.java` lines 18-19, `clockResetTime`, requirements SR-Var1
  - `java.generated.project/src/main/java/sranger/actuator/Actuator.java` lines 12-16, `Actuator`, requirements SR-DM6

### 4. [design_choice] Moving -> Turning needs both the obstacle event and obstacleDetected

- **raw:** SR-Beh2 triggers the transition on the obstacle event; SR-GP1 says obstacleDetected (distance <= obstacleThreshold) is the guard on the same transition.
- **fix_directive:** Implemented as trigger obstacle with condition obstacleDetected. On this branch clockResetTime is reset before Move(0, turnVel) is issued, the order SR-Beh2 states.
- **java_trace:**
  - `java.generated.project/src/main/java/sranger/controller/SRangerController.java` lines 44-44, `obstacleDetected`, requirements SR-GP1
  - `java.generated.project/src/main/java/sranger/controller/SRangerController.java` lines 52-55, `step`, requirements SR-Beh2, SR-FR2, SR-Var1

### 5. [design_choice] Priority in Turning: endTask, then the timed exit, then tick

- **raw:** The requirements give no priority between SR-Beh5 (autonomous, timed), SR-Beh6 (endTask) and SR-Beh7 (tick). tick arrives every cycle, so if tick came first the timed exit could never fire.
- **fix_directive:** endTask is first in both Moving and Turning (it is the shutdown). The timed exit comes before tick, and the tick self-loop conjoins !turnDurationElapsed so the two are exclusive in the model.
- **java_trace:**
  - `java.generated.project/src/main/java/sranger/controller/SRangerController.java` lines 49-51, `step`, requirements SR-Beh3
  - `java.generated.project/src/main/java/sranger/controller/SRangerController.java` lines 61-69, `step`, requirements SR-Beh5, SR-Beh6, SR-Beh7, SR-GP2, SR-DC1

### 6. [design_choice] Initial Move(moveVel, 0) is issued in the constructor

- **raw:** SR-FR1 and SR-Beh1: Moving is initial and entering Moving issues Move(moveVel, 0). There is no transition into the initial mode in step().
- **fix_directive:** The constructor issues the initial Move after wiring sensor, actuator and clock. If the extractor does not model constructor actions, Moving's entry action still comes from the Turning -> Moving branch.
- **java_trace:**
  - `java.generated.project/src/main/java/sranger/controller/SRangerController.java` lines 25-34, `SRangerController`, requirements SR-FR1, SR-Beh1

### 7. [design_choice] tick self-loops perform no action

- **raw:** SR-Beh4 and SR-Beh7 say the controller performs no action on the tick self-loop; the general rule is that a branch performs the target mode's entry actions.
- **fix_directive:** The requirement wins: the tick branches only re-assign the mode and do not re-issue Move or reset the clock.
- **java_trace:**
  - `java.generated.project/src/main/java/sranger/controller/SRangerController.java` lines 56-58, `step`, requirements SR-Beh4
  - `java.generated.project/src/main/java/sranger/controller/SRangerController.java` lines 67-69, `step`, requirements SR-Beh7

### 8. [design_choice] Clock is monotonic seconds since construction, held in a field named timer

- **raw:** SR-Var1 needs 'the current time' but no time source is specified; 'clock' is a RoboChart reserved word.
- **fix_directive:** None needed; Clock.now() returns System.nanoTime() seconds since the Clock was created, and the controller field holding it is named timer.
- **java_trace:**
  - `java.generated.project/src/main/java/sranger/controller/Clock.java` lines 8-22, `Clock`, requirements SR-Var1
  - `java.generated.project/src/main/java/sranger/controller/SRangerController.java` lines 45-45, `turnDurationElapsed`, requirements SR-GP2

### 9. [scope_question] OBSTACLE_THRESHOLD = 0.5 is rounded up to 1 in the model

- **raw:** The model's constants interface rounds a fractional constant up to the next integer, and obstacleDetected compares against OBSTACLE_THRESHOLD; the other three constants are whole numbers.
- **fix_directive:** The Java keeps 0.5 as SR-DM2 requires. In the model the guard reads distance <= 1, so verified properties that depend on the exact threshold describe a slightly larger detection zone.
- **java_trace:**
  - `java.generated.project/src/main/java/sranger/controller/SRangerConstants.java` lines 18-20, `OBSTACLE_THRESHOLD`, requirements SR-DM2, SR-GP1
  - `java.generated.project/src/main/java/sranger/controller/SRangerController.java` lines 44-44, `obstacleDetected`, requirements SR-GP1

## next_step

Run the pipeline (compile, coverage, profile check, transformations, FDR4, Dafny, Isabelle, vacuity) and fix the Java from the failing phases' reports.
