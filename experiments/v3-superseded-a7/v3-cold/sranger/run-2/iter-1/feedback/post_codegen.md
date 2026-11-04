# post_codegen

- **status**: uncertain
- **summary**: SRanger controller written in package sranger (9 Java files): one controller with modes MOVING (initial), TURNING, FINAL (terminal); six transitions plus the initial mode, one per SR-Beh requirement; guards obstacleDetected and turnDurationElapsed; clockResetTime as a clock variable. All 23 requirements are traced in result_codegen.json. Ten points are flagged: two invented defaults, one ambiguity (trigger plus guard on Moving to Turning), one model-rounding note, and six design choices.
- **next_step**: Run the pipeline (compile, coverage, profile check, transformations, verifiers). No change to the Java is planned before that run.

## Issues

### 1. [invented_default] Sensor distance when no reading is available: 100.0 m

- **raw**: SR-DM5: 'When no reading is available ... distance() returns a large default value so that the obstacle-detection condition is not falsely triggered by missing data.' No number is given.
- **fix_directive**: If a specific default is wanted, change NO_READING_DISTANCE in Sensor.java; any value above OBSTACLE_THRESHOLD (and above 1, its rounded model value) keeps the intent.
- **java_trace**:
  - `java.generated.project/src/main/java/sranger/sensor/Sensor.java` lines 12-14, element `NO_READING_DISTANCE`, requirements SR-DM5, SR-SF1

### 2. [invented_default] Initial values before the first assignment: clockResetTime = 0.0, actuator last command (0, 0)

- **raw**: SR-Var1 says clockResetTime is set on entry to Turning but gives no initial value; SR-DM6 does not say what the actuator reports before any command. The controller constructor issues Move(moveVel, 0) at once, so the actuator's (0, 0) is never observable after construction.
- **fix_directive**: Change the field initialisers if other initial values are required.
- **java_trace**:
  - `java.generated.project/src/main/java/sranger/controller/SRangerController.java` lines 18-18, element `clockResetTime`, requirements SR-Var1
  - `java.generated.project/src/main/java/sranger/actuator/Actuator.java` lines 12-13, element `lastLinearVelocity`, requirements SR-DM6

### 3. [design_choice] Move carries two real components (lv, av) in one event record

- **raw**: SR-DM4: 'a single output event, Move, carrying two real values'. The profile describes an event with one record component; the requirement fixes two values and forbids other output events, so Move is one record with two @RoboChartType("real") components rather than a wrapper record or two events.
- **fix_directive**: If the pipeline needs a single component, introduce a value record (e.g. Velocity(lv, av)) and make Move carry it; keep a single output event.
- **java_trace**:
  - `java.generated.project/src/main/java/sranger/event/OutputEvent.java` lines 16-17, element `Move`, requirements SR-DM4

### 4. [ambiguous_requirement] Moving to Turning uses both the obstacle event and the obstacleDetected guard

- **raw**: SR-Beh2 names only the obstacle event as the trigger; SR-GP1 states that obstacleDetected (distance <= obstacleThreshold) is the guard on the same transition. Both are honoured: trigger obstacle, condition obstacleDetected. An obstacle event while the reading is above the threshold is ignored.
- **fix_directive**: If the event alone should suffice, drop '&& obstacleDetected' from the Moving block (SR-GP1 would then be unused).
- **java_trace**:
  - `java.generated.project/src/main/java/sranger/controller/SRangerController.java` lines 43-43, element `obstacleDetected`, requirements SR-GP1
  - `java.generated.project/src/main/java/sranger/controller/SRangerController.java` lines 52-56, element `step`, requirements SR-Beh2, SR-GP1

### 5. [design_choice] Turning: autonomous return placed above the tick self-loop; tick guarded by !turnDurationElapsed

