# Iteration 1 — chemical_detector run 4 (condition F), cold codegen

**Changed.** There was no pre-existing Java, so nothing was deleted. I wrote a new tree of 20 files under `java.generated.project/src/main/java/chemical_detector/`:

- **Controllers.** `GasAnalysisController` (Reading/Analysis/NoGas/GasDetected/Concluded) and `MovementController` (Waiting/Going/Found/Avoiding/TryingAgain/AvoidingAgain/GettingOut). Both use a single mode-nested `step()` with named predicates.
- **Leaves.** `GasSensorArray` (analysis/intensity/location/goreq), `OdometerSensor`, `Vehicle` (move/randomWalk/shortRandomWalk/pause), `ChangeDirection(Loc)`, an `Actuator` output port and `Clock`.
- **Data and events.** Domain enums and records, sealed `InputEvent`/`OutputEvent`, constants, and a `ChemicalDetector` composition root.
- **Other outputs.** `result_codegen.json` (81/81 requirements, every file cited) and `post_codegen.{md,json}`. The project compiles, and a scratch harness kept outside the project exercises every state.

**Decisions the requirements did not settle:**

- **Constant values.** All are small, in Clock units (THR=1, LV=1, EVADE_TIME=1, STUCK_PERIOD=1, STUCK_DIST=1, OUT_PERIOD=1), plus HALT_VELOCITY=0.
- **Sink states.** The GA final state (named Concluded) and MV Found have no `Final` mode. Each gets a self-loop triggered by `InputEvent.Tick`, an event I invented.
- **Odometer.** It is a sampled sensor value (`d0`/`d1 = odometer()`), not an event.
- **`analysis`.** It returns gasD when the target Chem (a constructor argument) appears with i > 0. Positions 1–4 map to Left/Right/Back/Front. Empty readings give intensity 0 and location Front.
- **Analysis→GasDetected guard.** Written `!stsNoGas`, which is equivalent to `sts == gasD` since Status has two values. This makes the autonomous guards syntactically exhaustive.
- **Platform operations.** These are on `Vehicle`. changeDirection is a parameterised method, not a compute()-style operation.

**Unresolved / for human review:**

- CD-Fn2 takes the maximum intensity over all chemicals, so a strong non-target chemical could trigger stop. I implemented it as written.
- The Java harness drops a turn/stop/resume that arrives while MV is in the transient AvoidingAgain state. RoboChart would delay it.
