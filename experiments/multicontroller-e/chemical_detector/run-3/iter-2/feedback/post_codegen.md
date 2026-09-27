# post_codegen — chemical_detector (iteration 2)

**Status:** uncertain

Chemical_detector codegen, iteration 2. There are two controllers with 19 files, and every requirement is traced. Movement's AvoidingAgain guards are now propositional, and the invented obstacle self-loop in Found is removed.

## Issues

### 1. [invented_default] Constant values are not given by the specification

CD-Const1..6 say the values are configured at start-up but give none. Chosen: THR=1.0, LV=1.0, EVADE_TIME=1, STUCK_PERIOD=2, STUCK_DIST=1.0, OUT_PERIOD=1 (durations in Clock units). Small integers keep the waits and clock comparisons small for model checking.

**Fix directive:** Replace with deployment values if they are known; keep durations small if the model checker's clock range is bounded.

**Java trace:** `src/main/java/chemical_detector/constants/Constants.java` `Constants` (CD-Const1, CD-Const2, CD-Const3, CD-Const4, CD-Const5, CD-Const6)

### 2. [design_choice] Gas analysis ends in a live Concluded state, not a RoboChart Final state

CD-GA-Beh6 ends the analysis ('concludes its search, processing no further readings'). The codegen rules require every mode to have an outgoing transition, so GasDetected goes to Concluded, which keeps accepting gas events: it stores the reading in gs (CD-GA-Var1) but never analyses it and never emits anything again.

**Fix directive:** Confirm that absorbing readings without analysing them meets 'processing no further readings'.

**Java trace:** `src/main/java/chemical_detector/controller/GasAnalysisController.java` `step` (CD-GA-Beh6, CD-GA-Var1); `src/main/java/chemical_detector/mode/GasAnalysisMode.java` `Concluded` (CD-GA-Beh6)

### 3. [design_choice] Found keeps a stop self-loop instead of moving to a Final state

CD-MV-Beh9 (Found -> Final) says the robot stays halted. Found has one explicit self-loop with no action on stop, which is idempotent. flag and the halting move(0, Front) happen only on the transitions into Found, so flag is emitted once. (The iteration-1 obstacle self-loop in Found is removed.)

**Fix directive:** Confirm that a stop self-loop is an acceptable stand-in for the terminal transition.

**Java trace:** `src/main/java/chemical_detector/controller/MovementController.java` `step` (CD-MV-Beh9, CD-MV-FR3)

### 4. [design_choice] The stuck verdict is computed when the second obstacle is recorded

CD-MV-FR6/Beh17/Beh18 decide in AvoidingAgain whether the robot is making progress (elapsed evasion time < STUCK_PERIOD, or d1 - d0 > STUCK_DIST). makingProgress is assigned on TryingAgain -> AvoidingAgain, at the same moment d1 is sampled. AvoidingAgain then branches on makingProgress / !makingProgress. This mirrors the gas analyser's sts pattern. It keeps the clock comparison and the real arithmetic out of the transition guards, which is where iteration 1's Isabelle deadlock-freedom proof for MovementController timed out.

**Fix directive:** None, unless the verdict must be re-evaluated later than on entry to AvoidingAgain.

**Java trace:** `src/main/java/chemical_detector/controller/MovementController.java` `makingProgress` (CD-MV-FR6, CD-MV-Beh17, CD-MV-Beh18, CD-MV-Clock1)

### 5. [invented_default] analysis/location details not fixed by the specification

CD-Fn1: a reading 'indicates the target chemical' is taken to mean some sensor reports the configured target Chem with intensity > 0. CD-Fn3: angle(x) maps zero-based sensor position x to Angle.values()[x mod 4] (Left, Right, Back, Front), and the first sensor wins a tie. Empty reading defaults: noGas, 0.0 and Front.

**Fix directive:** Supply the real sensor-to-direction layout and detection criterion if they are known.

**Java trace:** `src/main/java/chemical_detector/sensor/Sensor.java` `analysis` (CD-Fn1); `src/main/java/chemical_detector/sensor/Sensor.java` `location` (CD-Fn3); `src/main/java/chemical_detector/sensor/Sensor.java` `angle` (CD-Fn3)

### 6. [ambiguous_requirement] intensity is the peak over all chemicals, not only the target

CD-Fn2 defines intensity as the maximum i across the whole reading. A strong non-target chemical could therefore trigger stop once analysis has returned gasD. Implemented literally.

**Fix directive:** Clarify whether the peak should be restricted to the target chemical.

**Java trace:** `src/main/java/chemical_detector/sensor/Sensor.java` `intensity` (CD-Fn2, CD-GA-FR4)

### 7. [design_choice] Odometer is sampled from the sensor service, not received as an event

CD-Evt3 describes the odometer as a reported distance that the movement subsystem samples into d0/d1. Implemented as Sensor.odometer(), a zero-argument reading fed by Sensor.recordOdometer.

**Fix directive:** None unless odometer must be an input event.

**Java trace:** `src/main/java/chemical_detector/sensor/Sensor.java` `odometer` (CD-Evt3, CD-MV-Var2, CD-MV-Var3)

### 8. [design_choice] Type realisations for Chem and Intensity

Chem is a record with one nat field (species) and uses record equality. Intensity is a real (double). goreq(i1, i2) is the only ordering the controllers use on it.

**Fix directive:** None.

**Java trace:** `src/main/java/chemical_detector/types/Chem.java` `Chem` (CD-DM4); `src/main/java/chemical_detector/sensor/Sensor.java` `goreq` (CD-DM5, CD-Fn4)

### 9. [design_choice] Operations are platform methods, not compute() classes

CD-OP1..3 (move, randomWalk, shortRandomWalk) are Vehicle-provided actuation, so they are Vehicle methods. CD-OP4 changeDirection(l) is ObstacleEvasion.changeDirection. None of them computes a state variable, so the compute()-style operation class pattern does not fit them.

**Fix directive:** None.

**Java trace:** `src/main/java/chemical_detector/actuator/Vehicle.java` `Vehicle` (CD-OP1, CD-OP2, CD-OP3); `src/main/java/chemical_detector/operation/ObstacleEvasion.java` `changeDirection` (CD-OP4)

### 10. [design_choice] Waiting's during action and the Analysis guards

Waiting's during action randomWalk() runs at the head of the Waiting block on every cycle. Analysis leaves on stsNoGas and !stsNoGas. This is equivalent to sts == gasD because Status has exactly two values.

**Fix directive:** None.

**Java trace:** `src/main/java/chemical_detector/controller/MovementController.java` `step` (CD-MV-FR1); `src/main/java/chemical_detector/controller/GasAnalysisController.java` `step` (CD-GA-Beh4, CD-GA-Beh5)

### 11. [scope_question] Events a mode does not handle are dropped in Java

Java ignores an event that the current mode does not handle, for example turn in AvoidingAgain or gas while the analyser is in Analysis. Wiring the two controllers together (forwarding actuator outputs) is left to the integrator. It should step the gas analyser with null until it is back in Reading.

**Fix directive:** Decide whether a system-level integration class is in scope.

**Java trace:** `src/main/java/chemical_detector/controller/GasAnalysisController.java` `GasAnalysisController` (CD-ARCH2); `src/main/java/chemical_detector/controller/MovementController.java` `MovementController` (CD-ARCH2)

## Next step

Run the pipeline (compile, coverage, preflight, t2m, m2m, m2t, verifiers) and fix Java from the first failing phase.
