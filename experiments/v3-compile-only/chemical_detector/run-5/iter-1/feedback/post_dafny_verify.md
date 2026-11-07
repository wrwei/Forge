# 6a — Dafny Verification — FAILED

## Summary
Dafny: 2 file(s), 0 verified, 0 errors.

## Run history
- New this run: 2
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: dafny_other — Dafny error: unresolved identifier: values

**Raw**
```
forge.transformations/output/GasAnalysisController.dfy(18,?): Error: unresolved identifier: values
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 2: dafny_other — Dafny error: unresolved identifier: values

**Raw**
```
forge.transformations/output/MovementController.dfy(18,?): Error: unresolved identifier: values
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

## Files to review
- Angle.java
- GasAnalysisController.java
- GasAnalysisMode.java
- Loc.java
- MovementMode.java
- Status.java
- VehicleActuator.java

## Next step
Read each issue above, follow the fix directive, edit the linked Java file, re-run Phase 5b (Dafny Generation), then Phase 6b (Dafny Verification).
