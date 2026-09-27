# Isabelle Verification — PASSED

## Summary
Isabelle: 1 theory, 21 lemma(s) verified, elapsed 0:01:09, peak 26 MB.

## Run history
- New this run: 1
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: isabelle_summary — Verification summary

**Raw**
```
Runtime: elapsed **0:01:09**, peak memory **26 MB**.

**Theories built (1), 11 lemma(s) proven total:**

**`GasAnalysisController_Beh.thy`** — 11 lemmas:
- *Deadlock-freedom* (1 lemma):
  - `GasAnalysisController_deadlock_free`
- *Invariant preservation* (10 lemmas): `Init_inv`, `InitialToREADING_inv`, `READINGToANALYSIS_inv`, `ANALYSISToNO_GAS_inv`, `ANALYSISToGAS_DETECTED_inv`, `NO_GASToREADING_inv`, `GAS_DETECTEDToSOURCE_FOUND_inv`, `GAS_DETECTEDToREADING_inv`, `SOURCE_FOUNDToCONCLUDED_inv`, `CONCLUDEDToCONCLUDED_inv`
```

**Fix directive**
No action — informational.

## Files to review
(none identified)

## Next step
Z-Machine deadlock-freedom (and any R-series lemmas) verified. Continue refinement in Phase 1; Isabelle is the slowest backend, so re-run only on milestones.
