# Isabelle Verification — FAILED

## Summary
Isabelle: exit 1, 1 theory marker(s) seen before failure.

## Run history
- New this run: 1
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: isabelle_tactic_timeout — Tactic interrupt — cascade from timeout in SteamBoilerController_deadlock_free

**Raw**
```
*** Interrupt
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh.thy`:918 (SteamBoilerController_deadlock_free)

**Fix directive**
Isabelle reported an interrupt. This is usually the cascade of a preceding `*** Timeout` rather than an independent failure — address the timeout above and this should resolve.

## Files to review
- BoilerMode.java
- SteamBoilerController.java

## Next step
Fix the issues above. If a specific lemma did not close, either change the Java code so the auto tactic can dispatch, or hand-write a proof script in the Z-Machine session theory.
