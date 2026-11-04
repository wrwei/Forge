# post_codegen — SRanger

**status:** uncertain

**summary:** All 23 SRanger requirements are implemented in 9 Java files (one controller, 3 modes, 7 transitions) and traced in `result_codegen.json`. The code compiles under `javac --release 17` with `-Xlint:all` and no warnings. A scratch harness showed every required transition. Nine items are flagged: two invented defaults, four design choices, one ambiguity (whether the Moving → Turning transition is triggered by the obstacle event or guarded by the distance) and two scope notes (the 0.5 m threshold is rounded to an integer in the model; events with no required transition are ignored).

## Issues

### 1. ambiguous_requirement — Moving → Turning: obstacle event trigger and obstacleDetected guard combined
- **raw:** SR-Beh2 says the transition fires "when the obstacle event is received"; SR-GP1 says obstacleDetected (distance <= obstacleThreshold) "is used as the guard on the Moving → Turning transition". Neither says whether the other also applies.
- **fix_directive:** Implemented as trigger `obstacle` with guard `obstacleDetected`, which satisfies both requirements literally. An obstacle event while distance > obstacleThreshold is ignored in Moving. If the intended model is event-only or guard-only, drop the other conjunct in the SR-Beh2 branch.
- **java_trace:** `src/main/java/sranger/controller/SRangerController.java` 38-51, `step` — SR-Beh2, SR-GP1

### 2. scope_question — obstacleThreshold = 0.5 is rounded up to 1 in the model's constants interface
- **raw:** The pipeline rounds a fractional constant up to the next integer in the RoboChart constants interface. OBSTACLE_THRESHOLD = 0.5 is the only fractional constant (MOVE_VEL 1.0, TURN_VEL 2.0 and TURN_DURATION 2.0 are integral).
- **fix_directive:** The exact value matters to the obstacleDetected guard (distance <= 0.5 m in Java, distance <= 1 in the model). Deadlock and divergence verdicts do not depend on it, because distance() is an uninterpreted function, but any value-level property over the threshold should be read with this in mind. No Java change can keep 0.5 without changing the requirement's units.
- **java_trace:** `src/main/java/sranger/controller/SRangerConstants.java` 17, `OBSTACLE_THRESHOLD` — SR-DM2, SR-GP1; `SRangerController.java` 38, `obstacleDetected` — SR-GP1

### 3. invented_default — Sensor default distance when no reading is available: 1000.0 m
- **raw:** SR-DM5 asks for "a large default value" when no reading is available but gives no number. It also requires readings to be non-negative but does not say what to do with an invalid reading.
- **fix_directive:** Chose NO_READING_DISTANCE = 1000.0 m, far above obstacleThreshold, so missing data never triggers obstacleDetected. `Sensor.update` ignores negative or NaN readings and keeps the previous value. Replace either choice if the platform specifies one.
- **java_trace:** `src/main/java/sranger/sensor/Sensor.java` 12-21, `Sensor` — SR-DM5, SR-SF1

### 4. invented_default — Actuator's last command before any command is Move(0, 0)
- **raw:** SR-DM6 says the actuator stores the latest issued command but does not say what it reports before the first one.
- **fix_directive:** Initialised lastLinearVelocity and lastAngularVelocity to 0.0 (stopped). The controller's constructor issues Move(moveVel, 0) immediately, so the value is observable only for an actuator not yet attached to a controller.
- **java_trace:** `src/main/java/sranger/actuator/Actuator.java` 11-12, `Actuator` — SR-DM6

### 5. design_choice — Move is one output event record with two real components (lv, av)
- **raw:** SR-DM4 requires a single output event Move carrying two real values. The profile describes events as typed by their (single) component.
- **fix_directive:** Chose `OutputEvent.Move(double lv, double av)`, sent as `actuator.apply(new OutputEvent.Move(..))`, over a separate velocity datatype, so that no type the requirements do not name is introduced. If the extractor accepts only single-component events, the alternative is a domain record (lv, av) as Move's sole component.
- **java_trace:** `src/main/java/sranger/event/OutputEvent.java` 12, `Move` — SR-DM4; `src/main/java/sranger/actuator/Actuator.java` 15-20, `apply` — SR-DM6

### 6. design_choice — Initial forward command issued in the controller constructor
- **raw:** SR-FR1 requires Move(moveVel, 0) on entering Moving, and Moving is the initial mode (SR-Beh1). Only the branches of `step()` are transitions, and none of them covers power-up.
- **fix_directive:** The constructor issues Move(moveVel, 0), so the Java robot drives forward from power-up. The extracted model's initial transition probably carries no action, so the model shows this entry only on Turning → Moving.
- **java_trace:** `src/main/java/sranger/controller/SRangerController.java` 25-30, `SRangerController` — SR-FR1, SR-Beh1

### 7. design_choice — Turning: timed transition ranked above tick; tick self-loops perform no action
- **raw:** SR-Beh5 (autonomous Turning → Moving) and SR-Beh7 (tick self-loop) can both apply at a tick once turnDuration has elapsed. SR-Beh4 and SR-Beh7 say the tick self-loops perform no action.
- **fix_directive:** Order in Turning: endTask (override), then `turnDurationElapsed`, then `tick && !turnDurationElapsed` (the negation is required by the profile). With tick first, the Java robot would never leave Turning while only ticks arrive. The self-loops assign the mode only, re-issuing no Move and leaving the clockResetTime clock unreset, so ticks do not extend the turn.
- **java_trace:** `src/main/java/sranger/controller/SRangerController.java` 52-69, `step` — SR-Beh4, SR-Beh5, SR-Beh7, SR-GP2

### 8. design_choice — Clock is a framework-advanced time source; controller field named `timer`
- **raw:** SR-Var1 needs "the current time". The requirements name no time source. `clock` is a reserved word, so it cannot be a controller field name.
- **fix_directive:** Added class `Clock` (`now()`, `advance(seconds)`), held by the controller as field `timer`. `clockResetTime = timer.now()` on Moving → Turning, and the guard `timer.now() - clockResetTime >= TURN_DURATION`, are expected to extract as `#clockResetTime` and `since(clockResetTime) >= turnDuration`.
- **java_trace:** `src/main/java/sranger/controller/Clock.java` 1-23, `Clock` — SR-Var1, SR-GP2; `SRangerController.java` 39, 50, `clockResetTime` — SR-Var1

### 9. scope_question — Events with no required transition are ignored (obstacle in Turning; every event in Final)
- **raw:** The requirements give no transition for obstacle in Turning, or for any event in Final (SR-FR3: terminal).
- **fix_directive:** No transitions were added beyond SR-Beh1..7 (SR-DC1). The Java ignores these events, and the model refuses them. Final has no outgoing transition, which the profile allows (time still passes). If a verifier treats a terminal state as a deadlock, the cause is this requirement-mandated terminal mode.
- **java_trace:** `src/main/java/sranger/controller/SRangerController.java` 71-73, `step` — SR-FR3, SR-DC1

## next_step

Run the pipeline (compile, coverage, profile check, transformations, FDR4, Dafny, Isabelle, vacuity) and check the extracted model for: the two-component Move event, the clock reset/`since()` encoding of clockResetTime, and the rounding of obstacleThreshold.
