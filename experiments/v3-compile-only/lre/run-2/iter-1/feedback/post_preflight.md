# 2c — Preflight (Structural Lint) — PASSED

## Summary
2c — Preflight (Structural Lint): no structural errors.

## Run history
- New this run: 10
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by 'inOpez' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: LreController.step(): triggerless branch guarded by 'inOpez' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-v3o18-run2-lre/java.generated.project/src/main/java/lre/controller/LreController.java:100
```

**Java trace**
  - Java: `LreController.java`:100-120

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. Trigger disjointness does NOT close this (it only separates two EVENT-triggered branches), and the negation synthesis that closes rule6 cannot either, because the event-triggered branch contributes no data predicate to negate. RoboChart has no event-absence guard, so this must be fixed in the Java: either give the later branch an explicit guard that excludes the earlier branch's case, or place the triggerless branch FIRST so the ordinary priority-negation synthesis applies. Otherwise the model admits two operations in states where the Java is deterministic.

### Issue 2: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by 'collisionCourse && (!inOpez)' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: LreController.step(): triggerless branch guarded by 'collisionCourse && (!inOpez)' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-v3o18-run2-lre/java.generated.project/src/main/java/lre/controller/LreController.java:102
```

**Java trace**
  - Java: `LreController.java`:102-120

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. Trigger disjointness does NOT close this (it only separates two EVENT-triggered branches), and the negation synthesis that closes rule6 cannot either, because the event-triggered branch contributes no data predicate to negate. RoboChart has no event-absence guard, so this must be fixed in the Java: either give the later branch an explicit guard that excludes the earlier branch's case, or place the triggerless branch FIRST so the ordinary priority-negation synthesis applies. Otherwise the model admits two operations in states where the Java is deterministic.

### Issue 3: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by '(horizVelHazard && (!inOpez)) && (!collisionCourse)' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM, ((event instanceof lre.event.InputEvent.EndTask) && (!inOpez)) && (!collisionCourse), ((event instanceof lre.event.InputEvent.ReqHCM) && (!inOpez)) && (!collisionCourse) — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: LreController.step(): triggerless branch guarded by '(horizVelHazard && (!inOpez)) && (!collisionCourse)' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM, ((event instanceof lre.event.InputEvent.EndTask) && (!inOpez)) && (!collisionCourse), ((event instanceof lre.event.InputEvent.ReqHCM) && (!inOpez)) && (!collisionCourse) — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-v3o18-run2-lre/java.generated.project/src/main/java/lre/controller/LreController.java:110
```

**Java trace**
  - Java: `LreController.java`:110-120

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. Trigger disjointness does NOT close this (it only separates two EVENT-triggered branches), and the negation synthesis that closes rule6 cannot either, because the event-triggered branch contributes no data predicate to negate. RoboChart has no event-absence guard, so this must be fixed in the Java: either give the later branch an explicit guard that excludes the earlier branch's case, or place the triggerless branch FIRST so the ordinary priority-negation synthesis applies. Otherwise the model admits two operations in states where the Java is deterministic.

### Issue 4: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by '((vertVelHazard && (!inOpez)) && (!collisionCourse)) && (!horizVelHazard)' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM, ((event instanceof lre.event.InputEvent.EndTask) && (!inOpez)) && (!collisionCourse), ((event instanceof lre.event.InputEvent.ReqHCM) && (!inOpez)) && (!collisionCourse) — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: LreController.step(): triggerless branch guarded by '((vertVelHazard && (!inOpez)) && (!collisionCourse)) && (!horizVelHazard)' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM, ((event instanceof lre.event.InputEvent.EndTask) && (!inOpez)) && (!collisionCourse), ((event instanceof lre.event.InputEvent.ReqHCM) && (!inOpez)) && (!collisionCourse) — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-v3o18-run2-lre/java.generated.project/src/main/java/lre/controller/LreController.java:113
```

**Java trace**
  - Java: `LreController.java`:113-120

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. Trigger disjointness does NOT close this (it only separates two EVENT-triggered branches), and the negation synthesis that closes rule6 cannot either, because the event-triggered branch contributes no data predicate to negate. RoboChart has no event-absence guard, so this must be fixed in the Java: either give the later branch an explicit guard that excludes the earlier branch's case, or place the triggerless branch FIRST so the ordinary priority-negation synthesis applies. Otherwise the model admits two operations in states where the Java is deterministic.

