# Iteration 1 — steam_boiler, run 3

No Java was present at the start.

## What I wrote

- `SteamBoilerController`: five modes (initialization first), one `step` per cycle triggered by `TransmissionReceived`. In every mode the guards are named predicates forming an exhaustive, mutually exclusive partition ordered by severity (SB-FR8). MODE is sent on every transition; emergency stop is terminal. State: mode, `boilerWaiting`, `readyAnnounced`, `evacuationValve`.
- `BoilerSensor`: the 14 boolean/nat queries the controller reads.
- `PlantMonitor` / `PumpUnit` / `UnitHealth`: reception, transmission check, STOP count, q, v, p, level estimate, failure judgements, unit status, failure announcements and repair acknowledgements.
- `SteamBoilerProgram`: the receive–analyse–transmit cycle. Events, domain enums and records, constants, and the actuator.

Plant analysis is kept outside the controller so that the extracted model has four state variables and no real arithmetic.

## Decisions the requirements left open

- Constant values (C=1000, M1=150, M2=850, N1=400, N2=600, W=25, U1=U2=4, P=10) and pump numbers 1–4.
- Regulation switches all four pumps together, based on the level predicted for the end of the coming cycle. Regulating on the current level reached the M1 risk at full steam in simulation. Pumps are not ordered in the cycle of a mode change.
- Initialization fills with pump 1 only ("a pump").
- A level failure while the steam device or any pump controller is out of order goes straight to emergency stop.
- The aberrant-message set, the timing of the pump and controller judgements, and the worst-case bounds are listed in `post_codegen.md`.
- The zero-steam check runs on every initialization cycle after STEAM_BOILER_WAITING.

## Unresolved

- SB-Beh24 (MODE on every cycle) versus SB-Beh22 (the program stops): MODE(EMERGENCY_STOP) is sent once, then nothing.
- FR6/FR7 message repetition and the failure judgements are not in the extracted model, so the verifiers do not check them.
