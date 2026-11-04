# 6a — Dafny Verification — FAILED

## Summary
Dafny: 1 file(s), 0 verified, 0 errors.

## Run history
- New this run: 90
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: dafny_other — Dafny error: the name 'INITIALIZATION' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(100,?): Error: the name 'INITIALIZATION' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.INITIALIZATION')
```

**Java trace**
  - RoboChart: Constructor `constructor`
  - Java: `SteamBoilerController.java`:35-282 (constructor)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 2: dafny_other — Dafny error: the name 'INITIALIZATION' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(103,?): Error: the name 'INITIALIZATION' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.INITIALIZATION')
```

**Java trace**
  - RoboChart: Constructor `constructor`
  - Java: `SteamBoilerController.java`:35-282 (constructor)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 3: dafny_other — Dafny error: the name 'INITIALIZATION' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(110,?): Error: the name 'INITIALIZATION' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.INITIALIZATION')
```

**Java trace**
  - RoboChart: TransitionMethod `INITIALIZATION`
  - Java: `SteamBoilerController.java`:35-282 (INITIALIZATION)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 4: dafny_other — Dafny error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(113,?): Error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.EMERGENCY_STOP')
```

**Java trace**
  - RoboChart: TransitionMethod `INITIALIZATION`
  - Java: `SteamBoilerController.java`:35-282 (INITIALIZATION)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 5: dafny_other — Dafny error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(114,?): Error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.EMERGENCY_STOP')
```

**Java trace**
  - RoboChart: TransitionMethod `INITIALIZATION`
  - Java: `SteamBoilerController.java`:35-282 (INITIALIZATION)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 6: dafny_other — Dafny error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(115,?): Error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.EMERGENCY_STOP')
```

**Java trace**
  - RoboChart: TransitionMethod `INITIALIZATION`
  - Java: `SteamBoilerController.java`:35-282 (INITIALIZATION)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 7: dafny_other — Dafny error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(116,?): Error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.EMERGENCY_STOP')
```

**Java trace**
  - RoboChart: TransitionMethod `INITIALIZATION`
  - Java: `SteamBoilerController.java`:35-282 (INITIALIZATION)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 8: dafny_other — Dafny error: the name 'NORMAL' denotes a datatype constructor, but does not do so uniquely; a

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(117,?): Error: the name 'NORMAL' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.NORMAL')
```

**Java trace**
  - RoboChart: TransitionMethod `INITIALIZATION`
  - Java: `SteamBoilerController.java`:35-282 (INITIALIZATION)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 9: dafny_other — Dafny error: the name 'DEGRADED' denotes a datatype constructor, but does not do so uniquely;

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(118,?): Error: the name 'DEGRADED' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.DEGRADED')
```

**Java trace**
  - RoboChart: TransitionMethod `INITIALIZATION`
  - Java: `SteamBoilerController.java`:35-282 (INITIALIZATION)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 10: dafny_other — Dafny error: the name 'INITIALIZATION' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(119,?): Error: the name 'INITIALIZATION' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.INITIALIZATION')
```

**Java trace**
  - RoboChart: TransitionMethod `INITIALIZATION`
  - Java: `SteamBoilerController.java`:35-282 (INITIALIZATION)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 11: dafny_other — Dafny error: the name 'INITIALIZATION' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(120,?): Error: the name 'INITIALIZATION' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.INITIALIZATION')
```

**Java trace**
  - RoboChart: TransitionMethod `INITIALIZATION`
  - Java: `SteamBoilerController.java`:35-282 (INITIALIZATION)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 12: dafny_other — Dafny error: the name 'INITIALIZATION' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(121,?): Error: the name 'INITIALIZATION' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.INITIALIZATION')
```

**Java trace**
  - RoboChart: TransitionMethod `INITIALIZATION`
  - Java: `SteamBoilerController.java`:35-282 (INITIALIZATION)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 13: dafny_other — Dafny error: the name 'INITIALIZATION' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(122,?): Error: the name 'INITIALIZATION' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.INITIALIZATION')
