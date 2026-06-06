# 4 — Model-to-Model (ETL Transformation) — PASSED

## Summary
4 — Model-to-Model (ETL Transformation) completed successfully.

## Run history
- New this run: 4
- Recurring from previous run: 0
- Resolved since previous run: 2

**Resolved issue titles**
- State without unconditional fallback: SRangerController.Moving
- State without unconditional fallback: SRangerController.Turning

## Issues
### Issue 1: deadlock_lint_warning — State without unconditional fallback: LreController.OCM

**Raw**
```
[deadlock-lint] LreController.OCM: all outgoing transitions are guarded or event-triggered (no unconditional fallback � Isabelle deadlock_free proof will fail). Add an unconditional else-branch in the Java mode block.
```

**Fix directive**
Add an `else { mode = <SameMode>; }` clause to the inner if-else chain of this mode block so the state has at least one bare-precondition outgoing transition. Without it, the Isabelle deadlock_free proof will fail (see CLAUDE.md "Isabelle/UTP Z-Machine `deadlock_free` proof" gotcha).

### Issue 2: deadlock_lint_warning — State without unconditional fallback: LreController.MOM

**Raw**
```
[deadlock-lint] LreController.MOM: all outgoing transitions are guarded or event-triggered (no unconditional fallback � Isabelle deadlock_free proof will fail). Add an unconditional else-branch in the Java mode block.
```

**Fix directive**
Add an `else { mode = <SameMode>; }` clause to the inner if-else chain of this mode block so the state has at least one bare-precondition outgoing transition. Without it, the Isabelle deadlock_free proof will fail (see CLAUDE.md "Isabelle/UTP Z-Machine `deadlock_free` proof" gotcha).

### Issue 3: deadlock_lint_warning — State without unconditional fallback: LreController.HCM

**Raw**
```
[deadlock-lint] LreController.HCM: all outgoing transitions are guarded or event-triggered (no unconditional fallback � Isabelle deadlock_free proof will fail). Add an unconditional else-branch in the Java mode block.
```

**Fix directive**
Add an `else { mode = <SameMode>; }` clause to the inner if-else chain of this mode block so the state has at least one bare-precondition outgoing transition. Without it, the Isabelle deadlock_free proof will fail (see CLAUDE.md "Isabelle/UTP Z-Machine `deadlock_free` proof" gotcha).

### Issue 4: deadlock_lint_warning — State without unconditional fallback: LreController.CAM

**Raw**
```
[deadlock-lint] LreController.CAM: all outgoing transitions are guarded or event-triggered (no unconditional fallback � Isabelle deadlock_free proof will fail). Add an unconditional else-branch in the Java mode block.
```

**Fix directive**
Add an `else { mode = <SameMode>; }` clause to the inner if-else chain of this mode block so the state has at least one bare-precondition outgoing transition. Without it, the Isabelle deadlock_free proof will fail (see CLAUDE.md "Isabelle/UTP Z-Machine `deadlock_free` proof" gotcha).

## Files to review
(none identified)

## Next step
Proceed to Phase 5a (CSP Generation) and/or 5b (Dafny Generation).
