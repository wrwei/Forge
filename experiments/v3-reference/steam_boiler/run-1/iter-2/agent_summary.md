# Steam Boiler, run 1: summary (iteration 2)

## What was written
- `SteamBoilerController` contains the five modes. Each transition is triggered by the per-cycle `Transmission` event, and each mode's guards are exclusive and exhaustive.
- `PlantMonitor` (Java) handles reception, failure detection, unit status, the estimate, announcements and acknowledgements, and writes boolean verdicts into `BoilerSensor`.
- `BoilerActuator` collects output messages; `SteamBoilerProgram` runs the cycle.

## Changes for iteration 2
- **Preflight:** every double field and parameter in `PlantMonitor` is annotated.
- **FDR4 (over 40 GB), Isabelle (deadlock_free timeout), vacuity (parser recursion):**
  - Guards read ten boolean verdicts instead of layered predicates over real-valued readings, so each guard has at most about seven literals.
  - Mode-dependent aberration checks moved into the monitor.
  - Regulation runs on every non-emergency operating cycle, so `levelAtRisk` is one boolean.

## Decisions the requirements left open
- **Constants (invented):** C=1000, M1=150, M2=850, N1=400, N2=600, W=25, P=10, U1=U2=2.
- **Regulation:** bang-bang on pumps 1–4, against the best known level (the reading, or the estimate's midpoint).
- **Risk:** worst-case bounds for the next cycle; pumps that this cycle closes are excluded.
- **Before STEAM_BOILER_WAITING:** only STOP×3, transmission failure, or a premature PHYSICAL_UNITS_READY ends initialisation.
- **Emergency stop:** MODE is sent once; then the program stops.
- **Rescue:** any pump-controller or steam failure makes it impossible; a level failure then means emergency stop.
- **Aberrant:** duplicates, missing data, bad pump numbers, unexpected repairs/acknowledgements, stray initialisation messages.
- **Initialisation:** pump 1 fills; the lower level bound is ignored while the valve drains.

## Unresolved
- Detection, the acknowledgement protocol (SB-FR1–FR7, SB-V9) and the risk arithmetic are outside the extracted model, so the verifiers do not cover them.
- Note: iteration 1's runner console printed a traceback naming a `forge.dashboard` file; that source was not opened.
