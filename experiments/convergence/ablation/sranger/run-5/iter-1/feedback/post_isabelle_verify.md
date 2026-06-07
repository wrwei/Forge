# Isabelle Verification — PASSED

## Summary
Isabelle: 1 theory, 10 lemma(s) verified, elapsed 0:00:18, peak 9 MB.

## Run history
- New this run: 1
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: isabelle_summary — Verification summary

**Raw**
```
Runtime: elapsed **0:00:18**, peak memory **8 MB**.

**Theories built (1), 10 lemma(s) proven total:**

**`SRangerController_Beh.thy`** — 10 lemmas:
- *Deadlock-freedom* (1 lemma):
  - `SRangerController_deadlock_free`
- *Invariant preservation* (9 lemmas): `Init_inv`, `InitialToMoving_inv`, `MovingToStopped_inv`, `MovingToTurning_inv`, `MovingToMoving_inv`, `TurningToStopped_inv`, `TurningToMoving_inv`, `TurningToTurning_inv`, `StoppedToStopped_inv`
```

**Fix directive**
No action — informational.

## Files to review
(none identified)

## Next step
Z-Machine deadlock-freedom (and any R-series lemmas) verified. Continue refinement in Phase 1; Isabelle is the slowest backend, so re-run only on milestones.
