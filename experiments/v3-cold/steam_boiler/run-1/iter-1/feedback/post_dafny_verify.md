# 6a — Dafny Verification — FAILED

## Summary
Dafny: 1 file(s), 0 verified, 0 errors.

## Run history
- New this run: 95
- Recurring from previous run: 0
- Resolved since previous run: 0

## Issues
### Issue 1: dafny_other — Dafny error: the name 'INITIALIZATION' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(112,?): Error: the name 'INITIALIZATION' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.INITIALIZATION')
```

**Java trace**
  - RoboChart: Constructor `constructor`
  - Java: `SteamBoilerController.java`:37-299 (constructor)
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
forge.transformations/output/SteamBoilerController.dfy(115,?): Error: the name 'INITIALIZATION' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.INITIALIZATION')
```

**Java trace**
  - RoboChart: Constructor `constructor`
  - Java: `SteamBoilerController.java`:37-299 (constructor)
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
forge.transformations/output/SteamBoilerController.dfy(121,?): Error: the name 'INITIALIZATION' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.INITIALIZATION')
```

**Java trace**
  - RoboChart: TransitionMethod `INITIALIZATION`
  - Java: `SteamBoilerController.java`:37-299 (INITIALIZATION)
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
forge.transformations/output/SteamBoilerController.dfy(124,?): Error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.EMERGENCY_STOP')
```

**Java trace**
  - RoboChart: TransitionMethod `INITIALIZATION`
  - Java: `SteamBoilerController.java`:37-299 (INITIALIZATION)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 5: dafny_other — Dafny error: the name 'INITIALIZATION' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(125,?): Error: the name 'INITIALIZATION' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.INITIALIZATION')
```

**Java trace**
  - RoboChart: TransitionMethod `INITIALIZATION`
  - Java: `SteamBoilerController.java`:37-299 (INITIALIZATION)
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
forge.transformations/output/SteamBoilerController.dfy(126,?): Error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.EMERGENCY_STOP')
```

**Java trace**
  - RoboChart: TransitionMethod `INITIALIZATION`
  - Java: `SteamBoilerController.java`:37-299 (INITIALIZATION)
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
forge.transformations/output/SteamBoilerController.dfy(127,?): Error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.EMERGENCY_STOP')
```

**Java trace**
  - RoboChart: TransitionMethod `INITIALIZATION`
  - Java: `SteamBoilerController.java`:37-299 (INITIALIZATION)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 8: dafny_other — Dafny error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(128,?): Error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.EMERGENCY_STOP')
```

**Java trace**
  - RoboChart: TransitionMethod `INITIALIZATION`
  - Java: `SteamBoilerController.java`:37-299 (INITIALIZATION)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 9: dafny_other — Dafny error: the name 'NORMAL' denotes a datatype constructor, but does not do so uniquely; a

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(129,?): Error: the name 'NORMAL' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.NORMAL')
```

**Java trace**
  - RoboChart: TransitionMethod `INITIALIZATION`
  - Java: `SteamBoilerController.java`:37-299 (INITIALIZATION)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 10: dafny_other — Dafny error: the name 'DEGRADED' denotes a datatype constructor, but does not do so uniquely;

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(130,?): Error: the name 'DEGRADED' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.DEGRADED')
```

**Java trace**
  - RoboChart: TransitionMethod `INITIALIZATION`
  - Java: `SteamBoilerController.java`:37-299 (INITIALIZATION)
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
forge.transformations/output/SteamBoilerController.dfy(131,?): Error: the name 'INITIALIZATION' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.INITIALIZATION')
```

**Java trace**
  - RoboChart: TransitionMethod `INITIALIZATION`
  - Java: `SteamBoilerController.java`:37-299 (INITIALIZATION)
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
forge.transformations/output/SteamBoilerController.dfy(132,?): Error: the name 'INITIALIZATION' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.INITIALIZATION')
```

**Java trace**
  - RoboChart: TransitionMethod `INITIALIZATION`
  - Java: `SteamBoilerController.java`:37-299 (INITIALIZATION)
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
forge.transformations/output/SteamBoilerController.dfy(133,?): Error: the name 'INITIALIZATION' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.INITIALIZATION')
```

**Java trace**
  - RoboChart: TransitionMethod `INITIALIZATION`
  - Java: `SteamBoilerController.java`:37-299 (INITIALIZATION)
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
forge.transformations/output/SteamBoilerController.dfy(134,?): Error: the name 'INITIALIZATION' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.INITIALIZATION')
```

