# 2c — Preflight (Structural Lint) — PASSED

## Summary
2c — Preflight (Structural Lint): no structural errors.

## Run history
- New this run: 6
- Recurring from previous run: 0
- Resolved since previous run: 18

**Resolved issue titles**
- LreController.step(): triggerless branch guarded by 'inOpez' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM, event instanceof lre.event.InputEvent.endTask, event instanceof lre.event.InputEvent.reqHCM — in the extracted model both are enabled together
- LreController.step(): triggerless branch guarded by 'cdaBelowMinSafeDist && tcpaNonNegative' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM, event instanceof lre.event.InputEvent.endTask, event instanceof lre.event.InputEvent.reqHCM — in the extracted model both are enabled together
- LreController.step(): branch guarded by 'cdaBelowMinSafeDist && tcpaNonNegative' does not negate preceding triggerless guard(s): inOpez
- LreController.step(): triggerless branch guarded by 'hvelAtLeastOne && hdistCstcAtMostHoriz' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM, event instanceof lre.event.InputEvent.endTask, event instanceof lre.event.InputEvent.reqHCM — in the extracted model both are enabled together
- LreController.step(): branch guarded by 'hvelAtLeastOne && hdistCstcAtMostHoriz' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafeDist&&tcpaNonNegative
- LreController.step(): triggerless branch guarded by 'vdistCstcAtMostDfltVert' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM, event instanceof lre.event.InputEvent.endTask, event instanceof lre.event.InputEvent.reqHCM — in the extracted model both are enabled together
- LreController.step(): branch guarded by 'vdistCstcAtMostDfltVert' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafeDist&&tcpaNonNegative, hvelAtLeastOne&&hdistCstcAtMostHoriz
- LreController.step(): triggerless branch guarded by 'vvelAtLeastOne && vdistCstcAtMostVert' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM, event instanceof lre.event.InputEvent.endTask, event instanceof lre.event.InputEvent.reqHCM — in the extracted model both are enabled together
- LreController.step(): branch guarded by 'vvelAtLeastOne && vdistCstcAtMostVert' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafeDist&&tcpaNonNegative, hvelAtLeastOne&&hdistCstcAtMostHoriz, vdistCstcAtMostDfltVert
- LreController.step(): triggerless branch guarded by 'inOpez' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM — in the extracted model both are enabled together
- LreController.step(): triggerless branch guarded by 'cdaBelowMinSafeDist && tcpaNonNegative' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM — in the extracted model both are enabled together
- LreController.step(): triggerless branch guarded by '(!hdistCstcAtMostHoriz) && (!vdistCstcAtMostVert)' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM — in the extracted model both are enabled together
- LreController.step(): branch guarded by '(!hdistCstcAtMostHoriz) && (!vdistCstcAtMostVert)' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafeDist&&tcpaNonNegative
- LreController.step(): triggerless branch guarded by '!cdaBelowMinSafeDist' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM — in the extracted model both are enabled together
- LreController: controller state field 'cstc' initialised with negative literal -1
- LreController: controller state field 'cdyn' initialised with negative literal -1
- field 'CalcCPA.MIN_REL_SPEED_SQ' is a double without @RoboChartType("real")
- field 'Sensor.SAFE_DISTANCE' is a double without @RoboChartType("real")

## Issues
### Issue 1: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by '(((!inOpez) && (!collisionRisk)) && (!hcmHorizTrigger)) && vdistCstcAtMostDfltVert' does not negate preceding triggerless guard(s): (!collisionRisk)&&hcmHorizTrigger

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by '(((!inOpez) && (!collisionRisk)) && (!hcmHorizTrigger)) && vdistCstcAtMostDfltVert' does not negate preceding triggerless guard(s): (!collisionRisk)&&hcmHorizTrigger
Location: /Users/ranwei/Gitee/forge-d2-run4-lre/java.generated.project/src/main/java/lre/controller/LreController.java:141
```

**Java trace**
  - Java: `LreController.java`:141-159

**Fix directive**
Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive.

### Issue 2: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by '((((!inOpez) && (!collisionRisk)) && (!hcmHorizTrigger)) && (!vdistCstcAtMostDfltVert)) && hcmVertTrigger' does not negate preceding triggerless guard(s): (!collisionRisk)&&hcmHorizTrigger, ((!collisionRisk)&&!hcmHorizTrigger)&&vdistCstcAtMostDfltVert

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by '((((!inOpez) && (!collisionRisk)) && (!hcmHorizTrigger)) && (!vdistCstcAtMostDfltVert)) && hcmVertTrigger' does not negate preceding triggerless guard(s): (!collisionRisk)&&hcmHorizTrigger, ((!collisionRisk)&&!hcmHorizTrigger)&&vdistCstcAtMostDfltVert
Location: /Users/ranwei/Gitee/forge-d2-run4-lre/java.generated.project/src/main/java/lre/controller/LreController.java:144
```

**Java trace**
  - Java: `LreController.java`:144-159

**Fix directive**
Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive.

