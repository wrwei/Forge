# 2c — Preflight (Structural Lint) — PASSED

## Summary
2c — Preflight (Structural Lint): no structural errors.

## Run history
- New this run: 5
- Recurring from previous run: 2
- Resolved since previous run: 5

**Resolved issue titles**
- LreController.step(): branch guarded by '(((((event instanceof lre.event.InputEvent.reqOCM) && (!inOpez)) && (!collisionImminent)) && (!nearStaticHoriz)) && (!vdistCstcAtMostDfltVertLimit)) && (!nearStaticVert)' does not negate preceding triggerless guard(s): (!collisionImminent)&&nearStaticHoriz, ((!collisionImminent)&&!nearStaticHoriz)&&vdistCstcAtMostDfltVertLimit, (((!collisionImminent)&&!nearStaticHoriz)&&!vdistCstcAtMostDfltVertLimit)&&nearStaticVert
- LreController.step(): branch guarded by '(((((event instanceof lre.event.InputEvent.endTask) && (!inOpez)) && (!collisionImminent)) && (!nearStaticHoriz)) && (!vdistCstcAtMostDfltVertLimit)) && (!nearStaticVert)' does not negate preceding triggerless guard(s): (!collisionImminent)&&nearStaticHoriz, ((!collisionImminent)&&!nearStaticHoriz)&&vdistCstcAtMostDfltVertLimit, (((!collisionImminent)&&!nearStaticHoriz)&&!vdistCstcAtMostDfltVertLimit)&&nearStaticVert
- LreController.step(): branch guarded by '(((((event instanceof lre.event.InputEvent.reqHCM) && (!inOpez)) && (!collisionImminent)) && (!nearStaticHoriz)) && (!vdistCstcAtMostDfltVertLimit)) && (!nearStaticVert)' does not negate preceding triggerless guard(s): (!collisionImminent)&&nearStaticHoriz, ((!collisionImminent)&&!nearStaticHoriz)&&vdistCstcAtMostDfltVertLimit, (((!collisionImminent)&&!nearStaticHoriz)&&!vdistCstcAtMostDfltVertLimit)&&nearStaticVert
- LreController.step(): branch guarded by '(((event instanceof lre.event.InputEvent.reqOCM) && (!inOpez)) && (!collisionImminent)) && (!clearOfStatic)' does not negate preceding triggerless guard(s): (!collisionImminent)&&clearOfStatic
- LreController.step(): branch guarded by '(event instanceof lre.event.InputEvent.reqOCM) && cdaBelowMinSafeDist' does not negate preceding triggerless guard(s): !cdaBelowMinSafeDist

## Issues
### Issue 1: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by '(((!inOpez) && (!collisionImminent)) && (!nearStaticHoriz)) && vdistCstcAtMostDfltVertLimit' does not negate preceding triggerless guard(s): (!collisionImminent)&&nearStaticHoriz [recurring x2]

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by '(((!inOpez) && (!collisionImminent)) && (!nearStaticHoriz)) && vdistCstcAtMostDfltVertLimit' does not negate preceding triggerless guard(s): (!collisionImminent)&&nearStaticHoriz
Location: /Users/ranwei/Gitee/forge-d2-run5-lre/java.generated.project/src/main/java/lre/controller/LreController.java:128
```

**Java trace**
  - Java: `LreController.java`:128-144

**Fix directive**
Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive.

### Issue 2: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by '((((!inOpez) && (!collisionImminent)) && (!nearStaticHoriz)) && (!vdistCstcAtMostDfltVertLimit)) && nearStaticVert' does not negate preceding triggerless guard(s): (!collisionImminent)&&nearStaticHoriz, ((!collisionImminent)&&!nearStaticHoriz)&&vdistCstcAtMostDfltVertLimit [recurring x2]

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by '((((!inOpez) && (!collisionImminent)) && (!nearStaticHoriz)) && (!vdistCstcAtMostDfltVertLimit)) && nearStaticVert' does not negate preceding triggerless guard(s): (!collisionImminent)&&nearStaticHoriz, ((!collisionImminent)&&!nearStaticHoriz)&&vdistCstcAtMostDfltVertLimit
Location: /Users/ranwei/Gitee/forge-d2-run5-lre/java.generated.project/src/main/java/lre/controller/LreController.java:132
```

**Java trace**
  - Java: `LreController.java`:132-144

**Fix directive**
Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive.

