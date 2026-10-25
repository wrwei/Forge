# Isabelle Verification — PASSED

## Summary
Isabelle: 2 theories, 86 lemma(s) verified, elapsed 0:00:11, peak 26 MB.

## Run history
- New this run: 1
- Recurring from previous run: 0
- Resolved since previous run: 2

**Resolved issue titles**
- Isabelle error: /tmp/isabelle-/bash_script7712529048815874256: line 2: lualatex: command not fou
- Isabelle error: Failed to build document "document"

## Issues
### Issue 1: isabelle_summary — Verification summary

**Raw**
```
Runtime: elapsed **0:00:11**, peak memory **26 MB**.

**Theories built (2), 44 lemma(s) proven total:**

**`GasAnalysisController_Beh.thy`** — 12 lemmas:
- *Deadlock-freedom* (1 lemma):
  - `GasAnalysisController_deadlock_free`
- *Invariant preservation* (11 lemmas): `Init_inv`, `InitialToREADING_inv`, `READINGToANALYSIS_inv`, `ANALYSISToNO_GAS_inv`, `ANALYSISToGAS_DETECTED_inv`, `NO_GASToREADING_inv`, `GAS_DETECTEDToFINAL_inv`, `GAS_DETECTEDToREADING_inv`, `FINALToFINAL_inv`, `Idle_READING_inv`, `Idle_FINAL_inv`

**`MovementController_Beh.thy`** — 32 lemmas:
- *Deadlock-freedom* (1 lemma):
  - `MovementController_deadlock_free`
- *Invariant preservation* (31 lemmas): `Init_inv`, `InitialToWAITING_inv`, `WAITINGToFOUND_inv`, `WAITINGToWAITING_inv`, `WAITINGToGOING_inv`, `GOINGToFOUND_inv`, `GOINGToWAITING_inv`, `GOINGToGOING_inv`, `GOINGToAVOIDING_inv`, `FOUNDToFOUND_inv`, `AVOIDINGToFOUND_inv`, `AVOIDINGToWAITING_inv`, `AVOIDINGToTRYING_AGAIN_inv`, `TRYING_AGAINToFOUND_inv`, `TRYING_AGAINToWAITING_inv`, `TRYING_AGAINToTRYING_AGAIN_inv`, `TRYING_AGAINToAVOIDING_AGAIN_inv`, `AVOIDING_AGAINToFOUND_inv`, `AVOIDING_AGAINToWAITING_inv`, `AVOIDING_AGAINToAVOIDING_inv`, `AVOIDING_AGAINToGETTING_OUT_inv`, `GETTING_OUTToFOUND_inv`, `GETTING_OUTToWAITING_inv`, `GETTING_OUTToGOING_inv`, `Idle_WAITING_inv`, `Idle_GOING_inv`, `Idle_FOUND_inv`, `Idle_AVOIDING_inv`, `Idle_TRYING_AGAIN_inv`, `Idle_AVOIDING_AGAIN_inv`, `Idle_GETTING_OUT_inv`
```

**Fix directive**
No action — informational.

## Files to review
(none identified)

## Next step
Z-Machine deadlock-freedom (and any R-series lemmas) verified. Continue refinement in Phase 1; Isabelle is the slowest backend, so re-run only on milestones.
