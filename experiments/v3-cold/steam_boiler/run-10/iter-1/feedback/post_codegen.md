# post_codegen

**status:** uncertain

**summary:** Steam boiler controller written in five layers: 14 Java files, 53 of 53 requirements traced. The mode logic is the controller's step: five modes, 40 transitions, all triggered by the per-cycle CycleStart event, with mutually exclusive, exhaustive guards per mode. Failure detection and the unit bookkeeping are in PlantMonitor. 13 items need confirmation: invented constants, interpretations of SB-Beh2/SB-Beh9, SB-Beh22/SB-Beh24, SB-Beh21 and SB-FR5, and design choices.

## Issues

### 1. [invented_default] Values of the boiler, steam and pump constants

- **raw:** SB-DM1, SB-DM2 and SB-DM3 name C, M1, M2, N1, N2, W, U1, U2 and P but give no values. Chosen: C=1000, M1=100, N1=400, N2=600, M2=900 litres; W=10 l/s; U1=U2=1 l/s/s; P=5 l/s. Given by the requirements: 4 pumps, 5 s cycle, 3 successive STOPs. All values are integral, so rounding in the model constants interface changes none of them.
- **fix_directive:** Replace with the plant's real values if known. Keep M1 < N1 < N2 < M2 < C. With bang-bang regulation, the worst-case one-cycle rise from four pumps (4 x P x 5 s) must stay below M2 - N2, and about two cycles of maximal steam must stay below N1 - M1, or the risk rule stops the boiler during ordinary regulation.
- **java_trace:**
  - `src/main/java/steam_boiler/BoilerConstants.java` lines 11-50, `BoilerConstants`, requirements SB-DM1, SB-DM2, SB-DM3

### 2. [invented_default] Pump numbering and analysed values before the first cycle

- **raw:** The requirements do not number the pumps. Pumps and pump controllers are numbered 0 to 3. Before the first analysed cycle, the sensor reports level, steam, estimate and predicted levels of 0.0 and all units sound. The controller reads none of these until STEAM_BOILER_WAITING has arrived.
- **fix_directive:** If the plant numbers pumps 1 to 4, shift the index in the monitor and in the controller's OpenPump and ClosePump literals.
- **java_trace:**
  - `src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 10-24, `BoilerSensor`, requirements SB-V5, SB-V6, SB-V8, SB-V9
  - `src/main/java/steam_boiler/domain/PumpReport.java` lines 1-14, `PumpReport`, requirements SB-DM3

### 3. [design_choice] One cycle event triggers every transition; the cycle's messages reach the controller through the sensor

- **raw:** SB-V3 says all messages of a cycle are received simultaneously. The program collects them in PlantMonitor and analyses them. The controller's step runs once per cycle on InputEvent.CycleStart, with guards over BoilerSensor queries. All transitions are triggered by CycleStart, so there are no autonomous transitions. Each mode's guards are mutually exclusive and together exhaustive. The other InputEvent records are messages to the monitor, not triggers.
- **fix_directive:** None expected. If a verifier needs the individual messages as model events, they would have to become triggers, which would break the simultaneous-reception rule.
- **java_trace:**
  - `src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 42-321, `step`, requirements SB-V3
  - `src/main/java/steam_boiler/event/InputEvent.java` lines 13-14, `CycleStart`, requirements SB-V3

### 4. [scope_question] Failure detection, the repair and acknowledgement protocol, and level estimation are outside the extracted state machine

- **raw:** SB-FR1 to SB-FR7, SB-V5 to SB-V10 and the estimate of SB-Beh19 are implemented in PlantMonitor (Java only). The controller sees their results only as sensor functions: levelDeviceFailed, transmissionFailure, predictedMinimumLevel and the others. The program driver sends the FAILURE_DETECTION and REPAIRED_ACKNOWLEDGEMENT messages after step, from the monitor's unit status, because one transition cannot send a variable subset of 20 per-unit messages within the profile. The verifiers therefore check the mode logic, not the detection rules.
- **fix_directive:** Accept, or move selected bookkeeping into controller fields if the model must cover it (at the cost of state space).
- **java_trace:**
  - `src/main/java/steam_boiler/monitor/PlantMonitor.java` lines 177-205, `analyse`, requirements SB-FR1, SB-FR2, SB-FR3, SB-FR4, SB-FR5
  - `src/main/java/steam_boiler/monitor/PlantMonitor.java` lines 211-240, `unitStatusMessages`, requirements SB-FR6, SB-FR7
  - `src/main/java/steam_boiler/SteamBoilerProgram.java` lines 39-57, `runCycle`, requirements SB-FR6, SB-FR7

