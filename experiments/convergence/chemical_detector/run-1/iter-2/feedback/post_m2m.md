# 4 — Model-to-Model (ETL Transformation) — PASSED

## Summary
4 — Model-to-Model (ETL Transformation) completed successfully.

## Run history
- New this run: 0
- Recurring from previous run: 10
- Resolved since previous run: 0

## Issues
### Issue 1: deadlock_lint_warning — State without unconditional fallback: GasAnalysisController.Reading [recurring x2]

**Raw**
```
[deadlock-lint] GasAnalysisController.Reading: all outgoing transitions are guarded or event-triggered (no unconditional fallback �� Isabelle deadlock_free proof will fail). Add an unconditional else-branch in the Java mode block.
```

**Fix directive**
Add an `else { mode = <SameMode>; }` clause to the inner if-else chain of this mode block so the state has at least one bare-precondition outgoing transition. Without it, the Isabelle deadlock_free proof will fail (see CLAUDE.md "Isabelle/UTP Z-Machine `deadlock_free` proof" gotcha).

### Issue 2: deadlock_lint_warning — State without unconditional fallback: GasAnalysisController.Analysis [recurring x2]

**Raw**
```
[deadlock-lint] GasAnalysisController.Analysis: all outgoing transitions are guarded or event-triggered (no unconditional fallback �� Isabelle deadlock_free proof will fail). Add an unconditional else-branch in the Java mode block.
```

**Fix directive**
Add an `else { mode = <SameMode>; }` clause to the inner if-else chain of this mode block so the state has at least one bare-precondition outgoing transition. Without it, the Isabelle deadlock_free proof will fail (see CLAUDE.md "Isabelle/UTP Z-Machine `deadlock_free` proof" gotcha).

### Issue 3: deadlock_lint_warning — State without unconditional fallback: GasAnalysisController.GasDetected [recurring x2]

**Raw**
```
[deadlock-lint] GasAnalysisController.GasDetected: all outgoing transitions are guarded or event-triggered (no unconditional fallback �� Isabelle deadlock_free proof will fail). Add an unconditional else-branch in the Java mode block.
```

**Fix directive**
Add an `else { mode = <SameMode>; }` clause to the inner if-else chain of this mode block so the state has at least one bare-precondition outgoing transition. Without it, the Isabelle deadlock_free proof will fail (see CLAUDE.md "Isabelle/UTP Z-Machine `deadlock_free` proof" gotcha).

### Issue 4: deadlock_lint_warning — State without unconditional fallback: MovementController.Waiting [recurring x2]

**Raw**
```
[deadlock-lint] MovementController.Waiting: all outgoing transitions are guarded or event-triggered (no unconditional fallback �� Isabelle deadlock_free proof will fail). Add an unconditional else-branch in the Java mode block.
```

**Fix directive**
Add an `else { mode = <SameMode>; }` clause to the inner if-else chain of this mode block so the state has at least one bare-precondition outgoing transition. Without it, the Isabelle deadlock_free proof will fail (see CLAUDE.md "Isabelle/UTP Z-Machine `deadlock_free` proof" gotcha).

### Issue 5: deadlock_lint_warning — State without unconditional fallback: MovementController.Going [recurring x2]

**Raw**
```
[deadlock-lint] MovementController.Going: all outgoing transitions are guarded or event-triggered (no unconditional fallback �� Isabelle deadlock_free proof will fail). Add an unconditional else-branch in the Java mode block.
```

**Fix directive**
Add an `else { mode = <SameMode>; }` clause to the inner if-else chain of this mode block so the state has at least one bare-precondition outgoing transition. Without it, the Isabelle deadlock_free proof will fail (see CLAUDE.md "Isabelle/UTP Z-Machine `deadlock_free` proof" gotcha).

### Issue 6: deadlock_lint_warning — State without unconditional fallback: MovementController.Avoiding [recurring x2]

**Raw**
```
[deadlock-lint] MovementController.Avoiding: all outgoing transitions are guarded or event-triggered (no unconditional fallback �� Isabelle deadlock_free proof will fail). Add an unconditional else-branch in the Java mode block.
```

**Fix directive**
Add an `else { mode = <SameMode>; }` clause to the inner if-else chain of this mode block so the state has at least one bare-precondition outgoing transition. Without it, the Isabelle deadlock_free proof will fail (see CLAUDE.md "Isabelle/UTP Z-Machine `deadlock_free` proof" gotcha).

### Issue 7: deadlock_lint_warning — State without unconditional fallback: MovementController.TryingAgain [recurring x2]

**Raw**
```
[deadlock-lint] MovementController.TryingAgain: all outgoing transitions are guarded or event-triggered (no unconditional fallback �� Isabelle deadlock_free proof will fail). Add an unconditional else-branch in the Java mode block.
```

**Fix directive**
Add an `else { mode = <SameMode>; }` clause to the inner if-else chain of this mode block so the state has at least one bare-precondition outgoing transition. Without it, the Isabelle deadlock_free proof will fail (see CLAUDE.md "Isabelle/UTP Z-Machine `deadlock_free` proof" gotcha).

### Issue 8: deadlock_lint_warning — State without unconditional fallback: MovementController.AvoidingAgain [recurring x2]

**Raw**
```
[deadlock-lint] MovementController.AvoidingAgain: all outgoing transitions are guarded or event-triggered (no unconditional fallback �� Isabelle deadlock_free proof will fail). Add an unconditional else-branch in the Java mode block.
```

**Fix directive**
Add an `else { mode = <SameMode>; }` clause to the inner if-else chain of this mode block so the state has at least one bare-precondition outgoing transition. Without it, the Isabelle deadlock_free proof will fail (see CLAUDE.md "Isabelle/UTP Z-Machine `deadlock_free` proof" gotcha).

### Issue 9: deadlock_lint_warning — State without unconditional fallback: MovementController.GettingOut [recurring x2]

**Raw**
```
[deadlock-lint] MovementController.GettingOut: all outgoing transitions are guarded or event-triggered (no unconditional fallback �� Isabelle deadlock_free proof will fail). Add an unconditional else-branch in the Java mode block.
```

**Fix directive**
Add an `else { mode = <SameMode>; }` clause to the inner if-else chain of this mode block so the state has at least one bare-precondition outgoing transition. Without it, the Isabelle deadlock_free proof will fail (see CLAUDE.md "Isabelle/UTP Z-Machine `deadlock_free` proof" gotcha).

### Issue 10: deadlock_lint_warning — State without unconditional fallback: MovementController.Found [recurring x2]

**Raw**
```
[deadlock-lint] MovementController.Found: all outgoing transitions are guarded or event-triggered (no unconditional fallback �� Isabelle deadlock_free proof will fail). Add an unconditional else-branch in the Java mode block.
```

**Fix directive**
Add an `else { mode = <SameMode>; }` clause to the inner if-else chain of this mode block so the state has at least one bare-precondition outgoing transition. Without it, the Isabelle deadlock_free proof will fail (see CLAUDE.md "Isabelle/UTP Z-Machine `deadlock_free` proof" gotcha).

## Files to review
(none identified)

## Next step
Proceed to Phase 5a (CSP Generation) and/or 5b (Dafny Generation).
