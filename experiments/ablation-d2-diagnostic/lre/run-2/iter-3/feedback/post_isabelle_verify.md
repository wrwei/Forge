# Isabelle Verification — FAILED

## Summary
Isabelle: exit 142, 1 theory marker(s) seen before failure.

## Run history
- New this run: 0
- Recurring from previous run: 2
- Resolved since previous run: 0

## Issues
### Issue 1: isabelle_tactic_timeout — Tactic timeout, suspected in LreController_deadlock_free (deadlock_free — inferred from .thy) [recurring x2]

**Raw**
```
*** Timeout
In theory: LreController_Beh
```

**Java trace**
  - Java: `LreController_Beh.thy`:626 (LreController_deadlock_free)

**Fix directive**
Z-Machine `deadlock_free` proof timed out on `LreController_deadlock_free`. (Isabelle's build output only provides theory-level granularity, so the lemma name is inferred from the .thy file.) See the raw Isabelle output above and the linked Java element.

### Issue 2: isabelle_tactic_timeout — Tactic interrupt — cascade from timeout in LreController_deadlock_free [recurring x2]

**Raw**
```
*** Interrupt
In theory: LreController_Beh
```

**Java trace**
  - Java: `LreController_Beh.thy`:626 (LreController_deadlock_free)

**Fix directive**
Isabelle reported an interrupt. This is usually the cascade of a preceding `*** Timeout` rather than an independent failure — address the timeout above and this should resolve.

## Files to review
- CalcCDyn.java
- CalcCPA.java
- CalcCStc.java
- CalcVel.java
- CheckOPEZ.java
- LreController.java
- LreMode.java

## Next step
Fix the issues above. If a specific lemma did not close, either change the Java code so the auto tactic can dispatch, or hand-write a proof script in the Z-Machine session theory.
