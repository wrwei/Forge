# SRanger Convergence Trajectory — run-5

- **Study:** sranger (package `sranger`)
- **Actor:** me-as-developer (interactive Claude Code session; no sub-agents)
- **Condition:** B (honest independent — playbook absent from the checkout and never read)
- **Date:** 2026-06-04
- **Outcome:** **converged at iter 2** (cap 7)
- **Base:** severed single-commit worktree (`d488524 isolated run base`), clean
  `CLAUDE_CONFIG_DIR`, no `.remember` hook.

## Independence

Iter-1 cold codegen read ONLY the canonical inputs:
`forge.assets/case-studies/sranger/system/system_description.txt`,
`forge.assets/case-studies/sranger/requirements/requirement_all.json`,
`CLAUDE.md`, the three `forge.assets/prompts/*codegen*.txt` files,
`experiments/HOWTO_RUN_CONVERGENCE_EXPERIMENT.md`, and
`experiments/RUN_TRAJECTORY.md`. Pipeline *sources* consulted as the tool
(allowed by RUN_TRAJECTORY §1): `java2robochart.etl` (Final-state lint,
Clock/`since()` rewrite, multi-arg actuator-call handling,
`extractOutputEventInfo`), `forge.dashboard/web/feedback/coverage.py`
(result_codegen.json schema), `forge.dashboard/web/csp_corrections.py`
(what the pre-FDR4 correction step can and cannot rewrite). No path under
`docs/`, `experiments/convergence/run-*`, `reference-runs/`, or
`experiments/cold-baseline/` was opened; the stale base-tree
`result_codegen.json` and `post_codegen.*` were deleted/overwritten
without being read. Session-start context audit: no agent auto-memory and
no `.remember` output were injected; the git-status snapshot exposed only
scrub-deletion *paths* (chemical_detector cold-baseline filenames, no
sranger run content, no iteration counts). Judged clean for condition B.

## Headline

| iter | change | compile | coverage | preflight | t2m | m2m | m2t | dafny_gen | isabelle_gen | fdr4 | dafny_verify | isabelle_verify | vacuity | pipeline wall-clock | converged |
|------|--------|---------|----------|-----------|-----|-----|-----|-----------|--------------|------|--------------|-----------------|---------|--------------------:|-----------|
| 1 | cold codegen (8 files, 206 LoC) | ✓ | ✓ | ✗ | ✓ | ✓ | ✓ | ✓ | ✓ | ✗ | ✗ | ✓ | ✓ | 51.2 s | no |
| 2 | 9 `@RoboChartType("real")` annotations; `TURN_VEL` 2.0→1.0; Turning→Moving gated on `Tick` | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | 50.1 s | **yes** |

FDR4 checked the auto-discovered untimed
`SRangerController_coreassertions.csp`: 2× deadlock-free + divergence-free
(determinism stripped by `run_fdr4` per pipeline policy). Isabelle: 1
theory, 10 lemmas, deadlock-freedom proved for
`SRangerController_deadlock_free` (~20 s in WSL). Vacuity: 0 findings both
iters.

## Iter-by-iter narrative

### Iter 1 — cold codegen (9/12 phases passed)

Generated `annotation/RoboChartType`, `mode/SRangerMode`
(`Moving`/`Turning`/`Halted`), `constants/SRangerConstants`,
`event/InputEvent` (sealed; `Obstacle`/`Tick`/`EndTask` signal records),
`sensor/Sensor` (`distance()`, large no-reading default 1000.0),
`sensor/Clock` (`now()`/`setTime()`), `actuator/Actuator`
(`move(lv, av)` storing last command), `controller/SRangerController`
(single `step(InputEvent)` with named predicates `obstacleDetected`,
`turnDurationElapsed`; pure two-level mode-nested if-else), plus
`result_codegen.json` tracing all 24 requirements.

Up-front design decisions taken from canonical inputs (no feedback yet):

- **Terminal mode named `Halted`, not `Final`** — CLAUDE.md states the
  theory-generated (primary) controller must not contain a `Final` state;
  SRanger is single-controller, so its controller is the primary. `Halted`
  is an ordinary absorbing state: entered on `endTask` with entry
  `move(0,0)`, tick self-loop with no action (bare-precondition pattern
  (b)). Result: the Isabelle deadlock-freedom proof passed on the very
  first pipeline contact.
- **`Move(lv, av)` realised as the two-argument `Actuator.move(lv, av)`
  call**, which the M2M turns into an LOperations `Call` preserving both
  payloads. (Verified in the ETL first: the `OutputEvent`-record path
  keeps only the **first** constructor argument, so a
  `record Move(double lv, double av)` event would silently drop `av`.)
