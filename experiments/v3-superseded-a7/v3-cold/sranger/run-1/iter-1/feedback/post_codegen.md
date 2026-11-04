# post_codegen

**status:** uncertain

**summary:** Nine Java files implement the single SRanger controller (modes MOVING, TURNING, FINAL; six transition branches plus the initial mode) and cover all 23 requirements. Eight items flagged: two invented defaults, one ambiguous requirement, five design choices.

## Issues

### 1. [invented_default] Sensor distance when no reading is available: 100.0 m

**raw:** SR-DM5 / system description: when no reading is available, distance() returns 'a large default value' so obstacle detection is not falsely triggered. No number is given.

**fix_directive:** Confirm or replace NO_READING_DISTANCE = 100.0 in Sensor. Any value well above OBSTACLE_THRESHOLD (0.5 m) keeps obstacleDetected false while there is no reading. Sensor.update also treats a negative or NaN reading as 'no reading', so distance() is never negative (SR-DM5); that rule is not stated in the requirements either.

**java_trace:**

- `java.generated.project/src/main/java/sranger/sensor/Sensor.java` lines 11-13, `NO_READING_DISTANCE`, requirement_ids: SR-DM5, SR-SF1
- `java.generated.project/src/main/java/sranger/sensor/Sensor.java` lines 19-25, `update`, requirement_ids: SR-DM5

### 2. [invented_default] Actuator state before the first command: Move(0, 0)

**raw:** SR-DM6: the Actuator stores the latest issued command. Its value before any command is not specified.

**fix_directive:** Before any command is applied, the last linear and angular velocities are 0.0 (robot at rest). Because the controller constructor issues Move(moveVel, 0) at once, this value is never visible once a controller exists. Confirm, or specify another value.

**java_trace:**

- `java.generated.project/src/main/java/sranger/actuator/Actuator.java` lines 11-12, `lastLinearVelocity, lastAngularVelocity`, requirement_ids: SR-DM6

### 3. [ambiguous_requirement] Moving to Turning: event trigger, guard, and threshold boundary

**raw:** SR-Beh2: the transition fires 'when the obstacle event is received'. SR-GP1: obstacleDetected (distance <= obstacleThreshold) 'is used as the guard on the Moving to Turning transition'. SR-DM3: the obstacle event is emitted when the reading 'crosses below' the threshold (strictly below).

**fix_directive:** Implemented as trigger Obstacle AND guard obstacleDetected (distance <= OBSTACLE_THRESHOLD, the 'at or below' of SR-GP1). Consequence: an Obstacle event that arrives while distance() > 0.5 m is ignored in Moving. If the event alone should trigger the transition, drop '&& obstacleDetected' from the branch; SR-GP1 would then have no guard to implement.

**java_trace:**

- `java.generated.project/src/main/java/sranger/controller/SRangerController.java` lines 40-40, `obstacleDetected`, requirement_ids: SR-GP1
- `java.generated.project/src/main/java/sranger/controller/SRangerController.java` lines 49-53, `step`, requirement_ids: SR-Beh2, SR-GP1, SR-DM3

### 4. [design_choice] Initial Move(moveVel, 0) is issued in the controller constructor

**raw:** SR-FR1 / SR-Beh1: Moving is the initial mode, and on entering Moving the controller issues Move(moveVel, 0). The profile has no place for an initial transition's action other than the controller's construction.

**fix_directive:** The constructor calls actuator.apply(new OutputEvent.Move(MOVE_VEL, 0.0)) after storing its collaborators, so the power-up entry into Moving issues the forward command. The extracted model's initial transition may not carry this action. Alternative: issue nothing at construction and rely on the first Turning-to-Moving entry.

**java_trace:**

- `java.generated.project/src/main/java/sranger/controller/SRangerController.java` lines 25-30, `SRangerController`, requirement_ids: SR-FR1, SR-Beh1

### 5. [design_choice] Move carries two record components (lv, av)

**raw:** SR-DM4: Move carries two real values, a linear and an angular velocity. The profile's guidance is one record component for an event that carries a value.

**fix_directive:** OutputEvent.Move is a record with two real components, lv and av, as SR-DM4 states. Alternative, if the model needs a single-typed event: wrap both values in a record datatype and give Move one component of that type.

**java_trace:**

- `java.generated.project/src/main/java/sranger/event/OutputEvent.java` lines 16-17, `Move`, requirement_ids: SR-DM4

### 6. [design_choice] Turning transition priority: endTask, then the timed return, then tick

**raw:** SR-Beh5 (autonomous Turning to Moving when turnDurationElapsed), SR-Beh6 (endTask), SR-Beh7 (tick self-loop). Their relative priority is not stated.

**fix_directive:** endTask (operator shutdown) is first. The timed return is second, so a tick never holds the robot in Turning once the duration has elapsed. The tick self-loop is last and conjoins !turnDurationElapsed, as the profile requires below a guard-only branch.

**java_trace:**

- `java.generated.project/src/main/java/sranger/controller/SRangerController.java` lines 59-71, `step`, requirement_ids: SR-Beh5, SR-Beh6, SR-Beh7, SR-GP2

### 7. [design_choice] Clock: framework-advanced seconds counter; controller field named timer

**raw:** SR-Var1 / SR-GP2: clockResetTime records 'the current time'; the timed transition compares elapsed time since it with turnDuration. The time source is not specified.

**fix_directive:** Clock is a monotonic counter in seconds that starts at 0.0 and is advanced by the framework (advance ignores non-positive steps). The controller's Clock field is named timer, because clock is a reserved word. turnDurationElapsed is timer.now() - clockResetTime >= TURN_DURATION. On Moving to Turning, clockResetTime is set before Move(0, turnVel) is issued, in the order SR-Beh2 lists.

**java_trace:**

- `java.generated.project/src/main/java/sranger/controller/Clock.java` lines 9-25, `Clock`, requirement_ids: SR-Var1
- `java.generated.project/src/main/java/sranger/controller/SRangerController.java` lines 41-41, `turnDurationElapsed`, requirement_ids: SR-GP2
- `java.generated.project/src/main/java/sranger/controller/SRangerController.java` lines 52-52, `clockResetTime`, requirement_ids: SR-Var1, SR-Beh2

### 8. [design_choice] OBSTACLE_THRESHOLD = 0.5 is fractional; the model rounds it up to 1

**raw:** SR-DM2: obstacleThreshold = 0.5 m. The model's constants interface rounds a fractional constant up to the next integer. The guard obstacleDetected depends on its exact value.

**fix_directive:** Kept the specified unit (metres) and value 0.5. In the model, obstacleDetected therefore reads distance <= 1, which is wider than the Java guard. Rescaling to centimetres (50.0) would keep the value exact but contradicts the units the requirements give. moveVel, turnVel and turnDuration are whole numbers and are not affected.

**java_trace:**

- `java.generated.project/src/main/java/sranger/controller/SRangerConstants.java` lines 17-17, `OBSTACLE_THRESHOLD`, requirement_ids: SR-DM2, SR-GP1

## next_step

Run the pipeline: compile, coverage, profile check, transformations, then FDR4, Dafny and Isabelle.
