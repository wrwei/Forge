# post_codegen

**status:** uncertain

**summary:** All 53 requirements are implemented and traced. One controller (SteamBoilerController) with five modes and 30 transitions, all triggered by the per-cycle TransmissionReceived event, with guards that are mutually exclusive and exhaustive within each mode. Perception, failure detection, unit bookkeeping and the level estimate live in BoilerSensor (with PumpMonitor and UnitStatus). The requirements leave several values and policies open; they are listed below.

## Issues

### 1. invented_default — Numeric values of C, M1, M2, N1, N2, W, U1, U2 and P
- **raw:** SB-DM1, SB-DM2 and SB-DM3 name the constants and their order (M1 < N1, N2 < M2) but give no values.
- **fix_directive:** Chosen: C=1000, M1=150, N1=400, N2=600, M2=850 (litre); W=25, P=10 (litre/sec); U1=U2=2 (litre/sec/sec); cycle 5 s; STOP limit 3. All are whole numbers, so the model's constants interface (which rounds fractional constants up) keeps them exact. Replace with the plant's real values if known.
- **java_trace:** `src/main/java/steam_boiler/SteamBoilerConstants.java` 13-46, SteamBoilerConstants — SB-DM1, SB-DM2, SB-DM3

### 2. invented_default — Pump numbering and the 'no pump' value
- **raw:** Messages carry a pump number n but the requirements do not fix the numbering.
- **fix_directive:** Pumps are numbered 1 to 4; a number outside 1..4 in an incoming message is aberrant (transmission failure). pumpToOpen()/pumpToClose() return 0 when no pump qualifies; the controller only uses them under openablePumpCount() > 0 / closablePumpCount() > 0. orderedPump starts at 0 (no order yet).
- **java_trace:** `src/main/java/steam_boiler/sensor/BoilerSensor.java` 441-452, pumpToOpen — SB-Beh5, SB-Beh10; `src/main/java/steam_boiler/controller/SteamBoilerController.java` 24, orderedPump — SB-Beh5, SB-Beh10

### 3. invented_default — Evacuation-valve flow rate
- **raw:** SB-DM7 says the valve drains water in initialization but gives no rate, yet SB-FR3 needs the level dynamics to judge the level device.
- **fix_directive:** While the valve is open the lower bound of the predicted level is dropped to 0 (any drop is plausible); the upper bound is kept.
- **java_trace:** `src/main/java/steam_boiler/sensor/BoilerSensor.java` 277-306, analyse — SB-FR3, SB-DM7

### 4. design_choice — One step per cycle, triggered by TransmissionReceived
- **raw:** SB-V3: all messages of a cycle are received simultaneously, but step(InputEvent) takes one event.
- **fix_directive:** SteamBoilerProgram.cycle delivers every incoming message to BoilerSensor.receive, runs BoilerSensor.analyse, then calls step(new TransmissionReceived()). Message-driven behaviour (STEAM_BOILER_WAITING, PHYSICAL_UNITS_READY, STOP, repairs) reaches the controller through sensor predicates. Every transition consumes the cycle event, so no guard-only cycle can diverge.
- **java_trace:** `src/main/java/steam_boiler/SteamBoilerProgram.java` 25-43, cycle — SB-V3; `src/main/java/steam_boiler/controller/SteamBoilerController.java` 40-242, step — SB-V3, SB-Beh2, SB-Beh8, SB-Beh23

### 5. design_choice — Failure announcements and repair acknowledgements are sent outside the state machine
- **raw:** SB-FR6/SB-FR7 need one message per affected unit per cycle; a transition action is straight-line, so it cannot emit a data-dependent number of messages.
- **fix_directive:** UnitStatus (per unit) keeps out-of-order, acknowledged and repair-acknowledgement-due; BoilerSensor.protocolMessages() lists the messages due and SteamBoilerProgram.cycle appends them after step (not after emergency stop). The extracted model therefore declares these output events but the controller never emits them.
- **java_trace:** `src/main/java/steam_boiler/sensor/BoilerSensor.java` 320-339, protocolMessages — SB-FR6, SB-FR7; `src/main/java/steam_boiler/sensor/UnitStatus.java` 8-50, UnitStatus — SB-V9, SB-FR6, SB-FR7

### 6. design_choice — Regulation policy: at most one pump switched per cycle
- **raw:** SB-Beh10/SB-Beh15/SB-Beh19 require switching pumps when the level leaves [N1, N2] but do not fix the policy.
- **fix_directive:** Normal/degraded: below N1 open one sound idle pump, above N2 close one running pump, else hold. Rescue: the same on the midpoint of the level estimate. A cycle that changes mode sends only MODE (no pump command); regulation resumes the next cycle. Safety relies on the worst-case limit check, not on the policy.
- **java_trace:** `src/main/java/steam_boiler/controller/SteamBoilerController.java` 84-103, step — SB-Beh10, SB-Beh15, SB-Beh19

