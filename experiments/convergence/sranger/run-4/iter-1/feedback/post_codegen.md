# Post-Codegen Review — sranger, iter 1 (cold codegen)

**Status:** uncertain (3 design choices flagged)

**Summary:** Full cold codegen of the `sranger` package (8 files) from
`system_description.txt` + `requirement_all.json`, following CLAUDE.md and
the codegen rule prompts. All 24 requirement IDs are traced in
`result_codegen.json`.

## Issues

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
