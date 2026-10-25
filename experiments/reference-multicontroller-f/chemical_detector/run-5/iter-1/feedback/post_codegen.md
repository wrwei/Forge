# post_codegen — chemical_detector

**Status:** uncertain

Two controllers (GasAnalysis, Movement) implement all 81 requirements; issues list invented defaults and design choices for review.

## 1. [invented_default] Constant values are placeholders

thr=5.0, lv=1.0, evadeTime=1, stuckPeriod=3, stuckDist=2.0, outPeriod=2 (clock units). The spec says they are configured at startup but gives no values; small integral values keep the timed model tractable and avoid the CSP-gen ceiling of fractional constants.

**Fix directive:** Replace with the deployment's configured values if known.

- `src/main/java/chemical_detector/constants/DetectorConstants.java:6-36` `DetectorConstants` — CD-Const1, CD-Const2, CD-Const3, CD-Const4, CD-Const5, CD-Const6

## 2. [invented_default] Initial values of state variables

gs=[] (spec), sts=noGas, ins=0.0, anl=Front, a=Front, l=front, d0=d1=0.0, T=0. Only gs's initial value is specified.

**Fix directive:** Confirm or supply initial values.

- `src/main/java/chemical_detector/controller/GasAnalysis.java:18-31` `GasAnalysis` — CD-GA-Var1, CD-GA-Var2, CD-GA-Var3, CD-GA-Var4
- `src/main/java/chemical_detector/controller/Movement.java:19-42` `Movement` — CD-MV-Var1, CD-MV-Var2, CD-MV-Var3, CD-MV-Var4, CD-MV-Clock1

## 3. [invented_default] Sensor position to Angle mapping and target-chemical detection rule

angle(x) maps 1-based position x to Left, Right, Back, Front cyclically; analysis reports gasD when a sensor reports the target chemical with intensity > 0. Neither rule is given by the spec. Empty readings default to noGas / 0.0 / Front.

**Fix directive:** Confirm the sensor layout and the presence criterion.

- `src/main/java/chemical_detector/sensor/VehicleSensors.java:39-88` `VehicleSensors` — CD-Fn1, CD-Fn3

## 4. [design_choice] Terminal modes carry a tick-triggered self-loop

GasAnalysis reaches CONCLUDED (the spec's final state) and Movement stays in FOUND; both idle on an invented InputEvent.Tick so that no mode is left without an outgoing transition, per java_codegen_rules.txt. The flag/halt actions are on the stop transitions, so the self-loop does not re-emit flag.

**Fix directive:** None unless the verifiers reject the encoding.

- `src/main/java/chemical_detector/controller/GasAnalysis.java:87-91` `step` — CD-GA-Beh6
- `src/main/java/chemical_detector/controller/Movement.java:115-119` `step` — CD-MV-Beh9, CD-MV-FR3

## 5. [design_choice] Odometer modelled as a sensor sample, not an input event

d0/d1 are sampled from VehicleSensors.odometer() inside the transitions (CD-Evt3 says the subsystem 'samples this distance'); the platform pushes reports via updateOdometer.

**Fix directive:** None.

- `src/main/java/chemical_detector/sensor/VehicleSensors.java:28-36` `odometer` — CD-Evt3, CD-MV-Var2, CD-MV-Var3

## 6. [design_choice] Intensity is a real; threshold guard is ins >= thr

Intensity is represented as double. goreq is implemented (and used by intensity/location) but the GasDetected guards use the equivalent ins >= THR comparison directly, as complementary named predicates.

**Fix directive:** None.

- `src/main/java/chemical_detector/controller/GasAnalysis.java:61-62` `step` — CD-Fn4, CD-GA-Beh6, CD-GA-Beh7

## 7. [ambiguous_requirement] location() over all chemicals

CD-Fn2/Fn3 take the peak over the whole sequence, not only over sensors reporting the target chemical; implemented as specified.

**Fix directive:** Clarify whether non-target chemicals should be excluded.

- `src/main/java/chemical_detector/sensor/VehicleSensors.java:50-73` `intensity` — CD-Fn2, CD-Fn3

## Next step

Run the pipeline (scripts/run_claude_iteration.sh) and act on the first failing phase.
