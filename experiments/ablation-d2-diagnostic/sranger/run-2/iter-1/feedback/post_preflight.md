# 2c — Preflight (Structural Lint) — PASSED

## Summary
2c — Preflight (Structural Lint): no structural errors.

## Run history
- New this run: 2
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: lint_rule8_event_branch_precedes_triggerless — SRangerController.step(): triggerless branch guarded by 'turnDurationElapsed' follows event-triggered branch(es): event instanceof sranger.event.InputEvent.EndTask — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: SRangerController.step(): triggerless branch guarded by 'turnDurationElapsed' follows event-triggered branch(es): event instanceof sranger.event.InputEvent.EndTask — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-d2-run3-sranger/java.generated.project/src/main/java/sranger/controller/SRangerController.java:60
```

**Java trace**
  - Java: `SRangerController.java`:60-65

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. RoboChart has no event-absence guard.

### Issue 2: lint_rule6_missing_priority_negation — SRangerController.step(): branch guarded by 'event instanceof sranger.event.InputEvent.Tick' does not negate preceding triggerless guard(s): turnDurationElapsed

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: SRangerController.step(): branch guarded by 'event instanceof sranger.event.InputEvent.Tick' does not negate preceding triggerless guard(s): turnDurationElapsed
Location: /Users/ranwei/Gitee/forge-d2-run3-sranger/java.generated.project/src/main/java/sranger/controller/SRangerController.java:63
```

**Java trace**
  - Java: `SRangerController.java`:63-65

**Fix directive**
Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive.

## Files to review
(none identified)

## Next step
Proceed to Phase 3 (T2M — Spoon Discovery).
