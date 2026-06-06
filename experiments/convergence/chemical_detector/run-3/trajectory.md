# Convergence Trajectory — chemical_detector, run-3

- **Actor:** me-as-developer (Claude Code session edits Java directly; no sub-agents)
- **Condition:** B (honest independent: playbook absent, scrubbed worktree)
- **Converged:** YES, at iter 3 (cap 7)
- **Date:** 2026-06-05
- **Base:** parentless severed commit `03677c4` ("isolated run base"), worktree
  `fmgvc-cd-run6-redo`, clean `CLAUDE_CONFIG_DIR` (`C:\tmp\claude-clean-cd-run6`)

## Independence

Iter-1 cold codegen read ONLY the canonical inputs: the case study's
`system_description.txt` + `requirement_all.json`, `CLAUDE.md`,
`java_codegen_rules.txt`, `chain_of_thought_codegen.txt`, `few_shot_codegen.txt`,
`HOWTO_RUN_CONVERGENCE_EXPERIMENT.md`, `RUN_TRAJECTORY.md`. No `docs/` path was
opened; no prior-run artefact, reference run, cold-baseline, playbook, or KB was
read. Pipeline *sources* consulted during diagnosis (allowed, "the tool"):
`forge.dashboard/web/feedback/coverage.py` (result_codegen.json schema),
`thy_generation_rule.egl` (SeqGs emission rule), plus the run's own generated
artefacts (`robochart_controller.rct`, `GasAnalysisController.dfy`,
`GasAnalysisController_Beh.thy`).

**Context-exposure disclosure:** no `.remember` hook output and no agent-memory
content was injected (fresh config dir). The harness's SessionStart git-status
snapshot listed *deleted file paths* from the scrub, exposing that a
chemical_detector cold-baseline run-1 previously existed and a handful of its
Java file names (`GasAnalysisOutput.java`, `Vehicle.java`,
`DetectorConstants.java`, annotation file names). No fix content, verdicts, or
iteration counts were visible. Design decisions here were derived from the
spec; the name overlaps (Vehicle, DetectorConstants) follow naturally from the
requirements text. One additional incidental exposure: a docstring example in
`coverage.py` (a pipeline source) mentions "GasAnalysisMode's four enum values
trace to CD-GA-FR1..4" — read for the JSON schema, after this run's enum was
already designed with five values.

## Headline

| iter | change | compile | coverage | preflight | t2m | m2m | m2t | dafny_gen | isabelle_gen | fdr4 | dafny_verify | isabelle_verify | vacuity | pipeline wall-clock |
|------|--------|---------|----------|-----------|-----|-----|-----|-----------|--------------|------|--------------|-----------------|---------|---------------------|
| 1 | cold codegen (16 files, 535 LOC, 2 controllers, no Final states) | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✗ CSP Bool/Int type error | ✗ goreq arity | ✗ deadlock_free timeout | ✓ | 637.2 s |
| 2 | goreq(ins,thr) guard → direct `ins >= THR`; gas-capturing self-loops on Analysis/GasDetected/Located | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ 9/9 | ✗ postconditions | ✗ duplicate SeqGs constant | ✓ | 44.7 s |
| 3 | gas self-loops → payload-less Tick self-loops placed AFTER autonomous guards; +InputEvent.Tick | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ 9/9 | ✓ 11/11 | ✓ 12 lemmas | ✓ 0 findings | 63.2 s |

## Iter-by-iter narrative

### Iter 1 — cold codegen

Two controllers per CD-ARCH2: `GasAnalysisController` (modes Reading, Analysis,
NoGas, GasDetected, Located) and `MovementController` (Waiting, Going, Avoiding,
TryingAgain, AvoidingAgain, GettingOut, Found). Design decisions taken from
CLAUDE.md's inlined guidance: **no `Final` mode in either controller** (the
spec's final states j1 became live terminal modes Located/Found with
event-triggered self-loops, terminating events stop/flag still emitted);
**total guard cover** on autonomous-only modes (Analysis splits on the
two-valued Status enum; GasDetected on `goreq`/`!goreq`; AvoidingAgain on
`p∨q` / `¬p∧¬q`); clock via a `Clock`-named class with field `timer` and
clock-field `stuckClock` in the rewrite-friendly form
`timer.nowMs() - stuckClock < STUCK_PERIOD`; shared turn/stop/resume as
same-named records in InputEvent (MV triggers) and OutputEvent (GA actions);
all constants 1 to fit the `{0..1}` FDR type ranges.

First pipeline run hit two machine-config failures (Dafny exe and WSL Isabelle
configured for user `willr`, actual `will`); fixed `pipeline.yaml` locally and
re-ran the same iter — not snapshotted as a separate iter (tool bridge, not a
verdict; per HOWTO §0).

Genuine verdicts: 8 phases green, three verifier failures:
- **fdr4**: `Couldn't match expected type Bool with actual type Int — insAboveThr :: Int`.
  The predicate `insAboveThr = sensor.goreq(ins, THR)` (a 2-arg sensor call)
  could not be inlined by the M2M; it was lifted into the Sensors interface as
  `var insAboveThr : nat` and then used as a transition condition.
