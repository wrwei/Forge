# 5a — CSP Generation (RoboChart) — FAILED

## Summary
5a — CSP Generation (RoboChart) failed.

## Run history
- New this run: 1
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: java_compile_error — Java compilation failed

**Raw**
```
5a — CSP Generation (RoboChart) reported errors to stdout while still exiting 0 (silent failure — gradle task ignored its own exit value).
ERROR:Couldn't resolve reference to NamedExpression 'nsRelDist'. (file:/C:/Users/willr/gitee/formal_method_guided_vibe_coding/t2m.transformation.java/output/robochart_controller.rct line : 81 column : 197)
ERROR:Couldn't resolve reference to NamedExpression 'ewRelDist'. (file:/C:/Users/willr/gitee/formal_method_guided_vibe_coding/t2m.transformation.java/output/robochart_controller.rct line : 81 column : 247)
BUILD SUCCESSFUL in 2s
1 actionable task: 1 executed
```

**Fix directive**
Fix the Java compile error. The pipeline cannot extract a model from code that does not compile.

## Files to review
(none identified)

## Next step
Read the issue above, follow the fix directive, edit the Java source, and re-run this phase.
