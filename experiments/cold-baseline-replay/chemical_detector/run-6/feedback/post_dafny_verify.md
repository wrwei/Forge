# 6a — Dafny Verification — FAILED

## Summary
Dafny: 2 file(s), 0 verified, 0 errors.

## Run history
- New this run: 42
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: dafny_other — Dafny error: first argument to ==> must be of type bool (instead got real)

**Raw**
```
forge.transformations/output/GasAnalysisController.dfy(96,?): Error: first argument to ==> must be of type bool (instead got real)
```

**Java trace**
  - RoboChart: TransitionMethod `GasDetected`
  - Java: `GasAnalysisController.java`:57-102 (GasDetected)

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 2: dafny_other — Dafny error: condition is expected to be of type bool, but is real

**Raw**
```
forge.transformations/output/GasAnalysisController.dfy(99,?): Error: condition is expected to be of type bool, but is real
```

**Java trace**
  - RoboChart: TransitionMethod `GasDetected`
  - Java: `GasAnalysisController.java`:57-102 (GasDetected)

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 3: dafny_other — Dafny error: unresolved identifier: Stop

**Raw**
```
forge.transformations/output/MovementController.dfy(67,?): Error: unresolved identifier: Stop
```

**Java trace**
  - RoboChart: TransitionMethod `Analysis`
  - Java: `GasAnalysisController.java`:57-102 (Analysis)

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 4: dafny_other — Dafny error: unresolved identifier: Stop

**Raw**
```
forge.transformations/output/MovementController.dfy(68,?): Error: unresolved identifier: Stop
```

**Java trace**
  - RoboChart: TransitionMethod `Analysis`
  - Java: `GasAnalysisController.java`:57-102 (Analysis)

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 5: dafny_other — Dafny error: unresolved identifier: Resume

**Raw**
```
forge.transformations/output/MovementController.dfy(69,?): Error: unresolved identifier: Resume
```

**Java trace**
  - RoboChart: TransitionMethod `Analysis`
  - Java: `GasAnalysisController.java`:57-102 (Analysis)

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 6: dafny_other — Dafny error: unresolved identifier: Stop

**Raw**
```
forge.transformations/output/MovementController.dfy(72,?): Error: unresolved identifier: Stop
```

**Java trace**
  - RoboChart: TransitionMethod `Analysis`
  - Java: `GasAnalysisController.java`:57-102 (Analysis)

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 7: dafny_other — Dafny error: unresolved identifier: Resume

**Raw**
```
forge.transformations/output/MovementController.dfy(76,?): Error: unresolved identifier: Resume
```

**Java trace**
  - RoboChart: TransitionMethod `Analysis`
  - Java: `GasAnalysisController.java`:57-102 (Analysis)

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 8: dafny_other — Dafny error: unresolved identifier: Turn

**Raw**
```
forge.transformations/output/MovementController.dfy(79,?): Error: unresolved identifier: Turn
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 9: dafny_other — Dafny error: unresolved identifier: Stop

**Raw**
```
forge.transformations/output/MovementController.dfy(88,?): Error: unresolved identifier: Stop
```

**Java trace**
  - RoboChart: TransitionMethod `NoGas`
  - Java: `GasAnalysisController.java`:57-102 (NoGas)

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 10: dafny_other — Dafny error: unresolved identifier: Stop

**Raw**
```
forge.transformations/output/MovementController.dfy(89,?): Error: unresolved identifier: Stop
```

**Java trace**
  - RoboChart: TransitionMethod `NoGas`
  - Java: `GasAnalysisController.java`:57-102 (NoGas)

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 11: dafny_other — Dafny error: unresolved identifier: Resume

**Raw**
```
forge.transformations/output/MovementController.dfy(90,?): Error: unresolved identifier: Resume
```

**Java trace**
  - RoboChart: TransitionMethod `NoGas`
  - Java: `GasAnalysisController.java`:57-102 (NoGas)

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 12: dafny_other — Dafny error: unresolved identifier: Turn

**Raw**
```
forge.transformations/output/MovementController.dfy(91,?): Error: unresolved identifier: Turn
```

**Java trace**
  - RoboChart: TransitionMethod `Going`
  - Java: `GasAnalysisController.java`:57-102 (Going)

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 13: dafny_other — Dafny error: unresolved identifier: Stop

**Raw**
```
forge.transformations/output/MovementController.dfy(94,?): Error: unresolved identifier: Stop
```

**Java trace**
  - RoboChart: TransitionMethod `GasDetected`
  - Java: `GasAnalysisController.java`:57-102 (GasDetected)

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 14: dafny_other — Dafny error: unresolved identifier: Resume

**Raw**
```
forge.transformations/output/MovementController.dfy(98,?): Error: unresolved identifier: Resume
```

**Java trace**
  - RoboChart: TransitionMethod `GasDetected`
  - Java: `GasAnalysisController.java`:57-102 (GasDetected)

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 15: dafny_other — Dafny error: unresolved identifier: Turn

**Raw**
```
forge.transformations/output/MovementController.dfy(101,?): Error: unresolved identifier: Turn
```

**Java trace**
  - RoboChart: TransitionMethod `GasDetected`
  - Java: `GasAnalysisController.java`:57-102 (GasDetected)

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 16: dafny_other — Dafny error: unresolved identifier: Obstacle

**Raw**
```
forge.transformations/output/MovementController.dfy(104,?): Error: unresolved identifier: Obstacle
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 17: dafny_other — Dafny error: unresolved identifier: Stop

