# 2c — Preflight (Structural Lint) — PASSED

## Summary
2c — Preflight (Structural Lint): no structural errors.

## Run history
- New this run: 4
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: lint_rule6_missing_priority_negation — GasAnalysis.step(): branch guarded by 'gasPresent' does not negate preceding triggerless guard(s): !gasPresent

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: GasAnalysis.step(): branch guarded by 'gasPresent' does not negate preceding triggerless guard(s): !gasPresent
Location: /Users/ranwei/Gitee/forge-mce-run1-chemical_detector/java.generated.project/src/main/java/chemical_detector/controller/GasAnalysis.java:77
```

**Java trace**
  - Java: `GasAnalysis.java`:77-80

**Fix directive**
Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive.

### Issue 2: lint_rule8_event_branch_precedes_triggerless — Movement.step(): triggerless branch guarded by 'withinStuckPeriod || advancedBeyondStuckDist' follows event-triggered branch(es): event instanceof chemical_detector.event.InputEvent.Stop, event instanceof chemical_detector.event.InputEvent.Resume — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: Movement.step(): triggerless branch guarded by 'withinStuckPeriod || advancedBeyondStuckDist' follows event-triggered branch(es): event instanceof chemical_detector.event.InputEvent.Stop, event instanceof chemical_detector.event.InputEvent.Resume — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-mce-run1-chemical_detector/java.generated.project/src/main/java/chemical_detector/controller/Movement.java:154
```

**Java trace**
  - Java: `Movement.java`:154-164

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. RoboChart has no event-absence guard.

### Issue 3: lint_rule8_event_branch_precedes_triggerless — Movement.step(): triggerless branch guarded by '(!withinStuckPeriod) && (!advancedBeyondStuckDist)' follows event-triggered branch(es): event instanceof chemical_detector.event.InputEvent.Stop, event instanceof chemical_detector.event.InputEvent.Resume — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: Movement.step(): triggerless branch guarded by '(!withinStuckPeriod) && (!advancedBeyondStuckDist)' follows event-triggered branch(es): event instanceof chemical_detector.event.InputEvent.Stop, event instanceof chemical_detector.event.InputEvent.Resume — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-mce-run1-chemical_detector/java.generated.project/src/main/java/chemical_detector/controller/Movement.java:160
```

**Java trace**
  - Java: `Movement.java`:160-164

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. RoboChart has no event-absence guard.

### Issue 4: lint_rule6_missing_priority_negation — Movement.step(): branch guarded by '(!withinStuckPeriod) && (!advancedBeyondStuckDist)' does not negate preceding triggerless guard(s): withinStuckPeriod||advancedBeyondStuckDist

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: Movement.step(): branch guarded by '(!withinStuckPeriod) && (!advancedBeyondStuckDist)' does not negate preceding triggerless guard(s): withinStuckPeriod||advancedBeyondStuckDist
Location: /Users/ranwei/Gitee/forge-mce-run1-chemical_detector/java.generated.project/src/main/java/chemical_detector/controller/Movement.java:160
```

**Java trace**
  - Java: `Movement.java`:160-164

**Fix directive**
Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive.

## Files to review
(none identified)

## Next step
Proceed to Phase 3 (T2M — Spoon Discovery).
