# post_codegen

- **status**: uncertain
- **summary**: Java written for all 53 requirements (single controller, 5 modes, every transition triggered by the per-cycle TransmissionReceived event, terminal emergency stop). Nothing is unimplemented; 14 choices the requirements do not settle are listed.
- **next_step**: Run the pipeline. The listed items are decisions to review, not known defects.

## Issues

### 1. [invented_default] Numeric values of the boiler, steam and pump constants

- **raw**: SB-DM1, SB-DM2 and SB-DM3 name C, M1, M2, N1, N2, W, U1, U2 and P but give no values.
- **fix_directive**: Chosen: C=1000, M1=150, M2=850, N1=400, N2=600 (litres), W=25 (l/s), U1=U2=2 (l/s/s), P=10 (l/s), cycle 5 s, 4 pumps, STOP limit 3. All are integral, so the model's rounding of fractional constants does not change any guard. Replace them with the plant's real values if known.
- **java_trace**:
  - `src/main/java/steam_boiler/BoilerConstants.java` lines 14-47, `BoilerConstants` — SB-DM1, SB-DM2, SB-DM3

### 2. [invented_default] Pump numbering and the pump returned when none qualifies

- **raw**: The messages name pump n without fixing the numbering; pumpToOpen()/pumpToClose() need a value of their declared (nat) type even when no pump qualifies.
- **fix_directive**: Pumps are numbered 1..4. pumpToOpen() returns 1 and pumpToClose() returns 4 when no pump qualifies; the controller only uses them under the guards openablePumpCount() > 0 / closablePumpCount() > 0, so the defaults are never transmitted.
- **java_trace**:
  - `src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 647-657, `pumpToOpen` — SB-DM3, SB-DM8
  - `src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 661-671, `pumpToClose` — SB-DM3, SB-DM8

### 3. [design_choice] One per-cycle trigger event; the transmission's contents are read through the sensor

- **raw**: SB-V3: all incoming messages of a cycle are received simultaneously, but step(InputEvent) takes one event.
- **fix_directive**: Added InputEvent.TransmissionReceived (not a physical-unit message) as the trigger of every transition: the cycle driver buffers the cycle's messages in BoilerSensor, analyses them, then calls step(TransmissionReceived). Every transition is event-triggered, so the model has no autonomous transition cycle (no divergence), and every non-terminal mode has a catch-all branch.
- **java_trace**:
  - `src/main/java/steam_boiler/event/InputEvent.java` lines 15, `TransmissionReceived` — SB-V3, SB-DM9
  - `src/main/java/steam_boiler/SteamBoilerProgram.java` lines 25-46, `cycle` — SB-V3

### 4. [design_choice] Variables q, v, p, the level estimate, unit status and STOP count live in the sensor layer

- **raw**: SB-V5..SB-V10 are variables; the profile allows guards to use sensor calls, constants and predicates.
- **fix_directive**: The controller's only state is its mode. q, v, p, the estimate bounds and the STOP count are fields of BoilerSensor; the per-unit status (out of order / announcement awaiting acknowledgement) is kept in UnitStatusBoard. The controller reads them through sensor methods, which the model treats as functions.
- **java_trace**:
  - `src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 46-78, `BoilerSensor` — SB-V5, SB-V6, SB-V7, SB-V8, SB-V10
  - `src/main/java/steam_boiler/sensor/UnitStatusBoard.java` lines 13-124, `UnitStatusBoard` — SB-V9

### 5. [design_choice] Failure announcements and repair acknowledgements are emitted by the unit-status board, not by step()

- **raw**: SB-FR6/SB-FR7: a variable set of per-unit messages each cycle, repeated until acknowledged.
- **fix_directive**: UnitStatusBoard.protocolMessages() builds, each cycle, every failure detection not yet acknowledged and the acknowledgement of each repair received in the cycle; the cycle driver transmits them with the controller's messages. They are therefore not part of the extracted state machine. After emergency stop nothing more is transmitted.
- **java_trace**:
  - `src/main/java/steam_boiler/sensor/UnitStatusBoard.java` lines 89-123, `protocolMessages` — SB-FR6, SB-FR7
  - `src/main/java/steam_boiler/SteamBoilerProgram.java` lines 40-41, `cycle` — SB-FR6, SB-FR7

### 6. [design_choice] Pump regulation law

- **raw**: SB-Beh10/SB-Beh15/SB-Beh19 require keeping the level in [N1, N2] by switching pumps but fix no control law.
- **fix_directive**: Each cycle at most one pump is changed: close one when the worst-case forecast maximum exceeds N2 (and the minimum does not fall below N1), open one in the symmetric case, otherwise hold. A cycle that changes mode holds the pumps (safe for that cycle because the level-risk check already covers the coming cycle).
- **java_trace**:
  - `src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 60-66, `step` — SB-Beh10, SB-Beh15, SB-Beh19

### 7. [design_choice] Level risk (SB-Beh13/18/21) judged on a worst-case forecast of the coming cycle

