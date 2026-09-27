# Iteration 1 — chemical_detector run 5 (cold codegen, condition D2)

**What changed.** The tree was empty, so nothing was deleted. I wrote 21 files under `chemical_detector/`, with two mode-nested controllers:

- `GasAnalysisController`: Reading, Analysis, NoGas, GasDetected, Concluded.
- `MovementController`: Waiting, Going, Found, Avoiding, TryingAgain, AvoidingAgain, GettingOut.

They communicate only through Turn, Stop and Resume. Also written: `result_codegen.json` (81/81 requirements, 21/21 files) and `post_codegen.{md,json}`.

**Decisions the spec did not settle:**
- **No Final modes.** Every mode needs an outgoing transition.
  - GasAnalysis ends in `Concluded`. Its gas self-loop keeps the latest reading (CD-GA-Var1) but never analyses it.
  - Movement `Found` self-loops on obstacle and only records `l`. Flag and halt ride on the incoming stop transitions, so flag fires once.
- **Odometer.** It is a sensor query (`OdometerSensor.odometer()`) sampled into d0 and d1, not an input event.
- **Operations.** move, randomWalk and shortRandomWalk are Vehicle methods. changeDirection(l) is an operation class that calls move. wait(n) is `Vehicle.pause(n)`.
- **Guards.** Written as p / ¬p (`stsGasD`, `goreq(ins, THR)`, `makingProgress`), so each mode's guards cover every case by syntax alone.
- **Invented values:**
  - Constants are all 1 or 1.0.
  - Sensor position maps to direction as Left, Right, Back, Front.
  - "Indicates the target" means `c == target && i > 0`.
  - The spec's GasSensor record is named `GasSample`, so no non-sensor class carries "sensor" in its name.

**Unresolved.** CD-GA-Beh6 and CD-MV-Beh9 name a Final state; my non-final reading of both needs human review. In the Java composition, a turn arriving while Movement is in AvoidingAgain is dropped (RoboChart would block the sender).

**Checked before the run:** `javac -Xlint:all` is clean, and a scratch harness passes 24/24 scenarios.
