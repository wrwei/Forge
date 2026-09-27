# post_codegen — chemical_detector, iteration 1

**Status:** uncertain

Iteration 1 cold codegen: 19 files under chemical_detector/ (two mode-nested controllers, sensors, Vehicle, command channel). All 81 requirements traced. 11 review items: design choices and invented defaults, none blocking.

## Issues

### 1. [design_choice] Gas analysis has no Final state; source-found goes to a live CONCLUDED sink

CD-GA-Beh6 names a Final target. GasAnalysisController is the first-discovered machine (it receives the Isabelle theory), and a Final state there leaves deadlock_free unprovable. GAS_DETECTED --[ins >= thr]/ stop--> CONCLUDED; CONCLUDED self-loops on gas, retaining the reading (CD-GA-Var1) without analysing it or sending further commands.

- **Fix directive:** Confirm CONCLUDED is an acceptable realisation of 'concludes its search, processing no further readings'.
- **Java:** `src/main/java/chemical_detector/controller/GasAnalysisController.java` (step)
- **Requirements:** CD-GA-Beh6, CD-GA-FR4, CD-GA-Var1

### 2. [design_choice] Movement Found has an obstacle-triggered self-loop instead of a Final successor

CD-MV-Beh9 (Found_to_Final). FOUND keeps the robot halted; it only records obstacle sides (CD-MV-Var4) so the state stays live. flag and the halt move are transition actions on the stop transitions, so the self-loop does not emit flag again.

- **Fix directive:** Confirm an obstacle self-loop is acceptable in FOUND.
- **Java:** `src/main/java/chemical_detector/controller/MovementController.java` (step)
- **Requirements:** CD-MV-Beh9, CD-MV-FR3, CD-MV-Var4

### 3. [design_choice] Odometer modelled as a sensor read, not an input event

CD-Evt3 is typed 'event'. d0/d1 are sampled with odometer.distanceTravelled() inside the Avoiding entry and TryingAgain->AvoidingAgain actions, because the Java pattern cannot receive an input event inside an entry action.

- **Fix directive:** None unless an explicit odometer channel is required.
- **Java:** `src/main/java/chemical_detector/sensor/OdometerSensor.java` (distanceTravelled)
- **Requirements:** CD-Evt3, CD-MV-Var2, CD-MV-Var3

### 4. [design_choice] Waiting 'during randomWalk()' is a top-of-mode statement

CD-MV-FR1. Java calls vehicle.randomWalk() on every cycle spent in WAITING; the extractor renders top-of-mode statements as an entry action.

- **Fix directive:** None.
- **Java:** `src/main/java/chemical_detector/controller/MovementController.java` (step)
- **Requirements:** CD-MV-FR1, CD-OP2

### 5. [design_choice] Platform operations are Vehicle methods; changeDirection is its own class without compute()

CD-OP1..4 are actuations, not computations, so the compute()-only Operation shape does not fit. move is a two-argument call (an operation call in the model); changeDirection(l) has one argument and is therefore extracted as an output event carrying l.

- **Fix directive:** None unless changeDirection must appear as an operation call.
- **Java:** `src/main/java/chemical_detector/actuator/Vehicle.java` (move); `src/main/java/chemical_detector/operation/ChangeDirection.java` (changeDirection)
- **Requirements:** CD-OP1, CD-OP2, CD-OP3, CD-OP4

### 6. [design_choice] Threshold guard written as ins >= THR rather than goreq(ins, thr)

CD-Fn4 / CD-GA-Beh6,7. On double intensities, >= is goreq. Writing it this way keeps the actual comparison in the extracted guard. goreq itself is used inside intensity() and location().

- **Fix directive:** None.
- **Java:** `src/main/java/chemical_detector/controller/GasAnalysisController.java` (step)
- **Requirements:** CD-Fn4, CD-GA-Beh6, CD-GA-Beh7

### 7. [invented_default] Constant values

thr=3.0, lv=1.0, evadeTime=1, stuckPeriod=2, stuckDist=1.0, outPeriod=1. The spec says 'configured at startup' but gives no values. They are kept small so model checking stays tractable.

- **Fix directive:** Replace with deployment values if known.
- **Java:** `src/main/java/chemical_detector/constants/DetectorConstants.java` (DetectorConstants)
- **Requirements:** CD-Const1, CD-Const2, CD-Const3, CD-Const4, CD-Const5, CD-Const6

### 8. [invented_default] Initial values of state variables

sts=noGas, ins=0.0, anl=Front, a=Front, l=front, d0=d1=0.0, gs=[] (the spec gives only gs, as empty).

- **Fix directive:** None.
- **Java:** `src/main/java/chemical_detector/controller/GasAnalysisController.java` (GasAnalysisController); `src/main/java/chemical_detector/controller/MovementController.java` (MovementController)
- **Requirements:** CD-GA-Var2, CD-GA-Var3, CD-GA-Var4, CD-MV-Var1, CD-MV-Var2, CD-MV-Var3, CD-MV-Var4

### 9. [ambiguous_requirement] What 'indicates the target chemical' means; intensity ignores chemical identity

CD-Fn1 does not define 'indicates'. It is implemented as: some sensor has c == targetChemical (a constructor parameter) and i > 0. CD-Fn2 takes the maximum over ALL sensors whatever their chemical, and is implemented literally.

- **Fix directive:** Clarify whether intensity/location should be restricted to the target chemical.
- **Java:** `src/main/java/chemical_detector/sensor/GasSensorService.java` (analysis)
- **Requirements:** CD-Fn1, CD-Fn2

### 10. [invented_default] Mapping from sensor position to Angle

CD-Fn3 / CD-DM7 say the position maps to a direction but not which. Positions 1..4 map to Left, Right, Back, Front and repeat cyclically. An empty reading yields Front.

- **Fix directive:** Supply the physical sensor layout if different.
- **Java:** `src/main/java/chemical_detector/sensor/GasSensorService.java` (location)
- **Requirements:** CD-DM7, CD-Fn3

### 11. [design_choice] Chem as a nat id, Intensity as real

CD-DM4/5 describe opaque/ordered types. Chem is an int species id (@nat), compared only by equality. Intensity is a double (@real).

- **Fix directive:** None.
- **Java:** `src/main/java/chemical_detector/data/GasSensor.java` (GasSensor)
- **Requirements:** CD-DM4, CD-DM5, CD-DM6

## Next step

Run the pipeline (compile -> coverage -> preflight -> T2M -> M2M -> M2T -> verifiers).
