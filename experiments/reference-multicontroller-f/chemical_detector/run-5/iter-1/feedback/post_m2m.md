# 4 — Model-to-Model (ETL Transformation) — PASSED

## Summary
4 — Model-to-Model (ETL Transformation) completed successfully.

## Run history
- New this run: 11
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: deadlock_lint_warning — State without unconditional fallback: GasAnalysis.READING

**Raw**
```
[deadlock-lint] GasAnalysis.READING: all outgoing transitions are guarded or event-triggered (no unconditional fallback — Isabelle deadlock_free proof will fail). Add an unconditional else-branch in the Java mode block.
```

**Fix directive**
Add an `else { mode = <SameMode>; }` clause to the inner if-else chain of this mode block so the state has at least one bare-precondition outgoing transition. Without it, the Isabelle deadlock_free proof will fail (see CLAUDE.md "Isabelle/UTP Z-Machine `deadlock_free` proof" gotcha).

### Issue 2: deadlock_lint_warning — State without unconditional fallback: GasAnalysis.ANALYSIS

**Raw**
```
[deadlock-lint] GasAnalysis.ANALYSIS: all outgoing transitions are guarded or event-triggered (no unconditional fallback — Isabelle deadlock_free proof will fail). Add an unconditional else-branch in the Java mode block.
```

**Fix directive**
Add an `else { mode = <SameMode>; }` clause to the inner if-else chain of this mode block so the state has at least one bare-precondition outgoing transition. Without it, the Isabelle deadlock_free proof will fail (see CLAUDE.md "Isabelle/UTP Z-Machine `deadlock_free` proof" gotcha).

### Issue 3: deadlock_lint_warning — State without unconditional fallback: GasAnalysis.GAS_DETECTED

**Raw**
```
[deadlock-lint] GasAnalysis.GAS_DETECTED: all outgoing transitions are guarded or event-triggered (no unconditional fallback — Isabelle deadlock_free proof will fail). Add an unconditional else-branch in the Java mode block.
```

**Fix directive**
Add an `else { mode = <SameMode>; }` clause to the inner if-else chain of this mode block so the state has at least one bare-precondition outgoing transition. Without it, the Isabelle deadlock_free proof will fail (see CLAUDE.md "Isabelle/UTP Z-Machine `deadlock_free` proof" gotcha).

### Issue 4: deadlock_lint_warning — State without unconditional fallback: GasAnalysis.CONCLUDED

**Raw**
```
[deadlock-lint] GasAnalysis.CONCLUDED: all outgoing transitions are guarded or event-triggered (no unconditional fallback — Isabelle deadlock_free proof will fail). Add an unconditional else-branch in the Java mode block.
```

**Fix directive**
Add an `else { mode = <SameMode>; }` clause to the inner if-else chain of this mode block so the state has at least one bare-precondition outgoing transition. Without it, the Isabelle deadlock_free proof will fail (see CLAUDE.md "Isabelle/UTP Z-Machine `deadlock_free` proof" gotcha).

### Issue 5: deadlock_lint_warning — State without unconditional fallback: Movement.WAITING

**Raw**
```
[deadlock-lint] Movement.WAITING: all outgoing transitions are guarded or event-triggered (no unconditional fallback — Isabelle deadlock_free proof will fail). Add an unconditional else-branch in the Java mode block.
```

**Fix directive**
Add an `else { mode = <SameMode>; }` clause to the inner if-else chain of this mode block so the state has at least one bare-precondition outgoing transition. Without it, the Isabelle deadlock_free proof will fail (see CLAUDE.md "Isabelle/UTP Z-Machine `deadlock_free` proof" gotcha).

### Issue 6: deadlock_lint_warning — State without unconditional fallback: Movement.GOING

**Raw**
```
[deadlock-lint] Movement.GOING: all outgoing transitions are guarded or event-triggered (no unconditional fallback — Isabelle deadlock_free proof will fail). Add an unconditional else-branch in the Java mode block.
```

**Fix directive**
Add an `else { mode = <SameMode>; }` clause to the inner if-else chain of this mode block so the state has at least one bare-precondition outgoing transition. Without it, the Isabelle deadlock_free proof will fail (see CLAUDE.md "Isabelle/UTP Z-Machine `deadlock_free` proof" gotcha).

### Issue 7: deadlock_lint_warning — State without unconditional fallback: Movement.FOUND

**Raw**
```
[deadlock-lint] Movement.FOUND: all outgoing transitions are guarded or event-triggered (no unconditional fallback — Isabelle deadlock_free proof will fail). Add an unconditional else-branch in the Java mode block.
```

**Fix directive**
Add an `else { mode = <SameMode>; }` clause to the inner if-else chain of this mode block so the state has at least one bare-precondition outgoing transition. Without it, the Isabelle deadlock_free proof will fail (see CLAUDE.md "Isabelle/UTP Z-Machine `deadlock_free` proof" gotcha).

### Issue 8: deadlock_lint_warning — State without unconditional fallback: Movement.AVOIDING

**Raw**
```
[deadlock-lint] Movement.AVOIDING: all outgoing transitions are guarded or event-triggered (no unconditional fallback — Isabelle deadlock_free proof will fail). Add an unconditional else-branch in the Java mode block.
```

**Fix directive**
Add an `else { mode = <SameMode>; }` clause to the inner if-else chain of this mode block so the state has at least one bare-precondition outgoing transition. Without it, the Isabelle deadlock_free proof will fail (see CLAUDE.md "Isabelle/UTP Z-Machine `deadlock_free` proof" gotcha).

### Issue 9: deadlock_lint_warning — State without unconditional fallback: Movement.TRYING_AGAIN

**Raw**
```
[deadlock-lint] Movement.TRYING_AGAIN: all outgoing transitions are guarded or event-triggered (no unconditional fallback — Isabelle deadlock_free proof will fail). Add an unconditional else-branch in the Java mode block.
```

**Fix directive**
Add an `else { mode = <SameMode>; }` clause to the inner if-else chain of this mode block so the state has at least one bare-precondition outgoing transition. Without it, the Isabelle deadlock_free proof will fail (see CLAUDE.md "Isabelle/UTP Z-Machine `deadlock_free` proof" gotcha).

### Issue 10: deadlock_lint_warning — State without unconditional fallback: Movement.AVOIDING_AGAIN

**Raw**
```
[deadlock-lint] Movement.AVOIDING_AGAIN: all outgoing transitions are guarded or event-triggered (no unconditional fallback — Isabelle deadlock_free proof will fail). Add an unconditional else-branch in the Java mode block.
```

**Fix directive**
Add an `else { mode = <SameMode>; }` clause to the inner if-else chain of this mode block so the state has at least one bare-precondition outgoing transition. Without it, the Isabelle deadlock_free proof will fail (see CLAUDE.md "Isabelle/UTP Z-Machine `deadlock_free` proof" gotcha).

### Issue 11: deadlock_lint_warning — State without unconditional fallback: Movement.GETTING_OUT

**Raw**
```
[deadlock-lint] Movement.GETTING_OUT: all outgoing transitions are guarded or event-triggered (no unconditional fallback — Isabelle deadlock_free proof will fail). Add an unconditional else-branch in the Java mode block.
```

**Fix directive**
Add an `else { mode = <SameMode>; }` clause to the inner if-else chain of this mode block so the state has at least one bare-precondition outgoing transition. Without it, the Isabelle deadlock_free proof will fail (see CLAUDE.md "Isabelle/UTP Z-Machine `deadlock_free` proof" gotcha).

## Files to review
(none identified)

## Next step
Proceed to Phase 5a (CSP Generation) and/or 5b (Dafny Generation).
