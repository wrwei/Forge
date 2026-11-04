# Iteration 1 summary: steam_boiler

**Written.** 14 Java files under `steam_boiler/`. One controller, `SteamBoilerController.step`, runs once per cycle on an `InputEvent.Transmission` trigger, since SB-V3 makes a cycle's messages simultaneous. `BoilerSensor` receives each transmission and exposes cached results as zero-argument getters: STOP count, transmission failure, unit status, q, v, p, level bounds and worst-case levels. The profile restricts guards to sensor calls and constants. `PumpMonitor` and `UnitStatus` implement FR1–FR4, FR6 and FR7. `SteamBoilerProgram` runs receive → analyse → step → transmit. Each guard excludes the higher-priority guards (FR8). The trace covers all 53 requirements.

**Decisions the requirements leave open.**
- I invented C, M1, M2, N1, N2, W, U1, U2 and P. Every constant used in a guard is an integer.
- Initialization uses four controller states (waiting, filling, draining, ready), all reporting MODE(initialization). The MODE payload is a separate five-value enum.
- Pumps switch as a group. All four open at start-up ("a pump"), and regulation is bang-bang on N1/N2 (rescue uses the estimate's midpoint).
- Level risk is judged one cycle ahead: worst-case steam, no guaranteed pump water, start-up latency respected.
- Aberrant messages: PHYSICAL_UNITS_READY before PROGRAM_READY, start-up messages after initialization, unsolicited acknowledgements or repairs, and invalid pump numbers.
- One failed pump controller ends rescue (SB-Beh21).
- Emergency stop is absorbing and only re-sends MODE (SB-Beh22 vs SB-Beh24).
- Failure detection starts at STEAM_BOILER_WAITING. The steam-zero check repeats on every later initialization cycle.

**Unresolved.** Failure announcements and repair acknowledgements are generated outside the state machine, so the model lacks them. The valve's drainage rate is unknown (lower bound 0 while open). With these constants, rescue lasts about ten cycles before emergency stop.

Note: the git context the harness attached showed commit subjects from earlier steam_boiler runs. I ran no git history commands.
