# Compile-Only Ablation — sranger, run-2

- **Condition:** `compile-only-ablation` (LAUNCH.md §B — only the eight
  compile-and-extraction feedback files visible; `post_fdr4`,
  `post_dafny_verify`, `post_isabelle_verify`, `post_vacuity` withheld).
- **Actor:** `me-as-developer` (no sub-agents).
- **Stop point:** iter-2 — all eight visible phases `passed`
  (`check_visible_phases.py` exit 0). Loop stopped per the ablation stop
  rule; the four withheld verifiers were **not** read during the loop.
- **compile_only_iters:** 2

## Independence

- Fresh session in an isolated worktree (`fmgvc-abl-sranger2`) whose git
  history is a single severed commit ("isolated run base"); no `run-*`/
  `iter-*` ancestry visible.
- No study-specific prior-run content was injected at session start: no
  agent auto-memory contents surfaced, no `.remember` SessionStart hook
  output, no sranger terms in any `<system-reminder>`.
- Iter-1 cold codegen read ONLY the canonical inputs:
  `forge.assets/case-studies/sranger/system/system_description.txt`,
  `forge.assets/case-studies/sranger/requirements/requirement_all.json`,
  `CLAUDE.md`, the three codegen prompt files, the two run docs
  (`LAUNCH.md`, `RUN_TRAJECTORY.md`), the HOWTO, plus pipeline *tool*
  sources (`java2robochart.etl`, `forge.dashboard/web/feedback/coverage.py`,
  `pipeline.yaml`, `build.gradle`) and the domain-independent
  `forge.assets/vibe-coding-prompts/05_review.md` (post_codegen schema).
  Nothing under `docs/`, no `run-*`/`iter-*`, no findings KB, no ablation
  `README.md`.
- During the loop, pipeline stdout was suppressed (`> /dev/null`) from
  iter-2 onward so the runner's CLI SUMMARY (which prints all 12 phase
  statuses) could not leak the withheld verifier verdicts. For iter-1 the
  runner was started with `| tail -30` but its output file was never read;
  status was taken exclusively from `check_visible_phases.py`.
  `summary.json` files were filled via blind scripted edits (JSON loaded,
  TODO fields set, written back) without displaying their contents.

## Headline table

| Iter | Change | Visible phases (8) | Pipeline wall-clock |
|------|--------|--------------------|---------------------|
| 1 | Cold codegen: 8 Java files + `result_codegen.json` (21/21 requirements traced) | 7/8 — `preflight` FAILED (lint rule4) | see `iter-1/summary.json` `pipeline_wall_clock_s` |
| 2 | Added `@RoboChartType("real")` to `Sensor.NO_READING_DEFAULT` (only change) | **8/8 passed → STOP** | see `iter-2/summary.json` `pipeline_wall_clock_s` |

(Per-phase wall-clocks are auto-recorded in each iter's `summary.json`;
not restated here because those files also carry the withheld verifier
verdicts, which this write-up must not consult.)

## Iter-by-iter narrative

### Iter 1 — cold codegen

From the canonical inputs only, produced the `sranger` package:

- `annotation/RoboChartType.java` — standard marker annotation.
- `mode/SRangerMode.java` — `Moving` (initial), `Turning`, `Halted`.
- `constants/SRangerConstants.java` — `MOVE_VEL=1.0`, `TURN_VEL=2.0`,
  `OBSTACLE_THRESHOLD=0.5`, `TURN_DURATION=2.0` (all `real`).
- `event/InputEvent.java` — sealed interface, signal records
  `Obstacle`, `Tick`, `EndTask`.
- `sensor/Sensor.java` — `distance()` with large no-reading default
  (100.0 m, invented).
- `sensor/Clock.java` — time source `now()` (seconds); detected as the
  clock dependency by class name.
- `actuator/Actuator.java` — `move(lv, av)` storing the last command.
- `controller/SRangerController.java` — single-method mode-nested
  if-else `step(InputEvent)`; named predicates `obstacleDetected`,
  `turnDurationElapsed`; `clockResetTime` field assigned from
  `timer.now()` (promoted to a RoboChart clock; elapsed-time guard
  rewrites to `since()`); tick self-loops in Moving/Turning per
  SR-Beh4/SR-Beh7 (bare-precondition cover).

Key design choices (full list in `iter-1/feedback/post_codegen.md`):

1. **No `Final` mode** — CLAUDE.md mandates that the theory-generated
   controller contain no `Final` state. Terminal semantics
   (SR-DM1/FR3/Beh3/Beh6) realised as absorbing live mode `Halted`:
   entry `Move(0,0)` on both endTask transitions, then a tick self-loop
   with no action.
2. **`Move(lv, av)` as a 2-arg `actuator.move` call** — RoboChart events
   carry one payload; the multi-arg invocation becomes an LOperations
   `Call` per CLAUDE.md.
3. Moving→Turning fires on `obstacle` event **and** `obstacleDetected`
   guard (SR-Beh2 + SR-GP1 conjunction).

Result: compile, coverage, t2m, m2m, m2t, dafny_gen, isabelle_gen all
passed; **preflight failed** with one error — lint
`rule4_double_missing_real_annotation` on `Sensor.NO_READING_DEFAULT`
(a private static final double without `@RoboChartType("real")`).

### Iter 2 — minimal lint fix

Sole change: added `@RoboChartType("real")` to
`Sensor.NO_READING_DEFAULT` (`sensor/Sensor.java`). Re-ran the
pipeline: all eight visible phases passed → compile-only stop point.
Stopped per the ablation stop rule without consulting the withheld
verifiers.

## Caveats

- The terminal-mode requirement is implemented as `Halted` (absorbing,
  tick self-loop) rather than a literal `Final` state — a deliberate
  CLAUDE.md-conformant deviation from the verbatim spec wording,
  recorded as a `design_choice` in `post_codegen`.
- Local (uncommitted) tuning per LAUNCH §C: `pipeline.yaml`
  `agent.active_case_study: sranger`, fdr4 `timeout: 3600`,
  `memory_limit_mb: 65536` (machine page-file size).
- `failure_summary` in `iter-2/summary.json` records the ablation stop,
  not a convergence claim — whether the run "converged" in the 12-phase
  sense is exactly the withheld measurement.

## Findings

- **F1 — Preflight lint rule4 applies to private compile-time constants
  too.** The structural lint demands `@RoboChartType("real")` on *every*
  double field, including a `private static final` sentinel that never
  reaches the formal model as a state variable (`Sensor.NO_READING_DEFAULT`).
  The codegen rules text only calls out fields/parameters that "map to
  RoboChart real", which reads as model-relevant fields; the lint is
  stricter. *How to apply:* annotate every `double` declaration in
  generated code, even private constants inside sensor/actuator helpers,
  at cold-codegen time.

## Reproducibility

- Stage `iter-2/java/` into `java.generated.project/src/main/java/sranger/`,
  restore `iter-2/traces/result_codegen.json` to
  `java.generated.project/result_codegen.json`, set
  `pipeline.yaml` `agent.active_case_study: sranger`, then run
  `python experiments/scripts/run_experiment_iteration.py`.
- The measurement is reproduced deterministically by
  `python experiments/convergence/ablation/scripts/record_ablation_result.py sranger 2`.
