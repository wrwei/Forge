# 2c — Preflight (Structural Lint) — PASSED

## Summary
2c — Preflight (Structural Lint): no structural errors.

## Run history
- New this run: 0
- Recurring from previous run: 17
- Resolved since previous run: 6

**Resolved issue titles**
- field 'LreConstants.MIN_SAFE_DIST' is a double without @RoboChartType("real")
- field 'LreConstants.STATIC_OBS_HORIZ_DIST' is a double without @RoboChartType("real")
- field 'LreConstants.STATIC_OBS_VERT_DIST' is a double without @RoboChartType("real")
- field 'LreConstants.STATIC_OBS_DFLT_VERT_DIST' is a double without @RoboChartType("real")
- field 'CalcCPA.MIN_REL_SPEED_SQ' is a double without @RoboChartType("real")
- field 'Sensor.SAFE_LARGE_DIST' is a double without @RoboChartType("real")

## Issues
### Issue 1: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by 'inOpez' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM, event instanceof lre.event.InputEvent.endTask, event instanceof lre.event.InputEvent.reqHCM — in the extracted model both are enabled together [recurring x2]

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

### Issue 2: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by 'cdaBelowMinSafeDist && tcpaNonNegative' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM, event instanceof lre.event.InputEvent.endTask, event instanceof lre.event.InputEvent.reqHCM — in the extracted model both are enabled together [recurring x2]

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

### Issue 3: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by 'cdaBelowMinSafeDist && tcpaNonNegative' does not negate preceding triggerless guard(s): inOpez [recurring x2]

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

### Issue 4: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by 'hvelAtOrAboveOne && hdistCstcAtOrBelowHoriz' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM, event instanceof lre.event.InputEvent.endTask, event instanceof lre.event.InputEvent.reqHCM — in the extracted model both are enabled together [recurring x2]

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

### Issue 5: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by 'hvelAtOrAboveOne && hdistCstcAtOrBelowHoriz' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafeDist&&tcpaNonNegative [recurring x2]

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

### Issue 6: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by 'vdistCstcAtOrBelowDfltVert' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM, event instanceof lre.event.InputEvent.endTask, event instanceof lre.event.InputEvent.reqHCM — in the extracted model both are enabled together [recurring x2]

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

### Issue 7: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by 'vdistCstcAtOrBelowDfltVert' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafeDist&&tcpaNonNegative, hvelAtOrAboveOne&&hdistCstcAtOrBelowHoriz [recurring x2]

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

### Issue 8: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by 'vvelAtOrAboveOne && vdistCstcAtOrBelowVert' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM, event instanceof lre.event.InputEvent.endTask, event instanceof lre.event.InputEvent.reqHCM — in the extracted model both are enabled together [recurring x2]

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

### Issue 9: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by 'vvelAtOrAboveOne && vdistCstcAtOrBelowVert' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafeDist&&tcpaNonNegative, hvelAtOrAboveOne&&hdistCstcAtOrBelowHoriz, vdistCstcAtOrBelowDfltVert [recurring x2]

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

### Issue 10: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by 'inOpez' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM — in the extracted model both are enabled together [recurring x2]

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

### Issue 11: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by 'cdaBelowMinSafeDist && tcpaNonNegative' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM — in the extracted model both are enabled together [recurring x2]

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

### Issue 12: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by 'cdaBelowMinSafeDist && tcpaNonNegative' does not negate preceding triggerless guard(s): inOpez [recurring x2]

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

### Issue 13: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by '(!hdistCstcAtOrBelowHoriz) && (!vdistCstcAtOrBelowVert)' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM — in the extracted model both are enabled together [recurring x2]

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

### Issue 14: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by '(!hdistCstcAtOrBelowHoriz) && (!vdistCstcAtOrBelowVert)' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafeDist&&tcpaNonNegative [recurring x2]

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

### Issue 15: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by '!cdaBelowMinSafeDist' follows event-triggered branch(es): event instanceof lre.event.InputEvent.reqOCM — in the extracted model both are enabled together [recurring x2]

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

### Issue 16: lint_rule7_signed_sentinel_on_controller_state — LreController: controller state field 'cstc' initialised with negative literal -1 [recurring x2]

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

### Issue 17: lint_rule7_signed_sentinel_on_controller_state — LreController: controller state field 'cdyn' initialised with negative literal -1 [recurring x2]

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

## Files to review
(none identified)

## Next step
Proceed to Phase 3 (T2M — Spoon Discovery).
