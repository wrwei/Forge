# post_codegen

**status:** uncertain

**summary:** All 53 requirements are implemented and traced. The 13 issues below are invented constants, design choices and interpretations of open points; none is unimplemented.

## Issues

### 1. [invented_default] Numeric values of C, M1, M2, N1, N2, W, U1, U2 and P

- **raw:** SB-DM1/DM2/DM3 name the constants but give no values.
- **fix_directive:** Chosen: C=1000, M1=100, N1=400, N2=600, M2=900 (litres), W=25, P=10 (l/s), U1=4, U2=6 (l/s/s), cycle 5 s, STOP limit 3. All integers, so the model's constants need no rounding; U1*25/2=50 and U2*25/2=75 are exact. Replace with the plant's real values if known.
- **java_trace:**
  - `src/main/java/steam_boiler/controller/BoilerConstants.java` lines 14-47, `BoilerConstants`, requirements SB-DM1, SB-DM2, SB-DM3

### 2. [design_choice] One step per cycle, triggered by TransmissionReceived

- **raw:** SB-V3: all messages of a cycle are received simultaneously; the profile gives step one event per call.
- **fix_directive:** The cycle's messages (the SB-DM9 records) go to MessageReceiver.receive as one list; the controller is then stepped once with InputEvent.TransmissionReceived. Every transition has this trigger, so the model has no internal transitions. The other InputEvent records are model events that no transition uses.
- **java_trace:**
  - `src/main/java/steam_boiler/event/InputEvent.java` lines 15, `TransmissionReceived`, requirements SB-V3, SB-DM9
  - `src/main/java/steam_boiler/SteamBoilerProgram.java` lines 34-41, `runCycle`, requirements SB-V3

### 3. [design_choice] The analysis of readings is in the receive stage; the controller sees boolean judgements

