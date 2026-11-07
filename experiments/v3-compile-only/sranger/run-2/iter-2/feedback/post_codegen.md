# post_codegen

**status:** uncertain

**summary:** All 23 SRanger requirements are implemented in 9 Java files (one controller, 3 modes, 6 transitions plus the initial mode). The issues below record defaults and choices that the requirements did not settle; none is blocking.

## Issues

### 1. [invented_default] Sensor no-reading default distance is 100.0 m

- **raw:** SR-DM5 asks for 'a large default value' when no reading is available but gives no number.
- **fix_directive:** Confirm or replace NO_READING_DISTANCE = 100.0 m (well above obstacleThreshold, beyond any IR range). A negative or NaN measurement passed to recordReading is also treated as 'no reading' and stored as this default, which keeps distance() non-negative.
- **java_trace:**
  - `java.generated.project/src/main/java/sranger/sensor/Sensor.java` lines 15-42 — NO_READING_DISTANCE / distance / recordReading — SR-DM5, SR-SF1

### 2. [invented_default] Actuator stores (0, 0) before any command

- **raw:** SR-DM6 does not say what the actuator reports before the first Move.
- **fix_directive:** lastLinearVelocity and lastAngularVelocity start at 0.0. The constructor issues Move(moveVel, 0) at once, so the default is visible only before the controller exists.
- **java_trace:**
  - `java.generated.project/src/main/java/sranger/actuator/Actuator.java` lines 12-16 — lastLinearVelocity / lastAngularVelocity — SR-DM6

### 3. [invented_default] Clock starts at 0 s; clockResetTime starts at 0.0

- **raw:** SR-Var1 gives no initial value for clockResetTime; the description gives no time source.
- **fix_directive:** clockResetTime is only read in Turning and is always set on Moving -> Turning, so its initial value never affects behaviour. Clock is a monotone time source that the framework advances.
- **java_trace:**
  - `java.generated.project/src/main/java/sranger/controller/SRangerController.java` lines 19-21 — clockResetTime — SR-Var1
  - `java.generated.project/src/main/java/sranger/controller/Clock.java` lines 9-34 — Clock — SR-Var1, SR-GP2

### 4. [design_choice] Moving -> Turning is triggered by obstacle and guarded by obstacleDetected

- **raw:** SR-Beh2 says the transition fires on the obstacle event; SR-GP1 says obstacleDetected (distance <= obstacleThreshold) is its guard.
- **fix_directive:** Both are honoured: the trigger is obstacle and the condition is obstacleDetected. An obstacle event with distance > threshold is ignored. On the transition clockResetTime is set to the current time, then Move(0, turnVel) is issued.
- **java_trace:**
  - `java.generated.project/src/main/java/sranger/controller/SRangerController.java` lines 59-69 — step / obstacleDetected — SR-Beh2, SR-GP1, SR-FR2

### 5. [design_choice] Turning: the timed transition takes priority over the tick self-loop

- **raw:** SR-Beh5 (autonomous, elapsed >= turnDuration) and SR-Beh7 (tick, stay in Turning) can both apply in the same cycle; tick arrives every cycle.
- **fix_directive:** The autonomous branch comes before the tick branch, and the tick branch is guarded by !turnDurationElapsed, so the two transitions are disjoint in the model as well. Without this the robot would never leave Turning, because every cycle carries a tick.
- **java_trace:**
  - `java.generated.project/src/main/java/sranger/controller/SRangerController.java` lines 60, 78-83 — step / turnDurationElapsed — SR-Beh5, SR-Beh7, SR-GP2

### 6. [design_choice] endTask is the first branch in Moving and Turning

- **raw:** The requirements give no priority between endTask and other transitions.
- **fix_directive:** endTask is the operator shutdown, so it overrides the others: it is the first inner branch in both modes, including over the timed Turning -> Moving transition.
- **java_trace:**
  - `java.generated.project/src/main/java/sranger/controller/SRangerController.java` lines 63-65, 75-77 — step — SR-Beh3, SR-Beh6, SR-FR3

### 7. [design_choice] Initial entry action of Moving is issued in the constructor

- **raw:** SR-FR1 requires Move(moveVel, 0) on entering Moving; SR-Beh1 makes Moving the initial mode, but there is no branch for the initial entry.
- **fix_directive:** The constructor issues Move(moveVel, 0) after the mode field is initialised to MOVING.
- **java_trace:**
  - `java.generated.project/src/main/java/sranger/controller/SRangerController.java` lines 17, 35-40 — SRangerController / currentMode — SR-Beh1, SR-FR1

### 8. [ambiguous_requirement] obstacleThreshold = 0.5 is fractional; the model rounds it up to 1

- **raw:** The profile's constants interface rounds fractional constants up, and SR-GP1 compares distance against obstacleThreshold.
- **fix_directive:** The Java uses the exact 0.5 m. In the extracted model the guard reads distance <= 1, which detects obstacles at more distances than the Java does. The other three constants (1.0, 2.0, 2.0) are integral and unaffected.
- **java_trace:**
  - `java.generated.project/src/main/java/sranger/controller/SRangerConstants.java` lines 17 — OBSTACLE_THRESHOLD — SR-DM2, SR-GP1

### 9. [design_choice] Clock field is named timer

- **raw:** 'clock' is a reserved RoboChart word and may not name a controller field.
- **fix_directive:** The Clock-typed field is named timer; clockResetTime = timer.now() and timer.now() - clockResetTime >= TURN_DURATION follow the profile's clock convention.
- **java_trace:**
  - `java.generated.project/src/main/java/sranger/controller/SRangerController.java` lines 25, 60, 68 — timer — SR-Var1, SR-GP2

### 10. [scope_question] Events with no transition are ignored

- **raw:** No requirement gives a transition for obstacle in Turning, or for any event in Final.
- **fix_directive:** Final has no outgoing transitions (terminal mode). obstacle in Turning is ignored. No transitions beyond SR-Beh1 to SR-Beh7 were added (SR-DC1).
- **java_trace:**
  - `java.generated.project/src/main/java/sranger/controller/SRangerController.java` lines 74-87 — step — SR-FR3, SR-DC1

### 11. [design_choice] Turning: endTask is kept before the triggerless timed branch (preflight rule 8 warning accepted)

- **raw:** Preflight warning rule8_event_branch_precedes_triggerless at SRangerController.java:78: in the model both endTask -> Final and the timed Turning -> Moving are enabled when turnDurationElapsed holds.
- **fix_directive:** Kept on purpose. Moving the timed branch first would make the Java drop an endTask that arrives in the same cycle the turn time runs out, so the operator's shutdown would be lost. As written, the Java always takes Final in that case, and the model allows Final or Moving (and Moving still accepts endTask), so the model covers the Java's behaviour. The only effect is nondeterminism in the model, which is not checked.
- **java_trace:**
  - `java.generated.project/src/main/java/sranger/controller/SRangerController.java` lines 75-80 — step — SR-Beh5, SR-Beh6, SR-GP2

## next_step

Run the pipeline (compile, coverage, profile check, transformations, verifiers) and fix the Java from any failing phase's report.
