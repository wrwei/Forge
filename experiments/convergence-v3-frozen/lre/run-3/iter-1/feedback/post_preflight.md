# 2c — Preflight (Structural Lint) — FAILED

## Summary
2c — Preflight (Structural Lint): 6 structural error(s) found. Downstream phases would produce a wrong formal model.

## Run history
- New this run: 23
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: lint_rule4_double_missing_real_annotation — field 'LreConstants.MIN_SAFE_DIST' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: field 'LreConstants.MIN_SAFE_DIST' is a double without @RoboChartType("real")
Location: /Users/ranwei/Gitee/forge-frozen-run3-lre/java.generated.project/src/main/java/lre/constants/LreConstants.java:7
```

**Java trace**
  - Java: `LreConstants.java`:7-7

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 2: lint_rule4_double_missing_real_annotation — field 'LreConstants.STATIC_OBS_HORIZ_DIST' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: field 'LreConstants.STATIC_OBS_HORIZ_DIST' is a double without @RoboChartType("real")
Location: /Users/ranwei/Gitee/forge-frozen-run3-lre/java.generated.project/src/main/java/lre/constants/LreConstants.java:10
```

**Java trace**
  - Java: `LreConstants.java`:10-10

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 3: lint_rule4_double_missing_real_annotation — field 'LreConstants.STATIC_OBS_VERT_DIST' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: field 'LreConstants.STATIC_OBS_VERT_DIST' is a double without @RoboChartType("real")
Location: /Users/ranwei/Gitee/forge-frozen-run3-lre/java.generated.project/src/main/java/lre/constants/LreConstants.java:13
```

**Java trace**
  - Java: `LreConstants.java`:13-13

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 4: lint_rule4_double_missing_real_annotation — field 'LreConstants.STATIC_OBS_DFLT_VERT_DIST' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: field 'LreConstants.STATIC_OBS_DFLT_VERT_DIST' is a double without @RoboChartType("real")
Location: /Users/ranwei/Gitee/forge-frozen-run3-lre/java.generated.project/src/main/java/lre/constants/LreConstants.java:16
```

**Java trace**
  - Java: `LreConstants.java`:16-16

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 5: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by 'inOpez' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM, event instanceof lre.event.InputEvent.endTask, event instanceof lre.event.InputEvent.reqHCM — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: LreController.step(): triggerless branch guarded by 'inOpez' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM, event instanceof lre.event.InputEvent.endTask, event instanceof lre.event.InputEvent.reqHCM — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-frozen-run3-lre/java.generated.project/src/main/java/lre/controller/LreController.java:123
```

**Java trace**
  - Java: `LreController.java`:123-136

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. Trigger disjointness does NOT close this (it only separates two EVENT-triggered branches), and the negation synthesis that closes rule6 cannot either, because the event-triggered branch contributes no data predicate to negate. RoboChart has no event-absence guard, so this must be fixed in the Java: either give the later branch an explicit guard that excludes the earlier branch's case, or place the triggerless branch FIRST so the ordinary priority-negation synthesis applies. Witness: LreController.java CAM block (reqOCM then cdaAboveOrAtMinSafe) — 128 of 8192 LRE abstract states admit two operations where the Java is deterministic.

### Issue 6: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by 'cdaBelowMinSafeDist && tcpaNonNegative' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM, event instanceof lre.event.InputEvent.endTask, event instanceof lre.event.InputEvent.reqHCM — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: LreController.step(): triggerless branch guarded by 'cdaBelowMinSafeDist && tcpaNonNegative' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM, event instanceof lre.event.InputEvent.endTask, event instanceof lre.event.InputEvent.reqHCM — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-frozen-run3-lre/java.generated.project/src/main/java/lre/controller/LreController.java:125
```

**Java trace**
  - Java: `LreController.java`:125-136

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. Trigger disjointness does NOT close this (it only separates two EVENT-triggered branches), and the negation synthesis that closes rule6 cannot either, because the event-triggered branch contributes no data predicate to negate. RoboChart has no event-absence guard, so this must be fixed in the Java: either give the later branch an explicit guard that excludes the earlier branch's case, or place the triggerless branch FIRST so the ordinary priority-negation synthesis applies. Witness: LreController.java CAM block (reqOCM then cdaAboveOrAtMinSafe) — 128 of 8192 LRE abstract states admit two operations where the Java is deterministic.

