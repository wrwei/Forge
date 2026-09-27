# post_codegen — chemical_detector

**Status:** uncertain

Cold codegen for chemical_detector: two controllers (GasAnalysisController, MovementController) plus Vehicle, Sensor, ChangeDirection and system wiring. All 81 requirements are traced. 11 review items: invented constants and defaults, terminal-mode handling, and underspecified gas functions.

## 1. [invented_default] Constant values are not given by the specification

- **Raw:** CD-Const1..6 say the constants are configured at startup but give no values.
- **Fix directive:** Confirm or replace THR=1.0, LV=1.0, EVADE_TIME=1, STUCK_PERIOD=2, STUCK_DIST=1.0, OUT_PERIOD=1 (time in Clock units). Small values were chosen deliberately to keep the formal model small.
- **Java:** `java.generated.project/src/main/java/chemical_detector/constants/Constants.java` L11-34 `Constants` — CD-Const1, CD-Const2, CD-Const3, CD-Const4, CD-Const5, CD-Const6

## 2. [design_choice] Gas analysis ends in a Concluded mode that discards later readings instead of a final state

- **Raw:** CD-GA-Beh6: after stop the subsystem 'concludes its search, processing no further readings'. The codegen rules require every mode to have an outgoing transition.
- **Fix directive:** Concluded takes each further gas event (updating gs) and stays in Concluded without analysing it or emitting anything. Review whether this counts as 'processing no further readings'.
- **Java:** `java.generated.project/src/main/java/chemical_detector/controller/GasAnalysisController.java` L79-86 `step` — CD-GA-Beh6, CD-GA-Var1

## 3. [design_choice] Found stays put on obstacle reports; flag and halt happen on the stop transitions

- **Raw:** CD-MV-Beh9/FR3: once in Found the subsystem stays halted. Found needs an outgoing transition, and flag must be emitted exactly once.
- **Fix directive:** Found's only transition is a self-loop on obstacle that records l and commands nothing. flag + move(0, Front) run on each of the six stop transitions, not on the self-loop, so the flag is not re-emitted.
- **Java:** `java.generated.project/src/main/java/chemical_detector/controller/MovementController.java` L97-104 `step` — CD-MV-Beh9, CD-MV-FR3, CD-Evt7

## 4. [ambiguous_requirement] Meaning of 'indicates the target chemical' and where the target identity comes from

- **Raw:** CD-Fn1 does not define 'indicates' and no requirement names the target chemical.
- **Fix directive:** analysis returns gasD iff some entry has c equal to the target Chem (a Sensor constructor argument set at startup) and i > 0.
- **Java:** `java.generated.project/src/main/java/chemical_detector/sensor/Sensor.java` L43-50 `analysis` — CD-Fn1

## 5. [ambiguous_requirement] intensity/location range over all chemicals, not only the target

- **Raw:** CD-Fn2/Fn3 define the peak over the whole sequence; CD-Fn1 is about the target chemical only.
- **Fix directive:** Implemented literally: peak over every entry. Restrict to target entries if a peak from another chemical should not steer or stop the robot.
- **Java:** `java.generated.project/src/main/java/chemical_detector/sensor/Sensor.java` L54-81 `intensity` — CD-Fn2, CD-Fn3

## 6. [invented_default] Sensor position to Angle mapping

- **Raw:** CD-DM7 says the position encodes a direction but not which.
- **Fix directive:** Position 1..4 map to Left, Right, Back, Front (Angle declaration order), wrapping for longer arrays. Empty-reading defaults: noGas, 0, Front.
- **Java:** `java.generated.project/src/main/java/chemical_detector/sensor/Sensor.java` L93-96 `angle` — CD-DM7, CD-Fn3

## 7. [design_choice] Odometer is a sampled sensor value, not a triggering event

- **Raw:** CD-Evt3 lists the odometer as an event but describes the movement subsystem as sampling it into d0/d1.
- **Fix directive:** Sensor.odometer() is read on entry to Avoiding (d0) and on TryingAgain -> AvoidingAgain (d1).
- **Java:** `java.generated.project/src/main/java/chemical_detector/sensor/Sensor.java` L35-37 `odometer` — CD-Evt3, CD-MV-Var2, CD-MV-Var3

## 8. [design_choice] Intensity is a real (double), Chem is a record with a nat id

- **Raw:** CD-DM4/DM5 describe opaque types. Java has no opaque ordered type.
- **Fix directive:** Intensity values are double and are compared through goreq (used in the threshold guard). Chem(int id) keeps equality-only use.
- **Java:** `java.generated.project/src/main/java/chemical_detector/controller/GasAnalysisController.java` L29-29 `ins` — CD-DM5, CD-Fn4, CD-GA-Var3

## 9. [design_choice] Vehicle operations live on Vehicle; ChangeDirection is an operation class without compute()

- **Raw:** CD-OP1..3 are provided by the Vehicle; CD-OP4 is steering logic that is conditional on l, so a compute() of plain assignments cannot express it.
- **Fix directive:** move/randomWalk/shortRandomWalk/pause are Vehicle methods; ChangeDirection.changeDirection(l) calls vehicle.move(LV, ...).
- **Java:** `java.generated.project/src/main/java/chemical_detector/controller/MovementController.java` L60-61 `step` — CD-OP1, CD-OP2, CD-OP3, CD-OP4, CD-MV-FR1

## 10. [invented_default] Initial values of state variables

- **Raw:** Only gs has a specified initial value (empty).
- **Fix directive:** sts=noGas, ins=0, anl=Front, a=Front, l=front, d0=d1=0, T=0.
- **Java:** `java.generated.project/src/main/java/chemical_detector/controller/MovementController.java` L30-37 `MovementController` — CD-MV-Var1, CD-MV-Var4, CD-MV-Clock1

## 11. [scope_question] AvoidingAgain has no entry behaviour

- **Raw:** CD-MV-FR6 lists no entry action; the robot is not steered away from the second obstacle until it returns to Avoiding.
- **Fix directive:** Nothing added beyond the requirements. The outgoing guards are exact complements (progress vs stuck), so the mode is always left on the next step.
- **Java:** `java.generated.project/src/main/java/chemical_detector/controller/MovementController.java` L138-156 `step` — CD-MV-FR6, CD-MV-Beh17, CD-MV-Beh18

## Next step

Run the pipeline (compile -> coverage -> preflight -> T2M -> M2M -> M2T -> verifiers) and review the design choices above.
