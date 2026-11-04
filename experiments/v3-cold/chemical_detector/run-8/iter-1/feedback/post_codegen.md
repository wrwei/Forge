# post_codegen

**status:** uncertain

**summary:** Two controllers (GasAnalysisController, MovementController) in 18 Java files; javac (release 17) compiles them cleanly, and result_codegen.json traces all 81 requirements. Every transition in CD-GA-Beh1..7 and CD-MV-Beh1..23 is implemented. Ten points need review: three invented defaults, one ambiguous requirement, five design choices and one scope question.

## Issues

### 1. [invented_default] Constant values are not given by the requirements

- **raw:** CD-Const1..6 name thr, lv, evadeTime, stuckPeriod, stuckDist and outPeriod with their types, but give no values.
- **fix_directive:** Chosen: THR = 10.0, LV = 1.0, EVADE_TIME = 2, STUCK_PERIOD = 5, STUCK_DIST = 1.0, OUT_PERIOD = 3. All are integral, so the model's rounding-up of fractional constants changes nothing. Replace them with the deployment's configured values; keep them non-negative, and integral wherever the exact value matters to a guard.
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/DetectorConstants.java` lines 7-23, `DetectorConstants`: CD-Const1, CD-Const2, CD-Const3, CD-Const4, CD-Const5, CD-Const6

### 2. [invented_default] Initial values of state variables other than gs

- **raw:** CD-GA-Var1 says gs is initially empty; no initial value is given for sts, ins, anl, a, d0, d1, l or the evasion clock.
- **fix_directive:** Chosen: sts = noGas, ins = 0.0, anl = Front, a = Front, l = front, d0 = d1 = 0.0, evasionStart = 0. On every path each one is written before a transition reads it (sts on entry to Analysis, ins on entry to GasDetected, anl and a before they are sent or used, l and d0 on entry to Avoiding, d1 on entry to AvoidingAgain). No change is needed unless start-up values are specified.
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/controller/GasAnalysisController.java` lines 24-31, `sts, ins, anl`: CD-GA-Var2, CD-GA-Var3, CD-GA-Var4
  - `java.generated.project/src/main/java/chemical_detector/controller/MovementController.java` lines 21-30, `a, d0, d1, l, evasionStart`: CD-MV-Var1, CD-MV-Var2, CD-MV-Var3, CD-MV-Var4, CD-MV-Clock1

### 3. [invented_default] Empty readings and the position-to-direction map

- **raw:** CD-Fn2 and CD-Fn3 have a non-empty precondition but say nothing about an empty input. CD-DM7 says that the position in a reading encodes a sensing direction, but not which one.
- **fix_directive:** Chosen: for an empty reading, intensity returns 0.0 and location returns Front. The controller never reaches either case: GasDetected is entered only after analysis found a target value, so gs is non-empty there. Position p (1-based) maps to Left, Right, Back, Front in enum order, cycling for more than four sensors; on ties location takes the first strongest position, so gs[x].i == intensity(gs). Replace ChemicalSensor.angle with the real sensor geometry.
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/sensor/ChemicalSensor.java` lines 37-81, `intensity, location, angle`: CD-Fn2, CD-Fn3, CD-DM7

### 4. [ambiguous_requirement] What 'indicates the target chemical', and which values the peak covers

- **raw:** CD-Fn1: analysis returns noGas 'if no reading indicates the target chemical', but no target chemical is identified. CD-Fn2: intensity is the maximum 'across the sequence', which covers every chemical.
- **fix_directive:** Chosen: ChemicalSensor is constructed with the target Chem, and analysis returns gasD if and only if some value has c equal to the target and i > 0. intensity and location follow CD-Fn2 and CD-Fn3 literally, over all values whatever their chemical, so a strong non-target chemical in a reading that also contains the target could reach thr. If the peak should count only the target chemical, filter by c in intensity and location.
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/sensor/ChemicalSensor.java` lines 16-71, `ChemicalSensor, analysis, intensity, location`: CD-Fn1, CD-Fn2, CD-Fn3, CD-DM4, CD-GA-FR4

### 5. [design_choice] Operations realised as VehicleActuator methods, not operation classes