### Issue 7: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by 'cdaBelowMinSafeDist && tcpaNonNegative' does not negate preceding triggerless guard(s): inOpez

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by 'cdaBelowMinSafeDist && tcpaNonNegative' does not negate preceding triggerless guard(s): inOpez
Location: /Users/ranwei/Gitee/forge-frozen-run3-lre/java.generated.project/src/main/java/lre/controller/LreController.java:125
```

**Java trace**
  - Java: `LreController.java`:125-136

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (hcmActive && !camActive)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above. See the hand-written LRE convention in reference-runs/lre/java/controller/LreController.java.

### Issue 8: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by 'hvelAtOrAboveOne && hdistCstcAtOrBelowHoriz' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM, event instanceof lre.event.InputEvent.endTask, event instanceof lre.event.InputEvent.reqHCM — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: LreController.step(): triggerless branch guarded by 'hvelAtOrAboveOne && hdistCstcAtOrBelowHoriz' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM, event instanceof lre.event.InputEvent.endTask, event instanceof lre.event.InputEvent.reqHCM — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-frozen-run3-lre/java.generated.project/src/main/java/lre/controller/LreController.java:127
```

**Java trace**
  - Java: `LreController.java`:127-136

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. Trigger disjointness does NOT close this (it only separates two EVENT-triggered branches), and the negation synthesis that closes rule6 cannot either, because the event-triggered branch contributes no data predicate to negate. RoboChart has no event-absence guard, so this must be fixed in the Java: either give the later branch an explicit guard that excludes the earlier branch's case, or place the triggerless branch FIRST so the ordinary priority-negation synthesis applies. Witness: LreController.java CAM block (reqOCM then cdaAboveOrAtMinSafe) — 128 of 8192 LRE abstract states admit two operations where the Java is deterministic.

### Issue 9: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by 'hvelAtOrAboveOne && hdistCstcAtOrBelowHoriz' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafeDist&&tcpaNonNegative

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by 'hvelAtOrAboveOne && hdistCstcAtOrBelowHoriz' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafeDist&&tcpaNonNegative
Location: /Users/ranwei/Gitee/forge-frozen-run3-lre/java.generated.project/src/main/java/lre/controller/LreController.java:127
```

**Java trace**
  - Java: `LreController.java`:127-136

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (hcmActive && !camActive)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above. See the hand-written LRE convention in reference-runs/lre/java/controller/LreController.java.

### Issue 10: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by 'vdistCstcAtOrBelowDfltVert' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM, event instanceof lre.event.InputEvent.endTask, event instanceof lre.event.InputEvent.reqHCM — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: LreController.step(): triggerless branch guarded by 'vdistCstcAtOrBelowDfltVert' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM, event instanceof lre.event.InputEvent.endTask, event instanceof lre.event.InputEvent.reqHCM — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-frozen-run3-lre/java.generated.project/src/main/java/lre/controller/LreController.java:130
```

**Java trace**
  - Java: `LreController.java`:130-136

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. Trigger disjointness does NOT close this (it only separates two EVENT-triggered branches), and the negation synthesis that closes rule6 cannot either, because the event-triggered branch contributes no data predicate to negate. RoboChart has no event-absence guard, so this must be fixed in the Java: either give the later branch an explicit guard that excludes the earlier branch's case, or place the triggerless branch FIRST so the ordinary priority-negation synthesis applies. Witness: LreController.java CAM block (reqOCM then cdaAboveOrAtMinSafe) — 128 of 8192 LRE abstract states admit two operations where the Java is deterministic.

### Issue 11: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by 'vdistCstcAtOrBelowDfltVert' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafeDist&&tcpaNonNegative, hvelAtOrAboveOne&&hdistCstcAtOrBelowHoriz

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by 'vdistCstcAtOrBelowDfltVert' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafeDist&&tcpaNonNegative, hvelAtOrAboveOne&&hdistCstcAtOrBelowHoriz
Location: /Users/ranwei/Gitee/forge-frozen-run3-lre/java.generated.project/src/main/java/lre/controller/LreController.java:130
```

**Java trace**
  - Java: `LreController.java`:130-136

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (hcmActive && !camActive)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above. See the hand-written LRE convention in reference-runs/lre/java/controller/LreController.java.

### Issue 12: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by 'vvelAtOrAboveOne && vdistCstcAtOrBelowVert' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM, event instanceof lre.event.InputEvent.endTask, event instanceof lre.event.InputEvent.reqHCM — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: LreController.step(): triggerless branch guarded by 'vvelAtOrAboveOne && vdistCstcAtOrBelowVert' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM, event instanceof lre.event.InputEvent.endTask, event instanceof lre.event.InputEvent.reqHCM — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-frozen-run3-lre/java.generated.project/src/main/java/lre/controller/LreController.java:133
```

