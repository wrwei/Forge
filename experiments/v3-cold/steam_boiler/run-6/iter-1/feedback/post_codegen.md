# post_codegen

**status:** uncertain

**summary:** 53 of 53 requirements are implemented and traced. The controller is stateless apart from currentMode, with 29 Cycle-triggered transitions over five modes; emergency stop is terminal. The flagged items are the invented constant values and design choices where the requirements leave room.

## Issues

### 1. [invented_default] Values of the boiler, steam and pump constants

**raw:**

The requirements name C, M1, M2, N1, N2, W, U1, U2 and P but give no values. Chosen: C=1000, M1=100, M2=900, N1=400, N2=600 (litre); W=20 (litre/sec); U1=U2=2 (litre/sec/sec); P=10 (litre/sec). The cycle length (5 s), the pump count (4) and the three STOP repetitions come from the requirements. All values are whole numbers, so rounding a fractional constant up in the model changes nothing.

**fix_directive:** Replace the values in BoilerConstants if the plant's real figures are known; keep M1 < N1 < N2 < M2 < C and 4*P > W.

**java_trace:**

- `src/main/java/steam_boiler/BoilerConstants.java` lines 15-48, `BoilerConstants`; requirement_ids: SB-DM1, SB-DM2, SB-DM3

### 2. [design_choice] One cycle event drives the controller; the messages go to the sensor

**raw:**

All of a cycle's messages count as received at the same moment (SB-V3), so step(InputEvent) is called once per cycle with InputEvent.Cycle. Every transition is triggered by Cycle and guarded by what the sensor analysed. The 15 input-message records are InputEvent records too, but BoilerSensor.receive consumes them. The controller keeps only currentMode. Memory needed in initialization (STEAM_BOILER_WAITING seen, PROGRAM_READY sent, valve open) is read through sensor predicates instead of controller fields, because the profile allows only sensor calls, constants and predicates in guards. In each mode the guards are mutually exclusive and together cover every case.

**fix_directive:** None unless the pipeline needs the input messages as separate triggers.

**java_trace:**

- `src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 48-227, `step`; requirement_ids: SB-V3, SB-DM9
- `src/main/java/steam_boiler/SteamBoilerProgram.java` lines 40-55, `runCycle`; requirement_ids: SB-V3

### 3. [design_choice] Emergency stop is terminal and the program stops

**raw:**

SB-Beh22 says the program stops on entering emergency stop; SB-Beh24 says MODE(m) is sent on every cycle. MODE(EMERGENCY_STOP) is sent on the cycle that enters the mode. The mode has no outgoing transition, and SteamBoilerProgram.runCycle does nothing once it is reached.

**fix_directive:** If MODE(EMERGENCY_STOP) must repeat after the stop, add a Cycle-triggered self-loop in the EMERGENCY_STOP block that only emits Mode.

**java_trace:**

- `src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 224-226, `step`; requirement_ids: SB-Beh22, SB-Beh24
- `src/main/java/steam_boiler/SteamBoilerProgram.java` lines 40-43, `runCycle`; requirement_ids: SB-Beh22

### 4. [design_choice] Water-level regulation policy

**raw:**

Normal, degraded and rescue modes: below N1, OPEN_PUMP for all four pumps; above N2, CLOSE_PUMP for all four; in between, no pump order. Rescue uses the middle of the estimate. A cycle that changes mode sends only MODE(m), and regulation resumes on the next cycle; this keeps the number of transitions down. In initialization only pump 1 fills ("a pump"). When the level reaches [N1, N2], the program closes pump 1, closes the valve if it is open, and announces PROGRAM_READY. Once announced, PROGRAM_READY repeats until PHYSICAL_UNITS_READY whatever the level does.

**fix_directive:** Refine the number of pumps opened if the level overshoots too much for the real constants.

**java_trace:**

- `src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 91-223, `step`; requirement_ids: SB-Beh4, SB-Beh5, SB-Beh7, SB-Beh10, SB-Beh15, SB-Beh19

### 5. [ambiguous_requirement] "If the pump-controller units fail" in rescue mode

**raw:**

SB-Beh21 does not say whether one failed pump controller is enough. Implemented: any pump controller out of order forces emergency stop from rescue, because the estimate relies on reliable pump-controller information (SB-Beh19).

**fix_directive:** Change pumpControlDown in rescueAborted if all controllers must fail.

**java_trace:**

- `src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 86-88, `rescueAborted`; requirement_ids: SB-Beh21

