# Chemical Detector — Cold Baseline Run 5

Single-shot Java implementation of the Chemical Detector case study. No prior code or verifier feedback consulted.

## Structure

- `data/` — enums Status, Angle, Loc and records Chem, Intensity, GasSensor (CD-DM1..CD-DM6).
- `constants/Constants.java` — thr, lv, evadeTime, stuckPeriod, stuckDist, outPeriod (CD-Const1..6).
- `event/` — GAInputEvent (Gas), MVInputEvent (Turn, Stop, Resume, Obstacle, Odometer), SharedEvent (GA->MV), OutputEvent (Flag).
- `sensor/Sensor.java` — analysis, intensity, location, goreq plus reading/odometer buffering (CD-Fn1..4).
- `actuator/Vehicle.java` — move, randomWalk, shortRandomWalk plus flag-event sink (CD-OP1..3, CD-Evt7).
- `clock/Clock.java` — nowMs() for stuck-detection.
- `mode/` — GAMode, MVMode enums.
- `controller/GasAnalysisController.java` — mode-nested if-else over Reading/Analysis/NoGas/GasDetected/Final.
- `controller/MovementController.java` — Waiting/Going/Avoiding/TryingAgain/AvoidingAgain/GettingOut/Found/Final.
- `controller/ChemicalDetectorSystem.java` — wires the two controllers; forwards shared turn/stop/resume from GA into MV.

## Design choices

- changeDirection(l) is inlined as a named-predicate if-else (Loc -> Angle map) at the Avoiding-state entries, per CD-OP4 specified behaviour.
- Threshold thr = 50, lv = 1.0, stuckPeriod = 10, stuckDist = 1.0, evadeTime/outPeriod = 5 — invented defaults; magnitudes chosen to keep the RoboChart constants interface populated.
- Odometer is buffered per-step into lastOdometer so the Avoiding/AvoidingAgain entry actions can assign d0 / d1 without a payload binding.
