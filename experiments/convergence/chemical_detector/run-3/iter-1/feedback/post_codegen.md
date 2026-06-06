# Post-Codegen Review — chemical_detector iter 1 (cold codegen)

**Status:** complete

## Summary

Full cold codegen of the Chemical Detector from `system_description.txt` +
`requirement_all.json`. Two controllers per CD-ARCH2 (GasAnalysisController,
MovementController), shared events turn/stop/resume, Vehicle as the combined
sensing/actuation surface, Clock-based stuck detection.

## Issues

1. **design_choice** — Neither controller has a terminal/final mode. The spec's
   final states (GA j1 after GasDetected, MV j1 after Found) are modelled as
   live modes `Located` / `Found` with event-triggered self-loops (Gas / Stop
   respectively), because the Isabelle theory generator cannot prove
   deadlock-freedom for a controller with a `Final` state (CLAUDE.md). The
   terminating events (stop, flag) are still emitted on the way in.
2. **design_choice** — CD-OP1..4 (move, randomWalk, shortRandomWalk,
   changeDirection) are methods on `actuator.Vehicle`, not `operation/`
   compute() classes: the requirements describe them as operations *provided by
   the Vehicle*, i.e. actuation primitives invoked from transition actions, not
   controller-side computation groups.
3. **design_choice** — Waiting's `during randomWalk()` (CD-MV-FR1) is encoded
   as a top-of-mode-block statement, which the ETL extracts as an entry-style
   action; RoboChart `during` has no direct Java encoding in this pipeline.
4. **invented_default** — `analysis()` classifies gasD on chemical identity
   match only (any reading whose `c` equals the target), ignoring intensity;
   the spec doesn't define "indicates the target chemical" precisely.
5. **invented_default** — `intensity()` returns 0.0 and `location()` returns
   `Front` for empty readings (spec preconditions say non-empty; safe defaults
   per the sensor-layer rule).
6. **invented_default** — `angle(index)` maps 0→Front, 1→Left, 2→Right,
   else→Back; the spec does not give the index→direction table.
7. **invented_default** — All constants set to 1 (THR=1.0, LV=1.0,
   EVADE_TIME=1, STUCK_PERIOD=1, STUCK_DIST=1.0, OUT_PERIOD=1) to stay within
   the FDR4 type ranges {0..1}.
8. **design_choice** — Intensity (CD-DM5) is `double`/real and Chem (CD-DM4)
   is a one-field record `Chem(nat id)`; the spec leaves both opaque.
9. **design_choice** — Odometer (CD-Evt3) is a zero-arg sensor method
   `GasSensorArray.odometer()` consumed in transition actions
   (`d0 = sensor.odometer()`), not an event trigger: the spec only ever reads
   it inside entry/transition actions (`odometer ? d0`), never as a trigger.

## Next step

Run the pipeline (compile → vacuity) and iterate from `post_*` feedback.
