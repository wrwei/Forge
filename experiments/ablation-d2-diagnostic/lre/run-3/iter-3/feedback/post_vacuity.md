# 6d — Vacuity Audit (Dafny + Isabelle) — PASSED

## Summary
No blocking vacuity signals detected (syntactic D1/I1 and semantic satisfiability/reachability/constant-consistency checks). Any `0 errors` from Dafny / Isabelle / FDR is at least potentially load-bearing for the checked signals. 4 ADVISORY finding(s) recorded and NOT blocking: P1/dead_state_no_offer, P1/dead_state_offered_pair_checked, R2/mode_unreachable_checked, T2/transition_unfireable_checked. These follow from the deliberate narrowing of the CSP data domains that FDR4's finiteness requirement imposes, not from the Java under test; they bound what the passing FDR4 assertions are evidence about and must be reported with any verdict drawn from them.

## Run history
**WARNING — thrashing detected.** 3 issue(s) have recurred 3+ runs in a row despite fixes: `[ADVISORY] Mode(s) unreachable under the CHECKED CSP instantiation: CAM, HCM, MOM`, `[ADVISORY] 1 (state, offered) pair(s) dead only under the checked CSP instantiation`, `[ADVISORY] 1 reachable state(s) from which no operation can fire when the environment offers nothing`. Previous fix strategies are not working — change approach (e.g. re-read the originating requirement, or escalate to the user).

- New this run: 0
- Recurring from previous run: 4
- Resolved since previous run: 0

## Issues
### Issue 1: mode_unreachable_checked — [ADVISORY] Mode(s) unreachable under the CHECKED CSP instantiation: CAM, HCM, MOM [RECURRING x3 — fix strategy failing]

**Raw**
```
checked-reachable = {OCM, initial} (narrowed ranges + constant stubs from instantiations.csp)
```

**Fix directive**
ADVISORY — no action for the codegen agent. This follows from the deliberate CSP domain narrowing (FDR4 finiteness), not from the Java. Record it with any verdict drawn from the CSP checks.

### Issue 2: transition_unfireable_checked — [ADVISORY] 15 transition(s) can never fire under the checked CSP instantiation [recurring x2]

**Raw**
```
OCMToMOM: `odist(cdyn)>1.0` evaluates to 0 > 1 = False; MOMToOCM: source mode MOM is unreachable; MOMToCAM: source mode MOM is unreachable; MOMToHCM: source mode MOM is unreachable; MOMToHCM_1: source mode MOM is unreachable; MOMToHCM_2: source mode MOM is unreachable; MOMToOCM_1: source mode MOM is unreachable; MOMToOCM_2: source mode MOM is unreachable; MOMToHCM_3: source mode MOM is unreachable; HCMToOCM: source mode HCM is unreachable; HCMToCAM: source mode HCM is unreachable; HCMToMOM: source mode HCM is unreachable; HCMToOCM_1: source mode HCM is unreachable; CAMToOCM: source mode CAM is unreachable; CAMToOCM_1: source mode CAM is unreachable
```

**Fix directive**
ADVISORY — no action for the codegen agent. This follows from the deliberate CSP domain narrowing (FDR4 finiteness), not from the Java. Record it with any verdict drawn from the CSP checks.

### Issue 3: dead_state_offered_pair_checked — [ADVISORY] 1 (state, offered) pair(s) dead only under the checked CSP instantiation [RECURRING x3 — fix strategy failing]

**Raw**
```
(OCM, {reqMOM})
```

**Fix directive**
ADVISORY — no action for the codegen agent. This follows from the deliberate CSP domain narrowing (FDR4 finiteness), not from the Java. Record it with any verdict drawn from the CSP checks.

### Issue 4: dead_state_no_offer — [ADVISORY] 1 reachable state(s) from which no operation can fire when the environment offers nothing [RECURRING x3 — fix strategy failing]

**Raw**
```
(OCM, {})
```

**Fix directive**
ADVISORY — no action for the codegen agent. This follows from the deliberate CSP domain narrowing (FDR4 finiteness), not from the Java. Record it with any verdict drawn from the CSP checks.

## Files to review
(none identified)

## Next step
Nothing to do. Re-run after each iteration to catch regressions.
