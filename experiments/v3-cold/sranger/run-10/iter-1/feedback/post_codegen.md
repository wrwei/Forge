# post_codegen

**status:** uncertain

**summary:** SRanger controller implemented in package `sranger` (9 Java files): one controller with modes MOVING/TURNING/FINAL, six `step()` transitions plus the initial mode, a timed Turning → Moving transition through a `Clock`-based `clockResetTime`, an IR `Sensor`, a recording `Actuator` and a single two-component `Move` output event. All 23 requirements are traced. Nine points the requirements did not settle are listed below; none blocks the pipeline.

## Issues

### 1. invented_default: Sensor.distance() default when no reading is available: 1000.0 m
- **raw:** SR-DM5: "When no reading is available ... distance() returns a large default value so that the obstacle-detection condition is not falsely triggered." No value is given.
- **fix_directive:** Chose `NO_READING_DISTANCE = 1000.0` m (far above obstacleThreshold 0.5 m). `update()` also treats a negative, NaN or > 1000 m measurement as "no reading", which keeps `distance()` non-negative (SR-SF1). If a different sentinel is wanted, change `NO_READING_DISTANCE` in Sensor.java; nothing else depends on its value.
- **java_trace:** `src/main/java/sranger/sensor/Sensor.java` 10–33, `Sensor.NO_READING_DISTANCE, Sensor.update, Sensor.clearReading`: SR-DM5, SR-SF1

### 2. invented_default: Actuator's recorded command before any command is (0.0, 0.0)
- **raw:** SR-DM6: "The Actuator ... stores the latest issued command." The value before the first command is not given.
- **fix_directive:** Initialised `lastLinearVelocity` and `lastAngularVelocity` to 0.0 (stationary). In practice the controller constructor issues Move(moveVel, 0) immediately, so the default is only observable before a controller is attached.
- **java_trace:** `src/main/java/sranger/actuator/Actuator.java` 12–17, `Actuator.lastLinearVelocity, Actuator.lastAngularVelocity`: SR-DM6

### 3. design_choice: Move is one event record with two real components, sent via actuator.apply(...)
- **raw:** SR-DM4: "a single output event, Move, carrying two real values — a linear velocity and an angular velocity." The profile's event guidance describes events with one component.
- **fix_directive:** Implemented `OutputEvent.Move(double lv, double av)` and emit it as `actuator.apply(new OutputEvent.Move(lv, av))`. The alternative (one component of a Velocity datatype) was rejected because the requirement names two real values, not a type. If the pipeline cannot type a two-component event, switch to a Velocity record payload.
- **java_trace:** `src/main/java/sranger/event/OutputEvent.java` 14, `OutputEvent.Move`: SR-DM4; `src/main/java/sranger/actuator/Actuator.java` 20–26, `Actuator.apply`: SR-DM6

### 4. design_choice: Moving's entry command at power-up is issued in the controller constructor
- **raw:** SR-FR1: "Moving is the initial mode on power-up ... on entering Moving, the controller issues Move(moveVel, 0)." SR-Beh1: "On power-up, the SRanger controller begins in Moving." `step()` carries transitions only, so it cannot express the initial entry.
- **fix_directive:** The constructor issues `Move(MOVE_VEL, 0.0)` after wiring its collaborators; without it the robot would sit still in Moving. The extracted model's initial transition may not carry this action; that affects only the first output, not deadlock or divergence.
- **java_trace:** `src/main/java/sranger/controller/SRangerController.java` 15–34, `SRangerController.currentMode, SRangerController(Sensor, Actuator, Clock)`: SR-FR1, SR-Beh1