**Java trace**
  - Java: `LreController.java`:133-136

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. Trigger disjointness does NOT close this (it only separates two EVENT-triggered branches), and the negation synthesis that closes rule6 cannot either, because the event-triggered branch contributes no data predicate to negate. RoboChart has no event-absence guard, so this must be fixed in the Java: either give the later branch an explicit guard that excludes the earlier branch's case, or place the triggerless branch FIRST so the ordinary priority-negation synthesis applies. Witness: LreController.java CAM block (reqOCM then cdaAboveOrAtMinSafe) — 128 of 8192 LRE abstract states admit two operations where the Java is deterministic.

### Issue 13: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by 'vvelAtOrAboveOne && vdistCstcAtOrBelowVert' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafeDist&&tcpaNonNegative, hvelAtOrAboveOne&&hdistCstcAtOrBelowHoriz, vdistCstcAtOrBelowDfltVert

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by 'vvelAtOrAboveOne && vdistCstcAtOrBelowVert' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafeDist&&tcpaNonNegative, hvelAtOrAboveOne&&hdistCstcAtOrBelowHoriz, vdistCstcAtOrBelowDfltVert
Location: /Users/ranwei/Gitee/forge-frozen-run3-lre/java.generated.project/src/main/java/lre/controller/LreController.java:133
```

**Java trace**
  - Java: `LreController.java`:133-136

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (hcmActive && !camActive)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above. See the hand-written LRE convention in reference-runs/lre/java/controller/LreController.java.

### Issue 14: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by 'inOpez' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: LreController.step(): triggerless branch guarded by 'inOpez' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-frozen-run3-lre/java.generated.project/src/main/java/lre/controller/LreController.java:141
```

**Java trace**
  - Java: `LreController.java`:141-148

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. Trigger disjointness does NOT close this (it only separates two EVENT-triggered branches), and the negation synthesis that closes rule6 cannot either, because the event-triggered branch contributes no data predicate to negate. RoboChart has no event-absence guard, so this must be fixed in the Java: either give the later branch an explicit guard that excludes the earlier branch's case, or place the triggerless branch FIRST so the ordinary priority-negation synthesis applies. Witness: LreController.java CAM block (reqOCM then cdaAboveOrAtMinSafe) — 128 of 8192 LRE abstract states admit two operations where the Java is deterministic.

### Issue 15: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by 'cdaBelowMinSafeDist && tcpaNonNegative' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: LreController.step(): triggerless branch guarded by 'cdaBelowMinSafeDist && tcpaNonNegative' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-frozen-run3-lre/java.generated.project/src/main/java/lre/controller/LreController.java:143
```

**Java trace**
  - Java: `LreController.java`:143-148

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. Trigger disjointness does NOT close this (it only separates two EVENT-triggered branches), and the negation synthesis that closes rule6 cannot either, because the event-triggered branch contributes no data predicate to negate. RoboChart has no event-absence guard, so this must be fixed in the Java: either give the later branch an explicit guard that excludes the earlier branch's case, or place the triggerless branch FIRST so the ordinary priority-negation synthesis applies. Witness: LreController.java CAM block (reqOCM then cdaAboveOrAtMinSafe) — 128 of 8192 LRE abstract states admit two operations where the Java is deterministic.

### Issue 16: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by 'cdaBelowMinSafeDist && tcpaNonNegative' does not negate preceding triggerless guard(s): inOpez

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by 'cdaBelowMinSafeDist && tcpaNonNegative' does not negate preceding triggerless guard(s): inOpez
Location: /Users/ranwei/Gitee/forge-frozen-run3-lre/java.generated.project/src/main/java/lre/controller/LreController.java:143
```

**Java trace**
  - Java: `LreController.java`:143-148

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (hcmActive && !camActive)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above. See the hand-written LRE convention in reference-runs/lre/java/controller/LreController.java.

### Issue 17: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by '(!hdistCstcAtOrBelowHoriz) && (!vdistCstcAtOrBelowVert)' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: LreController.step(): triggerless branch guarded by '(!hdistCstcAtOrBelowHoriz) && (!vdistCstcAtOrBelowVert)' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-frozen-run3-lre/java.generated.project/src/main/java/lre/controller/LreController.java:145
```

**Java trace**
  - Java: `LreController.java`:145-148

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. Trigger disjointness does NOT close this (it only separates two EVENT-triggered branches), and the negation synthesis that closes rule6 cannot either, because the event-triggered branch contributes no data predicate to negate. RoboChart has no event-absence guard, so this must be fixed in the Java: either give the later branch an explicit guard that excludes the earlier branch's case, or place the triggerless branch FIRST so the ordinary priority-negation synthesis applies. Witness: LreController.java CAM block (reqOCM then cdaAboveOrAtMinSafe) — 128 of 8192 LRE abstract states admit two operations where the Java is deterministic.

### Issue 18: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by '(!hdistCstcAtOrBelowHoriz) && (!vdistCstcAtOrBelowVert)' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafeDist&&tcpaNonNegative

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by '(!hdistCstcAtOrBelowHoriz) && (!vdistCstcAtOrBelowVert)' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafeDist&&tcpaNonNegative
Location: /Users/ranwei/Gitee/forge-frozen-run3-lre/java.generated.project/src/main/java/lre/controller/LreController.java:145
```

