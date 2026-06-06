# Post-Codegen Review — sranger, iter 2 (feedback fixes on iter-1 cold codegen)

**Status:** uncertain (5 design choices / invented defaults flagged)

**Summary:** Iter-2 applied three feedback-driven fixes to the iter-1 cold
codegen: 7 `@RoboChartType("real")` annotations added (preflight), the
autonomous Turning→Moving branch gated on Tick (dafny_verify), and TURN_VEL
scaled 2.0 → 1.0 to fit the `{0..1}` FDR4 type range (fdr4). All 24
requirement IDs remain traced in `result_codegen.json`.

## Issues

### 0a. invented_default — TURN_VEL scaled from the specified 2.0 to 1.0

SR-DM2 specifies turnVel default 2.0 rad/s. The experiment fixes all CSP
type ranges at `{0..1}`; the value 2 communicated on the `moveCall` channel
(`Move(0, turnVel)`) is rejected by FDR4 as an invalid channel value, making
every assertion inconclusive. Magnitudes are already distorted by the
abstraction (obstacleThreshold 0.5 is ceiled to 1), so the constant was
scaled to 1.0. Restore 2.0 if the verification budget is widened to {0..2}.

- `src/main/java/sranger/constants/SRangerConstants.java` — `TURN_VEL` (SR-DM2)

### 0b. design_choice — Autonomous Turning→Moving transition gated on Tick

SR-Beh5 says the transition is autonomous (no event required). The Dafny
contract for the TURNING transition method (`elapsed ==> mode == MOVING`)
is violated when a higher-priority EndTask arrives while elapsed is true.
Gating the branch on Tick makes the premise event-specific: the timed guard
is evaluated each control cycle, preserving observable timing semantics
while EndTask keeps priority.

- `src/main/java/sranger/controller/SRangerController.java` — `step` TURNING block (SR-Beh5, SR-GP2)

### 1. design_choice — Terminal mode named `HALTED` instead of `Final`

SR-DM1/SR-FR3 name the terminal mode "Final". CLAUDE.md ("Interpreting
Isabelle Results") states the theory-generated controller must NOT contain a
`Final` state — it leaves the `deadlock_free` proof unprovable and the
closing tactic hangs. SRanger is single-controller, so its controller IS the
theory-generated one. The mode is therefore named `HALTED`; semantics
(enter on endTask, issue Move(0,0), remain stopped) are unchanged.

- `src/main/java/sranger/mode/SRangerMode.java` — `SRangerMode.HALTED` (SR-DM1, SR-FR3)

### 2. design_choice — `HALTED` given a tick self-loop not present in the requirements

SR-Beh1..7 define no transition out of the terminal mode. An absorbing state
with no outgoing transitions deadlocks the CSP model and breaks the Isabelle
`deadlock_free` proof (every state needs a bare-precondition operation). A
Tick-triggered self-loop (CLAUDE.md pattern (b)) keeps HALTED live without
changing observable behaviour.

- `src/main/java/sranger/controller/SRangerController.java` — `step` HALTED block (SR-DC1, SR-FR3)

### 3. design_choice — `Move(lv, av)` encoded as a two-argument operation call

SR-DM4 describes a single output event `Move(lv : real, av : real)`.
RoboChart events carry one payload; the M2M's documented encoding for
multi-arg outputs is an `LOperations` Call. `Actuator.move(lv, av)` is
invoked directly in transition actions instead of constructing an
OutputEvent record.

- `src/main/java/sranger/actuator/Actuator.java` — `Actuator.move` (SR-DM4, SR-DM6)

## Next step

Run the pipeline (compile → … → vacuity) and iterate from `post_*` feedback.
