# 2c — Preflight (Structural Lint) — FAILED

## Summary
2c — Preflight (Structural Lint): 1 structural error(s) found. Downstream phases would produce a wrong formal model.

## Run history
- New this run: 12
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by 'inOpez' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: LreController.step(): triggerless branch guarded by 'inOpez' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-v3o18-run3-lre/java.generated.project/src/main/java/lre/controller/LreController.java:94
```

**Java trace**
  - Java: `LreController.java`:94-114

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. Trigger disjointness does NOT close this (it only separates two EVENT-triggered branches), and the negation synthesis that closes rule6 cannot either, because the event-triggered branch contributes no data predicate to negate. RoboChart has no event-absence guard, so this must be fixed in the Java: either give the later branch an explicit guard that excludes the earlier branch's case, or place the triggerless branch FIRST so the ordinary priority-negation synthesis applies. Otherwise the model admits two operations in states where the Java is deterministic.

### Issue 2: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by 'collisionRisk && (!inOpez)' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: LreController.step(): triggerless branch guarded by 'collisionRisk && (!inOpez)' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-v3o18-run3-lre/java.generated.project/src/main/java/lre/controller/LreController.java:96
```

**Java trace**
  - Java: `LreController.java`:96-114

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. Trigger disjointness does NOT close this (it only separates two EVENT-triggered branches), and the negation synthesis that closes rule6 cannot either, because the event-triggered branch contributes no data predicate to negate. RoboChart has no event-absence guard, so this must be fixed in the Java: either give the later branch an explicit guard that excludes the earlier branch's case, or place the triggerless branch FIRST so the ordinary priority-negation synthesis applies. Otherwise the model admits two operations in states where the Java is deterministic.

### Issue 3: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by '(horizVelHazard && (!inOpez)) && (!collisionRisk)' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM, ((event instanceof lre.event.InputEvent.EndTask) && (!inOpez)) && (!collisionRisk) — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: LreController.step(): triggerless branch guarded by '(horizVelHazard && (!inOpez)) && (!collisionRisk)' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM, ((event instanceof lre.event.InputEvent.EndTask) && (!inOpez)) && (!collisionRisk) — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-v3o18-run3-lre/java.generated.project/src/main/java/lre/controller/LreController.java:101
```

**Java trace**
  - Java: `LreController.java`:101-114

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. Trigger disjointness does NOT close this (it only separates two EVENT-triggered branches), and the negation synthesis that closes rule6 cannot either, because the event-triggered branch contributes no data predicate to negate. RoboChart has no event-absence guard, so this must be fixed in the Java: either give the later branch an explicit guard that excludes the earlier branch's case, or place the triggerless branch FIRST so the ordinary priority-negation synthesis applies. Otherwise the model admits two operations in states where the Java is deterministic.

### Issue 4: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by '((vertVelHazard && (!inOpez)) && (!collisionRisk)) && (!horizVelHazard)' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM, ((event instanceof lre.event.InputEvent.EndTask) && (!inOpez)) && (!collisionRisk) — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: LreController.step(): triggerless branch guarded by '((vertVelHazard && (!inOpez)) && (!collisionRisk)) && (!horizVelHazard)' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM, ((event instanceof lre.event.InputEvent.EndTask) && (!inOpez)) && (!collisionRisk) — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-v3o18-run3-lre/java.generated.project/src/main/java/lre/controller/LreController.java:104
```

**Java trace**
  - Java: `LreController.java`:104-114

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. Trigger disjointness does NOT close this (it only separates two EVENT-triggered branches), and the negation synthesis that closes rule6 cannot either, because the event-triggered branch contributes no data predicate to negate. RoboChart has no event-absence guard, so this must be fixed in the Java: either give the later branch an explicit guard that excludes the earlier branch's case, or place the triggerless branch FIRST so the ordinary priority-negation synthesis applies. Otherwise the model admits two operations in states where the Java is deterministic.

### Issue 5: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by '(((staticDfltVertNear && (!inOpez)) && (!collisionRisk)) && (!horizVelHazard)) && (!vertVelHazard)' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM, ((event instanceof lre.event.InputEvent.EndTask) && (!inOpez)) && (!collisionRisk) — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: LreController.step(): triggerless branch guarded by '(((staticDfltVertNear && (!inOpez)) && (!collisionRisk)) && (!horizVelHazard)) && (!vertVelHazard)' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM, ((event instanceof lre.event.InputEvent.EndTask) && (!inOpez)) && (!collisionRisk) — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-v3o18-run3-lre/java.generated.project/src/main/java/lre/controller/LreController.java:107
```

**Java trace**
  - Java: `LreController.java`:107-114

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. Trigger disjointness does NOT close this (it only separates two EVENT-triggered branches), and the negation synthesis that closes rule6 cannot either, because the event-triggered branch contributes no data predicate to negate. RoboChart has no event-absence guard, so this must be fixed in the Java: either give the later branch an explicit guard that excludes the earlier branch's case, or place the triggerless branch FIRST so the ordinary priority-negation synthesis applies. Otherwise the model admits two operations in states where the Java is deterministic.

### Issue 6: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by '(((staticDfltVertNear && (!inOpez)) && (!collisionRisk)) && (!horizVelHazard)) && (!vertVelHazard)' does not negate preceding triggerless guard(s): (vertVelHazard)

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by '(((staticDfltVertNear && (!inOpez)) && (!collisionRisk)) && (!horizVelHazard)) && (!vertVelHazard)' does not negate preceding triggerless guard(s): (vertVelHazard)
Location: /Users/ranwei/Gitee/forge-v3o18-run3-lre/java.generated.project/src/main/java/lre/controller/LreController.java:107
```

