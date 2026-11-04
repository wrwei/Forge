# post_codegen

**status:** uncertain

**summary:** All 53 requirements are implemented and traced (246 trace entries). The controller is a 5-mode machine with 42 transitions. Each is triggered by one of six per-cycle events the unit monitor derives from the transmission; guards are flat conjunctions of 8 boolean sensor facts and 3 controller booleans, and every non-terminal mode has an event-only transition. Failure detection, unit bookkeeping, estimation and the failure/repair protocol are in UnitMonitor. 12 points the requirements leave open are listed below.

## Issues

### 1. [invented_default] Boiler, steam and pump constants have no values in the requirements

- **raw:** SB-DM1..DM3 name C, M1, M2, N1, N2, W, U1, U2, P without values. Chosen: C=1000, M1=150, M2=850, N1=400, N2=600 (litre); W=25 (l/s); U1=U2=4 (l/s/s); P=10 (l/s). They respect M1<N1<N2<M2<=C, the DM1 danger statements (M1-5W>0, M2+4*5P>C) and N2+4*5P<M2. The constants are used by the unit monitor; the controller sees only the outcome of the comparisons.
- **fix_directive:** Replace with the plant's real constants if known.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerConstants.java` lines 13-44 — `SteamBoilerConstants` — SB-DM1, SB-DM2, SB-DM3

### 2. [invented_default] Pumps are expected closed until the program orders otherwise

- **raw:** SB-FR1/FR2 judge pumps against the program's orders, but nothing says what state a pump has before any order. The monitor assumes closed and not flowing; detection starts with STEAM_BOILER_WAITING (SB-Beh2). A repaired pump is expected to keep its reported state, and its controller gets two transmissions to settle, as after an order.
- **fix_directive:** If pumps may start open, take the first reported state as the baseline instead.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/monitor/UnitMonitor.java` lines 354-445 — `acceptRepairs / detectPumpFailures / detectPumpControlFailures` — SB-FR1, SB-FR2, SB-FR7

### 3. [design_choice] One input event per cycle, classified by the unit monitor; other facts read through the sensor service

- **raw:** SB-V3 makes all of a cycle's messages simultaneous, and PUMP_STATE(n,b) carries two values. The messages (SB-DM9) go to UnitMonitor's receive methods. The monitor analyses the transmission and steps the controller with exactly one event, in order of precedence: StopConfirmed (STOP on 3 successive transmissions), TransmissionFault, LevelRisk (outside initialisation), SteamBoilerWaiting (first arrival), PhysicalUnitsReady (legitimate answer to PROGRAM_READY), Transmission. Conditions that send every mode to emergency stop thus come first, as event-only transitions. The remaining facts are 8 booleans in BoilerSensor. Every guard is a flat conjunction of these facts and 3 controller booleans; for each event, the branches of a mode are mutually exclusive, checked over all assignments. All 42 transitions have a trigger, so there are no event-less cycles. In initialisation, SteamBoilerWaiting after the first and PhysicalUnitsReady before PROGRAM_READY have no branch: the monitor never delivers them.
- **fix_directive:** None.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/monitor/UnitMonitor.java` lines 243-310 — `analyse / classify` — SB-DM9, SB-V3, SB-FR8
  - `java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 47-311 — `step` — SB-FR8

### 4. [design_choice] Failure detection, unit status, STOP count, level estimate and the failure/repair protocol live in UnitMonitor, outside the extracted state machine

- **raw:** Actions in the profile cannot be conditional, and every controller field becomes a model state variable. Per-unit bookkeeping, per-unit announcements and real-valued history would multiply branches and state space (iterations 1 and 2: FDR4 ran out of memory, and Isabelle's deadlock-freedom proof timed out). SB-V5..V10 and SB-FR1..FR7 are implemented in Java but are not verified by the model checkers.
- **fix_directive:** Accept, or move selected bookkeeping into the controller if the model must cover it.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/monitor/UnitMonitor.java` lines 1-686 — `UnitMonitor` — SB-V5, SB-V6, SB-V7, SB-V8, SB-V9, SB-V10, SB-FR1, SB-FR2, SB-FR3, SB-FR4, SB-FR5, SB-FR6, SB-FR7

### 5. [design_choice] Regulation switches all four pumps together, on the predicted worst-case level

- **raw:** SB-Beh10 leaves the pump policy open. In normal, degraded and rescue modes the program opens all pumps when the level could fall below N1 before the next transmission, and closes them all when it could rise above N2 (and cannot fall below N1); this anticipates the one-cycle start-up latency (SB-Beh25). The limit risk (SB-Beh13, delivered as the LevelRisk event) is computed by the monitor with the pumps closed when the controller will close them, and as they are otherwise; the monitor thus embeds the controller's regulation rule. In a simulation under maximal steam, the level oscillates between about 240 and 550 l, with no emergency stop.
- **fix_directive:** A finer policy (per-pump counts) would narrow the oscillation at the cost of more transitions.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/monitor/UnitMonitor.java` lines 516-560 — `publish` — SB-V4, SB-Beh13, SB-Beh25
  - `java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 164-307 — `step (operating modes)` — SB-Beh10, SB-Beh15, SB-Beh19

