# Isabelle Verification — PASSED

## Summary
Isabelle: 1 theory, 49 lemma(s) verified, elapsed 0:02:36, peak 13 MB.

## Run history
- New this run: 1
- Recurring from previous run: 0
- Resolved since previous run: 2

**Resolved issue titles**
- Tactic timeout, suspected in SteamBoilerController_deadlock_free (deadlock_free — inferred from .thy)
- Tactic interrupt — cascade from timeout in SteamBoilerController_deadlock_free

## Issues
### Issue 1: isabelle_summary — Verification summary

**Raw**
```
Runtime: elapsed **0:02:36**, peak memory **13 MB**.

**Theories built (1), 25 lemma(s) proven total:**

**`SteamBoilerController_Beh.thy`** — 25 lemmas:
- *Deadlock-freedom* (1 lemma):
  - `SteamBoilerController_deadlock_free`
- *Invariant preservation* (24 lemmas): `Init_inv`, `InitialToINITIALIZATION_inv`, `INITIALIZATIONToEMERGENCY_STOP_inv`, `INITIALIZATIONToINITIALIZATION_inv`, `INITIALIZATIONToNORMAL_inv`, `INITIALIZATIONToDEGRADED_inv`, `INITIALIZATIONToINITIALIZATION_1_inv`, `INITIALIZATIONToINITIALIZATION_2_inv`, `INITIALIZATIONToINITIALIZATION_3_inv`, `NORMALToEMERGENCY_STOP_inv`, `NORMALToRESCUE_inv`, `NORMALToDEGRADED_inv`, `DEGRADEDToEMERGENCY_STOP_inv`, `DEGRADEDToRESCUE_inv`, `DEGRADEDToNORMAL_inv`, `RESCUEToEMERGENCY_STOP_inv`, `RESCUEToNORMAL_inv`, `RESCUEToDEGRADED_inv`, `EMERGENCY_STOPToEMERGENCY_STOP_inv`, `Idle_INITIALIZATION_inv`, `Idle_NORMAL_inv`, `Idle_DEGRADED_inv`, `Idle_RESCUE_inv`, `Idle_EMERGENCY_STOP_inv`
```

**Fix directive**
No action — informational.

## Files to review
(none identified)

## Next step
Z-Machine deadlock-freedom (and any R-series lemmas) verified. Continue refinement in Phase 1; Isabelle is the slowest backend, so re-run only on milestones.
