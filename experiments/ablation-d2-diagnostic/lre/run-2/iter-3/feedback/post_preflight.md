# 2c — Preflight (Structural Lint) — PASSED

## Summary
2c — Preflight (Structural Lint): no structural errors.

## Run history
- New this run: 11
- Recurring from previous run: 0
- Resolved since previous run: 6

**Resolved issue titles**
- LreController.step(): branch guarded by '(((!inOpez) && (!collisionRisk)) && (!hcmHorizTrigger)) && vdistCstcAtMostDfltVert' does not negate preceding triggerless guard(s): (!collisionRisk)&&hcmHorizTrigger
- LreController.step(): branch guarded by '((((!inOpez) && (!collisionRisk)) && (!hcmHorizTrigger)) && (!vdistCstcAtMostDfltVert)) && hcmVertTrigger' does not negate preceding triggerless guard(s): (!collisionRisk)&&hcmHorizTrigger, ((!collisionRisk)&&!hcmHorizTrigger)&&vdistCstcAtMostDfltVert
- LreController.step(): branch guarded by '(((((event instanceof lre.event.InputEvent.reqOCM) && (!inOpez)) && (!collisionRisk)) && (!hcmHorizTrigger)) && (!vdistCstcAtMostDfltVert)) && (!hcmVertTrigger)' does not negate preceding triggerless guard(s): (!collisionRisk)&&hcmHorizTrigger, ((!collisionRisk)&&!hcmHorizTrigger)&&vdistCstcAtMostDfltVert, (((!collisionRisk)&&!hcmHorizTrigger)&&!vdistCstcAtMostDfltVert)&&hcmVertTrigger
- LreController.step(): branch guarded by '(((((event instanceof lre.event.InputEvent.endTask) && (!inOpez)) && (!collisionRisk)) && (!hcmHorizTrigger)) && (!vdistCstcAtMostDfltVert)) && (!hcmVertTrigger)' does not negate preceding triggerless guard(s): (!collisionRisk)&&hcmHorizTrigger, ((!collisionRisk)&&!hcmHorizTrigger)&&vdistCstcAtMostDfltVert, (((!collisionRisk)&&!hcmHorizTrigger)&&!vdistCstcAtMostDfltVert)&&hcmVertTrigger
- LreController.step(): branch guarded by '(((((event instanceof lre.event.InputEvent.reqHCM) && (!inOpez)) && (!collisionRisk)) && (!hcmHorizTrigger)) && (!vdistCstcAtMostDfltVert)) && (!hcmVertTrigger)' does not negate preceding triggerless guard(s): (!collisionRisk)&&hcmHorizTrigger, ((!collisionRisk)&&!hcmHorizTrigger)&&vdistCstcAtMostDfltVert, (((!collisionRisk)&&!hcmHorizTrigger)&&!vdistCstcAtMostDfltVert)&&hcmVertTrigger
- LreController.step(): branch guarded by '(((event instanceof lre.event.InputEvent.reqOCM) && (!inOpez)) && (!collisionRisk)) && (!staticObsCleared)' does not negate preceding triggerless guard(s): (!collisionRisk)&&staticObsCleared

## Issues
### Issue 1: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by 'collisionRisk' does not negate preceding triggerless guard(s): inOpez

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by 'collisionRisk' does not negate preceding triggerless guard(s): inOpez
Location: /Users/ranwei/Gitee/forge-d2-run4-lre/java.generated.project/src/main/java/lre/controller/LreController.java:136
```

**Java trace**
  - Java: `LreController.java`:136-155

**Fix directive**
Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive.

### Issue 2: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by 'hcmHorizTrigger' does not negate preceding triggerless guard(s): inOpez, collisionRisk

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by 'hcmHorizTrigger' does not negate preceding triggerless guard(s): inOpez, collisionRisk
Location: /Users/ranwei/Gitee/forge-d2-run4-lre/java.generated.project/src/main/java/lre/controller/LreController.java:138
```

**Java trace**
  - Java: `LreController.java`:138-155

**Fix directive**
Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive.

### Issue 3: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by 'vdistCstcAtMostDfltVert' does not negate preceding triggerless guard(s): inOpez, collisionRisk, hcmHorizTrigger

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by 'vdistCstcAtMostDfltVert' does not negate preceding triggerless guard(s): inOpez, collisionRisk, hcmHorizTrigger
Location: /Users/ranwei/Gitee/forge-d2-run4-lre/java.generated.project/src/main/java/lre/controller/LreController.java:141
```

**Java trace**
  - Java: `LreController.java`:141-155

**Fix directive**
Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive.

### Issue 4: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by 'hcmVertTrigger' does not negate preceding triggerless guard(s): inOpez, collisionRisk, hcmHorizTrigger, vdistCstcAtMostDfltVert

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by 'hcmVertTrigger' does not negate preceding triggerless guard(s): inOpez, collisionRisk, hcmHorizTrigger, vdistCstcAtMostDfltVert
Location: /Users/ranwei/Gitee/forge-d2-run4-lre/java.generated.project/src/main/java/lre/controller/LreController.java:144
```

