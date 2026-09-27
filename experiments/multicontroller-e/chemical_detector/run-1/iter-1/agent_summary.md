# Iteration 1 — chemical_detector, run 1 (condition E), cold codegen

## What changed and why
The Java was written from scratch under `java.generated.project/src/main/java/chemical_detector/`. There was no earlier Java to delete. There are 19 main files and 4 package-private JUnit tests (19 tests, all passing under a scratch `javac` + JUnit run). `result_codegen.json` traces all 81 requirements in 116 rows and cites every file.

The two reasoning subsystems are two controllers in one `controller` package:
- `GasAnalysis`: Reading, Analysis, NoGas, GasDetected, Concluded.
- `Movement`: Waiting, Going, Found, Avoiding, TryingAgain, AvoidingAgain, GettingOut.

They communicate only through Turn/Stop/Resume. These are nested records in both `InputEvent` and `OutputEvent`, with the same payloads. `Flag` goes to the `Vehicle`.

## Decisions the spec left open
- No mode is named Final, and every mode has an outgoing transition:
  - GasAnalysis ends in `Concluded`. Its gas self-loop stores each reading but never analyses it.
  - `Found` self-loops on `stop` and does not re-flag.
- Guard-only exits use one predicate and its negation, so their guards cover every case: Analysis uses `gasPresent`, GasDetected uses `goreq(ins,THR)`, AvoidingAgain uses progress/stuck.
- The odometer is a sensor query (`d0/d1 = sensor.odometer()`), not an event.
- `changeDirection(l)` is its own class. `move`, `randomWalk`, `shortRandomWalk` and `pause` (for the waits) are `Vehicle` methods.
- Chem is a record with a nat id and Intensity is `double`. The (c,i) record is called `GasSample`, so no data record has "sensor" in its name.
- Invented values:
  - Detection means the target chemical with i > 0.
  - Sensor positions map 1→Front, 2→Right, 3→Back, 4→Left.
  - The constants are small whole numbers.

## Unresolved (in post_codegen.md)
- Whether keeping later readings in Concluded counts as "processing no further readings".
- Whether `intensity()` should be taken over the target chemical only.
