# 4 — Model-to-Model (ETL Transformation) — FAILED

## Summary
4 — Model-to-Model (ETL Transformation) failed.

## Run history
- New this run: 1
- Recurring from previous run: 0
- Resolved since previous run: 1

**Resolved issue titles**
- ETL (Java EMF → RoboChart) failed

## Issues
### Issue 1: m2m_failure — ETL (Java EMF → RoboChart) failed

**Raw**
```
4 — Model-to-Model (ETL Transformation) failed with exit code 1.
> Task :run FAILED
Java HotSpot(TM) 64-Bit Server VM warning: INFO: os::commit_memory(0x00000002d7800000, 1073741824, 0) failed; error='The paging file is too small for this operation to complete' (DOS error/errno=1455)
#
# There is insufficient memory for the Java Runtime Environment to continue.
# Native memory allocation (mmap) failed to map 1073741824 bytes for G1 virtual space
# An error report file with more information is saved as:
# C:\Users\willr\gitee\formal_method_guided_vibe_coding\t2m.transformation.java\hs_err_pid92536.log
FAILURE: Build failed with an exception.
* What went wrong:
Execution failed for task ':run'.
* Try:
BUILD FAILED in 2s
3 actionable tasks: 1 executed, 2 up-to-date
```

**Fix directive**
ETL (Java EMF → RoboChart) raised an error. Inspect the raw message above and correlate with the Java code. If the message references a Spoon/EMF construct, the Java source may use a feature banned by java_codegen_rules.txt.

## Files to review
(none identified)

## Next step
Read the issue above, follow the fix directive, edit the Java source, and re-run this phase.
