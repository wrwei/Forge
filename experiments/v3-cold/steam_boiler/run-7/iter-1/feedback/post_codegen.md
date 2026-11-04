# post_codegen

**status:** uncertain

**summary:** Steam-boiler controller written in five layers: 16 Java files, 53/53 requirements traced, compiles with javac --release 17. Eleven points the requirements did not settle are recorded below: 2 invented defaults, 5 design choices, 4 ambiguities.

## Issues

### 1. [invented_default] Numeric values of the physical constants

- **raw:** SB-DM1, SB-DM2 and SB-DM3 name C, M1, M2, N1, N2, W, U1, U2 and P but give no values.
- **fix_directive:** Chosen C=1000, M1=100, M2=900, N1=400, N2=600 (litre), W=10 (l/s), U1=U2=1 (l/s/s), P=5 (l/s), cycle 5 s. All are integral, so the model's rounding of constants changes nothing. Replace with the plant's real values when known; the guards only rely on M1 < N1 < N2 < M2.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/BoilerConstants.java` lines 13-46, `BoilerConstants` — SB-DM1, SB-DM2, SB-DM3

### 2. [invented_default] Sensor values when no reading is available

- **raw:** SB-V5, SB-V6 and SB-V8 do not say what q, v or the estimate are before the first reading or in a cycle whose transmission failed.
- **fix_directive:** Each query keeps its last published value: q and v start at 0.0, the level bounds at [0, C]. They are only consulted after a complete transmission has been analysed; a failed transmission sends the controller to emergency stop.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 13-27, `BoilerSensor` — SB-V5, SB-V6, SB-V8

### 3. [design_choice] One cycle trigger instead of per-message events

- **raw:** SB-V3: all messages of a cycle are received simultaneously, but the profile's step takes one event.
- **fix_directive:** step is called once per cycle with InputEvent.TransmissionReceived, a trigger not listed in SB-DM9. The SB-DM9 messages are records of InputEvent, delivered to CycleAnalyser.receive. Every transition of the model uses this one trigger; its guards read what was received through BoilerSensor.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/SteamBoilerProgram.java` lines 26-45, `runCycle` — SB-V3
  - `java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 41-258, `step` — SB-V3

### 4. [design_choice] Per-unit detection and bookkeeping kept out of the state machine

- **raw:** SB-V9, SB-V10 and SB-FR1 to SB-FR7 need per-unit state and conditional per-unit messages, which the profile's unconditional transition actions cannot express.
- **fix_directive:** CycleAnalyser and UnitStatus implement failure detection, the failed/acknowledged bookkeeping of each of the ten units, the STOP count, the transmission check, FAILURE_DETECTION repetition and REPAIRED_ACKNOWLEDGEMENT. The controller sees aggregated queries (levelDeviceFailed, pumpFailed, consecutiveStopCount, ...), so the verifiers check the mode logic over these as uninterpreted inputs, not the detection logic itself.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/sensor/CycleAnalyser.java` lines 175-255, `analyse` — SB-V9, SB-V10, SB-FR1, SB-FR2, SB-FR3, SB-FR4, SB-FR5
  - `java.generated.project/src/main/java/steam_boiler/sensor/CycleAnalyser.java` lines 262-293, `protocolMessages` — SB-FR6, SB-FR7

### 5. [design_choice] Regulation strategy: all four pumps on below N1, off above N2

- **raw:** SB-Beh10 says pumps are switched on or off to correct the level without saying how many; SB-Beh5 says 'activates a pump'.
- **fix_directive:** Normal and degraded mode open all four pumps when q < N1, close all four when q > N2, and change nothing in between. Rescue does the same on the midpoint of the estimate bounds. Initialization fills with all four pumps (robust to one failed pump). Orders are sent to failed pumps too. Mode-change cycles emit only MODE; regulation resumes in the next cycle.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 168-254, `step` — SB-Beh5, SB-Beh10, SB-Beh15, SB-Beh19

### 6. [design_choice] Worst-case risk of reaching M1 or M2

