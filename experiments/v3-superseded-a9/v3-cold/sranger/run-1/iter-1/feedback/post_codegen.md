# Post-codegen review

**Status:** uncertain

## Summary

SRanger controller generated in 9 files under sranger/: one controller (SRangerController) with three modes (MOVING initial, TURNING, FINAL), two named predicates (obstacleDetected, turnDurationElapsed) and six branch transitions plus the initial mode (SR-Beh1..7). All 23 requirements are traced in result_codegen.json. Nothing is unimplemented; the issues below are invented defaults and design choices the requirements did not settle.

## Issues (11)

### 1. [invented_default] Sensor.distance() returns 100.0 m when no reading is available

**Raw:** SR-DM5 asks for 'a large default value' when no IR reading is available but gives no number. NO_READING_DISTANCE = 100.0 m is used (far above OBSTACLE_THRESHOLD = 0.5 m). A negative or NaN measurement passed to update() is treated as 'no reading', so distance() is always non-negative as SR-DM5/SR-SF1 require.

**Fix directive:** Confirm 100.0 m as the no-reading default, or give the intended value; it is private to Sensor and not part of the controller's constants.

**Java trace:**

- `src/main/java/sranger/sensor/Sensor.java` lines 10-42, `Sensor` — SR-DM5, SR-SF1

### 2. [invented_default] Actuator reports Move(0, 0) before any command is issued

**Raw:** SR-DM6 says the actuator stores the latest command but not what it holds before the first one. lastCommand starts as Move(0.0, 0.0) (stopped). The controller's constructor issues Move(moveVel, 0) immediately, so the default is only visible on an actuator not yet attached to a controller.

**Fix directive:** None needed unless a different initial actuator state is intended.

**Java trace:**

- `src/main/java/sranger/actuator/Actuator.java` lines 11-11, `lastCommand` — SR-DM6

### 3. [design_choice] Move output event carries two real components (lv, av)

**Raw:** The codegen rules describe an event with a value as a record with one component. SR-DM4 specifies a single Move event carrying two reals, so OutputEvent.Move has two @RoboChartType("real") components, lv and av, rather than being split into two events or wrapped in a separate record type.

**Fix directive:** If the model loses the second component, wrap (lv, av) in a datatype record and give Move a single component of that type.

**Java trace:**

- `src/main/java/sranger/event/OutputEvent.java` lines 16-16, `Move` — SR-DM4

### 4. [design_choice] Moving -> Turning needs both the Obstacle event and obstacleDetected

**Raw:** SR-Beh2 triggers the transition on the obstacle event; SR-GP1 says obstacleDetected (distance <= obstacleThreshold) is the guard on the same transition. Both are used: event instanceof Obstacle && obstacleDetected. An Obstacle event arriving while the sensor reads above the threshold is ignored. GP1 says 'at or below' (<=) while SR-DM3 says the event fires when the reading 'crosses below'; <= from GP1 is used.

**Fix directive:** Drop '&& obstacleDetected' if the event alone should trigger the turn.

**Java trace:**

- `src/main/java/sranger/controller/SRangerController.java` lines 48-48, `obstacleDetected` — SR-GP1
- `src/main/java/sranger/controller/SRangerController.java` lines 56-59, `step` — SR-Beh2, SR-GP1

### 5. [design_choice] Turning priority: endTask, then the timed return, then tick (tick guarded by !turnDurationElapsed)

**Raw:** The requirements give no priority among Turning's three transitions. endTask (operator shutdown) is first. The autonomous turnDurationElapsed transition comes before tick, because tick arrives every cycle and would otherwise keep the controller in Turning forever. The tick self-loop conjoins !turnDurationElapsed, as the profile requires for a branch below a guard-only branch, so the two are exclusive in the model too.

**Fix directive:** None needed unless a different priority is intended.

**Java trace:**

- `src/main/java/sranger/controller/SRangerController.java` lines 65-76, `step` — SR-Beh5, SR-Beh6, SR-Beh7, SR-GP2, SR-DC1

### 6. [design_choice] Tick self-transitions perform no action (no Move, no clock reset)