- **dafny_verify**: `wrong number of arguments (got 2, but function 'goreq'
  expects 1: (p0: real))` ×4 — same root cause, the generator emitted a
  1-param signature for goreq against 2-arg call sites.
- **isabelle_verify**: `deadlock_free` tactic timeout (611 s) in
  `GasAnalysisController_Beh`. Analysis and GasDetected have *guarded-only*
  exits; the fixed closer `by (metis St.exhaust_disc)` cannot discharge the
  residual `st = X ∧ <guard>` disjuncts (it knows St-exhaustion only — not
  Status-exhaustion, not arithmetic). Total guard cover does NOT satisfy this
  prover even though it satisfies the FDR side.

The .rct also revealed a latent defect: the Located self-loop on `gas` without
payload capture was bound to the default `var v : real` (`trigger gas ? v`)
— a type mismatch against `Seq(GasSensor)` hidden behind the first FDR error.

### Iter 2 — kill the goreq call; add bare-precondition self-loops

- `insAboveThr` became the direct comparison `ins >= DetectorConstants.THR`
  (this *is* goreq's definition per CD-Fn4; the goreq method remains on
  GasSensorArray as the CD-Fn4 implementation). → fdr4 **passed 9/9**
  (deadlock + divergence freedom for all three modules) and the goreq arity
  errors vanished.
- Gas-triggered self-loops (with payload capture, re-firing the entry
  computation) added to Analysis and GasDetected; Located's self-loop made
  capture-correct. → two NEW failures:
  - **dafny_verify**: `a postcondition could not be proved` on
    transitionFromAnalysis / transitionFromGasDetected. The Dafny generator
    emits *unconditional* `ensures sts == noGas ==> mode == NoGas` for
    autonomous transitions and replicates the Java branch order — the gas
    branch placed before the guards is a return path that violates them.
  - **isabelle_verify**: `Duplicate constant declaration
    "GasAnalysisController_Beh.SeqGs"` — the theory template emits one
    `definition Seq<Var> = UNIV` per gas-payload-capturing transition with no
    dedup; four capturing transitions → duplicates → theory load error.

### Iter 3 — Tick self-loops after the guards

Added payload-less `InputEvent.Tick`. In Analysis and GasDetected the
autonomous guards now come FIRST and a `Tick` self-loop comes LAST (dead code
in Java/Dafny because the guards are exhaustive — so the unconditional ensures
prove; a real concurrent transition in RoboChart — so Isabelle gets its bare
`st = <Mode>` precondition). Located self-loops on Tick (no capture), leaving
exactly ONE gas-capturing transition machine-wide (Reading→Analysis) → exactly
one SeqGs definition. **All 12 phases pass; vacuity 0 findings. Converged.**

## Caveats (run-specific compromises)

1. **`InputEvent.Tick` is not in the requirements.** It exists purely so the
   theory-generated controller's autonomous-only modes (Analysis, GasDetected)
   and terminal mode (Located) have a bare-precondition operation the
   `deadlock_free` closer can use. CD-DC1 (unique source+trigger transitions)
   is still respected; CD-GA-FR3/FR4 gain an unspecified tick self-loop each.
2. **No final states.** The spec's GA j1 / MV j1 are live modes Located/Found
   with tick/stop self-loops (CLAUDE.md's no-`Final` constraint for the
   theory-generated controller; applied to both since discovery order wasn't
   known a priori).
3. **Waiting's `during randomWalk()`** (CD-MV-FR1) is encoded as a top-of-block
   statement, extracted as an entry-style action — `during` has no Java
   encoding in this pipeline.
4. **All constants = 1** (THR, LV, EVADE_TIME, STUCK_PERIOD, STUCK_DIST,
   OUT_PERIOD) to stay within the `{0..1}` CSP type ranges.
5. **Machine-local `pipeline.yaml` edits** (not committed): `dafny_path.win32`
   and `wsl_isabelle_bin` corrected from user `willr` to `Will`/`will`;
   fdr4 `timeout: 3600` / `memory_limit_mb: 78002` were already set.
6. Iter-1's pipeline was executed twice (tool-path config failures on the
   first attempt); the snapshot records the second, genuine-verdict run.

## Findings (durable, generalisable)

**F1 — Never call a multi-arg sensor/helper function inside a named guard
predicate.** The M2M cannot inline a multi-arg call in a guard: it lifts the
predicate into the Sensors interface as a *nat*-typed variable (FDR4
`Bool`/`Int` type error) and the Dafny generator emits a wrong-arity function
signature for the callee (it keeps only the first parameter). Diagnostic
signature: FDR "Couldn't match expected type Bool with actual type Int —
<predName> :: Int" + Dafny "wrong number of arguments". Apply: express the
guard as a direct comparison/boolean expression over state vars, constants and
*zero-arg* sensor calls; keep the helper method on the sensor class for
requirement traceability if needed.

**F2 — At most ONE payload-capturing transition per typed event per machine.**
The Isabelle theory template emits `definition Seq<Var> ... = UNIV` once per
transition whose trigger captures a Seq-typed payload, with no deduplication —
two captures of `gas ? gs` produce a duplicate-constant theory load error.
Apply: capture the payload on exactly one transition (the spec's
reading-consumption transition); any additional event-triggered branches a mode
needs must use a payload-less event.

