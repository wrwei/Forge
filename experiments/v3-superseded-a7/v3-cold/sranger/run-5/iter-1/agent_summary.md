# ITER_SUMMARY — sranger, pass 5, iteration 1

No Java was present at the start; nothing was deleted.

## What I wrote

Nine files under `sranger/`:

- `annotation` (RoboChartType);
- `event` (sealed `InputEvent`: Obstacle, Tick, EndTask; sealed `OutputEvent`: Move(lv, av));
- `sensor/Sensor` (`distance()`);
- `actuator/Actuator` (records the last Move);
- `controller`: SRangerMode (MOVING first), SRangerConstants, Clock, SRangerController.

`step()` declares the two predicates `obstacleDetected` and `turnDurationElapsed`, then has one block per mode. EndTask comes first in Moving and in Turning. `clockResetTime` is assigned from `timer.now()`, which the pipeline turns into a RoboChart clock reset and `since`. The trace covers all 23 requirements and all 9 files.

## Decisions the requirements left open

- **No-reading distance:** 1000.0 m. Negative or NaN readings count as no reading.
- **Moving to Turning:** needs both the obstacle event (SR-Beh2) and the `<=` guard (SR-GP1).
- **Turning priority:** endTask, then the timed transition, then tick with `!turnDurationElapsed`. Ticks arrive every cycle, so a tick branch placed first would hide the timeout.
- **Tick self-loops:** they do nothing, as SR-Beh4 and SR-Beh7 require. They neither re-issue Move nor reset the clock.
- **Initial Move(moveVel, 0):** issued in the constructor, because `step()` only handles transitions.
- **Move event:** one record with two real components, as SR-DM4 describes.
- **Clock field:** named `timer`, because `clock` is reserved.
- **Order on entering Turning:** the clock is reset before the turn command, as SR-Beh2 orders it.

## Unresolved

- `obstacleThreshold` is 0.5, and the model rounds fractional constants up, so the model's guard compares against 1.
- RoboChart self-transitions re-run entry actions. If the extractor makes Move or the clock reset an entry action, the tick self-loops would re-run them, which conflicts with "no action".
- How the extractor types a two-component event record is unknown.

Details: `post_codegen.{md,json}`.
