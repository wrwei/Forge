# post_codegen — chemical_detector iteration 1

**Status:** uncertain

Iteration 1 cold codegen for chemical_detector: two mode-nested controllers (GasAnalysisController, MovementController) plus sensor, vehicle, operation, event, domain and constants types; 81/81 requirements traced. 11 items flagged for review (design choices and invented defaults); nothing unimplemented.

## Issues

### 1. [design_choice] Terminal behaviour realised without a Final state

CD-GA-Beh6 says gas analysis 'concludes its search, processing no further readings'; CD-MV-Beh9 (named Found_to_Final) says Found 'stays halted'. Both are realised as ordinary modes with a self-loop so that every mode keeps an outgoing transition: GasAnalysisMode.Concluded self-loops on gas (the reading is retained per CD-GA-Var1 but never analysed; nothing is emitted), and Found self-loops on obstacle (l is updated per CD-MV-Var4; no move, no second flag).

**Fix directive:** Confirm that absorbing further gas/obstacle events without acting satisfies 'processing no further readings' and 'stays halted'.

**Java trace:**
- `src/main/java/chemical_detector/controller/GasAnalysisController.java:81-87` `step` — CD-GA-Beh6, CD-GA-FR4
- `src/main/java/chemical_detector/controller/MovementController.java:103-109` `step` — CD-MV-Beh9, CD-MV-FR3

### 2. [design_choice] GasSensor record named GasSample

CD-DM6 names the record GasSensor. CLAUDE.md states the extractor treats type names containing 'sensor' specially (sensor-service fallback, 'the sensor record'), so the value type is named GasSample to keep it a plain datatype.

**Fix directive:** Accept the rename or rename back if the extractor distinguishes records from sensor classes.

**Java trace:**
- `src/main/java/chemical_detector/domain/GasSample.java:10-10` `GasSample` — CD-DM6, CD-DM7

### 3. [design_choice] Odometer is a sampled sensor value, not an input event

CD-Evt3 is typed 'event' but describes sampling the current distance into d0/d1. A mid-entry-action input cannot be expressed in the single step(event) shape, so d0/d1 are assigned from Sensor.odometer() (a zero-arg sensor query) on entry to Avoiding and on TryingAgain->AvoidingAgain.

**Fix directive:** Confirm that a sensor query is an acceptable realisation of the odometer.

**Java trace:**
- `src/main/java/chemical_detector/sensor/Sensor.java:33-33` `odometer` — CD-Evt3, CD-MV-Var2, CD-MV-Var3

### 4. [invented_default] Meaning of 'indicates the target chemical' in analysis

CD-Fn1 does not define when a value indicates the target. Chosen: some GasSample has c equal to the configured target Chem AND i > 0. The target Chem is a Sensor constructor argument.

**Fix directive:** Confirm the classification rule and where the target chemical is configured.

**Java trace:**
- `src/main/java/chemical_detector/sensor/Sensor.java:41-48` `analysis` — CD-Fn1

### 5. [invented_default] Sensor position to Angle mapping

CD-Fn3/CD-DM7 say position maps to a sensing direction but give no mapping. Chosen: 1-based positions cycle Left, Right, Back, Front (enum order); ties resolve to the first maximum; empty readings yield Front and intensity 0.

**Fix directive:** Supply the physical sensor layout if it differs.

**Java trace:**
- `src/main/java/chemical_detector/sensor/Sensor.java:90-95` `angle` — CD-Fn3

### 6. [invented_default] Constant values and time unit

CD-Const1..6 give types but no values. Chosen: THR=5.0, LV=1.0, EVADE_TIME=2, STUCK_PERIOD=5, STUCK_DIST=1.0, OUT_PERIOD=3, all integral and small; time is in Clock units advanced once per cycle.

**Fix directive:** Replace with deployment values.

**Java trace:**
- `src/main/java/chemical_detector/constants/Constants.java:13-33` `THR` — CD-Const1, CD-Const2, CD-Const3, CD-Const4, CD-Const5, CD-Const6

### 7. [invented_default] Initial values of state variables

Only gs ('initially empty') has a specified initial value. Chosen: sts=noGas, ins=0, anl=Front, a=Front, l=front, d0=d1=0, T=0.

**Fix directive:** Confirm; none is read before being assigned on every path except a (move in Going always follows a turn that sets it).

**Java trace:**
- `src/main/java/chemical_detector/controller/GasAnalysisController.java:30-30` `sts` — CD-GA-Var2, CD-GA-Var3, CD-GA-Var4
- `src/main/java/chemical_detector/controller/MovementController.java:35-35` `a` — CD-MV-Var1, CD-MV-Var4

### 8. [design_choice] Chem as a nat-id record, Intensity as real

CD-DM4 (opaque, equality only) is a record Chem(id: nat); CD-DM5 (totally ordered) is represented by double/real, with goreq as the ordering used for the threshold guard.

**Fix directive:** None unless a different carrier type is required.

**Java trace:**
- `src/main/java/chemical_detector/domain/Chem.java:8-8` `Chem` — CD-DM4
- `src/main/java/chemical_detector/sensor/Sensor.java:98-98` `goreq` — CD-DM5, CD-Fn4

### 9. [design_choice] Complementary guards on autonomous-only modes

Analysis uses stsGasD / !stsGasD (for the two-valued Status, !stsGasD is exactly sts == noGas); GasDetected uses goreq(ins,THR) / its negation; AvoidingAgain uses (withinStuckPeriod || advancedBeyondStuckDist) / its De Morgan negation. Each such mode therefore always has exactly one enabled transition.

**Fix directive:** None.

**Java trace:**
- `src/main/java/chemical_detector/controller/GasAnalysisController.java:48-49` `step` — CD-GA-Beh4, CD-GA-Beh5, CD-GA-Beh6, CD-GA-Beh7
- `src/main/java/chemical_detector/controller/MovementController.java:63-64` `step` — CD-MV-Beh17, CD-MV-Beh18

### 10. [design_choice] changeDirection is a plain operation class without compute()

CD-OP4 takes a Loc argument and issues a move; the compute()/field-assignment operation shape does not fit an actuation with a parameter. ChangeDirection.changeDirection(Loc) is called from the Avoiding entry behaviour. CD-OP1..3 are Vehicle methods since the spec says the Vehicle provides them.

**Fix directive:** None unless operations must follow the compute() shape.

**Java trace:**
- `src/main/java/chemical_detector/operation/ChangeDirection.java:22-30` `changeDirection` — CD-OP4

### 11. [scope_question] Events arriving in a mode that does not accept them are dropped

In step(event) an event a mode has no transition for (e.g. turn in AvoidingAgain, gas in Analysis) is ignored, whereas in the RoboChart model the sender blocks until accepted. No system-level wiring/queue between the two controllers is implemented; the controllers exchange events only via Actuator (drain) and step(InputEvent).

**Fix directive:** Decide whether a buffering router between the subsystems is in scope.

**Java trace:**
- `src/main/java/chemical_detector/actuator/Actuator.java:26-30` `drain` — CD-ARCH2

## Next step

Run the pipeline (compile -> coverage -> preflight -> T2M -> M2M -> M2T -> verifiers) and review the flagged defaults.