### 5. ambiguous_requirement: Moving → Turning uses both the obstacle event and the obstacleDetected guard
- **raw:** SR-Beh2: "transitions from Moving to Turning when the obstacle event is received." SR-GP1: "obstacleDetected (distance <= obstacleThreshold) ... is used as the guard on the Moving → Turning transition."
- **fix_directive:** Implemented as trigger `Obstacle` with guard `obstacleDetected` (both requirements honoured). Consequence: an obstacle event that arrives while the latest reading is above the threshold (or absent) is ignored and the robot keeps Moving.
- **java_trace:** `src/main/java/sranger/controller/SRangerController.java` 46–56, `obstacleDetected, step (Moving block)`: SR-Beh2, SR-GP1, SR-DM3

### 6. design_choice: Turning priority: endTask, then the timed return, then tick
- **raw:** SR-Beh5 (autonomous Turning → Moving), SR-Beh6 (endTask), SR-Beh7 (tick self-loop) give no relative priority.
- **fix_directive:** `endTask` is first, as the stop that overrides everything. The autonomous `turnDurationElapsed` transition comes before the tick self-loop; otherwise a tick every cycle would keep the robot turning forever. The tick branch conjoins `!turnDurationElapsed` as the profile requires. The tick self-loops (SR-Beh4, SR-Beh7) perform no action, and in particular do not reset `clockResetTime`. In Java the return to Moving happens at the first `step()` at or after turnDuration, so it is quantised to the control cycle.
- **java_trace:** `src/main/java/sranger/controller/SRangerController.java` 47–70, `turnDurationElapsed, step (Turning block)`: SR-Beh5, SR-Beh6, SR-Beh7, SR-GP2, SR-DC1

### 7. design_choice: OBSTACLE_THRESHOLD = 0.5 is fractional and is rounded up to 1 in the model
- **raw:** SR-DM2: obstacleThreshold default 0.5 m. The model's constants interface rounds a fractional constant up to the next integer, and this constant appears in the `obstacleDetected` guard.
- **fix_directive:** Kept the requirement's unit (metres) and value 0.5 as a plain decimal literal. Rescaling everything to centimetres would change SR-DM5/SR-SF1's unit. In the extracted model the guard therefore reads `distance() <= 1`. Deadlock and divergence results are unaffected, but any model-level property about the exact detection distance is coarser than the Java. The other three constants (1.0, 2.0, 2.0) are integral and exact.
- **java_trace:** `src/main/java/sranger/controller/SRangerConstants.java` 19–20, `SRangerConstants.OBSTACLE_THRESHOLD`: SR-DM2, SR-GP1

### 8. design_choice: Time base is a framework-advanced Clock; the controller field is named timer
- **raw:** SR-Var1: clockResetTime records "the current time". The requirements do not say where time comes from.
- **fix_directive:** `Clock.now()` returns seconds and `advance(seconds)` moves it forward monotonically; the framework that delivers tick advances it each cycle. This is deterministic and testable; a wall-clock implementation could replace the body of `now()`. The controller field is named `timer` because `clock` is a RoboChart reserved word. `clockResetTime` starts at 0.0 and is only read in Turning, after it has been set.
- **java_trace:** `src/main/java/sranger/controller/Clock.java` 9–26, `Clock`: SR-Var1, SR-GP2; `src/main/java/sranger/controller/SRangerController.java` 17–23, `clockResetTime, timer`: SR-Var1

### 9. scope_question: Events no requirement handles are ignored; Final has no outgoing transition
- **raw:** No requirement gives a transition for obstacle in Turning, or for any event in Final. SR-FR3: "Final is the terminal mode." SR-DC1: each SR-Beh maps to exactly one transition.
- **fix_directive:** No extra transitions were invented: obstacle in Turning is ignored, apart from the autonomous check that runs on every step. Final's block is empty, a terminal mode (time still passes there in RoboChart). If a verifier needs Final to accept tick, add a Final → Final tick self-loop. No requirement asks for one.
- **java_trace:** `src/main/java/sranger/controller/SRangerController.java` 61–74, `step (Turning and Final blocks)`: SR-FR3, SR-DC1

## next_step

Run the pipeline (compile, coverage, profile check, transformations, FDR4, Dafny, Isabelle, vacuity) on `java.generated.project`.
