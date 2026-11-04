# Iteration 1 — steam_boiler

No Java was present at start; nothing deleted.

## What was written

- `controller/SteamBoilerController`: the only state machine. Its five modes (INITIALIZATION first) are written as one `step`. Every transition is triggered by `TransmissionReceived` (end of the receive stage), so no guard-only transition can diverge. In each mode the guards are exclusive and together cover every case, ordered by SB-FR8 (stop, transmission, level risk, rescue, degraded). EMERGENCY_STOP is terminal.
- `sensor/BoilerSensor` (+ `UnitStatus`): receive and analysis stages. Holds q, v, p, the level bounds, unit status and the STOP count, and runs the FR1–FR5 checks. The controller only calls trivial getters.
- `actuator/BoilerActuator`: the outbox, valve and pump commands, and the FR6/FR7 messages. `SteamBoilerProgram`: the cycle driver.
- Events and payload types follow SB-DM8/DM9. `ModeValue` is the MODE payload.

## Decisions the requirements left open

- Constant values (C=1000, M1=150, N1=400, N2=600, M2=800, W=25, U1=U2=2, P=10) are invented.
- Before STEAM_BOILER_WAITING only STOP counts; transmission failure is judged from then on.
- Aberrant messages: duplicates, bad pump numbers, PHYSICAL_UNITS_READY before PROGRAM_READY, an acknowledgement or repair for the wrong unit, start messages outside initialization.
- "Pump-controller units fail" (Beh21) is read as any controller.
- A level failure in normal or degraded goes to rescue first, even with a steam failure (FR8, literal reading).
- Level risk uses worst-case steam with the best pump action. Regulation is predictive over two cycles, to allow for the pump start-up latency.
- MODE(emergency stop) is sent once; then the program stops.
- Per-unit protocol messages are outside the extracted model.

Details are in `post_codegen.md`.

## Unresolved

None known.
