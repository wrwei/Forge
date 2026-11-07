# Iteration 1 — chemical_detector, run 2

No Java was present before this run.

## What I wrote

Package `chemical_detector`, with two controllers:

- `GasAnalysisController`: Reading → Analysis → NoGas / GasDetected → Concluded.
- `MovementController`: Waiting, Going, Found, Avoiding, TryingAgain, AvoidingAgain, GettingOut, Halted.

Supporting code:

- Domain types: Status, Angle, Loc, Chem, GasSensor.
- `InputEvent` and `OutputEvent`.
- A constants class.
- `GasReadingSensor`: analysis, intensity, location, goreq.
- `DistanceSensor`: the odometer.
- `Clock`: evasion timer, and `pause` for the model's wait.
- `Actuator`: receives emitted events.
- Four operation classes; the trace.

## Decisions the requirements left open

- **Constants:** THR 2.0, LV 1.0, EVADE_TIME 1, STUCK_PERIOD 2, STUCK_DIST 1.0, OUT_PERIOD 1. Whole numbers, so model rounding changes nothing. One tick is one second.
- **Target chemical:** a value counts when c equals the configured target and i > 0.
- **Sensor directions:** position 1 Left, 2 Right, 3 Back, 4 Front, repeating. Ties go to the first position.
- **Empty reading:** intensity 0.0, location Front.
- **Initial values:** sts noGas, ins 0.0, anl/a Front, l front, d0/d1 0.0.
- **Peak intensity:** over all chemicals, as CD-Fn2 literally says.
- **Operations:** the four operation classes store the latest command. changeDirection encodes its steering rule as three booleans plus lv.
- **during randomWalk():** the first statement of the Waiting block.
- **Final states:** terminal modes. NoGas→Reading and Found→Halted are bare, unguarded transitions.
- **Shared events:** turn, stop and resume are same-named `OutputEvent` (sender) and `InputEvent` (receiver) records.
- **Odometer:** d0 and d1 are sampled from `DistanceSensor`, because an action cannot receive an event.
- **Priority:** stop comes first in every movement state.

## Unresolved

Not yet known whether the extractor accepts:

- bare unconditional transitions;
- the Waiting preamble statement;
- `compute` methods with parameters;
- paired event records.
