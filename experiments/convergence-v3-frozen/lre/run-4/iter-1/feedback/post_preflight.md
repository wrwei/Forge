# 2c — Preflight (Structural Lint) — PASSED

## Summary
2c — Preflight (Structural Lint): no structural errors.

## Run history
- New this run: 17
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by 'inOpez' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM, event instanceof lre.event.InputEvent.EndTask, event instanceof lre.event.InputEvent.ReqHCM — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: LreController.step(): triggerless branch guarded by 'inOpez' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM, event instanceof lre.event.InputEvent.EndTask, event instanceof lre.event.InputEvent.ReqHCM — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-frozen-run4-lre/java.generated.project/src/main/java/lre/controller/LreController.java:144
```

**Java trace**
  - Java: `LreController.java`:144-157

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. Trigger disjointness does NOT close this (it only separates two EVENT-triggered branches), and the negation synthesis that closes rule6 cannot either, because the event-triggered branch contributes no data predicate to negate. RoboChart has no event-absence guard, so this must be fixed in the Java: either give the later branch an explicit guard that excludes the earlier branch's case, or place the triggerless branch FIRST so the ordinary priority-negation synthesis applies. Witness: LreController.java CAM block (reqOCM then cdaAboveOrAtMinSafe) — 128 of 8192 LRE abstract states admit two operations where the Java is deterministic.

### Issue 2: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by 'cdaBelowMinSafe && tcpaNonNegative' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM, event instanceof lre.event.InputEvent.EndTask, event instanceof lre.event.InputEvent.ReqHCM — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: LreController.step(): triggerless branch guarded by 'cdaBelowMinSafe && tcpaNonNegative' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM, event instanceof lre.event.InputEvent.EndTask, event instanceof lre.event.InputEvent.ReqHCM — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-frozen-run4-lre/java.generated.project/src/main/java/lre/controller/LreController.java:146
```

**Java trace**
  - Java: `LreController.java`:146-157

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. Trigger disjointness does NOT close this (it only separates two EVENT-triggered branches), and the negation synthesis that closes rule6 cannot either, because the event-triggered branch contributes no data predicate to negate. RoboChart has no event-absence guard, so this must be fixed in the Java: either give the later branch an explicit guard that excludes the earlier branch's case, or place the triggerless branch FIRST so the ordinary priority-negation synthesis applies. Witness: LreController.java CAM block (reqOCM then cdaAboveOrAtMinSafe) — 128 of 8192 LRE abstract states admit two operations where the Java is deterministic.

### Issue 3: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by 'cdaBelowMinSafe && tcpaNonNegative' does not negate preceding triggerless guard(s): inOpez

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by 'cdaBelowMinSafe && tcpaNonNegative' does not negate preceding triggerless guard(s): inOpez
Location: /Users/ranwei/Gitee/forge-frozen-run4-lre/java.generated.project/src/main/java/lre/controller/LreController.java:146
```

**Java trace**
  - Java: `LreController.java`:146-157

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (hcmActive && !camActive)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above. See the hand-written LRE convention in reference-runs/lre/java/controller/LreController.java.

### Issue 4: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by 'hvelAtLeast1 && hdistCstcAtMostHoriz' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM, event instanceof lre.event.InputEvent.EndTask, event instanceof lre.event.InputEvent.ReqHCM — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: LreController.step(): triggerless branch guarded by 'hvelAtLeast1 && hdistCstcAtMostHoriz' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM, event instanceof lre.event.InputEvent.EndTask, event instanceof lre.event.InputEvent.ReqHCM — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-frozen-run4-lre/java.generated.project/src/main/java/lre/controller/LreController.java:148
```

**Java trace**
  - Java: `LreController.java`:148-157

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. Trigger disjointness does NOT close this (it only separates two EVENT-triggered branches), and the negation synthesis that closes rule6 cannot either, because the event-triggered branch contributes no data predicate to negate. RoboChart has no event-absence guard, so this must be fixed in the Java: either give the later branch an explicit guard that excludes the earlier branch's case, or place the triggerless branch FIRST so the ordinary priority-negation synthesis applies. Witness: LreController.java CAM block (reqOCM then cdaAboveOrAtMinSafe) — 128 of 8192 LRE abstract states admit two operations where the Java is deterministic.

### Issue 5: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by 'hvelAtLeast1 && hdistCstcAtMostHoriz' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafe&&tcpaNonNegative

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by 'hvelAtLeast1 && hdistCstcAtMostHoriz' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafe&&tcpaNonNegative
Location: /Users/ranwei/Gitee/forge-frozen-run4-lre/java.generated.project/src/main/java/lre/controller/LreController.java:148
```

**Java trace**
  - Java: `LreController.java`:148-157

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (hcmActive && !camActive)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above. See the hand-written LRE convention in reference-runs/lre/java/controller/LreController.java.

### Issue 6: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by 'vdistCstcAtMostDfltVert' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM, event instanceof lre.event.InputEvent.EndTask, event instanceof lre.event.InputEvent.ReqHCM — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: LreController.step(): triggerless branch guarded by 'vdistCstcAtMostDfltVert' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM, event instanceof lre.event.InputEvent.EndTask, event instanceof lre.event.InputEvent.ReqHCM — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-frozen-run4-lre/java.generated.project/src/main/java/lre/controller/LreController.java:151
```

