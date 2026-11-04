# post_codegen

**status:** uncertain

**summary:** Two controllers implement all 81 requirements: GasAnalysisController (Reading, Analysis, NoGas, GasDetected, Concluded) and MovementController (Waiting, Going, Found, Avoiding, TryingAgain, AvoidingAgain, GettingOut, Halted). The issues below record the defaults invented and the design choices made where the requirements are silent or do not fit the profile.

## Issues

### 1. [invented_default] Values of the configuration constants

**raw:** CD-Const1..6 say the values are configured at start-up but give none. Chosen: THR=2.0, LV=1.0, EVADE_TIME=1, STUCK_PERIOD=2, STUCK_DIST=1.0, OUT_PERIOD=1. All are whole numbers, so the model's constants interface (which rounds a fractional constant up) keeps them exact, and they are small so that both outcomes of every guard stay reachable in a bounded model. HALT_VELOCITY=0.0 was added for the zero-velocity halt in Found (CD-OP1, CD-MV-FR3).

**fix_directive:** Replace with the deployment's configured values if known; keep them whole numbers or note any guard whose result depends on the fractional part.

**java_trace:**
- `java.generated.project/src/main/java/chemical_detector/ChemicalDetectorConstants.java` lines 9-28, `ChemicalDetectorConstants`, requirements: CD-Const1, CD-Const2, CD-Const3, CD-Const4, CD-Const5, CD-Const6, CD-OP1, CD-MV-FR3

### 2. [invented_default] Initial values of the state variables

**raw:** Only gs has a stated initial value (empty). Chosen: sts=noGas, ins=0.0, anl=Front (gas analysis); a=Front, l=front, d0=d1=0.0, T=0 (movement). Every one is overwritten before it is read on any path.

**fix_directive:** None needed unless the requirements fix initial values.

**java_trace:**
- `java.generated.project/src/main/java/chemical_detector/controller/GasAnalysisController.java` lines 22-25, `gs, sts, ins, anl`, requirements: CD-GA-Var1, CD-GA-Var2, CD-GA-Var3, CD-GA-Var4
- `java.generated.project/src/main/java/chemical_detector/controller/MovementController.java` lines 20-24, `a, d0, d1, l, T`, requirements: CD-MV-Var1, CD-MV-Var2, CD-MV-Var3, CD-MV-Var4, CD-MV-Clock1

### 3. [invented_default] What 'indicates the target chemical' means in analysis

**raw:** CD-Fn1 does not define when a reading indicates the target chemical, and Chem is opaque. Chosen: some GasSensor value has c equal to the target chemical (a VehicleSensor constructor parameter) and i > 0.

**fix_directive:** Confirm the detection criterion; it may instead need a detection threshold or a different rule.

**java_trace:**
- `java.generated.project/src/main/java/chemical_detector/sensor/VehicleSensor.java` lines 51-58, `analysis`, requirements: CD-Fn1, CD-DM4

### 4. [invented_default] angle(x), tie-breaking, and results outside the preconditions

**raw:** CD-Fn3 names angle(x) but not its mapping. Chosen: 1-based position 1->Left, 2->Right, 3->Back, 4->Front, repeating every four sensors (the order of the Angle enumeration). When several sensors share the peak, location returns the lowest position. Results outside the preconditions: intensity([]) = 0.0 and location([]) = Front. These are never used, because GasDetected is reached only when analysis returned gasD, which requires a non-empty reading.

**fix_directive:** Replace the mapping with the actual layout of the sensor array if known.

**java_trace:**
- `java.generated.project/src/main/java/chemical_detector/sensor/VehicleSensor.java` lines 66-127, `intensity, location, angleAt`, requirements: CD-Fn2, CD-Fn3, CD-DM7

### 5. [ambiguous_requirement] Peak intensity and location span all chemicals, not just the target

**raw:** CD-Fn2 and CD-Fn3 define the peak and its direction over the whole sequence, whatever each value's chemical. Implemented literally, so the threshold check and the turn toward the strongest signal can be driven by a non-target chemical.

**fix_directive:** If only target-chemical values should count, restrict intensity and location to those values.

**java_trace:**
- `java.generated.project/src/main/java/chemical_detector/sensor/VehicleSensor.java` lines 66-98, `intensity, location`, requirements: CD-Fn2, CD-Fn3, CD-GA-FR4, CD-GA-Beh6, CD-GA-Beh7

### 6. [design_choice] Intensity is represented as real; Chem is a record with a nat id

**raw:** Java has no type alias. Intensity values are double (RoboChart real), and goreq compares two of them, so thr can be a plain numeric constant. Chem is a record with one nat field, and equality is its only use.

**fix_directive:** None.

**java_trace:**
- `java.generated.project/src/main/java/chemical_detector/sensor/VehicleSensor.java` lines 109-111, `goreq`, requirements: CD-DM5, CD-Fn4, CD-Const1

