# Steam Boiler — pass 8, iteration 1

**Written.** 13 Java files under `steam_boiler/`:
- `SteamBoilerController`: five modes; `step` fires on one `Transmission` event per cycle; 46 transitions; no event-less transitions; guards in each mode exclusive and exhaustive.
- `PlantMonitor` with its `BoilerSensor` facade: receive and analysis stages, failure detection (FR1–FR5), unit status (V9), STOP count (V10), level estimate (V8).
- `BoilerActuator`, the sealed event types, `BoilerConstants`, and the `SteamBoilerProgram` cycle driver.

Also written: the trace (227 entries, all 53 requirements) and `post_codegen.{md,json}` (13 issues). Compiled with javac; hand-simulated start-up, rescue, pump failure and repair, STOP, and a missing message.

**Why.** The profile builds guards only from sensor calls and constants. So all stateful judgments sit sensor-side, and the controller holds only the mode.

**Unsettled decisions:**
- Constant values invented (C 1000, M1 100, M2 900, N1 400, N2 600, W 10, U1 = U2 = 1, P 10). Pumps numbered 1–4.
- Regulation: all pumps on below N1, all off above N2. "Activates a pump" in initialization means all four.
- Limit risk is the worst case one cycle ahead. Closing pumps stops inflow at once; newly opened pumps pour nothing for a cycle; the low side assumes no pump water.
- A level failure while steam or a pump controller is also out goes straight to emergency stop. Any single controller failure counts.
- Before STEAM_BOILER_WAITING only STOP acts.
- MODE(emergency stop) is sent once, then nothing.
- Aberrant messages: duplicated data, bad pump numbers, out-of-phase start-up messages. Repeated repair or ack messages are tolerated.
- No lower level-dynamics bound while the valve is open (drain rate unspecified).

**Unresolved.** The FR6/FR7 messages are conditional per unit, so the two-level if-else cannot express them. `PlantMonitor` emits them outside the controller, and they are not in the formal model.
