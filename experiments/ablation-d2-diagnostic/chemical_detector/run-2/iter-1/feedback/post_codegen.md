# post_codegen — iteration 1 (cold)

**Status:** uncertain

Cold iteration-1 codegen: 21 Java files, two controllers (GasAnalysisController, MovementController), all 81 requirements traced. 12 review items: invented defaults and design choices, none blocking.

## 1. [invented_default] Configuration constant values

- **Raw:** The requirements say thr, lv, evadeTime, stuckPeriod, stuckDist and outPeriod are configured at start-up but give no values.
- **Fix directive:** Chose small integer-valued defaults (THR=3.0, LV=1.0, EVADE_TIME=1, STUCK_PERIOD=3, STUCK_DIST=1.0, OUT_PERIOD=2) in the platform time unit. Replace with the deployment values.
- **Java trace:** `src/main/java/chemical_detector/constants/ChemConstants.java` `ChemConstants` — CD-Const1, CD-Const2, CD-Const3, CD-Const4, CD-Const5, CD-Const6

## 2. [invented_default] What 'a reading indicates the target chemical' means

- **Raw:** CD-Fn1 does not define when a GasSensor value indicates the target chemical.
- **Fix directive:** analysis() returns gasD iff some sample has c equal to the configured target Chem and intensity > 0.
- **Java trace:** `src/main/java/chemical_detector/sensor/GasSensorArray.java` `analysis` — CD-Fn1

## 3. [invented_default] Sensor position to Angle mapping, empty-reading defaults

- **Raw:** CD-DM7/CD-Fn3 say position maps to a sensing direction but not which. Fn2/Fn3 have a non-empty precondition.
- **Fix directive:** angle(x) follows Angle declaration order (1=Left, 2=Right, 3=Back, 4=Front, repeating). Ties pick the first peak. Empty reading: intensity 0.0, location Front.
- **Java trace:** `src/main/java/chemical_detector/sensor/GasSensorArray.java` `angle` — CD-Fn3, CD-DM7
- **Java trace:** `src/main/java/chemical_detector/sensor/GasSensorArray.java` `intensity` — CD-Fn2

## 4. [ambiguous_requirement] Peak intensity is taken over all chemicals

- **Raw:** CD-Fn2's postcondition (result >= every i in the input) makes intensity the maximum over every sample, including non-target chemicals, so a strong non-target reading can reach thr once the target is merely present.
- **Fix directive:** Implemented literally as the spec states. Confirm whether intensity should range over target-chemical samples only.
- **Java trace:** `src/main/java/chemical_detector/sensor/GasSensorArray.java` `intensity` — CD-Fn2, CD-GA-FR4

## 5. [design_choice] Gas-analysis terminal state is 'Concluded', not a RoboChart final state

- **Raw:** CD-GA-Beh6 says the subsystem concludes and processes no further readings; the codegen rules require every mode to have an outgoing transition.
- **Fix directive:** Concluded self-loops on gas, retaining the reading in gs (CD-GA-Var1) without analysing it or emitting anything. This is one extra transition beyond the 7 GA behaviours listed in CD-DC1.
- **Java trace:** `src/main/java/chemical_detector/controller/GasAnalysisController.java` `step` — CD-GA-Beh6, CD-DC1

## 6. [design_choice] Found stays halted via a silent self-loop on stop

- **Raw:** CD-MV-Beh9 (Found_to_Final) now says Found stays halted; every mode needs an outgoing transition, and the flag must be emitted once.
- **Fix directive:** Found self-loops on stop with no action. The flag + move(0, Front) are actions on each incoming stop transition, so a repeated stop never re-emits flag.
- **Java trace:** `src/main/java/chemical_detector/controller/MovementController.java` `step` — CD-MV-Beh9, CD-MV-FR3, CD-Evt7

## 7. [design_choice] Odometer is a sampled sensor value, not an input event

- **Raw:** CD-Evt3 calls the odometer an event but the movement subsystem only samples it on entry to Avoiding and on the TryingAgain->AvoidingAgain transition.
- **Fix directive:** OdometerSensor.odometer() is read into d0/d1; the platform updates it via update(). No odometer InputEvent exists.
- **Java trace:** `src/main/java/chemical_detector/sensor/OdometerSensor.java` `odometer` — CD-Evt3, CD-MV-Var2, CD-MV-Var3

## 8. [design_choice] Waiting's during randomWalk() runs at the head of the mode block

- **Raw:** CD-MV-FR1 specifies a during action; the step() pattern has no during construct.
- **Fix directive:** vehicle.randomWalk() is the first statement of the Waiting block, so it runs every cycle spent in Waiting.
- **Java trace:** `src/main/java/chemical_detector/controller/MovementController.java` `step` — CD-MV-FR1, CD-OP2

## 9. [design_choice] Guards written as complementary pairs

- **Raw:** Analysis guards on sts, GasDetected on ins vs thr, AvoidingAgain on progress vs stuck.
- **Fix directive:** Each pair is p / !p (stsIsNoGas, insAtOrAboveThr, withinStuckPeriod||advancedBeyondStuckDist). !stsIsNoGas equals sts == gasD for the two-valued Status. The threshold guard uses ins >= THR directly; goreq() is implemented and used inside intensity/location.
- **Java trace:** `src/main/java/chemical_detector/controller/GasAnalysisController.java` `step` — CD-GA-Beh4, CD-GA-Beh5, CD-GA-Beh6, CD-GA-Beh7, CD-Fn4
- **Java trace:** `src/main/java/chemical_detector/controller/MovementController.java` `step` — CD-MV-Beh17, CD-MV-Beh18

## 10. [design_choice] Type representations

- **Raw:** Chem (opaque), Intensity (ordered) and the GasSensor record need Java types.
- **Fix directive:** Chem is a record wrapping a nat id; Intensity is double (real). The GasSensor record is named GasSample so it is not mistaken for a sensor-service class.
- **Java trace:** `src/main/java/chemical_detector/data/GasSample.java` `GasSample` — CD-DM4, CD-DM5, CD-DM6

## 11. [design_choice] Operations: platform methods vs operation class

- **Raw:** Four operation requirements; move/randomWalk/shortRandomWalk are provided by the Vehicle, changeDirection is local.
- **Fix directive:** move/randomWalk/shortRandomWalk (and pause, for wait) are Vehicle methods. changeDirection is operation.ChangeDirection. None needs a compute() because none computes state variables.
- **Java trace:** `src/main/java/chemical_detector/actuator/Vehicle.java` `Vehicle` — CD-OP1, CD-OP2, CD-OP3
- **Java trace:** `src/main/java/chemical_detector/operation/ChangeDirection.java` `ChangeDirection` — CD-OP4

## 12. [scope_question] System wiring class

- **Raw:** CD-ARCH2 needs the two subsystems connected by turn/stop/resume.
- **Fix directive:** ChemicalDetectorSystem routes Vehicle events and forwards gas-analysis outputs to movement. Before delivering each event it runs one no-event cycle, so AvoidingAgain resolves before a turn arrives and the turn is not lost.
- **Java trace:** `src/main/java/chemical_detector/system/ChemicalDetectorSystem.java` `ChemicalDetectorSystem` — CD-ARCH1, CD-ARCH2

## Next step

Run the pipeline; review the invented defaults and the Concluded/Found terminal-state choices.
