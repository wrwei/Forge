# post_codegen

**status:** uncertain

**summary:** All 53 requirements are implemented and traced. Constant values were invented; several requirements were interpreted (emergency stop reporting only its mode, rescue-mode emergency conditions, level-risk forecast, aberrant messages, start-up check timing). Failure detection, the numeric comparisons and the failure/repair message protocol are outside the extracted state machine, which sees boolean sensor judgements.

## Issues

### 1. [invented_default] Values of the plant constants C, M1, M2, N1, N2, W, U1, U2, P

**raw:** SB-DM1, SB-DM2 and SB-DM3 name the constants but give no values. Chosen: C=1000, M1=150, M2=850, N1=400, N2=600 (litres); W=25 l/s; U1=U2=2 l/s/s; P=10 l/s; cycle 5 s. All are whole numbers, so rounding up in the model's constants interface changes none of them; the guards compare against N1, N2, M1, M2.

**fix_directive:** Replace the values in SteamBoilerConstants if the plant's real values are known; keep M1 < N1 < N2 < M2 < C.

**java_trace:**
- `java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerConstants.java` lines 12-48, `SteamBoilerConstants`, requirements: SB-DM1, SB-DM2, SB-DM3

### 2. [invented_default] Sensor quantities before the first transmission

**raw:** Before any transmission the level estimate, forecasts and steam rate are 0.0 and every unit is believed sound. SteamBoilerProgram always calls receive before step, so the controller never reads these initial values.

**fix_directive:** None needed unless step is called without a prior receive.

**java_trace:**
- `java.generated.project/src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 40-70, `BoilerSensor`, requirements: SB-V5, SB-V6, SB-V8, SB-V9

### 3. [design_choice] One trigger per cycle; message analysis outside the state machine

**raw:** SB-V3 says all messages of a cycle are received simultaneously. The whole transmission is passed to BoilerSensor.receive, then step is called once with InputEvent.TransmissionReceived, the only trigger of every transition. Failure detection (SB-FR1 to SB-FR5), unit status (SB-V9), the level estimate (SB-V8), the STOP count (SB-V10) and the failure-announcement and repair-acknowledgement messages (SB-FR6, SB-FR7) live in BoilerSensor and SteamBoilerProgram, so the extracted model sees them only as functions and the verifiers do not check their internals. The message records of SB-DM9 are declared as InputEvent records but are not triggers. The controller reads eight boolean sensor judgements. BoilerSensor.judge makes the comparisons with N1, N2, M1, M2, zero steam and the three-STOP limit, and combines the conditions that lead to emergency stop: initializationAbortDemanded (three STOPs, transmission failure, or nonzero steam / failed level device once STEAM_BOILER_WAITING has arrived) and emergencyStopDemanded (three STOPs, transmission failure, level risk, or the level unknowable). Every guard is the cycle event plus at most two of these judgements. The Java else-if order gives the priorities. The guards do not repeat the negations of the branches above them, and the last branch of each mode is the bare event, so the extracted model lets a mode's transitions overlap: it over-approximates the Java's priority order (needed for FDR4 memory and the Isabelle deadlock_free tactic).

**fix_directive:** Accepted design; a per-message event model would need an ordering of simultaneous messages that SB-V3 does not give.

**java_trace:**
- `java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 44-59, `step`, requirements: SB-V3, SB-DM9
- `java.generated.project/src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 108-127, `receive`, requirements: SB-V3, SB-FR5, SB-FR6, SB-FR7

### 4. [design_choice] Regulation strategy and initial filling

**raw:** In normal, degraded and rescue mode all four pumps are opened when the level (or, in rescue, the midpoint of the estimate) is below N1, closed when it is above N2, and left alone between. A cycle that changes mode issues no pump orders. In initialization only pump 1 is opened to fill (SB-Beh5 says 'a pump'), and it is closed once the level is acceptable.

**fix_directive:** None; any strategy keeping the level in [N1, N2] satisfies SB-Beh10.

**java_trace:**
- `java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 60-189, `step`, requirements: SB-Beh5, SB-Beh10, SB-Beh15, SB-Beh19

### 5. [design_choice] Separate enums for controller states and MODE(m) values

**raw:** BoilerMode (the five states) and OperatingMode (the value of MODE(m), with _MODE suffixes) model the same five alternatives of SB-DM10, so that state names and enumeration literals never share a name in the model.

**fix_directive:** None.

**java_trace:**
- `java.generated.project/src/main/java/steam_boiler/mode/OperatingMode.java` lines 1-15, `OperatingMode`, requirements: SB-DM10, SB-Beh24

