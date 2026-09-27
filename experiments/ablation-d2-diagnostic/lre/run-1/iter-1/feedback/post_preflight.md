# 2c — Preflight (Structural Lint) — FAILED

## Summary
2c — Preflight (Structural Lint): 2 structural error(s) found. Downstream phases would produce a wrong formal model.

## Run history
- New this run: 17
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by 'inOpez' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM, event instanceof lre.event.InputEvent.endTask, event instanceof lre.event.InputEvent.reqHCM — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: LreController.step(): triggerless branch guarded by 'inOpez' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM, event instanceof lre.event.InputEvent.endTask, event instanceof lre.event.InputEvent.reqHCM — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-d2-run3-lre/java.generated.project/src/main/java/lre/controller/LreController.java:127
```

**Java trace**
  - Java: `LreController.java`:127-140

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. RoboChart has no event-absence guard.

### Issue 2: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by 'cdaBelowMinSafeDist && tcpaNonNegative' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM, event instanceof lre.event.InputEvent.endTask, event instanceof lre.event.InputEvent.reqHCM — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: LreController.step(): triggerless branch guarded by 'cdaBelowMinSafeDist && tcpaNonNegative' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM, event instanceof lre.event.InputEvent.endTask, event instanceof lre.event.InputEvent.reqHCM — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-d2-run3-lre/java.generated.project/src/main/java/lre/controller/LreController.java:129
```

**Java trace**
  - Java: `LreController.java`:129-140

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. RoboChart has no event-absence guard.

### Issue 3: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by 'cdaBelowMinSafeDist && tcpaNonNegative' does not negate preceding triggerless guard(s): inOpez

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by 'cdaBelowMinSafeDist && tcpaNonNegative' does not negate preceding triggerless guard(s): inOpez
Location: /Users/ranwei/Gitee/forge-d2-run3-lre/java.generated.project/src/main/java/lre/controller/LreController.java:129
```

**Java trace**
  - Java: `LreController.java`:129-140

**Fix directive**
Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive.

### Issue 4: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by 'hvelAtOrAboveOne && hdistCstcAtOrBelowHorizDist' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM, event instanceof lre.event.InputEvent.endTask, event instanceof lre.event.InputEvent.reqHCM — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: LreController.step(): triggerless branch guarded by 'hvelAtOrAboveOne && hdistCstcAtOrBelowHorizDist' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM, event instanceof lre.event.InputEvent.endTask, event instanceof lre.event.InputEvent.reqHCM — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-d2-run3-lre/java.generated.project/src/main/java/lre/controller/LreController.java:131
```

**Java trace**
  - Java: `LreController.java`:131-140

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. RoboChart has no event-absence guard.

### Issue 5: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by 'hvelAtOrAboveOne && hdistCstcAtOrBelowHorizDist' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafeDist&&tcpaNonNegative

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by 'hvelAtOrAboveOne && hdistCstcAtOrBelowHorizDist' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafeDist&&tcpaNonNegative
Location: /Users/ranwei/Gitee/forge-d2-run3-lre/java.generated.project/src/main/java/lre/controller/LreController.java:131
```

**Java trace**
  - Java: `LreController.java`:131-140

**Fix directive**
Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive.

### Issue 6: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by 'vdistCstcAtOrBelowDfltVertDist' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM, event instanceof lre.event.InputEvent.endTask, event instanceof lre.event.InputEvent.reqHCM — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: LreController.step(): triggerless branch guarded by 'vdistCstcAtOrBelowDfltVertDist' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM, event instanceof lre.event.InputEvent.endTask, event instanceof lre.event.InputEvent.reqHCM — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-d2-run3-lre/java.generated.project/src/main/java/lre/controller/LreController.java:134
```

**Java trace**
  - Java: `LreController.java`:134-140

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. RoboChart has no event-absence guard.

### Issue 7: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by 'vdistCstcAtOrBelowDfltVertDist' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafeDist&&tcpaNonNegative, hvelAtOrAboveOne&&hdistCstcAtOrBelowHorizDist

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by 'vdistCstcAtOrBelowDfltVertDist' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafeDist&&tcpaNonNegative, hvelAtOrAboveOne&&hdistCstcAtOrBelowHorizDist
Location: /Users/ranwei/Gitee/forge-d2-run3-lre/java.generated.project/src/main/java/lre/controller/LreController.java:134
```

**Java trace**
  - Java: `LreController.java`:134-140

**Fix directive**
Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive.

### Issue 8: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by 'vvelAtOrAboveOne && vdistCstcAtOrBelowVertDist' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM, event instanceof lre.event.InputEvent.endTask, event instanceof lre.event.InputEvent.reqHCM — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: LreController.step(): triggerless branch guarded by 'vvelAtOrAboveOne && vdistCstcAtOrBelowVertDist' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM, event instanceof lre.event.InputEvent.endTask, event instanceof lre.event.InputEvent.reqHCM — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-d2-run3-lre/java.generated.project/src/main/java/lre/controller/LreController.java:137
```

