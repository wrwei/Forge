# ITER_SUMMARY: steam_boiler, pass 3

No Java was present before this pass; nothing was deleted.

**Written** (under `steam_boiler`):

- `domain`: mode, pump, flow and valve enums; two pump report records.
- `event`: `InputEvent` (15 messages plus `TransmissionReceived`) and `OutputEvent` (13 messages).
- `controller`: constants and `SteamBoilerController`.
- `sensor`: `IncomingTransmission` (decoder) and `BoilerSensor` (measures, level bounds, unit status, failure detection, STOP count).
- `actuator`: `BoilerActuator`.
- `SteamBoilerProgram`: the cycle (receive, analyse, step, transmit).

The controller's state is the mode plus three initialization fields. Every transition fires on `TransmissionReceived`, so there are no guard-only transitions. Each mode's guards are exclusive and exhaustive, ordered emergency stop, rescue, degraded (SB-FR8). The trace has 229 entries covering all 53 requirements.

**Open decisions:**
- Invented constants: C 1000, M1 125, M2 840, N1 400, N2 600, W 25, U1 = U2 = 2, P 8. They satisfy SB-DM1.
- q, v, p, level bounds, unit status and the STOP count live in `BoilerSensor` (functions in the model).
- Regulation: close all pumps above N2 or when keeping them risks M2; open all below N1; otherwise hold. Rescue uses the estimate's midpoint.
- Risk is the next cycle's worst case, given this cycle's pump decision and start-up latency.
- Initialization fills with pump 1; steam must be zero after STEAM_BOILER_WAITING.
- Out-of-place handshakes, duplicates, invalid pump numbers and unexpected repairs or acknowledgements are transmission failures.
- In rescue, a failed steam device or pump controller means emergency stop.
- `SteamBoilerProgram` sends failure announcements and repair acknowledgements, outside the model.
- Emergency stop is terminal.

**Unresolved:**
- A level failure coinciding with a steam or controller failure goes to rescue first (literal SB-FR8).
- The valve's drain rate is unknown, so the low-side level check is skipped while it is open.

Details: `post_codegen.md`.
