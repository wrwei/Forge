# Convergence Trajectory — chemical_detector, run-4

- **Study:** chemical_detector (package `chemdetector`)
- **Condition:** B (honest independent — playbook absent)
- **Actor:** me-as-developer (single Claude Code session; no sub-agents)
- **Date:** 2026-06-05
- **Base commit:** `7539d3e` — "isolated run base (history severed; answer material excluded from tree)" (parentless severed-history worktree per RUN_TRAJECTORY Appendix)
- **Converged:** YES, at **iter 2** (all 12 phases passed; vacuity audit 0 findings)

## Headline table

| Iter | Change | compile | coverage | preflight | t2m | m2m | m2t | dafny_gen | isabelle_gen | fdr4 | dafny_verify | isabelle_verify | vacuity | Pipeline wall-clock (s) | Converged |
|------|--------|---------|----------|-----------|-----|-----|-----|-----------|--------------|------|--------------|-----------------|---------|------------------------|-----------|
| 1 | Cold codegen: 17 files, two controllers, no Final states, total guard covers | ✓ | ✓ | ✗ | ✓ | ✓ | ✓ | ✓ | ✓ | ✗ | ✗ | ✗ | ✓ | 644.4 | no |
| 2 | 3 `@RoboChartType("real")` on constants; predicate `goreq(ins,THR)` → `ins >= THR`; Analysis enum-split → propositional split | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | 66.0 | **yes** |

Iter-2 verification detail: FDR4 9/9 assertions (deadlock-freedom ×2 forms + divergence-freedom, each for the System module and both controller refs; the 3 `:[deterministic]` assertions stripped per pipeline policy); Dafny 9 contracts verified; Isabelle 10 lemmas incl. `GasAnalysisController_deadlock_free`; vacuity 0 findings.

## Independence statement

**Inputs read for iter-1 cold codegen** (the canonical set only): the
chemical_detector `system_description.txt` and `requirement_all.json`,
`CLAUDE.md`, `java_codegen_rules.txt`, `chain_of_thought_codegen.txt`,
`few_shot_codegen.txt`, `HOWTO_RUN_CONVERGENCE_EXPERIMENT.md`,
`RUN_TRAJECTORY.md`. Tool sources consulted (permitted, "the tool, not the
answer key"): `forge.transformations/.../java2robochart.etl` (convention
constants, transition/communication extraction paths),
`forge.dashboard/web/feedback/coverage.py` (result_codegen.json schema),
generated pipeline outputs (`robochart_controller.rct`, `post_*.{md,json}`).
Nothing under `docs/` was opened; no `experiments/convergence/` run dirs,
`reference-runs/`, or cold-baseline artefacts existed in (or were read from)
this scrubbed worktree. No `.remember` hook fired; the agent auto-memory
namespace was fresh/empty (clean `CLAUDE_CONFIG_DIR`); `git log` shows only
the single severed base commit.

**Exposures to disclose** (content reaching the session through
setup-mandated or tool files, none of it consumed as a fix recipe):

1. `pipeline.yaml` (read as required by RUN_TRAJECTORY §2) contains a
   "historical note" comment in the `fdr4` block narrating a *prior*
   chemdetector experiment's iter-8/iter-9 InputEnv/OutputEnv workaround and
   its proper fix at the M2M template level. It leaks that a prior chemdetector
   run existed and reached ≥9 iterations *under the old pipeline*, and that
   boundary STM stubs no longer exist. It did not describe any failure this
   run encountered.
2. Tool-source comments name chemdetector examples: the ETL header notes
   annotation equivalents "defined under `chemdetector.annotation.*`", an ETL
   comment shows `(GasAnalysisEvent.Gas) event` as a payload-cast example, and
   `coverage.py` has a comment "GasAnalysisMode's four enum values trace to
   CD-GA-FR1..4". HOWTO §2 (canonical input) mentions chemdetector's
   `record Obstacle(Loc payload)` in its sub-agent-ban anecdote. These
   influenced naming choices (which the conventions would have produced
   anyway), not fixes.
3. `forge.assets/corrections/csp_overrides.csp` (a tracked pipeline input,
   read to know what the FDR4 phase applies) contains **LRE-only** function
   stubs — cross-study tool configuration, no chemdetector content.

