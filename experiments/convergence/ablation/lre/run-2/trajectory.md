# Compile-Only Ablation — lre, run-2

- **Condition:** `compile-only-ablation` (LAUNCH.md §B): only the eight
  compile-and-extraction feedback files were readable during the loop
  (`post_compile`, `post_coverage`, `post_preflight`, `post_t2m`,
  `post_m2m`, `post_m2t`, `post_dafny_gen`, `post_isabelle_gen`). The four
  behavioural-verifier files (`post_fdr4`, `post_dafny_verify`,
  `post_isabelle_verify`, `post_vacuity`) and `summary.json` were never
  opened during the loop; stop decisions used
  `ablation/scripts/check_visible_phases.py` only.
- **Actor:** me-as-developer (no sub-agents).
- **Package:** `lre`. **Stop rule:** first iteration with all 8 visible
  phases `passed`.

## Independence

This session drove exactly this one run. Worktree base is a single
severed commit ("isolated run base (history severed, answer keys
scrubbed)"); no agent auto-memory was recalled (empty memory index for
this worktree's namespace), no `.remember` SessionStart hook output
appeared in context, and nothing under `docs/`, no `run-*`/`iter-*`
snapshot, findings KB, or the ablation `README.md` was read. Iter-1 cold
codegen read only the canonical inputs: `system_description.txt`,
`requirement_all.json`, `CLAUDE.md` (+ its three inlined prompt files),
`HOWTO_RUN_CONVERGENCE_EXPERIMENT.md`, `RUN_TRAJECTORY.md`, and the
ablation `LAUNCH.md`. The `summary.json` TODO fields were filled by a
blind key-update script so the recorded verifier statuses never entered
context. (Pipeline sources under `forge.dashboard/` read as tools:
`feedback/coverage.py` for the `result_codegen.json` schema; `runners.py`
to diagnose the FDR4 timeout mechanics — see Caveats.)

## Headline

| iter | change | visible phases (8) | pipeline wall-clock |
|------|--------|--------------------|---------------------|
| 1 | cold codegen: 15 Java files + `result_codegen.json` (52 req ids traced) | 7/8 — `preflight` failed (5 lint errors) | 101.5 s |
| 2 | added 5 missing `@RoboChartType("real")` annotations in `Sensor.java` | **8/8 passed → compile-only stop point** | 3841.5 s (≈64 min of it a hung FDR4, hand-killed at the 60-min policy limit) |

`compile_only_iters = 2`.

## Iter-by-iter

**Iter 1 — cold codegen.** Package `lre` with sub-packages `annotation`,
`mode`, `constants`, `event`, `sensor`, `actuator`, `operation`,
`controller`. Event records named exactly as the spec's event names
(`reqVel`, `reqHdng`, `reqOCM`, `reqMOM`, `reqHCM`, `endTask`; `advVel`,
`advHdng`) so extracted RoboChart channels match LRE-DM6/DM7. Five
operation classes (`CalcVel`, `CheckOPEZ`, `CalcCStc`, `CalcCDyn`,
`CalcCPA`) with field-assignment-only `compute()`. Controller
`LreController`: single `step(InputEvent event)`, mode-nested if-else
over OCM/MOM/HCM/CAM, named predicates before the chain, state vars per
LRE-Var1–8 assigned from operation getters at the top of `step()`, no
`Final` state, and every mode given an event-triggered bare-precondition
branch (OCM: `reqVel`; MOM/HCM/CAM: `reqOCM`). Design choices and
invented defaults recorded in `feedback/post_codegen.md` (NO_OBSTACLE
default 1000.0; DM5-vs-OP5 accessor-default conflict resolved per OP5;
CPA formulae invented; CAM entry advisory left unspecified; transition
priority ordering). Result: `preflight` failed with 5
`rule4_double_missing_real_annotation` errors (the four `Sensor.update()`
double parameters and the `NO_OBSTACLE_DISTANCE` field); the other 7
visible phases passed.