```

**Java trace**
  - RoboChart: TransitionMethod `INITIALIZATION`
  - Java: `SteamBoilerController.java`:35-282 (INITIALIZATION)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 14: dafny_other — Dafny error: the name 'INITIALIZATION' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(123,?): Error: the name 'INITIALIZATION' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.INITIALIZATION')
```

**Java trace**
  - RoboChart: TransitionMethod `INITIALIZATION`
  - Java: `SteamBoilerController.java`:35-282 (INITIALIZATION)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 15: dafny_other — Dafny error: the name 'INITIALIZATION' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(124,?): Error: the name 'INITIALIZATION' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.INITIALIZATION')
```

**Java trace**
  - RoboChart: TransitionMethod `INITIALIZATION`
  - Java: `SteamBoilerController.java`:35-282 (INITIALIZATION)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 16: dafny_other — Dafny error: the name 'INITIALIZATION' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(125,?): Error: the name 'INITIALIZATION' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.INITIALIZATION')
```

**Java trace**
  - RoboChart: TransitionMethod `INITIALIZATION`
  - Java: `SteamBoilerController.java`:35-282 (INITIALIZATION)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 17: dafny_other — Dafny error: the name 'INITIALIZATION' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(126,?): Error: the name 'INITIALIZATION' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.INITIALIZATION')
```

**Java trace**
  - RoboChart: TransitionMethod `INITIALIZATION`
  - Java: `SteamBoilerController.java`:35-282 (INITIALIZATION)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 18: dafny_other — Dafny error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(130,?): Error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.EMERGENCY_STOP')
```

**Java trace**
  - RoboChart: TransitionMethod `INITIALIZATION`
  - Java: `SteamBoilerController.java`:35-282 (INITIALIZATION)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 19: dafny_other — Dafny error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(134,?): Error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.EMERGENCY_STOP')
```

**Java trace**
  - RoboChart: Controller `SteamBoilerController`
  - Java: `SteamBoilerController.java`:15-283 (SteamBoilerController)
  - Requirement(s): SB-Beh1, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-FR8 (FailureConsequencePrecedence): When several failure conditions are detected in the same cycle, the most severe consequence prevails: any condition demanding emergency stop (transmission fa...
  - SB-V1 (SystemArchitecture): The steam-boiler control program keeps the quantity of water in a steam boiler within safe bounds; a level that is too low or too high endangers the boiler o...
  - SB-V2 (OperatingModes): The program is always in exactly one of five operating modes: initialization, normal, degraded, rescue, and emergency stop. It starts in initialization mode.
  - ... (2 more)

### Issue 20: dafny_other — Dafny error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(138,?): Error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.EMERGENCY_STOP')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 21: dafny_other — Dafny error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(142,?): Error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.EMERGENCY_STOP')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 22: dafny_other — Dafny error: the name 'NORMAL' denotes a datatype constructor, but does not do so uniquely; a

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(146,?): Error: the name 'NORMAL' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.NORMAL')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 23: dafny_other — Dafny error: the name 'DEGRADED' denotes a datatype constructor, but does not do so uniquely;

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(150,?): Error: the name 'DEGRADED' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.DEGRADED')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 24: dafny_other — Dafny error: the name 'INITIALIZATION' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(154,?): Error: the name 'INITIALIZATION' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.INITIALIZATION')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 25: dafny_other — Dafny error: the name 'INITIALIZATION' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(158,?): Error: the name 'INITIALIZATION' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.INITIALIZATION')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 26: dafny_other — Dafny error: the name 'INITIALIZATION' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(162,?): Error: the name 'INITIALIZATION' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.INITIALIZATION')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 27: dafny_other — Dafny error: the name 'INITIALIZATION' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(166,?): Error: the name 'INITIALIZATION' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.INITIALIZATION')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 28: dafny_other — Dafny error: the name 'INITIALIZATION' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(170,?): Error: the name 'INITIALIZATION' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.INITIALIZATION')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 29: dafny_other — Dafny error: the name 'INITIALIZATION' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(174,?): Error: the name 'INITIALIZATION' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.INITIALIZATION')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 30: dafny_other — Dafny error: the name 'INITIALIZATION' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(178,?): Error: the name 'INITIALIZATION' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.INITIALIZATION')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 31: dafny_other — Dafny error: the name 'INITIALIZATION' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(182,?): Error: the name 'INITIALIZATION' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.INITIALIZATION')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 32: dafny_other — Dafny error: the name 'NORMAL' denotes a datatype constructor, but does not do so uniquely; a

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(188,?): Error: the name 'NORMAL' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.NORMAL')
```

