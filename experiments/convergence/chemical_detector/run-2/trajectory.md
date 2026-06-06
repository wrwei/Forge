# chemical_detector — run-2 convergence trajectory

- **Actor:** me-as-developer (the Claude Code session edited all Java directly; no sub-agents)
- **Condition:** B (honest independent — playbook absent; `docs/`, prior runs, KB all scrubbed from the worktree)
- **Converged:** YES, at **iter-2** (cap 7)
- **Date:** 2026-06-05
- **Base:** single severed commit `7960844` "isolated run base (history severed; answer material excluded from tree)"

## Headline

| Iter | Change | compile | coverage | preflight | t2m | m2m | m2t | dafny_gen | isabelle_gen | fdr4 | dafny_verify | isabelle_verify | vacuity | pipeline wall-clock | converged |
|------|--------|---------|----------|-----------|-----|-----|-----|-----------|--------------|------|--------------|-----------------|---------|--------------------|-----------|
| 1 | Cold codegen: 18 files, two controllers, no Final states, total guard covers | ✅ | ❌ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ✅ | ✅ | 58.6 s | no |
| 2 | Predicate `insAtOrAboveThr` → direct `ins >= THR` comparison; +2 trace rows (Actuator.java, Clock.java) | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ (9/9) | ✅ | ✅ | ✅ (0 findings) | 63.5 s | **yes** |

## Iter-by-iter narrative

### Iter 1 — cold codegen

From only the canonical inputs (system_description.txt, requirement_all.json,
CLAUDE.md, the three prompt files, the two runbooks), generated 18 Java files
under `chemdetector/`:

- **Two controllers** (CD-ARCH2): `GasAnalysisController` (modes Reading,
  Analysis, NoGas, GasDetected, **Done**) and `MovementController` (modes
  Waiting, Going, **Found**, Avoiding, TryingAgain, AvoidingAgain, GettingOut),
  both single-method mode-nested if-else with named predicates and payload
  capture as first action (`gs = g.value()` etc.) for typed-trigger rebinding.
- **No Final states** (applied CLAUDE.md's Isabelle constraint at codegen
  time): the spec's `GasDetected → j1` became `GasDetected → Done` (still
  sends `stop`), with `Done` self-looping on `gas`; `Found → j1` became a
  `Found` self-loop on `stop` (flag still emitted on entry). Both self-loops
  are event-triggered bare-precondition operations (pattern (b)).
- **Total guard covers** on autonomous-only modes: Analysis splits on
  `stsIsGasD`/`!stsIsGasD`, GasDetected on `insAtOrAboveThr`/`!insAtOrAboveThr`,
  AvoidingAgain on `makingProgress`/`!makingProgress` (after stop/resume
  event branches).
- Clock per the documented pattern: `Clock` class (convention name), field
  `tStuck` reset via `timer.nowMs()`, predicate
  `timer.nowMs() - tStuck < STUCK_PERIOD` → extracted as
  `since(tStuck) < stuckPeriod` with `# tStuck` resets.
- Shared events stop/turn/resume emitted via `actuator.apply(new OutputEvent.X(...))`
  on the GA side and consumed as `InputEvent.X` instanceof triggers on the MV
  side — the ETL unified them into the `Shared` interface by simple name and
  wired `ctrl_ref0 → ctrl_ref1` connections in one `GasAnalysisController_System_Module`.

**First pipeline run was discarded (environment, not verdict):**
`pipeline.yaml` carried another machine's tool paths
(`C:\Users\willr\...Dafny.exe`, `/home/willr/...isabelle`); dafny_verify and
isabelle_verify failed on not-found/exit-127. Fixed both paths (local tuning,
uncommitted) and re-ran the unchanged iter-1 code; the snapshot reflects the
clean re-run.

**Authentic iter-1 verdicts — 9/12 passed.** Three failures, two root causes:

1. **fdr4 + dafny_verify (one defect, two faces):** the named predicate
   `boolean insAtOrAboveThr = analyzer.goreq(ins, ChemConstants.THR);` — a
   *multi-arg sensor-function call* as a predicate RHS — was not inlined the
   way comparison predicates were. The ETL emitted it as a Sensors-interface
   variable `var insAtOrAboveThr : nat`, so (a) FDR4 died on a CSP type error
   (`Couldn't match expected type Bool with actual type Int` at the
   `condition insAtOrAboveThr` guard), (b) `thr` never reached the Constants
   interface, and (c) the Dafny generator declared `goreq` with arity 1
   (`(p0: real)`) while the call site passed 2 args. Diagnosed by reading the
   generated `robochart_controller.rct` — the misplaced Sensors var was
   directly visible. (Predicates whose RHS is a direct comparison were all
   extracted perfectly: `sts == gasD` inlined as `condition sts == gasD` /
   `not(...)`, and the clock predicate became `since(tStuck) < stuckPeriod`.)
