# Isabelle Verification — PASSED

## Summary
Isabelle: 1 theory, 53 lemma(s) verified, elapsed 0:03:03, peak 13 MB.

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
Runtime: elapsed **0:03:03**, peak memory **12 MB**.

**Theories built (1), 27 lemma(s) proven total:**

**`SteamBoilerController_Beh.thy`** — 27 lemmas:
- *Deadlock-freedom* (1 lemma):
  - `SteamBoilerController_deadlock_free`
- *Invariant preservation* (26 lemmas): `Init_inv`, `InitialToINITIALISATION_inv`, `INITIALISATIONToEMERGENCY_STOP_inv`, `INITIALISATIONToINITIALISATION_inv`, `INITIALISATIONToINITIALISATION_1_inv`, `INITIALISATIONToNORMAL_inv`, `INITIALISATIONToDEGRADED_inv`, `INITIALISATIONToINITIALISATION_2_inv`, `NORMALToEMERGENCY_STOP_inv`, `NORMALToRESCUE_inv`, `NORMALToDEGRADED_inv`, `NORMALToNORMAL_inv`, `DEGRADEDToEMERGENCY_STOP_inv`, `DEGRADEDToRESCUE_inv`, `DEGRADEDToNORMAL_inv`, `DEGRADEDToDEGRADED_inv`, `RESCUEToEMERGENCY_STOP_inv`, `RESCUEToNORMAL_inv`, `RESCUEToDEGRADED_inv`, `RESCUEToRESCUE_inv`, `EMERGENCY_STOPToEMERGENCY_STOP_inv`, `Idle_INITIALISATION_inv`, `Idle_NORMAL_inv`, `Idle_DEGRADED_inv`, `Idle_RESCUE_inv`, `Idle_EMERGENCY_STOP_inv`
```

**Fix directive**
No action — informational.

## Files to review
(none identified)

## Next step
Z-Machine deadlock-freedom (and any R-series lemmas) verified. Continue refinement in Phase 1; Isabelle is the slowest backend, so re-run only on milestones.
