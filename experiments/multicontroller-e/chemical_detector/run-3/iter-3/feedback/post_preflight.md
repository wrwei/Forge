# 2c — Preflight (Structural Lint) — PASSED

## Summary
2c — Preflight (Structural Lint): no structural errors.

## Run history
- New this run: 3
- Recurring from previous run: 0
- Resolved since previous run: 2

**Resolved issue titles**
- MovementController.step(): triggerless branch guarded by 'makingProgress' follows event-triggered branch(es): event instanceof chemical_detector.event.InputEvent.stop, event instanceof chemical_detector.event.InputEvent.resume — in the extracted model both are enabled together
- MovementController.step(): triggerless branch guarded by '!makingProgress' follows event-triggered branch(es): event instanceof chemical_detector.event.InputEvent.stop, event instanceof chemical_detector.event.InputEvent.resume — in the extracted model both are enabled together

## Issues
### Issue 1: lint_rule8_event_branch_precedes_triggerless — MovementController.step(): triggerless branch guarded by 'evasionWithinStuckPeriod || advancedBeyondStuckDist' follows event-triggered branch(es): event instanceof chemical_detector.event.InputEvent.stop, event instanceof chemical_detector.event.InputEvent.resume — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: MovementController.step(): triggerless branch guarded by 'evasionWithinStuckPeriod || advancedBeyondStuckDist' follows event-triggered branch(es): event instanceof chemical_detector.event.InputEvent.stop, event instanceof chemical_detector.event.InputEvent.resume — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-mce-run3-chemical_detector/java.generated.project/src/main/java/chemical_detector/controller/MovementController.java:150
```

**Java trace**
  - Java: `MovementController.java`:150-160

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. RoboChart has no event-absence guard.

### Issue 2: lint_rule8_event_branch_precedes_triggerless — MovementController.step(): triggerless branch guarded by '(!evasionWithinStuckPeriod) && (!advancedBeyondStuckDist)' follows event-triggered branch(es): event instanceof chemical_detector.event.InputEvent.stop, event instanceof chemical_detector.event.InputEvent.resume — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: MovementController.step(): triggerless branch guarded by '(!evasionWithinStuckPeriod) && (!advancedBeyondStuckDist)' follows event-triggered branch(es): event instanceof chemical_detector.event.InputEvent.stop, event instanceof chemical_detector.event.InputEvent.resume — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-mce-run3-chemical_detector/java.generated.project/src/main/java/chemical_detector/controller/MovementController.java:156
```

**Java trace**
  - Java: `MovementController.java`:156-160

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. RoboChart has no event-absence guard.

### Issue 3: lint_rule6_missing_priority_negation — MovementController.step(): branch guarded by '(!evasionWithinStuckPeriod) && (!advancedBeyondStuckDist)' does not negate preceding triggerless guard(s): evasionWithinStuckPeriod||advancedBeyondStuckDist

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: MovementController.step(): branch guarded by '(!evasionWithinStuckPeriod) && (!advancedBeyondStuckDist)' does not negate preceding triggerless guard(s): evasionWithinStuckPeriod||advancedBeyondStuckDist
Location: /Users/ranwei/Gitee/forge-mce-run3-chemical_detector/java.generated.project/src/main/java/chemical_detector/controller/MovementController.java:156
```

**Java trace**
  - Java: `MovementController.java`:156-160

**Fix directive**
Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive.

## Files to review
(none identified)

## Next step
Proceed to Phase 3 (T2M — Spoon Discovery).
