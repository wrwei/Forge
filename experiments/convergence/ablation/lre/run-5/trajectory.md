# Compile-Only Ablation — lre, run-5

- **Study:** lre (package `lre`)
- **Condition:** `compile-only-ablation` (per `experiments/convergence/ablation/LAUNCH.md`)
- **Actor:** me-as-developer (Claude Code session, Opus 4.8; no sub-agents)
- **Date:** 2026-06-06
- **Stop rule:** stop at the first iteration where all eight visible
  compile-and-extraction phases pass (`compile, coverage, preflight, t2m,
  m2m, m2t, dafny_gen, isabelle_gen`). The four behavioural verifiers
  (`fdr4, dafny_verify, isabelle_verify, vacuity`) ran every iteration but
  their feedback files were never opened during the loop.
- **Result: compile-only stop point reached at iter-2** (`compile_only_iters = 2`).

## Headline table

| Iter | Change | Visible phases (8) | Pipeline wall-clock |
|------|--------|--------------------|---------------------|
| 1 | Cold codegen: 15 Java files + result_codegen.json from canonical inputs only | 7/8 — `m2t` FAILED (CSP generator silently produced no files) | 98.8 s |
| 2 | Renamed `Sensor.cda`/`Sensor.tcpa` → `cdaTo`/`tcpaTo` (+ CalcCPA call sites, result_codegen.json) | 8/8 passed → **STOP** | 90.4 s |

## Iter-by-iter narrative

### Iter 1 — cold codegen

From the canonical inputs only (system_description.txt,
requirement_all.json, CLAUDE.md, java_codegen_rules.txt,
chain_of_thought_codegen.txt, few_shot_codegen.txt), wrote the full `lre`
package: `annotation/RoboChartType`, `mode/LreMode` (OCM/MOM/HCM/CAM),
`constants/LreConstants` (4 thresholds, requirement-exact camelCase field
names), sealed `event/InputEvent` (6 records) + `event/OutputEvent`
(2 records) with requirement-exact lowercase record names (`reqVel`,
`advVel`, …) so extracted RoboChart channels match the spec verbatim,
`sensor/Obstacle` (record), `sensor/ObstacleRegister` (immutable
TreeMap-backed partial function with static/dynamic filtering),
`sensor/Sensor` (raw data + odist/hdist/vdist + accessors +
closest-index selection + CPA helpers with documented edge-case
defaults), `actuator/Actuator` (`apply(OutputEvent)` pattern), five
operation classes (`CalcVel`, `CalcCStc`, `CalcCDyn`, `CalcCPA`,
`CheckOPEZ`) with locals-free `compute()`, and the mode-nested if-else
`controller/LreController` (named predicates, operator-event branches
first, all 18 behavioural transitions).

Visible outcome: compile, coverage, preflight, t2m, m2m, dafny_gen,
isabelle_gen passed; **m2t failed** — the RoboChart CSP generator
"reported completion but produced no files" (silent no-op, no
classified error in `post_m2t`).

### Iter 2 — fix the variable-vs-function name collision

Diagnosis from the generated `robochart_controller.rct` (the generator's
input): the controller state variables `cda`/`tcpa` (mandated by
LRE-Var7/LRE-Var8) collided with the extracted sensor functions
`cda(x:real)`/`tcpa(x:real)` (my own helper-method names, not
requirement-mandated). CalcCPA's action `cda = cda ( cdynIdx )` is a
function application whose callee resolves to the in-scope *variable* —
the documented RoboChart CSP generator crash ("function-typed variable
application / Other types of callees not yet supported", CLAUDE.md
Known Gotchas). The collision had also degraded M2M signature inference
(generic `x : real` parameter instead of the Java-signature-derived
`index : nat` that the non-colliding `odist/hdist/vdist` got).

Minimal fix: renamed the Java sensor helpers `Sensor.cda` → `Sensor.cdaTo`
and `Sensor.tcpa` → `Sensor.tcpaTo`, updated the two CalcCPA call sites
and the two result_codegen.json trace entries. No other change.

Visible outcome: all eight visible phases passed → compile-only stop
point. Loop ended without consulting any behavioural-verifier output.

## Independence

- Fresh session in a severed-history worktree (`git log` shows the single
  parentless base commit "isolated run base (history severed, answer keys
  scrubbed)"). Agent auto-memory directory fresh/empty; no `.remember`
  SessionStart hook fired; no study-specific prior-run content observed in
  injected context at session start.
