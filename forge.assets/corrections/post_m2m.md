# 4 — Model-to-Model (ETL Transformation) — PASSED

## Summary
4 — Model-to-Model (ETL Transformation) completed successfully.

## Run history
**WARNING — thrashing detected.** 6 issue(s) have recurred 3+ runs in a row despite fixes: `State without unconditional fallback: GasAnalysisController.Reading`, `State without unconditional fallback: MovementController.Waiting`, `State without unconditional fallback: MovementController.Going`, `State without unconditional fallback: MovementController.Avoiding`, `State without unconditional fallback: MovementController.TryingAgain`, `State without unconditional fallback: MovementController.GettingOut`. Previous fix strategies are not working — change approach (e.g. re-read the originating requirement, or escalate to the user).

- New this run: 0
- Recurring from previous run: 6
- Resolved since previous run: 0

## Issues
### Issue 1: deadlock_lint_warning — State without unconditional fallback: GasAnalysisController.Reading [RECURRING x18 — fix strategy failing]

**Raw**
```
[deadlock-lint] GasAnalysisController.Reading: all outgoing transitions are guarded or event-triggered (no unconditional fallback � Isabelle deadlock_free proof will fail). Add an unconditional else-branch in the Java mode block.
```

**Fix directive**
Add an `else { mode = <SameMode>; }` clause to the inner if-else chain of this mode block so the state has at least one bare-precondition outgoing transition. Without it, the Isabelle deadlock_free proof will fail (see CLAUDE.md "Isabelle/UTP Z-Machine `deadlock_free` proof" gotcha).

### Issue 2: deadlock_lint_warning — State without unconditional fallback: MovementController.Waiting [RECURRING x18 — fix strategy failing]

**Raw**
```
[deadlock-lint] MovementController.Waiting: all outgoing transitions are guarded or event-triggered (no unconditional fallback � Isabelle deadlock_free proof will fail). Add an unconditional else-branch in the Java mode block.
```

**Fix directive**
Add an `else { mode = <SameMode>; }` clause to the inner if-else chain of this mode block so the state has at least one bare-precondition outgoing transition. Without it, the Isabelle deadlock_free proof will fail (see CLAUDE.md "Isabelle/UTP Z-Machine `deadlock_free` proof" gotcha).

### Issue 3: deadlock_lint_warning — State without unconditional fallback: MovementController.Going [RECURRING x18 — fix strategy failing]

**Raw**
```
[deadlock-lint] MovementController.Going: all outgoing transitions are guarded or event-triggered (no unconditional fallback � Isabelle deadlock_free proof will fail). Add an unconditional else-branch in the Java mode block.
```

**Fix directive**
Add an `else { mode = <SameMode>; }` clause to the inner if-else chain of this mode block so the state has at least one bare-precondition outgoing transition. Without it, the Isabelle deadlock_free proof will fail (see CLAUDE.md "Isabelle/UTP Z-Machine `deadlock_free` proof" gotcha).

### Issue 4: deadlock_lint_warning — State without unconditional fallback: MovementController.Avoiding [RECURRING x18 — fix strategy failing]

**Raw**
```
[deadlock-lint] MovementController.Avoiding: all outgoing transitions are guarded or event-triggered (no unconditional fallback � Isabelle deadlock_free proof will fail). Add an unconditional else-branch in the Java mode block.
```

**Fix directive**
Add an `else { mode = <SameMode>; }` clause to the inner if-else chain of this mode block so the state has at least one bare-precondition outgoing transition. Without it, the Isabelle deadlock_free proof will fail (see CLAUDE.md "Isabelle/UTP Z-Machine `deadlock_free` proof" gotcha).

### Issue 5: deadlock_lint_warning — State without unconditional fallback: MovementController.TryingAgain [RECURRING x18 — fix strategy failing]

**Raw**
```
[deadlock-lint] MovementController.TryingAgain: all outgoing transitions are guarded or event-triggered (no unconditional fallback � Isabelle deadlock_free proof will fail). Add an unconditional else-branch in the Java mode block.
```

**Fix directive**
Add an `else { mode = <SameMode>; }` clause to the inner if-else chain of this mode block so the state has at least one bare-precondition outgoing transition. Without it, the Isabelle deadlock_free proof will fail (see CLAUDE.md "Isabelle/UTP Z-Machine `deadlock_free` proof" gotcha).

### Issue 6: deadlock_lint_warning — State without unconditional fallback: MovementController.GettingOut [RECURRING x18 — fix strategy failing]

**Raw**
```
[deadlock-lint] MovementController.GettingOut: all outgoing transitions are guarded or event-triggered (no unconditional fallback � Isabelle deadlock_free proof will fail). Add an unconditional else-branch in the Java mode block.
```

**Fix directive**
Add an `else { mode = <SameMode>; }` clause to the inner if-else chain of this mode block so the state has at least one bare-precondition outgoing transition. Without it, the Isabelle deadlock_free proof will fail (see CLAUDE.md "Isabelle/UTP Z-Machine `deadlock_free` proof" gotcha).

## Files to review
(none identified)

## Next step
Proceed to Phase 5a (CSP Generation) and/or 5b (Dafny Generation).