**Java trace**
  - RoboChart: TransitionMethod `INITIALIZATION`
  - Java: `SteamBoilerController.java`:37-299 (INITIALIZATION)
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
forge.transformations/output/SteamBoilerController.dfy(135,?): Error: the name 'INITIALIZATION' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.INITIALIZATION')
```

**Java trace**
  - RoboChart: TransitionMethod `INITIALIZATION`
  - Java: `SteamBoilerController.java`:37-299 (INITIALIZATION)
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
forge.transformations/output/SteamBoilerController.dfy(136,?): Error: the name 'INITIALIZATION' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.INITIALIZATION')
```

**Java trace**
  - RoboChart: TransitionMethod `INITIALIZATION`
  - Java: `SteamBoilerController.java`:37-299 (INITIALIZATION)
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
forge.transformations/output/SteamBoilerController.dfy(137,?): Error: the name 'INITIALIZATION' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.INITIALIZATION')
```

**Java trace**
  - RoboChart: TransitionMethod `INITIALIZATION`
  - Java: `SteamBoilerController.java`:37-299 (INITIALIZATION)
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
forge.transformations/output/SteamBoilerController.dfy(141,?): Error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.EMERGENCY_STOP')
```

**Java trace**
  - RoboChart: TransitionMethod `INITIALIZATION`
  - Java: `SteamBoilerController.java`:37-299 (INITIALIZATION)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 19: dafny_other — Dafny error: the name 'INITIALIZATION' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(145,?): Error: the name 'INITIALIZATION' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.INITIALIZATION')
```

**Java trace**
  - RoboChart: Controller `SteamBoilerController`
  - Java: `SteamBoilerController.java`:14-300 (SteamBoilerController)
  - Requirement(s): SB-Beh1, SB-V1

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-V1 (SystemArchitecture): The steam-boiler control program keeps the quantity of water in a steam boiler within safe bounds; a level that is too low or too high endangers the boiler o...

### Issue 20: dafny_other — Dafny error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(149,?): Error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.EMERGENCY_STOP')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 21: dafny_other — Dafny error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(153,?): Error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.EMERGENCY_STOP')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 22: dafny_other — Dafny error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(157,?): Error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.EMERGENCY_STOP')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 23: dafny_other — Dafny error: the name 'NORMAL' denotes a datatype constructor, but does not do so uniquely; a

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(161,?): Error: the name 'NORMAL' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.NORMAL')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 24: dafny_other — Dafny error: the name 'DEGRADED' denotes a datatype constructor, but does not do so uniquely;

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(165,?): Error: the name 'DEGRADED' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.DEGRADED')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 25: dafny_other — Dafny error: the name 'INITIALIZATION' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(169,?): Error: the name 'INITIALIZATION' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.INITIALIZATION')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 26: dafny_other — Dafny error: the name 'INITIALIZATION' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(173,?): Error: the name 'INITIALIZATION' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.INITIALIZATION')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 27: dafny_other — Dafny error: the name 'INITIALIZATION' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(177,?): Error: the name 'INITIALIZATION' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.INITIALIZATION')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 28: dafny_other — Dafny error: the name 'INITIALIZATION' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(181,?): Error: the name 'INITIALIZATION' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.INITIALIZATION')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 29: dafny_other — Dafny error: the name 'INITIALIZATION' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(185,?): Error: the name 'INITIALIZATION' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.INITIALIZATION')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 30: dafny_other — Dafny error: the name 'INITIALIZATION' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(189,?): Error: the name 'INITIALIZATION' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.INITIALIZATION')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 31: dafny_other — Dafny error: the name 'INITIALIZATION' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(193,?): Error: the name 'INITIALIZATION' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.INITIALIZATION')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 32: dafny_other — Dafny error: the name 'NORMAL' denotes a datatype constructor, but does not do so uniquely; a

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(199,?): Error: the name 'NORMAL' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.NORMAL')
```