**Java trace**
  - RoboChart: TransitionMethod `NORMAL`
  - Java: `SteamBoilerController.java`:35-282 (NORMAL)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 33: dafny_other — Dafny error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(191,?): Error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.EMERGENCY_STOP')
```

**Java trace**
  - RoboChart: TransitionMethod `NORMAL`
  - Java: `SteamBoilerController.java`:35-282 (NORMAL)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 34: dafny_other — Dafny error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(192,?): Error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.EMERGENCY_STOP')
```

**Java trace**
  - RoboChart: TransitionMethod `NORMAL`
  - Java: `SteamBoilerController.java`:35-282 (NORMAL)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 35: dafny_other — Dafny error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(193,?): Error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.EMERGENCY_STOP')
```

**Java trace**
  - RoboChart: TransitionMethod `NORMAL`
  - Java: `SteamBoilerController.java`:35-282 (NORMAL)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 36: dafny_other — Dafny error: the name 'RESCUE' denotes a datatype constructor, but does not do so uniquely; a

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(194,?): Error: the name 'RESCUE' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.RESCUE')
```

**Java trace**
  - RoboChart: TransitionMethod `NORMAL`
  - Java: `SteamBoilerController.java`:35-282 (NORMAL)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 37: dafny_other — Dafny error: the name 'DEGRADED' denotes a datatype constructor, but does not do so uniquely;

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(195,?): Error: the name 'DEGRADED' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.DEGRADED')
```

**Java trace**
  - RoboChart: TransitionMethod `NORMAL`
  - Java: `SteamBoilerController.java`:35-282 (NORMAL)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 38: dafny_other — Dafny error: the name 'NORMAL' denotes a datatype constructor, but does not do so uniquely; a

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(196,?): Error: the name 'NORMAL' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.NORMAL')
```

**Java trace**
  - RoboChart: TransitionMethod `NORMAL`
  - Java: `SteamBoilerController.java`:35-282 (NORMAL)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 39: dafny_other — Dafny error: the name 'NORMAL' denotes a datatype constructor, but does not do so uniquely; a

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(197,?): Error: the name 'NORMAL' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.NORMAL')
```

**Java trace**
  - RoboChart: TransitionMethod `NORMAL`
  - Java: `SteamBoilerController.java`:35-282 (NORMAL)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 40: dafny_other — Dafny error: the name 'NORMAL' denotes a datatype constructor, but does not do so uniquely; a

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(198,?): Error: the name 'NORMAL' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.NORMAL')
```

**Java trace**
  - RoboChart: TransitionMethod `NORMAL`
  - Java: `SteamBoilerController.java`:35-282 (NORMAL)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 41: dafny_other — Dafny error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(202,?): Error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.EMERGENCY_STOP')
