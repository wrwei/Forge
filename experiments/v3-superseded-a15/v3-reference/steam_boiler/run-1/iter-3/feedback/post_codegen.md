# post_codegen

**status:** uncertain

**summary:** Java for the steam-boiler program written in five layers; 53 requirements traced. Nothing is unimplemented; the issues below record invented values, interpretations and design choices.

## Issues

### 1. [invented_default] Numeric values of the plant constants

- **raw:** SB-DM1/DM2/DM3 name C, M1, M2, N1, N2, W, U1, U2 and P but give no values.
- **fix_directive:** Chosen: C=1000, M1=150, N1=400, N2=600, M2=800 (litre); W=25, P=10 (litre/sec); U1=U2=2 (litre/sec/sec); cycle 5 s; 4 pumps; STOP threshold 3. They respect M1<N1<N2<M2<C, M1>=5W and C-M2=5*4P. All are integers, so rounding in the constants interface changes nothing (only N1, N2 reach the model). Replace if real plant values are known.
- **java_trace:**
  - `src/main/java/steam_boiler/BoilerConstants.java` lines 12-52, `BoilerConstants` — SB-DM1, SB-DM2, SB-DM3

### 2. [invented_default] Measures before the first reading

- **raw:** SB-V5/V6 define q and v as the most recent readings; no value is given before any reading arrives.
- **fix_directive:** q and v start at 0.0 and keep their last value when a reading is missing (a missing reading is a transmission failure anyway). Level bounds start at [0, C], steam bounds at [0, W].
- **java_trace:**
  - `src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 69-83, `q, v, levelLow, levelHigh, steamLow, steamHigh` — SB-V5, SB-V6, SB-V8

### 3. [design_choice] One cycle event triggers every transition

- **raw:** SB-V3: all messages of a cycle are received simultaneously, then analysed. step(event) is called once per cycle.
- **fix_directive:** The unit messages (SB-DM9 records) are buffered by BoilerSensor in the receive stage; step() is then called with InputEvent.TransmissionReceived, and every transition of the machine is triggered by it, with the conditions read from the sensor. The cycle structure is kept, and no transition is guard-only, so the model cannot diverge.
- **java_trace:**
  - `src/main/java/steam_boiler/event/InputEvent.java` lines 12, `TransmissionReceived` — SB-V3, SB-DM9
  - `src/main/java/steam_boiler/SteamBoilerProgram.java` lines 33-50, `runCycle` — SB-V3

### 4. [design_choice] MODE payload is a separate enum

- **raw:** SB-DM10: the mode is a value of five alternatives carried by MODE(m).
- **fix_directive:** OperatingMode is the controller's state enum; ModeValue (INITIALIZATION_MODE ... EMERGENCY_STOP_MODE) is the payload of ModeMessage, so that the model's states and enumeration literals do not share names.
- **java_trace:**
  - `src/main/java/steam_boiler/event/ModeValue.java` lines 7-13, `ModeValue` — SB-DM10, SB-Beh24

### 5. [design_choice] Per-unit protocol and pump selection live outside the state machine

- **raw:** SB-FR6/FR7 need one message per failed or repaired unit per cycle; SB-Beh10 may need several OPEN/CLOSE_PUMP in one cycle. A transition action is a fixed sequence of statements.
- **fix_directive:** The controller gives argument-free commands (regulatePumps, runFillingPump, stopPumps, openValve, closeValve); BoilerActuator turns them into OPEN_PUMP/CLOSE_PUMP/VALVE messages, reading the required pump count from BoilerSensor. In the model these commands are events with no value. Failure-detection repeats and repair acknowledgements are added by BoilerActuator.announceUnitStatus(), called by the cycle driver while the program runs (after STEAM_BOILER_WAITING, before emergency stop). These messages are therefore not in the extracted model.
- **java_trace:**
  - `src/main/java/steam_boiler/actuator/BoilerActuator.java` lines 45-141, `openValve, closeValve, regulatePumps, runFillingPump, stopPumps, announceUnitStatus` — SB-FR6, SB-FR7, SB-Beh10
  - `src/main/java/steam_boiler/SteamBoilerProgram.java` lines 33-50, `runCycle` — SB-FR6, SB-FR7

### 6. [ambiguous_requirement] What happens before STEAM_BOILER_WAITING

- **raw:** SB-Beh2 says nothing else happens until STEAM_BOILER_WAITING arrives; SB-Beh9 (transmission failure in initialization) and SB-Beh23 (STOP in every mode) seem to apply throughout.
- **fix_directive:** Before STEAM_BOILER_WAITING only the STOP rule (three successive STOPs) and MODE broadcast apply; transmission failure is judged from the cycle in which STEAM_BOILER_WAITING arrives.
- **java_trace:**
  - `src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 53-61, `step (INITIALIZATION block)` — SB-Beh2, SB-Beh9, SB-Beh23

