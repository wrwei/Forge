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
FDR4 was killed by signal 9 before giving a verdict (peak memory seen by the monitor: 81373 MB).
Found 8000 processes including 90 names
Found 10000 processes including 90 names
Found 16000 processes including 90 names
Found 18000 processes including 90 names
Found 32000 processes including 90 names
Found 34000 processes including 90 names
Found 44000 processes including 90 names
Found 50000 processes including 90 names
Found 52000 processes including 90 names
Found 56000 processes including 90 names
Found 60000 processes including 94 names
Found 62000 processes including 94 names
Found 66000 processes including 94 names
Found 68000 processes including 94 names
Found 70000 processes including 94 names
Found 72000 processes including 94 names
Found 76000 processes including 94 names
Found 106000 processes including 94 names
Found 108000 processes including 94 names
Found 114000 processes including 94 names
Found 126000 processes including 94 names
Found 132000 processes including 96 names
Found 134000 processes including 96 names
Found 136000 processes including 96 names
Found 140000 processes including 96 names
Found 150000 processes including 96 names
Found 154000 processes including 96 names
Found 162000 processes including 96 names
Found 164000 processes including 96 names
Found 168000 processes including 96 names
Found 188000 processes including 96 names
Found 190000 processes including 96 names
Found 196000 processes including 96 names

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
