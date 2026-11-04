# post_codegen

- **phase**: codegen
- **status**: uncertain
- **summary**: All 23 SRanger requirements are implemented in 9 Java files (base package `sranger`). The project compiles under Java 17 and a behavioural smoke test of every transition passes. Eight points the requirements do not settle are recorded below: three invented defaults, four design choices or ambiguities, and one model-abstraction note (obstacleThreshold 0.5 is rounded up to 1 in the model's constants interface).
- **next_step**: Run the pipeline (compile, coverage, profile check, transformations, FDR4, Dafny, Isabelle, vacuity) and fix the Java from any failing phase's report.

## Issues

### 1. invented_default: Sensor: value of the 'large default' distance when no reading is available
- **raw**: SR-DM5: "When no reading is available ... distance() returns a large default value so that the obstacle-detection condition is not falsely triggered." No number is given.
- **fix_directive**: Chose 1000.0 m (2000 times obstacleThreshold), held in a private constant of Sensor and returned until the first valid reading. Change `NO_READING_DISTANCE` if the platform defines another sentinel.
- **java_trace**: `src/main/java/sranger/sensor/Sensor.java` lines 10–12, `NO_READING_DISTANCE`; requirements SR-DM5, SR-SF1

### 2. design_choice: Sensor: a negative or NaN raw reading is treated as "no reading available"
- **raw**: SR-DM5 / SR-SF1: distance() is non-negative. The requirements do not say how an invalid raw reading is reported.
- **fix_directive**: `Sensor.update(metres)` stores the value only when `metres >= 0.0`; otherwise it stores the no-reading default, so `distance()` is never negative and an invalid reading never triggers obstacleDetected.
- **java_trace**: `src/main/java/sranger/sensor/Sensor.java` lines 18–24, `update`; requirements SR-DM5, SR-SF1

### 3. invented_default: Actuator: last command before the controller issues any
- **raw**: SR-DM6: the Actuator stores the latest issued command. Its value before the first command is not specified.
- **fix_directive**: Initialised to Move(0.0, 0.0) (stationary). The controller's constructor overwrites it immediately with Moving's entry command.
- **java_trace**: `src/main/java/sranger/actuator/Actuator.java` line 10, `lastMove`; requirement SR-DM6

### 4. invented_default: clockResetTime before the first entry into Turning; clock source
- **raw**: SR-Var1: clockResetTime records when the controller most recently entered Turning. It has no meaningful value before the first entry, and the time source is not specified.
- **fix_directive**: `clockResetTime` starts at 0.0 and is read only in Turning, after Moving → Turning has set it. `Clock.now()` returns monotonic seconds (System.nanoTime) since the clock was created.
- **java_trace**: `src/main/java/sranger/controller/SRangerController.java` line 16, `clockResetTime` (SR-Var1); `src/main/java/sranger/controller/Clock.java` lines 8–14, `now` (SR-Var1, SR-GP2)

### 5. ambiguous_requirement: Moving → Turning: event trigger, guard, or both
- **raw**: SR-Beh2: transition "when the obstacle event is received". SR-GP1: obstacleDetected (distance <= obstacleThreshold) "is used as the guard on the Moving → Turning transition".
- **fix_directive**: Implemented as trigger `Obstacle` AND guard `obstacleDetected`, which satisfies both requirements and gives SR-DM5's "not falsely triggered by missing data" its effect. An Obstacle event while distance > obstacleThreshold is ignored (the controller stays in Moving).
- **java_trace**: `src/main/java/sranger/controller/SRangerController.java` line 40 (SR-GP1) and lines 47–50 (SR-Beh2, SR-GP1, SR-FR2), `step`

### 6. design_choice: Turning: priority of endTask, the timed exit and the tick self-loop
- **raw**: SR-Beh5 (autonomous, time since clockResetTime >= turnDuration), SR-Beh6 (endTask) and SR-Beh7 (tick, no action) all leave Turning. The requirements give no priority.
- **fix_directive**: Order: endTask first (a stop override, also first in Moving), then the autonomous timed exit, then tick guarded by `!turnDurationElapsed`. If tick came before the timed exit, the timed exit could never fire on a tick cycle. The negation keeps the model's branches disjoint.
- **java_trace**: `src/main/java/sranger/controller/SRangerController.java` lines 55–64, `step`; requirements SR-Beh5, SR-Beh6, SR-Beh7, SR-GP2

### 7. design_choice: Initial entry action of Moving issued by the constructor
- **raw**: SR-FR1: on entering Moving the controller issues Move(moveVel, 0). SR-Beh1: the controller begins in Moving at power-up.
- **fix_directive**: The constructor issues Move(MOVE_VEL, 0.0) once, so the robot moves forward from power-up. `step()` has no initialisation branch. The extracted model's transitions come from `step()`, so this initial command may not appear as an action of the model's initial transition.
- **java_trace**: `src/main/java/sranger/controller/SRangerController.java` lines 25–30, `SRangerController`; requirements SR-FR1, SR-Beh1

### 8. scope_question: obstacleThreshold = 0.5 is rounded up to 1 in the model's constants interface
- **raw**: The model's constants interface rounds a fractional constant up to the next integer. OBSTACLE_THRESHOLD (0.5) is the only fractional constant, and the obstacleDetected guard uses it.
- **fix_directive**: In the extracted model the guard reads `distance() <= 1` rather than `<= 0.5`, so verification of that guard is over the rounded value. The Java keeps the exact 0.5. MOVE_VEL, TURN_VEL and TURN_DURATION are integral and unaffected.
- **java_trace**: `src/main/java/sranger/controller/SRangerConstants.java` line 15, `OBSTACLE_THRESHOLD`; requirements SR-DM2, SR-GP1
