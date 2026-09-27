# Compile-Only Ablation — sranger, run-1

- **Study:** sranger (package `sranger`)
- **Condition:** `compile-only-ablation` (LAUNCH.md §B — only the eight
  compile-and-extraction feedback files visible; `post_fdr4`,
  `post_dafny_verify`, `post_isabelle_verify`, `post_vacuity` withheld)
- **Actor:** me-as-developer (no sub-agents)
- **Stop point:** **iter-1** — all eight visible phases `passed` on the first
  cold codegen; per the stop rule the loop ended immediately
  (`compile_only_iters = 1`).

## Independence

- Fresh session in a severed-history worktree (single base commit
  "isolated run base (history severed, answer keys scrubbed)").
- No study-specific prior-run content was injected: the agent auto-memory
  index was empty and no `.remember` SessionStart hook fired. Stated in the
  session's first message.
- Iter-1 cold codegen read ONLY the canonical inputs:
  `forge.assets/case-studies/sranger/system/system_description.txt`,
  `forge.assets/case-studies/sranger/requirements/requirement_all.json`,
  `CLAUDE.md`, the three `forge.assets/prompts/*codegen*.txt` files,
  `experiments/HOWTO_RUN_CONVERGENCE_EXPERIMENT.md`,
  `experiments/RUN_TRAJECTORY.md`, and the ablation LAUNCH.md. In addition,
  per RUN_TRAJECTORY §1 ("the tool, not the answer key"), targeted greps of
  `forge.transformations/.../java2robochart.etl` and
  `forge.dashboard/web/feedback/coverage.py` were used to confirm encoding
  conventions (clock detection by declaring type; multi-arg actuator calls →
  `LOperations`; mode literals → ordinary states; `result_codegen.json`
  schema). Nothing under `docs/`, no `run-*`/`iter-*`, no findings KB, no
  ablation `README.md` was opened.
- The four withheld verifier files, the runner's stdout (its SUMMARY block
  contains the verdicts), and `summary.json` were never read during the
  loop. The stop decision used only
  `experiments/convergence/ablation/scripts/check_visible_phases.py`.
- **Known minor leak:** `snapshot_iter.py` prints `converged=False` on its
  one-line completion banner. This is an aggregate over all 12 phases and
  does not identify any individual withheld verifier's verdict; it arrived
  after the stop decision had already been made (stop-gate was green), so it
  did not influence the loop.
- `summary.json`'s hand-filled TODO fields were written via a blind JSON
  edit script that did not print the withheld `phase_results`.

## Headline

| iter | change | visible phases (8) | pipeline wall-clock |
|------|--------|--------------------|---------------------|
| 1 | cold codegen from canonical inputs | all 8 passed | 52.1 s |

No feedback-driven iteration occurred: the first cold codegen already
satisfied compile, coverage, preflight, t2m, m2m, m2t, dafny_gen, and
isabelle_gen.

## Iter-1 narrative

Cold codegen produced 8 Java files under
`java.generated.project/src/main/java/sranger/` (annotation, mode,
constants, event, sensor, actuator, time, controller) plus
`result_codegen.json` tracing all 22 requirements. Design choices (full list
in `iter-1/feedback/post_codegen.md`):

1. **Terminal mode named `Halted`, not `Final`** — CLAUDE.md's inline
   Isabelle guidance forbids a `Final` state on the theory-generated
   controller; `Halted` is absorbing (entry `Move(0,0)`, only a no-op
   tick self-loop out), which also provides the bare-precondition cover
   CLAUDE.md says every mode needs.
2. **`Move(lv, av)` encoded as multi-arg `actuator.move(lv, av)`** —
   RoboChart events carry one payload; the ETL's documented encoding for
   multi-payload outputs is an `LOperations` operation call. An
   `OutputEvent.Move(lv, av)` record would have silently dropped `av`
   (the ETL's constructor-pattern path takes only the first ctor arg).
3. **Clock**: infrastructure class `Clock`, controller field `timer`
   (avoids the RoboChart-reserved identifier `clock`), `clockResetTime`
   assigned from `timer.nowSeconds()` so the ETL promotes it to a RoboChart
   clock and rewrites the elapsed-time predicate to `since(clockResetTime)`.
4. **Guards**: named predicates `obstacleDetected`
   (`sensor.distance() <= OBSTACLE_THRESHOLD`, conjoined with the
   `Obstacle` trigger per SR-GP1 × SR-Beh2) and `turnDurationElapsed`
   (autonomous Turning → Moving per SR-GP2 × SR-Beh5).
5. **Extra transition beyond SR-Beh1..7**: the `Halted` tick self-loop
   (deadlock-freedom cover; no duplicate (source, trigger) pair, SR-DC1
   holds).
6. **Invented defaults**: sensor no-reading default 1000.0 m; initial
   `Move(MOVE_VEL, 0)` issued in the constructor (power-up entry of Moving).

## Caveats

- The spec's mode name `Final` and event names `obstacle`/`tick`/`endTask`
  appear in Java casing as `Halted` (renamed) and `Obstacle`/`Tick`/`EndTask`;
  constants `moveVel` etc. as `MOVE_VEL` etc.
- The constructor-issued initial `Move(MOVE_VEL, 0)` is Java-side fidelity
  only; constructor statements are not extracted as model transition actions.

## Reproducibility

Stage `iter-1/java/` into `java.generated.project/src/main/java/sranger/`,
copy `iter-1/traces/result_codegen.json` to
`java.generated.project/result_codegen.json`, set `pipeline.yaml`
`agent.active_case_study: sranger`, then run
`python experiments/scripts/run_experiment_iteration.py`.
