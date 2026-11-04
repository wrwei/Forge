# post_codegen

**status:** uncertain

**summary:** Java for steam_boiler written in five layers (13 files; five-mode controller with a terminal emergency stop, 29 transitions); compiles; trace covers all 53 requirements. Issues below record the values and readings the requirements left open.

## Issues

### 1. [invented_default] Values of the plant constants C, M1, M2, N1, N2, W, U1, U2, P

- **raw:** SB-DM1/DM2/DM3 name the constants but give no values. Chosen: C=1000, M1=150 (MINIMAL_M1), M2=800 (MAXIMAL_M2), N1=400, N2=600 (litre); W=25 (l/s); U1=U2=4 (l/s/s); P=10 (l/s); 4 pumps; cycle 5 s; STOP x3. M1 >= 5 s of maximal steam (125 l) and C-M2 >= 5 s of four pumps (200 l), as SB-DM1 requires. All values are integral, so the model's rounding-up of constants changes nothing.
- **fix_directive:** None unless real plant values are supplied; then change BoilerConstants only.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/controller/BoilerConstants.java` lines 11-37, `BoilerConstants` — SB-DM1, SB-DM2, SB-DM3, SB-Beh23

### 2. [design_choice] One input event per cycle; incoming messages go to the sensor

- **raw:** All messages of a cycle arrive simultaneously (SB-V3) and step is called once per cycle, so the controller is triggered by one event, MessagesReceived. The SB-DM9 messages are handed to BoilerSensor.receive... methods and reach the guards as sensor queries, not as InputEvent records.
- **fix_directive:** None.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/event/InputEvent.java` lines 1-11, `MessagesReceived` — SB-V3, SB-DM9
  - `java.generated.project/src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 97-171, `receive methods` — SB-DM9

### 3. [design_choice] Failure detection, unit status, STOP count and level estimate live in BoilerSensor

- **raw:** Per-unit bookkeeping (10 units, failed/acknowledged), pump order tracking, steam/level dynamics and the lower/upper level estimate are computed in BoilerSensor.analyse() (with UnitStatus and PumpMonitor). The state machine sees only their results (successiveStops, transmissionFailure, levelDeviceSound, otherUnitsSound, ..., lowest/highestReachableLevel, waterLevel). This keeps the model's state small; the detection logic itself is not part of the verified model.
- **fix_directive:** None.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 176-419, `analyse` — SB-V8, SB-V9, SB-V10, SB-FR1, SB-FR2, SB-FR3, SB-FR4, SB-FR5, SB-Beh25

### 4. [design_choice] Failure announcements and repair acknowledgements are sent outside the state machine

- **raw:** PUMP_FAILURE_DETECTION(n) ... STEAM_REPAIRED_ACKNOWLEDGEMENT depend on per-unit status, which a transition action cannot branch on. BoilerSensor.protocolMessages() produces them each cycle and ControlCycle appends them to the transmission. The controller sends MODE, PROGRAM_READY, VALVE, OPEN_PUMP and CLOSE_PUMP.
- **fix_directive:** None.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 235-253, `protocolMessages` — SB-FR6, SB-FR7, SB-DM8
  - `java.generated.project/src/main/java/steam_boiler/controller/ControlCycle.java` lines 27-42, `runCycle` — SB-V3, SB-FR6, SB-FR7

### 5. [design_choice] Separate enums for controller modes and the MODE(m) payload

- **raw:** BoilerMode gives the states; OperatingMode is the datatype MODE(m) carries. Its literals are MODE_INITIALIZATION ... MODE_EMERGENCY_STOP so that no enum literal shares a name with a state (iteration 1: Dafny and Isabelle could not resolve the shared names).
- **fix_directive:** None.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/controller/BoilerMode.java` lines 1-10, `BoilerMode` — SB-V2
  - `java.generated.project/src/main/java/steam_boiler/domain/OperatingMode.java` lines 1-15, `OperatingMode` — SB-DM10

### 6. [design_choice] Regulation policy

- **raw:** Normal/degraded/rescue: below N1 open all four pumps, above N2 close all four, otherwise leave pumps as they are (rescue uses the midpoint of the level estimate). Initialization: below N1 opens all four pumps (SB-Beh5 says 'a pump'; four tolerate a pump failure), and the pumps are closed when PROGRAM_READY is first sent. A cycle that changes mode sends MODE only; regulation resumes the next cycle.
- **fix_directive:** None.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 63-190, `step` — SB-Beh5, SB-Beh10, SB-Beh15, SB-Beh19

### 7. [ambiguous_requirement] Meaning of 'the level risks reaching M1 or M2'

