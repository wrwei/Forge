# post_codegen

**status:** uncertain

**summary:** All 53 requirements are implemented and traced. The requirements left the items below open; each records how it was resolved. Nothing is unimplemented.

## Issues

### 1. [invented_default] Values of the boiler, steam and pump constants

- **raw:** SB-DM1, SB-DM2 and SB-DM3 name C, M1, M2, N1, N2, W, U1, U2 and P but give no values.
- **fix_directive:** Chosen: C=1000, M1=150, M2=850, N1=400, N2=600 (litre); W=25, P=10 (litre/sec); U1=U2=2 (litre/sec/sec); cycle 5 s; STOP limit 3. All are whole numbers, so rounding in the constants interface does not change them. Replace them if the plant's real values are known.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/controller/BoilerConstants.java` lines 14-47, `BoilerConstants`, requirements SB-DM1, SB-DM2, SB-DM3

### 2. [design_choice] One trigger event per cycle (Transmission)

- **raw:** SB-V3: all incoming messages of a cycle are received simultaneously, but a transition has one trigger.
- **fix_directive:** The messages are delivered to PlantMonitor.receive, analysed by PlantMonitor.analyse into BoilerSensor, and then step(Transmission) runs once. Each transition is triggered by Transmission and its guard tests what that transmission carried. The other InputEvent records are the message types themselves; the controller does not take them as triggers.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/event/InputEvent.java` lines 15-15, `Transmission`, requirements SB-V3, SB-DM9
  - `java.generated.project/src/main/java/steam_boiler/SteamBoilerProgram.java` lines 29-39, `cycle`, requirements SB-V3

### 3. [design_choice] Separate OperatingMode enumeration for MODE(m)

- **raw:** SB-DM10 makes the mode a value carried by MODE(m); SB-V2 makes it the controller state.
- **fix_directive:** BoilerMode holds the states. OperatingMode, with the literals *_MODE, is the message payload, so that no enumeration literal shares a name with a state.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/event/OutputEvent.java` lines 10-10, `Mode`, requirements SB-DM10, SB-Beh24

### 4. [design_choice] Bang-bang regulation of all four pumps; no regulation on a mode-change cycle

- **raw:** SB-Beh10/15/19 require correcting the level with the pumps but give no control law.
- **fix_directive:** Below N1 the program opens pumps 1-4, above N2 it closes them, and in the band it leaves them alone. Rescue mode uses the midpoint of the estimate. A cycle that changes mode sends only MODE, so the running pumps stay on for that cycle. The risk test allows for this: if the cycle's branch closes the pumps, it uses the maximum with the pumps closed, and otherwise the maximum with them running.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 76-91, `step`, requirements SB-Beh10, SB-Beh13, SB-Beh15, SB-Beh19

### 5. [design_choice] Worst-case prediction used for the M1/M2 risk

