# Compile-Only Ablation — sranger, run-3

- **Study:** sranger (package `sranger`)
- **Condition:** compile-only-ablation (per `experiments/convergence/ablation/LAUNCH.md`)
- **Actor:** me-as-developer (Claude Code session; no sub-agents)
- **Stop point:** iter-2 — all eight visible phases passed (`compile_only_iters = 2`)

## Independence

- Fresh session in an isolated worktree; git history is a single severed
  commit ("isolated run base (history severed, answer keys scrubbed)").
- No sranger-specific prior-run content was injected at session start: the
  agent auto-memory index was empty, and no `.remember` SessionStart hook
  output appeared. Stated in the session's first message.
- Iter-1 cold codegen read ONLY the canonical inputs:
  `forge.assets/case-studies/sranger/system/system_description.txt`,
  `forge.assets/case-studies/sranger/requirements/requirement_all.json`,
  `CLAUDE.md` (+ its three inlined prompt files, injected via claudeMd),
  `experiments/RUN_TRAJECTORY.md`, `experiments/HOWTO_RUN_CONVERGENCE_EXPERIMENT.md`,
  and the ablation `LAUNCH.md`. Pipeline sources consulted (permitted tool
  reads, not answer keys): `java2robochart.etl` (conventions header,
  output-event extraction, since() rewrite, deadlock-lint block) and
  `forge.dashboard/web/feedback/coverage.py` (result_codegen.json schema).
- During the loop, only the eight visible `post_*` feedback files were
  read. The four withheld verifier files (`post_fdr4`, `post_dafny_verify`,
  `post_isabelle_verify`, `post_vacuity`) and `summary.json` were never
  opened; the iter `summary.json` TODO fields were filled by a blind
  scripted update (`ablation/scripts/_fill_summary_todo.py`) that does not
  print file contents. Stop decisions used only
  `ablation/scripts/check_visible_phases.py`.
- The runner's CLI SUMMARY block (which lists all 12 phases) was never
  read: the pipeline ran as a background task and only the visible-phase
  helper was consulted afterwards.

## Headline table

| Iter | Change | Visible phases (8) | Pipeline wall-clock |
|------|--------|--------------------|---------------------|
| 1 | Cold codegen: 9 Java files + result_codegen.json | 7/8 — preflight FAILED (lint rule4: `Sensor.NO_READING_DEFAULT` double without `@RoboChartType("real")`) | 132.9 s |
| 2 | One-line fix: annotate `NO_READING_DEFAULT` with `@RoboChartType("real")` | 8/8 passed → STOP | 33.6 s |

## Iter-by-iter narrative

**Iter 1 — cold codegen.** Implemented SRanger from the canonical inputs:
sealed `InputEvent` (Obstacle/Tick/EndTask signal records), sealed
`OutputEvent.Move(lv, av)`, `SRangerMode` enum (Moving/Turning/Stopped),
`SRangerConstants` (MOVE_VEL 1.0, TURN_VEL 2.0, OBSTACLE_THRESHOLD 0.5,
TURN_DURATION 2.0, all `@RoboChartType("real")`), `Sensor` (large
no-reading default 1000.0), `Clock` (seconds-based `now()`/`set()`),
`Actuator` (stores last lv/av), and the single-method mode-nested
`SRangerController.step()` with named predicates `obstacleDetected` and
`turnDurationElapsed`, clock-reset field `clockResetTime`, and tick
self-loops on every mode. Result: compile, coverage, t2m, m2m, m2t,
dafny_gen, isabelle_gen all passed; preflight failed on one lint error —
the private `static final double NO_READING_DEFAULT` lacked
`@RoboChartType("real")`.

**Iter 2 — minimal fix.** Added the missing annotation (one line in
`Sensor.java`). All eight visible phases passed → compile-only stop point
per the LAUNCH.md stop rule. No further iteration.

## Design choices / caveats (also in iter-1 `feedback/post_codegen.md`)

1. **Terminal mode named `Stopped`, not `Final`** (SR-DM1/SR-FR3 name it
   "Final"). CLAUDE.md states as a hard constraint that the
   theory-generated controller must NOT contain a `Final` state; the
   terminal mode is kept live with a tick self-loop (bare-precondition
   operation, CLAUDE.md pattern (b)). Behaviour is unchanged: EndTask
   enters a terminal mode that issues Move(0,0) and never leaves.
2. **Extra tick self-loop on Stopped** — not among SR-Beh1..7; added for
   the deadlock-freedom bare-precondition rule.
3. **No power-up actuator command** — the initial Move(moveVel, 0) entry
   action (SR-FR1/SR-Beh1) is represented via entry-action lifting on
   transitions into Moving; the Java constructor stays pure.
4. **Obstacle transition** conjoins the Obstacle event (SR-Beh2) with the
   `obstacleDetected` guard (SR-GP1).
5. **Sensor no-reading default = 1000.0 m** (spec: "a large default value").
6. **Clock in seconds** so `turnDurationElapsed` compares directly against
   TURN_DURATION = 2.0 s; ETL clock promotion matches any method on a
   Clock-typed receiver.
7. **Local tuning, not committed:** `pipeline.yaml` fdr4 `timeout: 3600`,
   `memory_limit_mb: 65536` (page-file size), `active_case_study: sranger`.

## Findings

- **F1 — preflight rule4 applies to private compile-time constants too.**
  The lint flags ANY double field without `@RoboChartType("real")`,
  including a `private static final` default used only as a field
  initializer (`Sensor.NO_READING_DEFAULT`). The codegen rules text reads
  as if the annotation targets model-relevant fields/params; a future cold
  codegen should annotate every `double` declaration unconditionally,
  whatever its visibility or role.

## Run-level cost

- **Claude token total:** not read out mid-session; `/cost` is a
  user-side command — the human should capture it at session end and may
  append it here. Per-iter token fields are `null` (`kind: "estimated"`)
  per protocol.
- **Pipeline wall-clock:** 132.9 s (iter-1) + 33.6 s (iter-2) = 166.5 s.
- **End-to-end:** recoverable from the session transcript JSONL
  (`max(timestamp) − min(timestamp)`); agent time = end-to-end − 166.5 s.

## Reproducibility

Stage `run-3/iter-2/java/` into `java.generated.project/src/main/java/sranger/`,
copy `run-3/iter-2/traces/result_codegen.json` to
`java.generated.project/result_codegen.json`, set `pipeline.yaml`
`agent.active_case_study: sranger`, then run
`python experiments/scripts/run_experiment_iteration.py`.

## Measurement

`ablation_result.json` in this directory is emitted deterministically by
`python experiments/convergence/ablation/scripts/record_ablation_result.py sranger 3`,
which reads the archived `iter-*/summary.json`, confirms the eight visible
phases are green at the stop point, and extracts the four withheld
verifiers' outcomes. The driving session did not read the verifier
feedback files at any point; the verifier pass/fail figures below were
first revealed to it by the recorder's output, after the loop ended.
