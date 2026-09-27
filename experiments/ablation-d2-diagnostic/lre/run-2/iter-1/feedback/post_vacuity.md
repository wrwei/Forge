# 6d — Vacuity Audit (Dafny + Isabelle) — FAILED

## Summary
1 blocking vacuity signal(s) detected (plus 1 advisory). The corresponding verifier passes discharge `True` obligations and do not establish behavioural content.

## Run history
- New this run: 2
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: dead_state_no_offer — [ADVISORY] 1 reachable state(s) from which no operation can fire when the environment offers nothing

**Raw**
```
(OCM, {})
```

**Fix directive**
ADVISORY — no action for the codegen agent. This follows from the deliberate CSP domain narrowing (FDR4 finiteness), not from the Java. Record it with any verdict drawn from the CSP checks.

### Issue 2: constant_divergence — [BLOCKING] Constant `minrelspeedsq` diverges between the Java source and generated artefact(s)

**Raw**
```
Java (forge.transformations/output/constant_defaults.json): 1e-09; forge.transformations/output/LreController.dfy: 1
```

**Fix directive**
At least one backend verified a system with a different constant than the Java implements, and the divergence is not the declared CSP float ceiling. Fix the constant emission.

## Files to review
(none identified)

## Next step
Strengthen the flagged template(s) so the verifier discharges behavioural obligations rather than `True`. Until that is done, do not count the corresponding verifier pass as evidence of behavioural correctness in the experiment write-up.
