# post_codegen — iteration 1

**Status:** uncertain

Initial Java for the Chemical Detector: two controllers in one package (GasAnalysis and Movement), 19 source files, all 81 requirements traced. No mode is named Final, and every mode has an outgoing transition. 13 items need human review (4 invented defaults, 8 design choices, 1 ambiguity).

## Issues

### 1. [design_choice] Gas analysis has no Final state; it ends in a Concluded mode that accepts further readings

CD-GA-Beh6 says the subsystem 'concludes its search, processing no further readings'. The controller models this as the mode Concluded, which has one outgoing transition: a gas self-loop that stores the reading in gs and does not analyse it or emit anything.

**Fix directive:** Review whether keeping (without analysing) later readings satisfies 'processing no further readings'. The alternative is a terminal state with no outgoing transition, which the codegen rules forbid.

**Java trace:**
- `src/main/java/chemical_detector/controller/GasAnalysis.java` L85-101 `step` — CD-GA-Beh6, CD-Const1, CD-GA-Var1
- `src/main/java/chemical_detector/mode/GasAnalysisMode.java` L9-10 `Concluded` — CD-GA-Beh6

### 2. [design_choice] Found has a stop self-loop instead of Found -> Final

CD-MV-Beh9 (MV_Found_to_Final): Found stays halted. Found now self-loops on stop with no action, so flag and the halt are issued only on the first entry.

**Fix directive:** Confirm that an idempotent stop is an acceptable way for Found to remain halted.

**Java trace:**
- `src/main/java/chemical_detector/controller/Movement.java` L109-113 `step` — CD-MV-Beh9, CD-MV-FR3

### 3. [design_choice] Odometer is a sensor query, not an input event

CD-Evt3 lists the odometer as an event. Avoiding entry and TryingAgain->AvoidingAgain sample it as d0/d1 = sensor.odometer() so no transition waits on an odometer event.

**Fix directive:** Confirm that sampling the odometer as a sensor value is acceptable.

**Java trace:**
- `src/main/java/chemical_detector/sensor/Sensor.java` L31-34 `odometer` — CD-Evt3, CD-MV-Var2, CD-MV-Var3
- `src/main/java/chemical_detector/controller/Movement.java` L101-101 `step` — CD-MV-FR4
- `src/main/java/chemical_detector/controller/Movement.java` L142-142 `step` — CD-MV-Beh16

### 4. [invented_default] What 'indicates the target chemical' means

CD-Fn1 does not say when a reading indicates the target. analysis() returns gasD when some sample has c equal to the configured target Chem and i > 0. The target is a Sensor constructor argument.

**Fix directive:** Confirm the detection criterion and where the target chemical is configured.

**Java trace:**
- `src/main/java/chemical_detector/sensor/Sensor.java` L40-47 `analysis` — CD-Fn1

### 5. [ambiguous_requirement] intensity() takes the peak over all chemicals

CD-Fn2 says 'maximum intensity across the sequence', so the peak can come from a non-target chemical. analysis() only looks at the target. Implemented as written.

**Fix directive:** Decide whether intensity/location should look only at target-chemical samples.

**Java trace:**
- `src/main/java/chemical_detector/sensor/Sensor.java` L51-62 `intensity` — CD-Fn2
- `src/main/java/chemical_detector/sensor/Sensor.java` L68-76 `location` — CD-Fn3

### 6. [invented_default] Mapping from sensor position to direction

CD-DM7/CD-Fn3 give no position-to-Angle mapping. Used 1->Front, 2->Right, 3->Back, 4->Left, repeating every 4. An empty reading gives Front and intensity 0.

**Fix directive:** Supply the real sensor layout.

**Java trace:**
- `src/main/java/chemical_detector/sensor/Sensor.java` L84-94 `angle` — CD-Fn3, CD-DM7

### 7. [invented_default] Constant values

No values are given. Chose THR=5.0, LV=1.0, EVADE_TIME=1, STUCK_PERIOD=3, STUCK_DIST=2.0, OUT_PERIOD=2 (whole numbers; durations are small, in Clock units).

