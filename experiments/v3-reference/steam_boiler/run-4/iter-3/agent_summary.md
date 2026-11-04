# Steam Boiler, run 4: iteration 3

**History.** Iteration 1 failed preflight (missing `real` annotations), FDR4 (killed for memory), Isabelle (`deadlock_free` timeout) and vacuity (a crash in the audit). Iteration 2 annotated the parameters and made every guard a flat conjunction of boolean sensor judgements. Preflight and vacuity then passed, but FDR4 (82 GB) and Isabelle (`deadlock_free` timeout) still failed. Integer types were already narrowed to {0..1}, so numeric ranges were not the cause.

**Iteration 3 change.** The controller's interface no longer declares events it never uses. Before, 15 plant messages (several carrying data) and 8 protocol outputs were declared but never used.
- The SB-DM9 messages are now the sealed interface `PlantMessage`, consumed only by the sensor's receive stage.
- `InputEvent` keeps only the per-cycle `TransmissionReceived`.
- `OutputEvent` keeps the five commands the controller issues: MODE, PROGRAM_READY, VALVE, OPEN_PUMP, CLOSE_PUMP.
- The failure detections and repair acknowledgements are the sealed interface `ProtocolMessage`, sent through `BoilerActuator.announce`.
- Both output kinds extend `Message`.

Behaviour is unchanged; a scratch simulation and the edge-case checks give the same results. The trace covers all 53 requirements and all 17 files.

**What the Java is.**
- `SteamBoilerController.step`: five modes and 27 transitions, guards over nine boolean judgements from `BoilerSensor`.
- `BoilerSensor`: FR1–FR7 detection, V5–V10, worst-case prediction.
- `BoilerActuator`, the `SteamBoilerProgram` driver, events, domain types, constants.

**Open decisions.** All are in `post_codegen.md`: invented constant values, the regulation policy, MODE once on emergency stop, the FR8 reading for rescue, which messages are aberrant, the valve's drain rate, and FR2's delay for stop orders.

**Possible instrument issue.** In iteration 1 the vacuity audit crashed with a Python RecursionError rather than reporting. Flattening the guards avoided it.

**Unresolved:** none yet.
