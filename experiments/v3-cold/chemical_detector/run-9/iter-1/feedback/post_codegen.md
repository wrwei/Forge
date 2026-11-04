# post_codegen

**status:** uncertain

**summary:** Two controllers (GasAnalysisController, MovementController) implement all 81 requirements. Nothing is unimplemented; the issues below record 5 invented_default, 6 design_choice, 2 ambiguous_requirement, 1 scope_question.

## Issues
### 1. [invented_default] Values of the six configuration constants

**raw:** The requirements say thr, lv, evadeTime, stuckPeriod, stuckDist and outPeriod are configured at startup but give no values. Chosen: THR=2.0, LV=1.0, EVADE_TIME=2, STUCK_PERIOD=5, STUCK_DIST=1.0, OUT_PERIOD=3. The real constants are whole numbers, so rounding fractional constants up in the model changes no guard.

**fix_directive:** Replace with the deployment's configured values if known; keep real-valued constants whole if the model's rounding must not alter the guards.

**java_trace:**
- `src/main/java/chemical_detector/ChemicalDetectorConstants.java` lines 8-18: `ChemicalDetectorConstants` (CD-Const1, CD-Const2, CD-Const3, CD-Const4, CD-Const5, CD-Const6)

### 2. [invented_default] Initial values of state variables other than gs

**raw:** Only gs has a stated initial value (empty). Chosen: sts=noGas, ins=0.0, anl=Front, a=Front, l=front, d0=d1=0.0, evasionStart=0; the odometer starts at 0.0. Each is overwritten before it is read on every path.

**fix_directive:** None required unless the specification fixes initial values.

**java_trace:**
- `src/main/java/chemical_detector/controller/GasAnalysisController.java` lines 24-27: `gs, sts, ins, anl` (CD-GA-Var1, CD-GA-Var2, CD-GA-Var3, CD-GA-Var4)
- `src/main/java/chemical_detector/controller/MovementController.java` lines 23-27: `a, d0, d1, l, evasionStart` (CD-MV-Var1, CD-MV-Var2, CD-MV-Var3, CD-MV-Var4, CD-MV-Clock1)

### 3. [invented_default] What 'indicates the target chemical' means, and where the target comes from

**raw:** CD-Fn1 does not define 'indicates' or name a target constant. A value indicates the target when its c equals the target Chem and its i is positive; the target Chem is a constructor argument of GasSensorArray.

**fix_directive:** Confirm the indication rule (equality of c alone, or a minimum intensity) and whether the target is a configuration constant.

**java_trace:**
- `src/main/java/chemical_detector/sensor/GasSensorArray.java` lines 15-28: `analysis` (CD-Fn1, CD-DM4)

### 4. [invented_default] Mapping from sensor position to Angle (angle(x))

**raw:** CD-Fn3 uses angle(x) but does not define it. Position p (1-based) maps cyclically to Left, Right, Back, Front, in the declaration order of Angle.

**fix_directive:** Replace with the physical layout of the gas-sensor array.

**java_trace:**
- `src/main/java/chemical_detector/sensor/GasSensorArray.java` lines 61-72: `angle` (CD-Fn3, CD-DM7)

### 5. [invented_default] Results outside the non-empty precondition

**raw:** intensity and location require a non-empty reading. For an empty one they return 0.0 and Front rather than throwing. The controller never reaches them with an empty reading: GasDetected is entered only after analysis returned gasD, which needs at least one value.

**fix_directive:** None required.

**java_trace:**
- `src/main/java/chemical_detector/sensor/GasSensorArray.java` lines 31-54: `intensity, location` (CD-Fn2, CD-Fn3)

### 6. [design_choice] Vehicle operations realised as actuator methods, not compute() operation classes

**raw:** move(lv, a), randomWalk(), shortRandomWalk() and changeDirection(l) are methods of the Vehicle actuator. A profile operation class has an argument-less compute() made only of field assignments, so it can neither carry move's (lv, a) or changeDirection's l nor issue a motion command. changeDirection steers away (left->Right, right->Left, front->Back at LV) by delegating to move. CD-ARCH2 lists only the first three as Vehicle operations.

**fix_directive:** If the model must define changeDirection's steering itself rather than treat it as a platform call, a different encoding is needed.

**java_trace:**
- `src/main/java/chemical_detector/actuator/Vehicle.java` lines 19-47: `move, randomWalk, shortRandomWalk, changeDirection` (CD-OP1, CD-OP2, CD-OP3, CD-OP4)

### 7. [design_choice] Waiting's during action randomWalk() issued on entry

**raw:** The profile has no during-action shape, and a bare action in the Waiting block would be an event-less self-loop (a divergence). randomWalk() is therefore issued on every transition into Waiting. Since an unbounded walk lasts until the next move, the behaviour is the same, except that nothing is issued at system start, before the first resume.

