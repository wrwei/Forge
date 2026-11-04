# 6b — Formal Verification (FDR4) — FAILED

## Summary
FDR4 loaded the CSP and was exploring the state space when it was killed by a signal (on macOS, the system's memory-pressure killer ends the largest process when memory runs out). Not a parse error.

## Run history
- New this run: 1
- Recurring from previous run: 0
- Resolved since previous run: 1

**Resolved issue titles**
- FDR4 memory kill — verdict unmeasured

## Issues
### Issue 1: fdr4_signal_kill — FDR4 killed — verdict unmeasured

**Raw**
```
FDR4 was killed by signal 9 before giving a verdict (peak memory seen by the monitor: 79880 MB).
Found 6000 processes including 96 names

{ "copyright_notice": "FDR Version 4.2.7 copyright 2016 Oxford University Innovation Ltd. All Rights Reserved.","license_type": "Academic license for non-commercial use only","results": [
```

**Fix directive**
The model's state space exceeded the machine's memory, so FDR4 gave no verdict. The memory budget is already at the machine's limit and cannot be raised. This is not a parse error or a template defect. You may reduce the controller's state space in the Java (fewer transitions, fewer or narrower state variables, merged conditions), but only without changing the behaviour the requirements specify; otherwise record the FDR4 assertions as unmeasured.

## Files to review
- ModeValue.java
- OperatingMode.java
- SteamBoilerController.java
- WaterFlow.java

## Next step
Read each issue above, follow the fix directive, edit the linked Java file, and re-run Phase 6a (FDR4 Verification).
