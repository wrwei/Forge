# Isabelle Verification — FAILED

## Summary
Isabelle: exit 142, 1 theory marker(s) seen before failure.

## Run history
- New this run: 2
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: isabelle_tactic_timeout — Tactic timeout, suspected in GasAnalysisController_deadlock_free (deadlock_free — inferred from .thy)

**Raw**
```
*** Timeout
In theory: GasAnalysisController_Beh
```

**Java trace**
  - Java: `GasAnalysisController_Beh.thy`:236 (GasAnalysisController_deadlock_free)

**Fix directive**
Z-Machine `deadlock_free` proof timed out — most likely on the `by (metis St.exhaust_disc)` step of `GasAnalysisController_deadlock_free`. (Isabelle's build output only provides theory-level granularity, so the lemma name is inferred from the .thy file.) That tactic requires every state in the `St` enum to have at least one bare-precondition operation. If any mode block in the controller has only guarded transitions, the residual disjunction leaves a `st = X ∧ <guard>` case that `St.exhaust_disc` cannot match, so `metis` searches indefinitely.

Fix: inspect every mode block in the controller's step() method. Confirm at least one inner branch is either (a) an event with no extra guard (e.g. `if (event instanceof X)`), or (b) the explicit `else { /* stay */ }` fallback. See docs/fixes/I1_deadlock_free_proof.md for the proof tactic rationale and residual-goal shape.

### Issue 2: isabelle_tactic_timeout — Tactic interrupt — cascade from timeout in GasAnalysisController_deadlock_free

**Raw**
```
*** Interrupt
In theory: GasAnalysisController_Beh
```

**Java trace**
  - Java: `GasAnalysisController_Beh.thy`:236 (GasAnalysisController_deadlock_free)

**Fix directive**
Isabelle reported an interrupt. This is usually the cascade of a preceding `*** Timeout` rather than an independent failure — address the timeout above and this should resolve.

## Files to review
- Angle.java
- Chem.java
- GasAnalysisMode.java
- Loc.java
- MovementController.java
- MovementMode.java
- SignalBus.java
- Status.java
- Vehicle.java

## Next step
Fix the issues above. If a specific lemma did not close, either change the Java code so the auto tactic can dispatch, or hand-write a proof script in the Z-Machine session theory.
