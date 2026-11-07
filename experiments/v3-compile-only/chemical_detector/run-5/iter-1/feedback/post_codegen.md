# post_codegen

**status:** uncertain

**summary:** Two controllers (GasAnalysisController, MovementController) implement all 81 requirements. Most issues record values or choices the requirements leave open; one is a profile limitation (the operations' arguments).

## Issues

### 1. [invented_default] Constant values are not given by the requirements

- **raw:** CD-Const1..6 say the constants are configured at startup but give no values. Chosen: THR=2.0, LV=1.0, EVADE_TIME=1, STUCK_PERIOD=2, STUCK_DIST=1.0, OUT_PERIOD=1. All are integral, so the model's constants interface (which rounds fractions up) represents them exactly; they are small to keep the verifiers' state space small.
- **fix_directive:** Replace with the deployment's values if known; keep real-valued ones integral or check the rounding against the guards that use them (THR in goreq, STUCK_DIST in d1 - d0 > stuckDist).
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/controller/ChemicalDetectorConstants.java` lines 13-28, `ChemicalDetectorConstants`, requirements CD-Const1, CD-Const2, CD-Const3, CD-Const4, CD-Const5, CD-Const6

### 2. [invented_default] Initial values of state variables

- **raw:** Only gs has a stated initial value (empty). Chosen: sts=noGas, ins=0.0, anl=Front, a=Front, l=front, d0=d1=0.0, clock start 0. None of them is read before it is written on any path.
- **fix_directive:** None needed unless the requirements fix other initial values.
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/controller/GasAnalysisController.java` lines 22-31, `gs, sts, ins, anl`, requirements CD-GA-Var1, CD-GA-Var2, CD-GA-Var3, CD-GA-Var4
  - `java.generated.project/src/main/java/chemical_detector/controller/MovementController.java` lines 19-30, `a, l, d0, d1, evasionStart`, requirements CD-MV-Var1, CD-MV-Var2, CD-MV-Var3, CD-MV-Var4, CD-MV-Clock1

### 3. [ambiguous_requirement] What it means for a reading to indicate the target chemical

- **raw:** CD-Fn1 says analysis returns gasD if some reading indicates the target chemical, but does not name the target or define 'indicates'. Chosen: the target Chem is given to GasAnalysisSensor's constructor, and a value indicates it when its chemical equals the target and its intensity is strictly positive. intensity (CD-Fn2) takes the maximum over all values, whatever their chemical, as stated.
- **fix_directive:** Confirm the definition, or supply the target chemical as a configuration constant.
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/sensor/GasAnalysisSensor.java` lines 22-39, `analysis`, requirements CD-Fn1, CD-DM4

### 4. [invented_default] Mapping from sensor position to Angle; results for an empty reading; ties

- **raw:** CD-DM7 says position k (1-based) maps to a sensing direction but gives no mapping. Chosen: 1 Left, 2 Right, 3 Back, 4 Front, wrapping round after four. On a tie, location picks the lowest position. For an empty reading (outside the stated precondition) intensity returns 0.0 and location returns Front; analysis returns noGas, so the controller never calls intensity or location on one.
- **fix_directive:** Replace the mapping with the robot's actual sensor layout.
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/sensor/GasAnalysisSensor.java` lines 41-84, `intensity, location, angle`, requirements CD-Fn2, CD-Fn3, CD-DM7

### 5. [design_choice] Chem as a record with a nat id; Intensity as real; record named GasSample

- **raw:** Chem (opaque, equality only) is record Chem(int id). Intensity (totally ordered, compared only by goreq) is double, i.e. real, so thr is a real constant. The GasSensor record of CD-DM6 is named GasSample, because the extractor treats any class whose name contains 'sensor' as a sensor service.
- **fix_directive:** None, unless a distinct Intensity type is required in the model.
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/types/Chem.java` lines 11, `Chem`, requirements CD-DM4
  - `java.generated.project/src/main/java/chemical_detector/types/GasSample.java` lines 14, `GasSample`, requirements CD-DM5, CD-DM6

### 6. [design_choice] Operations implemented as Vehicle actuator commands, not operation classes

- **raw:** The profile's operation shape (a class with an argument-less compute() of field assignments, inputs from sensors and constants) cannot carry the arguments of move(lv, a) or changeDirection(l), which come from controller state. So the four operations are methods of VehicleActuator, called in entry actions like the actuator call in the worked example. changeDirection's specified behaviour (left turns Right, right turns Left, front turns Back, at lv) is the method's Java body; in the model it is a call with argument l.
- **fix_directive:** If the extractor requires classes under operation/, move these into argument-less operation classes and accept losing the parameters.
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/actuator/VehicleActuator.java` lines 30-60, `move, randomWalk, shortRandomWalk, changeDirection`, requirements CD-OP1, CD-OP2, CD-OP3, CD-OP4

### 7. [design_choice] Waiting's during action realised on entry to Waiting

- **raw:** CD-MV-FR1 makes randomWalk() Waiting's during action. The profile has no during-action shape, so randomWalk() is issued by every transition into Waiting (the resume transitions). It is not issued at system start, because the initial transition has no action (CD-MV-Beh1).
- **fix_directive:** None, unless the extractor supports during actions.
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/controller/MovementController.java` lines 67-71, `step (WAITING block)`, requirements CD-MV-FR1, CD-MV-Beh3, CD-MV-Beh1

### 8. [design_choice] NoGas -> Reading written with the guard sts == noGas

- **raw:** CD-GA-Beh3 has no guard and no trigger. The profile writes an autonomous transition as a predicate-tested branch, so it is guarded by noGasClassified (sts == noGas). This guard always holds in NoGas, which is entered only with sts == noGas and never changes sts, so the behaviour is unchanged.
- **fix_directive:** None.
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/controller/GasAnalysisController.java` lines 73-78, `step (NO_GAS block)`, requirements CD-GA-Beh3, CD-DC1

### 9. [design_choice] Final states: CONCLUDED for gas analysis; Found is terminal

- **raw:** The gas-analysis final state (CD-GA-Beh6) is the terminal mode CONCLUDED. For the movement subsystem, CD-MV-Beh9 (Found to Final) is realised by making Found terminal: an unguarded Found -> Final step would add a state with no observable difference. So CD-MV-Beh9 maps to the Found mode, not to a transition (a departure from CD-DC1's one-to-one wording).
- **fix_directive:** None.
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/controller/GasAnalysisController.java` lines 92-94, `step (CONCLUDED block)`, requirements CD-GA-Beh6
  - `java.generated.project/src/main/java/chemical_detector/controller/MovementController.java` lines 101-102, `step (FOUND block)`, requirements CD-MV-Beh9, CD-DC1

### 10. [design_choice] Shared events declared on both sides under the same name

- **raw:** turn, stop and resume are sent by the gas-analysis subsystem (OutputEvent.Turn/Stop/Resume) and received by the movement subsystem (InputEvent.Turn/Stop/Resume), so the module can connect them by name. gas and obstacle come only from the Vehicle; flag goes only to it.
- **fix_directive:** None.
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/event/InputEvent.java` lines 13-30, `InputEvent`, requirements CD-ARCH2, CD-Evt4, CD-Evt5, CD-Evt6
  - `java.generated.project/src/main/java/chemical_detector/event/OutputEvent.java` lines 10-23, `OutputEvent`, requirements CD-ARCH2, CD-Evt4, CD-Evt5, CD-Evt6, CD-Evt7

### 11. [design_choice] Odometer as a sensor quantity sampled on entry

- **raw:** CD-Evt3 is labelled an event, but it is used only to sample the distance into d0 or d1 inside an action that already has another trigger. It is therefore VehicleSensor.odometer(), read in those actions, and not a separate input event.
- **fix_directive:** None.
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/sensor/VehicleSensor.java` lines 11-22, `odometer`, requirements CD-Evt3, CD-MV-Var2, CD-MV-Var3

### 12. [ambiguous_requirement] Clock restart point and AvoidingAgain entry

- **raw:** T is restarted on every entry to Avoiding (CD-MV-Clock1, CD-MV-Beh7, CD-MV-Beh17), before wait(evadeTime), so since(T) includes the evade wait. AvoidingAgain has no entry action: the requirements give it none, and it is left at once by one of its two complementary guards (CD-MV-FR6).
- **fix_directive:** Confirm whether evadeTime should count towards stuckPeriod.
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/controller/MovementController.java` lines 86-93, 158-170, `step (AVOIDING entry)`, requirements CD-MV-Clock1, CD-MV-Beh7, CD-MV-Beh17, CD-MV-Beh18

### 13. [scope_question] No Java runtime wiring between the two subsystems

- **raw:** The two controllers are independent classes; nothing in Java forwards GasAnalysisActuator's commands to MovementController.step. The model's module makes the connection. In Java, an event that arrives in a state that does not accept it is dropped, whereas in RoboChart the sender waits. wait(n) is Clock.pause, which blocks the caller.
- **fix_directive:** Add a wiring and scheduling layer if a runnable system is required.
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/actuator/GasAnalysisActuator.java` lines 8-24, `GasAnalysisActuator`, requirements CD-ARCH2

## next_step

Run the pipeline (compile, coverage, profile check, transformations, verifiers) and act on its reports.
