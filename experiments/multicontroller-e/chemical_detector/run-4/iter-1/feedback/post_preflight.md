# 2c — Preflight (Structural Lint) — FAILED

## Summary
2c — Preflight (Structural Lint): 1 structural error(s) found. Downstream phases would produce a wrong formal model.

## Run history
- New this run: 4
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: lint_rule8_event_branch_precedes_triggerless — MovementController.step(): triggerless branch guarded by 'withinStuckPeriod || advancedBeyondStuckDist' follows event-triggered branch(es): event instanceof chemical_detector.event.InputEvent.Stop, event instanceof chemical_detector.event.InputEvent.Resume — in the extracted model both are enabled together

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

### Issue 2: lint_rule8_event_branch_precedes_triggerless — MovementController.step(): triggerless branch guarded by '(!withinStuckPeriod) && (!advancedBeyondStuckDist)' follows event-triggered branch(es): event instanceof chemical_detector.event.InputEvent.Stop, event instanceof chemical_detector.event.InputEvent.Resume — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: MovementController.step(): triggerless branch guarded by '(!withinStuckPeriod) && (!advancedBeyondStuckDist)' follows event-triggered branch(es): event instanceof chemical_detector.event.InputEvent.Stop, event instanceof chemical_detector.event.InputEvent.Resume — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-mce-run4-chemical_detector/java.generated.project/src/main/java/chemical_detector/controller/MovementController.java:151
```

**Java trace**
  - Java: `MovementController.java`:151-155

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. RoboChart has no event-absence guard.

### Issue 3: lint_rule6_missing_priority_negation — MovementController.step(): branch guarded by '(!withinStuckPeriod) && (!advancedBeyondStuckDist)' does not negate preceding triggerless guard(s): withinStuckPeriod||advancedBeyondStuckDist

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: MovementController.step(): branch guarded by '(!withinStuckPeriod) && (!advancedBeyondStuckDist)' does not negate preceding triggerless guard(s): withinStuckPeriod||advancedBeyondStuckDist
Location: /Users/ranwei/Gitee/forge-mce-run4-chemical_detector/java.generated.project/src/main/java/chemical_detector/controller/MovementController.java:151
```

**Java trace**
  - Java: `MovementController.java`:151-155

**Fix directive**
Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive.

### Issue 4: lint_rule4_double_missing_real_annotation — parameter 'ChemicalDetector.odometer#distance' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: parameter 'ChemicalDetector.odometer#distance' is a double without @RoboChartType("real")
Location: /Users/ranwei/Gitee/forge-mce-run4-chemical_detector/java.generated.project/src/main/java/chemical_detector/system/ChemicalDetector.java:58
```

**Java trace**
  - Java: `ChemicalDetector.java`:58-58

**Fix directive**
Without an explicit RoboChart type, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type.

## Files to review
(none identified)

## Next step
Preflight found structural rule violations (errors). Fix them before continuing — downstream phases would either crash or produce a silently-wrong formal model.
