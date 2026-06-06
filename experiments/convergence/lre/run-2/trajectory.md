# LRE Convergence Trajectory — run-2

- **Actor:** me-as-developer (the LLM agent in the user's session edited all
  Java directly; no `Agent`-tool sub-agents).
- **Condition:** B (honest independent — playbook absent).
- **Study / package:** `lre` / `java.generated.project/src/main/java/lre/`.
- **Date:** 2026-06-05.
- **Outcome:** **converged at iter 2** (all 12 phases passed, vacuity audit
  0 findings).

## Independence

This run was driven in a scrubbed, history-severed worktree
(`fmgvc-lre-run5-redo`, single parentless base commit; `experiments/convergence/`,
`reference-runs/`, `experiments/cold-baseline/`, the playbook, the findings
knowledgebase, and `.remember/` were absent from the tree) under a clean
`CLAUDE_CONFIG_DIR` (`C:\tmp\claude-clean-lre-run5`, empty agent memory).
No prior-run narratives, memory lines, or `.remember` content were injected
into the session context at start; the git snapshot exposed only the severed
base commit. Nothing under `docs/` was read at any point.

Iter-1 cold codegen read ONLY the canonical inputs:
`forge.assets/case-studies/lre/system/system_description.txt`,
`forge.assets/case-studies/lre/requirements/requirement_all.json`,
`CLAUDE.md`, `forge.assets/prompts/java_codegen_rules.txt`,
`forge.assets/prompts/chain_of_thought_codegen.txt`,
`forge.assets/prompts/few_shot_codegen.txt`,
`experiments/HOWTO_RUN_CONVERGENCE_EXPERIMENT.md`, and
`experiments/RUN_TRAJECTORY.md`. Tool-side (non-answer) reads:
`pipeline.yaml`, `forge.dashboard/corrections/type_ranges.json`, and
`forge.dashboard/web/feedback/coverage.py` (to learn the
`result_codegen.json` schema). Iter 2 read only the live workspace source
and iter-1's `post_*` feedback.

## Headline

| Iter | Change | compile | coverage | preflight | t2m | m2m | m2t | dafny_gen | isabelle_gen | fdr4 | dafny_verify | isabelle_verify | vacuity | Pipeline wall-clock | Converged |
|------|--------|---------|----------|-----------|-----|-----|-----|-----------|--------------|------|--------------|-----------------|---------|--------------------:|-----------|
| 1 | Cold codegen: 15 files, 652 LOC, all 52 requirements | ✅ | ✅ | ❌ (21 lint errors) | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ 3/3 | ❌ (6 postcondition errors) | ✅ (20 lemmas + deadlock-free) | ✅ 0 findings | 140.8 s | no |
| 2 | +21 `@RoboChartType("real")` annotations; Tick-gated autonomous transitions with higher-priority guard negations | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ 3/3 | ✅ | ✅ (20 lemmas + deadlock-free) | ✅ 0 findings | 145.2 s | **yes** |

## Iter-by-iter narrative

### Iter 1 — cold codegen

From the canonical inputs alone, produced the full `lre` package: annotation
(`RoboChartType`), mode enum (`LreMode`: OCM/MOM/HCM/CAM), constants
(`LreConstants`, camelCase names matching the spec), sealed event interfaces
(`InputEvent` with `reqVel`/`reqHdng`/`reqOCM`/`reqMOM`/`reqHCM`/`endTask`,
`OutputEvent` with `advVel`/`advHdng` — record names verbatim from LRE-DM6/7
so extracted channels carry the spec names), datamodel (`Obstacle` record,
copy-on-write `ObstacleRegister`), `Sensor` (distance functions with
safe-large default 1.0e6, zero-default accessors, closest-static/dynamic
selection), `Actuator` (`apply(OutputEvent)` — the pattern the EGL detects
for entry actions), five operations (`CalcVel`, `CalcCStc`, `CalcCDyn`,
`CheckOPEZ`, `CalcCPA`), and the mode-nested if-else `LreController` with
named predicates, no `Final` state, and a bare-precondition event branch in
every mode (OCM: reqVel/reqHdng; MOM/HCM/CAM: reqOCM).

