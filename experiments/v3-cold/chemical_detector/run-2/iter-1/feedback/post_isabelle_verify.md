# Isabelle Verification — PASSED

## Summary
Isabelle: 2 theories, 76 lemma(s) verified, elapsed 0:03:47, peak 27 MB.

## Run history
- New this run: 1
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: isabelle_summary — Verification summary

**Raw**
```
Runtime: elapsed **0:03:47**, peak memory **27 MB**.

**Theories built (2), 39 lemma(s) proven total:**

**`GasAnalysisController_Beh.thy`** — 10 lemmas:
- *Deadlock-freedom* (1 lemma):
  - `GasAnalysisController_deadlock_free`
- *Invariant preservation* (9 lemmas): `Init_inv`, `InitialToREADING_inv`, `READINGToANALYSING_inv`, `ANALYSINGToGAS_ABSENT_inv`, `ANALYSINGToGAS_DETECTED_inv`, `GAS_ABSENTToREADING_inv`, `GAS_DETECTEDToCONCLUDED_inv`, `GAS_DETECTEDToREADING_inv`, `Idle_READING_inv`

**`MovementController_Beh.thy`** — 29 lemmas:
- *Deadlock-freedom* (1 lemma):
  - `MovementController_deadlock_free`
- *Invariant preservation* (28 lemmas): `Init_inv`, `InitialToWAITING_inv`, `WAITINGToFOUND_inv`, `WAITINGToWAITING_inv`, `WAITINGToGOING_inv`, `GOINGToFOUND_inv`, `GOINGToWAITING_inv`, `GOINGToGOING_inv`, `GOINGToAVOIDING_inv`, `AVOIDINGToFOUND_inv`, `AVOIDINGToWAITING_inv`, `AVOIDINGToTRYING_AGAIN_inv`, `TRYING_AGAINToFOUND_inv`, `TRYING_AGAINToWAITING_inv`, `TRYING_AGAINToTRYING_AGAIN_inv`, `TRYING_AGAINToAVOIDING_AGAIN_inv`, `AVOIDING_AGAINToFOUND_inv`, `AVOIDING_AGAINToWAITING_inv`, `AVOIDING_AGAINToAVOIDING_inv`, `AVOIDING_AGAINToGETTING_OUT_inv`, `GETTING_OUTToFOUND_inv`, `GETTING_OUTToWAITING_inv`, `GETTING_OUTToGOING_inv`, `Idle_WAITING_inv`, `Idle_GOING_inv`, `Idle_AVOIDING_inv`, `Idle_TRYING_AGAIN_inv`, `Idle_GETTING_OUT_inv`
```

**Fix directive**
No action — informational.

## Files to review
(none identified)

## Next step
Z-Machine deadlock-freedom (and any R-series lemmas) verified. Continue refinement in Phase 1; Isabelle is the slowest backend, so re-run only on milestones.
