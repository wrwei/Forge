# Isabelle Verification — PASSED

## Summary
Isabelle: 2 theories, 86 lemma(s) verified, elapsed 0:06:18, peak 27 MB.

## Run history
- New this run: 1
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: isabelle_summary — Verification summary

**Raw**
```
Runtime: elapsed **0:06:18**, peak memory **27 MB**.

**Theories built (2), 44 lemma(s) proven total:**

**`GasAnalysis_Beh.thy`** — 12 lemmas:
- *Deadlock-freedom* (1 lemma):
  - `GasAnalysis_deadlock_free`
- *Invariant preservation* (11 lemmas): `Init_inv`, `InitialToREADING_inv`, `READINGToANALYSIS_inv`, `ANALYSISToNO_GAS_inv`, `ANALYSISToGAS_DETECTED_inv`, `NO_GASToREADING_inv`, `GAS_DETECTEDToCONCLUDED_inv`, `GAS_DETECTEDToREADING_inv`, `CONCLUDEDToCONCLUDED_inv`, `Idle_READING_inv`, `Idle_CONCLUDED_inv`

**`Movement_Beh.thy`** — 32 lemmas:
- *Deadlock-freedom* (1 lemma):
  - `Movement_deadlock_free`
- *Invariant preservation* (31 lemmas): `Init_inv`, `InitialToWAITING_inv`, `WAITINGToFOUND_inv`, `WAITINGToGOING_inv`, `WAITINGToWAITING_inv`, `GOINGToFOUND_inv`, `GOINGToGOING_inv`, `GOINGToAVOIDING_inv`, `GOINGToWAITING_inv`, `FOUNDToFOUND_inv`, `AVOIDINGToFOUND_inv`, `AVOIDINGToTRYING_AGAIN_inv`, `AVOIDINGToWAITING_inv`, `TRYING_AGAINToFOUND_inv`, `TRYING_AGAINToTRYING_AGAIN_inv`, `TRYING_AGAINToAVOIDING_AGAIN_inv`, `TRYING_AGAINToWAITING_inv`, `AVOIDING_AGAINToFOUND_inv`, `AVOIDING_AGAINToWAITING_inv`, `AVOIDING_AGAINToAVOIDING_inv`, `AVOIDING_AGAINToGETTING_OUT_inv`, `GETTING_OUTToFOUND_inv`, `GETTING_OUTToGOING_inv`, `GETTING_OUTToWAITING_inv`, `Idle_WAITING_inv`, `Idle_GOING_inv`, `Idle_FOUND_inv`, `Idle_AVOIDING_inv`, `Idle_TRYING_AGAIN_inv`, `Idle_AVOIDING_AGAIN_inv`, `Idle_GETTING_OUT_inv`
```

**Fix directive**
No action — informational.

## Files to review
(none identified)

## Next step
Z-Machine deadlock-freedom (and any R-series lemmas) verified. Continue refinement in Phase 1; Isabelle is the slowest backend, so re-run only on milestones.
