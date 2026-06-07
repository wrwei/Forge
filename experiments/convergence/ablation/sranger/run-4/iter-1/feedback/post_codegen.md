# Post-codegen review — sranger, ablation run-4, iter 1 (cold codegen)

**Status:** complete_with_notes

## Summary

Full cold codegen of the SRanger controller from
`forge.assets/case-studies/sranger/system/system_description.txt` +
`requirements/requirement_all.json`, following CLAUDE.md /
`java_codegen_rules.txt`. 8 files under
`java.generated.project/src/main/java/sranger/`
(annotation, mode, constants, event, sensor, actuator, time, controller).
All 21 requirements traced in `result_codegen.json`.

## Issues for user review

1. **design_choice — terminal mode named `Halted`, not `Final`.**
   SR-DM1/SR-FR3 name the terminal mode "Final", but CLAUDE.md states the
   theory-generated (primary) controller must NOT contain a `Final` state
   (unprovable deadlock-freedom residual). The mode is renamed `Halted`
   with identical semantics: absorbing, entered on endTask from Moving and
   Turning, entry action Move(0, 0).

2. **invented_default — tick self-loop on `Halted`.**
   SR-Beh1..7 define no transition out of the terminal mode. A
   tick-triggered self-loop (no action) was added so the absorbing mode has
   a bare-precondition event-triggered transition (CLAUDE.md pattern (b)),
   keeping every state covered without a τ-self-loop. This is one
   transition beyond the SR-Beh list; SR-DC1 (no duplicate source+trigger
   pairs) is still satisfied.

3. **design_choice — `Move(lv, av)` realised as a multi-arg operation
   call, not a two-payload output event.** SR-DM4's Move carries two
   reals; RoboChart typed events carry one payload. The controller calls
   `actuator.move(lv, av)` directly, which the ETL maps to an LOperations
   `Call` (per CLAUDE.md "Generated RoboChart structure"). No OutputEvent
   sealed interface is emitted.

4. **invented_default — clock units.** The Clock convention method is
   `nowMs()`, but `TURN_DURATION = 2.0` is in seconds per SR-DM2. The
   clock is treated as an abstract time source in the same unit as
   turnDuration; no ms↔s conversion is performed.

5. **design_choice — initial Moving entry action in the constructor.**
   SR-FR1/SR-Beh1 require Move(MOVE_VEL, 0) on power-up entry into
   Moving. This is issued in the controller constructor; the same command
   is issued on the Turning → Moving branch.

6. **design_choice — obstacle transition guarded.** SR-Beh2 (trigger:
   obstacle event) and SR-GP1 (guard: obstacleDetected) are combined:
   `event instanceof Obstacle && obstacleDetected`, so a stale obstacle
   event with distance above threshold does not cause a turn.

7. **invented_default — Sensor no-reading default = 1000.0 m** (private
   constant; spec says only "a large default value").

## Next step

Run the pipeline (`run_experiment_iteration.py`) and act on visible
compile/extraction feedback.
