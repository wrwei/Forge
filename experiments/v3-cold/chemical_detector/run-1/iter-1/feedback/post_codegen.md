# post_codegen

**status:** uncertain

**summary:** Two controllers (GasAnalysisController: 5 modes, 7 transitions; MovementController: 8 modes, 23 transitions) over the Vehicle's sensors and operations; all 81 requirements traced, none unimplemented. 12 choices the requirements do not settle are listed below.

## Issues

### 1. Constant values are not given by the requirements

- **kind:** `invented_default`
- **raw:** CD-Const1..6 say thr, lv, evadeTime, stuckPeriod, stuckDist and outPeriod are configured at start-up but give no values.
- **fix_directive:** Chosen: THR = 5.0, LV = 1.0, EVADE_TIME = 1, STUCK_PERIOD = 2, STUCK_DIST = 2.0, OUT_PERIOD = 1. All are whole numbers, so the model's rounding of fractional constants does not change any guard. Replace them with the deployment's configuration if it is known.
- **java_trace:**
  - `src/main/java/chemical_detector/DetectorConstants.java` lines 12-31, `DetectorConstants`, requirements: CD-Const1, CD-Const2, CD-Const3, CD-Const4, CD-Const5, CD-Const6

### 2. Initial values of state variables

- **kind:** `invented_default`
- **raw:** Only gs has a stated initial value (empty). sts, ins, anl, a, l, d0, d1 and the evasion clock have none.
- **fix_directive:** Chosen: sts = noGas, ins = 0.0, anl = Front, a = Front, l = front, d0 = d1 = 0.0, evasionStart = 0. Each is overwritten before it is read on every path (sts in Analysis entry, ins in GasDetected entry, a by turn, l by obstacle, d0/d1/evasionStart on the way into Avoiding/AvoidingAgain).
- **java_trace:**
  - `src/main/java/chemical_detector/controller/GasAnalysisController.java` lines 25-28, `gs, sts, ins, anl`, requirements: CD-GA-Var1, CD-GA-Var2, CD-GA-Var3, CD-GA-Var4
  - `src/main/java/chemical_detector/controller/MovementController.java` lines 22-26, `a, d0, d1, l, evasionStart`, requirements: CD-MV-Var1, CD-MV-Var2, CD-MV-Var3, CD-MV-Var4, CD-MV-Clock1

### 3. intensity and location on an empty reading; direction of each sensor position

- **kind:** `invented_default`
- **raw:** CD-Fn2 and CD-Fn3 have the precondition that the reading is non-empty, and CD-Fn3 uses angle(x) without saying which direction each position has.
- **fix_directive:** For an empty reading intensity returns 0.0 and location returns Front (the controller never calls them with one: GasDetected is reached only after analysis found a target sample). Positions 1..4 map to Left, Right, Back, Front (the Angle declaration order) and the mapping repeats for longer readings. On a tie, the first position with the peak intensity wins.
- **java_trace:**
  - `src/main/java/chemical_detector/sensor/GasSensorArray.java` lines 38-61, `intensity, location`, requirements: CD-Fn2, CD-Fn3, CD-DM7
  - `src/main/java/chemical_detector/sensor/GasSensorArray.java` lines 70-86, `strongest, angle`, requirements: CD-Fn3

### 4. What 'indicates the target chemical' means, and which chemical is the target

- **kind:** `ambiguous_requirement`
- **raw:** CD-Fn1: analysis returns noGas if no reading indicates the target chemical. The target's identity is not given; CD-Fn2/CD-Fn3 take the peak over all samples without restricting to the target.
- **fix_directive:** A sample indicates the target if its chemical equals the target and its intensity is positive. The target is a constructor parameter of GasSensorArray. intensity and location take the peak over every sample, as CD-Fn2/CD-Fn3 state. If they should consider target samples only, change GasSensorArray.strongest.
- **java_trace:**
  - `src/main/java/chemical_detector/sensor/GasSensorArray.java` lines 25-36, `analysis`, requirements: CD-Fn1, CD-DM4

