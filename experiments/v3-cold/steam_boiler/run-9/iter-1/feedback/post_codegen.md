# post_codegen

- **status**: uncertain
- **summary**: All 53 requirements are implemented. The requirements give no numeric constants, so values were chosen. Several behaviours the requirements leave open were resolved as listed below.

## Issues

### 1. [invented_default] Plant constants have no values in the requirements

- **raw**: SB-DM1..SB-DM3 name C, M1, M2, N1, N2, W, U1, U2 and P but give no values.
- **fix_directive**: Chosen C=1000, M1=100, M2=900, N1=400, N2=600 (litre); W=25 (l/s); U1=U2=2 (l/s/s); P=10 (l/s); 4 pumps; cycle 5 s; STOP threshold 3. All whole numbers, so the constants interface's rounding does not change them. Replace them with the real plant values when known.
- **java_trace**:
  - `src/main/java/steam_boiler/controller/BoilerConstants.java` lines 10-37, `BoilerConstants`: SB-DM1, SB-DM2, SB-DM3

### 2. [design_choice] One cycle = one TransmissionComplete event

- **raw**: SB-V3: all messages of a cycle are received simultaneously; step takes a single event.
- **fix_directive**: BoilerSensor.receive collects the cycle's messages; step is then called once with InputEvent.TransmissionComplete. Every transition is triggered by that event, so no transition is guard-only.
- **java_trace**:
  - `src/main/java/steam_boiler/event/InputEvent.java` lines 51-52, `TransmissionComplete`: SB-V3, SB-DM9
  - `src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 142-200, `receive`: SB-V3, SB-DM9

### 3. [design_choice] Variables and failure detection live in the sensor stage

- **raw**: SB-V5..SB-V10 and SB-FR1..SB-FR5 need per-unit, per-cycle history that a two-level if-else with simple predicates cannot express.
- **fix_directive**: BoilerSensor maintains q, v, p, the level bounds, the unit status and the STOP count, and judges failures. The controller reads them through sensor functions, so its own state is the mode plus two initialization flags.
- **java_trace**:
  - `src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 202-362, `analyse`: SB-V5, SB-V6, SB-V7, SB-V8, SB-V9, SB-V10, SB-FR1, SB-FR2, SB-FR3, SB-FR4, SB-FR5

### 4. [design_choice] Failure announcements and repair acknowledgements

- **raw**: SB-FR6/SB-FR7 require per-unit messages repeated until acknowledged.
- **fix_directive**: Each non-terminal branch calls actuator.reportUnitStatus(), which transmits sensor.statusMessagesDue(): unacknowledged detections and the acknowledgements for repairs received in this cycle.
- **java_trace**:
  - `src/main/java/steam_boiler/actuator/BoilerActuator.java` lines 85-91, `reportUnitStatus`: SB-FR6, SB-FR7
  - `src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 441-475, `statusMessagesDue`: SB-FR6, SB-FR7

### 5. [design_choice] Regulation policy

- **raw**: SB-Beh10/15/19 say pumps are switched on or off but not how many or when.
- **fix_directive**: All four pumps are ordered on when the level (or the estimate's midpoint) is below N1. They are all ordered off when the worst-case level at the next cycle exceeds N2. Orders are sent only when they change the commanded state. A branch that changes mode sends no pump orders. In rescue mode the widening estimate makes the pumps alternate on and off.
- **java_trace**:
  - `src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 59-60, `step`: SB-Beh10, SB-Beh15, SB-Beh19

### 6. [ambiguous_requirement] Level risk under worst-case behaviour

- **raw**: SB-Beh13/18/21 and SB-V4 do not define the worst case.
- **fix_directive**: Next-cycle low = lower bound - maximal steam volume (no pump assumed to pour). Next-cycle high = upper bound - minimal steam volume + P*5 s for each pump that is on, flowing or uncertain. Steam volume uses v*dt +/- U*dt^2/2, clamped to [0, W*dt].
- **java_trace**:
  - `src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 348-351, `analyseReadings`: SB-Beh13, SB-Beh18, SB-Beh21, SB-V4, SB-Beh25

### 7. [ambiguous_requirement] Transmission failure before STEAM_BOILER_WAITING

- **raw**: SB-Beh2 says nothing happens before STEAM_BOILER_WAITING; SB-Beh9/SB-FR5 require data every cycle.
- **fix_directive**: In initialization, transmission failure, non-zero steam and level failure cause emergency stop only once STEAM_BOILER_WAITING has arrived. Operator STOP applies always.
- **java_trace**:
  - `src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 51-52, `step`: SB-Beh2, SB-Beh3, SB-Beh6, SB-Beh9

### 8. [ambiguous_requirement] 'the pump-controller units fail' in rescue mode

- **raw**: SB-Beh21 does not say whether one controller failure suffices.
- **fix_directive**: Any pump controller out of order forces emergency stop from rescue mode.
- **java_trace**:
  - `src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 58-58, `step`: SB-Beh21, SB-FR8

### 9. [ambiguous_requirement] MODE after emergency stop

- **raw**: SB-Beh24 says MODE is sent every cycle; SB-Beh22 says the program stops.
- **fix_directive**: MODE(emergency stop) is sent in the cycle that enters the mode; nothing is sent afterwards.
- **java_trace**:
  - `src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 232-234, `step`: SB-Beh22, SB-Beh24

### 10. [invented_default] Initialization filling and draining

- **raw**: SB-Beh5 says 'a pump'; no evacuation rate is given for the valve.
- **fix_directive**: Pump 1 fills. The level-dynamics check is skipped during a cycle when the valve was open, because no evacuation rate is given. The valve is closed when the level is in [N1, N2] and when initialization is left.
- **java_trace**:
  - `src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 63-116, `step`: SB-Beh4, SB-Beh5, SB-Beh7
  - `src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 329-330, `analyseReadings`: SB-FR3, SB-DM7

### 11. [design_choice] Pump and pump-controller judgement timing

- **raw**: SB-FR1/SB-FR2 give the transmission after which a pump or a controller is judged.
- **fix_directive**: A pump fails when its reported state differs from its commanded state. A controller is judged only while its pump is sound, from the second transmission after an order. It fails when the reported flow differs from the commanded state. After a repair, a pump's reported state becomes the commanded state.
- **java_trace**:
  - `src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 272-286, `analyseReadings`: SB-FR1, SB-FR2, SB-Beh25

### 12. [design_choice] Aberrant messages

- **raw**: SB-FR5 does not list the aberrant messages.
- **fix_directive**: Aberrant: STEAM_BOILER_WAITING outside initialization. PHYSICAL_UNITS_READY not answering a PROGRAM_READY of the previous cycle. A repair notice for a unit that is not out of order. A failure acknowledgement with no pending announcement. A pump number outside 1..4. A data message missing or duplicated.
- **java_trace**:
  - `src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 202-250, `analyse`: SB-FR5, SB-FR7

## next_step

Run the pipeline; review the invented constants and the resolved ambiguities.