**Java trace**
  - Java: `LreController.java`:151-157

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. Trigger disjointness does NOT close this (it only separates two EVENT-triggered branches), and the negation synthesis that closes rule6 cannot either, because the event-triggered branch contributes no data predicate to negate. RoboChart has no event-absence guard, so this must be fixed in the Java: either give the later branch an explicit guard that excludes the earlier branch's case, or place the triggerless branch FIRST so the ordinary priority-negation synthesis applies. Witness: LreController.java CAM block (reqOCM then cdaAboveOrAtMinSafe) — 128 of 8192 LRE abstract states admit two operations where the Java is deterministic.

### Issue 7: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by 'vdistCstcAtMostDfltVert' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafe&&tcpaNonNegative, hvelAtLeast1&&hdistCstcAtMostHoriz

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by 'vdistCstcAtMostDfltVert' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafe&&tcpaNonNegative, hvelAtLeast1&&hdistCstcAtMostHoriz
Location: /Users/ranwei/Gitee/forge-frozen-run4-lre/java.generated.project/src/main/java/lre/controller/LreController.java:151
```

**Java trace**
  - Java: `LreController.java`:151-157

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (hcmActive && !camActive)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above. See the hand-written LRE convention in reference-runs/lre/java/controller/LreController.java.

### Issue 8: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by 'vvelAtLeast1 && vdistCstcAtMostVert' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM, event instanceof lre.event.InputEvent.EndTask, event instanceof lre.event.InputEvent.ReqHCM — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: LreController.step(): triggerless branch guarded by 'vvelAtLeast1 && vdistCstcAtMostVert' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM, event instanceof lre.event.InputEvent.EndTask, event instanceof lre.event.InputEvent.ReqHCM — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-frozen-run4-lre/java.generated.project/src/main/java/lre/controller/LreController.java:154
```

**Java trace**
  - Java: `LreController.java`:154-157

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. Trigger disjointness does NOT close this (it only separates two EVENT-triggered branches), and the negation synthesis that closes rule6 cannot either, because the event-triggered branch contributes no data predicate to negate. RoboChart has no event-absence guard, so this must be fixed in the Java: either give the later branch an explicit guard that excludes the earlier branch's case, or place the triggerless branch FIRST so the ordinary priority-negation synthesis applies. Witness: LreController.java CAM block (reqOCM then cdaAboveOrAtMinSafe) — 128 of 8192 LRE abstract states admit two operations where the Java is deterministic.

### Issue 9: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by 'vvelAtLeast1 && vdistCstcAtMostVert' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafe&&tcpaNonNegative, hvelAtLeast1&&hdistCstcAtMostHoriz, vdistCstcAtMostDfltVert

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by 'vvelAtLeast1 && vdistCstcAtMostVert' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafe&&tcpaNonNegative, hvelAtLeast1&&hdistCstcAtMostHoriz, vdistCstcAtMostDfltVert
Location: /Users/ranwei/Gitee/forge-frozen-run4-lre/java.generated.project/src/main/java/lre/controller/LreController.java:154
```

**Java trace**
  - Java: `LreController.java`:154-157

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (hcmActive && !camActive)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above. See the hand-written LRE convention in reference-runs/lre/java/controller/LreController.java.

### Issue 10: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by 'inOpez' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: LreController.step(): triggerless branch guarded by 'inOpez' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-frozen-run4-lre/java.generated.project/src/main/java/lre/controller/LreController.java:162
```

**Java trace**
  - Java: `LreController.java`:162-169

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. Trigger disjointness does NOT close this (it only separates two EVENT-triggered branches), and the negation synthesis that closes rule6 cannot either, because the event-triggered branch contributes no data predicate to negate. RoboChart has no event-absence guard, so this must be fixed in the Java: either give the later branch an explicit guard that excludes the earlier branch's case, or place the triggerless branch FIRST so the ordinary priority-negation synthesis applies. Witness: LreController.java CAM block (reqOCM then cdaAboveOrAtMinSafe) — 128 of 8192 LRE abstract states admit two operations where the Java is deterministic.

### Issue 11: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by 'cdaBelowMinSafe && tcpaNonNegative' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: LreController.step(): triggerless branch guarded by 'cdaBelowMinSafe && tcpaNonNegative' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-frozen-run4-lre/java.generated.project/src/main/java/lre/controller/LreController.java:164
```

**Java trace**
  - Java: `LreController.java`:164-169

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. Trigger disjointness does NOT close this (it only separates two EVENT-triggered branches), and the negation synthesis that closes rule6 cannot either, because the event-triggered branch contributes no data predicate to negate. RoboChart has no event-absence guard, so this must be fixed in the Java: either give the later branch an explicit guard that excludes the earlier branch's case, or place the triggerless branch FIRST so the ordinary priority-negation synthesis applies. Witness: LreController.java CAM block (reqOCM then cdaAboveOrAtMinSafe) — 128 of 8192 LRE abstract states admit two operations where the Java is deterministic.

### Issue 12: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by 'cdaBelowMinSafe && tcpaNonNegative' does not negate preceding triggerless guard(s): inOpez

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by 'cdaBelowMinSafe && tcpaNonNegative' does not negate preceding triggerless guard(s): inOpez
Location: /Users/ranwei/Gitee/forge-frozen-run4-lre/java.generated.project/src/main/java/lre/controller/LreController.java:164
```