- **raw:** SB-V4, SB-Beh13, SB-Beh18 and SB-Beh21 require judging the risk against worst-case behaviour without giving the formula.
- **fix_directive:** Risk if (lower bound - maximal steam outflow over the next cycle) <= M1, or (upper bound + supply of pumps now on or flowing - minimal steam outflow) >= M2. Steam outflow is bounded by U1, U2 and W from the current reading, or taken as 0..5W when the steam device is out of order. Pumps that are off contribute nothing in the next cycle (SB-Beh25).
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 65-66, `levelAtRisk` — SB-V4, SB-Beh13, SB-Beh18, SB-Beh21, SB-Beh25
  - `java.generated.project/src/main/java/steam_boiler/sensor/CycleAnalyser.java` lines 437-456, `predictNextCycle` — SB-V4, SB-Beh25

### 7. [ambiguous_requirement] 'If the pump-controller units fail' in rescue mode

- **raw:** SB-Beh21 does not say whether one or all pump controllers must fail; SB-FR8 orders precedence 'in rescue mode' only.
- **fix_directive:** Read as: any pump controller out of order. The same condition, together with a failed steam device, also applies on the way into rescue: a level failure while the estimate is already untrustworthy goes straight to emergency stop from normal or degraded mode.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 53-91, `estimateUnreliable` — SB-Beh21, SB-FR8, SB-Beh11, SB-Beh17

### 8. [ambiguous_requirement] Which messages are aberrant

- **raw:** SB-FR5 calls a transmission failed when 'a message arrives whose presence is aberrant' without listing them.
- **fix_directive:** Aberrant: a missing or duplicated LEVEL, STEAM, PUMP_STATE(n) or PUMP_CONTROL_STATE(n); a pump number outside 1-4; STEAM_BOILER_WAITING or PHYSICAL_UNITS_READY outside initialization; PHYSICAL_UNITS_READY before PROGRAM_READY. A repair message for a sound unit is re-acknowledged, and an acknowledgement nothing awaits is ignored; neither counts as a transmission failure.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/sensor/CycleAnalyser.java` lines 324-332, `transmissionIncomplete` — SB-FR5
  - `java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 47-72, `initMessageMisplaced` — SB-FR5, SB-Beh9, SB-Beh14

### 9. [ambiguous_requirement] What happens in initialization before STEAM_BOILER_WAITING

- **raw:** SB-Beh2 says nothing else happens until STEAM_BOILER_WAITING; SB-Beh23 and SB-Beh24 apply in every mode.
- **fix_directive:** Before STEAM_BOILER_WAITING the program only counts STOP and sends MODE(initialization); transmission, steam and level checks start in the cycle that brings STEAM_BOILER_WAITING. A repeated STEAM_BOILER_WAITING during initialization is tolerated.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/sensor/CycleAnalyser.java` lines 189-192, `analyse` — SB-Beh2
  - `java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 72-74, `initEmergency` — SB-Beh2, SB-Beh23

### 10. [ambiguous_requirement] Pump and pump-controller failure timing

- **raw:** SB-FR1 and SB-FR2 fix the transmission at which a pump or controller is judged after an order, but not how repeated orders or failed pumps interact.
- **fix_directive:** A pump is judged every cycle against its expected state (last order that changed it, else its last report). A controller is judged from the second transmission after a state-changing order onward, only while its pump is sound, and must then report flow exactly when the pump reports on; repeating an order that does not change the expected state does not restart the window.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/sensor/CycleAnalyser.java` lines 378-397, `detectPumpControlFailures` — SB-FR1, SB-FR2, SB-Beh25

### 11. [design_choice] Emergency stop is terminal and silent

- **raw:** SB-Beh22 says the program stops in emergency stop; SB-Beh24 says MODE is sent every cycle in whatever mode.
- **fix_directive:** MODE(emergency stop) is sent once, on entry; the state has no outgoing transition and the program emits nothing afterwards.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 255-257, `step` — SB-Beh22, SB-Beh24
  - `java.generated.project/src/main/java/steam_boiler/SteamBoilerProgram.java` lines 28-30, `runCycle` — SB-Beh22

## next_step

Run the pipeline. If a verifier fails, read its report against the design choices above before changing the controller.
