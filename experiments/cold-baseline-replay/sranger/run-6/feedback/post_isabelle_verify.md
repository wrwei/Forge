# Isabelle Verification — PASSED

## Summary
Isabelle: 1 theory, 21 lemma(s) verified, elapsed 0:00:33, peak 29 MB.

## Run history
- New this run: 1
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: isabelle_summary — Verification summary

**Raw**
```
Runtime: elapsed **0:00:33**, peak memory **29 MB**.

**Theories built (1), 11 lemma(s) proven total:**

**`SRangerController_Beh.thy`** — 11 lemmas:
- *Deadlock-freedom* (1 lemma):
  - `SRangerController_deadlock_free`
- *Invariant preservation* (10 lemmas): `Init_inv`, `InitialToMoving_inv`, `MovingToFinal_inv`, `MovingToTurning_inv`, `MovingToMoving_inv`, `TurningToFinal_inv`, `TurningToMoving_inv`, `TurningToTurning_inv`, `Idle_Moving_inv`, `Idle_Turning_inv`
```

**Fix directive**
No action — informational.

## Files to review
(none identified)

## Next step
Z-Machine deadlock-freedom (and any R-series lemmas) verified. Continue refinement in Phase 1; Isabelle is the slowest backend, so re-run only on milestones.
