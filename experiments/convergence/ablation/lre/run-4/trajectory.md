# Compile-only ablation — lre, run-4 — trajectory

- **Condition:** `compile-only-ablation` (feedback restricted to the eight
  compile-and-extraction phases; `post_fdr4` / `post_dafny_verify` /
  `post_isabelle_verify` / `post_vacuity` withheld, never opened during the
  loop; stop decision taken via `check_visible_phases.py` only).
- **Session:** fresh, one run. `2026-06-06`, driver Claude Opus 4.8
  (me-as-developer), package `lre`, study `lre`.
- **Independence note:** no prior LRE trajectory content was in context
  (no auto-memory / `.remember` recall fired). The only study-adjacent
  context was the domain-independent project CLAUDE.md injected by the
  harness. `RUN_TRAJECTORY.md` is absent in this severed-history checkout;
  loop mechanics were reconstructed from `LAUNCH.md` + the runner /
  snapshot / check scripts themselves. Forbidden-reads honored: no
  `docs/`, no prior `run-*`/`iter-*`, no findings KB, no ablation
  `README.md`, no `summary.json` during the loop (summaries were read
  only after the stop point, for §D archival/reporting). One nuance:
  the runner's exit code (1) after iter-2 implied *some* phase failed,
  but which withheld verifier(s) was unknown until post-stop extraction.

## Setup

`pipeline.yaml` `agent.active_case_study: lre`; FDR4 `timeout: 3600`,
`memory_limit_mb: 65536` (= local page file, local tuning, not for
commit); type ranges already `[0..1]`; package dir wiped.

## iter-1 — cold codegen

Inputs: `forge.assets/case-studies/lre/system/system_description.txt`,
`requirements/requirement_all.json` (+ `requirements/README.md` ID
conventions), CLAUDE.md codegen rules/prompts. Produced 15 Java files
(676 LOC): sealed `InputEvent`/`OutputEvent` with spec-verbatim
lowercase record names (`reqVel` … `advVel`), `LreMode` enum,
`LreConstants`, `Obstacle` record + copy-on-write `ObstacleRegister`,
`Sensor` (distances, accessors, closest-index selection, CPA math with
safe missing-data defaults), `Actuator.apply(OutputEvent)`, five
operations (`CalcVel`, `CalcCStc`, `CalcCDyn`, `CalcCPA`, `CheckOPEZ`)
with restricted `compute()` bodies, and the mode-nested if-else
`LreController.step()` (named predicates; every mode given an
event-triggered bare-precondition branch; no Final state). Plus
`result_codegen.json` (all 51 requirement IDs traced) and
`post_codegen.{md,json}` (status `uncertain`, 9 flagged
choices/defaults/ambiguities).

**Visible result:** compile ✓, coverage ✓, **preflight ✗** (21 ×
`rule4_double_missing_real_annotation`), t2m ✓, m2m ✓, m2t ✓,
dafny_gen ✓, isabelle_gen ✓.

## iter-2 — minimal fix from visible feedback

Added `@RoboChartType("real")` at exactly the 21 sites `post_preflight`
flagged: `LreConstants` (4 fields), `Actuator` (2 fields), `Obstacle`
(6 record components), `Sensor` (5 double fields incl.
`SAFE_LARGE_DISTANCE`, 4 `update` params). No other changes.

**Visible result:** all 8 visible phases ✓ → **compile-only stop point**
(`check_visible_phases.py` exited 0). Loop stopped per the ablation
stop rule.

## Measurement (extracted post-stop by §D)

At the stop point (iter-2): `fdr4` passed, `dafny_verify` **failed**,
`isabelle_verify` passed, `vacuity` passed.

- `compile_only_iters`: **2**
- `ships_broken`: **true** (dafny_verify failed at the stop point)

Per the run doc, the dafny_verify failure is *not* fixed — the run ends
here; that failure is the measurement.
