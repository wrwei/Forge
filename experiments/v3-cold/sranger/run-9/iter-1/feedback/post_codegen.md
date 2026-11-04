# Post-codegen review

**Phase:** codegen  
**Status:** uncertain

## Summary

All 23 SRanger requirements are implemented in 9 Java files (one controller, three modes, seven transitions) and traced in result_codegen.json. The issues below are decisions the requirements did not settle; none is an unimplemented requirement.

## Issues (11)

### 1. [invented_default] Sensor default distance when no reading is available

**Raw:** SR-DM5 asks for 'a large default value' without giving one. Sensor.distance() returns 1000.0 m until a valid reading arrives.

**Fix directive:** If the requirements fix a value (or the sensor's maximum range), replace NO_READING_DISTANCE in Sensor.java. It only has to exceed OBSTACLE_THRESHOLD.

**Java trace:**

- `src/main/java/sranger/sensor/Sensor.java` lines 10-13, `NO_READING_DISTANCE` — SR-DM5, SR-SF1

### 2. [design_choice] Negative or NaN IR readings are treated as 'no reading available'

**Raw:** SR-DM5/SR-SF1 say the reading is non-negative but not what to do with an invalid sample. Sensor.update() stores a reading >= 0.0 and otherwise falls back to the large default.

**Fix directive:** If invalid readings should count as an obstacle (fail-safe turn) instead, make update() store 0.0 for them.

**Java trace:**

- `src/main/java/sranger/sensor/Sensor.java` lines 20-26, `update` — SR-DM5, SR-SF1

### 3. [design_choice] obstacleThreshold = 0.5 is fractional and guards a transition

**Raw:** The model's constants interface rounds a fractional constant up to the next integer, so the threshold that guards Moving -> Turning becomes 1 in the model while the Java uses 0.5. The other constants (1.0, 2.0, 2.0) are integral. I kept the requirement's metres and value rather than rescaling units.

**Fix directive:** Treat verifier results that depend on the exact obstacle threshold with this rounding in mind. If exactness matters, express distances in an integral unit (e.g. centimetres, threshold 50) in both Sensor and SRangerConstants.

**Java trace:**

- `src/main/java/sranger/controller/SRangerConstants.java` lines 17-17, `OBSTACLE_THRESHOLD` — SR-DM2, SR-GP1
- `src/main/java/sranger/controller/SRangerController.java` lines 42-42, `obstacleDetected` — SR-GP1

### 4. [design_choice] Moving -> Turning needs both the obstacle event and the obstacleDetected guard

**Raw:** SR-Beh2 triggers the transition on the obstacle event; SR-GP1 says obstacleDetected is its guard. The branch tests both, so an obstacle event that disagrees with the current reading (distance > 0.5) is ignored.

**Fix directive:** If the event alone should suffice, drop '&& obstacleDetected' (SR-GP1 would then be unused).

**Java trace:**

- `src/main/java/sranger/controller/SRangerController.java` lines 50-53, `step` — SR-Beh2, SR-GP1

### 5. [design_choice] Priority in Turning: endTask, then timeout, then tick

**Raw:** No priority is given. endTask is the operator shutdown, so it comes first in both Moving and Turning. The timed return to Moving is ranked above the tick self-loop; otherwise a tick every cycle would keep the robot turning forever. The tick branch therefore conjoins !turnDurationElapsed. An obstacle event in Turning does nothing.

**Fix directive:** None unless the requirements set a different priority.

**Java trace:**

- `src/main/java/sranger/controller/SRangerController.java` lines 58-67, `step` — SR-Beh5, SR-Beh6, SR-Beh7

### 6. [ambiguous_requirement] Tick self-loops perform no action vs. entry actions of Moving/Turning

**Raw:** SR-Beh4/SR-Beh7 say the tick self-loops perform no action, while SR-FR1/SR-FR2 attach Move(...) and the clock reset to entering the mode. In RoboChart a self-transition re-enters its state and re-runs the entry action. The Java follows SR-Beh4/7: no Move is reissued and clockResetTime is not reset on tick. If the extracted model places Turning's actions in its entry action, a tick in the model would restart the turn timer.

**Fix directive:** Confirm that a tick must not restart the turn timer. If the model differs, the source is the self-transition re-entry semantics, not a missing Java action.

**Java trace:**

- `src/main/java/sranger/controller/SRangerController.java` lines 54-56, `step` — SR-Beh4, SR-FR1
- `src/main/java/sranger/controller/SRangerController.java` lines 65-67, `step` — SR-Beh7, SR-FR2

### 7. [design_choice] Power-up entry action of Moving issued in the constructor

**Raw:** SR-FR1/SR-Beh1: Moving is the initial mode and entering it issues Move(moveVel, 0). No transition leads into the initial mode, so the constructor issues the command. It is outside step(), so the extracted model's initial transition may not carry it.

**Fix directive:** None in Java; if the model lacks the initial Move, it comes from this construction-time action.

**Java trace:**

- `src/main/java/sranger/controller/SRangerController.java` lines 25-30, `SRangerController` — SR-FR1, SR-Beh1

### 8. [design_choice] Move carries two real components (lv, av)

**Raw:** SR-DM4 describes Move as carrying two real values. OutputEvent.Move is a record with two double components instead of one component of a wrapper datatype.

**Fix directive:** If the extraction needs a single-component event, wrap (lv, av) in a record datatype and give Move one component of that type.

**Java trace:**

- `src/main/java/sranger/event/OutputEvent.java` lines 16-16, `Move` — SR-DM4

### 9. [design_choice] Clock is a framework-advanced time source; field named timer

**Raw:** SR-Var1 needs 'the current time'. Clock.now() returns seconds advanced by the framework through advance(), never backwards. The controller field is named timer because 'clock' is a RoboChart reserved word. clockResetTime starts at 0.0 and is only read in Turning, which is entered only through the transition that sets it.

**Fix directive:** None.

**Java trace:**

- `src/main/java/sranger/clock/Clock.java` lines 8-26, `Clock` — SR-Var1, SR-GP2
- `src/main/java/sranger/controller/SRangerController.java` lines 17-20, `clockResetTime` — SR-Var1

### 10. [invented_default] Actuator's last command before any command is (0, 0)

**Raw:** SR-DM6 does not say what the actuator reports before the first command. Both velocities start at 0.0 (stationary); the controller's constructor overwrites them with Move(moveVel, 0) immediately.

**Fix directive:** None.

**Java trace:**

- `src/main/java/sranger/actuator/Actuator.java` lines 11-12, `Actuator` — SR-DM6

### 11. [design_choice] Final is terminal and ignores all events

**Raw:** SR-FR3 gives Final no outgoing transitions; the Final block of step() is empty, so tick, obstacle and endTask are ignored there. In RoboChart time still passes in a terminal state.

**Fix directive:** None.

**Java trace:**

- `src/main/java/sranger/controller/SRangerController.java` lines 69-71, `step` — SR-FR3

## Next step

Run the pipeline (compile, coverage, profile check, transformations, FDR4, Dafny, Isabelle, vacuity) on this Java.