### 5. Intensity is a real; the GasSensor record is named GasSample

- **kind:** `design_choice`
- **raw:** CD-DM5 makes Intensity a totally ordered type and CD-Const1 makes thr an Intensity. CD-DM6 names the record GasSensor.
- **fix_directive:** Intensity is a double (real), so thr is a numeric constant and goreq compares two reals. The record is GasSample(Chem c, real i): a type whose name contains 'sensor' is read as a sensor service by the extractor. Chem is a record with a nat id (an opaque identity with equality only).
- **java_trace:**
  - `src/main/java/chemical_detector/domain/GasSample.java` lines 10-11, `GasSample`, requirements: CD-DM5, CD-DM6
  - `src/main/java/chemical_detector/domain/Chem.java` lines 8-9, `Chem`, requirements: CD-DM4
  - `src/main/java/chemical_detector/sensor/GasSensorArray.java` lines 66-68, `goreq`, requirements: CD-Fn4, CD-DM5

### 6. move, randomWalk, shortRandomWalk and changeDirection are actuator methods

- **kind:** `design_choice`
- **raw:** CD-OP1..4 are of type 'operation'. They actuate the Vehicle and take arguments, which the compute() operation-class shape (no arguments, assignments only) cannot express.
- **fix_directive:** They are methods of VehicleActuator, so the model sees calls to operations. changeDirection implements CD-OP4's steering in Java (left -> Right, right -> Left, front -> Back, at LV) by calling move. The model keeps it as an operation call without that body. Halting is move(HALT_VELOCITY = 0.0, Front).
- **java_trace:**
  - `src/main/java/chemical_detector/actuator/VehicleActuator.java` lines 20-56, `move, randomWalk, shortRandomWalk, changeDirection`, requirements: CD-OP1, CD-OP2, CD-OP3, CD-OP4

### 7. The odometer is a sensor method, not an input event

- **kind:** `design_choice`
- **raw:** CD-Evt3 types the odometer as an event; it is sampled inside the Avoiding entry and on the TryingAgain -> AvoidingAgain transition, not used as a trigger.
- **fix_directive:** OdometerSensor.odometer() is read in those actions (d0 = odometer, d1 = odometer) and becomes a sensor variable in the model.
- **java_trace:**
  - `src/main/java/chemical_detector/sensor/OdometerSensor.java` lines 8-26, `odometer`, requirements: CD-Evt3
  - `src/main/java/chemical_detector/controller/MovementController.java` lines 81-89, `step (Going -> Avoiding)`, requirements: CD-MV-Var2, CD-MV-Beh7
  - `src/main/java/chemical_detector/controller/MovementController.java` lines 122-127, `step (TryingAgain -> AvoidingAgain)`, requirements: CD-MV-Var3, CD-MV-Beh16

### 8. turn, stop and resume are declared as both OutputEvent and InputEvent records

- **kind:** `design_choice`
- **raw:** CD-ARCH2: the two reasoning subsystems communicate only via turn, stop and resume. Gas analysis sends them; movement receives them.
- **fix_directive:** The gas-analysis controller sends OutputEvent.Turn/Stop/Resume through CommandLink.apply; the movement controller receives InputEvent.Turn/Stop/Resume. The names are identical so each pair becomes one shared event. ChemicalDetector.cycle forwards them at run time. If the model shows them as unconnected events, this pairing is the cause.
- **java_trace:**
  - `src/main/java/chemical_detector/event/OutputEvent.java` lines 10-23, `OutputEvent`, requirements: CD-Evt4, CD-Evt5, CD-Evt6, CD-Evt7
  - `src/main/java/chemical_detector/event/InputEvent.java` lines 13-29, `InputEvent`, requirements: CD-Evt1, CD-Evt2, CD-Evt4, CD-Evt5, CD-Evt6
  - `src/main/java/chemical_detector/ChemicalDetector.java` lines 46-61, `cycle`, requirements: CD-ARCH1, CD-ARCH2

