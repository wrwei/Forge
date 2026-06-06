# 6a — Dafny Verification — FAILED

## Summary
Dafny: 1 file(s), 6 verified, 7 errors.

## Run history
- New this run: 3
- Recurring from previous run: 1
- Resolved since previous run: 3

**Resolved issue titles**
- Postcondition could not be proved
- Postcondition could not be proved
- Postcondition could not be proved

## Issues
### Issue 1: dafny_postcondition — Postcondition could not be proved

**Raw**
```
forge.transformations/output/LreController.dfy(113,?): Error: a postcondition could not be proved on this return path
```

**Fix directive**
The method body cannot be shown to satisfy its `ensures` clause(s). Inspect the Java method linked below: either the body is missing a case, or the postcondition is too strong given the body's actual behaviour.

### Issue 2: dafny_postcondition — Postcondition could not be proved [recurring x2]

**Raw**
```
forge.transformations/output/LreController.dfy(116,?): Error: a postcondition could not be proved on this return path
```

**Fix directive**
The method body cannot be shown to satisfy its `ensures` clause(s). Inspect the Java method linked below: either the body is missing a case, or the postcondition is too strong given the body's actual behaviour.

### Issue 3: dafny_postcondition — Postcondition could not be proved

**Raw**
```
forge.transformations/output/LreController.dfy(153,?): Error: a postcondition could not be proved on this return path
```

**Fix directive**
The method body cannot be shown to satisfy its `ensures` clause(s). Inspect the Java method linked below: either the body is missing a case, or the postcondition is too strong given the body's actual behaviour.

### Issue 4: dafny_postcondition — Postcondition could not be proved

**Raw**
```
forge.transformations/output/LreController.dfy(156,?): Error: a postcondition could not be proved on this return path
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
