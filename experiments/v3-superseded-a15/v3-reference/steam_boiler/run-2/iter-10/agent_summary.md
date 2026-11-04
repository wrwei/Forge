# ITER_SUMMARY — steam_boiler, run 2

## Design

14 Java files (none existed). `SteamBoilerController`: mode
enum `BoilerMode`, state variables `plantWaiting`, `valveOpen`, `programReadySent`. It is stepped once per
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
- emergency stop terminal.

## Iterations

- **It 1–7:** fixed a literal-name clash (`MODE_*`), missing annotations, bare-alias predicates and
  unparenthesised predicate expansion (one operator per predicate, other-operator references only
  negated). The risk query stayed real-typed whatever its form; non-converged at the old cap.

## Iterations 8–10 (cap lifted by Amendment 13)

- **It 8:** every predicate on one source line (refuted).
- **It 9:** the real-typed function was always the only one no initialization guard uses. Risk was
  folded into `emergencyDemanded()` (after initialization), and `otherUnitsSound` was split into
  `pumpsSound`/`estimateTrustworthy`. The ℝ errors vanished; Isabelle then hit `exception Empty`
  on an initialization operation (three-level nesting). FDR4 still did not finish.
- **It 10:** nesting back to two levels. Pump regulation is one `SwitchPumps(ALL_ON|ALL_OFF)` order
  that the actuator sends per pump (75 → 45 output events). The start-up steam/level check is
  inside `emergencyDemanded()`; eight guard functions remain.

**Possible pipeline defects:** functions used only by non-initial states typed real; positive
predicate references expanded unparenthesised; alias predicates keep their own name.
