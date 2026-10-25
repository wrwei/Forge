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
ERROR:Couldn't resolve reference to Event 'odometer'. (file:/Users/ranwei/Git/Forge/forge.transformations/output/robochart_controller.rct line : 171 column : 25)
ERROR:Couldn't resolve reference to Event 'odometer'. (file:/Users/ranwei/Git/Forge/forge.transformations/output/robochart_controller.rct line : 171 column : 64)
BUILD SUCCESSFUL in 1s
1 actionable task: 1 executed
```

**Fix directive**
Fix the Java compile error. The pipeline cannot extract a model from code that does not compile.

## Files to review
(none identified)

## Next step
Read the issue above, follow the fix directive, edit the Java source, and re-run this phase.
