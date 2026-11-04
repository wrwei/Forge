# ITER_SUMMARY — sranger, pass 6, iteration 1

No Java was present beforehand; nothing was deleted.

## What was written

Nine files under `sranger/`: `annotation.RoboChartType`; `event.InputEvent` (Obstacle, Tick, EndTask) and `event.OutputEvent` (Move(lv, av)); `controller.SRangerMode` (MOVING first, TURNING, FINAL), `controller.SRangerConstants` (four real constants), `controller.SRangerController`; `sensor.Sensor` (`distance()`); `actuator.Actuator` (`apply(OutputEvent)`, last lv/av); `timing.Clock` (`now()`, `advance()`). `step()` has two named predicates (`obstacleDetected`, `turnDurationElapsed`), one block per mode and six branches, so SR-DC1 holds by construction. Trace: 23/23 requirements, 9/9 files. Compiles cleanly (`javac -Xlint:all`); a scratch smoke test showed every transition behaving as specified.

## Decisions the requirements left open

- No-reading distance = 1000.0 m. A negative or NaN reading counts as missing.
- The actuator records (0, 0) before any command.
- The power-up Move(moveVel, 0) is issued in the constructor, because the initial mode has no incoming branch. The model may not carry it.
- Move is one record with two real components. I did not wrap the pair in a datatype the requirements do not name.
- Moving→Turning needs both the obstacle event and `obstacleDetected` (SR-Beh2 and SR-GP1 together).
- Priority: endTask comes first in each mode. In Turning, the timed return sits above the tick self-loop, and that self-loop is guarded by `!turnDurationElapsed`.
- Time comes from a `Clock` that the framework advances. The controller field is named `timer`.
- Final is terminal. Events that have no specified transition are ignored, and no extra self-loops were added.

## Unresolved

- obstacleThreshold = 0.5 is rounded up to 1 in the model's constants interface, so the model's guard is `distance() <= 1`.
- Whether the extractor accepts a two-component event record and the constructor's command is unknown.

All of these are recorded in `post_codegen.{md,json}`.
