# Post-Codegen Review — chemical_detector iter-2 (run-5, condition B)

**Status:** complete

## Summary

Iter-2 minimal fixes on the iter-1 tree: (1) `GasAnalysisController` predicate
`insAtOrAboveThr` changed from `analyzer.goreq(ins, THR)` to the direct
comparison `ins >= ChemConstants.THR` — the sensor-call form was mis-extracted
as a `var insAtOrAboveThr : nat` Sensors variable (FDR4 Int/Bool type error,
Dafny goreq arity error). `goreq` remains on `GasAnalyzer` as the CD-Fn4
implementation but is no longer referenced from a guard (the `>=` comparison
IS goreq's definition — design choice). (2) Added `result_codegen.json` rows
for `Actuator.java` (CD-ARCH2) and `Clock.java` (CD-MV-Clock1) to fix the
coverage over-implementation findings.

Iter-1 baseline (still current):

Iter-1 cold codegen: 18 Java files under `chemdetector/` implementing two
controllers (`GasAnalysisController`, `MovementController`), the `Vehicle`
platform surface, `GasAnalyzer` sensor functions, events, modes, constants,
and annotations. All 81 requirements traced in `result_codegen.json`.

## Issues for review

1. **design_choice — No Final states.** CD-GA-Beh6 / CD-MV-Beh9 specify
   transitions to final state j1. The Isabelle deadlock-freedom proof cannot
   handle a Final state, so: `GasDetected -[goreq(ins,thr)]-> Done` (still
   sends `stop`), `Done` self-loops on `gas`; `Found` self-loops on `stop`
   (flag still emitted on entry). Both self-loops are event-triggered
   bare-precondition operations.
2. **design_choice — Odometer as sensor method.** CD-Evt3's `odometer ? d0`
   entry-action reception cannot be expressed in the single-method step()
   pattern; `d0 = vehicle.odometer()` assignments are used instead
   (zero-arg sensor method → Sensors interface variable).
3. **invented_default — TARGET_CHEM / analysis() semantics.** A reading
   "indicates the target chemical" iff some entry's `c == TARGET_CHEM (=1)`.
4. **invented_default — angle() mapping.** 1→Front, 2→Left, 3→Right,
   else Back. Empty-reading defaults: intensity()=0.0, location()=Front.
5. **design_choice — Chem/Intensity as int(nat)/double(real).** No opaque
   types in the pipeline.
6. **design_choice — Constants all = 1 / 1.0** to fit the `{0..1}` FDR4
   type ranges.
7. **design_choice — Waiting `randomWalk()` during-action** expressed as a
   top-of-mode statement.

## Next step

Run the deterministic pipeline and iterate on `post_*` feedback.