**Java trace**
  - RoboChart: TransitionMethod `NORMAL`
  - Java: `SteamBoilerController.java`:37-299 (NORMAL)
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
forge.transformations/output/SteamBoilerController.dfy(202,?): Error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.EMERGENCY_STOP')
```

**Java trace**
  - RoboChart: TransitionMethod `NORMAL`
  - Java: `SteamBoilerController.java`:37-299 (NORMAL)
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
forge.transformations/output/SteamBoilerController.dfy(203,?): Error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.EMERGENCY_STOP')
```

**Java trace**
  - RoboChart: TransitionMethod `NORMAL`
  - Java: `SteamBoilerController.java`:37-299 (NORMAL)
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
forge.transformations/output/SteamBoilerController.dfy(204,?): Error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.EMERGENCY_STOP')
```

**Java trace**
  - RoboChart: TransitionMethod `NORMAL`
  - Java: `SteamBoilerController.java`:37-299 (NORMAL)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 36: dafny_other — Dafny error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(205,?): Error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.EMERGENCY_STOP')
```

**Java trace**
  - RoboChart: TransitionMethod `NORMAL`
  - Java: `SteamBoilerController.java`:37-299 (NORMAL)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 37: dafny_other — Dafny error: the name 'RESCUE' denotes a datatype constructor, but does not do so uniquely; a

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(206,?): Error: the name 'RESCUE' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.RESCUE')
```

**Java trace**
  - RoboChart: TransitionMethod `NORMAL`
  - Java: `SteamBoilerController.java`:37-299 (NORMAL)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 38: dafny_other — Dafny error: the name 'DEGRADED' denotes a datatype constructor, but does not do so uniquely;

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(207,?): Error: the name 'DEGRADED' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.DEGRADED')
```

**Java trace**
  - RoboChart: TransitionMethod `NORMAL`
  - Java: `SteamBoilerController.java`:37-299 (NORMAL)
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
forge.transformations/output/SteamBoilerController.dfy(208,?): Error: the name 'NORMAL' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.NORMAL')
```

**Java trace**
  - RoboChart: TransitionMethod `NORMAL`
  - Java: `SteamBoilerController.java`:37-299 (NORMAL)
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
forge.transformations/output/SteamBoilerController.dfy(209,?): Error: the name 'NORMAL' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.NORMAL')
```

**Java trace**
  - RoboChart: TransitionMethod `NORMAL`
  - Java: `SteamBoilerController.java`:37-299 (NORMAL)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 41: dafny_other — Dafny error: the name 'NORMAL' denotes a datatype constructor, but does not do so uniquely; a

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(210,?): Error: the name 'NORMAL' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.NORMAL')
```

**Java trace**
  - RoboChart: TransitionMethod `NORMAL`
  - Java: `SteamBoilerController.java`:37-299 (NORMAL)
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
forge.transformations/output/SteamBoilerController.dfy(214,?): Error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.EMERGENCY_STOP')
```

**Java trace**
  - RoboChart: TransitionMethod `NORMAL`
  - Java: `SteamBoilerController.java`:37-299 (NORMAL)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 43: dafny_other — Dafny error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(218,?): Error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.EMERGENCY_STOP')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 44: dafny_other — Dafny error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(222,?): Error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.EMERGENCY_STOP')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 45: dafny_other — Dafny error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(226,?): Error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.EMERGENCY_STOP')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 46: dafny_other — Dafny error: the name 'RESCUE' denotes a datatype constructor, but does not do so uniquely; a

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(230,?): Error: the name 'RESCUE' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.RESCUE')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 47: dafny_other — Dafny error: the name 'DEGRADED' denotes a datatype constructor, but does not do so uniquely;

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(234,?): Error: the name 'DEGRADED' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.DEGRADED')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 48: dafny_other — Dafny error: the name 'NORMAL' denotes a datatype constructor, but does not do so uniquely; a

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(238,?): Error: the name 'NORMAL' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.NORMAL')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 49: dafny_other — Dafny error: the name 'NORMAL' denotes a datatype constructor, but does not do so uniquely; a

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(242,?): Error: the name 'NORMAL' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.NORMAL')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 50: dafny_other — Dafny error: the name 'NORMAL' denotes a datatype constructor, but does not do so uniquely; a

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(246,?): Error: the name 'NORMAL' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.NORMAL')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 51: dafny_other — Dafny error: the name 'DEGRADED' denotes a datatype constructor, but does not do so uniquely;

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(252,?): Error: the name 'DEGRADED' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.DEGRADED')
```

**Java trace**
  - RoboChart: TransitionMethod `DEGRADED`
  - Java: `SteamBoilerController.java`:37-299 (DEGRADED)
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
forge.transformations/output/SteamBoilerController.dfy(255,?): Error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.EMERGENCY_STOP')
```

