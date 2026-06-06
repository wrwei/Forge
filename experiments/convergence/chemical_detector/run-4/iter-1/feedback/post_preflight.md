# 2c — Preflight (Structural Lint) — FAILED

## Summary
2c — Preflight (Structural Lint): 3 structural error(s) found. Downstream phases would produce a wrong formal model.

## Run history
- New this run: 0
- Recurring from previous run: 3
- Resolved since previous run: 0

## Issues
### Issue 1: lint_rule4_double_missing_real_annotation — field 'Constants.THR' is a double without @RoboChartType("real") [recurring x2]

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: field 'Constants.THR' is a double without @RoboChartType("real")
Location: C:/Users/Will/Gitee/fmgvc-cd-run7-redo/java.generated.project/src/main/java/chemdetector/constants/Constants.java:9
```

**Java trace**
  - Java: `Constants.java`:9-9

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 2: lint_rule4_double_missing_real_annotation — field 'Constants.LV' is a double without @RoboChartType("real") [recurring x2]

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: field 'Constants.LV' is a double without @RoboChartType("real")
Location: C:/Users/Will/Gitee/fmgvc-cd-run7-redo/java.generated.project/src/main/java/chemdetector/constants/Constants.java:12
```

**Java trace**
  - Java: `Constants.java`:12-12

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 3: lint_rule4_double_missing_real_annotation — field 'Constants.STUCK_DIST' is a double without @RoboChartType("real") [recurring x2]

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: field 'Constants.STUCK_DIST' is a double without @RoboChartType("real")
Location: C:/Users/Will/Gitee/fmgvc-cd-run7-redo/java.generated.project/src/main/java/chemdetector/constants/Constants.java:21
```

**Java trace**
  - Java: `Constants.java`:21-21

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

## Files to review
- Angle.java
- Chem.java
- GasAnalysisMode.java
- Loc.java
- MovementController.java
- MovementMode.java
- Status.java
- Vehicle.java

## Next step
Preflight found structural rule violations (errors). Fix them before continuing — downstream phases would either crash or produce a silently-wrong formal model.
