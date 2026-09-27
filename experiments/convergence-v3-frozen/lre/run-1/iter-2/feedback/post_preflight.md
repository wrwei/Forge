# 2c — Preflight (Structural Lint) — PASSED

## Summary
2c — Preflight (Structural Lint): no structural errors.

## Run history
- New this run: 0
- Recurring from previous run: 6
- Resolved since previous run: 0

## Issues
### Issue 1: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by '(((hcmVertVel && (!inOpez)) && (!camActive)) && (!hcmHorizVel)) && (!vdistCstcAtMostDfltVert)' does not negate preceding triggerless guard(s): (vdistCstcAtMostDfltVert) [recurring x2]

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by '(((hcmVertVel && (!inOpez)) && (!camActive)) && (!hcmHorizVel)) && (!vdistCstcAtMostDfltVert)' does not negate preceding triggerless guard(s): (vdistCstcAtMostDfltVert)
Location: /Users/ranwei/Gitee/forge-frozen-run1-lre/java.generated.project/src/main/java/lre/controller/LreController.java:127
```

**Java trace**
  - Java: `LreController.java`:127-139

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (hcmActive && !camActive)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above. See the hand-written LRE convention in reference-runs/lre/java/controller/LreController.java.

### Issue 2: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by 'event instanceof lre.event.InputEvent.reqOCM' does not negate preceding triggerless guard(s): inOpez, camActive, hcmHorizVel, (vdistCstcAtMostDfltVert), (((hcmVertVel)))&&!vdistCstcAtMostDfltVert [recurring x2]

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by 'event instanceof lre.event.InputEvent.reqOCM' does not negate preceding triggerless guard(s): inOpez, camActive, hcmHorizVel, (vdistCstcAtMostDfltVert), (((hcmVertVel)))&&!vdistCstcAtMostDfltVert
Location: /Users/ranwei/Gitee/forge-frozen-run1-lre/java.generated.project/src/main/java/lre/controller/LreController.java:131
```

**Java trace**
  - Java: `LreController.java`:131-139

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (hcmActive && !camActive)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above. See the hand-written LRE convention in reference-runs/lre/java/controller/LreController.java.

### Issue 3: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by 'event instanceof lre.event.InputEvent.endTask' does not negate preceding triggerless guard(s): inOpez, camActive, hcmHorizVel, (vdistCstcAtMostDfltVert), (((hcmVertVel)))&&!vdistCstcAtMostDfltVert [recurring x2]

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by 'event instanceof lre.event.InputEvent.endTask' does not negate preceding triggerless guard(s): inOpez, camActive, hcmHorizVel, (vdistCstcAtMostDfltVert), (((hcmVertVel)))&&!vdistCstcAtMostDfltVert
Location: /Users/ranwei/Gitee/forge-frozen-run1-lre/java.generated.project/src/main/java/lre/controller/LreController.java:133
```

**Java trace**
  - Java: `LreController.java`:133-139

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (hcmActive && !camActive)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above. See the hand-written LRE convention in reference-runs/lre/java/controller/LreController.java.

### Issue 4: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by 'event instanceof lre.event.InputEvent.reqHCM' does not negate preceding triggerless guard(s): inOpez, camActive, hcmHorizVel, (vdistCstcAtMostDfltVert), (((hcmVertVel)))&&!vdistCstcAtMostDfltVert [recurring x2]

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by 'event instanceof lre.event.InputEvent.reqHCM' does not negate preceding triggerless guard(s): inOpez, camActive, hcmHorizVel, (vdistCstcAtMostDfltVert), (((hcmVertVel)))&&!vdistCstcAtMostDfltVert
Location: /Users/ranwei/Gitee/forge-frozen-run1-lre/java.generated.project/src/main/java/lre/controller/LreController.java:136
```

**Java trace**
  - Java: `LreController.java`:136-139

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (hcmActive && !camActive)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above. See the hand-written LRE convention in reference-runs/lre/java/controller/LreController.java.

### Issue 5: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by 'event instanceof lre.event.InputEvent.reqOCM' does not negate preceding triggerless guard(s): inOpez, camActive, hcmCleared [recurring x2]

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by 'event instanceof lre.event.InputEvent.reqOCM' does not negate preceding triggerless guard(s): inOpez, camActive, hcmCleared
Location: /Users/ranwei/Gitee/forge-frozen-run1-lre/java.generated.project/src/main/java/lre/controller/LreController.java:149
```

**Java trace**
  - Java: `LreController.java`:149-151

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (hcmActive && !camActive)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above. See the hand-written LRE convention in reference-runs/lre/java/controller/LreController.java.

### Issue 6: lint_rule6_missing_priority_negation — LreController.step(): branch guarded by 'event instanceof lre.event.InputEvent.reqOCM' does not negate preceding triggerless guard(s): !cdaBelowMinSafeDist [recurring x2]

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: LreController.step(): branch guarded by 'event instanceof lre.event.InputEvent.reqOCM' does not negate preceding triggerless guard(s): !cdaBelowMinSafeDist
Location: /Users/ranwei/Gitee/forge-frozen-run1-lre/java.generated.project/src/main/java/lre/controller/LreController.java:157
```

**Java trace**
  - Java: `LreController.java`:157-159

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (hcmActive && !camActive)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above. See the hand-written LRE convention in reference-runs/lre/java/controller/LreController.java.

## Files to review
(none identified)

## Next step
Proceed to Phase 3 (T2M — Spoon Discovery).
