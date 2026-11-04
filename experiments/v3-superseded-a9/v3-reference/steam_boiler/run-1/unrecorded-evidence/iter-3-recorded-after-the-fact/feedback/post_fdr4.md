# 6b — Formal Verification (FDR4) — FAILED

## Summary
FDR4 killed: memory usage exceeded 10240 MB limit (peak: 10311 MB)

## Run history
- New this run: 1
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: fdr4_memory_kill — FDR4 memory kill — verdict unmeasured

**Raw**
```
FDR4 killed: memory usage exceeded 10240 MB limit (peak: 10311 MB)
```

**Fix directive**
Do NOT edit Java or templates for this issue. Either raise phases.fdr4.memory_limit_mb in pipeline.yaml (machine permitting) and re-run, or record the assertion set as unmeasured under the memory budget. Sequence- or record-typed channel domains are the usual driver.

## Files to review
- BoilerActuator.java
- FlowObservation.java
- OperatingMode.java
- PumpPosition.java
- SteamBoilerController.java
- ValvePosition.java

## Next step
Read each issue above, follow the fix directive, edit the linked Java file, and re-run Phase 6a (FDR4 Verification).
