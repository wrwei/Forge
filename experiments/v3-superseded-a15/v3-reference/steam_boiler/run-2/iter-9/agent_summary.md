# ITER_SUMMARY — steam_boiler, run 2

## Design

13 Java files (none existed). `SteamBoilerController`: mode
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

- **It 1–4:** fixed a literal-name clash (`MODE_*`), missing annotations, bare-alias predicates and
  unparenthesised predicate expansion (one operator per predicate, other-operator references only
  negated). Guards read boolean field getters; one emergency branch per mode (29 transitions).
- **It 5–7:** the risk query stayed real-typed in Isabelle (renamed, reshaped, split, re-merged,
  sources cleaned). Iteration 7 ended non-converged at the cap (fdr4, isabelle_verify).

## Iterations 8–9 (cap lifted by Amendment 13)

- **It 8:** put every predicate on one source line (refuted).
- **It 9:** the one constant across it 4–8 is that the real-typed function was the only one no
  initialization guard uses. The risk check now lives inside `emergencyDemanded()` (STOP ×3,
  transmission failure, or level risk once initialization is over). `otherUnitsSound` is split
  into `pumpsSound` and `estimateTrustworthy`. All nine guard functions now occur in initialization
  guards; behaviour unchanged.

**Possible pipeline defects:** functions used only by non-initial states typed real; positive
predicate references expanded unparenthesised; alias predicates keep their own name.
