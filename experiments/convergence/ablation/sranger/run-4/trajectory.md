# sranger — compile-only ablation run-4 — trajectory

- **Condition:** `compile-only-ablation` (visible feedback = the eight
  compile-and-extraction phases only; `post_fdr4`, `post_dafny_verify`,
  `post_isabelle_verify`, `post_vacuity` and `summary.json` withheld
  during the loop per `experiments/convergence/ablation/LAUNCH.md` §B).
- **Actor:** me-as-developer (no sub-agents).
- **Study / package / N:** sranger / `sranger` / run-4.
- **Stop rule:** stop at the first iteration with all eight visible
  phases `passed` → reached at **iter 2**.

## Independence

- Fresh session in a severed-history worktree (`git log` shows the single
  "isolated run base" commit; answer keys scrubbed).
- No study-specific prior-run content was injected at session start: no
  auto-memory recall blocks, no `.remember` hook output, no `run-*`/
  `iter-*` narratives. The injected `CLAUDE.md` named `lre` as active
  study and carried no sranger trajectory knowledge.
- Iter-1 cold codegen read ONLY the canonical inputs:
  `forge.assets/case-studies/sranger/system/system_description.txt`,
  `forge.assets/case-studies/sranger/requirements/requirement_all.json`,
  `CLAUDE.md`, the three prompt files
  (`java_codegen_rules` / `chain_of_thought_codegen` / `few_shot_codegen`),
  `experiments/HOWTO_RUN_CONVERGENCE_EXPERIMENT.md`,
  `experiments/RUN_TRAJECTORY.md`, and the ablation `LAUNCH.md`.
  One pipeline-tool source was consulted for an artefact schema
  (`forge.dashboard/web/feedback/coverage.py`, to get the
  `result_codegen.json` shape right) — the tool, not the answer key.
- Blinding during the loop: the runner's stdout was never read (it
  prints all 12 phase verdicts); phase status was checked exclusively via
  `check_visible_phases.py`. The two snapshot `summary.json` TODO fields
  were filled by a Python one-liner that never displayed file contents.

## Headline table

| iter | change | visible phases (8) | pipeline wall-clock |
|------|--------|--------------------|---------------------|
| 1 | cold codegen: 8 Java files + result_codegen.json | 7/8 — preflight FAILED (1 lint error) | 48.2 s |
| 2 | `@RoboChartType("real")` on `Sensor.NO_READING_DISTANCE` | **8/8 passed → STOP** | 36.0 s |

## Iter-by-iter narrative

### Iter 1 — cold codegen

Generated the full SRanger source tree under
`java.generated.project/src/main/java/sranger/`: `annotation/RoboChartType`,
`mode/SRangerMode` (Moving, Turning, Halted), `constants/SRangerConstants`
(MOVE_VEL 1.0, TURN_VEL 2.0, OBSTACLE_THRESHOLD 0.5, TURN_DURATION 2.0),
`event/InputEvent` (sealed; Obstacle/Tick/EndTask signal records),
`sensor/Sensor` (distance() with 1000.0 m no-reading default),
`actuator/Actuator` (move(lv, av) + last-command getters),
`time/Clock` (nowMs() convention), `controller/SRangerController`
(single-method mode-nested if-else, named predicates `obstacleDetected`,
`turnDurationElapsed`). All 21 requirement ids traced in
`result_codegen.json` (`codegen_trace` schema).

Result: compile, coverage, t2m, m2m, m2t, dafny_gen, isabelle_gen all
passed on the cold pass. preflight failed with exactly one lint error:
`rule4_double_missing_real_annotation` on the **private static final**
`Sensor.NO_READING_DISTANCE` — the lint covers private constants too,
which the rules text ("fields") did not make obvious.

### Iter 2 — minimal fix

Added `@RoboChartType("real")` to `Sensor.NO_READING_DISTANCE`. No other
edits. All eight visible phases passed → compile-only stop point. The
loop ended here per the ablation stop rule; the four behavioural
verifiers were not consulted at any point during the loop.

## Design choices / invented defaults (carried from iter-1 post_codegen)

1. **Terminal mode named `Halted`, not `Final`** — SR-DM1/SR-FR3 name it
   "Final", but CLAUDE.md (a canonical input) states the theory-generated
   controller must NOT contain a `Final` state. Semantics preserved:
   absorbing, entered on endTask from Moving and Turning, entry Move(0,0).
2. **Tick self-loop on `Halted`** (no action) — added beyond SR-Beh1..7
   so the absorbing mode has a bare-precondition event-triggered
   transition (CLAUDE.md pattern (b)). SR-DC1 (no duplicate
   source+trigger) still holds.
3. **`Move(lv, av)` as a multi-arg operation call** (`actuator.move`),
   not a two-payload output event — RoboChart typed events carry one
   payload; the ETL maps multi-arg invocations to LOperations Calls.
4. **Clock units abstract** — `Clock.nowMs()` (convention name) compared
   directly against `TURN_DURATION = 2.0` (spec value, seconds); no
   ms↔s conversion.
5. **Initial Moving entry action in the constructor**
   (`actuator.move(MOVE_VEL, 0)` at power-up), duplicated on the
   Turning → Moving branch.
6. **Obstacle transition combines trigger and guard**
   (`event instanceof Obstacle && obstacleDetected`) per SR-Beh2 + SR-GP1.
7. **Sensor no-reading default = 1000.0 m** (spec: "a large default value").

## Caveats

- The runner exited 1 on both iters; per the run docs the authoritative
  statuses are the `post_<phase>.json` files, and under this condition
  the exit code also reflects the withheld verifier phases — it was
  ignored in favour of `check_visible_phases.py`.
- `pipeline.yaml` local tuning (fdr4 `timeout: 3600`,
  `memory_limit_mb: 65536`, `active_case_study: sranger`) per LAUNCH §C —
  not to be committed.

## Findings

- **F1 — the preflight double-annotation lint includes private
  constants.** `rule4_double_missing_real_annotation` fires on any
  `double` field, including `private static final` implementation
  details never extracted into the model. The codegen rules' examples
  show only instance fields/parameters; annotate *every* double field at
  codegen time to pass preflight first-try.
- **F2 — a single-controller study with disciplined CLAUDE.md-conformant
  codegen can reach the compile-only stop point in 2 iters**, with the
  only visible-feedback iteration being a mechanical annotation fix. The
  eight visible phases exercised none of the behavioural design choices
  (Halted-vs-Final, self-loop coverage, operation-call encoding) — those
  are exactly what the withheld verifiers judge, which is the point of
  the ablation measurement.

## Run-level cost

- Token total and end-to-end time: this ablation session drove iters 1-2
  plus archive/write-up; per-iter `codegen_cost` fields are `null` +
  `kind: "estimated"` per protocol. Pipeline wall-clock: 48.2 s (iter 1)
  + 36.0 s (iter 2) = 84.2 s. (Run-level token total to be read from
  `/cost` by the human at session end if desired.)

## Reproducibility

Stage `run-4/iter-2/java/` into
`java.generated.project/src/main/java/sranger/`, copy
`run-4/iter-2/traces/result_codegen.json` to
`java.generated.project/result_codegen.json`, set `pipeline.yaml`
`agent.active_case_study: sranger`, then run
`python experiments/scripts/run_experiment_iteration.py`.
