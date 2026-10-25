# post_codegen — chemical_detector run 4, iteration 1

**Status:** uncertain

Cold codegen: two controllers (GasAnalysisController, MovementController) plus leaves. All 81 requirements are traced. 11 review items (invented defaults and design choices), none blocking.

## Issues

### 1. [invented_default] Constant values are invented

- **Raw:** Requirements say the constants are configured at startup but give no values.
- **Fix directive:** Confirm or replace the values: THR=1.0, LV=1.0, EVADE_TIME=1, STUCK_PERIOD=1, STUCK_DIST=1.0, OUT_PERIOD=1. They are small integers in Clock time units so that every guard can be both true and false within small verification type ranges.
- **Java:** `src/main/java/chemical_detector/constants/ChemicalDetectorConstants.java:10-33` (ChemicalDetectorConstants) — CD-Const1, CD-Const2, CD-Const3, CD-Const4, CD-Const5, CD-Const6

### 2. [invented_default] HALT_VELOCITY constant added for the zero-velocity halt

- **Raw:** CD-OP1/CD-MV-FR3: halting is move with zero velocity facing Front.
- **Fix directive:** Found's entry action is move(HALT_VELOCITY, Front) with HALT_VELOCITY = 0.0, used instead of a bare literal.
- **Java:** `src/main/java/chemical_detector/constants/ChemicalDetectorConstants.java:34-36` (HALT_VELOCITY) — CD-OP1, CD-MV-FR3

### 3. [invented_default] analysis(): what 'indicates the target chemical' means

- **Raw:** CD-Fn1 does not say how a reading indicates the target chemical, and no constant names the target.
- **Fix directive:** The target Chem is a GasSensorArray constructor argument. analysis returns gasD if and only if some element has c equal to the target and i > 0.
- **Java:** `src/main/java/chemical_detector/sensor/GasSensorArray.java:22-30` (analysis) — CD-Fn1, CD-DM4

### 4. [invented_default] angle(x) position-to-direction mapping and empty-reading defaults

- **Raw:** CD-Fn3/CD-DM7: position encodes a sensing direction; mapping unspecified. CD-Fn2/3 have a non-empty precondition.
- **Fix directive:** Positions 1, 2, 3, 4 map to Left, Right, Back, Front (cyclic). Ties go to the first maximum. An empty reading gives intensity 0.0 and location Front (safe defaults, no sentinel).
- **Java:** `src/main/java/chemical_detector/sensor/GasSensorArray.java:32-77` (location) — CD-Fn2, CD-Fn3

### 5. [ambiguous_requirement] intensity() is the maximum over all chemicals, not only the target

- **Raw:** CD-Fn2 takes the max over the whole sequence. A strong non-target chemical in a reading that also has a trace of the target would trigger stop.
- **Fix directive:** Implemented exactly as specified. Human review: should intensity/location be restricted to the target chemical?
- **Java:** `src/main/java/chemical_detector/sensor/GasSensorArray.java:32-45` (intensity) — CD-Fn2, CD-GA-FR4

### 6. [design_choice] Terminal states use a tick-triggered self-loop; GA final state named Concluded

- **Raw:** CD-GA-Beh6 (GasDetected to final) and CD-MV-Beh9 (Found to final) describe sink behaviour.
- **Fix directive:** There is no Final mode. GA goes to Concluded and MV stays in Found, each with a self-loop triggered by InputEvent.Tick (an event the requirements do not list) so that the sink keeps an outgoing transition. Found's flag/halt actions are on the incoming transitions, so the self-loop does not re-emit flag.
- **Java:** `src/main/java/chemical_detector/controller/GasAnalysisController.java:75-79` (step) — CD-GA-Beh6
- **Java:** `src/main/java/chemical_detector/controller/MovementController.java:93-97` (step) — CD-MV-Beh9, CD-MV-FR3

### 7. [design_choice] Platform operations live on Vehicle; changeDirection is a parameterised operation class

- **Raw:** CD-OP1..3 are provided by the Vehicle. CD-OP4 takes a Loc parameter and has side effects (move), so it cannot be a compute() of field assignments.
- **Fix directive:** move/randomWalk/shortRandomWalk/pause are Vehicle methods. ChangeDirection.changeDirection(Loc l) issues move(LV, Right/Left/Back). No compute()-style operation class was generated.
- **Java:** `src/main/java/chemical_detector/operation/ChangeDirection.java:9-32` (ChangeDirection) — CD-OP1, CD-OP2, CD-OP3, CD-OP4

### 8. [design_choice] Odometer is a sampled sensor value, not an input event

- **Raw:** CD-Evt3: the movement subsystem samples the odometer into d0 or d1.
- **Fix directive:** d0/d1 = odometerSensor.odometer() at the sampling points (entering Avoiding; the TryingAgain to AvoidingAgain transition).
- **Java:** `src/main/java/chemical_detector/controller/MovementController.java:83-83` (step) — CD-Evt3, CD-MV-Var2
- **Java:** `src/main/java/chemical_detector/controller/MovementController.java:123-123` (step) — CD-Evt3, CD-MV-Var3

### 9. [design_choice] Analysis to GasDetected guard written as !stsNoGas

- **Raw:** CD-GA-Beh5 guard is sts == gasD.
- **Fix directive:** Status has exactly two values, so !(sts == noGas) is equivalent to sts == gasD. It makes the two autonomous guards out of Analysis syntactically exhaustive. The same applies to GasDetected (insAtOrAboveThr / !insAtOrAboveThr) and AvoidingAgain (progress / stuck).
- **Java:** `src/main/java/chemical_detector/controller/GasAnalysisController.java:42-60` (step) — CD-GA-Beh4, CD-GA-Beh5

### 10. [design_choice] Waiting's during action randomWalk() runs at the head of the Waiting block

- **Raw:** CD-MV-FR1: during action randomWalk().
- **Fix directive:** Called on every cycle spent in Waiting, before that cycle's transition is evaluated.
- **Java:** `src/main/java/chemical_detector/controller/MovementController.java:57-57` (step) — CD-MV-FR1, CD-OP2

### 11. [scope_question] ChemicalDetector composition harness is cycle-based

- **Raw:** CD-ARCH2 composes the subsystems. The RoboChart semantics is synchronous, while the Java harness forwards GA outputs to MV within the same cycle.
- **Fix directive:** A turn/stop/resume that reaches MV while it is in the transient AvoidingAgain state is consumed by AvoidingAgain's autonomous step and dropped (RoboChart would delay it instead). Only the harness is affected, not the controllers.
- **Java:** `src/main/java/chemical_detector/system/ChemicalDetector.java:45-68` (ChemicalDetector) — CD-ARCH1, CD-ARCH2

## Next step

Run the pipeline (compile, coverage, preflight, T2M, M2M, M2T, verifiers) and address any failing phase.
