# 6a — Dafny Verification — FAILED

## Summary
Dafny: 1 file(s), 6 verified, 7 errors.

## Run history
- New this run: 0
- Recurring from previous run: 4
- Resolved since previous run: 0

## Issues
### Issue 1: dafny_postcondition — Postcondition could not be proved [recurring x2]

**Raw**
```
forge.transformations/output/LreController.dfy(107,?): Error: a postcondition could not be proved on this return path
```

**Fix directive**
The method body cannot be shown to satisfy its `ensures` clause(s). Inspect the Java method linked below: either the body is missing a case, or the postcondition is too strong given the body's actual behaviour.

### Issue 2: dafny_postcondition — Postcondition could not be proved [recurring x2]

**Raw**
```
forge.transformations/output/LreController.dfy(111,?): Error: a postcondition could not be proved on this return path
```

**Fix directive**
The method body cannot be shown to satisfy its `ensures` clause(s). Inspect the Java method linked below: either the body is missing a case, or the postcondition is too strong given the body's actual behaviour.

### Issue 3: dafny_postcondition — Postcondition could not be proved [recurring x2]

**Raw**
```
forge.transformations/output/LreController.dfy(144,?): Error: a postcondition could not be proved on this return path
```

**Java trace**
  - RoboChart: TransitionMethod `HCM`
  - Java: `LreController.java`:74-179 (HCM)
  - Requirement(s): LRE-ARCH1, LRE-ARCH2, LRE-Beh1, LRE-Beh10, LRE-Beh11, LRE-Beh12, LRE-Beh13, LRE-Beh14, LRE-Beh15, LRE-Beh16, LRE-Beh17, LRE-Beh18, LRE-Beh19, LRE-Beh2, LRE-Beh3, LRE-Beh4, LRE-Beh5, LRE-Beh6, LRE-Beh7, LRE-Beh8, LRE-Beh9, LRE-DC1, LRE-GP1, LRE-Var1, LRE-Var2, LRE-Var3, LRE-Var4, LRE-Var5, LRE-Var6, LRE-Var7, LRE-Var8

**Fix directive**
The method body cannot be shown to satisfy its `ensures` clause(s). Inspect the Java method linked below: either the body is missing a case, or the postcondition is too strong given the body's actual behaviour.

Related requirements:
  - LRE-ARCH1 (SystemArchitecture): The Last Response Engine (LRE) system is an autonomous underwater vehicle (AUV) controller that mediates between an operator controller (which receives comma...
  - LRE-ARCH2 (ControllerArchitecture): The LRE controller is composed of: (1) a Sensor interface to receive environmental data and obstacle information, (2) an Actuator interface to send advised c...
  - LRE-Beh1 (InitialState): On power-up, the LRE begins in OCM.
  - LRE-Beh10 (MOM_to_HCM_DfltVert): The LRE transitions from MOM to HCM when the vertical distance (vdist) to the closest static obstacle (cstc) is at or below the staticObsDfltVertDist thresho...
  - ... (27 more)

### Issue 4: dafny_postcondition — Postcondition could not be proved [recurring x2]

**Raw**
```
forge.transformations/output/LreController.dfy(147,?): Error: a postcondition could not be proved on this return path
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
