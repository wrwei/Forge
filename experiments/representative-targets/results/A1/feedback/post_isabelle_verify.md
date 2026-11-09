# Isabelle Verification — PASSED

## Summary
Isabelle: 1 theory, 43 lemma(s) verified, elapsed 0:00:07, peak 12 MB.

## Run history
- New this run: 1
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: isabelle_summary — Verification summary

**Raw**
```
Runtime: elapsed **0:00:07**, peak memory **12 MB**.

**Theories built (1), 22 lemma(s) proven total:**

**`LreController_Beh.thy`** — 22 lemmas:
- *Deadlock-freedom* (1 lemma):
  - `LreController_deadlock_free`
- *Invariant preservation* (21 lemmas): `Init_inv`, `InitialToOCM_inv`, `OCMToOCM_inv`, `OCMToOCM_1_inv`, `OCMToMOM_inv`, `MOMToCAM_inv`, `MOMToHCM_inv`, `MOMToOCM_inv`, `MOMToOCM_1_inv`, `MOMToOCM_2_inv`, `MOMToHCM_1_inv`, `HCMToCAM_inv`, `HCMToOCM_inv`, `HCMToMOM_inv`, `HCMToOCM_1_inv`, `CAMToOCM_inv`, `CAMToOCM_1_inv`, `Idle_OCM_inv`, `Idle_MOM_inv`, `Idle_HCM_inv`, `Idle_CAM_inv`
```

**Fix directive**
No action — informational.

## Files to review
(none identified)

## Next step
Z-Machine deadlock-freedom (and any R-series lemmas) verified. Continue refinement in Phase 1; Isabelle is the slowest backend, so re-run only on milestones.