**Java trace**
  - Java: `LreController.java`:137-140

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. RoboChart has no event-absence guard.

### Issue 9: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by 'vvelAtOrAboveOne && vdistCstcAtOrBelowVertDist' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafeDist&&tcpaNonNegative, hvelAtOrAboveOne&&hdistCstcAtOrBelowHorizDist, vdistCstcAtOrBelowDfltVertDist

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by 'vvelAtOrAboveOne && vdistCstcAtOrBelowVertDist' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafeDist&&tcpaNonNegative, hvelAtOrAboveOne&&hdistCstcAtOrBelowHorizDist, vdistCstcAtOrBelowDfltVertDist
Location: /Users/ranwei/Gitee/forge-d2-run3-lre/java.generated.project/src/main/java/lre/controller/LreController.java:137
```

**Java trace**
  - Java: `LreController.java`:137-140

**Fix directive**
Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive.

### Issue 10: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by 'inOpez' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: LreController.step(): triggerless branch guarded by 'inOpez' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-d2-run3-lre/java.generated.project/src/main/java/lre/controller/LreController.java:145
```

**Java trace**
  - Java: `LreController.java`:145-152

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. RoboChart has no event-absence guard.

### Issue 11: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by 'cdaBelowMinSafeDist && tcpaNonNegative' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: LreController.step(): triggerless branch guarded by 'cdaBelowMinSafeDist && tcpaNonNegative' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-d2-run3-lre/java.generated.project/src/main/java/lre/controller/LreController.java:147
```

**Java trace**
  - Java: `LreController.java`:147-152

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. RoboChart has no event-absence guard.

### Issue 12: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by 'cdaBelowMinSafeDist && tcpaNonNegative' does not negate preceding triggerless guard(s): inOpez

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by 'cdaBelowMinSafeDist && tcpaNonNegative' does not negate preceding triggerless guard(s): inOpez
Location: /Users/ranwei/Gitee/forge-d2-run3-lre/java.generated.project/src/main/java/lre/controller/LreController.java:147
```

**Java trace**
  - Java: `LreController.java`:147-152

**Fix directive**
Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive.

### Issue 13: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by '(!hdistCstcAtOrBelowHorizDist) && (!vdistCstcAtOrBelowVertDist)' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: LreController.step(): triggerless branch guarded by '(!hdistCstcAtOrBelowHorizDist) && (!vdistCstcAtOrBelowVertDist)' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-d2-run3-lre/java.generated.project/src/main/java/lre/controller/LreController.java:149
```

**Java trace**
  - Java: `LreController.java`:149-152

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. RoboChart has no event-absence guard.

### Issue 14: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by '(!hdistCstcAtOrBelowHorizDist) && (!vdistCstcAtOrBelowVertDist)' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafeDist&&tcpaNonNegative

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by '(!hdistCstcAtOrBelowHorizDist) && (!vdistCstcAtOrBelowVertDist)' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafeDist&&tcpaNonNegative
Location: /Users/ranwei/Gitee/forge-d2-run3-lre/java.generated.project/src/main/java/lre/controller/LreController.java:149
```

**Java trace**
  - Java: `LreController.java`:149-152

**Fix directive**
Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive.

### Issue 15: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by '!cdaBelowMinSafeDist' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: LreController.step(): triggerless branch guarded by '!cdaBelowMinSafeDist' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-d2-run3-lre/java.generated.project/src/main/java/lre/controller/LreController.java:157
```

**Java trace**
  - Java: `LreController.java`:157-160

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. RoboChart has no event-absence guard.

### Issue 16: lint_rule4_double_missing_real_annotation — field 'CalcCPA.EPSILON' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: field 'CalcCPA.EPSILON' is a double without @RoboChartType("real")
Location: /Users/ranwei/Gitee/forge-d2-run3-lre/java.generated.project/src/main/java/lre/operation/CalcCPA.java:15
```

**Java trace**
  - Java: `CalcCPA.java`:15-15

**Fix directive**
Without an explicit RoboChart type, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type.

### Issue 17: lint_rule4_double_missing_real_annotation — field 'Sensor.SAFE_LARGE_DIST' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: field 'Sensor.SAFE_LARGE_DIST' is a double without @RoboChartType("real")
Location: /Users/ranwei/Gitee/forge-d2-run3-lre/java.generated.project/src/main/java/lre/sensor/Sensor.java:15
```

**Java trace**
  - Java: `Sensor.java`:15-15

**Fix directive**
Without an explicit RoboChart type, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type.

## Files to review
(none identified)

## Next step
Preflight found structural rule violations (errors). Fix them before continuing — downstream phases would either crash or produce a silently-wrong formal model.
