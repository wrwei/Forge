# 2c — Preflight (Structural Lint) — FAILED

## Summary
2c — Preflight (Structural Lint): 7 structural error(s) found. Downstream phases would produce a wrong formal model.

## Run history
- New this run: 7
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: lint_rule4_double_missing_real_annotation — parameter 'BoilerSensor.clampToCapacity#litres' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: parameter 'BoilerSensor.clampToCapacity#litres' is a double without @RoboChartType("real")
Location: /Users/ranwei/Gitee/forge-v3r10-run2-steam_boiler/java.generated.project/src/main/java/steam_boiler/sensor/BoilerSensor.java:401
```

**Java trace**
  - Java: `BoilerSensor.java`:401-401

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 2: lint_rule4_double_missing_real_annotation — parameter 'BoilerSensor.judgeLevel#reachedLower' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: parameter 'BoilerSensor.judgeLevel#reachedLower' is a double without @RoboChartType("real")
Location: /Users/ranwei/Gitee/forge-v3r10-run2-steam_boiler/java.generated.project/src/main/java/steam_boiler/sensor/BoilerSensor.java:337
```

**Java trace**
  - Java: `BoilerSensor.java`:337-337

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 3: lint_rule4_double_missing_real_annotation — parameter 'BoilerSensor.judgeLevel#reachedUpper' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: parameter 'BoilerSensor.judgeLevel#reachedUpper' is a double without @RoboChartType("real")
Location: /Users/ranwei/Gitee/forge-v3r10-run2-steam_boiler/java.generated.project/src/main/java/steam_boiler/sensor/BoilerSensor.java:337
```

**Java trace**
  - Java: `BoilerSensor.java`:337-337

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 4: lint_rule4_double_missing_real_annotation — parameter 'BoilerSensor.maximalSteamVolume#startRate' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: parameter 'BoilerSensor.maximalSteamVolume#startRate' is a double without @RoboChartType("real")
Location: /Users/ranwei/Gitee/forge-v3r10-run2-steam_boiler/java.generated.project/src/main/java/steam_boiler/sensor/BoilerSensor.java:361
```

**Java trace**
  - Java: `BoilerSensor.java`:361-361

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 5: lint_rule4_double_missing_real_annotation — parameter 'BoilerSensor.minimalSteamVolume#startRate' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: parameter 'BoilerSensor.minimalSteamVolume#startRate' is a double without @RoboChartType("real")
Location: /Users/ranwei/Gitee/forge-v3r10-run2-steam_boiler/java.generated.project/src/main/java/steam_boiler/sensor/BoilerSensor.java:368
```

**Java trace**
  - Java: `BoilerSensor.java`:368-368

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 6: lint_rule4_double_missing_real_annotation — parameter 'BoilerSensor.steamVolumeBetween#startRate' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: parameter 'BoilerSensor.steamVolumeBetween#startRate' is a double without @RoboChartType("real")
Location: /Users/ranwei/Gitee/forge-v3r10-run2-steam_boiler/java.generated.project/src/main/java/steam_boiler/sensor/BoilerSensor.java:380
```

**Java trace**
  - Java: `BoilerSensor.java`:380-380

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

### Issue 7: lint_rule4_double_missing_real_annotation — parameter 'BoilerSensor.steamVolumeBetween#endRate' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: parameter 'BoilerSensor.steamVolumeBetween#endRate' is a double without @RoboChartType("real")
Location: /Users/ranwei/Gitee/forge-v3r10-run2-steam_boiler/java.generated.project/src/main/java/steam_boiler/sensor/BoilerSensor.java:380
```

**Java trace**
  - Java: `BoilerSensor.java`:380-380

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

## Files to review
(none identified)

## Next step
Preflight found structural rule violations (errors). Fix them before continuing — downstream phases would either crash or produce a silently-wrong formal model.