```

**Java trace**
  - RoboChart: TransitionMethod `NORMAL`
  - Java: `SteamBoilerController.java`:35-282 (NORMAL)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 42: dafny_other — Dafny error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(206,?): Error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.EMERGENCY_STOP')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 43: dafny_other — Dafny error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(210,?): Error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.EMERGENCY_STOP')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 44: dafny_other — Dafny error: the name 'RESCUE' denotes a datatype constructor, but does not do so uniquely; a

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(214,?): Error: the name 'RESCUE' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.RESCUE')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 45: dafny_other — Dafny error: the name 'DEGRADED' denotes a datatype constructor, but does not do so uniquely;

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(218,?): Error: the name 'DEGRADED' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.DEGRADED')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 46: dafny_other — Dafny error: the name 'NORMAL' denotes a datatype constructor, but does not do so uniquely; a

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(222,?): Error: the name 'NORMAL' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.NORMAL')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 47: dafny_other — Dafny error: the name 'NORMAL' denotes a datatype constructor, but does not do so uniquely; a

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(226,?): Error: the name 'NORMAL' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.NORMAL')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 48: dafny_other — Dafny error: the name 'NORMAL' denotes a datatype constructor, but does not do so uniquely; a

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(230,?): Error: the name 'NORMAL' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.NORMAL')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 49: dafny_other — Dafny error: the name 'DEGRADED' denotes a datatype constructor, but does not do so uniquely;

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(236,?): Error: the name 'DEGRADED' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.DEGRADED')
```

**Java trace**
  - RoboChart: TransitionMethod `DEGRADED`
  - Java: `SteamBoilerController.java`:35-282 (DEGRADED)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 50: dafny_other — Dafny error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(239,?): Error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.EMERGENCY_STOP')
```

**Java trace**
  - RoboChart: TransitionMethod `DEGRADED`
  - Java: `SteamBoilerController.java`:35-282 (DEGRADED)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 51: dafny_other — Dafny error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(240,?): Error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.EMERGENCY_STOP')
```

**Java trace**
  - RoboChart: TransitionMethod `DEGRADED`
  - Java: `SteamBoilerController.java`:35-282 (DEGRADED)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 52: dafny_other — Dafny error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(241,?): Error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.EMERGENCY_STOP')
```

**Java trace**
  - RoboChart: TransitionMethod `DEGRADED`
  - Java: `SteamBoilerController.java`:35-282 (DEGRADED)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 53: dafny_other — Dafny error: the name 'RESCUE' denotes a datatype constructor, but does not do so uniquely; a

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(242,?): Error: the name 'RESCUE' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.RESCUE')
```

**Java trace**
  - RoboChart: TransitionMethod `DEGRADED`
  - Java: `SteamBoilerController.java`:35-282 (DEGRADED)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 54: dafny_other — Dafny error: the name 'NORMAL' denotes a datatype constructor, but does not do so uniquely; a

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(243,?): Error: the name 'NORMAL' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.NORMAL')
```

**Java trace**
  - RoboChart: TransitionMethod `DEGRADED`
  - Java: `SteamBoilerController.java`:35-282 (DEGRADED)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 55: dafny_other — Dafny error: the name 'DEGRADED' denotes a datatype constructor, but does not do so uniquely;

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(244,?): Error: the name 'DEGRADED' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.DEGRADED')
```

**Java trace**
  - RoboChart: TransitionMethod `DEGRADED`
  - Java: `SteamBoilerController.java`:35-282 (DEGRADED)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 56: dafny_other — Dafny error: the name 'DEGRADED' denotes a datatype constructor, but does not do so uniquely;

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(245,?): Error: the name 'DEGRADED' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.DEGRADED')
```

**Java trace**
  - RoboChart: TransitionMethod `DEGRADED`
  - Java: `SteamBoilerController.java`:35-282 (DEGRADED)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 57: dafny_other — Dafny error: the name 'DEGRADED' denotes a datatype constructor, but does not do so uniquely;

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(246,?): Error: the name 'DEGRADED' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.DEGRADED')
```

**Java trace**
  - RoboChart: TransitionMethod `DEGRADED`
  - Java: `SteamBoilerController.java`:35-282 (DEGRADED)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 58: dafny_other — Dafny error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(250,?): Error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.EMERGENCY_STOP')