- **raw:** Interpreted as: the lowest level reachable within the coming cycle (worst-case steam from U1/W, only pumps already past their start-up latency counted) is below M1, or the highest reachable level (least steam, defective pumps assumed to keep pouring, sound pumps closable at once) is above M2. Also true if the current bounds are already outside [M1, M2].
- **fix_directive:** None unless a different risk criterion is intended.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 53, `operatingEmergency` — SB-V4, SB-Beh13, SB-Beh18, SB-Beh21
  - `java.generated.project/src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 287-373, `analyseReadings, predictReachableLevels` — SB-Beh13, SB-Beh25

### 8. [ambiguous_requirement] 'If the pump-controller units fail' in rescue mode

- **raw:** Read as: any pump controller out of order (the estimate needs reliable controller information). If the level device fails while the steam device or a pump controller is (or becomes) defective, the program goes to emergency stop directly instead of entering rescue first: the rescue estimate could not be trusted, and SB-FR8 says the most severe consequence prevails.
- **fix_directive:** None.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 477-549, `summariseResults (emergencyFlag)` — SB-Beh21, SB-FR8

### 9. [ambiguous_requirement] 'Nothing else happens' before STEAM_BOILER_WAITING

- **raw:** Before STEAM_BOILER_WAITING the program only sends MODE(initialization); no failure detection, no level adjustment. Operator stop (SB-Beh23, 'in every mode') and transmission failure (SB-Beh9) still force emergency stop.
- **fix_directive:** None.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 63-113, `step (initialization block)` — SB-Beh2, SB-Beh9, SB-Beh23

### 10. [scope_question] Which messages count as aberrant (transmission failure)

- **raw:** Aberrant (judged by BoilerSensor, reported through transmissionFailure and emergencyDemanded): STEAM_BOILER_WAITING or PHYSICAL_UNITS_READY after initialization; PHYSICAL_UNITS_READY before PROGRAM_READY was sent (also tested by the controller in initialization); a data message duplicated; a failure acknowledgement with no pending announcement; a repair notice for a unit not out of order. Missing: any of LEVEL, STEAM, PUMP_STATE(n), PUMP_CONTROL_STATE(n).
- **fix_directive:** None.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 40-53, `readyAberrant, initCalm, operatingEmergency` — SB-FR5, SB-Beh9, SB-Beh14
  - `java.generated.project/src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 176-212, `analyse` — SB-FR5

### 11. [design_choice] Emergency stop ends control but keeps reporting MODE

- **raw:** SB-Beh22 ends the control task in emergency stop; SB-Beh24 asks for MODE(m) on every cycle in whatever mode. In emergency stop the program does no analysis, no regulation and no failure protocol; each cycle it only sends MODE(emergency stop) (one event-triggered self-loop). Without that operation the Z-Machine had a reachable state with no enabled operation, and its deadlock-freedom proof timed out (it 11).
- **fix_directive:** None.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 183-190, `step (EMERGENCY_STOP block)` — SB-Beh22, SB-Beh24
  - `java.generated.project/src/main/java/steam_boiler/controller/ControlCycle.java` lines 27-42, `runCycle` — SB-Beh22, SB-V3

### 12. [design_choice] When the level dynamics check is suspended

- **raw:** The level-dynamics check (SB-FR3) is not applied while the evacuation valve is open (its drain rate is not given), on the first cycle after STEAM_BOILER_WAITING, and on the first reading after LEVEL_REPAIRED; the static range check [0, C] always applies.
- **fix_directive:** None.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 397-419, `judgeLevel` — SB-FR3, SB-DM7

### 13. [design_choice] Shape of the guards: seven boolean sensor queries, one operator per predicate

- **raw:** All numeric comparisons (level vs N1/N2, reachable levels vs M1/M2, steam vs zero, STOP count vs STOP_REPETITIONS) are made in BoilerSensor.analyse(). The guards call seven boolean queries, each returning a field set from literals, a single comparison or another boolean, and every value those fields depend on is a field, an annotated parameter or a constant, assigned in methods that declare no double locals and call no Math function (predictReachableLevels, judgeLevel, judgeSteam): emergencyDemanded (three STOPs or a transmission failure, aberrant initialization messages included; during initialization after STEAM_BOILER_WAITING, a non-zero steam rate or a failed level device; once PHYSICAL_UNITS_READY has arrived in an earlier cycle, a level that can leave [M1, M2] within the coming cycle), boilerWaitingSeen (STEAM_BOILER_WAITING has arrived; replaces the controller's former plantWaiting variable), physicalUnitsReadyReceived, levelDeviceSound, otherUnitsSound, levelBelowNormalBand, levelAboveNormalBand (seven queries, each also used by an initialization guard). emergencyDemanded also covers, after initialization, a failed level device together with a failed steam device or pump controller, so the rescue block uses the same predicates as normal and degraded. Pump regulation is one SwitchPumps(ALL_ON | ALL_OFF) order per branch, which BoilerActuator sends as OPEN_PUMP(n) / CLOSE_PUMP(n) for every pump; at most four output events per transition. Each predicate and each branch condition is written on a single source line; each predicate uses one operator; a predicate of the other operator is referenced only negated; no predicate is a bare alias of a sensor call. History: real-valued functions (it 1), an alias predicate (it 2) and a query whose result is computed in its body (it 3) and, in it 4 to it 8, the one query used by no initialization guard (the level-risk query, whatever its name, body, provenance or source line) were typed real in Isabelle; positive predicate references were expanded without parentheses (it 3, Dafny); FDR4 never finished compiling.
- **fix_directive:** None in the Java. Possible pipeline defects for the operator: (0) a sensor function used only by guards of non-initial states is typed real; (0b) Isabelle raised exception Empty on the initialization operation INIT -> NORMAL once its guard called pumpsSound/estimateTrustworthy instead of otherUnitsSound (it 9, it 10); positive predicate references are expanded unparenthesised; the type of some boolean sensor functions is inferred as real from how the returned value is computed; an alias predicate is rendered under its own name.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 36-61, `step (predicates)` — SB-V4, SB-Beh10, SB-Beh13, SB-Beh23, SB-FR8
  - `java.generated.project/src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 477-549, `summariseResults` — SB-V9, SB-V10, SB-Beh3, SB-Beh13, SB-Beh21

## next_step

Run the pipeline; fix the Java from the phase reports.
