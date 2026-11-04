# post_codegen

status: uncertain

summary: Two controllers (GasAnalysisController, MovementController) implement all 81 requirements; 12 points the requirements leave open are recorded below.

## Issues

### 1. [design_choice] GasSensor record named GasSample

- raw: CD-DM6 names the record GasSensor. The profile treats any class whose name contains 'sensor' as a sensor service, so the record is named GasSample (fields c: Chem, i: real).
- fix_directive: If the pipeline treats records separately from sensor services, the record can be renamed GasSensor.
- java_trace:
  - java.generated.project/src/main/java/chemical_detector/domain/GasSample.java:13 `GasSample` (CD-DM6, CD-DM7)

### 2. [design_choice] Mode names differ from the requirement state names

- raw: Modes are UPPER_SNAKE. NoGas is GAS_ABSENT and Analysis is ANALYSING, so that they don't clash, ignoring case, with Status.noGas and the function analysis. The final state of gas analysis is the terminal mode CONCLUDED. Found is a terminal mode, with no separate final state (CD-MV-Beh9).
- fix_directive: None unless state names must match the requirements literally.
- java_trace:
  - java.generated.project/src/main/java/chemical_detector/controller/GasAnalysisMode.java:3-9 `GasAnalysisMode` (CD-GA-FR1, CD-GA-FR3, CD-GA-Beh6)
  - java.generated.project/src/main/java/chemical_detector/controller/MovementMode.java:3-11 `FOUND` (CD-MV-FR3, CD-MV-Beh9)

### 3. [design_choice] Operations implemented as Vehicle methods, not operation classes

- raw: move, randomWalk, shortRandomWalk and changeDirection are actuation commands with no computed result, so they don't fit the compute()-of-assignments operation shape. They are methods of the Vehicle actuator. changeDirection's left->Right, right->Left, front->Back mapping at LV is in its Java body, so the model sees changeDirection(l) as an abstract operation call.
- fix_directive: If operation requirements must be operation-package classes, say what such a class should compute.
- java_trace:
  - java.generated.project/src/main/java/chemical_detector/actuator/Vehicle.java:25-60 `Vehicle` (CD-OP1, CD-OP2, CD-OP3, CD-OP4)

### 4. [design_choice] Odometer is a sensor method, not an event

- raw: CD-Evt3 is typed event, but the movement subsystem samples the distance inside actions (d0, d1). It is read through OdometerSensor.distanceTravelled(), which becomes a sensor variable. There is no Odometer event record.
- fix_directive: None.
- java_trace:
  - java.generated.project/src/main/java/chemical_detector/sensor/OdometerSensor.java:24-26 `distanceTravelled` (CD-Evt3, CD-MV-Var2, CD-MV-Var3)

### 5. [design_choice] Shared events declared in both hierarchies

- raw: Turn(Angle), Stop and Resume are OutputEvent records sent by GasAnalysisController through EventBus.apply. Records with the same names and payloads in InputEvent are received by MovementController, so the two can be matched as shared events. Flag is an OutputEvent sent by MovementController.
- fix_directive: If shared events need a single record, merge each pair.
- java_trace:
  - java.generated.project/src/main/java/chemical_detector/event/OutputEvent.java:13-22 `OutputEvent` (CD-Evt4, CD-Evt5, CD-Evt6, CD-Evt7)
  - java.generated.project/src/main/java/chemical_detector/event/InputEvent.java:31-37 `InputEvent` (CD-Evt4, CD-Evt5, CD-Evt6)

### 6. [design_choice] Waiting's during action is issued on entry

- raw: The profile has no during actions. randomWalk() is called on every transition into WAITING, including the resume self-loop: re-entering a state restarts its during action, even though CD-MV-Beh3 says 'no action'. The initial transition has no action (CD-MV-Beh1), so the Vehicle is constructed already random-walking.
- fix_directive: None unless during semantics are required in the model.
- java_trace:
  - java.generated.project/src/main/java/chemical_detector/controller/MovementController.java:62-65 `step` (CD-MV-FR1, CD-MV-Beh3, CD-OP2)
  - java.generated.project/src/main/java/chemical_detector/actuator/Vehicle.java:15 `randomWalking` (CD-MV-Beh1)

