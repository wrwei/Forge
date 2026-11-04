# post_codegen

**status**: uncertain

**summary**: All 81 requirements implemented in two controllers (gas analysis, movement), 18 files. Nothing unimplemented; the issues below are defaults and choices the requirements did not settle.

## Issues

### 1. [invented_default] Values of the six configuration constants

- **raw**: CD-Const1..6 name thr, lv, evadeTime, stuckPeriod, stuckDist, outPeriod but give no values.
- **fix_directive**: THR=2.0, LV=1.0, EVADE_TIME=1, STUCK_PERIOD=3, STUCK_DIST=1.0, OUT_PERIOD=2. Small and integer-valued, so the model's constant rounding does not change them; STUCK_PERIOD > EVADE_TIME so both outcomes of AvoidingAgain are reachable. Replace with deployment values.
- **java_trace**: `java.generated.project/src/main/java/chemical_detector/DetectorConstants.java` lines 13-28, `DetectorConstants`, requirements CD-Const1, CD-Const2, CD-Const3, CD-Const4, CD-Const5, CD-Const6

### 2. [invented_default] Sensing direction of each reading position (angle(x))

- **raw**: CD-Fn3 uses angle(x) and CD-DM7 says the 1-based position maps to a sensing direction, but the mapping is not given.
- **fix_directive**: Position 1 -> Front, 2 -> Right, 3 -> Back, 4 -> Left, repeating; on a tie the first position wins.
- **java_trace**: `java.generated.project/src/main/java/chemical_detector/sensor/GasSensorArray.java` lines 73-107, `location`, requirements CD-Fn3, CD-DM7

### 3. [invented_default] intensity and location on an empty reading

- **raw**: CD-Fn2 and CD-Fn3 have the precondition 'non-empty'; Java functions must return something.
- **fix_directive**: Empty reading: intensity 0.0, location Front. Unreachable from GasDetected, since gasD implies a non-empty reading.
- **java_trace**: `java.generated.project/src/main/java/chemical_detector/sensor/GasSensorArray.java` lines 52-80, `intensity`, requirements CD-Fn2, CD-Fn3

### 4. [invented_default] Initial values of state variables

- **raw**: Only gs has a stated initial value (empty).
- **fix_directive**: sts=noGas, ins=0.0, anl=Front, a=Front, l=front, d0=d1=0.0, evasion clock start=0. Each is written before it is read on every path.
- **java_trace**: `java.generated.project/src/main/java/chemical_detector/controller/GasAnalysisController.java` lines 25-28, `GasAnalysisController`, requirements CD-GA-Var1, CD-GA-Var2, CD-GA-Var3, CD-GA-Var4
- **java_trace**: `java.generated.project/src/main/java/chemical_detector/controller/MovementController.java` lines 25-29, `MovementController`, requirements CD-MV-Var1, CD-MV-Var2, CD-MV-Var3, CD-MV-Var4, CD-MV-Clock1

### 5. [ambiguous_requirement] What 'indicates the target chemical' means, and which chemical is the target

- **raw**: CD-Fn1: noGas 'if no reading indicates the target chemical'. No requirement names the target chemical.
- **fix_directive**: The target Chem is given to GasSensorArray at startup; a value indicates it when c equals the target and i > 0.
- **java_trace**: `java.generated.project/src/main/java/chemical_detector/sensor/GasSensorArray.java` lines 34-41, `analysis`, requirements CD-Fn1, CD-DM4

### 6. [design_choice] Final states as terminal modes

- **raw**: CD-GA-Beh6 ends gas analysis in a final state not listed among its states; CD-MV-Beh9 keeps Movement halted in Found.
- **fix_directive**: Gas analysis gets a terminal mode CONCLUDED; Movement's FOUND is terminal. Neither has outgoing transitions.
- **java_trace**: `java.generated.project/src/main/java/chemical_detector/controller/GasAnalysisController.java` lines 79-91, `step`, requirements CD-GA-Beh6
- **java_trace**: `java.generated.project/src/main/java/chemical_detector/controller/MovementController.java` lines 103-104, `step`, requirements CD-MV-Beh9, CD-MV-FR3

### 7. [design_choice] Waiting's during action randomWalk() issued on entry

- **raw**: CD-MV-FR1: 'The during action is randomWalk()'. The Java profile has no during construct.
- **fix_directive**: randomWalk() is issued on every entry to WAITING (each resume branch) and, for the initial entry, in the constructor. The walk is unbounded until superseded, so starting it on entry has the same effect.
- **java_trace**: `java.generated.project/src/main/java/chemical_detector/controller/MovementController.java` lines 43-168, `step`, requirements CD-MV-FR1, CD-MV-Beh3, CD-OP2

### 8. [design_choice] Platform operations as Vehicle actuator methods, not operation classes

- **raw**: CD-OP1..4 are of type operation; the profile's operation class has a parameterless compute() of assignments that returns results through getters.
- **fix_directive**: move(lv, a), randomWalk(), shortRandomWalk() and changeDirection(l) are parameterised commands with no result, so they are methods of VehicleActuator, called in the entry actions. changeDirection maps left->Right, right->Left, front->Back at LV.
- **java_trace**: `java.generated.project/src/main/java/chemical_detector/actuator/VehicleActuator.java` lines 28-61, `VehicleActuator`, requirements CD-OP1, CD-OP2, CD-OP3, CD-OP4

### 9. [design_choice] Odometer as a sensor read

- **raw**: CD-Evt3 is typed event, but the movement subsystem only samples the distance (into d0 or d1); it never triggers a transition.
- **fix_directive**: OdometerSensor.odometer() is read in the Avoiding entry (d0) and on TryingAgain -> AvoidingAgain (d1).
- **java_trace**: `java.generated.project/src/main/java/chemical_detector/controller/MovementController.java` lines 97-138, `step`, requirements CD-Evt3, CD-MV-Var2, CD-MV-Var3

### 10. [design_choice] Names and representations

- **raw**: State 'Analysis' and function 'analysis' differ only in case; Intensity and Chem are abstract types.
- **fix_directive**: Analysis state is mode ANALYSING. Intensity is real (double), ordered by goreq. Chem is a record with a nat species id, compared by equality.
- **java_trace**: `java.generated.project/src/main/java/chemical_detector/controller/GasAnalysisMode.java` lines 1-16, `ANALYSING`, requirements CD-GA-FR3
- **java_trace**: `java.generated.project/src/main/java/chemical_detector/domain/Chem.java` lines 1-12, `Chem`, requirements CD-DM4, CD-DM5

### 11. [scope_question] Delivery of turn/stop/resume between the two controllers

- **raw**: CD-ARCH2: the reasoning subsystems communicate only via turn, stop and resume.
- **fix_directive**: Gas analysis sends them through AnalysisActuator (OutputEvent.Turn/Stop/Resume); Movement receives InputEvent.Turn/Stop/Resume. No Java wiring class forwards one to the other: delivery is left to the deployment harness, and in the model the module connects the shared events.
- **java_trace**: `java.generated.project/src/main/java/chemical_detector/actuator/AnalysisActuator.java` lines 1-44, `AnalysisActuator`, requirements CD-ARCH2, CD-Evt4, CD-Evt5, CD-Evt6

## next_step

Run the pipeline; revisit these choices only if a phase or the operator objects.
