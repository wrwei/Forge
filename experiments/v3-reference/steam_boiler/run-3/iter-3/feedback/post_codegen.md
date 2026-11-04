# post_codegen

**status:** uncertain

**summary:** All 53 requirements are implemented and traced. The controller decides the five modes with priority-ordered guards and an unguarded last branch per non-terminal mode; level regulation, plant analysis and the per-unit message protocol are outside the extracted model. The issues below record values and choices the requirements leave open.

## Issues

### 1. [invented_default] Numeric values of the boiler, steam and pump constants

- **raw:** SB-DM1, SB-DM2 and SB-DM3 name C, M1, M2, N1, N2, W, U1, U2 and P but give no values.
- **fix_directive:** Chosen: C=1000, M1=150, M2=850, N1=400, N2=600 (litres); W=25 l/s; U1=U2=4 l/s/s; P=10 l/s; cycle and pump latency 5 s; STOP_REPETITIONS=3. All integral. Replace if the plant's real values are known.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/controller/BoilerConstants.java` lines 10-53, `BoilerConstants`, requirements SB-DM1, SB-DM2, SB-DM3, SB-V3, SB-V10

### 2. [invented_default] Pump numbering 1 to 4

- **raw:** SB-DM3/SB-DM8/SB-DM9 refer to pump n without fixing the numbering.
- **fix_directive:** Pumps are numbered 1..4; a per-pump message with another number is aberrant (transmission failure).
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/controller/BoilerConstants.java` lines 40-40, `PUMP_COUNT`, requirements SB-DM3, SB-DM8, SB-DM9

### 3. [design_choice] The extracted controller decides modes only; regulation and plant analysis are outside it

- **raw:** Iterations 1 and 2 put the initialization procedure and the pump regulation in the controller (28 to 34 transitions, 12 to 14 sensor functions); FDR4 ran out of memory both times (81 GB, 71 GB) without a verdict.
- **fix_directive:** SteamBoilerController keeps every mode transition (SB-Beh1-3, 6, 8, 9, 11-14, 16-18, 20-23, SB-FR8) and MODE on every cycle: 16 transitions over 6 boolean sensor facts, no other state variable. LevelRegulator issues VALVE, OPEN/CLOSE_PUMP and PROGRAM_READY for the mode the controller has just chosen (SB-Beh4, 5, 7, 10, 15, 19); PlantMonitor does reception, failure judgement, unit status and the FR6/FR7 messages. These parts are not in the extracted model and are not verified by FDR4/Isabelle.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 38-132, `step`, requirements SB-V1, SB-V2
  - `java.generated.project/src/main/java/steam_boiler/monitor/LevelRegulator.java` lines 18-84, `LevelRegulator`, requirements SB-Beh4, SB-Beh5, SB-Beh7, SB-Beh10, SB-Beh15, SB-Beh19
  - `java.generated.project/src/main/java/steam_boiler/monitor/PlantMonitor.java` lines 29-620, `PlantMonitor`, requirements SB-V5, SB-V6, SB-V7, SB-V8, SB-V9, SB-V10, SB-FR1, SB-FR2, SB-FR3, SB-FR4, SB-FR5, SB-FR6, SB-FR7

### 4. [design_choice] One controller step per cycle, triggered by TransmissionReceived

- **raw:** SB-V3: all incoming messages of a cycle are received simultaneously, then analysed, then all outgoing messages emitted.
- **fix_directive:** SteamBoilerProgram.runCycle hands the whole transmission to PlantMonitor.receive, calls step(TransmissionReceived) once, adds the regulator's orders and the monitor's protocol messages, and emits them together. The units' messages are UnitMessage values, not model events.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/SteamBoilerProgram.java` lines 36-53, `runCycle`, requirements SB-V3, SB-DM9
  - `java.generated.project/src/main/java/steam_boiler/message/UnitMessage.java` lines 1-140, `UnitMessage`, requirements SB-DM9

### 5. [design_choice] Regulation: all four pumps together, on the level predicted for the end of the cycle

- **raw:** SB-Beh10: correct the level by switching pumps on or off; the strategy is open. Regulating on the current level lets the one-cycle start-up latency (SB-Beh25) carry the level to the M1 risk threshold at maximal steam rate.
- **fix_directive:** In normal, degraded and rescue mode all pumps are opened when the predicted level is below N1 and closed when it is above N2; otherwise they are left as they are. Regulation also applies in the cycle of a mode change, for the new mode.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/monitor/LevelRegulator.java` lines 73-83, `pumpOrders`, requirements SB-Beh10, SB-Beh15, SB-Beh19
  - `java.generated.project/src/main/java/steam_boiler/monitor/PlantMonitor.java` lines 464-490, `predictedLevelBelowNormal`, requirements SB-Beh10, SB-Beh25

### 6. [design_choice] Initialization fills with pump 1 only

- **raw:** SB-Beh5: 'activates a pump'.
- **fix_directive:** OPEN_PUMP(1) while the level is below N1; CLOSE_PUMP(1) when the valve opens or PROGRAM_READY is first sent. If pump 1 is out of order the filling stalls until the operator stops the program.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/monitor/LevelRegulator.java` lines 40-71, `initialisationOrders`, requirements SB-Beh4, SB-Beh5, SB-Beh7

