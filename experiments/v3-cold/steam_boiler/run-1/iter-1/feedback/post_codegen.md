# post_codegen — steam_boiler

**status:** `uncertain`

## Summary

Steam-boiler Java written in five layers (17 files under steam_boiler/). One controller, SteamBoilerController, with the five operating modes and 42 transitions, every one triggered by the per-cycle InputEvent.Transmission; each mode's branches are mutually exclusive and together cover every case. Failure detection, the failure/repair message protocol and the level estimate live in sensor services (TransmissionSensor, PlantSensor). Compiles with javac and Gradle; result_codegen.json maps all 53 requirements. Nothing is unimplemented; 16 decisions the requirements did not settle are listed below.

## Issues (16)

### 1. [invented_default] Values of the boiler, steam and pump constants

**raw:** SB-DM1, SB-DM2 and SB-DM3 name C, M1, M2, N1, N2, W, U1, U2 and P but give no values. Chosen: C = 1000 l, M1 = 100 l, N1 = 400 l, N2 = 600 l, M2 = 900 l, W = 10 l/s, U1 = U2 = 1 l/s/s, P = 5 l/s per pump, cycle = 5 s, STOP limit = 3. Consistent with SB-DM1: five seconds of maximal steam from M1 removes at most 50 l; five seconds of four pumps from M2 adds 100 l and reaches C. All values are integral, so rounding fractional constants up in the model's constants interface changes nothing.

**fix_directive:** If the plant's real figures are known, replace the values in SteamBoilerConstants, keeping M1 < N1 < N2 < M2 < C.

**java_trace:**

- `src/main/java/steam_boiler/controller/SteamBoilerConstants.java` lines 13–36, `SteamBoilerConstants` — SB-DM1, SB-DM2, SB-DM3

### 2. [invented_default] Pump numbering 0 to 3

**raw:** The messages carry a pump number n whose range is not stated. Pumps are numbered 0 to 3. An incoming message naming any other number makes the transmission malformed, which is treated as a transmission failure.

**fix_directive:** If the units number pumps 1 to 4, change validPump in TransmissionSensor and BoilerActuator and the literals 0..3 in SteamBoilerController.step.

**java_trace:**

- `src/main/java/steam_boiler/sensor/TransmissionSensor.java` lines 36–110, `receive` — SB-DM8, SB-DM9, SB-FR5

### 3. [design_choice] One controller event per cycle; message contents read through sensor services

**raw:** SB-V3 makes all of a cycle's messages simultaneous. step(InputEvent) is called once per cycle, with InputEvent.Transmission, after TransmissionSensor has taken in the whole transmission. The 15 incoming messages are records of IncomingMessage, consumed by the sensor, not controller events. The alternative, one step per message, would impose an order on simultaneous messages and need per-cycle buffering state in the controller.

**fix_directive:** None needed unless the verifiers require incoming messages to be model events.

**java_trace:**

- `src/main/java/steam_boiler/event/InputEvent.java` lines 1–14, `InputEvent` — SB-V3
- `src/main/java/steam_boiler/SteamBoilerProgram.java` lines 24–40, `cycle` — SB-V3, SB-DM9

### 4. [scope_question] Per-unit failure detection, acknowledgement protocol and level estimation are outside the extracted state machine

**raw:** Bookkeeping for ten units (SB-FR1 to SB-FR4, SB-FR6, SB-FR7, SB-V7 to SB-V9) needs conditional per-unit updates and outputs. The two-level if-else profile cannot express these without nested ifs or a combinatorial number of transitions. They are implemented in PlantSensor, PumpTracker, UnitStatus and LevelEstimator. The controller sees only their results: levelDeviceDefective, steamDeviceDefective, pumpDefective, pumpControllerDefective, levelLow, levelHigh, predictedLevelMin and predictedLevelMax. The verifiers therefore check the mode logic over these functions, not the detection rules themselves, and the *_FAILURE_DETECTION and *_REPAIRED_ACKNOWLEDGEMENT messages are sent by PlantSensor.announce, not by the controller's model.

**fix_directive:** Accept as the verification scope, or split the protocol into further controllers if it must be model-checked.

**java_trace:**

- `src/main/java/steam_boiler/sensor/PlantSensor.java` lines 30–84, `analyse` — SB-FR1, SB-FR2, SB-FR3, SB-FR4, SB-FR7, SB-V7, SB-V8, SB-V9
- `src/main/java/steam_boiler/sensor/PlantSensor.java` lines 87–117, `announce` — SB-FR6, SB-FR7

