# ITER_SUMMARY — steam_boiler, run 2

## Design

13 Java files (none existed before). `SteamBoilerController`: mode
enum `BoilerMode`, state variables `plantWaiting`, `valveOpen`, `programReadySent`. It is stepped once per
cycle on one event, `MessagesReceived` (a cycle's messages arrive together); guards read boolean
`BoilerSensor` queries. The sensor holds failure
detection, unit status, the STOP count and the level bounds. Guards per mode are
disjoint and exhaustive (SB-FR8 precedence).

Decisions the requirements left open (see `post_codegen.md`):
- constant values invented (C=1000, M1=150, M2=800, N1=400, N2=600, W=25, U1=U2=4, P=10);
- level risk = worst-case level reachable next cycle crosses M1/M2;
- regulation: all four pumps on below N1, off above N2; initialization fills with all four;
- in rescue, any defective pump controller forces emergency stop;
- STOP/transmission failure apply before STEAM_BOILER_WAITING;
- failure announcements and repair acks come from the sensor;
- emergency stop terminal.

## Iterations

- **It 1–5:** fixed a literal-name clash (`MODE_*`), missing annotations, bare-alias predicates and
  unparenthesised predicate expansion (one operator per predicate, other-operator references only
  negated). Guards now read boolean field getters; one emergency branch per mode (29 transitions).
  FDR4 never finished compiling while a guard function was real-typed.
- **It 5 → 6:** Isabelle still typed the risk query (`dangerousLevelPredicted`) as real; only its flag's flag depended on `lowestReachable` and
  `highestReachable`. Those came from unannotated locals and `Math.min`/`max`, so the extractor
  likely traces the returned value's sources. They are now computed from annotated fields only.
  Risk is split into `levelMayFallTooLow`/`levelMayRiseTooHigh`, the same single-comparison shape
  as the band queries that work. `LIMIT_M*` renamed `MINIMAL_M1`/`MAXIMAL_M2`.

**Possible pipeline defects:** unparenthesised predicate expansion; boolean sensor functions typed
real depending on the returned value's provenance; alias predicates keep their own name.
