# 2c — Preflight (Structural Lint) — FAILED

## Summary
2c — Preflight (Structural Lint): 1 structural error(s) found. Downstream phases would produce a wrong formal model.

## Run history
- New this run: 3
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: lint_rule4_double_missing_real_annotation — field 'GasSensor.i' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: field 'GasSensor.i' is a double without @RoboChartType("real")
```

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 2: lint_rule8_event_branch_precedes_triggerless — MovementController.step(): triggerless branch guarded by 'makingProgress' follows event-triggered branch(es): event instanceof chemical_detector.event.InputEvent.Stop, event instanceof chemical_detector.event.InputEvent.Resume — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: MovementController.step(): triggerless branch guarded by 'makingProgress' follows event-triggered branch(es): event instanceof chemical_detector.event.InputEvent.Stop, event instanceof chemical_detector.event.InputEvent.Resume — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-v3r-run2-chemical_detector/java.generated.project/src/main/java/chemical_detector/controller/MovementController.java:171
```

**Java trace**
  - Java: `MovementController.java`:171-183

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. Trigger disjointness does NOT close this (it only separates two EVENT-triggered branches), and the negation synthesis that closes rule6 cannot either, because the event-triggered branch contributes no data predicate to negate. RoboChart has no event-absence guard, so this must be fixed in the Java: either give the later branch an explicit guard that excludes the earlier branch's case, or place the triggerless branch FIRST so the ordinary priority-negation synthesis applies. Otherwise the model admits two operations in states where the Java is deterministic.

### Issue 3: lint_rule8_event_branch_precedes_triggerless — MovementController.step(): triggerless branch guarded by '!makingProgress' follows event-triggered branch(es): event instanceof chemical_detector.event.InputEvent.Stop, event instanceof chemical_detector.event.InputEvent.Resume — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: MovementController.step(): triggerless branch guarded by '!makingProgress' follows event-triggered branch(es): event instanceof chemical_detector.event.InputEvent.Stop, event instanceof chemical_detector.event.InputEvent.Resume — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-v3r-run2-chemical_detector/java.generated.project/src/main/java/chemical_detector/controller/MovementController.java:179
```

**Java trace**
  - Java: `MovementController.java`:179-183

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. Trigger disjointness does NOT close this (it only separates two EVENT-triggered branches), and the negation synthesis that closes rule6 cannot either, because the event-triggered branch contributes no data predicate to negate. RoboChart has no event-absence guard, so this must be fixed in the Java: either give the later branch an explicit guard that excludes the earlier branch's case, or place the triggerless branch FIRST so the ordinary priority-negation synthesis applies. Otherwise the model admits two operations in states where the Java is deterministic.

## Files to review
(none identified)

## Next step
Preflight found structural rule violations (errors). Fix them before continuing — downstream phases would either crash or produce a silently-wrong formal model.
