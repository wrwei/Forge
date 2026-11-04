# post_codegen

**status:** uncertain

**summary:** All 53 requirements are implemented and traced (268 trace entries). The controller is a 5-mode machine with 17 transitions, each triggered by one of four per-cycle events the unit monitor derives from the transmission, over 4 boolean sensor facts and 3 initialisation booleans. It decides the mode and the initialisation handshake. The transmission stage sends MODE(m), the controller's valve/pump/PROGRAM_READY decisions, the regulation of the operating modes and the failure/repair protocol. 12 points the requirements leave open are listed below.

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
  - `java.generated.project/src/main/java/steam_boiler/monitor/UnitMonitor.java` lines 369-462 — `acceptRepairs / detectPumpFailures / detectPumpControlFailures` — SB-FR1, SB-FR2, SB-FR7

### 3. [design_choice] The extracted state machine holds the mode logic and the initialisation handshake; the monitor classifies each cycle into one event

- **raw:** SB-V3 makes all of a cycle's messages simultaneous, and PUMP_STATE(n,b) carries two values. The messages (SB-DM9) go to UnitMonitor's receive methods. The monitor analyses the transmission and steps the controller with exactly one event, in order of precedence: EmergencyStopDemanded, SteamBoilerWaiting (first arrival), PhysicalUnitsReady (legitimate answer to PROGRAM_READY), Transmission. EmergencyStopDemanded means a condition that demands emergency stop in the current mode: STOP on three successive transmissions or a transmission failure in every mode; in initialisation, after STEAM_BOILER_WAITING, a level-device failure or steam not zero; outside initialisation a risk of M1/M2; in rescue a steam-device or pump-controller failure. The controller has 17 transitions, all triggered by an event, over 2 unit facts, 2 level facts and 3 initialisation booleans; each mode has an event-only transition, and for each event the branches are mutually exclusive. This shape follows iterations 1-5. FDR4 exhausted memory until the free sensor facts were cut to 4. Isabelle's generic deadlock_free proof timed out with 29-43 transitions (the corpus examples it is used on have 3-5 operations). The cost: which conditions demand emergency stop in which mode is decided in the monitor, not visible in the model.
- **fix_directive:** Accept, or move the per-mode emergency conditions back into the controller if a larger model can be verified.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/monitor/UnitMonitor.java` lines 251-325 — `analyse / classify` — SB-DM9, SB-V3, SB-FR8, SB-Beh3, SB-Beh6, SB-Beh9, SB-Beh13, SB-Beh14, SB-Beh18, SB-Beh21, SB-Beh23
  - `java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 61-145 — `step` — SB-FR8

### 4. [design_choice] Failure detection, unit status, STOP count, level estimate and the failure/repair protocol live in UnitMonitor, outside the extracted state machine

- **raw:** Actions in the profile cannot be conditional, and every controller field becomes a model state variable. Per-unit bookkeeping, per-unit announcements and real-valued history would multiply branches and state space (iterations 1 to 5: FDR4 ran out of memory, Isabelle timed out). SB-V5..V10 and SB-FR1..FR7 are implemented in Java but are not verified by the model checkers.
- **fix_directive:** Accept, or move selected bookkeeping into the controller if the model must cover it.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/monitor/UnitMonitor.java` lines 1-743 — `UnitMonitor` — SB-V5, SB-V6, SB-V7, SB-V8, SB-V9, SB-V10, SB-FR1, SB-FR2, SB-FR3, SB-FR4, SB-FR5, SB-FR6, SB-FR7

### 5. [design_choice] Regulation in the operating modes is applied by the transmission stage; it switches all four pumps together, on the predicted worst-case level

- **raw:** SB-Beh10, SB-Beh15 and SB-Beh19 say what the normal, degraded and rescue modes do on every cycle, not how they change. That per-cycle control law is applied by the program after the controller's step, according to the mode: all pumps are ordered open when the level predicted until the next transmission could fall below N1, and closed when it could rise above N2 (and cannot fall below N1); otherwise they stay as they are. This anticipates the one-cycle start-up latency (SB-Beh25), and in rescue it uses the estimate. Orders are sent only to pumps whose last order differs. The limit risk is computed with the pumps closed when the regulation will close them. In a simulation under maximal steam, the level oscillates between about 220 and 550 l, with no emergency stop.
- **fix_directive:** A finer policy (per-pump counts) would narrow the oscillation at the cost of more transitions.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/monitor/UnitMonitor.java` lines 531-613 — `publish / regulatePumps / sendPumpOrders` — SB-V4, SB-Beh10, SB-Beh13, SB-Beh15, SB-Beh19, SB-Beh25
  - `java.generated.project/src/main/java/steam_boiler/SteamBoilerProgram.java` lines 52-76 — `runCycle` — SB-Beh10, SB-Beh15, SB-Beh19

### 6. [design_choice] Initialisation: all four pumps fill; the valve, pumps and PROGRAM_READY are controller decisions sent by the transmission stage

