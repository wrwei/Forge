# 6a — Formal Verification (FDR4) — PASSED

## Summary
FDR4: 8 assertion(s) checked, 6 passed, 2 expected failure(s).

## Run history
**WARNING — thrashing detected.** 1 issue(s) have recurred 3+ runs in a row despite fixes: `Expected failure (informational)`. Previous fix strategies are not working — change approach (e.g. re-read the originating requirement, or escalate to the user).

- New this run: 0
- Recurring from previous run: 1
- Resolved since previous run: 0

## Issues
### Issue 1: expected_failure — Expected failure (informational) [RECURRING x3 — fix strategy failing]

**Raw**
```
The following failures are marked EXPECTED in config.phases.fdr4.expected_failures and are reported for information only: P_SRangerController_Module :[deterministic]; P_SRangerController_Module_ctrl_ref0 :[deterministic].
```

**Fix directive**
No action required.

## Files to review
(none identified)

## Next step
Proceed to Phase 6b (Dafny Verification). On a clean FDR4 pass you may continue refining features in Phase 1.