### Issue 5: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by '(((cstcVertWithinDfltLimit && (!inOpez)) && (!collisionCourse)) && (!horizVelHazard)) && (!vertVelHazard)' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM, ((event instanceof lre.event.InputEvent.EndTask) && (!inOpez)) && (!collisionCourse), ((event instanceof lre.event.InputEvent.ReqHCM) && (!inOpez)) && (!collisionCourse) — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: LreController.step(): triggerless branch guarded by '(((cstcVertWithinDfltLimit && (!inOpez)) && (!collisionCourse)) && (!horizVelHazard)) && (!vertVelHazard)' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM, ((event instanceof lre.event.InputEvent.EndTask) && (!inOpez)) && (!collisionCourse), ((event instanceof lre.event.InputEvent.ReqHCM) && (!inOpez)) && (!collisionCourse) — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-v3o18-run2-lre/java.generated.project/src/main/java/lre/controller/LreController.java:116
```

**Java trace**
  - Java: `LreController.java`:116-120

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. Trigger disjointness does NOT close this (it only separates two EVENT-triggered branches), and the negation synthesis that closes rule6 cannot either, because the event-triggered branch contributes no data predicate to negate. RoboChart has no event-absence guard, so this must be fixed in the Java: either give the later branch an explicit guard that excludes the earlier branch's case, or place the triggerless branch FIRST so the ordinary priority-negation synthesis applies. Otherwise the model admits two operations in states where the Java is deterministic.

### Issue 6: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by '(((cstcVertWithinDfltLimit && (!inOpez)) && (!collisionCourse)) && (!horizVelHazard)) && (!vertVelHazard)' does not negate preceding triggerless guard(s): (vertVelHazard)

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by '(((cstcVertWithinDfltLimit && (!inOpez)) && (!collisionCourse)) && (!horizVelHazard)) && (!vertVelHazard)' does not negate preceding triggerless guard(s): (vertVelHazard)
Location: /Users/ranwei/Gitee/forge-v3o18-run2-lre/java.generated.project/src/main/java/lre/controller/LreController.java:116
```

**Java trace**
  - Java: `LreController.java`:116-120

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (queueLong && !waitAboveLimit)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above.

### Issue 7: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by 'inOpez' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: LreController.step(): triggerless branch guarded by 'inOpez' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-v3o18-run2-lre/java.generated.project/src/main/java/lre/controller/LreController.java:125
```

**Java trace**
  - Java: `LreController.java`:125-132

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. Trigger disjointness does NOT close this (it only separates two EVENT-triggered branches), and the negation synthesis that closes rule6 cannot either, because the event-triggered branch contributes no data predicate to negate. RoboChart has no event-absence guard, so this must be fixed in the Java: either give the later branch an explicit guard that excludes the earlier branch's case, or place the triggerless branch FIRST so the ordinary priority-negation synthesis applies. Otherwise the model admits two operations in states where the Java is deterministic.

### Issue 8: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by 'collisionCourse && (!inOpez)' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: LreController.step(): triggerless branch guarded by 'collisionCourse && (!inOpez)' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-v3o18-run2-lre/java.generated.project/src/main/java/lre/controller/LreController.java:127
```

**Java trace**
  - Java: `LreController.java`:127-132

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. Trigger disjointness does NOT close this (it only separates two EVENT-triggered branches), and the negation synthesis that closes rule6 cannot either, because the event-triggered branch contributes no data predicate to negate. RoboChart has no event-absence guard, so this must be fixed in the Java: either give the later branch an explicit guard that excludes the earlier branch's case, or place the triggerless branch FIRST so the ordinary priority-negation synthesis applies. Otherwise the model admits two operations in states where the Java is deterministic.

### Issue 9: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by '(((!cstcHorizWithinLimit) && (!cstcVertWithinLimit)) && (!inOpez)) && (!collisionCourse)' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: LreController.step(): triggerless branch guarded by '(((!cstcHorizWithinLimit) && (!cstcVertWithinLimit)) && (!inOpez)) && (!collisionCourse)' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-v3o18-run2-lre/java.generated.project/src/main/java/lre/controller/LreController.java:129
```

**Java trace**
  - Java: `LreController.java`:129-132

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. Trigger disjointness does NOT close this (it only separates two EVENT-triggered branches), and the negation synthesis that closes rule6 cannot either, because the event-triggered branch contributes no data predicate to negate. RoboChart has no event-absence guard, so this must be fixed in the Java: either give the later branch an explicit guard that excludes the earlier branch's case, or place the triggerless branch FIRST so the ordinary priority-negation synthesis applies. Otherwise the model admits two operations in states where the Java is deterministic.

### Issue 10: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by '!cdaBelowMinSafeDist' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: LreController.step(): triggerless branch guarded by '!cdaBelowMinSafeDist' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-v3o18-run2-lre/java.generated.project/src/main/java/lre/controller/LreController.java:137
```

**Java trace**
  - Java: `LreController.java`:137-140

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. Trigger disjointness does NOT close this (it only separates two EVENT-triggered branches), and the negation synthesis that closes rule6 cannot either, because the event-triggered branch contributes no data predicate to negate. RoboChart has no event-absence guard, so this must be fixed in the Java: either give the later branch an explicit guard that excludes the earlier branch's case, or place the triggerless branch FIRST so the ordinary priority-negation synthesis applies. Otherwise the model admits two operations in states where the Java is deterministic.

## Files to review
(none identified)

## Next step
Proceed to Phase 3 (T2M — Spoon Discovery).
