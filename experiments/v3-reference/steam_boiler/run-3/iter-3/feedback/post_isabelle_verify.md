# Isabelle Verification — PASSED

## Summary
Isabelle: 1 theory, 45 lemma(s) verified, elapsed 0:01:58, peak 12 MB.

## Run history
- New this run: 1
- Recurring from previous run: 0
- Resolved since previous run: 1

**Resolved issue titles**
- Verification summary

## Issues
### Issue 1: isabelle_summary — Verification summary

**Raw**
```
Runtime: elapsed **0:01:58**, peak memory **12 MB**.

**Theories built (1), 23 lemma(s) proven total:**

**`SteamBoilerController_Beh.thy`** — 23 lemmas:
- *Deadlock-freedom* (1 lemma):
  - `SteamBoilerController_deadlock_free`
- *Invariant preservation* (22 lemmas): `Init_inv`, `InitialToINITIALIZATION_inv`, `INITIALIZATIONToEMERGENCY_STOP_inv`, `INITIALIZATIONToDEGRADED_inv`, `INITIALIZATIONToNORMAL_inv`, `INITIALIZATIONToINITIALIZATION_inv`, `NORMALToEMERGENCY_STOP_inv`, `NORMALToRESCUE_inv`, `NORMALToDEGRADED_inv`, `NORMALToNORMAL_inv`, `DEGRADEDToEMERGENCY_STOP_inv`, `DEGRADEDToRESCUE_inv`, `DEGRADEDToNORMAL_inv`, `DEGRADEDToDEGRADED_inv`, `RESCUEToEMERGENCY_STOP_inv`, `RESCUEToNORMAL_inv`, `RESCUEToDEGRADED_inv`, `RESCUEToRESCUE_inv`, `Idle_INITIALIZATION_inv`, `Idle_NORMAL_inv`, `Idle_DEGRADED_inv`, `Idle_RESCUE_inv`
```

**Fix directive**
No action — informational.

## Files to review
(none identified)

## Next step
Z-Machine deadlock-freedom (and any R-series lemmas) verified. Continue refinement in Phase 1; Isabelle is the slowest backend, so re-run only on milestones.
