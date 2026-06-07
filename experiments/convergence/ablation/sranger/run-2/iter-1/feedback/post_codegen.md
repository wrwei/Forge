# Phase 2 — Interactive Code Generation (sranger, iter 1, cold)

**Status:** uncertain

**Summary:** Cold codegen for `sranger` from the canonical inputs
(system_description.txt, requirement_all.json, CLAUDE.md + codegen
prompt rules). All 21 requirements traced in
`java.generated.project/result_codegen.json`. Four issues flagged
below for user review.

## Issues

### 1. design_choice — Terminal mode realised as absorbing live mode `Halted`, not a `Final` state

SR-DM1 / SR-FR3 / SR-Beh3 / SR-Beh6 specify a terminal **Final** mode.
CLAUDE.md mandates that the theory-generated (primary — here, only)
controller must **NOT** contain a `Final` state: the forked Isabelle
theory generator emits the weak store invariant and never designates a
terminal state, leaving the deadlock-freedom lemma unprovable.
Implemented instead as mode `Halted`: entry `Move(0,0)` on both
endTask transitions, then an absorbing `tick` self-loop with no
action; `obstacle`/`endTask` are ignored in Halted. Behaviourally the
robot is permanently stopped — terminal-in-effect.

*Fix directive:* if a literal Final state is required for spec
fidelity, rename `SRangerMode.Halted` to `Final` and remove its tick
self-loop — but expect the Isabelle deadlock-freedom proof to become
unprovable per CLAUDE.md.

### 2. design_choice — `Move(lv, av)` encoded as a two-argument `Actuator.move` call, not an event record

SR-DM4 describes one output event `Move` carrying two reals. RoboChart
events carry a single payload; CLAUDE.md documents the multi-arg
invocation route (`LOperations` interface + `Call` action). The
controller calls `actuator.move(lv, av)` directly in transition
actions; no `OutputEvent` Java type exists.

### 3. invented_default — Sensor no-reading default fixed at 100.0 m

SR-DM5 requires "a large default value" when no reading is available
but does not specify it. Chose `100.0` m (≫ obstacleThreshold 0.5 m).

### 4. design_choice — Moving→Turning fires on obstacle event AND obstacleDetected guard

SR-Beh2: transition fires when the obstacle event is received.
SR-GP1: `obstacleDetected` (distance ≤ obstacleThreshold) is the guard
on that transition. Implemented as the conjunction
`event instanceof InputEvent.Obstacle && obstacleDetected`. The guard
is near-redundant (the IR framework only emits obstacle when the
condition holds) but realises SR-GP1 verbatim as a named predicate.

## Next step

Run the pipeline (compile → coverage → preflight → t2m → m2m → m2t →
dafny_gen → isabelle_gen) and iterate on visible feedback.
