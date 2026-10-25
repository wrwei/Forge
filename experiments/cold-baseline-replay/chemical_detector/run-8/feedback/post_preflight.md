# 2c — Preflight (Structural Lint) — FAILED

## Summary
2c — Preflight (Structural Lint): 1 structural error(s) found. Downstream phases would produce a wrong formal model.

## Run history
- New this run: 5
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: lint_rule6_missing_priority_negation — GasAnalysisController.step(): branch guarded by 'stsIsGasD' does not negate preceding triggerless guard(s): stsIsNoGas

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: GasAnalysisController.step(): branch guarded by 'stsIsGasD' does not negate preceding triggerless guard(s): stsIsNoGas
Location: /Users/ranwei/Git/Forge/java.generated.project/src/main/java/chemdetector/controller/gas/GasAnalysisController.java:66
```

**Java trace**
  - Java: `GasAnalysisController.java`:66-70

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (hcmActive && !camActive)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above. See the hand-written LRE convention in reference-runs/lre/java/controller/LreController.java.

### Issue 2: lint_rule8_event_branch_precedes_triggerless — MovementController.step(): triggerless branch guarded by 'makingProgress' follows event-triggered branch(es): event instanceof chemdetector.event.InputEvent.Stop, event instanceof chemdetector.event.InputEvent.Resume — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: MovementController.step(): triggerless branch guarded by 'makingProgress' follows event-triggered branch(es): event instanceof chemdetector.event.InputEvent.Stop, event instanceof chemdetector.event.InputEvent.Resume — in the extracted model both are enabled together
Location: /Users/ranwei/Git/Forge/java.generated.project/src/main/java/chemdetector/controller/movement/MovementController.java:169
```

**Java trace**
  - Java: `MovementController.java`:169-185

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. Trigger disjointness does NOT close this (it only separates two EVENT-triggered branches), and the negation synthesis that closes rule6 cannot either, because the event-triggered branch contributes no data predicate to negate. RoboChart has no event-absence guard, so this must be fixed in the Java: either give the later branch an explicit guard that excludes the earlier branch's case, or place the triggerless branch FIRST so the ordinary priority-negation synthesis applies. Witness: LreController.java CAM block (reqOCM then cdaAboveOrAtMinSafe) — 128 of 8192 LRE abstract states admit two operations where the Java is deterministic.

### Issue 3: lint_rule8_event_branch_precedes_triggerless — MovementController.step(): triggerless branch guarded by 'stuck' follows event-triggered branch(es): event instanceof chemdetector.event.InputEvent.Stop, event instanceof chemdetector.event.InputEvent.Resume — in the extracted model both are enabled together

**Raw**
```
Rule: rule8_event_branch_precedes_triggerless
Severity: warning
Message: MovementController.step(): triggerless branch guarded by 'stuck' follows event-triggered branch(es): event instanceof chemdetector.event.InputEvent.Stop, event instanceof chemdetector.event.InputEvent.Resume — in the extracted model both are enabled together
Location: /Users/ranwei/Git/Forge/java.generated.project/src/main/java/chemdetector/controller/movement/MovementController.java:179
```

**Java trace**
  - Java: `MovementController.java`:179-185

**Fix directive**
An event-triggered branch followed by a TRIGGERLESS branch in the same else-if chain has no counterpart in RoboChart: the Java takes only the first branch, but the extracted transitions form an unguarded external choice and the triggerless one is enabled whenever its data guard holds — including when the event is present. Trigger disjointness does NOT close this (it only separates two EVENT-triggered branches), and the negation synthesis that closes rule6 cannot either, because the event-triggered branch contributes no data predicate to negate. RoboChart has no event-absence guard, so this must be fixed in the Java: either give the later branch an explicit guard that excludes the earlier branch's case, or place the triggerless branch FIRST so the ordinary priority-negation synthesis applies. Witness: LreController.java CAM block (reqOCM then cdaAboveOrAtMinSafe) — 128 of 8192 LRE abstract states admit two operations where the Java is deterministic.

### Issue 4: lint_rule6_missing_priority_negation — MovementController.step(): branch guarded by 'stuck' does not negate preceding triggerless guard(s): makingProgress

**Raw**
```
Rule: rule6_missing_priority_negation
Severity: warning
Message: MovementController.step(): branch guarded by 'stuck' does not negate preceding triggerless guard(s): makingProgress
Location: /Users/ranwei/Git/Forge/java.generated.project/src/main/java/chemdetector/controller/movement/MovementController.java:179
```

**Java trace**
  - Java: `MovementController.java`:179-185

**Fix directive**
Branches below a triggerless guarded branch must textually carry the negation of each preceding triggerless guard (e.g. 'else if (hcmActive && !camActive)'). Relying on the implicit priority of else-if produces a formal model whose operations can fire nondeterministically — the extracted preconditions are not mutually exclusive. Conjoin '&& !<guard>' for each guard listed above. See the hand-written LRE convention in reference-runs/lre/java/controller/LreController.java.

### Issue 5: lint_rule4_double_missing_real_annotation — field 'Vehicle.lastVel' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: field 'Vehicle.lastVel' is a double without @RoboChartType("real")
Location: /Users/ranwei/Git/Forge/java.generated.project/src/main/java/chemdetector/vehicle/Vehicle.java:14
```

**Java trace**
  - Java: `Vehicle.java`:14-14

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

## Files to review
(none identified)

## Next step
Preflight found structural rule violations (errors). Fix them before continuing — downstream phases would either crash or produce a silently-wrong formal model.
