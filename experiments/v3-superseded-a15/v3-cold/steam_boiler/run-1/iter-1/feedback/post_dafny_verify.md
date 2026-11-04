# 6a — Dafny Verification — FAILED

## Summary
Dafny: 1 file(s), 0 verified, 0 errors.

## Run history
- New this run: 5
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: dafny_other — Dafny error: Ambiguous use of && and ||. Use parentheses to disambiguate.

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(162,?): Error: Ambiguous use of && and ||. Use parentheses to disambiguate.
```

**Java trace**
  - RoboChart: TransitionMethod `INITIALIZATION`
  - Java: `SteamBoilerController.java`:35-269 (INITIALIZATION)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR8, SB-V1, SB-V2, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (25 more)

### Issue 2: dafny_other — Dafny error: Ambiguous use of && and ||. Use parentheses to disambiguate.

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(163,?): Error: Ambiguous use of && and ||. Use parentheses to disambiguate.
```

**Java trace**
  - RoboChart: TransitionMethod `INITIALIZATION`
  - Java: `SteamBoilerController.java`:35-269 (INITIALIZATION)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR8, SB-V1, SB-V2, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (25 more)

### Issue 3: dafny_other — Dafny error: Ambiguous use of && and ||. Use parentheses to disambiguate.

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(164,?): Error: Ambiguous use of && and ||. Use parentheses to disambiguate.
```

**Java trace**
  - RoboChart: TransitionMethod `INITIALIZATION`
  - Java: `SteamBoilerController.java`:35-269 (INITIALIZATION)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR8, SB-V1, SB-V2, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (25 more)

### Issue 4: dafny_other — Dafny error: Ambiguous use of && and ||. Use parentheses to disambiguate.

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(165,?): Error: Ambiguous use of && and ||. Use parentheses to disambiguate.
```

**Java trace**
  - RoboChart: TransitionMethod `INITIALIZATION`
  - Java: `SteamBoilerController.java`:35-269 (INITIALIZATION)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR8, SB-V1, SB-V2, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (25 more)

### Issue 5: dafny_other — Dafny error: Ambiguous use of && and ||. Use parentheses to disambiguate.

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(220,?): Error: Ambiguous use of && and ||. Use parentheses to disambiguate.
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

## Files to review
- OperatingMode.java
- PumpPosition.java
- SteamBoilerController.java
- WaterFlow.java

## Next step
Read each issue above, follow the fix directive, edit the linked Java file, re-run Phase 5b (Dafny Generation), then Phase 6b (Dafny Verification).
