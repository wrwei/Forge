# 6a — Dafny Verification — FAILED

## Summary
Dafny: 1 file(s), 0 verified, 0 errors.

## Run history
- New this run: 2
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: dafny_other — Dafny error: type of right argument to - (real) must agree with the result type (int)

**Raw**
```
forge.transformations/output/SRangerController.dfy(67,?): Error: type of right argument to - (real) must agree with the result type (int)
```

**Java trace**
  - RoboChart: TransitionMethod `Turning`
  - Java: `SRangerController.java`:48-89 (Turning)

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 2: dafny_other — Dafny error: type of right argument to - (real) must agree with the result type (int)

**Raw**
```
forge.transformations/output/SRangerController.dfy(74,?): Error: type of right argument to - (real) must agree with the result type (int)
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

## Files to review
- SRangerController.java
- SRangerMode.java

## Next step
Read each issue above, follow the fix directive, edit the linked Java file, re-run Phase 5b (Dafny Generation), then Phase 6b (Dafny Verification).
