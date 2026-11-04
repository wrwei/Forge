# post_codegen

**status:** uncertain

**summary:** All 81 requirements are implemented. The two controllers are GasAnalysisController and MovementController. Issues: 3 invented defaults, 1 ambiguous requirement, 8 design choices and 1 scope question. Nothing is unimplemented.

## Issues

### 1. [invented_default] Values of the six configuration constants

- **raw:** CD-Const1..6 say the values are configured at start-up but give none.
- **fix_directive:** Chosen: THR = 3.0, LV = 1.0, EVADE_TIME = 1, STUCK_PERIOD = 3, STUCK_DIST = 1.0, OUT_PERIOD = 2 (time in control cycles). All are whole numbers, so rounding fractional constants in the model changes nothing. STUCK_PERIOD > EVADE_TIME so both AvoidingAgain outcomes stay reachable. Replace these with the deployed configuration.
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/ChemicalDetectorConstants.java` lines 9-31, `ChemicalDetectorConstants`: CD-Const1, CD-Const2, CD-Const3, CD-Const4, CD-Const5, CD-Const6

### 2. [invented_default] Initial values of state variables

- **raw:** Only gs has an initial value (empty). sts, ins, anl, a, l, d0, d1 and the evasion clock have none.
- **fix_directive:** Chosen: sts = noGas, ins = 0.0, anl = Front, a = Front, l = front, d0 = d1 = 0.0, evasionStart = 0. Every one is written before it is read on every path.
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/controller/GasAnalysisController.java` lines 22-25, `gs, sts, ins, anl`: CD-GA-Var1, CD-GA-Var2, CD-GA-Var3, CD-GA-Var4
  - `java.generated.project/src/main/java/chemical_detector/controller/MovementController.java` lines 20-24, `a, d0, d1, l, evasionStart`: CD-MV-Var1, CD-MV-Var2, CD-MV-Var3, CD-MV-Var4, CD-MV-Clock1

### 3. [invented_default] intensity and location on an empty reading

- **raw:** CD-Fn2 and CD-Fn3 require a non-empty reading and do not say what an empty one yields.
- **fix_directive:** intensity returns 0.0 and location returns Front for an empty reading. The controller only calls them in GasDetected, which needs analysis = gasD, which implies a non-empty reading.
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/sensor/ChemicalSensor.java` lines 43-69, `intensity, location`: CD-Fn2, CD-Fn3

### 4. [ambiguous_requirement] What 'indicates the target chemical' means, and which chemical is the target

- **raw:** CD-Fn1 classifies against 'the target chemical' but no requirement names it or defines 'indicates'.
- **fix_directive:** The target Chem is a constructor argument of ChemicalSensor. A reading indicates it when some GasSensor has c equal to the target and i > 0.
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/sensor/ChemicalSensor.java` lines 16-37, `ChemicalSensor.analysis`: CD-Fn1, CD-DM4

### 5. [design_choice] Mapping from sensor position to Angle, and ties

- **raw:** CD-DM7/CD-Fn3: position (1-based) 'maps to a sensing direction' via angle(x), which is not defined.
- **fix_directive:** Position k maps to Front, Right, Back, Left for k = 1, 2, 3, 4 (clockwise from front), repeating every four. On a tie, location returns the first sensor with the peak.
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/sensor/ChemicalSensor.java` lines 56-87, `location, angleOf`: CD-Fn3, CD-DM7

### 6. [design_choice] Representation of Chem and Intensity

- **raw:** CD-DM4: Chem is opaque with equality only. CD-DM5: Intensity is totally ordered, compared via goreq.
- **fix_directive:** Chem is a record holding a nat species identifier. Intensity is real (double), because thr has to be a numeric constant. Ordering is only ever tested through goreq.
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/Chem.java` lines 5-11, `Chem`: CD-DM4
  - `java.generated.project/src/main/java/chemical_detector/GasSensor.java` lines 5-13, `GasSensor`: CD-DM5, CD-DM6

### 7. [design_choice] Operations implemented as Vehicle actuator calls, not operation/compute() classes

