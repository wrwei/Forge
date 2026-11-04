# ITER_SUMMARY — chemical_detector, run 1

## Iteration 1

**What I wrote.** Two controllers, following CD-ARCH2:

- `GasAnalysisController`: Reading, Analysis, NoGas, GasDetected, Concluded.
- `MovementController`: Waiting, Going, Found, Avoiding, TryingAgain, AvoidingAgain, GettingOut, Halted.

They share only turn, stop and resume. Gas analysis emits them as `OutputEvent` records; movement receives `InputEvent` records with the same names. Supporting code:

- `GasSensorService`: analysis, intensity, location, goreq.
- `OdometerSensor.odometer()`: the odometer, a sensor variable.
- `Vehicle`: move, randomWalk, shortRandomWalk, changeDirection, flag.
- `Clock`: clock T (field `evasionStart`) and the waits (`pause`).
- One constants class.
- The trace covers all 81 requirements and 18 files.

**Decisions the requirements did not settle:**
- **Constants:** thr=10, lv=1, evadeTime=2, stuckPeriod=5, stuckDist=2, outPeriod=3. Integral and small; stuckPeriod > evadeTime so both AvoidingAgain outcomes are reachable.
- **Initial values:** a=Front, l=front, d0=d1=0, sts=noGas, ins=0, anl=Front.
- **"Indicates the target":** a sample of the configured target chemical with intensity > 0.
- **Empty reading:** intensity 0, location Front (unreachable from the controller).
- **Sensor position to direction:** cycles Left, Right, Back, Front; on a tie the first sensor wins.
- **Operations:** CD-OP1..4 are Vehicle methods, not compute() classes. They are commands with no result, and changeDirection takes an argument.
- **Waiting's randomWalk():** issued on every entry to Waiting, including the initial entry in the constructor.
- **Final states:** terminal modes Concluded and Halted; Found→Halted is unguarded.
- **Naming:** the GasSensor record is named `GasSample` so it cannot be mistaken for a sensor service. Intensity is real.
- **Ignored events:** obstacle events in Waiting, Avoiding, AvoidingAgain and GettingOut, which have no transition for them.

**Unresolved:** none yet; pending the pipeline's verdict.
