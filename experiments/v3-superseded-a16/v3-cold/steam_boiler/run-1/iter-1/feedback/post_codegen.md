# post_codegen

**phase:** codegen  
**status:** uncertain

## Summary

All 53 requirements are implemented and traced; the controller compiles and a scratch plant simulation exercised initialization, regulation, failure/repair handling, STOP and transmission failure. The requirements fix no constant values and leave several design points open; the values and choices made are listed below for review. Nothing is unimplemented.

## Issues

### 1. [invented_default] Values of C, M1, M2, N1, N2, W, U1, U2 and P

**raw:** SB-DM1, SB-DM2 and SB-DM3 name the boiler, steam and pump constants but give no values. Chosen: C=1000, M1=100, M2=900, N1=400, N2=600 (litre); W=10 (l/s); U1=U2=1 (l/s/s); P=5 (l/s); cycle 5 s; four pumps; STOP limit 3.

**fix_directive:** Replace with the plant's actual values, keeping M1 < N1 < N2 < M2 < C. Every constant used in a guard is integral, so the model's rounding of fractional constants does not change any guard.

**java_trace:**

- `src/main/java/steam_boiler/SteamBoilerConstants.java` lines 12-34, `SteamBoilerConstants` — SB-DM1, SB-DM2, SB-DM3

### 2. [invented_default] Readings and estimate before any trusted value; valve drainage rate

**raw:** q and v read 0.0 before the first transmission. If the level device fails before any trusted reading, the estimate is [0, C]. A reading is compared with a predicted bound with a 0.001 litre rounding allowance. The evacuation valve's drainage rate is not given, so while the valve is open the lowest reachable level is taken as 0.

