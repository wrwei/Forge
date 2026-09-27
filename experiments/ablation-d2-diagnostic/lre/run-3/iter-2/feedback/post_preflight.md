# 2c — Preflight (Structural Lint) — PASSED

## Summary
2c — Preflight (Structural Lint): no structural errors.

## Run history
- New this run: 7
- Recurring from previous run: 0
- Resolved since previous run: 16

**Resolved issue titles**
- LreController.step(): triggerless branch guarded by 'inOpez' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM, event instanceof lre.event.InputEvent.endTask, event instanceof lre.event.InputEvent.reqHCM — in the extracted model both are enabled together
- LreController.step(): triggerless branch guarded by 'cdaBelowMinSafeDist && tcpaNonNegative' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM, event instanceof lre.event.InputEvent.endTask, event instanceof lre.event.InputEvent.reqHCM — in the extracted model both are enabled together
- LreController.step(): branch guarded by 'cdaBelowMinSafeDist && tcpaNonNegative' does not negate preceding triggerless guard(s): inOpez
- LreController.step(): triggerless branch guarded by 'hvelAtLeastOne && hdistCstcAtMostHorizLimit' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM, event instanceof lre.event.InputEvent.endTask, event instanceof lre.event.InputEvent.reqHCM — in the extracted model both are enabled together
- LreController.step(): branch guarded by 'hvelAtLeastOne && hdistCstcAtMostHorizLimit' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafeDist&&tcpaNonNegative
- LreController.step(): triggerless branch guarded by 'vdistCstcAtMostDfltVertLimit' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM, event instanceof lre.event.InputEvent.endTask, event instanceof lre.event.InputEvent.reqHCM — in the extracted model both are enabled together
- LreController.step(): branch guarded by 'vdistCstcAtMostDfltVertLimit' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafeDist&&tcpaNonNegative, hvelAtLeastOne&&hdistCstcAtMostHorizLimit
- LreController.step(): triggerless branch guarded by 'vvelAtLeastOne && vdistCstcAtMostVertLimit' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM, event instanceof lre.event.InputEvent.endTask, event instanceof lre.event.InputEvent.reqHCM — in the extracted model both are enabled together
- LreController.step(): branch guarded by 'vvelAtLeastOne && vdistCstcAtMostVertLimit' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafeDist&&tcpaNonNegative, hvelAtLeastOne&&hdistCstcAtMostHorizLimit, vdistCstcAtMostDfltVertLimit
- LreController.step(): triggerless branch guarded by 'inOpez' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM — in the extracted model both are enabled together
- LreController.step(): triggerless branch guarded by 'cdaBelowMinSafeDist && tcpaNonNegative' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM — in the extracted model both are enabled together
- LreController.step(): triggerless branch guarded by '(!hdistCstcAtMostHorizLimit) && (!vdistCstcAtMostVertLimit)' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM — in the extracted model both are enabled together
- LreController.step(): branch guarded by '(!hdistCstcAtMostHorizLimit) && (!vdistCstcAtMostVertLimit)' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafeDist&&tcpaNonNegative
- LreController.step(): triggerless branch guarded by '!cdaBelowMinSafeDist' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM — in the extracted model both are enabled together
- field 'Sensor.SAFE_LARGE_DIST' is a double without @RoboChartType("real")
- field 'Sensor.MIN_REL_SPEED_SQ' is a double without @RoboChartType("real")

## Issues
### Issue 1: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by '(((!inOpez) && (!collisionImminent)) && (!nearStaticHoriz)) && vdistCstcAtMostDfltVertLimit' does not negate preceding triggerless guard(s): (!collisionImminent)&&nearStaticHoriz

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by '(((!inOpez) && (!collisionImminent)) && (!nearStaticHoriz)) && vdistCstcAtMostDfltVertLimit' does not negate preceding triggerless guard(s): (!collisionImminent)&&nearStaticHoriz
Location: /Users/ranwei/Gitee/forge-d2-run5-lre/java.generated.project/src/main/java/lre/controller/LreController.java:127
```

**Java trace**
  - Java: `LreController.java`:127-146

**Fix directive**
Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive.

### Issue 2: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by '((((!inOpez) && (!collisionImminent)) && (!nearStaticHoriz)) && (!vdistCstcAtMostDfltVertLimit)) && nearStaticVert' does not negate preceding triggerless guard(s): (!collisionImminent)&&nearStaticHoriz, ((!collisionImminent)&&!nearStaticHoriz)&&vdistCstcAtMostDfltVertLimit

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by '((((!inOpez) && (!collisionImminent)) && (!nearStaticHoriz)) && (!vdistCstcAtMostDfltVertLimit)) && nearStaticVert' does not negate preceding triggerless guard(s): (!collisionImminent)&&nearStaticHoriz, ((!collisionImminent)&&!nearStaticHoriz)&&vdistCstcAtMostDfltVertLimit
Location: /Users/ranwei/Gitee/forge-d2-run5-lre/java.generated.project/src/main/java/lre/controller/LreController.java:131
```

**Java trace**
  - Java: `LreController.java`:131-146

**Fix directive**
Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive.

