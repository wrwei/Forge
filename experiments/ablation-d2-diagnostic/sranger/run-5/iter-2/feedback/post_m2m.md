# 4 — Model-to-Model (ETL Transformation) — PASSED

## Summary
4 — Model-to-Model (ETL Transformation) completed successfully.

## Run history
- New this run: 0
- Recurring from previous run: 3
- Resolved since previous run: 0

## Issues
### Issue 1: deadlock_lint_warning — State without unconditional fallback: SRangerController.MOVING [recurring x2]

**Raw**
```
[deadlock-lint] SRangerController.MOVING: all outgoing transitions are guarded or event-triggered (no unconditional fallback — Isabelle deadlock_free proof will fail). Add an unconditional else-branch in the Java mode block.
```

**Fix directive**
The structural lint reports that this state has no unconditional outgoing transition. See the raw lint line above for the state and its source location.

### Issue 2: deadlock_lint_warning — State without unconditional fallback: SRangerController.TURNING [recurring x2]

**Raw**
```
[deadlock-lint] SRangerController.TURNING: all outgoing transitions are guarded or event-triggered (no unconditional fallback — Isabelle deadlock_free proof will fail). Add an unconditional else-branch in the Java mode block.
```

**Fix directive**
The structural lint reports that this state has no unconditional outgoing transition. See the raw lint line above for the state and its source location.

### Issue 3: deadlock_lint_warning — State without unconditional fallback: SRangerController.FINAL [recurring x2]

**Raw**
```
[deadlock-lint] SRangerController.FINAL: all outgoing transitions are guarded or event-triggered (no unconditional fallback — Isabelle deadlock_free proof will fail). Add an unconditional else-branch in the Java mode block.
```

**Fix directive**
The structural lint reports that this state has no unconditional outgoing transition. See the raw lint line above for the state and its source location.

## Files to review
(none identified)

## Next step
Proceed to Phase 5a (CSP Generation) and/or 5b (Dafny Generation).
