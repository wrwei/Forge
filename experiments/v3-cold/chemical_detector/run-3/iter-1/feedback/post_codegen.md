# post_codegen

**status:** uncertain

**summary:** All 81 requirements are implemented by 18 Java files (two controllers: gas analysis and movement). Below: 5 invented defaults, 2 ambiguities, 8 design choices and 1 scope question. Nothing is unimplemented.

## Issues

### 1. [invented_default] Constant values are not given by the requirements

**raw:** CD-Const1..6 say the constants are configured at start-up but give no values. Chosen: THR = 2.0, LV = 1.0, EVADE_TIME = 1, STUCK_PERIOD = 3, STUCK_DIST = 1.0, OUT_PERIOD = 2 (times in clock ticks). All are integer-valued so that rounding fractional constants up in the model changes nothing; STUCK_PERIOD > EVADE_TIME so that both outcomes of stuck detection stay reachable.

**fix_directive:** Replace the values in DetectorConstants if the system's real configuration is known; keep them integer-valued or note any guard whose meaning depends on a fraction.

**java_trace:**

- `java.generated.project/src/main/java/chemical_detector/controller/DetectorConstants.java` lines 12-27, `DetectorConstants`; requirement_ids: CD-Const1, CD-Const2, CD-Const3, CD-Const4, CD-Const5, CD-Const6

### 2. [invented_default] Initial values of state variables other than gs

**raw:** Only gs has a specified initial value (empty, CD-GA-Var1). Chosen: sts = noGas, ins = 0.0, anl = Front, a = Front, l = front, d0 = d1 = 0.0, evasionStart = 0. Each is overwritten before it is read on every path, so the choice affects no behaviour.

**fix_directive:** Change the field initialisers if particular initial values are required.

**java_trace:**

- `java.generated.project/src/main/java/chemical_detector/controller/GasAnalysisController.java` lines 26-29, `sts, ins, anl`; requirement_ids: CD-GA-Var2, CD-GA-Var3, CD-GA-Var4
- `java.generated.project/src/main/java/chemical_detector/controller/MovementController.java` lines 27-31, `a, d0, d1, l, evasionStart`; requirement_ids: CD-MV-Var1, CD-MV-Var2, CD-MV-Var3, CD-MV-Var4, CD-MV-Clock1

### 3. [invented_default] Results of intensity and location on an empty reading

**raw:** CD-Fn2 and CD-Fn3 have the precondition that the reading is non-empty and say nothing for an empty one. Chosen: intensity returns 0.0 and location returns Front.

**fix_directive:** None needed unless an empty reading must be rejected instead.

**java_trace:**

- `java.generated.project/src/main/java/chemical_detector/sensor/GasSensorArray.java` lines 34-68, `intensity, location`; requirement_ids: CD-Fn2, CD-Fn3

### 4. [invented_default] What makes a reading 'indicate the target chemical'

**raw:** CD-Fn1 does not name the target chemical or say when a value indicates it. Chosen: the target Chem is passed to the GasSensorArray constructor; analysis returns gasD iff some value has c equal to the target and i > 0.0.

**fix_directive:** Adjust GasSensorArray.analysis if presence should use a different criterion, such as a detection floor.

**java_trace:**

- `java.generated.project/src/main/java/chemical_detector/sensor/GasSensorArray.java` lines 16-32, `GasSensorArray, analysis`; requirement_ids: CD-Fn1, CD-DM4

### 5. [invented_default] Mapping from reading position to direction (angle)

**raw:** CD-DM7 and CD-Fn3 say that a position (1-based) maps to a sensing direction but do not give the mapping. Chosen: positions 1, 2, 3, 4 map to Left, Right, Back, Front (declaration order of Angle), repeating for longer readings; a position below 1 maps to Front. When several positions share the peak intensity, location returns the first one.

**fix_directive:** Change GasSensorArray.angle to the real sensor layout.

**java_trace:**

- `java.generated.project/src/main/java/chemical_detector/sensor/GasSensorArray.java` lines 52-85, `location, angle`; requirement_ids: CD-Fn3, CD-DM7

### 6. [ambiguous_requirement] Peak intensity and its location are taken over all chemicals

