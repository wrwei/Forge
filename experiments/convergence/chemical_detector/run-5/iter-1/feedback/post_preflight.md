# 2c — Preflight (Structural Lint) — FAILED

## Summary
2c — Preflight (Structural Lint): 5 structural error(s) found. Downstream phases would produce a wrong formal model.

## Run history
- New this run: 0
- Recurring from previous run: 5
- Resolved since previous run: 0

## Issues
### Issue 1: lint_rule4_double_missing_real_annotation — field 'ChemConstants.THR' is a double without @RoboChartType("real") [recurring x2]

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: field 'ChemConstants.THR' is a double without @RoboChartType("real")
Location: C:/Users/Will/Gitee/fmgvc-cd-run9/java.generated.project/src/main/java/chemdetector/constants/ChemConstants.java:11
```

**Java trace**
  - Java: `ChemConstants.java`:11-11

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 2: lint_rule4_double_missing_real_annotation — field 'ChemConstants.LV' is a double without @RoboChartType("real") [recurring x2]

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: field 'ChemConstants.LV' is a double without @RoboChartType("real")
Location: C:/Users/Will/Gitee/fmgvc-cd-run9/java.generated.project/src/main/java/chemdetector/constants/ChemConstants.java:14
```

**Java trace**
  - Java: `ChemConstants.java`:14-14

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 3: lint_rule4_double_missing_real_annotation — field 'ChemConstants.STUCK_DIST' is a double without @RoboChartType("real") [recurring x2]

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: field 'ChemConstants.STUCK_DIST' is a double without @RoboChartType("real")
Location: C:/Users/Will/Gitee/fmgvc-cd-run9/java.generated.project/src/main/java/chemdetector/constants/ChemConstants.java:23
```

**Java trace**
  - Java: `ChemConstants.java`:23-23

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 4: lint_rule4_double_missing_real_annotation — parameter 'ChemSensorService.goreq#first' is a double without @RoboChartType("real") [recurring x2]

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: parameter 'ChemSensorService.goreq#first' is a double without @RoboChartType("real")
Location: C:/Users/Will/Gitee/fmgvc-cd-run9/java.generated.project/src/main/java/chemdetector/sensor/ChemSensorService.java:80
```

**Java trace**
  - Java: `ChemSensorService.java`:80-80

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 5: lint_rule4_double_missing_real_annotation — parameter 'ChemSensorService.goreq#second' is a double without @RoboChartType("real") [recurring x2]

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: parameter 'ChemSensorService.goreq#second' is a double without @RoboChartType("real")
Location: C:/Users/Will/Gitee/fmgvc-cd-run9/java.generated.project/src/main/java/chemdetector/sensor/ChemSensorService.java:80
```

**Java trace**
  - Java: `ChemSensorService.java`:80-80

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

## Files to review
- Angle.java
- Chem.java
- GasAnalysisController.java
- GasAnalysisMode.java
- Loc.java
- MovementMode.java
- Status.java
- Vehicle.java

## Next step
Preflight found structural rule violations (errors). Fix them before continuing — downstream phases would either crash or produce a silently-wrong formal model.
