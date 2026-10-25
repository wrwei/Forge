# post_codegen — uncertain

Cold codegen for chemical_detector: two controllers (GasAnalysisController, MovementController) plus leaves; all 81 requirements traced. No requirement is unimplemented; the issues below are invented defaults and design choices for review.

## 1. [invented_default] Constant values are not given by the specification

**Raw:** CD-Const1..6 say the values are configured at start-up but give none.

**Fix directive:** Chose small values so the extracted model stays small: THR=2.0, LV=1.0, EVADE_TIME=1, STUCK_PERIOD=2, STUCK_DIST=1.0, OUT_PERIOD=1. Replace with deployment values if they are known.

- `java.generated.project/src/main/java/chemical_detector/constants/ChemConstants.java:13-33` `ChemConstants` — CD-Const1, CD-Const2, CD-Const3, CD-Const4, CD-Const5, CD-Const6

## 2. [invented_default] What 'indicates the target chemical' means in analysis()

**Raw:** CD-Fn1 says analysis returns gasD if a reading indicates the target chemical but names neither the target nor the test.

**Fix directive:** analysis() returns gasD iff some GasSensor has c == TARGET_CHEM (=1, invented) and i > 0. Confirm the target identity and the detection test.

- `java.generated.project/src/main/java/chemical_detector/sensor/ChemSensors.java:33-41` `analysis` — CD-Fn1
- `java.generated.project/src/main/java/chemical_detector/constants/ChemConstants.java:37-37` `TARGET_CHEM` — CD-Fn1

## 3. [invented_default] Sensor position to Angle mapping

**Raw:** CD-Fn3 says location returns angle(x) for the strongest sensor x, but angle() is not defined.

**Fix directive:** 1-based position p maps to Front, Right, Back, Left for (p-1) mod 4 = 0,1,2,3. Ties choose the first peak. An empty reading returns Front and intensity() returns 0.0 (safe defaults; both functions have a non-empty precondition).

- `java.generated.project/src/main/java/chemical_detector/sensor/ChemSensors.java:44-84` `location` — CD-Fn2, CD-Fn3

## 4. [design_choice] Sink states keep an event-triggered self-loop

**Raw:** CD-GA-Beh6 says the analysis 'concludes ... processing no further readings'. CD-MV-Beh9 says Found 'stays halted'. The spec has no tick event.

**Fix directive:** GA FINAL self-loops on gas, storing the reading in gs (CD-GA-Var1) without analysing it. MV FOUND self-loops on obstacle, recording l (CD-MV-Var4) without moving or re-sending flag. No separate Final state was added after Found.

- `java.generated.project/src/main/java/chemical_detector/controller/GasAnalysisController.java:79-86` `step` — CD-GA-Beh6, CD-GA-Var1
- `java.generated.project/src/main/java/chemical_detector/controller/MovementController.java:93-99` `step` — CD-MV-Beh9, CD-MV-FR3, CD-MV-Var4

## 5. [design_choice] Complementary guards on the autonomous decisions

**Raw:** Analysis is guarded by sts. GasDetected is guarded by ins vs thr. AvoidingAgain is guarded by the progress/stuck rule.

**Fix directive:** Analysis uses sts == noGas and its negation, which equals sts == gasD because Status has exactly two values. GasDetected uses ins >= THR and its negation. AvoidingAgain uses (since < stuckPeriod || d1-d0 > stuckDist) and its De Morgan complement. Each mode's guards therefore cover every case.

- `java.generated.project/src/main/java/chemical_detector/controller/GasAnalysisController.java:57-77` `step` — CD-GA-Beh4, CD-GA-Beh5, CD-GA-Beh6, CD-GA-Beh7
- `java.generated.project/src/main/java/chemical_detector/controller/MovementController.java:134-151` `step` — CD-MV-Beh17, CD-MV-Beh18

## 6. [design_choice] goreq is implemented but the threshold guard uses >=

**Raw:** CD-Fn4 says goreq is used in the threshold check intensity(gs) >= thr.

**Fix directive:** Intensity is real (double), so the guard is written ins >= THR, which is the meaning of goreq, rather than an opaque goreq(ins, THR) call. ChemSensors.goreq realises CD-Fn4 and is used inside intensity() and location().

- `java.generated.project/src/main/java/chemical_detector/sensor/ChemSensors.java:69-71` `goreq` — CD-Fn4, CD-DM5

## 7. [design_choice] Odometer is sampled through the sensor service

**Raw:** CD-Evt3 is typed 'event' but describes sampling the current distance into d0/d1.

**Fix directive:** Modelled as the zero-argument sensor method ChemSensors.odometer(), read in actions (d0 on entry to Avoiding, d1 on TryingAgain -> AvoidingAgain), not as an input event.

- `java.generated.project/src/main/java/chemical_detector/sensor/ChemSensors.java:28-30` `odometer` — CD-Evt3, CD-MV-Var2, CD-MV-Var3

## 8. [design_choice] Operations live on the Vehicle

**Raw:** CD-OP1..3 are Vehicle-provided. CD-OP4 changeDirection has specified behaviour.

**Fix directive:** move, randomWalk, shortRandomWalk and changeDirection are Vehicle methods. changeDirection is built on move(LV, Right/Left/Back). There are no compute()-style operation classes because the spec has no computed-quantity operations. Chem is modelled as nat and Intensity as real.

- `java.generated.project/src/main/java/chemical_detector/actuator/Vehicle.java:44-52` `changeDirection` — CD-OP1, CD-OP4, CD-DM4, CD-DM5

## 9. [ambiguous_requirement] Peak intensity ignores chemical identity

**Raw:** CD-Fn2 defines intensity as the maximum over the whole reading, so a strong non-target chemical in a reading already classified gasD can reach thr.

**Fix directive:** Followed the spec literally. If only the target's intensity should count, intensity/location must filter on c.

- `java.generated.project/src/main/java/chemical_detector/sensor/ChemSensors.java:44-55` `intensity` — CD-Fn2, CD-GA-Beh6

## 10. [design_choice] Cycle semantics of step()

**Raw:** Autonomous transitions (Analysis, NoGas, GasDetected, AvoidingAgain) need a step() call to fire.

**Fix directive:** step(null) means no event arrived in this cycle. ChemicalDetector runs two extra analysis cycles after each gas reading and one settling movement cycle after each delivered event. Waiting's during action randomWalk() runs at the head of the WAITING block on every cycle.

- `java.generated.project/src/main/java/chemical_detector/system/ChemicalDetector.java:40-101` `ChemicalDetector` — CD-ARCH2, CD-MV-FR1

## Next step

Run the pipeline (compile -> coverage -> preflight -> t2m -> m2m -> m2t -> verifiers) and fix from feedback.
