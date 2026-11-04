# Steam Boiler, run 1: iteration summary

## What was written and why
- The scaffold held no Java, so none was deleted.
- `SteamBoilerController` contains the five modes. Each mode's transitions are triggered by the per-cycle `Transmission` event, so no transition is internal, and each mode's guards are exclusive and exhaustive.
- The controller's state is the mode plus three initialisation flags: plant waiting, valve open, PROGRAM_READY sent.
- `BoilerSensor` holds the analysed quantities the controller reads.
- `PlantMonitor` (Java only) handles message reception, the STOP count, all failure detection, unit status, the estimate bounds, and the announcement and acknowledgement messages.
- `BoilerActuator` collects output messages; `SteamBoilerProgram` runs one cycle per call.

## Decisions the requirements left open
- **Constants (invented):** C=1000, M1=150, M2=850, N1=400, N2=600, W=25, P=10, U1=U2=2.
- **Regulation:** bang-bang on all four pumps. Rescue uses the estimate's midpoint. A cycle that changes mode sends only MODE.
- **Risk:** worst-case bounds for the next cycle. The minimum counts only pumps known to pour. The maximum counts running pumps, unless the cycle's branch closes them.
- **Before STEAM_BOILER_WAITING:** only STOP×3, transmission failure, or a premature PHYSICAL_UNITS_READY can end initialisation.
- **Emergency stop:** MODE is sent once; then the program stops (SB-Beh22 over SB-Beh24).
- **Rescue:** any pump-controller or steam failure makes it impossible, and a level failure then goes straight to emergency stop.
- **Aberrant messages:** duplicate or missing data, a bad pump number, unexpected repairs or acknowledgements, initialisation messages elsewhere.
- **Initialisation:** pump 1 fills. The drain rate is unknown, so the lower level bound is dropped while the valve is open.
- **MODE(m) payload:** a separate `OperatingMode` enum.

## Unresolved
- Detection and the acknowledgement protocol (SB-FR1–FR7, SB-V9) are outside the extracted state machine, so the verifiers do not cover them.
