# ITER_SUMMARY — steam_boiler, run 2

## Design (iteration 1)

13 Java files under `steam_boiler/` (no Java was present beforehand). `SteamBoilerController`: mode
enum `BoilerMode`, 39 transitions, state variables `plantWaiting`, `valveOpen`, `programReadySent`.
A cycle's messages arrive together, so the controller is stepped once per cycle on one event,
`MessagesReceived`, and its guards read `BoilerSensor` queries. The sensor holds failure detection
(FR1–FR5), unit status, the STOP count and the level bounds, which keeps the model small. Guards in
each mode are disjoint and exhaustive, ordered emergency > rescue > degraded (SB-FR8).

Decisions the requirements left open (details in `post_codegen.md`):
- constant values invented (C=1000, M1=150, M2=800, N1=400, N2=600, W=25, U1=U2=4, P=10);
- level risk = the worst-case level reachable within the next cycle crosses M1/M2, with pump latency;
- regulation: all four pumps on below N1, off above N2; initialization fills with all four;
- in rescue, any defective pump controller forces emergency stop;
- STOP and transmission failure apply even before STEAM_BOILER_WAITING;
- failure announcements and repair acknowledgements are sent by the sensor, outside the state machine;
- emergency stop is terminal.

## Iteration 2

Failures in iteration 1 and fixes:
- **Dafny, Isabelle:** the state names were ambiguous because `OperatingMode` (the MODE(m) payload)
  reused `BoilerMode`'s literals. Renamed to `MODE_INITIALIZATION` … `MODE_EMERGENCY_STOP`.
- **Preflight:** private double parameters and returns in `BoilerSensor` now carry
  `@RoboChartType("real")`.
- **FDR4:** compilation grew past 10000 processes without finishing. I attribute this to the
  real-valued sensor functions in the guards. The level, reachable-limit and steam comparisons are
  now booleans computed in the sensor (`levelBelowNormalBand`, `levelAboveNormalBand`,
  `levelLimitAtRisk`, `steamDetected`). Controller logic is unchanged; simulated scenarios behave
  as before.

Unresolved: nothing known.
