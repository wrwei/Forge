# post_codegen

**status:** uncertain

**summary:** Two controllers (gas analysis, movement) implement all 81 requirements; 13 items record values or choices the requirements did not settle.

## Issues

### 1. [invented_default] Values of the six configuration constants

- **raw:** The requirements fix the types of thr, lv, evadeTime, stuckPeriod, stuckDist and outPeriod but not their values. Chosen: THR = 1.0, LV = 1.0, EVADE_TIME = 1, STUCK_PERIOD = 3, STUCK_DIST = 1.0, OUT_PERIOD = 1. The reals are whole-valued so the model's constants interface (which rounds fractional constants up) matches the Java; STUCK_PERIOD exceeds EVADE_TIME so that both outcomes of AvoidingAgain are reachable; values are small to keep the timed model small.
- **fix_directive:** Replace with the deployment's configured values if known; keep reals whole-valued or record the rounding.
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/ChemicalDetectorConstants.java` lines 13-28, `ChemicalDetectorConstants`, requirements CD-Const1, CD-Const2, CD-Const3, CD-Const4, CD-Const5, CD-Const6

### 2. [invented_default] Initial values of state variables

- **raw:** Only gs has a stated initial value (empty). Chosen: sts = noGas, ins = 0.0, anl = Front; a = Front, l = front, d0 = d1 = 0.0, evasion clock start = 0. None of these is read before it is assigned on every reachable path except a (used only after a turn sets it).
- **fix_directive:** None needed unless the requirements fix initial values.
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/controller/GasAnalysisController.java` lines 28-34, `gs, sts, ins, anl`, requirements CD-GA-Var1, CD-GA-Var2, CD-GA-Var3, CD-GA-Var4
  - `java.generated.project/src/main/java/chemical_detector/controller/MovementController.java` lines 26-34, `a, d0, d1, l, evasionStart`, requirements CD-MV-Var1, CD-MV-Var2, CD-MV-Var3, CD-MV-Var4, CD-MV-Clock1

### 3. [invented_default] What it means for a reading to indicate the target chemical

- **raw:** CD-Fn1 says analysis returns noGas if no reading indicates the target chemical, without defining 'indicates' or naming the target. Chosen: a value indicates the target when its chemical equals the target Chem given to GasAnalysisSensor's constructor and its intensity is positive. An empty reading is noGas.
- **fix_directive:** Confirm the criterion (e.g. whether a zero-intensity target value counts).
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/sensor/GasAnalysisSensor.java` lines 21-40, `GasAnalysisSensor.analysis`, requirements CD-Fn1, CD-DM4

### 4. [invented_default] Sensor position to direction mapping; ties; empty readings

- **raw:** CD-DM7/CD-Fn3 say the 1-based position maps to a sensing direction via angle(x) but give no mapping. Chosen: 1 Left, 2 Right, 3 Back, 4 Front, repeating cyclically. Ties for the highest intensity go to the first sensor. Outside the non-empty precondition, intensity returns 0.0 and location returns Front.
- **fix_directive:** Replace angle(x) with the vehicle's actual sensor layout if known.
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/sensor/GasAnalysisSensor.java` lines 42-96, `intensity, location, angle`, requirements CD-Fn2, CD-Fn3, CD-DM7

### 5. [ambiguous_requirement] Peak intensity and location range over all chemicals in a reading

- **raw:** CD-Fn2 defines intensity as the maximum over every value of the reading, and CD-Fn3 the location of that maximum, regardless of which chemical each value is for; only analysis refers to the target chemical. Implemented literally, so a stronger non-target value could set ins and anl.
- **fix_directive:** If only target-chemical values should count, restrict intensity and location to them.
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/sensor/GasAnalysisSensor.java` lines 42-78, `intensity, location`, requirements CD-Fn2, CD-Fn3, CD-GA-FR4

### 6. [design_choice] Two controllers connected by same-named sent and received events

- **raw:** The gas-analysis and movement subsystems are two controllers. The gas-analysis controller sends OutputEvent.Turn/Stop/Resume through GasAnalysisActuator.apply; the movement controller receives InputEvent.Turn/Stop/Resume; the identical names make them shared events. ChemicalDetectorSystem wires the two in Java (it is not a controller).
- **fix_directive:** None.
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/ChemicalDetectorSystem.java` lines 21-83, `ChemicalDetectorSystem`, requirements CD-ARCH1, CD-ARCH2, CD-Evt4, CD-Evt5, CD-Evt6

### 7. [design_choice] Odometer modelled as a sensor quantity, not an input event

