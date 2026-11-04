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

- **It 1 → 2:** `OperatingMode` literals renamed `MODE_*` (name clash with states); private doubles
  annotated; real-valued guard functions replaced by boolean queries (FDR4 blow-up).
- **It 2 → 3:** no predicate is a bare alias of a sensor call (Isabelle typed one real); one
  emergency branch per mode (29 transitions).
- **It 3 → 4:** Isabelle typed computed-body queries as real (plausibly FDR4's blow-up); Dafny
  found positive predicate references expanded unparenthesised. Queries became field getters, and
  predicates use one operator, with other-operator references only negated.
- **It 4 → 5:** `levelLimitAtRisk` was still real (its flag was assigned a `||` of comparisons).
  Every guard-read flag is now set from literals or single comparisons. The guard functions shrank
  to ten (`haltDemanded`, `estimateTrustworthy`, `startupReadingsPlausible` merge requirement-named
  conditions) to shrink FDR4's model.

**Possible pipeline defects:** unparenthesised predicate expansion; boolean sensor functions typed
real depending on how the returned value is computed; alias predicates keep their own name.
