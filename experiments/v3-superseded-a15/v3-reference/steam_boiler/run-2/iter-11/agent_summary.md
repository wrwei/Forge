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

- **It 1–7:** fixed a name clash, annotations, alias predicates and unparenthesised predicate
  expansion. A risk query stayed real-typed; non-converged at the old cap.
- **It 8–9 (cap lifted):** the real-typed function was always the only one no initialization guard
  used. With every function in an initialization guard the ℝ errors vanished; Isabelle then raised
  `exception Empty` on INIT → NORMAL once its guard called `pumpsSound`/`estimateTrustworthy`.
- **It 10:** one `SwitchPumps` order per branch (75 → 45 output events); `Empty` persisted.
- **It 11:** INIT → NORMAL back to its it-8 form. Rescue's untrustworthy-estimate condition is in
  `emergencyDemanded()`, so level plus steam/controller failure goes straight to emergency stop
  (SB-FR8). Seven guard functions.

FDR4 has never finished compiling (killed at 40–60k processes in every iteration).

**Possible pipeline defects:** functions used only by non-initial states typed real; positive
predicate references expanded unparenthesised; alias predicates keep their own name.
