# 6d — Vacuity Audit (Dafny + Isabelle) — PASSED

## Summary
No blocking vacuity signals detected (syntactic D1/I1 and semantic satisfiability/reachability/constant-consistency checks). Any `0 errors` from Dafny / Isabelle / FDR is at least potentially load-bearing for the checked signals. 17 ADVISORY finding(s) recorded and NOT blocking: D2/dafny_valid_placeholder, I2/isabelle_requirement_placeholder, K1/constant_divergence_csp_ceiling, T2/transition_unfireable_checked. These follow from the deliberate narrowing of the CSP data domains that FDR4's finiteness requirement imposes, not from the Java under test; they bound what the passing FDR4 assertions are evidence about and must be reported with any verdict drawn from them.

## Run history
- New this run: 17
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: dafny_valid_placeholder — [ADVISORY] Dafny Valid() body is literally `true`; the obligations are the per-transition ensures clauses

**Raw**
```
predicate Valid() { true }  /* 38 behavioural ensures clauses active */
```

**Fix directive**
ADVISORY — no action for the codegen agent. This follows from the deliberate CSP domain narrowing (FDR4 finiteness), not from the Java. Record it with any verdict drawn from the CSP checks.

### Issue 2: isabelle_requirement_placeholder — [ADVISORY] Isabelle requirement predicate R1 is literally `True`; 99 of 199 lemmas establish or preserve it

**Raw**
```
zexpr R1 is "True"
```

**Fix directive**
ADVISORY — no action for the codegen agent. This follows from the deliberate CSP domain narrowing (FDR4 finiteness), not from the Java. Record it with any verdict drawn from the CSP checks.

### Issue 3: transition_unfireable_checked — [ADVISORY] 19 transition(s) can never fire under the checked CSP instantiation

