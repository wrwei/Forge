# 2c — Preflight (Structural Lint) — PASSED

## Summary
2c — Preflight (Structural Lint): no structural errors.

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
Location: /Users/ranwei/Gitee/forge-d2-run7-lre/java.generated.project/src/main/java/lre/controller/LreController.java:130
```

**Java trace**
  - Java: `LreController.java`:130-143

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. RoboChart has no event-absence guard.

### Issue 2: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by 'cdaBelowMinSafeDist && tcpaNonNegative' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM, event instanceof lre.event.InputEvent.endTask, event instanceof lre.event.InputEvent.reqHCM — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: LreController.step(): triggerless branch guarded by 'cdaBelowMinSafeDist && tcpaNonNegative' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM, event instanceof lre.event.InputEvent.endTask, event instanceof lre.event.InputEvent.reqHCM — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-d2-run7-lre/java.generated.project/src/main/java/lre/controller/LreController.java:132
```

**Java trace**
  - Java: `LreController.java`:132-143

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. RoboChart has no event-absence guard.

### Issue 3: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by 'cdaBelowMinSafeDist && tcpaNonNegative' does not negate preceding triggerless guard(s): inOpez

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by 'cdaBelowMinSafeDist && tcpaNonNegative' does not negate preceding triggerless guard(s): inOpez
Location: /Users/ranwei/Gitee/forge-d2-run7-lre/java.generated.project/src/main/java/lre/controller/LreController.java:132
```

**Java trace**
  - Java: `LreController.java`:132-143

**Fix directive**
Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive.

### Issue 4: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by 'hvelAtLeastOne && hdistCStcAtMostHorizDist' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM, event instanceof lre.event.InputEvent.endTask, event instanceof lre.event.InputEvent.reqHCM — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: LreController.step(): triggerless branch guarded by 'hvelAtLeastOne && hdistCStcAtMostHorizDist' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM, event instanceof lre.event.InputEvent.endTask, event instanceof lre.event.InputEvent.reqHCM — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-d2-run7-lre/java.generated.project/src/main/java/lre/controller/LreController.java:134
```

**Java trace**
  - Java: `LreController.java`:134-143

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. RoboChart has no event-absence guard.

### Issue 5: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by 'hvelAtLeastOne && hdistCStcAtMostHorizDist' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafeDist&&tcpaNonNegative

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by 'hvelAtLeastOne && hdistCStcAtMostHorizDist' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafeDist&&tcpaNonNegative
Location: /Users/ranwei/Gitee/forge-d2-run7-lre/java.generated.project/src/main/java/lre/controller/LreController.java:134
```

**Java trace**
  - Java: `LreController.java`:134-143

**Fix directive**
Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive.

### Issue 6: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by 'vdistCStcAtMostDfltVertDist' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM, event instanceof lre.event.InputEvent.endTask, event instanceof lre.event.InputEvent.reqHCM — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: LreController.step(): triggerless branch guarded by 'vdistCStcAtMostDfltVertDist' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM, event instanceof lre.event.InputEvent.endTask, event instanceof lre.event.InputEvent.reqHCM — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-d2-run7-lre/java.generated.project/src/main/java/lre/controller/LreController.java:137
```

**Java trace**
  - Java: `LreController.java`:137-143

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. RoboChart has no event-absence guard.

### Issue 7: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by 'vdistCStcAtMostDfltVertDist' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafeDist&&tcpaNonNegative, hvelAtLeastOne&&hdistCStcAtMostHorizDist

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by 'vdistCStcAtMostDfltVertDist' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafeDist&&tcpaNonNegative, hvelAtLeastOne&&hdistCStcAtMostHorizDist
Location: /Users/ranwei/Gitee/forge-d2-run7-lre/java.generated.project/src/main/java/lre/controller/LreController.java:137
```

**Java trace**
  - Java: `LreController.java`:137-143

**Fix directive**
Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive.

### Issue 8: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by 'vvelAtLeastOne && vdistCStcAtMostVertDist' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM, event instanceof lre.event.InputEvent.endTask, event instanceof lre.event.InputEvent.reqHCM — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: LreController.step(): triggerless branch guarded by 'vvelAtLeastOne && vdistCStcAtMostVertDist' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM, event instanceof lre.event.InputEvent.endTask, event instanceof lre.event.InputEvent.reqHCM — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-d2-run7-lre/java.generated.project/src/main/java/lre/controller/LreController.java:140
```

**Java trace**
  - Java: `LreController.java`:140-143

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. RoboChart has no event-absence guard.

