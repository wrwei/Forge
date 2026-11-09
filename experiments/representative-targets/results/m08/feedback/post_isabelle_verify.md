# Isabelle Verification — PASSED

## Summary
Isabelle: 1 theory, 79 lemma(s) verified, elapsed 0:07:09, peak 13 MB.

## Run history
- New this run: 1
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: isabelle_summary — Verification summary

**Raw**
```
Runtime: elapsed **0:07:09**, peak memory **13 MB**.

**Theories built (1), 40 lemma(s) proven total:**

**`SynController_Beh.thy`** — 40 lemmas:
- *Deadlock-freedom* (1 lemma):
  - `SynController_deadlock_free`
- *Invariant preservation* (39 lemmas): `Init_inv`, `InitialToM1_inv`, `M1ToFinal_inv`, `M1ToM2_inv`, `M1ToM1_inv`, `M1ToM1_1_inv`, `M2ToFinal_inv`, `M2ToM3_inv`, `M2ToM1_inv`, `M2ToM2_inv`, `M3ToFinal_inv`, `M3ToM4_inv`, `M3ToM2_inv`, `M3ToM3_inv`, `M4ToFinal_inv`, `M4ToM5_inv`, `M4ToM3_inv`, `M4ToM4_inv`, `M5ToFinal_inv`, `M5ToM6_inv`, `M5ToM4_inv`, `M5ToM5_inv`, `M6ToFinal_inv`, `M6ToM7_inv`, `M6ToM5_inv`, `M6ToM6_inv`, `M7ToFinal_inv`, `M7ToM1_inv`, `M7ToM6_inv`, `M7ToM7_inv`, `FinalToFinal_inv`, `Idle_M1_inv`, `Idle_M2_inv`, `Idle_M3_inv`, `Idle_M4_inv`, `Idle_M5_inv`, `Idle_M6_inv`, `Idle_M7_inv`, `Idle_Final_inv`
```

**Fix directive**
No action — informational.

## Files to review
(none identified)

## Next step
Z-Machine deadlock-freedom (and any R-series lemmas) verified. Continue refinement in Phase 1; Isabelle is the slowest backend, so re-run only on milestones.
