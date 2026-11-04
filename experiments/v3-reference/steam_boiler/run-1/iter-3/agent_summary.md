# Steam Boiler, run 1: summary (iteration 3)

## What was written
- `SteamBoilerController` contains the five modes. Each transition is triggered by the per-cycle `Transmission` event, and each mode's guards are exclusive and exhaustive.
- `PlantMonitor` (Java) handles reception, failure detection, unit status, the estimate, announcements and acknowledgements, and writes boolean verdicts into `BoilerSensor`.
- `BoilerActuator` collects output messages; `SteamBoilerProgram` runs the cycle.

## Changes
- **Iteration 2:** doubles annotated; guards read ten boolean verdicts instead of real-valued readings; aberration checks moved to the monitor; regulation on every operating cycle. Preflight and vacuity then passed.
- **Iteration 3:**
  - Isabelle: each mode's last (stay) branch tests only the event, and emergency stop has an empty event-only self-loop. Same Java behaviour.
  - FDR4 (still over 40 GB): q, v, p, the estimate and the STOP count moved from `BoilerSensor` into `PlantMonitor`, so the sensor exposes only booleans.

## Decisions the requirements left open
- **Constants (invented):** C=1000, M1=150, M2=850, N1=400, N2=600, W=25, P=10, U1=U2=2.
- **Regulation:** bang-bang on pumps 1–4, against the best known level (the reading, or the estimate's midpoint).
- **Risk:** worst-case bounds for the next cycle; pumps that this cycle closes are excluded.
- **Before STEAM_BOILER_WAITING:** only STOP×3 or transmission failure ends initialisation.
- **Emergency stop:** MODE sent once; then the program stops.
- **Rescue:** any pump-controller or steam failure makes it impossible; a level failure then means emergency stop.
- **Aberrant:** duplicates, missing data, bad pump numbers, unexpected repairs/acknowledgements, stray initialisation messages.
- **Initialisation:** pump 1 fills; no lower level bound while draining.

## Unresolved
- Detection, the acknowledgement protocol (SB-FR1–FR7, SB-V9) and the risk arithmetic are outside the extracted model, so the verifiers do not cover them.
- Iteration 1's runner console showed a traceback naming a `forge.dashboard` file; it was not opened.
