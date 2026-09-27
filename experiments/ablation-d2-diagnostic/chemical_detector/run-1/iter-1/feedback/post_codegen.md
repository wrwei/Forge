# post_codegen

**status:** uncertain

Two controllers (GasAnalysis, Movement) implement all 81 requirements; 7 review items flagged (invented defaults and design choices).

## 1. [invented_default] Constant values are invented

Requirements CD-Const1..6 name thr, lv, evadeTime, stuckPeriod, stuckDist, outPeriod but give no values.

**fix_directive:** Confirm or replace thr=3.0, lv=1.0, evadeTime=1, stuckPeriod=2, stuckDist=1.0, outPeriod=1 in DetectorConstants.

- `src/main/java/chemical_detector/constants/DetectorConstants.java` `DetectorConstants` — CD-Const1, CD-Const2, CD-Const3, CD-Const4, CD-Const5, CD-Const6

## 2. [invented_default] analysis() and sensor-position-to-Angle mapping are invented

CD-Fn1 does not define when a reading "indicates" the target chemical; CD-DM7/CD-Fn3 do not define angle(x).

**fix_directive:** analysis = gasD iff some value is for the target Chem with intensity > 0; angle(1..4) = Front, Right, Back, Left (mod 4). Confirm.

- `src/main/java/chemical_detector/sensor/VehicleSensors.java` `analysis` — CD-Fn1
- `src/main/java/chemical_detector/sensor/VehicleSensors.java` `angle` — CD-Fn3, CD-DM7

## 3. [design_choice] Terminal modes keep one self-loop

CD-GA-Beh6 "concludes, processing no further readings"; CD-MV-Beh9 "stays halted". Neither says how later inputs are handled.

**fix_directive:** GasAnalysis.Concluded consumes later gas events (stores gs, never classifies); Movement.Found consumes later obstacle events without moving. Every mode therefore has an outgoing transition.

- `src/main/java/chemical_detector/controller/GasAnalysis.java` `step` — CD-GA-Beh6
- `src/main/java/chemical_detector/controller/Movement.java` `step` — CD-MV-Beh9, CD-MV-FR3

## 4. [design_choice] Operations realised as Vehicle methods, not compute() classes

CD-OP1..4 (move, randomWalk, shortRandomWalk, changeDirection) are actuations, not computations; no operation package was created.

**fix_directive:** Vehicle.move/randomWalk/shortRandomWalk/changeDirection; changeDirection maps left->Right, right->Left, front->Back at lv.

- `src/main/java/chemical_detector/actuator/Vehicle.java` `Vehicle` — CD-OP1, CD-OP2, CD-OP3, CD-OP4

## 5. [design_choice] Odometer is a sensor read, not an input event

CD-Evt3 is typed "event" but only describes sampling the distance into d0/d1.

**fix_directive:** d0/d1 = sensors.odometer() on the relevant transitions.

- `src/main/java/chemical_detector/sensor/VehicleSensors.java` `odometer` — CD-Evt3, CD-MV-Var2, CD-MV-Var3

## 6. [design_choice] Chem modelled as record(nat id); Intensity as real

CD-DM4/CD-DM5 describe opaque types.

**fix_directive:** Chem(int id) with equality; intensity values are double with goreq as the comparison.

- `src/main/java/chemical_detector/types/Chem.java` `Chem` — CD-DM4
- `src/main/java/chemical_detector/types/GasSensor.java` `GasSensor` — CD-DM5, CD-DM6

## 7. [ambiguous_requirement] Waiting during-action randomWalk()

CD-MV-FR1 specifies a during action; the Java controller calls vehicle.randomWalk() at the head of the Waiting block each cycle.

**fix_directive:** No change unless the extracted model needs a different placement.

- `src/main/java/chemical_detector/controller/Movement.java` `step` — CD-MV-FR1

## next_step

Run the pipeline (compile, coverage, preflight, T2M, M2M, M2T, verifiers).
