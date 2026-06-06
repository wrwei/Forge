# 2c — Preflight (Structural Lint) — FAILED

## Summary
2c — Preflight (Structural Lint): 21 structural error(s) found. Downstream phases would produce a wrong formal model.

## Run history
- New this run: 0
- Recurring from previous run: 21
- Resolved since previous run: 0

## Issues
### Issue 1: lint_rule4_double_missing_real_annotation — field 'Actuator.lastAdvVel' is a double without @RoboChartType("real") [recurring x2]

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: field 'Actuator.lastAdvVel' is a double without @RoboChartType("real")
Location: C:/Users/Will/Gitee/fmgvc-lre-run5-redo/java.generated.project/src/main/java/lre/actuator/Actuator.java:11
```

**Java trace**
  - Java: `Actuator.java`:11-11

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 2: lint_rule4_double_missing_real_annotation — field 'Actuator.lastAdvHdng' is a double without @RoboChartType("real") [recurring x2]

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: field 'Actuator.lastAdvHdng' is a double without @RoboChartType("real")
Location: C:/Users/Will/Gitee/fmgvc-lre-run5-redo/java.generated.project/src/main/java/lre/actuator/Actuator.java:12
```

**Java trace**
  - Java: `Actuator.java`:12-12

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 3: lint_rule4_double_missing_real_annotation — field 'LreConstants.minSafeDist' is a double without @RoboChartType("real") [recurring x2]

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: field 'LreConstants.minSafeDist' is a double without @RoboChartType("real")
Location: C:/Users/Will/Gitee/fmgvc-lre-run5-redo/java.generated.project/src/main/java/lre/constants/LreConstants.java:9
```

**Java trace**
  - Java: `LreConstants.java`:9-9

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 4: lint_rule4_double_missing_real_annotation — field 'LreConstants.staticObsHorizDist' is a double without @RoboChartType("real") [recurring x2]

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: field 'LreConstants.staticObsHorizDist' is a double without @RoboChartType("real")
Location: C:/Users/Will/Gitee/fmgvc-lre-run5-redo/java.generated.project/src/main/java/lre/constants/LreConstants.java:10
```

**Java trace**
  - Java: `LreConstants.java`:10-10

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 5: lint_rule4_double_missing_real_annotation — field 'LreConstants.staticObsVertDist' is a double without @RoboChartType("real") [recurring x2]

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: field 'LreConstants.staticObsVertDist' is a double without @RoboChartType("real")
Location: C:/Users/Will/Gitee/fmgvc-lre-run5-redo/java.generated.project/src/main/java/lre/constants/LreConstants.java:11
```

**Java trace**
  - Java: `LreConstants.java`:11-11

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 6: lint_rule4_double_missing_real_annotation — field 'LreConstants.staticObsDfltVertDist' is a double without @RoboChartType("real") [recurring x2]

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: field 'LreConstants.staticObsDfltVertDist' is a double without @RoboChartType("real")
Location: C:/Users/Will/Gitee/fmgvc-lre-run5-redo/java.generated.project/src/main/java/lre/constants/LreConstants.java:12
```

**Java trace**
  - Java: `LreConstants.java`:12-12

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 7: lint_rule4_double_missing_real_annotation — field 'Obstacle.nsRelDist' is a double without @RoboChartType("real") [recurring x2]

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: field 'Obstacle.nsRelDist' is a double without @RoboChartType("real")
Location: C:/Users/Will/Gitee/fmgvc-lre-run5-redo/java.generated.project/src/main/java/lre/datamodel/Obstacle.java:15
```

**Java trace**
  - Java: `Obstacle.java`:15-15

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 8: lint_rule4_double_missing_real_annotation — field 'Obstacle.ewRelDist' is a double without @RoboChartType("real") [recurring x2]

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: field 'Obstacle.ewRelDist' is a double without @RoboChartType("real")
Location: C:/Users/Will/Gitee/fmgvc-lre-run5-redo/java.generated.project/src/main/java/lre/datamodel/Obstacle.java:16
```

**Java trace**
  - Java: `Obstacle.java`:16-16

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 9: lint_rule4_double_missing_real_annotation — field 'Obstacle.obsDepth' is a double without @RoboChartType("real") [recurring x2]

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: field 'Obstacle.obsDepth' is a double without @RoboChartType("real")
Location: C:/Users/Will/Gitee/fmgvc-lre-run5-redo/java.generated.project/src/main/java/lre/datamodel/Obstacle.java:17
```

**Java trace**
  - Java: `Obstacle.java`:17-17

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 10: lint_rule4_double_missing_real_annotation — field 'Obstacle.obsNsVel' is a double without @RoboChartType("real") [recurring x2]

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: field 'Obstacle.obsNsVel' is a double without @RoboChartType("real")
Location: C:/Users/Will/Gitee/fmgvc-lre-run5-redo/java.generated.project/src/main/java/lre/datamodel/Obstacle.java:18
```

