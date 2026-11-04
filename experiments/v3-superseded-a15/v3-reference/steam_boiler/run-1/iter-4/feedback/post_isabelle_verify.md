# Isabelle Verification — PASSED

## Summary
Isabelle: 1 theory, 53 lemma(s) verified, elapsed 0:03:18, peak 28 MB.

## Run history
- New this run: 1
- Recurring from previous run: 0
- Resolved since previous run: 1

**Resolved issue titles**
- Tactic interrupt — cascade from timeout in SteamBoilerController_deadlock_free

## Issues
### Issue 1: isabelle_summary — Verification summary

**Raw**
```
Runtime: elapsed **0:03:18**, peak memory **28 MB**.

**Theories built (1), 27 lemma(s) proven total:**

**`SteamBoilerController_Beh.thy`** — 27 lemmas:
- *Deadlock-freedom* (1 lemma):
  - `SteamBoilerController_deadlock_free`
- *Invariant preservation* (26 lemmas): `Init_inv`, `InitialToINITIALIZATION_inv`, `INITIALIZATIONToEMERGENCY_STOP_inv`, `INITIALIZATIONToNORMAL_inv`, `INITIALIZATIONToDEGRADED_inv`, `INITIALIZATIONToINITIALIZATION_inv`, `INITIALIZATIONToINITIALIZATION_1_inv`, `INITIALIZATIONToINITIALIZATION_2_inv`, `INITIALIZATIONToINITIALIZATION_3_inv`, `NORMALToEMERGENCY_STOP_inv`, `NORMALToRESCUE_inv`, `NORMALToDEGRADED_inv`, `NORMALToNORMAL_inv`, `DEGRADEDToEMERGENCY_STOP_inv`, `DEGRADEDToRESCUE_inv`, `DEGRADEDToNORMAL_inv`, `DEGRADEDToDEGRADED_inv`, `RESCUEToEMERGENCY_STOP_inv`, `RESCUEToEMERGENCY_STOP_1_inv`, `RESCUEToRESCUE_inv`, `RESCUEToNORMAL_inv`, `RESCUEToDEGRADED_inv`, `Idle_INITIALIZATION_inv`, `Idle_NORMAL_inv`, `Idle_DEGRADED_inv`, `Idle_RESCUE_inv`
```

**Fix directive**
No action — informational.

## Files to review
(none identified)

## Next step
Z-Machine deadlock-freedom (and any R-series lemmas) verified. Continue refinement in Phase 1; Isabelle is the slowest backend, so re-run only on milestones.