### 6. [design_choice] Level device failure while rescue is already impossible

**raw:**

If the level device fails in normal or degraded mode while the steam device or a pump controller is already out of order, the program goes straight to emergency stop. Going to rescue would meet rescue's own emergency condition at once; this is SB-FR8's most-severe-consequence rule applied to the combination.

**fix_directive:** Remove the last disjunct of operatingAborted to go through rescue first.

**java_trace:**

- `src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 81-83, `operatingAborted`; requirement_ids: SB-FR8, SB-Beh11, SB-Beh17, SB-Beh21

### 7. [design_choice] Which messages count as aberrant (transmission failure)

**raw:**

Transmission failure means any of the following:
- a LEVEL, STEAM, PUMP_STATE(n) or PUMP_CONTROL_STATE(n) missing or duplicated;
- a failure acknowledgement with no announcement awaiting it;
- STEAM_BOILER_WAITING or PHYSICAL_UNITS_READY outside initialization;
- PHYSICAL_UNITS_READY before PROGRAM_READY was sent.
A repair message for a unit already sound is acknowledged again, not treated as aberrant.

**fix_directive:** Extend BoilerSensor.analyse if more messages are to be judged aberrant.

**java_trace:**

- `src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 142-177, `analyse`; requirement_ids: SB-FR5
- `src/main/java/steam_boiler/sensor/UnitStatus.java` lines 46-61, `UnitStatus`; requirement_ids: SB-FR5, SB-FR7
- `src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 73-88, `step`; requirement_ids: SB-FR5, SB-Beh9, SB-Beh14

### 8. [design_choice] Failure detection, level estimate and worst-case prediction

**raw:**

- Steam: outside [0, W], or a change larger than U1*5 or U2*5.
- Level: outside [0, C], or outside the range reachable from the previous bounds. That range uses the extreme steam outflow from the previous rate under W, U1 and U2, and pump inflow bounded by the controller observations at both ends of the cycle. A sound pump ordered off stops at once; one ordered on pours nothing for a cycle. While the valve is open there is no lower dynamic bound.
- Pump (FR1): its state differs from the last order, or from its previous state when there was no order.
- Pump controller (FR2): its flow differs from an order two transmissions old to a sound pump (unless a contrary order followed), or changes with no order in the last two transmissions.
- Worst-case next level: the lower bound assumes no inflow and maximal steam; the upper bound assumes every pump that may pour does and minimal steam. Pumps ordered on in the current cycle are excluded (SB-Beh25).
- No measurement tolerance. Before STEAM_BOILER_WAITING only the transmission and STOP checks run (SB-Beh2).
With the chosen constants the estimate widens by about 44 L per cycle, so rescue lasts a few cycles before the worst case reaches M1 or M2.

**fix_directive:** Add a tolerance, or tighten the outflow bounds with both endpoint steam readings, if the plant is noisy or rescue must last longer.

**java_trace:**

- `src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 220-301, `analyse`; requirement_ids: SB-FR3, SB-FR4, SB-V7, SB-V8, SB-Beh13, SB-Beh19, SB-Beh25
- `src/main/java/steam_boiler/sensor/PumpUnit.java` lines 140-209, `PumpUnit`; requirement_ids: SB-FR1, SB-FR2, SB-Beh25

### 9. [scope_question] Unit announcements and acknowledgements are outside the state machine

**raw:**

PUMP_FAILURE_DETECTION(n), the other failure announcements and the repaired acknowledgements depend on per-unit bookkeeping. Writing them as controller transitions would multiply the transitions. Instead the sensor's bookkeeping produces them (collectUnitStatusMessages), and the program's transmit stage sends them in the same cycle's messages. The extracted RoboChart model therefore contains these output events but no transition emits them.

**fix_directive:** None unless the verified model must include them.

**java_trace:**

- `src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 336-353, `collectUnitStatusMessages`; requirement_ids: SB-FR6, SB-FR7
- `src/main/java/steam_boiler/SteamBoilerProgram.java` lines 40-55, `runCycle`; requirement_ids: SB-FR6, SB-FR7

## next_step

Run the pipeline (compile, coverage, profile check, transformations, FDR4, Dafny, Isabelle, vacuity).
