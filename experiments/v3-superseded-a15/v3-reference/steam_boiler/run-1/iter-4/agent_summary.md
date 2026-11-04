# Iteration 4 — steam_boiler

## Iteration 3 result

Failed: fdr4, isabelle_verify. All other phases passed. Both were timeouts, not type or parse errors:

- FDR4 was still compiling (104000+ processes) after 961 s. The report labels this a "parse error".
- The Isabelle deadlock-freedom proof timed out.

The model was too large: 37 transitions, 14 boolean predicates and real-valued comparisons.

## Changes for iteration 4

- `BoilerSensor` now combines the causes that lead to the same mode change into single judgements:
  - `initialEmergency`: STOP x3, or after STEAM_BOILER_WAITING a transmission failure, non-zero steam or a failed level device;
  - `runningEmergency`: STOP x3, a transmission failure (including start messages outside initialization) or level risk;
  - `estimateUnreliable`: the steam device or a pump controller out of order;
  - also `startConfirmed`, `drainNeeded`, `fillNeeded`.
- `SteamBoilerController` now has 20 transitions over 9 boolean predicates, with no real-valued comparisons. Per mode the guards are exclusive and cover every case, ordered by SB-FR8. Each predicate is used by name in a branch condition.
- Behaviour is unchanged (scratch simulation plus edge cases: non-zero steam at start, PUR before PROGRAM_READY, STOP before SBW, steam failure in rescue). The trace and `post_codegen` are refreshed.

## Decisions the requirements left open

- Invented constants (C=1000, M1=150, N1=400, N2=600, M2=800, W=25, U1=U2=2, P=10).
- Before STEAM_BOILER_WAITING only STOP counts.
- Aberrant messages are listed in `post_codegen.md`.
- "Pump-controller units fail" means any controller.
- A level failure in normal or degraded goes to rescue first (FR8, literal reading).
- Risk and regulation are worst-case and predictive over two cycles.
- MODE(emergency stop) is sent once.

## Unresolved

The extraction drops the MODE payload, so the model does not distinguish mode values. Merged causes share one transition in the model.
