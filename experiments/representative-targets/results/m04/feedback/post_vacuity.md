# 6d — Vacuity Audit (Dafny + Isabelle) — PASSED

## Summary
No blocking vacuity signals detected (syntactic D1/I1 and semantic satisfiability/reachability/constant-consistency checks). Any `0 errors` from Dafny / Isabelle / FDR is at least potentially load-bearing for the checked signals. 5 ADVISORY finding(s) recorded and NOT blocking: D2/dafny_valid_placeholder, I2/isabelle_requirement_placeholder, K1/constant_divergence_csp_ceiling, T2/transition_unfireable_checked. These follow from the deliberate narrowing of the CSP data domains that FDR4's finiteness requirement imposes, not from the Java under test; they bound what the passing FDR4 assertions are evidence about and must be reported with any verdict drawn from them.

## Run history
- New this run: 5
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: dafny_valid_placeholder — [ADVISORY] Dafny Valid() body is literally `true`; the obligations are the per-transition ensures clauses

**Raw**
```
predicate Valid() { true }  /* 6 behavioural ensures clauses active */
```

**Fix directive**
ADVISORY — no action for the codegen agent. This follows from the deliberate CSP domain narrowing (FDR4 finiteness), not from the Java. Record it with any verdict drawn from the CSP checks.

### Issue 2: isabelle_requirement_placeholder — [ADVISORY] Isabelle requirement predicate R1 is literally `True`; 19 of 39 lemmas establish or preserve it

**Raw**
```
zexpr R1 is "True"
```

**Fix directive**
ADVISORY — no action for the codegen agent. This follows from the deliberate CSP domain narrowing (FDR4 finiteness), not from the Java. Record it with any verdict drawn from the CSP checks.

### Issue 3: transition_unfireable_checked — [ADVISORY] 3 transition(s) can never fire under the checked CSP instantiation

**Raw**
```
M1ToM1: no value of `reading1()` in the checked domain [0..1] satisfies its constraints; M2ToM1: no value of `reading2()` in the checked domain [0..1] satisfies its constraints; M3ToM2: no value of `reading3()` in the checked domain [0..1] satisfies its constraints
```

**Fix directive**
ADVISORY — no action for the codegen agent. This follows from the deliberate CSP domain narrowing (FDR4 finiteness), not from the Java. Record it with any verdict drawn from the CSP checks.

### Issue 4: constant_divergence_csp_ceiling — [ADVISORY] Constant `threshold1` diverges between the Java source and generated artefact(s)

**Raw**
```
Java (forge.transformations/output/constant_defaults.json): 0.75; forge.transformations/output/robochart_controller.rct: 1; forge.transformations/output/csp-gen/file_robochart_controller_coreassertions.csp: 1; forge.transformations/output/csp-gen/timed/file_robochart_controller_coreassertions.csp: 1
```

**Fix directive**
ADVISORY — no action for the codegen agent. This follows from the deliberate CSP domain narrowing (FDR4 finiteness), not from the Java. Record it with any verdict drawn from the CSP checks.

### Issue 5: constant_divergence_csp_ceiling — [ADVISORY] Constant `threshold3` diverges between the Java source and generated artefact(s)

**Raw**
```
Java (forge.transformations/output/constant_defaults.json): 1.25; forge.transformations/output/robochart_controller.rct: 2; forge.transformations/output/csp-gen/file_robochart_controller_coreassertions.csp: 2; forge.transformations/output/csp-gen/timed/file_robochart_controller_coreassertions.csp: 2
```

**Fix directive**
ADVISORY — no action for the codegen agent. This follows from the deliberate CSP domain narrowing (FDR4 finiteness), not from the Java. Record it with any verdict drawn from the CSP checks.

## Files to review
(none identified)

## Next step
Nothing to do. Re-run after each iteration to catch regressions.
