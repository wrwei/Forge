# Post-Codegen Review — sranger, iter 1 (cold codegen)

**Status:** complete

**Summary:** Full cold codegen of the SRanger controller from
`system_description.txt` + `requirement_all.json` (24 requirements,
SR-ARCH1..SR-DC1). 8 Java files under `sranger/` (annotation, mode,
constants, event, sensor, actuator, controller). All requirements
traced in `result_codegen.json`.

## Issues for user review

1. **design_choice — terminal mode renamed `Final` → `Halted`.**
   SR-DM1/SR-FR3 name the terminal mode "Final", but CLAUDE.md
   ("Interpreting Isabelle Results") requires the theory-generated
   (primary) controller to have no RoboChart `Final` state — the
   deadlock-freedom proof is unprovable otherwise. SRanger is
   single-controller, so its controller is the primary. `Halted` is an
   ordinary absorbing state: entered on endTask from Moving/Turning,
   issues Move(0,0) on entry, and self-loops on tick with no action.

2. **invented_default — Halted tick self-loop.** Not among SR-Beh1..7.
   Added so the terminal mode has a bare-precondition operation
   (CLAUDE.md pattern (b)) for the Isabelle deadlock-freedom proof.
   Does not duplicate any source-mode/trigger pair, so SR-DC1 holds.

3. **design_choice — Move(lv, av) as a 2-arg actuator call.**
   SR-DM4's output event is realised as `Actuator.move(lv, av)`; the
   M2M turns multi-arg actuator calls into RoboChart operation Calls
   (LOperations), which preserves both payload values. A single-payload
   `OutputEvent.Move` record would have dropped `av` in extraction.

4. **design_choice — initial Moving entry action in the constructor.**
   SR-FR1's "drive forward on entering Moving" fires at power-up from
   the constructor at the Java level; the model-level Initial → Moving
   transition carries no action (constructors are not extracted).

5. **design_choice — obstacle transition guarded by `obstacleDetected`.**
   SR-Beh2 alone would make the transition trigger-only, but SR-GP1
   explicitly requires the `obstacleDetected` predicate
   (distance <= obstacleThreshold) as the guard on Moving → Turning,
   so the transition has both the Obstacle trigger and the condition.

6. **invented_default — Sensor no-reading default = 1000.0 m.** The
   spec says "a large default value"; 1000.0 chosen (well above
   obstacleThreshold 0.5).

7. **design_choice — autonomous Turning → Moving kept autonomous.**
   SR-Beh5 says "autonomous, no event required", so the
   `turnDurationElapsed` branch has no event check. It is placed below
   the endTask branch (priority) and above the tick self-loop.

**next_step:** Run the pipeline (compile → … → vacuity) and act on the
per-phase feedback.
