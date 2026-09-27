# 2c — Preflight (Structural Lint) — PASSED

## Summary
2c — Preflight (Structural Lint): no structural errors.

## Run history
**WARNING — thrashing detected.** 17 issue(s) have recurred 3+ runs in a row despite fixes: `LreController.step(): triggerless branch guarded by 'inOpez' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM, event instanceof lre.event.InputEvent.endTask, event instanceof lre.event.InputEvent.reqHCM — in the extracted model both are enabled together`, `LreController.step(): triggerless branch guarded by 'cdaBelowMinSafe && tcpaNonNegative' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM, event instanceof lre.event.InputEvent.endTask, event instanceof lre.event.InputEvent.reqHCM — in the extracted model both are enabled together`, `LreController.step(): branch guarded by 'cdaBelowMinSafe && tcpaNonNegative' does not negate preceding triggerless guard(s): inOpez`, `LreController.step(): triggerless branch guarded by 'hvelAtLeastOne && hdistCstcAtMostHoriz' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM, event instanceof lre.event.InputEvent.endTask, event instanceof lre.event.InputEvent.reqHCM — in the extracted model both are enabled together`, `LreController.step(): branch guarded by 'hvelAtLeastOne && hdistCstcAtMostHoriz' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafe&&tcpaNonNegative`, `LreController.step(): triggerless branch guarded by 'vdistCstcAtMostDfltVert' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM, event instanceof lre.event.InputEvent.endTask, event instanceof lre.event.InputEvent.reqHCM — in the extracted model both are enabled together`, `LreController.step(): branch guarded by 'vdistCstcAtMostDfltVert' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafe&&tcpaNonNegative, hvelAtLeastOne&&hdistCstcAtMostHoriz`, `LreController.step(): triggerless branch guarded by 'vvelAtLeastOne && vdistCstcAtMostVert' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM, event instanceof lre.event.InputEvent.endTask, event instanceof lre.event.InputEvent.reqHCM — in the extracted model both are enabled together`, `LreController.step(): branch guarded by 'vvelAtLeastOne && vdistCstcAtMostVert' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafe&&tcpaNonNegative, hvelAtLeastOne&&hdistCstcAtMostHoriz, vdistCstcAtMostDfltVert`, `LreController.step(): triggerless branch guarded by 'inOpez' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM — in the extracted model both are enabled together`, `LreController.step(): triggerless branch guarded by 'cdaBelowMinSafe && tcpaNonNegative' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM — in the extracted model both are enabled together`, `LreController.step(): branch guarded by 'cdaBelowMinSafe && tcpaNonNegative' does not negate preceding triggerless guard(s): inOpez`, `LreController.step(): triggerless branch guarded by '(!hdistCstcAtMostHoriz) && (!vdistCstcAtMostVert)' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM — in the extracted model both are enabled together`, `LreController.step(): branch guarded by '(!hdistCstcAtMostHoriz) && (!vdistCstcAtMostVert)' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafe&&tcpaNonNegative`, `LreController.step(): triggerless branch guarded by '!cdaBelowMinSafe' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM — in the extracted model both are enabled together`, `LreController: controller state field 'cstc' initialised with negative literal -1`, `LreController: controller state field 'cdyn' initialised with negative literal -1`. Previous fix strategies are not working — change approach (e.g. re-read the originating requirement, or escalate to the user).

- New this run: 0
- Recurring from previous run: 17
- Resolved since previous run: 0

## Issues
### Issue 1: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by 'inOpez' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM, event instanceof lre.event.InputEvent.endTask, event instanceof lre.event.InputEvent.reqHCM — in the extracted model both are enabled together [RECURRING x3 — fix strategy failing]

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: LreController.step(): triggerless branch guarded by 'inOpez' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM, event instanceof lre.event.InputEvent.endTask, event instanceof lre.event.InputEvent.reqHCM — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-d2-run6-lre/java.generated.project/src/main/java/lre/controller/LreController.java:134
```

**Java trace**
  - Java: `LreController.java`:134-147

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. RoboChart has no event-absence guard.

### Issue 2: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by 'cdaBelowMinSafe && tcpaNonNegative' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM, event instanceof lre.event.InputEvent.endTask, event instanceof lre.event.InputEvent.reqHCM — in the extracted model both are enabled together [RECURRING x3 — fix strategy failing]

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: LreController.step(): triggerless branch guarded by 'cdaBelowMinSafe && tcpaNonNegative' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM, event instanceof lre.event.InputEvent.endTask, event instanceof lre.event.InputEvent.reqHCM — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-d2-run6-lre/java.generated.project/src/main/java/lre/controller/LreController.java:136
```

