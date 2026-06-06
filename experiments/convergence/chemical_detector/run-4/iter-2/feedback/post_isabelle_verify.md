# Isabelle Verification — PASSED

## Summary
Isabelle: 1 theory, 10 lemma(s) verified, elapsed 0:00:26, peak 8 MB.

## Run history
- New this run: 1
- Recurring from previous run: 0
- Resolved since previous run: 2

**Resolved issue titles**
- Tactic timeout, suspected in GasAnalysisController_deadlock_free (deadlock_free — inferred from .thy)
- Tactic interrupt — cascade from timeout in GasAnalysisController_deadlock_free

## Issues
### Issue 1: isabelle_summary — Verification summary

**Raw**
```
Runtime: elapsed **0:00:26**, peak memory **8 MB**.

**Theories built (1), 10 lemma(s) proven total:**

**`GasAnalysisController_Beh.thy`** — 10 lemmas:
- *Deadlock-freedom* (1 lemma):
  - `GasAnalysisController_deadlock_free`
- *Invariant preservation* (9 lemmas): `Init_inv`, `InitialToReading_inv`, `ReadingToAnalysis_inv`, `AnalysisToGasDetected_inv`, `AnalysisToNoGas_inv`, `NoGasToReading_inv`, `GasDetectedToDone_inv`, `GasDetectedToReading_inv`, `DoneToDone_inv`
```

**Fix directive**
No action — informational.

## Files to review
(none identified)

## Next step
Z-Machine deadlock-freedom (and any R-series lemmas) verified. Continue refinement in Phase 1; Isabelle is the slowest backend, so re-run only on milestones.