### 7. design_choice — Level estimate and worst-case prediction
- **raw:** SB-V8 and SB-Beh19 leave the form of the estimate open; SB-Beh13/18/21 and SB-Beh25 require worst-case judgements.
- **fix_directive:** The estimate is an interval [low, high], reset to [q, q] while the level device is sound and otherwise advanced each cycle by pump inflow bounds and steam-outflow bounds from W, U1, U2. A pump ordered on pours nothing in the following cycle; an out-of-order pump counts as [0, P]. The risk check compares the reachable range of the coming cycle with M1/M2 (inclusive).
- **java_trace:** `src/main/java/steam_boiler/sensor/BoilerSensor.java` 236-317, analyse — SB-V8, SB-Beh13, SB-Beh19, SB-Beh25; `src/main/java/steam_boiler/sensor/PumpMonitor.java` 145-200, PumpMonitor — SB-V7, SB-Beh25

### 8. design_choice — Initialization sequence details
- **raw:** SB-Beh4/5/7 say to drain above N2, fill below N1 and announce PROGRAM_READY within [N1, N2]; they do not say when to close the valve or stop the filling pump.
- **fix_directive:** The valve is closed as soon as the level is no longer above N2; the filling pump is closed as soon as the level is no longer below N1; PROGRAM_READY is sent only with valve closed and filling pump off, and leaving initialization requires the same. The valve and the filling pump are never open together.
- **java_trace:** `src/main/java/steam_boiler/controller/SteamBoilerController.java` 66-82, step — SB-Beh4, SB-Beh5, SB-Beh7, SB-Beh8

### 9. design_choice — Level failure while rescue would be untrustworthy goes straight to emergency stop
- **raw:** SB-FR8 lets rescue-mode emergency conditions dominate a change to rescue mode.
- **fix_directive:** In normal and degraded mode, a level-device failure with the steam device or any pump controller out of order leads to emergency stop rather than rescue. In rescue, any steam-device or pump-controller failure (SB-Beh21, 'the pump-controller units fail') leads to emergency stop.
- **java_trace:** `src/main/java/steam_boiler/controller/SteamBoilerController.java` 62-99, step — SB-FR8, SB-Beh21, SB-Beh11, SB-Beh17

### 10. design_choice — Two enums for the mode
- **raw:** SB-V2 (the program's mode) and SB-DM10 (the value MODE(m) carries) describe the same five alternatives.
- **fix_directive:** OperatingMode gives the state machine's states; ModeValue (MODE_*) is the MODE message payload. They are separate so the model's states and enumeration literals do not share names.
- **java_trace:** `src/main/java/steam_boiler/event/ModeValue.java` 4-10, ModeValue — SB-DM10, SB-V2

### 11. ambiguous_requirement — What counts as an aberrant message
- **raw:** SB-FR5 judges transmission failure on 'a message whose presence is aberrant' without listing such messages.
- **fix_directive:** Aberrant: a data message missing or duplicated (each pump state, each pump-controller state, LEVEL, STEAM exactly once); STEAM_BOILER_WAITING outside initialization; PHYSICAL_UNITS_READY unless PROGRAM_READY went out in the previous cycle; a REPAIRED message for a unit not out of order; a FAILURE_ACKNOWLEDGEMENT with no pending detection; a pump number outside 1..4. Transmission time is neglected (SB-V3), so a repeated REPAIRED after the acknowledgement is aberrant.
- **java_trace:** `src/main/java/steam_boiler/sensor/BoilerSensor.java` 452-490, analyse — SB-FR5, SB-Beh9, SB-Beh14

### 12. ambiguous_requirement — Initialization before STEAM_BOILER_WAITING
- **raw:** SB-Beh2 says nothing else happens until STEAM_BOILER_WAITING; SB-FR5 requires the data messages in every transmission; SB-Beh9/SB-Beh23 apply during initialization.
- **fix_directive:** Before STEAM_BOILER_WAITING the program sends only MODE, but still stops on a transmission failure or a third STOP; unit failures are judged only from STEAM_BOILER_WAITING on. The steam-must-be-zero check (SB-Beh3) and the level-device check (SB-Beh6) apply on every initialization cycle after it.
- **java_trace:** `src/main/java/steam_boiler/controller/SteamBoilerController.java` 67-69, step — SB-Beh2, SB-Beh3, SB-Beh6, SB-Beh9

### 13. ambiguous_requirement — Pump-controller judgement window
- **raw:** SB-FR2 judges the controller at the second transmission after a start or stop order, although stopping is immediate (SB-DM3).
- **fix_directive:** The controller is not judged at the first transmission after an order to its pump; from the second on, it must report flow exactly when its pump (known sound) is on. Spontaneous changes outside that window are failures.
- **java_trace:** `src/main/java/steam_boiler/sensor/PumpMonitor.java` 115-142, PumpMonitor — SB-FR1, SB-FR2

### 14. scope_question — MODE broadcast after emergency stop
- **raw:** SB-Beh24 sends MODE(m) on every cycle in whatever mode; SB-Beh22 says the program stops on entering emergency stop.
- **fix_directive:** MODE(emergency stop) is sent once, on the transition into EMERGENCY_STOP; afterwards the mode is terminal (no transitions) and SteamBoilerProgram.cycle answers nothing.
- **java_trace:** `src/main/java/steam_boiler/controller/SteamBoilerController.java` 239-241, step — SB-Beh22, SB-Beh24; `src/main/java/steam_boiler/SteamBoilerProgram.java` 26-28, cycle — SB-Beh22

## next_step

Run the pipeline (compile, coverage, profile check, transformations, FDR4, Dafny, Isabelle, vacuity) and correct the Java against its reports.
