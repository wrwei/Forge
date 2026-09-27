# Phase 2 — Interactive Code Generation

**Status:** uncertain
**Summary:** All 23 SRanger requirements implemented across 9 Java files; 4 items need human review (terminal-mode encoding, output-event arity, endTask priority, sensor default).

## Requirement → Java coverage

All 23 ids in `requirement_all.json` have at least one entry in
`java.generated.project/result_codegen.json`, and every one of the 9 Java
files is named by at least one entry.

| Layer | Requirements | Java |
|---|---|---|
| Architecture | SR-ARCH1, SR-ARCH2 | `SRangerController`, `RoboChartType` |
| Data types | SR-DM1, SR-DM5 | `SRangerMode`, `Sensor` |
| Constants | SR-DM2 | `SRangerConstants` (MOVE_VEL, TURN_VEL, OBSTACLE_THRESHOLD, TURN_DURATION) |
| Events | SR-DM3, SR-DM4 | `InputEvent.{Obstacle,Tick,EndTask}`, `OutputEvent.Move` |
| Actuator | SR-DM6 | `Actuator.apply` + last-command accessors |
| Sensor function | SR-SF1 | `Sensor.distance()` |
| Variable | SR-Var1 | `SRangerController.clockResetTime`, `Clock.nowSeconds()` |
| States | SR-FR1, SR-FR2, SR-FR3 | `SRangerMode.{MOVING,TURNING,FINAL}` + `step()` branches |
| Guards | SR-GP1, SR-GP2 | `obstacleDetected`, `turnDurationElapsed` in `step()` |
| Transitions | SR-Beh1 … SR-Beh7 | `currentMode` initialiser + the 6 `step()` branches |
| Constraint | SR-DC1 | `SRangerController` |

## Issues

### 1. design_choice — FINAL has no outgoing transition (no tick self-loop added)

SR-DM1/SR-FR3 specify Final as terminal; SR-Beh1..SR-Beh7 enumerate exactly
seven transitions and SR-DC1 forbids duplicates. No Final → Final transition
is specified, so none was written.

`java_codegen_rules.txt` ("EVERY MODE MUST HAVE AN OUTGOING TRANSITION") asks
for a tick-triggered self-loop on sink modes. That measurement predates
the frozen instrument's fix, which emits `until (st = FINAL)` for
source-of-no-transition states and so exempts them from the `deadlock_free`
obligation; `vacuity.py` likewise classifies `dead_state_no_offer` as
ADVISORY for "a specified terminal mode". The requirement text was therefore
followed.

**Fix directive.** If Isabelle 6c reports an unprovable or hanging
`<Stm>_deadlock_free`, or FDR4 reports a deadlock whose witness trace ends in
FINAL, add a tick-triggered self-loop to the FINAL block of
`SRangerController.step()`:
`if (event instanceof InputEvent.Tick) { this.currentMode = SRangerMode.FINAL; }`.
Never a bare `else { ... }` fallback — that becomes a τ-self-loop and diverges.

Requirements: SR-DM1, SR-FR3, SR-Beh3, SR-Beh6, SR-DC1.

### 2. design_choice — endTask given highest priority in Turning

The requirements do not order SR-Beh5 (autonomous Turning → Moving on
`turnDurationElapsed`) against SR-Beh6 (Turning → Final on `endTask`).
`endTask` is an operator-level shutdown, so it is placed first, per CLAUDE.md's
rule that operator overrides are duplicated as the first inner branch of each
mode block.

Consequence: preflight `rule8_event_branch_precedes_triggerless` will fire as a
WARNING on the `turnDurationElapsed` branch. This is the same shape as the
documented LRE CAM block. The alternative ordering (triggerless first) silences
the warning but drops an `endTask` arriving in the same cycle the turn
completes, leaving the robot running after a commanded shutdown.

**Fix directive.** Treat rule8 here as accepted. If the model must be made
overlap-free, move `turnDurationElapsed` to the first inner branch and conjoin
`&& !turnDurationElapsed` onto both event branches — but confirm first that
losing `endTask` on the turn-completion cycle is acceptable.

Requirements: SR-Beh5, SR-Beh6, SR-Beh7.

### 3. scope_question — Move carries two reals but the extractor keeps only the first

SR-DM4 requires a single output event `Move(lv, av)` with two real values and
forbids introducing any other output event. `java2robochart.etl`'s
`extractOutputEventInfo` records only `ctorArgs.at(0)`, so the extracted
RoboChart event `move` is typed `real` and carries the linear velocity only;
the angular velocity is dropped from the formal model. The Java is faithful to
SR-DM4; the loss is in the M2M, which this experiment may not modify.

**Fix directive.** No Java change. If the angular component must reach the
formal model that is an instrument change and must be reported to the operator
— not worked around by splitting `Move` into two events, which SR-DM4 forbids.

Requirements: SR-DM4.

### 4. invented_default — Sensor no-reading default fixed at 1000.0 m

SR-DM5 requires `distance()` to return "a large default value" when no reading
is available but names no magnitude. 1000.0 m was chosen: three orders of
magnitude above `obstacleThreshold` (0.5 m), so the obstacle-detection
condition cannot be satisfied by missing data.

**Fix directive.** Adjust `Sensor.NO_READING` if the domain specifies a sensor
range ceiling. Any value strictly greater than
`SRangerConstants.OBSTACLE_THRESHOLD` preserves the required behaviour.

Requirements: SR-DM5, SR-SF1.

### 5. ambiguous_requirement — SR-FR1 entry action vs SR-Beh4 "no action"

SR-FR1 says `Move(moveVel, 0)` is issued "on entering Moving". SR-Beh4 says the
Moving → Moving tick transition performs no action. Read literally the two
conflict for a self-transition, which in RoboChart re-enters the state. SR-Beh4
was taken as the more specific statement: the tick self-loop assigns the mode
and issues nothing. `Move(moveVel, 0)` is issued in the constructor (power-up
entry, SR-Beh1) and on the Turning → Moving transition (SR-Beh5).

**Fix directive.** No change unless the reviewer intends the tick self-loop to
re-issue the forward command; if so, add the `actuator.apply(...)` call to the
Moving tick branch and note the deviation from SR-Beh4.

Requirements: SR-FR1, SR-Beh1, SR-Beh4, SR-Beh5.

## Files to review

- `SRangerController.java` — SR-ARCH1, SR-ARCH2, SR-Var1, SR-GP1, SR-GP2, SR-Beh1…SR-Beh7, SR-DC1
- `OutputEvent.java` — SR-DM4
- `Sensor.java` — SR-DM5, SR-SF1

## Next step

Run the pipeline from Phase 2a (compile) through 6d. Expect preflight rule8 as
an accepted warning on the Turning block, and the M2M deadlock-lint to name
FINAL. Escalate only a genuine Isabelle/FDR4 failure.
