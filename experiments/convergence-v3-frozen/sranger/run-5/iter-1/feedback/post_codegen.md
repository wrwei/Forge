# post_codegen — SRanger, iteration 1

- **status**: uncertain
- **summary**: All 23 SRanger requirements implemented across 8 Java files
  (mode enum, input/output events, constants, IR sensor, actuator, clock,
  controller, `@RoboChartType` annotation). `./gradlew build` is clean.
  Four judgement calls are flagged below for human review.

## Issues

### 1. design_choice — Move(lv, av) is emitted as a 2-arg actuator call, not an event constructor
- **raw**: SR-DM4 specifies a single output event `Move(lv : real, av : real)`.
  RoboChart events carry at most one payload value, and the M2M's
  `extractOutputEventInfo` keeps only `ctorArgs.at(0)`, so
  `actuator.apply(new OutputEvent.Move(lv, av))` would silently drop `av`.
  The controller therefore calls `actuator.move(lv, av)`, which the M2M
  turns into an `LOperations` operation call carrying both values.
  `OutputEvent.Move` is still declared and is the Actuator's stored
  last-command type, so the requirement's type exists in the source.
- **fix_directive**: If the extracted model must show `Move` as a RoboChart
  event rather than an operation, the transformation chain (not the Java)
  has to gain two-payload event support.
- **java_trace**: `src/main/java/sranger/actuator/Actuator.java` — `move`
  — requirement_ids: [SR-DM4, SR-DM6]

### 2. design_choice — Turning: the autonomous timeout out-prioritises endTask
- **raw**: SR-Beh5 (Turning → Moving, autonomous) and SR-Beh6 (Turning →
  Final on endTask) give no relative priority. Placing the event-triggered
  branch first makes the later triggerless branch enabled simultaneously in
  the extracted model (preflight `rule8_event_branch_precedes_triggerless`).
  The triggerless branch is therefore first, and the two event branches
  carry `&& !turnDurationElapsed`. Consequence: an endTask arriving in the
  same step in which the turn duration has already elapsed is served one
  step later, from Moving. The three Turning guards then form a total
  cover (`turnDurationElapsed` ∨ `¬turnDurationElapsed`), which is what
  keeps the mode deadlock-free without a bare self-loop.
- **fix_directive**: If endTask must pre-empt the timeout, reorder and accept
  the nondeterminism warning.
- **java_trace**: `src/main/java/sranger/controller/SRangerController.java`
  — `step` — requirement_ids: [SR-Beh5, SR-Beh6, SR-Beh7]

### 3. ambiguous_requirement — obstacle event vs. obstacleDetected guard
- **raw**: SR-Beh2 says Moving → Turning fires "when the obstacle event is
  received" (no guard); SR-GP1 says `obstacleDetected` "is used as the guard
  on the Moving → Turning transition". Implemented as trigger AND guard
  (`event instanceof InputEvent.Obstacle && obstacleDetected`), which
  satisfies both readings. The guard is redundant if the framework only
  raises `obstacle` when `distance <= obstacleThreshold`.
- **fix_directive**: Confirm whether the guard is intended to be redundant
  belt-and-braces or whether SR-Beh2 is the authoritative form.
- **java_trace**: `src/main/java/sranger/controller/SRangerController.java`
  — `step` — requirement_ids: [SR-Beh2, SR-GP1]

### 4. invented_default — sensor no-reading distance and clock unit
- **raw**: SR-DM5 requires "a large default value" without naming one;
  `IrSensor.NO_READING_DISTANCE = 1000.0` m was chosen. SR-Var1 states
  clockResetTime is in seconds but no clock API is specified; `Clock`
  exposes `nowSeconds()` so that `TURN_DURATION = 2.0` is compared in the
  same unit. Note that CSP-gen ceils fractional constants, so
  `obstacleThreshold = 0.5` reaches the CSP model as 1 — a disclosed
  instrument limitation (vacuity signal K1, advisory), not a source defect.
- **fix_directive**: Replace 1000.0 with the IR sensor's documented
  out-of-range reading if one exists.
- **java_trace**: `src/main/java/sranger/sensor/IrSensor.java` — `distance`
  — requirement_ids: [SR-DM5, SR-SF1, SR-DM2, SR-Var1]