- **`Clock` class + `timer` field + `this.clockResetTime = timer.now()`**
  → clock promotion and `since(clockResetTime) >= turnDuration` rewrite
  fired exactly as the ETL conventions describe; `.rct` shows
  `clock clockResetTime`, the `# clockResetTime` reset on Moving→Turning,
  and the `since` guard on Turning→Moving.

Failures and diagnosis:

1. **preflight** — 9× `rule4_double_missing_real_annotation`: every
   `double` parameter/field needs `@RoboChartType("real")`, including
   private constants (`Sensor.NO_READING_DEFAULT`), the four
   `SRangerConstants` fields, and framework-facing setter params
   (`Sensor.update#reading`, `Clock.setTime#t`, `Actuator.move#lv/av`).
2. **fdr4** — reported only "3 assertion(s) checked, 0 passed,
   3 inconclusive" with an **empty issues list**. Running `refines.exe`
   manually on the auto-discovered file surfaced the real error:
   `SRangerController::moveCall.0.2 ... second field: 2 is not a member
   of the set {0, 1}` — the Turning entry action `move(0, turnVel)`
   carries the constant `turnVel = 2` (from SR-DM2's 2.0), which exceeds
   the experiment-mandated `{0..1}` channel ranges. The constants are
   let-bound inside the generated `*_coreassertions.csp` (from the `.rct`
   Constants defaults), out of reach of `apply_csp_corrections` /
   `csp_overrides.csp`, which only rewrite `instantiations.csp` blocks —
   so the fix has to be on the Java side.
3. **dafny_verify** — `transitionFromTurning` has
   `ensures now() - clockResetTime >= 2.0 ==> mode == Moving`, but the
   higher-priority `EndTask` branch sends the controller to `Halted`
   while the premise can be true. Exactly the autonomous-preemption
   pattern HOWTO §3 documents, with the documented fix (gate on `Tick`).

### Iter 2 — three minimal fixes (12/12 passed → converged)

1. Added the 9 flagged `@RoboChartType("real")` annotations.
2. `TURN_VEL` 2.0 → 1.0 (documented spec compromise; see Caveats).
3. Turning→Moving branch became
   `else if (event instanceof InputEvent.Tick && turnDurationElapsed)`,
   making the generated Dafny premise event-specific
   (`event == Tick && elapsed ==> mode == Moving`), no longer violable by
   `EndTask`. `Turning` keeps bare-precondition cover through the
   `endTask` and `tick` self-loop operations, so the Isabelle proof was
   unaffected.

All 12 phases passed; vacuity 0 findings. Converged.

## Caveats (run-specific compromises)

- **`TURN_VEL` scaled 2.0 → 1.0** (deviates from SR-DM2's `turnVel = 2.0`).
  Required because the value rides the `moveCall` channel whose fields are
  `core_real = {0..1}` under the experiment's fixed type ranges. Forward /
  turn / stop commands stay distinguishable by argument position
  (`move(1,0)` / `move(0,1)` / `move(0,0)`); deadlock/divergence verdicts
  do not depend on the magnitude.
- **Turning→Moving gated on `Tick`** (deviates from SR-Beh5's "autonomous,
  no event required"). Cycle-level semantics preserved — time is sampled
  once per control cycle anyway.
- **Terminal mode renamed `Final` → `Halted`** (SR-DM1/SR-FR3 naming), with
  an invented no-action tick self-loop on `Halted` (not in SR-Beh1..7; no
  source-mode/trigger duplication, so SR-DC1 holds).
- **`turnDuration = 2` exceeds `core_clock_type = {0..1}`**, so in the
  *timed* CSP instantiation the `since(clockResetTime) >= turnDuration`
  guard is unsatisfiable. The verified assertions are on the untimed
  `SRangerController_coreassertions.csp` the runner auto-discovers (the
  pipeline's standard choice), where the verdicts passed; the timed model
  was not separately checked.
- `obstacleThreshold` 0.5 is ceiled to 1 in the model constants
  (CSP-gen v3.0.0 limitation, automatic; guard `distance <= 1` over
  `{0..1}` is always true, which only widens the checked behaviour).
- **Tooling workarounds (not committed):** machine env `GRADLE_USER_HOME`
  pointed at a non-existent `D:\repos\repository` — overridden per command
  to `C:\Users\Will\.gradle`; `pipeline.yaml` local tuning per
  RUN_TRAJECTORY §2 (fdr4 `timeout: 3600`, `memory_limit_mb: 51200` =
  page-file size, corrected `dafny_path` win32 and `wsl_isabelle_bin`
  user paths).

## Run-level Claude token total

Source: summed `message.usage` blocks from the driving session's
transcript JSONL (isolated `CLAUDE_CONFIG_DIR`;
`projects/C--Users-Will-Gitee-fmgvc-sranger-run8/06621ed1-….jsonl`),
captured at archive time — `/cost` is not exposed to the agent. Slightly
undercounts the final archive-commit messages written after capture.

- input_tokens (uncached): **8,997**
- cache_creation_input_tokens: **797,147**
- cache_read_input_tokens: **19,449,534**
- output_tokens: **319,092**
- total input incl. cache: **20,255,678** (152 assistant messages)

Per-iter token fields in `summary.json` are `null` / `kind: "estimated"`
per protocol (not measurable per-iter from inside the session).

## Findings (durable, generalisable)

**F1 — Spec constants that ride a channel must fit the FDR4 type ranges,
and only a Java-side change can fix them.** The CSP generator inlines the
`.rct` Constants defaults as `let`-bound `const_<Stm>_<name>` values
inside the generated `*_coreassertions.csp`. The pre-FDR4 correction step
(`apply_csp_corrections` + `csp_overrides.csp`) rewrites only
`-- generate` blocks in `instantiations.csp`, so it **cannot** reach
those constants. Under the experiment's `{0..1}` ranges, any constant
> 1 that is passed as an event/operation payload (e.g. a velocity handed
to an actuator call) kills every assertion at load time. *How to apply:*
at cold-codegen time, scale payload-carried constants into the verifier
range (documenting the deviation), or catch it on iter 2 from the
refines error; guard-only constants (thresholds, durations) merely make
guards trivially true/false and don't error.

**F2 — `post_fdr4` "N inconclusive" with an empty issues list means the
CSP failed to *load*, and the feedback layer won't tell you why.** A
channel-range violation (F1) aborts evaluation before any assertion runs;
the runner classifies this as "0 passed, N inconclusive, 0 errors" and
emits no issue, no fix directive, and a misleadingly tiny wall-clock
(~1 s). *How to apply:* whenever FDR4 reports inconclusive assertions,
re-run `refines.exe` manually on the file named by the
"auto-discovered" line in `post_fdr4.md` and read the raw error; don't
guess from the (empty) structured feedback. (Mind RUN_TRAJECTORY §2's
kill policy — manual *diagnostic* runs are fine; never interrupt the
pipeline's own live FDR4 run.)

**F3 — Preflight's rule4 is stricter than the codegen-rules text reads:
EVERY `double` field and parameter needs `@RoboChartType("real")`,
including private constants and framework-facing setter parameters that
never reach the model** (`Sensor.update(double)`, `Clock.setTime(double)`,
a private `static final` default). *How to apply:* annotate every
`double` declaration site mechanically during cold codegen; it's nine
silent error-severity lint hits otherwise, and preflight failure blocks
nothing downstream in this pipeline (t2m..isabelle still ran) but costs
the iteration its converged status.

**F4 — (Confirmation) The two documented patterns fired exactly as the
canonical inputs describe, and applying them proactively/on-cue is what
made this a 2-iter run:** (a) CLAUDE.md's "no `Final` state on the
theory-generated controller" — implemented at iter 1 as a *renamed*
ordinary absorbing state (`Halted` + no-action tick self-loop + entry
`move(0,0)`), which kept the spec's terminal semantics and let the
Isabelle deadlock-freedom proof pass on first contact, no rerouting to a
live operational state needed; (b) HOWTO §3's Dafny
autonomous-preemption fix (gate the autonomous transition on `Tick`) —
the iter-1 failure matched the documented symptom verbatim and the
documented fix resolved it in one edit.

## Reproducibility

1. Stage iter-N: copy `run-5/iter-N/java/**` to
   `java.generated.project/src/main/java/sranger/` and
   `run-5/iter-N/traces/result_codegen.json` to
   `java.generated.project/result_codegen.json`.
2. Apply RUN_TRAJECTORY §2 local tuning to `pipeline.yaml` (fdr4
   `timeout: 3600`, `memory_limit_mb` = machine page-file MB, machine
   `dafny_path` / `wsl_isabelle_bin`), confirm
   `agent.active_case_study: sranger` and `{0..1}` ranges in
   `forge.dashboard/corrections/type_ranges.json`.
3. Run `python experiments/scripts/run_experiment_iteration.py` (ensure
   `GRADLE_USER_HOME` resolves to a writable path). Per-phase verdicts in
   `forge.assets/corrections/post_<phase>.json`; timing in
   `phase_timings.json`.


## Execution time (recovered post-hoc)

Recovered from the driving session's transcript JSONL (first→last message timestamp); the per-phase deterministic times are from each `iter-N/summary.json`. Cross-run table: [../../execution_times.md](../../execution_times.md).

- **End-to-end (session):** 24.7 min
- **Pipeline (Σ deterministic phases, raw):** 1.7 min — FDR4 0.0 min · Isabelle-verify 0.9 min · other 10 phases 0.8 min
- **Agent (end-to-end − pipeline):** 23.0 min — codegen + feedback diagnosis + this write-up + idle (not pure codegen)
