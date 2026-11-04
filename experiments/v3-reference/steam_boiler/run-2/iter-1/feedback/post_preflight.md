# 2c — Preflight (Structural Lint) — FAILED

## Summary
2c — Preflight (Structural Lint): 1 structural error(s) found. Downstream phases would produce a wrong formal model.

## Run history
- New this run: 1
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: lint_rule4_double_missing_real_annotation — parameter 'UnitMonitor.clamp#litres' is a double without @RoboChartType("real")

**Raw**
```
Rule: rule4_double_missing_real_annotation
Severity: error
Message: parameter 'UnitMonitor.clamp#litres' is a double without @RoboChartType("real")
Location: /Users/ranwei/Gitee/forge-v3r16-run2-steam_boiler/java.generated.project/src/main/java/steam_boiler/monitor/UnitMonitor.java:599
```

**Java trace**
  - Java: `UnitMonitor.java`:599-599

**Fix directive**
Annotate with @RoboChartType("real"). Without the annotation, the formal model extraction cannot confidently map Java double to RoboChart real and may produce an incorrect model type. See java_codegen_rules.txt.

## Files to review
(none identified)

## Next step
Preflight found structural rule violations (errors). Fix them before continuing — downstream phases would either crash or produce a silently-wrong formal model.
