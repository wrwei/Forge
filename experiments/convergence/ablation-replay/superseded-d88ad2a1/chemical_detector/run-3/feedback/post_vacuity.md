# 6d — Vacuity Audit (Dafny + Isabelle) — FAILED

## Summary
1 blocking vacuity signal(s) detected (plus 1 advisory). The corresponding verifier passes discharge `True` obligations and do not establish behavioural content.

## Run history
- New this run: 2
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: precondition_unsat — [BLOCKING] Precondition of zoperation Idle_Analysis is unsatisfiable

**Raw**
```
pre "st= Analysis \<and> \<not>(sts= (gasD)) \<and> \<not>(sts= (noGas))"  -- enum lens `sts` has no admissible value left (last atom: `! (sts= (noGas))`)
```

**Fix directive**
The precondition of `Idle_Analysis` can never hold (enum lens `sts` has no admissible value left (last atom: `! (sts= (noGas))`)), so every proof obligation about this operation is vacuously true and the operation can never fire. The extractor prepends the negations of every EARLIER branch guard in the same mode (faithful else-if semantics), so a contradiction here almost always means the SOURCE JAVA TRANSITION IS DEAD CODE: an earlier branch in the same mode has a guard that subsumes or equals part of this branch's guard, so this branch can never be reached. FIX IN THE JAVA: find this operation's source branch via the traceability entry, compare its guard with the earlier branches in the same mode, and reorder the branches (most-specific first) or merge them per the requirements' intent. Do NOT touch any .egl template. Only if the named atoms are NOT equivalent in the Java (i.e. the contradiction was introduced by extraction, not by branch ordering) is this an instrument issue — report it to the operator instead of editing anything.

### Issue 2: idle_unreachable — [ADVISORY] Idle operation Idle_Analysis is unreachable: the mode's branches are exhaustive

**Raw**
```
pre "st= Analysis \<and> \<not>(sts= (gasD)) \<and> \<not>(sts= (noGas))"  -- offered={}: UNSAT (enum lens `sts` has no admissible value left (last atom: `! (sts= (noGas))`))
```

**Fix directive**
ADVISORY — no action for the codegen agent. This follows from the deliberate CSP domain narrowing (FDR4 finiteness), not from the Java. Record it with any verdict drawn from the CSP checks.

## Files to review
(none identified)

## Next step
Strengthen the flagged template(s) so the verifier discharges behavioural obligations rather than `True`. Until that is done, do not count the corresponding verifier pass as evidence of behavioural correctness in the experiment write-up.
