# 4 — Model-to-Model (ETL Transformation) — PASSED

## Summary
4 — Model-to-Model (ETL Transformation) completed successfully.

## Run history
- New this run: 5
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: deadlock_lint_warning — State without unconditional fallback: SteamBoilerController.INITIALISATION

**Raw**
```
[deadlock-lint] SteamBoilerController.INITIALISATION: all outgoing transitions are guarded or event-triggered (no unconditional fallback; the Isabelle generator adds an idle operation for the remaining cases, so no change is needed).
```

**Fix directive**
This state has no unconditional outgoing transition. The Isabelle generator represents the remaining cases by an idle operation, so the deadlock-freedom obligation does not depend on this; no change is required.

### Issue 2: deadlock_lint_warning — State without unconditional fallback: SteamBoilerController.NORMAL

**Raw**
```
[deadlock-lint] SteamBoilerController.NORMAL: all outgoing transitions are guarded or event-triggered (no unconditional fallback; the Isabelle generator adds an idle operation for the remaining cases, so no change is needed).
```

**Fix directive**
This state has no unconditional outgoing transition. The Isabelle generator represents the remaining cases by an idle operation, so the deadlock-freedom obligation does not depend on this; no change is required.

### Issue 3: deadlock_lint_warning — State without unconditional fallback: SteamBoilerController.DEGRADED

**Raw**
```
[deadlock-lint] SteamBoilerController.DEGRADED: all outgoing transitions are guarded or event-triggered (no unconditional fallback; the Isabelle generator adds an idle operation for the remaining cases, so no change is needed).
```

**Fix directive**
This state has no unconditional outgoing transition. The Isabelle generator represents the remaining cases by an idle operation, so the deadlock-freedom obligation does not depend on this; no change is required.

### Issue 4: deadlock_lint_warning — State without unconditional fallback: SteamBoilerController.RESCUE

**Raw**
```
[deadlock-lint] SteamBoilerController.RESCUE: all outgoing transitions are guarded or event-triggered (no unconditional fallback; the Isabelle generator adds an idle operation for the remaining cases, so no change is needed).
```

**Fix directive**
This state has no unconditional outgoing transition. The Isabelle generator represents the remaining cases by an idle operation, so the deadlock-freedom obligation does not depend on this; no change is required.

### Issue 5: deadlock_lint_warning — State without unconditional fallback: SteamBoilerController.EMERGENCY_STOP

**Raw**
```
[deadlock-lint] SteamBoilerController.EMERGENCY_STOP: no outgoing transitions (terminal state; the Isabelle generator exempts it from deadlock-freedom)
```

**Fix directive**
This state has no unconditional outgoing transition. The Isabelle generator represents the remaining cases by an idle operation, so the deadlock-freedom obligation does not depend on this; no change is required.

## Files to review
(none identified)

## Next step
Proceed to Phase 5a (CSP Generation) and/or 5b (Dafny Generation).
