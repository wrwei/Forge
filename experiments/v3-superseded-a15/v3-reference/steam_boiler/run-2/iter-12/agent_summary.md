# ITER_SUMMARY — steam_boiler, run 2

## Design

14 Java files (none existed). `SteamBoilerController`: mode
enum `BoilerMode`, state variables `valveOpen`, `programReadySent`. It is stepped once per
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
  with all guard functions in initialization guards (risk and rescue checks folded into
  `emergencyDemanded()`), typing passed. Pump regulation became one `SwitchPumps` order.
- **It 12:** Isabelle reached the deadlock-freedom proof and timed out. EMERGENCY_STOP had no
  operation, so it now reports MODE each cycle (SB-Beh24). `plantWaiting` became the sensor's
  latched `boilerWaitingSeen()`; aberrant initialization messages are judged by the sensor.

FDR4 has never finished compiling (killed at 40–80k processes in every iteration).

**Possible pipeline defects:** functions used only by non-initial states typed real; positive
predicate references expanded unparenthesised; alias predicates keep their own name.