**Java trace**
  - RoboChart: TransitionMethod `DEGRADED`
  - Java: `SteamBoilerController.java`:37-299 (DEGRADED)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 53: dafny_other — Dafny error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(256,?): Error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.EMERGENCY_STOP')
```

**Java trace**
  - RoboChart: TransitionMethod `DEGRADED`
  - Java: `SteamBoilerController.java`:37-299 (DEGRADED)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 54: dafny_other — Dafny error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(257,?): Error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.EMERGENCY_STOP')
```

**Java trace**
  - RoboChart: TransitionMethod `DEGRADED`
  - Java: `SteamBoilerController.java`:37-299 (DEGRADED)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 55: dafny_other — Dafny error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(258,?): Error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.EMERGENCY_STOP')
```

**Java trace**
  - RoboChart: TransitionMethod `DEGRADED`
  - Java: `SteamBoilerController.java`:37-299 (DEGRADED)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 56: dafny_other — Dafny error: the name 'RESCUE' denotes a datatype constructor, but does not do so uniquely; a

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(259,?): Error: the name 'RESCUE' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.RESCUE')
```

**Java trace**
  - RoboChart: TransitionMethod `DEGRADED`
  - Java: `SteamBoilerController.java`:37-299 (DEGRADED)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 57: dafny_other — Dafny error: the name 'NORMAL' denotes a datatype constructor, but does not do so uniquely; a

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(260,?): Error: the name 'NORMAL' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.NORMAL')
```

**Java trace**
  - RoboChart: TransitionMethod `DEGRADED`
  - Java: `SteamBoilerController.java`:37-299 (DEGRADED)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 58: dafny_other — Dafny error: the name 'DEGRADED' denotes a datatype constructor, but does not do so uniquely;

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(261,?): Error: the name 'DEGRADED' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.DEGRADED')
```

**Java trace**
  - RoboChart: TransitionMethod `DEGRADED`
  - Java: `SteamBoilerController.java`:37-299 (DEGRADED)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 59: dafny_other — Dafny error: the name 'DEGRADED' denotes a datatype constructor, but does not do so uniquely;

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(262,?): Error: the name 'DEGRADED' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.DEGRADED')
```

**Java trace**
  - RoboChart: TransitionMethod `DEGRADED`
  - Java: `SteamBoilerController.java`:37-299 (DEGRADED)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 60: dafny_other — Dafny error: the name 'DEGRADED' denotes a datatype constructor, but does not do so uniquely;

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(263,?): Error: the name 'DEGRADED' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.DEGRADED')
```

**Java trace**
  - RoboChart: TransitionMethod `DEGRADED`
  - Java: `SteamBoilerController.java`:37-299 (DEGRADED)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 61: dafny_other — Dafny error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(267,?): Error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.EMERGENCY_STOP')
```

**Java trace**
  - RoboChart: TransitionMethod `DEGRADED`
  - Java: `SteamBoilerController.java`:37-299 (DEGRADED)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 62: dafny_other — Dafny error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(271,?): Error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.EMERGENCY_STOP')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 63: dafny_other — Dafny error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(275,?): Error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.EMERGENCY_STOP')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 64: dafny_other — Dafny error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(279,?): Error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.EMERGENCY_STOP')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 65: dafny_other — Dafny error: the name 'RESCUE' denotes a datatype constructor, but does not do so uniquely; a

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(283,?): Error: the name 'RESCUE' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.RESCUE')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 66: dafny_other — Dafny error: the name 'NORMAL' denotes a datatype constructor, but does not do so uniquely; a

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(287,?): Error: the name 'NORMAL' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.NORMAL')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 67: dafny_other — Dafny error: the name 'DEGRADED' denotes a datatype constructor, but does not do so uniquely;

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(291,?): Error: the name 'DEGRADED' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.DEGRADED')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 68: dafny_other — Dafny error: the name 'DEGRADED' denotes a datatype constructor, but does not do so uniquely;

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(295,?): Error: the name 'DEGRADED' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.DEGRADED')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 69: dafny_other — Dafny error: the name 'DEGRADED' denotes a datatype constructor, but does not do so uniquely;

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(299,?): Error: the name 'DEGRADED' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.DEGRADED')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 70: dafny_other — Dafny error: the name 'RESCUE' denotes a datatype constructor, but does not do so uniquely; a

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(305,?): Error: the name 'RESCUE' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.RESCUE')
```

