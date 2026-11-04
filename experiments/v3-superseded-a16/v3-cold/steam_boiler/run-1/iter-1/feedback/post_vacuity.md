# 6d — Vacuity Audit (Dafny + Isabelle) — PASSED

## Summary
No blocking vacuity signals detected (syntactic D1/I1 and semantic satisfiability/reachability/constant-consistency checks). Any `0 errors` from Dafny / Isabelle / FDR is at least potentially load-bearing for the checked signals. 2 ADVISORY finding(s) recorded and NOT blocking: R2/mode_unreachable_checked, T2/transition_unfireable_checked. These follow from the deliberate narrowing of the CSP data domains that FDR4's finiteness requirement imposes, not from the Java under test; they bound what the passing FDR4 assertions are evidence about and must be reported with any verdict drawn from them.

## Run history
- New this run: 2
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: mode_unreachable_checked — [ADVISORY] Mode(s) unreachable under the CHECKED CSP instantiation: DEGRADED, INITIALIZATION_DRAINING, INITIALIZATION_READY, NORMAL, RESCUE

**Raw**
```
checked-reachable = {EMERGENCY_STOP, INITIALIZATION, INITIALIZATION_FILLING, initial} (narrowed ranges + constant stubs from instantiations.csp)
```

**Fix directive**
ADVISORY — no action for the codegen agent. This follows from the deliberate CSP domain narrowing (FDR4 finiteness), not from the Java. Record it with any verdict drawn from the CSP checks.

### Issue 2: transition_unfireable_checked — [ADVISORY] 44 transition(s) can never fire under the checked CSP instantiation

**Raw**
```
INITIALIZATIONToEMERGENCY_STOP: no value of `successiveStops()` in the checked domain [0..1] satisfies its constraints; INITIALIZATIONToINITIALIZATION_DRAINING: no value of `waterLevel()` in the checked domain [0..1] satisfies its constraints; INITIALIZATIONToINITIALIZATION_READY: no value of `waterLevel()` in the checked domain [0..1] satisfies its constraints; INITIALIZATION_FILLINGToEMERGENCY_STOP: no value of `successiveStops()` in the checked domain [0..1] satisfies its constraints; INITIALIZATION_FILLINGToINITIALIZATION_DRAINING: no value of `waterLevel()` in the checked domain [0..1] satisfies its constraints; INITIALIZATION_FILLINGToINITIALIZATION_READY: no value of `waterLevel()` in the checked domain [0..1] satisfies its constraints; INITIALIZATION_DRAININGToEMERGENCY_STOP: source mode INITIALIZATION_DRAINING is unreachable; INITIALIZATION_DRAININGToEMERGENCY_STOP_1: source mode INITIALIZATION_DRAINING is unreachable; INITIALIZATION_DRAININGToEMERGENCY_STOP_2: source mode INITIALIZATION_DRAINING is unreachable; INITIALIZATION_DRAININGToINITIALIZATION_DRAINING: source mode INITIALIZATION_DRAINING is unreachable; INITIALIZATION_DRAININGToINITIALIZATION_FILLING: source mode INITIALIZATION_DRAINING is unreachable; INITIALIZATION_DRAININGToINITIALIZATION_READY: source mode INITIALIZATION_DRAINING is unreachable; INITIALIZATION_READYToEMERGENCY_STOP: source mode INITIALIZATION_READY is unreachable; INITIALIZATION_READYToEMERGENCY_STOP_1: source mode INITIALIZATION_READY is unreachable; INITIALIZATION_READYToEMERGENCY_STOP_2: source mode INITIALIZATION_READY is unreachable; INITIALIZATION_READYToNORMAL: source mode INITIALIZATION_READY is unreachable; INITIALIZATION_READYToDEGRADED: source mode INITIALIZATION_READY is unreachable; INITIALIZATION_READYToINITIALIZATION_READY: source mode INITIALIZATION_READY is unreachable; NORMALToEMERGENCY_STOP: source mode NORMAL is unreachable; NORMALToEMERGENCY_STOP_1: source mode NORMAL is unreachable; NORMALToRESCUE: source mode NORMAL is unreachable; NORMALToDEGRADED: source mode NORMAL is unreachable; NORMALToNORMAL: source mode NORMAL is unreachable; NORMALToNORMAL_1: source mode NORMAL is unreachable; NORMALToNORMAL_2: source mode NORMAL is unreachable; DEGRADEDToEMERGENCY_STOP: source mode DEGRADED is unreachable; DEGRADEDToEMERGENCY_STOP_1: source mode DEGRADED is unreachable; DEGRADEDToRESCUE: source mode DEGRADED is unreachable; DEGRADEDToNORMAL: source mode DEGRADED is unreachable; DEGRADEDToDEGRADED: source mode DEGRADED is unreachable; DEGRADEDToDEGRADED_1: source mode DEGRADED is unreachable; DEGRADEDToDEGRADED_2: source mode DEGRADED is unreachable; RESCUEToEMERGENCY_STOP: source mode RESCUE is unreachable; RESCUEToEMERGENCY_STOP_1: source mode RESCUE is unreachable; RESCUEToNORMAL: source mode RESCUE is unreachable; RESCUEToDEGRADED: source mode RESCUE is unreachable; RESCUEToRESCUE: source mode RESCUE is unreachable; RESCUEToRESCUE_1: source mode RESCUE is unreachable; RESCUEToRESCUE_2: source mode RESCUE is unreachable; Idle_INITIALIZATION_DRAINING: source mode INITIALIZATION_DRAINING is unreachable; Idle_INITIALIZATION_READY: source mode INITIALIZATION_READY is unreachable; Idle_NORMAL: source mode NORMAL is unreachable; Idle_DEGRADED: source mode DEGRADED is unreachable; Idle_RESCUE: source mode RESCUE is unreachable
```

**Fix directive**
ADVISORY — no action for the codegen agent. This follows from the deliberate CSP domain narrowing (FDR4 finiteness), not from the Java. Record it with any verdict drawn from the CSP checks.

## Files to review
(none identified)

## Next step
Nothing to do. Re-run after each iteration to catch regressions.