Design choices logged in `post_codegen`: `cstc`/`cdyn` kept `int` (the −1
sentinel contradicts `nat`); CalcCPA implemented as a horizontal-plane CPA
whose separation term uses `hdist(cdyn)²` so the sensor's large-distance
default yields a safe `cda` when no dynamic obstacle exists; CheckOPEZ/CalcCPA
read `closestStaticIndex()`/`closestDynamicIndex()` from the Sensor directly
(the compute() RHS whitelist excludes cross-operation getters); transition
priority order invented (operator events > inOpez > CAM guard > HCM guards).

First pipeline invocation hit two **environmental** failures (Dafny and
WSL-Isabelle paths in `pipeline.yaml` pointed at another user's home);
fixed the paths (machine-local tuning, uncommitted) and re-ran unchanged —
that re-run provides iter-1's authentic verdicts. Result: only two genuine
failures —

- **preflight** (21 × `rule4_double_missing_real_annotation`): the lint
  requires `@RoboChartType("real")` on *every* double field/parameter,
  including model-invisible ones (Actuator latches, `Sensor.SAFE_LARGE_DISTANCE`,
  `updateVehicle` params, Obstacle components, constants).
- **dafny_verify** (6 errors, all `dafny_postcondition`, in
  `transitionFromMOM`/`transitionFromHCM`): the Dafny generator emits each
  autonomous branch as an unconditional `ensures guard ==> mode' == X`,
  without the event conjunct or negations of higher-priority guards. Any
  preempting branch (reqOCM/endTask/reqHCM, or an earlier autonomous guard
  like `inOpez` overlapping `cda<1 ∧ tcpa≥0`) then violates the implication
  on its return path. An encoding mismatch, not a control-logic bug — FDR4
  (deadlock/divergence) and Isabelle (20 lemmas + `deadlock_free`) already
  passed on the same model.

### Iter 2 — minimal feedback-driven fixes

1. Added the 21 missing `@RoboChartType("real")` annotations exactly where
   flagged.
2. Made every generated `ensures` premise self-contained:
   - added `InputEvent.Tick` (control-cycle event, no operator payload) and
     gated all autonomous branches on `event instanceof InputEvent.Tick` —
     event-triggered return paths now falsify autonomous premises;
   - conjoined explicit negations of higher-priority *same-event* guards
     where target modes differ, via composite named predicates
     (`camGuard`, `hcmHorizGuard`, `hcmVertGuard`, `backToMomGuard`):
     MOM: `Tick∧inOpez → OCM`; `Tick∧!inOpez∧camGuard → CAM`;
     `Tick∧!inOpez∧!camGuard∧<hcmGuard_i> → HCM` (the three HCM-target
     guards are NOT mutually excluded — same conclusion, no conflict).
     HCM and CAM analogously.

All 12 phases passed; vacuity audit clean. Converged.

## Run-level cost

- **Claude token total (run-level, source: summed `usage` blocks from the
  driving session's transcript JSONL, captured at write-up time):**
  output ≈ **306 k**, non-cached input ≈ 5.8 k, cache-read input ≈ 15.39 M,
  cache-creation input ≈ 0.77 M, over 113 assistant turns. `/cost` is not
  exposed to the agent as a tool; the JSONL sum is the stated fallback.
  The trajectory write-up tail (after capture) adds a small amount not
  included here.
- **Run-level end-to-end execution time** (from the same transcript,
  `max(timestamp) − min(timestamp)` at write-up time):
  - **end-to-end ≈ 1,356 s (≈ 22.6 min)** (excludes the write-up tail after
    capture);
  - **pipeline = 286.0 s** (Σ `pipeline_wall_clock_s`: 140.8 + 145.2; the
    discarded env-broken first invocation, ~64 s, is not counted);
  - **agent ≈ 1,070 s** (end-to-end − pipeline: codegen + diagnosis +
    snapshots + write-up + idle).
- Per-iter token fields in `summary.json` are `null` / `kind: "estimated"`
  per protocol (not separately measurable in-session).

## Caveats (run-specific compromises)

- `pipeline.yaml` carried two wrong machine paths (`dafny_path.win32` and
  `wsl_isabelle_bin` pointed at `willr`'s home); corrected locally to this
  machine's paths (`C:\Users\Will\dafny\dafny\Dafny.exe`,
  `/home/will/isabelle/Isabelle2023-CyPhyAssure/bin/isabelle`). Uncommitted,
  per protocol. The first pipeline invocation was discarded (environmental,
  not snapshotted); its only genuine verdicts (preflight failure, and passes
  elsewhere) were identical in the clean re-run.
- `Tick` is an input event beyond LRE-DM6's six-event list — an
  encoding-driven addition so autonomous guard evaluation is event-specific
  in the extracted model. It carries no domain behaviour.
- The guard negation conjuncts (`!inOpez`, `!camGuard`) are redundant in
  Java (else-if already encodes priority) but make the extracted formal
  guards mutually exclusive where targets differ.
- `cstc`/`cdyn` are `int`, not `nat`, despite LRE-Var5/6 saying "natural
  number" — their −1 sentinel cannot inhabit `nat`.
- CAM has no entry action: "evasive manoeuvres" (LRE-FR4) has no defined
  advVel/advHdng output in the requirements.

## Findings (durable, generalisable)

**F1 — Dafny's autonomous `ensures` premises are emitted verbatim from the
branch guard; make every guard self-contained at codegen time.** The Dafny
generator turns each guarded branch into `ensures <guard> ==> mode' == X`
with no event conjunct and no negation of higher-priority branches, so any
preempting branch violates lower branches' implications (iter-1: 6 errors in
MOM/HCM, e.g. the `endTask` return path with `cda<1 ∧ tcpa≥0` true).
**How to apply:** in any future run, write autonomous transitions gated on a
dedicated `Tick` input event AND conjoin negations of every higher-priority
same-event guard whose target mode differs (composite named predicates keep
this within the predicate rules). Branches sharing a target mode need no
mutual exclusion — identical conclusions cannot conflict. Doing this in
iter 1 would likely have saved the iteration.

**F2 — Preflight rule4 wants `@RoboChartType("real")` on *every* double
field and parameter, not just model-relevant ones.** It flagged Actuator
latch fields, a private static sensor default (`SAFE_LARGE_DISTANCE`),
`updateVehicle` parameters, record components, and constants-class fields —
none of which CLAUDE.md's "annotate fields whose Java type does not directly
convey the RoboChart type" phrasing obviously demands. **How to apply:**
annotate all doubles (fields, params, record components) uniformly at cold
codegen; it is cheap and removes a whole failure class. (Method *return*
types were not flagged.)

**F3 — When an operation's spec says "uses the index computed by
OperationX", read the equivalent sensor selector directly instead of the
other operation's getter.** The compute() RHS whitelist (sensor calls,
Math.sqrt, arithmetic, own fields) excludes cross-operation getters.
`CheckOPEZ`/`CalcCPA` call `sensor.closestStaticIndex()`/`closestDynamicIndex()`
themselves — same value by construction, and the pipeline accepted it
end-to-end (Dafny abstracts the sensor functions as uninterpreted).

