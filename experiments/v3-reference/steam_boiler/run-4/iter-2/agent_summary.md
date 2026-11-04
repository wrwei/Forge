# Steam Boiler, run 4: iteration 2

**Iteration 1 results.**
- Preflight: two private `double` parameters lacked `@RoboChartType("real")`.
- FDR4: killed at 78 GB with no verdict.
- Isabelle: `deadlock_free` timed out.
- Vacuity: crashed with a recursion error while parsing guards.

**Changes in iteration 2.**
- Annotated the two parameters.
- Every sensor query the controller's guards use is now boolean; before, some were real- or nat-valued and compared with constants. The comparisons against M1, M2, N1 and N2 moved into `BoilerSensor`.
- The disjunctions moved into three sensor judgements (emergency in initialization, operation and rescue), so every guard is a flat conjunction of literals.
- The aberrant start-up message checks moved into the sensor, which learns the mode and PROGRAM_READY from the messages transmitted.
- The controller keeps only `currentMode` and `valveOpen`.
- The valve is now closed on its own cycle before filling or PROGRAM_READY.

Behaviour is otherwise unchanged, checked with a scratch simulation.

**What the Java is.** `SteamBoilerController.step` (five modes, 27 transitions, each triggered by `TransmissionReceived` and sending MODE), `BoilerSensor` (FR1–FR7, V5–V10, prediction), `BoilerActuator`, the `SteamBoilerProgram` driver, events, domain types, constants.

**Open decisions (all in `post_codegen.md`):**
- The constant values are invented.
- Regulation opens all pumps below N1 and closes all above N2.
- Emergency stop sends MODE once, then the program stops.
- STOP and transmission checks also run before STEAM_BOILER_WAITING.
- A level failure while the steam device or a pump controller is out of order goes to emergency stop (FR8).
- Which messages count as aberrant; the drain rate of the valve; FR2's delay is applied to stop orders too.

**Possible instrument issue.** In iteration 1 the vacuity audit crashed (a Python RecursionError) instead of reporting. I worked around it by flattening the guards.

**Unresolved:** none so far.
