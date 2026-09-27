# post_codegen — iteration 1

**Status:** uncertain

Iteration 1 cold codegen: 19 Java files, two controllers (GasAnalysisController, MovementController). Every requirement is traced. The issues below are design choices and invented defaults for human review; no requirement is unimplemented.

## 1. [design_choice] Terminal behaviour modelled as ordinary states, not RoboChart final states

- **Raw:** CD-GA-Beh6 says gas analysis 'concludes its search, processing no further readings'; CD-MV-Beh9 says Movement 'stays halted' in Found. The codegen rules require every mode to have an outgoing transition.
- **Resolution / fix directive:** Gas analysis enters mode Concluded, whose only transition is a gas-triggered self-loop that just retains the reading in gs (CD-GA-Var1) without analysing it. Found's only transition is an obstacle-triggered self-loop that records l (CD-MV-Var4) and issues no motion. Confirm this reading of 'concludes' / 'stays halted'.
- **Java:** `src/main/java/chemical_detector/controller/GasAnalysisController.java` L76-82 `step` — CD-GA-Beh6, CD-GA-FR4
- **Java:** `src/main/java/chemical_detector/controller/MovementController.java` L90-96 `step` — CD-MV-Beh9, CD-MV-FR3

## 2. [design_choice] Analysis guards written as sts == noGas and its negation

- **Raw:** CD-GA-FR3: both transitions leaving Analysis are guarded by sts.
- **Resolution / fix directive:** The gasD branch is guarded by !(sts == noGas), which is equivalent to sts == gasD because Status has exactly two values (CD-DM1).
- **Java:** `src/main/java/chemical_detector/controller/GasAnalysisController.java` L43-43 `step` — CD-GA-Beh4, CD-GA-Beh5

## 3. [invented_default] Chem representation and what counts as 'indicating the target chemical'

- **Raw:** CD-DM4 leaves Chem opaque; CD-Fn1 does not define when a reading indicates the target.
- **Resolution / fix directive:** Chem is an enum {TARGET, OTHER}. analysis returns gasD iff some entry has c == TARGET and i > 0.
- **Java:** `src/main/java/chemical_detector/sensor/GasSensorArray.java` L17-24 `analysis` — CD-DM4, CD-Fn1

## 4. [invented_default] angle(x): sensor position to direction mapping

- **Raw:** CD-Fn3/CD-DM7 reference angle(x) but never define it.
- **Resolution / fix directive:** Position 1->Front, 2->Right, 3->Back, 4->Left, repeating every four positions. Ties resolve to the lowest position. An empty reading yields Front.
- **Java:** `src/main/java/chemical_detector/sensor/GasSensorArray.java` L45-73 `location` — CD-Fn3, CD-DM7

## 5. [ambiguous_requirement] intensity/location range over every entry regardless of chemical

- **Raw:** CD-Fn2/CD-Fn3 take the maximum across the whole sequence, so a strong non-target chemical could steer the robot or trigger 'found'.
- **Resolution / fix directive:** Implemented literally, over all entries. Restricting the functions to target-chemical entries would need a spec change.
- **Java:** `src/main/java/chemical_detector/sensor/GasSensorArray.java` L28-51 `intensity` — CD-Fn2, CD-Fn3

## 6. [invented_default] Constant values and initial variable values

- **Raw:** CD-Const1..6 give no values; no initial values are given for a, l, anl, sts, ins, d0, d1, T.
- **Resolution / fix directive:** THR=2.0, LV=1.0, EVADE_TIME=1, STUCK_PERIOD=2, STUCK_DIST=1.0, OUT_PERIOD=1 (in clock units). Initial values: a=Front, anl=Front, l=front, sts=noGas, ins=0, d0=d1=0, T=0.
- **Java:** `src/main/java/chemical_detector/constants/DetectorConstants.java` L13-33 `DetectorConstants` — CD-Const1, CD-Const2, CD-Const3, CD-Const4, CD-Const5, CD-Const6

## 7. [design_choice] Odometer sampled as a sensor query; changeDirection placed on Vehicle

- **Raw:** CD-Evt3 is typed 'event' but describes sampling. CD-OP4 does not say which component provides changeDirection.
- **Resolution / fix directive:** d0 and d1 are assigned from OdometerSensor.odometer(). Vehicle.changeDirection(l) is layered on move(lv, ...).
- **Java:** `src/main/java/chemical_detector/controller/MovementController.java` L50-50 `step` — CD-Evt3, CD-MV-Var2, CD-MV-Var3, CD-OP4

## 8. [ambiguous_requirement] Obstacle events outside Going/TryingAgain/Found

- **Raw:** CD-MV-Var4 says l is updated on each obstacle event, but no transition accepts obstacle in Waiting, Avoiding, AvoidingAgain or GettingOut.
- **Resolution / fix directive:** Followed the transition list: obstacle is not accepted in those states, so l is not updated there.
- **Java:** `src/main/java/chemical_detector/controller/MovementController.java` L53-54 `step` — CD-MV-Var4

## 9. [design_choice] During action and autonomous cycles

- **Raw:** CD-MV-FR1: Waiting's during action is randomWalk().
- **Resolution / fix directive:** randomWalk() is the first statement of the Waiting block, so it runs on every cycle spent in Waiting. Autonomous transitions fire on step(null). ChemicalDetector settles both controllers and forwards turn/stop/resume from gas analysis to movement (CD-ARCH2).
- **Java:** `src/main/java/chemical_detector/controller/MovementController.java` L53-54 `step` — CD-MV-FR1
- **Java:** `src/main/java/chemical_detector/ChemicalDetector.java` L66-90 `ChemicalDetector` — CD-ARCH2

## Next step

Run the pipeline via scripts/run_claude_iteration.sh and act on the first failing phase.
