# Phase 2 — Interactive code generation (sranger, ablation run-1, iter-1)

**Status:** complete

Full cold codegen of the SRanger controller from `system_description.txt` +
`requirement_all.json`. 8 Java files under
`java.generated.project/src/main/java/sranger/`, all 22 requirements traced in
`result_codegen.json`.

## Design choices

1. **Terminal mode named `Halted` instead of the spec's `Final`** (SR-DM1,
   SR-FR3, SR-Beh3/6). CLAUDE.md mandates no `Final` state on the
   theory-generated controller (the Isabelle `deadlock_free` closer hangs on
   one). `Halted` is an ordinary absorbing mode: entry issues `Move(0,0)`;
   its only outgoing transition is a no-op tick self-loop, which also gives
   it the bare-precondition cover the deadlock-freedom proof needs.

2. **`Move(lv, av)` encoded as the multi-arg actuator call
   `actuator.move(lv, av)`** (SR-DM4/DM6) — RoboChart events carry a single
   payload; the ETL models multi-payload outputs as an `LOperations`
   operation call. An `OutputEvent.Move(lv, av)` record would silently lose
   the second payload in extraction.

3. **Extra transition beyond SR-Beh1..7:** the `Halted` tick self-loop (see
   1). No duplicate (source, trigger) pair — SR-DC1 holds.

## Invented defaults

- **Sensor no-reading default = 1000.0 m** (SR-DM5 says only "a large
  default value").
- **Initial `Move(MOVE_VEL, 0)` issued in the constructor** (SR-FR1 ×
  SR-Beh1) — Java behaviour matches the spec at power-up; the extracted
  model's initial transition may not carry this action.

## Naming notes

- Spec events `obstacle`/`tick`/`endTask` → records `Obstacle`/`Tick`/`EndTask`;
  spec constants `moveVel` etc. → `MOVE_VEL` etc. (Java casing, passed into
  the model verbatim).
- Clock dependency: class `Clock` (ETL convention), controller field named
  `timer` (avoids the RoboChart-reserved identifier `clock`), reading
  `nowSeconds()`; `clockResetTime` is promoted to a RoboChart clock.

**Next step:** run the deterministic pipeline; act on visible feedback only
(compile-only ablation).
