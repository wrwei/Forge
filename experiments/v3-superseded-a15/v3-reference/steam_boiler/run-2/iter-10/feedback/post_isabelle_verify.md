# Isabelle Verification — FAILED

## Summary
Isabelle: exit 1, 1 theory marker(s) seen before failure.

## Run history
- New this run: 1
- Recurring from previous run: 1
- Resolved since previous run: 1

**Resolved issue titles**
- Isabelle error: At command "zoperation" (line 116 of "/Users/ranwei/Gitee/forge-v3r10-run2-steam

## Issues
### Issue 1: isabelle_other — Isabelle error: exception Empty raised (line 47 of "./basis/List.sml") [recurring x2]

**Raw**
```
*** exception Empty raised (line 47 of "./basis/List.sml")
At: ./basis/List.sml:47
```

**Java trace**
  - Java: `List.sml`:47

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 2: isabelle_other — Isabelle error: At command "zoperation" (line 117 of "/Users/ranwei/Gitee/forge-v3r10-run2-steam

**Raw**
```
*** At command "zoperation" (line 117 of "/Users/ranwei/Gitee/forge-v3r10-run2-steam_boiler/forge.transformations/output/isabelle/SteamBoilerController_Beh.thy")
At: /Users/ranwei/Gitee/forge-v3r10-run2-steam_boiler/forge.transformations/output/isabelle/SteamBoilerController_Beh.thy:117
```

**Java trace**
  - Java: `SteamBoilerController_Beh.thy`:117

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

## Files to review
- BoilerMode.java
- OperatingMode.java
- PumpId.java
- PumpSwitch.java
- SteamBoilerController.java

## Next step
Fix the issues above. If a specific lemma did not close, either change the Java code so the auto tactic can dispatch, or hand-write a proof script in the Z-Machine session theory.