```

**Java trace**
  - RoboChart: TransitionMethod `DEGRADED`
  - Java: `SteamBoilerController.java`:35-282 (DEGRADED)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 59: dafny_other — Dafny error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(254,?): Error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.EMERGENCY_STOP')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 60: dafny_other — Dafny error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(258,?): Error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.EMERGENCY_STOP')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 61: dafny_other — Dafny error: the name 'RESCUE' denotes a datatype constructor, but does not do so uniquely; a

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(262,?): Error: the name 'RESCUE' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.RESCUE')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 62: dafny_other — Dafny error: the name 'NORMAL' denotes a datatype constructor, but does not do so uniquely; a

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(266,?): Error: the name 'NORMAL' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.NORMAL')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 63: dafny_other — Dafny error: the name 'DEGRADED' denotes a datatype constructor, but does not do so uniquely;

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(270,?): Error: the name 'DEGRADED' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.DEGRADED')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 64: dafny_other — Dafny error: the name 'DEGRADED' denotes a datatype constructor, but does not do so uniquely;

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(274,?): Error: the name 'DEGRADED' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.DEGRADED')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 65: dafny_other — Dafny error: the name 'DEGRADED' denotes a datatype constructor, but does not do so uniquely;

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(278,?): Error: the name 'DEGRADED' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.DEGRADED')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 66: dafny_other — Dafny error: the name 'RESCUE' denotes a datatype constructor, but does not do so uniquely; a

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(284,?): Error: the name 'RESCUE' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.RESCUE')
```

**Java trace**
  - RoboChart: TransitionMethod `RESCUE`
  - Java: `SteamBoilerController.java`:35-282 (RESCUE)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 67: dafny_other — Dafny error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(287,?): Error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.EMERGENCY_STOP')
```

**Java trace**
  - RoboChart: TransitionMethod `RESCUE`
  - Java: `SteamBoilerController.java`:35-282 (RESCUE)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 68: dafny_other — Dafny error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(288,?): Error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.EMERGENCY_STOP')
```

**Java trace**
  - RoboChart: TransitionMethod `RESCUE`
  - Java: `SteamBoilerController.java`:35-282 (RESCUE)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 69: dafny_other — Dafny error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(289,?): Error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.EMERGENCY_STOP')
```

**Java trace**
  - RoboChart: TransitionMethod `RESCUE`
  - Java: `SteamBoilerController.java`:35-282 (RESCUE)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 70: dafny_other — Dafny error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(290,?): Error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.EMERGENCY_STOP')
```

**Java trace**
  - RoboChart: TransitionMethod `RESCUE`
  - Java: `SteamBoilerController.java`:35-282 (RESCUE)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 71: dafny_other — Dafny error: the name 'NORMAL' denotes a datatype constructor, but does not do so uniquely; a

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(291,?): Error: the name 'NORMAL' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.NORMAL')
```

**Java trace**
  - RoboChart: TransitionMethod `RESCUE`
  - Java: `SteamBoilerController.java`:35-282 (RESCUE)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 72: dafny_other — Dafny error: the name 'DEGRADED' denotes a datatype constructor, but does not do so uniquely;

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(292,?): Error: the name 'DEGRADED' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.DEGRADED')
```

**Java trace**
  - RoboChart: TransitionMethod `RESCUE`
  - Java: `SteamBoilerController.java`:35-282 (RESCUE)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 73: dafny_other — Dafny error: the name 'RESCUE' denotes a datatype constructor, but does not do so uniquely; a

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(293,?): Error: the name 'RESCUE' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.RESCUE')
```

**Java trace**
  - RoboChart: TransitionMethod `RESCUE`
  - Java: `SteamBoilerController.java`:35-282 (RESCUE)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 74: dafny_other — Dafny error: the name 'RESCUE' denotes a datatype constructor, but does not do so uniquely; a

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(294,?): Error: the name 'RESCUE' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.RESCUE')
```