- **raw:** SB-V5..V10, SB-FR1..FR5, SB-Beh13/19/25 need per-pump order history, previous readings and real arithmetic. In iteration 1, real-valued controller state and nested predicates made FDR4 run out of resources and broke Isabelle generation.
- **fix_directive:** MessageReceiver (with UnitStatusTable and Transmitter's order log, last MODE and PROGRAM_READY record) does the per-unit failure detection, the transmission check, the STOP count, q/v/p, the level estimate and the worst-case forecasts. It publishes them as boolean (and one nat) judgements on PlantSensor. The controller holds only the mode and readyAnnounced, and its guards are conjunctions of those judgements, so the numeric rules are not in the verified model.
- **java_trace:**
  - `src/main/java/steam_boiler/sensor/MessageReceiver.java` lines 49-284, `receive`, requirements SB-V5, SB-V6, SB-V7, SB-V8, SB-V9, SB-V10, SB-FR1, SB-FR2, SB-FR3, SB-FR4, SB-FR5
  - `src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 39-52, `step`, requirements SB-V9, SB-FR8

### 4. [design_choice] Level-risk criterion for emergency stop

- **raw:** SB-Beh13/18/21, SB-V4: 'risks reaching M1 or M2, judged against worst-case behaviour'.
- **fix_directive:** Risk = the level one cycle ahead under the best response still passes a limit: low side q + p*5 - maxSteamVolume < M1 (flowing pumps kept); high side q - minSteamVolume > M2 (all pumps closed, effective at once). In rescue the estimate bounds replace q. Steam volumes are bounded by W, U1 and U2.
- **java_trace:**
  - `src/main/java/steam_boiler/sensor/MessageReceiver.java` lines 246-252, `receive`, requirements SB-Beh13, SB-Beh18, SB-Beh21, SB-V4

### 5. [design_choice] Pump regulation policy

- **raw:** SB-Beh10/15/19 say pumps are switched to correct the level, but give no policy.
- **fix_directive:** Each cycle, if the worst-case forecast for the next cycle is below N1, open all sound pumps; else if the best-case forecast is above N2, close all pumps; otherwise leave them. In rescue the estimate bounds drive the same rule. A cycle that changes mode leaves the pumps as they are. Initialization fills with one pump.
- **java_trace:**
  - `src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 121-252, `step`, requirements SB-Beh5, SB-Beh10, SB-Beh15, SB-Beh19

### 6. [design_choice] Form of the level estimate

- **raw:** SB-V8 leaves the form of the estimate open beyond lower and upper bounds.
- **fix_directive:** The receive stage keeps the interval [levelLow, levelHigh]. It is [q, q] while the level device works; while it is out of order, the interval is advanced each cycle by the steam bounds and the water known to be supplied. The lower bound counts pumps seen pouring that stayed ordered on; the upper bound counts pumps reported on that stayed ordered on; a pump ordered on in the last cycle supplied nothing yet (SB-Beh25). With the chosen constants the interval widens by at least the steam uncertainty each cycle, so rescue lasts only a few cycles before the estimate forces emergency stop.
- **java_trace:**
  - `src/main/java/steam_boiler/sensor/MessageReceiver.java` lines 215-244, `receive`, requirements SB-V8, SB-Beh19, SB-Beh25

### 7. [ambiguous_requirement] 'If the pump-controller units fail' in rescue

- **raw:** SB-Beh21 does not say whether one controller failing is enough.
- **fix_directive:** Interpreted as any pump controller out of order. By SB-FR8, a level-device failure detected while the steam device or a controller is already out of order goes straight to emergency stop instead of rescue.
- **java_trace:**
  - `src/main/java/steam_boiler/sensor/MessageReceiver.java` lines 271-276, `receive`, requirements SB-Beh21, SB-FR8
  - `src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 121-164, `step`, requirements SB-FR8

### 8. [ambiguous_requirement] Scope of 'nothing else happens' before STEAM_BOILER_WAITING

- **raw:** SB-Beh2 against SB-Beh9 and SB-Beh23.
- **fix_directive:** Before STEAM_BOILER_WAITING the operator stop and transmission-failure rules still apply, MODE is still sent, and unit failure detection does not run. The steam-zero check (SB-Beh3) and level-device check (SB-Beh6) apply on every initialization cycle from STEAM_BOILER_WAITING on, not only on the cycle it arrives.
- **java_trace:**
  - `src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 55-119, `step`, requirements SB-Beh2, SB-Beh3, SB-Beh6, SB-Beh9, SB-Beh23

### 9. [design_choice] Which messages count as aberrant (transmission failure)

- **raw:** SB-FR5: 'a message whose presence is aberrant' is not enumerated.
- **fix_directive:** Aberrant: a duplicated LEVEL/STEAM/PUMP_STATE/PUMP_CONTROL_STATE, a pump number outside 1..4, STEAM_BOILER_WAITING or PHYSICAL_UNITS_READY outside initialization, and PHYSICAL_UNITS_READY before PROGRAM_READY. Tolerated: a repair message for a sound unit (acknowledged again) and an acknowledgement for a failure not announced (ignored).
- **java_trace:**
  - `src/main/java/steam_boiler/sensor/MessageReceiver.java` lines 147-162, `receive`, requirements SB-FR5, SB-Beh7

### 10. [design_choice] Timing of the pump and pump-controller failure rules

- **raw:** SB-FR1/FR2.
- **fix_directive:** A pump has failed when its reported state differs from its last order on any transmission after that order, which covers both the missed order and the spontaneous change. A controller is judged only at the second transmission after an order or later, and only while its pump is known to be working. A pump out of order is not ordered on; any pump not wanted is ordered off.
- **java_trace:**
  - `src/main/java/steam_boiler/sensor/MessageReceiver.java` lines 335-355, `judgePumps`, requirements SB-FR1, SB-FR2
  - `src/main/java/steam_boiler/actuator/Transmitter.java` lines 90-119, `composePumpOrders`, requirements SB-FR1, SB-DM3

### 11. [scope_question] MODE broadcast after emergency stop

- **raw:** SB-Beh22 (the program stops) against SB-Beh24 (MODE every cycle in whatever mode).
- **fix_directive:** SteamBoilerProgram sends MODE(EMERGENCY_STOP) on the cycle that enters emergency stop, then stops driving the controller and sends nothing more. The controller itself never leaves EMERGENCY_STOP: its only branch there is an event-only self-loop that repeats MODE(EMERGENCY_STOP). That branch was added in iteration 3 because the Z-Machine deadlock-freedom tactic needs every state to have an unguarded operation.
- **java_trace:**
  - `src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 253-259, `step`, requirements SB-Beh22, SB-Beh24
  - `src/main/java/steam_boiler/SteamBoilerProgram.java` lines 34-41, `runCycle`, requirements SB-Beh22

### 12. [design_choice] The last branch of each mode tests the event only

- **raw:** Isabelle iteration 2: the deadlock_free tactic (metis St.exhaust_disc) timed out because no operation in a state had a bare precondition.
- **fix_directive:** In each mode the last branch (INITIALIZATION: wait for STEAM_BOILER_WAITING; NORMAL/DEGRADED/RESCUE: hold the pumps) has no guard. In Java, as the last else-if, it still fires only in the one case the earlier branches leave, which was checked over every combination of atoms. In the RoboChart model it is an alternative that is always enabled, so the model over-approximates the program by allowing a 'hold' cycle in any situation.
- **java_trace:**
  - `src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 115-119, `step`, requirements SB-Beh2
  - `src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 158-162, `step`, requirements SB-Beh10
  - `src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 202-206, `step`, requirements SB-Beh15
  - `src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 246-250, `step`, requirements SB-Beh19

### 13. [invented_default] Steam bounds while the steam device is out of order; initial estimate

- **raw:** No requirement says what to assume for v once the steam device has failed.
- **fix_directive:** The steam rate is taken to lie anywhere in 0..W. If the level device is out of order with no earlier interval, the level is taken to lie anywhere in 0..C. While the valve is open, the estimate's lower bound drops to 0.
- **java_trace:**
  - `src/main/java/steam_boiler/sensor/MessageReceiver.java` lines 197-244, `receive`, requirements SB-V6, SB-V8, SB-DM2

## next_step

Run the pipeline. If a verifier rejects the model, fix the Java according to that phase's report.