**Java trace**
  - Java: `LreController.java`:144-155

**Fix directive**
Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive.

### Issue 5: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by 'event instanceof lre.event.InputEvent.reqOCM' does not negate preceding triggerless guard(s): inOpez, collisionRisk, hcmHorizTrigger, vdistCstcAtMostDfltVert, hcmVertTrigger

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by 'event instanceof lre.event.InputEvent.reqOCM' does not negate preceding triggerless guard(s): inOpez, collisionRisk, hcmHorizTrigger, vdistCstcAtMostDfltVert, hcmVertTrigger
Location: /Users/ranwei/Gitee/forge-d2-run4-lre/java.generated.project/src/main/java/lre/controller/LreController.java:147
```

**Java trace**
  - Java: `LreController.java`:147-155

**Fix directive**
Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive.

### Issue 6: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by 'event instanceof lre.event.InputEvent.endTask' does not negate preceding triggerless guard(s): inOpez, collisionRisk, hcmHorizTrigger, vdistCstcAtMostDfltVert, hcmVertTrigger

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by 'event instanceof lre.event.InputEvent.endTask' does not negate preceding triggerless guard(s): inOpez, collisionRisk, hcmHorizTrigger, vdistCstcAtMostDfltVert, hcmVertTrigger
Location: /Users/ranwei/Gitee/forge-d2-run4-lre/java.generated.project/src/main/java/lre/controller/LreController.java:149
```

**Java trace**
  - Java: `LreController.java`:149-155

**Fix directive**
Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive.

### Issue 7: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by 'event instanceof lre.event.InputEvent.reqHCM' does not negate preceding triggerless guard(s): inOpez, collisionRisk, hcmHorizTrigger, vdistCstcAtMostDfltVert, hcmVertTrigger

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by 'event instanceof lre.event.InputEvent.reqHCM' does not negate preceding triggerless guard(s): inOpez, collisionRisk, hcmHorizTrigger, vdistCstcAtMostDfltVert, hcmVertTrigger
Location: /Users/ranwei/Gitee/forge-d2-run4-lre/java.generated.project/src/main/java/lre/controller/LreController.java:152
```

**Java trace**
  - Java: `LreController.java`:152-155

**Fix directive**
Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive.

### Issue 8: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by 'collisionRisk' does not negate preceding triggerless guard(s): inOpez

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by 'collisionRisk' does not negate preceding triggerless guard(s): inOpez
Location: /Users/ranwei/Gitee/forge-d2-run4-lre/java.generated.project/src/main/java/lre/controller/LreController.java:160
```

**Java trace**
  - Java: `LreController.java`:160-167

**Fix directive**
Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive.

### Issue 9: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by 'staticObsCleared' does not negate preceding triggerless guard(s): inOpez, collisionRisk

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by 'staticObsCleared' does not negate preceding triggerless guard(s): inOpez, collisionRisk
Location: /Users/ranwei/Gitee/forge-d2-run4-lre/java.generated.project/src/main/java/lre/controller/LreController.java:162
```

**Java trace**
  - Java: `LreController.java`:162-167

**Fix directive**
Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive.

### Issue 10: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by 'event instanceof lre.event.InputEvent.reqOCM' does not negate preceding triggerless guard(s): inOpez, collisionRisk, staticObsCleared

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by 'event instanceof lre.event.InputEvent.reqOCM' does not negate preceding triggerless guard(s): inOpez, collisionRisk, staticObsCleared
Location: /Users/ranwei/Gitee/forge-d2-run4-lre/java.generated.project/src/main/java/lre/controller/LreController.java:165
```

**Java trace**
  - Java: `LreController.java`:165-167

**Fix directive**
Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive.

### Issue 11: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by 'event instanceof lre.event.InputEvent.reqOCM' does not negate preceding triggerless guard(s): collisionCleared

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by 'event instanceof lre.event.InputEvent.reqOCM' does not negate preceding triggerless guard(s): collisionCleared
Location: /Users/ranwei/Gitee/forge-d2-run4-lre/java.generated.project/src/main/java/lre/controller/LreController.java:173
```

**Java trace**
  - Java: `LreController.java`:173-175

**Fix directive**
Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive.

## Files to review
(none identified)

## Next step
Proceed to Phase 3 (T2M — Spoon Discovery).