**Java trace**
  - RoboChart: TransitionMethod `RESCUE`
  - Java: `SteamBoilerController.java`:37-299 (RESCUE)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 71: dafny_other — Dafny error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(308,?): Error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.EMERGENCY_STOP')
```

**Java trace**
  - RoboChart: TransitionMethod `RESCUE`
  - Java: `SteamBoilerController.java`:37-299 (RESCUE)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 72: dafny_other — Dafny error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(309,?): Error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.EMERGENCY_STOP')
```

**Java trace**
  - RoboChart: TransitionMethod `RESCUE`
  - Java: `SteamBoilerController.java`:37-299 (RESCUE)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 73: dafny_other — Dafny error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(310,?): Error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.EMERGENCY_STOP')
```

**Java trace**
  - RoboChart: TransitionMethod `RESCUE`
  - Java: `SteamBoilerController.java`:37-299 (RESCUE)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 74: dafny_other — Dafny error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(311,?): Error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.EMERGENCY_STOP')
```

**Java trace**
  - RoboChart: TransitionMethod `RESCUE`
  - Java: `SteamBoilerController.java`:37-299 (RESCUE)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 75: dafny_other — Dafny error: the name 'NORMAL' denotes a datatype constructor, but does not do so uniquely; a

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(312,?): Error: the name 'NORMAL' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.NORMAL')
```

**Java trace**
  - RoboChart: TransitionMethod `RESCUE`
  - Java: `SteamBoilerController.java`:37-299 (RESCUE)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 76: dafny_other — Dafny error: the name 'DEGRADED' denotes a datatype constructor, but does not do so uniquely;

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(313,?): Error: the name 'DEGRADED' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.DEGRADED')
```

**Java trace**
  - RoboChart: TransitionMethod `RESCUE`
  - Java: `SteamBoilerController.java`:37-299 (RESCUE)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 77: dafny_other — Dafny error: the name 'RESCUE' denotes a datatype constructor, but does not do so uniquely; a

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(314,?): Error: the name 'RESCUE' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.RESCUE')
```

**Java trace**
  - RoboChart: TransitionMethod `RESCUE`
  - Java: `SteamBoilerController.java`:37-299 (RESCUE)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 78: dafny_other — Dafny error: the name 'RESCUE' denotes a datatype constructor, but does not do so uniquely; a

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(315,?): Error: the name 'RESCUE' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.RESCUE')
```

**Java trace**
  - RoboChart: TransitionMethod `RESCUE`
  - Java: `SteamBoilerController.java`:37-299 (RESCUE)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 79: dafny_other — Dafny error: the name 'RESCUE' denotes a datatype constructor, but does not do so uniquely; a

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(316,?): Error: the name 'RESCUE' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.RESCUE')
```

**Java trace**
  - RoboChart: TransitionMethod `RESCUE`
  - Java: `SteamBoilerController.java`:37-299 (RESCUE)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 80: dafny_other — Dafny error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(320,?): Error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.EMERGENCY_STOP')
```

