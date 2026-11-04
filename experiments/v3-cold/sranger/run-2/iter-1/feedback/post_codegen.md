# Codegen review (Layer 5)

**status:** `uncertain`

## Summary

All 23 SRanger requirements are implemented in 9 Java files (one controller with modes MOVING, TURNING, FINAL and seven transitions, one per SR-Beh requirement). Nothing is unimplemented. The issues below record two invented defaults, three design choices, two ambiguities between requirements, and two scope notes. Issue 7 records that obstacleThreshold = 0.5 is rounded up to 1 in the model.

## Issues

### 1. No-reading IR distance is 1000.0 m; invalid readings fall back to it

- **kind:** `invented_default`
- **raw:** SR-DM5 asks for 'a large default value' when no reading is available but gives no number. Sensor.distance() returns 1000.0 m before the first valid reading, and Sensor.update treats a negative or NaN reading as no reading, so distance() is never negative (SR-SF1).
- **fix_directive:** If the requirements fix a value or a different treatment of invalid readings, change NO_READING_DISTANCE and Sensor.update; the controller is unaffected.
- **java_trace:**
  - `src/main/java/sranger/sensor/Sensor.java` lines 10-26, `Sensor`, requirements SR-DM5, SR-SF1

### 2. Initial values of the actuator's stored command and of clockResetTime