### 6. [ambiguous_requirement] Emergency stop is terminal versus MODE(m) on every cycle

**raw:** SB-Beh24 asks for MODE(m) on every cycle in whatever mode; SB-Beh22 says the program stops on entering emergency stop. Resolved: EMERGENCY_STOP has no way out and no control action; the program keeps reporting MODE(EMERGENCY_STOP) on every cycle (a self-loop on TransmissionReceived) and sends no other message. Iteration 1 sent nothing after entry; changed in iteration 2.

**fix_directive:** If the program must fall silent after emergency stop, have SteamBoilerProgram stop calling step once EMERGENCY_STOP is reached.

**java_trace:**
- `java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 190-197, `step`, requirements: SB-Beh22, SB-Beh24
- `java.generated.project/src/main/java/steam_boiler/SteamBoilerProgram.java` lines 34-47, `runCycle`, requirements: SB-Beh22

### 7. [ambiguous_requirement] Rescue-mode emergency conditions and their precedence

**raw:** SB-Beh21 'the pump-controller units fail' is read as any pump controller out of order. Because SB-FR8 lets these rescue-mode conditions dominate a change to rescue, a level-device failure in normal or degraded mode while the steam device or a pump controller is already out of order leads directly to emergency stop.

**fix_directive:** Change levelUnknowable in BoilerSensor.judge if only the failure of all pump controllers is meant.

**java_trace:**
- `java.generated.project/src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 239-256, `judge`, requirements: SB-Beh21, SB-FR8, SB-Beh17

### 8. [ambiguous_requirement] How the level risk is judged

**raw:** SB-Beh13, SB-Beh18 and SB-Beh21 judge the risk of reaching M1 or M2 against worst-case behaviour without saying how. Risk = the worst-case level one cycle ahead reaches M1 or M2, assuming the program's best response: against M2 every sound pump is closed (takes effect at once); against M1 only pumps already pouring count, because a pump ordered on pours nothing for five seconds (SB-Beh25). Steam is bounded by W, U1 and U2.

**fix_directive:** None.

**java_trace:**
- `java.generated.project/src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 550-563, `updateForecasts`, requirements: SB-V4, SB-Beh13, SB-Beh25

### 9. [ambiguous_requirement] Which messages are aberrant

**raw:** SB-FR5 does not list aberrant messages. Treated as transmission failure: a data message missing or repeated; a pump number outside 1..4; a failure acknowledgement for a unit with no unacknowledged failure; STEAM_BOILER_WAITING or PHYSICAL_UNITS_READY when the last MODE sent was not initialization; PHYSICAL_UNITS_READY before any PROGRAM_READY (the sensor learns both from the outgoing transmission). A repair message from a sound unit is acknowledged again, not treated as aberrant.

**fix_directive:** None.

**java_trace:**
- `java.generated.project/src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 262-267, `startupMessageAberrant`, requirements: SB-FR5
- `java.generated.project/src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 371-377, `sortAcknowledgement`, requirements: SB-FR5
- `java.generated.project/src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 239-256, `judge`, requirements: SB-FR5, SB-Beh9, SB-Beh14

### 10. [ambiguous_requirement] Timing of the initialization checks

**raw:** Before STEAM_BOILER_WAITING only three STOPs or a transmission failure lead to emergency stop (SB-Beh2: nothing else happens). From its arrival on, a nonzero steam reading (SB-Beh3) or a level-device failure (SB-Beh6) is checked on every initialization cycle, not only once. PROGRAM_READY keeps repeating until PHYSICAL_UNITS_READY even if the level drifts.

**fix_directive:** None.

**java_trace:**
- `java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 60-102, `step`, requirements: SB-Beh2, SB-Beh3, SB-Beh6, SB-Beh7

### 11. [scope_question] Dynamic plausibility checks use no measurement tolerance

**raw:** SB-FR3, SB-FR4 and SB-FR2 are implemented exactly: a level outside the interval reachable from the previous estimate, a steam change beyond 5*U1 or 5*U2, or a controller observation that disagrees with the pump in the second transmission after an order counts as a failure, with no allowance for sensor noise.

**fix_directive:** Add a tolerance constant if the real devices are noisy.

**java_trace:**
- `java.generated.project/src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 511-525, `detectLevelFailure`, requirements: SB-FR2, SB-FR3, SB-FR4

## next_step

Run the pipeline (compile, coverage, profile check, transformations, FDR4, Dafny, Isabelle, vacuity) and fix the Java from the phase reports.
