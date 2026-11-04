# ITER_SUMMARY — steam_boiler, run 1, iteration 1

No Java was present before writing. I wrote 16 files under `steam_boiler/`.

**What and why.** The design has one controller, `SteamBoilerController`, with modes INITIALIZATION, NORMAL, DEGRADED, RESCUE and EMERGENCY_STOP. Its `step` runs once per cycle on `TransmissionReceived`. `SteamBoilerProgram.cycle` first gives every incoming message to `BoilerSensor`, which judges the transmission, the pumps and pump controllers (via `PumpMonitor`), the level and steam devices, and each unit's status (`UnitStatus`). It also advances an interval estimate of the level and the worst-case reachable range. Every transition consumes the cycle event, so no guard-only cycle can diverge. Within each mode the guards are written to exclude each other and together cover every case. Controller state is limited to the mode, the valve position, a filling-pump flag and the last ordered pump. None of these is updated by arithmetic.

**Decisions the requirements left open** (details in `post_codegen.md`):
- Invented constant values: C=1000, M1=150, N1=400, N2=600, M2=850, W=25, P=10, U1=U2=2. Pumps are numbered 1–4.
- At most one pump is switched per cycle. A cycle that changes mode sends only MODE.
- Failure announcements and repair acknowledgements are emitted by the sensor's bookkeeping, not by the state machine.
- A level failure while the steam device or any pump controller is out of order goes straight to emergency stop.
- "Aberrant message" is defined in `post_codegen.md`. Transmission failure and STOP also apply before STEAM_BOILER_WAITING.
- The valve's drain rate is unknown, so the lower level bound is 0 while the valve is open.
- MODE(emergency stop) is sent once; after that the program is silent.

**Unresolved.** Field references inside guard predicates (`evacuationValve`, `fillingPumpOn`) go beyond the rule's literal list. I don't know how the verifiers treat sensor functions compared against large constants.
