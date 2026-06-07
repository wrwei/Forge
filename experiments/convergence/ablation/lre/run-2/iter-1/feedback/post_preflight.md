# 2c — Preflight (Structural Lint) — FAILED

## Summary
2c — Preflight (Structural Lint): 5 structural error(s) found. Downstream phases would produce a wrong formal model.

## Run history
- New this run: 5
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: lint_rule4_double_missing_real_annotation — parameter 'Sensor.update#depth' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: parameter 'Sensor.update#depth' is a double without @RoboChartType("real")
Location: C:/Users/willr/gitee/fmgvc-abl-lre2/java.generated.project/src/main/java/lre/sensor/Sensor.java:31
```

**Java trace**
  - Java: `Sensor.java`:31-31

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 2: lint_rule4_double_missing_real_annotation — parameter 'Sensor.update#nsVel' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: parameter 'Sensor.update#nsVel' is a double without @RoboChartType("real")
Location: C:/Users/willr/gitee/fmgvc-abl-lre2/java.generated.project/src/main/java/lre/sensor/Sensor.java:31
```

**Java trace**
  - Java: `Sensor.java`:31-31

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 3: lint_rule4_double_missing_real_annotation — parameter 'Sensor.update#ewVel' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: parameter 'Sensor.update#ewVel' is a double without @RoboChartType("real")
Location: C:/Users/willr/gitee/fmgvc-abl-lre2/java.generated.project/src/main/java/lre/sensor/Sensor.java:31
```

**Java trace**
  - Java: `Sensor.java`:31-31

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 4: lint_rule4_double_missing_real_annotation — parameter 'Sensor.update#rateOfClimb' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: parameter 'Sensor.update#rateOfClimb' is a double without @RoboChartType("real")
Location: C:/Users/willr/gitee/fmgvc-abl-lre2/java.generated.project/src/main/java/lre/sensor/Sensor.java:32
```

**Java trace**
  - Java: `Sensor.java`:32-32

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 5: lint_rule4_double_missing_real_annotation — field 'Sensor.NO_OBSTACLE_DISTANCE' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: field 'Sensor.NO_OBSTACLE_DISTANCE' is a double without @RoboChartType("real")
Location: C:/Users/willr/gitee/fmgvc-abl-lre2/java.generated.project/src/main/java/lre/sensor/Sensor.java:15
```

**Java trace**
  - Java: `Sensor.java`:15-15

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

## Files to review
(none identified)

## Next step
Preflight found structural rule violations (errors). Fix them before continuing — downstream phases would either crash or produce a silently-wrong formal model.
