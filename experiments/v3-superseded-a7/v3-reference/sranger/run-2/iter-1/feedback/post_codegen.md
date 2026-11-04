# post_codegen

- **status**: uncertain

## Summary

All 23 SRanger requirements are implemented in 9 Java files under java.generated.project/src/main/java/sranger/ and traced in result_codegen.json. One controller (SRangerController) with three modes (MOVING initial, TURNING, FINAL), six transitions (SR-Beh2..SR-Beh7) plus the initial mode (SR-Beh1), two named guard predicates (obstacleDetected, turnDurationElapsed) and one clock-reset variable (clockResetTime). Nothing is unimplemented; the issues below are defaults the requirements do not give, design choices, and two ambiguities.

## Issues

### 1. [invented_default] Sensor: value of distance() when no reading is available

- **kind**: invented_default
- **raw**: SR-DM5 / SR-SF1 say distance() returns 'a large default value' when no reading is available, without giving the value.
- **fix_directive**: Chosen 1000.0 m (Sensor.NO_READING_DISTANCE), far above obstacleThreshold = 0.5 m, so missing data never satisfies obstacleDetected. A negative reading is not a valid (non-negative) distance and is also treated as no reading. Change the value if the platform specifies one.
- **java_trace**:
  - `src/main/java/sranger/sensor/Sensor.java` lines 11-27, element `Sensor.update`, requirement_ids: SR-DM5, SR-SF1

### 2. [invented_default] Initial value of clockResetTime

- **kind**: invented_default
- **raw**: SR-Var1 defines clockResetTime as the time Turning was most recently entered; before the first entry to Turning it has no specified value.
- **fix_directive**: Initialised to 0.0. It is only read in TURNING (turnDurationElapsed), which is reached only after the Moving -> Turning transition has reset it, so the initial value is never observed.
- **java_trace**:
  - `src/main/java/sranger/controller/SRangerController.java` lines 17-17, element `clockResetTime`, requirement_ids: SR-Var1

### 3. [design_choice] obstacleThreshold = 0.5 is fractional and is rounded up to 1 in the model's constants interface

- **kind**: design_choice
- **raw**: java_codegen_rules.txt section 2: the model's constants interface rounds a fractional constant up to the next integer. OBSTACLE_THRESHOLD = 0.5 is the only fractional constant and the exact value matters to the guard obstacleDetected (distance() <= OBSTACLE_THRESHOLD).
- **fix_directive**: Kept the requirement's value and unit (0.5 m) rather than rescaling (e.g. to centimetres) to obtain an integer, because SR-DM2 fixes the name, value and unit. In the extracted model the guard reads distance() <= 1, so a verified property involving the threshold holds for a 1 m threshold, not 0.5 m. The Java behaviour uses 0.5. MOVE_VEL, TURN_VEL and TURN_DURATION are whole numbers and are not affected.
- **java_trace**:
  - `src/main/java/sranger/controller/SRangerConstants.java` lines 17-17, element `OBSTACLE_THRESHOLD`, requirement_ids: SR-DM2, SR-GP1
  - `src/main/java/sranger/controller/SRangerController.java` lines 42-42, element `obstacleDetected`, requirement_ids: SR-GP1

### 4. [design_choice] Initial Move(moveVel, 0) is issued by the constructor

- **kind**: design_choice
- **raw**: SR-FR1: on entering Moving the controller issues Move(moveVel, 0); SR-Beh1: on power-up the controller begins in Moving. step() is only called per cycle, so power-up entry is not a branch of step().
- **fix_directive**: The constructor issues Move(MOVE_VEL, 0.0) once, as Moving's entry action on power-up; currentMode is initialised to MOVING. Every branch of step() that enters Moving from another mode issues the same command inline.
- **java_trace**:
  - `src/main/java/sranger/controller/SRangerController.java` lines 27-32, element `SRangerController`, requirement_ids: SR-FR1, SR-Beh1

### 5. [design_choice] Turning: autonomous return to Moving takes priority over the tick self-loop

