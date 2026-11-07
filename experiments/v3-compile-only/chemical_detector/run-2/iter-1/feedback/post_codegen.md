# post_codegen

**status:** uncertain

## Summary

Two controllers (GasAnalysisController, MovementController) implement all 81 requirements. The requirements leave constant values, the target-chemical test, the sensor-position-to-direction map, empty-reading results and initial values open; these are invented defaults. The platform operations, the Waiting during-action, the final states, the shared events and odometer sampling are design choices forced by the Java profile.

## Issues

### 1. [invented_default] Values of the six configuration constants and the clock tick

**raw:** No requirement gives a value for thr, lv, evadeTime, stuckPeriod, stuckDist or outPeriod. Chosen: THR = 2.0, LV = 1.0, EVADE_TIME = 1, STUCK_PERIOD = 2, STUCK_DIST = 1.0, OUT_PERIOD = 1; one clock tick is one second. All values are whole numbers, so rounding a fractional constant up to an integer in the model has no effect.

**fix_directive:** Replace with the deployment's configured values; keep them whole numbers or check the guards that use them (goreq(ins, THR), since(evasionStart) < STUCK_PERIOD, d1 - d0 > STUCK_DIST).

**java_trace:**

- `src/main/java/chemical_detector/ChemicalDetectorConstants.java` lines 12-27, `ChemicalDetectorConstants`: CD-Const1, CD-Const2, CD-Const3, CD-Const4, CD-Const5, CD-Const6
- `src/main/java/chemical_detector/timing/Clock.java` lines 8-9, `Clock`: CD-MV-Clock1

### 2. [invented_default] What makes a reading indicate the target chemical

**raw:** CD-Fn1 says analysis returns gasD if some reading indicates the target chemical, but does not say which chemical is the target or what 'indicates' means. Chosen: the target Chem is passed to GasReadingSensor's constructor; a value indicates it when c equals the target and i > 0.0. An empty reading gives noGas.

**fix_directive:** Confirm the criterion (for example, a minimum detection intensity instead of > 0).

**java_trace:**

- `src/main/java/chemical_detector/sensor/GasReadingSensor.java` lines 26-38, `analysis`: CD-Fn1, CD-DM4

### 3. [invented_default] Map from sensor position to Angle, and tie-breaking in location

**raw:** CD-DM7 says the 1-based position encodes a sensing direction and CD-Fn3 uses angle(x), but the map is not given. Chosen: position 1 Left, 2 Right, 3 Back, 4 Front, repeating every four positions (the order of the Angle enum). When several values share the peak intensity, the lowest position wins.

**fix_directive:** Replace with the physical layout of the gas-sensor array.

**java_trace:**

- `src/main/java/chemical_detector/sensor/GasReadingSensor.java` lines 58-91, `location`: CD-Fn3, CD-DM7

### 4. [invented_default] Results of intensity and location for an empty reading

**raw:** CD-Fn2 and CD-Fn3 have the precondition that the reading is non-empty. The controller only calls them after analysis returned gasD, which implies a non-empty reading, but the Java methods still need a result: intensity returns 0.0 and location returns Front.

**fix_directive:** None needed unless the precondition is to be enforced by an exception.

**java_trace:**

- `src/main/java/chemical_detector/sensor/GasReadingSensor.java` lines 40-70, `intensity`: CD-Fn2, CD-Fn3

### 5. [invented_default] Initial values of the state variables

**raw:** Only gs has a stated initial value (empty). Chosen: sts = noGas, ins = 0.0, anl = Front, a = Front, l = front, d0 = d1 = 0.0, evasionStart = 0. Each is overwritten before it is read on every path, except a, which keeps Front until the first turn.

**fix_directive:** None needed.

**java_trace:**

- `src/main/java/chemical_detector/controller/GasAnalysisController.java` lines 24-27, `GasAnalysisController`: CD-GA-Var1, CD-GA-Var2, CD-GA-Var3, CD-GA-Var4
- `src/main/java/chemical_detector/controller/MovementController.java` lines 26-30, `MovementController`: CD-MV-Var1, CD-MV-Var2, CD-MV-Var3, CD-MV-Var4, CD-MV-Clock1

### 6. [design_choice] Platform operations written as operation classes holding the latest command

**raw:** move, randomWalk and shortRandomWalk are provided by the Vehicle; changeDirection is local. Each is an operation class whose compute() takes the operation's arguments and assigns the command it issues: Move stores velocity and direction; RandomWalk and ShortRandomWalk store a request flag; ChangeDirection stores lv plus three booleans (Right iff l == left, Left iff l == right, Back iff l == front), which encode the steering rule without a conditional. The Vehicle reads these values. Halting is move(0.0, Front).

**fix_directive:** If the profile expects platform operations elsewhere (for example on an actuator), move the calls; the controller's transitions do not change.

**java_trace:**

