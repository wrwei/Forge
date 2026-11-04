# Steam boiler, pass 10, iteration 1

No Java was present at the start.

## What was written

14 files under `steam_boiler/`:

- `SteamBoilerController.step`: five modes, 40 transitions, all triggered once per cycle by `InputEvent.CycleStart` (SB-V3: a cycle's messages are simultaneous). Each mode's guards are mutually exclusive and cover every case. Emergency stop is terminal. Every branch sends MODE.
- `BoilerSensor`: the analysed facts the guards read.
- `PlantMonitor` (Java only, outside the model): message reception, failure detection (SB-FR1 to FR5), the repair and acknowledgement protocol (SB-FR6, FR7), and q, v, p, the level interval and the STOP count.
- `SteamBoilerProgram`: runs the receive, analyse and transmit stages; appends the per-unit announcements after `step`.

## Decisions the requirements left open

- **Constants:** C=1000, M1=100, N1=400, N2=600, M2=900 litres; W=10 l/s; U1=U2=1 l/s²; P=5 l/s. Pumps are numbered 0 to 3.
- **Regulation:** all four pumps on below N1, all off above N2. Rescue uses the middle of the estimated interval.
- **Risk:** the worst-case range at the next cycle reaches M1 or M2. A pump not yet on adds nothing for one cycle.
- **Before STEAM_BOILER_WAITING:** only STOP counting and MODE (SB-Beh2 against SB-Beh9).
- **Emergency stop:** MODE on entry, then the program stops (SB-Beh22 against SB-Beh24).
- **SB-Beh21:** "pump-controller units fail" read as any one controller.
- **Level fails while rescue cannot run:** straight to emergency (SB-FR8).
- **Aberrant messages:** duplicates, bad pump numbers, initialization messages outside initialization, PHYSICAL_UNITS_READY before PROGRAM_READY, a repair report for a sound unit. Stray acknowledgements are ignored.

## Unresolved

The model checks only the mode logic; the detection rules reach it as opaque sensor functions.

Details: `post_codegen.md`. A throwaway scratchpad simulation exercised initialization, rescue, degraded, the repairs, STOP×3 and a missing message.
