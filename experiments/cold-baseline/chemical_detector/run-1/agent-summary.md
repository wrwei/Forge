# Chemical Detector — Cold Baseline Run 1

## Design decisions

- **Two controllers** matching the spec's subsystem split: `GasAnalysisController` (Reading/Analysis/NoGas/GasDetected/Final) under `chemdetector.gasanalysis.controller`, and `MovementController` (Waiting/Going/Found/Avoiding/TryingAgain/AvoidingAgain/GettingOut/Final) under `chemdetector.movement.controller`. Both use the mandated single-method, mode-nested if-else pattern with named predicates declared before the chain.
- **Shared events as a sink-then-drain pattern**: `GasAnalysisOutput` collects `turn`/`stop`/`resume` emissions; `ChemicalDetectorSystem` drains them into `MovementController.step` after every boundary step. This mirrors RoboChart's Shared interface semantics.
- **Sensor functions** (`analysis`, `intensity`, `location`, `goreq`, `angle`) live on a single `@SensorService`-annotated `Sensor` class so the M2M lifts them as RoboChart `function` declarations. Parameter names avoid the reserved word `event` (used `gs`, `a`, `b`, `idx`).
- **Clock T** modelled via `chemdetector.sensor.Clock.nowMs()` stored into a `long T` field; the difference `clock.nowMs() - T` is the pattern the M2M rewrites into `since(T)`.
- **Odometer events** cache `odometerValue` per mode block (kept inside each mode branch to preserve the pure two-level if-else); entry actions copy that cached value into `d0`/`d1`.
- **Invented defaults**: `thr=10.0`, `lv=1.0`, `evadeTime=2`, `stuckPeriod=5`, `stuckDist=1.0`, `outPeriod=3`; `Chem` modelled as a record over `int id`; `Intensity` over `double value`; `analysis` returns `gasD` whenever any reading has positive intensity; `angle(idx)` maps `idx mod 4` to Front/Right/Back/Left.
- **Known risk**: `Analysis` and `AvoidingAgain` have only guarded outgoing transitions (no bare-precondition), which may break the Isabelle `deadlock_free` proof.
