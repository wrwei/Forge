# post_codegen

**status:** uncertain

## Summary

9 Java files implement all 23 SRanger requirements: one controller (SRangerController) with modes MOVING, TURNING, FINAL; 7 behavioural requirements, each one branch or the initial value (SR-DC1 holds by construction); events Obstacle, Tick, EndTask in and Move(lv, av) out; Sensor.distance(); a Clock for the timed Turning -> Moving transition. No requirement is unimplemented; 3 invented defaults, 4 design choices, 1 ambiguity and 2 scope questions are listed.

## Issues

### 1. [invented_default] Sensor: value of distance() when no reading is available

**raw:** SR-DM5 requires a 'large default value' when no reading is available but gives no number. Chosen: NO_READING_DISTANCE = 1000.0 m, the initial value of the reading. updateDistance ignores a negative or NaN reading (not a valid non-negative distance) and keeps the previous reading.

**fix_directive:** None required unless the requirement authors fix a different default or want invalid readings handled differently (for example clamped to 0, which would trigger obstacle detection).

**java_trace:**

- `java.generated.project/src/main/java/sranger/sensor/Sensor.java` lines 9-32, `Sensor`, requirements: SR-DM5, SR-SF1

### 2. [invented_default] Actuator: last command before any Move is issued

**raw:** SR-DM6 says the Actuator stores the latest command but not its value before the first one. Chosen: lv = 0.0, av = 0.0 (at rest). In practice the controller's constructor issues Move(moveVel, 0) at once.

**fix_directive:** None required.

**java_trace:**

- `java.generated.project/src/main/java/sranger/actuator/Actuator.java` lines 10-18, `Actuator`, requirements: SR-DM6

### 3. [invented_default] Clock origin and initial clockResetTime

**raw:** No requirement says where time comes from. Chosen: a Clock class whose time starts at 0 s and is advanced (never moved back) by the system framework; clockResetTime starts at 0.0 and is only read in Turning, after Moving -> Turning has set it.

**fix_directive:** None required.

**java_trace:**

- `java.generated.project/src/main/java/sranger/controller/Clock.java` lines 5-25, `Clock`, requirements: SR-Var1, SR-GP2
- `java.generated.project/src/main/java/sranger/controller/SRangerController.java` lines 15-16, `clockResetTime`, requirements: SR-Var1

### 4. [design_choice] Initial entry action of Moving issued in the controller constructor

**raw:** SR-FR1/SR-Beh1: Moving is the initial mode and entering Moving issues Move(moveVel, 0). At power-up there is no transition branch in step(), so the constructor issues the command. The extracted model may not show this action on its initial transition.

**fix_directive:** If the extracted model must show the initial Move, the pipeline has to read it from the constructor; the Java behaviour is as required.

**java_trace:**

- `java.generated.project/src/main/java/sranger/controller/SRangerController.java` lines 21-27, `SRangerController`, requirements: SR-FR1, SR-Beh1

### 5. [ambiguous_requirement] Moving -> Turning: event trigger plus obstacleDetected guard; <= versus 'crosses below'

**raw:** SR-Beh2 says the transition fires when the obstacle event is received; SR-GP1 says obstacleDetected is its guard. Implemented as trigger obstacle AND guard obstacleDetected, so an obstacle event with distance() above the threshold is ignored. SR-GP1 says distance <= obstacleThreshold, the system description says the event is emitted when the distance 'crosses below' (strict); <= was used, as SR-GP1 states.

**fix_directive:** None required unless the authors intend the event alone (no guard) to trigger the turn.

**java_trace:**

- `java.generated.project/src/main/java/sranger/controller/SRangerController.java` lines 36-36, `obstacleDetected`, requirements: SR-GP1
- `java.generated.project/src/main/java/sranger/controller/SRangerController.java` lines 43-46, `step`, requirements: SR-Beh2

### 6. [scope_question] obstacleThreshold = 0.5 is fractional and is used in a guard

**raw:** The model's constants interface rounds a fractional constant up to an integer, so obstacleThreshold becomes 1 there and the model's guard reads distance() <= 1, while the Java tests <= 0.5. The other three constants (1.0, 2.0, 2.0) are integral and unaffected. The value is fixed by SR-DM2 and was kept.

**fix_directive:** Treat verifier results on the obstacle guard as concerning the rounded threshold; do not change the Java constant.

**java_trace:**

- `java.generated.project/src/main/java/sranger/controller/SRangerConstants.java` lines 15-16, `OBSTACLE_THRESHOLD`, requirements: SR-DM2, SR-GP1

### 7. [design_choice] Turning: timed transition ranked above the tick self-loop

**raw:** Turning has endTask -> Final (first, as an override shared with Moving), then the autonomous turnDurationElapsed -> Moving (SR-Beh5), then tick -> Turning (SR-Beh7) guarded by !turnDurationElapsed. If tick came first, a tick delivered every cycle would keep the controller in Turning for ever in Java; the negation keeps the model's alternatives disjoint, as the profile requires.

**fix_directive:** None required.

**java_trace:**

- `java.generated.project/src/main/java/sranger/controller/SRangerController.java` lines 51-63, `step`, requirements: SR-Beh5, SR-Beh6, SR-Beh7, SR-GP2

### 8. [design_choice] Move as a record with two real components

**raw:** SR-DM4 and the system description give Move(lv : real, av : real). Implemented as OutputEvent.Move(double lv, double av), both annotated real, rather than a single component of an invented datatype. The self-loops of SR-Beh4 and SR-Beh7 assign the mode only and issue no Move, as those requirements state.

**fix_directive:** If the extractor supports only single-component events, wrap lv and av in a domain record and give Move that one component.

**java_trace:**

- `java.generated.project/src/main/java/sranger/event/OutputEvent.java` lines 15-15, `Move`, requirements: SR-DM4

### 9. [design_choice] Clock service in the controller package, field named timer

**raw:** RoboChart clocks belong to the state machine, so Clock is in sranger.controller. The controller's Clock field is named timer because clock is reserved. clockResetTime = timer.now() is the clock reset and timer.now() - clockResetTime >= TURN_DURATION is meant to become since(clockResetTime) >= turnDuration.

**fix_directive:** None required.

**java_trace:**

- `java.generated.project/src/main/java/sranger/controller/SRangerController.java` lines 15-19, `timer`, requirements: SR-Var1, SR-GP2
- `java.generated.project/src/main/java/sranger/controller/SRangerController.java` lines 37-37, `turnDurationElapsed`, requirements: SR-GP2

### 10. [scope_question] Obstacle event while Turning is ignored

**raw:** No requirement gives a transition for obstacle in Turning, so it is ignored. In Java, if an obstacle event arrives in the same cycle that the turn duration elapses, the autonomous Turning -> Moving transition consumes it; if the sensor framework emits obstacle only on a downward crossing, it is not repeated and the robot drives forward towards a near obstacle. Not handled, because handling it would add a transition no requirement states.

**fix_directive:** Raise with the requirement authors; no Java change without a requirement.

**java_trace:**

- `java.generated.project/src/main/java/sranger/controller/SRangerController.java` lines 51-63, `step`, requirements: SR-DM3, SR-Beh5

## Next step

Run the pipeline. None of the issues blocks it; they record decisions the requirements did not settle.
