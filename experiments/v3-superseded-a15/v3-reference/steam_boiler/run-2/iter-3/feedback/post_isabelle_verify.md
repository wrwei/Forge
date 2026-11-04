# Isabelle Verification — FAILED

## Summary
Isabelle: exit 1, 1 theory marker(s) seen before failure.

## Run history
- New this run: 11
- Recurring from previous run: 17
- Resolved since previous run: 9

**Resolved issue titles**
- Isabelle error: *** Operator:  (∨) :: 𝔹 ⇒ 𝔹 ⇒ 𝔹
- Isabelle error: Operand:   transmissionFailed () :: ℝ
- Isabelle error: *** Coercion Inference:
- Isabelle error: *** Local coercion insertion on the operand failed:
- Isabelle error: assigned base type "ℝ" clashes with the upper bound of variable ??'a169
- Isabelle error: ??'a  <:  𝔹   from function application  (∨) (transmissionFailed ())
- Isabelle error: 𝔹  <:  𝔹   from function application  (∧) (unitsReady ())
- Isabelle error: transmissionFailed::unit ⇒ ??'a
- Isabelle error: At command "zoperation" (line 120 of "/Users/ranwei/Gitee/forge-v3r10-run2-steam

## Issues
### Issue 1: isabelle_other — Isabelle error: "ℝ" is not a subtype of "𝔹" [recurring x2]

**Raw**
```
*** "ℝ" is not a subtype of "𝔹"
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 2: isabelle_other — Isabelle error: *** Now trying to infer coercions globally. [recurring x2]

**Raw**
```
*** *** Now trying to infer coercions globally.
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 3: isabelle_other — Isabelle error: *** Coercion inference failed: [recurring x2]

**Raw**
```
*** *** Coercion inference failed:
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 4: isabelle_other — Isabelle error: assigned base type "ℝ" clashes with the upper bound of variable ??'a188

**Raw**
```
*** assigned base type "ℝ" clashes with the upper bound of variable ??'a188
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 5: isabelle_other — Isabelle error: *** Cannot fulfil subtype constraints: [recurring x2]

**Raw**
```
*** *** Cannot fulfil subtype constraints:
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 6: isabelle_other — Isabelle error: (unit ⇒ SteamBoilerController ⇒ 𝔹) [recurring x2]

**Raw**
```
*** (unit ⇒ SteamBoilerController ⇒ 𝔹)
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 7: isabelle_other — Isabelle error: ⇒ (unit ⇒ SteamBoilerController ⇒ SteamBoilerController) [recurring x2]

**Raw**
```
*** ⇒ (unit ⇒ SteamBoilerController ⇒ SteamBoilerController)
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 8: isabelle_other — Isabelle error: ⇒ (unit ⇒ SteamBoilerController ⇒ unit) [recurring x2]

**Raw**
```
*** ⇒ (unit ⇒ SteamBoilerController ⇒ unit)
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 9: isabelle_other — Isabelle error: ⇒ unit [recurring x2]

**Raw**
```
*** ⇒ unit
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 10: isabelle_other — Isabelle error: ⇒ SteamBoilerController ⇒ (??'a, unit × SteamBoilerController) itree [recurring x2]

**Raw**
```
*** ⇒ SteamBoilerController ⇒ (??'a, unit × SteamBoilerController) itree
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 11: isabelle_other — Isabelle error: <: [recurring x2]

**Raw**
```
*** <:
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 12: isabelle_other — Isabelle error: ⇒ (??'b ⇒ SteamBoilerController ⇒ ??'c) [recurring x2]

**Raw**
```
*** ⇒ (??'b ⇒ SteamBoilerController ⇒ ??'c)
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 13: isabelle_other — Isabelle error: ⇒ SteamBoilerController [recurring x2]

**Raw**
```
*** ⇒ SteamBoilerController
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 14: isabelle_other — Isabelle error: ⇒ (??'a, unit × SteamBoilerController) itree [recurring x2]

**Raw**
```
*** ⇒ (??'a, unit × SteamBoilerController) itree
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 15: isabelle_other — Isabelle error: from function application [recurring x2]

**Raw**
```
*** from function application
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 16: isabelle_other — Isabelle error: mk_zop::(unit ⇒ SteamBoilerController ⇒ 𝔹) [recurring x2]

**Raw**
```
*** mk_zop::(unit ⇒ SteamBoilerController ⇒ 𝔹)
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 17: isabelle_other — Isabelle error: ??'a  <:  𝔹   from function application  (∨) (operatorStopDemanded ())

**Raw**
```
*** ??'a  <:  𝔹   from function application  (∨) (operatorStopDemanded ())
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 18: isabelle_other — Isabelle error: 𝔹  <:  𝔹   from function application  (∧) (physicalUnitsReadyReceived ())

**Raw**
```
*** 𝔹  <:  𝔹   from function application  (∧) (physicalUnitsReadyReceived ())
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 19: isabelle_other — Isabelle error: 𝔹  <:  𝔹   from function application  ¬ programReadySent<𝗌'> [recurring x2]

**Raw**
```
*** 𝔹  <:  𝔹   from function application  ¬ programReadySent<𝗌'>
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 20: isabelle_other — Isabelle error: 𝔹  <:  𝔹   from function application  (∨) (plantWaiting<𝗌'>)

**Raw**
```
*** 𝔹  <:  𝔹   from function application  (∨) (plantWaiting<𝗌'>)
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 21: isabelle_other — Isabelle error: ??'a  <:  𝔹   from function application  (∨) (steamDetected ())

**Raw**
```
*** ??'a  <:  𝔹   from function application  (∨) (steamDetected ())
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 22: isabelle_other — Isabelle error: 𝔹  <:  𝔹   from function application  ¬ levelDeviceSound ()

**Raw**
```
*** 𝔹  <:  𝔹   from function application  ¬ levelDeviceSound ()
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 23: isabelle_other — Isabelle error: ??'a  <:  𝔹   from function application

**Raw**
```
*** ??'a  <:  𝔹   from function application
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 24: isabelle_other — Isabelle error: plantWaiting<𝗌'> ∨ steamBoilerWaitingReceived ()

**Raw**
```
*** plantWaiting<𝗌'> ∨ steamBoilerWaitingReceived ()
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 25: isabelle_other — Isabelle error: operatorStopDemanded () ∨ transmissionFailure ()

**Raw**
```
*** operatorStopDemanded () ∨ transmissionFailure ()
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 26: isabelle_other — Isabelle error: unit ⇒ ℝ  <:  unit ⇒ ??'a   from function application [recurring x2]

**Raw**
```
*** unit ⇒ ℝ  <:  unit ⇒ ??'a   from function application
At: /Users/ranwei/Gitee/forge-v3r10-run2-steam_boiler/forge.transformations/output/isabelle/SteamBoilerController_Beh.thy:110
```

**Java trace**
  - Java: `SteamBoilerController_Beh.thy`:110

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 27: isabelle_other — Isabelle error: operatorStopDemanded::unit ⇒ ??'a

**Raw**
```
*** operatorStopDemanded::unit ⇒ ??'a
At: /Users/ranwei/Gitee/forge-v3r10-run2-steam_boiler/forge.transformations/output/isabelle/SteamBoilerController_Beh.thy:110
```

**Java trace**
  - Java: `SteamBoilerController_Beh.thy`:110

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 28: isabelle_other — Isabelle error: At command "zoperation" (line 110 of "/Users/ranwei/Gitee/forge-v3r10-run2-steam

**Raw**
```
*** At command "zoperation" (line 110 of "/Users/ranwei/Gitee/forge-v3r10-run2-steam_boiler/forge.transformations/output/isabelle/SteamBoilerController_Beh.thy")
At: /Users/ranwei/Gitee/forge-v3r10-run2-steam_boiler/forge.transformations/output/isabelle/SteamBoilerController_Beh.thy:110
```

**Java trace**
  - Java: `SteamBoilerController_Beh.thy`:110

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

## Files to review
- BoilerMode.java
- OperatingMode.java
- PumpId.java
- SteamBoilerController.java

## Next step
Fix the issues above. If a specific lemma did not close, either change the Java code so the auto tactic can dispatch, or hand-write a proof script in the Z-Machine session theory.