**Java trace**
  - Java: `Obstacle.java`:18-18

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 11: lint_rule4_double_missing_real_annotation — field 'Obstacle.obsEwVel' is a double without @RoboChartType("real") [recurring x2]

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: field 'Obstacle.obsEwVel' is a double without @RoboChartType("real")
Location: C:/Users/Will/Gitee/fmgvc-lre-run5-redo/java.generated.project/src/main/java/lre/datamodel/Obstacle.java:19
```

**Java trace**
  - Java: `Obstacle.java`:19-19

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 12: lint_rule4_double_missing_real_annotation — field 'Obstacle.obsRoc' is a double without @RoboChartType("real") [recurring x2]

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: field 'Obstacle.obsRoc' is a double without @RoboChartType("real")
Location: C:/Users/Will/Gitee/fmgvc-lre-run5-redo/java.generated.project/src/main/java/lre/datamodel/Obstacle.java:20
```

**Java trace**
  - Java: `Obstacle.java`:20-20

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 13: lint_rule4_double_missing_real_annotation — parameter 'Sensor.updateVehicle#depth' is a double without @RoboChartType("real") [recurring x2]

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: parameter 'Sensor.updateVehicle#depth' is a double without @RoboChartType("real")
Location: C:/Users/Will/Gitee/fmgvc-lre-run5-redo/java.generated.project/src/main/java/lre/sensor/Sensor.java:29
```

**Java trace**
  - Java: `Sensor.java`:29-29

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 14: lint_rule4_double_missing_real_annotation — parameter 'Sensor.updateVehicle#nsVel' is a double without @RoboChartType("real") [recurring x2]

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: parameter 'Sensor.updateVehicle#nsVel' is a double without @RoboChartType("real")
Location: C:/Users/Will/Gitee/fmgvc-lre-run5-redo/java.generated.project/src/main/java/lre/sensor/Sensor.java:29
```

**Java trace**
  - Java: `Sensor.java`:29-29

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 15: lint_rule4_double_missing_real_annotation — parameter 'Sensor.updateVehicle#ewVel' is a double without @RoboChartType("real") [recurring x2]

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: parameter 'Sensor.updateVehicle#ewVel' is a double without @RoboChartType("real")
Location: C:/Users/Will/Gitee/fmgvc-lre-run5-redo/java.generated.project/src/main/java/lre/sensor/Sensor.java:29
```

**Java trace**
  - Java: `Sensor.java`:29-29

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 16: lint_rule4_double_missing_real_annotation — parameter 'Sensor.updateVehicle#rateOfClimb' is a double without @RoboChartType("real") [recurring x2]

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: parameter 'Sensor.updateVehicle#rateOfClimb' is a double without @RoboChartType("real")
Location: C:/Users/Will/Gitee/fmgvc-lre-run5-redo/java.generated.project/src/main/java/lre/sensor/Sensor.java:29
```

**Java trace**
  - Java: `Sensor.java`:29-29

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 17: lint_rule4_double_missing_real_annotation — field 'Sensor.SAFE_LARGE_DISTANCE' is a double without @RoboChartType("real") [recurring x2]

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: field 'Sensor.SAFE_LARGE_DISTANCE' is a double without @RoboChartType("real")
Location: C:/Users/Will/Gitee/fmgvc-lre-run5-redo/java.generated.project/src/main/java/lre/sensor/Sensor.java:20
```

**Java trace**
  - Java: `Sensor.java`:20-20

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 18: lint_rule4_double_missing_real_annotation — field 'Sensor.depth' is a double without @RoboChartType("real") [recurring x2]

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: field 'Sensor.depth' is a double without @RoboChartType("real")
Location: C:/Users/Will/Gitee/fmgvc-lre-run5-redo/java.generated.project/src/main/java/lre/sensor/Sensor.java:22
```

**Java trace**
  - Java: `Sensor.java`:22-22

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 19: lint_rule4_double_missing_real_annotation — field 'Sensor.nsVel' is a double without @RoboChartType("real") [recurring x2]

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: field 'Sensor.nsVel' is a double without @RoboChartType("real")
Location: C:/Users/Will/Gitee/fmgvc-lre-run5-redo/java.generated.project/src/main/java/lre/sensor/Sensor.java:23
```

**Java trace**
  - Java: `Sensor.java`:23-23

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 20: lint_rule4_double_missing_real_annotation — field 'Sensor.ewVel' is a double without @RoboChartType("real") [recurring x2]

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: field 'Sensor.ewVel' is a double without @RoboChartType("real")
Location: C:/Users/Will/Gitee/fmgvc-lre-run5-redo/java.generated.project/src/main/java/lre/sensor/Sensor.java:24
```

**Java trace**
  - Java: `Sensor.java`:24-24

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 21: lint_rule4_double_missing_real_annotation — field 'Sensor.rateOfClimb' is a double without @RoboChartType("real") [recurring x2]

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: field 'Sensor.rateOfClimb' is a double without @RoboChartType("real")
Location: C:/Users/Will/Gitee/fmgvc-lre-run5-redo/java.generated.project/src/main/java/lre/sensor/Sensor.java:25
```

**Java trace**
  - Java: `Sensor.java`:25-25

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