### 7. [ambiguous_requirement] Level failure while the estimate is unreliable; 'the pump-controller units fail'

- **raw:** SB-Beh11/SB-Beh17 send a level failure to rescue; SB-Beh21 and SB-FR8 make steam or pump-controller failure an emergency in rescue.
- **fix_directive:** A level failure in normal or degraded mode while the steam device or any pump controller is out of order goes straight to emergency stop. 'Pump-controller units fail' is read as any one controller.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 47-49, `operatingAbort/rescueAbort`, requirements SB-Beh11, SB-Beh17, SB-Beh21, SB-FR8

### 8. [ambiguous_requirement] When the zero-steam check applies

- **raw:** SB-Beh3: once STEAM_BOILER_WAITING has arrived, check the steam rate is zero.
- **fix_directive:** Checked in every initialization cycle from STEAM_BOILER_WAITING on (exact comparison v == 0.0), not only in the cycle it arrives.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/monitor/PlantMonitor.java` lines 375-389, `initialisationAbort/startupCheckFailed`, requirements SB-Beh3, SB-Beh6

### 9. [design_choice] What counts as an aberrant message

- **raw:** SB-FR5 gives only the four indispensable data messages as an example.
- **fix_directive:** Aberrant: missing or duplicated LEVEL, STEAM, PUMP_STATE(n), PUMP_CONTROL_STATE(n); a pump number outside 1..4; STEAM_BOILER_WAITING or PHYSICAL_UNITS_READY outside initialization; PHYSICAL_UNITS_READY before PROGRAM_READY. Accepted: repeated STEAM_BOILER_WAITING in initialization, repair messages for sound units (acknowledged), failure acknowledgements with nothing pending (ignored).
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/monitor/PlantMonitor.java` lines 375-399, `initialisationAbort/operatingEmergency`, requirements SB-FR5, SB-Beh9, SB-Beh14
  - `java.generated.project/src/main/java/steam_boiler/monitor/PlantMonitor.java` lines 106-151, `accept`, requirements SB-FR5

### 10. [design_choice] Timing of pump and pump-controller judgements

- **raw:** SB-FR1: next transmission after an order; SB-FR2: second transmission after a start or stop order.
- **fix_directive:** Pump: judged in the next transmission after any order; otherwise a change of state is spontaneous. Controller: judged in the second transmission after an order unless a contrary order followed; not judged right after an order; otherwise a change is spontaneous. Units just repaired are not judged in that cycle.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/monitor/PumpUnit.java` lines 129-152, `pumpReactionWrong/flowReactionWrong`, requirements SB-FR1, SB-FR2

### 11. [invented_default] Bounds used for dynamics checks, estimate and limit risk

- **raw:** SB-FR3, SB-V8, SB-Beh13 require worst-case reasoning without fixing its form.
- **fix_directive:** Steam outflow over a cycle lies within U*T*T/4 of the trapezoid of the two readings (one reading: v*T -/+ U*T*T/2), clipped to [0, W*T]; while the valve is open any decrease is possible; an out-of-order pump may pour, a pump just switched on pours nothing for one cycle; 'risks reaching' means the bound is <= M1 or >= M2. Failure judgement starts only after STEAM_BOILER_WAITING (SB-Beh2).
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/monitor/PlantMonitor.java` lines 241-306, `checkSteam/updateLevel`, requirements SB-FR3, SB-FR4, SB-V8, SB-Beh25
  - `java.generated.project/src/main/java/steam_boiler/monitor/PlantMonitor.java` lines 499-526, `levelLimitRisk`, requirements SB-V4, SB-Beh13, SB-Beh18, SB-Beh21

### 12. [ambiguous_requirement] MODE on every cycle versus a terminal emergency stop

- **raw:** SB-Beh24: MODE on every cycle in whatever mode; SB-Beh22: on entering emergency stop the program stops.
- **fix_directive:** MODE(EMERGENCY_STOP) is sent in the cycle of entry; emergency stop has no transitions and runCycle returns no message afterwards.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 129-131, `step`, requirements SB-Beh22, SB-Beh24

### 13. [design_choice] Transitions ordered by priority, with an unguarded last branch per mode

- **raw:** Iteration 1 conjoined the negation of every earlier guard; the Isabelle deadlock_free tactic timed out on the resulting disjunctions.
- **fix_directive:** Within a mode, each branch relies on the else-if order (allowed for event-triggered branches) and the last branch tests only the event. Java takes exactly one branch per cycle. In the extracted model the branches of a mode are alternatives, so the model over-approximates the Java (for example the keep-mode branch is always possible).
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 53-131, `step`, requirements SB-FR8, SB-Beh24

## next_step

Run the pipeline; revisit these choices only if a verifier or the operator objects to one of them.