### Issue 3: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by 'event instanceof lre.event.InputEvent.reqOCM' does not negate preceding triggerless guard(s): inOpez, collisionImminent, (!collisionImminent)&&nearStaticHoriz, ((!collisionImminent)&&!nearStaticHoriz)&&vdistCstcAtMostDfltVertLimit, (((!collisionImminent)&&!nearStaticHoriz)&&!vdistCstcAtMostDfltVertLimit)&&nearStaticVert

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by 'event instanceof lre.event.InputEvent.reqOCM' does not negate preceding triggerless guard(s): inOpez, collisionImminent, (!collisionImminent)&&nearStaticHoriz, ((!collisionImminent)&&!nearStaticHoriz)&&vdistCstcAtMostDfltVertLimit, (((!collisionImminent)&&!nearStaticHoriz)&&!vdistCstcAtMostDfltVertLimit)&&nearStaticVert
Location: /Users/ranwei/Gitee/forge-d2-run5-lre/java.generated.project/src/main/java/lre/controller/LreController.java:136
```

**Java trace**
  - Java: `LreController.java`:136-144

**Fix directive**
Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive.

### Issue 4: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by 'event instanceof lre.event.InputEvent.endTask' does not negate preceding triggerless guard(s): inOpez, collisionImminent, (!collisionImminent)&&nearStaticHoriz, ((!collisionImminent)&&!nearStaticHoriz)&&vdistCstcAtMostDfltVertLimit, (((!collisionImminent)&&!nearStaticHoriz)&&!vdistCstcAtMostDfltVertLimit)&&nearStaticVert

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by 'event instanceof lre.event.InputEvent.endTask' does not negate preceding triggerless guard(s): inOpez, collisionImminent, (!collisionImminent)&&nearStaticHoriz, ((!collisionImminent)&&!nearStaticHoriz)&&vdistCstcAtMostDfltVertLimit, (((!collisionImminent)&&!nearStaticHoriz)&&!vdistCstcAtMostDfltVertLimit)&&nearStaticVert
Location: /Users/ranwei/Gitee/forge-d2-run5-lre/java.generated.project/src/main/java/lre/controller/LreController.java:138
```

**Java trace**
  - Java: `LreController.java`:138-144

**Fix directive**
Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive.

### Issue 5: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by 'event instanceof lre.event.InputEvent.reqHCM' does not negate preceding triggerless guard(s): inOpez, collisionImminent, (!collisionImminent)&&nearStaticHoriz, ((!collisionImminent)&&!nearStaticHoriz)&&vdistCstcAtMostDfltVertLimit, (((!collisionImminent)&&!nearStaticHoriz)&&!vdistCstcAtMostDfltVertLimit)&&nearStaticVert

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by 'event instanceof lre.event.InputEvent.reqHCM' does not negate preceding triggerless guard(s): inOpez, collisionImminent, (!collisionImminent)&&nearStaticHoriz, ((!collisionImminent)&&!nearStaticHoriz)&&vdistCstcAtMostDfltVertLimit, (((!collisionImminent)&&!nearStaticHoriz)&&!vdistCstcAtMostDfltVertLimit)&&nearStaticVert
Location: /Users/ranwei/Gitee/forge-d2-run5-lre/java.generated.project/src/main/java/lre/controller/LreController.java:141
```

**Java trace**
  - Java: `LreController.java`:141-144

**Fix directive**
Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive.

### Issue 6: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by 'event instanceof lre.event.InputEvent.reqOCM' does not negate preceding triggerless guard(s): inOpez, collisionImminent, (!collisionImminent)&&clearOfStatic

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by 'event instanceof lre.event.InputEvent.reqOCM' does not negate preceding triggerless guard(s): inOpez, collisionImminent, (!collisionImminent)&&clearOfStatic
Location: /Users/ranwei/Gitee/forge-d2-run5-lre/java.generated.project/src/main/java/lre/controller/LreController.java:154
```

**Java trace**
  - Java: `LreController.java`:154-156

**Fix directive**
Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive.

### Issue 7: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by 'event instanceof lre.event.InputEvent.reqOCM' does not negate preceding triggerless guard(s): !cdaBelowMinSafeDist

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by 'event instanceof lre.event.InputEvent.reqOCM' does not negate preceding triggerless guard(s): !cdaBelowMinSafeDist
Location: /Users/ranwei/Gitee/forge-d2-run5-lre/java.generated.project/src/main/java/lre/controller/LreController.java:162
```

**Java trace**
  - Java: `LreController.java`:162-164

**Fix directive**
Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive.

## Files to review
(none identified)

## Next step
Proceed to Phase 3 (T2M — Spoon Discovery).
