# Iteration 1: chemical_detector, cold pass 6

No Java was present at the start. I wrote 19 files under `chemical_detector/`:

- `GasAnalysisController` (Reading, Analysis, NoGas, GasDetected, Concluded) and `MovementController` (Waiting, Going, Found, Avoiding, TryingAgain, AvoidingAgain, GettingOut), each a profile `step`;
- domain types; sealed `InputEvent`/`OutputEvent`; constants;
- `ChemicalSensor` (analysis, intensity, location, goreq), `OdometerSensor`, `Clock`;
- `VehicleActuator`, `CommandActuator`, and a `ChemicalDetectorSystem` that wires the controllers.

turn/stop/resume appear in both event interfaces under the same names, so they become shared events. The trace covers all 81 requirements. The code compiles (Java 17), and a scenario smoke test behaved as specified.

## Decisions the requirements left open

- **Constants:** THR=2, LV=1.0, EVADE_TIME=1, STUCK_PERIOD=2, STUCK_DIST=1.0, OUT_PERIOD=1. All integral and small, so every guard outcome stays reachable.
- **Initial values:** sts=noGas, ins=0, anl/a=Front, l=front, d0=d1=0, T=0.
- **Types:** Intensity is nat; Chem is a record with a nat id.
- **analysis:** gasD when some value is the target chemical (a constructor argument) with i>0.
- **Position to Angle:** position p is the p-th Angle (Left, Right, Back, Front). An empty reading gives 0 and Front.
- **Odometer:** a sampled sensor, not an event.
- **move, randomWalk, shortRandomWalk, changeDirection:** actuator methods, because they take arguments; waits are `pause(n)`.
- **Waiting's during randomWalk():** issued on every transition into Waiting, so the robot does not walk before the first resume or turn.
- **Evasion timer:** reset on both transitions into Avoiding.
- **Final states:** Found is terminal; gas analysis ends in Concluded.

## Unresolved

- CD-DC1 does not hold literally for CD-MV-Beh9 or the two initial transitions.
- NoGas→Reading has no trigger and no guard, as CD-GA-Beh3 requires; the profile discourages that form.
- Gas readings that arrive outside Reading are not buffered.

Details are in `post_codegen.md`.