### 5. [design_choice] Regulation policy: all four pumps on below N1, all off above N2

- **raw:** SB-Beh10, SB-Beh15 and SB-Beh19 require switching pumps on or off without naming which ones. Below N1 the program sends OPEN_PUMP for all four pumps; above N2 it sends CLOSE_PUMP for all four; between N1 and N2 it sends no pump order. In rescue mode the middle of the estimated interval is used. On a cycle that changes mode, no pump order is sent.
- **fix_directive:** A finer policy (a number of pumps chosen from the predicted level) would need per-pump branches or computed pump numbers.
- **java_trace:**
  - `src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 194-218, `step`, requirements SB-Beh10
  - `src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 245-266, `step`, requirements SB-Beh15
  - `src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 293-316, `step`, requirements SB-Beh19

### 6. [design_choice] How the risk of reaching M1 or M2 is judged

- **raw:** SB-Beh13, SB-Beh18 and SB-Beh21 say "risks reaching M1 or M2" under worst-case behaviour. The program predicts the level range at the next cycle. It starts from the current level, or the estimated interval. Steam may change at its maximal gradients (anywhere in [0, W] if the steam device is down). Inflow lies in [0, P x 5 s x pumps now on]; a pump not yet on contributes nothing, because it takes one cycle to start (SB-Beh25). Emergency stop follows if the predicted minimum is at or below M1, or the predicted maximum at or above M2. The prediction does not credit the program's own pump orders of this cycle.
- **fix_directive:** Confirm the intended reading of "risk".
- **java_trace:**
  - `src/main/java/steam_boiler/monitor/PlantMonitor.java` lines 482-508, `predictLevel`, requirements SB-Beh13, SB-Beh25, SB-V4
  - `src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 59-62, `step`, requirements SB-Beh13, SB-Beh18, SB-Beh21

### 7. [ambiguous_requirement] Initialization before STEAM_BOILER_WAITING: transmission failures not checked

- **raw:** SB-Beh2 says nothing else happens until STEAM_BOILER_WAITING arrives; SB-Beh9 and SB-FR5 say a transmission failure during initialization forces emergency stop, and that the data messages are required in every transmission. Resolved: before the plant signals waiting, the program only counts STOP messages (SB-Beh23 applies in every mode) and sends MODE. Transmission, steam and level checks start in the cycle STEAM_BOILER_WAITING arrives, and the steam-zero check (SB-Beh3) is repeated on every cycle of initialization after that.
- **fix_directive:** If transmission failures must be detected from the very first cycle, remove the waitingSignalled gate in PlantMonitor.analyse and plantWaiting from initTransmissionStop.
- **java_trace:**
  - `src/main/java/steam_boiler/monitor/PlantMonitor.java` lines 177-205, `analyse`, requirements SB-Beh2, SB-Beh9
  - `src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 72-81, `step`, requirements SB-Beh2, SB-Beh3, SB-Beh9

### 8. [ambiguous_requirement] MODE on every cycle versus a terminal emergency stop

- **raw:** SB-Beh24 requires MODE(m) on every cycle in whatever mode; SB-Beh22 says that on entering emergency stop the program stops. Resolved: MODE(EMERGENCY_STOP) is sent on the cycle that enters emergency stop, which has no outgoing transition. After that the program runs no more cycles and transmits nothing.
- **fix_directive:** If MODE must keep being sent after emergency stop, add a CycleStart self-transition to EMERGENCY_STOP that sends ModeReport, and remove the hasStopped gate in SteamBoilerProgram.runCycle.
- **java_trace:**
  - `src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 318-320, `step`, requirements SB-Beh22, SB-Beh24
  - `src/main/java/steam_boiler/SteamBoilerProgram.java` lines 39-43, `runCycle`, requirements SB-Beh22

