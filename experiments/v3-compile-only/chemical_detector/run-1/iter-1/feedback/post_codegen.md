# post_codegen

**status:** uncertain

**summary:** Two controllers (GasAnalysisController, MovementController) and the system wiring cover all 81 requirements. Nothing is unimplemented; listed below are the invented defaults, design choices and one scope question.

## Issues

### 1. [invented_default] Values of the six configuration constants

- **raw:** CD-Const1..6 say the constants are configured at start-up but give no values.
- **fix_directive:** Chosen: THR=2.0, LV=1.0, EVADE_TIME=1, STUCK_PERIOD=2, STUCK_DIST=1.0, OUT_PERIOD=1. The real-typed ones are whole numbers, so rounding a fractional constant up in the constants interface leaves them unchanged (the STUCK_DIST guard d1 - d0 > STUCK_DIST depends on the exact value). Replace them with the deployment values; keep THR and STUCK_DIST whole numbers or check the guards after rounding.
- **java_trace:**
  - `src/main/java/chemical_detector/ChemicalDetectorConstants.java` lines 9-24, `ChemicalDetectorConstants`: CD-Const1, CD-Const2, CD-Const3, CD-Const4, CD-Const5, CD-Const6

### 2. [invented_default] Initial values of the state variables

- **raw:** Only gs has a stated initial value (empty, CD-GA-Var1).
- **fix_directive:** Chosen: sts=noGas, ins=0.0, anl=Front, a=Front, l=front, d0=d1=0.0, evasionStart=0. Each is overwritten before it is read on every path (sts on entry to Analysis, ins on entry to GasDetected, a by every turn, l by every obstacle, d0/T on entry to Avoiding, d1 before AvoidingAgain), so none affects behaviour.
- **java_trace:**
  - `src/main/java/chemical_detector/controller/GasAnalysisController.java` lines 27-33, `gs, sts, ins, anl`: CD-GA-Var1, CD-GA-Var2, CD-GA-Var3, CD-GA-Var4
  - `src/main/java/chemical_detector/controller/MovementController.java` lines 26-34, `a, l, d0, d1, evasionStart`: CD-MV-Var1, CD-MV-Var2, CD-MV-Var3, CD-MV-Var4, CD-MV-Clock1

### 3. [invented_default] Meaning of 'indicates the target chemical', the target identity, angle(x), and empty readings

- **raw:** CD-Fn1 does not say how a reading indicates the target chemical or how the target is known; CD-Fn3 uses angle(x) without defining it; CD-Fn2/Fn3 have a non-empty precondition.
- **fix_directive:** Chosen: the target chemical identity is a ChemicalSensor constructor parameter; a sample indicates it when c == target and i > 0. angle(x) maps positions 1, 2, 3, 4 to Left, Right, Back, Front, repeating for longer readings; on ties location picks the first strongest position. For an empty reading intensity returns 0.0 and location returns Front; the controller never calls them with one, because an empty reading is classified noGas.
- **java_trace:**
  - `src/main/java/chemical_detector/sensor/ChemicalSensor.java` lines 15-83, `ChemicalSensor`: CD-Fn1, CD-Fn2, CD-Fn3, CD-DM7

### 4. [design_choice] Chem and Intensity as primitive types; the record is named GasSample

- **raw:** CD-DM4/DM5 define opaque Chem (equality only) and ordered Intensity; CD-DM6 names the record GasSensor.
- **fix_directive:** Chem is a nat identifier and Intensity is real (goreq is >=), so thr stays a numeric constant and the reading type stays flat. The record is named GasSample because a class whose name contains 'sensor' is taken for a sensor service.
- **java_trace:**
  - `src/main/java/chemical_detector/domain/GasSample.java` lines 10-11, `GasSample`: CD-DM4, CD-DM5, CD-DM6

### 5. [design_choice] Operations move, randomWalk, shortRandomWalk and changeDirection are Vehicle commands

