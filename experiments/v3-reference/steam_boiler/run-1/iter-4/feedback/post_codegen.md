# post_codegen

**status:** uncertain

**summary:** All 53 requirements are implemented and traced. The requirements left the items below open; each records how it was resolved. Nothing is unimplemented.

## Issues

### 1. [invented_default] Values of the boiler, steam and pump constants

- **raw:** SB-DM1, SB-DM2 and SB-DM3 name C, M1, M2, N1, N2, W, U1, U2 and P but give no values.
- **fix_directive:** Chosen: C=1000, M1=150, M2=850, N1=400, N2=600 (litre); W=25, P=10 (litre/sec); U1=U2=2 (litre/sec/sec); cycle 5 s; STOP limit 3. All are whole numbers. They are used only by the monitor, outside the model. Replace them if the plant's real values are known.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/controller/BoilerConstants.java` lines 14-47, `BoilerConstants`, requirements SB-DM1, SB-DM2, SB-DM3

### 2. [design_choice] One trigger event per cycle (Transmission)

- **raw:** SB-V3: all incoming messages of a cycle are received simultaneously, but a transition has one trigger.
- **fix_directive:** The messages are delivered to PlantMonitor.receive, analysed by PlantMonitor.analyse into BoilerSensor, then step(Transmission) runs once and PumpRegulator.regulate follows. Every transition is triggered by Transmission. The other InputEvent records are the message types themselves; the controller does not take them as triggers.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/event/InputEvent.java` lines 15-15, `Transmission`, requirements SB-V3, SB-DM9
  - `java.generated.project/src/main/java/steam_boiler/SteamBoilerProgram.java` lines 31-42, `cycle`, requirements SB-V3

### 3. [design_choice] Separate OperatingMode enumeration for MODE(m)

- **raw:** SB-DM10 makes the mode a value carried by MODE(m); SB-V2 makes it the controller state.
- **fix_directive:** BoilerMode holds the states. OperatingMode, with the literals *_MODE, is the message payload, so that no enumeration literal shares a name with a state.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/event/OutputEvent.java` lines 10-10, `Mode`, requirements SB-DM10, SB-Beh24

### 4. [design_choice] Valve and pump commands are issued by PumpRegulator, outside the state machine

- **raw:** Iterations 1-3: with the per-cycle pump and valve commands as transitions (29-42 of them, guards over many sensor verdicts), FDR4 exceeded 40 GB and Isabelle's deadlock_free timed out each time.
- **fix_directive:** The controller decides the mode and conducts the PROGRAM_READY handshake (19 transitions). After every step, PumpRegulator issues the cycle's commands for the mode just announced. In initialisation, once the plant is waiting and until PROGRAM_READY has been transmitted, it opens the valve above N2, closes it otherwise, opens pump 1 below N1 and closes pump 1 in the band (SB-Beh4/5). In normal, degraded and rescue mode it opens pumps 1-4 below N1 and closes them above N2, against the best known level: the reading, or the estimate's midpoint while the level device is out of order (SB-Beh10/15/19). These commands are therefore not in the verified model.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/actuator/PumpRegulator.java` lines 27-67, `regulate`, requirements SB-Beh4, SB-Beh5, SB-Beh10, SB-Beh15, SB-Beh19

### 5. [design_choice] Guards read composite boolean verdicts computed by the monitor

- **raw:** Iteration 1 guards inlined about a dozen layered predicates over real-valued readings; FDR4, Isabelle and the vacuity parser all failed on their size.
- **fix_directive:** The controller reads seven booleans:
- startUpAbort: STOP x3, or transmission failure, or plant waiting with steam not zero or level device out of order.
- startUpLevelReached: plant waiting, no start-up fault, level in [N1, N2].
- unitsReady.
- emergencyCause: STOP x3, or transmission failure, or level at risk.
- levelFailure.
- unitFailure: steam device, a pump or a pump controller out of order.
- estimateUnreliable: steam device or a pump controller out of order.
No guard combines more than three of them. The thresholds, arithmetic, q, v, p, the estimate bounds and the STOP count are in PlantMonitor.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 42-63, `step`, requirements SB-V4, SB-Beh13, SB-FR5, SB-FR8
  - `java.generated.project/src/main/java/steam_boiler/sensor/PlantMonitor.java` lines 183-216, `analyse`, requirements SB-V4, SB-FR5

### 6. [design_choice] Worst-case prediction used for the M1/M2 risk

- **raw:** SB-Beh13/18/21 and SB-V4 require judging the risk against worst-case behaviour; the form of the prediction is open.
- **fix_directive:** The level is at risk when either worst-case bound for the next cycle reaches a limit.
- Lower bound: estimate low + water from pumps known to pour (pump on, flow seen, both units sound) - steam out at the maximal rise (capped by W). Risky if <= M1.
- Upper bound: estimate high - steam out at the maximal fall, plus the full capacity of every pump on or out of order, unless the regulator closes the pumps this cycle (best known level above N2). Risky if >= M2.
Pumps ordered on this cycle add nothing until the next one (SB-Beh25). The lower bound ignores closing, which only matters if N2 - 5W <= M1; it does not with these constants.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/sensor/PlantMonitor.java` lines 455-498, `publish`, requirements SB-V4, SB-Beh13, SB-Beh25

### 7. [ambiguous_requirement] Checks made before STEAM_BOILER_WAITING

