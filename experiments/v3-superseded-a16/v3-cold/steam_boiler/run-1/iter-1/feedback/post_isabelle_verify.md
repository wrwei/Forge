# Isabelle Verification — FAILED

## Summary
Isabelle: exit 1, 1 theory marker(s) seen before failure.

## Run history
- New this run: 3
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: isabelle_parse_error — Theory will not parse

**Raw**
```
*** Outer syntax error (line 51 of "/Users/ranwei/Gitee/forge-v3c15-run1-steam_boiler/forge.transformations/output/isabelle/SteamBoilerController_Beh.thy"): command expected,
At: /Users/ranwei/Gitee/forge-v3c15-run1-steam_boiler/forge.transformations/output/isabelle/SteamBoilerController_Beh.thy:51
```

**Java trace**
  - Java: `SteamBoilerController_Beh.thy`:51

**Fix directive**
Isabelle's outer syntax parser rejected the generated .thy file. The line number above points at the offending construct. This is usually a template-side bug — see docs/fixes/I2_template_fork.md for known classes.

### Issue 2: isabelle_other — Isabelle error: but keyword open (line 51 of "/Users/ranwei/Gitee/forge-v3c15-run1-steam_boiler/

**Raw**
```
*** but keyword open (line 51 of "/Users/ranwei/Gitee/forge-v3c15-run1-steam_boiler/forge.transformations/output/isabelle/SteamBoilerController_Beh.thy") was found
At: /Users/ranwei/Gitee/forge-v3c15-run1-steam_boiler/forge.transformations/output/isabelle/SteamBoilerController_Beh.thy:51
```

**Java trace**
  - Java: `SteamBoilerController_Beh.thy`:51

**Fix directive**
See the verbatim message above. If it points at a specific lemma in the Z-Machine theory, treat it like a proof failure and inspect the originating Java element.

### Issue 3: isabelle_parse_error — Theory will not parse

**Raw**
```
*** At command "<malformed>" (line 51 of "/Users/ranwei/Gitee/forge-v3c15-run1-steam_boiler/forge.transformations/output/isabelle/SteamBoilerController_Beh.thy")
At: /Users/ranwei/Gitee/forge-v3c15-run1-steam_boiler/forge.transformations/output/isabelle/SteamBoilerController_Beh.thy:51
```

**Java trace**
  - Java: `SteamBoilerController_Beh.thy`:51

**Fix directive**
Isabelle's outer syntax parser rejected the generated .thy file. The line number above points at the offending construct. This is usually a template-side bug — see docs/fixes/I2_template_fork.md for known classes.

## Files to review
- BoilerMode.java
- OperatingMode.java
- SteamBoilerController.java

## Next step
Fix the issues above. If a specific lemma did not close, either change the Java code so the auto tactic can dispatch, or hand-write a proof script in the Z-Machine session theory.
