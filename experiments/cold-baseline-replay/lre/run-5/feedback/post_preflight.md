# 2c — Preflight (Structural Lint) — PASSED

## Summary
2c — Preflight (Structural Lint): no structural errors.

## Run history
- New this run: 15
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by 'cdaBelowMinSafe && tcpaNonNegative' does not negate preceding triggerless guard(s): inOpez

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by 'cdaBelowMinSafe && tcpaNonNegative' does not negate preceding triggerless guard(s): inOpez
Location: /Users/ranwei/Git/Forge/java.generated.project/src/main/java/lre/controller/LreController.java:136
```

**Java trace**
  - Java: `LreController.java`:136-167

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (hcmActive && !camActive)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above. See the hand-written LRE convention in reference-runs/lre/java/controller/LreController.java.

### Issue 2: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by 'event instanceof lre.event.InputEvent.ReqOCM' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafe&&tcpaNonNegative

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by 'event instanceof lre.event.InputEvent.ReqOCM' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafe&&tcpaNonNegative
Location: /Users/ranwei/Git/Forge/java.generated.project/src/main/java/lre/controller/LreController.java:140
```

**Java trace**
  - Java: `LreController.java`:140-167

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (hcmActive && !camActive)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above. See the hand-written LRE convention in reference-runs/lre/java/controller/LreController.java.

### Issue 3: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by 'event instanceof lre.event.InputEvent.EndTask' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafe&&tcpaNonNegative

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by 'event instanceof lre.event.InputEvent.EndTask' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafe&&tcpaNonNegative
Location: /Users/ranwei/Git/Forge/java.generated.project/src/main/java/lre/controller/LreController.java:144
```

**Java trace**
  - Java: `LreController.java`:144-167

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (hcmActive && !camActive)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above. See the hand-written LRE convention in reference-runs/lre/java/controller/LreController.java.

### Issue 4: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by 'event instanceof lre.event.InputEvent.ReqHCM' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafe&&tcpaNonNegative

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by 'event instanceof lre.event.InputEvent.ReqHCM' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafe&&tcpaNonNegative
Location: /Users/ranwei/Git/Forge/java.generated.project/src/main/java/lre/controller/LreController.java:149
```

**Java trace**
  - Java: `LreController.java`:149-167

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (hcmActive && !camActive)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above. See the hand-written LRE convention in reference-runs/lre/java/controller/LreController.java.

### Issue 5: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by 'hvelAtLeast1 && hdistCstcLeqHoriz' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM, event instanceof lre.event.InputEvent.EndTask, event instanceof lre.event.InputEvent.ReqHCM — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: LreController.step(): triggerless branch guarded by 'hvelAtLeast1 && hdistCstcLeqHoriz' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM, event instanceof lre.event.InputEvent.EndTask, event instanceof lre.event.InputEvent.ReqHCM — in the extracted model both are enabled together
Location: /Users/ranwei/Git/Forge/java.generated.project/src/main/java/lre/controller/LreController.java:154
```

**Java trace**
  - Java: `LreController.java`:154-167

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. Trigger disjointness does NOT close this (it only separates two EVENT-triggered branches), and the negation synthesis that closes rule6 cannot either, because the event-triggered branch contributes no data predicate to negate. RoboChart has no event-absence guard, so this must be fixed in the Java: either give the later branch an explicit guard that excludes the earlier branch's case, or place the triggerless branch FIRST so the ordinary priority-negation synthesis applies. Witness: LreController.java CAM block (reqOCM then cdaAboveOrAtMinSafe) — 128 of 8192 LRE abstract states admit two operations where the Java is deterministic.

### Issue 6: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by 'hvelAtLeast1 && hdistCstcLeqHoriz' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafe&&tcpaNonNegative

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by 'hvelAtLeast1 && hdistCstcLeqHoriz' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafe&&tcpaNonNegative
Location: /Users/ranwei/Git/Forge/java.generated.project/src/main/java/lre/controller/LreController.java:154
```

