# Compile-Only Ablation — chemical_detector — run-1

- **Condition:** `compile-only-ablation` (feedback restricted to the eight
  compile-and-extraction phases; `post_fdr4` / `post_dafny_verify` /
  `post_isabelle_verify` / `post_vacuity` withheld as a forbidden answer key).
- **Actor:** `me-as-developer` (Claude Code session, Opus 4.8; no sub-agents).
- **Study / package:** `chemical_detector` / `chemdetector`.
- **Stop rule:** stop at the first iteration where all eight visible phases
  are `passed` (per `experiments/convergence/ablation/LAUNCH.md` §B).

## Headline

| iter | change | compile | coverage | preflight | t2m | m2m | m2t | dafny_gen | isabelle_gen | pipeline wall-clock |
|------|--------|---------|----------|-----------|-----|-----|-----|-----------|--------------|---------------------|
| 1 | cold codegen (18 files, 2 controllers) | passed | passed | passed | passed | passed | passed | passed | passed | 637.4 s |

**`compile_only_iters` = 1.** All eight visible phases were green on the
first (cold) iteration, so the compile-only stop point is iter 1. No
visible-feedback-driven fix iterations occurred. The four withheld
verifiers' outcomes at this stop point are recorded deterministically in
`ablation_result.json` (by `scripts/record_ablation_result.py`), not by
this write-up.

## Iter 1 — cold codegen (narrative)

Inputs read (canonical only): `system_description.txt`,
`requirement_all.json`, `CLAUDE.md`, `java_codegen_rules.txt`,
`chain_of_thought_codegen.txt`, `few_shot_codegen.txt`,
`HOWTO_RUN_CONVERGENCE_EXPERIMENT.md`, `RUN_TRAJECTORY.md`, `LAUNCH.md`.
Additionally consulted (the tool, not the answer key, per RUN_TRAJECTORY §1):
`forge.transformations/.../java2robochart.etl` ("Configurable conventions"
header, event-naming/`getOrCreateEvent`/Pass-3 sensor detection, multi-
controller extraction loop), `robochart2rct.egl` (Shared-interface grep),
`StructuralLinter.java` (lint rules), and
`forge.dashboard/web/feedback/coverage.py` (`result_codegen.json` schema).

Key design decisions (full list in `iter-1/feedback/post_codegen.md`):

- **Two controllers** with own mode enums + sealed event interfaces;
  inter-controller `turn`/`stop`/`resume` realised as `SignalBus` method
  emissions (GA side) name-matched to `MovementEvent` record triggers
  (MV side), yielding the Shared interface in the model.
- **No `Final` mode in either controller** (CLAUDE.md Isabelle rule): GA
  emits `stop` then enters a live `Finished` mode self-looping on `gas`;
  MV `Found` self-loops on `stop`.
- **Total guard cover** on autonomous-only modes (Analysis splits on the
  two-valued `Status` enum; GasDetected and AvoidingAgain on complementary
  boolean guards) instead of self-loops.
- **Clock convention**: `Clock` class + `evadeStart` field assigned from
  `timer.nowMs()`; stuck-period predicate shaped
  `timer.nowMs() - evadeStart < STUCK_PERIOD` for the `since()` rewrite.
- Invented defaults: `Chem {chemA, chemB}` (target = chemA), index→Angle
  map 0→Front/1→Left/2→Right/≥3→Back, all constants 1 / 1.0 (inside the
  FDR4 `{0..1}` ranges).

Result: compile, coverage (61/61 requirements traced, no
over-implementation), preflight lint, Spoon T2M, ETL M2M, RCT+CSP M2T,
Dafny generation, and Isabelle theory generation all passed first try.
Stop rule fired; loop ended without using any behavioural-verifier
feedback.

## Independence

- Session context at start: only the harness-injected canonical inputs
  (CLAUDE.md + the three prompt files). **No study-specific prior-run
  content was present** — no auto-memory recalls, no `.remember` hook
  output; git history is a single severed commit ("isolated run base").
- No forbidden paths were read: nothing under `docs/`, no `run-*`/`iter-*`
  snapshots, no findings knowledge-base, no `ablation/README.md`, and none
  of the four withheld `post_*` feedback files were opened.
- **Disclosure (console leak):** the pipeline runner streams all 12 phases'
  console output to stdout. While checking the background run's progress I
  saw, in that stream, the FDR4 phase's failure detail and the
  dafny_verify status line. This happened *after* the iter-1 edits were
  complete and could not influence any code (the stop rule fired at this
  same iteration and no further iterations are permitted), but it means
  this session's *context* contained partial withheld-verifier output at
  archive time. The measurement itself is taken deterministically by
  `record_ablation_result.py` from the archived `summary.json`.

## Caveats

- `pipeline.yaml` FDR4 kill policy locally tuned (timeout 3600,
  memory_limit_mb 65536 = page-file size) per RUN_TRAJECTORY §2.3; not to
  be committed.
- The `Finished` self-loop re-captures the gas payload into `gs` (typed
  trigger binding) — readings after conclusion are consumed-and-ignored
  rather than refused (spec says "processing no further readings").
- `changeDirection(l)` surfaces as a typed output *event* in the model
  (single-arg vehicle calls become Communications; only multi-arg calls
  become LOperations operation Calls).

## Findings

- **F1 — Reading the M2M/EGL/linter sources up front substitutes for
  iteration.** This run reached the compile-only stop point in one shot
  by deriving the extraction conventions (event camelCasing from record
  names, `@SensorService` vs the "sensor" name-hint and its collision with
  a `GasSensor` record, the `result_codegen.json` schema from
  `coverage.py`, lint rule 4's "every double field/param needs
  `@RoboChartType(\"real\")`" including record components) from the
  pipeline sources *before* writing any Java. The visible-phase feedback
  loop never had to fire. Future runs of any study should treat
  `java2robochart.etl`'s "Configurable conventions" header,
  `StructuralLinter.java`, and `feedback/coverage.py` as required
  pre-codegen reading.
- **F2 — One trace entry per Java file is the cheap way to keep coverage
  green.** `coverage.py` suppresses member-level over-implementation for
  any file that contributes at least one traced element (basename match),
  so getters/accessors never need individual requirement mappings.
- **F3 — The runner's stdout is itself a feedback channel.** For
  restricted-feedback (ablation) runs, "don't open the four `post_*`
  files" is not sufficient isolation: `run_experiment_iteration.py`
  prints each phase's errors to the console as it runs. A future ablation
  harness should redirect per-phase console output to files, or the
  driver should only inspect the run via `check_visible_phases.py` after
  completion (and never tail the live log past the eighth phase).

## Reproducibility

Stage `run-1/iter-1/java/` into
`java.generated.project/src/main/java/chemdetector/`, copy
`run-1/iter-1/traces/result_codegen.json` to
`java.generated.project/result_codegen.json`, set
`pipeline.yaml agent.active_case_study: chemical_detector`, then run
`python experiments/scripts/run_experiment_iteration.py`.

## Run-level cost

- **Tokens:** not read mid-run; `/cost` is a user-side command. The
  driving session is the cost unit (single session, single iteration).
  Per-iter `codegen_cost` fields are `null`/`"estimated"` per protocol.
- **Pipeline:** 637.4 s total deterministic pipeline wall-clock (iter 1,
  all 12 phases; from `phase_timings.json` via `summary.json`).
- **End-to-end:** session span recoverable from the session transcript
  JSONL (`max(timestamp) − min(timestamp)`); agent time = end-to-end −
  pipeline.
