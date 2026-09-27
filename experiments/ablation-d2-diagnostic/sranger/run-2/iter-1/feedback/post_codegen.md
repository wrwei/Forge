# post_codegen — SRanger (iteration 1)

**Status:** uncertain
**Summary:** Initial SRanger controller generated (9 Java files, 3 modes, 7 specified transitions plus one Final self-loop). Four review items flagged.

## Issues

1. **design_choice — Final mode given a tick self-loop** (SR-FR3, SR-DC1)
   Final is terminal per SR-FR3, but the codegen rules require every mode to have an outgoing transition. Final gets a tick-triggered Final→Final self-loop with no action; it is never left. This is one transition beyond SR-Beh1..7.
   `src/main/java/sranger/controller/SRangerController.java` — `step`

2. **design_choice — `Clock.nowMs()` returns seconds** (SR-Var1, SR-DM2)
   The extractor reads clocks through `nowMs()`; the spec is in seconds. Returning seconds keeps turnDuration = 2.0 rather than scaling the model constant to 2000.
   `src/main/java/sranger/timing/Clock.java` — `nowMs`

3. **ambiguous_requirement — obstacle event AND obstacleDetected guard** (SR-Beh2, SR-GP1)
   Implemented as trigger `obstacle` guarded by `obstacleDetected`. An obstacle event while distance > threshold is ignored.
   `src/main/java/sranger/controller/SRangerController.java` — `step`

4. **invented_default — no-reading distance 1000.0 m** (SR-DM5)
   `Sensor.NO_READING_DISTANCE = 1000.0`, private to Sensor.
   `src/main/java/sranger/sensor/Sensor.java` — `distance`

## Next step
Run the pipeline (compile → coverage → preflight → T2M → M2M → M2T → verifiers).