**Raw**
```
forge.transformations/output/MovementController.dfy(113,?): Error: unresolved identifier: Stop
```

**Java trace**
  - RoboChart: TransitionMethod `J1`
  - Java: `GasAnalysisController.java`:57-102 (J1)

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 18: dafny_other — Dafny error: unresolved identifier: Stop

**Raw**
```
forge.transformations/output/MovementController.dfy(114,?): Error: unresolved identifier: Stop
```

**Java trace**
  - RoboChart: TransitionMethod `Avoiding`
  - Java: `GasAnalysisController.java`:57-102 (Avoiding)

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 19: dafny_other — Dafny error: unresolved identifier: Resume

**Raw**
```
forge.transformations/output/MovementController.dfy(115,?): Error: unresolved identifier: Resume
```

**Java trace**
  - RoboChart: StepMethod `step`
  - Java: `GasAnalysisController.java`:57-102 (step)

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 20: dafny_other — Dafny error: unresolved identifier: Stop

**Raw**
```
forge.transformations/output/MovementController.dfy(118,?): Error: unresolved identifier: Stop
```

**Java trace**
  - RoboChart: StepMethod `step`
  - Java: `GasAnalysisController.java`:57-102 (step)

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 21: dafny_other — Dafny error: unresolved identifier: Resume

**Raw**
```
forge.transformations/output/MovementController.dfy(122,?): Error: unresolved identifier: Resume
```

**Java trace**
  - RoboChart: StepMethod `step`
  - Java: `GasAnalysisController.java`:57-102 (step)

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 22: dafny_other — Dafny error: unresolved identifier: Turn

**Raw**
```
forge.transformations/output/MovementController.dfy(125,?): Error: unresolved identifier: Turn
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 23: dafny_other — Dafny error: unresolved identifier: Stop

**Raw**
```
forge.transformations/output/MovementController.dfy(134,?): Error: unresolved identifier: Stop
```

**Java trace**
  - RoboChart: TransitionMethod `TryingAgain`
  - Java: `GasAnalysisController.java`:57-102 (TryingAgain)

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 24: dafny_other — Dafny error: unresolved identifier: Stop

**Raw**
```
forge.transformations/output/MovementController.dfy(135,?): Error: unresolved identifier: Stop
```

**Java trace**
  - RoboChart: TransitionMethod `TryingAgain`
  - Java: `GasAnalysisController.java`:57-102 (TryingAgain)

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 25: dafny_other — Dafny error: unresolved identifier: Resume

**Raw**
```
forge.transformations/output/MovementController.dfy(136,?): Error: unresolved identifier: Resume
```

**Java trace**
  - RoboChart: TransitionMethod `TryingAgain`
  - Java: `GasAnalysisController.java`:57-102 (TryingAgain)

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 26: dafny_other — Dafny error: unresolved identifier: Turn

**Raw**
```
forge.transformations/output/MovementController.dfy(137,?): Error: unresolved identifier: Turn
```

**Java trace**
  - RoboChart: TransitionMethod `TryingAgain`
  - Java: `GasAnalysisController.java`:57-102 (TryingAgain)

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 27: dafny_other — Dafny error: unresolved identifier: Stop

**Raw**
```
forge.transformations/output/MovementController.dfy(140,?): Error: unresolved identifier: Stop
```

**Java trace**
  - RoboChart: Lemma `Analysis_deterministic`
  - Java: `GasAnalysisController.java`:57-102 (Analysis_deterministic)

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 28: dafny_other — Dafny error: unresolved identifier: Resume

**Raw**
```
forge.transformations/output/MovementController.dfy(144,?): Error: unresolved identifier: Resume
```

**Java trace**
  - RoboChart: Lemma `Analysis_deterministic`
  - Java: `GasAnalysisController.java`:57-102 (Analysis_deterministic)

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 29: dafny_other — Dafny error: unresolved identifier: Turn

**Raw**
```
forge.transformations/output/MovementController.dfy(147,?): Error: unresolved identifier: Turn
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 30: dafny_other — Dafny error: unresolved identifier: Obstacle

