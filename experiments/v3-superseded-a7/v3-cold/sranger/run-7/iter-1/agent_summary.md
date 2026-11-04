# SRanger — cold pass 7, iteration 1

No Java was present beforehand; nothing was deleted.

## What I wrote

Nine files in package `sranger`: `RoboChartType`; sealed `InputEvent` (`Obstacle`, `Tick`,
`EndTask`) and `OutputEvent` (`Move(lv, av)`); `Sensor.distance()`; `Actuator.apply(OutputEvent)`
with last-velocity getters; `SRangerMode` {MOVING, TURNING, FINAL}; `SRangerConstants`; `Clock`;
and `SRangerController` with the two predicates `obstacleDetected` and `turnDurationElapsed` and a
two-level `step`. All 23 requirements are traced in `result_codegen.json`; the code compiles
cleanly, and a throwaway smoke run matched the requirements.

## Decisions the requirements did not settle

- Moving → Turning uses the `Obstacle` trigger and the `obstacleDetected` guard (SR-Beh2 names the
  event; SR-GP1 names the guard).
- Turning priority is endTask, then the autonomous timeout, then tick (with
  `!turnDurationElapsed`). A tick arrives every cycle, so tick-first would never leave Turning.
- Tick self-loops do nothing else (no Move, no clock reset). Final ignores every event.
- The power-up `Move(moveVel, 0)` is issued in the constructor, because entering Moving at power-up
  is not a branch of `step`.
- `Move` is one event with two real components, emitted via `actuator.apply(new OutputEvent.Move(..))`.
- The clock field is named `timer` (`clock` is reserved).
- Invented defaults: no-reading distance 100.0 m; negative or NaN readings count as no reading;
  `clockResetTime`, clock and actuator start at 0.
- The actuator method is `apply`, not `move`, so it cannot collide with the event `Move`
  case-insensitively.

## Unresolved

- `obstacleThreshold` = 0.5 rounds up to 1 in the model's constants, so the model's obstacle guard
  differs from the Java's.
- Whether the extractor accepts a two-component event and keeps the constructor's initial command
  is unknown until the pipeline runs.

Details: `post_codegen.md`.
