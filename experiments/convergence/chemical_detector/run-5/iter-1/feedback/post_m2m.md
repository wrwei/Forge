# 4 — Model-to-Model (ETL Transformation) — PASSED

## Summary
4 — Model-to-Model (ETL Transformation) completed successfully.

## Run history
- New this run: 11
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: deadlock_lint_warning — State without unconditional fallback: GasAnalysis.Reading

**Raw**
```
[deadlock-lint] GasAnalysis.Reading: all outgoing transitions are guarded or event-triggered (no unconditional fallback �� Isabelle deadlock_free proof will fail). Add an unconditional else-branch in the Java mode block.
```

**Fix directive**
Add an `else { mode = <SameMode>; }` clause to the inner if-else chain of this mode block so the state has at least one bare-precondition outgoing transition. Without it, the Isabelle deadlock_free proof will fail (see CLAUDE.md "Isabelle/UTP Z-Machine `deadlock_free` proof" gotcha).

### Issue 2: deadlock_lint_warning — State without unconditional fallback: GasAnalysis.Analysis

**Raw**
```
[deadlock-lint] GasAnalysis.Analysis: all outgoing transitions are guarded or event-triggered (no unconditional fallback �� Isabelle deadlock_free proof will fail). Add an unconditional else-branch in the Java mode block.
```

**Fix directive**
Add an `else { mode = <SameMode>; }` clause to the inner if-else chain of this mode block so the state has at least one bare-precondition outgoing transition. Without it, the Isabelle deadlock_free proof will fail (see CLAUDE.md "Isabelle/UTP Z-Machine `deadlock_free` proof" gotcha).

### Issue 3: deadlock_lint_warning — State without unconditional fallback: GasAnalysis.GasDetected

**Raw**
```
[deadlock-lint] GasAnalysis.GasDetected: all outgoing transitions are guarded or event-triggered (no unconditional fallback �� Isabelle deadlock_free proof will fail). Add an unconditional else-branch in the Java mode block.
```

**Fix directive**
Add an `else { mode = <SameMode>; }` clause to the inner if-else chain of this mode block so the state has at least one bare-precondition outgoing transition. Without it, the Isabelle deadlock_free proof will fail (see CLAUDE.md "Isabelle/UTP Z-Machine `deadlock_free` proof" gotcha).

### Issue 4: deadlock_lint_warning — State without unconditional fallback: GasAnalysis.Done

**Raw**
```
[deadlock-lint] GasAnalysis.Done: all outgoing transitions are guarded or event-triggered (no unconditional fallback �� Isabelle deadlock_free proof will fail). Add an unconditional else-branch in the Java mode block.
```

**Fix directive**
Add an `else { mode = <SameMode>; }` clause to the inner if-else chain of this mode block so the state has at least one bare-precondition outgoing transition. Without it, the Isabelle deadlock_free proof will fail (see CLAUDE.md "Isabelle/UTP Z-Machine `deadlock_free` proof" gotcha).

### Issue 5: deadlock_lint_warning — State without unconditional fallback: Movement.Waiting

**Raw**
```
[deadlock-lint] Movement.Waiting: all outgoing transitions are guarded or event-triggered (no unconditional fallback �� Isabelle deadlock_free proof will fail). Add an unconditional else-branch in the Java mode block.
```

**Fix directive**
Add an `else { mode = <SameMode>; }` clause to the inner if-else chain of this mode block so the state has at least one bare-precondition outgoing transition. Without it, the Isabelle deadlock_free proof will fail (see CLAUDE.md "Isabelle/UTP Z-Machine `deadlock_free` proof" gotcha).

### Issue 6: deadlock_lint_warning — State without unconditional fallback: Movement.Going

**Raw**
```
[deadlock-lint] Movement.Going: all outgoing transitions are guarded or event-triggered (no unconditional fallback �� Isabelle deadlock_free proof will fail). Add an unconditional else-branch in the Java mode block.
```

**Fix directive**
Add an `else { mode = <SameMode>; }` clause to the inner if-else chain of this mode block so the state has at least one bare-precondition outgoing transition. Without it, the Isabelle deadlock_free proof will fail (see CLAUDE.md "Isabelle/UTP Z-Machine `deadlock_free` proof" gotcha).

### Issue 7: deadlock_lint_warning — State without unconditional fallback: Movement.Avoiding

**Raw**
```
[deadlock-lint] Movement.Avoiding: all outgoing transitions are guarded or event-triggered (no unconditional fallback �� Isabelle deadlock_free proof will fail). Add an unconditional else-branch in the Java mode block.
```

**Fix directive**
Add an `else { mode = <SameMode>; }` clause to the inner if-else chain of this mode block so the state has at least one bare-precondition outgoing transition. Without it, the Isabelle deadlock_free proof will fail (see CLAUDE.md "Isabelle/UTP Z-Machine `deadlock_free` proof" gotcha).

### Issue 8: deadlock_lint_warning — State without unconditional fallback: Movement.TryingAgain

**Raw**
```
[deadlock-lint] Movement.TryingAgain: all outgoing transitions are guarded or event-triggered (no unconditional fallback �� Isabelle deadlock_free proof will fail). Add an unconditional else-branch in the Java mode block.
```

**Fix directive**
Add an `else { mode = <SameMode>; }` clause to the inner if-else chain of this mode block so the state has at least one bare-precondition outgoing transition. Without it, the Isabelle deadlock_free proof will fail (see CLAUDE.md "Isabelle/UTP Z-Machine `deadlock_free` proof" gotcha).

### Issue 9: deadlock_lint_warning — State without unconditional fallback: Movement.AvoidingAgain

**Raw**
```
[deadlock-lint] Movement.AvoidingAgain: all outgoing transitions are guarded or event-triggered (no unconditional fallback �� Isabelle deadlock_free proof will fail). Add an unconditional else-branch in the Java mode block.
```

**Fix directive**
Add an `else { mode = <SameMode>; }` clause to the inner if-else chain of this mode block so the state has at least one bare-precondition outgoing transition. Without it, the Isabelle deadlock_free proof will fail (see CLAUDE.md "Isabelle/UTP Z-Machine `deadlock_free` proof" gotcha).

### Issue 10: deadlock_lint_warning — State without unconditional fallback: Movement.GettingOut

**Raw**
```
[deadlock-lint] Movement.GettingOut: all outgoing transitions are guarded or event-triggered (no unconditional fallback �� Isabelle deadlock_free proof will fail). Add an unconditional else-branch in the Java mode block.
```

**Fix directive**
Add an `else { mode = <SameMode>; }` clause to the inner if-else chain of this mode block so the state has at least one bare-precondition outgoing transition. Without it, the Isabelle deadlock_free proof will fail (see CLAUDE.md "Isabelle/UTP Z-Machine `deadlock_free` proof" gotcha).

### Issue 11: deadlock_lint_warning — State without unconditional fallback: Movement.Found

**Raw**
```
[deadlock-lint] Movement.Found: all outgoing transitions are guarded or event-triggered (no unconditional fallback �� Isabelle deadlock_free proof will fail). Add an unconditional else-branch in the Java mode block.
```

**Fix directive**
Add an `else { mode = <SameMode>; }` clause to the inner if-else chain of this mode block so the state has at least one bare-precondition outgoing transition. Without it, the Isabelle deadlock_free proof will fail (see CLAUDE.md "Isabelle/UTP Z-Machine `deadlock_free` proof" gotcha).

## Files to review
(none identified)

## Next step
Proceed to Phase 5a (CSP Generation) and/or 5b (Dafny Generation).
