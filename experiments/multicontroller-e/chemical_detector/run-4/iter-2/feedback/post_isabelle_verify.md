# Isabelle Verification — PASSED

## Summary
Isabelle: 2 theories, 68 lemma(s) verified, elapsed 0:04:30, peak 27 MB.

## Run history
- New this run: 1
- Recurring from previous run: 0
- Resolved since previous run: 1

**Resolved issue titles**
- Verification summary

## Issues
### Issue 1: isabelle_summary — Verification summary

**Raw**
```
Runtime: elapsed **0:04:30**, peak memory **26 MB**.

**Theories built (2), 35 lemma(s) proven total:**

**`GasAnalysisController_Beh.thy`** — 10 lemmas:
- *Deadlock-freedom* (1 lemma):
  - `GasAnalysisController_deadlock_free`
- *Invariant preservation* (9 lemmas): `Init_inv`, `InitialToReading_inv`, `ReadingToAnalysis_inv`, `AnalysisToNoGas_inv`, `AnalysisToGasDetected_inv`, `NoGasToReading_inv`, `GasDetectedToConcluded_inv`, `GasDetectedToReading_inv`, `ConcludedToConcluded_inv`

**`MovementController_Beh.thy`** — 25 lemmas:
- *Deadlock-freedom* (1 lemma):
  - `MovementController_deadlock_free`
- *Invariant preservation* (24 lemmas): `Init_inv`, `InitialToWaiting_inv`, `WaitingToFound_inv`, `WaitingToGoing_inv`, `WaitingToWaiting_inv`, `GoingToFound_inv`, `GoingToGoing_inv`, `GoingToAvoiding_inv`, `GoingToWaiting_inv`, `FoundToFound_inv`, `AvoidingToFound_inv`, `AvoidingToTryingAgain_inv`, `AvoidingToWaiting_inv`, `TryingAgainToFound_inv`, `TryingAgainToTryingAgain_inv`, `TryingAgainToAvoidingAgain_inv`, `TryingAgainToWaiting_inv`, `AvoidingAgainToFound_inv`, `AvoidingAgainToWaiting_inv`, `AvoidingAgainToAvoiding_inv`, `AvoidingAgainToGettingOut_inv`, `GettingOutToFound_inv`, `GettingOutToGoing_inv`, `GettingOutToWaiting_inv`
```

**Fix directive**
No action — informational.

## Files to review
(none identified)

## Next step
Z-Machine deadlock-freedom (and any R-series lemmas) verified. Continue refinement in Phase 1; Isabelle is the slowest backend, so re-run only on milestones.
