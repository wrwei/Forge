# 6b — Dafny Verification — FAILED

## Summary
Dafny: 1 file(s), 6 verified, 5 errors.

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
### Issue 1: dafny_postcondition — Postcondition could not be proved

**Raw**
```
t2m.transformation.java/output/LreController.dfy(103,?): Error: a postcondition could not be proved on this return path
```

**Java trace**
  - RoboChart: TransitionMethod `MOM`
  - Java: `LreController.java`:78-199 (MOM)

**Fix directive**
The method body cannot be shown to satisfy its `ensures` clause(s). Inspect the Java method linked below: either the body is missing a case, or the postcondition is too strong given the body's actual behaviour.

### Issue 2: dafny_postcondition — Postcondition could not be proved

**Raw**
```
t2m.transformation.java/output/LreController.dfy(106,?): Error: a postcondition could not be proved on this return path
```

**Fix directive**
The method body cannot be shown to satisfy its `ensures` clause(s). Inspect the Java method linked below: either the body is missing a case, or the postcondition is too strong given the body's actual behaviour.

### Issue 3: dafny_postcondition — Postcondition could not be proved

**Raw**
```
t2m.transformation.java/output/LreController.dfy(143,?): Error: a postcondition could not be proved on this return path
```

**Java trace**
  - RoboChart: TransitionMethod `HCM`
  - Java: `LreController.java`:78-199 (HCM)

**Fix directive**
The method body cannot be shown to satisfy its `ensures` clause(s). Inspect the Java method linked below: either the body is missing a case, or the postcondition is too strong given the body's actual behaviour.

### Issue 4: dafny_postcondition — Postcondition could not be proved

**Raw**
```
t2m.transformation.java/output/LreController.dfy(146,?): Error: a postcondition could not be proved on this return path
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
