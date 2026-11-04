# post_codegen

**phase:** codegen  
**status:** uncertain

## Summary

SRanger controller written in 9 Java files under sranger/. All 23 requirements are implemented and traced. SR-DC1 holds by construction: SR-Beh2..SR-Beh7 are one step() branch each, and SR-Beh1 is the currentMode initialiser. Seven points the requirements did not settle are listed below. None blocks the pipeline.

## Issues

### 1. [design_choice] Moving -> Turning needs both the Obstacle event and the obstacleDetected guard

**raw:** SR-Beh2 makes the transition event-triggered (obstacle). SR-GP1 says obstacleDetected (distance() <= obstacleThreshold) is its guard. The branch is written as `event instanceof InputEvent.Obstacle && obstacleDetected`, so an Obstacle event that arrives while the reading is above the threshold is ignored. In the model this is trigger Obstacle with condition distance() <= obstacleThreshold.

**fix_directive:** If the obstacle event alone should trigger the turn, remove `&& obstacleDetected` from the Moving block's Obstacle branch. SR-GP1 would then have no use.

**java_trace:**

- `src/main/java/sranger/controller/SRangerController.java` lines 56-66, element `step`, requirements SR-Beh2, SR-GP1

### 2. [design_choice] Turning branch priority: endTask, then the timed return, then tick (tick guarded by !turnDurationElapsed)

**raw:** The framework delivers a tick on every cycle. If the tick self-loop (SR-Beh7) came before the autonomous return (SR-Beh5), the return could never fire in Java. The order is therefore EndTask (SR-Beh6, the shutdown override), then turnDurationElapsed (SR-Beh5), then Tick && !turnDurationElapsed (SR-Beh7). The `!turnDurationElapsed` conjunct follows the profile rule for a branch below a guard-only branch. Java checks the timed return once per step, so it can fire up to one cycle after the model's since(clockResetTime) >= turnDuration.

**fix_directive:** None expected. If FDR4 reports a timing-related deadlock in Turning, check this block first.

**java_trace:**

- `src/main/java/sranger/controller/SRangerController.java` lines 71-81, element `step`, requirements SR-Beh5, SR-Beh6, SR-Beh7, SR-GP2

### 3. [design_choice] The power-up Move(moveVel, 0) is issued in the constructor

**raw:** SR-FR1 requires Move(moveVel, 0) on entering Moving, and SR-Beh1 makes Moving the initial mode. Without a power-up command the Java robot would stand still until its first turn. The constructor therefore issues Move(moveVel, 0) once. This happens outside step(), so the extracted model may not show the initial entry command. Every later entry into Moving (SR-Beh5) issues it inside step().

**fix_directive:** If the model must carry the initial entry action, report it as a pipeline question. Do not move the command into step(), because it would then repeat on every cycle.

**java_trace:**

- `src/main/java/sranger/controller/SRangerController.java` lines 33-38, element `SRangerController`, requirements SR-FR1, SR-Beh1

### 4. [design_choice] Time comes from a framework-advanced Clock; the controller field is named timer

**raw:** SR-Var1 needs a current-time source. Clock (now(), advance(seconds)) starts at 0 and never decreases. It is advanced by the framework rather than read from the wall clock, so runs are deterministic. The controller field is named `timer` because `clock` is a RoboChart reserved word. clockResetTime = timer.now() should extract as a clock reset, and timer.now() - clockResetTime >= TURN_DURATION as since(clockResetTime) >= turnDuration.

**fix_directive:** To use wall-clock time instead, change only Clock.now() and Clock.advance().

**java_trace:**

- `src/main/java/sranger/sensor/Clock.java` lines 11-32, element `Clock`, requirements SR-Var1, SR-GP2
- `src/main/java/sranger/controller/SRangerController.java` lines 20-23, element `clockResetTime`, requirements SR-Var1

### 5. [invented_default] No-reading distance is 100.0 m; negative or NaN readings are ignored

**raw:** SR-DM5 asks for 'a large default value' when no reading is available but gives no number. Sensor reports NO_READING_DISTANCE = 100.0 m until the first valid reading. That is 200 times obstacleThreshold and beyond any IR range. updateDistance ignores negative or NaN measurements and keeps the previous value, so distance() is never negative (SR-DM5, SR-SF1).

**fix_directive:** If a different default is specified, change NO_READING_DISTANCE in Sensor.java. If invalid readings should revert to 'no reading', change updateDistance.

**java_trace:**

- `src/main/java/sranger/sensor/Sensor.java` lines 14-38, element `Sensor`, requirements SR-DM5, SR-SF1

### 6. [scope_question] obstacleThreshold = 0.5 is fractional, so the model's constants interface rounds it up to 1

**raw:** The exact value matters to the obstacleDetected guard (distance() <= obstacleThreshold). In the model's constants interface it becomes 1, so the model's guard is weaker than the Java guard. moveVel = 1.0, turnVel = 2.0 and turnDuration = 2.0 are whole numbers and keep their values.

**fix_directive:** None in Java: the requirement fixes 0.5. Read verifier results about obstacleDetected with the rounded value in mind.

**java_trace:**

- `src/main/java/sranger/controller/SRangerConstants.java` lines 17-17, element `OBSTACLE_THRESHOLD`, requirements SR-DM2, SR-GP1

### 7. [ambiguous_requirement] 'crosses below' (SR-DM3) versus 'at or below' (SR-GP1)

**raw:** SR-DM3 says the obstacle event is raised when the distance crosses below obstacleThreshold, which reads as strict. SR-GP1 defines obstacleDetected as distance <= obstacleThreshold, which includes the threshold. The guard follows SR-GP1 and uses <=.

**fix_directive:** If the threshold itself should not count as an obstacle, change <= to < in the obstacleDetected predicate.

**java_trace:**

- `src/main/java/sranger/controller/SRangerController.java` lines 56-56, element `step`, requirements SR-DM3, SR-GP1

## Next step

Run the pipeline (compile, coverage, profile check, transformations, FDR4/Dafny/Isabelle, vacuity) and act on its reports.
