# 6b — Formal Verification (FDR4) — FAILED

## Summary
FDR4 killed: memory usage exceeded 16384 MB limit (peak: 16750 MB)

## Run history
- New this run: 1
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: fdr4_memory_kill — FDR4 memory kill — verdict unmeasured

**Raw**
```
FDR4 killed: memory usage exceeded 16384 MB limit (peak: 16750 MB)
```

**Fix directive**
The model's state space exceeded the machine's memory, so FDR4 gave no verdict. The memory budget is already at the machine's limit and cannot be raised. This is not a parse error or a template defect. You may reduce the controller's state space in the Java (fewer transitions, fewer or narrower state variables, merged conditions), but only without changing the behaviour the requirements specify; otherwise record the FDR4 assertions as unmeasured.

## Files to review
- SynController.java
- SynMode.java

## Next step
Read each issue above, follow the fix directive, edit the linked Java file, and re-run Phase 6a (FDR4 Verification).
