# Isabelle Verification — PASSED

## Summary
Isabelle: 1 theory, 17 lemma(s) verified, elapsed 0:00:40, peak 27 MB.

## Run history
- New this run: 1
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: isabelle_summary — Verification summary

**Raw**
```
Runtime: elapsed **0:00:40**, peak memory **26 MB**.

**Theories built (1), 9 lemma(s) proven total:**

**`SRangerController_Beh.thy`** — 9 lemmas:
- *Deadlock-freedom* (1 lemma):
  - `SRangerController_deadlock_free`
- *Invariant preservation* (8 lemmas): `Init_inv`, `InitialToMOVING_inv`, `MOVINGToFINAL_inv`, `MOVINGToTURNING_inv`, `MOVINGToMOVING_inv`, `TURNINGToFINAL_inv`, `TURNINGToMOVING_inv`, `TURNINGToTURNING_inv`
```

**Fix directive**
No action — informational.

## Files to review
(none identified)

## Next step
Z-Machine deadlock-freedom (and any R-series lemmas) verified. Continue refinement in Phase 1; Isabelle is the slowest backend, so re-run only on milestones.
