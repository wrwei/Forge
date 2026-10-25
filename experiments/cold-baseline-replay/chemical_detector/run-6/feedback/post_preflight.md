# 2c — Preflight (Structural Lint) — FAILED

## Summary
2c — Preflight (Structural Lint): 1 structural error(s) found. Downstream phases would produce a wrong formal model.

## Run history
- New this run: 17
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: lint_rule6_missing_priority_negation — GasAnalysisController.step(): branch guarded by 'stsIsGasD' does not negate preceding triggerless guard(s): stsIsNoGas

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: GasAnalysisController.step(): branch guarded by 'stsIsGasD' does not negate preceding triggerless guard(s): stsIsNoGas
Location: /Users/ranwei/Git/Forge/java.generated.project/src/main/java/chemdetector/controller/GasAnalysisController.java:78
```

**Java trace**
  - Java: `GasAnalysisController.java`:78-83

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (hcmActive && !camActive)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above. See the hand-written LRE convention in reference-runs/lre/java/controller/LreController.java.

### Issue 2: lint_rule6_missing_priority_negation — MovementController.step(): branch guarded by 'evtIsResume' does not negate preceding triggerless guard(s): evtIsStop

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: MovementController.step(): branch guarded by 'evtIsResume' does not negate preceding triggerless guard(s): evtIsStop
Location: /Users/ranwei/Git/Forge/java.generated.project/src/main/java/chemdetector/controller/MovementController.java:103
```

**Java trace**
  - Java: `MovementController.java`:103-114

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (hcmActive && !camActive)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above. See the hand-written LRE convention in reference-runs/lre/java/controller/LreController.java.

### Issue 3: lint_rule6_missing_priority_negation — MovementController.step(): branch guarded by 'evtIsTurn' does not negate preceding triggerless guard(s): evtIsStop, evtIsResume

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: MovementController.step(): branch guarded by 'evtIsTurn' does not negate preceding triggerless guard(s): evtIsStop, evtIsResume
Location: /Users/ranwei/Git/Forge/java.generated.project/src/main/java/chemdetector/controller/MovementController.java:107
```

**Java trace**
  - Java: `MovementController.java`:107-114

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (hcmActive && !camActive)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above. See the hand-written LRE convention in reference-runs/lre/java/controller/LreController.java.

### Issue 4: lint_rule6_missing_priority_negation — MovementController.step(): branch guarded by 'evtIsResume' does not negate preceding triggerless guard(s): evtIsStop

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: MovementController.step(): branch guarded by 'evtIsResume' does not negate preceding triggerless guard(s): evtIsStop
Location: /Users/ranwei/Git/Forge/java.generated.project/src/main/java/chemdetector/controller/MovementController.java:121
```

**Java trace**
  - Java: `MovementController.java`:121-143

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (hcmActive && !camActive)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above. See the hand-written LRE convention in reference-runs/lre/java/controller/LreController.java.

### Issue 5: lint_rule6_missing_priority_negation — MovementController.step(): branch guarded by 'evtIsTurn' does not negate preceding triggerless guard(s): evtIsStop, evtIsResume

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: MovementController.step(): branch guarded by 'evtIsTurn' does not negate preceding triggerless guard(s): evtIsStop, evtIsResume
Location: /Users/ranwei/Git/Forge/java.generated.project/src/main/java/chemdetector/controller/MovementController.java:125
```

**Java trace**
  - Java: `MovementController.java`:125-143

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (hcmActive && !camActive)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above. See the hand-written LRE convention in reference-runs/lre/java/controller/LreController.java.

### Issue 6: lint_rule6_missing_priority_negation — MovementController.step(): branch guarded by 'evtIsObstacle' does not negate preceding triggerless guard(s): evtIsStop, evtIsResume, evtIsTurn

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: MovementController.step(): branch guarded by 'evtIsObstacle' does not negate preceding triggerless guard(s): evtIsStop, evtIsResume, evtIsTurn
Location: /Users/ranwei/Git/Forge/java.generated.project/src/main/java/chemdetector/controller/MovementController.java:131
```

**Java trace**
  - Java: `MovementController.java`:131-143

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (hcmActive && !camActive)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above. See the hand-written LRE convention in reference-runs/lre/java/controller/LreController.java.

### Issue 7: lint_rule6_missing_priority_negation — MovementController.step(): branch guarded by 'evtIsResume' does not negate preceding triggerless guard(s): evtIsStop

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: MovementController.step(): branch guarded by 'evtIsResume' does not negate preceding triggerless guard(s): evtIsStop
Location: /Users/ranwei/Git/Forge/java.generated.project/src/main/java/chemdetector/controller/MovementController.java:150
```

**Java trace**
  - Java: `MovementController.java`:150-161

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (hcmActive && !camActive)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above. See the hand-written LRE convention in reference-runs/lre/java/controller/LreController.java.

### Issue 8: lint_rule6_missing_priority_negation — MovementController.step(): branch guarded by 'evtIsTurn' does not negate preceding triggerless guard(s): evtIsStop, evtIsResume

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: MovementController.step(): branch guarded by 'evtIsTurn' does not negate preceding triggerless guard(s): evtIsStop, evtIsResume
Location: /Users/ranwei/Git/Forge/java.generated.project/src/main/java/chemdetector/controller/MovementController.java:154
```

**Java trace**
  - Java: `MovementController.java`:154-161

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (hcmActive && !camActive)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above. See the hand-written LRE convention in reference-runs/lre/java/controller/LreController.java.