### 7. [design_choice] Waiting's during action randomWalk() is issued on entry to Waiting

**raw:** The profile has no during-action construct. randomWalk() is therefore issued on every transition into Waiting, including the resume self-loop. RoboChart re-enters a state on a self-loop, so the self-loop's own action stays empty while Waiting's during behaviour restarts. At start-up the initial transition carries no action, so the first randomWalk() comes with the first resume.

**fix_directive:** If randomWalk() must start at power-on, the platform has to start it; the profile gives the initial transition no action.

**java_trace:**
- `java.generated.project/src/main/java/chemical_detector/controller/MovementController.java` lines 55-167, `step`, requirements: CD-MV-FR1, CD-MV-Beh3, CD-MV-Beh8, CD-MV-Beh12, CD-MV-Beh15, CD-MV-Beh20, CD-MV-Beh23, CD-OP2

### 8. [design_choice] Vehicle operations are Actuator calls, not operation-subpackage classes

**raw:** move, randomWalk and shortRandomWalk are provided by the Vehicle and compute nothing. changeDirection takes a parameter (l). None of them fits the parameterless compute() shape of an operation class, so all four are Actuator methods that the controller calls in actions. The steering rule of changeDirection (left->Right, right->Left, front->Back, at lv) is in the Actuator body, so the model sees an operation call changeDirection(l) without that body.

**fix_directive:** If the model must contain the steering rule, provide it as a guarded definition of changeDirection.

**java_trace:**
- `java.generated.project/src/main/java/chemical_detector/actuator/Actuator.java` lines 28-62, `move, randomWalk, shortRandomWalk, changeDirection`, requirements: CD-OP1, CD-OP2, CD-OP3, CD-OP4

### 9. [design_choice] The odometer is a sensor variable, not an input event

**raw:** CD-Evt3 has the movement subsystem sample the distance inside entry and transition actions (d0 on entry to Avoiding, d1 on TryingAgain->AvoidingAgain). A step-triggering input event cannot be read inside an action, so the odometer is the zero-argument sensor method odometer().

**fix_directive:** None.

**java_trace:**
- `java.generated.project/src/main/java/chemical_detector/sensor/VehicleSensor.java` lines 39-42, `odometer`, requirements: CD-Evt3, CD-MV-Var2, CD-MV-Var3

### 10. [design_choice] Final states and the two transitions with no trigger and no guard

**raw:** Gas analysis ends in the terminal mode Concluded (CD-GA-Beh6). For CD-MV-Beh9 (Found to final), movement has a terminal mode Halted, reached from Found by a transition with no trigger and no guard, so that CD-DC1's one-transition-per-requirement holds. NoGas->Reading (CD-GA-Beh3) likewise has no trigger and no guard. Both are mode blocks with no inner test. Neither lies on a cycle of internal steps: Reading waits for gas, and Halted is terminal.

**fix_directive:** If the profile checker rejects a mode block that has no inner test, report it as a pipeline limitation; the requirements give these transitions no trigger and no guard.

**java_trace:**
- `java.generated.project/src/main/java/chemical_detector/controller/GasAnalysisController.java` lines 69-71, `step (NoGas)`, requirements: CD-GA-Beh3, CD-GA-FR1
- `java.generated.project/src/main/java/chemical_detector/controller/MovementController.java` lines 93-95, `step (Found)`, requirements: CD-MV-Beh9, CD-MV-FR3, CD-DC1

### 11. [design_choice] turn, stop and resume are both OutputEvent and InputEvent records

**raw:** Gas analysis sends OutputEvent.Turn, Stop and Resume through actuator.apply(...); movement receives InputEvent.Turn, Stop and Resume. The two sets share their names on purpose, so that each becomes a single shared event between the two machines. No Java class routes one controller's outputs to the other's inputs; the connection is left to the extracted module.

**fix_directive:** None, unless a runnable Java system wiring is wanted.

**java_trace:**
- `java.generated.project/src/main/java/chemical_detector/controller/GasAnalysisController.java` lines 60-82, `step`, requirements: CD-ARCH2, CD-Evt4, CD-Evt5, CD-Evt6

### 12. [scope_question] Names that differ only in case, fixed by the requirements

**raw:** The requirements fix these names: mode Analysis and function analysis; mode NoGas and value Status.noGas; Angle.Left/Right/Front and Loc.left/right/front. Each pair is distinct when case matters. No event name collides with any other name.

**fix_directive:** If a phase treats names case-insensitively, rename the modes (for example Analysing, NoGasFound) rather than the requirement-named functions and values.

**java_trace:**
- `java.generated.project/src/main/java/chemical_detector/controller/GasAnalysisController.java` lines 52-85, `step`, requirements: CD-GA-FR3, CD-GA-FR1, CD-Fn1, CD-DM1, CD-DM2, CD-DM3

## next_step

Run the pipeline (compile, coverage, profile check, transformations, verifiers).
