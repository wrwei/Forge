# Isabelle Verification — FAILED

## Summary
Isabelle: exit 1, 1 theory marker(s) seen before failure.

## Run history
**WARNING — thrashing detected.** 1 issue(s) have recurred 3+ runs in a row despite fixes: `Tactic interrupt — cascade from timeout in SteamBoilerController_deadlock_free`. Previous fix strategies are not working — change approach (e.g. re-read the originating requirement, or escalate to the user).

- New this run: 0
- Recurring from previous run: 1
- Resolved since previous run: 0

## Issues
### Issue 1: isabelle_tactic_timeout — Tactic interrupt — cascade from timeout in SteamBoilerController_deadlock_free [RECURRING x3 — fix strategy failing]

**Raw**
```
*** Interrupt
In theory: SteamBoilerController_Beh
```

**Java trace**
  - Java: `SteamBoilerController_Beh.thy`:657 (SteamBoilerController_deadlock_free)

**Fix directive**
Isabelle reported an interrupt. This is usually the cascade of a preceding `*** Timeout` rather than an independent failure — address the timeout above and this should resolve.

## Files to review
- BoilerMode.java
- OperatingMode.java
- PumpId.java
- PumpSwitch.java
- SteamBoilerController.java
- ValveSetting.java

## Next step
Fix the issues above. If a specific lemma did not close, either change the Java code so the auto tactic can dispatch, or hand-write a proof script in the Z-Machine session theory.
