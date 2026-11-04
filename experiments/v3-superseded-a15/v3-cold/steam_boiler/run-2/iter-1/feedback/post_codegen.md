# post_codegen

**status:** uncertain

**summary:** All 53 requirements are implemented and traced, and the code compiles under Java 17. Eleven decisions the requirements leave open are listed below: two invented defaults, six design choices, two ambiguities and one scope question.

## Issues

### 1. [invented_default] Numeric values of the boiler, steam and pump constants

- **raw:** SB-DM1, SB-DM2 and SB-DM3 name C, M1, M2, N1, N2, W, U1, U2 and P but give no values.
- **fix_directive:** Chosen: C=1000, M1=100, M2=900, N1=400, N2=600 (litre); W=15 (l/s); U1=U2=1 (l/s/s); P=5 (l/s). They satisfy M1<N1<N2<M2<C, M1 >= 5W and C-M2 >= 4*5P, as SB-DM1 implies. All are integers, so rounding in the model's constants interface changes nothing. Replace them with the real plant values when known.
- **java_trace:**
  - `src/main/java/steam_boiler/controller/BoilerConstants.java` lines 16-50, `BoilerConstants`, requirements: SB-DM1, SB-DM2, SB-DM3

### 2. [design_choice] One cycle trigger; the incoming messages are decoded by the sensor

- **raw:** SB-V3: all incoming messages of a cycle are received simultaneously; the profile gives step one event per call, once per cycle.
- **fix_directive:** step is triggered by InputEvent.TransmissionReceived once per cycle. The 15 SB-DM9 message records make up the transmission that BoilerSensor.receive decodes. The controller reads their content through sensor predicates. As a result, every transition in the extracted model has the same trigger, and the message events are declared but not used by the state machine.
- **java_trace:**
  - `src/main/java/steam_boiler/event/InputEvent.java` lines 20-20, `TransmissionReceived`, requirements: SB-V3, SB-DM9
  - `src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 87-115, `receive`, requirements: SB-V3, SB-DM9
  - `src/main/java/steam_boiler/SteamBoilerProgram.java` lines 26-37, `runCycle`, requirements: SB-V3

### 3. [scope_question] Unit bookkeeping, failure detection and the level variables live in the sensor, not in the state machine

- **raw:** SB-V5 to SB-V10 are 'variable' requirements and SB-FR1 to SB-FR7 describe per-unit detection, announcement and repair for ten units.
- **fix_directive:** Per-unit conditional updates cannot be written as the profile's straight-line transition actions without multiplying transitions. So q, v, p, the level bounds, the stop count and the unit status table (BoilerSensor, UnitStatus) are computed once per cycle in BoilerSensor.receive. The controller reads them through zero-argument getters. The controller's state variables are only valveOpened and programReadySent. In the model these quantities are uninterpreted sensor functions, so the verifiers check the mode logic over them, not the detection algorithms themselves.
- **java_trace:**
  - `src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 87-115, `receive`, requirements: SB-V5, SB-V6, SB-V7, SB-V8, SB-V9, SB-V10, SB-FR1, SB-FR2, SB-FR3, SB-FR4, SB-FR6, SB-FR7
  - `src/main/java/steam_boiler/sensor/UnitStatus.java` lines 15-92, `UnitStatus`, requirements: SB-V9, SB-FR6, SB-FR7

### 4. [design_choice] Worst-case level prediction used for the M1/M2 risk judgement

- **raw:** SB-Beh13/18/21 and SB-V4: emergency stop if the level risks reaching M1 or M2 under worst-case behaviour; SB-Beh25: pump start latency.
- **fix_directive:** predictedLevelMin = lower bound - min(5W, 5v + 12.5*U1), assuming no pump water in the next cycle because pumps may fail. predictedLevelMax = upper bound + 5P * (pumps on or out of order) - max(0, 5v - 12.5*U2). There is a risk when min <= M1 or max >= M2. A pump ordered on contributes nothing to the bounds until its first cycle has passed.
- **java_trace:**
  - `src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 62-64, `step`, requirements: SB-Beh13, SB-Beh18, SB-Beh21, SB-V4
  - `src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 510-558, `computeLevelBounds`, requirements: SB-V4, SB-V8, SB-Beh19, SB-Beh25

### 5. [design_choice] Regulation policy