### 6. [design_choice] Initialisation: checks start the cycle after STEAM_BOILER_WAITING; all four pumps fill; the valve closes one cycle before filling

- **raw:** Each initialisation action is its own flat transition. The arrival of STEAM_BOILER_WAITING is recorded, and the steam-zero and level checks and the adjustment start on the next cycle (5 s later). SB-Beh5 says 'activates a pump'; all four are opened. When the level stops being above N2, the valve is closed first, and filling or PROGRAM_READY follow on the next cycle. An abort leaves the valve as it is (the environment takes over, SB-Beh22).
- **fix_directive:** Open a single pump instead if 'a pump' is meant literally.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 65-163 — `step (INITIALIZATION block)` — SB-Beh2, SB-Beh3, SB-Beh4, SB-Beh5, SB-Beh7, SB-Beh8, SB-DM7

### 7. [design_choice] SB-Beh3 is a steam-device failure judgement

- **raw:** In initialisation, after STEAM_BOILER_WAITING, a steam reading other than zero marks the steam device out of order (and STEAM_FAILURE_DETECTION is announced); the controller then goes to emergency stop.
- **fix_directive:** None.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/monitor/UnitMonitor.java` lines 383-406 — `detectSteamFailure` — SB-Beh3, SB-FR4

### 8. [ambiguous_requirement] MODE on every cycle (SB-Beh24) vs. the program stopping in emergency stop (SB-Beh22)

- **raw:** Emergency stop is terminal: MODE(emergency_stop) is sent in the cycle that enters it, after which the program analyses and sends nothing.
- **fix_directive:** If MODE must continue in emergency stop, add a Transmission self-loop to EMERGENCY_STOP.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 308-310 — `step (EMERGENCY_STOP block)` — SB-Beh22, SB-Beh24
  - `java.generated.project/src/main/java/steam_boiler/SteamBoilerProgram.java` lines 46-56 — `runCycle` — SB-Beh22

### 9. [ambiguous_requirement] Which messages are 'aberrant' (SB-FR5)

- **raw:** Treated as transmission failures: a data message missing or duplicated; a pump number outside 1..4; STEAM_BOILER_WAITING outside initialisation; PHYSICAL_UNITS_READY other than in initialisation after PROGRAM_READY; a repair notice for a unit not out of order; a failure acknowledgement with no pending announcement.
- **fix_directive:** Relax any of these if the physical units may legitimately repeat a message.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/monitor/UnitMonitor.java` lines 312-330 — `transmissionFaulty` — SB-FR5, SB-Beh9, SB-Beh14

### 10. [design_choice] Level-device dynamics check and estimate

- **raw:** The evacuation valve's rate is not given, so SB-FR3's dynamic check is skipped during initialisation (static [0, C] check only). Elsewhere a reading must lie within [lower bound - max steam, upper bound + open pumps - min steam] since the last transmission. The rescue estimate (SB-V8) is a lower/upper bound advanced by worst-case steam and the water of sound pumps that stayed open.
- **fix_directive:** Supply the valve's outflow rate to enable the dynamic check during initialisation.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/monitor/UnitMonitor.java` lines 467-486 — `updateLevelKnowledge` — SB-FR3, SB-V8, SB-Beh19
  - `java.generated.project/src/main/java/steam_boiler/monitor/UnitMonitor.java` lines 632-656 — `prepareNextEnvelope` — SB-V8, SB-Beh25

### 11. [design_choice] Simultaneous level failure and steam/pump-controller failure in normal or degraded mode

- **raw:** Per SB-FR8, the steam/pump-controller conditions demand emergency stop only in rescue mode. Normal and degraded therefore go to rescue first, and rescue stops at the next cycle.
- **fix_directive:** Add steam/pump-controller emergency branches to normal/degraded if an immediate stop is wanted.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 164-253 — `step (NORMAL, DEGRADED blocks)` — SB-FR8, SB-Beh11, SB-Beh17, SB-Beh21

### 12. [scope_question] Separate enum for the MODE(m) value

- **raw:** ModeValue (SB-DM10) mirrors OperatingMode (SB-V2) with distinct literal names, so the names of the payload enumeration do not coincide with the state names in the model.
- **fix_directive:** None.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/mode/ModeValue.java` lines 7-13 — `ModeValue` — SB-DM10

## next_step

Run the verification pipeline; fix the Java from the phase reports.
