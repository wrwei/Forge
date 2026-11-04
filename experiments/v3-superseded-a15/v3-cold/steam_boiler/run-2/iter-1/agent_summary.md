# Iteration 1 summary: steam_boiler, pass 2

The scaffold contained no Java, so nothing was deleted.

## What was written

15 files under `steam_boiler/`:
- `domain`: the enums and records.
- `event`: the sealed `InputEvent` (the 15 SB-DM9 messages plus the per-cycle trigger `TransmissionReceived`) and `OutputEvent` (SB-DM8).
- `controller`: `BoilerConstants` and `SteamBoilerController`.
- `sensor`: `BoilerSensor` and `UnitStatus`.
- `actuator`: `BoilerActuator`.
- `SteamBoilerProgram`: runs the cycles.

The controller has five modes and 41 event-triggered transitions. In each mode the guards are mutually exclusive and cover every case. Emergency stop is terminal.

Once per cycle, `BoilerSensor.receive` decodes the transmission, detects transmission and unit failures (FR1–FR5), keeps the per-unit status table (V9, FR6–FR7) and the STOP count, and computes the level bounds and worst-case predictions. The controller reads the results through simple getters. The profile forces this split: per-unit conditional updates cannot be straight-line transition actions.

The code compiles under Java 17 with no warnings. A jshell plant simulation behaved as expected for filling, regulation, pump failure and repair, rescue, STOP and missing data.

## Decisions the requirements left open

- Constants are invented and integral: C=1000, M1=100, M2=900, N1=400, N2=600, W=15, U1=U2=1, P=5.
- Risk: the low side assumes no pump water next cycle; the high side counts every pump that is on or out of order.
- Regulation: all pumps open below N1 and close above N2, using the middle of the estimate in rescue.
- A level failure while rescue would be unusable goes straight to emergency stop.
- Beh21 "pump-controller units fail" means any controller.
- MODE(emergency stop) is sent once, then the program stops.
- The rest are in `post_codegen.md`.

## Unresolved

The model sees the detection algorithms only as uninterpreted sensor functions, so the verifiers do not check them.