**Java trace**
  - RoboChart: TransitionMethod `RESCUE`
  - Java: `SteamBoilerController.java`:37-299 (RESCUE)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 81: dafny_other — Dafny error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(324,?): Error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.EMERGENCY_STOP')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 82: dafny_other — Dafny error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(328,?): Error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.EMERGENCY_STOP')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 83: dafny_other — Dafny error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(332,?): Error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.EMERGENCY_STOP')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 84: dafny_other — Dafny error: the name 'NORMAL' denotes a datatype constructor, but does not do so uniquely; a

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(336,?): Error: the name 'NORMAL' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.NORMAL')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 85: dafny_other — Dafny error: the name 'DEGRADED' denotes a datatype constructor, but does not do so uniquely;

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(340,?): Error: the name 'DEGRADED' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.DEGRADED')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 86: dafny_other — Dafny error: the name 'RESCUE' denotes a datatype constructor, but does not do so uniquely; a

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(344,?): Error: the name 'RESCUE' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.RESCUE')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 87: dafny_other — Dafny error: the name 'RESCUE' denotes a datatype constructor, but does not do so uniquely; a

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(348,?): Error: the name 'RESCUE' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.RESCUE')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 88: dafny_other — Dafny error: the name 'RESCUE' denotes a datatype constructor, but does not do so uniquely; a

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(352,?): Error: the name 'RESCUE' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.RESCUE')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 89: dafny_other — Dafny error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(358,?): Error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.EMERGENCY_STOP')
```

**Java trace**
  - RoboChart: TransitionMethod `EMERGENCY_STOP`
  - Java: `SteamBoilerController.java`:37-299 (EMERGENCY_STOP)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 90: dafny_other — Dafny error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(364,?): Error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.EMERGENCY_STOP')
```

**Java trace**
  - RoboChart: TransitionMethod `EMERGENCY_STOP`
  - Java: `SteamBoilerController.java`:37-299 (EMERGENCY_STOP)
  - Requirement(s): SB-Beh1, SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-DM7, SB-FR5, SB-FR8, SB-V1, SB-V2, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh1 (InitialMode): On start-up the program is in initialization mode.
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - ... (27 more)

### Issue 91: dafny_other — Dafny error: the name 'INITIALIZATION' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(374,?): Error: the name 'INITIALIZATION' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.INITIALIZATION')
```

**Java trace**
  - RoboChart: StepMethod `step`
  - Java: `SteamBoilerController.java`:37-299 (step)
  - Requirement(s): SB-Beh10, SB-Beh11, SB-Beh12, SB-Beh13, SB-Beh14, SB-Beh15, SB-Beh16, SB-Beh17, SB-Beh18, SB-Beh19, SB-Beh2, SB-Beh20, SB-Beh21, SB-Beh22, SB-Beh23, SB-Beh24, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh6, SB-Beh7, SB-Beh8, SB-Beh9, SB-FR5, SB-FR8, SB-V3, SB-V4

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

Related requirements:
  - SB-Beh10 (Normal_LevelRegulation): Normal mode is the standard operating mode: all physical units are believed sound, and the program keeps the water level between N1 and N2. Whenever the leve...
  - SB-Beh11 (Normal_to_Rescue): The program moves from normal mode to rescue mode as soon as it detects a failure of the water-level measuring device.
  - SB-Beh12 (Normal_to_Degraded): The program moves from normal mode to degraded mode as soon as it detects a failure of any physical unit other than the water-level measuring device.
  - SB-Beh13 (Normal_to_Emergency_LevelRisk): The program moves from normal mode to emergency stop mode if the water level risks reaching one of the limit quantities M1 or M2, this risk being judged agai...
  - ... (23 more)

### Issue 92: dafny_other — Dafny error: the name 'NORMAL' denotes a datatype constructor, but does not do so uniquely; a

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(377,?): Error: the name 'NORMAL' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.NORMAL')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 93: dafny_other — Dafny error: the name 'DEGRADED' denotes a datatype constructor, but does not do so uniquely;

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(380,?): Error: the name 'DEGRADED' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.DEGRADED')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 94: dafny_other — Dafny error: the name 'RESCUE' denotes a datatype constructor, but does not do so uniquely; a

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(383,?): Error: the name 'RESCUE' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.RESCUE')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

### Issue 95: dafny_other — Dafny error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uni

**Raw**
```
forge.transformations/output/SteamBoilerController.dfy(386,?): Error: the name 'EMERGENCY_STOP' denotes a datatype constructor, but does not do so uniquely; add an explicit qualification (for example, 'Mode.EMERGENCY_STOP')
```

**Fix directive**
Inspect the linked Java code; the generated Dafny contract could not be verified. The exact verifier message above is the most specific guide.

## Files to review
- ModeValue.java
- OperatingMode.java
- SteamBoilerController.java

## Next step
Read each issue above, follow the fix directive, edit the linked Java file, re-run Phase 5b (Dafny Generation), then Phase 6b (Dafny Verification).
