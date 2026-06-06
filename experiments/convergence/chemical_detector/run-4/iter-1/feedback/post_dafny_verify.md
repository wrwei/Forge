# 6a — Dafny Verification — FAILED

## Summary
Dafny: 1 file(s), 0 verified, 0 errors.

## Run history
- New this run: 4
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: dafny_other — Dafny error: wrong number of arguments (got 2, but function 'goreq' expects 1: (p0: real))

**Raw**
```
forge.transformations/output/GasAnalysisController.dfy(95,?): Error: wrong number of arguments (got 2, but function 'goreq' expects 1: (p0: real))
```

**Java trace**
  - RoboChart: TransitionMethod `GasDetected`
  - Java: `MovementController.java`:48-160 (GasDetected)
  - Requirement(s): CD-ARCH2, CD-DC1, CD-MV-Beh1, CD-MV-Beh10, CD-MV-Beh11, CD-MV-Beh12, CD-MV-Beh13, CD-MV-Beh14, CD-MV-Beh15, CD-MV-Beh16, CD-MV-Beh17, CD-MV-Beh18, CD-MV-Beh19, CD-MV-Beh2, CD-MV-Beh20, CD-MV-Beh21, CD-MV-Beh22, CD-MV-Beh23, CD-MV-Beh3, CD-MV-Beh4, CD-MV-Beh5, CD-MV-Beh6, CD-MV-Beh7, CD-MV-Beh8, CD-MV-Beh9, CD-MV-Clock1, CD-MV-Var1, CD-MV-Var2, CD-MV-Var3, CD-MV-Var4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - CD-ARCH2 (ControllerArchitecture): The Chemical Detector is composed of three subsystems: (1) a sensing-and-actuation surface — the Vehicle — that exposes sensor events (gas, obstacle, odomete...
  - CD-DC1 (UniqueTransitions): Each behavioural requirement (CD-GA-Beh1..7 and CD-MV-Beh1..23) maps to exactly one transition. There are no duplicate transitions for the same source state ...
  - CD-MV-Beh1 (MV_Init): On system startup, the movement subsystem transitions from its initial state to Waiting. No guard, no trigger, no action.
  - CD-MV-Beh10 (MV_Avoiding_to_TryingAgain): In Avoiding, on receiving the turn event with payload a, the movement subsystem transitions to TryingAgain. Trigger: turn ? a. No guard, no action.
  - ... (26 more)

### Issue 2: dafny_other — Dafny error: wrong number of arguments (got 2, but function 'goreq' expects 1: (p0: real))

**Raw**
```
forge.transformations/output/GasAnalysisController.dfy(96,?): Error: wrong number of arguments (got 2, but function 'goreq' expects 1: (p0: real))
```

**Java trace**
  - RoboChart: TransitionMethod `GasDetected`
  - Java: `MovementController.java`:48-160 (GasDetected)
  - Requirement(s): CD-ARCH2, CD-DC1, CD-MV-Beh1, CD-MV-Beh10, CD-MV-Beh11, CD-MV-Beh12, CD-MV-Beh13, CD-MV-Beh14, CD-MV-Beh15, CD-MV-Beh16, CD-MV-Beh17, CD-MV-Beh18, CD-MV-Beh19, CD-MV-Beh2, CD-MV-Beh20, CD-MV-Beh21, CD-MV-Beh22, CD-MV-Beh23, CD-MV-Beh3, CD-MV-Beh4, CD-MV-Beh5, CD-MV-Beh6, CD-MV-Beh7, CD-MV-Beh8, CD-MV-Beh9, CD-MV-Clock1, CD-MV-Var1, CD-MV-Var2, CD-MV-Var3, CD-MV-Var4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - CD-ARCH2 (ControllerArchitecture): The Chemical Detector is composed of three subsystems: (1) a sensing-and-actuation surface — the Vehicle — that exposes sensor events (gas, obstacle, odomete...
  - CD-DC1 (UniqueTransitions): Each behavioural requirement (CD-GA-Beh1..7 and CD-MV-Beh1..23) maps to exactly one transition. There are no duplicate transitions for the same source state ...
  - CD-MV-Beh1 (MV_Init): On system startup, the movement subsystem transitions from its initial state to Waiting. No guard, no trigger, no action.
  - CD-MV-Beh10 (MV_Avoiding_to_TryingAgain): In Avoiding, on receiving the turn event with payload a, the movement subsystem transitions to TryingAgain. Trigger: turn ? a. No guard, no action.
  - ... (26 more)

### Issue 3: dafny_other — Dafny error: wrong number of arguments (got 2, but function 'goreq' expects 1: (p0: real))

**Raw**
```
forge.transformations/output/GasAnalysisController.dfy(99,?): Error: wrong number of arguments (got 2, but function 'goreq' expects 1: (p0: real))
```

**Java trace**
  - RoboChart: TransitionMethod `GasDetected`
  - Java: `MovementController.java`:48-160 (GasDetected)
  - Requirement(s): CD-ARCH2, CD-DC1, CD-MV-Beh1, CD-MV-Beh10, CD-MV-Beh11, CD-MV-Beh12, CD-MV-Beh13, CD-MV-Beh14, CD-MV-Beh15, CD-MV-Beh16, CD-MV-Beh17, CD-MV-Beh18, CD-MV-Beh19, CD-MV-Beh2, CD-MV-Beh20, CD-MV-Beh21, CD-MV-Beh22, CD-MV-Beh23, CD-MV-Beh3, CD-MV-Beh4, CD-MV-Beh5, CD-MV-Beh6, CD-MV-Beh7, CD-MV-Beh8, CD-MV-Beh9, CD-MV-Clock1, CD-MV-Var1, CD-MV-Var2, CD-MV-Var3, CD-MV-Var4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - CD-ARCH2 (ControllerArchitecture): The Chemical Detector is composed of three subsystems: (1) a sensing-and-actuation surface — the Vehicle — that exposes sensor events (gas, obstacle, odomete...
  - CD-DC1 (UniqueTransitions): Each behavioural requirement (CD-GA-Beh1..7 and CD-MV-Beh1..23) maps to exactly one transition. There are no duplicate transitions for the same source state ...
  - CD-MV-Beh1 (MV_Init): On system startup, the movement subsystem transitions from its initial state to Waiting. No guard, no trigger, no action.
  - CD-MV-Beh10 (MV_Avoiding_to_TryingAgain): In Avoiding, on receiving the turn event with payload a, the movement subsystem transitions to TryingAgain. Trigger: turn ? a. No guard, no action.
  - ... (26 more)

### Issue 4: dafny_other — Dafny error: wrong number of arguments (got 2, but function 'goreq' expects 1: (p0: real))

**Raw**
```
forge.transformations/output/GasAnalysisController.dfy(103,?): Error: wrong number of arguments (got 2, but function 'goreq' expects 1: (p0: real))
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

## Files to review
- Angle.java
- Chem.java
- GasAnalysisMode.java
- Loc.java
- MovementController.java
- MovementMode.java
- Status.java
- Vehicle.java

## Next step
Read each issue above, follow the fix directive, edit the linked Java file, re-run Phase 5b (Dafny Generation), then Phase 6b (Dafny Verification).