- **kind:** `invented_default`
- **raw:** The Actuator stores (0.0, 0.0) before any command is issued (the controller's constructor overwrites it at once with Move(moveVel, 0)). clockResetTime starts at 0.0; it is read only in Turning, which is always entered through the Moving -> Turning branch that sets it.
- **fix_directive:** None needed unless the requirements fix these initial values.
- **java_trace:**
  - `src/main/java/sranger/actuator/Actuator.java` lines 12-13, `Actuator`, requirements SR-DM6
  - `src/main/java/sranger/controller/SRangerController.java` lines 18-18, `clockResetTime`, requirements SR-Var1

### 3. Entry action of the initial mode Moving is issued in the constructor

- **kind:** `design_choice`
- **raw:** SR-FR1 requires Move(moveVel, 0) on entering Moving, and SR-Beh1 makes Moving the initial mode. No step() branch leads into the initial mode, so the constructor issues the command. Whether the extracted model carries it as the action of the initial transition depends on the extractor.
- **fix_directive:** If the power-up command must appear in the model and does not, report it as a pipeline question; do not add a fourth mode (SR-DM1 fixes three).
- **java_trace:**
  - `src/main/java/sranger/controller/SRangerController.java` lines 31-36, `SRangerController`, requirements SR-FR1, SR-Beh1

### 4. Moving -> Turning requires both the obstacle event and the obstacleDetected guard

- **kind:** `ambiguous_requirement`
- **raw:** SR-Beh2 names the obstacle event as the trigger; SR-GP1 says obstacleDetected (distance <= obstacleThreshold) is the guard of the same transition. The branch tests both, so an obstacle event while distance > obstacleThreshold is ignored and the robot stays in Moving. Events no requirement handles (obstacle in Turning, every event in Final) are likewise ignored.
- **fix_directive:** If the obstacle event alone should trigger the turn, drop '&& obstacleDetected' from the branch and SR-GP1 loses its use as a guard.
- **java_trace:**
  - `src/main/java/sranger/controller/SRangerController.java` lines 56-56, `obstacleDetected`, requirements SR-GP1
  - `src/main/java/sranger/controller/SRangerController.java` lines 64-67, `step`, requirements SR-Beh2, SR-GP1

### 5. Branch priority: endTask first; in Turning the timed transition outranks tick

- **kind:** `design_choice`
- **raw:** endTask is the operator override, so it is the first branch of Moving and of Turning (SR-Beh3, SR-Beh6). Tick arrives every control cycle (SR-DM3), so the autonomous Turning -> Moving branch (SR-Beh5) sits above the tick self-loop (SR-Beh7); otherwise Turning would never be left. The tick branch conjoins !turnDurationElapsed, as the profile requires below a guard-only branch, so the two are exclusive in the model.
- **fix_directive:** None.
- **java_trace:**
  - `src/main/java/sranger/controller/SRangerController.java` lines 60-81, `step`, requirements SR-Beh3, SR-Beh5, SR-Beh6, SR-Beh7

### 6. Order of the clock reset and Move(0, turnVel) on Moving -> Turning

- **kind:** `ambiguous_requirement`
- **raw:** SR-FR2 lists the Move command before recording the time; SR-Beh2 lists the clock reset first. The code resets clockResetTime first (following the transition requirement), so the turn duration is measured from the start of the transition.
- **fix_directive:** Swap the two statements if the Move command must precede the reset.
- **java_trace:**
  - `src/main/java/sranger/controller/SRangerController.java` lines 65-67, `step`, requirements SR-FR2, SR-Beh2, SR-Var1

### 7. obstacleThreshold = 0.5 is kept in metres; the model's constants interface rounds it up to 1

- **kind:** `design_choice`
- **raw:** A fractional constant is rounded up to the next integer in the model's constants interface. OBSTACLE_THRESHOLD = 0.5 is the only fractional constant (moveVel 1.0, turnVel 2.0, turnDuration 2.0 are integral), and its exact value matters to the guard obstacleDetected (distance <= obstacleThreshold, SR-GP1): in the model the guard compares against 1, not 0.5. The Java keeps the requirement's value and unit, because SR-DM2 makes all four constants real-valued in metres/seconds; rescaling to integer centimetres would contradict it.
- **fix_directive:** No Java change unless a verifier's verdict depends on the exact threshold; if it does, it is a limit of the constants translation rather than of the Java.
- **java_trace:**
  - `src/main/java/sranger/controller/SRangerConstants.java` lines 17-17, `OBSTACLE_THRESHOLD`, requirements SR-DM2
  - `src/main/java/sranger/controller/SRangerController.java` lines 56-56, `obstacleDetected`, requirements SR-GP1

### 8. Tick self-loops reassign the mode and do nothing else

- **kind:** `scope_question`
- **raw:** SR-Beh4 and SR-Beh7 are self-transitions with no action. Each is its own branch, reassigning the mode only, so that SR-DC1 (one transition per behavioural requirement) holds. In RoboChart a self-transition exits and re-enters its state; if the model places the Move commands in state entry actions, a re-entry would re-issue Move, which these requirements say does not happen.
- **fix_directive:** None in the Java; the branches carry no action.
- **java_trace:**
  - `src/main/java/sranger/controller/SRangerController.java` lines 68-69, `step`, requirements SR-Beh4, SR-DC1
  - `src/main/java/sranger/controller/SRangerController.java` lines 79-80, `step`, requirements SR-Beh7, SR-DC1

### 9. Time source and sensor input are not named by the requirements

- **kind:** `scope_question`
- **raw:** SR-Var1 and SR-GP2 need the current time but name no time source. A Clock class (now(), advance(seconds)) provides it; the controller field is called timer because 'clock' is a reserved word, and Clock sits in the sensor package as an environment input. Sensor.update(reading) is the input side of SR-DM5. Neither adds behaviour the requirements do not describe.
- **fix_directive:** None.
- **java_trace:**
  - `src/main/java/sranger/sensor/Clock.java` lines 10-35, `Clock`, requirements SR-Var1, SR-GP2
  - `src/main/java/sranger/controller/SRangerController.java` lines 21-21, `timer`, requirements SR-Var1
  - `src/main/java/sranger/sensor/Sensor.java` lines 20-26, `update`, requirements SR-DM5

## Next step

Run the pipeline (compile, coverage, profile check, transformations, FDR4, Dafny, Isabelle, vacuity) on java.generated.project.
