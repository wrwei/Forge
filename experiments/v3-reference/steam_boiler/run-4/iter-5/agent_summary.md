# Steam Boiler, run 4: iteration 5

**History.**
- Iteration 1: preflight (annotations), FDR4, Isabelle and vacuity failed.
- Iteration 2: flat boolean guards; preflight and vacuity passed.
- Iteration 3: unused events removed; no effect on FDR4 or Isabelle.
- Iteration 4: the controller reduced to its mode logic (16 transitions, each sending MODE); `DeviceCommands` issues the valve, pump and PROGRAM_READY commands with the same rules. Isabelle passed.

FDR4 hit the 80 GB cap in every iteration, whatever the controller's size. The cost must therefore come from Java the controller depends on that never changed. The prime suspect is the public surface of the sensor and actuator: `List`-typed methods (lists become sequences in the model), plus record- and nat-valued methods on the sensor.

**Iteration 5 (no behaviour change).**
- `PlantMonitor` (new package `monitor`) holds the receive stage, failure detection, bookkeeping, estimate and protocol messages.
- It publishes nine boolean judgements to `BoilerSensor`, now only `update(...)` and getters, as in the few-shot.
- `BoilerActuator` keeps only `apply` and `lastMode`; `OutgoingMessages` buffers the cycle's messages.

The simulation output is identical. The trace covers all 53 requirements, all 20 files and every public method.

**Open decisions** (in `post_codegen.md`): invented constants, the regulation policy, MODE sent once on emergency stop, the FR8 reading for rescue, which messages are aberrant, the valve's drain rate, and FR2's delay for stop orders. Also recorded: the device commands are outside the extracted state machine.

**Instrument notes.** In iteration 1 the vacuity audit crashed (Python RecursionError). If FDR4 still exceeds memory with this minimal model-facing surface, I will report non-convergence: what remains visible to the extractor is a 5-state, boolean-guarded machine.

**Unresolved:** FDR4 memory, pending this iteration.
