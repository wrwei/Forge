# 2c — Preflight (Structural Lint) — PASSED

## Summary
2c — Preflight (Structural Lint): no structural errors.

## Run history
- New this run: 8
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: lint_rule6_missing_priority_negation — GasAnalysis.step(): branch guarded by 'stsGasD' does not negate preceding triggerless guard(s): stsNoGas

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: GasAnalysis.step(): branch guarded by 'stsGasD' does not negate preceding triggerless guard(s): stsNoGas
Location: /Users/ranwei/Git/Forge/java.generated.project/src/main/java/chemdetector/controller/GasAnalysis.java:74
```

**Java trace**
  - Java: `GasAnalysis.java`:74-78

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (hcmActive && !camActive)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above. See the hand-written LRE convention in reference-runs/lre/java/controller/LreController.java.

### Issue 2: lint_rule6_missing_priority_negation — Movement.step(): branch guarded by 'l == chemdetector.datatype.Loc.right' does not negate preceding triggerless guard(s): l==chemdetector.datatype.Loc.left

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: Movement.step(): branch guarded by 'l == chemdetector.datatype.Loc.right' does not negate preceding triggerless guard(s): l==chemdetector.datatype.Loc.left
Location: /Users/ranwei/Git/Forge/java.generated.project/src/main/java/chemdetector/controller/Movement.java:131
```

**Java trace**
  - Java: `Movement.java`:131-135

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (hcmActive && !camActive)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above. See the hand-written LRE convention in reference-runs/lre/java/controller/LreController.java.

### Issue 3: lint_rule6_missing_priority_negation — Movement.step(): branch guarded by 'l == chemdetector.datatype.Loc.front' does not negate preceding triggerless guard(s): l==chemdetector.datatype.Loc.left, l==chemdetector.datatype.Loc.right

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: Movement.step(): branch guarded by 'l == chemdetector.datatype.Loc.front' does not negate preceding triggerless guard(s): l==chemdetector.datatype.Loc.left, l==chemdetector.datatype.Loc.right
Location: /Users/ranwei/Git/Forge/java.generated.project/src/main/java/chemdetector/controller/Movement.java:133
```

**Java trace**
  - Java: `Movement.java`:133-135

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (hcmActive && !camActive)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above. See the hand-written LRE convention in reference-runs/lre/java/controller/LreController.java.

### Issue 4: lint_rule8_event_branch_precedes_triggerless — Movement.step(): triggerless branch guarded by 'makingProgress' follows event-triggered branch(es): event instanceof chemdetector.event.MovementInputEvent.Stop, event instanceof chemdetector.event.MovementInputEvent.Resume — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: Movement.step(): triggerless branch guarded by 'makingProgress' follows event-triggered branch(es): event instanceof chemdetector.event.MovementInputEvent.Stop, event instanceof chemdetector.event.MovementInputEvent.Resume — in the extracted model both are enabled together
Location: /Users/ranwei/Git/Forge/java.generated.project/src/main/java/chemdetector/controller/Movement.java:210
```

**Java trace**
  - Java: `Movement.java`:210-228

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. Trigger disjointness does NOT close this (it only separates two EVENT-triggered branches), and the negation synthesis that closes rule6 cannot either, because the event-triggered branch contributes no data predicate to negate. RoboChart has no event-absence guard, so this must be fixed in the Java: either give the later branch an explicit guard that excludes the earlier branch's case, or place the triggerless branch FIRST so the ordinary priority-negation synthesis applies. Witness: LreController.java CAM block (reqOCM then cdaAboveOrAtMinSafe) — 128 of 8192 LRE abstract states admit two operations where the Java is deterministic.

### Issue 5: lint_rule8_event_branch_precedes_triggerless — Movement.step(): triggerless branch guarded by 'stuck' follows event-triggered branch(es): event instanceof chemdetector.event.MovementInputEvent.Stop, event instanceof chemdetector.event.MovementInputEvent.Resume — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: Movement.step(): triggerless branch guarded by 'stuck' follows event-triggered branch(es): event instanceof chemdetector.event.MovementInputEvent.Stop, event instanceof chemdetector.event.MovementInputEvent.Resume — in the extracted model both are enabled together
Location: /Users/ranwei/Git/Forge/java.generated.project/src/main/java/chemdetector/controller/Movement.java:223
```

**Java trace**
  - Java: `Movement.java`:223-228

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. Trigger disjointness does NOT close this (it only separates two EVENT-triggered branches), and the negation synthesis that closes rule6 cannot either, because the event-triggered branch contributes no data predicate to negate. RoboChart has no event-absence guard, so this must be fixed in the Java: either give the later branch an explicit guard that excludes the earlier branch's case, or place the triggerless branch FIRST so the ordinary priority-negation synthesis applies. Witness: LreController.java CAM block (reqOCM then cdaAboveOrAtMinSafe) — 128 of 8192 LRE abstract states admit two operations where the Java is deterministic.

### Issue 6: lint_rule6_missing_priority_negation — Movement.step(): branch guarded by 'stuck' does not negate preceding triggerless guard(s): makingProgress

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: Movement.step(): branch guarded by 'stuck' does not negate preceding triggerless guard(s): makingProgress
Location: /Users/ranwei/Git/Forge/java.generated.project/src/main/java/chemdetector/controller/Movement.java:223
```

**Java trace**
  - Java: `Movement.java`:223-228

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (hcmActive && !camActive)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above. See the hand-written LRE convention in reference-runs/lre/java/controller/LreController.java.

### Issue 7: lint_rule6_missing_priority_negation — Movement.step(): branch guarded by 'l == chemdetector.datatype.Loc.right' does not negate preceding triggerless guard(s): l==chemdetector.datatype.Loc.left

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: Movement.step(): branch guarded by 'l == chemdetector.datatype.Loc.right' does not negate preceding triggerless guard(s): l==chemdetector.datatype.Loc.left
Location: /Users/ranwei/Git/Forge/java.generated.project/src/main/java/chemdetector/controller/Movement.java:215
```

**Java trace**
  - Java: `Movement.java`:215-219

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (hcmActive && !camActive)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above. See the hand-written LRE convention in reference-runs/lre/java/controller/LreController.java.

### Issue 8: lint_rule6_missing_priority_negation — Movement.step(): branch guarded by 'l == chemdetector.datatype.Loc.front' does not negate preceding triggerless guard(s): l==chemdetector.datatype.Loc.left, l==chemdetector.datatype.Loc.right

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: Movement.step(): branch guarded by 'l == chemdetector.datatype.Loc.front' does not negate preceding triggerless guard(s): l==chemdetector.datatype.Loc.left, l==chemdetector.datatype.Loc.right
Location: /Users/ranwei/Git/Forge/java.generated.project/src/main/java/chemdetector/controller/Movement.java:217
```

**Java trace**
  - Java: `Movement.java`:217-219

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (hcmActive && !camActive)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above. See the hand-written LRE convention in reference-runs/lre/java/controller/LreController.java.

## Files to review
(none identified)

## Next step
Proceed to Phase 3 (T2M — Spoon Discovery).