**Java trace**
  - RoboChart: TransitionMethod `RESCUE`
  - Java: `SteamBoilerController.java`:35-282 (RESCUE)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 75: dafny_other — Dafny error: the name 'RESCUE' denotes a datatype constructor, but does not do so uniquely; a

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(295,?): Error: the name 'RESCUE' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.RESCUE')
```

**Java trace**
  - RoboChart: TransitionMethod `RESCUE`
  - Java: `SteamBoilerController.java`:35-282 (RESCUE)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 76: dafny_other — Dafny error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(299,?): Error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.EMERGENCY_STOP')
```

**Java trace**
  - RoboChart: TransitionMethod `RESCUE`
  - Java: `SteamBoilerController.java`:35-282 (RESCUE)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 77: dafny_other — Dafny error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(303,?): Error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.EMERGENCY_STOP')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 78: dafny_other — Dafny error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(307,?): Error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.EMERGENCY_STOP')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 79: dafny_other — Dafny error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(311,?): Error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.EMERGENCY_STOP')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 80: dafny_other — Dafny error: the name 'NORMAL' denotes a datatype constructor, but does not do so uniquely; a

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(315,?): Error: the name 'NORMAL' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.NORMAL')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 81: dafny_other — Dafny error: the name 'DEGRADED' denotes a datatype constructor, but does not do so uniquely;

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(319,?): Error: the name 'DEGRADED' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.DEGRADED')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 82: dafny_other — Dafny error: the name 'RESCUE' denotes a datatype constructor, but does not do so uniquely; a

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(323,?): Error: the name 'RESCUE' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.RESCUE')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 83: dafny_other — Dafny error: the name 'RESCUE' denotes a datatype constructor, but does not do so uniquely; a

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(327,?): Error: the name 'RESCUE' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.RESCUE')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 84: dafny_other — Dafny error: the name 'RESCUE' denotes a datatype constructor, but does not do so uniquely; a

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(331,?): Error: the name 'RESCUE' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.RESCUE')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 85: dafny_other — Dafny error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(337,?): Error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.EMERGENCY_STOP')
```

**Java trace**
  - RoboChart: TransitionMethod `EMERGENCY_STOP`
  - Java: `SteamBoilerController.java`:35-282 (EMERGENCY_STOP)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 86: dafny_other — Dafny error: the name 'INITIALIZATION' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(349,?): Error: the name 'INITIALIZATION' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.INITIALIZATION')
```

**Java trace**
  - RoboChart: StepMethod `step`
  - Java: `SteamBoilerController.java`:35-282 (step)
  - Requirement(s): SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-FR5, SB-FR8, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - SB-Beh13 (Normal_to_Emergency_LevelRisk): The program moves from normal mode to emergency stop mode if the water level risks reaching one of the limit quantities M1 or M2, this risk being judged agai...
  - ... (22 more)

### Issue 87: dafny_other — Dafny error: the name 'NORMAL' denotes a datatype constructor, but does not do so uniquely; a

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(352,?): Error: the name 'NORMAL' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.NORMAL')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 88: dafny_other — Dafny error: the name 'DEGRADED' denotes a datatype constructor, but does not do so uniquely;

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(355,?): Error: the name 'DEGRADED' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.DEGRADED')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 89: dafny_other — Dafny error: the name 'RESCUE' denotes a datatype constructor, but does not do so uniquely; a

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(358,?): Error: the name 'RESCUE' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.RESCUE')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 90: dafny_other — Dafny error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(361,?): Error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.EMERGENCY_STOP')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

## Files to review
- BoilerMode.java
- OperatingMode.java
- PumpId.java
- SteamBoilerController.java

## Next step
Read each issue above, follow the fix directive, edit the linked Java file, re-run Phase 5b (Dafny Generation), then Phase 6b (Dafny Verification).