**Java trace**
  - Java: `LreController.java`:107-114

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (queueLong && !waitAboveLimit)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above.

### Issue 7: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by '(((((event instanceof lre.event.InputEvent.ReqHCM) && (!inOpez)) && (!collisionRisk)) && (!horizVelHazard)) && (!vertVelHazard)) && (!staticDfltVertNear)' does not negate preceding triggerless guard(s): (vertVelHazard), (((staticDfltVertNear)))&&!vertVelHazard

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by '(((((event instanceof lre.event.InputEvent.ReqHCM) && (!inOpez)) && (!collisionRisk)) && (!horizVelHazard)) && (!vertVelHazard)) && (!staticDfltVertNear)' does not negate preceding triggerless guard(s): (vertVelHazard), (((staticDfltVertNear)))&&!vertVelHazard
Location: /Users/ranwei/Gitee/forge-v3o18-run3-lre/java.generated.project/src/main/java/lre/controller/LreController.java:110
```

**Java trace**
  - Java: `LreController.java`:110-114

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (queueLong && !waitAboveLimit)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above.

### Issue 8: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by 'inOpez' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: LreController.step(): triggerless branch guarded by 'inOpez' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-v3o18-run3-lre/java.generated.project/src/main/java/lre/controller/LreController.java:119
```

**Java trace**
  - Java: `LreController.java`:119-126

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. Trigger disjointness does NOT close this (it only separates two EVENT-triggered branches), and the negation synthesis that closes rule6 cannot either, because the event-triggered branch contributes no data predicate to negate. RoboChart has no event-absence guard, so this must be fixed in the Java: either give the later branch an explicit guard that excludes the earlier branch's case, or place the triggerless branch FIRST so the ordinary priority-negation synthesis applies. Otherwise the model admits two operations in states where the Java is deterministic.

### Issue 9: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by 'collisionRisk && (!inOpez)' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: LreController.step(): triggerless branch guarded by 'collisionRisk && (!inOpez)' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-v3o18-run3-lre/java.generated.project/src/main/java/lre/controller/LreController.java:121
```

**Java trace**
  - Java: `LreController.java`:121-126

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. Trigger disjointness does NOT close this (it only separates two EVENT-triggered branches), and the negation synthesis that closes rule6 cannot either, because the event-triggered branch contributes no data predicate to negate. RoboChart has no event-absence guard, so this must be fixed in the Java: either give the later branch an explicit guard that excludes the earlier branch's case, or place the triggerless branch FIRST so the ordinary priority-negation synthesis applies. Otherwise the model admits two operations in states where the Java is deterministic.

### Issue 10: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by '(((!staticHorizNear) && (!staticVertNear)) && (!inOpez)) && (!collisionRisk)' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: LreController.step(): triggerless branch guarded by '(((!staticHorizNear) && (!staticVertNear)) && (!inOpez)) && (!collisionRisk)' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-v3o18-run3-lre/java.generated.project/src/main/java/lre/controller/LreController.java:123
```

**Java trace**
  - Java: `LreController.java`:123-126

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. Trigger disjointness does NOT close this (it only separates two EVENT-triggered branches), and the negation synthesis that closes rule6 cannot either, because the event-triggered branch contributes no data predicate to negate. RoboChart has no event-absence guard, so this must be fixed in the Java: either give the later branch an explicit guard that excludes the earlier branch's case, or place the triggerless branch FIRST so the ordinary priority-negation synthesis applies. Otherwise the model admits two operations in states where the Java is deterministic.

### Issue 11: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by '!cdaBelowMinSafe' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: LreController.step(): triggerless branch guarded by '!cdaBelowMinSafe' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-v3o18-run3-lre/java.generated.project/src/main/java/lre/controller/LreController.java:131
```

**Java trace**
  - Java: `LreController.java`:131-134

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. Trigger disjointness does NOT close this (it only separates two EVENT-triggered branches), and the negation synthesis that closes rule6 cannot either, because the event-triggered branch contributes no data predicate to negate. RoboChart has no event-absence guard, so this must be fixed in the Java: either give the later branch an explicit guard that excludes the earlier branch's case, or place the triggerless branch FIRST so the ordinary priority-negation synthesis applies. Otherwise the model admits two operations in states where the Java is deterministic.

### Issue 12: lint_rule4_double_missing_real_annotation — field 'Sensor.SAFE_LARGE_DIST' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: field 'Sensor.SAFE_LARGE_DIST' is a double without @RoboChartType("real")
Location: /Users/ranwei/Gitee/forge-v3o18-run3-lre/java.generated.project/src/main/java/lre/sensor/Sensor.java:21
```

**Java trace**
  - Java: `Sensor.java`:21-21

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

## Files to review
(none identified)

## Next step
Preflight found structural rule violations (errors). Fix them before continuing — downstream phases would either crash or produce a silently-wrong formal model.