**Java trace**
  - Java: `LreController.java`:136-147

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. RoboChart has no event-absence guard.

### Issue 3: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by 'cdaBelowMinSafe && tcpaNonNegative' does not negate preceding triggerless guard(s): inOpez [RECURRING x3 — fix strategy failing]

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by 'cdaBelowMinSafe && tcpaNonNegative' does not negate preceding triggerless guard(s): inOpez
Location: /Users/ranwei/Gitee/forge-d2-run6-lre/java.generated.project/src/main/java/lre/controller/LreController.java:136
```

**Java trace**
  - Java: `LreController.java`:136-147

**Fix directive**
Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive.

### Issue 4: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by 'hvelAtLeastOne && hdistCstcAtMostHoriz' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM, event instanceof lre.event.InputEvent.endTask, event instanceof lre.event.InputEvent.reqHCM — in the extracted model both are enabled together [RECURRING x3 — fix strategy failing]

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: LreController.step(): triggerless branch guarded by 'hvelAtLeastOne && hdistCstcAtMostHoriz' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM, event instanceof lre.event.InputEvent.endTask, event instanceof lre.event.InputEvent.reqHCM — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-d2-run6-lre/java.generated.project/src/main/java/lre/controller/LreController.java:138
```

**Java trace**
  - Java: `LreController.java`:138-147

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. RoboChart has no event-absence guard.

### Issue 5: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by 'hvelAtLeastOne && hdistCstcAtMostHoriz' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafe&&tcpaNonNegative [RECURRING x3 — fix strategy failing]

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by 'hvelAtLeastOne && hdistCstcAtMostHoriz' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafe&&tcpaNonNegative
Location: /Users/ranwei/Gitee/forge-d2-run6-lre/java.generated.project/src/main/java/lre/controller/LreController.java:138
```

**Java trace**
  - Java: `LreController.java`:138-147

**Fix directive**
Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive.

### Issue 6: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by 'vdistCstcAtMostDfltVert' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM, event instanceof lre.event.InputEvent.endTask, event instanceof lre.event.InputEvent.reqHCM — in the extracted model both are enabled together [RECURRING x3 — fix strategy failing]

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: LreController.step(): triggerless branch guarded by 'vdistCstcAtMostDfltVert' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM, event instanceof lre.event.InputEvent.endTask, event instanceof lre.event.InputEvent.reqHCM — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-d2-run6-lre/java.generated.project/src/main/java/lre/controller/LreController.java:141
```

**Java trace**
  - Java: `LreController.java`:141-147

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. RoboChart has no event-absence guard.

### Issue 7: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by 'vdistCstcAtMostDfltVert' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafe&&tcpaNonNegative, hvelAtLeastOne&&hdistCstcAtMostHoriz [RECURRING x3 — fix strategy failing]

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by 'vdistCstcAtMostDfltVert' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafe&&tcpaNonNegative, hvelAtLeastOne&&hdistCstcAtMostHoriz
Location: /Users/ranwei/Gitee/forge-d2-run6-lre/java.generated.project/src/main/java/lre/controller/LreController.java:141
```

**Java trace**
  - Java: `LreController.java`:141-147

**Fix directive**
Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive.

### Issue 8: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by 'vvelAtLeastOne && vdistCstcAtMostVert' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM, event instanceof lre.event.InputEvent.endTask, event instanceof lre.event.InputEvent.reqHCM — in the extracted model both are enabled together [RECURRING x3 — fix strategy failing]

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: LreController.step(): triggerless branch guarded by 'vvelAtLeastOne && vdistCstcAtMostVert' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM, event instanceof lre.event.InputEvent.endTask, event instanceof lre.event.InputEvent.reqHCM — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-d2-run6-lre/java.generated.project/src/main/java/lre/controller/LreController.java:144
```

**Java trace**
  - Java: `LreController.java`:144-147

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. RoboChart has no event-absence guard.

### Issue 9: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by 'vvelAtLeastOne && vdistCstcAtMostVert' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafe&&tcpaNonNegative, hvelAtLeastOne&&hdistCstcAtMostHoriz, vdistCstcAtMostDfltVert [RECURRING x3 — fix strategy failing]

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by 'vvelAtLeastOne && vdistCstcAtMostVert' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafe&&tcpaNonNegative, hvelAtLeastOne&&hdistCstcAtMostHoriz, vdistCstcAtMostDfltVert
Location: /Users/ranwei/Gitee/forge-d2-run6-lre/java.generated.project/src/main/java/lre/controller/LreController.java:144
```

**Java trace**
  - Java: `LreController.java`:144-147

**Fix directive**
Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive.

### Issue 10: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by 'inOpez' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM — in the extracted model both are enabled together [RECURRING x3 — fix strategy failing]

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: LreController.step(): triggerless branch guarded by 'inOpez' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-d2-run6-lre/java.generated.project/src/main/java/lre/controller/LreController.java:152
```