### 5. [ambiguous_requirement] Emergency stop is terminal, yet MODE(m) is sent on every cycle

**raw:** SB-Beh22 says the program stops on entering emergency stop; SB-Beh24 says MODE(m) is sent on every cycle in whatever mode. Resolved: EMERGENCY_STOP has no transition to any other mode. On each later cycle it only re-sends MODE(EMERGENCY_STOP), a self-loop on the cycle event. After entry no pump, valve, failure or repair message is sent: SteamBoilerProgram skips analysis and announcements.

**fix_directive:** If the program must fall silent, remove the EMERGENCY_STOP branch body (the mode then has no transitions).

**java_trace:**

- `src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 292–298, `step` — SB-Beh22, SB-Beh24
- `src/main/java/steam_boiler/SteamBoilerProgram.java` lines 24–40, `cycle` — SB-Beh22

### 6. [ambiguous_requirement] Behaviour before STEAM_BOILER_WAITING

**raw:** SB-Beh2 says nothing else happens until STEAM_BOILER_WAITING arrives; SB-Beh9 says a transmission failure in initialization forces emergency stop; SB-Beh23 applies in every mode. Resolved: before STEAM_BOILER_WAITING the program only sends MODE(INITIALIZATION), and only the STOP count can act. The transmission, steam and level checks start with the transmission that carries STEAM_BOILER_WAITING, and no unit is judged before it.

**fix_directive:** Move the transmission-failure branch above the waiting branch if a failure must be caught while waiting.

**java_trace:**

- `src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 85–104, `step` — SB-Beh2, SB-Beh9, SB-Beh23

### 7. [design_choice] Initialization fills with all four pumps and closes pumps and valve at PROGRAM_READY

**raw:** SB-Beh5 says the program activates 'a pump'. It sends OPEN_PUMP to all four, so that a single failed pump cannot stall filling; the failure is recorded and leads to degraded mode at PHYSICAL_UNITS_READY. In the cycle the level is first within N1..N2, the program closes all pumps and the valve, if it is open, and sends the first PROGRAM_READY. The level then stays put while the program repeats PROGRAM_READY.

**fix_directive:** To activate a single pump, keep one OpenPump statement in the two fill branches.

**java_trace:**

- `src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 112–160, `step` — SB-Beh4, SB-Beh5, SB-Beh7

### 8. [design_choice] Regulation policy: all pumps on below N1, all off above N2, held in between

**raw:** SB-Beh10, SB-Beh15 and SB-Beh19 leave the policy open. The controller opens all four pumps when the lower level bound is below N1 and closes them when the upper bound is above N2; otherwise it sends no pump order. Orders go to every pump, including pumps out of order. In rescue mode the bounds are the estimate, and if it straddles both N1 and N2 the pumps are held. With the chosen constants the overshoot is at most about 100 l, well inside M1..M2.

**fix_directive:** None needed; a finer policy (number of pumps) would need one transition per pump count.

**java_trace:**

- `src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 59–67, `step` — SB-Beh10, SB-Beh15, SB-Beh19

### 9. [design_choice] Worst-case prediction used for the M1/M2 risk

**raw:** SB-Beh13, SB-Beh18, SB-Beh21 and SB-Beh25: risk means the level could reach M1 or M2 by the next cycle. The lowest level is the lower bound minus the maximal steam out (rate rising at U1, capped at W), with no water from the pumps. The highest is the upper bound plus P x 5 s for every pump that may pour (reported on, flowing, or out of order), minus the minimal steam out (rate falling at U2). A pump ordered on in this cycle pours nothing before the next one (the start-up latency).

**fix_directive:** None needed unless a less conservative prediction is wanted.

**java_trace:**

- `src/main/java/steam_boiler/sensor/LevelEstimator.java` lines 91–113, `LevelEstimator` — SB-V4, SB-Beh13, SB-Beh18, SB-Beh21, SB-Beh25

### 10. [design_choice] Form of the level estimate

**raw:** SB-V8 and SB-Beh19 leave the estimate's form open. Each cycle the bounds advance: low' = low + 5 x P x (pumps known to pour and not closed) - maximal steam out; high' = high + 5 x P x (pumps that may pour and are not closed) - minimal steam out; both are clamped to 0..C. While the level device is sound, both bounds equal its reading. The same range serves as the dynamic plausibility test for a level reading (SB-FR3).