## Iter-by-iter narrative

### Iter 1 — cold codegen

From the canonical inputs alone, generated 17 Java files (561 LOC):
two controllers per CD-ARCH2 — `GasAnalysisController` (Reading, Analysis,
NoGas, GasDetected + a live `Done` mode replacing final state j1) and
`MovementController` (Waiting, Going, Avoiding, TryingAgain, AvoidingAgain,
GettingOut, Found self-looping on `stop` instead of transitioning to j1) —
plus sealed `InputEvent`/`OutputEvent` hierarchies (turn/stop/resume present
in both so the M2M unifies them as Shared inter-controller events),
`VehicleSensors` (analysis/intensity/location/goreq/odometer), `Vehicle`
(move/randomWalk/shortRandomWalk/changeDirection/pause), `Clock`, `Constants`
(all values 1 to fit the {0..1} FDR type ranges), four domain enums,
`GasSensor` record, `@RoboChartType`. Design rationale recorded in
`feedback/post_codegen.md`. `result_codegen.json` traces all 88 requirement
ids.

First pipeline invocation hit two **machine-local environment faults** (not
verifier verdicts): `pipeline.yaml`'s `dafny_path` and `wsl_isabelle_bin`
pointed at another user's home (`willr`). Fixed locally (uncommitted, per
RUN_TRAJECTORY §6) and re-ran the pipeline unchanged — per HOWTO §0 ("bridge
failing, not the proof failing... resume the iteration"). The snapshot records
the second, complete run. Genuine iter-1 verdicts:

- **preflight FAILED** — 3 errors: `Constants.THR/LV/STUCK_DIST` are doubles
  without `@RoboChartType("real")`.
- **fdr4 FAILED** — CSP type error `Couldn't match expected type Bool with
  actual type Int ... insAtOrAboveThr :: Int`. The predicate
  `boolean insAtOrAboveThr = sensors.goreq(ins, Constants.THR);` has a *bare
  method call* as RHS; the ETL inlines only comparison-shaped initializers
  (comment in `buildGuardLeafExpr`'s vicinity: "comparisons, boolean combos,
  arithmetic — NOT bare method calls"), so the name leaked into the model as
  `var insAtOrAboveThr : nat` in the Sensors interface, referenced as a bare
  (Int-typed) condition. `thr` was consequently absent from the Constants
  interface.
- **dafny_verify FAILED** — same root cause, different symptom: generated
  `goreq` with arity 1 `(p0: real)` but call sites `goreq(ins, thr)` with 2
  args (4 errors).
- **isabelle_verify FAILED** — `*** Timeout` (600 s) in
  `GasAnalysisController_Beh` at `GasAnalysisController_deadlock_free`. GA is
  the theory-generated (primary) controller. The `Analysis` state's two
  autonomous guards split on the two-valued **domain enum** `Status`
  (`sts == noGas` / `sts == gasD`). The fixed closer
  `by (metis St.exhaust_disc)` only has the *mode*-enum exhaustiveness fact;
  the residual `sts = noGas ∨ sts = gasD` needs `Status.exhaust_disc`, which
  metis is never given, so it searches until the per-goal timeout.

Everything else passed; the extracted `.rct` was otherwise faithful (clock
`tStuck` with `# tStuck` resets and `since(tStuck) < stuckPeriod` rewriting,
entry-action lifting for Avoiding/Found/Going/GettingOut, Shared interface
with turn/stop/resume, unified System module with cref→cref connections).

### Iter 2 — three minimal fixes → converged

1. Added `@RoboChartType("real")` to the three double constants (preflight).
2. Replaced the predicate RHS with an inlineable comparison:
   `boolean insAtOrAboveThr = ins >= Constants.THR;` — fixes both the FDR4
   Bool/Int error and the Dafny arity errors (the `goreq` call no longer
   reaches the model; `thr` now appears in Constants). `VehicleSensors.goreq`
   remains in Java as the CD-Fn4 implementation but is no longer called from
   the controller (see caveats).
3. Recast the Analysis total cover propositionally: `if (stsIsGasD) → 
   GasDetected; else if (!stsIsGasD) → NoGas + resume`. The guards become
   `sts == gasD` / `not (sts == gasD)` — a `b ∨ ¬b` residual that
   `metis St.exhaust_disc` discharges instantly. (GasDetected's cover was
   already propositional: `ins >= thr` / `not (ins >= thr)`.)

All 12 phases passed; FDR4 checked 9/9 assertions in 2.4 s (small state space
is expected with all type ranges {0..1} and all constants = 1); Isabelle
proved 10 lemmas in 26 s; vacuity clean. Converged.

## Caveats (run-specific compromises)

- **No Final states** (CD-GA-Beh6, CD-MV-Beh9): final state j1 replaced by a
  live `Done` mode (GA, absorbs further `gas`) and a `Found` self-loop on
  `stop` (MV). The terminating events `stop`/`flag` are still emitted.
  Driven by CLAUDE.md's documented Isabelle constraint (a Final on the
  theory controller leaves `deadlock_free` unprovable/hanging).
- **`goreq` not in the formal guard** (CD-Fn4): the threshold check is the
  semantically-equal inline `ins >= thr`; `goreq` exists in Java but is
  uncalled by the controller after iter 2.
- **Odometer is a sensor read, not an event** (CD-Evt3): `odometer ? d0`
  mid-entry reception is not expressible in the single-step Java idiom;
  encoded as `d0 = sensors.odometer()` → `var odometer : real` in Sensors.
- **Waiting's `during randomWalk()`** encoded as top-of-mode statement →
  extracted as state *entry* action (pipeline cannot produce `during`).
- **Invented defaults:** Chem literals {CH4, CO2} with constructor-injected
  target; presence-based `analysis` (any intensity); Intensity ≡ double/real;
  all six constants = 1; 0-based `angle()` map (0=Front, 1=Left, 2=Right,
  else Back).
- **Machine-local `pipeline.yaml` edits, not committed:** `dafny_path` →
  `C:\Users\Will\dafny\dafny\Dafny.exe`; `wsl_isabelle_bin` →
  `/home/will/isabelle/...`. FDR4 `timeout: 3600` / `memory_limit_mb: 78002`
  were already set by the human per §2.
- **Iter-1 pipeline ran twice** (env fault → resume); the snapshot's timings
  are from the second, complete run. The aborted first run added ≈40 s of
  unrecorded pipeline time.
- The Constants interface types `evadeTime/stuckPeriod/outPeriod` as `real`
  despite the Java `int` declarations; benign here (wait() accepts it), not
  chased.

## Cost

**Run-level Claude token total** (source: summed `usage` blocks from the
driving session's transcript JSONL, captured at write-up time — `/cost` is
not exposed to the agent as a tool):

- input tokens (uncached): 9,036
- cache-creation input tokens: 1,068,608
- cache-read input tokens: 25,584,106
- **output tokens: 477,052**
- (≈26.66 M total input-side tokens including cache traffic)

Per-iter token costs are `null`/estimated in the iter summaries, per the
runbook — a me-as-developer session cannot split its own cumulative usage.

**Run-level end-to-end execution time** (source: transcript timestamp span,
first→last message at capture time):

- **end-to-end:** ≈2,250 s (~37.5 min, measured just before the archive commit; includes this write-up)
- **pipeline:** 710.4 s productive (644.4 iter-1 complete run + 66.0 iter-2;
  excludes the ≈40 s aborted env-fault run)
- **agent = end-to-end − pipeline:** ≈1,540 s (codegen + diagnosis +
  write-up + idle; not pure codegen)

The consolidated `execution_times.md` row and the KB append must be done at
copy-back in the main checkout (both files are scrubbed from this worktree —
RUN_TRAJECTORY §6.3).

## Reproducibility

1. Check out the base (or any commit with the current pipeline), set
   `pipeline.yaml` `agent.active_case_study: chemical_detector`, fix the
   per-machine tool paths, confirm
   `forge.dashboard/corrections/type_ranges.json` is all {0..1}.
2. Stage iter-N: `rm -rf java.generated.project/src/main/java/chemdetector &&
   cp -r experiments/convergence/chemical_detector/run-4/iter-N/java
   java.generated.project/src/main/java/chemdetector` and copy
   `iter-N/traces/result_codegen.json` to
   `java.generated.project/result_codegen.json`.
3. `python experiments/scripts/run_experiment_iteration.py` — iter-2 source
   reproduces the converged 12-phase pass.
