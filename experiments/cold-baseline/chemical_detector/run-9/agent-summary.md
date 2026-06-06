# Chemical Detector — Cold Baseline Run 9

Single-shot, no prior session, no verifier feedback.

## Architecture

Two controllers in `chemdetector.controller`:

- `GasAnalysisController` over `GasAnalysisMode` (Reading, Analysis, NoGas, GasDetected, Final) — implements CD-GA-FR1..4 and CD-GA-Beh1..7.
- `MovementController` over `MovementMode` (Waiting, Going, Avoiding, TryingAgain, AvoidingAgain, GettingOut, Found, Final) — implements CD-MV-FR1..7 and CD-MV-Beh1..23.

Supporting packages: `datatype` (Status, Angle, Loc, Chem, Intensity, GasSensor), `event` (sealed input interfaces + OutputEvent), `constants` (Constants), `sensor` (GasSensorService, MovementSensorService), `actuator` (Vehicle, Clock), `operation` (Analysis, IntensityCompute, LocationCompute, ChangeDirection), and `annotation` (RoboChartType, Clock, SensorService, RoboChartWait).

## Design notes

- Inter-controller events (turn / stop / resume) flow GA -> MV through a small enqueue/nextPending bridge on `MovementController`. `Main` drains the queue.
- Each controller uses a strict two-level mode-nested if-else with named boolean predicates declared before the chain. Event triggers appear directly as `event instanceof X` per the few-shot example.
- Stuck-detection guards are split into named atomic predicates (`clockBelowStuckPeriod`, `distanceAboveStuck`, `makingProgress`, `stuckDetected`).
- Found state's autonomous transition to Final is handled by a bare-precondition `currentMode == Found` branch.
- Odometer events update `MovementSensorService` from within each mode block (self-looping) to preserve the mode-nested structure.

## Invented defaults

- `Constants.thr = 80.0`, `lv = 1.0`, `evadeTime = 2`, `stuckPeriod = 5`, `stuckDist = 1.0`, `outPeriod = 3`.
- `Intensity` modelled as a `double`-carrying record; `Chem` as an opaque int id.
- `Analysis.classify` flags `gasD` when any reading has positive intensity.
