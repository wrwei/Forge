# post_codegen

**status:** uncertain

**summary:** All 53 requirements are implemented and traced (227 trace entries). The controller is a 5-mode machine whose every transition is triggered by the per-cycle Transmission event. The guards in each mode are made mutually exclusive and exhaustive with explicit negations. Failure detection, unit bookkeeping, estimation and the failure/repair protocol are in UnitMonitor. 11 points the requirements leave open are listed below.

## Issues

### 1. [invented_default] Boiler, steam and pump constants have no values in the requirements

- **raw:** SB-DM1..DM3 name C, M1, M2, N1, N2, W, U1, U2, P without values. Chosen: C=1000, M1=150, M2=850, N1=400, N2=600 (litre); W=25 (l/s); U1=U2=4 (l/s/s); P=10 (l/s). They respect M1<N1<N2<M2<=C, the DM1 danger statements (M1-5W>0, M2+4*5P>C) and N2+4*5P<M2. All are integers, so the model's rounding of fractional constants does not apply.
- **fix_directive:** Replace with the plant's real constants if known; keep N2 + PUMP_COUNT*P*CYCLE_SECONDS < M2 so all-pump regulation cannot itself create an M2 risk.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerConstants.java` lines 13-44 — `SteamBoilerConstants` — SB-DM1, SB-DM2, SB-DM3

### 2. [invented_default] Pumps are expected closed until the program orders otherwise

- **raw:** SB-FR1/FR2 judge pumps against the program's orders, but nothing says what state a pump has before any order. The monitor assumes closed and not flowing; a pump reported open (or a controller reporting flow) before any order counts as a spontaneous change. Detection starts with STEAM_BOILER_WAITING (SB-Beh2).
- **fix_directive:** If pumps may start open, take the first reported state as the baseline instead.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/monitor/UnitMonitor.java` lines 365-402 — `detectPumpFailures / detectPumpControlFailures` — SB-FR1, SB-FR2

### 3. [design_choice] One input event per cycle; message contents read through the sensor service

- **raw:** SB-V3 makes all of a cycle's messages simultaneous, and PUMP_STATE(n,b) carries two values. The controller is stepped once per cycle by InputEvent.Transmission; the messages (SB-DM9) are delivered to UnitMonitor's receive methods and the controller reads their analysis from BoilerSensor. Every model transition is triggered by Transmission, so there are no event-less cycles (no divergence).
- **fix_directive:** None unless one record per input message is required as a model event.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/event/InputEvent.java` lines 9-13 — `Transmission` — SB-DM9, SB-V3
  - `java.generated.project/src/main/java/steam_boiler/monitor/UnitMonitor.java` lines 135-224 — `receive*` — SB-DM9

### 4. [design_choice] Failure detection, unit status, STOP count, level estimate and the failure/repair protocol live in UnitMonitor, outside the extracted state machine

