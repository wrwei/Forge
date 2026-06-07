# Compile-only ablation — chemical_detector, run-2

- **Condition:** compile-only-ablation (feedback restricted to the eight
  compile-and-extraction phases; `post_fdr4`, `post_dafny_verify`,
  `post_isabelle_verify`, `post_vacuity` withheld as the answer key).
- **Actor:** me-as-developer (Claude Opus 4.8 in the user's session), no
  sub-agents.
- **Date:** 2026-06-06.

## Independence statement (LAUNCH.md §E)

Fresh session; no prior knowledge of any chemical_detector trajectory,
no `run-*`/`iter-*` content, no findings knowledge-base content, and no
study-specific auto-memory in context. `experiments/convergence/RUN_TRAJECTORY.md`
is absent in this scrubbed base; the run followed LAUNCH.md's own
§B/§C/§D/§E. This session drove no other run.

During the run the four withheld feedback files and `summary.json` were
never opened. The pipeline runner's exit code (1) and the snapshot
script's `converged=False` line unavoidably leaked *that* at least one
withheld phase failed, but not *which* or *why*; both leaks occurred
after the stop rule had already fired, so no iteration decision used
them.

## Trajectory

### iter-1 (cold codegen) — STOP POINT

Cold codegen from the canonical inputs only (CLAUDE.md + prompt files,
`system_description.txt`, `requirement_all.json`): 18 Java files,
two mode-nested if-else controllers (`GasAnalysisController` with 4
modes, `MovementController` with 7 modes), shared `turn`/`stop`/`resume`
events via matching record names in `InputEvent`/`OutputEvent`, clock
pattern `clock.nowMs()` for stuck detection, `vehicle.pause(N)` waits,
`result_codegen.json` trace for all 60 requirements, and
`post_codegen.{md,json}` flagging 8 issues (3 invented defaults, 4
design choices, 1 ambiguity).

All eight visible phases passed on this first iteration:
`compile, coverage, preflight, t2m, m2m, m2t, dafny_gen, isabelle_gen`
→ compile-only stop rule fired immediately. **No visible-feedback fix
iterations were needed; `compile_only_iters = 1`.**

Notable design choices made blind to the verifiers (from CLAUDE.md
guidance only): no `Final` state on either controller (GasDetected
found-branch reroutes to Reading after emitting `stop`); Found state
covered by an event-triggered `stop` self-loop; total guard cover on the
autonomous-only modes (Analysis via the 2-valued `Status` enum,
GasDetected and AvoidingAgain via complementary predicates).
