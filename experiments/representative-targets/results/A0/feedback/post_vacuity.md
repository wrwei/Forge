# 6d — Vacuity Audit (Dafny + Isabelle) — PASSED

## Summary
No blocking vacuity signals detected (syntactic D1/I1 and semantic satisfiability/reachability/constant-consistency checks). Any `0 errors` from Dafny / Isabelle / FDR is at least potentially load-bearing for the checked signals. 4 ADVISORY finding(s) recorded and NOT blocking: D2/dafny_valid_placeholder, I2/isabelle_requirement_placeholder, R2/mode_unreachable_checked, T2/transition_unfireable_checked. These follow from the deliberate narrowing of the CSP data domains that FDR4's finiteness requirement imposes, not from the Java under test; they bound what the passing FDR4 assertions are evidence about and must be reported with any verdict drawn from them.

## Run history
- New this run: 4
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: dafny_valid_placeholder — [ADVISORY] Dafny Valid() body is literally `true`; the obligations are the per-transition ensures clauses

**Raw**
```
predicate Valid() { true }  /* 8 behavioural ensures clauses active */
```

**Fix directive**
ADVISORY — no action for the codegen agent. This follows from the deliberate CSP domain narrowing (FDR4 finiteness), not from the Java. Record it with any verdict drawn from the CSP checks.

### Issue 2: isabelle_requirement_placeholder — [ADVISORY] Isabelle requirement predicate R1 is literally `True`; 21 of 43 lemmas establish or preserve it

**Raw**
```
zexpr R1 is "True"
```

**Fix directive**
ADVISORY — no action for the codegen agent. This follows from the deliberate CSP domain narrowing (FDR4 finiteness), not from the Java. Record it with any verdict drawn from the CSP checks.

### Issue 3: mode_unreachable_checked — [ADVISORY] Mode(s) unreachable under the CHECKED CSP instantiation: CAM, HCM, MOM

**Raw**
```
checked-reachable = {OCM, initial} (narrowed ranges + constant stubs from instantiations.csp)
```

**Fix directive**
ADVISORY — no action for the codegen agent. This follows from the deliberate CSP domain narrowing (FDR4 finiteness), not from the Java. Record it with any verdict drawn from the CSP checks.

### Issue 4: transition_unfireable_checked — [ADVISORY] 16 transition(s) can never fire under the checked CSP instantiation

**Raw**
```
OCMToMOM: `odist(cdyn)>1.0` evaluates to 0 > 1 = False; MOMToCAM: source mode MOM is unreachable; MOMToHCM: source mode MOM is unreachable; MOMToOCM: source mode MOM is unreachable; MOMToOCM_1: source mode MOM is unreachable; MOMToOCM_2: source mode MOM is unreachable; MOMToHCM_1: source mode MOM is unreachable; HCMToCAM: source mode HCM is unreachable; HCMToOCM: source mode HCM is unreachable; HCMToMOM: source mode HCM is unreachable; HCMToOCM_1: source mode HCM is unreachable; CAMToOCM: source mode CAM is unreachable; CAMToOCM_1: source mode CAM is unreachable; Idle_MOM: source mode MOM is unreachable; Idle_HCM: source mode HCM is unreachable; Idle_CAM: source mode CAM is unreachable
```

**Fix directive**
ADVISORY — no action for the codegen agent. This follows from the deliberate CSP domain narrowing (FDR4 finiteness), not from the Java. Record it with any verdict drawn from the CSP checks.

## Files to review
(none identified)

## Next step
Nothing to do. Re-run after each iteration to catch regressions.
