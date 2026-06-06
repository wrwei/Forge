# Isabelle Verification — PASSED

## Summary
Isabelle: 1 theory, 9 lemma(s) verified, elapsed 0:00:23, peak 8 MB.

## Run history
- New this run: 1
- Recurring from previous run: 0
- Resolved since previous run: 1

**Resolved issue titles**
- Isabelle reported a failure but no error lines parsed

## Issues
### Issue 1: isabelle_summary — Verification summary

**Raw**
```
Runtime: elapsed **0:00:23**, peak memory **8 MB**.

**Theories built (1), 9 lemma(s) proven total:**

**`GasAnalysisController_Beh.thy`** — 9 lemmas:
- *Deadlock-freedom* (1 lemma):
  - `GasAnalysisController_deadlock_free`
- *Invariant preservation* (8 lemmas): `Init_inv`, `InitialToReading_inv`, `ReadingToAnalysis_inv`, `AnalysisToNoGas_inv`, `AnalysisToGasDetected_inv`, `NoGasToReading_inv`, `GasDetectedToReading_inv`, `GasDetectedToReading_1_inv`
```

**Fix directive**
No action — informational.

## Files to review
(none identified)

## Next step
Z-Machine deadlock-freedom (and any R-series lemmas) verified. Continue refinement in Phase 1; Isabelle is the slowest backend, so re-run only on milestones.
