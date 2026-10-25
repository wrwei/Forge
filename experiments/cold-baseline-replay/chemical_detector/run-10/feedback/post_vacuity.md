# 6d — Vacuity Audit (Dafny + Isabelle) — FAILED

## Summary
2 blocking vacuity signal(s) detected (plus 1 advisory). The corresponding verifier passes discharge `True` obligations and do not establish behavioural content.

## Run history
- New this run: 3
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: dafny_valid_vacuous — [BLOCKING] Dafny Valid() body is literally `true` (or empty) and no other behavioural ensures clauses are active

**Raw**
```
ghost predicate Valid()
        reads this
    {
        true  // Extend with domain invariants
    }
```

**Fix directive**
Every `ensures Valid()` clause in the generated Dafny is equivalent to `ensures true` while Valid() returns true, AND no per-transition behavioural `ensures` clauses are active (the template emits them as comments). The Dafny verifier therefore discharges only `ensures true` obligations — `0 errors` does not establish behavioural correctness, only well-formedness.

To make Dafny verification load-bearing, the M2T-Dafny template (forge.transformations/src/main/resources/transformations/java2dafny.egl) must either: (a) derive a non-trivial Valid() body from the requirements, or (b) emit per-transition `ensures` clauses as ACTIVE obligations rather than comments. (b) is the more direct route to behavioural verification.

### Issue 2: idle_unreachable — [ADVISORY] Idle operation Idle_Analysis is unreachable: the mode's branches are exhaustive

**Raw**
```
pre "st= Analysis \<and> \<not>(sts= (noGas)) \<and> \<not>(sts= (gasD))"  -- offered={}: UNSAT (enum lens `sts` has no admissible value left (last atom: `! (sts= (gasD))`))
```

**Fix directive**
ADVISORY — no action for the codegen agent. This follows from the deliberate CSP domain narrowing (FDR4 finiteness), not from the Java. Record it with any verdict drawn from the CSP checks.

### Issue 3: mode_unreachable — [BLOCKING] Mode(s) unreachable from Init in the symbolic model: Avoiding, AvoidingAgain, Final, Found, GettingOut, Going, TryingAgain

**Raw**
```
Init st = initial; reachable = {Waiting, initial}
```

**Fix directive**
No sequence of operations reaches these modes even with unnarrowed numeric domains and uninterpreted functions. Any invariant lemma about them is vacuous. Check the transition extraction for the missing edges.

## Files to review
(none identified)

## Next step
Strengthen the flagged template(s) so the verifier discharges behavioural obligations rather than `True`. Until that is done, do not count the corresponding verifier pass as evidence of behavioural correctness in the experiment write-up.
