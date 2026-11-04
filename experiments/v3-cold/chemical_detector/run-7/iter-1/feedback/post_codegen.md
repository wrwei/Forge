# post_codegen

**status:** uncertain

**summary:** Two controllers (GasAnalysisController, MovementController) implement all 81 requirements; 18 Java files compile. 12 points the requirements did not settle are listed below (3 invented defaults, 1 ambiguity, 7 design choices, 1 scope question); none is unimplemented.

## Issues

### 1. [invented_default] Values of the six configuration constants

- **raw:** CD-Const1..6 say thr, lv, evadeTime, stuckPeriod, stuckDist and outPeriod are configured at startup but give no values.
- **fix_directive:** Placeholders chosen, all integer-valued so the model's rounding-up of fractional constants changes nothing: THR=2.0, LV=1.0, EVADE_TIME=1, STUCK_PERIOD=3, STUCK_DIST=1.0, OUT_PERIOD=2 (small, to keep bounded model checking tractable). Replace with deployment values if known.
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/DetectorConstants.java` lines 12-27, DetectorConstants — CD-Const1, CD-Const2, CD-Const3, CD-Const4, CD-Const5, CD-Const6

### 2. [invented_default] Initial values of the state variables

- **raw:** Only gs has a stated initial value (empty). sts, ins, anl, a, d0, d1 and l have none.
- **fix_directive:** Chosen: sts=noGas, ins=0.0, anl=Front, a=Front, d0=d1=0.0, l=front, evasion clock start=0. Each is overwritten before it is read on every path.
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/controller/GasAnalysisController.java` lines 22-25, gs, sts, ins, anl — CD-GA-Var1, CD-GA-Var2, CD-GA-Var3, CD-GA-Var4
  - `java.generated.project/src/main/java/chemical_detector/controller/MovementController.java` lines 20-24, a, d0, d1, l, evasionStart — CD-MV-Var1, CD-MV-Var2, CD-MV-Var3, CD-MV-Var4, CD-MV-Clock1

### 3. [invented_default] angle(x) mapping and empty-reading results of intensity/location

- **raw:** CD-DM7/CD-Fn3 say list position maps to a sensing direction via angle(x) but do not give the map; CD-Fn2/CD-Fn3 have a non-empty precondition with no defined result otherwise.
- **fix_directive:** Positions 1,2,3,4 map to Left, Right, Back, Front (enum order), repeating for longer arrays. location returns the first index of maximum intensity. On an empty reading intensity returns 0.0 and location returns Front; neither is reachable from the controller, which calls them only after analysis returned gasD (so the reading is non-empty).
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/sensor/GasSensorArray.java` lines 36-82, intensity, location, angleAt — CD-Fn2, CD-Fn3, CD-DM7

### 4. [ambiguous_requirement] What 'a reading indicates the target chemical' means

- **raw:** CD-Fn1: analysis returns gasD if some reading indicates the target chemical; the target and the test are not defined. CD-Fn2 takes the maximum over all samples, whatever their chemical.
- **fix_directive:** The target Chem is configuration of GasSensorArray (constructor). A sample indicates it when its chemical id equals the target's and its intensity is > 0. intensity/location follow CD-Fn2/CD-Fn3 literally (all samples), so a stronger non-target sample can win the steering direction.
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/sensor/GasSensorArray.java` lines 18-32, GasSensorArray, analysis — CD-Fn1, CD-Fn2, CD-DM4

### 5. [design_choice] Concrete Java types for Chem, Intensity, GasSensor and GasSensorReading

- **raw:** CD-DM4/CD-DM5 describe Chem and Intensity as abstract types; CD-DM6 names the record GasSensor.
- **fix_directive:** Chem is a record with a nat id (equality only). Intensity is real (double), so thr can be a numeric constant and goreq is >=. The record is named GasMeasurement (fields c, i) because classes whose name contains 'sensor' are treated as sensor services. GasSensorReading is List<GasMeasurement> (a sequence).
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/domain/Chem.java` lines 6-7, Chem — CD-DM4
  - `java.generated.project/src/main/java/chemical_detector/domain/GasMeasurement.java` lines 9-10, GasMeasurement — CD-DM5, CD-DM6, CD-DM7

### 6. [design_choice] Operations move, randomWalk, shortRandomWalk, changeDirection as Vehicle actuator methods

- **raw:** CD-OP1..4 are type 'operation'. The profile's operation shape (a class with a parameterless compute() of this.field = expr) cannot take move's (lv, a) or changeDirection's l, which are controller variables, and these are platform commands, not computations.
- **fix_directive:** Implemented as methods of VehicleActuator, called from the controller's actions; no operation-package classes. changeDirection(l) implements left->Right, right->Left, front->Back at LV via move. If an operation class per requirement is mandatory, a parameterised form is needed.
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/actuator/VehicleActuator.java` lines 19-48, move, randomWalk, shortRandomWalk, changeDirection — CD-OP1, CD-OP2, CD-OP3, CD-OP4