**Raw:** SR-Beh4 and SR-Beh7 say the tick self-transitions perform no action, while the profile says every branch performs the target mode's entry actions. The requirement is followed: the branches only reassign the same mode. In particular the Turning self-loop does not reset clockResetTime, otherwise the turn would never time out. If the model places Turning's actions in the state's entry action, a RoboChart self-transition would re-run them.

**Fix directive:** None needed in the Java; if verification shows the turn clock reset on tick, the actions are being lifted into Turning's entry.

**Java trace:**

- `src/main/java/sranger/controller/SRangerController.java` lines 60-63, `step` — SR-Beh4
- `src/main/java/sranger/controller/SRangerController.java` lines 73-76, `step` — SR-Beh7, SR-FR2

### 7. [design_choice] Moving's power-up entry command is issued in the constructor

**Raw:** SR-FR1 and SR-Beh1: the controller starts in Moving and entering Moving issues Move(moveVel, 0). currentMode is initialised to MOVING and the constructor issues Move(moveVel, 0) once, so the robot drives forward from power-up. The extracted model's initial transition may not carry this action.

**Fix directive:** None needed unless the initial command should be left to the first cycle.

**Java trace:**

- `src/main/java/sranger/controller/SRangerController.java` lines 15-15, `currentMode` — SR-Beh1, SR-DM1
- `src/main/java/sranger/controller/SRangerController.java` lines 29-34, `SRangerController` — SR-FR1, SR-Beh1

### 8. [design_choice] On entering Turning the clock is reset before Move(0, turnVel) is issued

**Raw:** SR-Beh2 lists 'set clockResetTime to the current time and issue Move(0, turnVel)'; SR-FR2 lists them in the other order. The SR-Beh2 order is used: clockResetTime = timer.now(); then Move(0, turnVel).

**Fix directive:** Swap the two statements if the turn time should be measured from the issue of the Move command.

**Java trace:**

- `src/main/java/sranger/controller/SRangerController.java` lines 57-59, `step` — SR-FR2, SR-Var1, SR-Beh2

### 9. [design_choice] Clock is a framework-advanced time source in seconds, held in field 'timer'

**Raw:** SR-Var1 needs 'the current time'. Clock keeps a time in seconds that the framework advances (advance(seconds)); now() returns it. The controller field holding it is named 'timer' because 'clock' is a RoboChart reserved word. clockResetTime is assigned from timer.now() and turnDurationElapsed is timer.now() - clockResetTime >= TURN_DURATION.

**Fix directive:** Replace the body of Clock.now() with a wall-clock source if one is wanted; the controller does not change.

**Java trace:**

- `src/main/java/sranger/controller/Clock.java` lines 8-28, `Clock` — SR-Var1, SR-ARCH2
- `src/main/java/sranger/controller/SRangerController.java` lines 49-49, `turnDurationElapsed` — SR-GP2, SR-Var1

### 10. [ambiguous_requirement] obstacleThreshold = 0.5 is fractional; the model rounds it up to 1

**Raw:** SR-DM2 fixes obstacleThreshold = 0.5 m. The model's constants interface rounds a fractional constant up to the next integer, so the guard obstacleDetected becomes distance() <= 1 in the model rather than <= 0.5. The exact value matters to this guard. moveVel, turnVel and turnDuration are whole numbers and are unaffected.

**Fix directive:** Read the model's obstacleDetected guard with threshold 1; the Java keeps 0.5 as SR-DM2 requires.

**Java trace:**

- `src/main/java/sranger/controller/SRangerConstants.java` lines 17-17, `OBSTACLE_THRESHOLD` — SR-DM2, SR-GP1

### 11. [scope_question] Events with no transition in the current mode are ignored; Final has no transitions

**Raw:** Obstacle in Turning and every event in Final have no transition in the requirements, so step() ignores them. Final is terminal (SR-FR3) and its block is empty; no transition was added there, because SR-DC1 maps each of SR-Beh1..7 to exactly one transition.

**Fix directive:** None needed unless Final should accept tick as a self-loop.

**Java trace:**

- `src/main/java/sranger/controller/SRangerController.java` lines 78-80, `step` — SR-FR3, SR-DC1

## Next step

Run the pipeline (compile, coverage, profile check, transformations, verifiers) and review the issues above against its reports.