### 9. Waiting's during action randomWalk() runs on entry to Waiting

- **kind:** `design_choice`
- **raw:** CD-MV-FR1: the during action of Waiting is randomWalk(). The Java profile has no during actions, and an unguarded, event-free branch would diverge.
- **fix_directive:** randomWalk() is called on every transition into Waiting (the six resume transitions, CD-MV-Beh3/8/12/15/20/23, which themselves have no action). An unbounded random walk continues until a later move supersedes it, so this matches the during action, except at start-up: the initial entry into Waiting (CD-MV-Beh1, no action) issues no randomWalk.
- **java_trace:**
  - `src/main/java/chemical_detector/controller/MovementController.java` lines 53-66, `step (Waiting)`, requirements: CD-MV-FR1, CD-MV-Beh1, CD-MV-Beh3

### 10. Final states are terminal modes Concluded and Halted

- **kind:** `design_choice`
- **raw:** CD-GA-Beh6: gas analysis concludes; CD-MV-Beh9: Found -> final. CD-DC1 asks for one transition per behavioural requirement.
- **fix_directive:** Gas analysis goes to the terminal mode Concluded (emitting stop). Movement has an unconditional Found -> Halted transition, and Halted is terminal. NoGas -> Reading and Found -> Halted are written as mode blocks without an inner if (no trigger, no guard); neither is part of a cycle of internal steps.
- **java_trace:**
  - `src/main/java/chemical_detector/controller/GasAnalysisController.java` lines 68-83, `step (NoGas, Concluded)`, requirements: CD-GA-Beh3, CD-GA-Beh6
  - `src/main/java/chemical_detector/controller/MovementController.java` lines 91-92, `step (Found)`, requirements: CD-MV-Beh9, CD-MV-FR3

### 11. Entry actions inlined; evasion clock reset on the transitions into Avoiding

- **kind:** `design_choice`
- **raw:** CD-MV-Clock1/Beh7/Beh17: the evasion timing restarts on each fresh evasion. CD-MV-FR6 gives AvoidingAgain no entry action and no turn/obstacle transitions.
- **fix_directive:** Each branch into Avoiding resets evasionStart = timer.now() and then performs the Avoiding entry (d0 = odometer, changeDirection(l), pause(EVADE_TIME)). d1 is recorded on the TryingAgain -> AvoidingAgain transition. AvoidingAgain is transient: its guards makingProgress and !makingProgress are complementary. Obstacles in Waiting, Avoiding, AvoidingAgain and GettingOut are not handled, as the requirements give no transition for them.
- **java_trace:**
  - `src/main/java/chemical_detector/controller/MovementController.java` lines 49-51, `step (predicates)`, requirements: CD-MV-Clock1, CD-Const4, CD-Const5, CD-MV-FR6
  - `src/main/java/chemical_detector/controller/MovementController.java` lines 129-147, `step (AvoidingAgain)`, requirements: CD-MV-Beh17, CD-MV-Beh18, CD-MV-Beh19, CD-MV-Beh20

### 12. Wiring classes not named by the requirements

- **kind:** `scope_question`
- **raw:** ChemicalDetector (composition and one control cycle), CommandLink (gas analysis -> movement signals) and Clock (logical time with now/tick/pause) are not requirements of their own.
- **fix_directive:** They realise CD-ARCH1/CD-ARCH2 and CD-MV-Clock1 and are traced to them. Neither controller depends on ChemicalDetector.
- **java_trace:**
  - `src/main/java/chemical_detector/ChemicalDetector.java` lines 18-62, `ChemicalDetector`, requirements: CD-ARCH1, CD-ARCH2
  - `src/main/java/chemical_detector/actuator/CommandLink.java` lines 10-37, `CommandLink`, requirements: CD-ARCH2
  - `src/main/java/chemical_detector/timing/Clock.java` lines 9-34, `Clock`, requirements: CD-MV-Clock1

## next_step

Run the pipeline (compile, coverage, profile check, transformations, verifiers).
