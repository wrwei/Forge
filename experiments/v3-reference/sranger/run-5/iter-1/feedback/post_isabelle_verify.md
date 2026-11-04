# Isabelle Verification — PASSED

## Summary
Isabelle: 1 theory, 21 lemma(s) verified, elapsed 0:00:29, peak 28 MB.

## Run history
- New this run: 1
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: isabelle_summary — Verification summary

**Raw**
```
Runtime: elapsed **0:00:29**, peak memory **28 MB**.

**Theories built (1), 11 lemma(s) proven total:**

**`SRangerController_Beh.thy`** — 11 lemmas:
- *Deadlock-freedom* (1 lemma):
  - `SRangerController_deadlock_free`
- *Invariant preservation* (10 lemmas): `Init_inv`, `InitialToMOVING_inv`, `MOVINGToFINAL_inv`, `MOVINGToTURNING_inv`, `MOVINGToMOVING_inv`, `TURNINGToFINAL_inv`, `TURNINGToMOVING_inv`, `TURNINGToTURNING_inv`, `Idle_MOVING_inv`, `Idle_TURNING_inv`
```

**Fix directive**
No action — informational.

## Files to review
(none identified)

## Next step
Z-Machine deadlock-freedom (and any R-series lemmas) verified. Continue refinement in Phase 1; Isabelle is the slowest backend, so re-run only on milestones.
