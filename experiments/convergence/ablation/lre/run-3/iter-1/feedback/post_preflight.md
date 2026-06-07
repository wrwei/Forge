# 2c — Preflight (Structural Lint) — FAILED

## Summary
2c — Preflight (Structural Lint): 9 structural error(s) found. Downstream phases would produce a wrong formal model.

## Run history
- New this run: 9
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: lint_rule4_double_missing_real_annotation — field 'LreConstants.MIN_SAFE_DIST' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: field 'LreConstants.MIN_SAFE_DIST' is a double without @RoboChartType("real")
Location: C:/Users/willr/gitee/fmgvc-abl-lre3/java.generated.project/src/main/java/lre/constants/LreConstants.java:9
```

**Java trace**
  - Java: `LreConstants.java`:9-9

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 2: lint_rule4_double_missing_real_annotation — field 'LreConstants.STATIC_OBS_HORIZ_DIST' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: field 'LreConstants.STATIC_OBS_HORIZ_DIST' is a double without @RoboChartType("real")
Location: C:/Users/willr/gitee/fmgvc-abl-lre3/java.generated.project/src/main/java/lre/constants/LreConstants.java:12
```

**Java trace**
  - Java: `LreConstants.java`:12-12

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 3: lint_rule4_double_missing_real_annotation — field 'LreConstants.STATIC_OBS_VERT_DIST' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: field 'LreConstants.STATIC_OBS_VERT_DIST' is a double without @RoboChartType("real")
Location: C:/Users/willr/gitee/fmgvc-abl-lre3/java.generated.project/src/main/java/lre/constants/LreConstants.java:15
```

**Java trace**
  - Java: `LreConstants.java`:15-15

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 4: lint_rule4_double_missing_real_annotation — field 'LreConstants.STATIC_OBS_DFLT_VERT_DIST' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: field 'LreConstants.STATIC_OBS_DFLT_VERT_DIST' is a double without @RoboChartType("real")
Location: C:/Users/willr/gitee/fmgvc-abl-lre3/java.generated.project/src/main/java/lre/constants/LreConstants.java:18
```

**Java trace**
  - Java: `LreConstants.java`:18-18

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 5: lint_rule4_double_missing_real_annotation — parameter 'Sensor.update#depth' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: parameter 'Sensor.update#depth' is a double without @RoboChartType("real")
Location: C:/Users/willr/gitee/fmgvc-abl-lre3/java.generated.project/src/main/java/lre/sensor/Sensor.java:26
```

**Java trace**
  - Java: `Sensor.java`:26-26

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 6: lint_rule4_double_missing_real_annotation — parameter 'Sensor.update#nsVel' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: parameter 'Sensor.update#nsVel' is a double without @RoboChartType("real")
Location: C:/Users/willr/gitee/fmgvc-abl-lre3/java.generated.project/src/main/java/lre/sensor/Sensor.java:26
```

**Java trace**
  - Java: `Sensor.java`:26-26

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 7: lint_rule4_double_missing_real_annotation — parameter 'Sensor.update#ewVel' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: parameter 'Sensor.update#ewVel' is a double without @RoboChartType("real")
Location: C:/Users/willr/gitee/fmgvc-abl-lre3/java.generated.project/src/main/java/lre/sensor/Sensor.java:26
```

**Java trace**
  - Java: `Sensor.java`:26-26

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 8: lint_rule4_double_missing_real_annotation — parameter 'Sensor.update#rateOfClimb' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: parameter 'Sensor.update#rateOfClimb' is a double without @RoboChartType("real")
Location: C:/Users/willr/gitee/fmgvc-abl-lre3/java.generated.project/src/main/java/lre/sensor/Sensor.java:27
```

**Java trace**
  - Java: `Sensor.java`:27-27

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 9: lint_rule4_double_missing_real_annotation — field 'Sensor.SAFE_DISTANCE' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: field 'Sensor.SAFE_DISTANCE' is a double without @RoboChartType("real")
Location: C:/Users/willr/gitee/fmgvc-abl-lre3/java.generated.project/src/main/java/lre/sensor/Sensor.java:13
```

**Java trace**
  - Java: `Sensor.java`:13-13

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

## Files to review
(none identified)

## Next step
Preflight found structural rule violations (errors). Fix them before continuing — downstream phases would either crash or produce a silently-wrong formal model.
