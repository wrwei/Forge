# Compile-Only Ablation — lre, run-1

- **Condition:** `compile-only-ablation` (per `experiments/convergence/ablation/LAUNCH.md`)
- **Actor:** `me-as-developer` (single session, no sub-agents)
- **Study / package / N:** `lre` / `lre` / 1
- **Stop rule:** stop at the first iteration where all **eight** visible
  compile-and-extraction phases pass (`compile coverage preflight t2m m2m
  m2t dafny_gen isabelle_gen`). The four behavioural-verifier feedback
  files (`post_fdr4`, `post_dafny_verify`, `post_isabelle_verify`,
  `post_vacuity`) and `summary.json` were **never opened** during the loop;
  the stop decision was made exclusively via
  `experiments/convergence/ablation/scripts/check_visible_phases.py`.
- **Result:** compile-only stop point reached at **iter-2**
  (`compile_only_iters = 2`). Withheld-verifier outcomes are recorded
  deterministically by `record_ablation_result.py` in
  `ablation_result.json` — not consulted as feedback.

## Independence

- Fresh session in a severed-history worktree (`fmgvc-abl`, single base
  commit "isolated run base (history severed)"); one run in this session.
- No agent auto-memory or `.remember` SessionStart content was injected
  (memory index empty; hook absent).
- **Exposure note:** the harness-injected git-status snapshot at session
  start listed *deleted file paths* from the scrub — among them
  `experiments/cold-baseline/chemical_detector/run-1/...` filenames (a
  different study) and `docs/CONVERGENCE_PLAYBOOK.md`. Path names only, no
  content, no lre-specific prior-run information. Judged non-contaminating
  for an lre run, declared here for auditability.
- Iter-1 read only the canonical inputs: `system_description.txt`,
  `requirement_all.json`, `CLAUDE.md` (+ its four inlined prompt files),
  `experiments/RUN_TRAJECTORY.md`,
  `experiments/HOWTO_RUN_CONVERGENCE_EXPERIMENT.md`, and the LAUNCH doc.
  One tool-source read: `forge.dashboard/web/feedback/coverage.py` (to get
  the `result_codegen.json` schema the coverage phase parses — the tool,
  not the answer key). Nothing under `docs/`, no `run-*`/`iter-*`, no
  findings KB, no ablation `README.md`.
- The runner's stdout SUMMARY block (which lists all 12 phases) was never
  read: the runner was launched in the background and its output file was
  only read once, early in iter-1, while the run was still in the m2t
  phase (visible-phase region; transcript-auditable). All stop decisions
  used `check_visible_phases.py`. `snapshot_iter.py`'s one-line stdout
  echo (`converged=False`) was observed both iterations — it leaks one
  aggregate bit (some phase among the 12 failed), but no identity or
  detail of any withheld verifier.

## Headline table

| iter | change | visible phases (8) | pipeline wall-clock |
|------|--------|--------------------|---------------------|
| 1 | cold codegen: 15 Java files + `result_codegen.json` (51 requirement ids traced) | 7/8 — preflight FAILED (5 errors) | 97.1 s |
| 2 | added `@RoboChartType("real")` to 5 unannotated `double` constants | **8/8 passed → STOP** | 38.7 s |

(Per-phase wall-clocks are in each `iter-N/summary.json`
`phase_wall_clocks_s`; the table reports the auto-recorded totals.)

## Iter-by-iter narrative

### Iter 1 — cold codegen

From the canonical inputs only, wrote the full `lre` package:

- `annotation/RoboChartType.java` — SOURCE-retention marker annotation.
- `mode/LreMode.java` — `OCM, MOM, HCM, CAM` (LRE-DM1).
- `constants/LreConstants.java` — the four thresholds, all `1.0` (LRE-DM4).
- `event/InputEvent.java` / `event/OutputEvent.java` — sealed interfaces
  whose record names match the spec event names **verbatim** (`reqVel`,
  `reqHdng`, `reqOCM`, `reqMOM`, `reqHCM`, `endTask`; `advVel`,
  `advHdng`), so extracted RoboChart channels carry the required names.
- `sensor/Obstacle.java` (record, 6 real fields, `isStatic`/`isDynamic`),
  `sensor/ObstacleRegister.java` (immutable nat→Obstacle map, copy-on-write,
  static/dynamic filtering), `sensor/Sensor.java` (raw data; `hdist`/
  `vdist`/`odist`; field accessors; `closestStaticIndex`/`closestDynamicIndex`;
  safe defaults: 1000.0 distance, 0.0 fields, −1 index).