**Java trace**
  - Java: `LreController.java`:154-167

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (hcmActive && !camActive)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above. See the hand-written LRE convention in reference-runs/lre/java/controller/LreController.java.

### Issue 7: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by 'vdistCstcLeqDfltVert' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM, event instanceof lre.event.InputEvent.EndTask, event instanceof lre.event.InputEvent.ReqHCM — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: LreController.step(): triggerless branch guarded by 'vdistCstcLeqDfltVert' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM, event instanceof lre.event.InputEvent.EndTask, event instanceof lre.event.InputEvent.ReqHCM — in the extracted model both are enabled together
Location: /Users/ranwei/Git/Forge/java.generated.project/src/main/java/lre/controller/LreController.java:159
```

**Java trace**
  - Java: `LreController.java`:159-167

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. Trigger disjointness does NOT close this (it only separates two EVENT-triggered branches), and the negation synthesis that closes rule6 cannot either, because the event-triggered branch contributes no data predicate to negate. RoboChart has no event-absence guard, so this must be fixed in the Java: either give the later branch an explicit guard that excludes the earlier branch's case, or place the triggerless branch FIRST so the ordinary priority-negation synthesis applies. Witness: LreController.java CAM block (reqOCM then cdaAboveOrAtMinSafe) — 128 of 8192 LRE abstract states admit two operations where the Java is deterministic.

### Issue 8: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by 'vdistCstcLeqDfltVert' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafe&&tcpaNonNegative, hvelAtLeast1&&hdistCstcLeqHoriz

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by 'vdistCstcLeqDfltVert' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafe&&tcpaNonNegative, hvelAtLeast1&&hdistCstcLeqHoriz
Location: /Users/ranwei/Git/Forge/java.generated.project/src/main/java/lre/controller/LreController.java:159
```

**Java trace**
  - Java: `LreController.java`:159-167

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (hcmActive && !camActive)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above. See the hand-written LRE convention in reference-runs/lre/java/controller/LreController.java.

### Issue 9: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by 'vvelAtLeast1 && vdistCstcLeqVert' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM, event instanceof lre.event.InputEvent.EndTask, event instanceof lre.event.InputEvent.ReqHCM — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: LreController.step(): triggerless branch guarded by 'vvelAtLeast1 && vdistCstcLeqVert' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM, event instanceof lre.event.InputEvent.EndTask, event instanceof lre.event.InputEvent.ReqHCM — in the extracted model both are enabled together
Location: /Users/ranwei/Git/Forge/java.generated.project/src/main/java/lre/controller/LreController.java:164
```

**Java trace**
  - Java: `LreController.java`:164-167

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. Trigger disjointness does NOT close this (it only separates two EVENT-triggered branches), and the negation synthesis that closes rule6 cannot either, because the event-triggered branch contributes no data predicate to negate. RoboChart has no event-absence guard, so this must be fixed in the Java: either give the later branch an explicit guard that excludes the earlier branch's case, or place the triggerless branch FIRST so the ordinary priority-negation synthesis applies. Witness: LreController.java CAM block (reqOCM then cdaAboveOrAtMinSafe) — 128 of 8192 LRE abstract states admit two operations where the Java is deterministic.

### Issue 10: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by 'vvelAtLeast1 && vdistCstcLeqVert' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafe&&tcpaNonNegative, hvelAtLeast1&&hdistCstcLeqHoriz, vdistCstcLeqDfltVert

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by 'vvelAtLeast1 && vdistCstcLeqVert' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafe&&tcpaNonNegative, hvelAtLeast1&&hdistCstcLeqHoriz, vdistCstcLeqDfltVert
Location: /Users/ranwei/Git/Forge/java.generated.project/src/main/java/lre/controller/LreController.java:164
```

**Java trace**
  - Java: `LreController.java`:164-167

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (hcmActive && !camActive)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above. See the hand-written LRE convention in reference-runs/lre/java/controller/LreController.java.

