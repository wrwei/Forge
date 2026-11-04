# Isabelle Verification — FAILED

## Summary
Isabelle: exit 1, 1 theory marker(s) seen before failure.

## Run history
- New this run: 2
- Recurring from previous run: 0
- Resolved since previous run: 28

**Resolved issue titles**
- Isabelle error: "ℝ" is not a subtype of "𝔹"
- Isabelle error: *** Now trying to infer coercions globally.
- Isabelle error: *** Coercion inference failed:
- Isabelle error: assigned base type "ℝ" clashes with the upper bound of variable ??'a190
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
- Isabelle error: 𝔹  <:  𝔹   from function application  (∨) (haltDemanded ())
- Isabelle error: 𝔹  <:  𝔹   from function application
- Isabelle error: (haltDemanded () ∨ steamBoilerWaitingReceived ()) ∨
- Isabelle error: physicalUnitsReadyReceived ()
- Isabelle error: ??'a  <:  𝔹   from function application
- Isabelle error: ((haltDemanded () ∨ steamBoilerWaitingReceived ()) ∨
- Isabelle error: physicalUnitsReadyReceived ()) ∨
- Isabelle error: levelMayLeaveSafeRange ()
- Isabelle error: haltDemanded () ∨ steamBoilerWaitingReceived ()
- Isabelle error: unit ⇒ ℝ  <:  unit ⇒ ??'a   from function application
- Isabelle error: levelMayLeaveSafeRange::unit ⇒ ??'a
- Isabelle error: At command "zoperation" (line 228 of "/Users/ranwei/Gitee/forge-v3r10-run2-steam

## Issues
### Issue 1: isabelle_other — Isabelle error: exception Empty raised (line 47 of "./basis/List.sml")

**Raw**
```
*** exception Empty raised (line 47 of "./basis/List.sml")
At: ./basis/List.sml:47
```

**Java trace**
  - Java: `List.sml`:47

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 2: isabelle_other — Isabelle error: At command "zoperation" (line 116 of "/Users/ranwei/Gitee/forge-v3r10-run2-steam

**Raw**
```
*** At command "zoperation" (line 116 of "/Users/ranwei/Gitee/forge-v3r10-run2-steam_boiler/forge.transformations/output/isabelle/SteamBoilerController_Beh.thy")
At: /Users/ranwei/Gitee/forge-v3r10-run2-steam_boiler/forge.transformations/output/isabelle/SteamBoilerController_Beh.thy:116
```

**Java trace**
  - Java: `SteamBoilerController_Beh.thy`:116

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

## Files to review
- BoilerMode.java
- OperatingMode.java
- PumpId.java
- SteamBoilerController.java

## Next step
Fix the issues above. If a specific lemma did not close, either change the Java code so the auto tactic can dispatch, or hand-write a proof script in the Z-Machine session theory.