**Java trace**
  - Java: `LreController.java`:152-159

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. RoboChart has no event-absence guard.

### Issue 11: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by 'cdaBelowMinSafe && tcpaNonNegative' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM — in the extracted model both are enabled together [RECURRING x3 — fix strategy failing]

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: LreController.step(): triggerless branch guarded by 'cdaBelowMinSafe && tcpaNonNegative' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-d2-run6-lre/java.generated.project/src/main/java/lre/controller/LreController.java:154
```

**Java trace**
  - Java: `LreController.java`:154-159

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. RoboChart has no event-absence guard.

### Issue 12: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by 'cdaBelowMinSafe && tcpaNonNegative' does not negate preceding triggerless guard(s): inOpez [RECURRING x3 — fix strategy failing]

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by 'cdaBelowMinSafe && tcpaNonNegative' does not negate preceding triggerless guard(s): inOpez
Location: /Users/ranwei/Gitee/forge-d2-run6-lre/java.generated.project/src/main/java/lre/controller/LreController.java:154
```

**Java trace**
  - Java: `LreController.java`:154-159

**Fix directive**
Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive.

### Issue 13: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by '(!hdistCstcAtMostHoriz) && (!vdistCstcAtMostVert)' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM — in the extracted model both are enabled together [RECURRING x3 — fix strategy failing]

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: LreController.step(): triggerless branch guarded by '(!hdistCstcAtMostHoriz) && (!vdistCstcAtMostVert)' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-d2-run6-lre/java.generated.project/src/main/java/lre/controller/LreController.java:156
```

**Java trace**
  - Java: `LreController.java`:156-159

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. RoboChart has no event-absence guard.

### Issue 14: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by '(!hdistCstcAtMostHoriz) && (!vdistCstcAtMostVert)' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafe&&tcpaNonNegative [RECURRING x3 — fix strategy failing]

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by '(!hdistCstcAtMostHoriz) && (!vdistCstcAtMostVert)' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafe&&tcpaNonNegative
Location: /Users/ranwei/Gitee/forge-d2-run6-lre/java.generated.project/src/main/java/lre/controller/LreController.java:156
```

**Java trace**
  - Java: `LreController.java`:156-159

**Fix directive**
Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive.

### Issue 15: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by '!cdaBelowMinSafe' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM — in the extracted model both are enabled together [RECURRING x3 — fix strategy failing]

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: LreController.step(): triggerless branch guarded by '!cdaBelowMinSafe' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-d2-run6-lre/java.generated.project/src/main/java/lre/controller/LreController.java:164
```

**Java trace**
  - Java: `LreController.java`:164-167

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. RoboChart has no event-absence guard.

### Issue 16: lint_rule7_signed_sentinel_on_controller_state — LreController: controller state field 'cstc' initialised with negative literal -1 [RECURRING x3 — fix strategy failing]

**Raw**
```
Rule: rule7_signed_sentinel_on_controller_state
Severity: warning
Message: LreController: controller state field 'cstc' initialised with negative literal -1
Location: /Users/ranwei/Gitee/forge-d2-run6-lre/java.generated.project/src/main/java/lre/controller/LreController.java:44
```

**Java trace**
  - Java: `LreController.java`:44-44

**Fix directive**
Integer controller-state fields historically mapped to RoboChart 'nat', so a negative sentinel silently leaves the formal type's domain and the model diverges from the Java.

### Issue 17: lint_rule7_signed_sentinel_on_controller_state — LreController: controller state field 'cdyn' initialised with negative literal -1 [RECURRING x3 — fix strategy failing]

**Raw**
```
Rule: rule7_signed_sentinel_on_controller_state
Severity: warning
Message: LreController: controller state field 'cdyn' initialised with negative literal -1
Location: /Users/ranwei/Gitee/forge-d2-run6-lre/java.generated.project/src/main/java/lre/controller/LreController.java:47
```

**Java trace**
  - Java: `LreController.java`:47-47

**Fix directive**
Integer controller-state fields historically mapped to RoboChart 'nat', so a negative sentinel silently leaves the formal type's domain and the model diverges from the Java.

## Files to review
(none identified)

## Next step
Proceed to Phase 3 (T2M — Spoon Discovery).
