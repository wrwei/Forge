# 2c — Preflight (Structural Lint) — FAILED

## Summary
2c — Preflight (Structural Lint): 5 structural error(s) found. Downstream phases would produce a wrong formal model.

## Run history
- New this run: 5
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: lint_rule4_double_missing_real_annotation — parameter 'BoilerSensor.clamp#litres' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: parameter 'BoilerSensor.clamp#litres' is a double without @RoboChartType("real")
Location: /Users/ranwei/Gitee/forge-v3c16-run5-steam_boiler/java.generated.project/src/main/java/steam_boiler/sensor/BoilerSensor.java:477
```

**Java trace**
  - Java: `BoilerSensor.java`:477-477

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 2: lint_rule4_double_missing_real_annotation — parameter 'BoilerSensor.detectLevelFailure#expectedLow' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: parameter 'BoilerSensor.detectLevelFailure#expectedLow' is a double without @RoboChartType("real")
Location: /Users/ranwei/Gitee/forge-v3c16-run5-steam_boiler/java.generated.project/src/main/java/steam_boiler/sensor/BoilerSensor.java:397
```

**Java trace**
  - Java: `BoilerSensor.java`:397-397

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 3: lint_rule4_double_missing_real_annotation — parameter 'BoilerSensor.detectLevelFailure#expectedHigh' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: parameter 'BoilerSensor.detectLevelFailure#expectedHigh' is a double without @RoboChartType("real")
Location: /Users/ranwei/Gitee/forge-v3c16-run5-steam_boiler/java.generated.project/src/main/java/steam_boiler/sensor/BoilerSensor.java:397
```

**Java trace**
  - Java: `BoilerSensor.java`:397-397

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 4: lint_rule4_double_missing_real_annotation — parameter 'BoilerSensor.updateLevelKnowledge#expectedLow' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: parameter 'BoilerSensor.updateLevelKnowledge#expectedLow' is a double without @RoboChartType("real")
Location: /Users/ranwei/Gitee/forge-v3c16-run5-steam_boiler/java.generated.project/src/main/java/steam_boiler/sensor/BoilerSensor.java:413
```

**Java trace**
  - Java: `BoilerSensor.java`:413-413

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 5: lint_rule4_double_missing_real_annotation — parameter 'BoilerSensor.updateLevelKnowledge#expectedHigh' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: parameter 'BoilerSensor.updateLevelKnowledge#expectedHigh' is a double without @RoboChartType("real")
Location: /Users/ranwei/Gitee/forge-v3c16-run5-steam_boiler/java.generated.project/src/main/java/steam_boiler/sensor/BoilerSensor.java:413
```

**Java trace**
  - Java: `BoilerSensor.java`:413-413

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

## Files to review
(none identified)

## Next step
Preflight found structural rule violations (errors). Fix them before continuing — downstream phases would either crash or produce a silently-wrong formal model.