2. **coverage:** `Actuator.java` and `Clock.java` had no rows in
   `result_codegen.json`, so their public elements (apply, lastEvent, nowMs,
   advance) were flagged `over_implementation` — the per-file basename rule
   needs at least one trace row per file.

Notably, **isabelle_verify passed on iter-1** (10 lemmas + deadlock-freedom
for `GasAnalysisController_Beh`, ~25 s): the mis-extracted predicate became an
uninterpreted `consts insAtOrAboveThr :: "unit ⇒ bool"` whose complementary
guards (`b()`/`¬b()`) were still provably exhaustive. Vacuity also passed.

### Iter 2 — two minimal fixes → converged

1. `GasAnalysisController.step()`: predicate changed to the direct comparison
   `boolean insAtOrAboveThr = ins >= ChemConstants.THR;` (semantically goreq's
   definition). The model now has `condition ins >= thr` / `not(ins >= thr)`
   inline, `const thr : real = 1` in Constants, no stray Sensors var, and no
   goreq in the Dafny output. `GasAnalyzer.goreq` remains as the CD-Fn4
   implementation, just not referenced from a guard.
2. `result_codegen.json`: added rows CD-ARCH2 → `Actuator` and
   CD-MV-Clock1 → `Clock`.

Result: **all 12 phases passed** — FDR4 9/9 assertions (deadlock- and
divergence-freedom; determinism stripped by design) in 2.4 s, Dafny verified,
Isabelle 10 lemmas + deadlock-freedom in ~25 s, vacuity 0 findings.

## Caveats (run-specific spec compromises / tooling workarounds)

