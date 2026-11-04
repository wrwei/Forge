# Isabelle Verification — FAILED

## Summary
Isabelle: exit 1, 1 theory marker(s) seen before failure.

## Run history
- New this run: 9
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: isabelle_other — Isabelle error: Inner syntax error (line 327 of "/Users/ranwei/Gitee/forge-v3c16-run4-steam_boil

**Raw**
```
*** Inner syntax error (line 327 of "/Users/ranwei/Gitee/forge-v3c16-run4-steam_boiler/forge.transformations/output/isabelle/SteamBoilerController_Beh.thy")
At: /Users/ranwei/Gitee/forge-v3c16-run4-steam_boiler/forge.transformations/output/isabelle/SteamBoilerController_Beh.thy:327
```

**Java trace**
  - Java: `SteamBoilerController_Beh.thy`:327

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 2: isabelle_other — Isabelle error: at ", levelHigh ′ = , steamReading ′ = steamValue () , pumpThroughput ′ =

**Raw**
```
*** at ", levelHigh ′ = , steamReading ′ = steamValue () , pumpThroughput ′ =
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 3: isabelle_other — Isabelle error: flowingThroughput () , tr ′ = tr @ [ Event transmission ] @ [ Event

**Raw**
```
*** flowingThroughput () , tr ′ = tr @ [ Event transmission ] @ [ Event
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 4: isabelle_other — Isabelle error: modeAnnouncement ] @ [ State RESCUE ] , listens ′ = { transmission ,

**Raw**
```
*** modeAnnouncement ] @ [ State RESCUE ] , listens ′ = { transmission ,
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 5: isabelle_other — Isabelle error: transmission , transmission , transmission , transmission ,

**Raw**
```
*** transmission , transmission , transmission , transmission ,
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 6: isabelle_other — Isabelle error: transmission , transmission , transmission , transmission } , offered ′

**Raw**
```
*** transmission , transmission , transmission , transmission } , offered ′
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh`

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 7: isabelle_other — Isabelle error: = nxt ]"

**Raw**
```
*** = nxt ]"
At: /Users/ranwei/Gitee/forge-v3c16-run4-steam_boiler/forge.transformations/output/isabelle/SteamBoilerController_Beh.thy:321
```

**Java trace**
  - Java: `SteamBoilerController_Beh.thy`:321

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 8: isabelle_other — Isabelle error: Failed to parse term

**Raw**
```
*** Failed to parse term
At: /Users/ranwei/Gitee/forge-v3c16-run4-steam_boiler/forge.transformations/output/isabelle/SteamBoilerController_Beh.thy:321
```

**Java trace**
  - Java: `SteamBoilerController_Beh.thy`:321

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 9: isabelle_other — Isabelle error: At command "zoperation" (line 321 of "/Users/ranwei/Gitee/forge-v3c16-run4-steam

**Raw**
```
*** At command "zoperation" (line 321 of "/Users/ranwei/Gitee/forge-v3c16-run4-steam_boiler/forge.transformations/output/isabelle/SteamBoilerController_Beh.thy")
At: /Users/ranwei/Gitee/forge-v3c16-run4-steam_boiler/forge.transformations/output/isabelle/SteamBoilerController_Beh.thy:321
```

**Java trace**
  - Java: `SteamBoilerController_Beh.thy`:321

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

## Files to review
- ModeValue.java
- OperatingMode.java
- SteamBoilerController.java
- UnitStatus.java

## Next step
Fix the issues above. If a specific lemma did not close, either change the Java code so the auto tactic can dispatch, or hand-write a proof script in the Z-Machine session theory.
