# Isabelle Verification — PASSED

## Summary
Isabelle: 2 theories, 86 lemma(s) verified, elapsed 0:05:14, peak 26 MB.

## Run history
- New this run: 1
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: isabelle_summary — Verification summary

**Raw**
```
Runtime: elapsed **0:05:14**, peak memory **26 MB**.

**Theories built (2), 44 lemma(s) proven total:**

**`GasAnalysisController_Beh.thy`** — 12 lemmas:
- *Deadlock-freedom* (1 lemma):
  - `GasAnalysisController_deadlock_free`
- *Invariant preservation* (11 lemmas): `Init_inv`, `InitialToReading_inv`, `ReadingToAnalysis_inv`, `AnalysisToNoGas_inv`, `AnalysisToGasDetected_inv`, `NoGasToReading_inv`, `GasDetectedToConcluded_inv`, `GasDetectedToReading_inv`, `ConcludedToConcluded_inv`, `Idle_Reading_inv`, `Idle_Concluded_inv`

**`MovementController_Beh.thy`** — 32 lemmas:
- *Deadlock-freedom* (1 lemma):
  - `MovementController_deadlock_free`
- *Invariant preservation* (31 lemmas): `Init_inv`, `InitialToWaiting_inv`, `WaitingToFound_inv`, `WaitingToWaiting_inv`, `WaitingToGoing_inv`, `GoingToFound_inv`, `GoingToWaiting_inv`, `GoingToAvoiding_inv`, `GoingToGoing_inv`, `FoundToFound_inv`, `AvoidingToFound_inv`, `AvoidingToWaiting_inv`, `AvoidingToTryingAgain_inv`, `TryingAgainToFound_inv`, `TryingAgainToWaiting_inv`, `TryingAgainToAvoidingAgain_inv`, `TryingAgainToTryingAgain_inv`, `AvoidingAgainToFound_inv`, `AvoidingAgainToWaiting_inv`, `AvoidingAgainToAvoiding_inv`, `AvoidingAgainToGettingOut_inv`, `GettingOutToFound_inv`, `GettingOutToWaiting_inv`, `GettingOutToGoing_inv`, `Idle_Waiting_inv`, `Idle_Going_inv`, `Idle_Found_inv`, `Idle_Avoiding_inv`, `Idle_TryingAgain_inv`, `Idle_AvoidingAgain_inv`, `Idle_GettingOut_inv`
```

**Fix directive**
No action — informational.

## Files to review
(none identified)

## Next step
Z-Machine deadlock-freedom (and any R-series lemmas) verified. Continue refinement in Phase 1; Isabelle is the slowest backend, so re-run only on milestones.
