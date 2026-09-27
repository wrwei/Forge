# 2c — Preflight (Structural Lint) — PASSED

## Summary
2c — Preflight (Structural Lint): no structural errors.

## Run history
- New this run: 1
- Recurring from previous run: 1
- Resolved since previous run: 3

**Resolved issue titles**
- MovementController.step(): triggerless branch guarded by '(!withinStuckPeriod) && (!advancedBeyondStuckDist)' follows event-triggered branch(es): event instanceof chemical_detector.event.InputEvent.Stop, event instanceof chemical_detector.event.InputEvent.Resume — in the extracted model both are enabled together
- MovementController.step(): branch guarded by '(!withinStuckPeriod) && (!advancedBeyondStuckDist)' does not negate preceding triggerless guard(s): withinStuckPeriod||advancedBeyondStuckDist
- parameter 'ChemicalDetector.odometer#distance' is a double without @RoboChartType("real")

## Issues
### Issue 1: lint_rule8_event_branch_precedes_triggerless — MovementController.step(): triggerless branch guarded by 'withinStuckPeriod || advancedBeyondStuckDist' follows event-triggered branch(es): event instanceof chemical_detector.event.InputEvent.Stop, event instanceof chemical_detector.event.InputEvent.Resume — in the extracted model both are enabled together [recurring x2]

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: MovementController.step(): triggerless branch guarded by 'withinStuckPeriod || advancedBeyondStuckDist' follows event-triggered branch(es): event instanceof chemical_detector.event.InputEvent.Stop, event instanceof chemical_detector.event.InputEvent.Resume — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-mce-run4-chemical_detector/java.generated.project/src/main/java/chemical_detector/controller/MovementController.java:145
```

**Java trace**
  - Java: `MovementController.java`:145-155

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. RoboChart has no event-absence guard.

### Issue 2: lint_rule8_event_branch_precedes_triggerless — MovementController.step(): triggerless branch guarded by '!(withinStuckPeriod || advancedBeyondStuckDist)' follows event-triggered branch(es): event instanceof chemical_detector.event.InputEvent.Stop, event instanceof chemical_detector.event.InputEvent.Resume — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: MovementController.step(): triggerless branch guarded by '!(withinStuckPeriod || advancedBeyondStuckDist)' follows event-triggered branch(es): event instanceof chemical_detector.event.InputEvent.Stop, event instanceof chemical_detector.event.InputEvent.Resume — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-mce-run4-chemical_detector/java.generated.project/src/main/java/chemical_detector/controller/MovementController.java:151
```

**Java trace**
  - Java: `MovementController.java`:151-155

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. RoboChart has no event-absence guard.

## Files to review
(none identified)

## Next step
Proceed to Phase 3 (T2M — Spoon Discovery).
