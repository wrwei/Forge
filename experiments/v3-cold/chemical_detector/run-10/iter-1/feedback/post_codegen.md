# post_codegen

**status:** uncertain

**summary:** All 81 requirements are implemented by two controllers (GasAnalysisController, MovementController) and their sensors, actuators, events and data types; CD-DC1 holds (one branch per behavioural requirement, no duplicate state/trigger pair). The issues below record design choices, invented defaults and ambiguities; nothing is unimplemented.

## Issues

### 1. [design_choice] Operations move, randomWalk, shortRandomWalk and changeDirection live on actuators, not operation/compute() classes

- **raw:** CD-OP1..3 are provided by the Vehicle and take arguments supplied by the controller (move(lv, a)); CD-OP4 changeDirection(l) takes the obstacle side. The profile's operation pattern (a parameterless compute() fed only by sensors and constants) cannot receive the controller variables a and l, so the Vehicle operations are methods of VehicleActuator and changeDirection is SteeringActuator.changeDirection(l), which moves at lv to Right/Left/Back for an obstacle on the left/right/front.
- **fix_directive:** None unless a later phase requires operation-package classes; then add one class per operation in chemical_detector.operation and keep the actuator calls.
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/actuator/VehicleActuator.java` lines 20-41, `move, randomWalk, shortRandomWalk` (CD-OP1, CD-OP2, CD-OP3)
  - `java.generated.project/src/main/java/chemical_detector/actuator/SteeringActuator.java` lines 17-29, `changeDirection` (CD-OP4)

### 2. [design_choice] The (chemical, intensity) record is named GasSample, not GasSensor

- **raw:** CD-DM6 names the record GasSensor. The extractor treats any class whose name contains 'sensor' as a sensor service, so the data record is named GasSample; a reading (CD-DM7) is List<GasSample>.
- **fix_directive:** None.
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/domain/GasSample.java` lines 5-11, `GasSample` (CD-DM6, CD-DM7)

### 3. [design_choice] Intensity is real; Chem is a record with a nat species id; the target chemical is configured in GasSensorArray

- **raw:** CD-DM5 describes an ordered type Intensity and CD-Const1 a constant thr of that type; constants must be numeric, so Intensity is real (double) and goreq compares reals. CD-DM4 Chem is opaque with equality only: a record Chem(speciesId). The requirements name no target-chemical constant; the target is a constructor argument of GasSensorArray.
- **fix_directive:** None.
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/domain/GasSample.java` lines 10-10, `GasSample.i` (CD-DM5)
  - `java.generated.project/src/main/java/chemical_detector/domain/Chem.java` lines 5-7, `Chem` (CD-DM4)
  - `java.generated.project/src/main/java/chemical_detector/sensor/GasSensorArray.java` lines 15-18, `GasSensorArray(Chem)` (CD-Fn1)

### 4. [design_choice] The odometer is a sensor quantity sampled in actions, not an input event

- **raw:** CD-Evt3 says the movement subsystem samples the distance into d0 (entry to Avoiding) and d1 (TryingAgain to AvoidingAgain). A trigger cannot be sampled inside an action, so the odometer is the zero-argument sensor method odometer().
- **fix_directive:** None.
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/sensor/OdometerSensor.java` lines 15-19, `odometer` (CD-Evt3)
  - `java.generated.project/src/main/java/chemical_detector/controller/MovementController.java` lines 86-86, `d0 = sensor.odometer()` (CD-MV-Var2)
  - `java.generated.project/src/main/java/chemical_detector/controller/MovementController.java` lines 126-126, `d1 = sensor.odometer()` (CD-MV-Var3)

### 5. [design_choice] Waiting's during action randomWalk() is issued on every transition into Waiting

- **raw:** CD-MV-FR1 gives Waiting the during action randomWalk(); the profile has no during construct. randomWalk is a persistent motion command (it lasts until superseded), so every transition into Waiting calls it. The initial transition has no action (CD-MV-Beh1), so the Vehicle starts in a random walk (VehicleActuator initial state).
- **fix_directive:** None.
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/controller/MovementController.java` lines 58-60, `Waiting resume self-loop` (CD-MV-FR1, CD-MV-Beh3)
  - `java.generated.project/src/main/java/chemical_detector/actuator/VehicleActuator.java` lines 16-16, `randomWalking = true` (CD-OP2, CD-MV-Beh1)

### 6. [design_choice] NoGas to Reading is guarded by a constant-true predicate

- **raw:** CD-GA-Beh3 has no guard and no trigger; the profile forbids a transition with neither. The predicate readyForNextReading = true is semantically 'no guard' and keeps NoGas free of deadlock without relying on an invariant about sts.
- **fix_directive:** None.
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/controller/GasAnalysisController.java` lines 46-69, `readyForNextReading` (CD-GA-Beh3)

### 7. [design_choice] turn, stop and resume are OutputEvent records of the gas-analysis controller and same-named InputEvent records of the movement controller