**F4 — A 'closest distance' quantity computed from zero-default accessors is
a safety trap; route its magnitude through a large-default distance
function.** LRE-DM5 makes field accessors return 0 when no obstacle exists;
a CPA formula built only on accessors then yields `cda = 0` ⇒ spurious
CAM entry whenever no dynamic obstacle exists. Using `hdist(cdyn)²` (default
10¹²) as the separation term makes the no-obstacle case safe purely through
the sensor-layer defaults, with no sentinel checks in the operation. Check
every derived quantity's missing-data limit against the guards it feeds.

**F5 — Environment verification before iter 1 should include executing the
verifier binaries, not just reading the config.** Both `dafny_path.win32`
and `wsl_isabelle_bin` were syntactically plausible but pointed at another
user's home; the cost was a full discarded pipeline invocation. A 2-second
`Test-Path` + `wsl ls` of the two configured binaries during §2 setup would
have caught it before any run.

## Reproducibility

1. Stage iter-N source: copy `run-2/iter-N/java/` over
   `java.generated.project/src/main/java/lre/` and `run-2/iter-N/traces/result_codegen.json`
   to `java.generated.project/result_codegen.json`.
2. Ensure `pipeline.yaml`: `agent.active_case_study: lre`, valid
   `dafny_path`/`wsl_isabelle_bin` for the machine, fdr4 `timeout: 3600`.
3. `python experiments/scripts/run_experiment_iteration.py` — per-phase
   verdicts land in `forge.assets/corrections/post_<phase>.json`; compare
   with `iter-N/summary.json.phase_results`.
