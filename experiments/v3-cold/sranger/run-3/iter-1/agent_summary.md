# SRanger: cold pass 3, iteration 1

## What I wrote
Nine Java files under `sranger/`:
- the `RoboChartType` annotation;
- sealed `InputEvent` (Obstacle, Tick, EndTask) and `OutputEvent` (Move(lv, av));
- `Sensor` (distance) and `Clock` (now);
- `Actuator` (apply, stores the last command);
- `SRangerMode` (MOVING first), `SRangerConstants` and `SRangerController`.

`step` declares two predicates, `obstacleDetected` and `turnDurationElapsed`, then one outer block per mode with the six transitions SR-Beh2 to SR-Beh7. The project held no Java beforehand. I also wrote `result_codegen.json` (all 23 requirements, all 9 files) and `post_codegen.{md,json}`. The code compiles cleanly, and a scratch harness confirmed the expected mode and command sequence.

## Decisions the requirements left open
- **Moving -> Turning:** fires on the obstacle event and also needs the obstacleDetected guard (SR-Beh2 with SR-GP1).
- **Where actions go:** every action is on a transition, not an entry action. That keeps the tick self-loops action-free (SR-Beh4/7) and stops them resetting the turn clock.
- **Priority:** endTask comes first in each mode. In Turning, the timed transition comes before tick, and tick conjoins `!turnDurationElapsed`.
- **Power-up command:** the initial Move(moveVel, 0) is issued in the constructor (SR-FR1).
- **Move payload:** two real components, not one record component.
- **Clock:** monotonic `System.nanoTime` seconds; the field is named `timer` because `clock` is reserved.
- **Invented defaults:** distance with no reading is 1000.0 m (negative or NaN input counts as no reading); the actuator starts at Move(0, 0).

## Unresolved
- OBSTACLE_THRESHOLD = 0.5 rounds up to 1 in the model's constants, so the model's guard is weaker than the Java's.
- The model may not show the power-up command on its initial transition.
- I don't know whether the extractor accepts a two-argument output event.
