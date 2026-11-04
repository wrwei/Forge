# Iteration 1 — steam_boiler (cold pass)

**Written.** 17 Java files under `steam_boiler/` (no Java was present beforehand), `result_codegen.json` (all 53 requirements), `post_codegen.{md,json}`. Compiles with javac and Gradle.

**Design.** SB-V3 makes a cycle's messages simultaneous, so `SteamBoilerController.step` runs once per cycle on `InputEvent.Transmission`; `TransmissionSensor` reads the incoming messages. The controller has five modes and 42 transitions, all triggered by that event; per mode, guards are exclusive and exhaustive. Per-unit failure detection, the acknowledgement protocol and the level estimate need per-unit conditional updates the profile cannot express as transitions, so they live in `PlantSensor`, which the controller reads as sensor functions. The verifiers therefore check the mode logic, not the detection rules.

**Decisions the requirements left open**
- Constants: C 1000, M1 100, N1 400, N2 600, M2 900 l; W 10 l/s; U1 = U2 = 1 l/s/s; P 5 l/s; pumps numbered 0–3.
- Emergency stop never exits but re-sends MODE every cycle (SB-Beh22 against SB-Beh24).
- Before STEAM_BOILER_WAITING: only MODE and STOP counting.
- Initialization fills with all four pumps, then closes pumps and valve at the first PROGRAM_READY.
- Regulation: all pumps on below N1, off above N2.
- Level risk: worst-case bounds one cycle ahead; a newly started pump pours nothing that cycle.
- The estimate is a pair of bounds advanced each cycle; the valve's drain rate is unknown, so the lower bound is 0 while it is open.
- A level failure while the steam device or a pump controller is already failed goes straight to emergency stop.
- "Pump-controller units fail" means any one.
- Transmission failure: a missing or duplicated data message, an invalid pump number, or an initialization message out of place.

**Unresolved.** Nothing blocks. Open question: is verifying only the mode logic acceptable scope (post_codegen issue 4)?