### 7. [design_choice] Waiting's during action randomWalk() realised as an entry action

- **raw:** CD-MV-FR1 gives Waiting a during action randomWalk(). The step() profile has no during construct, and a bare else would be an eventless self-loop (divergence).
- **fix_directive:** Every branch into Waiting calls randomWalk(), and the constructor calls it for the initial entry. randomWalk is unbounded and persists until a move supersedes it, so the behaviour matches the during action.
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/controller/MovementController.java` lines 31-63, MovementController, step — CD-MV-FR1, CD-MV-Beh1, CD-MV-Beh3

### 8. [design_choice] Unguarded autonomous transitions and final states

- **raw:** CD-GA-Beh3 (NoGas->Reading) has no trigger and no guard; CD-GA-Beh6 and CD-MV-Beh9 go to a final state; CD-DC1 wants one transition per behaviour requirement.
- **fix_directive:** Unguarded transitions are written with a constant-true predicate alwaysEnabled. GA's final state is CONCLUDED; MV gets FOUND -> HALTED (final) so CD-MV-Beh9 is a transition. Neither forms an eventless cycle: Reading waits for gas, HALTED and CONCLUDED are terminal.
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/controller/GasAnalysisController.java` lines 64-81, step — CD-GA-Beh3, CD-GA-Beh6, CD-DC1
  - `java.generated.project/src/main/java/chemical_detector/controller/MovementController.java` lines 88-91, step — CD-MV-Beh9, CD-DC1

### 9. [design_choice] Odometer as a sensor quantity, not an input event

- **raw:** CD-Evt3 is typed 'event' but describes the movement subsystem sampling the distance into d0/d1 inside entry/transition actions.
- **fix_directive:** VehicleSensor.odometer() is read where d0 (Avoiding entry) and d1 (TryingAgain->AvoidingAgain) are recorded; no Odometer event record exists.
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/controller/MovementController.java` lines 80-122, step — CD-Evt3, CD-MV-Var2, CD-MV-Var3

### 10. [design_choice] Mode names differ from the requirement state names

- **raw:** State Analysis and function analysis, state NoGas and Status value noGas differ only in case.
- **fix_directive:** Modes are READING, ANALYSING, NO_GAS, GAS_DETECTED, CONCLUDED and WAITING, GOING, FOUND, AVOIDING, TRYING_AGAIN, AVOIDING_AGAIN, GETTING_OUT, HALTED, so no model name clashes with another.
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/controller/GasAnalysisMode.java` lines 4-10, GasAnalysisMode — CD-GA-FR1, CD-GA-FR2, CD-GA-FR3, CD-GA-FR4
  - `java.generated.project/src/main/java/chemical_detector/controller/MovementMode.java` lines 4-13, MovementMode — CD-MV-FR1, CD-MV-FR2, CD-MV-FR3, CD-MV-FR4, CD-MV-FR5, CD-MV-FR6, CD-MV-FR7

### 11. [design_choice] Evasion clock and waits

- **raw:** CD-MV-Clock1 restarts the evasion timing on each entry to Avoiding; CD-Const3/CD-Const6 are waits in Avoiding and GettingOut entry.
- **fix_directive:** evasionStart = timeSource.now() on every entry to Avoiding (from Going and from AvoidingAgain); stuck test since(evasionStart) < STUCK_PERIOD || d1 - d0 > STUCK_DIST and its negation. Waits are Clock.delay(n), which advances logical time by n.
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/controller/MovementController.java` lines 45-147, step — CD-MV-Clock1, CD-Const3, CD-Const4, CD-Const5, CD-Const6, CD-MV-Beh17, CD-MV-Beh18

### 12. [scope_question] Delivery of gas-analysis events to the movement subsystem

- **raw:** CD-ARCH2: the subsystems communicate only via turn, stop and resume.
- **fix_directive:** GasAnalysisController emits OutputEvent.Turn/Stop/Resume through CommandActuator; MovementController consumes the same-named InputEvent records. No wiring class was written: forwarding is left to the platform harness (and to the model's module).
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/actuator/CommandActuator.java` lines 6-19, CommandActuator — CD-ARCH2, CD-Evt4, CD-Evt5, CD-Evt6

## next_step

Run the pipeline (compile, coverage, profile check, transformations, FDR4, Dafny, Isabelle, vacuity).
