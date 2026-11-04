# 6b — Formal Verification (FDR4) — FAILED

## Summary
FDR4 loaded the CSP and was exploring the state space when it was killed by a signal (on macOS, the system's memory-pressure killer ends the largest process when memory runs out). Not a parse error.

## Run history
- New this run: 1
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: fdr4_signal_kill — FDR4 killed — verdict unmeasured

**Raw**
```
FDR4 was killed by signal 9 before giving a verdict (peak memory seen by the monitor: 76976 MB).
Found 2000 processes including 78 names
Found 8000 processes including 82 names
Found 18000 processes including 82 names
Found 20000 processes including 82 names
Found 22000 processes including 82 names
Found 24000 processes including 82 names
Found 26000 processes including 82 names
Found 28000 processes including 82 names
Found 30000 processes including 82 names
Found 32000 processes including 82 names
Found 38000 processes including 86 names
Found 40000 processes including 86 names
Found 44000 processes including 86 names
Found 52000 processes including 86 names
Found 58000 processes including 86 names
Found 60000 processes including 86 names
Found 62000 processes including 86 names
Found 68000 processes including 86 names
Found 74000 processes including 86 names
Found 76000 processes including 86 names
Found 78000 processes including 86 names
Found 80000 processes including 86 names
Found 82000 processes including 86 names

{ "copyright_notice": "FDR Version 4.2.7 copyright 2016 Oxford University Innovation Ltd. All Rights Reserved.","license_type": "Academic license for non-commercial use only","results": [
```

**Fix directive**
The model's state space exceeded the machine's memory, so FDR4 gave no verdict. The memory budget is already at the machine's limit and cannot be raised. This is not a parse error or a template defect. You may reduce the controller's state space in the Java (fewer transitions, fewer or narrower state variables, merged conditions), but only without changing the behaviour the requirements specify; otherwise record the FDR4 assertions as unmeasured.

## Files to review
- BoilerMode.java
- OperatingMode.java
- PumpPosition.java
- SteamBoilerController.java
- WaterFlow.java

## Next step
Read each issue above, follow the fix directive, edit the linked Java file, and re-run Phase 6a (FDR4 Verification).
