# Isabelle Verification — FAILED

## Summary
Isabelle: exit 1, 1 theory marker(s) seen before failure.

## Run history
- New this run: 26
- Recurring from previous run: 0
- Resolved since previous run: 14

**Resolved issue titles**
- Isabelle error: OperatingMode.NORMAL::??'a
- Isabelle error: OperatingMode  <:  ??'a   from function application
- Isabelle error: OperatingMode.DEGRADED::??'a
- Isabelle error: OperatingMode.RESCUE::??'a
- Isabelle error: OperatingMode.EMERGENCY_STOP::??'a
- Isabelle error: OperatingMode.INITIALIZATION::??'a
- Isabelle error: ??'a  <:  ??'b   from function application
- Isabelle error: st = OperatingMode.INITIALIZATION
- Isabelle error: ??'a  <:  St   from function application  st::St
- Isabelle error: St  <:  ??'a   from function application  st::??'a
- Isabelle error: St  <:  ??'a   from function application  initial::??'a
- Isabelle error: St  <:  ??'a   from function application  st::??'a
- Isabelle error: ??'a  <:  ??'b   from function application  (=) st
- Isabelle error: At command "zstore" (line 72 of "/Users/ranwei/Gitee/forge-v3r10-run2-steam_boil

## Issues
### Issue 1: isabelle_other — Isabelle error: *** Operator:  (∨) :: 𝔹 ⇒ 𝔹 ⇒ 𝔹

**Raw**
```
*** *** Operator:  (∨) :: 𝔹 ⇒ 𝔹 ⇒ 𝔹
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 2: isabelle_other — Isabelle error: Operand:   transmissionFailed () :: ℝ

**Raw**
```
*** Operand:   transmissionFailed () :: ℝ
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 3: isabelle_other — Isabelle error: *** Coercion Inference:

**Raw**
```
*** *** Coercion Inference:
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 4: isabelle_other — Isabelle error: *** Local coercion insertion on the operand failed:

**Raw**
```
*** *** Local coercion insertion on the operand failed:
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 5: isabelle_other — Isabelle error: "ℝ" is not a subtype of "𝔹"

**Raw**
```
*** "ℝ" is not a subtype of "𝔹"
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 6: isabelle_other — Isabelle error: *** Now trying to infer coercions globally.

**Raw**
```
*** *** Now trying to infer coercions globally.
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 7: isabelle_other — Isabelle error: *** Coercion inference failed:

**Raw**
```
*** *** Coercion inference failed:
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 8: isabelle_other — Isabelle error: assigned base type "ℝ" clashes with the upper bound of variable ??'a169

**Raw**
```
*** assigned base type "ℝ" clashes with the upper bound of variable ??'a169
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 9: isabelle_other — Isabelle error: *** Cannot fulfil subtype constraints:

**Raw**
```
*** *** Cannot fulfil subtype constraints:
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 10: isabelle_other — Isabelle error: (unit ⇒ SteamBoilerController ⇒ 𝔹)

**Raw**
```
*** (unit ⇒ SteamBoilerController ⇒ 𝔹)
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 11: isabelle_other — Isabelle error: ⇒ (unit ⇒ SteamBoilerController ⇒ SteamBoilerController)

**Raw**
```
*** ⇒ (unit ⇒ SteamBoilerController ⇒ SteamBoilerController)
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 12: isabelle_other — Isabelle error: ⇒ (unit ⇒ SteamBoilerController ⇒ unit)

**Raw**
```
*** ⇒ (unit ⇒ SteamBoilerController ⇒ unit)
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 13: isabelle_other — Isabelle error: ⇒ unit

**Raw**
```
*** ⇒ unit
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 14: isabelle_other — Isabelle error: ⇒ SteamBoilerController ⇒ (??'a, unit × SteamBoilerController) itree

**Raw**
```
*** ⇒ SteamBoilerController ⇒ (??'a, unit × SteamBoilerController) itree
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 15: isabelle_other — Isabelle error: <:

**Raw**
```
*** <:
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 16: isabelle_other — Isabelle error: ⇒ (??'b ⇒ SteamBoilerController ⇒ ??'c)

**Raw**
```
*** ⇒ (??'b ⇒ SteamBoilerController ⇒ ??'c)
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 17: isabelle_other — Isabelle error: ⇒ SteamBoilerController

**Raw**
```
*** ⇒ SteamBoilerController
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 18: isabelle_other — Isabelle error: ⇒ (??'a, unit × SteamBoilerController) itree

**Raw**
```
*** ⇒ (??'a, unit × SteamBoilerController) itree
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 19: isabelle_other — Isabelle error: from function application

**Raw**
```
*** from function application
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 20: isabelle_other — Isabelle error: mk_zop::(unit ⇒ SteamBoilerController ⇒ 𝔹)

**Raw**
```
*** mk_zop::(unit ⇒ SteamBoilerController ⇒ 𝔹)
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 21: isabelle_other — Isabelle error: ??'a  <:  𝔹   from function application  (∨) (transmissionFailed ())

**Raw**
```
*** ??'a  <:  𝔹   from function application  (∨) (transmissionFailed ())
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 22: isabelle_other — Isabelle error: 𝔹  <:  𝔹   from function application  ¬ programReadySent<𝗌'>

**Raw**
```
*** 𝔹  <:  𝔹   from function application  ¬ programReadySent<𝗌'>
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 23: isabelle_other — Isabelle error: 𝔹  <:  𝔹   from function application  (∧) (unitsReady ())

**Raw**
```
*** 𝔹  <:  𝔹   from function application  (∧) (unitsReady ())
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 24: isabelle_other — Isabelle error: unit ⇒ ℝ  <:  unit ⇒ ??'a   from function application

**Raw**
```
*** unit ⇒ ℝ  <:  unit ⇒ ??'a   from function application
At: /Users/ranwei/Gitee/forge-v3r10-run2-steam_boiler/forge.transformations/output/isabelle/SteamBoilerController_Beh.thy:120
```

**Java trace**
  - Java: `SteamBoilerController_Beh.thy`:120

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 25: isabelle_other — Isabelle error: transmissionFailed::unit ⇒ ??'a

**Raw**
```
*** transmissionFailed::unit ⇒ ??'a
At: /Users/ranwei/Gitee/forge-v3r10-run2-steam_boiler/forge.transformations/output/isabelle/SteamBoilerController_Beh.thy:120
```

**Java trace**
  - Java: `SteamBoilerController_Beh.thy`:120

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 26: isabelle_other — Isabelle error: At command "zoperation" (line 120 of "/Users/ranwei/Gitee/forge-v3r10-run2-steam

**Raw**
```
*** At command "zoperation" (line 120 of "/Users/ranwei/Gitee/forge-v3r10-run2-steam_boiler/forge.transformations/output/isabelle/SteamBoilerController_Beh.thy")
At: /Users/ranwei/Gitee/forge-v3r10-run2-steam_boiler/forge.transformations/output/isabelle/SteamBoilerController_Beh.thy:120
```

**Java trace**
  - Java: `SteamBoilerController_Beh.thy`:120

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

## Files to review
- BoilerMode.java
- OperatingMode.java
- PumpId.java
- SteamBoilerController.java

## Next step
Fix the issues above. If a specific lemma did not close, either change the Java code so the auto tactic can dispatch, or hand-write a proof script in the Z-Machine session theory.
