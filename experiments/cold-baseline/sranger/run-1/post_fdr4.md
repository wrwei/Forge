# 6a — Formal Verification (FDR4) — FAILED

## Summary
FDR4: 8 assertion(s) checked, 4 passed, 2 failed, 2 expected failure(s).

## Run history
- New this run: 3
- Recurring from previous run: 0
- Resolved since previous run: 1

**Resolved issue titles**
- CSP-M parse error

## Issues
### Issue 1: deadlock — Deadlock-freedom violated

**Raw**
```
P_SRangerController_Module :[deadlock free]
Counterexample: SRangerController_Module::move.out.1 -> SRangerController_Module::endTask.in -> SRangerController_Module::move.out.0
```

**Fix directive**
Trace: event `SRangerController_Module::move.out.1` fires; then event `SRangerController_Module::endTask.in` fires; then event `SRangerController_Module::move.out.0` fires.

The counterexample ends with event `SRangerController_Module::move.out.0`. No transition in the RoboChart model handles `SRangerController_Module::move.out.0` — some mode reaches a state where this event occurs without a matching branch. Add an `instanceof` check for the event type in the corresponding mode block of step().

One or more states have no reachable outgoing transition. Every mode in the controller's step() method must have at least one reachable branch out.

### Issue 2: deadlock — Deadlock-freedom violated

**Raw**
```
P_SRangerController_Module ; RUN({r__}) :[deadlock free]
Counterexample: SRangerController_Module::move.out.1 -> SRangerController_Module::endTask.in -> SRangerController_Module::move.out.0
```

**Fix directive**
Trace: event `SRangerController_Module::move.out.1` fires; then event `SRangerController_Module::endTask.in` fires; then event `SRangerController_Module::move.out.0` fires.

The counterexample ends with event `SRangerController_Module::move.out.0`. No transition in the RoboChart model handles `SRangerController_Module::move.out.0` — some mode reaches a state where this event occurs without a matching branch. Add an `instanceof` check for the event type in the corresponding mode block of step().

One or more states have no reachable outgoing transition. Every mode in the controller's step() method must have at least one reachable branch out.

### Issue 3: expected_failure — Expected failure (informational)

**Raw**
```
The following failures are marked EXPECTED in config.phases.fdr4.expected_failures and are reported for information only: P_SRangerController_Module :[deterministic]; P_SRangerController_Module_ctrl_ref0 :[deterministic].
```

**Fix directive**
No action required.

## Files to review
- SRangerController.java
- SRangerMode.java

## Next step
Read each issue above, follow the fix directive, edit the linked Java file, and re-run Phase 6a (FDR4 Verification).
