# 2c — Preflight (Structural Lint) — PASSED

## Summary
2c — Preflight (Structural Lint): no structural errors.

## Run history
- New this run: 16
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: lint_rule6_missing_priority_negation — GasAnalysisController.step(): branch guarded by 'stsIsGasD' does not negate preceding triggerless guard(s): stsIsNoGas

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: GasAnalysisController.step(): branch guarded by 'stsIsGasD' does not negate preceding triggerless guard(s): stsIsNoGas
Location: /Users/ranwei/Git/Forge/java.generated.project/src/main/java/chemdetector/controller/GasAnalysisController.java:87
```

**Java trace**
  - Java: `GasAnalysisController.java`:87-91

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (hcmActive && !camActive)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above. See the hand-written LRE convention in reference-runs/lre/java/controller/LreController.java.

### Issue 2: lint_rule6_missing_priority_negation — MovementController.step(): branch guarded by 'evtIsTurn' does not negate preceding triggerless guard(s): evtIsStop

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: MovementController.step(): branch guarded by 'evtIsTurn' does not negate preceding triggerless guard(s): evtIsStop
Location: /Users/ranwei/Git/Forge/java.generated.project/src/main/java/chemdetector/controller/MovementController.java:116
```

**Java trace**
  - Java: `MovementController.java`:116-126

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (hcmActive && !camActive)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above. See the hand-written LRE convention in reference-runs/lre/java/controller/LreController.java.

### Issue 3: lint_rule6_missing_priority_negation — MovementController.step(): branch guarded by 'evtIsResume' does not negate preceding triggerless guard(s): evtIsStop, evtIsTurn

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: MovementController.step(): branch guarded by 'evtIsResume' does not negate preceding triggerless guard(s): evtIsStop, evtIsTurn
Location: /Users/ranwei/Git/Forge/java.generated.project/src/main/java/chemdetector/controller/MovementController.java:123
```

**Java trace**
  - Java: `MovementController.java`:123-126

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (hcmActive && !camActive)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above. See the hand-written LRE convention in reference-runs/lre/java/controller/LreController.java.

### Issue 4: lint_rule6_missing_priority_negation — MovementController.step(): branch guarded by 'evtIsResume' does not negate preceding triggerless guard(s): evtIsStop

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: MovementController.step(): branch guarded by 'evtIsResume' does not negate preceding triggerless guard(s): evtIsStop
Location: /Users/ranwei/Git/Forge/java.generated.project/src/main/java/chemdetector/controller/MovementController.java:135
```

**Java trace**
  - Java: `MovementController.java`:135-156

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (hcmActive && !camActive)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above. See the hand-written LRE convention in reference-runs/lre/java/controller/LreController.java.

### Issue 5: lint_rule6_missing_priority_negation — MovementController.step(): branch guarded by 'evtIsObstacle' does not negate preceding triggerless guard(s): evtIsStop, evtIsResume

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: MovementController.step(): branch guarded by 'evtIsObstacle' does not negate preceding triggerless guard(s): evtIsStop, evtIsResume
Location: /Users/ranwei/Git/Forge/java.generated.project/src/main/java/chemdetector/controller/MovementController.java:140
```

**Java trace**
  - Java: `MovementController.java`:140-156

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (hcmActive && !camActive)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above. See the hand-written LRE convention in reference-runs/lre/java/controller/LreController.java.

### Issue 6: lint_rule6_missing_priority_negation — MovementController.step(): branch guarded by 'evtIsTurn' does not negate preceding triggerless guard(s): evtIsStop, evtIsResume, evtIsObstacle

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: MovementController.step(): branch guarded by 'evtIsTurn' does not negate preceding triggerless guard(s): evtIsStop, evtIsResume, evtIsObstacle
Location: /Users/ranwei/Git/Forge/java.generated.project/src/main/java/chemdetector/controller/MovementController.java:151
```

**Java trace**
  - Java: `MovementController.java`:151-156

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (hcmActive && !camActive)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above. See the hand-written LRE convention in reference-runs/lre/java/controller/LreController.java.

### Issue 7: lint_rule6_missing_priority_negation — MovementController.step(): branch guarded by 'evtIsResume' does not negate preceding triggerless guard(s): evtIsStop

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: MovementController.step(): branch guarded by 'evtIsResume' does not negate preceding triggerless guard(s): evtIsStop
Location: /Users/ranwei/Git/Forge/java.generated.project/src/main/java/chemdetector/controller/MovementController.java:165
```

**Java trace**
  - Java: `MovementController.java`:165-175

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (hcmActive && !camActive)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above. See the hand-written LRE convention in reference-runs/lre/java/controller/LreController.java.

### Issue 8: lint_rule6_missing_priority_negation — MovementController.step(): branch guarded by 'evtIsTurn' does not negate preceding triggerless guard(s): evtIsStop, evtIsResume

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: MovementController.step(): branch guarded by 'evtIsTurn' does not negate preceding triggerless guard(s): evtIsStop, evtIsResume
Location: /Users/ranwei/Git/Forge/java.generated.project/src/main/java/chemdetector/controller/MovementController.java:170
```

**Java trace**
  - Java: `MovementController.java`:170-175

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (hcmActive && !camActive)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above. See the hand-written LRE convention in reference-runs/lre/java/controller/LreController.java.

