# Iteration 2 — chemical_detector run 3 (condition E)

**Feedback from iteration 1:** 11 of 12 phases passed, including FDR4 (9/9), Dafny and vacuity. Only `isabelle_verify` failed: the deadlock-freedom proof for `MovementController_deadlock_free` timed out. `GasAnalysisController` did not fail. A timeout prints no residual goal, and the generated theory is off-limits, so the diagnosis below is inferred.

**Diagnosis:**
- Every Movement state already has an operation with no guard and no payload (`stop`). So the proof is not missing an enabled operation; it is too expensive.
- The two controllers differ in their guards. Gas analysis branches on propositional guards (`stsNoGas`/`!stsNoGas`, uninterpreted `goreq` and its negation). Movement's `AvoidingAgain` branched on a clock comparison plus real arithmetic (`since(evadeStart) < STUCK_PERIOD ∨ d1 − d0 > STUCK_DIST` and its complement).
- CLAUDE.md records that `auto` times out on real-arithmetic guards.

**Changed (only `MovementController`):**
1. The stuck verdict is now a boolean state variable, `makingProgress`. It is assigned on `TryingAgain → AvoidingAgain`, at the same moment `d1` is sampled, from the same clock and distance test. `AvoidingAgain` then branches on `makingProgress` / `!makingProgress`. This is the gas analyser's own `sts` pattern (compute in the action, guard on the variable). Behaviour is unchanged: the verdict still uses the time since the evasion began and `d1 − d0`, evaluated when the second obstacle is hit.
2. I removed the invented `obstacle` self-loop in `Found`, a payload-carrying transition the spec does not have. `Found` keeps its bare `stop` self-loop.

The trace (+4 rows for `makingProgress`) and `post_codegen` are refreshed.

**Risk:** if the extractor does not rewrite `timer.nowMs() − evadeStart < …` inside an assignment, M2M/M2T will fail next iteration. That would be an extraction limit, not a design flaw.

**Open for human review:** the same points as iteration 1 (invented constants, terminal-state stand-ins, CD-Fn2 peak over all chemicals).
