# 2c — Preflight (Structural Lint) — FAILED

## Summary
2c — Preflight (Structural Lint): 1 structural error(s) found. Downstream phases would produce a wrong formal model.

## Run history
- New this run: 3
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: lint_rule8_event_branch_precedes_triggerless — SRangerController.step(): triggerless branch guarded by 'turnDurationElapsed' follows event-triggered branch(es): event instanceof sranger.event.InputEvent.EndTask — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: SRangerController.step(): triggerless branch guarded by 'turnDurationElapsed' follows event-triggered branch(es): event instanceof sranger.event.InputEvent.EndTask — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-d2-run5-sranger/java.generated.project/src/main/java/sranger/controller/SRangerController.java:58
```

**Java trace**
  - Java: `SRangerController.java`:58-63

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. RoboChart has no event-absence guard.

### Issue 2: lint_rule6_missing_priority_negation — SRangerController.step(): branch guarded by 'event instanceof sranger.event.InputEvent.Tick' does not negate preceding triggerless guard(s): turnDurationElapsed

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: SRangerController.step(): branch guarded by 'event instanceof sranger.event.InputEvent.Tick' does not negate preceding triggerless guard(s): turnDurationElapsed
Location: /Users/ranwei/Gitee/forge-d2-run5-sranger/java.generated.project/src/main/java/sranger/controller/SRangerController.java:61
```

**Java trace**
  - Java: `SRangerController.java`:61-63

**Fix directive**
Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive.

### Issue 3: lint_rule4_double_missing_real_annotation — field 'Sensor.NO_READING_DISTANCE' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: field 'Sensor.NO_READING_DISTANCE' is a double without @RoboChartType("real")
Location: /Users/ranwei/Gitee/forge-d2-run5-sranger/java.generated.project/src/main/java/sranger/sensor/Sensor.java:10
```

**Java trace**
  - Java: `Sensor.java`:10-10

**Fix directive**
Without an explicit RoboChart type, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type.

## Files to review
(none identified)

## Next step
Preflight found structural rule violations (errors). Fix them before continuing — downstream phases would either crash or produce a silently-wrong formal model.
