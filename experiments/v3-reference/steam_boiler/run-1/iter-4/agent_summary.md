# Steam Boiler, run 1: summary (iteration 4)

## What was written
- `SteamBoilerController` contains the five modes, every mode transition, the MODE broadcast and the PROGRAM_READY handshake: 19 transitions, all triggered by the per-cycle `Transmission`. Each guard combines at most three boolean verdicts, and each mode ends with an event-only stay branch.
- `PlantMonitor` (Java) handles reception, failure detection, unit status, the estimate and the announcement/acknowledgement messages, and writes the verdicts to `BoilerSensor`.
- `PumpRegulator` (Java) issues the valve and pump commands after each step.
- `BoilerActuator` collects output messages; `SteamBoilerProgram` runs the cycle.

## Changes
- **Iteration 2:** doubles annotated; boolean verdicts instead of real-valued readings. Preflight and vacuity then passed.
- **Iteration 3:** event-only stay branches; numeric members removed from the sensor. Isabelle still timed out and FDR4 still exceeded 40 GB.
- **Iteration 4:** the per-cycle valve and pump commands (29–42 transitions) moved from the state machine into `PumpRegulator`, without changing behaviour, to shrink the model the verifiers check.

## Decisions the requirements left open
- **Constants (invented):** C=1000, M1=150, M2=850, N1=400, N2=600, W=25, P=10, U1=U2=2.
- **Regulation:** bang-bang on pumps 1–4 against the best known level; pump 1 fills during initialisation.
- **Risk:** worst-case bounds for the next cycle.
- **Before STEAM_BOILER_WAITING:** only STOP×3 or transmission failure ends initialisation.
- **Emergency stop:** MODE sent once; then nothing.
- **Rescue:** any pump-controller or steam failure rules it out.
- **Aberrant:** duplicates, missing data, bad pump numbers, unexpected repairs/acknowledgements, stray initialisation messages.

## Unresolved
- The verified model now excludes detection, the acknowledgement protocol, the risk arithmetic and the valve and pump commands.
- Iteration 1's console showed a traceback naming a `forge.dashboard` file; it was not opened.