- **No Final states** (deviation from CD-GA-Beh6 / CD-MV-Beh9's `→ j1`):
  terminal-live `Done` (self-loop on `gas`) and `Found` (self-loop on `stop`),
  required by the Isabelle theory generator's weak store invariant. The
  terminating events (`stop`, `flag`) are still emitted.
- **Odometer is a zero-arg Vehicle sensor method**, not an event (deviation
  from CD-Evt3): the spec's entry-action reception `odometer ? d0` cannot be
  expressed in the single-method step() pattern; `d0 = vehicle.odometer()`
  maps to a Sensors-interface var read.
- **Threshold guard is `ins >= thr`, not `goreq(ins, thr)`** (deviation from
  CD-GA-FR4/Beh6/Beh7's letter, identical semantics) — forced by the ETL's
  handling of multi-arg sensor calls in predicates.
- **Chem/Intensity opaque types** rendered as `int`(nat)/`double`(real);
  invented `TARGET_CHEM = 1` ("reading indicates the target chemical" ⇔ some
  entry's `c == TARGET_CHEM`); invented `angle()` mapping 1→Front, 2→Left,
  3→Right, else Back; empty-reading defaults intensity=0.0, location=Front.
- **All constants = 1 / 1.0** to fit the `{0..1}` FDR4 type ranges.
- **Waiting's `randomWalk()` during-action** expressed as a top-of-mode
  statement (becomes an entry action in the extracted model).
- **Tooling:** `pipeline.yaml` dafny_path/wsl_isabelle_bin corrected for this
  machine (willr→Will/will); FDR4 timeout=3600 and memory_limit_mb=78002 were
  pre-set by the human. None of these committed.
- The discarded env-broken pipeline run (~35 s) is not counted in any iter's
  pipeline wall-clock.

## Run-level cost

**Claude token total** (source: summed `usage` blocks from the driving
session's transcript JSONL `5ad94cfe…cdf6c.jsonl`, captured at archive time —
excludes the final write-up/commit turns; `/cost` is not exposed to the agent):

- output tokens: **237,898**
- non-cached input tokens: **9,414**
- cache-read input tokens: 19,385,092; cache-write input tokens: 870,386
  (input-side total ≈ 20.26 M, overwhelmingly prompt-cache reads)

**End-to-end execution time** (source: first→last message timestamp span of
the same transcript, captured at archive time):

- **end-to-end:** ≈ 1,206 s (~20 min) up to the archive step
- **pipeline:** 122.1 s = iter-1 58.6 s + iter-2 63.5 s (productive runs only;
  the discarded env-broken run adds ~35 s if counted)
- **agent (end-to-end − pipeline):** ≈ 1,084 s — codegen + diagnosis +
  write-up + idle, not pure codegen

(The consolidated cross-run table `experiments/convergence/execution_times.md`
was scrubbed from this worktree; add this run's row during copy-back.)

## Findings (durable, generalisable)

**F1 — Never wrap a multi-arg sensor-function call in a named predicate.**
The ETL inlines predicates whose RHS is a direct comparison (`sts == gasD`,
`ins >= thr`, `clock.nowMs() - t < C`), but a predicate whose RHS is a
sensor-method call with arguments (`boolean p = sensor.goreq(ins, thr)`)
becomes an untyped Sensors-interface variable (`var p : nat`). One such
predicate produced three downstream symptoms at once in this run: an FDR4
CSP Bool/Int type error on the guard, the referenced constant silently
missing from the Constants interface, and a wrong-arity function declaration
in the generated Dafny (declared `(p0: real)`, called with 2 args). *How to
apply:* express threshold/ordering guards as comparison operators in the
predicate; keep spec-named comparison functions (goreq-style) as sensor-layer
implementation detail. Zero-arg sensor calls in actions are fine (they become
Sensors vars by design — that's the odometer pattern).

**F2 — Diagnose multi-phase failures from the .rct before fixing per phase.**
fdr4 and dafny_verify failed with unrelated-looking messages (CSP type
mismatch vs Dafny arity error) that shared one root cause. The generated
`robochart_controller.rct` is the cheapest cross-check: scan the emitted
interfaces (Sensors/Constants/Ctrl_State) for variables that should have been
guards or constants that are missing. One .rct read collapsed two phase
failures into one one-line Java fix.

**F3 — Preflight the machine-specific tool paths before iter-1.**
`pipeline.yaml` carried another machine's user directories for Dafny
(win32 path) and Isabelle (wsl_isabelle_bin); both verify phases failed on
not-found in the first pipeline run. These are environment failures, not
verdicts: fix the paths, re-run the *unchanged* code, and snapshot only the
clean run. A 10-second check (`Test-Path` the dafny_path; `wsl -- ls` the
isabelle bin) before the first run would have saved a full pipeline cycle.

**F4 — Front-loading CLAUDE.md's verification-driven structure converges fast.**
Designing iter-1 for the verifiers — no Final states (terminal-live modes
with event-triggered bare-precondition self-loops), total guard covers
(`b`/`!b` complementary pairs) on autonomous-only modes, payload capture as
first action on every typed trigger — made isabelle_verify and the FDR4
deadlock/divergence checks pass on the first real attempt with zero
verifier-driven control-flow iterations. The entire trajectory cost was one
encoding fix (F1) plus trace bookkeeping. Corollary: the feared state-space
blowup of the two-controller composition never materialized under `{0..1}`
ranges with all constants = 1 (FDR4: 9 assertions, 2.4 s).

**F5 — Every source file needs at least one result_codegen.json row.**
Coverage's over-implementation check is per-file: any public element in a
file with no trace rows is flagged, while elements in files that have ≥1 row
are implicitly covered. Supporting classes (Actuator, Clock) are easy to
forget because no single requirement names them — map them to the
architecture/variable requirement they serve (CD-ARCH2, CD-MV-Clock1) at
cold-codegen time.

## Independence

- **Iter-1 inputs (complete list):**
  `forge.assets/case-studies/chemical_detector/system/system_description.txt`,
  `forge.assets/case-studies/chemical_detector/requirements/requirement_all.json`,
  `CLAUDE.md`, `forge.assets/prompts/java_codegen_rules.txt`,
  `forge.assets/prompts/chain_of_thought_codegen.txt`,
  `forge.assets/prompts/few_shot_codegen.txt`,
  `experiments/HOWTO_RUN_CONVERGENCE_EXPERIMENT.md`,
  `experiments/RUN_TRAJECTORY.md`, plus pipeline *tool* sources
  (`forge.dashboard/web/feedback/coverage.py` for the result_codegen.json
  schema; `pipeline.yaml`; `forge.dashboard/corrections/type_ranges.json`).
- **Iter-2 inputs:** the live workspace source + iter-1's `post_*` feedback +
  the generated artifacts of this run (`robochart_controller.rct`,
  `GasAnalysisController_Beh.thy`).
- **No §4 path was opened**; nothing under `docs/` was read; no prior-run
  snapshot, reference run, KB, or playbook existed in (or was read from)
  this worktree.
- **Injected-context audit:** clean `CLAUDE_CONFIG_DIR` (empty memory
  namespace, no `.remember` hook); git history is one severed commit with no
  run/iter/archive terms. Two minor incidental exposures, disclosed: (a) the
  worktree directory name `fmgvc-cd-run5-redo` reveals this is a redo of
  run-2 (no trajectory content); (b) `coverage.py`'s docstring — read for the
  trace schema — contains the example "GasAnalysisMode's four enum values
  trace to CD-GA-FR1..4", confirming a mode-enum naming convention that was
  in any case derivable from the spec. Neither names any prior run's fixes,
  iteration counts, or failure modes.

## Reproducibility

1. Stage the source: copy `run-2/iter-N/java/` →
   `java.generated.project/src/main/java/chemdetector/` (wipe the dir first)
   and `run-2/iter-N/traces/result_codegen.json` → `java.generated.project/`.
2. Ensure `pipeline.yaml`: `agent.active_case_study: chemical_detector`,
   machine-correct `dafny_path` / `wsl_isabelle_bin`, fdr4 `timeout: 3600`.
3. Run `python experiments/scripts/run_experiment_iteration.py`; per-phase
   verdicts land in `forge.assets/corrections/post_<phase>.json`.
4. Iter-2 should report all 12 phases passed and vacuity 0 findings.
