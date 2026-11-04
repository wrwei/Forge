# 5b — CSP Generation (RoboChart) — FAILED

## Summary
5b — CSP Generation (RoboChart) failed.

## Run history
- New this run: 1
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: java_compile_error — Java compilation failed

**Raw**
```
5b — CSP Generation (RoboChart) reported errors to stdout while still exiting 0 (silent failure — gradle task ignored its own exit value).
ERROR:mismatched input 'on' expecting '}' (file:/Users/ranwei/Gitee/forge-v3r10-run1-steam_boiler/forge.transformations/output/robochart_controller.rct line : 14 column : 2)
ERROR:mismatched input 'on' expecting RULE_ID (file:/Users/ranwei/Gitee/forge-v3r10-run1-steam_boiler/forge.transformations/output/robochart_controller.rct line : 30 column : 6)
ERROR:mismatched input 'var' expecting '(' (file:/Users/ranwei/Gitee/forge-v3r10-run1-steam_boiler/forge.transformations/output/robochart_controller.rct line : 31 column : 2)
ERROR:The required feature 'name' of 'circus.robocalc.robochart.impl.VariableImplCustom@37d28f02{file:/Users/ranwei/Gitee/forge-v3r10-run1-steam_boiler/forge.transformations/output/robochart_controller.rct#//@interfaces.2/@variableList.1/@vars.0}' must be set (file:/Users/ranwei/Gitee/forge-v3r10-run1-steam_boiler/forge.transformations/output/robochart_controller.rct line : 30 column : 6)
ERROR:The required feature 'type' of 'circus.robocalc.robochart.impl.VariableImplCustom@37d28f02{file:/Users/ranwei/Gitee/forge-v3r10-run1-steam_boiler/forge.transformations/output/robochart_controller.rct#//@interfaces.2/@variableList.1/@vars.0}' must be set (file:/Users/ranwei/Gitee/forge-v3r10-run1-steam_boiler/forge.transformations/output/robochart_controller.rct line : 30 column : 6)
BUILD SUCCESSFUL in 988ms
1 actionable task: 1 executed
```

**Fix directive**
Fix the Java compile error. The pipeline cannot extract a model from code that does not compile.

## Files to review
(none identified)

## Next step
Read the issue above, follow the fix directive, edit the Java source, and re-run this phase.
