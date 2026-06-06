# Isabelle Verification — PASSED

## Summary
Isabelle: 1 theory, 10 lemma(s) verified, elapsed 0:00:19, peak 8 MB.

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
Runtime: elapsed **0:00:19**, peak memory **8 MB**.

**Theories built (1), 10 lemma(s) proven total:**

**`SRangerController_Beh.thy`** — 10 lemmas:
- *Deadlock-freedom* (1 lemma):
  - `SRangerController_deadlock_free`
- *Invariant preservation* (9 lemmas): `Init_inv`, `InitialToMoving_inv`, `MovingToHalted_inv`, `MovingToTurning_inv`, `MovingToMoving_inv`, `TurningToHalted_inv`, `TurningToMoving_inv`, `TurningToTurning_inv`, `HaltedToHalted_inv`
```

**Fix directive**
No action — informational.

## Files to review
(none identified)

## Next step
Z-Machine deadlock-freedom (and any R-series lemmas) verified. Continue refinement in Phase 1; Isabelle is the slowest backend, so re-run only on milestones.
