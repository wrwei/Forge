# post_codegen

**status:** uncertain

**summary:** All 53 requirements are implemented and traced; 12 decisions the requirements did not settle are recorded below (2 invented defaults, 8 design choices, 1 ambiguity, 1 scope question).

## Issues

### 1. [invented_default] Numeric values of the boiler, steam and pump constants

- **raw:** The requirements name C, M1, M2, N1, N2, W, U1, U2 and P without values. Chosen: C=1000, M1=150, M2=850, N1=400, N2=600 (litre), W=10 (litre/sec), U1=U2=2 (litre/sec/sec), P=5 (litre/sec), cycle 5 s. The derived constants (STEAM_RISE_MARGIN, STEAM_FALL_MARGIN, STEAM_MAX_OUTFLOW, PUMPS_MAX_INFLOW, STEAM_RISE_PER_CYCLE, STEAM_FALL_PER_CYCLE) are literals computed from these. All of them are integers, so the model's rounding-up of fractional constants changes nothing.
- **fix_directive:** Replace with the plant's real values. Recompute the derived literals at the same time.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/controller/BoilerConstants.java` lines 15-53, `BoilerConstants`: SB-DM1, SB-DM2, SB-DM3

### 2. [invented_default] Level and steam values when the message is missing

- **raw:** levelValue() and steamValue() return 0.0 when LEVEL or STEAM is absent. A missing message is a transmission failure (SB-FR5), and that already forces emergency stop.
- **fix_directive:** None unless another default is wanted.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 40-56, `beginTransmission`: SB-DM5, SB-DM6, SB-FR5

### 3. [design_choice] One Transmission event per cycle; messages decoded by the sensor

- **raw:** SB-V3 says all messages of a cycle arrive simultaneously. The controller therefore steps once per cycle on InputEvent.Transmission and reads the decoded transmission through BoilerSensor. The SB-DM9 message records are decoded by the sensor and are not used as triggers.
- **fix_directive:** None.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 56-484, `step`: SB-V3, SB-DM9
  - `java.generated.project/src/main/java/steam_boiler/SteamBoilerProgram.java` lines 25-41, `cycle`: SB-V3

### 4. [scope_question] Unit-status protocol lives outside the controller's state machine

- **raw:** Per-unit status (SB-V9), the failure judgements (SB-FR1 to SB-FR5), the repeated failure announcements (SB-FR6) and the repair acknowledgements (SB-FR7) are in BoilerSensor, UnitRecord and PumpUnit. The controller sees them only as boolean sensor functions, so the extracted model does not contain those messages or the per-unit bookkeeping.
- **fix_directive:** Accept the abstraction, or ask for the per-unit protocol to be modelled.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 131-180, `analyse`: SB-V9, SB-FR1, SB-FR2, SB-FR3, SB-FR4, SB-FR5
  - `java.generated.project/src/main/java/steam_boiler/sensor/UnitRecord.java` lines 39-58, `settleNotices`: SB-FR6, SB-FR7

### 5. [design_choice] Regulation policy: all four pumps on or off

- **raw:** SB-Beh10 leaves the policy open. Below N1 all four pumps are opened, above N2 all four are closed, and between N1 and N2 nothing changes. Initialization fills with all four pumps (SB-Beh5 says 'a pump'). In rescue the decision uses the midpoint of the estimate.
- **fix_directive:** Replace with a finer policy if wanted.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 287-323, `step`: SB-Beh5, SB-Beh10, SB-Beh15, SB-Beh19

### 6. [design_choice] Worst-case risk prediction and level estimate

- **raw:** Risk (SB-Beh13, SB-Beh18, SB-Beh21) uses the next cycle's worst case. On the low side no water enters and steam rises by U1. On the high side all pumps pour and steam falls by U2. A defective steam device is treated as W on the low side and 0 on the high side. The estimate (SB-V8, SB-Beh19) advances each cycle as follows. The lower bound adds 5*p, where p counts the pumps known to pour, drops pumps just closed and excludes pumps just opened (SB-Beh25). The upper bound adds the pumps that may pour.
- **fix_directive:** Review the formulas against the plant's dynamics.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 82-119, `step`: SB-V4, SB-V8, SB-Beh13, SB-Beh19, SB-Beh25

### 7. [design_choice] Level failure with an untrustworthy estimate goes straight to emergency stop

- **raw:** In normal or degraded mode, a level failure while the steam device or a pump controller is out of order leads directly to emergency stop. The literal reading would go to rescue first, and rescue would then stop on the next cycle (SB-Beh21). The rescue estimate cannot be trusted in this case (SB-Beh19), so the level risk cannot be bounded.
- **fix_directive:** Route through RESCUE if the literal reading is wanted.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 95, `step`: SB-Beh11, SB-Beh17, SB-Beh21, SB-FR8

### 8. [ambiguous_requirement] 'the pump-controller units fail' in SB-Beh21

- **raw:** Read as: any one pump controller out of order.
- **fix_directive:** Confirm, or change it to all pump controllers.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 95, `step`: SB-Beh21

### 9. [design_choice] Which messages count as aberrant (SB-FR5)

- **raw:** The following count as aberrant: STEAM_BOILER_WAITING or PHYSICAL_UNITS_READY outside initialization; PHYSICAL_UNITS_READY before PROGRAM_READY; a pump number outside 1 to 4; a missing or duplicated data message; a failure acknowledgement with no detection outstanding; a repair notice for a sound unit. This strict reading stops the plant on a late repeated message.
- **fix_directive:** Relax it if the units can repeat messages after an acknowledgement.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 60-67, `step`: SB-FR5, SB-Beh9
  - `java.generated.project/src/main/java/steam_boiler/sensor/UnitRecord.java` lines 39-58, `settleNotices`: SB-FR5

### 10. [design_choice] Initial pump state and repair

- **raw:** Pumps are assumed off, with no order pending, at start-up, so a pump that is on at start is judged failed. A repaired pump is taken to be in the state it reports, and its controller gets one transmission to settle.
- **fix_directive:** None unless the plant can start with pumps on.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/sensor/PumpUnit.java` lines 16-61, `PumpUnit`: SB-FR1, SB-FR2

### 11. [design_choice] Initialization details

- **raw:** PROGRAM_READY is repeated on each cycle while the level stays in [N1, N2], and the pumps are closed while it is. Level failure detected before STEAM_BOILER_WAITING is acted on only once that message arrives (SB-Beh2). The level-failure check in initialization is the static range only, because the valve's drain rate is not given.
- **fix_directive:** None.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 121-249, `step`: SB-Beh2, SB-Beh6, SB-Beh7, SB-FR3

### 12. [design_choice] Emergency stop is terminal; MODE value type

- **raw:** EMERGENCY_STOP has no outgoing transition. The program sends MODE(emergency stop) once and then nothing more (SB-Beh22 takes precedence over SB-Beh24). MODE(m) carries a separate ModeValue enum rather than the controller's OperatingMode.
- **fix_directive:** None.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 481-484, `step`: SB-Beh22, SB-Beh24
  - `java.generated.project/src/main/java/steam_boiler/event/ModeValue.java` lines 4-10, `ModeValue`: SB-DM10

## next_step

Run the pipeline (compile, coverage, profile, transformations, verifiers).
