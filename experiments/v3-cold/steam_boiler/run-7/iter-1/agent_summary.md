# Iteration 1 — steam_boiler, pass 7

No Java was present beforehand.

## What I wrote

Sixteen files under `steam_boiler/`. `SteamBoilerController` is the mode machine: five modes, one `step` per cycle, and in each mode guards that are exclusive and complete. Emergency stop takes precedence over rescue, and rescue over degraded (FR8). Its only state variables are `plantWaiting`, `readyAnnounced` and `valveOpen`.

`CycleAnalyser` checks each transmission, detects unit failures (FR1–FR4), keeps each unit's status, counts STOPs, bounds the level, and composes the FAILURE_DETECTION and REPAIRED_ACKNOWLEDGEMENT messages. It publishes aggregates through `BoilerSensor`. `SteamBoilerProgram` runs each cycle: receive, analyse, step, transmit.

Why: profile actions cannot be conditional, so per-unit messages cannot live in the state machine. The verifiers see these results as inputs to the mode logic.

## Decisions the requirements left open

- Invented constants: C=1000, M1=100, M2=900, N1=400, N2=600, W=10, U1=U2=1, P=5, all integers.
- The cycle trigger `TransmissionReceived` is not an SB-DM9 message.
- Regulation turns all four pumps on below N1 and off above N2; rescue uses the estimate's midpoint. Cycles that change mode send only MODE.
- Risk: the worst-case level after one cycle reaches M1 or M2.
- "Pump-controller units fail" means any one controller. Losing the level device while the estimate is already unreliable goes straight to emergency stop.
- Aberrant messages: missing or duplicated data, a bad pump number, misplaced initialization messages. Stray repairs and acknowledgements are tolerated.
- Before STEAM_BOILER_WAITING, only STOP is acted on.
- A controller is judged from the second transmission after an order that changes its pump's state.
- Emergency stop is terminal: one MODE message, then nothing.
- Readings keep their last value; q and v start at 0.

## Unresolved

Nothing in the requirements. Untested: whether the profile accepts controller fields inside predicates.
