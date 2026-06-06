# Isabelle Verification — PASSED

## Summary
Isabelle: 1 theory, 12 lemma(s) verified, elapsed 0:00:30, peak 8 MB.

## Run history
- New this run: 1
- Recurring from previous run: 0
- Resolved since previous run: 2

**Resolved issue titles**
- Isabelle error: Duplicate constant declaration "GasAnalysisController_Beh.SeqGs" vs. "GasAnalysi
- Isabelle error: At command "definition" (line 60 of "/mnt/c/Users/Will/Gitee/fmgvc-cd-run6-redo/

## Issues
### Issue 1: isabelle_summary — Verification summary

**Raw**
```
Runtime: elapsed **0:00:30**, peak memory **8 MB**.

**Theories built (1), 12 lemma(s) proven total:**

**`GasAnalysisController_Beh.thy`** — 12 lemmas:
- *Deadlock-freedom* (1 lemma):
  - `GasAnalysisController_deadlock_free`
- *Invariant preservation* (11 lemmas): `Init_inv`, `InitialToReading_inv`, `ReadingToAnalysis_inv`, `AnalysisToNoGas_inv`, `AnalysisToGasDetected_inv`, `AnalysisToAnalysis_inv`, `NoGasToReading_inv`, `GasDetectedToLocated_inv`, `GasDetectedToReading_inv`, `GasDetectedToGasDetected_inv`, `LocatedToLocated_inv`
```

**Fix directive**
No action — informational.

## Files to review
(none identified)

## Next step
Z-Machine deadlock-freedom (and any R-series lemmas) verified. Continue refinement in Phase 1; Isabelle is the slowest backend, so re-run only on milestones.
