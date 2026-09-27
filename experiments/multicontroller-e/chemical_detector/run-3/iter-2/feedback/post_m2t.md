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
ERROR:Couldn't resolve reference to NamedExpression 'makingProgress'. (file:/Users/ranwei/Gitee/forge-mce-run3-chemical_detector/forge.transformations/output/robochart_controller.rct line : 261 column : 13)
ERROR:Couldn't resolve reference to NamedExpression 'makingProgress'. (file:/Users/ranwei/Gitee/forge-mce-run3-chemical_detector/forge.transformations/output/robochart_controller.rct line : 266 column : 19)
Loaded: lib/robochart/core.rct
Loaded: lib/robochart/function_toolkit.rct
Loaded: lib/robochart/sequence_toolkit.rct
Loaded: lib/robochart/set_toolkit.rct
Loaded: /Users/ranwei/Gitee/forge-mce-run3-chemical_detector/forge.transformations/output/robochart_controller.rct
BUILD SUCCESSFUL in 4s
1 actionable task: 1 executed
```

**Fix directive**
Fix the Java compile error. The pipeline cannot extract a model from code that does not compile.

## Files to review
(none identified)

## Next step
Read the issue above, follow the fix directive, edit the Java source, and re-run this phase.