**Raw**
```
M1ToM1: no value of `reading1()` in the checked domain [0..1] satisfies its constraints; M2ToM1: no value of `reading2()` in the checked domain [0..1] satisfies its constraints; M3ToM2: no value of `reading3()` in the checked domain [0..1] satisfies its constraints; M4ToM3: no value of `reading4()` in the checked domain [0..1] satisfies its constraints; M5ToM4: no value of `reading5()` in the checked domain [0..1] satisfies its constraints; M6ToM5: no value of `reading6()` in the checked domain [0..1] satisfies its constraints; M7ToM6: no value of `reading7()` in the checked domain [0..1] satisfies its constraints; M8ToM7: no value of `reading8()` in the checked domain [0..1] satisfies its constraints; M9ToM8: no value of `reading9()` in the checked domain [0..1] satisfies its constraints; M10ToM9: no value of `reading10()` in the checked domain [0..1] satisfies its constraints; M11ToM10: no value of `reading11()` in the checked domain [0..1] satisfies its constraints; M12ToM11: no value of `reading12()` in the checked domain [0..1] satisfies its constraints; M13ToM12: no value of `reading13()` in the checked domain [0..1] satisfies its constraints; M14ToM13: no value of `reading14()` in the checked domain [0..1] satisfies its constraints; M15ToM14: no value of `reading15()` in the checked domain [0..1] satisfies its constraints; M16ToM15: no value of `reading16()` in the checked domain [0..1] satisfies its constraints; M17ToM16: no value of `reading17()` in the checked domain [0..1] satisfies its constraints; M18ToM17: no value of `reading18()` in the checked domain [0..1] satisfies its constraints; M19ToM18: no value of `reading19()` in the checked domain [0..1] satisfies its constraints
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

### Issue 5: constant_divergence_csp_ceiling — [ADVISORY] Constant `threshold11` diverges between the Java source and generated artefact(s)

**Raw**
```
Java (forge.transformations/output/constant_defaults.json): 3.25; forge.transformations/output/robochart_controller.rct: 4; forge.transformations/output/csp-gen/file_robochart_controller_coreassertions.csp: 4; forge.transformations/output/csp-gen/timed/file_robochart_controller_coreassertions.csp: 4
```

**Fix directive**
ADVISORY — no action for the codegen agent. This follows from the deliberate CSP domain narrowing (FDR4 finiteness), not from the Java. Record it with any verdict drawn from the CSP checks.

### Issue 6: constant_divergence_csp_ceiling — [ADVISORY] Constant `threshold12` diverges between the Java source and generated artefact(s)

**Raw**
```
Java (forge.transformations/output/constant_defaults.json): 3.5; forge.transformations/output/robochart_controller.rct: 4; forge.transformations/output/csp-gen/file_robochart_controller_coreassertions.csp: 4; forge.transformations/output/csp-gen/timed/file_robochart_controller_coreassertions.csp: 4
```

**Fix directive**
ADVISORY — no action for the codegen agent. This follows from the deliberate CSP domain narrowing (FDR4 finiteness), not from the Java. Record it with any verdict drawn from the CSP checks.

### Issue 7: constant_divergence_csp_ceiling — [ADVISORY] Constant `threshold13` diverges between the Java source and generated artefact(s)

**Raw**
```
Java (forge.transformations/output/constant_defaults.json): 3.75; forge.transformations/output/robochart_controller.rct: 4; forge.transformations/output/csp-gen/file_robochart_controller_coreassertions.csp: 4; forge.transformations/output/csp-gen/timed/file_robochart_controller_coreassertions.csp: 4
```

**Fix directive**
ADVISORY — no action for the codegen agent. This follows from the deliberate CSP domain narrowing (FDR4 finiteness), not from the Java. Record it with any verdict drawn from the CSP checks.

### Issue 8: constant_divergence_csp_ceiling — [ADVISORY] Constant `threshold15` diverges between the Java source and generated artefact(s)

**Raw**
```
Java (forge.transformations/output/constant_defaults.json): 4.25; forge.transformations/output/robochart_controller.rct: 5; forge.transformations/output/csp-gen/file_robochart_controller_coreassertions.csp: 5; forge.transformations/output/csp-gen/timed/file_robochart_controller_coreassertions.csp: 5
```

**Fix directive**
ADVISORY — no action for the codegen agent. This follows from the deliberate CSP domain narrowing (FDR4 finiteness), not from the Java. Record it with any verdict drawn from the CSP checks.

### Issue 9: constant_divergence_csp_ceiling — [ADVISORY] Constant `threshold16` diverges between the Java source and generated artefact(s)

**Raw**
```
Java (forge.transformations/output/constant_defaults.json): 4.5; forge.transformations/output/robochart_controller.rct: 5; forge.transformations/output/csp-gen/file_robochart_controller_coreassertions.csp: 5; forge.transformations/output/csp-gen/timed/file_robochart_controller_coreassertions.csp: 5
```

**Fix directive**
ADVISORY — no action for the codegen agent. This follows from the deliberate CSP domain narrowing (FDR4 finiteness), not from the Java. Record it with any verdict drawn from the CSP checks.

### Issue 10: constant_divergence_csp_ceiling — [ADVISORY] Constant `threshold17` diverges between the Java source and generated artefact(s)

**Raw**
```
Java (forge.transformations/output/constant_defaults.json): 4.75; forge.transformations/output/robochart_controller.rct: 5; forge.transformations/output/csp-gen/file_robochart_controller_coreassertions.csp: 5; forge.transformations/output/csp-gen/timed/file_robochart_controller_coreassertions.csp: 5
```

**Fix directive**
ADVISORY — no action for the codegen agent. This follows from the deliberate CSP domain narrowing (FDR4 finiteness), not from the Java. Record it with any verdict drawn from the CSP checks.

### Issue 11: constant_divergence_csp_ceiling — [ADVISORY] Constant `threshold19` diverges between the Java source and generated artefact(s)

**Raw**
```
Java (forge.transformations/output/constant_defaults.json): 5.25; forge.transformations/output/robochart_controller.rct: 6; forge.transformations/output/csp-gen/file_robochart_controller_coreassertions.csp: 6; forge.transformations/output/csp-gen/timed/file_robochart_controller_coreassertions.csp: 6
```

**Fix directive**
ADVISORY — no action for the codegen agent. This follows from the deliberate CSP domain narrowing (FDR4 finiteness), not from the Java. Record it with any verdict drawn from the CSP checks.

### Issue 12: constant_divergence_csp_ceiling — [ADVISORY] Constant `threshold3` diverges between the Java source and generated artefact(s)

**Raw**
```
Java (forge.transformations/output/constant_defaults.json): 1.25; forge.transformations/output/robochart_controller.rct: 2; forge.transformations/output/csp-gen/file_robochart_controller_coreassertions.csp: 2; forge.transformations/output/csp-gen/timed/file_robochart_controller_coreassertions.csp: 2
```

**Fix directive**
ADVISORY — no action for the codegen agent. This follows from the deliberate CSP domain narrowing (FDR4 finiteness), not from the Java. Record it with any verdict drawn from the CSP checks.

### Issue 13: constant_divergence_csp_ceiling — [ADVISORY] Constant `threshold4` diverges between the Java source and generated artefact(s)

**Raw**
```
Java (forge.transformations/output/constant_defaults.json): 1.5; forge.transformations/output/robochart_controller.rct: 2; forge.transformations/output/csp-gen/file_robochart_controller_coreassertions.csp: 2; forge.transformations/output/csp-gen/timed/file_robochart_controller_coreassertions.csp: 2
```

**Fix directive**
ADVISORY — no action for the codegen agent. This follows from the deliberate CSP domain narrowing (FDR4 finiteness), not from the Java. Record it with any verdict drawn from the CSP checks.

### Issue 14: constant_divergence_csp_ceiling — [ADVISORY] Constant `threshold5` diverges between the Java source and generated artefact(s)

**Raw**
```
Java (forge.transformations/output/constant_defaults.json): 1.75; forge.transformations/output/robochart_controller.rct: 2; forge.transformations/output/csp-gen/file_robochart_controller_coreassertions.csp: 2; forge.transformations/output/csp-gen/timed/file_robochart_controller_coreassertions.csp: 2
```

**Fix directive**
ADVISORY — no action for the codegen agent. This follows from the deliberate CSP domain narrowing (FDR4 finiteness), not from the Java. Record it with any verdict drawn from the CSP checks.

### Issue 15: constant_divergence_csp_ceiling — [ADVISORY] Constant `threshold7` diverges between the Java source and generated artefact(s)

**Raw**
```
Java (forge.transformations/output/constant_defaults.json): 2.25; forge.transformations/output/robochart_controller.rct: 3; forge.transformations/output/csp-gen/file_robochart_controller_coreassertions.csp: 3; forge.transformations/output/csp-gen/timed/file_robochart_controller_coreassertions.csp: 3
```

**Fix directive**
ADVISORY — no action for the codegen agent. This follows from the deliberate CSP domain narrowing (FDR4 finiteness), not from the Java. Record it with any verdict drawn from the CSP checks.

### Issue 16: constant_divergence_csp_ceiling — [ADVISORY] Constant `threshold8` diverges between the Java source and generated artefact(s)

**Raw**
```
Java (forge.transformations/output/constant_defaults.json): 2.5; forge.transformations/output/robochart_controller.rct: 3; forge.transformations/output/csp-gen/file_robochart_controller_coreassertions.csp: 3; forge.transformations/output/csp-gen/timed/file_robochart_controller_coreassertions.csp: 3
```

**Fix directive**
ADVISORY — no action for the codegen agent. This follows from the deliberate CSP domain narrowing (FDR4 finiteness), not from the Java. Record it with any verdict drawn from the CSP checks.

### Issue 17: constant_divergence_csp_ceiling — [ADVISORY] Constant `threshold9` diverges between the Java source and generated artefact(s)

**Raw**
```
Java (forge.transformations/output/constant_defaults.json): 2.75; forge.transformations/output/robochart_controller.rct: 3; forge.transformations/output/csp-gen/file_robochart_controller_coreassertions.csp: 3; forge.transformations/output/csp-gen/timed/file_robochart_controller_coreassertions.csp: 3
```

**Fix directive**
ADVISORY — no action for the codegen agent. This follows from the deliberate CSP domain narrowing (FDR4 finiteness), not from the Java. Record it with any verdict drawn from the CSP checks.

## Files to review
(none identified)

## Next step
Nothing to do. Re-run after each iteration to catch regressions.
