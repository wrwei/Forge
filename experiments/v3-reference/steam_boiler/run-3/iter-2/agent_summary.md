# Iteration 2 — steam_boiler, run 3

No Java was present at the start of the run.

## What the Java is

- `SteamBoilerController`: five modes, one `step` per cycle on `TransmissionReceived`, named predicates, MODE on every transition, emergency stop terminal. State: mode, `readyAnnounced`, `evacuationValve`.
- `BoilerSensor`: 12 boolean queries.
- `PlantMonitor`/`PumpUnit`/`UnitHealth`: reception, transmission check, STOP count, q, v, p, estimate, failure judgements, unit status, announcements and repair acknowledgements.
- `UnitMessage`: the units' messages; `SteamBoilerProgram`: the cycle.

## Changes for iteration 1's failures

- Preflight: annotated a `double` parameter and the `double` return types.
- FDR4 (memory exhausted, 81 GB): the units' messages are no longer `InputEvent` records, leaving one input event; fewer, merged sensor queries; shorter guards; `boilerWaiting` replaced by the monitor's latch. Behaviour is unchanged, as a replay of the same plant simulations confirmed.
- Isabelle (`deadlock_free` timeout): each mode's branches now rely on else-if priority, and the last branch tests only the event. The model therefore over-approximates the Java.

## Decisions the requirements left open

- Constants (C=1000, M1=150, M2=850, N1=400, N2=600, W=25, U1=U2=4, P=10); pumps 1–4.
- Regulation switches all four pumps on the level predicted for the end of the coming cycle, but not in the cycle of a mode change.
- Initialization fills with pump 1 only.
- A level failure with steam or a pump controller out of order goes straight to emergency stop.
- The aberrant-message set, judgement timing and worst-case bounds are in `post_codegen.md`.

## Unresolved

- SB-Beh24 versus SB-Beh22: MODE(EMERGENCY_STOP) is sent once, then the program stops.
- FR6/FR7 and the failure judgements are outside the extracted model and are not verified.
