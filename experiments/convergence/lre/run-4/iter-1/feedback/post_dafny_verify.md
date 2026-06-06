# 6a — Dafny Verification — FAILED

## Summary
Dafny: 1 file(s), 6 verified, 6 errors.

## Run history
- New this run: 3
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: dafny_postcondition — Postcondition could not be proved

**Raw**
```
forge.transformations/output/LreController.dfy(100,?): Error: a postcondition could not be proved on this return path
```

**Fix directive**
The method body cannot be shown to satisfy its `ensures` clause(s). Inspect the Java method linked below: either the body is missing a case, or the postcondition is too strong given the body's actual behaviour.

### Issue 2: dafny_postcondition — Postcondition could not be proved

**Raw**
```
forge.transformations/output/LreController.dfy(104,?): Error: a postcondition could not be proved on this return path
```

**Fix directive**
The method body cannot be shown to satisfy its `ensures` clause(s). Inspect the Java method linked below: either the body is missing a case, or the postcondition is too strong given the body's actual behaviour.

### Issue 3: dafny_postcondition — Postcondition could not be proved

**Raw**
```
forge.transformations/output/LreController.dfy(140,?): Error: a postcondition could not be proved on this return path
```

**Fix directive**
The method body cannot be shown to satisfy its `ensures` clause(s). Inspect the Java method linked below: either the body is missing a case, or the postcondition is too strong given the body's actual behaviour.

## Files to review
- CalcCDyn.java
- CalcCPA.java
- CalcCStc.java
- CalcVel.java
- CheckOPEZ.java
- LreController.java
- LreMode.java

## Next step
Read each issue above, follow the fix directive, edit the linked Java file, re-run Phase 5b (Dafny Generation), then Phase 6b (Dafny Verification).