- **raw:** CD-Evt3 is typed 'event' but describes a sampled value. d0 and d1 are sampled inside actions, which the profile can only express as a zero-argument sensor call (OdometerSensor.odometer(), a sensor variable), not as an event trigger.
- **fix_directive:** None.
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/sensor/OdometerSensor.java` lines 1-30, `OdometerSensor.odometer`, requirements CD-Evt3, CD-MV-Var2, CD-MV-Var3

### 8. [design_choice] Waiting's during action randomWalk()

- **raw:** The profile has no during actions, so randomWalk() is performed on every transition into Waiting (including the resume self-loop), as for an entry action. The initial transition has no action (CD-MV-Beh1), so the start-up random walk is the VehicleActuator's power-on behaviour (randomWalking = true) rather than a model action.
- **fix_directive:** None, unless the initial random walk must appear in the model.
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/controller/MovementController.java` lines 70-74, `step (Waiting resume)`, requirements CD-MV-FR1, CD-MV-Beh3, CD-OP2
  - `java.generated.project/src/main/java/chemical_detector/actuator/VehicleActuator.java` lines 19-19, `randomWalking`, requirements CD-MV-FR1, CD-MV-Beh1

### 9. [design_choice] Platform operations and changeDirection as Vehicle actuator methods

- **raw:** move(lv, a), randomWalk(), shortRandomWalk() and changeDirection(l) are commands with parameters and side effects, not computed variables, so they are methods of VehicleActuator called from the controller's actions instead of operation-package classes with compute(). changeDirection's steering rule (left to Right, right to Left, front to Back, at lv) is in VehicleActuator.changeDirection; the model sees a call changeDirection(l).
- **fix_directive:** None, unless the pipeline requires operation-package classes for these.
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/actuator/VehicleActuator.java` lines 22-59, `move, randomWalk, shortRandomWalk, changeDirection`, requirements CD-OP1, CD-OP2, CD-OP3, CD-OP4

### 10. [design_choice] NoGas to Reading guard; Analysis and GasDetected entry actions

- **raw:** CD-GA-Beh3 is unguarded and untriggered. The profile has no form for a single unguarded transition, so it is guarded by sts == noGas, which holds whenever NoGas is reached (its only incoming transition requires it). The entry actions of Analysis (sts = analysis(gs)) and GasDetected (ins = intensity(gs)) are written on their single incoming transitions, as the profile prescribes.
- **fix_directive:** If verification needs the transition unguarded, revisit this guard.
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/controller/GasAnalysisController.java` lines 81-87, `step (NoGas)`, requirements CD-GA-Beh3, CD-GA-FR1

### 11. [design_choice] Final states as terminal modes

- **raw:** The gas-analysis final state is the terminal mode CONCLUDED, entered by CD-GA-Beh6. Found has no outgoing transition, so CD-MV-Beh9 (Found to final) is realised by Found being terminal rather than by a separate unguarded transition to a final state.
- **fix_directive:** None.
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/controller/GasAnalysisController.java` lines 101-103, `step (CONCLUDED)`, requirements CD-GA-Beh6
  - `java.generated.project/src/main/java/chemical_detector/controller/MovementController.java` lines 114-116, `step (FOUND)`, requirements CD-MV-Beh9, CD-MV-FR3, CD-DC1

### 12. [design_choice] Representation of Intensity, Chem and the enumeration literals

- **raw:** Intensity is a real (double); goreq is its ordering. Chem is a record with a natural-number identifier, compared only by equality. Enumeration literals keep the requirements' names (Angle Left/Right/Back/Front, Loc left/right/front, Status noGas/gasD); Angle and Loc literals differ only in case.
- **fix_directive:** If the pipeline needs case-distinct literals, rename the Loc literals.
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/domain/GasSensor.java` lines 1-16, `GasSensor`, requirements CD-DM5, CD-DM6
  - `java.generated.project/src/main/java/chemical_detector/domain/Chem.java` lines 1-12, `Chem`, requirements CD-DM4

### 13. [scope_question] Java wiring: readings during analysis; commands during AvoidingAgain

- **raw:** ChemicalDetectorSystem.cycle gives each controller one step per cycle. A gas reading arriving while the previous one is still being analysed is not processed (the model likewise does not accept gas outside Reading). A command arriving while movement is in AvoidingAgain is delivered after one step that completes the autonomous transition, matching the model where the sender waits.
- **fix_directive:** None; outside the extracted model.
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/ChemicalDetectorSystem.java` lines 39-57, `cycle`, requirements CD-ARCH2

## next_step

Run the pipeline (compile, coverage, profile, transformations, FDR4, Dafny, Isabelle, vacuity) and address any failing phase in the Java.
