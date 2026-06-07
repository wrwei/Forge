# Post-codegen review — chemical_detector, compile-only ablation run-3, iter-1

**Status:** complete — cold codegen from `system_description.txt` + `requirement_all.json` (all 81 requirements).

## What was generated

- `chemdetector.domain` — `Status`, `Angle`, `Loc`, `Chem` enums; `GasSensor` record.
- `chemdetector.event` — `InputEvent` (Gas, Obstacle, Turn, Stop, Resume), `OutputEvent` (Turn, Stop, Resume, Flag). Turn/Stop/Resume are the GA→MV shared events; Gas/Obstacle inputs; Flag output.
- `chemdetector.sensor` — `GasSensors` (analysis/intensity/location/goreq), `OdometerSensor`, `Clock`.
- `chemdetector.actuator` — `Vehicle` (move/randomWalk/shortRandomWalk/changeDirection/pause), `Actuator`.
- `chemdetector.controller` — `Constants`, `GasAnalysisMode`, `MovementMode`, `GasAnalysisController`, `MovementController`.
- `result_codegen.json` — all 81 requirement IDs traced.

## Invented defaults (user should review)

1. **Chem literals** `Target`/`Other`; "indicates the target chemical" = `c == Target && i > 0.0`.
2. **angle(x) mapping** in `location()`: index 0→Left, 1→Right, 2→Back, else Front (empty reading → Front).
3. **Constant values** all 1 / 1.0 (spec gives types only; keeps the model inside FDR4 type ranges [0..1]).
4. **Initial values**: gs=[], sts=noGas, ins=0.0, anl=Front; a=Front, d0=d1=0.0, l=front, evasionTimer=0.

## Design choices

5. **No `Final` state in either controller** (CLAUDE.md Isabelle rule). GA's source-found outcome emits `stop` then returns to `Reading`; MV's `Found` stays live via a stop-triggered self-loop (consistent with CD-MV-Beh9 "stays halted").
6. **CD-OP1..4 are Vehicle methods**, not `compute()` operation classes — they are platform operations, not controller computation groups.
7. **Total guard cover on autonomous-only modes** (Analysis, GasDetected, AvoidingAgain) instead of self-loops, per CLAUDE.md's trilemma guidance.

## Ambiguities

8. Obstacle events in modes other than Going/TryingAgain are ignored (spec defines no transition).

**Next step:** run phases 2a–5c and iterate on the eight visible feedback files only.
