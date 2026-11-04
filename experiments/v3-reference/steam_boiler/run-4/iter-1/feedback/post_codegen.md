# post_codegen

**status:** uncertain

**summary:** All 53 requirements are implemented and traced. The project compiles. Nothing is unimplemented. 12 points are flagged where the requirements left a value or a choice open; each has been resolved in the Java.

## Issues

### 1. [invented_default] Numeric values of the plant constants

- **raw:** SB-DM1, SB-DM2 and SB-DM3 name C, M1, M2, N1, N2, W, U1, U2 and P but give no values. Chosen: C=1000 L, M1=100 L, N1=400 L, N2=600 L, M2=900 L, W=10 L/s, U1=U2=1 L/s^2, P=5 L/s, cycle 5 s, 4 pumps, STOP limit 3. All values are integral, so the model's rounding of fractional constants does not apply. With these values one cycle of worst-case evolution from [N1, N2] cannot reach M1 or M2.
- **fix_directive:** Replace the values in BoilerConstants if the plant's real constants are known; keep M1 < N1 < N2 < M2 < C.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/BoilerConstants.java` lines 14-47, `BoilerConstants`, requirements SB-DM1, SB-DM2, SB-DM3

### 2. [design_choice] Per-cycle trigger event TransmissionReceived

- **raw:** SB-V3 has all of a cycle's messages received simultaneously, but step(InputEvent) takes one event. The SB-DM9 messages are recorded by BoilerSensor.receive (the receive stage). step is then called once per cycle with InputEvent.TransmissionReceived, which the requirements do not name. Every transition is triggered by it, so no guard-only transition can cycle.
- **fix_directive:** None unless the cycle semantics should differ.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/event/InputEvent.java` lines 60, `TransmissionReceived`, requirements SB-V3
  - `java.generated.project/src/main/java/steam_boiler/SteamBoilerProgram.java` lines 29-50, `cycle`, requirements SB-V3

### 3. [design_choice] Failure detection, unit bookkeeping and the level estimate live in the sensor

- **raw:** SB-FR1 to SB-FR7, SB-V5 to SB-V10 and SB-Beh25 are implemented in BoilerSensor (analyse, protocolMessages, recordTransmitted). The controller sees only the summary queries, which the model treats as functions, so the extracted state machine stays small. Failure announcements and repair acknowledgements are emitted by the cycle driver, not by controller transitions.
- **fix_directive:** None.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 195-233, `analyse`, requirements SB-FR1, SB-FR2, SB-FR3, SB-FR4, SB-FR5, SB-V8, SB-V9, SB-V10
  - `java.generated.project/src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 500-519, `protocolMessages`, requirements SB-FR6, SB-FR7

### 4. [design_choice] Regulation policy

- **raw:** SB-Beh10, SB-Beh15 and SB-Beh19 say only that pumps are switched to keep the level within [N1, N2]. Chosen: below N1, OPEN_PUMP for all four pumps; above N2, CLOSE_PUMP for all four; within the band, no order. Rescue uses the middle of the estimate. No pump orders are sent on a cycle that changes mode. In initialization, 'activates a pump' (SB-Beh5) is realised by opening all four pumps.
- **fix_directive:** None unless a finer policy (a computed number of pumps) is wanted.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 68-240, `step`, requirements SB-Beh5, SB-Beh10, SB-Beh15, SB-Beh19

### 5. [ambiguous_requirement] MODE on every cycle versus the program stopping in emergency stop

- **raw:** SB-Beh24 asks for MODE(m) on every cycle; SB-Beh22 says the program stops on entering emergency stop. Resolved: MODE(EMERGENCY_STOP) is sent on the entering cycle. The mode is terminal, with no transitions, and the driver transmits nothing afterwards.
- **fix_directive:** None.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 241-243, `step`, requirements SB-Beh22, SB-Beh24
  - `java.generated.project/src/main/java/steam_boiler/SteamBoilerProgram.java` lines 30-32, `cycle`, requirements SB-Beh22

### 6. [ambiguous_requirement] What 'nothing else happens' allows before STEAM_BOILER_WAITING

