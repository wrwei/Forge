# ITER_SUMMARY — steam_boiler, run 2

## Design

13 Java files (none existed before). `SteamBoilerController`: mode
enum `BoilerMode`, state variables `plantWaiting`, `valveOpen`, `programReadySent`. A cycle's
messages arrive together, so the controller is stepped once per cycle on one event,
`MessagesReceived`, and its guards read boolean `BoilerSensor` queries. The sensor holds failure
detection, unit status, the STOP count and the level bounds. Guards per mode are
disjoint and exhaustive (SB-FR8 precedence).

Decisions the requirements left open (details in `post_codegen.md`):
- constant values invented (C=1000, M1=150, M2=800, N1=400, N2=600, W=25, U1=U2=4, P=10);
- level risk = the worst-case level reachable within the next cycle crosses M1/M2, with pump latency;
- regulation: all four pumps on below N1, off above N2; initialization fills with all four;
- in rescue, any defective pump controller forces emergency stop;
- STOP and transmission failure apply before STEAM_BOILER_WAITING;
- failure announcements and repair acknowledgements come from the sensor;
- emergency stop is terminal.

## Iterations

- **It 1 → 2:** the `OperatingMode` literals shared names with the states (ambiguous in Dafny/Isabelle):
  renamed `MODE_*`. Private doubles were annotated (preflight). FDR4's
  compilation exploded, so the real-valued guard functions became booleans in the sensor.
- **It 2 → 3:** Isabelle rendered a bare-alias predicate (`transmissionFailed = sensor.transmissionFailure()`)
  as an undeclared real-typed function `transmissionFailed ()`. FDR4 was killed again (exit -9),
  plausibly for this reason. No predicate is now a bare sensor call: every one is a compound
  expression. Each mode has one merged emergency branch (39 → 29 transitions), and the STOP count
  reaches the guards as a boolean, so the model has no numeric functions.

**Possible pipeline defect:** a predicate whose right-hand side is a single sensor call is rendered
under the predicate's name instead of the sensor function's. Worked around in the Java.
