# 6d — Vacuity Audit (Dafny + Isabelle) — FAILED

## Summary
1 blocking vacuity signal(s) detected (plus 4 advisory). The corresponding verifier passes discharge `True` obligations and do not establish behavioural content.

## Run history
- New this run: 5
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: mode_unreachable_checked — [ADVISORY] Mode(s) unreachable under the CHECKED CSP instantiation: CAM, HCM, MOM

**Raw**
```
checked-reachable = {OCM, initial} (narrowed ranges + constant stubs from instantiations.csp)
```

**Fix directive**
ADVISORY — no action for the codegen agent. This follows from the deliberate CSP domain narrowing (FDR4 finiteness), not from the Java. Record it with any verdict drawn from the CSP checks.

### Issue 2: transition_unfireable_checked — [ADVISORY] 15 transition(s) can never fire under the checked CSP instantiation

**Raw**
```
OCMToMOM: `odist(cdyn)>minSafeDist()` evaluates to 0 > 1 = False; MOMToOCM: source mode MOM is unreachable; MOMToOCM_1: source mode MOM is unreachable; MOMToHCM: source mode MOM is unreachable; MOMToOCM_2: source mode MOM is unreachable; MOMToCAM: source mode MOM is unreachable; MOMToHCM_1: source mode MOM is unreachable; MOMToHCM_2: source mode MOM is unreachable; MOMToHCM_3: source mode MOM is unreachable; HCMToOCM: source mode HCM is unreachable; HCMToOCM_1: source mode HCM is unreachable; HCMToCAM: source mode HCM is unreachable; HCMToMOM: source mode HCM is unreachable; CAMToOCM: source mode CAM is unreachable; CAMToOCM_1: source mode CAM is unreachable
```

**Fix directive**
ADVISORY — no action for the codegen agent. This follows from the deliberate CSP domain narrowing (FDR4 finiteness), not from the Java. Record it with any verdict drawn from the CSP checks.

### Issue 3: dead_state_offered_pair_checked — [ADVISORY] 1 (state, offered) pair(s) dead only under the checked CSP instantiation

**Raw**
```
(OCM, {reqMOM})
```

**Fix directive**
ADVISORY — no action for the codegen agent. This follows from the deliberate CSP domain narrowing (FDR4 finiteness), not from the Java. Record it with any verdict drawn from the CSP checks.

### Issue 4: dead_state_no_offer — [ADVISORY] 1 reachable state(s) from which no operation can fire when the environment offers nothing

**Raw**
```
(OCM, {})
```

**Fix directive**
ADVISORY — no action for the codegen agent. This follows from the deliberate CSP domain narrowing (FDR4 finiteness), not from the Java. Record it with any verdict drawn from the CSP checks.

### Issue 5: constant_divergence — [BLOCKING] Constant `minrelspeedsq` diverges between the Java source and generated artefact(s)

**Raw**
```
Java (forge.transformations/output/constant_defaults.json): 1e-06; forge.transformations/output/LreController.dfy: 1
```

**Fix directive**
At least one backend verified a system with a different constant than the Java implements, and the divergence is not the declared CSP float ceiling. Fix the constant emission.

## Files to review
(none identified)

## Next step
Strengthen the flagged template(s) so the verifier discharges behavioural obligations rather than `True`. Until that is done, do not count the corresponding verifier pass as evidence of behavioural correctness in the experiment write-up.