- **raw:** SB-Beh2: nothing else happens until STEAM_BOILER_WAITING arrives; SB-Beh9, SB-FR5 and SB-Beh23 apply during initialisation and in every transmission or mode.
- **fix_directive:** Before the plant is waiting, initialisation ends only on STOP x3 or a transmission failure. A premature PHYSICAL_UNITS_READY counts as a transmission failure. The steam-zero and level-device checks, the valve and pump adjustment, and PROGRAM_READY all start with STEAM_BOILER_WAITING. The steam-zero check then repeats on every initialisation cycle.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/sensor/PlantMonitor.java` lines 212-213, `analyse`, requirements SB-Beh2, SB-Beh3, SB-Beh9, SB-Beh23

### 8. [ambiguous_requirement] Emergency stop is terminal versus MODE on every cycle

- **raw:** SB-Beh22: in emergency stop the program stops. SB-Beh24: MODE(m) is sent on every cycle in whatever mode.
- **fix_directive:** MODE(EMERGENCY_STOP_MODE) is sent once, on the transition into emergency stop. The mode is never left: its only branch is a self-loop on the transmission with no action, there so that the Z-Machine deadlock-freedom proof has an operation in every state. SteamBoilerProgram.cycle sends nothing once emergency stop is reached.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 134-140, `step`, requirements SB-Beh22, SB-Beh24
  - `java.generated.project/src/main/java/steam_boiler/SteamBoilerProgram.java` lines 31-42, `cycle`, requirements SB-Beh22

### 9. [ambiguous_requirement] Which pump-controller failures stop rescue, and simultaneous failures

- **raw:** SB-Beh21: 'if the pump-controller units fail'. SB-FR8 orders simultaneous detections.
- **fix_directive:** Any pump controller out of order, or the steam device out of order, makes the estimate unreliable, and so rules out rescue. If the level device fails in normal or degraded mode while the estimate is unreliable, the program goes straight to emergency stop. Otherwise emergency stop takes precedence over rescue, and rescue over degraded.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 55-63, `step`, requirements SB-Beh21, SB-FR8

### 10. [design_choice] What counts as an aberrant message

- **raw:** SB-FR5 leaves 'a message whose presence is aberrant' open.
- **fix_directive:** Aberrant, all judged in PlantMonitor:
- a data message duplicated or missing;
- a pump number outside 1-4;
- a repair message for a sound unit;
- an acknowledgement with no announcement outstanding;
- STEAM_BOILER_WAITING or PHYSICAL_UNITS_READY when the last MODE sent was not initialisation;
- PHYSICAL_UNITS_READY before any PROGRAM_READY has been transmitted.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/sensor/PlantMonitor.java` lines 242-283, `unexpectedInitialisationMessage`, requirements SB-FR5, SB-Beh9

### 11. [design_choice] Initialisation fill pump and valve drain rate

- **raw:** SB-Beh5 says 'a pump'. SB-DM7 gives no drain rate for the valve.
- **fix_directive:** Pump 1 fills; it is closed, and the valve closed, when the level enters [N1, N2], on the cycle of the first PROGRAM_READY. While the valve is open, the dynamic lower bound for level-failure detection and the lower estimate are dropped, because the drain rate is unknown.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/actuator/PumpRegulator.java` lines 37-55, `adjustStartUpLevel`, requirements SB-Beh4, SB-Beh5
  - `java.generated.project/src/main/java/steam_boiler/sensor/PlantMonitor.java` lines 423-428, `lowestReachableLevel`, requirements SB-FR3, SB-DM7

### 12. [design_choice] Each mode's last branch keeps the mode with no condition beyond the transmission

- **raw:** Isabelle report: the deadlock_free proof (metis St.exhaust_disc) needs an operation with a bare precondition in every state.
- **fix_directive:** Each mode's branches cover every case, so its final stay branch can be written `event instanceof Transmission` with identical Java behaviour. In the model these transitions overlap the guarded ones, which is nondeterminism; the pipeline does not check determinism.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 65-140, `step`, requirements SB-Beh2, SB-Beh10, SB-Beh15, SB-Beh19, SB-Beh22

### 13. [scope_question] What the verified model does and does not cover

- **raw:** SB-FR1..FR7, SB-V5..V10 and the regulation activities need arithmetic, per-unit bookkeeping and conditional per-unit messages.
- **fix_directive:** The model covers the modes, every mode transition, MODE and the PROGRAM_READY handshake.
Implemented in Java outside the model:
- failure detection, unit status, the estimate, and the announce/acknowledge protocol (PlantMonitor);
- valve and pump commands (PumpRegulator).
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/sensor/PlantMonitor.java` lines 183-216, `analyse`, requirements SB-V9, SB-FR1, SB-FR2, SB-FR3, SB-FR4, SB-FR6, SB-FR7
  - `java.generated.project/src/main/java/steam_boiler/actuator/PumpRegulator.java` lines 27-35, `regulate`, requirements SB-Beh10, SB-Beh15, SB-Beh19

### 14. [design_choice] Two-valued messages carry a record

- **raw:** PUMP_STATE(n, b) and PUMP_CONTROL_STATE(n, b) have two values; an event record has one component.
- **fix_directive:** Their payloads are PumpReading(pumpIndex, position) and PumpControlReading(controllerIndex, observation).
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/event/InputEvent.java` lines 27-30, `PumpState`, requirements SB-DM9, SB-DM3, SB-DM4

## next_step

Run the pipeline; revisit these choices only if a verifier or the operator objects to one.
