# 6b — Formal Verification (FDR4) — FAILED

## Summary
FDR4: 3 assertion(s) checked, 1 passed, 2 failed.

## Run history
- New this run: 2
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: deadlock — Deadlock-freedom violated

**Raw**
```
P_SRangerController :[deadlock free]
Counterexample: SRangerController::move.out.1 -> SRangerController::endTask.in -> SRangerController::move.out.0
```

**Fix directive**
Trace: event `SRangerController::move.out.1` fires; then event `SRangerController::endTask.in` fires; then event `SRangerController::move.out.0` fires.

The counterexample ends with event `SRangerController::move.out.0`. No transition in the RoboChart model handles `SRangerController::move.out.0` — some mode reaches a state where this event occurs without a matching branch. Add an `instanceof` check for the event type in the corresponding mode block of step().

One or more states have no reachable outgoing transition. Every mode in the controller's step() method must have at least one reachable branch out.

### Issue 2: deadlock — Deadlock-freedom violated

**Raw**
```
P_SRangerController ; RUN({r__}) :[deadlock free]
Counterexample: SRangerController::move.out.1 -> SRangerController::endTask.in -> SRangerController::move.out.0
```

**Fix directive**
Trace: event `SRangerController::move.out.1` fires; then event `SRangerController::endTask.in` fires; then event `SRangerController::move.out.0` fires.

The counterexample ends with event `SRangerController::move.out.0`. No transition in the RoboChart model handles `SRangerController::move.out.0` — some mode reaches a state where this event occurs without a matching branch. Add an `instanceof` check for the event type in the corresponding mode block of step().

One or more states have no reachable outgoing transition. Every mode in the controller's step() method must have at least one reachable branch out.

## Files to review
- SRangerController.java
- SRangerMode.java

## Next step
Read each issue above, follow the fix directive, edit the linked Java file, and re-run Phase 6a (FDR4 Verification).
