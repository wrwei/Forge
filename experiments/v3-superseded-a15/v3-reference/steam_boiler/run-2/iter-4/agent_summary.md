# ITER_SUMMARY — steam_boiler, run 2

## Design

13 Java files (none existed before). `SteamBoilerController`: mode
enum `BoilerMode`, state variables `plantWaiting`, `valveOpen`, `programReadySent`. It is stepped once per
cycle on one event, `MessagesReceived` (a cycle's messages arrive together); guards read boolean
`BoilerSensor` queries. The sensor holds failure
detection, unit status, the STOP count and the level bounds. Guards per mode are
disjoint and exhaustive (SB-FR8 precedence).

Decisions the requirements left open (details in `post_codegen.md`):
- constant values invented (C=1000, M1=150, M2=800, N1=400, N2=600, W=25, U1=U2=4, P=10);
- level risk = worst-case level reachable next cycle crosses M1/M2;
- regulation: all four pumps on below N1, off above N2; initialization fills with all four;
- in rescue, any defective pump controller forces emergency stop;
- STOP and transmission failure apply before STEAM_BOILER_WAITING;
- failure announcements and repair acks come from the sensor;
- emergency stop is terminal.

## Iterations

- **It 1 → 2:** the `OperatingMode` literals shared names with the states (ambiguous in Dafny/Isabelle):
  renamed `MODE_*`. Private doubles annotated. FDR4's
  compilation exploded, so the real-valued guard functions became booleans in the sensor.
- **It 2 → 3:** Isabelle typed a bare-alias predicate as a real function and FDR4 was killed
  (exit -9). Every predicate became compound; one emergency branch per mode (29 transitions).
- **It 3 → 4:** Isabelle typed sensor queries with a computed body (`operatorStopDemanded`,
  `steamDetected`) as real. Probably also FDR4's explosion. Dafny
  found predicates expanded without parentheses ("ambiguous && and ||"). Now every queried method
  returns a boolean field, and each predicate uses one operator, with other-operator predicates
  referenced only negated. The init emergency guard is `!initCalm`.

**Possible pipeline defects:** positive predicate references are expanded unparenthesised; a sensor
function whose body is not a field read is typed real; an alias predicate keeps its own name.