**F3 — Never leave a typed-event trigger uncaptured.** A self-loop on a typed
event without capturing the payload binds the trigger to the machine's default
`var v : real` (`trigger gas ? v`), a latent CSP type error against non-real
payloads. Apply: either capture into the matching state var (subject to F2) or
trigger on a payload-less event.

**F4 — Total guard cover does NOT satisfy the Isabelle `deadlock_free`
closer.** CLAUDE.md recommends total guard cover for autonomous-only modes (it
does give FDR-side deadlock/divergence freedom — fdr4 passed 9/9 with it), but
`by (metis St.exhaust_disc)` cannot use Status-enum exhaustiveness or
arithmetic (`g ∨ ¬g`) to discharge the residual `st = X ∧ <guard>` disjuncts —
the symptom is a tactic *timeout*, not a fast failure. Every mode of the
theory-generated controller needs a literally-bare operation: an
event-triggered branch with no extra guard, or an unconditional autonomous
transition. A payload-less Tick self-loop is the least-invasive form.

**F5 — In a mode mixing autonomous guarded exits with an added event-triggered
branch, put the event branch AFTER the guards.** The Dafny generator emits
*unconditional* `ensures <guard> ==> mode' = <target>` for autonomous
transitions and replicates the Java branch order in the method body: an event
branch ahead of the guards is a reachable return path that violates the
ensures; placed after exhaustive guards it is dead in Dafny (postconditions
prove) yet still a real transition in RoboChart/Isabelle (branch order is
immaterial there — transitions are concurrent choices). This composes with F4:
"guards first, Tick self-loop last" satisfies Dafny, FDR and Isabelle
simultaneously.

**F6 — Tool-bridge failures are distinguishable from verdicts by speed and
message shape.** Dafny exe missing and WSL-Isabelle path wrong both fail in
seconds with a path string in the raw output (`No such file or directory`,
exit 127/`not found`), vs. 600+ s for a genuine proof timeout. Fix
`pipeline.yaml` (`dafny_path.win32`, `wsl_isabelle_bin` — beware per-machine
*usernames* in both) and re-run the same iter without snapshotting the broken
run.

**F7 — The Isabelle theory is generated for the diagram-named (first)
controller, but design both controllers to its constraints anyway.** Here the
theory covered GasAnalysisController only (12 lemmas; Dafny likewise emitted
only GasAnalysisController.dfy), yet discovery order is not knowable at
codegen time — giving every controller bare-precondition coverage and no
`Final` state costs little and removes the gamble.

## Cost

- **Run-level Claude token total** (source: summed `usage` blocks from the
  driving session's transcript JSONL, `e63e1205…f.jsonl`, measured at write-up
  time; `/cost` is not exposed to the agent): **input 7,078 + cache-read
  24,541,014 + cache-write 587,531 ≈ 25.14 M input-side tokens; output
  320,234 tokens.** Whole-run figure; excludes only the final
  trajectory/commit turns after measurement.
- **Per-iter tokens:** `null` in each `summary.json` (kind: estimated) — not
  separately measurable mid-session; this is the honest value.

## Execution time

- **End-to-end (session span, first→last transcript timestamp at measurement):
  1,987 s (~33 min)** — excludes the final write-up/commit turns after
  measurement.
- **Pipeline (Σ `pipeline_wall_clock_s` over snapshotted iters):
  637.2 + 44.7 + 63.2 = 745.0 s** (dominated by iter-1's 611 s Isabelle
  deadlock_free timeout). The discarded first iter-1 attempt (broken tool
  paths, ~40 s) is not in this figure.
- **Agent (end-to-end − pipeline): ≈ 1,242 s** — codegen, feedback diagnosis,
  artefact reading (.rct/.dfy/.thy/EGL), snapshots; write-up time extends this
  after the measurement point.
- The consolidated cross-run table `experiments/convergence/execution_times.md`
  was scrubbed from this worktree; add this run's row during copy-back.

## Reproducibility

1. Stage `run-3/iter-3/java/**` into
   `java.generated.project/src/main/java/chemdetector/` (wipe the dir first).
2. Ensure `pipeline.yaml`: `agent.active_case_study: chemical_detector`,
   correct `dafny_path.win32` / `wsl_isabelle_bin` for the machine, fdr4
   `timeout: 3600`.
3. `python experiments/scripts/run_experiment_iteration.py` — expect all 12
   phases green and vacuity 0 findings. Per-iter feedback pairs are preserved
   under `run-3/iter-N/feedback/`; formal artefacts under
   `run-3/iter-N/formal-artefacts/`.

## Knowledgebase append

This run executed in a scrubbed worktree where
`experiments/convergence/CONVERGENCE_FINDINGS_KNOWLEDGEBASE.md` does not exist.
Per RUN_TRAJECTORY §6.3 the findings above (F1–F7) are recorded here only; the
KB append happens during copy-back to the main checkout (append-only, without
reading the KB).
