# Iteration 1 — chemical_detector run 3 (condition E, cold codegen)

**Changed:** I wrote the whole tree from scratch under `java.generated.project/src/main/java/chemical_detector/`: 19 files, plus `result_codegen.json` (122 rows, all 81 requirements) and `post_codegen.{md,json}`. There was no Java to delete. `build.gradle` is unchanged.

**Architecture:**
- Two mode-nested controllers, `GasAnalysisController` and `MovementController`. They share one `InputEvent`/`OutputEvent` pair of sealed hierarchies. The records use the spec's event names (`gas`, `obstacle`, `turn`, `stop`, `resume`, `flag`), so `turn`/`stop`/`resume` are emitted by one controller and received by the other.
- `Sensor` (`@SensorService`) holds `analysis`, `intensity`, `location`, `goreq` and `odometer()`.
- `Vehicle` holds `move`, `randomWalk`, `shortRandomWalk` and `pause`. `ObstacleEvasion.changeDirection(l)` implements CD-OP4.
- A logical `Clock` sits behind the field `timer`. The field `evadeStart` is the clock T.

**Decisions the spec did not settle:**
- **Terminal states.** No mode may lack an outgoing transition, so neither Final is modelled as a dead end. The gas analyser moves to `Concluded`, which absorbs `gas` (stores it, never analyses it). Movement's `Found` has action-free self-loops on `stop` and `obstacle`. `flag` and the halt fire only on the transitions into `Found`.
- **Guards.** Paired guards are written as complements (`stsNoGas`/`!stsNoGas`, `insAtLeastThr`/`!insAtLeastThr`, progress/`!`progress), so each autonomous mode's guards cover every case.
- **Odometer.** It is a sampled sensor reading, not an input event.
- **Types.** Intensity is a real. Chem is a record with a nat species id.
- **Invented values.** THR=1, LV=1, EVADE_TIME=1, STUCK_PERIOD=2, STUCK_DIST=1, OUT_PERIOD=1. The analysis criterion is "target chemical with intensity > 0". Sensor position maps to Angle modulo 4.

**Unresolved (for human review):**
- CD-Fn2 takes the peak over all chemicals, not just the target.
- Java drops events that a mode does not handle.
- Wiring the two controllers together is left to the integrator.

Not compiled locally: the brief forbids running phases outside the runner.
