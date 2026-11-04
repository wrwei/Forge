# ITER_SUMMARY: chemical_detector, pass 8

**Written.** 18 Java files in `chemical_detector`:
- domain types: Status, Angle, Loc, Chem, GasSample;
- sealed InputEvent and OutputEvent; DetectorConstants;
- sensors: ChemicalSensor (analysis, intensity, location, goreq), OdometerSensor, Clock;
- actuators: GasAnalysisActuator (turn, stop, resume) and VehicleActuator (move, randomWalk, shortRandomWalk, changeDirection, pause, flag);
- two controllers. GasAnalysisController runs Reading, Analysis, NoGas or GasDetected, then Reading or Concluded. MovementController has seven modes, with stop and resume first in each.

turn, stop and resume are outputs of gas analysis and same-named inputs of movement, so they become shared events. All 81 requirements are traced. javac compiles cleanly, and a scratch scenario reached every mode.

**Decisions the requirements left open.**
- Constants: THR 10.0, LV 1.0, EVADE_TIME 2, STUCK_PERIOD 5, STUCK_DIST 1.0, OUT_PERIOD 3. All integral.
- Initial values: sts noGas, ins 0, anl and a Front, l front, d0 and d1 0. None is read before it is written.
- analysis gives gasD when the target Chem (given to the sensor) has a positive intensity. intensity and location cover all chemicals.
- Sensor position maps to Left, Right, Back, Front, cycling. An empty reading gives 0.0 and Front.
- Operations are VehicleActuator methods: move(lv, a) and changeDirection(l) need arguments that the parameterless compute() cannot carry.
- Waiting's randomWalk is issued on every entry to Waiting and at construction.
- The odometer is a sensor reading, not an event record.
- GasSensor is renamed GasSample (the "sensor" naming rule), and Intensity is a real.
- Gas analysis ends in a terminal Concluded, and Found is terminal.

**Unresolved.**
- CD-MV-Beh9 maps to no transition, the one exception to CD-DC1.
- No class wires the two controllers; the platform forwards GasAnalysisActuator's events.
- It is untested whether the pipeline accepts operations written as actuator methods.
