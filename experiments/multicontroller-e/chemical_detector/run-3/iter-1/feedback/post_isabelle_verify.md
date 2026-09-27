# Isabelle Verification — FAILED

## Summary
Isabelle: exit 142, 2 theory marker(s) seen before failure.

## Run history
- New this run: 2
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: isabelle_tactic_timeout — Tactic timeout, suspected in MovementController_deadlock_free (deadlock_free — inferred from .thy)

**Raw**
```
*** Timeout
In theory: MovementController_Beh
```

**Java trace**
  - Java: `MovementController_Beh.thy`:523 (MovementController_deadlock_free)

**Fix directive**
Z-Machine `deadlock_free` proof timed out on `MovementController_deadlock_free`. (Isabelle's build output only provides theory-level granularity, so the lemma name is inferred from the .thy file.) See the raw Isabelle output above and the linked Java element.

### Issue 2: isabelle_tactic_timeout — Tactic interrupt — cascade from timeout in MovementController_deadlock_free

**Raw**
```
*** Interrupt
In theory: MovementController_Beh
```

**Java trace**
  - Java: `MovementController_Beh.thy`:523 (MovementController_deadlock_free)

**Fix directive**
Isabelle reported an interrupt. This is usually the cascade of a preceding `*** Timeout` rather than an independent failure — address the timeout above and this should resolve.

## Files to review
- Angle.java
- GasAnalysisController.java
- GasAnalysisMode.java
- Loc.java
- MovementMode.java
- ObstacleEvasion.java
- Status.java
- Vehicle.java

## Next step
Fix the issues above. If a specific lemma did not close, either change the Java code so the auto tactic can dispatch, or hand-write a proof script in the Z-Machine session theory.
