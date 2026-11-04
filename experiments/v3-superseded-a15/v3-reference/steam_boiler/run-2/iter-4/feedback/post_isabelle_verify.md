# Isabelle Verification — FAILED

## Summary
Isabelle: exit 1, 1 theory marker(s) seen before failure.

## Run history
**WARNING — thrashing detected.** 13 issue(s) have recurred 3+ runs in a row despite fixes: `Isabelle error: *** Cannot fulfil subtype constraints:`, `Isabelle error: (unit ⇒ SteamBoilerController ⇒ 𝔹)`, `Isabelle error: ⇒ (unit ⇒ SteamBoilerController ⇒ SteamBoilerController)`, `Isabelle error: ⇒ (unit ⇒ SteamBoilerController ⇒ unit)`, `Isabelle error: ⇒ unit`, `Isabelle error: ⇒ SteamBoilerController ⇒ (??'a, unit × SteamBoilerController) itree`, `Isabelle error: <:`, `Isabelle error: ⇒ (??'b ⇒ SteamBoilerController ⇒ ??'c)`, `Isabelle error: ⇒ SteamBoilerController`, `Isabelle error: ⇒ (??'a, unit × SteamBoilerController) itree`, `Isabelle error: from function application`, `Isabelle error: mk_zop::(unit ⇒ SteamBoilerController ⇒ 𝔹)`, `Isabelle error: unit ⇒ ℝ  <:  unit ⇒ ??'a   from function application`. Previous fix strategies are not working — change approach (e.g. re-read the originating requirement, or escalate to the user).

- New this run: 13
- Recurring from previous run: 15
- Resolved since previous run: 13

