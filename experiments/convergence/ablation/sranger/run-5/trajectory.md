# sranger — compile-only ablation run-5 trajectory

- **Condition:** `compile-only-ablation` (per `experiments/convergence/ablation/LAUNCH.md`)
- **Actor:** `me-as-developer` (single session, no sub-agents)
- **Study / package / N:** `sranger` / `sranger` / run-5
- **Stop rule:** stop at the first iteration where all **eight** visible
  compile-and-extraction phases are green; the four behavioural verifiers
  (`fdr4`, `dafny_verify`, `isabelle_verify`, `vacuity`) were withheld and
  never read as feedback.
- **Result:** stop point reached at **iter 2** (`compile_only_iters = 2`).

## Headline table

| Iter | Change | compile | coverage | preflight | t2m | m2m | m2t | dafny_gen | isabelle_gen | pipeline wall-clock |
|------|--------|---------|----------|-----------|-----|-----|-----|-----------|--------------|---------------------|
| 1 | Cold codegen from canonical inputs (8 Java files + result_codegen.json) | pass | pass | **fail** | pass | pass | pass | pass | pass | 52.1 s |
| 2 | Added `@RoboChartType("real")` to 5 flagged double fields | pass | pass | pass | pass | pass | pass | pass | pass | 36.6 s |

(The four withheld verifier columns are deliberately absent — see
`ablation_result.json` for their outcomes, extracted deterministically by
`record_ablation_result.py` after the stop.)

## Independence

- Fresh session in a severed-history worktree (`git log` shows a single
  parentless base commit, "isolated run base (history severed, answer keys
  scrubbed)"). No `.remember` hook output and no agent auto-memory content
  was injected; the only study-relevant context at session start was the
  CLAUDE.md tree (canonical input). No prior sranger trajectory knowledge
  was present in context. No sub-agents were dispatched.
- Iter-1 reads: `forge.assets/case-studies/sranger/system/system_description.txt`,
  `forge.assets/case-studies/sranger/requirements/requirement_all.json`,
  `CLAUDE.md` (+ its inlined prompt files `java_codegen_rules.txt`,
  `chain_of_thought_codegen.txt`, `few_shot_codegen.txt`, `fdr4_system.txt`,
  injected as project instructions), `experiments/RUN_TRAJECTORY.md`,
  `experiments/HOWTO_RUN_CONVERGENCE_EXPERIMENT.md`,
  `experiments/convergence/ablation/LAUNCH.md` +
  `scripts/check_visible_phases.py`, and — as permitted tool sources (the
  tool, not the answer key) — excerpts of
  `forge.transformations/src/main/resources/transformations/java2robochart.etl`
  and `forge.dashboard/web/feedback/coverage.py` to confirm extraction
  conventions (clock promotion, multi-arg operation calls, constants-class
  detection, result_codegen.json schema).
- **Exposure note (recorded for judging):** while tailing the iter-1
  runner's console output to monitor progress, the session saw the
  scrolled-past console status lines of two withheld phases (`fdr4:
  failed` with "3 assertions, 0 passed, 3 inconclusive", and
  `dafny_verify: failed` with one truncated postcondition-error line
  naming `SRangerController.dfy(70,30)`). None of the four withheld
  `post_*` feedback files nor `summary.json` were ever opened, and **no
  edit was driven by that console exposure** — the only iter-2 change was
  the five annotations that the *visible* preflight phase demanded, and the
  loop stopped at the stop rule regardless of what the verifiers said.
  Iter-2's runner invocation filtered console output to the eight visible
  phase status lines to prevent a repeat.

## Iter-by-iter narrative

### Iter 1 — cold codegen

Wrote the full Java tree under `java.generated.project/src/main/java/sranger/`:

- `annotation/RoboChartType.java` — standard marker annotation.
- `mode/SRangerMode.java` — enum `Moving`, `Turning`, `Stopped`.
- `constants/SRangerConstants.java` — `MOVE_VEL=1.0`, `TURN_VEL=2.0`,
  `OBSTACLE_THRESHOLD=0.5`, `TURN_DURATION=2.0`.
- `event/InputEvent.java` — sealed interface; signal records `Obstacle`,
  `Tick`, `EndTask`.
- `time/Clock.java` — monotonic seconds source (ETL clock convention:
  class named `Clock`).
- `sensor/Sensor.java` — `distance()` with no-reading default `100.0` m.
- `actuator/Actuator.java` — `move(lv, av)` storing last command.
- `controller/SRangerController.java` — single-method mode-nested if-else
  state machine; named predicates `obstacleDetected`,
  `turnDurationElapsed`; clock-reset field `clockResetTime` assigned from
  `timer.now()` on Moving→Turning (ETL promotes it to a RoboChart clock
  with `since()` guard rewriting).

Key design choices (full list in iter-1 `feedback/post_codegen.md`):

1. **Terminal mode `Stopped`, not `Final`** — CLAUDE.md mandates no `Final`
   state on the theory-generated controller; `Stopped` is behaviourally
   terminal with a `Tick` self-loop providing event-triggered
   bare-precondition cover (CLAUDE.md pattern (b)).
2. **`Move(lv, av)` as multi-arg `Actuator.move` call** — RoboChart events
   carry one payload; the ETL's designed mapping for two-payload commands
   is an `LOperations` operation call (`buildOperationCall` documents
   exactly this `vehicle.move(LV, a)` shape).
3. **Moving→Turning conjoins `Obstacle` trigger with `obstacleDetected`
   guard** (honours both SR-Beh2 and SR-GP1).
4. Sensor no-reading default `100.0` m (SR-DM5 says only "large").
5. Power-up `move(MOVE_VEL, 0)` issued in the constructor (not extracted;
   the model's initial transition covers entry semantics).

Pipeline: 7/8 visible phases passed; **preflight failed** with 5 ×
`rule4_double_missing_real_annotation` (the four constants + the sensor
default — `static final double` fields still need `@RoboChartType("real")`,
which the iter-1 code had only put on instance fields/params/returns).

### Iter 2 — minimal preflight fix

Added `@RoboChartType("real")` to exactly the five flagged fields. No other
change. All eight visible phases passed → compile-only stop point. Loop
stopped per the ablation stop rule; no verifier feedback was consulted.

## Caveats

- The terminal-mode rename (`Stopped` vs the requirements' `Final`) and the
  `Move`-as-operation-call encoding are spec deviations forced by pipeline
  constraints; both are flagged in `post_codegen` and `result_codegen.json`
  notes.
- `pipeline.yaml` got the local (uncommitted) tuning required by setup §C:
  `active_case_study: sranger`, fdr4 `timeout: 3600`,
  `memory_limit_mb: 65536` (machine page-file size).

## Findings

- **F1 — preflight's rule4 covers `static final` constants, not just
  state fields.** The codegen-rules text exemplifies `@RoboChartType` on
  instance fields and parameters; iter-1 applied it there but left the
  constants class bare, which is the single thing preflight caught. In any
  future run, annotate *every* `double` field — including `static final`
  constants and private sensor defaults — at cold-codegen time.

## Reproducibility

Stage `run-5/iter-N/java/` into `java.generated.project/src/main/java/sranger/`,
copy `run-5/iter-N/traces/result_codegen.json` to
`java.generated.project/result_codegen.json`, set
`pipeline.yaml agent.active_case_study: sranger`, then
`python experiments/scripts/run_experiment_iteration.py`.