### Issue 3: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by '(((((event instanceof lre.event.InputEvent.reqOCM) && (!inOpez)) && (!collisionRisk)) && (!hcmHorizTrigger)) && (!vdistCstcAtMostDfltVert)) && (!hcmVertTrigger)' does not negate preceding triggerless guard(s): (!collisionRisk)&&hcmHorizTrigger, ((!collisionRisk)&&!hcmHorizTrigger)&&vdistCstcAtMostDfltVert, (((!collisionRisk)&&!hcmHorizTrigger)&&!vdistCstcAtMostDfltVert)&&hcmVertTrigger

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by '(((((event instanceof lre.event.InputEvent.reqOCM) && (!inOpez)) && (!collisionRisk)) && (!hcmHorizTrigger)) && (!vdistCstcAtMostDfltVert)) && (!hcmVertTrigger)' does not negate preceding triggerless guard(s): (!collisionRisk)&&hcmHorizTrigger, ((!collisionRisk)&&!hcmHorizTrigger)&&vdistCstcAtMostDfltVert, (((!collisionRisk)&&!hcmHorizTrigger)&&!vdistCstcAtMostDfltVert)&&hcmVertTrigger
Location: /Users/ranwei/Gitee/forge-d2-run4-lre/java.generated.project/src/main/java/lre/controller/LreController.java:148
```

**Java trace**
  - Java: `LreController.java`:148-159

**Fix directive**
Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive.

### Issue 4: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by '(((((event instanceof lre.event.InputEvent.endTask) && (!inOpez)) && (!collisionRisk)) && (!hcmHorizTrigger)) && (!vdistCstcAtMostDfltVert)) && (!hcmVertTrigger)' does not negate preceding triggerless guard(s): (!collisionRisk)&&hcmHorizTrigger, ((!collisionRisk)&&!hcmHorizTrigger)&&vdistCstcAtMostDfltVert, (((!collisionRisk)&&!hcmHorizTrigger)&&!vdistCstcAtMostDfltVert)&&hcmVertTrigger

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by '(((((event instanceof lre.event.InputEvent.endTask) && (!inOpez)) && (!collisionRisk)) && (!hcmHorizTrigger)) && (!vdistCstcAtMostDfltVert)) && (!hcmVertTrigger)' does not negate preceding triggerless guard(s): (!collisionRisk)&&hcmHorizTrigger, ((!collisionRisk)&&!hcmHorizTrigger)&&vdistCstcAtMostDfltVert, (((!collisionRisk)&&!hcmHorizTrigger)&&!vdistCstcAtMostDfltVert)&&hcmVertTrigger
Location: /Users/ranwei/Gitee/forge-d2-run4-lre/java.generated.project/src/main/java/lre/controller/LreController.java:151
```

**Java trace**
  - Java: `LreController.java`:151-159

**Fix directive**
Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive.

### Issue 5: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by '(((((event instanceof lre.event.InputEvent.reqHCM) && (!inOpez)) && (!collisionRisk)) && (!hcmHorizTrigger)) && (!vdistCstcAtMostDfltVert)) && (!hcmVertTrigger)' does not negate preceding triggerless guard(s): (!collisionRisk)&&hcmHorizTrigger, ((!collisionRisk)&&!hcmHorizTrigger)&&vdistCstcAtMostDfltVert, (((!collisionRisk)&&!hcmHorizTrigger)&&!vdistCstcAtMostDfltVert)&&hcmVertTrigger

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by '(((((event instanceof lre.event.InputEvent.reqHCM) && (!inOpez)) && (!collisionRisk)) && (!hcmHorizTrigger)) && (!vdistCstcAtMostDfltVert)) && (!hcmVertTrigger)' does not negate preceding triggerless guard(s): (!collisionRisk)&&hcmHorizTrigger, ((!collisionRisk)&&!hcmHorizTrigger)&&vdistCstcAtMostDfltVert, (((!collisionRisk)&&!hcmHorizTrigger)&&!vdistCstcAtMostDfltVert)&&hcmVertTrigger
Location: /Users/ranwei/Gitee/forge-d2-run4-lre/java.generated.project/src/main/java/lre/controller/LreController.java:155
```

**Java trace**
  - Java: `LreController.java`:155-159

**Fix directive**
Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive.

### Issue 6: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by '(((event instanceof lre.event.InputEvent.reqOCM) && (!inOpez)) && (!collisionRisk)) && (!staticObsCleared)' does not negate preceding triggerless guard(s): (!collisionRisk)&&staticObsCleared

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by '(((event instanceof lre.event.InputEvent.reqOCM) && (!inOpez)) && (!collisionRisk)) && (!staticObsCleared)' does not negate preceding triggerless guard(s): (!collisionRisk)&&staticObsCleared
Location: /Users/ranwei/Gitee/forge-d2-run4-lre/java.generated.project/src/main/java/lre/controller/LreController.java:169
```

**Java trace**
  - Java: `LreController.java`:169-172

**Fix directive**
Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive.

## Files to review
(none identified)

## Next step
Proceed to Phase 3 (T2M — Spoon Discovery).
