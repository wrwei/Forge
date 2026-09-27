# Isabelle Verification — PASSED

## Summary
Isabelle: 1 theory, 39 lemma(s) verified, elapsed 0:02:26, peak 26 MB.

## Run history
- New this run: 1
- Recurring from previous run: 0
- Resolved since previous run: 8

**Resolved issue titles**
- Type unification failed
- Isabelle error: *** Type error in application: operator not of function type
- Isabelle error: *** Operator:  LreController.cda<𝗌> :: ℝ
- Isabelle error: Operand:   closestDynamicIndex () :: ℤ
- Isabelle error: *** Coercion Inference:
- Isabelle error: *** Local coercion insertion on the operator failed:
- Isabelle error: No complex coercion from "real" to "fun"
- Isabelle error: At command "zoperation" (line 100 of "/Users/ranwei/Gitee/forge-d2-run3-lre/forg

## Issues
### Issue 1: isabelle_summary — Verification summary

**Raw**
```
Runtime: elapsed **0:02:26**, peak memory **25 MB**.

**Theories built (1), 20 lemma(s) proven total:**

**`LreController_Beh.thy`** — 20 lemmas:
- *Deadlock-freedom* (1 lemma):
  - `LreController_deadlock_free`
- *Invariant preservation* (19 lemmas): `Init_inv`, `InitialToOCM_inv`, `OCMToMOM_inv`, `OCMToOCM_inv`, `OCMToOCM_1_inv`, `MOMToOCM_inv`, `MOMToOCM_1_inv`, `MOMToHCM_inv`, `MOMToOCM_2_inv`, `MOMToCAM_inv`, `MOMToHCM_1_inv`, `MOMToHCM_2_inv`, `MOMToHCM_3_inv`, `HCMToOCM_inv`, `HCMToOCM_1_inv`, `HCMToCAM_inv`, `HCMToMOM_inv`, `CAMToOCM_inv`, `CAMToOCM_1_inv`
```

**Fix directive**
No action — informational.

## Files to review
(none identified)

## Next step
Z-Machine deadlock-freedom (and any R-series lemmas) verified. Continue refinement in Phase 1; Isabelle is the slowest backend, so re-run only on milestones.
