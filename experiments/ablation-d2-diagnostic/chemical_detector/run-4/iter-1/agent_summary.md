# chemical_detector run 4, iteration 1 (cold codegen)

## What changed
The tree started empty, so nothing was deleted. I wrote 18 files under `chemical_detector/`:
- **Two controllers**: `GasAnalysisController` and `MovementController`, each a single `step(InputEvent)` with a mode-nested if-else over the `mode` enums.
- **Events**: sealed `InputEvent`/`OutputEvent` with record names that match the spec (gas, obstacle, turn, stop, resume, flag).
- **`Sensor`**: odometer, analysis, intensity, location, goreq.
- **`Vehicle`**: move, randomWalk, shortRandomWalk, pause.
- **`ChangeDirection`**, **`Clock`**, **`Constants`**, the domain types, and `@RoboChartType`.

`result_codegen.json` traces all 81 requirements; every file is claimed. `post_codegen` flags 11 items. Compiles cleanly (javac `-Xlint:all`); a scratch harness passed 28/28.

## Decisions the requirements did not settle
- **No Final state.** Gas analysis "concludes" in a `Concluded` mode that absorbs further gas readings without analysing them. Found self-loops on obstacle and stays halted with no second flag. Every mode keeps an outgoing transition.
- **Autonomous-only modes use a predicate and its negation**, so each has exactly one enabled transition: Analysis `stsGasD`/`!stsGasD`, GasDetected `goreq(ins,THR)`/negation, AvoidingAgain progress/De Morgan negation.
- **GasSensor is renamed to GasSample.** CLAUDE.md says the extractor treats "sensor" in a type name specially.
- **The odometer is a sensor query**, not an input event (a mid-entry input can't be expressed in `step(event)`).
- **Invented defaults**:
  - "indicates target" means `c == target && i > 0`
  - positions map in the cycle Left/Right/Back/Front
  - constant values are small integers
  - initial values: `sts = noGas`, angles Front
- **Types**: Chem is a nat-id record, and Intensity is real.

## Unresolved
- Java drops an event that its current mode does not accept, while the model blocks the sender. No inter-controller router is implemented.
- CD-Evt3 is typed "event" but describes a sampled value.
- CD-MV-Beh9 is named "Found_to_Final" but describes staying halted.