### Issue 9: lint_rule6_missing_priority_negation — MovementController.step(): branch guarded by 'evtIsResume' does not negate preceding triggerless guard(s): evtIsStop

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: MovementController.step(): branch guarded by 'evtIsResume' does not negate preceding triggerless guard(s): evtIsStop
Location: /Users/ranwei/Git/Forge/java.generated.project/src/main/java/chemdetector/controller/MovementController.java:184
```

**Java trace**
  - Java: `MovementController.java`:184-201

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (hcmActive && !camActive)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above. See the hand-written LRE convention in reference-runs/lre/java/controller/LreController.java.

### Issue 10: lint_rule6_missing_priority_negation — MovementController.step(): branch guarded by 'evtIsObstacle' does not negate preceding triggerless guard(s): evtIsStop, evtIsResume

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: MovementController.step(): branch guarded by 'evtIsObstacle' does not negate preceding triggerless guard(s): evtIsStop, evtIsResume
Location: /Users/ranwei/Git/Forge/java.generated.project/src/main/java/chemdetector/controller/MovementController.java:189
```

**Java trace**
  - Java: `MovementController.java`:189-201

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (hcmActive && !camActive)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above. See the hand-written LRE convention in reference-runs/lre/java/controller/LreController.java.

### Issue 11: lint_rule6_missing_priority_negation — MovementController.step(): branch guarded by 'evtIsTurn' does not negate preceding triggerless guard(s): evtIsStop, evtIsResume, evtIsObstacle

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: MovementController.step(): branch guarded by 'evtIsTurn' does not negate preceding triggerless guard(s): evtIsStop, evtIsResume, evtIsObstacle
Location: /Users/ranwei/Git/Forge/java.generated.project/src/main/java/chemdetector/controller/MovementController.java:196
```

**Java trace**
  - Java: `MovementController.java`:196-201

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (hcmActive && !camActive)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above. See the hand-written LRE convention in reference-runs/lre/java/controller/LreController.java.

### Issue 12: lint_rule6_missing_priority_negation — MovementController.step(): branch guarded by 'evtIsResume' does not negate preceding triggerless guard(s): evtIsStop

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: MovementController.step(): branch guarded by 'evtIsResume' does not negate preceding triggerless guard(s): evtIsStop
Location: /Users/ranwei/Git/Forge/java.generated.project/src/main/java/chemdetector/controller/MovementController.java:210
```

**Java trace**
  - Java: `MovementController.java`:210-228

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (hcmActive && !camActive)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above. See the hand-written LRE convention in reference-runs/lre/java/controller/LreController.java.

### Issue 13: lint_rule6_missing_priority_negation — MovementController.step(): branch guarded by 'makingProgress' does not negate preceding triggerless guard(s): evtIsStop, evtIsResume

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: MovementController.step(): branch guarded by 'makingProgress' does not negate preceding triggerless guard(s): evtIsStop, evtIsResume
Location: /Users/ranwei/Git/Forge/java.generated.project/src/main/java/chemdetector/controller/MovementController.java:215
```

**Java trace**
  - Java: `MovementController.java`:215-228

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (hcmActive && !camActive)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above. See the hand-written LRE convention in reference-runs/lre/java/controller/LreController.java.

### Issue 14: lint_rule6_missing_priority_negation — MovementController.step(): branch guarded by 'stuck' does not negate preceding triggerless guard(s): evtIsStop, evtIsResume, makingProgress

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: MovementController.step(): branch guarded by 'stuck' does not negate preceding triggerless guard(s): evtIsStop, evtIsResume, makingProgress
Location: /Users/ranwei/Git/Forge/java.generated.project/src/main/java/chemdetector/controller/MovementController.java:224
```

**Java trace**
  - Java: `MovementController.java`:224-228

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (hcmActive && !camActive)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above. See the hand-written LRE convention in reference-runs/lre/java/controller/LreController.java.

### Issue 15: lint_rule6_missing_priority_negation — MovementController.step(): branch guarded by 'evtIsResume' does not negate preceding triggerless guard(s): evtIsStop

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: MovementController.step(): branch guarded by 'evtIsResume' does not negate preceding triggerless guard(s): evtIsStop
Location: /Users/ranwei/Git/Forge/java.generated.project/src/main/java/chemdetector/controller/MovementController.java:237
```

**Java trace**
  - Java: `MovementController.java`:237-247

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (hcmActive && !camActive)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above. See the hand-written LRE convention in reference-runs/lre/java/controller/LreController.java.

### Issue 16: lint_rule6_missing_priority_negation — MovementController.step(): branch guarded by 'evtIsTurn' does not negate preceding triggerless guard(s): evtIsStop, evtIsResume

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: MovementController.step(): branch guarded by 'evtIsTurn' does not negate preceding triggerless guard(s): evtIsStop, evtIsResume
Location: /Users/ranwei/Git/Forge/java.generated.project/src/main/java/chemdetector/controller/MovementController.java:242
```

**Java trace**
  - Java: `MovementController.java`:242-247

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (hcmActive && !camActive)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above. See the hand-written LRE convention in reference-runs/lre/java/controller/LreController.java.

## Files to review
(none identified)

## Next step
Proceed to Phase 3 (T2M — Spoon Discovery).
