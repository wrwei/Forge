# 6b — Dafny Verification — FAILED

## Summary
Dafny: 1 file(s), 4 verified, 1 errors.

## Run history
- New this run: 1
- Recurring from previous run: 0
- Resolved since previous run: 2

**Resolved issue titles**
- Dafny error: wrong number of arguments (got 2, but function 'goreq' expects 1: (p0: real))
- Dafny error: wrong number of arguments (got 2, but function 'goreq' expects 1: (p0: real))

## Issues
### Issue 1: dafny_postcondition — Postcondition could not be proved

**Raw**
```
t2m.transformation.java/output/SRangerController.dfy(71,?): Error: a postcondition could not be proved on this return path
```

**Java trace**
  - RoboChart: TransitionMethod `Turning`
  - Java: `SRangerController.java`:59-103 (Turning)

**Fix directive**
The method body cannot be shown to satisfy its `ensures` clause(s). Inspect the Java method linked below: either the body is missing a case, or the postcondition is too strong given the body's actual behaviour.

## Files to review
- SRangerController.java
- SRangerMode.java

## Next step
Read each issue above, follow the fix directive, edit the linked Java file, re-run Phase 5b (Dafny Generation), then Phase 6b (Dafny Verification).
