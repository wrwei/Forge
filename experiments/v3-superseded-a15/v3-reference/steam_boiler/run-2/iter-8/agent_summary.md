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
- regulation: all four pumps on below N1, off above N2; initialization fills with all four;
- in rescue, any defective pump controller forces emergency stop;
- STOP/transmission failure apply before STEAM_BOILER_WAITING;
- failure announcements and repair acks come from the sensor;
- emergency stop terminal.

## Iterations

- **It 1–4:** fixed a literal-name clash (`MODE_*`), missing annotations, bare-alias predicates and
  unparenthesised predicate expansion (one operator per predicate, other-operator references only
  negated). Guards read boolean field getters; one emergency branch per mode (29 transitions).
- **It 5–7:** Isabelle kept typing the risk query as real. Renamed, reshaped, split and re-merged it;
  its sources are now computed from fields only (`predictReachableLevels`). Ten guard
  functions remain.

## Iteration 8 (cap lifted by Amendment 13)

At iteration 7, fdr4 and isabelle_verify failed: `levelMayLeaveSafeRange` was typed `unit ⇒ ℝ`. In
iterations 4–7, every real-typed function occurred only on a continuation line of a multi-line
predicate; every boolean-typed one occurs on a single-line declaration. Every predicate and branch
condition in `step()` is now on one line. Logic unchanged.

**Possible pipeline defects:** a function's type seems to depend on the source line its call is
on; positive predicate references are expanded unparenthesised; alias predicates keep their own name.
