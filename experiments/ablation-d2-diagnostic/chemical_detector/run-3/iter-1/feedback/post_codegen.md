# post_codegen — chemical_detector, cold codegen

**Status:** uncertain (12 items for review)

**Summary:** Two controllers, `GasAnalysisController` and `MovementController`, are wired together by `ChemicalDetectorSystem`. All 81 requirements are traced in `result_codegen.json`. `gradlew build` passes, including 19 unit tests.

## Design choices
1. **Terminal modes (CD-GA-Beh6, CD-MV-Beh9, CD-MV-FR3).** There is no Final state, because the codegen rules require every mode to have an outgoing transition.
   - Gas analysis ends in `Concluded`, which loops back to itself on `gas`. It stores the reading in `gs` (CD-GA-Var1) but never analyses it.
   - `Found` loops back to itself on `obstacle`. It records `l` (CD-MV-Var4) but does not move or emit `flag` again.
2. **Data-type name (CD-DM6).** The `GasSensor` record is named `GasSample`. CLAUDE.md says the extractor treats any class or record whose name contains "sensor" as the sensor record.
3. **Type representations (CD-DM4/5).** Intensity is a `double` (real). Chem is `record Chem(int id)`.
4. **Operations (CD-OP1..4).** All four are `Vehicle` methods. None is a sensor-derived computation, so there are no `compute()` operation classes. `changeDirection` maps left→Right, right→Left and front→Back, at `LV`.
5. **Odometer (CD-Evt3).** It is read as a sensor value (`Sensor.odometer()`), not received as an event.
6. **"During" and "wait" (CD-MV-FR1/4/7).**
   - Waiting's `randomWalk()` runs at the head of the Waiting block, so it executes on every cycle spent in Waiting.
   - The waits are `Vehicle.pause(n)`, which only records the request; `step()` does not block.
7. **Complementary guards.** The condition-only exits of Analysis, GasDetected and AvoidingAgain are exact complements (`p` / `!p` and their De Morgan forms), so one of them always holds.

## Invented defaults
- **Constants:** THR=1.0, LV=1.0, EVADE_TIME=1, STUCK_PERIOD=1, STUCK_DIST=1.0, OUT_PERIOD=1. Durations are in abstract Clock units.
- **`angle(x)`:** sensor position 1..4 maps to Left, Right, Back, Front (mod 4). On a tie, the first peak wins.
- **Initial values:** a=Front, l=front, anl=Front, sts=noGas, ins=d0=d1=T=0. Each is overwritten before it is read.

## Ambiguous requirements
- **CD-Fn1.** I read "indicates the target chemical" as: some sample has `c == target` and `i > 0`.
- **CD-Fn2/Fn3.** The peak intensity is taken over every chemical, as the postcondition says literally. A stronger non-target chemical can therefore trigger the stop and set the homing direction. Please confirm this is intended.

## Scope question
- **`ChemicalDetectorSystem` and `InputEvent.NoEvent`** are integration scaffolding that the spec doesn't define.
- In Java, an event that Movement cannot take while in AvoidingAgain is lost. The RoboChart model would block it instead. AvoidingAgain lasts only one cycle.

**Next step:** run the pipeline.
