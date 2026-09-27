# Iteration 1 — chemical_detector run 3 (cold codegen)

**What changed.** The tree was cold: there was no Java under `java.generated.project/src/main/java`, so nothing was deleted. I wrote 18 main sources under `chemical_detector/`:
- packages `annotation`, `data`, `constants`, `event`, `mode`, `sensor`, `actuator`, `timing`, `controller` and `system`;
- 4 JUnit test classes (19 tests, all passing);
- `result_codegen.json` (81/81 requirements; every file cited);
- `post_codegen.{md,json}`.

The spec describes two state machines, so there are two controllers: `GasAnalysisController` and `MovementController`. Each has a single `step()` built as a mode-nested if-else. Gas analysis emits `OutputEvent.Turn/Stop/Resume`; Movement consumes the same-named `InputEvent`s. `ChemicalDetectorSystem` forwards the events between them.

**Decisions the requirements did not settle.**
- **Terminal modes.** The codegen rules require every mode to have an outgoing transition.
  - Gas analysis ends in `Concluded`, which loops back to itself on `gas`. It stores the reading but never analyses it.
  - Movement's `Found` loops back to itself on `obstacle`. It records `l`, stays halted, and does not flag again.
- **Condition-only exits** (Analysis, GasDetected, AvoidingAgain) are written as exact complements, so one of them always holds.
- **Record name.** The `GasSensor` record is named `GasSample`, because CLAUDE.md says the extractor pattern-matches "sensor" in class names.
- **Operations and odometer.** All four operations are `Vehicle` methods. The odometer is read as a sensor value.
- **Invented values:**
  - the constant values (small integers);
  - the sensor-position→Angle mapping;
  - "indicates target" = the target chemical is present with `i > 0`.

**Unresolved / for human review.**
- CD-Fn2's postcondition takes the peak intensity over every chemical, so a non-target chemical can trigger the stop. I implemented it literally.
- CD-Evt3 lists the odometer as an event, but the requirement describes only sampling.
