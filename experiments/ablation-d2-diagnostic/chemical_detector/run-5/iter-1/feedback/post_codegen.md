# post_codegen — chemical_detector (iteration 1, cold)

**Status:** uncertain

Cold codegen for chemical_detector. There are two mode-nested controllers: GasAnalysis (5 modes) and Movement (7 modes). All 81 requirements are traced. There is no Final mode in either machine. 10 review items: 5 design choices, 3 invented defaults, 1 ambiguity, 1 scope note.

## Issues

### 1. [design_choice] Gas analysis concludes in a non-final 'Concluded' mode with a gas self-loop

CD-GA-Beh6 says GasDetected -> Final: emit stop and process no further readings. Every mode must have an outgoing transition, so there is no Final mode. GasDetected goes to Concluded when ins >= thr. Concluded accepts later gas events and keeps the latest reading in gs (CD-GA-Var1) but never analyses it again.

**Fix directive:** Review: confirm that a 'retain but do not analyse' self-loop is an acceptable reading of 'concludes its search, processing no further readings'.

**Java trace:**
- `java.generated.project/src/main/java/chemical_detector/controller/GasAnalysisController.java` L63-78 `step` — CD-GA-Beh6, CD-GA-FR4, CD-GA-Var1
- `java.generated.project/src/main/java/chemical_detector/mode/GasAnalysisMode.java` L9 `Concluded` — CD-GA-Beh6

### 2. [design_choice] Movement Found self-loops on obstacle; flag and halt ride on the incoming stop transitions

CD-MV-Beh9 says Found -> Final, and CD-MV-FR3 says Found remains stopped. Found self-loops on obstacle and only records l (CD-MV-Var4); there is no motion and no flag. Each of the six stop transitions into Found sends flag and then move(0, Front), so flag is emitted exactly once, as the system description requires.

**Fix directive:** Review: confirm that the obstacle-driven self-loop is an acceptable way to 'remain stopped'.

**Java trace:**
- `java.generated.project/src/main/java/chemical_detector/controller/MovementController.java` L92-97 `step` — CD-MV-FR3, CD-MV-Beh9, CD-Evt7

### 3. [design_choice] Odometer is a sensor query, not an input event

CD-Evt3 describes the odometer as reporting a distance that is 'sampled' into d0/d1 during a transition. A Java step() handles one event at a time, so the odometer is read with OdometerSensor.odometer() where d0 (entry to Avoiding) and d1 (TryingAgain -> AvoidingAgain) are recorded.

**Fix directive:** Review: confirm that a sampled sensor value satisfies CD-Evt3.

**Java trace:**
- `java.generated.project/src/main/java/chemical_detector/sensor/OdometerSensor.java` L20 `odometer` — CD-Evt3, CD-MV-Var2, CD-MV-Var3

### 4. [invented_default] Constant values

The spec fixes the constants' types but not their values. Chosen values: THR = 1.0, LV = 1.0, EVADE_TIME = 1, STUCK_PERIOD = 1, STUCK_DIST = 1.0, OUT_PERIOD = 1. They are kept small so that bounded model checking can reach both sides of every guard.

**Fix directive:** Replace with deployment values if they are known.

**Java trace:**
- `java.generated.project/src/main/java/chemical_detector/constants/ChemicalDetectorConstants.java` L10-30 `ChemicalDetectorConstants` — CD-Const1, CD-Const2, CD-Const3, CD-Const4, CD-Const5, CD-Const6

### 5. [invented_default] Sensor position to Angle mapping

CD-DM7 and CD-Fn3 use angle(x) but never define it. Chosen mapping, 1-based: position 1 -> Left, 2 -> Right, 3 -> Back, 4 -> Front, cycling for longer readings.

**Fix directive:** Confirm the physical sensor layout.

**Java trace:**
- `java.generated.project/src/main/java/chemical_detector/sensor/GasSensorFunctions.java` L51-69 `location` — CD-Fn3, CD-DM7

### 6. [ambiguous_requirement] What 'indicates the target chemical' means, and where the target is configured

CD-Fn1 does not say how a reading indicates the target, and no requirement names the target chemical. Chosen rule: a sample indicates the target when its c equals the target Chem and its i > 0. The target is passed to GasSensorFunctions' constructor. intensity(gs) is the peak over all samples, whatever their chemical, as CD-Fn2 states.

**Fix directive:** Review whether intensity should be restricted to target-chemical samples.

**Java trace:**
- `java.generated.project/src/main/java/chemical_detector/sensor/GasSensorFunctions.java` L26-48 `analysis` — CD-Fn1, CD-Fn2, CD-DM4

### 7. [invented_default] Initial values of state variables

gs = [] is specified (CD-GA-Var1). Invented: sts = noGas, ins = 0.0, anl = Front, a = Front, l = front, d0 = d1 = 0.0, evasion timer = 0.

**Fix directive:** None expected; every variable is written before it is read on any path that uses it.

**Java trace:**
- `java.generated.project/src/main/java/chemical_detector/controller/GasAnalysisController.java` L24-28 `GasAnalysisController` — CD-GA-Var2, CD-GA-Var3, CD-GA-Var4
- `java.generated.project/src/main/java/chemical_detector/controller/MovementController.java` L24-31 `MovementController` — CD-MV-Var1, CD-MV-Var2, CD-MV-Var3, CD-MV-Var4, CD-MV-Clock1

### 8. [design_choice] Data type representations

Intensity is a Java double (real), compared through goreq (CD-Fn4), which is also the guard at GasDetected. Chem is a record wrapping a nat id, which gives equality only. The spec's GasSensor record is named GasSample so that no class other than the real sensor services has 'sensor' in its name.

**Fix directive:** None.

**Java trace:**
- `java.generated.project/src/main/java/chemical_detector/domain/GasSample.java` L9 `GasSample` — CD-DM5, CD-DM6
- `java.generated.project/src/main/java/chemical_detector/domain/Chem.java` L6 `Chem` — CD-DM4

### 9. [design_choice] Platform operations, changeDirection, and wait

move, randomWalk and shortRandomWalk are Vehicle methods, because the Vehicle provides them (CD-OP1-3). changeDirection(l) is an operation class that calls move (CD-OP4). None of these is a compute() class, because they act on the platform rather than compute values. wait(evadeTime) and wait(outPeriod) are Vehicle.pause(n), which does not block in Java. Waiting's during action randomWalk() is a statement at the head of the Waiting block, so it runs on every cycle spent in Waiting.

**Fix directive:** None.

**Java trace:**
- `java.generated.project/src/main/java/chemical_detector/actuator/Vehicle.java` L42 `pause` — CD-Const3, CD-Const6, CD-MV-FR4, CD-MV-FR7
- `java.generated.project/src/main/java/chemical_detector/operation/ChangeDirection.java` L21-29 `changeDirection` — CD-OP4
- `java.generated.project/src/main/java/chemical_detector/controller/MovementController.java` L58 `step` — CD-MV-FR1, CD-OP2

### 10. [scope_question] Java runtime drops a turn that arrives while Movement is in AvoidingAgain

AvoidingAgain has no turn transition. In RoboChart the sender would block until AvoidingAgain leaves on its own. In the Java composition (ChemicalDetectorSystem) that turn is dropped, and the next reading supplies a new one.

**Fix directive:** None unless lossless delivery between the subsystems is required.

**Java trace:**
- `java.generated.project/src/main/java/chemical_detector/system/ChemicalDetectorSystem.java` L40-54 `cycle` — CD-ARCH2, CD-MV-FR6

## Next step

Run the pipeline (compile -> coverage -> preflight -> T2M -> M2M -> M2T -> verifiers).
