# Compile-Only Ablation — lre, run-3

- **Condition:** `compile-only-ablation` (LAUNCH.md §B): during the loop only the
  eight compile-and-extraction feedback files were read
  (`post_compile`, `post_coverage`, `post_preflight`, `post_t2m`, `post_m2m`,
  `post_m2t`, `post_dafny_gen`, `post_isabelle_gen`). The four behavioural
  verifier feedback files (`post_fdr4`, `post_dafny_verify`,
  `post_isabelle_verify`, `post_vacuity`) and `summary.json` were never opened
  during the loop.
- **Actor:** me-as-developer (no sub-agents).
- **Stop rule:** stopped at the first iteration where all eight visible phases
  passed — iter-2.

## Headline

| iter | change | visible phases | pipeline wall-clock |
|------|--------|----------------|---------------------|
| 1 | Cold codegen from canonical inputs: 15 Java files under `lre/` (annotation, mode, constants, event, sensor, actuator, operation, controller) + `result_codegen.json` tracing all 51 requirement ids | 7/8 — **preflight failed** (9× `rule4_double_missing_real_annotation`), all others passed | 89.2 s |
| 2 | Added the 9 missing `@RoboChartType("real")` annotations preflight flagged (4 `LreConstants` fields, 4 `Sensor.update` params, `Sensor.SAFE_DISTANCE`); no other changes | **8/8 passed → compile-only stop point** | 45.6 s |

**compile_only_iters = 2.**

## Iter-by-iter narrative

### Iter 1 — cold codegen

Read only the canonical inputs (system_description.txt, requirement_all.json,
CLAUDE.md + the three codegen prompt files, HOWTO, RUN_TRAJECTORY.md,
LAUNCH.md). Produced:

- `LreMode` enum (OCM initial, MOM, HCM, CAM); `LreConstants` (4 thresholds,
  all 1.0); sealed `InputEvent` (reqVel/reqHdng payload-carrying, reqOCM/
  reqMOM/reqHCM/endTask signals) and `OutputEvent` (advVel, advHdng) with
  record names matching the requirement event names exactly (lowercase) so the
  extracted RoboChart events carry the spec names.
- `Obstacle` record (6 real fields + `isStatic()`), copy-on-write
  `ObstacleRegister` (partial fn nat→Obstacle), `Sensor` with distance
  functions (hdist/vdist/odist; safe large distance 1.0E9 when index absent),
  field accessors (zero defaults), closest-static/dynamic selection
  (imperative loops, -1 sentinel), and `closestApproachDist`/
  `closestApproachTime` (3-D CPA; division-by-zero and no-obstacle cases
  handled inside the Sensor layer so operation `compute()` bodies stay
  single-expression assignments).
- Five operations named exactly per the requirements (CalcVel, CheckOPEZ,
  CalcCStc, CalcCDyn, CalcCPA), each `compute()` only direct
  `this.field = expr` assignments; CheckOPEZ takes CalcCStc, CalcCPA takes
  CalcCDyn for the cstc/cdyn dependencies.
- `LreController`: ops invoked at top of `step()`, 8 controller state vars
  (inOpez, hvel, vvel, vel, cstc, cdyn, cda, tcpa) refreshed from op getters,
  10 named boolean predicates, pure two-level mode-nested if-else implementing
  LRE-Beh1..19 (OCM pass-throughs as explicit self-loops; reqOCM bare-
  precondition cover in MOM/HCM/CAM; MOM entry advVel(1), HCM entry advVel(0)
  inline at every incoming transition).

Visible result: compile, coverage, t2m, m2m, m2t, dafny_gen, isabelle_gen all
passed; **preflight failed** with 9 errors, all the same rule
(`rule4_double_missing_real_annotation`): the 4 `LreConstants` doubles, the
4 `Sensor.update` double params, and `Sensor.SAFE_DISTANCE` lacked
`@RoboChartType("real")`.

### Iter 2 — minimal preflight fix

Added exactly the 9 annotations at the locations preflight named; nothing else
changed. All eight visible phases passed → compile-only stop point. Stopped
per the stop rule without iterating further.

## Independence

- Fresh worktree session; git history is a single severed commit
  ("isolated run base (history severed, answer keys scrubbed)").
- No study-specific prior-run content was injected at session start: no agent
  auto-memory recall (memory index empty), no `.remember` SessionStart hook
  output.
- Iter-1 read only the canonical inputs listed in RUN_TRAJECTORY §1 plus
  LAUNCH.md. Nothing under `docs/`, no `run-*`/`iter-*` of any study, no
  findings KB, no ablation README was opened.
- Tool-source reads (allowed, "the tool, not the answer key"):
  `forge.dashboard/web/feedback/coverage.py` (to learn the
  `result_codegen.json` schema) and `pipeline.yaml` (setup edits).

### Protocol deviation — partial verifier-status exposure (iter-1)

While the iter-1 pipeline was running I read the background runner's stdout
file to check progress. Beyond the visible phases, the tail of what I read
included two withheld-verifier status lines: `fdr4: passed` (3 assertions
passed) and `dafny_verify: failed` (one postcondition error line naming
`LreController.dfy(104,35)`). I did **not** open any of the four withheld
`post_*` files or `summary.json`, and no iteration decision used that
information — the only fix applied in the run (iter-2) was the mechanical
preflight annotation fix, which was already determined by the visible
`post_preflight.md` before the exposure and is unrelated to Dafny
postconditions. From iter-2 onward the runner's stdout was suppressed
(`| Out-Null`) and only `check_visible_phases.py` was consulted. Judges may
wish to weigh this; the stop-point measurement itself (what the verifiers say
at the compile-only stop) is computed deterministically by
`record_ablation_result.py` from the archived snapshots and is unaffected.

## Caveats

- The system description says HCM operates at 0.1 m/s, but LRE-FR3 says
  "On entering HCM, the LRE advises a velocity of 0 m/s"; the requirement file
  is canonical, so HCM entry advises 0.
- LRE-Beh4's "distance greater than 1" guards were encoded against
  `MIN_SAFE_DIST` (= 1.0) rather than a bare literal, reading the constant's
  documented meaning ("minimal safe distance to any obstacle") as the intent.
- CPA math is 3-D (includes vertical via obs_roc/rate_of_climb); the
  requirement does not fix the dimensionality.
- `cstc`/`cdyn` annotated `@RoboChartType("nat")` per LRE-Var5/6 ("natural
  number variable") even though -1 is the no-obstacle sentinel, matching the
  requirement text; the sentinel never reaches controller guards directly
  (sensor layer returns safe defaults).

## Reproducibility

Stage `run-3/iter-2/java/` into `java.generated.project/src/main/java/lre/`,
copy `run-3/iter-2/traces/result_codegen.json` to
`java.generated.project/result_codegen.json`, set `pipeline.yaml`
`agent.active_case_study: lre`, then run
`python experiments/scripts/run_experiment_iteration.py`.
Local (uncommitted) `pipeline.yaml` tuning used: fdr4 `timeout: 3600`,
`memory_limit_mb: 65536`.
