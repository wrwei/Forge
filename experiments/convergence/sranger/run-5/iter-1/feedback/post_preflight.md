# 2c — Preflight (Structural Lint) — FAILED

## Summary
2c — Preflight (Structural Lint): 9 structural error(s) found. Downstream phases would produce a wrong formal model.

## Run history
- New this run: 9
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: lint_rule4_double_missing_real_annotation — parameter 'Actuator.move#lv' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: parameter 'Actuator.move#lv' is a double without @RoboChartType("real")
Location: C:/Users/Will/Gitee/fmgvc-sranger-run8/java.generated.project/src/main/java/sranger/actuator/Actuator.java:21
```

**Java trace**
  - Java: `Actuator.java`:21-21

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 2: lint_rule4_double_missing_real_annotation — parameter 'Actuator.move#av' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: parameter 'Actuator.move#av' is a double without @RoboChartType("real")
Location: C:/Users/Will/Gitee/fmgvc-sranger-run8/java.generated.project/src/main/java/sranger/actuator/Actuator.java:21
```

**Java trace**
  - Java: `Actuator.java`:21-21

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 3: lint_rule4_double_missing_real_annotation — field 'SRangerConstants.MOVE_VEL' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: field 'SRangerConstants.MOVE_VEL' is a double without @RoboChartType("real")
Location: C:/Users/Will/Gitee/fmgvc-sranger-run8/java.generated.project/src/main/java/sranger/constants/SRangerConstants.java:9
```

**Java trace**
  - Java: `SRangerConstants.java`:9-9

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 4: lint_rule4_double_missing_real_annotation — field 'SRangerConstants.TURN_VEL' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: field 'SRangerConstants.TURN_VEL' is a double without @RoboChartType("real")
Location: C:/Users/Will/Gitee/fmgvc-sranger-run8/java.generated.project/src/main/java/sranger/constants/SRangerConstants.java:12
```

**Java trace**
  - Java: `SRangerConstants.java`:12-12

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 5: lint_rule4_double_missing_real_annotation — field 'SRangerConstants.OBSTACLE_THRESHOLD' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: field 'SRangerConstants.OBSTACLE_THRESHOLD' is a double without @RoboChartType("real")
Location: C:/Users/Will/Gitee/fmgvc-sranger-run8/java.generated.project/src/main/java/sranger/constants/SRangerConstants.java:15
```

**Java trace**
  - Java: `SRangerConstants.java`:15-15

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 6: lint_rule4_double_missing_real_annotation — field 'SRangerConstants.TURN_DURATION' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: field 'SRangerConstants.TURN_DURATION' is a double without @RoboChartType("real")
Location: C:/Users/Will/Gitee/fmgvc-sranger-run8/java.generated.project/src/main/java/sranger/constants/SRangerConstants.java:18
```

**Java trace**
  - Java: `SRangerConstants.java`:18-18

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 7: lint_rule4_double_missing_real_annotation — parameter 'Clock.setTime#t' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: parameter 'Clock.setTime#t' is a double without @RoboChartType("real")
Location: C:/Users/Will/Gitee/fmgvc-sranger-run8/java.generated.project/src/main/java/sranger/sensor/Clock.java:21
```

**Java trace**
  - Java: `Clock.java`:21-21

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 8: lint_rule4_double_missing_real_annotation — parameter 'Sensor.update#reading' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: parameter 'Sensor.update#reading' is a double without @RoboChartType("real")
Location: C:/Users/Will/Gitee/fmgvc-sranger-run8/java.generated.project/src/main/java/sranger/sensor/Sensor.java:22
```

**Java trace**
  - Java: `Sensor.java`:22-22

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 9: lint_rule4_double_missing_real_annotation — field 'Sensor.NO_READING_DEFAULT' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: field 'Sensor.NO_READING_DEFAULT' is a double without @RoboChartType("real")
Location: C:/Users/Will/Gitee/fmgvc-sranger-run8/java.generated.project/src/main/java/sranger/sensor/Sensor.java:11
```

**Java trace**
  - Java: `Sensor.java`:11-11

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

## Files to review
(none identified)

## Next step
Preflight found structural rule violations (errors). Fix them before continuing — downstream phases would either crash or produce a silently-wrong formal model.