### 7. [design_choice] Intensity is real; Chem is a record over nat

- raw: Intensity is a double (real), compared with goreq, so thr can be a numeric constant. Chem is record Chem(nat id), whose only operation is equality.
- fix_directive: None.
- java_trace:
  - java.generated.project/src/main/java/chemical_detector/domain/GasSample.java:13 `i` (CD-DM4, CD-DM5)

### 8. [ambiguous_requirement] GasAbsent -> Reading has no guard and no trigger

- raw: CD-GA-Beh3 is unguarded and untriggered. The GAS_ABSENT mode block is therefore a bare mode assignment, with no inner if. It cannot repeat, because Reading leaves only on a gas event.
- fix_directive: If the extractor needs an inner if, this block is the one to change.
- java_trace:
  - java.generated.project/src/main/java/chemical_detector/controller/GasAnalysisController.java:67-69 `step` (CD-GA-Beh3)

### 9. [ambiguous_requirement] Peak intensity and location span all chemicals

- raw: CD-Fn2 and CD-Fn3 take the maximum over the whole reading, not just the target chemical. A strong non-target value in a gasD reading can therefore trigger stop or steer the robot. This is implemented as written.
- fix_directive: Filter by the target chemical if that was intended.
- java_trace:
  - java.generated.project/src/main/java/chemical_detector/sensor/GasSensorArray.java:52-84 `intensity` (CD-Fn2, CD-Fn3)

### 10. [invented_default] Constant values

- raw: THR=10.0, LV=1.0, EVADE_TIME=1, STUCK_PERIOD=3, STUCK_DIST=1.0, OUT_PERIOD=2. All are whole numbers, so the constants interface does not round them. STUCK_PERIOD > EVADE_TIME, so both parts of the progress guard can hold.
- fix_directive: Replace with the real configuration values.
- java_trace:
  - java.generated.project/src/main/java/chemical_detector/ChemicalDetectorConstants.java:12-27 `ChemicalDetectorConstants` (CD-Const1, CD-Const2, CD-Const3, CD-Const4, CD-Const5, CD-Const6)

### 11. [invented_default] Initial values of state variables

- raw: gs is initially empty, as specified. Invented: sts=noGas, ins=0.0, anl=Front, a=Front, l=front, d0=d1=0.0, evasionStart=0. Each is assigned before it is used.
- fix_directive: None.
- java_trace:
  - java.generated.project/src/main/java/chemical_detector/controller/GasAnalysisController.java:25-28 `sts` (CD-GA-Var1, CD-GA-Var2, CD-GA-Var3, CD-GA-Var4)
  - java.generated.project/src/main/java/chemical_detector/controller/MovementController.java:26-30 `a` (CD-MV-Var1, CD-MV-Var2, CD-MV-Var3, CD-MV-Var4, CD-MV-Clock1)

### 12. [invented_default] Meaning of analysis, angle(x) and empty readings

- raw: analysis returns gasD iff some value has the target chemical (passed to GasSensorArray's constructor) with i > 0. Position k maps to the k-th Angle in listed order (Left, Right, Back, Front), wrapping round, and ties go to the first position. On an empty reading, intensity returns 0.0 and location returns Front. This is unreachable, because gasD implies a non-empty reading.
- fix_directive: Replace with the real sensor geometry and detection rule.
- java_trace:
  - java.generated.project/src/main/java/chemical_detector/sensor/GasSensorArray.java:35-100 `GasSensorArray` (CD-Fn1, CD-Fn2, CD-Fn3)

## next_step

Run the pipeline; check how the extractor handles the shared events, the bare GAS_ABSENT transition and the Vehicle operations.
