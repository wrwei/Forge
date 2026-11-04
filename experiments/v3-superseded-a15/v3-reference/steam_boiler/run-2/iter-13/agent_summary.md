# ITER_SUMMARY — steam_boiler, run 2

## Design

15 Java files (none existed). `SteamBoilerController`: mode
enum `BoilerMode`, state variable `programReadySent`. It is stepped once per
cycle on one event, `MessagesReceived` (a cycle's messages arrive together); guards read boolean
`BoilerSensor` queries. The sensor holds failure
detection, unit status, the STOP count and the level bounds. Guards per mode are
disjoint and exhaustive (SB-FR8 precedence).

Decisions the requirements left open (see `post_codegen.md`):
- constant values invented (C=1000, M1=150, M2=800, N1=400, N2=600, W=25, U1=U2=4, P=10);
- level risk = worst-case level reachable next cycle crosses M1/M2;
- regulation: all four pumps on below N1, off above N2;
- in rescue, any defective pump controller forces emergency stop;
- STOP/transmission failure apply before STEAM_BOILER_WAITING;
- failure announcements and repair acks come from the sensor;
- emergency stop: no control, only MODE each cycle;

## Iterations

- **It 1–7:** fixed a name clash, annotations, alias predicates and unparenthesised predicate
  expansion; a risk query stayed real-typed (non-converged at the old cap).
- **It 8–11 (cap lifted):** the real-typed function was always one no initialization guard used;
  with every guard function in an initialization guard, typing passed.
- **It 12:** EMERGENCY_STOP had no operation (deadlock proof timed out), so it now reports MODE each
  cycle (SB-Beh24); `plantWaiting` became the sensor's `boilerWaitingSeen()`.
- **It 13:** FDR4 compiled for the first time (57,703 processes) but was killed exploring. The valve
  became an idempotent `DrainValve` order the actuator turns into VALVE edges (controller
  `valveOpen` removed, 11 → 8 initialization transitions); every guard is one flat predicate.

**Possible pipeline defects:** functions used only by non-initial states typed real; positive
predicate references expanded unparenthesised; alias predicates keep their own name.