### Issue 11: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by 'cdaBelowMinSafe && tcpaNonNegative' does not negate preceding triggerless guard(s): inOpez

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by 'cdaBelowMinSafe && tcpaNonNegative' does not negate preceding triggerless guard(s): inOpez
Location: /Users/ranwei/Git/Forge/java.generated.project/src/main/java/lre/controller/LreController.java:175
```

**Java trace**
  - Java: `LreController.java`:175-186

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (hcmActive && !camActive)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above. See the hand-written LRE convention in reference-runs/lre/java/controller/LreController.java.

### Issue 12: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by 'event instanceof lre.event.InputEvent.ReqOCM' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafe&&tcpaNonNegative

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by 'event instanceof lre.event.InputEvent.ReqOCM' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafe&&tcpaNonNegative
Location: /Users/ranwei/Git/Forge/java.generated.project/src/main/java/lre/controller/LreController.java:179
```

**Java trace**
  - Java: `LreController.java`:179-186

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (hcmActive && !camActive)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above. See the hand-written LRE convention in reference-runs/lre/java/controller/LreController.java.

### Issue 13: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by 'hdistCstcAboveHoriz && vdistCstcAboveVert' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: LreController.step(): triggerless branch guarded by 'hdistCstcAboveHoriz && vdistCstcAboveVert' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM — in the extracted model both are enabled together
Location: /Users/ranwei/Git/Forge/java.generated.project/src/main/java/lre/controller/LreController.java:183
```

**Java trace**
  - Java: `LreController.java`:183-186

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. Trigger disjointness does NOT close this (it only separates two EVENT-triggered branches), and the negation synthesis that closes rule6 cannot either, because the event-triggered branch contributes no data predicate to negate. RoboChart has no event-absence guard, so this must be fixed in the Java: either give the later branch an explicit guard that excludes the earlier branch's case, or place the triggerless branch FIRST so the ordinary priority-negation synthesis applies. Witness: LreController.java CAM block (reqOCM then cdaAboveOrAtMinSafe) — 128 of 8192 LRE abstract states admit two operations where the Java is deterministic.

### Issue 14: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by 'hdistCstcAboveHoriz && vdistCstcAboveVert' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafe&&tcpaNonNegative

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by 'hdistCstcAboveHoriz && vdistCstcAboveVert' does not negate preceding triggerless guard(s): inOpez, cdaBelowMinSafe&&tcpaNonNegative
Location: /Users/ranwei/Git/Forge/java.generated.project/src/main/java/lre/controller/LreController.java:183
```

**Java trace**
  - Java: `LreController.java`:183-186

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (hcmActive && !camActive)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above. See the hand-written LRE convention in reference-runs/lre/java/controller/LreController.java.

### Issue 15: lint_rule8_event_branch_precedes_triggerless — LreController.step(): triggerless branch guarded by 'cdaAtLeastMinSafe' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: LreController.step(): triggerless branch guarded by 'cdaAtLeastMinSafe' follows event-triggered branch(es): event instanceof lre.event.InputEvent.ReqOCM — in the extracted model both are enabled together
Location: /Users/ranwei/Git/Forge/java.generated.project/src/main/java/lre/controller/LreController.java:194
```

**Java trace**
  - Java: `LreController.java`:194-197

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. Trigger disjointness does NOT close this (it only separates two EVENT-triggered branches), and the negation synthesis that closes rule6 cannot either, because the event-triggered branch contributes no data predicate to negate. RoboChart has no event-absence guard, so this must be fixed in the Java: either give the later branch an explicit guard that excludes the earlier branch's case, or place the triggerless branch FIRST so the ordinary priority-negation synthesis applies. Witness: LreController.java CAM block (reqOCM then cdaAboveOrAtMinSafe) — 128 of 8192 LRE abstract states admit two operations where the Java is deterministic.

## Files to review
(none identified)

## Next step
Proceed to Phase 3 (T2M — Spoon Discovery).