- **raw:** SB-Beh10, SB-Beh15, SB-Beh19 say the program switches pumps on or off to keep the level between N1 and N2, but not how many.
- **fix_directive:** If the regulated level (q, or the middle of the estimate in rescue) is below N1, all four pumps are opened. Above N2, all four are closed. Between N1 and N2 the pumps are left as they are. On a cycle that changes the mode no pump orders are sent; the risk judgement covers pumps that are held.
- **java_trace:**
  - `src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 176-197, `step`, requirements: SB-Beh10
  - `src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 217-238, `step`, requirements: SB-Beh15
  - `src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 258-279, `step`, requirements: SB-Beh19

### 6. [design_choice] Level failure while rescue would be unusable goes straight to emergency stop

- **raw:** SB-FR8 orders simultaneous consequences; SB-Beh11/17 send a level failure to rescue; SB-Beh21 stops rescue when the steam device or pump controllers fail.
- **fix_directive:** In normal and degraded mode, a level-device failure while the steam device or any pump controller is out of order goes straight to emergency stop. It does not pass through one cycle of rescue with an unusable estimate.
- **java_trace:**
  - `src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 80-80, `step`, requirements: SB-FR8, SB-Beh11, SB-Beh17, SB-Beh21
  - `src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 167-169, `step`, requirements: SB-FR8
  - `src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 208-210, `step`, requirements: SB-FR8

### 7. [ambiguous_requirement] 'If the pump-controller units fail' in rescue mode

- **raw:** SB-Beh21: rescue -> emergency stop 'if the pump-controller units fail'.
- **fix_directive:** Read as: any pump controller is out of order, since the estimate then has no reliable pump information. It is not read as all four controllers.
- **java_trace:**
  - `src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 56-56, `step`, requirements: SB-Beh21
  - `src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 249-251, `step`, requirements: SB-Beh21

### 8. [ambiguous_requirement] Terminal emergency stop versus MODE on every cycle

- **raw:** SB-Beh22: on entering emergency stop the program stops. SB-Beh24: MODE(m) on every cycle, in whatever mode.
- **fix_directive:** MODE(emergency stop) is sent in the cycle in which the mode is entered. The EMERGENCY_STOP block has no transitions, and SteamBoilerProgram.runCycle does nothing once that mode is reached.
- **java_trace:**
  - `src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 280-282, `step`, requirements: SB-Beh22, SB-Beh24
  - `src/main/java/steam_boiler/SteamBoilerProgram.java` lines 26-30, `runCycle`, requirements: SB-Beh22

### 9. [design_choice] Initialization details

- **raw:** SB-Beh2 to SB-Beh9 leave the timing of checks, the pump used and the end of filling open.
- **fix_directive:** (a) The zero-steam check (SB-Beh3) runs on every initialization cycle from STEAM_BOILER_WAITING on, not only once. (b) STOP and transmission failure are checked even before STEAM_BOILER_WAITING; unit failure judgement starts with it. (c) Filling uses the lowest-numbered pump whose pump and controller are sound. (d) When the level reaches [N1, N2] the program closes the valve if open, closes all pumps and starts PROGRAM_READY; after that the level is not adjusted again. (e) VALVE is treated as a toggle and sent once to open and once to close.
- **java_trace:**
  - `src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 69-77, `step`, requirements: SB-Beh2, SB-Beh3, SB-Beh6, SB-Beh9
  - `src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 108-155, `step`, requirements: SB-Beh4, SB-Beh5, SB-Beh7
  - `src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 224-226, `fillPump`, requirements: SB-Beh5

### 10. [design_choice] Which messages count as aberrant (transmission failure)

- **raw:** SB-FR5: a message whose presence is aberrant, or a missing indispensable message.
- **fix_directive:** A transmission counts as failed if: a data message is missing or duplicated; a pump number is outside 1..4; a repair message names a unit that is not out of order; a failure acknowledgement arrives with no announcement outstanding; STEAM_BOILER_WAITING or PHYSICAL_UNITS_READY arrives outside initialization; or PHYSICAL_UNITS_READY arrives before PROGRAM_READY was sent.
- **java_trace:**
  - `src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 228-256, `decode`, requirements: SB-FR5
  - `src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 359-384, `processRepairs`, requirements: SB-FR5, SB-FR7
  - `src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 69-79, `step`, requirements: SB-FR5, SB-Beh9, SB-Beh14

### 11. [invented_default] Tolerances and windows in failure detection

- **raw:** SB-FR1 to SB-FR4 give the detection rules without tolerances, and nothing bounds the drain rate of the evacuation valve.
- **fix_directive:** The checks are exact, with no measurement tolerance. A pump controller is excused only in the first transmission after an order that changes its pump's state. A pump with no order must keep its previous state. While the valve is open, the lower bound of the level-dynamics check is skipped and the estimate's lower bound resets to 0. A unit's history is forgotten when it is repaired.
- **java_trace:**
  - `src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 406-476, `detectFailures`, requirements: SB-FR1, SB-FR2, SB-FR3, SB-FR4

## next_step

Run the pipeline (compile, coverage, profile check, transformations, FDR4, Dafny, Isabelle, vacuity). Revisit the listed choices only if a verifier or reviewer objects to them.
