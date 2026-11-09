# Isabelle Verification — PASSED

## Summary
Isabelle: 1 theory, 39 lemma(s) verified, elapsed 0:01:06, peak 13 MB.

## Run history
- New this run: 1
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: isabelle_summary — Verification summary

**Raw**
```
Runtime: elapsed **0:01:06**, peak memory **12 MB**.

**Theories built (1), 20 lemma(s) proven total:**

**`SynController_Beh.thy`** — 20 lemmas:
- *Deadlock-freedom* (1 lemma):
  - `SynController_deadlock_free`
- *Invariant preservation* (19 lemmas): `Init_inv`, `InitialToM1_inv`, `M1ToFinal_inv`, `M1ToM2_inv`, `M1ToM1_inv`, `M1ToM1_1_inv`, `M2ToFinal_inv`, `M2ToM3_inv`, `M2ToM1_inv`, `M2ToM2_inv`, `M3ToFinal_inv`, `M3ToM1_inv`, `M3ToM2_inv`, `M3ToM3_inv`, `FinalToFinal_inv`, `Idle_M1_inv`, `Idle_M2_inv`, `Idle_M3_inv`, `Idle_Final_inv`
```

**Fix directive**
No action — informational.

## Files to review
(none identified)

## Next step
Z-Machine deadlock-freedom (and any R-series lemmas) verified. Continue refinement in Phase 1; Isabelle is the slowest backend, so re-run only on milestones.
