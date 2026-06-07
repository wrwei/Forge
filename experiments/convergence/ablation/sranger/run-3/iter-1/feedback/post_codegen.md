# Post-codegen review — sranger, ablation run-3, iter-1 (cold codegen)

**Status:** complete

## Summary

Full cold codegen of the SRanger controller from the canonical inputs
(system_description.txt, requirement_all.json, CLAUDE.md, codegen rules,
chain-of-thought, few-shot). 9 Java files under
`java.generated.project/src/main/java/sranger/` plus `result_codegen.json`.

## Issues for user review

1. **design_choice — terminal mode named `Stopped`, not `Final`** (SR-DM1,
   SR-FR3). CLAUDE.md mandates that the theory-generated primary controller
   must NOT contain a `Final` state (the forked Isabelle theory generator
   leaves a `Final` state's deadlock-freedom disjunct unprovable). The
   terminal mode is therefore named `Stopped` and kept live with a
   tick-triggered self-loop (bare-precondition operation, CLAUDE.md
   pattern (b)). Behaviour is unchanged: EndTask still enters a terminal
   mode that issues Move(0, 0) and never leaves it.

2. **design_choice — no power-up actuator command** (SR-FR1, SR-Beh1). The
   controller constructor only assigns fields; the initial Move(moveVel, 0)
   entry action is represented in the formal model via the entry-action
   lifting on transitions into Moving, not as a Java constructor side
   effect. Rationale: ETL extracts actions from step() only; a constructor
   side effect would be invisible to the model and untestable through it.

3. **design_choice — extra tick self-loop on Stopped.** Not in SR-Beh1..7;
   added so the terminal mode has a bare-precondition operation
   (deadlock-freedom). SR-DC1 (unique transitions per source/trigger pair)
   is still satisfied — it is the only Stopped transition.

4. **invented_default — Sensor no-reading default = 1000.0 m.** Spec says
   "a large default value"; 1000.0 chosen (>> obstacleThreshold 0.5).

5. **design_choice — obstacle transition guard.** Moving → Turning requires
   both the Obstacle event (SR-Beh2) and the obstacleDetected predicate
   (SR-GP1), conjoined as `event instanceof InputEvent.Obstacle &&
   obstacleDetected`.

6. **design_choice — Clock units in seconds.** `Clock.now()` returns
   seconds so the `turnDurationElapsed` predicate compares directly against
   TURN_DURATION = 2.0 s. ETL clock-promotion pattern (field assigned from
   a Clock-typed receiver method) applies regardless of method name.

## Unimplemented

None — all 22 requirement IDs traced in result_codegen.json.