**fix_directive:** None needed.

**java_trace:**

- `src/main/java/steam_boiler/sensor/LevelEstimator.java` lines 28–43, `advance` — SB-V8, SB-Beh19, SB-FR3

### 11. [invented_default] Evacuation-valve drain rate

**raw:** The valve's drain rate is not given. While the valve is open the lower dynamic bound on the level is 0, so only the upper bound is checked.

**fix_directive:** Use the valve's real capacity in LevelEstimator.advance if it is known.

**java_trace:**

- `src/main/java/steam_boiler/sensor/LevelEstimator.java` lines 36–39, `advance` — SB-DM7, SB-FR3

### 12. [design_choice] Level failure when rescue mode is already impossible goes straight to emergency stop

**raw:** SB-Beh11 and SB-Beh17 send a level-device failure to rescue mode, but SB-Beh21 ends rescue in emergency stop when the steam device or a pump controller has failed. If the level device fails while one of those is out of order (or fails in the same cycle), the controller goes directly to emergency stop instead of entering rescue for one cycle. Otherwise precedence follows SB-FR8: STOP, transmission failure and level risk first, then rescue, then degraded.

**fix_directive:** None needed.

**java_trace:**

- `src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 173–183, `step` — SB-Beh11, SB-Beh12, SB-FR8
- `src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 215–222, `step` — SB-Beh17, SB-Beh21, SB-FR8

### 13. [ambiguous_requirement] 'If the pump-controller units fail' in rescue mode

**raw:** SB-Beh21 is read as: any one pump controller out of order makes the pump-controller information unreliable, so rescue mode ends in emergency stop.

**fix_directive:** If all four controllers must fail, change estimateUnreliable accordingly (PlantSensor would need an all-controllers query).

**java_trace:**

- `src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 57–57, `step` — SB-Beh19, SB-Beh21

### 14. [design_choice] What counts as a transmission failure

**raw:** SB-FR5 gives no list of aberrant messages. Treated as transmission failure: a missing or duplicated LEVEL, STEAM, PUMP_STATE(n) or PUMP_CONTROL_STATE(n); a pump number outside 0..3; STEAM_BOILER_WAITING or PHYSICAL_UNITS_READY outside initialization; PHYSICAL_UNITS_READY before PROGRAM_READY has been sent. A repeated repair message is acknowledged again; an acknowledgement with nothing to acknowledge is ignored.

**fix_directive:** Extend transmissionIntact if other messages should count as aberrant.

**java_trace:**

- `src/main/java/steam_boiler/sensor/TransmissionSensor.java` lines 36–110, `receive` — SB-FR5
- `src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 39–47, `step` — SB-FR5, SB-Beh9, SB-Beh14, SB-Beh18, SB-Beh21

### 15. [design_choice] Details of pump and pump-controller failure judgement

**raw:** SB-FR1: on every transmission the reported pump state must equal the last ordered state; this covers both a disobeyed order and a spontaneous change. SB-FR2: a controller is judged only while its pump is sound, from the second transmission after an order that changed the pump's state, and must then report flow exactly when the pump is on. A repeated OPEN_PUMP to a running pump is not a state change. A repaired pump resumes from the state it reports.

**fix_directive:** None needed.

**java_trace:**

- `src/main/java/steam_boiler/sensor/PumpTracker.java` lines 24–70, `PumpTracker` — SB-FR1, SB-FR2, SB-Beh25

### 16. [ambiguous_requirement] Steam-zero check in initialization is repeated every cycle

**raw:** SB-Beh3 ('checks that the steam rate is really zero') is applied on every initialization cycle from STEAM_BOILER_WAITING onwards, comparing the reading exactly with 0.0. The STOP count of SB-V10 is capped at 3, which already forces emergency stop, so the cap is not observable.

**fix_directive:** None needed.

**java_trace:**

- `src/main/java/steam_boiler/controller/SteamBoilerController.java` lines 48–48, `step` — SB-Beh3
- `src/main/java/steam_boiler/sensor/TransmissionSensor.java` lines 101–107, `receive` — SB-V10, SB-Beh23

## Next step

Run the pipeline: compile, coverage, profile check, transformations, FDR4, Dafny, Isabelle, vacuity.
