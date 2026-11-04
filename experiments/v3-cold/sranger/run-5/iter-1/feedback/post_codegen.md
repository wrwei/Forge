# post_codegen

- **phase**: codegen
- **status**: uncertain

## Summary

All 23 SRanger requirements are implemented in 9 Java files (package sranger) and traced in result_codegen.json. The project compiles. Nine points the requirements do not settle are recorded below: three invented defaults, five design choices and one ambiguity.

## Issues

### 1. [ambiguous_requirement] Moving to Turning: obstacle event (SR-Beh2) and obstacleDetected guard (SR-GP1) both apply

**raw**: SR-Beh2 triggers Moving to Turning on the obstacle event; SR-GP1 says obstacleDetected (distance <= obstacleThreshold) is the guard on the same transition. The transition is implemented as 'event instanceof InputEvent.Obstacle && obstacleDetected'. An obstacle event that arrives while the IR reading is above the threshold is ignored and the robot keeps moving.

**fix_directive**: If the obstacle event alone should trigger the turn, drop '&& obstacleDetected' from the Moving block's obstacle branch and keep the predicate only for SR-GP1 traceability. If the guard alone should, make the branch autonomous.

**java_trace**:

- `java.generated.project/src/main/java/sranger/controller/SRangerController.java` lines 42-42, element `obstacleDetected`, requirements: SR-GP1
- `java.generated.project/src/main/java/sranger/controller/SRangerController.java` lines 51-55, element `step`, requirements: SR-Beh2, SR-FR2, SR-Var1

### 2. [design_choice] OBSTACLE_THRESHOLD = 0.5 is fractional and the model's constants interface rounds it up to 1

**raw**: The project rules say a fractional constant is rounded up to the next integer in the model's constants interface. obstacleThreshold = 0.5 (SR-DM2) is the only fractional constant, and its exact value matters to the guard obstacleDetected (SR-GP1): in the model the guard reads distance <= 1 rather than distance <= 0.5. moveVel, turnVel and turnDuration are whole numbers and are not affected.

**fix_directive**: None in the Java: the constant is written as the plain decimal literal 0.5, as the rules require. When reading verification results, treat the model's obstacle threshold as an over-approximation of the Java value.

**java_trace**:

- `java.generated.project/src/main/java/sranger/controller/SRangerConstants.java` lines 17-17, element `OBSTACLE_THRESHOLD`, requirements: SR-DM2, SR-GP1

### 3. [design_choice] Power-up Move(moveVel, 0) is issued in the controller constructor

**raw**: SR-FR1 requires Move(moveVel, 0) on entering Moving, and SR-Beh1 makes Moving the power-up mode. step() handles only transitions, so the initial entry command is issued once in the constructor. The extracted model may not carry this action on its initial transition.

**fix_directive**: None needed for the Java behaviour. If the model must show the initial Move, the pipeline would have to map constructor actions to the initial transition.

**java_trace**:

- `java.generated.project/src/main/java/sranger/controller/SRangerController.java` lines 27-32, element `SRangerController`, requirements: SR-FR1, SR-Beh1

### 4. [design_choice] Transition priority within Moving and Turning

**raw**: All requirements have priority 1. endTask (the operator shutdown) is the first branch in both Moving and Turning. In Turning, the autonomous Turning to Moving transition (SR-Beh5) comes before the tick self-loop (SR-Beh7), and the tick branch conjoins !turnDurationElapsed, as the rules require. So a tick that arrives after the turn duration has elapsed takes the robot back to Moving, and endTask beats an elapsed turn. In Java the autonomous transition is evaluated on every step() call, whatever the event. Obstacle events in Turning are ignored, because no requirement gives a transition for them.

**fix_directive**: Reorder the inner branches if the requirements intend a different priority.

**java_trace**:

- `java.generated.project/src/main/java/sranger/controller/SRangerController.java` lines 46-73, element `step`, requirements: SR-Beh2, SR-Beh3, SR-Beh4, SR-Beh5, SR-Beh6, SR-Beh7, SR-DC1

### 5. [design_choice] Move carries both velocities as two real record components

**raw**: SR-DM4 gives Move two real values (lv, av). The profile's usual event shape is one record component, so Move is the only event with two components: record Move(double lv, double av). How the extraction types a two-component event (for example as a product type) is not stated in the rules.

**fix_directive**: If the extraction needs a single component, wrap lv and av in a record datatype and give Move one component of that type.

**java_trace**:

- `java.generated.project/src/main/java/sranger/event/OutputEvent.java` lines 16-16, element `Move`, requirements: SR-DM4, SR-DM6

### 6. [design_choice] Time base: Clock class, field named timer, seconds since creation

**raw**: clockResetTime (SR-Var1) and turnDurationElapsed (SR-GP2) use a class named Clock, the extractor's clock convention. The controller field is named timer because 'clock' is a RoboChart reserved word. Clock.now() returns real seconds since the clock was created (System.nanoTime), so it is never negative. Clock is in the controller package, not the sensor package, so that it is not taken for a sensor.

**fix_directive**: None expected.

**java_trace**:

- `java.generated.project/src/main/java/sranger/controller/Clock.java` lines 10-19, element `Clock`, requirements: SR-Var1, SR-GP2
- `java.generated.project/src/main/java/sranger/controller/SRangerController.java` lines 44-44, element `turnDurationElapsed`, requirements: SR-GP2

### 7. [invented_default] No-reading IR distance is 1000.0 m; negative or NaN readings count as no reading

**raw**: SR-DM5 asks for 'a large default value' when no reading is available but gives no number. NO_READING_DISTANCE = 1000.0 m, well above obstacleThreshold, is returned before the first reading and after an invalid one (negative or NaN), so distance() is always non-negative.

**fix_directive**: Replace 1000.0 with the intended default if the requirements fix one.

**java_trace**:

- `java.generated.project/src/main/java/sranger/sensor/Sensor.java` lines 13-27, element `NO_READING_DISTANCE`, requirements: SR-DM5, SR-SF1

### 8. [invented_default] Initial value of clockResetTime is 0.0

**raw**: SR-Var1 does not give an initial value. clockResetTime starts at 0.0 and is read only in Turning, after Moving to Turning has set it.

**fix_directive**: None expected.

**java_trace**:

- `java.generated.project/src/main/java/sranger/controller/SRangerController.java` lines 17-17, element `clockResetTime`, requirements: SR-Var1

### 9. [invented_default] Actuator's stored velocities start at 0.0

**raw**: SR-DM6 does not say what the Actuator reports before any command. Both stored velocities start at 0.0 (at rest). The controller constructor immediately issues Move(moveVel, 0), so this is visible only before a controller is attached.

**fix_directive**: None expected.

**java_trace**:

- `java.generated.project/src/main/java/sranger/actuator/Actuator.java` lines 12-13, element `Actuator`, requirements: SR-DM6

## Notes

- Final (SR-FR3) has no outgoing transitions: it is a terminal mode, as the rules allow. No self-loop on tick was added, because no requirement gives one (SR-DC1).
- SR-DC1 is enforced: each of SR-Beh2 to SR-Beh7 is exactly one branch of step(), SR-Beh1 is the initial value of currentMode, and no mode has two branches for the same trigger.

## Next step

Run the pipeline (compile, coverage, profile check, transformations, verifiers).
