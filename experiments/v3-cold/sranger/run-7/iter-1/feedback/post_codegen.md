# post_codegen — codegen self-review

**status:** uncertain

**summary:** All 23 SRanger requirements are implemented in 9 Java files (single controller SRangerController, modes MOVING/TURNING/FINAL, 6 transitions plus the initial mode). Nothing is unimplemented. Flagged: 4 invented defaults, 4 design choices, 2 ambiguous requirements, 1 scope question about the model's rounding of OBSTACLE_THRESHOLD.

## Issues

### 1. invented_default — No-reading IR distance is 1000.0 m
- **raw:** SR-DM5 asks for 'a large default value' when no reading is available but gives no number.
- **fix_directive:** Sensor.distance() returns NO_READING_DISTANCE = 1000.0 until a valid reading arrives; far above OBSTACLE_THRESHOLD so obstacleDetected stays false. Change the value if the platform defines one.
- **java_trace:** `src/main/java/sranger/sensor/Sensor.java` lines 11–13, `NO_READING_DISTANCE` — SR-DM5, SR-SF1

### 2. design_choice — Negative or NaN IR readings are treated as no reading
- **raw:** SR-DM5 / SR-SF1 say the reading is non-negative but do not say what to do with an invalid measurement.
- **fix_directive:** Sensor.update() maps a negative or NaN value to NO_READING_DISTANCE, so distance() stays non-negative and invalid data cannot trigger a turn. The alternative (clamp to 0.0, forcing a turn) was rejected because SR-DM5 says missing data must not falsely trigger detection.
- **java_trace:** `src/main/java/sranger/sensor/Sensor.java` lines 21–27, `update` — SR-DM5

### 3. invented_default — Actuator's last command before any command is Move(0, 0)
- **raw:** SR-DM6 says the Actuator stores the latest command but not its value before the first one.
- **fix_directive:** lastLinearVelocity and lastAngularVelocity start at 0.0 (stationary). The controller constructor overwrites them immediately with Move(MOVE_VEL, 0).
- **java_trace:** `src/main/java/sranger/actuator/Actuator.java` lines 12–13, `Actuator` — SR-DM6

### 4. invented_default — clockResetTime starts at 0.0
- **raw:** SR-Var1 defines when clockResetTime is set (on Moving -> Turning) but not its value before the first turn.
- **fix_directive:** Initialised to 0.0. It is only read in Turning, which is always entered through the branch that resets it, so the initial value has no behavioural effect.
- **java_trace:** `src/main/java/sranger/controller/SRangerController.java` line 18, `clockResetTime` — SR-Var1

### 5. design_choice — Clock is a framework-advanced simulated clock starting at 0.0 s
- **raw:** SR-Var1 / SR-FR2 require 'the current time' but name no time source.
- **fix_directive:** Clock.now() returns a time advanced by the framework through advance(seconds); non-positive or NaN steps are ignored so time is monotonic. The controller field is named timer (clock is a reserved word); clockResetTime = timer.now() and timer.now() - clockResetTime >= TURN_DURATION follow the clock convention, so the model has a clock with since(clockResetTime).
- **java_trace:** `src/main/java/sranger/timing/Clock.java` lines 10–28, `Clock` — SR-Var1

### 6. design_choice — Move is one output record with two real components, sent through Actuator.apply
- **raw:** SR-DM4 requires a single output event carrying two reals; the profile's examples show single-value events.
- **fix_directive:** OutputEvent.Move(double lv, double av) is the only output; the controller emits it as actuator.apply(new OutputEvent.Move(lv, av)). If the extractor cannot type an event with two components, wrap them in a datatype record rather than splitting Move into two events (SR-DM4 forbids extra events).
- **java_trace:**
  - `src/main/java/sranger/event/OutputEvent.java` line 16, `Move` — SR-DM4
  - `src/main/java/sranger/actuator/Actuator.java` lines 20–26, `apply` — SR-DM6

### 7. ambiguous_requirement — Moving -> Turning needs both the obstacle event and obstacleDetected
- **raw:** SR-Beh2 triggers the transition on the obstacle event; SR-GP1 says obstacleDetected (distance <= obstacleThreshold) is its guard.
- **fix_directive:** Implemented as trigger obstacle with condition obstacleDetected. An obstacle event while distance() > OBSTACLE_THRESHOLD is ignored (the robot keeps moving). If the event alone should suffice, drop '&& obstacleDetected'.
- **java_trace:**
  - `src/main/java/sranger/controller/SRangerController.java` line 50, `obstacleDetected` — SR-GP1
  - `src/main/java/sranger/controller/SRangerController.java` lines 58–61, `step` — SR-Beh2, SR-FR2, SR-Var1

### 8. design_choice — Turning priority: endTask > turnDurationElapsed > tick
- **raw:** SR-Beh5, SR-Beh6 and SR-Beh7 give no priority among Turning's transitions.
- **fix_directive:** endTask is first (a stop overrides). The autonomous return to Moving precedes tick, otherwise a framework that delivers tick every cycle would keep the robot turning forever; tick is therefore guarded by !turnDurationElapsed, as the profile requires below a guard-only branch.
- **java_trace:** `src/main/java/sranger/controller/SRangerController.java` lines 66–75, `step` — SR-Beh5, SR-Beh6, SR-Beh7, SR-GP2

### 9. ambiguous_requirement — Power-up entry command for Moving is issued in the constructor
- **raw:** SR-FR1 says entering Moving issues Move(moveVel, 0); SR-Beh1 says the controller starts in Moving but not whether start-up counts as entering it.
- **fix_directive:** Read as yes: the constructor issues Move(MOVE_VEL, 0). The extracted model may not carry this initial action, since only step() is translated into transitions. The Moving tick self-loop performs no action, per SR-Beh4.
- **java_trace:** `src/main/java/sranger/controller/SRangerController.java` lines 31–36, `SRangerController` — SR-FR1, SR-Beh1

### 10. scope_question — OBSTACLE_THRESHOLD = 0.5 is rounded up to 1 in the model
- **raw:** The model's constants interface rounds a fractional constant up to an integer; obstacleDetected compares against it.
- **fix_directive:** Java uses 0.5 exactly. In the model the guard of Moving -> Turning becomes distance() <= 1, so model-level results about this guard are for threshold 1, not 0.5. MOVE_VEL, TURN_VEL and TURN_DURATION are integral and unaffected.
- **java_trace:** `src/main/java/sranger/controller/SRangerConstants.java` line 17, `OBSTACLE_THRESHOLD` — SR-DM2, SR-GP1

### 11. design_choice — Final has no outgoing transitions; events there are ignored
- **raw:** SR-FR3 calls Final terminal; SR-DC1 allows exactly the seven listed behaviours.
- **fix_directive:** The FINAL block is empty: tick, obstacle and endTask have no effect after shutdown. No self-loop was added, to keep SR-DC1's one-transition-per-behaviour mapping.
- **java_trace:** `src/main/java/sranger/controller/SRangerController.java` lines 77–79, `step` — SR-FR3, SR-DC1

## next_step

Run the pipeline (compile, coverage, profile check, transformations, verifiers). No requirement is unimplemented; the flagged items are choices to confirm, not known defects.
