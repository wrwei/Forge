# Isabelle Verification — PASSED

## Summary
Isabelle: 1 theory, 20 lemma(s) verified, elapsed 0:01:18, peak 8 MB.

## Run history
- New this run: 1
- Recurring from previous run: 0
- Resolved since previous run: 2

**Resolved issue titles**
- Tactic timeout, suspected in LreController_deadlock_free (deadlock_free — inferred from .thy)
- Tactic interrupt — cascade from timeout in LreController_deadlock_free

## Issues
### Issue 1: isabelle_summary — Verification summary

**Raw**
```
Runtime: elapsed **0:01:18**, peak memory **8 MB**.

**Theories built (1), 20 lemma(s) proven total:**

**`LreController_Beh.thy`** — 20 lemmas:
- *Deadlock-freedom* (1 lemma):
  - `LreController_deadlock_free`
- *Invariant preservation* (19 lemmas): `Init_inv`, `InitialToOCM_inv`, `OCMToOCM_inv`, `OCMToOCM_1_inv`, `OCMToMOM_inv`, `MOMToCAM_inv`, `MOMToOCM_inv`, `MOMToHCM_inv`, `MOMToHCM_1_inv`, `MOMToHCM_2_inv`, `MOMToOCM_1_inv`, `MOMToOCM_2_inv`, `MOMToHCM_3_inv`, `HCMToCAM_inv`, `HCMToOCM_inv`, `HCMToMOM_inv`, `HCMToOCM_1_inv`, `CAMToOCM_inv`, `CAMToOCM_1_inv`
```

**Fix directive**
No action — informational.

## Files to review
(none identified)

## Next step
Z-Machine deadlock-freedom (and any R-series lemmas) verified. Continue refinement in Phase 1; Isabelle is the slowest backend, so re-run only on milestones.
