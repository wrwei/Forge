# Steam Boiler, pass 5, iteration 1

**Written.** 14 Java files under `steam_boiler/`, plus `result_codegen.json` (198 entries; all 53 requirements covered) and `post_codegen.{md,json}`. No Java existed beforehand.

- `SteamBoilerController` has one mode field. Its `step` covers five modes, each transition fired by the per-cycle `TransmissionReceived` event.
  - Every guard conjoins the negations of the guards above it.
  - An exhaustive enumeration found exactly one branch enabled in each non-terminal mode.
  - EMERGENCY_STOP is terminal.
- `BoilerSensor` buffers each cycle's messages and does all the judging:
  - failure detection (FR1–FR5), STOP count, q, v, p, level estimate, worst-case forecast.
  - `UnitStatusBoard` keeps per-unit status (V9) and builds the announcement and acknowledgement messages (FR6/FR7).
- `SteamBoilerProgram` runs each cycle: receive, analyse, transmit.

**Decisions the requirements left open:**
- **Constants:** C=1000, M1=150, M2=850, N1=400, N2=600, W=25, U1=U2=2, P=10; pumps numbered 1..4.
- **Regulation:** one pump per cycle, steered by a worst-case forecast. Cycles that change mode leave the pumps as they are.
- **Level risk:** forecast min ≤ M1 or max ≥ M2. A pump counts as surely pouring only once its flow is confirmed.
- **Aberrant messages:** duplicates, unknown pump numbers, init-only messages after initialization, PHYSICAL_UNITS_READY before PROGRAM_READY, repairs or acks with nothing outstanding.
- **Emergency stop:** MODE(emergency_stop) is sent once, then nothing more.
- **Before STEAM_BOILER_WAITING:** STOP and transmission failure still lead to emergency stop.
- **Combined failures:** a level failure while steam or a pump controller is out of order goes straight to emergency stop.
- **MODE(m) payload:** a separate `ModeIndicator` enum.

**Unresolved:** announcements and acknowledgements are outside the extracted state machine. The model's guards are uninterpreted sensor functions, so the verifiers check mode logic, not the detection arithmetic. The evacuation valve's drain rate is unknown, so the level check has no lower bound while the valve is open.