- `src/main/java/chemical_detector/operation/Move.java` lines 17-20, `compute`: CD-OP1
- `src/main/java/chemical_detector/operation/RandomWalk.java` lines 12-14, `compute`: CD-OP2
- `src/main/java/chemical_detector/operation/ShortRandomWalk.java` lines 12-14, `compute`: CD-OP3
- `src/main/java/chemical_detector/operation/ChangeDirection.java` lines 20-25, `compute`: CD-OP4

### 7. [design_choice] Waiting's during action randomWalk() as the first statement of the Waiting block

**raw:** The profile describes entry actions (inline in each incoming branch) but no during actions. randomWalk() is called at the top of the Waiting block, before its transitions, so it runs on every cycle spent in Waiting, including the first.

**fix_directive:** If the extractor rejects the statement, call randomWalk on every transition into Waiting instead (the walk is unbounded, so an entry call is equivalent).

**java_trace:**

- `src/main/java/chemical_detector/controller/MovementController.java` lines 76-77, `step`: CD-MV-FR1, CD-OP2

### 8. [design_choice] Final states and unconditional transitions

**raw:** The final states of both machines are explicit terminal modes: Concluded (gas analysis, entered from GasDetected with stop) and Halted (movement, entered from Found). NoGas -> Reading (CD-GA-Beh3) and Found -> Halted (CD-MV-Beh9) have no trigger and no guard and are written as a bare mode assignment in the mode block. Neither is a self-loop, so no divergence.

**fix_directive:** If the extractor needs a guard, guard NoGas -> Reading with !readingIndicatesGas (always true in NoGas).

**java_trace:**

- `src/main/java/chemical_detector/controller/GasAnalysisController.java` lines 71-86, `step`: CD-GA-Beh3, CD-GA-Beh6
- `src/main/java/chemical_detector/controller/MovementController.java` lines 113-114, `step`: CD-MV-Beh9, CD-MV-FR3

### 9. [design_choice] turn, stop and resume declared as both output and input records

**raw:** The gas-analysis controller sends turn, stop and resume (OutputEvent.Turn/Stop/Resume through Actuator.apply) and the movement controller receives them (InputEvent.Turn/Stop/Resume). The records share their names so that each pair becomes one shared event. flag is output only; gas and obstacle are input only.

**fix_directive:** None, unless the pipeline reports a name clash between the paired records.

**java_trace:**

- `src/main/java/chemical_detector/event/OutputEvent.java` lines 12-21, `OutputEvent`: CD-ARCH2, CD-Evt4, CD-Evt5, CD-Evt6, CD-Evt7
- `src/main/java/chemical_detector/event/InputEvent.java` lines 26-32, `InputEvent`: CD-Evt4, CD-Evt5, CD-Evt6

### 10. [design_choice] Odometer sampled through a sensor

**raw:** In the profile an action cannot wait for an event, so d0 (entry to Avoiding) and d1 (TryingAgain -> AvoidingAgain) are sampled from DistanceSensor.distanceTravelled(). The Odometer event updates DistanceSensor, not the controller.

**fix_directive:** None needed.

**java_trace:**

- `src/main/java/chemical_detector/sensor/DistanceSensor.java` lines 14-23, `DistanceSensor`: CD-Evt3, CD-MV-Var2, CD-MV-Var3

### 11. [ambiguous_requirement] Peak intensity is taken over all chemicals

**raw:** CD-Fn2 defines intensity as the maximum i across the whole reading, and CD-Fn3 steers towards it. Taken literally, a reading with the target present but another chemical stronger compares the other chemical's intensity with thr. The literal definition is implemented.

**fix_directive:** If only the target chemical's values should count, restrict intensity and location to values with c equal to the target.

**java_trace:**

- `src/main/java/chemical_detector/sensor/GasReadingSensor.java` lines 40-56, `intensity`: CD-Fn2, CD-Fn3, CD-GA-FR4

### 12. [design_choice] Transition priority within a state

**raw:** The requirements give no priorities. stop is the first branch in every movement state; in AvoidingAgain, stop and resume come before the two autonomous branches (making progress -> Avoiding, otherwise -> GettingOut), and the second autonomous branch is the negation of the first.

**fix_directive:** None needed; in the model the branches are alternatives.

**java_trace:**

- `src/main/java/chemical_detector/controller/MovementController.java` lines 149-166, `step`: CD-MV-FR6, CD-MV-Beh17, CD-MV-Beh18, CD-MV-Beh19, CD-MV-Beh20

### 13. [design_choice] Representation of Intensity and Chem

**raw:** Intensity is a real (double with @RoboChartType("real")), so thr is a numeric constant and goreq is >=. Chem is a record with one nat field, species, used only through equality.

**fix_directive:** None needed.

**java_trace:**

- `src/main/java/chemical_detector/domain/GasSensor.java` lines 11-12, `GasSensor`: CD-DM5, CD-DM6
- `src/main/java/chemical_detector/domain/Chem.java` lines 9-10, `Chem`: CD-DM4

## Next step

Run the pipeline (compile, coverage, profile check, transformations, FDR4, Dafny, Isabelle, vacuity) and fix the Java from the phase reports.
