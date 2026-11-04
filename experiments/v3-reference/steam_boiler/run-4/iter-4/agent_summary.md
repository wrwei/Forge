# Steam Boiler, run 4: iteration 4

**History.**
- Iteration 1: preflight failed (annotations); FDR4 was killed for memory; Isabelle's `deadlock_free` timed out; vacuity crashed.
- Iteration 2: flat boolean guards. Preflight and vacuity passed.
- Iteration 3: unused events removed from the controller's interface.
- FDR4 (about 80 GB) and Isabelle (timeout, flagged as thrashing) failed unchanged in iterations 2 and 3.

The cost therefore sat in the state machine itself: 27 transitions, guards of up to seven literals, actions of up to seven outputs.

**Iteration 4: different approach.** The extracted controller keeps only the mode logic: 16 transitions, at most three literals per guard, each sending MODE.
- `DeviceCommands`, run by the driver right after `step`, issues VALVE, OPEN_PUMP, CLOSE_PUMP and PROGRAM_READY from the resulting mode, with the same rules as before.
- The program's messages are unchanged, except that regulation now also runs on a cycle that changes between normal, degraded and rescue. This was checked with the scratch simulation and the edge-case checks.
- Trade-off, recorded as a `scope_question` in `post_codegen.md`: the verified model covers the mode transitions and MODE, not the device commands of Beh4, Beh5, Beh7, Beh10, Beh15 and Beh19.

**Java.**
- Controller: `SteamBoilerController`.
- Sensor: `BoilerSensor` (FR1–FR7, V5–V10, worst-case prediction, the boolean judgements used by the guards).
- `BoilerActuator`, `DeviceCommands`, the `SteamBoilerProgram` driver.
- Events: `InputEvent`, `PlantMessage`, `OutputEvent`, `ProtocolMessage`.
- Domain types and constants.

The trace covers all 53 requirements and all 18 files.

**Open decisions** (all in `post_codegen.md`): invented constants, the regulation policy, MODE sent once on emergency stop, the FR8 reading for rescue, which messages are aberrant, the valve's drain rate, and FR2's delay for stop orders.

**Possible instrument issue.** In iteration 1 the vacuity audit crashed with a Python RecursionError.

**Unresolved:** none yet.
