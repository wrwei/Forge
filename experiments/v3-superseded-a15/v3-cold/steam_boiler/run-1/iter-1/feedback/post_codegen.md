# post_codegen

**status:** uncertain

**summary:** All 53 requirements are implemented and traced (14 files, 198 trace entries); the code compiles. The issues below record invented constant values and design choices the requirements leave open.

## Issues

### 1. [invented_default] Values of the boiler, steam and pump constants

- **raw:** SB-DM1/DM2/DM3 name C, M1, M2, N1, N2, W, U1, U2 and P but give no values.
- **fix_directive:** Chosen: C=1000, M1=125, N1=400, N2=600, M2=800 (litre); W=25, U1=U2=2 (litre/sec, litre/sec/sec); P=10 (litre/sec); cycle 5 s. All integral, so the model's rounding of fractional constants does not affect any guard. Replace with plant values if known.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/BoilerConstants.java` lines 13-39, `BoilerConstants`: SB-DM1, SB-DM2, SB-DM3

### 2. [invented_default] Pump numbering and the pump returned when none is available

- **raw:** The requirements say 'pump n' without a range, and nothing about which pump to order.
- **fix_directive:** Pumps are numbered 1..4; an index outside that range is an aberrant message. pumpToOpen/pumpToClose return the lowest-numbered sound pump that is off and the highest-numbered sound pump that is on; they return 1 when there is none, but are only read when startablePumpAvailable/stoppablePumpAvailable hold.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 702-711, `pumpToOpen, pumpToClose`: SB-Beh5, SB-Beh10

### 3. [design_choice] One trigger per cycle; message records consumed by the receive stage

- **raw:** SB-V3: all messages of a cycle are received simultaneously, but step takes one event.
- **fix_directive:** SteamBoilerProgram.runCycle feeds every incoming message to BoilerSensor.receive, runs analyse, then calls step(TransmissionReceived). Every transition is triggered by TransmissionReceived, so the model has no guard-only transitions; the message contents reach the guards as sensor functions.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/SteamBoilerProgram.java` lines 35-50, `runCycle`: SB-V3, SB-DM9
  - `java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 35-270, `step`: SB-V3

### 4. [design_choice] Failure judgement, unit bookkeeping and level estimate live in BoilerSensor

- **raw:** FR1-FR5 and SB-V5..V10 need loops over four pumps, previous readings and interval arithmetic, which the controller profile cannot express.
- **fix_directive:** BoilerSensor does them in plain Java; the controller reads verdicts (levelFailureDetected, allUnitsSound, predictedLevelLow, ...). The variables q, v, p, the estimate, unit status and the STOP count are therefore sensor state, not controller state variables. The FR6/FR7 detection and repair-acknowledgement messages are emitted in the transmit stage by appendProtocolMessages, so they do not appear in the RoboChart model.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 217-349, `analyse`: SB-FR1, SB-FR2, SB-FR3, SB-FR4, SB-FR5, SB-V9, SB-V10
  - `java.generated.project/src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 480-512, `appendProtocolMessages`: SB-FR6, SB-FR7

### 5. [design_choice] Regulation policy

- **raw:** SB-Beh10/15/19 require correcting the level with the pumps but fix no policy.
- **fix_directive:** The sensor computes pumpsWanted = round((steam + (mid(N1,N2) - level)/(2*cycle)) / P), clamped to 0..4; the controller opens or closes at most one pump per cycle towards it. Cycles that change mode issue no pump order. In rescue the level is the middle of the estimate.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 455-470, `pumpsForTarget`: SB-Beh10, SB-Beh15, SB-Beh19
  - `java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 51-53, `supplyTooLow/supplyTooHigh`: SB-Beh10

### 6. [ambiguous_requirement] Level risk and level plausibility bounds

- **raw:** SB-Beh13/FR3 refer to worst-case behaviour without giving the computation.
- **fix_directive:** Risk: the next-cycle interval counts only water known to be supplied (sound pump on, controller sees flow) on the low side and P for every pump on or untrusted on the high side; steam moves at U1/U2; a pump just ordered on pours nothing (Beh25). Emergency stop if low <= M1 or high >= M2. Not applied in initialization. FR3's plausibility check uses the same high bound but lets every pump stop on the low side; it has no tolerance and is skipped while the valve is open and on a repair cycle.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 351-410, `deriveQuantities`: SB-Beh13, SB-FR3, SB-Beh25, SB-V4
  - `java.generated.project/src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 514-566, `endCycle`: SB-FR3, SB-V8

### 7. [ambiguous_requirement] Rescue when the estimate is untrustworthy

- **raw:** SB-Beh21 says 'the pump-controller units fail'; Beh11/Beh17 send a level failure to rescue even when the estimate (Beh19) could not be trusted.
- **fix_directive:** Any pump controller out of order, or the steam device out of order, makes the estimate unreliable: in rescue this is emergency stop; in normal or degraded a level failure under this condition goes straight to emergency stop (FR8, most severe consequence) instead of rescue for one cycle.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 85-92, `levelLossUnrecoverable, rescueAbandoned`: SB-Beh11, SB-Beh17, SB-Beh21, SB-FR8

### 8. [ambiguous_requirement] What counts as an aberrant message

- **raw:** SB-FR5 gives no list.
- **fix_directive:** Aberrant: a duplicate data or indexed message, a pump index outside 1..4, STEAM_BOILER_WAITING outside initialization, PHYSICAL_UNITS_READY outside initialization or before PROGRAM_READY was sent, a repair for a unit not out of order, an acknowledgement with no pending detection.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 121-349, `receive, analyse`: SB-FR5

### 9. [design_choice] Initialization details

- **raw:** SB-Beh2..Beh7 leave the order of actions open.
- **fix_directive:** Before STEAM_BOILER_WAITING only STOP is acted on. After it, the zero-steam check (Beh3) applies on every initialization cycle. Filling uses one pump; running pumps are closed before the valve is opened; the valve is closed when the level drops below N1 or enters [N1, N2]. Once PROGRAM_READY is sent it is repeated each cycle with no further level adjustment.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 59-79, `init predicates`: SB-Beh2, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh7
  - `java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 99-177, `step (INITIALIZATION)`: SB-Beh2, SB-Beh9

### 10. [scope_question] Emergency stop ends the program

- **raw:** SB-Beh22 (program stops) versus SB-Beh24 (MODE every cycle).
- **fix_directive:** MODE(emergency stop) is sent once, on entry; afterwards runCycle returns no messages and EMERGENCY_STOP has no transitions (a terminal state).
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 265-268, `step (EMERGENCY_STOP)`: SB-Beh22, SB-Beh24

## next_step

Run the pipeline; revisit the invented constants and policies only if the requirements owner supplies values or a verifier objects.