- **raw:** Actions in the profile cannot be conditional, and every controller field becomes a model state variable. Per-unit bookkeeping (10 units x status/ack), per-unit announcements and acknowledgements, and real-valued history would multiply the model's branches and state space. The controller keeps the mode logic, the initialisation sub-phases and the valve; the model sees the monitor's results as sensor functions. SB-V5..V10 and SB-FR1..FR7 are therefore implemented, but not verified by the model checkers.
- **fix_directive:** Accept, or move selected bookkeeping into the controller if the model must cover it.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/monitor/UnitMonitor.java` lines 1-627 — `UnitMonitor` — SB-V5, SB-V6, SB-V7, SB-V8, SB-V9, SB-V10, SB-FR1, SB-FR2, SB-FR3, SB-FR4, SB-FR5, SB-FR6, SB-FR7

### 5. [design_choice] Regulation switches all four pumps together, on the predicted worst-case level

- **raw:** SB-Beh10 leaves the pump policy open. In normal, degraded and rescue modes the program opens all pumps when the level could fall below N1 before the next transmission, and closes them all when it could rise above N2; this anticipates the one-cycle start-up latency (SB-Beh25). The risk of M1/M2 (SB-Beh13) is judged with the pumps as this cycle leaves them (closed-pump bounds when closing). A one-cycle simulation outside the deliverable shows the level oscillating at about 240-550 l under maximal steam, with no emergency stop.
- **fix_directive:** A finer policy (per-pump counts) would narrow the oscillation at the cost of more transitions.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 57-66 — `levelMayFallBelowN1 / riskAboveM2` — SB-Beh10, SB-Beh13, SB-Beh15, SB-Beh19, SB-Beh25, SB-V4

### 6. [design_choice] Initialisation fills with all four pumps and adjusts on the current reading

- **raw:** SB-Beh5 says 'activates a pump'; all four are opened (any sound pump fills, idempotent orders). Initialisation follows SB-Beh4/5/7 literally on q. When the level is between N1 and N2 the program closes the pumps (and the valve if open), announces PROGRAM_READY, and stops adjusting until PHYSICAL_UNITS_READY; so the valve is always closed when initialisation ends normally. An abort with the valve open closes it on the way to emergency stop.
- **fix_directive:** Open a single pump instead if 'a pump' is meant literally.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 78-164 — `step (INITIALIZATION block)` — SB-Beh4, SB-Beh5, SB-Beh7, SB-DM7

### 7. [ambiguous_requirement] MODE on every cycle (SB-Beh24) vs. the program stopping in emergency stop (SB-Beh22)

- **raw:** Emergency stop is terminal: MODE(emergency_stop) is sent in the cycle that enters it, after which the program analyses and sends nothing.
- **fix_directive:** If MODE must continue in emergency stop, add a Transmission self-loop to EMERGENCY_STOP.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 267-269 — `step (EMERGENCY_STOP block)` — SB-Beh22, SB-Beh24
  - `java.generated.project/src/main/java/steam_boiler/SteamBoilerProgram.java` lines 46-56 — `runCycle` — SB-Beh22

### 8. [ambiguous_requirement] Which messages are 'aberrant' (SB-FR5)

- **raw:** Treated as transmission failures: a data message missing or duplicated; a pump number outside 1..4; STEAM_BOILER_WAITING outside initialisation; PHYSICAL_UNITS_READY other than in initialisation after PROGRAM_READY; a repair notice for a unit not out of order; a failure acknowledgement with no pending announcement.
- **fix_directive:** Relax any of these if the physical units may legitimately repeat a message.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/monitor/UnitMonitor.java` lines 278-296 — `transmissionFaulty` — SB-FR5, SB-Beh9, SB-Beh14

### 9. [design_choice] Level-device dynamics check and estimate

- **raw:** The rate of the evacuation valve is not given, so the dynamic check of SB-FR3 is skipped in initialisation (static [0, C] check only). Elsewhere a reading must lie within [lower bound - max steam, upper bound + open pumps - min steam] since the last transmission. The rescue estimate (SB-V8) is a lower/upper bound advanced by worst-case steam and the water of sound pumps that stayed open; it is published as worst-case predictions.
- **fix_directive:** Supply the valve's outflow rate to enable the dynamic check during initialisation.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/monitor/UnitMonitor.java` lines 424-443 — `updateLevelKnowledge` — SB-FR3, SB-V8, SB-Beh19
  - `java.generated.project/src/main/java/steam_boiler/monitor/UnitMonitor.java` lines 574-597 — `prepareNextEnvelope` — SB-V8, SB-Beh25

### 10. [design_choice] Simultaneous level failure and steam/pump-controller failure in normal or degraded mode

- **raw:** Per SB-FR8 the steam/pump-controller conditions demand emergency stop only in rescue mode, so normal/degraded go to rescue first and rescue stops at the next cycle.
- **fix_directive:** Add steamDeviceFailed || pumpControllerFailed to the emergency guard of normal/degraded if an immediate stop is wanted.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 166-232 — `step (NORMAL, DEGRADED blocks)` — SB-FR8, SB-Beh11, SB-Beh17, SB-Beh21

### 11. [scope_question] Separate enum for the MODE(m) value

- **raw:** ModeValue (SB-DM10) mirrors OperatingMode (SB-V2) with distinct literal names, so that the payload enumeration's names do not coincide with the state names in the extracted model.
- **fix_directive:** None.
- **java_trace:**
  - `java.generated.project/src/main/java/steam_boiler/mode/ModeValue.java` lines 7-13 — `ModeValue` — SB-DM10

## next_step

Run the verification pipeline; fix the Java from the phase reports.