**Raw**
```
forge.transformations/output/MovementController.dfy(150,?): Error: unresolved identifier: Obstacle
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 31: dafny_other — Dafny error: unresolved identifier: Stop

**Raw**
```
forge.transformations/output/MovementController.dfy(159,?): Error: unresolved identifier: Stop
```

**Java trace**
  - RoboChart: TransitionMethod `AvoidingAgain`
  - Java: `GasAnalysisController.java`:57-102 (AvoidingAgain)

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 32: dafny_other — Dafny error: unresolved identifier: Stop

**Raw**
```
forge.transformations/output/MovementController.dfy(160,?): Error: unresolved identifier: Stop
```

**Java trace**
  - RoboChart: TransitionMethod `AvoidingAgain`
  - Java: `GasAnalysisController.java`:57-102 (AvoidingAgain)

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 33: dafny_other — Dafny error: unresolved identifier: Resume

**Raw**
```
forge.transformations/output/MovementController.dfy(161,?): Error: unresolved identifier: Resume
```

**Java trace**
  - RoboChart: TransitionMethod `AvoidingAgain`
  - Java: `GasAnalysisController.java`:57-102 (AvoidingAgain)

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 34: dafny_other — Dafny error: unresolved identifier: Resume

**Raw**
```
forge.transformations/output/MovementController.dfy(162,?): Error: unresolved identifier: Resume
```

**Java trace**
  - RoboChart: TransitionMethod `AvoidingAgain`
  - Java: `GasAnalysisController.java`:57-102 (AvoidingAgain)

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 35: dafny_other — Dafny error: unresolved identifier: Stop

**Raw**
```
forge.transformations/output/MovementController.dfy(165,?): Error: unresolved identifier: Stop
```

**Java trace**
  - RoboChart: TransitionMethod `AvoidingAgain`
  - Java: `GasAnalysisController.java`:57-102 (AvoidingAgain)

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 36: dafny_other — Dafny error: unresolved identifier: Resume

**Raw**
```
forge.transformations/output/MovementController.dfy(169,?): Error: unresolved identifier: Resume
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 37: dafny_other — Dafny error: unresolved identifier: Stop

**Raw**
```
forge.transformations/output/MovementController.dfy(184,?): Error: unresolved identifier: Stop
```

**Java trace**
  - RoboChart: TransitionMethod `GettingOut`
  - Java: `GasAnalysisController.java`:57-102 (GettingOut)

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 38: dafny_other — Dafny error: unresolved identifier: Stop

**Raw**
```
forge.transformations/output/MovementController.dfy(185,?): Error: unresolved identifier: Stop
```

**Java trace**
  - RoboChart: TransitionMethod `GettingOut`
  - Java: `GasAnalysisController.java`:57-102 (GettingOut)

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 39: dafny_other — Dafny error: unresolved identifier: Resume

**Raw**
```
forge.transformations/output/MovementController.dfy(186,?): Error: unresolved identifier: Resume
```

**Java trace**
  - RoboChart: TransitionMethod `GettingOut`
  - Java: `GasAnalysisController.java`:57-102 (GettingOut)

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 40: dafny_other — Dafny error: unresolved identifier: Stop

**Raw**
```
forge.transformations/output/MovementController.dfy(189,?): Error: unresolved identifier: Stop
```

**Java trace**
  - RoboChart: TransitionMethod `GettingOut`
  - Java: `GasAnalysisController.java`:57-102 (GettingOut)

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 41: dafny_other — Dafny error: unresolved identifier: Resume

**Raw**
```
forge.transformations/output/MovementController.dfy(193,?): Error: unresolved identifier: Resume
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 42: dafny_other — Dafny error: unresolved identifier: Turn

**Raw**
```
forge.transformations/output/MovementController.dfy(196,?): Error: unresolved identifier: Turn
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

## Files to review
(none identified)

## Next step
Read each issue above, follow the fix directive, edit the linked Java file, re-run Phase 5b (Dafny Generation), then Phase 6b (Dafny Verification).