### Issue 3: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by '(((((event instanceof lre.event.InputEvent.reqOCM) && (!inOpez)) && (!collisionImminent)) && (!nearStaticHoriz)) && (!vdistCstcAtMostDfltVertLimit)) && (!nearStaticVert)' does not negate preceding triggerless guard(s): (!collisionImminent)&&nearStaticHoriz, ((!collisionImminent)&&!nearStaticHoriz)&&vdistCstcAtMostDfltVertLimit, (((!collisionImminent)&&!nearStaticHoriz)&&!vdistCstcAtMostDfltVertLimit)&&nearStaticVert

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by '(((((event instanceof lre.event.InputEvent.reqOCM) && (!inOpez)) && (!collisionImminent)) && (!nearStaticHoriz)) && (!vdistCstcAtMostDfltVertLimit)) && (!nearStaticVert)' does not negate preceding triggerless guard(s): (!collisionImminent)&&nearStaticHoriz, ((!collisionImminent)&&!nearStaticHoriz)&&vdistCstcAtMostDfltVertLimit, (((!collisionImminent)&&!nearStaticHoriz)&&!vdistCstcAtMostDfltVertLimit)&&nearStaticVert
Location: /Users/ranwei/Gitee/forge-d2-run5-lre/java.generated.project/src/main/java/lre/controller/LreController.java:135
```

**Java trace**
  - Java: `LreController.java`:135-146

**Fix directive**
Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive.

### Issue 4: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by '(((((event instanceof lre.event.InputEvent.endTask) && (!inOpez)) && (!collisionImminent)) && (!nearStaticHoriz)) && (!vdistCstcAtMostDfltVertLimit)) && (!nearStaticVert)' does not negate preceding triggerless guard(s): (!collisionImminent)&&nearStaticHoriz, ((!collisionImminent)&&!nearStaticHoriz)&&vdistCstcAtMostDfltVertLimit, (((!collisionImminent)&&!nearStaticHoriz)&&!vdistCstcAtMostDfltVertLimit)&&nearStaticVert

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by '(((((event instanceof lre.event.InputEvent.endTask) && (!inOpez)) && (!collisionImminent)) && (!nearStaticHoriz)) && (!vdistCstcAtMostDfltVertLimit)) && (!nearStaticVert)' does not negate preceding triggerless guard(s): (!collisionImminent)&&nearStaticHoriz, ((!collisionImminent)&&!nearStaticHoriz)&&vdistCstcAtMostDfltVertLimit, (((!collisionImminent)&&!nearStaticHoriz)&&!vdistCstcAtMostDfltVertLimit)&&nearStaticVert
Location: /Users/ranwei/Gitee/forge-d2-run5-lre/java.generated.project/src/main/java/lre/controller/LreController.java:138
```

**Java trace**
  - Java: `LreController.java`:138-146

**Fix directive**
Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive.

### Issue 5: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by '(((((event instanceof lre.event.InputEvent.reqHCM) && (!inOpez)) && (!collisionImminent)) && (!nearStaticHoriz)) && (!vdistCstcAtMostDfltVertLimit)) && (!nearStaticVert)' does not negate preceding triggerless guard(s): (!collisionImminent)&&nearStaticHoriz, ((!collisionImminent)&&!nearStaticHoriz)&&vdistCstcAtMostDfltVertLimit, (((!collisionImminent)&&!nearStaticHoriz)&&!vdistCstcAtMostDfltVertLimit)&&nearStaticVert

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by '(((((event instanceof lre.event.InputEvent.reqHCM) && (!inOpez)) && (!collisionImminent)) && (!nearStaticHoriz)) && (!vdistCstcAtMostDfltVertLimit)) && (!nearStaticVert)' does not negate preceding triggerless guard(s): (!collisionImminent)&&nearStaticHoriz, ((!collisionImminent)&&!nearStaticHoriz)&&vdistCstcAtMostDfltVertLimit, (((!collisionImminent)&&!nearStaticHoriz)&&!vdistCstcAtMostDfltVertLimit)&&nearStaticVert
Location: /Users/ranwei/Gitee/forge-d2-run5-lre/java.generated.project/src/main/java/lre/controller/LreController.java:142
```

**Java trace**
  - Java: `LreController.java`:142-146

**Fix directive**
Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive.

### Issue 6: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by '(((event instanceof lre.event.InputEvent.reqOCM) && (!inOpez)) && (!collisionImminent)) && (!clearOfStatic)' does not negate preceding triggerless guard(s): (!collisionImminent)&&clearOfStatic

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by '(((event instanceof lre.event.InputEvent.reqOCM) && (!inOpez)) && (!collisionImminent)) && (!clearOfStatic)' does not negate preceding triggerless guard(s): (!collisionImminent)&&clearOfStatic
Location: /Users/ranwei/Gitee/forge-d2-run5-lre/java.generated.project/src/main/java/lre/controller/LreController.java:156
```

**Java trace**
  - Java: `LreController.java`:156-159

**Fix directive**
Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive.

### Issue 7: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by '(event instanceof lre.event.InputEvent.reqOCM) && cdaBelowMinSafeDist' does not negate preceding triggerless guard(s): !cdaBelowMinSafeDist

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by '(event instanceof lre.event.InputEvent.reqOCM) && cdaBelowMinSafeDist' does not negate preceding triggerless guard(s): !cdaBelowMinSafeDist
Location: /Users/ranwei/Gitee/forge-d2-run5-lre/java.generated.project/src/main/java/lre/controller/LreController.java:165
```

**Java trace**
  - Java: `LreController.java`:165-167

**Fix directive**
Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive.

## Files to review
(none identified)

## Next step
Proceed to Phase 3 (T2M — Spoon Discovery).
