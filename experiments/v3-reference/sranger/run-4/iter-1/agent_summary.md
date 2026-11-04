# ITER_SUMMARY — sranger run 4

## Iteration 1

**What I wrote.** The starting tree had no Java. I wrote nine files under `sranger/`:
- the `RoboChartType` annotation;
- the `SRangerMode` enum (MOVING first, then TURNING and FINAL);
- the sealed `InputEvent` (Obstacle, Tick, EndTask) and `OutputEvent` (Move(lv, av));
- `SRangerConstants`, holding the four real constants;
- `Sensor.distance()`;
- `Actuator.apply(OutputEvent)`, which records the last lv and av;
- a `Clock` with `now()`;
- `SRangerController`, with one `step(InputEvent)`.

**Transitions.**
- Moving: EndTask goes to Final with Move(0,0). Obstacle with `obstacleDetected` goes to Turning: it resets `clockResetTime` and issues Move(0,turnVel). Tick is a self-loop.
- Turning: EndTask goes to Final. `turnDurationElapsed` (autonomous) goes to Moving with Move(moveVel,0). Tick with `!turnDurationElapsed` is a self-loop.
- Final: terminal, with an empty block.

The trace file `result_codegen.json` covers all 23 requirements and all nine files.

**Decisions the requirements left open:**
- The distance when there is no reading is 1000.0 m (an invented default). Negative or NaN readings count as no reading.
- Moving→Turning uses both the Obstacle trigger (SR-Beh2) and the `obstacleDetected` guard (SR-GP1).
- The power-up Move(moveVel,0) is issued in the constructor.
- The Clock field is named `timer` because `clock` is a reserved word. The Clock uses monotonic nanoTime seconds. `clockResetTime` is reset before the turn command is issued.
- EndTask has the highest priority in both active modes. In Turning, Obstacle is ignored; in Final, every event is ignored.
- Sensor and Actuator are concrete classes, although SR-ARCH2 calls them "interfaces".
- In the model, obstacleThreshold = 0.5 rounds up to 1. I kept metres.

**Unresolved.** I don't know whether the extractor accepts the two-component `Move` record as one event of type real×real. The pipeline will tell.