- **raw:** SB-Beh5 says 'activates a pump'; all four are ordered open. Nothing happens before STEAM_BOILER_WAITING (SB-Beh2). From the next cycle, the controller decides on the reading q: above N2, valve open and pumps closed; below N1, valve closed and pumps open; between N1 and N2, both closed and PROGRAM_READY. The steam and level checks apply already in the arrival cycle (EmergencyStopDemanded). The transmission stage sends VALVE when the valve decision changes, the pump orders, and PROGRAM_READY on every cycle until PHYSICAL_UNITS_READY. An abort leaves the valve as it is (the environment takes over, SB-Beh22).
- **fix_directive:** Open a single pump instead if 'a pump' is meant literally.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 71-102 — `step (INITIALIZATION block)` — SB-Beh2, SB-Beh4, SB-Beh5, SB-Beh7, SB-Beh8, SB-DM7
  - `java.generated.project/src/main/java/steam_boiler/SteamBoilerProgram.java` lines 52-76 — `runCycle` — SB-Beh4, SB-Beh5, SB-Beh7

### 7. [design_choice] SB-Beh3 is a steam-device failure judgement

- **raw:** In initialisation, once STEAM_BOILER_WAITING has arrived, a steam reading other than zero marks the steam device out of order (and STEAM_FAILURE_DETECTION is announced); the cycle is delivered as EmergencyStopDemanded, and the controller goes to emergency stop.
- **fix_directive:** None.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/monitor/UnitMonitor.java` lines 398-421 — `detectSteamFailure` — SB-Beh3, SB-FR4
  - `java.generated.project/src/main/java/steam_boiler/monitor/UnitMonitor.java` lines 288-325 — `classify` — SB-Beh3, SB-Beh6

### 8. [ambiguous_requirement] MODE on every cycle (SB-Beh24) vs. the program stopping in emergency stop (SB-Beh22)

- **raw:** Resolved for SB-Beh24: MODE(m) is sent by the transmission stage on every cycle, in every mode. In emergency stop the control task has ended (SB-Beh22): the program analyses nothing and sends nothing else, but goes on announcing MODE(emergency_stop). In the model this is an event-only self-loop of EMERGENCY_STOP with no action.
- **fix_directive:** If the program must fall silent in emergency stop, drop the MODE message there (the self-loop can stay).
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 139-143 — `step (EMERGENCY_STOP block)` — SB-Beh22, SB-Beh24
  - `java.generated.project/src/main/java/steam_boiler/SteamBoilerProgram.java` lines 52-76 — `runCycle` — SB-Beh22, SB-Beh24

### 9. [ambiguous_requirement] Which messages are 'aberrant' (SB-FR5)

- **raw:** Treated as transmission failures: a data message missing or duplicated; a pump number outside 1..4; STEAM_BOILER_WAITING outside initialisation; PHYSICAL_UNITS_READY other than in initialisation after PROGRAM_READY; a repair notice for a unit not out of order; a failure acknowledgement with no pending announcement.
- **fix_directive:** Relax any of these if the physical units may legitimately repeat a message.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/monitor/UnitMonitor.java` lines 327-345 — `transmissionFaulty` — SB-FR5, SB-Beh9, SB-Beh14

### 10. [design_choice] Level-device dynamics check and estimate

- **raw:** The evacuation valve's rate is not given, so SB-FR3's dynamic check is skipped during initialisation (static [0, C] check only). Elsewhere a reading must lie within [lower bound - max steam, upper bound + open pumps - min steam] since the last transmission. The rescue estimate (SB-V8) is a lower/upper bound advanced by worst-case steam and the water of sound pumps that stayed open.
- **fix_directive:** Supply the valve's outflow rate to enable the dynamic check during initialisation.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/monitor/UnitMonitor.java` lines 482-501 — `updateLevelKnowledge` — SB-FR3, SB-V8, SB-Beh19
  - `java.generated.project/src/main/java/steam_boiler/monitor/UnitMonitor.java` lines 689-713 — `prepareNextEnvelope` — SB-V8, SB-Beh25

### 11. [design_choice] Simultaneous level failure and steam/pump-controller failure in normal or degraded mode

- **raw:** Per SB-FR8, the steam/pump-controller conditions demand emergency stop only in rescue mode. Normal and degraded therefore go to rescue first, and rescue stops at the next cycle.
- **fix_directive:** Add steam/pump-controller emergency branches to normal/degraded if an immediate stop is wanted.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 103-126 — `step (NORMAL, DEGRADED blocks)` — SB-FR8, SB-Beh11, SB-Beh17
  - `java.generated.project/src/main/java/steam_boiler/monitor/UnitMonitor.java` lines 288-325 — `classify` — SB-Beh21

### 12. [scope_question] Separate enum for the MODE(m) value

- **raw:** ModeValue (SB-DM10) mirrors OperatingMode (SB-V2) with distinct literal names, so the names of the payload enumeration do not coincide with the state names in the model.
- **fix_directive:** None.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/mode/ModeValue.java` lines 7-13 — `ModeValue` — SB-DM10

## next_step

Run the verification pipeline; fix the Java from the phase reports.
