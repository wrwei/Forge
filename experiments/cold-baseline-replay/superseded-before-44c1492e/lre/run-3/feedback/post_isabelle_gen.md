# 5c — Isabelle Theory Generation — FAILED

## Summary
5c — Isabelle Theory Generation failed.

## Run history
- New this run: 1
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: isabelle_gen_failure — isabelle_gen failed

**Raw**
```
5c — Isabelle Theory Generation failed with exit code 1.
> Task :run FAILED
Exception in thread "main" java.io.IOException: robochart_model.xmi not found at /Users/ranwei/Git/Forge/forge.transformations/output/robochart_model.xmi. Run 'transform' first.
	at forge.transformations.m2t.IsabellePhase.loadRoboChartModel(IsabellePhase.java:70)
	at forge.transformations.m2t.IsabellePhase.run(IsabellePhase.java:50)
	at forge.transformations.core.App.main(App.java:68)
FAILURE: Build failed with an exception.
* What went wrong:
Execution failed for task ':run'.
* Try:
BUILD FAILED in 416ms
3 actionable tasks: 1 executed, 2 up-to-date
```

**Fix directive**
isabelle_gen raised an error. Inspect the raw message above and correlate with the Java code. If the message references a Spoon/EMF construct, the Java source may use a feature banned by java_codegen_rules.txt.

## Files to review
(none identified)

## Next step
Read the issue above, follow the fix directive, edit the Java source, and re-run this phase.
