# Isabelle Verification — PASSED

## Summary
Isabelle: 1 theory, 25 lemma(s) verified, elapsed 0:00:35, peak 28 MB.

## Run history
- New this run: 1
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: isabelle_summary — Verification summary

**Raw**
```
Runtime: elapsed **0:00:35**, peak memory **27 MB**.

**Theories built (1), 13 lemma(s) proven total:**

**`SRangerController_Beh.thy`** — 13 lemmas:
- *Deadlock-freedom* (1 lemma):
  - `SRangerController_deadlock_free`
- *Invariant preservation* (12 lemmas): `Init_inv`, `InitialToMoving_inv`, `MovingToFinal_inv`, `MovingToTurning_inv`, `MovingToMoving_inv`, `TurningToFinal_inv`, `TurningToMoving_inv`, `TurningToTurning_inv`, `FinalToFinal_inv`, `Idle_Moving_inv`, `Idle_Turning_inv`, `Idle_Final_inv`
```

**Fix directive**
No action — informational.

## Files to review
(none identified)

## Next step
Z-Machine deadlock-freedom (and any R-series lemmas) verified. Continue refinement in Phase 1; Isabelle is the slowest backend, so re-run only on milestones.
