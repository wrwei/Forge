# SRanger — pass 1 summary

No Java was present before writing; nothing was deleted.

## What was written

Nine files under `sranger/`: `RoboChartType`; sealed `InputEvent` (Obstacle, Tick, EndTask) and `OutputEvent` (Move(lv, av)); `Sensor.distance()`; `Actuator.apply(OutputEvent)`, which keeps the last command; `SRangerMode` (MOVING first, TURNING, FINAL); `SRangerConstants`; `Clock`; and `SRangerController`. `step` declares two predicates, `obstacleDetected` and `turnDurationElapsed` (`timer.now() - clockResetTime >= TURN_DURATION`), followed by one block per mode. The trace covers all 23 requirements and all 9 files.

## Decisions the requirements left open

- Moving→Turning needs both the Obstacle event (SR-Beh2) and `obstacleDetected` (SR-GP1).
- Turning priority: endTask first, then the timed return, then tick. The tick branch also requires `!turnDurationElapsed`, so a tick every cycle cannot block the return.
- The tick self-loops do nothing, as SR-Beh4/7 say. They do not reset the turn clock.
- Entering Turning resets `clockResetTime`, then issues Move(0, turnVel), in SR-Beh2's order.
- Move(moveVel, 0) is issued in the constructor for power-up entry into Moving (SR-FR1).
- With no reading, the Sensor returns 100.0 m. Negative or NaN input counts as no reading.
- The Actuator holds Move(0, 0) until the first command.
- The Clock is framework-advanced, in seconds, held in a field named `timer` because `clock` is reserved.
- Move keeps two real components, matching SR-DM4.
- Final has no transitions. Events without a transition in the current mode are ignored.

## Unresolved

- obstacleThreshold = 0.5 is rounded up to 1 in the model's constants, so the model's guard is `distance <= 1`.
- The model's initial transition may not carry the constructor's Move command.
- `corrections/csp_overrides.csp` is labelled for LRE and bounds `core_clock_type` to {0..1}, below turnDuration = 2. If it applies here, `since(clockResetTime) >= 2` could never hold.

All details are in `post_codegen.md`.
