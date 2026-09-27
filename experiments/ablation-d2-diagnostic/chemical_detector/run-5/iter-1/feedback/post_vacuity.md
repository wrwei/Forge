# 6d — Vacuity Audit (Dafny + Isabelle) — PASSED

## Summary
No blocking vacuity signals detected (syntactic D1/I1 and semantic satisfiability/reachability/constant-consistency checks). Any `0 errors` from Dafny / Isabelle / FDR is at least potentially load-bearing for the checked signals. 1 ADVISORY finding(s) recorded and NOT blocking: P1/dead_state_no_offer. These follow from the deliberate narrowing of the CSP data domains that FDR4's finiteness requirement imposes, not from the Java under test; they bound what the passing FDR4 assertions are evidence about and must be reported with any verdict drawn from them.

## Run history
- New this run: 1
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: dead_state_no_offer — [ADVISORY] 2 reachable state(s) from which no operation can fire when the environment offers nothing

**Raw**
```
(Concluded, {}); (Reading, {})
```

**Fix directive**
ADVISORY — no action for the codegen agent. This follows from the deliberate CSP domain narrowing (FDR4 finiteness), not from the Java. Record it with any verdict drawn from the CSP checks.

## Files to review
(none identified)

## Next step
Nothing to do. Re-run after each iteration to catch regressions.