- **raw**: SR-Beh5 (autonomous, turnDurationElapsed) and SR-Beh7 (tick self-loop, no condition) have equal priority. A tick arrives every control cycle, so a tick branch above the timed branch would keep the robot turning forever. Order in Turning: endTask, then turnDurationElapsed, then tick && !turnDurationElapsed (the negation is required below a predicate-only branch, and makes the two transitions mutually exclusive in the model). endTask is first in both Moving and Turning as the override.
- **fix_directive**: None expected; revisit only if the timed transition should yield to tick.
- **java_trace**:
  - `java.generated.project/src/main/java/sranger/controller/SRangerController.java` lines 62-74, element `step`, requirements SR-Beh5, SR-Beh6, SR-Beh7, SR-GP2, SR-DC1

### 6. [design_choice] Tick self-loops perform no action; entry actions are inlined only on transitions that change mode

- **raw**: SR-Beh4 and SR-Beh7: 'The controller performs no action on this transition.' The Turning entry actions (clockResetTime reset, Move(0, turnVel)) are therefore not repeated on the Turning tick self-loop; repeating the clock reset would prevent the timed return.
- **fix_directive**: None expected.
- **java_trace**:
  - `java.generated.project/src/main/java/sranger/controller/SRangerController.java` lines 57-59, element `step`, requirements SR-Beh4
  - `java.generated.project/src/main/java/sranger/controller/SRangerController.java` lines 71-73, element `step`, requirements SR-Beh7

### 7. [design_choice] Power-up entry command Move(moveVel, 0) issued in the controller constructor

- **raw**: SR-Beh1 (start in Moving) with SR-FR1 (on entering Moving issue Move(moveVel, 0)). The initial mode is the field initialiser; the entry command of the initial entry is issued once in the constructor, since step() holds only transitions out of a mode.
- **fix_directive**: If the extracted model must carry this as Moving's initial entry action and does not, report it as a pipeline limitation rather than adding a transition (SR-DC1).
- **java_trace**:
  - `java.generated.project/src/main/java/sranger/controller/SRangerController.java` lines 28-33, element `SRangerController`, requirements SR-Beh1, SR-FR1

### 8. [scope_question] OBSTACLE_THRESHOLD = 0.5 is rounded up to 1 in the model's constants interface

- **raw**: SR-DM2 fixes obstacleThreshold = 0.5, and the guard obstacleDetected (SR-GP1) compares against it. Per the profile, a fractional constant becomes the next integer in the model, so the model's guard reads distance() <= 1. The Java keeps the exact 0.5. moveVel, turnVel and turnDuration are integral and unaffected.
- **fix_directive**: None in the Java (the value is fixed by the requirement); interpret model-level results on obstacleDetected with this rounding in mind.
- **java_trace**:
  - `java.generated.project/src/main/java/sranger/controller/SRangerConstants.java` lines 17-17, element `OBSTACLE_THRESHOLD`, requirements SR-DM2, SR-GP1
  - `java.generated.project/src/main/java/sranger/controller/SRangerController.java` lines 43-43, element `obstacleDetected`, requirements SR-GP1

### 9. [design_choice] Sensor ignores negative or NaN readings

- **raw**: SR-DM5/SR-SF1: distance() is non-negative. update() keeps the previous value when given a reading that is not >= 0.0, so distance() never returns a negative or NaN value.
- **fix_directive**: None expected.
- **java_trace**:
  - `java.generated.project/src/main/java/sranger/sensor/Sensor.java` lines 20-24, element `update`, requirements SR-DM5, SR-SF1

### 10. [design_choice] Clock is monotonic time in seconds since the clock was created

- **raw**: SR-Var1 records 'the current time' without naming a time source. Clock.now() returns seconds from System.nanoTime() relative to construction; the controller field is named timer because 'clock' is a reserved word.
- **fix_directive**: None expected.
- **java_trace**:
  - `java.generated.project/src/main/java/sranger/sensor/Clock.java` lines 11-17, element `now`, requirements SR-Var1, SR-GP2
