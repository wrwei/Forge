# 4 — Model-to-Model (ETL Transformation) — PASSED

## Summary
4 — Model-to-Model (ETL Transformation) completed successfully.

## Run history
- New this run: 0
- Recurring from previous run: 11
- Resolved since previous run: 0

## Issues
### Issue 1: deadlock_lint_warning — State without unconditional fallback: GasAnalysisController.Reading [recurring x2]

**Raw**
```
[deadlock-lint] GasAnalysisController.Reading: all outgoing transitions are guarded or event-triggered (no unconditional fallback; the Isabelle generator adds an idle operation for the remaining cases, so no change is needed).
```

**Fix directive**
This state has no unconditional outgoing transition. The Isabelle generator represents the remaining cases by an idle operation, so the deadlock-freedom obligation does not depend on this; no change is required.

### Issue 2: deadlock_lint_warning — State without unconditional fallback: GasAnalysisController.Analysis [recurring x2]

**Raw**
```
[deadlock-lint] GasAnalysisController.Analysis: all outgoing transitions are guarded or event-triggered (no unconditional fallback; the Isabelle generator adds an idle operation for the remaining cases, so no change is needed).
```

**Fix directive**
This state has no unconditional outgoing transition. The Isabelle generator represents the remaining cases by an idle operation, so the deadlock-freedom obligation does not depend on this; no change is required.

### Issue 3: deadlock_lint_warning — State without unconditional fallback: GasAnalysisController.GasDetected [recurring x2]

**Raw**
```
[deadlock-lint] GasAnalysisController.GasDetected: all outgoing transitions are guarded or event-triggered (no unconditional fallback; the Isabelle generator adds an idle operation for the remaining cases, so no change is needed).
```

**Fix directive**
This state has no unconditional outgoing transition. The Isabelle generator represents the remaining cases by an idle operation, so the deadlock-freedom obligation does not depend on this; no change is required.

### Issue 4: deadlock_lint_warning — State without unconditional fallback: GasAnalysisController.Concluded [recurring x2]

**Raw**
```
[deadlock-lint] GasAnalysisController.Concluded: no outgoing transitions (terminal state; the Isabelle generator exempts it from deadlock-freedom)
```

**Fix directive**
This state has no unconditional outgoing transition. The Isabelle generator represents the remaining cases by an idle operation, so the deadlock-freedom obligation does not depend on this; no change is required.

### Issue 5: deadlock_lint_warning — State without unconditional fallback: MovementController.Waiting [recurring x2]

**Raw**
```
[deadlock-lint] MovementController.Waiting: all outgoing transitions are guarded or event-triggered (no unconditional fallback; the Isabelle generator adds an idle operation for the remaining cases, so no change is needed).
```

**Fix directive**
This state has no unconditional outgoing transition. The Isabelle generator represents the remaining cases by an idle operation, so the deadlock-freedom obligation does not depend on this; no change is required.

### Issue 6: deadlock_lint_warning — State without unconditional fallback: MovementController.Going [recurring x2]

**Raw**
```
[deadlock-lint] MovementController.Going: all outgoing transitions are guarded or event-triggered (no unconditional fallback; the Isabelle generator adds an idle operation for the remaining cases, so no change is needed).
```

**Fix directive**
This state has no unconditional outgoing transition. The Isabelle generator represents the remaining cases by an idle operation, so the deadlock-freedom obligation does not depend on this; no change is required.

### Issue 7: deadlock_lint_warning — State without unconditional fallback: MovementController.Found [recurring x2]

**Raw**
```
[deadlock-lint] MovementController.Found: no outgoing transitions (terminal state; the Isabelle generator exempts it from deadlock-freedom)
```

**Fix directive**
This state has no unconditional outgoing transition. The Isabelle generator represents the remaining cases by an idle operation, so the deadlock-freedom obligation does not depend on this; no change is required.

### Issue 8: deadlock_lint_warning — State without unconditional fallback: MovementController.Avoiding [recurring x2]

**Raw**
```
[deadlock-lint] MovementController.Avoiding: all outgoing transitions are guarded or event-triggered (no unconditional fallback; the Isabelle generator adds an idle operation for the remaining cases, so no change is needed).
```

**Fix directive**
This state has no unconditional outgoing transition. The Isabelle generator represents the remaining cases by an idle operation, so the deadlock-freedom obligation does not depend on this; no change is required.

### Issue 9: deadlock_lint_warning — State without unconditional fallback: MovementController.TryingAgain [recurring x2]

**Raw**
```
[deadlock-lint] MovementController.TryingAgain: all outgoing transitions are guarded or event-triggered (no unconditional fallback; the Isabelle generator adds an idle operation for the remaining cases, so no change is needed).
```

**Fix directive**
This state has no unconditional outgoing transition. The Isabelle generator represents the remaining cases by an idle operation, so the deadlock-freedom obligation does not depend on this; no change is required.

### Issue 10: deadlock_lint_warning — State without unconditional fallback: MovementController.AvoidingAgain [recurring x2]

**Raw**
```
[deadlock-lint] MovementController.AvoidingAgain: all outgoing transitions are guarded or event-triggered (no unconditional fallback; the Isabelle generator adds an idle operation for the remaining cases, so no change is needed).
```

**Fix directive**
This state has no unconditional outgoing transition. The Isabelle generator represents the remaining cases by an idle operation, so the deadlock-freedom obligation does not depend on this; no change is required.

### Issue 11: deadlock_lint_warning — State without unconditional fallback: MovementController.GettingOut [recurring x2]

**Raw**
```
[deadlock-lint] MovementController.GettingOut: all outgoing transitions are guarded or event-triggered (no unconditional fallback; the Isabelle generator adds an idle operation for the remaining cases, so no change is needed).
```

**Fix directive**
This state has no unconditional outgoing transition. The Isabelle generator represents the remaining cases by an idle operation, so the deadlock-freedom obligation does not depend on this; no change is required.

## Files to review
(none identified)

## Next step
Proceed to Phase 5a (CSP Generation) and/or 5b (Dafny Generation).
