# post_codegen

**status:** uncertain

**summary:** SRanger implemented in 9 Java files (one controller, 3 modes, 6 transitions plus the initial mode). All 23 requirements are traced in result_codegen.json. One invented default, seven design choices or ambiguities and one scope question are flagged.

## Issues

### 1. [invented_default] Distance reported when no IR reading is available: 1000.0 m

**raw:** SR-DM5 asks for 'a large default value' without giving it. Sensor.distance() returns 1000.0 m until the first reading, and update() treats a negative or NaN reading as no reading (also 1000.0 m), so distance() is always non-negative.

**fix_directive:** Confirm 1000.0 m, or give the required default; change NO_READING_DISTANCE in Sensor.java.

**java_trace:**

- `java.generated.project/src/main/java/sranger/sensor/Sensor.java` lines 10-26, `NO_READING_DISTANCE / update`, requirement_ids: SR-DM5, SR-SF1

### 2. [design_choice] obstacleThreshold = 0.5 is fractional; the model rounds it up to 1

**raw:** The constants interface rounds a fractional constant up to an integer, so the model's obstacleDetected guard reads distance() <= 1 while the Java reads distance() <= 0.5. The exact value matters to this guard. Java keeps 0.5 m because SR-DM2 and SR-DM5 fix the units (metres).

**fix_directive:** None in Java unless the model must keep the exact threshold; rescaling units (e.g. centimetres) would contradict SR-DM5.

**java_trace:**

- `java.generated.project/src/main/java/sranger/controller/SRangerConstants.java` lines 17-17, `OBSTACLE_THRESHOLD`, requirement_ids: SR-DM2
- `java.generated.project/src/main/java/sranger/controller/SRangerController.java` lines 40-40, `obstacleDetected`, requirement_ids: SR-GP1

### 3. [design_choice] Moving to Turning needs both the obstacle event and the obstacleDetected guard

**raw:** SR-Beh2 names the obstacle event as the trigger; SR-GP1 says obstacleDetected (distance <= obstacleThreshold) guards the same transition. Both are used. An obstacle event while the reading is above the threshold is ignored. SR-DM3 says the event fires when the reading 'crosses below' the threshold (strict); the guard uses <= as SR-GP1 states.

**fix_directive:** Confirm that the guard is meant in addition to the event.

**java_trace:**

- `java.generated.project/src/main/java/sranger/controller/SRangerController.java` lines 49-53, `step`, requirement_ids: SR-Beh2, SR-GP1, SR-DM3

### 4. [ambiguous_requirement] Tick self-transitions perform no action, but a RoboChart self-transition re-runs entry actions

**raw:** SR-Beh4 and SR-Beh7 say Moving/Turning to itself on tick performs no action, so the Java only re-assigns the mode. In RoboChart a self-transition exits and re-enters the state; if the extracted model makes Move(...) and the clockResetTime reset entry actions of Moving/Turning, each tick would re-issue Move and, in Turning, reset the clock, postponing Turning to Moving for as long as ticks arrive.

**fix_directive:** Check in the extracted .rct whether the tick self-loops re-enter Moving/Turning with their entry actions; if they do, the requirement and the RoboChart semantics disagree and need a decision (e.g. drop the tick self-loops).

**java_trace:**

- `java.generated.project/src/main/java/sranger/controller/SRangerController.java` lines 54-57, `step`, requirement_ids: SR-Beh4
- `java.generated.project/src/main/java/sranger/controller/SRangerController.java` lines 68-71, `step`, requirement_ids: SR-Beh7

### 5. [design_choice] Turning priority: endTask, then turnDurationElapsed, then tick (with !turnDurationElapsed)

**raw:** The requirements give no priority. endTask is first (operator stop). The autonomous timed transition precedes the tick self-loop, because ticks arrive every cycle and a tick branch placed first would hide it; the tick branch conjoins !turnDurationElapsed, so the two are never enabled together.

**fix_directive:** None unless a different priority is intended.

**java_trace:**

- `java.generated.project/src/main/java/sranger/controller/SRangerController.java` lines 59-71, `step`, requirement_ids: SR-Beh5, SR-Beh6, SR-Beh7, SR-DC1

### 6. [design_choice] Initial entry action of Moving is issued in the controller constructor

**raw:** SR-FR1/SR-Beh1: Moving is initial and on entering Moving the controller issues Move(moveVel, 0). step() holds transitions only, so the constructor issues the initial Move. The Actuator records (0, 0) before any command, which the constructor overwrites at once. The model has this initial command only if the extractor makes Move(moveVel, 0) an entry action of Moving.

**fix_directive:** None in Java; check the .rct for an entry action on Moving.

**java_trace:**

- `java.generated.project/src/main/java/sranger/controller/SRangerController.java` lines 25-30, `SRangerController`, requirement_ids: SR-Beh1, SR-FR1

### 7. [design_choice] Move is one event record with two real components (lv, av)

**raw:** SR-DM4 and the system description give Move(lv : real, av : real) as the single output event. It is one record with two @RoboChartType("real") components, not a wrapper datatype. The rules describe value-carrying events with one component, so the model type of Move (a pair of reals) depends on how the extractor handles two components.

**fix_directive:** If the transformation rejects a two-component event record, wrap (lv, av) in a datatype record and give Move one component of that type.

**java_trace:**

- `java.generated.project/src/main/java/sranger/event/OutputEvent.java` lines 14-15, `Move`, requirement_ids: SR-DM4, SR-DM6

### 8. [design_choice] Time source: Clock class, field named timer, clock reset before the turn command

**raw:** clockResetTime is a controller field assigned from timer.now() (a Clock; 'clock' is reserved, so the field is named timer), and turnDurationElapsed is timer.now() - clockResetTime >= TURN_DURATION, i.e. since(clockResetTime) >= turnDuration. Clock.update(time) never moves time backwards. On Moving to Turning the clock is reset before Move(0, turnVel) is issued, the order SR-Beh2 gives (SR-FR2 lists them the other way round).

**fix_directive:** None.

**java_trace:**

- `java.generated.project/src/main/java/sranger/controller/Clock.java` lines 10-22, `Clock`, requirement_ids: SR-Var1, SR-GP2
- `java.generated.project/src/main/java/sranger/controller/SRangerController.java` lines 42-42, `turnDurationElapsed`, requirement_ids: SR-GP2
- `java.generated.project/src/main/java/sranger/controller/SRangerController.java` lines 52-53, `step`, requirement_ids: SR-Var1, SR-FR2

### 9. [scope_question] Events with no transition in the current mode are ignored; Final is terminal

**raw:** obstacle in Turning, and every event in Final, have no transition in the requirements and are ignored. Final has an empty mode block (no outgoing transitions), which the rules allow; time still passes there.

**fix_directive:** Confirm that Final should have no outgoing transitions and that obstacle during Turning is ignored.

**java_trace:**

- `java.generated.project/src/main/java/sranger/controller/SRangerController.java` lines 73-75, `step`, requirement_ids: SR-FR3, SR-DC1

## next_step

Run the pipeline (compile, coverage, profile check, transformations, FDR4, Dafny, Isabelle, vacuity); in the extracted .rct check the tick self-loops and the entry actions of Moving/Turning (issues 4 and 6) and the typing of Move (issue 7).
