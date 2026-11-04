# 2c — Preflight (Structural Lint) — FAILED

## Summary
2c — Preflight (Structural Lint): 11 structural error(s) found. Downstream phases would produce a wrong formal model.

## Run history
- New this run: 11
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: lint_rule4_double_missing_real_annotation — parameter 'CycleAnalyser.clampToCapacity#quantity' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: parameter 'CycleAnalyser.clampToCapacity#quantity' is a double without @RoboChartType("real")
Location: /Users/ranwei/Gitee/forge-v3c16-run7-steam_boiler/java.generated.project/src/main/java/steam_boiler/sensor/CycleAnalyser.java:498
```

**Java trace**
  - Java: `CycleAnalyser.java`:498-498

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 2: lint_rule4_double_missing_real_annotation — parameter 'CycleAnalyser.steamOutflowMax#rate' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: parameter 'CycleAnalyser.steamOutflowMax#rate' is a double without @RoboChartType("real")
Location: /Users/ranwei/Gitee/forge-v3c16-run7-steam_boiler/java.generated.project/src/main/java/steam_boiler/sensor/CycleAnalyser.java:487
```

**Java trace**
  - Java: `CycleAnalyser.java`:487-487

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 3: lint_rule4_double_missing_real_annotation — parameter 'CycleAnalyser.steamOutflowMin#rate' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: parameter 'CycleAnalyser.steamOutflowMin#rate' is a double without @RoboChartType("real")
Location: /Users/ranwei/Gitee/forge-v3c16-run7-steam_boiler/java.generated.project/src/main/java/steam_boiler/sensor/CycleAnalyser.java:493
```

**Java trace**
  - Java: `CycleAnalyser.java`:493-493

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 4: lint_rule4_double_missing_real_annotation — parameter 'CycleAnalyser.updateLevel#outflowMin' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: parameter 'CycleAnalyser.updateLevel#outflowMin' is a double without @RoboChartType("real")
Location: /Users/ranwei/Gitee/forge-v3c16-run7-steam_boiler/java.generated.project/src/main/java/steam_boiler/sensor/CycleAnalyser.java:399
```

**Java trace**
  - Java: `CycleAnalyser.java`:399-399

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 5: lint_rule4_double_missing_real_annotation — parameter 'CycleAnalyser.updateLevel#outflowMax' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: parameter 'CycleAnalyser.updateLevel#outflowMax' is a double without @RoboChartType("real")
Location: /Users/ranwei/Gitee/forge-v3c16-run7-steam_boiler/java.generated.project/src/main/java/steam_boiler/sensor/CycleAnalyser.java:399
```

**Java trace**
  - Java: `CycleAnalyser.java`:399-399

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 6: lint_rule4_double_missing_real_annotation — field 'CycleAnalyser.CYCLE' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: field 'CycleAnalyser.CYCLE' is a double without @RoboChartType("real")
Location: /Users/ranwei/Gitee/forge-v3c16-run7-steam_boiler/java.generated.project/src/main/java/steam_boiler/sensor/CycleAnalyser.java:28
```

**Java trace**
  - Java: `CycleAnalyser.java`:28-28

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 7: lint_rule4_double_missing_real_annotation — field 'CycleAnalyser.levelReading' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: field 'CycleAnalyser.levelReading' is a double without @RoboChartType("real")
Location: /Users/ranwei/Gitee/forge-v3c16-run7-steam_boiler/java.generated.project/src/main/java/steam_boiler/sensor/CycleAnalyser.java:41
```

**Java trace**
  - Java: `CycleAnalyser.java`:41-41

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 8: lint_rule4_double_missing_real_annotation — field 'CycleAnalyser.steamReading' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: field 'CycleAnalyser.steamReading' is a double without @RoboChartType("real")
Location: /Users/ranwei/Gitee/forge-v3c16-run7-steam_boiler/java.generated.project/src/main/java/steam_boiler/sensor/CycleAnalyser.java:42
```

**Java trace**
  - Java: `CycleAnalyser.java`:42-42

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 9: lint_rule4_double_missing_real_annotation — field 'CycleAnalyser.previousSteam' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: field 'CycleAnalyser.previousSteam' is a double without @RoboChartType("real")
Location: /Users/ranwei/Gitee/forge-v3c16-run7-steam_boiler/java.generated.project/src/main/java/steam_boiler/sensor/CycleAnalyser.java:58
```

**Java trace**
  - Java: `CycleAnalyser.java`:58-58

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 10: lint_rule4_double_missing_real_annotation — field 'CycleAnalyser.levelLow' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: field 'CycleAnalyser.levelLow' is a double without @RoboChartType("real")
Location: /Users/ranwei/Gitee/forge-v3c16-run7-steam_boiler/java.generated.project/src/main/java/steam_boiler/sensor/CycleAnalyser.java:59
```

**Java trace**
  - Java: `CycleAnalyser.java`:59-59

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 11: lint_rule4_double_missing_real_annotation — field 'CycleAnalyser.levelHigh' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: field 'CycleAnalyser.levelHigh' is a double without @RoboChartType("real")
Location: /Users/ranwei/Gitee/forge-v3c16-run7-steam_boiler/java.generated.project/src/main/java/steam_boiler/sensor/CycleAnalyser.java:60
```

**Java trace**
  - Java: `CycleAnalyser.java`:60-60

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

## Files to review
(none identified)

## Next step
Preflight found structural rule violations (errors). Fix them before continuing — downstream phases would either crash or produce a silently-wrong formal model.
