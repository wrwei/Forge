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
- **It 12–13:** EMERGENCY_STOP got a MODE-reporting self-loop (it had no operation; SB-Beh24);
  `plantWaiting` and `valveOpen` left the controller (sensor's `boilerWaitingSeen()`, an idempotent
  `DrainValve` order). FDR4 now compiles fully; Isabelle's deadlock proof times out.
- **It 14:** each mode's last branch is "none of the others", so guard coverage is syntactic (checked
  over all 256 valuations; behaviour unchanged).

**Possible pipeline defects:** functions used only by non-initial states typed real; positive
predicate references expanded unparenthesised; alias predicates keep their own name.
