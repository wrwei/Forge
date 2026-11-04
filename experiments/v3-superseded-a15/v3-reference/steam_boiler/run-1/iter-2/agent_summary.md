# Iteration 2 — steam_boiler

## Iteration 1 result

Failed: preflight, m2t, isabelle_verify; fdr4 had no verdict (blocked by m2t).

## Changes for iteration 2 (Java only; the trace's elements are unchanged)

- Preflight: added `@RoboChartType("real")` (and `"nat"`) to the parameters and returns of the private numeric helpers in `BoilerSensor`.
- M2T: the `PumpStateData` component `on` is a keyword of RoboChart's textual notation, although the rules' reserved-word list doesn't include it. It is renamed `switchedOn`. The `regulatePumps` parameter is renamed `targetCount` so it no longer shares the sensor method name `pumpsRequired`.
- Isabelle: in the generated theory, a compound predicate is inlined, and a bare boolean sensor call inside it was typed `ℝ`. `startMessageOutOfPlace` now combines two predicates (`waitingSignalNow`, `unitsReady`), each holding a single sensor call. Behaviour is unchanged.

## Design (from iteration 1)

- One state machine. Every transition is triggered by `TransmissionReceived`. In each mode the guards are exclusive and cover every case, ordered by SB-FR8. Emergency stop is terminal.
- Analysis and FR1–FR5 are in `BoilerSensor`, the outbox and FR6/FR7 messages in `BoilerActuator`, and the cycle driver is `SteamBoilerProgram`.

## Decisions the requirements left open

- The constant values are invented (C=1000, M1=150, N1=400, N2=600, M2=800, W=25, U1=U2=2, P=10).
- Before STEAM_BOILER_WAITING only STOP counts.
- Aberrant messages are defined in `post_codegen.md`.
- "Pump-controller units fail" means any controller.
- A level failure in normal or degraded goes to rescue first (FR8, literal reading).
- Risk and regulation are worst-case and predictive over two cycles.
- MODE(emergency stop) is sent once.

## Unresolved

None known. `on` missing from the rules' reserved-word list may be a gap in the instrument's documentation; I worked around it in the Java.