### 9. [ambiguous_requirement] "If the pump-controller units fail" in rescue mode

- **raw:** SB-Beh21 does not say whether one failed pump controller suffices. Resolved: any pump controller out of order forces emergency stop from rescue, because the estimate relies on the pump-controller information (SB-Beh19).
- **fix_directive:** If only the failure of all controllers is meant, the sensor should report that instead of pumpControlFailed.
- **java_trace:**
  - `src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 65-65, `step`, requirements SB-Beh21
  - `src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 117-120, `pumpControlFailed`, requirements SB-Beh21

### 10. [design_choice] A level-device failure while rescue cannot run goes straight to emergency stop

- **raw:** SB-Beh11 and SB-Beh17 send the program to rescue on a level-device failure. SB-Beh21 stops rescue if the steam device or a pump controller is out of order, and SB-FR8 lets emergency conditions dominate a change to rescue. Resolved: in normal and degraded modes, if the level device fails while the steam device or a pump controller is already out of order, the program enters emergency stop directly instead of passing through rescue for one cycle.
- **fix_directive:** If a one-cycle stay in rescue is wanted, drop the levelLossBlocked branches.
- **java_trace:**
  - `src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 84-85, `step`, requirements SB-FR8, SB-Beh11, SB-Beh17, SB-Beh21

### 11. [design_choice] Which messages count as aberrant

- **raw:** SB-FR5 does not list the aberrant messages. Treated as transmission failures: a duplicated LEVEL, STEAM, PUMP_STATE or PUMP_CONTROL_STATE in one transmission; a pump number outside 0..3; STEAM_BOILER_WAITING outside initialization; PHYSICAL_UNITS_READY outside initialization or before PROGRAM_READY has been sent; a REPAIRED message for a unit that is not out of order. Tolerated (ignored): a failure acknowledgement with no announcement outstanding, and a repeated STOP or STEAM_BOILER_WAITING within one transmission. A repair report ends any outstanding failure announcement for that unit.
- **fix_directive:** Adjust detectTransmissionFailure and processRepairsAndAcknowledgements if the plant protocol differs.
- **java_trace:**
  - `src/main/java/steam_boiler/monitor/PlantMonitor.java` lines 314-333, `detectTransmissionFailure`, requirements SB-FR5
  - `src/main/java/steam_boiler/monitor/PlantMonitor.java` lines 340-371, `processRepairsAndAcknowledgements`, requirements SB-FR5, SB-FR6, SB-FR7

### 12. [design_choice] Failure-detection timing for pumps and pump controllers

- **raw:** SB-FR1: a pump must report, in the next transmission, the state it was last ordered to; with no order, the state it reported before. SB-FR2: a pump controller is judged only when its pump is sound and has kept the same state over the last two transmissions, which is the second transmission after an order. It must then report flow exactly when the pump is on. Units already out of order, or repaired in this cycle, are not judged. Level dynamics (SB-FR3) are checked against the previous cycle's worst-case prediction; while the evacuation valve is open the lower bound is 0, because no drainage rate is given.
- **fix_directive:** None expected.
- **java_trace:**
  - `src/main/java/steam_boiler/monitor/PlantMonitor.java` lines 399-423, `detectPumpFailures`, requirements SB-FR1, SB-FR2, SB-Beh25
  - `src/main/java/steam_boiler/monitor/PlantMonitor.java` lines 444-456, `detectLevelFailure`, requirements SB-FR3

### 13. [design_choice] Initialization adjustments use pump 0 and the valve toggle

- **raw:** SB-Beh5 says "activates a pump": pump 0 is used. When the level is above N2, the valve is opened (VALVE toggles it) and pump 0 is closed. When the level is back between N1 and N2, the valve is closed if open, pump 0 is closed, and PROGRAM_READY is sent on every cycle until PHYSICAL_UNITS_READY arrives. If the level falls below N1 with the valve open, the valve is closed and pump 0 opened. The program knows the valve's state only from the VALVE messages it has sent.
- **fix_directive:** None expected.
- **java_trace:**
  - `src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 127-168, `step`, requirements SB-Beh4, SB-Beh5, SB-Beh7

## next_step

Run the pipeline (compile, coverage, profile check, transformations, FDR4, Dafny, Isabelle, vacuity).
