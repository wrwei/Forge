# 2c — Preflight (Structural Lint) — FAILED

## Summary
2c — Preflight (Structural Lint): 5 structural error(s) found. Downstream phases would produce a wrong formal model.

## Run history
- New this run: 0
- Recurring from previous run: 5
- Resolved since previous run: 0

## Issues
### Issue 1: lint_rule4_double_missing_real_annotation — parameter 'Sensor.updateEnvironment#depth' is a double without @RoboChartType("real") [recurring x2]

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: parameter 'Sensor.updateEnvironment#depth' is a double without @RoboChartType("real")
Location: C:/Users/Will/Gitee/fmgvc-lre-run4-redo/java.generated.project/src/main/java/lre/sensor/Sensor.java:27
```

**Java trace**
  - Java: `Sensor.java`:27-27

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 2: lint_rule4_double_missing_real_annotation — parameter 'Sensor.updateEnvironment#nsVel' is a double without @RoboChartType("real") [recurring x2]

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: parameter 'Sensor.updateEnvironment#nsVel' is a double without @RoboChartType("real")
Location: C:/Users/Will/Gitee/fmgvc-lre-run4-redo/java.generated.project/src/main/java/lre/sensor/Sensor.java:27
```

**Java trace**
  - Java: `Sensor.java`:27-27

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 3: lint_rule4_double_missing_real_annotation — parameter 'Sensor.updateEnvironment#ewVel' is a double without @RoboChartType("real") [recurring x2]

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: parameter 'Sensor.updateEnvironment#ewVel' is a double without @RoboChartType("real")
Location: C:/Users/Will/Gitee/fmgvc-lre-run4-redo/java.generated.project/src/main/java/lre/sensor/Sensor.java:27
```

**Java trace**
  - Java: `Sensor.java`:27-27

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 4: lint_rule4_double_missing_real_annotation — parameter 'Sensor.updateEnvironment#rateOfClimb' is a double without @RoboChartType("real") [recurring x2]

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: parameter 'Sensor.updateEnvironment#rateOfClimb' is a double without @RoboChartType("real")
Location: C:/Users/Will/Gitee/fmgvc-lre-run4-redo/java.generated.project/src/main/java/lre/sensor/Sensor.java:27
```

**Java trace**
  - Java: `Sensor.java`:27-27

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 5: lint_rule4_double_missing_real_annotation — field 'Sensor.SAFE_LARGE_DISTANCE' is a double without @RoboChartType("real") [recurring x2]

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: field 'Sensor.SAFE_LARGE_DISTANCE' is a double without @RoboChartType("real")
Location: C:/Users/Will/Gitee/fmgvc-lre-run4-redo/java.generated.project/src/main/java/lre/sensor/Sensor.java:14
```

**Java trace**
  - Java: `Sensor.java`:14-14

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

## Files to review
- CalcCDyn.java
- CalcCPA.java
- CalcCStc.java
- CalcVel.java
- CheckOPEZ.java
- LreController.java
- LreMode.java

## Next step
Preflight found structural rule violations (errors). Fix them before continuing — downstream phases would either crash or produce a silently-wrong formal model.