- **raw**: The requirements say 'risks reaching M1 or M2 under worst-case behaviour' without a formula.
- **fix_directive**: Forecast = current level bounds (q, or the estimate) minus worst-case steam over 5 s (rate bounded by W, U1, U2) plus pumps surely/possibly pouring (a pump counts as surely pouring only once its controller confirms flow; newly ordered pumps pour nothing for one cycle, SB-Beh25). Risk = forecast min <= M1 or forecast max >= M2. The same bounds give the expected range for the level-device dynamic check (SB-FR3) and, while the device is out of order, the estimate (SB-V8).
- **java_trace**:
  - `src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 53-58, `step` — SB-Beh13, SB-Beh18, SB-Beh21, SB-V4
  - `src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 188-224, `analyse` — SB-V8, SB-Beh25, SB-FR3

### 8. [design_choice] What counts as an aberrant message (SB-FR5)

- **raw**: SB-FR5 says 'a message whose presence is aberrant' without listing them.
- **fix_directive**: Aberrant: a data message duplicated for the same unit; a pump number outside 1..4; STEAM_BOILER_WAITING after initialization; PHYSICAL_UNITS_READY outside initialization or before PROGRAM_READY was sent; a repair message for a unit not out of order; a failure acknowledgement for a unit with no announcement pending. Missing: any of LEVEL, STEAM, or PUMP_STATE / PUMP_CONTROL_STATE for any of the four pumps.
- **java_trace**:
  - `src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 227-277, `analyse` — SB-FR5

### 9. [design_choice] Initialization procedure details

- **raw**: SB-Beh4/5/7 say drain above N2, fill below N1, announce PROGRAM_READY once within [N1, N2]; the valve's flow rate is not given.
- **fix_directive**: Above N2: send VALVE to open the valve if closed. Below N1: close the valve if open, else open one pump if none is on. Within [N1, N2]: close the valve or the filling pump if needed and send PROGRAM_READY, every cycle until PHYSICAL_UNITS_READY. While the valve is open the level-device dynamic check has no lower bound (drain rate unknown). In initialization a non-zero steam reading after STEAM_BOILER_WAITING also marks the steam device out of order.
- **java_trace**:
  - `src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 71-128, `step` — SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh7
  - `src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 333-351, `analyse` — SB-Beh3, SB-FR4

### 10. [design_choice] MODE(m) payload is a separate enum ModeIndicator

- **raw**: SB-DM10: the mode is a five-valued datum carried by MODE(m); the controller's mode enum is also its states.
- **fix_directive**: ModeIndicator (INITIALIZATION_MODE ... EMERGENCY_STOP_MODE) is the message payload; BoilerMode is the controller's state enum. Kept apart so that enumeration literals and state names do not clash.
- **java_trace**:
  - `src/main/java/steam_boiler/domain/ModeIndicator.java` lines 1-12, `ModeIndicator` — SB-DM10

### 11. [ambiguous_requirement] Emergency stop is terminal versus MODE(m) on every cycle

- **raw**: SB-Beh22: on entering emergency stop the program stops. SB-Beh24: MODE(m) is sent on every cycle in whatever mode.
- **fix_directive**: EMERGENCY_STOP has no outgoing transition. MODE(emergency_stop) is sent once, in the cycle that enters it; afterwards the program has stopped and transmits nothing.
- **java_trace**:
  - `src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 210-213, `step` — SB-Beh22, SB-Beh24
  - `src/main/java/steam_boiler/SteamBoilerProgram.java` lines 27-29, `cycle` — SB-Beh22

### 12. [ambiguous_requirement] 'Nothing else happens' before STEAM_BOILER_WAITING

- **raw**: SB-Beh2 says nothing else happens until STEAM_BOILER_WAITING arrives; SB-Beh9 and SB-Beh23 demand emergency stop on transmission failure and on three STOPs during initialization.
- **fix_directive**: Operator stop and transmission failure lead to emergency stop even before STEAM_BOILER_WAITING; everything else (steam check, level failure, filling, draining) waits for it.
- **java_trace**:
  - `src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 73, `step` — SB-Beh2, SB-Beh9, SB-Beh23

### 13. [ambiguous_requirement] Level failure together with a steam or pump-controller failure outside rescue mode

- **raw**: SB-Beh11/SB-Beh17 send a level failure to rescue; SB-Beh21 stops rescue on steam or pump-controller failure; SB-FR8 orders simultaneous consequences.
- **fix_directive**: In normal and degraded mode a level failure while the steam device or a pump controller is out of order goes directly to emergency stop (the rescue estimate would be untrustworthy at once). 'The pump-controller units fail' (SB-Beh21) is read as: any pump controller out of order.
- **java_trace**:
  - `src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 51-58, `step` — SB-FR8, SB-Beh11, SB-Beh17, SB-Beh21

### 14. [invented_default] STOP count saturates at the stop limit; p counts pumps with flow observed

- **raw**: SB-V10 describes the count without a bound; SB-V7 derives p without a formula.
- **fix_directive**: The STOP count stops growing at 3 (emergency stop is entered at 3). p = P x number of pumps reported on whose controller reports flow.
- **java_trace**:
  - `src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 190-196, `analyse` — SB-V10
  - `src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 450-465, `analyse` — SB-V7