**raw:** CD-Fn2 and CD-Fn3 define intensity and location over every value in the reading, while CD-Fn1 is about the target chemical only. If a reading also contains another chemical, the threshold check and the steering may be driven by that chemical. Implemented literally, over all values.

**fix_directive:** If only target-chemical values should count, restrict the loops in intensity and location to values whose c is the target.

**java_trace:**

- `java.generated.project/src/main/java/chemical_detector/sensor/GasSensorArray.java` lines 34-68, `intensity, location`; requirement_ids: CD-Fn2, CD-Fn3, CD-GA-FR4

### 7. [ambiguous_requirement] Non-empty precondition of intensity and location is not stated in the code

**raw:** GasDetected is entered only when analysis returned gasD, which in this Java needs a non-empty reading, so the preconditions of CD-Fn2 and CD-Fn3 always hold. The Java states no contract for this, so a model in which analysis is uninterpreted cannot see it.

**fix_directive:** None in the Java, unless a verifier reports the precondition as unproved.

**java_trace:**

- `java.generated.project/src/main/java/chemical_detector/controller/GasAnalysisController.java` lines 61-87, `step (Analysis and GasDetected blocks)`; requirement_ids: CD-Fn2, CD-Fn3, CD-GA-Beh5, CD-GA-Beh7

### 8. [design_choice] Intensity is represented as real, and Chem as a record

**raw:** CD-DM5 makes Intensity an abstract ordered type, but it must be compared with the numeric constant thr, so it is a double (real) throughout: GasSensor.i, ins, THR and goreq's parameters. Chem (CD-DM4) is a record with a nat id; only equality is used.

**fix_directive:** None needed.

**java_trace:**

- `java.generated.project/src/main/java/chemical_detector/domain/GasSensor.java` lines 1-12, `GasSensor`; requirement_ids: CD-DM5, CD-DM6
- `java.generated.project/src/main/java/chemical_detector/domain/Chem.java` lines 1-11, `Chem`; requirement_ids: CD-DM4

### 9. [design_choice] The odometer is a sensor quantity sampled in actions, not an input event

**raw:** CD-Evt3 says the movement subsystem samples the odometer into d0 or d1. Those samples happen inside actions whose trigger is already obstacle (or a guard), and a step can carry only one event, so the odometer is the zero-argument sensor method NavigationSensor.odometer().

**fix_directive:** None needed.

**java_trace:**

- `java.generated.project/src/main/java/chemical_detector/sensor/NavigationSensor.java` lines 12-21, `odometer, updateOdometer`; requirement_ids: CD-Evt3
- `java.generated.project/src/main/java/chemical_detector/controller/MovementController.java` lines 97-107, 152-158, 172-178, `step`; requirement_ids: CD-MV-Var2, CD-MV-Var3

### 10. [design_choice] Vehicle-provided operations are actuator methods, not operation classes

**raw:** CD-OP1..3 (move, randomWalk, shortRandomWalk) are typed 'operation', but CD-ARCH2 says the Vehicle provides them. They command the platform and compute no variables, and an operation class (argument-less compute() of field assignments) cannot carry move's arguments. They are methods of VehicleActuator, so there is no operation package.

**fix_directive:** If operation classes are required for these, say what variables they should compute.

**java_trace:**

- `java.generated.project/src/main/java/chemical_detector/actuator/VehicleActuator.java` lines 1-56, `move, randomWalk, shortRandomWalk`; requirement_ids: CD-OP1, CD-OP2, CD-OP3

### 11. [design_choice] changeDirection(l) is realised as move(lv, evasionAngle(l))

**raw:** CD-OP4 needs the obstacle side l, which a parameterless compute() cannot take, and its effect is one move at lv. The left/right/front to Right/Left/Back mapping is the pure function NavigationSensor.evasionAngle, and Avoiding's entry calls vehicle.move(LV, sensor.evasionAngle(l)).

**fix_directive:** None needed.

**java_trace:**

- `java.generated.project/src/main/java/chemical_detector/sensor/NavigationSensor.java` lines 23-35, `evasionAngle`; requirement_ids: CD-OP4
- `java.generated.project/src/main/java/chemical_detector/controller/MovementController.java` lines 97-107, 172-178, `step`; requirement_ids: CD-OP4, CD-MV-FR4

