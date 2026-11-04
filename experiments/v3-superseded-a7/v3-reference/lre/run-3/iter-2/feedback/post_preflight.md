# 2c — Preflight (Structural Lint) — PASSED

## Summary
2c — Preflight (Structural Lint): no structural errors.

## Run history
- New this run: 4
- Recurring from previous run: 1
- Resolved since previous run: 8

**Resolved issue titles**
- LreController.step(): triggerless branch guarded by 'inOpez' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM — in the extracted model both are enabled together
- LreController.step(): triggerless branch guarded by 'collisionPredicted && (!inOpez)' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM — in the extracted model both are enabled together
- LreController.step(): triggerless branch guarded by '(horizontalApproach && (!inOpez)) && (!collisionPredicted)' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM, ((event instanceof lre.event.InputEvent.EndTask) && (!inOpez)) && (!collisionPredicted), ((event instanceof lre.event.InputEvent.ReqHCM) && (!inOpez)) && (!collisionPredicted) — in the extracted model both are enabled together
- LreController.step(): triggerless branch guarded by '((verticalApproach && (!inOpez)) && (!collisionPredicted)) && (!horizontalApproach)' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM, ((event instanceof lre.event.InputEvent.EndTask) && (!inOpez)) && (!collisionPredicted), ((event instanceof lre.event.InputEvent.ReqHCM) && (!inOpez)) && (!collisionPredicted) — in the extracted model both are enabled together
- LreController.step(): triggerless branch guarded by '(((staticVertDistWithinDfltLimit && (!inOpez)) && (!collisionPredicted)) && (!horizontalApproach)) && (!verticalApproach)' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM, ((event instanceof lre.event.InputEvent.EndTask) && (!inOpez)) && (!collisionPredicted), ((event instanceof lre.event.InputEvent.ReqHCM) && (!inOpez)) && (!collisionPredicted) — in the extracted model both are enabled together
- LreController.step(): triggerless branch guarded by '(staticObsClear && (!inOpez)) && (!collisionPredicted)' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM — in the extracted model both are enabled together
- LreController.step(): triggerless branch guarded by '!cdaBelowMinSafeDist' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM — in the extracted model both are enabled together
- field 'Sensor.SAFE_LARGE_DIST' is a double without @RoboChartType("real")

## Issues
### Issue 1: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by '(((staticVertDistWithinDfltLimit && (!inOpez)) && (!collisionPredicted)) && (!horizontalApproach)) && (!verticalApproach)' does not negate preceding triggerless guard(s): (verticalApproach) [recurring x2]

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by '(((staticVertDistWithinDfltLimit && (!inOpez)) && (!collisionPredicted)) && (!horizontalApproach)) && (!verticalApproach)' does not negate preceding triggerless guard(s): (verticalApproach)
Location: /Users/ranwei/Gitee/forge-v3r-run3-lre/java.generated.project/src/main/java/lre/controller/LreController.java:115
```

**Java trace**
  - Java: `LreController.java`:115-136

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (queueLong && !waitAboveLimit)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above.

### Issue 2: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by '(((((event instanceof lre.event.InputEvent.ReqOCM) && (!inOpez)) && (!collisionPredicted)) && (!horizontalApproach)) && (!verticalApproach)) && (!staticVertDistWithinDfltLimit)' does not negate preceding triggerless guard(s): (verticalApproach), (((staticVertDistWithinDfltLimit)))&&!verticalApproach

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by '(((((event instanceof lre.event.InputEvent.ReqOCM) && (!inOpez)) && (!collisionPredicted)) && (!horizontalApproach)) && (!verticalApproach)) && (!staticVertDistWithinDfltLimit)' does not negate preceding triggerless guard(s): (verticalApproach), (((staticVertDistWithinDfltLimit)))&&!verticalApproach
Location: /Users/ranwei/Gitee/forge-v3r-run3-lre/java.generated.project/src/main/java/lre/controller/LreController.java:121
```

**Java trace**
  - Java: `LreController.java`:121-136

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (queueLong && !waitAboveLimit)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above.

### Issue 3: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by '(((((event instanceof lre.event.InputEvent.EndTask) && (!inOpez)) && (!collisionPredicted)) && (!horizontalApproach)) && (!verticalApproach)) && (!staticVertDistWithinDfltLimit)' does not negate preceding triggerless guard(s): (verticalApproach), (((staticVertDistWithinDfltLimit)))&&!verticalApproach

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by '(((((event instanceof lre.event.InputEvent.EndTask) && (!inOpez)) && (!collisionPredicted)) && (!horizontalApproach)) && (!verticalApproach)) && (!staticVertDistWithinDfltLimit)' does not negate preceding triggerless guard(s): (verticalApproach), (((staticVertDistWithinDfltLimit)))&&!verticalApproach
Location: /Users/ranwei/Gitee/forge-v3r-run3-lre/java.generated.project/src/main/java/lre/controller/LreController.java:126
```

**Java trace**
  - Java: `LreController.java`:126-136

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (queueLong && !waitAboveLimit)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above.

### Issue 4: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by '(((((event instanceof lre.event.InputEvent.ReqHCM) && (!inOpez)) && (!collisionPredicted)) && (!horizontalApproach)) && (!verticalApproach)) && (!staticVertDistWithinDfltLimit)' does not negate preceding triggerless guard(s): (verticalApproach), (((staticVertDistWithinDfltLimit)))&&!verticalApproach

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by '(((((event instanceof lre.event.InputEvent.ReqHCM) && (!inOpez)) && (!collisionPredicted)) && (!horizontalApproach)) && (!verticalApproach)) && (!staticVertDistWithinDfltLimit)' does not negate preceding triggerless guard(s): (verticalApproach), (((staticVertDistWithinDfltLimit)))&&!verticalApproach
Location: /Users/ranwei/Gitee/forge-v3r-run3-lre/java.generated.project/src/main/java/lre/controller/LreController.java:132
```

**Java trace**
  - Java: `LreController.java`:132-136

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (queueLong && !waitAboveLimit)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above.

### Issue 5: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by '(event instanceof lre.event.InputEvent.ReqOCM) && cdaBelowMinSafeDist' does not negate preceding triggerless guard(s): !cdaBelowMinSafeDist

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by '(event instanceof lre.event.InputEvent.ReqOCM) && cdaBelowMinSafeDist' does not negate preceding triggerless guard(s): !cdaBelowMinSafeDist
Location: /Users/ranwei/Gitee/forge-v3r-run3-lre/java.generated.project/src/main/java/lre/controller/LreController.java:164
```

**Java trace**
  - Java: `LreController.java`:164-166

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (queueLong && !waitAboveLimit)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above.

## Files to review
(none identified)

## Next step
Proceed to Phase 3 (T2M — Spoon Discovery).