- **raw:** CD-OP1..4 are type 'operation'; the profile's operation classes have a parameterless compute() made only of assignments.
- **fix_directive:** The three platform operations are VehicleActuator methods. changeDirection(l) is also a VehicleActuator method, defined as move(LV, Right/Left/Back) for an obstacle on the left/right/front, because a branching operation that takes a parameter cannot be written as compute(). The halt in Found is move(0.0, Front).
- **java_trace:**
  - `src/main/java/chemical_detector/actuator/VehicleActuator.java` lines 25-57, `move, randomWalk, shortRandomWalk, changeDirection`: CD-OP1, CD-OP2, CD-OP3, CD-OP4

### 6. [design_choice] Waiting's during action randomWalk() is issued on every transition into Waiting

- **raw:** CD-MV-FR1: the during action of Waiting is randomWalk().
- **fix_directive:** A step() without an event cannot carry a during action without creating a self-loop with no trigger and no guard, which would diverge. So randomWalk() is performed by every transition into Waiting (resume from any state, including the Waiting self-loop). It is not issued at start-up, before the first resume.
- **java_trace:**
  - `src/main/java/chemical_detector/controller/MovementController.java` lines 64-212, `step`: CD-MV-FR1, CD-MV-Beh3, CD-MV-Beh8, CD-MV-Beh12, CD-MV-Beh15, CD-MV-Beh20, CD-MV-Beh23

### 7. [design_choice] Final states are the modes Concluded and Halted; Found to Halted carries CD-MV-Beh9

- **raw:** CD-GA-Beh6 and CD-MV-Beh9 name a Final state; CD-DC1 asks for one transition per behavioural requirement.
- **fix_directive:** Gas analysis ends in the terminal mode Concluded. Movement goes from Found to the terminal mode Halted with no trigger and no guard, so CD-MV-Beh9 is exactly one transition and the Vehicle stays halted. Each mode block has one transition per behavioural requirement and no duplicate state/event pair (CD-DC1).
- **java_trace:**
  - `src/main/java/chemical_detector/controller/MovementController.java` lines 114-116, `step`: CD-MV-Beh9, CD-MV-FR3, CD-DC1
  - `src/main/java/chemical_detector/controller/GasAnalysisController.java` lines 80-95, `step`: CD-GA-Beh6, CD-DC1

### 8. [design_choice] Inter-subsystem events, odometer sampling and cycles without an event

- **raw:** CD-ARCH2: the subsystems communicate only via turn, stop and resume. CD-Evt3: movement samples the odometer distance.
- **fix_directive:** turn, stop and resume are OutputEvent records sent by gas analysis and InputEvent records of the same names received by movement; ChemicalDetectorSystem forwards them. The odometer is the sensor method odometer(), read in actions (d0, d1), not an event. Transitions with no trigger fire on a step(null) cycle, which ChemicalDetectorSystem runs after each event.
- **java_trace:**
  - `src/main/java/chemical_detector/event/OutputEvent.java` lines 9-18, `Turn, Stop, Resume, Flag`: CD-Evt4, CD-Evt5, CD-Evt6, CD-Evt7, CD-ARCH2
  - `src/main/java/chemical_detector/ChemicalDetectorSystem.java` lines 42-84, `dispatch`: CD-ARCH1, CD-ARCH2, CD-Evt3

### 9. [scope_question] Obstacle events outside Going and TryingAgain are ignored

- **raw:** Only CD-MV-Beh7 and CD-MV-Beh16 consume obstacle.
- **fix_directive:** An obstacle in Waiting, Avoiding, AvoidingAgain, GettingOut, Found or Halted triggers no transition (l is not updated either). Add requirements if those states should react.
- **java_trace:**
  - `src/main/java/chemical_detector/controller/MovementController.java` lines 64-212, `step`: CD-MV-Var4, CD-Evt2

## next_step

Run the pipeline (compile, coverage, profile check, transformations, FDR4, Dafny, Isabelle, vacuity) and fix the Java from its reports.