### Issue 9: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by 'vvelAtLeastOne && vdistCStcAtMostVertDist' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafeDist&&tcpaNonNegative, hvelAtLeastOne&&hdistCStcAtMostHorizDist, vdistCStcAtMostDfltVertDist

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by 'vvelAtLeastOne && vdistCStcAtMostVertDist' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafeDist&&tcpaNonNegative, hvelAtLeastOne&&hdistCStcAtMostHorizDist, vdistCStcAtMostDfltVertDist
Location: /Users/ranwei/Gitee/forge-d2-run7-lre/java.generated.project/src/main/java/lre/controller/LreController.java:140
```

**Java trace**
  - Java: `LreController.java`:140-143

**Fix directive**
Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive.

### Issue 10: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by 'inOpez' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: LreController.step(): triggerless branch guarded by 'inOpez' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-d2-run7-lre/java.generated.project/src/main/java/lre/controller/LreController.java:148
```

**Java trace**
  - Java: `LreController.java`:148-155

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. RoboChart has no event-absence guard.

### Issue 11: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by 'cdaBelowMinSafeDist && tcpaNonNegative' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: LreController.step(): triggerless branch guarded by 'cdaBelowMinSafeDist && tcpaNonNegative' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-d2-run7-lre/java.generated.project/src/main/java/lre/controller/LreController.java:150
```

**Java trace**
  - Java: `LreController.java`:150-155

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. RoboChart has no event-absence guard.

### Issue 12: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by 'cdaBelowMinSafeDist && tcpaNonNegative' does not negate preceding triggerless guard(s): inOpez

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by 'cdaBelowMinSafeDist && tcpaNonNegative' does not negate preceding triggerless guard(s): inOpez
Location: /Users/ranwei/Gitee/forge-d2-run7-lre/java.generated.project/src/main/java/lre/controller/LreController.java:150
```

**Java trace**
  - Java: `LreController.java`:150-155

**Fix directive**
Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive.

### Issue 13: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by '(!hdistCStcAtMostHorizDist) && (!vdistCStcAtMostVertDist)' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: LreController.step(): triggerless branch guarded by '(!hdistCStcAtMostHorizDist) && (!vdistCStcAtMostVertDist)' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-d2-run7-lre/java.generated.project/src/main/java/lre/controller/LreController.java:152
```

**Java trace**
  - Java: `LreController.java`:152-155

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. RoboChart has no event-absence guard.

### Issue 14: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by '(!hdistCStcAtMostHorizDist) && (!vdistCStcAtMostVertDist)' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafeDist&&tcpaNonNegative

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by '(!hdistCStcAtMostHorizDist) && (!vdistCStcAtMostVertDist)' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafeDist&&tcpaNonNegative
Location: /Users/ranwei/Gitee/forge-d2-run7-lre/java.generated.project/src/main/java/lre/controller/LreController.java:152
```

**Java trace**
  - Java: `LreController.java`:152-155

**Fix directive**
Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive.

### Issue 15: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by '!cdaBelowMinSafeDist' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: LreController.step(): triggerless branch guarded by '!cdaBelowMinSafeDist' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-d2-run7-lre/java.generated.project/src/main/java/lre/controller/LreController.java:160
```

**Java trace**
  - Java: `LreController.java`:160-163

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. RoboChart has no event-absence guard.

### Issue 16: lint_rule7_signed_sentinel_on_controller_state — LreController: controller state field 'cstc' initialised with negative literal -1

**Raw**
```
Rule: rule7_signed_sentinel_on_controller_state
Severity: warning
Message: LreController: controller state field 'cstc' initialised with negative literal -1
Location: /Users/ranwei/Gitee/forge-d2-run7-lre/java.generated.project/src/main/java/lre/controller/LreController.java:52
```

**Java trace**
  - Java: `LreController.java`:52-52

**Fix directive**
Integer controller-state fields historically mapped to RoboChart 'nat', so a negative sentinel silently leaves the formal type's domain and the model diverges from the Java.

### Issue 17: lint_rule7_signed_sentinel_on_controller_state — LreController: controller state field 'cdyn' initialised with negative literal -1

**Raw**
```
Rule: rule7_signed_sentinel_on_controller_state
Severity: warning
Message: LreController: controller state field 'cdyn' initialised with negative literal -1
Location: /Users/ranwei/Gitee/forge-d2-run7-lre/java.generated.project/src/main/java/lre/controller/LreController.java:55
```

**Java trace**
  - Java: `LreController.java`:55-55

**Fix directive**
Integer controller-state fields historically mapped to RoboChart 'nat', so a negative sentinel silently leaves the formal type's domain and the model diverges from the Java.

## Files to review
(none identified)

## Next step
Proceed to Phase 3 (T2M — Spoon Discovery).