**Java trace**
  - Java: `LreController.java`:164-169

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (hcmActive && !camActive)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above. See the hand-written LRE convention in reference-runs/lre/java/controller/LreController.java.

### Issue 13: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by '(!hdistCstcAtMostHoriz) && (!vdistCstcAtMostVert)' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: LreController.step(): triggerless branch guarded by '(!hdistCstcAtMostHoriz) && (!vdistCstcAtMostVert)' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-frozen-run4-lre/java.generated.project/src/main/java/lre/controller/LreController.java:166
```

**Java trace**
  - Java: `LreController.java`:166-169

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. Trigger disjointness does NOT close this (it only separates two EVENT-triggered branches), and the negation synthesis that closes rule6 cannot either, because the event-triggered branch contributes no data predicate to negate. RoboChart has no event-absence guard, so this must be fixed in the Java: either give the later branch an explicit guard that excludes the earlier branch's case, or place the triggerless branch FIRST so the ordinary priority-negation synthesis applies. Witness: LreController.java CAM block (reqOCM then cdaAboveOrAtMinSafe) — 128 of 8192 LRE abstract states admit two operations where the Java is deterministic.

### Issue 14: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by '(!hdistCstcAtMostHoriz) && (!vdistCstcAtMostVert)' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafe&&tcpaNonNegative

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by '(!hdistCstcAtMostHoriz) && (!vdistCstcAtMostVert)' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafe&&tcpaNonNegative
Location: /Users/ranwei/Gitee/forge-frozen-run4-lre/java.generated.project/src/main/java/lre/controller/LreController.java:166
```

**Java trace**
  - Java: `LreController.java`:166-169

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (hcmActive && !camActive)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above. See the hand-written LRE convention in reference-runs/lre/java/controller/LreController.java.

### Issue 15: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by '!cdaBelowMinSafe' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: LreController.step(): triggerless branch guarded by '!cdaBelowMinSafe' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM — in the extracted model both are enabled together
Location: /Users/ranwei/Gitee/forge-frozen-run4-lre/java.generated.project/src/main/java/lre/controller/LreController.java:174
```

**Java trace**
  - Java: `LreController.java`:174-177

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. Trigger disjointness does NOT close this (it only separates two EVENT-triggered branches), and the negation synthesis that closes rule6 cannot either, because the event-triggered branch contributes no data predicate to negate. RoboChart has no event-absence guard, so this must be fixed in the Java: either give the later branch an explicit guard that excludes the earlier branch's case, or place the triggerless branch FIRST so the ordinary priority-negation synthesis applies. Witness: LreController.java CAM block (reqOCM then cdaAboveOrAtMinSafe) — 128 of 8192 LRE abstract states admit two operations where the Java is deterministic.

### Issue 16: lint_rule7_signed_sentinel_on_controller_state — LreController: controller state field 'cstc' initialised with negative literal -1

**Raw**
```
Rule: rule7_signed_sentinel_on_controller_state
Severity: warning
Message: LreController: controller state field 'cstc' initialised with negative literal -1
Location: /Users/ranwei/Gitee/forge-frozen-run4-lre/java.generated.project/src/main/java/lre/controller/LreController.java:53
```

**Java trace**
  - Java: `LreController.java`:53-53

**Fix directive**
Integer controller-state fields historically mapped to RoboChart 'nat', so a negative sentinel silently leaves the formal type's domain and the model diverges from the Java. Prefer a non-negative sentinel, an Optional/enum encoding, or confirm the M2M maps this field to a signed RoboChart 'int' before relying on negative values. See java_codegen_rules.txt and the int->nat history in the M2M type map.

### Issue 17: lint_rule7_signed_sentinel_on_controller_state — LreController: controller state field 'cdyn' initialised with negative literal -1

**Raw**
```
Rule: rule7_signed_sentinel_on_controller_state
Severity: warning
Message: LreController: controller state field 'cdyn' initialised with negative literal -1
Location: /Users/ranwei/Gitee/forge-frozen-run4-lre/java.generated.project/src/main/java/lre/controller/LreController.java:57
```

**Java trace**
  - Java: `LreController.java`:57-57

**Fix directive**
Integer controller-state fields historically mapped to RoboChart 'nat', so a negative sentinel silently leaves the formal type's domain and the model diverges from the Java. Prefer a non-negative sentinel, an Optional/enum encoding, or confirm the M2M maps this field to a signed RoboChart 'int' before relying on negative values. See java_codegen_rules.txt and the int->nat history in the M2M type map.

## Files to review
(none identified)

## Next step
Proceed to Phase 3 (T2M — Spoon Discovery).
