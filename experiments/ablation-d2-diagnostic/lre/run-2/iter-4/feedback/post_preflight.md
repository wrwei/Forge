# 2c — Preflight (Structural Lint) — PASSED

## Summary
2c — Preflight (Structural Lint): no structural errors.

## Run history
- New this run: 11
- Recurring from previous run: 0
- Resolved since previous run: 10

**Resolved issue titles**
- LreController.step(): branch guarded by 'collisionRisk' does not negate preceding triggerless guard(s): inOpez
- LreController.step(): branch guarded by 'hcmHorizTrigger' does not negate preceding triggerless guard(s): inOpez, collisionRisk
- LreController.step(): branch guarded by 'vdistCstcAtMostDfltVert' does not negate preceding triggerless guard(s): inOpez, collisionRisk, hcmHorizTrigger
- LreController.step(): branch guarded by 'hcmVertTrigger' does not negate preceding triggerless guard(s): inOpez, collisionRisk, hcmHorizTrigger, vdistCstcAtMostDfltVert
- LreController.step(): branch guarded by 'event instanceof lre.event.InputEvent.reqOCM' does not negate preceding triggerless guard(s): inOpez, collisionRisk, hcmHorizTrigger, vdistCstcAtMostDfltVert, hcmVertTrigger
- LreController.step(): branch guarded by 'event instanceof lre.event.InputEvent.endTask' does not negate preceding triggerless guard(s): inOpez, collisionRisk, hcmHorizTrigger, vdistCstcAtMostDfltVert, hcmVertTrigger
- LreController.step(): branch guarded by 'event instanceof lre.event.InputEvent.reqHCM' does not negate preceding triggerless guard(s): inOpez, collisionRisk, hcmHorizTrigger, vdistCstcAtMostDfltVert, hcmVertTrigger
- LreController.step(): branch guarded by 'staticObsCleared' does not negate preceding triggerless guard(s): inOpez, collisionRisk
- LreController.step(): branch guarded by 'event instanceof lre.event.InputEvent.reqOCM' does not negate preceding triggerless guard(s): inOpez, collisionRisk, staticObsCleared
- LreController.step(): branch guarded by 'event instanceof lre.event.InputEvent.reqOCM' does not negate preceding triggerless guard(s): collisionCleared

## Issues
### Issue 1: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by 'cdaBelowMinSafeDist && tcpaNonNegative' does not negate preceding triggerless guard(s): inOpez

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by 'cdaBelowMinSafeDist && tcpaNonNegative' does not negate preceding triggerless guard(s): inOpez
Location: /Users/ranwei/Gitee/forge-d2-run4-lre/java.generated.project/src/main/java/lre/controller/LreController.java:131
```

**Java trace**
  - Java: `LreController.java`:131-150

**Fix directive**
Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive.

### Issue 2: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by 'hvelAtLeastOne && hdistCstcAtMostHoriz' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafeDist&&tcpaNonNegative

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by 'hvelAtLeastOne && hdistCstcAtMostHoriz' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafeDist&&tcpaNonNegative
Location: /Users/ranwei/Gitee/forge-d2-run4-lre/java.generated.project/src/main/java/lre/controller/LreController.java:133
```

**Java trace**
  - Java: `LreController.java`:133-150

**Fix directive**
Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive.

### Issue 3: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by 'vdistCstcAtMostDfltVert' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafeDist&&tcpaNonNegative, hvelAtLeastOne&&hdistCstcAtMostHoriz

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by 'vdistCstcAtMostDfltVert' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafeDist&&tcpaNonNegative, hvelAtLeastOne&&hdistCstcAtMostHoriz
Location: /Users/ranwei/Gitee/forge-d2-run4-lre/java.generated.project/src/main/java/lre/controller/LreController.java:136
```

**Java trace**
  - Java: `LreController.java`:136-150

**Fix directive**
Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive.

### Issue 4: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by 'vvelAtLeastOne && vdistCstcAtMostVert' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafeDist&&tcpaNonNegative, hvelAtLeastOne&&hdistCstcAtMostHoriz, vdistCstcAtMostDfltVert

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by 'vvelAtLeastOne && vdistCstcAtMostVert' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafeDist&&tcpaNonNegative, hvelAtLeastOne&&hdistCstcAtMostHoriz, vdistCstcAtMostDfltVert
Location: /Users/ranwei/Gitee/forge-d2-run4-lre/java.generated.project/src/main/java/lre/controller/LreController.java:139
```

**Java trace**
  - Java: `LreController.java`:139-150

**Fix directive**
Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive.