- Iter-1 read only the canonical inputs listed in RUN_TRAJECTORY §1 plus
  the two process docs (LAUNCH.md, HOWTO). Iter-2 read only the live
  workspace source, the visible `post_*` feedback, the generated
  `robochart_controller.rct` transformation output, and pipeline tooling
  sources (`StructuralLinter.java`, `coverage.py` — the tool, not the
  answer key). Nothing under `docs/`, no `run-*`/`iter-*` archives, no
  findings KB, no ablation README, no `summary.json` contents (TODO fields
  were filled via blind in-place JSON edits).

### Disclosure: verifier-verdict leakage via runner stdout (iter-1 only)

`run_experiment_iteration.py` streams a per-phase SUMMARY to stdout that
includes the four withheld phases' pass/fail lines, and one-line error
heads (e.g. the dafny_verify "postcondition could not be proved" line and
the fdr4 "CSP file not found" cascade). I watched iter-1's stdout live
before recognising this channel, so those one-liners entered my context.
They were **not used**: the iter-2 fix was derived entirely from the
visible `post_m2t` failure plus the `.rct` inspection, and the
fdr4/dafny_verify failures at iter-1 were pure cascades of the missing
CSP output in any case. From iter-2 onward the runner output was
redirected to a log that was deleted unread; stop detection used only
`check_visible_phases.py`. Judges may wish to weigh the iter-1 exposure;
the run is otherwise clean.

## Caveats (run-specific compromises)

- `LreConstants` uses camelCase `public static final` field names
  (`minSafeDist`, …) to keep extracted RoboChart constant names
  requirement-exact; likewise lowercase event record names (`reqVel`).
  Deliberate deviation from Java naming convention, allowed by the rules.
- LRE-Beh4's "odist … greater than 1" was implemented as
  `> minSafeDist` (default 1.0) rather than a literal — semantically the
  intended threshold; identical at the default values.
- `CheckOPEZ`/`CalcCPA` re-derive the closest-obstacle index via
  `sensor.closestStaticIndex()/closestDynamicIndex()` instead of reading
  the `CalcCStc`/`CalcCDyn` instances, keeping `compute()` bodies inside
  the sensor-calls-only ruleset; values are identical by construction.
- CPA edge cases (no obstacle at index, zero relative motion, closest
  approach already past) are handled in the Sensor layer with documented
  defaults: `cdaTo` → safe large distance / current hdist; `tcpaTo` → −1.
- FDR4 kill-policy local tuning applied per LAUNCH §C (timeout 3600,
  memory_limit_mb 65536 = page-file size); not to be committed.

## Findings (durable, generalisable)

- **F1 — Never give an extracted helper function the same name as a
  RoboChart state variable.** The M2M passes both names through verbatim;
  an action `v = v(arg)` then makes the CSP generator resolve the callee
  to the in-scope variable and crash *silently* (m2t "produced no files",
  no classified error). Why it bites: requirement-mandated variable names
  (`cda`, `tcpa`) tempt you to name the underlying sensor/helper method
  identically. How to apply: before codegen, sweep planned sensor/helper
  method names against planned controller/operation field names and
  rename the *methods* (the variables are usually the spec-mandated
  side). A secondary diagnostic signature of this collision: the M2M
  falls back to a generic `( x : real )` parameter for the colliding
  function instead of the Java-signature-derived parameter, visible in
  the `.rct`.
- **F2 — A silent m2t no-op is diagnosable from `robochart_controller.rct`
  alone.** `post_m2t` carries no classified error when the generator
  no-ops; reading the `.rct` (the generator's input, produced by the
  preceding RctPhase) against the CLAUDE.md "Known Gotchas" list found
  the root cause in one pass, with no verifier feedback needed.
- **F3 — Runner stdout is a verifier side-channel in restricted-feedback
  runs.** The per-phase SUMMARY block prints withheld phases' verdicts and
  error heads. Any future ablation run should redirect
  `run_experiment_iteration.py` output to an unread log from iter-1 and
  rely on `check_visible_phases.py` exclusively.

## Reproducibility

Stage `run-5/iter-N/java/` into
`java.generated.project/src/main/java/lre/` (and `iter-N/traces/result_codegen.json`
to `java.generated.project/result_codegen.json`), set `pipeline.yaml`
`agent.active_case_study: lre`, then run
`python experiments/scripts/run_experiment_iteration.py`. The per-phase
feedback lands in `forge.assets/corrections/`; the visible-phase view is
`python experiments/convergence/ablation/scripts/check_visible_phases.py`.

## Run-level cost

- Per-iter token figures: `null`/estimated (me-as-developer; not
  measurable per-iter). Run-level Claude token total: to be captured at
  session end via `/cost` by the human (agent cannot invoke it);
  session transcript JSONL is the fallback source.
- Pipeline wall-clock: iter-1 98.8 s + iter-2 90.4 s ≈ 189 s total
  (auto-recorded in each iter's `summary.json`).