### Issue 9: lint_rule6_missing_priority_negation — MovementController.step(): branch guarded by 'evtIsResume' does not negate preceding triggerless guard(s): evtIsStop

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: MovementController.step(): branch guarded by 'evtIsResume' does not negate preceding triggerless guard(s): evtIsStop
Location: /Users/ranwei/Git/Forge/java.generated.project/src/main/java/chemdetector/controller/MovementController.java:168
```

**Java trace**
  - Java: `MovementController.java`:168-184

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (hcmActive && !camActive)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above. See the hand-written LRE convention in reference-runs/lre/java/controller/LreController.java.

### Issue 10: lint_rule6_missing_priority_negation — MovementController.step(): branch guarded by 'evtIsTurn' does not negate preceding triggerless guard(s): evtIsStop, evtIsResume

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: MovementController.step(): branch guarded by 'evtIsTurn' does not negate preceding triggerless guard(s): evtIsStop, evtIsResume
Location: /Users/ranwei/Git/Forge/java.generated.project/src/main/java/chemdetector/controller/MovementController.java:172
```

**Java trace**
  - Java: `MovementController.java`:172-184

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (hcmActive && !camActive)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above. See the hand-written LRE convention in reference-runs/lre/java/controller/LreController.java.

### Issue 11: lint_rule6_missing_priority_negation — MovementController.step(): branch guarded by 'evtIsObstacle' does not negate preceding triggerless guard(s): evtIsStop, evtIsResume, evtIsTurn

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: MovementController.step(): branch guarded by 'evtIsObstacle' does not negate preceding triggerless guard(s): evtIsStop, evtIsResume, evtIsTurn
Location: /Users/ranwei/Git/Forge/java.generated.project/src/main/java/chemdetector/controller/MovementController.java:178
```

**Java trace**
  - Java: `MovementController.java`:178-184

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (hcmActive && !camActive)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above. See the hand-written LRE convention in reference-runs/lre/java/controller/LreController.java.

### Issue 12: lint_rule6_missing_priority_negation — MovementController.step(): branch guarded by 'evtIsResume' does not negate preceding triggerless guard(s): evtIsStop

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: MovementController.step(): branch guarded by 'evtIsResume' does not negate preceding triggerless guard(s): evtIsStop
Location: /Users/ranwei/Git/Forge/java.generated.project/src/main/java/chemdetector/controller/MovementController.java:191
```

**Java trace**
  - Java: `MovementController.java`:191-209

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (hcmActive && !camActive)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above. See the hand-written LRE convention in reference-runs/lre/java/controller/LreController.java.

### Issue 13: lint_rule6_missing_priority_negation — MovementController.step(): branch guarded by 'makingProgress' does not negate preceding triggerless guard(s): evtIsStop, evtIsResume

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: MovementController.step(): branch guarded by 'makingProgress' does not negate preceding triggerless guard(s): evtIsStop, evtIsResume
Location: /Users/ranwei/Git/Forge/java.generated.project/src/main/java/chemdetector/controller/MovementController.java:195
```

**Java trace**
  - Java: `MovementController.java`:195-209

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (hcmActive && !camActive)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above. See the hand-written LRE convention in reference-runs/lre/java/controller/LreController.java.

### Issue 14: lint_rule6_missing_priority_negation — MovementController.step(): branch guarded by 'stuckDetected' does not negate preceding triggerless guard(s): evtIsStop, evtIsResume, makingProgress

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: MovementController.step(): branch guarded by 'stuckDetected' does not negate preceding triggerless guard(s): evtIsStop, evtIsResume, makingProgress
Location: /Users/ranwei/Git/Forge/java.generated.project/src/main/java/chemdetector/controller/MovementController.java:203
```

**Java trace**
  - Java: `MovementController.java`:203-209

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (hcmActive && !camActive)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above. See the hand-written LRE convention in reference-runs/lre/java/controller/LreController.java.

### Issue 15: lint_rule6_missing_priority_negation — MovementController.step(): branch guarded by 'evtIsResume' does not negate preceding triggerless guard(s): evtIsStop

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: MovementController.step(): branch guarded by 'evtIsResume' does not negate preceding triggerless guard(s): evtIsStop
Location: /Users/ranwei/Git/Forge/java.generated.project/src/main/java/chemdetector/controller/MovementController.java:216
```

**Java trace**
  - Java: `MovementController.java`:216-226

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (hcmActive && !camActive)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above. See the hand-written LRE convention in reference-runs/lre/java/controller/LreController.java.

### Issue 16: lint_rule6_missing_priority_negation — MovementController.step(): branch guarded by 'evtIsTurn' does not negate preceding triggerless guard(s): evtIsStop, evtIsResume

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: MovementController.step(): branch guarded by 'evtIsTurn' does not negate preceding triggerless guard(s): evtIsStop, evtIsResume
Location: /Users/ranwei/Git/Forge/java.generated.project/src/main/java/chemdetector/controller/MovementController.java:220
```

**Java trace**
  - Java: `MovementController.java`:220-226

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (hcmActive && !camActive)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above. See the hand-written LRE convention in reference-runs/lre/java/controller/LreController.java.

### Issue 17: lint_rule4_double_missing_real_annotation — field 'Intensity.value' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: field 'Intensity.value' is a double without @RoboChartType("real")
Location: /Users/ranwei/Git/Forge/java.generated.project/src/main/java/chemdetector/domain/Intensity.java:7
```

**Java trace**
  - Java: `Intensity.java`:7-7

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

## Files to review
(none identified)

## Next step
Preflight found structural rule violations (errors). Fix them before continuing — downstream phases would either crash or produce a silently-wrong formal model.
