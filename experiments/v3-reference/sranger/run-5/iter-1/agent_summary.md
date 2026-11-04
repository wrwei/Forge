# ITER_SUMMARY — sranger, run 5, iteration 1

## What was written

Nine Java files under `java.generated.project/src/main/java/sranger/` (no Java was present beforehand):

- `annotation/RoboChartType`
- `event/InputEvent` (Obstacle, Tick, EndTask) and `event/OutputEvent` (Move(lv, av), both `real`)
- `sensor/Sensor` with `distance()`
- `actuator/Actuator` with `apply(OutputEvent)`, which records the last lv/av
- `timing/Clock` with `now()`
- `controller/SRangerMode` {MOVING, TURNING, FINAL}, `SRangerConstants` (four real constants) and `SRangerController`

`step` has two predicates, `obstacleDetected` and `turnDurationElapsed` (`timer.now() - clockResetTime >= TURN_DURATION`, so `since(clockResetTime)`), and one block per mode. The trace `result_codegen.json` covers all 23 requirements and all files. A javac harness confirmed the behaviour.

## Decisions the requirements did not settle

- **No-reading distance:** 1000.0 m (invented). Negative/NaN readings also count as "no reading".
- **Moving→Turning:** trigger is the Obstacle event and the guard is `obstacleDetected` (SR-Beh2 + SR-GP1 together).
- **Priority:** endTask comes first in Moving and Turning. In Turning the autonomous return to Moving outranks the tick self-loop, which carries `!turnDurationElapsed`. Otherwise per-cycle ticks would block SR-Beh5.
- **Power-up entry action:** Move(moveVel, 0) is issued in the constructor. The model's initial transition may not carry it.
- **Clock field:** named `timer`, because `clock` is reserved.
- **Final:** no outgoing transitions (terminal).
- **Actuator method:** named `apply`, not `move`, to avoid a name clash with the `Move` event.

## Unresolved / notes

- `obstacleThreshold` 0.5 is rounded up to 1 in the model's constants interface. It only affects the guard on an uninterpreted sensor function.
- Element type for the step-local predicates in the trace: `field` (the trace rules list no type for locals).
