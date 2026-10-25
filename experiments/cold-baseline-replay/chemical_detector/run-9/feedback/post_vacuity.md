# 6d — Vacuity Audit (Dafny + Isabelle) — PASSED

## Summary
No blocking vacuity signals detected (syntactic D1/I1 and semantic satisfiability/reachability/constant-consistency checks). Any `0 errors` from Dafny / Isabelle / FDR is at least potentially load-bearing for the checked signals. 3 ADVISORY finding(s) recorded and NOT blocking: P1/dead_state_no_offer, S1/idle_unreachable. These follow from the deliberate narrowing of the CSP data domains that FDR4's finiteness requirement imposes, not from the Java under test; they bound what the passing FDR4 assertions are evidence about and must be reported with any verdict drawn from them.

## Run history
- New this run: 3
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: idle_unreachable — [ADVISORY] Idle operation Idle_Analysis is unreachable: the mode's branches are exhaustive

**Raw**
```
pre "st= Analysis \<and> \<not>(sts= (noGas)) \<and> \<not>(sts= (gasD))"  -- offered={}: UNSAT (enum lens `sts` has no admissible value left (last atom: `! (sts= (gasD))`))
```

**Fix directive**
ADVISORY — no action for the codegen agent. This follows from the deliberate CSP domain narrowing (FDR4 finiteness), not from the Java. Record it with any verdict drawn from the CSP checks.

### Issue 2: dead_state_no_offer — [ADVISORY] 1 reachable state(s) from which no operation can fire when the environment offers nothing

**Raw**
```
(Final, {})
```

**Fix directive**
ADVISORY — no action for the codegen agent. This follows from the deliberate CSP domain narrowing (FDR4 finiteness), not from the Java. Record it with any verdict drawn from the CSP checks.

### Issue 3: dead_state_no_offer — [ADVISORY] 1 reachable state(s) from which no operation can fire when the environment offers nothing

**Raw**
```
(Final, {})
```

**Fix directive**
ADVISORY — no action for the codegen agent. This follows from the deliberate CSP domain narrowing (FDR4 finiteness), not from the Java. Record it with any verdict drawn from the CSP checks.

## Files to review
(none identified)

## Next step
Nothing to do. Re-run after each iteration to catch regressions.