### 12. [design_choice] Waiting's during action randomWalk() is issued on entry

**raw:** The profile has no during actions. Because randomWalk is unbounded (it continues until superseded), it is issued on every transition into Waiting and once at start-up in the constructor (CD-MV-Beh1 has no action). The resume transitions' 'no action' (CD-MV-Beh3, 8, 12, 15, 20, 23) is respected in that the call is Waiting's own behaviour, written inline as the profile requires for target-state actions.

**fix_directive:** None needed.

**java_trace:**

- `java.generated.project/src/main/java/chemical_detector/controller/MovementController.java` lines 33-41, `MovementController (constructor)`; requirement_ids: CD-MV-FR1, CD-MV-Beh1
- `java.generated.project/src/main/java/chemical_detector/controller/MovementController.java` lines 53-210, `step`; requirement_ids: CD-MV-Beh3, CD-MV-Beh8, CD-MV-Beh12, CD-MV-Beh15, CD-MV-Beh20, CD-MV-Beh23

### 13. [design_choice] Final states and unguarded transitions

**raw:** GasDetected at or above thr goes to the terminal mode CONCLUDED (CD-GA-Beh6). Found goes on to a terminal mode HALTED by an unguarded transition, so that CD-MV-Beh9 has exactly one transition (CD-DC1). NoGas returns to Reading unguarded (CD-GA-Beh3). None of these is a self-loop, and every cycle through them contains an event.

**fix_directive:** None needed.

**java_trace:**

- `java.generated.project/src/main/java/chemical_detector/controller/GasAnalysisController.java` lines 73-94, `step`; requirement_ids: CD-GA-Beh3, CD-GA-Beh6
- `java.generated.project/src/main/java/chemical_detector/controller/MovementController.java` lines 109-111, 207-209, `step`; requirement_ids: CD-MV-Beh9, CD-MV-FR3, CD-DC1

### 14. [design_choice] Timed waits and the evasion clock

**raw:** wait(evadeTime) and wait(outPeriod) are Clock.delay calls. In Java the Clock is logical time advanced by advance() and by delay(). The evasion clock T is the field evasionStart, restarted on both transitions into Avoiding (CD-MV-Beh7, Beh17); stuck detection compares timer.now() - evasionStart with STUCK_PERIOD.

**fix_directive:** None needed.

**java_trace:**

- `java.generated.project/src/main/java/chemical_detector/sensor/Clock.java` lines 1-25, `Clock`; requirement_ids: CD-MV-Clock1, CD-Const3, CD-Const6
- `java.generated.project/src/main/java/chemical_detector/controller/MovementController.java` lines 53-57, 97-107, 172-185, `step`; requirement_ids: CD-MV-FR4, CD-MV-FR7, CD-MV-Beh17, CD-MV-Beh18

### 15. [design_choice] Order of Analysis's two guarded branches

**raw:** The requirements list noGas (CD-GA-Beh4) before gasD (CD-GA-Beh5). The code tests readingHasTarget first and then !readingHasTarget, so the second guard is the negation of the first. The guards are complementary, so the order does not change behaviour.

**fix_directive:** None needed.

**java_trace:**

- `java.generated.project/src/main/java/chemical_detector/controller/GasAnalysisController.java` lines 61-71, `step`; requirement_ids: CD-GA-Beh4, CD-GA-Beh5, CD-GA-FR3

### 16. [scope_question] Routing of turn, stop and resume between the subsystems

**raw:** Gas analysis emits OutputEvent.Turn, Stop and Resume through a SignalActuator; movement consumes InputEvent.Turn, Stop and Resume. The records share names so that they form the shared events of CD-ARCH2. No wiring class was written: forwarding emitted events into MovementController.step is left to the integrator, as is calling step(null) when no event occurred.

**fix_directive:** Add a system class that forwards events if a runnable composition is in scope.

**java_trace:**

- `java.generated.project/src/main/java/chemical_detector/actuator/SignalActuator.java` lines 1-22, `SignalActuator`; requirement_ids: CD-ARCH2, CD-Evt4, CD-Evt5, CD-Evt6, CD-Evt7

## next_step

Run the pipeline (compile, coverage, profile check, transformations, FDR4, Dafny, Isabelle, vacuity).
