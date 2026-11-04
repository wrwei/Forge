# 2c — Preflight (Structural Lint) — FAILED

## Summary
2c — Preflight (Structural Lint): 10 structural error(s) found. Downstream phases would produce a wrong formal model.

## Run history
- New this run: 10
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: lint_rule4_double_missing_real_annotation — parameter 'PlantMonitor.steamOutMaximum#rate' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: parameter 'PlantMonitor.steamOutMaximum#rate' is a double without @RoboChartType("real")
Location: /Users/ranwei/Gitee/forge-v3r16-run1-steam_boiler/java.generated.project/src/main/java/steam_boiler/sensor/PlantMonitor.java:520
```

**Java trace**
  - Java: `PlantMonitor.java`:520-520

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 2: lint_rule4_double_missing_real_annotation — parameter 'PlantMonitor.steamOutMinimum#rate' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: parameter 'PlantMonitor.steamOutMinimum#rate' is a double without @RoboChartType("real")
Location: /Users/ranwei/Gitee/forge-v3r16-run1-steam_boiler/java.generated.project/src/main/java/steam_boiler/sensor/PlantMonitor.java:529
```

**Java trace**
  - Java: `PlantMonitor.java`:529-529

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 3: lint_rule4_double_missing_real_annotation — field 'PlantMonitor.CYCLE' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: field 'PlantMonitor.CYCLE' is a double without @RoboChartType("real")
Location: /Users/ranwei/Gitee/forge-v3r16-run1-steam_boiler/java.generated.project/src/main/java/steam_boiler/sensor/PlantMonitor.java:22
```

**Java trace**
  - Java: `PlantMonitor.java`:22-22

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 4: lint_rule4_double_missing_real_annotation — field 'PlantMonitor.levelValue' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: field 'PlantMonitor.levelValue' is a double without @RoboChartType("real")
Location: /Users/ranwei/Gitee/forge-v3r16-run1-steam_boiler/java.generated.project/src/main/java/steam_boiler/sensor/PlantMonitor.java:35
```

**Java trace**
  - Java: `PlantMonitor.java`:35-35

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 5: lint_rule4_double_missing_real_annotation — field 'PlantMonitor.steamValue' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: field 'PlantMonitor.steamValue' is a double without @RoboChartType("real")
Location: /Users/ranwei/Gitee/forge-v3r16-run1-steam_boiler/java.generated.project/src/main/java/steam_boiler/sensor/PlantMonitor.java:36
```

**Java trace**
  - Java: `PlantMonitor.java`:36-36

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 6: lint_rule4_double_missing_real_annotation — field 'PlantMonitor.previousLow' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: field 'PlantMonitor.previousLow' is a double without @RoboChartType("real")
Location: /Users/ranwei/Gitee/forge-v3r16-run1-steam_boiler/java.generated.project/src/main/java/steam_boiler/sensor/PlantMonitor.java:65
```

**Java trace**
  - Java: `PlantMonitor.java`:65-65

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 7: lint_rule4_double_missing_real_annotation — field 'PlantMonitor.previousHigh' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: field 'PlantMonitor.previousHigh' is a double without @RoboChartType("real")
Location: /Users/ranwei/Gitee/forge-v3r16-run1-steam_boiler/java.generated.project/src/main/java/steam_boiler/sensor/PlantMonitor.java:66
```

**Java trace**
  - Java: `PlantMonitor.java`:66-66

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 8: lint_rule4_double_missing_real_annotation — field 'PlantMonitor.previousSteam' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: field 'PlantMonitor.previousSteam' is a double without @RoboChartType("real")
Location: /Users/ranwei/Gitee/forge-v3r16-run1-steam_boiler/java.generated.project/src/main/java/steam_boiler/sensor/PlantMonitor.java:67
```

**Java trace**
  - Java: `PlantMonitor.java`:67-67

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 9: lint_rule4_double_missing_real_annotation — field 'PlantMonitor.estimateLow' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: field 'PlantMonitor.estimateLow' is a double without @RoboChartType("real")
Location: /Users/ranwei/Gitee/forge-v3r16-run1-steam_boiler/java.generated.project/src/main/java/steam_boiler/sensor/PlantMonitor.java:78
```

**Java trace**
  - Java: `PlantMonitor.java`:78-78

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 10: lint_rule4_double_missing_real_annotation — field 'PlantMonitor.estimateHigh' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: field 'PlantMonitor.estimateHigh' is a double without @RoboChartType("real")
Location: /Users/ranwei/Gitee/forge-v3r16-run1-steam_boiler/java.generated.project/src/main/java/steam_boiler/sensor/PlantMonitor.java:79
```

**Java trace**
  - Java: `PlantMonitor.java`:79-79

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

## Files to review
(none identified)

## Next step
Preflight found structural rule violations (errors). Fix them before continuing — downstream phases would either crash or produce a silently-wrong formal model.
