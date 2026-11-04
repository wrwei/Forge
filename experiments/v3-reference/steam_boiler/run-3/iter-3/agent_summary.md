# Iteration 3 — steam_boiler, run 3

No Java was present at the start of the run.

## What the Java is

- `SteamBoilerController`: decides the five modes, one `step` per cycle on `TransmissionReceived`, sends MODE every cycle; emergency stop is terminal. 16 transitions over 6 boolean sensor facts, no state besides the mode.
- `LevelRegulator`: orders for the chosen mode (VALVE, OPEN/CLOSE_PUMP, PROGRAM_READY).
- `PlantMonitor`/`PumpUnit`/`UnitHealth`: reception, transmission check, STOP count, q, v, p, estimate, failure judgements, unit status, FR6/FR7 messages.
- `UnitMessage` + `MessageKind`: the units' messages; `SteamBoilerProgram`: the cycle.

## Changes for iteration 2's failures

- FDR4 ran out of memory again (71 GB) with 28 transitions and 12 functions. The in-mode orders now come from `LevelRegulator`, so the controller is a mode manager. Simulations show the same behaviour, except that pumps are now also regulated in the cycle of a mode change.
- Vacuity crashed inside the audit tool (Python recursion error), which I take to be a pipeline defect. The construct new in iteration 2, an interface whose nested classes implement it, is replaced by one flat class and an enum.

## Decisions the requirements left open

- Constants (C=1000, M1=150, M2=850, N1=400, N2=600, W=25, U1=U2=4, P=10); pumps 1–4.
- Regulation switches all four pumps on the level predicted for the end of the coming cycle.
- Initialization fills with pump 1 only.
- A level failure with steam or a pump controller out of order goes straight to emergency stop.
- Within a mode, branches rely on else-if priority and end in an unguarded branch, so the model over-approximates the Java.

## Unresolved

- SB-Beh24 versus SB-Beh22: MODE(EMERGENCY_STOP) is sent once, then the program stops.
- Regulation, failure judgement and FR6/FR7 are outside the extracted model and are not verified.
