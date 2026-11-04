# 2c — Preflight (Structural Lint) — FAILED

## Summary
2c — Preflight (Structural Lint): 11 structural error(s) found. Downstream phases would produce a wrong formal model.

## Run history
- New this run: 11
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: lint_rule4_double_missing_real_annotation — parameter 'BoilerSensor.levelAfterOneCycle#level' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: parameter 'BoilerSensor.levelAfterOneCycle#level' is a double without @RoboChartType("real")
Location: /Users/ranwei/Gitee/forge-v3r10-run1-steam_boiler/java.generated.project/src/main/java/steam_boiler/sensor/BoilerSensor.java:693
```

**Java trace**
  - Java: `BoilerSensor.java`:693-693

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 2: lint_rule4_double_missing_real_annotation — parameter 'BoilerSensor.levelAfterOneCycle#steam' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: parameter 'BoilerSensor.levelAfterOneCycle#steam' is a double without @RoboChartType("real")
Location: /Users/ranwei/Gitee/forge-v3r10-run1-steam_boiler/java.generated.project/src/main/java/steam_boiler/sensor/BoilerSensor.java:693
```

**Java trace**
  - Java: `BoilerSensor.java`:693-693

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 3: lint_rule4_double_missing_real_annotation — parameter 'BoilerSensor.levelAfterTwoCycles#level' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: parameter 'BoilerSensor.levelAfterTwoCycles#level' is a double without @RoboChartType("real")
Location: /Users/ranwei/Gitee/forge-v3r10-run1-steam_boiler/java.generated.project/src/main/java/steam_boiler/sensor/BoilerSensor.java:697
```

**Java trace**
  - Java: `BoilerSensor.java`:697-697

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 4: lint_rule4_double_missing_real_annotation — parameter 'BoilerSensor.levelAfterTwoCycles#steam' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: parameter 'BoilerSensor.levelAfterTwoCycles#steam' is a double without @RoboChartType("real")
Location: /Users/ranwei/Gitee/forge-v3r10-run1-steam_boiler/java.generated.project/src/main/java/steam_boiler/sensor/BoilerSensor.java:697
```

**Java trace**
  - Java: `BoilerSensor.java`:697-697

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 5: lint_rule4_double_missing_real_annotation — parameter 'BoilerSensor.maximalOutflow#start' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: parameter 'BoilerSensor.maximalOutflow#start' is a double without @RoboChartType("real")
Location: /Users/ranwei/Gitee/forge-v3r10-run1-steam_boiler/java.generated.project/src/main/java/steam_boiler/sensor/BoilerSensor.java:729
```

**Java trace**
  - Java: `BoilerSensor.java`:729-729

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 6: lint_rule4_double_missing_real_annotation — parameter 'BoilerSensor.maximalOutflow#end' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: parameter 'BoilerSensor.maximalOutflow#end' is a double without @RoboChartType("real")
Location: /Users/ranwei/Gitee/forge-v3r10-run1-steam_boiler/java.generated.project/src/main/java/steam_boiler/sensor/BoilerSensor.java:729
```

**Java trace**
  - Java: `BoilerSensor.java`:729-729

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 7: lint_rule4_double_missing_real_annotation — parameter 'BoilerSensor.maximalOutflowAhead#start' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: parameter 'BoilerSensor.maximalOutflowAhead#start' is a double without @RoboChartType("real")
Location: /Users/ranwei/Gitee/forge-v3r10-run1-steam_boiler/java.generated.project/src/main/java/steam_boiler/sensor/BoilerSensor.java:749
```

**Java trace**
  - Java: `BoilerSensor.java`:749-749

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 8: lint_rule4_double_missing_real_annotation — parameter 'BoilerSensor.minimalOutflow#start' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: parameter 'BoilerSensor.minimalOutflow#start' is a double without @RoboChartType("real")
Location: /Users/ranwei/Gitee/forge-v3r10-run1-steam_boiler/java.generated.project/src/main/java/steam_boiler/sensor/BoilerSensor.java:738
```

**Java trace**
  - Java: `BoilerSensor.java`:738-738

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 9: lint_rule4_double_missing_real_annotation — parameter 'BoilerSensor.minimalOutflow#end' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: parameter 'BoilerSensor.minimalOutflow#end' is a double without @RoboChartType("real")
Location: /Users/ranwei/Gitee/forge-v3r10-run1-steam_boiler/java.generated.project/src/main/java/steam_boiler/sensor/BoilerSensor.java:738
```

**Java trace**
  - Java: `BoilerSensor.java`:738-738

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 10: lint_rule4_double_missing_real_annotation — parameter 'BoilerSensor.minimalOutflowAhead#start' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: parameter 'BoilerSensor.minimalOutflowAhead#start' is a double without @RoboChartType("real")
Location: /Users/ranwei/Gitee/forge-v3r10-run1-steam_boiler/java.generated.project/src/main/java/steam_boiler/sensor/BoilerSensor.java:761
```

**Java trace**
  - Java: `BoilerSensor.java`:761-761

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 11: lint_rule4_double_missing_real_annotation — parameter 'BoilerSensor.withinNormal#level' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: parameter 'BoilerSensor.withinNormal#level' is a double without @RoboChartType("real")
Location: /Users/ranwei/Gitee/forge-v3r10-run1-steam_boiler/java.generated.project/src/main/java/steam_boiler/sensor/BoilerSensor.java:689
```

**Java trace**
  - Java: `BoilerSensor.java`:689-689

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

## Files to review
(none identified)

## Next step
Preflight found structural rule violations (errors). Fix them before continuing — downstream phases would either crash or produce a silently-wrong formal model.