- **raw:** SB-Beh13/18/21 and SB-V4 require judging the risk against worst-case behaviour; the form of the prediction is open.
- **fix_directive:** predictedMinimum = lower bound + water from pumps known to pour (pump on, flow seen, both units sound) - steam out at the maximal rise (capped by W). predictedMaximum = upper bound + full capacity of every pump on or out of order - steam out at the maximal fall. Pumps ordered on this cycle add nothing until the next one (SB-Beh25). The minimum ignores closing, which only matters if N2 - 5W <= M1; it does not with these constants.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/sensor/PlantMonitor.java` lines 429-463, `publish`, requirements SB-V4, SB-Beh13, SB-Beh25

### 6. [ambiguous_requirement] Checks made before STEAM_BOILER_WAITING

- **raw:** SB-Beh2: nothing else happens until STEAM_BOILER_WAITING arrives; SB-Beh9, SB-FR5 and SB-Beh23 apply during initialisation and in every transmission or mode.
- **fix_directive:** Before the plant is waiting, only three things can end initialisation: STOP x3, a transmission failure, or PHYSICAL_UNITS_READY arriving before PROGRAM_READY. The steam-zero and level-device checks, the valve, the pump and PROGRAM_READY all start with STEAM_BOILER_WAITING. The steam-zero check then repeats on every initialisation cycle.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 68-75, `step`, requirements SB-Beh2, SB-Beh3, SB-Beh9, SB-Beh23

### 7. [ambiguous_requirement] Emergency stop is terminal versus MODE on every cycle

- **raw:** SB-Beh22: in emergency stop the program stops. SB-Beh24: MODE(m) is sent on every cycle in whatever mode.
- **fix_directive:** MODE(EMERGENCY_STOP_MODE) is sent once, on the transition into emergency stop. The mode has no outgoing transition, and SteamBoilerProgram.cycle sends nothing afterwards.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 244-247, `step`, requirements SB-Beh22, SB-Beh24
  - `java.generated.project/src/main/java/steam_boiler/SteamBoilerProgram.java` lines 29-39, `cycle`, requirements SB-Beh22

### 8. [ambiguous_requirement] Which pump-controller failures stop rescue, and simultaneous failures

- **raw:** SB-Beh21: 'if the pump-controller units fail'. SB-FR8 orders simultaneous detections.
- **fix_directive:** Any pump controller out of order, or the steam device out of order, makes rescue impossible. If the level device fails in normal or degraded mode while rescue is impossible, the program goes straight to emergency stop instead of entering rescue. Otherwise emergency stop takes precedence over rescue, and rescue over degraded.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 52-53, `step`, requirements SB-Beh21, SB-FR8

### 9. [design_choice] What counts as an aberrant message

- **raw:** SB-FR5 leaves 'a message whose presence is aberrant' open.
- **fix_directive:** Aberrant: a data message duplicated or missing; a pump number outside 1-4; a repair message for a sound unit; an acknowledgement with no announcement outstanding (all in PlantMonitor); STEAM_BOILER_WAITING or PHYSICAL_UNITS_READY outside initialisation; PHYSICAL_UNITS_READY before PROGRAM_READY (both in the controller).
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/sensor/PlantMonitor.java` lines 170-261, `analyse`, requirements SB-FR5
  - `java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 70-80, `step`, requirements SB-FR5, SB-Beh9

### 10. [design_choice] Initialisation fill pump and valve drain rate

- **raw:** SB-Beh5 says 'a pump'. SB-DM7 gives no drain rate for the valve.
- **fix_directive:** Pump 1 fills, and it is closed when the level enters [N1, N2] together with the first PROGRAM_READY. While the valve is open, the dynamic lower bound for level-failure detection and the lower estimate are dropped, because the drain rate is unknown.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 110-133, `step`, requirements SB-Beh4, SB-Beh5, SB-Beh7
  - `java.generated.project/src/main/java/steam_boiler/sensor/PlantMonitor.java` lines 397-402, `lowestReachableLevel`, requirements SB-FR3, SB-DM7

### 11. [scope_question] Failure detection and the announcement/acknowledgement protocol live outside the state machine

- **raw:** SB-FR1..FR7 and SB-V9 need per-unit bookkeeping and conditional per-unit messages.
- **fix_directive:** This is implemented in PlantMonitor (Java). The state machine sees only the aggregated failure flags, through BoilerSensor. The formal model therefore covers the mode logic, not the detection rules or the repeat-until-acknowledged protocol.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/sensor/PlantMonitor.java` lines 170-207, `analyse`, requirements SB-V9, SB-FR1, SB-FR2, SB-FR3, SB-FR4, SB-FR6, SB-FR7

### 12. [design_choice] Two-valued messages carry a record

- **raw:** PUMP_STATE(n, b) and PUMP_CONTROL_STATE(n, b) have two values; an event record has one component.
- **fix_directive:** Their payloads are PumpReading(pumpIndex, position) and PumpControlReading(controllerIndex, observation).
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/event/InputEvent.java` lines 27-30, `PumpState`, requirements SB-DM9, SB-DM3, SB-DM4

## next_step

Run the pipeline; revisit these choices only if a verifier or the operator objects to one.
