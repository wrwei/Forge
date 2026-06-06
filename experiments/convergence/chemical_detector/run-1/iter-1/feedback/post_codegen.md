# Post-Codegen Review — chemical_detector (run-4, iter 1, cold codegen)

**Status:** complete

## Summary

Full cold codegen of the chemical_detector study from
`system_description.txt` + `requirement_all.json` + CLAUDE.md codegen rules.
Two controllers (GasAnalysisController, MovementController), one sensor
service, Vehicle actuator, Clock, 6 constants, 5 input events + 1 output
event. All requirements traced in `result_codegen.json`.

## Issues

1. **design_choice — no Final states in either controller.**
   The spec gives both subsystems a final state j1 (CD-GA-Beh6,
   CD-MV-Beh9). CLAUDE.md requires the theory-generated (primary)
   controller to have NO Final mode; since discovery order is not
   controlled here, neither controller gets one. GasAnalysis reroutes
   GasDetected -> Reading after sending stop; Movement's Found mode is a
   live terminal state that self-loops on stop (re-emitting flag — the
   stop/flag emissions are idempotent).

2. **design_choice — odometer modeled as a sensor variable, not an event.**
   CD-Evt3 declares an odometer event, but the spec consumes it inside
   entry/transition actions (odometer ? d0 in Avoiding entry,
   odometer ? d1 on TryingAgain -> AvoidingAgain), which a
   one-event-per-step Java controller cannot express as a trigger.
   Implemented as zero-arg `DetectorSensors.odometerDistance()` assigned
   in those actions, which the M2M lifts into the Sensors interface.

3. **design_choice — Vehicle operations are plain methods, not compute() classes.**
   CD-OP1..4 are platform-provided operations (move, randomWalk,
   shortRandomWalk, changeDirection), invoked as actions; they are
   implemented as Vehicle methods so the ETL extracts Calls, not
   operation state machines. No requirement defines a compute()-style
   named computation group, so the `operation` subpackage is empty.

4. **invented_default — analysis() classification rule.**
   CD-Fn1 says "noGas if no reading indicates the target chemical".
   Implemented "indicates" as: chemical identity equals Chem.TARGET AND
   intensity > 0. Chem is an invented two-literal enum (TARGET, OTHER)
   since CD-DM4 only requires equality.

5. **invented_default — angle(index) mapping.**
   CD-DM7 references an angle function mapping sequence position to
   direction without defining it. Implemented 0->Front, 1->Left,
   2->Right, >=3->Back (0-based Java indices).

6. **design_choice — Waiting's during-action randomWalk() encoded as
   top-of-mode-block statement.** The pipeline extracts entry actions,
   not during actions; CLAUDE.md names this exact pattern (randomWalk at
   the head of the Waiting block).

7. **design_choice — guards written as complementary predicate pairs.**
   Analysis uses sts==noGas / !(sts==noGas), GasDetected uses
   goreq(ins,thr) / !goreq(ins,thr), AvoidingAgain uses
   (withinStuckPeriod || escapedDistance) and its negation — total guard
   cover on every autonomous-only mode, no self-loops needed
   (per CLAUDE.md's trilemma guidance).

8. **invented_default — constant values.** THR=1.0, LV=1.0, EVADE_TIME=1,
   STUCK_PERIOD=1, STUCK_DIST=1.0, OUT_PERIOD=1 — spec gives no values;
   chosen within the {0..1} CSP instantiation ranges.

## Next step

Run the full pipeline (`python experiments/scripts/run_experiment_iteration.py`).
