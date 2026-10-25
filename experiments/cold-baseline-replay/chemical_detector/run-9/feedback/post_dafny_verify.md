# 6a — Dafny Verification — FAILED

## Summary
Dafny: 2 file(s), 12 verified, 0 errors.

## Run history
- New this run: 4
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: dafny_other — Dafny error: type real does not have a member value

**Raw**
```
forge.transformations/output/GasAnalysisController.dfy(95,?): Error: type real does not have a member value
```

**Java trace**
  - RoboChart: TransitionMethod `GasDetected`
  - Java: `GasAnalysisController.java`:76-132 (GasDetected)

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 2: dafny_other — Dafny error: type real does not have a member value

**Raw**
```
forge.transformations/output/GasAnalysisController.dfy(96,?): Error: type real does not have a member value
```

**Java trace**
  - RoboChart: TransitionMethod `GasDetected`
  - Java: `GasAnalysisController.java`:76-132 (GasDetected)

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 3: dafny_other — Dafny error: type real does not have a member value

**Raw**
```
forge.transformations/output/GasAnalysisController.dfy(99,?): Error: type real does not have a member value
```

**Java trace**
  - RoboChart: TransitionMethod `GasDetected`
  - Java: `GasAnalysisController.java`:76-132 (GasDetected)

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 4: dafny_other — Dafny error: type real does not have a member value

**Raw**
```
forge.transformations/output/GasAnalysisController.dfy(102,?): Error: type real does not have a member value
```

**Java trace**
  - RoboChart: TransitionMethod `Avoiding`
  - Java: `GasAnalysisController.java`:76-132 (Avoiding)

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

## Files to review
- Analysis.java
- Angle.java
- ChangeDirection.java
- GasAnalysisController.java
- GasAnalysisMode.java
- GasSensorService.java
- IntensityCompute.java
- Loc.java
- LocationCompute.java
- MovementMode.java
- MovementSensorService.java
- Status.java
- Vehicle.java

## Next step
Read each issue above, follow the fix directive, edit the linked Java file, re-run Phase 5b (Dafny Generation), then Phase 6b (Dafny Verification).