- **raw:** CD-ARCH2: the subsystems communicate only through turn, stop and resume. The gas-analysis controller sends OutputEvent.Turn/Stop/Resume; the movement controller receives InputEvent.Turn/Stop/Resume; MovementLinkActuator delivers one to the other. In Java an event the movement controller does not accept in its current state (e.g. turn in AvoidingAgain) is dropped, where RoboChart would make the sender wait.
- **fix_directive:** None.
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/event/OutputEvent.java` lines 12-19, `Turn, Stop, Resume` (CD-Evt4, CD-Evt5, CD-Evt6)
  - `java.generated.project/src/main/java/chemical_detector/event/InputEvent.java` lines 20-27, `Turn, Stop, Resume` (CD-Evt4, CD-Evt5, CD-Evt6)
  - `java.generated.project/src/main/java/chemical_detector/actuator/MovementLinkActuator.java` lines 20-30, `apply` (CD-ARCH2)

### 8. [design_choice] Terminal states: SourceLocated (gas analysis) and Found (movement)

- **raw:** CD-GA-Beh6: the gas-analysis subsystem concludes after emitting stop; its final state is the mode SourceLocated, with no outgoing transition. CD-MV-Beh9: Found has no outgoing transition (flag and halt are its entry actions).
- **fix_directive:** None.
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/controller/GasAnalysisController.java` lines 71-83, `GasDetected / SourceLocated` (CD-GA-Beh6)
  - `java.generated.project/src/main/java/chemical_detector/controller/MovementController.java` lines 91-92, `Found` (CD-MV-FR3, CD-MV-Beh9)

### 9. [design_choice] Clock T is the field evasionStart, reset in every transition into Avoiding

- **raw:** CD-MV-Clock1 restarts the timing on entry to Avoiding; CD-MV-Beh7 and CD-MV-Beh17 restart it on the transition. Both transitions into Avoiding reset evasionStart, so the two readings agree. The Clock-typed field is named timer because 'clock' is reserved.
- **fix_directive:** None.
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/controller/MovementController.java` lines 48-50, `withinStuckPeriod, makingProgress` (CD-MV-Clock1, CD-MV-Beh17, CD-MV-Beh18)
  - `java.generated.project/src/main/java/chemical_detector/controller/MovementController.java` lines 85-85, `evasionStart = timer.now()` (CD-MV-Beh7)
  - `java.generated.project/src/main/java/chemical_detector/controller/MovementController.java` lines 139-139, `evasionStart = timer.now()` (CD-MV-Beh17)

### 10. [invented_default] Values of the six configuration constants

- **raw:** CD-Const1..6 give no values. Chosen: THR = 1.0, LV = 1.0, EVADE_TIME = 1, STUCK_PERIOD = 2, STUCK_DIST = 1.0, OUT_PERIOD = 1. All are whole numbers, so the model's rounding up of fractional constants changes nothing; STUCK_PERIOD > EVADE_TIME so both outcomes of AvoidingAgain are reachable; small values keep the timed model small.
- **fix_directive:** Replace with the deployment values when known.
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/controller/DetectorConstants.java` lines 11-27, `DetectorConstants` (CD-Const1, CD-Const2, CD-Const3, CD-Const4, CD-Const5, CD-Const6)

### 11. [invented_default] Initial values of sts, ins, anl, a, l, d0, d1

- **raw:** Only gs has a stated initial value (empty, CD-GA-Var1). Chosen: sts = noGas, ins = 0.0, anl = Front, a = Front, l = front, d0 = d1 = 0.0. None is read before it is assigned: each state that reads one is entered only through a transition that sets it.
- **fix_directive:** None.
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/controller/GasAnalysisController.java` lines 21-24, `gs, sts, ins, anl` (CD-GA-Var1, CD-GA-Var2, CD-GA-Var3, CD-GA-Var4)
  - `java.generated.project/src/main/java/chemical_detector/controller/MovementController.java` lines 20-24, `a, d0, d1, l, evasionStart` (CD-MV-Var1, CD-MV-Var2, CD-MV-Var3, CD-MV-Var4, CD-MV-Clock1)

### 12. [invented_default] intensity and location of an empty reading; pre/postconditions only documented

- **raw:** CD-Fn2 and CD-Fn3 require a non-empty reading. For an empty one the Java returns 0.0 and Front. This cannot happen at the call sites: GasDetected is entered only when analysis returned gasD, which needs at least one sample. The pre- and postconditions are stated in Javadoc; the profile gives no contract syntax.
- **fix_directive:** None.
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/sensor/GasSensorArray.java` lines 36-70, `intensity, location` (CD-Fn2, CD-Fn3)

### 13. [ambiguous_requirement] angle(x), the sensing direction of sensor position x, is not defined

- **raw:** CD-Fn3 and CD-DM7 say position x maps to a direction angle(x) but do not give the map. Chosen: position k is the k-th Angle value in declaration order (1 Left, 2 Right, 3 Back, 4 Front), repeating beyond four sensors; on a tie the lowest position wins.
- **fix_directive:** Replace angleOf with the platform's real sensor layout when known.
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/sensor/GasSensorArray.java` lines 55-82, `location, angleOf` (CD-Fn3, CD-DM7)

### 14. [ambiguous_requirement] What 'a reading indicates the target chemical' means

- **raw:** CD-Fn1 does not define it. Chosen: a sample whose chemical equals the configured target and whose intensity is positive. CD-Fn2, as written, takes the maximum over all samples, including those of other chemicals; implemented as written.
- **fix_directive:** None.
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/sensor/GasSensorArray.java` lines 20-34, `analysis` (CD-Fn1, CD-Fn2)

## next_step

Run the pipeline: compile, coverage, profile check, transformations, FDR4, Dafny, Isabelle, vacuity.
