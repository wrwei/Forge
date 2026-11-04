# post_codegen

**status:** uncertain

**summary:** Steam-boiler controller generated in five layers; 53/53 requirements traced. Ten points recorded where the requirements left a choice: constant values, the cycle event, per-unit messages outside the model, the regulation strategy, worst-case ranges, rescue/emergency precedence, MODE in emergency stop, aberrant messages, and the rescue pump-controller rule.

## 1. [invented_default] Numeric values of the boiler, steam and pump constants

**raw:** The requirements name C, M1, M2, N1, N2, W, U1, U2 and P but give no values. Chosen: C=1000, M1=100, M2=900, N1=400, N2=600 (litre); W=10 (l/s); U1=U2=1 (l/s/s); P=5 (l/s); cycle 5 s; 4 pumps; STOP limit 3. All are integral, so the model's rounding of fractional constants does not change any guard.

**fix_directive:** Replace the values in BoilerConstants with the plant's real figures if they are known; keep M1 < N1 < N2 < M2 < C.

**java_trace:**
- `java.generated.project/src/main/java/steam_boiler/controller/BoilerConstants.java` lines 13-35, `BoilerConstants`: SB-DM1, SB-DM2, SB-DM3

## 2. [invented_default] Readings before any data and level bounds when nothing is known

**raw:** levelReading(), steamReading() and pumpThroughput() return 0.0 until the first data transmission; with no usable reading and no estimate yet the level is bounded by [0, C].

**fix_directive:** None needed unless a different start value is specified.

**java_trace:**
- `java.generated.project/src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 23-48, `BoilerSensor`: SB-V5, SB-V6, SB-V7, SB-V8

## 3. [design_choice] One controller step per cycle, triggered by a Transmission event

**raw:** SB-V3 makes all messages of a cycle simultaneous, so the controller is stepped once per cycle with InputEvent.Transmission; the DM9 message records are delivered to BoilerSensor.receive, which analyses them before the step. The step's guards read the analysis through sensor queries.

**fix_directive:** Accept, or model individual messages as triggers if per-message transitions are wanted.

**java_trace:**
- `java.generated.project/src/main/java/steam_boiler/SteamBoilerProgram.java` lines 30-45, `cycle`: SB-V3, SB-DM9
- `java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 46-71, `step`: SB-V3

## 4. [design_choice] Per-unit failure announcements and repair acknowledgements outside the state machine

**raw:** PUMP_FAILURE_DETECTION(n) etc. and the *_REPAIRED_ACKNOWLEDGEMENT messages depend on per-unit bookkeeping for ten units. They are produced by UnitRegistry and appended by SteamBoilerProgram after the step, so they are not part of the extracted RoboChart machine; the machine sees only the aggregated failure predicates.

**fix_directive:** Accept; FR6/FR7 are then covered by the Java but not by the model checkers.

**java_trace:**
- `java.generated.project/src/main/java/steam_boiler/sensor/UnitRegistry.java` lines 1-111, `UnitRegistry`: SB-FR6, SB-FR7, SB-V9

## 5. [design_choice] Level regulation is on/off with all four pumps

**raw:** NORMAL, DEGRADED and RESCUE open all four pumps when the known level (lower bound) is below N1, close all four when it (upper bound) is above N2, and hold otherwise. In initialization the fill branch also opens all four pumps although SB-Beh5 says 'a pump'. Repeated OPEN/CLOSE of a pump already in that ordered state is not a new order for failure detection.

**fix_directive:** Use a finer pump-count strategy if required.

**java_trace:**
- `java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 198-404, `step`: SB-Beh5, SB-Beh10, SB-Beh15, SB-Beh19
- `java.generated.project/src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 425-434, `BoilerSensor`: SB-FR1, SB-FR2

## 6. [design_choice] Worst-case risk and plausibility ranges

**raw:** Risk of M1/M2 (and the range a level reading must lie in, SB-FR3) uses the steam gradients U1/U2 bounded by [0, W] and every pump that is on, flowing or out of order; pumps now off pour nothing within the next cycle (SB-Beh25). The rescue estimate uses only water confirmed by a sound pump controller as the lower inflow. While the valve is open no lower plausibility bound is applied (drain rate not given).

**fix_directive:** Accept or tighten.

**java_trace:**
- `java.generated.project/src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 375-470, `BoilerSensor`: SB-Beh13, SB-Beh25, SB-FR3, SB-V8

## 7. [design_choice] Level failure together with steam or pump-controller failure goes straight to emergency stop

**raw:** From NORMAL/DEGRADED a level failure normally leads to RESCUE (SB-Beh11/17), but if the steam device or a pump controller is also out of order the estimate cannot be trusted (SB-Beh19/21), so by SB-FR8 the program goes directly to EMERGENCY_STOP.

**fix_directive:** Accept, or go to RESCUE first and leave it on the next cycle.

**java_trace:**
- `java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 67-69, `step`: SB-FR8, SB-Beh11, SB-Beh17, SB-Beh21

## 8. [ambiguous_requirement] MODE in emergency stop

**raw:** SB-Beh24 asks for MODE(m) on every cycle in whatever mode; SB-Beh22 says emergency stop is terminal and the program stops. MODE(EMERGENCY_STOP) is sent once, on entry; afterwards the program sends nothing and EMERGENCY_STOP has no transitions.

**fix_directive:** Add a self-transition re-broadcasting MODE if the program must keep transmitting.

**java_trace:**
- `java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 405-408, `step`: SB-Beh22, SB-Beh24
- `java.generated.project/src/main/java/steam_boiler/SteamBoilerProgram.java` lines 30-33, `cycle`: SB-Beh22

## 9. [ambiguous_requirement] Which messages are aberrant (transmission failure)

**raw:** Counted as transmission failure: a missing or repeated LEVEL, STEAM, PUMP_STATE(n) or PUMP_CONTROL_STATE(n); any other message repeated within one transmission; a pump number outside 1..4; PHYSICAL_UNITS_READY before PROGRAM_READY or after initialization; STEAM_BOILER_WAITING after initialization. Repair messages for sound units are still acknowledged and stray acknowledgements are ignored. Before STEAM_BOILER_WAITING arrives no message is checked (SB-Beh2).

**fix_directive:** Confirm the list.

**java_trace:**
- `java.generated.project/src/main/java/steam_boiler/sensor/BoilerSensor.java` lines 91-93, `analyse`: SB-FR5, SB-Beh2, SB-Beh9
- `java.generated.project/src/main/java/steam_boiler/sensor/TransmissionBuffer.java` lines 143-158, `TransmissionBuffer`: SB-FR5

## 10. [scope_question] 'The pump-controller units fail' in rescue mode

**raw:** SB-Beh21 is read as: any one pump controller out of order forces emergency stop in RESCUE.

**fix_directive:** Relax to 'all controllers' or to the controllers of running pumps if intended.

**java_trace:**
- `java.generated.project/src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 70-71, `step`: SB-Beh21

**next_step:** Run the pipeline; review the design choices above if a verifier or reviewer disagrees.
