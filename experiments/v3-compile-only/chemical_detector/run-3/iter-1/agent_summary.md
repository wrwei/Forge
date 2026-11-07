# Iteration 1 — chemical_detector, run 3

No Java was present before writing.

## What I wrote
Two controllers in `chemical_detector.controller`:
- `GasAnalysisController`: Reading → Analysis (gas) → NoGas (sends resume) → Reading, or → GasDetected → Final (sends stop) / → Reading (sends turn with location(gs)).
- `MovementController`: Waiting, Going, Found, Avoiding, TryingAgain, AvoidingAgain, GettingOut, with stop and resume first in every mode block.

Supporting classes: domain types (`Status`, `Angle`, `Loc`, `Chem`, `GasSensor`), events (`InputEvent`/`OutputEvent`; turn/stop/resume share names so they become shared events), constants, `ChemicalSensor` (analysis/intensity/location/goreq), `VehicleSensor` (odometer), `Clock`, `Vehicle` (move/randomWalk/shortRandomWalk/changeDirection/delay), and `Actuator` (sends output events).

## Decisions the requirements left open
- Constant values: THR=2.0, LV=1.0, EVADE_TIME=1, STUCK_PERIOD=3, STUCK_DIST=1.0, OUT_PERIOD=2. All are whole numbers, so the model's rounding has no effect.
- Initial values: sts=noGas, ins=0.0, anl=a=Front, l=front, d0=d1=0.
- Intensity is a `double` (real). The target chemical is given to `ChemicalSensor`. A reading "indicates" the target when some value has that chemical with intensity > 0.
- Sensor positions 1–4 map to Left, Right, Back, Front, repeating after 4.
- The operations are `Vehicle` methods, not compute()-style classes, because they take arguments and return nothing.
- The odometer is a sensor method that is sampled, not an event.
- Waiting's during action randomWalk() runs when Waiting is entered.
- GasAnalysis ends in a terminal mode `Final`, and Found is terminal.
- The evasion timer restarts each time Avoiding is entered.

## Unresolved
- intensity and location take the maximum over all chemicals, as worded (CD-Fn2/3). That may not be what was intended.

Details: `post_codegen.md`.