- **raw:** Rules section 5 implements an operation as a class with a parameterless compute(). move(lv, a) and changeDirection(l) take arguments that are controller state (a, l), and CD-OP1..3 are operations the Vehicle provides.
- **fix_directive:** Chosen: VehicleActuator.move, randomWalk, shortRandomWalk and changeDirection, called in actions as move(LV, a), move(0.0, Front) (halt), changeDirection(l), randomWalk() and shortRandomWalk(). wait(evadeTime) and wait(outPeriod) are VehicleActuator.pause(n). There is no operation package. If operation classes are mandatory, the profile needs a way to pass arguments to compute(); report that rather than dropping a and l.
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/actuator/VehicleActuator.java` lines 14-68, `move, randomWalk, shortRandomWalk, changeDirection, pause`: CD-OP1, CD-OP2, CD-OP3, CD-OP4, CD-Const3, CD-Const6

### 6. [design_choice] Waiting's during action randomWalk() is performed on entry to Waiting

- **raw:** CD-MV-FR1: the during action of Waiting is randomWalk(). The profile has no during actions, and an unconditioned branch that re-runs it every cycle would be a divergent internal step.
- **fix_directive:** Chosen: randomWalk() is issued on every transition into Waiting, including the resume self-loop, and once in the constructor for the initial entry. randomWalk is unbounded and lasts until a later command supersedes it, so the effect is the same as a during action. The initial call is in the constructor, so it may not appear in the extracted model.
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/controller/MovementController.java` lines 37-42, `MovementController (constructor)`: CD-MV-FR1, CD-MV-Beh1, CD-OP2
  - `java.generated.project/src/main/java/chemical_detector/controller/MovementController.java` lines 62-66, `step (Waiting, resume)`: CD-MV-Beh3

### 7. [design_choice] Odometer sampled as a sensor reading, not an input event

- **raw:** CD-Evt3 has type event, but its text says the movement subsystem 'samples this distance (into d0 or d1)'. Each step consumes one event, so an odometer input cannot be read inside an action.
- **fix_directive:** Chosen: OdometerSensor.odometer(), a zero-argument sensor method, is read into d0 on entry to Avoiding and into d1 on the TryingAgain to AvoidingAgain transition. There is no InputEvent.Odometer record, because no transition would ever consume it.
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/sensor/OdometerSensor.java` lines 1-19, `OdometerSensor.odometer`: CD-Evt3
  - `java.generated.project/src/main/java/chemical_detector/controller/MovementController.java` lines 95-102, `step (Going, obstacle)`: CD-MV-Var2, CD-MV-Beh7
  - `java.generated.project/src/main/java/chemical_detector/controller/MovementController.java` lines 148-153, `step (TryingAgain, obstacle)`: CD-MV-Var3, CD-MV-Beh16

### 8. [design_choice] Representation and naming of the data types

- **raw:** CD-DM6 names the record GasSensor. CD-DM5 Intensity and CD-DM4 Chem are opaque types, one ordered and one with equality only.
- **fix_directive:** Chosen: the record is named GasSample, because the profile treats any class whose name contains 'sensor' as a sensor service. Intensity is a real (double), compared only through goreq, so thr is a real constant. Chem is a record with a nat id, compared by equality. A reading is a List<GasSample> (a sequence).
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/domain/GasSample.java` lines 1-11, `GasSample`: CD-DM5, CD-DM6, CD-DM7
  - `java.generated.project/src/main/java/chemical_detector/domain/Chem.java` lines 1-7, `Chem`: CD-DM4

### 9. [design_choice] Final states: gas analysis ends in Concluded, movement stays in Found

- **raw:** CD-GA-Beh6 and CD-MV-Beh9 name transitions to a final state. CD-MV-Beh9 and CD-MV-FR3 say Found simply stays halted. CD-DC1 requires that each behavioural requirement maps to exactly one transition.
- **fix_directive:** Chosen: GasDetected goes to a terminal mode Concluded (CD-GA-Beh6). Found is itself terminal, so CD-MV-Beh9 is realised by Found having no outgoing transition and maps to no transition at all. It is the one behavioural requirement that does not map to exactly one transition (CD-DC1). A Found-to-Final transition would add an internal step and a mode that CD-MV-FR1..7 do not name.
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/controller/GasAnalysisController.java` lines 77-92, `step (GasDetected, Concluded)`: CD-GA-Beh6, CD-GA-FR4
  - `java.generated.project/src/main/java/chemical_detector/controller/MovementController.java` lines 105-106, `step (Found)`: CD-MV-Beh9, CD-MV-FR3, CD-DC1

### 10. [scope_question] Delivery of turn, stop and resume between the two controllers

- **raw:** CD-ARCH2: the two reasoning subsystems communicate only through turn, stop and resume.
- **fix_directive:** Chosen: gas analysis emits OutputEvent.Turn, Stop and Resume through GasAnalysisActuator, and movement receives the InputEvent records of the same names, which the module should join into shared events. No Java class wires the two controllers together: the integrating platform drains GasAnalysisActuator and passes each event to MovementController.step. No requirement asks for a system class, so none was written. Within each step, gas analysis takes one transition, so a reading needs up to three steps to be classified.
- **java_trace:**
  - `java.generated.project/src/main/java/chemical_detector/actuator/GasAnalysisActuator.java` lines 1-28, `GasAnalysisActuator`: CD-ARCH2, CD-Evt4, CD-Evt5, CD-Evt6

## next_step

Run the pipeline (compile, coverage, profile check, T2M, M2M, M2T, FDR4, Dafny, Isabelle, vacuity) and review the issues above.
