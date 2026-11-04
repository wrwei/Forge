# Steam Boiler, run 4: iteration 1

**What I wrote.** I wrote 14 files under `steam_boiler/`.

- `SteamBoilerController.step` has five modes. Every transition is triggered by the per-cycle event `TransmissionReceived`, guarded by named predicates over sensor queries and three booleans (plant waiting, valve open, ready announced), and sends MODE.
- `BoilerSensor` does the receive stage, failure detection (FR1–FR5), unit bookkeeping and acknowledgements (V9, FR6, FR7), the level bounds (V8), the stop count (V10) and the worst-case prediction (V4, Beh13).
- `BoilerActuator` collects the cycle's output, and `SteamBoilerProgram` runs receive, analyse and transmit.
- The rest are the events, the domain types and the constants.

Keeping the bookkeeping out of the controller keeps the extracted model small. Each mode's guards are mutually exclusive and together cover every case, in SB-FR8's order.

**Decisions the requirements left open:**
- Constant values (invented): C=1000, M1=100, N1=400, N2=600, M2=900, W=10, U1=U2=1, P=5.
- Regulation: all pumps open below N1, all closed above N2; initialization also opens all four pumps.
- Emergency stop sends MODE once, then the program stops (Beh22 against Beh24).
- Before STEAM_BOILER_WAITING the program still sends MODE and checks STOP and transmission failures.
- A level failure while the steam device or any pump controller is out of order goes to emergency stop, not rescue (FR8).
- Aberrant messages: duplicates, an invalid pump number, PHYSICAL_UNITS_READY before PROGRAM_READY, an initialization message outside initialization, an unexpected acknowledgement, and a repair of a sound unit.
- An open valve removes the lower bound of the level estimate.
- The two-transmission delay in FR2 is applied to stop orders too.

All are listed in `post_codegen.md`.

**Unresolved:** none so far.
