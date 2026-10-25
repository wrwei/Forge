# 6a — Dafny Verification — FAILED

## Summary
Dafny: 2 file(s), 0 verified, 0 errors.

## Run history
- New this run: 2
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: dafny_other — Dafny error: type for constant 'TARGET' is 'real', but its initialization value type is 'int'

**Raw**
```
forge.transformations/output/GasAnalysisController.dfy(12,?): Error: type for constant 'TARGET' is 'real', but its initialization value type is 'int'
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 2: dafny_other — Dafny error: type for constant 'TARGET' is 'real', but its initialization value type is 'int'

**Raw**
```
forge.transformations/output/MovementController.dfy(12,?): Error: type for constant 'TARGET' is 'real', but its initialization value type is 'int'
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

## Files to review
- Angle.java
- GAMode.java
- Loc.java
- MVMode.java
- MovementController.java
- SharedEventBus.java
- Status.java
- Vehicle.java

## Next step
Read each issue above, follow the fix directive, edit the linked Java file, re-run Phase 5b (Dafny Generation), then Phase 6b (Dafny Verification).