**Java trace**
  - Java: `LreController.java`:145-148

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (hcmActive && !camActive)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above. See the hand-written LRE convention in reference-runs/lre/java/controller/LreController.java.

### Issue 19: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by '!cdaBelowMinSafeDist' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: LreController.step(): triggerless branch guarded by '!cdaBelowMinSafeDist' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-frozen-run3-lre/java.generated.project/src/main/java/lre/controller/LreController.java:153
```

**Java trace**
  - Java: `LreController.java`:153-156

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. Trigger disjointness does NOT close this (it only separates two EVENT-triggered branches), and the negation synthesis that closes rule6 cannot either, because the event-triggered branch contributes no data predicate to negate. RoboChart has no event-absence guard, so this must be fixed in the Java: either give the later branch an explicit guard that excludes the earlier branch's case, or place the triggerless branch FIRST so the ordinary priority-negation synthesis applies. Witness: LreController.java CAM block (reqOCM then cdaAboveOrAtMinSafe) — 128 of 8192 LRE abstract states admit two operations where the Java is deterministic.

### Issue 20: lint_rule7_signed_sentinel_on_controller_state — LreController: controller state field 'cstc' initialised with negative literal -1

**Raw**
```
Rule: rule7_signed_sentinel_on_controller_state
Severity: warning
Message: LreController: controller state field 'cstc' initialised with negative literal -1
Location: /Users/ranwei/Gitee/forge-frozen-run3-lre/java.generated.project/src/main/java/lre/controller/LreController.java:44
```

**Java trace**
  - Java: `LreController.java`:44-44

**Fix directive**
Integer controller-state fields historically mapped to RoboChart 'nat', so a negative sentinel silently leaves the formal type's domain and the model diverges from the Java. Prefer a non-negative sentinel, an Optional/enum encoding, or confirm the M2M maps this field to a signed RoboChart 'int' before relying on negative values. See java_codegen_rules.txt and the int->nat history in the M2M type map.

### Issue 21: lint_rule7_signed_sentinel_on_controller_state — LreController: controller state field 'cdyn' initialised with negative literal -1

**Raw**
```
Rule: rule7_signed_sentinel_on_controller_state
Severity: warning
Message: LreController: controller state field 'cdyn' initialised with negative literal -1
Location: /Users/ranwei/Gitee/forge-frozen-run3-lre/java.generated.project/src/main/java/lre/controller/LreController.java:47
```

**Java trace**
  - Java: `LreController.java`:47-47

**Fix directive**
Integer controller-state fields historically mapped to RoboChart 'nat', so a negative sentinel silently leaves the formal type's domain and the model diverges from the Java. Prefer a non-negative sentinel, an Optional/enum encoding, or confirm the M2M maps this field to a signed RoboChart 'int' before relying on negative values. See java_codegen_rules.txt and the int->nat history in the M2M type map.

### Issue 22: lint_rule4_double_missing_real_annotation — field 'CalcCPA.MIN_REL_SPEED_SQ' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: field 'CalcCPA.MIN_REL_SPEED_SQ' is a double without @RoboChartType("real")
Location: /Users/ranwei/Gitee/forge-frozen-run3-lre/java.generated.project/src/main/java/lre/operation/CalcCPA.java:16
```

**Java trace**
  - Java: `CalcCPA.java`:16-16

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 23: lint_rule4_double_missing_real_annotation — field 'Sensor.SAFE_LARGE_DIST' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: field 'Sensor.SAFE_LARGE_DIST' is a double without @RoboChartType("real")
Location: /Users/ranwei/Gitee/forge-frozen-run3-lre/java.generated.project/src/main/java/lre/sensor/Sensor.java:16
```

**Java trace**
  - Java: `Sensor.java`:16-16

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

## Files to review
(none identified)

## Next step
Preflight found structural rule violations (errors). Fix them before continuing — downstream phases would either crash or produce a silently-wrong formal model.
