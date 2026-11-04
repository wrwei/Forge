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
FDR4 was killed by signal 9 before giving a verdict (peak memory seen by the monitor: 77110 MB).
Found 2000 processes including 91 names
Found 12000 processes including 99 names
Found 16000 processes including 99 names
Found 18000 processes including 99 names
Found 20000 processes including 99 names
Found 28000 processes including 99 names
Found 32000 processes including 99 names
Found 38000 processes including 99 names
Found 40000 processes including 99 names
Found 42000 processes including 101 names
Found 44000 processes including 101 names
Found 52000 processes including 101 names
Found 54000 processes including 101 names
Found 58000 processes including 101 names
Found 60000 processes including 101 names
Found 70000 processes including 101 names
Found 72000 processes including 101 names
Found 74000 processes including 101 names
Found 78000 processes including 101 names
Found 80000 processes including 101 names
Found 96000 processes including 101 names
Found 100000 processes including 101 names
Found 112000 processes including 101 names
Found 114000 processes including 101 names
Found 118000 processes including 101 names
Found 130000 processes including 102 names
Found 132000 processes including 102 names
Found 134000 processes including 102 names
Found 136000 processes including 102 names
Found 140000 processes including 102 names
Found 142000 processes including 102 names
Found 144000 processes including 102 names
Found 148000 processes including 102 names
Found 156000 processes including 102 names
Found 170000 processes including 102 names
Found 172000 processes including 102 names
Found 176000 processes including 102 names
Found 186000 processes including 102 names
Found 198000 processes including 102 names
Found 198000 processes including 102 names
Found 204000 processes including 102 names
Found 216000 processes including 103 names
Found 220000 processes including 103 names
Found 224000 processes including 103 names
Found 230000 processes including 103 names
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