### Issue 5: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by 'event instanceof lre.event.InputEvent.reqOCM' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafeDist&&tcpaNonNegative, hvelAtLeastOne&&hdistCstcAtMostHoriz, vdistCstcAtMostDfltVert, vvelAtLeastOne&&vdistCstcAtMostVert

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by 'event instanceof lre.event.InputEvent.reqOCM' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafeDist&&tcpaNonNegative, hvelAtLeastOne&&hdistCstcAtMostHoriz, vdistCstcAtMostDfltVert, vvelAtLeastOne&&vdistCstcAtMostVert
Location: /Users/ranwei/Gitee/forge-d2-run4-lre/java.generated.project/src/main/java/lre/controller/LreController.java:142
```

**Java trace**
  - Java: `LreController.java`:142-150

**Fix directive**
Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive.

### Issue 6: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by 'event instanceof lre.event.InputEvent.endTask' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafeDist&&tcpaNonNegative, hvelAtLeastOne&&hdistCstcAtMostHoriz, vdistCstcAtMostDfltVert, vvelAtLeastOne&&vdistCstcAtMostVert

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by 'event instanceof lre.event.InputEvent.endTask' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafeDist&&tcpaNonNegative, hvelAtLeastOne&&hdistCstcAtMostHoriz, vdistCstcAtMostDfltVert, vvelAtLeastOne&&vdistCstcAtMostVert
Location: /Users/ranwei/Gitee/forge-d2-run4-lre/java.generated.project/src/main/java/lre/controller/LreController.java:144
```

**Java trace**
  - Java: `LreController.java`:144-150

**Fix directive**
Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive.

### Issue 7: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by 'event instanceof lre.event.InputEvent.reqHCM' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafeDist&&tcpaNonNegative, hvelAtLeastOne&&hdistCstcAtMostHoriz, vdistCstcAtMostDfltVert, vvelAtLeastOne&&vdistCstcAtMostVert

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by 'event instanceof lre.event.InputEvent.reqHCM' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafeDist&&tcpaNonNegative, hvelAtLeastOne&&hdistCstcAtMostHoriz, vdistCstcAtMostDfltVert, vvelAtLeastOne&&vdistCstcAtMostVert
Location: /Users/ranwei/Gitee/forge-d2-run4-lre/java.generated.project/src/main/java/lre/controller/LreController.java:147
```

**Java trace**
  - Java: `LreController.java`:147-150

**Fix directive**
Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive.

### Issue 8: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by 'cdaBelowMinSafeDist && tcpaNonNegative' does not negate preceding triggerless guard(s): inOpez

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by 'cdaBelowMinSafeDist && tcpaNonNegative' does not negate preceding triggerless guard(s): inOpez
Location: /Users/ranwei/Gitee/forge-d2-run4-lre/java.generated.project/src/main/java/lre/controller/LreController.java:155
```

**Java trace**
  - Java: `LreController.java`:155-162

**Fix directive**
Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive.

### Issue 9: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by '(!hdistCstcAtMostHoriz) && (!vdistCstcAtMostVert)' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafeDist&&tcpaNonNegative

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by '(!hdistCstcAtMostHoriz) && (!vdistCstcAtMostVert)' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafeDist&&tcpaNonNegative
Location: /Users/ranwei/Gitee/forge-d2-run4-lre/java.generated.project/src/main/java/lre/controller/LreController.java:157
```

**Java trace**
  - Java: `LreController.java`:157-162

**Fix directive**
Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive.

### Issue 10: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by 'event instanceof lre.event.InputEvent.reqOCM' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafeDist&&tcpaNonNegative, !hdistCstcAtMostHoriz&&!vdistCstcAtMostVert

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by 'event instanceof lre.event.InputEvent.reqOCM' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafeDist&&tcpaNonNegative, !hdistCstcAtMostHoriz&&!vdistCstcAtMostVert
Location: /Users/ranwei/Gitee/forge-d2-run4-lre/java.generated.project/src/main/java/lre/controller/LreController.java:160
```

**Java trace**
  - Java: `LreController.java`:160-162

**Fix directive**
Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive.

### Issue 11: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by 'event instanceof lre.event.InputEvent.reqOCM' does not negate preceding triggerless guard(s): !cdaBelowMinSafeDist

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by 'event instanceof lre.event.InputEvent.reqOCM' does not negate preceding triggerless guard(s): !cdaBelowMinSafeDist
Location: /Users/ranwei/Gitee/forge-d2-run4-lre/java.generated.project/src/main/java/lre/controller/LreController.java:168
```

**Java trace**
  - Java: `LreController.java`:168-170

**Fix directive**
Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive.

## Files to review
(none identified)

## Next step
Proceed to Phase 3 (T2M — Spoon Discovery).
