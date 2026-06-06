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
forge.transformations/output/GasAnalysisController.dfy(94,?): Error: wrong number of arguments (got 2, but function 'goreq' expects 1: (p0: real))
```

**Java trace**
  - RoboChart: TransitionMethod `GasDetected`
  - Java: `GasAnalysisController.java`:48-89 (GasDetected)
  - Requirement(s): CD-ARCH2, CD-DM7, CD-GA-Beh1, CD-GA-Beh2, CD-GA-Beh3, CD-GA-Beh4, CD-GA-Beh5, CD-GA-Beh6, CD-GA-Beh7, CD-GA-Var1, CD-GA-Var2, CD-GA-Var3, CD-GA-Var4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - CD-ARCH2 (ControllerArchitecture): The Chemical Detector is composed of three subsystems: (1) a sensing-and-actuation surface — the Vehicle — that exposes sensor events (gas, obstacle, odomete...
  - CD-DM7 (GasSensorReading): A GasSensorReading is a sequence of GasSensor values produced by the Vehicle's gas event. The position in the sequence (1-based index) encodes which sensor p...
  - CD-GA-Beh1 (GA_Init): On system startup, the gas-analysis subsystem transitions from its initial state to Reading. No guard, no trigger, no action.
  - CD-GA-Beh2 (GA_Reading_to_Analysis): In Reading, on receiving the gas event with payload gs, the gas-analysis subsystem transitions to Analysis. Trigger: gas ? gs. No guard, no action.
  - ... (9 more)

### Issue 2: dafny_other — Dafny error: wrong number of arguments (got 2, but function 'goreq' expects 1: (p0: real))

**Raw**
```
forge.transformations/output/GasAnalysisController.dfy(95,?): Error: wrong number of arguments (got 2, but function 'goreq' expects 1: (p0: real))
```

**Java trace**
  - RoboChart: TransitionMethod `GasDetected`
  - Java: `GasAnalysisController.java`:48-89 (GasDetected)
  - Requirement(s): CD-ARCH2, CD-DM7, CD-GA-Beh1, CD-GA-Beh2, CD-GA-Beh3, CD-GA-Beh4, CD-GA-Beh5, CD-GA-Beh6, CD-GA-Beh7, CD-GA-Var1, CD-GA-Var2, CD-GA-Var3, CD-GA-Var4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - CD-ARCH2 (ControllerArchitecture): The Chemical Detector is composed of three subsystems: (1) a sensing-and-actuation surface — the Vehicle — that exposes sensor events (gas, obstacle, odomete...
  - CD-DM7 (GasSensorReading): A GasSensorReading is a sequence of GasSensor values produced by the Vehicle's gas event. The position in the sequence (1-based index) encodes which sensor p...
  - CD-GA-Beh1 (GA_Init): On system startup, the gas-analysis subsystem transitions from its initial state to Reading. No guard, no trigger, no action.
  - CD-GA-Beh2 (GA_Reading_to_Analysis): In Reading, on receiving the gas event with payload gs, the gas-analysis subsystem transitions to Analysis. Trigger: gas ? gs. No guard, no action.
  - ... (9 more)

### Issue 3: dafny_other — Dafny error: wrong number of arguments (got 2, but function 'goreq' expects 1: (p0: real))

**Raw**
```
forge.transformations/output/GasAnalysisController.dfy(98,?): Error: wrong number of arguments (got 2, but function 'goreq' expects 1: (p0: real))
```

**Java trace**
  - RoboChart: TransitionMethod `GasDetected`
  - Java: `GasAnalysisController.java`:48-89 (GasDetected)
  - Requirement(s): CD-ARCH2, CD-DM7, CD-GA-Beh1, CD-GA-Beh2, CD-GA-Beh3, CD-GA-Beh4, CD-GA-Beh5, CD-GA-Beh6, CD-GA-Beh7, CD-GA-Var1, CD-GA-Var2, CD-GA-Var3, CD-GA-Var4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - CD-ARCH2 (ControllerArchitecture): The Chemical Detector is composed of three subsystems: (1) a sensing-and-actuation surface — the Vehicle — that exposes sensor events (gas, obstacle, odomete...
  - CD-DM7 (GasSensorReading): A GasSensorReading is a sequence of GasSensor values produced by the Vehicle's gas event. The position in the sequence (1-based index) encodes which sensor p...
  - CD-GA-Beh1 (GA_Init): On system startup, the gas-analysis subsystem transitions from its initial state to Reading. No guard, no trigger, no action.
  - CD-GA-Beh2 (GA_Reading_to_Analysis): In Reading, on receiving the gas event with payload gs, the gas-analysis subsystem transitions to Analysis. Trigger: gas ? gs. No guard, no action.
  - ... (9 more)

### Issue 4: dafny_other — Dafny error: wrong number of arguments (got 2, but function 'goreq' expects 1: (p0: real))

**Raw**
```
forge.transformations/output/GasAnalysisController.dfy(102,?): Error: wrong number of arguments (got 2, but function 'goreq' expects 1: (p0: real))
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

## Files to review
- Angle.java
- GasAnalysisController.java
- GasAnalysisMode.java
- Loc.java
- MovementMode.java
- Status.java
- Vehicle.java

## Next step
Read each issue above, follow the fix directive, edit the linked Java file, re-run Phase 5b (Dafny Generation), then Phase 6b (Dafny Verification).