- **raw:** CD-OP1..3 are provided by the Vehicle. CD-OP4 is changeDirection(l). All four are actuations whose arguments are controller variables (a, l). The profile's operation class is a compute() made of sensor-derived assignments, which cannot take those arguments.
- **fix_directive:** move(lv, a), randomWalk(), shortRandomWalk() and changeDirection(l) are methods of VehicleActuator, called in the entry actions. changeDirection maps left to Right, right to Left and front to Back at LV. The wait(n) of Avoiding and GettingOut is VehicleActuator.pause(n).
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/actuator/VehicleActuator.java` lines 22-63, `VehicleActuator`: CD-OP1, CD-OP2, CD-OP3, CD-OP4, CD-Const3, CD-Const6

### 8. [design_choice] Odometer sampled as a sensor value, not received as an event

- **raw:** CD-Evt3 is typed 'event'; the movement subsystem 'samples this distance (into d0 or d1)'.
- **fix_directive:** VehicleSensor.odometer() is a zero-argument sensor read in the Avoiding entry (d0) and on TryingAgain -> AvoidingAgain (d1). Both of those transitions are triggered by obstacle.
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/sensor/VehicleSensor.java` lines 5-22, `odometer`: CD-Evt3, CD-MV-Var2, CD-MV-Var3

### 9. [design_choice] Waiting's during action randomWalk() realised as an entry action

- **raw:** CD-MV-FR1: Waiting's during action is randomWalk(). The Java profile has no during actions.
- **fix_directive:** Every transition into Waiting, including the resume self-loop, calls randomWalk(). The constructor also calls it once for the initial entry. randomWalk is unbounded, so one call covers the whole stay in Waiting.
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/controller/MovementController.java` lines 33-41, `MovementController()`: CD-MV-FR1
  - `java.generated.project/src/main/java/chemical_detector/controller/MovementController.java` lines 53-206, `step`: CD-MV-Beh3, CD-MV-Beh8, CD-MV-Beh12, CD-MV-Beh15, CD-MV-Beh20, CD-MV-Beh23

### 10. [design_choice] Final states as terminal modes

- **raw:** CD-GA-Beh6: GasDetected 'concludes'. CD-MV-Beh9: Found 'stays halted'. CD-DC1 maps each behavioural requirement to exactly one transition.
- **fix_directive:** Gas analysis concludes in a terminal mode, Concluded, reached by the CD-GA-Beh6 transition. Found is itself terminal, so CD-MV-Beh9 has no transition of its own: it is realised as the absence of any transition out of Found. Each other behavioural requirement is exactly one branch.
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/controller/GasAnalysisController.java` lines 78-93, `step`: CD-GA-Beh6, CD-DC1
  - `java.generated.project/src/main/java/chemical_detector/controller/MovementController.java` lines 108-110, `step`: CD-MV-Beh9, CD-DC1

### 11. [design_choice] When the evasion clock restarts and when d0 is sampled

- **raw:** CD-MV-Clock1/CD-MV-Var2: both happen 'on entry to Avoiding'. CD-MV-Beh17: AvoidingAgain -> Avoiding 'restarts the evasion timer'.
- **fix_directive:** Both transitions into Avoiding (Going on obstacle, and AvoidingAgain when making progress) restart evasionStart and re-sample d0 before changeDirection(l) and pause(EVADE_TIME). The stuck test is timer.now() - evasionStart < STUCK_PERIOD || d1 - d0 > STUCK_DIST.
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/controller/MovementController.java` lines 53-206, `step`: CD-MV-Clock1, CD-MV-Var2, CD-MV-Beh7, CD-MV-Beh17, CD-MV-Beh18

### 12. [scope_question] System wiring classes not named by any requirement

- **raw:** CD-ARCH1/2 describe the composition but name no Java elements.
- **fix_directive:** ChemicalDetectorSystem wires the Vehicle, the two controllers, the clock and the odometer. GasAnalysisActuator delivers turn/stop/resume from gas analysis to movement as a synchronous call (no queue). Neither contains state-machine logic.
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/ChemicalDetectorSystem.java` lines 13-52, `ChemicalDetectorSystem`: CD-ARCH1, CD-ARCH2
  - `java.generated.project/src/main/java/chemical_detector/actuator/GasAnalysisActuator.java` lines 7-33, `GasAnalysisActuator`: CD-ARCH2

### 13. [design_choice] Unguarded NoGas -> Reading written as a mode block with no inner if

- **raw:** CD-GA-Beh3: no guard, no trigger, no action.
- **fix_directive:** The NoGas block only assigns currentMode = Reading. It is not a self-loop, and Reading can only be left on a gas event, so the step cannot repeat without an event.
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/controller/GasAnalysisController.java` lines 74-77, `step`: CD-GA-Beh3

## next_step

Run the pipeline (compile, coverage, profile check, transformations, FDR4, Dafny, Isabelle, vacuity).
