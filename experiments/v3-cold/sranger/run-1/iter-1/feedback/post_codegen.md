# post_codegen

**status:** `uncertain`

**summary:** All 23 SRanger requirements are implemented in 9 Java files under sranger/ and traced in result_codegen.json. 9 points flagged: 2 invented defaults, 5 design choices, 1 ambiguous requirement, 1 scope question; nothing unimplemented.

## Issues

### 1. No-reading IR distance is 1000.0 m

- **kind:** `invented_default`
- **raw:** SR-DM5 requires a 'large default value' when no reading is available but gives no number. Sensor returns NO_READING_DISTANCE = 1000.0 m (far beyond any IR range) until a valid reading arrives and after clearReading().
- **fix_directive:** Replace 1000.0 if the platform specifies a different no-reading value; it must stay above OBSTACLE_THRESHOLD.
- **java_trace:**
  - `src/main/java/sranger/sensor/Sensor.java` lines 12-14, `NO_READING_DISTANCE`, requirement_ids: SR-DM5, SR-SF1

### 2. clockResetTime starts at 0.0

- **kind:** `invented_default`
- **raw:** SR-Var1 defines clockResetTime only from the first entry into Turning. The field is initialised to 0.0; it is read only in Turning, after Moving -> Turning has reset it.
- **fix_directive:** None needed unless an initial value is specified.
- **java_trace:**
  - `src/main/java/sranger/controller/SRangerController.java` lines 17-17, `clockResetTime`, requirement_ids: SR-Var1

### 3. Move is one output event record with two real components

- **kind:** `design_choice`
- **raw:** SR-DM4 gives one output event carrying two reals. Implemented as record Move(double lv, double av), not as a single component of a datatype or as two events (which SR-DM4 forbids). The profile's examples show events with at most one component.
- **fix_directive:** If the transformation cannot type a two-component event, wrap lv and av in a record type and give Move that single component.
- **java_trace:**
  - `src/main/java/sranger/event/OutputEvent.java` lines 16-16, `Move`, requirement_ids: SR-DM4, SR-DM6

### 4. Moving -> Turning: obstacle event versus obstacleDetected guard

- **kind:** `ambiguous_requirement`
- **raw:** SR-Beh2 triggers the transition on the obstacle event alone; SR-GP1 says obstacleDetected is its guard. SR-DM3 says the event fires when the distance crosses below (strict) the threshold, SR-GP1 tests distance <= threshold. Implemented as trigger obstacle with guard obstacleDetected (<=), so a spurious obstacle event with no valid reading does not turn the robot (SR-DM5).
- **fix_directive:** Drop the guard if the event alone must trigger the turn.
- **java_trace:**
  - `src/main/java/sranger/controller/SRangerController.java` lines 38-38, `obstacleDetected`, requirement_ids: SR-GP1, SR-DM3
  - `src/main/java/sranger/controller/SRangerController.java` lines 46-49, `step`, requirement_ids: SR-Beh2, SR-FR2, SR-Var1

### 5. Initial Move(moveVel, 0) issued in the constructor

- **kind:** `design_choice`
- **raw:** SR-FR1 issues Move(moveVel, 0) on entering Moving, and SR-Beh1 enters Moving at power-up, but no step() branch leads into the initial mode. The constructor issues the command once.
- **fix_directive:** None needed unless start-up must wait for the first event.
- **java_trace:**
  - `src/main/java/sranger/controller/SRangerController.java` lines 23-28, `SRangerController`, requirement_ids: SR-FR1, SR-Beh1

### 6. Transition priority within Moving and Turning

- **kind:** `design_choice`
- **raw:** The requirements give no priorities. endTask (a stop that applies in both modes) is the first branch of each. In Turning, the timed exit is above the tick self-loop, and the tick branch conjoins !turnDurationElapsed so the model's alternatives do not overlap. A tick arriving after turnDuration therefore returns the robot to Moving. Events with no transition in the current mode (obstacle in Turning, anything in Final) are ignored.
- **fix_directive:** Reorder if the shutdown must not take priority over the timed exit.
- **java_trace:**
  - `src/main/java/sranger/controller/SRangerController.java` lines 42-69, `step`, requirement_ids: SR-Beh3, SR-Beh4, SR-Beh5, SR-Beh6, SR-Beh7, SR-DC1, SR-FR3

### 7. obstacleThreshold 0.5 is rounded up to 1 in the model

- **kind:** `scope_question`
- **raw:** OBSTACLE_THRESHOLD = 0.5 is the only fractional constant; the model's constants interface rounds it up to 1, so the model's obstacleDetected guard reads distance() <= 1. distance() is a sensor function in the model, so deadlock and divergence results do not depend on the value, but any obligation about the threshold's exact value would use 1. MOVE_VEL, TURN_VEL and TURN_DURATION are whole numbers and are not affected.
- **fix_directive:** None in the Java; the Java keeps the specified 0.5.
- **java_trace:**
  - `src/main/java/sranger/controller/SRangerConstants.java` lines 17-17, `OBSTACLE_THRESHOLD`, requirement_ids: SR-DM2, SR-GP1

### 8. Clock is a framework-advanced time source, held in field timer

- **kind:** `design_choice`
- **raw:** The requirements need 'the current time' but do not name a source. Clock holds a monotonic time in seconds advanced by update(); now() reads it. The controller field is named timer because clock is a RoboChart reserved word. clockResetTime = timer.now() and timer.now() - clockResetTime >= TURN_DURATION follow the clock convention.
- **fix_directive:** None needed.
- **java_trace:**
  - `src/main/java/sranger/time/Clock.java` lines 9-25, `Clock`, requirement_ids: SR-Var1, SR-GP2
  - `src/main/java/sranger/controller/SRangerController.java` lines 20-20, `timer`, requirement_ids: SR-Var1
  - `src/main/java/sranger/controller/SRangerController.java` lines 39-39, `turnDurationElapsed`, requirement_ids: SR-GP2

### 9. Invalid readings mean no reading; Sensor and Actuator are classes

- **kind:** `design_choice`
- **raw:** A negative or NaN measurement passed to Sensor.update() is treated as no reading (the large default), keeping distance() non-negative (SR-DM5). SR-ARCH2 calls the Sensor and Actuator 'interfaces'; they are concrete classes, following the profile's examples.
- **fix_directive:** None needed.
- **java_trace:**
  - `src/main/java/sranger/sensor/Sensor.java` lines 20-26, `update`, requirement_ids: SR-DM5
  - `src/main/java/sranger/controller/SRangerController.java` lines 15-21, `SRangerController`, requirement_ids: SR-ARCH2

## next_step

Run the pipeline. If a transformation phase rejects the Move event, start with the 'Move is one output event record' issue.
