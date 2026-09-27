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
5b — CSP Generation (RoboChart) reported completion but produced no files matching: file_*.csp, instantiations.csp, timed/file_*.csp, timed/instantiations.csp. Check whether the gradle task silently no-op'd (e.g. RoboChart CSP generator errors with ignoreExitValue=true).
```

**Fix directive**
EGL / RCT / RoboChart CSP generator raised an error. Inspect the raw message above and correlate with the Java code. If the message references a Spoon/EMF construct, the Java source may use a feature banned by java_codegen_rules.txt.

## Files to review
(none identified)

## Next step
Read the issue above, follow the fix directive, edit the Java source, and re-run this phase.
