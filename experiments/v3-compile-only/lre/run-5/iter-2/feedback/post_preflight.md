# 2c — Preflight (Structural Lint) — PASSED

## Summary
2c — Preflight (Structural Lint): no structural errors.

## Run history
- New this run: 3
- Recurring from previous run: 1
- Resolved since previous run: 10

**Resolved issue titles**
- LreController.step(): triggerless branch guarded by 'inOpez' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM, event instanceof lre.event.InputEvent.EndTask, event instanceof lre.event.InputEvent.ReqHCM — in the extracted model both are enabled together
- LreController.step(): triggerless branch guarded by 'collisionCourse && (!inOpez)' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM, event instanceof lre.event.InputEvent.EndTask, event instanceof lre.event.InputEvent.ReqHCM — in the extracted model both are enabled together
- LreController.step(): triggerless branch guarded by '(horizVelHazard && (!inOpez)) && (!collisionCourse)' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM, event instanceof lre.event.InputEvent.EndTask, event instanceof lre.event.InputEvent.ReqHCM — in the extracted model both are enabled together
- LreController.step(): triggerless branch guarded by '((vertVelHazard && (!inOpez)) && (!collisionCourse)) && (!horizVelHazard)' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM, event instanceof lre.event.InputEvent.EndTask, event instanceof lre.event.InputEvent.ReqHCM — in the extracted model both are enabled together
- LreController.step(): triggerless branch guarded by '(((staticDfltVertNear && (!inOpez)) && (!collisionCourse)) && (!horizVelHazard)) && (!vertVelHazard)' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM, event instanceof lre.event.InputEvent.EndTask, event instanceof lre.event.InputEvent.ReqHCM — in the extracted model both are enabled together
- LreController.step(): triggerless branch guarded by 'inOpez' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM — in the extracted model both are enabled together
- LreController.step(): triggerless branch guarded by 'collisionCourse && (!inOpez)' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM — in the extracted model both are enabled together
- LreController.step(): triggerless branch guarded by '(staticObsClear && (!inOpez)) && (!collisionCourse)' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM — in the extracted model both are enabled together
- LreController.step(): triggerless branch guarded by '!cdaBelowMinSafe' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM — in the extracted model both are enabled together
- field 'Sensor.NO_OBSTACLE_DIST' is a double without @RoboChartType("real")

## Issues
### Issue 1: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by '(((staticDfltVertNear && (!inOpez)) && (!collisionCourse)) && (!horizVelHazard)) && (!vertVelHazard)' does not negate preceding triggerless guard(s): (vertVelHazard) [recurring x2]

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by '(((staticDfltVertNear && (!inOpez)) && (!collisionCourse)) && (!horizVelHazard)) && (!vertVelHazard)' does not negate preceding triggerless guard(s): (vertVelHazard)
Location: /Users/ranwei/Gitee/forge-v3o18-run5-lre/java.generated.project/src/main/java/lre/controller/LreController.java:112
```

**Java trace**
  - Java: `LreController.java`:112-127

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (queueLong && !waitAboveLimit)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above.

### Issue 2: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by '(((((event instanceof lre.event.InputEvent.ReqOCM) && (!inOpez)) && (!collisionCourse)) && (!horizVelHazard)) && (!vertVelHazard)) && (!staticDfltVertNear)' does not negate preceding triggerless guard(s): (vertVelHazard), (((staticDfltVertNear)))&&!vertVelHazard

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by '(((((event instanceof lre.event.InputEvent.ReqOCM) && (!inOpez)) && (!collisionCourse)) && (!horizVelHazard)) && (!vertVelHazard)) && (!staticDfltVertNear)' does not negate preceding triggerless guard(s): (vertVelHazard), (((staticDfltVertNear)))&&!vertVelHazard
Location: /Users/ranwei/Gitee/forge-v3o18-run5-lre/java.generated.project/src/main/java/lre/controller/LreController.java:116
```

**Java trace**
  - Java: `LreController.java`:116-127

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (queueLong && !waitAboveLimit)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above.

### Issue 3: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by '(((((event instanceof lre.event.InputEvent.EndTask) && (!inOpez)) && (!collisionCourse)) && (!horizVelHazard)) && (!vertVelHazard)) && (!staticDfltVertNear)' does not negate preceding triggerless guard(s): (vertVelHazard), (((staticDfltVertNear)))&&!vertVelHazard

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by '(((((event instanceof lre.event.InputEvent.EndTask) && (!inOpez)) && (!collisionCourse)) && (!horizVelHazard)) && (!vertVelHazard)) && (!staticDfltVertNear)' does not negate preceding triggerless guard(s): (vertVelHazard), (((staticDfltVertNear)))&&!vertVelHazard
Location: /Users/ranwei/Gitee/forge-v3o18-run5-lre/java.generated.project/src/main/java/lre/controller/LreController.java:119
```

**Java trace**
  - Java: `LreController.java`:119-127

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (queueLong && !waitAboveLimit)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above.

### Issue 4: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by '(((((event instanceof lre.event.InputEvent.ReqHCM) && (!inOpez)) && (!collisionCourse)) && (!horizVelHazard)) && (!vertVelHazard)) && (!staticDfltVertNear)' does not negate preceding triggerless guard(s): (vertVelHazard), (((staticDfltVertNear)))&&!vertVelHazard

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by '(((((event instanceof lre.event.InputEvent.ReqHCM) && (!inOpez)) && (!collisionCourse)) && (!horizVelHazard)) && (!vertVelHazard)) && (!staticDfltVertNear)' does not negate preceding triggerless guard(s): (vertVelHazard), (((staticDfltVertNear)))&&!vertVelHazard
Location: /Users/ranwei/Gitee/forge-v3o18-run5-lre/java.generated.project/src/main/java/lre/controller/LreController.java:123
```

**Java trace**
  - Java: `LreController.java`:123-127

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (queueLong && !waitAboveLimit)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above.

## Files to review
(none identified)

## Next step
Proceed to Phase 3 (T2M — Spoon Discovery).
