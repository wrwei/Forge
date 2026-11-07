# post_codegen

**status:** uncertain

**summary:** All 23 requirements are implemented and traced; the project compiles. Six items are flagged for review: one invented default, one ambiguity and four design choices. Nothing is unimplemented.

## Issues

### 1. [invented_default] Sensor no-reading distance is 1000.0 m; negative readings treated as no reading

**raw:** SR-DM5 requires distance() to return 'a large default value' when no reading is available but gives no number. Chosen: NO_READING_DISTANCE = 1000.0 m (far above obstacleThreshold = 0.5). Sensor.update() also maps a negative (invalid) reading to this default so distance() stays non-negative.

**fix_directive:** Confirm 1000.0 m is acceptable as the no-reading value, and that an invalid negative reading should count as 'no reading' rather than 0.0 (which would trigger obstacle detection).

**java_trace:**
- `java.generated.project/src/main/java/sranger/sensor/Sensor.java` lines 13-36 — Sensor.NO_READING_DISTANCE / update / clearReading — SR-DM5, SR-SF1

### 2. [design_choice] Fractional constant OBSTACLE_THRESHOLD = 0.5 matters to the obstacleDetected guard

**raw:** The model's constants interface rounds fractional constants up, so OBSTACLE_THRESHOLD (0.5 m) becomes 1 in the model and the guard distance() <= obstacleThreshold is looser in the model than in Java. The requirements fix both the value (0.5) and the unit (metres), so the constant is kept as specified. MOVE_VEL, TURN_VEL and TURN_DURATION are integral and are not affected.

**fix_directive:** None in Java. When reading verification results, remember that the model threshold is 1, not 0.5.

**java_trace:**
- `java.generated.project/src/main/java/sranger/controller/SRangerConstants.java` lines 18-20 — OBSTACLE_THRESHOLD — SR-DM2, SR-GP1
- `java.generated.project/src/main/java/sranger/controller/SRangerController.java` lines 59-59 — obstacleDetected — SR-GP1

### 3. [ambiguous_requirement] Moving -> Turning uses both the obstacle event and the obstacleDetected guard

**raw:** SR-Beh2 triggers the transition on the obstacle event; SR-GP1 says obstacleDetected (distance <= threshold) is the guard on the same transition. SR-DM3 says the event fires when the distance crosses *below* the threshold (strict), while SR-GP1 uses 'at or below'. Implemented as trigger Obstacle with condition obstacleDetected, so an obstacle event that arrives while distance() > threshold is ignored.

**fix_directive:** Confirm the transition should require both, rather than the event alone.

**java_trace:**
- `java.generated.project/src/main/java/sranger/controller/SRangerController.java` lines 66-69 — step: MOVING -> TURNING — SR-Beh2, SR-GP1, SR-DM3

### 4. [design_choice] Transition priority in Turning: endTask, then timed return, then tick

**raw:** No priorities are given. endTask is the override, so it is first in both Moving and Turning. In Turning the autonomous turnDurationElapsed transition comes before the tick self-loop, and the tick branch conjoins !turnDurationElapsed; with tick first, a tick every cycle would keep the robot in Turning forever. Tick self-loops perform no action (SR-Beh4, SR-Beh7) and in particular do not reset clockResetTime, so the turn duration counts from the Moving -> Turning transition. Obstacle in Turning, and every event in Final, have no transition and are ignored.

**fix_directive:** Confirm the priority order.

**java_trace:**
- `java.generated.project/src/main/java/sranger/controller/SRangerController.java` lines 62-85 — step — SR-Beh2, SR-Beh3, SR-Beh4, SR-Beh5, SR-Beh6, SR-Beh7, SR-DC1

### 5. [design_choice] Initial Move(moveVel, 0) is issued in the constructor

**raw:** SR-FR1 says entering Moving issues Move(moveVel, 0), and Moving is the initial mode (SR-Beh1). Entry actions are written inline on each transition into a mode (Turning -> Moving), and the power-up entry is issued once in the controller constructor. The extracted model may not carry the constructor's command onto its initial transition.

**fix_directive:** None unless the model must show the power-up Move command.

**java_trace:**
- `java.generated.project/src/main/java/sranger/controller/SRangerController.java` lines 36-41 — SRangerController constructor — SR-FR1, SR-Beh1

### 6. [design_choice] Move carries two real components; the clock field is named timer

**raw:** OutputEvent.Move(lv, av) has two real components, as SR-DM4 gives it, rather than one datatype component. The time source is a class named Clock (sranger.time.Clock), held in the controller field 'timer' because 'clock' is a RoboChart reserved word. clockResetTime = timer.now() makes clockResetTime a RoboChart clock, and the guard timer.now() - clockResetTime >= TURN_DURATION becomes since(clockResetTime) >= turnDuration (SR-Var1, SR-GP2).

**fix_directive:** If the toolchain cannot type an event with two components, wrap (lv, av) in a datatype record.

**java_trace:**
- `java.generated.project/src/main/java/sranger/event/OutputEvent.java` lines 17-17 — Move — SR-DM4
- `java.generated.project/src/main/java/sranger/controller/SRangerController.java` lines 20-26 — clockResetTime / timer — SR-Var1, SR-GP2

## next_step

Run the pipeline. Revisit these items only if verification feedback points at them.