**Iter 2 — minimal fix.** Added `@RoboChartType("real")` to exactly the
five flagged declarations in `Sensor.java`; nothing else changed. All 8
visible phases passed → stop per the ablation stop rule, without reading
the verifiers.

## Caveats

- **FDR4 hand-kill at the policy limit (tooling workaround).** Iter-2's
  FDR4 (`_refines.exe`) sat nearly idle (~0.4 CPU-min over 62
  wall-minutes, ~1.85 GB) and never finished. The configured
  `timeout: 3600` could not fire: `run_fdr4` only enforces it via
  `proc.wait(timeout=...)` *after* the `for line in proc.stdout:` loop
  ends, so a hung-but-alive FDR4 holding stdout open blocks the phase
  indefinitely (only the memory monitor can kill it in that state). At
  62 min I killed the `refines`/`_refines` processes manually — the same
  kill the policy mandates at 60 min — after which the runner unblocked,
  classified the phase itself, and completed the remaining phases. No
  live (computing) FDR4 run was interrupted. Whatever `post_fdr4` status
  this produced is part of the withheld measurement, not loop feedback.
- Iter-2 `pipeline_wall_clock_s` (3841.5 s) is dominated by that hang;
  the productive deterministic phases took ~20 s.
- The runner was launched once with the wrong CWD inherited from a prior
  shell step (failed before any phase ran, exit 2); re-launched from the
  repo root. No snapshot impact.

## Findings

- **F1 — `run_fdr4`'s timeout is unenforceable for a quiet hang.** The
  subprocess timeout only applies after stdout EOF
  (`forge.dashboard/web/runners.py`: stdout iteration precedes
  `proc.wait(timeout=...)`), so a wedged `refines.exe` that emits nothing
  and holds stdout open hangs the phase forever; the 60-min kill policy
  must then be applied by hand (kill the process; the runner recovers and
  classifies the failure on its own). How to apply: if FDR4 shows ~zero
  CPU accumulation well past the configured timeout, deliver the
  policy kill manually instead of waiting; fixing the runner would mean
  reading stdout with a deadline (e.g. a reader thread + timed join).
- **F2 — preflight's rule4 wants `@RoboChartType("real")` on *every*
  double declaration, including setter parameters and private constants
  never referenced by guards.** Iter-1 annotated all model-relevant
  doubles (fields, sensor-function params, record components) but skipped
  the `update()` plumbing params and a private constant; rule4 flags
  those too. How to apply: at codegen time, annotate doubles
  mechanically-everywhere rather than reasoning about model relevance.

## Reproducibility

Stage `run-2/iter-N/java/` into
`java.generated.project/src/main/java/lre/`, copy
`iter-N/traces/result_codegen.json` to `java.generated.project/`, set
`pipeline.yaml` `agent.active_case_study: lre`, then run
`python experiments/scripts/run_experiment_iteration.py`. Local
(uncommitted) tuning used: `phases.fdr4.timeout: 3600`,
`phases.fdr4.memory_limit_mb: 65536` (page-file size).

## Measurement

Emitted deterministically by
`experiments/convergence/ablation/scripts/record_ablation_result.py lre 2`
→ `ablation_result.json` in this directory (verifier outcomes at the
stop point extracted from the archived `iter-2/summary.json`, not from
loop-time reads):

- `compile_only_iters = 2`
- verifiers at stop: `fdr4: passed`, `dafny_verify: failed`,
  `isabelle_verify: passed`, `vacuity: passed`
- `ships_broken = true`

**Reliability caveat on `fdr4: passed`:** iter-2's FDR4 hung and was
killed at the 60-min policy limit (see Caveats), and a killed FDR4 run
is known to record an unreliable "pass" (RUN_TRAJECTORY §2.3 / KB O4).
The `fdr4` entry above should therefore be read as *no usable FDR4
verdict at the stop point*, not as verified deadlock/divergence-freedom.
`ships_broken = true` already holds independently via `dafny_verify`.
