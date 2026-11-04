# Iteration 3 — steam_boiler

## Iteration 2 result

Failed: fdr4, isabelle_verify. All other phases passed.

## Changes for iteration 3

- FDR4 (CSP type error at `regulatePumps.out`): an actuator call becomes an event named after the method, and its argument is dropped. An `int` parameter still typed the event, so `regulatePumps(n)` had a type but no value. The controller now uses argument-free commands: `regulatePumps()` (count read from the sensor), `runFillingPump()`, `stopPumps()`, `openValve()`, `closeValve()`.
- Isabelle (`waitingSignalNow () :: ℝ`): a predicate referenced only inside another predicate was typed `ℝ`. The compound predicates are removed. Every guard is built in the `if` conditions from single-call predicates, each used by name in some branch. SBW/PUR outside initialization is now one sensor query, `startMessageReceived()`.
- Behaviour is unchanged (checked in a scratch simulation). The trace and `post_codegen` are refreshed.

## Design

There is one state machine, and every transition is triggered by `TransmissionReceived`. In each mode the guards are exclusive and cover every case, ordered by SB-FR8. Emergency stop is terminal. Analysis and FR1–FR5 are in `BoilerSensor`, the outbox and FR6/FR7 in `BoilerActuator`, and the cycle driver is `SteamBoilerProgram`.

## Decisions the requirements left open

- Invented constants (C=1000, M1=150, N1=400, N2=600, M2=800, W=25, U1=U2=2, P=10).
- Before STEAM_BOILER_WAITING only STOP counts.
- Aberrant messages are listed in `post_codegen.md`.
- "Pump-controller units fail" means any controller.
- A level failure in normal or degraded goes to rescue first (FR8, literal reading).
- Risk and regulation are worst-case and predictive over two cycles.
- MODE(emergency stop) is sent once.

## Unresolved

The extraction drops the MODE payload (`modeMessage` is untyped), so the model does not distinguish mode values.