**fix_directive:** Confirm these defaults or supply the missing figures (especially the valve's drainage rate, which would tighten the level-dynamics check during initialization).

**java_trace:**

- `src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 19-19, `ROUNDING_ALLOWANCE` — SB-FR3, SB-FR4
- `src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 234-254, `checkLevel` — SB-FR3, SB-DM7
- `src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 262-312, `estimateLevel` — SB-V5, SB-V6, SB-V8

### 3. [design_choice] One controller step per cycle, triggered by InputEvent.Transmission

**raw:** SB-V3 makes all messages of a cycle simultaneous. The controller is stepped once per cycle with a Transmission event; message contents reach it as sensor quantities. The per-message InputEvent records are delivered to BoilerSensor.receive, not to the controller, so they appear in the model as events but trigger no transition.

**fix_directive:** None unless per-message triggers are wanted; that would need either several steps per cycle or controller state for buffered messages.

**java_trace:**

- `src/main/java/steam_boiler/event/InputEvent.java` lines 14-14, `Transmission` — SB-V3, SB-DM9
- `src/main/java/steam_boiler/SteamBoilerProgram.java` lines 26-45, `cycle` — SB-V3

### 4. [design_choice] Initialization split into four controller states; separate MODE payload enum

**raw:** The profile allows guards to use only sensor calls, constants and predicates, so the initialization protocol (waiting for STEAM_BOILER_WAITING, filling, draining with the valve open, PROGRAM_READY sent) is held in four controller states INITIALIZATION, INITIALIZATION_FILLING, INITIALIZATION_DRAINING and INITIALIZATION_READY. All four broadcast MODE(INITIALIZATION_MODE). The MODE payload is a separate five-value enum OperatingMode whose literals carry a _MODE suffix so that they do not share names with the states.

**fix_directive:** None; SB-V2 holds at the operating-mode level (each state reports exactly one of the five modes).

**java_trace:**

- `src/main/java/steam_boiler/mode/BoilerMode.java` lines 12-26, `BoilerMode` — SB-V2, SB-Beh1, SB-Beh2, SB-Beh4, SB-Beh5, SB-Beh7
- `src/main/java/steam_boiler/mode/OperatingMode.java` lines 4-10, `OperatingMode` — SB-DM10

### 5. [design_choice] Pumps switched as a group, bang-bang between N1 and N2

**raw:** SB-Beh5 says initialization 'activates a pump'; all four pumps are opened (robust to a single failed pump, and it exercises every pump and controller before PHYSICAL_UNITS_READY decides normal vs degraded). In normal, degraded and rescue modes all four pumps are opened when the level (rescue: the estimate) is below N1 and closed when above N2; no orders between. Open orders are repeated while the level stays below N1.

**fix_directive:** If a single pump must be used at start-up, or finer regulation is wanted, change the OpenPump/ClosePump actions in step.

**java_trace:**

- `src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 90-197, `step` — SB-Beh5
- `src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 226-342, `step` — SB-Beh10, SB-Beh15, SB-Beh19

### 6. [design_choice] Failure bookkeeping, STOP counting, announcements and repair acknowledgements outside the state machine

**raw:** Unit status (SB-V9), the STOP count (SB-V10), failure announcements repeated until acknowledged (SB-FR6) and repair acknowledgements (SB-FR7) are kept in BoilerSensor, PumpMonitor and UnitStatus; SteamBoilerProgram adds the announcement/acknowledgement messages to each cycle's transmission. The controller sees only summaries (successiveStops, levelDeviceFailed, anyPumpFailed, ...), so these messages are not part of the extracted model.

**fix_directive:** None unless the per-unit messages must appear in the model; that would need per-unit transitions.

**java_trace:**

- `src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 165-212, `analyse` — SB-V9, SB-V10, SB-FR5
- `src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 352-370, `statusMessages` — SB-FR6, SB-FR7

### 7. [design_choice] Worst-case level risk judged one cycle ahead

**raw:** Risk of M1/M2 (SB-Beh13, SB-Beh18, SB-Beh21) is judged on the level reachable by the next transmission: steam at the maximal gradient (capped by W) from the current rate, no guaranteed pump water, nothing from pumps that are off now even if opened this cycle (SB-Beh25), and full capacity from failed pumps.

**fix_directive:** Confirm a one-cycle horizon is the intended reaction margin.

**java_trace:**

- `src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 262-312, `estimateLevel` — SB-V4, SB-Beh13, SB-Beh18, SB-Beh21, SB-Beh25

### 8. [design_choice] Rescue estimate and regulation

**raw:** The estimate advances lower/upper bounds each cycle using steam outflow bounded from the previous rate by U1, U2 and W, and pump water bounded by the controllers' flow reports at both ends of the cycle (one litre pumped = one litre of content). Regulation in rescue acts on the midpoint of the bounds. With the invented constants, a scratch simulation stayed in rescue for about ten cycles before the widening bounds forced emergency stop.

**fix_directive:** Review whether a tighter steam bound (using both the previous and current rate) or a different regulation target is wanted to prolong rescue mode.

**java_trace:**

- `src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 262-312, `estimateLevel` — SB-V8, SB-Beh19
- `src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 304-342, `step` — SB-Beh19

### 9. [design_choice] Which messages count as aberrant (transmission failure)

**raw:** Transmission failure: a LEVEL, STEAM, PUMP_STATE(n) or PUMP_CONTROL_STATE(n) missing or duplicated; a pump number outside 1..4; PHYSICAL_UNITS_READY before PROGRAM_READY; STEAM_BOILER_WAITING or PHYSICAL_UNITS_READY after initialization; a failure acknowledgement with no announcement pending; a repair message for a unit not out of order.

**fix_directive:** Relax any of these if the transmission system may legitimately repeat such messages.

**java_trace:**

- `src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 165-212, `analyse` — SB-FR5
- `src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 58-86, `step` — SB-FR5, SB-Beh9, SB-Beh14, SB-Beh18, SB-Beh21

### 10. [ambiguous_requirement] Emergency stop: 'the program stops' (SB-Beh22) vs MODE on every cycle in whatever mode (SB-Beh24)

**raw:** Resolved by making EMERGENCY_STOP absorbing: on entry and on every later cycle the program only sends MODE(EMERGENCY_STOP_MODE); it no longer analyses messages, detects failures or sends orders.

**fix_directive:** If the program must fall silent after entering emergency stop, remove the EMERGENCY_STOP self-transition (the mode then becomes terminal) and stop calling step.

**java_trace:**

- `src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 343-349, `step` — SB-Beh22, SB-Beh24
- `src/main/java/steam_boiler/SteamBoilerProgram.java` lines 26-45, `cycle` — SB-Beh22

### 11. [ambiguous_requirement] SB-Beh21 'if the pump-controller units fail'

**raw:** Interpreted as: the failure of any one pump controller forces emergency stop from rescue mode (the estimate relies on every controller's flow report).

**fix_directive:** If only the failure of all controllers is meant, change pumpControllerFaulty in the rescue guard.

**java_trace:**

- `src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 83-85, `step` — SB-Beh21

### 12. [ambiguous_requirement] Timing of pump and pump-controller failure judgements

**raw:** SB-FR1: the reference is the latest OPEN/CLOSE order sent in the previous cycle, otherwise the previous reported state. SB-FR2: a controller is judged only when the pump's reported state is unchanged since the previous transmission (i.e. from the second transmission after an order), flow being expected exactly when the pump is on; it is not judged while its pump is failed or was repaired this cycle.

**fix_directive:** Confirm this reading of 'the second transmission after a start or stop order' for stop orders, whose effect is instantaneous.

**java_trace:**

- `src/main/java/steam_boiler/sensor/PumpMonitor.java` lines 100-113, `detectFailures` — SB-FR1, SB-FR2, SB-Beh25

### 13. [ambiguous_requirement] What happens before STEAM_BOILER_WAITING, and how long the steam-zero check applies

**raw:** Before STEAM_BOILER_WAITING the program only sends MODE, counts STOP and checks the transmission (SB-Beh2 'nothing else happens'); unit failure detection starts with that message. The steam-is-zero check (SB-Beh3) and the level-device check (SB-Beh6) are applied on every initialization cycle from then on, not only once.

**fix_directive:** Confirm, or restrict the steam check to the cycle in which STEAM_BOILER_WAITING arrives.

**java_trace:**

- `src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 90-225, `step` — SB-Beh2, SB-Beh3, SB-Beh6

### 14. [scope_question] No level regulation or timeout while waiting for PHYSICAL_UNITS_READY

**raw:** In INITIALIZATION_READY the program repeats PROGRAM_READY and does not regulate the level; no phase of initialization has a timeout (the requirements give none and say PHYSICAL_UNITS_READY must necessarily be sent).

**fix_directive:** Add regulation or a timeout to INITIALIZATION_READY only if the requirements are extended.

**java_trace:**

- `src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 198-225, `step` — SB-Beh7, SB-Beh8

## Next step

Run the pipeline (compile, coverage, profile check, transformations, FDR4, Dafny, Isabelle, vacuity); review the invented constants and the design choices above.
