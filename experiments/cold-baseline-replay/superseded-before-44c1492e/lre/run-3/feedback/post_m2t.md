# 5b — CSP Generation (RoboChart) — FAILED

## Summary
5b — CSP Generation (RoboChart) failed.

## Run history
- New this run: 1
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: m2t_failure — EGL / RCT / RoboChart CSP generator failed

**Raw**
```
5b — CSP Generation (RoboChart) failed with exit code 1.
> Task :run FAILED
Exception in thread "main" java.io.IOException: robochart_model.xmi not found at /Users/ranwei/Git/Forge/forge.transformations/output/robochart_model.xmi. Run 'transform' first.
	at forge.transformations.m2t.RctPhase.loadRoboChartModel(RctPhase.java:112)
	at forge.transformations.m2t.RctPhase.run(RctPhase.java:42)
	at forge.transformations.core.App.main(App.java:68)
FAILURE: Build failed with an exception.
* What went wrong:
Execution failed for task ':run'.
* Try:
BUILD FAILED in 369ms
3 actionable tasks: 1 executed, 2 up-to-date
```

**Fix directive**
EGL / RCT / RoboChart CSP generator raised an error. Inspect the raw message above and correlate with the Java code. If the message references a Spoon/EMF construct, the Java source may use a feature banned by java_codegen_rules.txt.

## Files to review
(none identified)

## Next step
Read the issue above, follow the fix directive, edit the Java source, and re-run this phase.