- **kind**: design_choice
- **raw**: SR-Beh5 (autonomous, turnDurationElapsed) and SR-Beh7 (tick, stay in Turning) are both enabled on a tick once the turn duration has elapsed; the requirements give no priority between them.
- **fix_directive**: Order in TURNING: endTask (SR-Beh6), then turnDurationElapsed (SR-Beh5), then tick && !turnDurationElapsed (SR-Beh7). If tick came first, a controller driven only by ticks would never leave Turning. The tick branch conjoins !turnDurationElapsed so the two branches stay disjoint in the model, where transitions are alternatives. endTask is first in both Moving and Turning as the shutdown override.
- **java_trace**:
  - `src/main/java/sranger/controller/SRangerController.java` lines 58-67, element `step`, requirement_ids: SR-Beh5, SR-Beh6, SR-Beh7, SR-DC1

### 6. [design_choice] Time source is an externally advanced Clock

- **kind**: design_choice
- **raw**: SR-Var1 / SR-FR2 require 'the current time' but name no time source.
- **fix_directive**: Clock (sranger.controller.Clock) holds the time in seconds, advanced by the system framework each control cycle (advance(seconds)) and read with now(). It is held in the controller field 'timer' ('clock' is a reserved word). clockResetTime = timer.now() resets it; timer.now() - clockResetTime >= TURN_DURATION is the elapsed-time test.
- **java_trace**:
  - `src/main/java/sranger/controller/Clock.java` lines 9-29, element `Clock`, requirement_ids: SR-Var1, SR-GP2
  - `src/main/java/sranger/controller/SRangerController.java` lines 43-43, element `turnDurationElapsed`, requirement_ids: SR-GP2

### 7. [ambiguous_requirement] Moving -> Turning: event trigger versus distance guard

- **kind**: ambiguous_requirement
- **raw**: SR-Beh2: transition 'when the obstacle event is received'. SR-GP1: obstacleDetected (distance <= obstacleThreshold) 'is used as the guard on the Moving -> Turning transition'. SR-DM3: obstacle is emitted when the distance 'crosses below' (strictly <) the threshold.
- **fix_directive**: Implemented as trigger obstacle AND guard obstacleDetected (<=, as SR-GP1 states). An obstacle event that arrives while the reading is above the threshold (or no reading is available) is ignored and the controller stays in Moving.
- **java_trace**:
  - `src/main/java/sranger/controller/SRangerController.java` lines 50-53, element `step`, requirement_ids: SR-Beh2, SR-GP1, SR-DM3

### 8. [ambiguous_requirement] Tick self-loops perform no action

- **kind**: ambiguous_requirement
- **raw**: SR-Beh4 / SR-Beh7: Moving -> Moving and Turning -> Turning on tick, 'The controller performs no action on this transition.' The profile says every branch performs the target mode's entry actions, and in RoboChart a self-transition exits and re-enters its state.
- **fix_directive**: Followed the requirements: the tick branches only re-assign the mode, with no Move command and no clock reset. Re-issuing the entry actions would re-send Move on every tick and, in Turning, reset clockResetTime every cycle so Turning would never end.
- **java_trace**:
  - `src/main/java/sranger/controller/SRangerController.java` lines 54-55, element `step`, requirement_ids: SR-Beh4
  - `src/main/java/sranger/controller/SRangerController.java` lines 65-66, element `step`, requirement_ids: SR-Beh7

### 9. [scope_question] Who produces the obstacle event

- **kind**: scope_question
- **raw**: SR-DM3: obstacle is emitted by the IR sensor framework when the reading crosses below the threshold.
- **fix_directive**: The event source is outside the controller and is not implemented: the Sensor class only exposes distance(); the framework is expected to deliver InputEvent.Obstacle, InputEvent.Tick and InputEvent.EndTask to step().
- **java_trace**:
  - `src/main/java/sranger/sensor/Sensor.java` lines 8-37, element `Sensor`, requirement_ids: SR-DM3, SR-DM5

## Next step

Run the pipeline (compile, coverage, profile check, transformations, FDR4, Dafny, Isabelle, vacuity) and fix the Java from the phase reports.
