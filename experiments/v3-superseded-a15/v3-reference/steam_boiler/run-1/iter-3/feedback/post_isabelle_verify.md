# Isabelle Verification — FAILED

## Summary
Isabelle: exit 1, 1 theory marker(s) seen before failure.

## Run history
- New this run: 1
- Recurring from previous run: 0
- Resolved since previous run: 27

**Resolved issue titles**
- Isabelle error: Operator:  (∨) :: 𝔹 ⇒ 𝔹 ⇒ 𝔹
- Isabelle error: Operand:   waitingSignalNow () :: ℝ
- Isabelle error: *** Coercion Inference:
- Isabelle error: *** Local coercion insertion on the operand failed:
- Isabelle error: "ℝ" is not a subtype of "𝔹"
- Isabelle error: *** Now trying to infer coercions globally.
- Isabelle error: *** Coercion inference failed:
- Isabelle error: assigned base type "ℝ" clashes with the upper bound of variable ??'a182
- Isabelle error: *** Cannot fulfil subtype constraints:
- Isabelle error: (unit ⇒ SteamBoilerController ⇒ 𝔹)
- Isabelle error: ⇒ (unit ⇒ SteamBoilerController ⇒ SteamBoilerController)
- Isabelle error: ⇒ (unit ⇒ SteamBoilerController ⇒ unit)
- Isabelle error: ⇒ unit
- Isabelle error: ⇒ SteamBoilerController ⇒ (??'a, unit × SteamBoilerController) itree
- Isabelle error: <:
- Isabelle error: ⇒ (??'b ⇒ SteamBoilerController ⇒ ??'c)
- Isabelle error: ⇒ SteamBoilerController
- Isabelle error: ⇒ (??'a, unit × SteamBoilerController) itree
- Isabelle error: from function application
- Isabelle error: mk_zop::(unit ⇒ SteamBoilerController ⇒ 𝔹)
- Isabelle error: 𝔹  <:  𝔹   from function application  ¬ operatorStopRequested ()
- Isabelle error: ??'a  <:  𝔹   from function application  (∨) (waitingSignalNow ())
- Isabelle error: 𝔹  <:  𝔹   from function application  waitingSignalNow () ∨ unitsReady ()
- Isabelle error: 𝔹  <:  𝔹   from function application  (∨) (transmissionLost ())
- Isabelle error: unit ⇒ ℝ  <:  unit ⇒ ??'a   from function application
- Isabelle error: waitingSignalNow::unit ⇒ ??'a
- Isabelle error: At command "zoperation" (line 236 of "/Users/ranwei/Gitee/forge-v3r10-run1-steam

## Issues
### Issue 1: isabelle_tactic_timeout — Tactic interrupt — cascade from timeout in SteamBoilerController_deadlock_free

**Raw**
```
*** Interrupt
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh.thy`:755 (SteamBoilerController_deadlock_free)

**Fix directive**
Isabelle reported an interrupt. This is usually the cascade of a preceding `*** Timeout` rather than an independent failure — address the timeout above and this should resolve.

## Files to review
- BoilerActuator.java
- FlowObservation.java
- ModeValue.java
- OperatingMode.java
- SteamBoilerController.java
- ValvePosition.java

## Next step
Fix the issues above. If a specific lemma did not close, either change the Java code so the auto tactic can dispatch, or hand-write a proof script in the Z-Machine session theory.
