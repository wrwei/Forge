# Compile-only ablation — chemical_detector, run-3

- **Condition:** `compile-only-ablation` — feedback restricted to the eight
  compile-and-extraction phases (`post_compile`, `post_coverage`,
  `post_preflight`, `post_t2m`, `post_m2m`, `post_m2t`, `post_dafny_gen`,
  `post_isabelle_gen`). The four behavioural-verifier feedback files
  (`post_fdr4`, `post_dafny_verify`, `post_isabelle_verify`, `post_vacuity`)
  were never opened during the loop, nor was any `summary.json`.
- **Actor:** me-as-developer (Claude Opus 4.8 in the user's session), no
  sub-agents.
- **Stop rule:** stopped at the first iteration with all eight visible phases
  `passed` (iter-2).

## Independence statement

This session had no prior knowledge of any chemical_detector trajectory: no
auto-memory, no `.remember` hook content, no prior-run material was in
context. `RUN_TRAJECTORY.md` (referenced by LAUNCH.md) does not exist in this
severed run base; the loop mechanics were followed as restated in LAUNCH.md
and the experiment scripts themselves. Sources consulted for codegen:
CLAUDE.md + the three prompt files it embeds, the ETL source
(`java2robochart.etl`) for naming/extraction conventions, the dashboard
coverage runner (for the `result_codegen.json` schema), and the canonical
case-study inputs (`system_description.txt`, `requirement_all.json`,
requirements `README.md`). Nothing under `docs/`, no `run-*`/`iter-*`, no
findings KB, and not the ablation `README.md`.

## Iterations

### iter-1 — cold codegen (visible result: 7/8, preflight failed)

Generated the full `chemdetector` package from the canonical inputs: domain
enums (`Status{noGas,gasD}`, `Angle`, `Loc`, `Chem`), `GasSensor` record,
`InputEvent`/`OutputEvent` sealed interfaces (Turn/Stop/Resume shared GA→MV;
Gas/Obstacle in; Flag out), `GasSensors` functions (analysis/intensity/
location/goreq), `OdometerSensor`, `Clock`, `Vehicle`
(move/randomWalk/shortRandomWalk/changeDirection/pause), `Actuator`,
`Constants`, and the two mode-nested if-else controllers
(`GasAnalysisController`, `MovementController`). Plus `result_codegen.json`
tracing all 81 requirement IDs. Design choices recorded in
`iter-1/feedback/post_codegen.md` (no `Final` mode in either controller —
GA's found-outcome emits `stop` and reroutes to `Reading`; total guard cover
on the three autonomous-only modes; CD-OP1..4 as Vehicle methods).

Visible feedback: only **preflight** failed —
`rule4_double_missing_real_annotation` on `Vehicle.velocity` (a double field
without `@RoboChartType("real")`). All other seven visible phases passed.

### iter-2 — minimal fix (visible result: 8/8 → STOP)

Added `@RoboChartType("real")` to the `Vehicle.velocity` field declaration
(one line). Re-ran all 12 phases. All eight visible phases passed →
compile-only stop point. No further iteration, per the ablation stop rule.

## Withheld-information notes

- The iteration runner prints a 12-phase summary to stdout; I never read its
  output files (decisions were made solely via `check_visible_phases.py` and
  the eight visible `post_*` files).
- `snapshot_iter.py` prints `converged=False` on its console output (iter-2).
  Since all eight visible phases were green, that leaks one bit (≥1 withheld
  phase not passed) — it arrived **after** the stop decision was already
  forced by the stop rule, so it influenced nothing.
- The runner's background-task completion notification reported only the
  process exit code (1), which is ambiguous across all 12 phases and was not
  used for any decision.

## Result

`compile_only_iters = 2`. The four withheld verifiers' outcomes at this stop
point are extracted deterministically by `record_ablation_result.py` into
`ablation_result.json` (see that file / RESULTS.md, and the final session
report).