- `actuator/Actuator.java` — `apply(OutputEvent)` storing last advVel/advHdng
  (the `actuator.apply(new OutputEvent.advVel(...))` shape the EGL
  entry-action detection expects).
- Five operations (`CalcVel`, `CalcCStc`, `CalcCDyn`, `CheckOPEZ`,
  `CalcCPA`), each a single `compute()` of direct `this.field = expr`
  assignments; `CheckOPEZ` reads `calcCStc.cstc()`, `CalcCPA` reads
  `calcCDyn.cdyn()`.
- `controller/LreController.java` — single `step(InputEvent)`; operation
  invocations + state-var refresh at the top; 13 named boolean predicates;
  pure two-level mode-nested if-else implementing LRE-Beh2..Beh18; every
  mode has an event-triggered bare-precondition branch (OCM: `reqVel`/
  `reqHdng` self-loops; MOM/HCM/CAM: `reqOCM`); no `Final` state.

Pipeline: compile, coverage, t2m, m2m, m2t, dafny_gen, isabelle_gen passed;
**preflight failed** with 5× `rule4_double_missing_real_annotation`
(the four `LreConstants` thresholds + `Sensor.NO_OBSTACLE_DISTANCE`).

### Iter 2 — minimal preflight fix

Added `@RoboChartType("real")` to exactly those five `double` constant
fields. No other change. All eight visible phases passed → compile-only
stop point. Loop ended without consulting any behavioural verifier.

## Design choices / invented defaults (post_codegen-style disclosure)

1. **`CalcCPA` treats `obsNsVel`/`obsEwVel` as already-relative
   velocities** (no subtraction of AUV velocity). LRE-DM2 pairs them with
   *relative* distances; LRE-OP5 says CalcCPA "accesses the obstacle's
   relative position and velocity". Subtracting AUV velocity would make
   the no-dynamic-obstacle default (all-zero fields) yield cda = 0 and a
   spurious MOM→CAM trigger; the chosen reading yields 0/0 → NaN → all
   CPA guards false → no spurious CAM.
2. **Requirements conflict noted:** LRE-DM5 says field accessors return
   zero when no obstacle exists, but LRE-OP5 expects "large-distance"
   defaults to reach cda. Followed DM5's letter for accessors and resolved
   OP5's intent via choice (1).
3. **Literal `1` thresholds in LRE-Beh4 guards** implemented as
   `MIN_SAFE_DIST` (default 1.0) rather than a bare literal.
4. **`NO_OBSTACLE_DISTANCE = 1000.0`** invented for the "safe large
   distance" the spec leaves unquantified.
5. **MOM/HCM inner-branch priority** (Java else-if order): events first
   (`reqOCM`, `endTask`, `reqHCM`), then autonomous CAM-entry, then
   `inOpez`, then the three HCM-entry guards. The requirements give no
   priority ordering; RoboChart treats them as concurrent choices anyway.
6. **`advVel(0)` on every HCM entry** (LRE-FR3 "advises a velocity of
   0 m/s"), including the Beh12 `reqHCM` transition.

## Caveats

- Local non-committed tuning per RUN_TRAJECTORY §2 / LAUNCH §C:
  `pipeline.yaml` `active_case_study: lre`, fdr4 `timeout: 3600`,
  `memory_limit_mb: 65536` (page-file size). FDR4 ran inside the pipeline
  every iteration (its *result* simply was not read).
- `java.generated.project/build.gradle` `group` left as the stale
  `'chemdetector'` → changed to `'lre'`? **No** — the edit was skipped
  (read-before-write constraint); group id is build metadata with no
  pipeline effect.

## Findings

- **F1 — Preflight rule 4 applies to *constants*, not just model-visible
  state.** `java_codegen_rules.txt` motivates `@RoboChartType` by nat/int
  disambiguation and the prompt examples annotate fields/params of
  sensors, operations and the controller — it is easy to leave
  `public static final double` threshold constants (and even *private*
  doubles like a sensor-internal default) unannotated. The preflight
  linter requires `@RoboChartType("real")` on **every** double field
  regardless of visibility or finality. Future runs: annotate every
  `double` field at codegen time, including constants classes and private
  statics; it is the difference between a 1-iter and 2-iter compile-only
  trajectory.

## Reproducibility

- Stage `run-1/iter-2/java/**` into `java.generated.project/src/main/java/lre/`
  and `run-1/iter-2/traces/result_codegen.json` into
  `java.generated.project/result_codegen.json`, set
  `pipeline.yaml` `agent.active_case_study: lre`, then
  `python experiments/scripts/run_experiment_iteration.py`.
- The measurement is reproduced by
  `python experiments/convergence/ablation/scripts/record_ablation_result.py lre 1`.