**Fix directive:** Replace with the deployment's values.

**Java trace:**
- `src/main/java/chemical_detector/constants/ChemConstants.java` L11-33 `ChemConstants` — CD-Const1, CD-Const2, CD-Const3, CD-Const4, CD-Const5, CD-Const6

### 8. [invented_default] Initial values of state variables

Only gs has a stated initial value (empty). Chose sts=noGas, ins=0, anl=Front, a=Front, l=front, d0=d1=0, evasion clock=0.

**Fix directive:** Confirm the initial values.

**Java trace:**
- `src/main/java/chemical_detector/controller/GasAnalysis.java` L25-29 `GasAnalysis` — CD-GA-Var1, CD-GA-Var2, CD-GA-Var3, CD-GA-Var4
- `src/main/java/chemical_detector/controller/Movement.java` L26-33 `Movement` — CD-MV-Var1, CD-MV-Var2, CD-MV-Var3, CD-MV-Var4, CD-MV-Clock1

### 9. [design_choice] How Chem, Intensity and GasSensor are represented

Chem (opaque) is a record with a nat id. Intensity is real (double). The (c, i) record is named GasSample, not GasSensor, so no data record has 'sensor' in its name (the extractor uses that substring to find the sensor service).

**Fix directive:** Confirm these representations.

**Java trace:**
- `src/main/java/chemical_detector/data/Chem.java` L9-10 `Chem` — CD-DM4
- `src/main/java/chemical_detector/data/GasSample.java` L10-11 `GasSample` — CD-DM5, CD-DM6

### 10. [design_choice] Analysis guards use one predicate and its negation

The spec guards Analysis with sts == noGas and sts == gasD. The code uses !gasPresent and gasPresent (gasPresent = sts == gasD). For the two-valued Status these are the same, and the two guards together cover every case.

**Fix directive:** None expected.

**Java trace:**
- `src/main/java/chemical_detector/controller/GasAnalysis.java` L62-80 `step` — CD-GA-Beh4, CD-GA-Beh5, CD-GA-FR3

### 11. [design_choice] Operation requirements are action calls, not compute() operation classes

move/randomWalk/shortRandomWalk are Vehicle methods (CD-ARCH2 says the Vehicle provides them). changeDirection(l) is its own class, ChangeDirection, called from the Avoiding transitions. None of them computes a value, so none uses the compute() pattern.

**Fix directive:** Confirm this placement.

**Java trace:**
- `src/main/java/chemical_detector/operation/ChangeDirection.java` L21-29 `changeDirection` — CD-OP4
- `src/main/java/chemical_detector/actuator/Vehicle.java` L22-46 `Vehicle` — CD-OP1, CD-OP2, CD-OP3

### 12. [design_choice] Waiting's randomWalk() and event-free cycles

Waiting calls randomWalk() at the top of its mode block, so it runs every cycle spent in Waiting (CD-MV-FR1). step(null) runs a cycle with no event. After each event the system runs extra no-event cycles so guard-only transitions (Analysis, NoGas, GasDetected, AvoidingAgain) fire before the next event arrives.

**Fix directive:** None expected.

**Java trace:**
- `src/main/java/chemical_detector/controller/Movement.java` L72-73 `step` — CD-MV-FR1
- `src/main/java/chemical_detector/system/ChemicalDetector.java` L47-99 `deliver` — CD-ARCH2

### 13. [design_choice] The evasion clock T is the field evasionStart

CD-MV-Clock1's clock T is a Java field (evasionStart) holding the Clock time when Avoiding is entered. The stuck test is timer.nowMs() - evasionStart < STUCK_PERIOD. Clock counts discrete time units.

**Fix directive:** None expected.

**Java trace:**
- `src/main/java/chemical_detector/controller/Movement.java` L69-70 `step` — CD-MV-Clock1, CD-MV-Beh17, CD-MV-Beh18

## Next step

Run the pipeline (compile -> coverage -> preflight -> T2M -> M2M -> M2T -> verifiers) and fix whatever fails.
