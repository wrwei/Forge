# Iteration 1 summary: steam_boiler (cold pass)

## What was written
14 Java files under `steam_boiler/`. `SteamBoilerController.step` has five modes and 41 transitions, all triggered by `TransmissionReceived` (no guard-only cycles). Guards within a mode are mutually exclusive, ordered stop > hazard > rescue > degraded > regulation (FR8). Emergency stop is terminal. Controller fields: `boilerWaitingSeen`, `programReadySent`, `valveOpen`.

`BoilerSensor` runs the receive stage and the FR1–FR5 judgements. It also keeps the unit bookkeeping (V9, through `UnitHealth`), the STOP count, q, v and p, and the interval estimate and prediction (V8, Beh19, Beh25). It emits the FR6/FR7 detection and acknowledgement messages. `SteamBoilerProgram` runs receive, analyse, transmit. The trace maps 53 requirements in 198 entries.

## Decisions the requirements left open
- **Constants:** C=1000, M1=125, N1=400, N2=600, M2=800, W=25, U1=U2=2, P=10, cycle 5 s.
- **Pumps:** numbered 1..4.
- **Regulation:** a target pump count; at most one pump order per cycle; no pump order on a cycle that changes mode.
- **Risk:** the next-cycle low bound counts only water known to be supplied, and the high bound counts every running or untrusted pump. Emergency stop at ≤M1 or ≥M2.
- **Level plausibility (FR3):** assumes any pump may stop.
- **Rescue:** if the steam device or any pump controller is out of order, a level failure goes straight to emergency stop.
- **Aberrant messages (FR5):** duplicates; bad pump index; misplaced WAITING or READY; a repair for a sound unit; an acknowledgement with no pending detection.
- **Initialization:** only STOP counts before WAITING; filling uses one pump.
- **Emergency stop:** MODE is sent once, then the program stops.

## Unresolved
- The FR6/FR7 messages and the per-unit bookkeeping are outside the RoboChart model.
- The constants are invented.
- Only a scratchpad simulation exercised the main scenarios; there are no unit tests.