**Resolved issue titles**
- Isabelle error: "ℝ" is not a subtype of "𝔹"
- Isabelle error: *** Now trying to infer coercions globally.
- Isabelle error: *** Coercion inference failed:
- Isabelle error: assigned base type "ℝ" clashes with the upper bound of variable ??'a188
- Isabelle error: ??'a  <:  𝔹   from function application  (∨) (operatorStopDemanded ())
- Isabelle error: 𝔹  <:  𝔹   from function application  (∧) (physicalUnitsReadyReceived ())
- Isabelle error: 𝔹  <:  𝔹   from function application  ¬ programReadySent<𝗌'>
- Isabelle error: 𝔹  <:  𝔹   from function application  (∨) (plantWaiting<𝗌'>)
- Isabelle error: ??'a  <:  𝔹   from function application  (∨) (steamDetected ())
- Isabelle error: 𝔹  <:  𝔹   from function application  ¬ levelDeviceSound ()
- Isabelle error: plantWaiting<𝗌'> ∨ steamBoilerWaitingReceived ()
- Isabelle error: operatorStopDemanded::unit ⇒ ??'a
- Isabelle error: At command "zoperation" (line 110 of "/Users/ranwei/Gitee/forge-v3r10-run2-steam

## Issues
### Issue 1: isabelle_other — Isabelle error: assigned base type "ℝ" clashes with the upper bound of variable ??'a201

**Raw**
```
*** assigned base type "ℝ" clashes with the upper bound of variable ??'a201
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 2: isabelle_other — Isabelle error: *** Cannot fulfil subtype constraints: [RECURRING x3 — fix strategy failing]

**Raw**
```
*** *** Cannot fulfil subtype constraints:
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 3: isabelle_other — Isabelle error: (unit ⇒ SteamBoilerController ⇒ 𝔹) [RECURRING x3 — fix strategy failing]

**Raw**
```
*** (unit ⇒ SteamBoilerController ⇒ 𝔹)
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 4: isabelle_other — Isabelle error: ⇒ (unit ⇒ SteamBoilerController ⇒ SteamBoilerController) [RECURRING x3 — fix strategy failing]

**Raw**
```
*** ⇒ (unit ⇒ SteamBoilerController ⇒ SteamBoilerController)
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 5: isabelle_other — Isabelle error: ⇒ (unit ⇒ SteamBoilerController ⇒ unit) [RECURRING x3 — fix strategy failing]

**Raw**
```
*** ⇒ (unit ⇒ SteamBoilerController ⇒ unit)
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 6: isabelle_other — Isabelle error: ⇒ unit [RECURRING x3 — fix strategy failing]

**Raw**
```
*** ⇒ unit
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 7: isabelle_other — Isabelle error: ⇒ SteamBoilerController ⇒ (??'a, unit × SteamBoilerController) itree [RECURRING x3 — fix strategy failing]

**Raw**
```
*** ⇒ SteamBoilerController ⇒ (??'a, unit × SteamBoilerController) itree
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 8: isabelle_other — Isabelle error: <: [RECURRING x3 — fix strategy failing]

**Raw**
```
*** <:
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 9: isabelle_other — Isabelle error: ⇒ (??'b ⇒ SteamBoilerController ⇒ ??'c) [RECURRING x3 — fix strategy failing]

**Raw**
```
*** ⇒ (??'b ⇒ SteamBoilerController ⇒ ??'c)
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 10: isabelle_other — Isabelle error: ⇒ SteamBoilerController [RECURRING x3 — fix strategy failing]

**Raw**
```
*** ⇒ SteamBoilerController
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 11: isabelle_other — Isabelle error: ⇒ (??'a, unit × SteamBoilerController) itree [RECURRING x3 — fix strategy failing]

**Raw**
```
*** ⇒ (??'a, unit × SteamBoilerController) itree
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 12: isabelle_other — Isabelle error: from function application [RECURRING x3 — fix strategy failing]

**Raw**
```
*** from function application
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 13: isabelle_other — Isabelle error: mk_zop::(unit ⇒ SteamBoilerController ⇒ 𝔹) [RECURRING x3 — fix strategy failing]

**Raw**
```
*** mk_zop::(unit ⇒ SteamBoilerController ⇒ 𝔹)
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 14: isabelle_other — Isabelle error: 𝔹  <:  𝔹   from function application  (∨) (operatorStopDemanded ())

**Raw**
```
*** 𝔹  <:  𝔹   from function application  (∨) (operatorStopDemanded ())
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 15: isabelle_other — Isabelle error: 𝔹  <:  𝔹   from function application

**Raw**
```
*** 𝔹  <:  𝔹   from function application
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 16: isabelle_other — Isabelle error: (operatorStopDemanded () ∨ transmissionFailure ()) ∨

**Raw**
```
*** (operatorStopDemanded () ∨ transmissionFailure ()) ∨
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 17: isabelle_other — Isabelle error: steamBoilerWaitingReceived ()

**Raw**
```
*** steamBoilerWaitingReceived ()
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 18: isabelle_other — Isabelle error: ??'a  <:  𝔹   from function application [recurring x2]

**Raw**
```
*** ??'a  <:  𝔹   from function application
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 19: isabelle_other — Isabelle error: (((operatorStopDemanded () ∨ transmissionFailure ()) ∨

**Raw**
```
*** (((operatorStopDemanded () ∨ transmissionFailure ()) ∨
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 20: isabelle_other — Isabelle error: steamBoilerWaitingReceived ()) ∨

**Raw**
```
*** steamBoilerWaitingReceived ()) ∨
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 21: isabelle_other — Isabelle error: physicalUnitsReadyReceived ()) ∨

**Raw**
```
*** physicalUnitsReadyReceived ()) ∨
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 22: isabelle_other — Isabelle error: levelLimitAtRisk ()

**Raw**
```
*** levelLimitAtRisk ()
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 23: isabelle_other — Isabelle error: ((operatorStopDemanded () ∨ transmissionFailure ()) ∨

**Raw**
```
*** ((operatorStopDemanded () ∨ transmissionFailure ()) ∨
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 24: isabelle_other — Isabelle error: physicalUnitsReadyReceived ()

**Raw**
```
*** physicalUnitsReadyReceived ()
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 25: isabelle_other — Isabelle error: operatorStopDemanded () ∨ transmissionFailure () [recurring x2]

**Raw**
```
*** operatorStopDemanded () ∨ transmissionFailure ()
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 26: isabelle_other — Isabelle error: unit ⇒ ℝ  <:  unit ⇒ ??'a   from function application [RECURRING x3 — fix strategy failing]

**Raw**
```
*** unit ⇒ ℝ  <:  unit ⇒ ??'a   from function application
At: /Users/ranwei/Gitee/forge-v3r10-run2-steam_boiler/forge.transformations/output/isabelle/SteamBoilerController_Beh.thy:230
```

**Java trace**
  - Java: `SteamBoilerController_Beh.thy`:230

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 27: isabelle_other — Isabelle error: levelLimitAtRisk::unit ⇒ ??'a

**Raw**
```
*** levelLimitAtRisk::unit ⇒ ??'a
At: /Users/ranwei/Gitee/forge-v3r10-run2-steam_boiler/forge.transformations/output/isabelle/SteamBoilerController_Beh.thy:230
```

**Java trace**
  - Java: `SteamBoilerController_Beh.thy`:230

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 28: isabelle_other — Isabelle error: At command "zoperation" (line 230 of "/Users/ranwei/Gitee/forge-v3r10-run2-steam

**Raw**
```
*** At command "zoperation" (line 230 of "/Users/ranwei/Gitee/forge-v3r10-run2-steam_boiler/forge.transformations/output/isabelle/SteamBoilerController_Beh.thy")
At: /Users/ranwei/Gitee/forge-v3r10-run2-steam_boiler/forge.transformations/output/isabelle/SteamBoilerController_Beh.thy:230
```

**Java trace**
  - Java: `SteamBoilerController_Beh.thy`:230

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

## Files to review
- BoilerMode.java
- OperatingMode.java
- PumpId.java
- SteamBoilerController.java

## Next step
Fix the issues above. If a specific lemma did not close, either change the Java code so the auto tactic can dispatch, or hand-write a proof script in the Z-Machine session theory.