- **raw:** SB-Beh2 says nothing else happens until STEAM_BOILER_WAITING arrives. Before it arrives the program still sends MODE (SB-Beh24) and still reacts to three STOPs (SB-Beh23) and to a transmission failure (SB-Beh9). The steam and level checks start on the cycle that carries STEAM_BOILER_WAITING.
- **fix_directive:** None.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 44-63, `step`, requirements SB-Beh2, SB-Beh3, SB-Beh6, SB-Beh9, SB-Beh23

### 7. [ambiguous_requirement] Precedence when entering rescue with a failed steam device or pump controller

- **raw:** SB-FR8 says emergency-stop conditions, including (in rescue mode) a steam-device or pump-controller failure, dominate a change to rescue. Read as: in normal or degraded mode, a level-device failure while the steam device or any pump controller is out of order leads to emergency stop, not rescue (levelLossFatal). 'The pump-controller units fail' (SB-Beh21) is read as any one pump controller.
- **fix_directive:** None.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 53-65, `step`, requirements SB-FR8, SB-Beh11, SB-Beh17, SB-Beh21

### 8. [design_choice] Which messages count as aberrant (SB-FR5)

- **raw:** Aberrant, and therefore a transmission failure: a duplicated data message, a pump number outside 1..4, PHYSICAL_UNITS_READY before PROGRAM_READY has been sent, STEAM_BOILER_WAITING or PHYSICAL_UNITS_READY outside initialization, a failure acknowledgement with no announcement pending, and a repair message for a unit not out of order. STEAM_BOILER_WAITING repeated during initialization is tolerated.
- **fix_directive:** None.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 195-306, `analyse`, requirements SB-FR5
  - `java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 45-46, `step`, requirements SB-FR5, SB-Beh9, SB-Beh14, SB-Beh18

### 9. [design_choice] Worst-case judgement of level risk and of reading plausibility

- **raw:** SB-Beh13 and SB-V4: the level is at risk when, over one more cycle, the lower bound minus the maximal steam outflow is below M1, or the upper bound plus all four pumps minus the minimal steam outflow is above M2. Steam outflow over a cycle is bounded from the last trusted rate with U1 and U2. SB-FR3: a reading outside the bounds advanced over the elapsed cycle is a failure. Level risk is not checked in initialization, which the requirements do not list.
- **fix_directive:** None.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 380-468, `advanceLevel/predictNextCycle`, requirements SB-V4, SB-V8, SB-Beh13, SB-FR3, SB-Beh25

### 10. [invented_default] Evacuation rate of the valve

- **raw:** SB-DM7 gives no drain rate. While the valve is open the lower bound of the level estimate is dropped to 0, so draining cannot cause a false level failure in initialization.
- **fix_directive:** Use the valve's rate if it becomes known.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 380-435, `advanceLevel`, requirements SB-DM7, SB-FR3

### 11. [design_choice] Timing of pump and pump-controller judgements

- **raw:** SB-FR1: a pump must show the ordered state in the next transmission. SB-FR2: the controller is not judged in the first transmission after an order (start-up latency, SB-Beh25), and from the second must show the ordered flow. As SB-FR2 is written, this applies to stop orders as well as start orders. An order that repeats the pump's expected state does not count as an order. A controller is judged only while its pump is sound.
- **fix_directive:** None.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 339-378, `judgePumps/judgePumpControls`, requirements SB-FR1, SB-FR2, SB-Beh25
  - `java.generated.project/src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 527-556, `recordTransmitted`, requirements SB-FR1, SB-FR2

### 12. [design_choice] Start-up handshake details

- **raw:** SB-Beh7: once the level is in [N1, N2] the program closes the valve if it is open, sends CLOSE_PUMP for all pumps and sends PROGRAM_READY, repeating these each cycle until PHYSICAL_UNITS_READY. SB-Beh3: the zero-steam check applies on every initialization cycle from STEAM_BOILER_WAITING onwards, including the cycle that carries PHYSICAL_UNITS_READY. SB-Beh8: 'defective unit' covers the steam device, the pumps and the pump controllers.
- **fix_directive:** None.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 68-139, `step`, requirements SB-Beh3, SB-Beh4, SB-Beh7, SB-Beh8

**next_step:** Run the pipeline (compile, coverage, profile check, transformations, FDR4, Dafny, Isabelle, vacuity) and fix the Java from its reports.
