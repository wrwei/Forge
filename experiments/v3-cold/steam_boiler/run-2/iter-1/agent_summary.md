# Steam Boiler — pass 2, iteration 1

No Java was present beforehand. I wrote 13 files under `steam_boiler/`:

- `SteamBoilerController`: five-mode enum state machine, 33 transitions. Each one is triggered by `InputEvent.Transmission` (one step per 5 s cycle, SB-V3) and has guards that are mutually exclusive and cover every case in each mode. Each transition broadcasts MODE, records q, v, p and the estimate bounds, and issues pump, valve and PROGRAM_READY commands.
- `BoilerSensor` (with `TransmissionBuffer` and `UnitRegistry`): the program's analysis of each transmission. It counts STOPs, detects transmission, pump, controller, level and steam failures (FR1–FR5), handles repairs and acknowledgements, and keeps the level estimate and worst-case ranges.
- `BoilerActuator`, the event hierarchies, the constants, `BoilerMode`, and `SteamBoilerProgram` (the cycle driver).
- `result_codegen.json` (53/53 requirements) and `post_codegen.{md,json}`.

Decisions the requirements left open:

1. Constant values are invented: C=1000, M1=100, N1=400, N2=600, M2=900, W=10, U1=U2=1, P=5.
2. The per-unit failure detections and repair acknowledgements are emitted by `UnitRegistry`, outside the extracted machine. The model sees only the aggregated failure predicates.
3. Regulation is on/off with all four pumps; initialization also opens all four for filling. A repeated order counts as new only when it changes the ordered state.
4. Risk and plausibility use the U1/U2 gradients and every pump that could be pouring. Pumps that are now off pour nothing for one cycle.
5. A level failure together with a steam or pump-controller failure goes straight to emergency stop (FR8).
6. Emergency stop is terminal: MODE is sent once on entry, then nothing (SB-Beh22 against SB-Beh24).
7. The aberrant-message list is my own. Checks start only once STEAM_BOILER_WAITING has arrived.
8. In rescue mode, any one failed pump controller forces emergency stop.

Unresolved: none blocking. Item 2 leaves FR6/FR7 outside model checking.
