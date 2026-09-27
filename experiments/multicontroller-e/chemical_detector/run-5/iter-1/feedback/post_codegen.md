# post_codegen — chemical_detector

**Status:** uncertain

Two controllers (GasAnalysisController, MovementController) implement all 81 requirements. Eight review items: one terminal-state design choice, two invented defaults, four design choices and one scope question. None of them blocks the build.

## 1. [design_choice] No Final state in either controller; terminal modes keep one outgoing transition

- **Raw:** CD-GA-Beh6 and CD-MV-Beh9 describe transitions into a final state. Each controller must still be able to make progress from every mode it can reach.
- **Fix directive:** GasAnalysis: GasDetected goes to a non-final mode Concluded when goreq(ins, thr) holds. In Concluded a gas event only replaces gs (CD-GA-Var1) and runs no analysis, so no further readings are processed. Movement: Found keeps a stop self-loop with no actions, so flag and the halt fire once, on the transitions into Found. Review whether this reading of 'concludes' and 'stays halted' is acceptable.
- **Trace:** `src/main/java/chemical_detector/controller/GasAnalysisController.java` :: `step` — CD-GA-Beh6, CD-GA-FR4
- **Trace:** `src/main/java/chemical_detector/controller/MovementController.java` :: `step` — CD-MV-Beh9, CD-MV-FR3

## 2. [invented_default] Constant values are not given by the requirements

- **Raw:** CD-Const1..6 say the values are configured at startup but give none.
- **Fix directive:** Values chosen: THR=1.0, LV=1.0, EVADE_TIME=1, STUCK_PERIOD=1, STUCK_DIST=1.0, OUT_PERIOD=1, all in Clock.nowMs() units. Added HALT_VELOCITY=0.0 for the Found halt, move(0, Front) (CD-OP1, CD-MV-FR3). Replace them with platform values.
- **Trace:** `src/main/java/chemical_detector/constants/DetectorConstants.java` :: `DetectorConstants` — CD-Const1, CD-Const2, CD-Const3, CD-Const4, CD-Const5, CD-Const6

## 3. [invented_default] Meaning of 'reading indicates the target chemical' and the sensor-position-to-Angle map

- **Raw:** CD-Fn1 does not define 'indicates'. CD-Fn3 uses angle(x) without defining it.
- **Fix directive:** analysis(gs) is gasD iff some element has c == target chemical (a Sensor constructor argument) and i > 0. location(gs) returns angle(first index holding the peak), with positions 1,2,3,4 mapped to Left, Right, Back, Front, repeating in that cycle. For an empty reading, intensity returns 0 and location returns Front.
- **Trace:** `src/main/java/chemical_detector/sensor/Sensor.java` :: `analysis` — CD-Fn1
- **Trace:** `src/main/java/chemical_detector/sensor/Sensor.java` :: `location` — CD-Fn3

## 4. [design_choice] Chem and Intensity are represented by primitives

- **Raw:** CD-DM4/DM5 define opaque Chem (equality only) and ordered Intensity.
- **Fix directive:** GasSensor(c : nat, i : real). Chem is a nat identifier and Intensity is real, and goreq(i1, i2) is the only ordering used by the controller. This avoids nested record types in the extracted model.
- **Trace:** `src/main/java/chemical_detector/domain/GasSensor.java` :: `GasSensor` — CD-DM4, CD-DM5, CD-DM6

## 5. [design_choice] Platform operations live on Vehicle; changeDirection is an operation class without compute()

- **Raw:** CD-OP1..3 are provided by the Vehicle. CD-OP4 is an actuation with a Loc parameter, not a computation.
- **Fix directive:** move/randomWalk/shortRandomWalk/pause are methods on actuator.Vehicle. ChangeDirection.changeDirection(l) maps left->Right, right->Left, front->Back at LV. No compute() method, because nothing is computed and a parameter is required.
- **Trace:** `src/main/java/chemical_detector/actuator/Vehicle.java` :: `Vehicle` — CD-OP1, CD-OP2, CD-OP3
- **Trace:** `src/main/java/chemical_detector/operation/ChangeDirection.java` :: `ChangeDirection` — CD-OP4

## 6. [design_choice] Odometer is sampled from the sensor, not received as a trigger event

- **Raw:** CD-Evt3 is typed 'event' but is only ever sampled into d0/d1.
- **Fix directive:** d0/d1 = sensor.odometer(). The platform pushes reports through Sensor.updateOdometer.
- **Trace:** `src/main/java/chemical_detector/sensor/Sensor.java` :: `odometer` — CD-Evt3, CD-MV-Var2, CD-MV-Var3

## 7. [design_choice] Complementary guards on autonomous choices

- **Raw:** Analysis (sts), GasDetected (ins vs thr) and AvoidingAgain (progress vs stuck) each choose between two outcomes.
- **Fix directive:** The guards are written P / !P: stsNoGas / !stsNoGas, which is equivalent to sts == gasD because Status has two values; insAtOrAboveThr / !insAtOrAboveThr; and (withinStuckPeriod || advancedBeyondStuckDist) / its negation.
- **Trace:** `src/main/java/chemical_detector/controller/GasAnalysisController.java` :: `step` — CD-GA-Beh4, CD-GA-Beh5, CD-GA-Beh6, CD-GA-Beh7
- **Trace:** `src/main/java/chemical_detector/controller/MovementController.java` :: `step` — CD-MV-Beh17, CD-MV-Beh18

## 8. [scope_question] Wiring class and output port are not named by the requirements

- **Raw:** CD-ARCH2 requires the subsystems to communicate only via turn/stop/resume and flag to reach the Vehicle.
- **Fix directive:** system.ChemicalDetectorSystem forwards the GasAnalysis Actuator's turn/stop/resume to Movement and delivers flag to Vehicle.signalSourceFound(). Waits (evadeTime, outPeriod) are recorded by Vehicle.pause and do not block.
- **Trace:** `src/main/java/chemical_detector/system/ChemicalDetectorSystem.java` :: `ChemicalDetectorSystem` — CD-ARCH1, CD-ARCH2

## Next step

Run the pipeline (compile, coverage, preflight, T2M, M2M, M2T, verifiers) and diagnose the failing phases.
