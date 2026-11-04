# Iteration 1 summary: chemical_detector, pass 3

No Java was present beforehand; nothing was deleted.

## What I wrote

18 files under `chemical_detector`:
- domain types: Status, Angle, Loc, Chem, GasSensor;
- sealed `InputEvent` and `OutputEvent`;
- two mode enums;
- sensors GasSensorArray, NavigationSensor, Clock;
- actuators VehicleActuator, SignalActuator;
- constants;
- `GasAnalysisController` and `MovementController`, each with a two-level if-else `step`.

Gas analysis emits turn, stop and resume; movement consumes them under the same record names, making them shared events. Also written: `result_codegen.json` (all 81 requirements, every file named) and `post_codegen.{md,json}` (uncertain, 16 issues).

## Decisions the requirements left open

- Constants: THR 2.0, LV 1.0, EVADE_TIME 1, STUCK_PERIOD 3, STUCK_DIST 1.0, OUT_PERIOD 2 (integer-valued; ticks).
- Initial values of every variable except gs.
- The target chemical is a sensor constructor argument; a value indicates it when c matches and i > 0.
- Positions 1–4 map to Left, Right, Back, Front, cyclically. Ties go to the first position; empty readings give 0.0 or Front.
- Intensity is real; the odometer is a sampled sensor quantity, not an event.
- move, randomWalk and shortRandomWalk are actuator methods: the Vehicle provides them, and an operation class takes no arguments. changeDirection(l) is move(LV, evasionAngle(l)).
- Waiting's during action is randomWalk on every entry and at start-up.
- Found goes to HALTED unguarded, giving CD-MV-Beh9 its own transition (CD-DC1).

## Unresolved

- Should intensity and location consider only the target chemical? Implemented literally, over all values.
- The non-empty precondition of intensity and location holds in Java but no contract states it.
- Forwarding gas-analysis outputs into movement is left to the integrator.
- Unchecked before the run: how the extractor treats the two-argument `move`, unguarded mode blocks and the list-typed `gs`.
