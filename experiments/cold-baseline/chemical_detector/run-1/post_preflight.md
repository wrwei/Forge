# 2c — Preflight (Structural Lint) — FAILED

## Summary
2c — Preflight (Structural Lint) failed.

## Run history
- New this run: 1
- Recurring from previous run: 0
- Resolved since previous run: 1

**Resolved issue titles**
- preflight failed

## Issues
### Issue 1: preflight_failure — preflight failed

**Raw**
```
2c — Preflight (Structural Lint) failed with exit code 1.
> Task :run FAILED
Java HotSpot(TM) 64-Bit Server VM warning: #
# There is insufficient memory for the Java Runtime Environment to continue.
# Native memory allocation (mmap) failed to map 1073741824 bytes for G1 virtual space
INFO: os::commit_memory(0x0000000403800000, 1073741824, 0) failed; error='The paging file is too small for this operation to complete' (DOS error/errno=1455)
# An error report file with more information is saved as:
# C:\Users\willr\gitee\formal_method_guided_vibe_coding\t2m.transformation.java\hs_err_pid90668.log
FAILURE: Build failed with an exception.
* What went wrong:
Execution failed for task ':run'.
* Try:
BUILD FAILED in 2s
3 actionable tasks: 1 executed, 2 up-to-date
```

**Fix directive**
preflight raised an error. Inspect the raw message above and correlate with the Java code. If the message references a Spoon/EMF construct, the Java source may use a feature banned by java_codegen_rules.txt.

## Files to review
(none identified)

## Next step
Preflight found only warnings. You may continue to Phase 3 (T2M), but address the warnings at the next convenient point.
