# post_codegen

**status:** uncertain

**summary:** All 23 SRanger requirements are implemented (9 Java files under sranger/; the trace maps every requirement and file). The code compiles with javac --release 17 -Xlint:all and no warnings. Nothing is unimplemented. Two defaults were invented, six design choices were made, and one value changes when it reaches the model.

## Issues

### 1. [invented_default] Sensor distance when no reading is available

- **raw:** SR-DM5 asks for 'a large default value' when no reading is available but gives no number.
- **fix_directive:** distance() returns 1000.0 m (NO_READING_DISTANCE) until the first valid reading. update() also falls back to it for a negative or NaN value. 1000.0 is far above OBSTACLE_THRESHOLD, so missing data never looks like an obstacle. Replace it if the platform specifies a sentinel.
- **java_trace:**
  - `src/main/java/sranger/sensor/Sensor.java` lines 12-27, `Sensor.NO_READING_DISTANCE / update`, requirements SR-DM5, SR-SF1

### 2. [invented_default] Actuator's stored command before any command is issued

- **raw:** SR-DM6 says the Actuator stores the latest command, but not what it holds before the first one.
- **fix_directive:** The stored linear and angular velocities start at 0.0 (a stop). The controller's constructor overwrites them straight away with Move(moveVel, 0).
- **java_trace:**
  - `src/main/java/sranger/actuator/Actuator.java` lines 12-13, `Actuator.lastLinear / lastAngular`, requirements SR-DM6

### 3. [design_choice] Moving -> Turning needs both the obstacle event and the obstacleDetected guard

- **raw:** SR-Beh2 triggers the transition on the obstacle event; SR-GP1 says obstacleDetected (distance <= obstacleThreshold) is its guard.
- **fix_directive:** The branch is 'event instanceof Obstacle && obstacleDetected'. An obstacle event that arrives while the distance is above the threshold is ignored and the controller stays in Moving.
- **java_trace:**
  - `src/main/java/sranger/controller/SRangerController.java` lines 51-61, `step / obstacleDetected`, requirements SR-Beh2, SR-GP1

### 4. [design_choice] Actions go on transitions, not state entry actions; the tick self-loops do nothing

- **raw:** SR-FR1 and SR-FR2 describe entry actions, while SR-Beh4 and SR-Beh7 say the tick self-loops perform no action. In RoboChart a self-transition re-enters its state, so entry actions would re-issue Move and reset clockResetTime on every tick, and Turning would never time out.
- **fix_directive:** Each mode-changing branch issues its target's Move (and, into Turning, resets clockResetTime) as the transition's action. The tick branches only reassign the mode.
- **java_trace:**
  - `src/main/java/sranger/controller/SRangerController.java` lines 54-76, `step`, requirements SR-FR1, SR-FR2, SR-FR3, SR-Beh4, SR-Beh7

### 5. [design_choice] Transition priority within each mode

- **raw:** The requirements give no priority between transitions that leave the same mode.
- **fix_directive:** endTask comes first in Moving and Turning (it is the operator's shutdown). In Turning the timed transition comes before tick, and the tick branch conjoins !turnDurationElapsed. Otherwise a tick delivered every cycle would block the return to Moving in Java. An obstacle event in Turning has no transition (none is required). Final is an empty, terminal block. There is one transition per (mode, trigger), as SR-DC1 requires.
- **java_trace:**
  - `src/main/java/sranger/controller/SRangerController.java` lines 54-79, `step`, requirements SR-Beh3, SR-Beh5, SR-Beh6, SR-Beh7, SR-DC1

### 6. [design_choice] The initial Move(moveVel, 0) is issued in the constructor

- **raw:** SR-FR1 and SR-Beh1: Moving is the initial mode, and entering Moving issues Move(moveVel, 0). step() has no initial-transition branch.
- **fix_directive:** The constructor issues the command once, at power-up. The extracted model may not show it as the action of the initial transition.
- **java_trace:**
  - `src/main/java/sranger/controller/SRangerController.java` lines 31-36, `SRangerController(Sensor, Actuator, Clock)`, requirements SR-FR1, SR-Beh1

### 7. [design_choice] Move has two real components

- **raw:** SR-DM4: Move carries two real values. The codegen guide assumes an event has one component.
- **fix_directive:** Move is record Move(double lv, double av), as written in the system description, rather than a single component of a separate record type.
- **java_trace:**
  - `src/main/java/sranger/event/OutputEvent.java` lines 16-16, `OutputEvent.Move`, requirements SR-DM4

### 8. [design_choice] Clock source and the name of the clock field

- **raw:** SR-Var1 does not say where 'current time' comes from. 'clock' is a reserved word in RoboChart.
- **fix_directive:** Clock.now() returns monotonic seconds (System.nanoTime) since the clock was created. The controller field is named 'timer'. clockResetTime = timer.now() on Moving -> Turning, and turnDurationElapsed = timer.now() - clockResetTime >= TURN_DURATION.
- **java_trace:**
  - `src/main/java/sranger/sensor/Clock.java` lines 13-21, `Clock.now`, requirements SR-Var1, SR-GP2
  - `src/main/java/sranger/controller/SRangerController.java` lines 52-60, `turnDurationElapsed / clockResetTime`, requirements SR-Var1, SR-GP2

### 9. [ambiguous_requirement] OBSTACLE_THRESHOLD = 0.5 becomes 1 in the model

- **raw:** SR-DM2 fixes obstacleThreshold at 0.5 m. The model's constants interface rounds a fractional constant up to an integer.
- **fix_directive:** The Java uses 0.5 exactly. In the model, the obstacleDetected guard reads distance <= 1, so results that depend on the exact threshold do not carry over. The other three constants are whole numbers and are unaffected.
- **java_trace:**
  - `src/main/java/sranger/controller/SRangerConstants.java` lines 17-17, `OBSTACLE_THRESHOLD`, requirements SR-DM2, SR-GP1

## next_step

Run the pipeline (compile, coverage, profile check, transformations, verifiers).