### 7. [ambiguous_requirement] Steam or pump-controller failure together with a level failure in normal/degraded

- **raw:** SB-FR8 lists steam-device and pump-controller failure as emergency-stop conditions only in rescue mode.
- **fix_directive:** Read literally: in normal or degraded a level failure leads to rescue even if the steam device or a pump controller is also out of order; the next cycle, in rescue, leads to emergency stop (SB-Beh21). Level risk is still judged in that cycle.
- **java_trace:**
  - `src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 117-230, `step (NORMAL, DEGRADED, RESCUE blocks)` — SB-FR8, SB-Beh11, SB-Beh17, SB-Beh21

### 8. [ambiguous_requirement] 'The pump-controller units fail' in rescue mode

- **raw:** SB-Beh21: rescue goes to emergency stop 'if the pump-controller units fail'.
- **fix_directive:** Interpreted as any pump controller being out of order (SB-Beh19: the estimate needs reliable pump-controller information).
- **java_trace:**
  - `src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 187-230, `step (RESCUE block)` — SB-Beh21, SB-Beh19

### 9. [design_choice] Which messages are aberrant (transmission failure)

- **raw:** SB-FR5 does not list aberrant messages.
- **fix_directive:** A transmission fails when a data message is missing or duplicated, a pump number is outside 1..4, a message is duplicated, PHYSICAL_UNITS_READY arrives before PROGRAM_READY was sent, a failure acknowledgement names a unit with no pending detection, a repair names a unit that is not out of order, or (outside initialization) STEAM_BOILER_WAITING or PHYSICAL_UNITS_READY arrives.
- **java_trace:**
  - `src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 430-454, `checkTransmission` — SB-FR5
  - `src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 40, `step (startMessageArrived)` — SB-Beh14, SB-Beh18, SB-Beh21

### 10. [design_choice] Level risk, estimate and regulation algorithm

- **raw:** SB-Beh13/Beh19/Beh10 leave the prediction and regulation algorithm open.
- **fix_directive:** Risk: worst-case steam over the next cycle (gradients U1/U2, bounded by W and 0) with the best pump action (pumps on now kept on, newly opened pumps pour nothing for one cycle, pumps out of order counted as pouring) reaches M1 or M2. Estimate: lower/upper bounds advanced with the exact worst-case steam outflow between two readings and the pump inflow bounds. Regulation: keep the running pumps while the level predicted over two cycles stays in [N1, N2], else choose the number of sound pumps bringing it closest to (N1+N2)/2.
- **java_trace:**
  - `src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 565-712, `estimateInflow, checkLevelDevice, assessLevelRisk, planPumps` — SB-Beh10, SB-Beh13, SB-Beh19, SB-Beh25, SB-V8

### 11. [design_choice] Pump-controller and level checks around orders and initialization

- **raw:** SB-FR2 judges a controller at the second transmission after an order; SB-FR3 dynamics cannot be bounded while the evacuation valve (rate unknown) is in use.
- **fix_directive:** A controller of a sound pump must report flow exactly when its pump is on, except in the transmission right after an order. The level device gets a static check only during initialization; the dynamic check starts after PHYSICAL_UNITS_READY.
- **java_trace:**
  - `src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 542-556, `checkPumpControllers` — SB-FR2
  - `src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 618-641, `checkLevelDevice` — SB-FR3

### 12. [design_choice] Initialization details

- **raw:** SB-Beh4/5/7 do not say when the valve closes or the filling pump stops, or whether PROGRAM_READY continues if the level drifts.
- **fix_directive:** Above N2: valve open, pumps off. Below N1: valve closed, one pump on. Between N1 and N2: valve closed, pumps off, PROGRAM_READY. Once PROGRAM_READY has been sent it is repeated every cycle until PHYSICAL_UNITS_READY, whatever the level. The valve is closed again on leaving initialization.
- **java_trace:**
  - `src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 53-115, `step (INITIALIZATION block)` — SB-Beh4, SB-Beh5, SB-Beh7, SB-DM7

### 13. [scope_question] MODE in emergency stop

- **raw:** SB-Beh24 sends MODE every cycle in whatever mode; SB-Beh22 says the program stops on entering emergency stop.
- **fix_directive:** MODE(emergency stop) is sent once, in the cycle emergency stop is entered; afterwards the program sends nothing. The state is terminal in the model.
- **java_trace:**
  - `src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 232-234, `step (EMERGENCY_STOP block)` — SB-Beh22, SB-Beh24
  - `src/main/java/steam_boiler/SteamBoilerProgram.java` lines 34-36, `runCycle` — SB-Beh22

## next_step

Run the pipeline; revisit an item here only if a verifier or the operator objects to it.
