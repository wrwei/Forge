# post_codegen — chemical_detector, iteration 1

**Status:** uncertain

Initial Java for chemical_detector (iteration 1): two controllers (GasAnalysisController, MovementController) that talk only through turn/stop/resume, and all 81 requirements are traced. The issues listed are invented defaults and design choices the specification leaves open.

## Issues

### 1. [invented_default] Constant values are placeholders

- **Raw:** CD-Const1..6 say the values are configured at start-up but give none.
- **Fix directive:** Replace THR, LV, EVADE_TIME, STUCK_PERIOD, STUCK_DIST, OUT_PERIOD with deployment values. The current values (all 1) were chosen to lie inside the verifier's configured type ranges (0..1), so every guard can be both true and false during model checking.
- **Java:** `java.generated.project/src/main/java/chemical_detector/constants/ChemConstants.java:14-34` (ChemConstants), requirements CD-Const1, CD-Const2, CD-Const3, CD-Const4, CD-Const5, CD-Const6

### 2. [design_choice] Chem modelled as a two-literal enum

- **Raw:** CD-DM4: Chem is opaque, equality only. The analysis must know which species is the target, but no configuration for it is specified.
- **Fix directive:** Chem is enum {TARGET, OTHER}; the target species is fixed rather than configured. A String/record Chem would be extracted as `real` or leak into the Sensors interface, which is why an enum was used.
- **Java:** `java.generated.project/src/main/java/chemical_detector/domain/Chem.java:8-14` (Chem), requirements CD-DM4

### 3. [invented_default] Meaning of 'a reading indicates the target chemical'

- **Raw:** CD-Fn1 does not define 'indicates'.
- **Fix directive:** analysis returns gasD iff some GasSensor has c == TARGET and i > 0. Confirm or change the criterion.
- **Java:** `java.generated.project/src/main/java/chemical_detector/sensor/GasAnalysisFunctions.java:25-32` (analysis), requirements CD-Fn1

### 4. [invented_default] Sensor position to direction mapping

- **Raw:** CD-DM7/CD-Fn3 say position maps to a direction (angle(x)) without giving the map.
- **Fix directive:** angle(position): 1 Front, 2 Right, 3 Back, 4 Left, repeating. location returns Front for an empty reading.
- **Java:** `java.generated.project/src/main/java/chemical_detector/sensor/GasAnalysisFunctions.java:75-86` (angle), requirements CD-Fn3, CD-DM7

### 5. [design_choice] Threshold guard written as a comparison, not a goreq call

- **Raw:** CD-Fn4 says goreq is used in the threshold check.
- **Fix directive:** Intensity is a Java double (RoboChart real). The guard is `ins >= THR` (goreq is exactly >=) because the extractor keeps a bare method-call predicate as an opaque boolean, which would disconnect the guard from ins and thr. goreq is still used inside intensity/location.
- **Java:** `java.generated.project/src/main/java/chemical_detector/controller/GasAnalysisController.java:65-65` (insAtOrAboveThr), requirements CD-Fn4, CD-DM5, CD-GA-Beh6, CD-GA-Beh7

### 6. [design_choice] Tick event added so terminal modes stay live

- **Raw:** Tick is not in the specification. GasAnalysis FINAL and Movement FOUND have no outgoing transitions in the spec.
- **Fix directive:** Per java_codegen_rules.txt, each terminal mode gets a tick-triggered self-loop with no action (a transition-less state is a CSP STOP). FINAL still processes no further readings (CD-GA-Beh6), and FOUND stays halted (CD-MV-Beh9). flag and halt run once, on the transitions into FOUND.
- **Java:** `java.generated.project/src/main/java/chemical_detector/event/InputEvent.java:42-43` (Tick), requirements CD-GA-Beh6, CD-MV-Beh9
- **Java:** `java.generated.project/src/main/java/chemical_detector/controller/GasAnalysisController.java:97-101` (step), requirements CD-GA-Beh6
- **Java:** `java.generated.project/src/main/java/chemical_detector/controller/MovementController.java:113-117` (step), requirements CD-MV-FR3, CD-MV-Beh9

### 7. [ambiguous_requirement] MV_Found_to_Final names a transition the description does not describe

- **Raw:** CD-MV-Beh9 is named Found_to_Final, but its description only says the subsystem stays halted in Found.
- **Fix directive:** Implemented as 'stays in FOUND' (tick self-loop). No Movement Final state exists.
- **Java:** `java.generated.project/src/main/java/chemical_detector/controller/MovementController.java:113-117` (step), requirements CD-MV-Beh9

### 8. [design_choice] Odometer sampled as a sensor read, not an input event

- **Raw:** CD-Evt3 lists the odometer under events but says the subsystem 'samples' the distance.
- **Fix directive:** d0/d1 are assigned from VehicleSensors.odometer(), which the extractor lifts into the Sensors interface (`d0 = odometer`).
- **Java:** `java.generated.project/src/main/java/chemical_detector/sensor/VehicleSensors.java:16-20` (odometer), requirements CD-Evt3, CD-MV-Var2, CD-MV-Var3

### 9. [design_choice] changeDirection and the Waiting during-action

- **Raw:** CD-OP4 is an operation; CD-MV-FR1 specifies a during action.
- **Fix directive:** changeDirection(l) is a one-argument method, so the extractor models it as an output event `changeDirection : Loc`, not a RoboChart operation. Waiting's during randomWalk() is a statement at the top of the WAITING block (it runs on every cycle spent in Waiting); the extractor renders it as an entry action.
- **Java:** `java.generated.project/src/main/java/chemical_detector/operation/ChangeDirection.java:24-32` (changeDirection), requirements CD-OP4
- **Java:** `java.generated.project/src/main/java/chemical_detector/controller/MovementController.java:77-77` (step), requirements CD-MV-FR1, CD-OP2

### 10. [scope_question] System dispatcher settles autonomous modes

- **Raw:** The requirements do not say how cycles are scheduled.
- **Fix directive:** ChemicalDetector.dispatch routes Vehicle events, delivers turn/stop/resume to Movement in order, and steps a subsystem (bounded, 8 steps) while it is in an autonomous mode (ANALYSIS, NO_GAS, GAS_DETECTED, AVOIDING_AGAIN).
- **Java:** `java.generated.project/src/main/java/chemical_detector/ChemicalDetector.java:46-59` (dispatch), requirements CD-ARCH1, CD-ARCH2

## Next step

Run the pipeline (compile → coverage → preflight → T2M → M2M → M2T → verifiers).