**fix_directive:** The host should start the platform in random-walk mode at power-on.

**java_trace:**
- `src/main/java/chemical_detector/controller/MovementController.java` lines 52-54: `step (resume branches)` (CD-MV-FR1, CD-MV-Beh3, CD-MV-Beh8, CD-MV-Beh12, CD-MV-Beh15, CD-MV-Beh20, CD-MV-Beh23)

### 8. [design_choice] Odometer realised as a sampled sensor quantity, not an event record

**raw:** CD-Evt3 says the movement subsystem samples the distance into d0 (on entry to Avoiding) and d1 (on the obstacle transition). Neither point can wait for an event trigger, so odometer() is a zero-argument sensor method read in those actions.

**fix_directive:** None required.

**java_trace:**
- `src/main/java/chemical_detector/sensor/OdometerSensor.java` lines 15-18: `odometer` (CD-Evt3, CD-MV-Var2, CD-MV-Var3)

### 9. [design_choice] Final states as terminal modes CONCLUDED and HALTED

**raw:** CD-GA-Beh6 and CD-MV-Beh9 lead to final states that no state requirement names. They are terminal modes, so each behavioural requirement has exactly one transition (CD-DC1). FOUND -> HALTED is an unconditional autonomous transition, taken once (no cycle).

**fix_directive:** None required.

**java_trace:**
- `src/main/java/chemical_detector/controller/GasAnalysisController.java` lines 66-78: `step (GAS_DETECTED, CONCLUDED)` (CD-GA-Beh6, CD-DC1)
- `src/main/java/chemical_detector/controller/MovementController.java` lines 85-86: `step (FOUND)` (CD-MV-Beh9, CD-MV-FR3, CD-DC1)

### 10. [design_choice] Intensity as real and Chem as a record

**raw:** Intensity needs ordering and a threshold constant, so it is a real (double), ordered by goreq. Chem needs only equality, so it is a record with one nat field. GasSensor keeps the requirement's name: it is a record (a datatype), not a sensor service, although its name contains 'Sensor'.

**fix_directive:** None required.

**java_trace:**
- `src/main/java/chemical_detector/domain/GasSensor.java` lines 10-11: `GasSensor` (CD-DM4, CD-DM5, CD-DM6)

### 11. [design_choice] Waits delegated to the Vehicle

**raw:** wait(evadeTime) and wait(outPeriod) are Vehicle.pause(n) calls (a RoboChart wait by the naming convention). In Java the pause is recorded for the platform and does not block step(); the model's wait is what delays the next transition.

**fix_directive:** None required.

**java_trace:**
- `src/main/java/chemical_detector/controller/MovementController.java` lines 82-82: `step` (CD-MV-FR4, CD-MV-FR7, CD-Const3, CD-Const6)
- `src/main/java/chemical_detector/controller/MovementController.java` lines 136-140: `step` (CD-MV-FR4, CD-MV-FR7)

### 12. [ambiguous_requirement] Peak intensity and location ignore chemical identity

**raw:** CD-Fn2 and CD-Fn3 take the maximum over the whole reading, whatever its c. Implemented literally, so a stronger non-target chemical could set the heading and the threshold check.

**fix_directive:** Confirm whether intensity and location should consider only target-chemical values.

**java_trace:**
- `src/main/java/chemical_detector/sensor/GasSensorArray.java` lines 31-54: `intensity, location` (CD-Fn2, CD-Fn3)

### 13. [ambiguous_requirement] Progress rule of AvoidingAgain

**raw:** Implemented as written: progress = since(evasionStart) < STUCK_PERIOD || d1 - d0 > STUCK_DIST, and stuck is its exact negation. Treating a quick second obstacle as progress is what the text says, even though it may be the opposite of the intent.

**fix_directive:** Confirm the direction of the time condition.

**java_trace:**
- `src/main/java/chemical_detector/controller/MovementController.java` lines 43-45: `withinStuckPeriod, advancedBeyondStuckDist, makingProgress` (CD-MV-FR6, CD-MV-Beh17, CD-MV-Beh18, CD-Const4, CD-Const5)

### 14. [scope_question] No system-level wiring class

**raw:** Composition is left to the model (shared events Turn, Stop and Resume, matched by name). A Java host must route Gas to gas analysis and Obstacle to movement, forward CommandLink commands to movement as InputEvents, and call step(null) so that autonomous transitions fire.

**fix_directive:** Add a host class if an executable Java system is required.

**java_trace:**
- `src/main/java/chemical_detector/actuator/CommandLink.java` lines 6-25: `CommandLink` (CD-ARCH2)

## next_step

Run the pipeline. None of these issues blocks verification; resolve the invented defaults and ambiguities with the requirement owner.
