# 6a — Formal Verification (FDR4) — FAILED

## Summary
FDR4: 8 assertion(s) checked, 2 passed, 4 failed, 2 expected failure(s).

## Run history
**WARNING — thrashing detected.** 1 issue(s) have recurred 3+ runs in a row despite fixes: `Expected failure (informational)`. Previous fix strategies are not working — change approach (e.g. re-read the originating requirement, or escalate to the user).

- New this run: 4
- Recurring from previous run: 1
- Resolved since previous run: 0

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

### Issue 3: deadlock — Deadlock-freedom violated

**Raw**
```
P_SRangerController_Module_ctrl_ref0 :[deadlock free]
Counterexample: SRangerController_Module::ctrl_ref0::move.out.1 -> SRangerController_Module::ctrl_ref0::endTask.in -> SRangerController_Module::ctrl_ref0::move.out.0
```

**Fix directive**
Trace: event `SRangerController_Module::ctrl_ref0::move.out.1` fires; then event `SRangerController_Module::ctrl_ref0::endTask.in` fires; then event `SRangerController_Module::ctrl_ref0::move.out.0` fires.

The counterexample ends with event `SRangerController_Module::ctrl_ref0::move.out.0`. No transition in the RoboChart model handles `SRangerController_Module::ctrl_ref0::move.out.0` — some mode reaches a state where this event occurs without a matching branch. Add an `instanceof` check for the event type in the corresponding mode block of step().

One or more states have no reachable outgoing transition. Every mode in the controller's step() method must have at least one reachable branch out.

### Issue 4: deadlock — Deadlock-freedom violated

**Raw**
```
P_SRangerController_Module_ctrl_ref0 ; RUN({r__}) :[deadlock free]
Counterexample: SRangerController_Module::ctrl_ref0::move.out.1 -> SRangerController_Module::ctrl_ref0::endTask.in -> SRangerController_Module::ctrl_ref0::move.out.0
```

**Fix directive**
Trace: event `SRangerController_Module::ctrl_ref0::move.out.1` fires; then event `SRangerController_Module::ctrl_ref0::endTask.in` fires; then event `SRangerController_Module::ctrl_ref0::move.out.0` fires.

The counterexample ends with event `SRangerController_Module::ctrl_ref0::move.out.0`. No transition in the RoboChart model handles `SRangerController_Module::ctrl_ref0::move.out.0` — some mode reaches a state where this event occurs without a matching branch. Add an `instanceof` check for the event type in the corresponding mode block of step().

One or more states have no reachable outgoing transition. Every mode in the controller's step() method must have at least one reachable branch out.

### Issue 5: expected_failure — Expected failure (informational) [RECURRING x6 — fix strategy failing]

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
