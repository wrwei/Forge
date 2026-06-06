# 6b — Dafny Verification — FAILED

## Summary
Dafny: 1 file(s), 0 verified, 0 errors.

## Run history
- New this run: 4
- Recurring from previous run: 0
- Resolved since previous run: 4

**Resolved issue titles**
- Postcondition could not be proved
- Postcondition could not be proved
- Postcondition could not be proved
- Postcondition could not be proved

## Issues
### Issue 1: dafny_other — Dafny error: wrong number of arguments (got 2, but function 'goreq' expects 1: (p0: real))

**Raw**
```
t2m.transformation.java/output/GasAnalysisController.dfy(93,?): Error: wrong number of arguments (got 2, but function 'goreq' expects 1: (p0: real))
```

**Java trace**
  - RoboChart: TransitionMethod `GasDetected`
  - Java: `GasAnalysisController.java`:53-102 (GasDetected)

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 2: dafny_other — Dafny error: wrong number of arguments (got 2, but function 'goreq' expects 1: (p0: real))

**Raw**
```
t2m.transformation.java/output/GasAnalysisController.dfy(94,?): Error: wrong number of arguments (got 2, but function 'goreq' expects 1: (p0: real))
```

**Java trace**
  - RoboChart: TransitionMethod `GasDetected`
  - Java: `GasAnalysisController.java`:53-102 (GasDetected)

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 3: dafny_other — Dafny error: wrong number of arguments (got 2, but function 'goreq' expects 1: (p0: real))

**Raw**
```
t2m.transformation.java/output/GasAnalysisController.dfy(97,?): Error: wrong number of arguments (got 2, but function 'goreq' expects 1: (p0: real))
```

**Java trace**
  - RoboChart: TransitionMethod `GasDetected`
  - Java: `GasAnalysisController.java`:53-102 (GasDetected)

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 4: dafny_other — Dafny error: wrong number of arguments (got 2, but function 'goreq' expects 1: (p0: real))

**Raw**
```
t2m.transformation.java/output/GasAnalysisController.dfy(100,?): Error: wrong number of arguments (got 2, but function 'goreq' expects 1: (p0: real))
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

## Files to review
- Angle.java
- ChangeDirection.java
- GasAnalysisController.java
- GasAnalysisMode.java
- GasAnalysisOutput.java
- Loc.java
- MovementMode.java
- Status.java
- Vehicle.java

## Next step
Read each issue above, follow the fix directive, edit the linked Java file, re-run Phase 5b (Dafny Generation), then Phase 6b (Dafny Verification).
