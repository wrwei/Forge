# Isabelle Verification — PASSED

## Summary
Isabelle: 2 theories, 78 lemma(s) verified, elapsed 0:03:45, peak 28 MB.

## Run history
- New this run: 1
- Recurring from previous run: 0
- Resolved since previous run: 26

**Resolved issue titles**
- Isabelle error: Coercion inference failed:
- Isabelle error: no supremum
- Isabelle error: *** Cannot fulfil subtype constraints:
- Isabelle error: Loc  <:  ??'a   from function application  side () = right
- Isabelle error: Loc  <:  ??'a   from function application  side () = front
- Isabelle error: Loc  <:  ??'a   from function application  side () = left
- Isabelle error: MovementController ⇒ ℝ  <:  MovementController ⇒ ℝ
- Isabelle error: from function application
- Isabelle error: [st ↝ Waiting, evadeVelocity ↝ lv (), turnRight ↝ side () = left,
- Isabelle error: turnLeft ↝ side () = right, turnBack ↝ side () = front,
- Isabelle error: velocity ↝ speed ()]
- Isabelle error: At command "zoperation" (line 131 of "/Users/ranwei/Gitee/forge-v3r7-run4-chemic
- Type unification failed
- Isabelle error: *** Type error in application: incompatible operand type
- Isabelle error: *** Operator:  (=) (side ()) :: ℝ ⇒ 𝔹
- Isabelle error: Operand:   left :: Loc
- Isabelle error: *** Coercion Inference:
- Isabelle error: *** Local coercion insertion on the operand failed:
- Isabelle error: "Loc" is not a subtype of "ℝ"
- Isabelle error: *** Now trying to infer coercions globally.
- Isabelle error: *** Coercion inference failed:
- Isabelle error: GasAnalysisController ⇒ ℝ  <:  GasAnalysisController ⇒ ℝ
- Isabelle error: [st ↝ Reading, evadeVelocity ↝ lv (), turnRight ↝ side () = left,
- Isabelle error: turnLeft ↝ side () = right, turnBack ↝ side () = front,
- Isabelle error: velocity ↝ speed ()]
- Isabelle error: At command "zoperation" (line 116 of "/Users/ranwei/Gitee/forge-v3r7-run4-chemic

## Issues
### Issue 1: isabelle_summary — Verification summary

**Raw**
```
Runtime: elapsed **0:03:45**, peak memory **28 MB**.

**Theories built (2), 40 lemma(s) proven total:**

**`GasAnalysisController_Beh.thy`** — 11 lemmas:
- *Deadlock-freedom* (1 lemma):
  - `GasAnalysisController_deadlock_free`
- *Invariant preservation* (10 lemmas): `Init_inv`, `InitialToReading_inv`, `ReadingToAnalysis_inv`, `AnalysisToNoGas_inv`, `AnalysisToGasDetected_inv`, `NoGasToReading_inv`, `GasDetectedToConcluded_inv`, `GasDetectedToReading_inv`, `Idle_Reading_inv`, `Idle_Analysis_inv`

**`MovementController_Beh.thy`** — 29 lemmas:
- *Deadlock-freedom* (1 lemma):
  - `MovementController_deadlock_free`
- *Invariant preservation* (28 lemmas): `Init_inv`, `InitialToWaiting_inv`, `WaitingToFound_inv`, `WaitingToWaiting_inv`, `WaitingToGoing_inv`, `GoingToFound_inv`, `GoingToWaiting_inv`, `GoingToGoing_inv`, `GoingToAvoiding_inv`, `AvoidingToFound_inv`, `AvoidingToWaiting_inv`, `AvoidingToTryingAgain_inv`, `TryingAgainToFound_inv`, `TryingAgainToWaiting_inv`, `TryingAgainToTryingAgain_inv`, `TryingAgainToAvoidingAgain_inv`, `AvoidingAgainToFound_inv`, `AvoidingAgainToWaiting_inv`, `AvoidingAgainToAvoiding_inv`, `AvoidingAgainToGettingOut_inv`, `GettingOutToFound_inv`, `GettingOutToWaiting_inv`, `GettingOutToGoing_inv`, `Idle_Waiting_inv`, `Idle_Going_inv`, `Idle_Avoiding_inv`, `Idle_TryingAgain_inv`, `Idle_GettingOut_inv`
```

**Fix directive**
No action — informational.

## Files to review
(none identified)

## Next step
Z-Machine deadlock-freedom (and any R-series lemmas) verified. Continue refinement in Phase 1; Isabelle is the slowest backend, so re-run only on milestones.
