# Convergence Trajectory — chemical_detector, run-1

- **Actor:** `me-as-developer` (the LLM agent in the user's session edited all
  Java directly with Read/Edit/Write/Bash; no sub-agent dispatch).
- **Condition:** B (honest independent — playbook absent, knowledgebase never read).
- **Converged:** YES, at **iter 2** (cap 7).
- **Java package:** `chemdetector` (19 files, 563 LOC, unchanged file count
  across iters).
- **Date:** 2026-06-05. Severed-history worktree base commit: `eedebcd`
  ("isolated run base (history severed for run independence)").

## Headline table

| Iter | Change | compile | coverage | preflight | t2m | m2m | m2t | dafny_gen | isabelle_gen | fdr4 | dafny_verify | isabelle_verify | vacuity | Pipeline wall-clock | Converged |
|------|--------|---------|----------|-----------|-----|-----|-----|-----------|--------------|------|--------------|-----------------|---------|---------------------|-----------|
| 1 | Cold codegen from canonical inputs | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ CSP type error | ❌ 4 × goreq arity | ✅ (9 lemmas, deadlock-free) | ✅ (0 findings) | 54.91 s | no |
| 2 | One line: guard predicate `sensor.goreq(ins, THR)` → `ins >= DetectorConstants.THR` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ 9/9 assertions | ✅ | ✅ (9 lemmas, deadlock-free) | ✅ (0 findings) | 54.82 s | **yes** |

## Iter-by-iter narrative

### Iter 1 — cold codegen

Generated the full source tree from only the canonical inputs (§ Independence
below): two controllers — `GasAnalysisController` (modes Reading, Analysis,
NoGas, GasDetected) and `MovementController` (modes Waiting, Going, Avoiding,
TryingAgain, AvoidingAgain, GettingOut, Found) — plus `DetectorSensors`
(@SensorService), `Vehicle` actuator, `GasAnalysisOutput` actuator (carries the
inter-controller turn/stop/resume sends), `Clock`, `DetectorConstants`, domain
enums/record, and a 5-record `InputEvent` + 1-record `OutputEvent` hierarchy.

Key design decisions made cold (all from CLAUDE.md's inlined guidance):

- **No `Final` state in either controller** (CLAUDE.md: the theory-generated
  controller must not have one; discovery order was unknown, so neither got
  one). GasAnalysis reroutes GasDetected → Reading after `send stop`;
  Movement's Found is a live terminal mode self-looping on `stop`.
- **Total guard cover via complementary predicate pairs** on every
  autonomous-only mode: `if (p) … else if (!p) …` (never bare `else`), so the
  extracted guards are propositionally complementary. Applied to Analysis
  (`statusNoGas` / `!statusNoGas`), GasDetected (`intensityAtThreshold` /
  `!intensityAtThreshold`) and AvoidingAgain (`makingProgress` /
  `!makingProgress`, where `makingProgress = withinStuckPeriod || escapedDistance`).
- **Clock conventions**: `Clock` class + field `clock`; reset via
  `stuckTimer = clock.nowMs()`; predicate in the exact rewritable shape
  `clock.nowMs() - stuckTimer < DetectorConstants.STUCK_PERIOD` →
  `since(stuckTimer) < stuckPeriod` in the model. `vehicle.pause(N)`
  (@RoboChartWait) for the evade/out waits.
- **Odometer as a zero-arg sensor method** (`odometerDistance()` → Sensors
  var), because the spec consumes `odometer ? d0/d1` inside actions, which the
  one-event-per-step Java encoding cannot express as a trigger.

Result: 10/12 phases passed immediately. Isabelle proved all 9 lemmas
including `GasAnalysisController_deadlock_free` on the first attempt — the
complementary-pair design needed no self-loops and closed under the canned
`metis St.exhaust_disc` tactic. The two failures shared one root cause:

- **fdr4 (parse/type error):** `Couldn't match expected type Bool with actual
  type Int — In the expression: (intensityAtThreshold)`. The predicate
  `boolean intensityAtThreshold = sensor.goreq(ins, DetectorConstants.THR)`
  — a **2-arg sensor-function call in a guard predicate** — was mis-extracted
  as a zero-arg Sensors interface variable `var intensityAtThreshold : nat`
  (see iter-1 `formal-artefacts/csp/../robochart_controller.rct` line 40), so
  the CSP guard was Int-typed. `goreq` itself never appeared in the model's
  function list, and the `thr` constant silently vanished from the Constants
  interface (it was referenced only inside the mangled call).
- **dafny_verify (4 errors):** the Dafny generator declared
  `function goreq(p0: real)` (one parameter) while the transition methods
  called `goreq(ins, thr)` with two arguments — same construct, second
  mis-extraction.

### Iter 2 — one-line fix

Replaced the guard predicate with the directly-supported comparison form:

```java
boolean intensityAtThreshold = ins >= DetectorConstants.THR;
```

`DetectorSensors.goreq` stays in the Java as CD-Fn4's implementation (the
controller no longer calls it). All 12 phases passed: FDR4 9/9 assertions
(deadlock- and divergence-freedom on the unified two-controller
`GasAnalysisController_System_Module`), Dafny verified, Isabelle 9 lemmas +
deadlock-freedom, vacuity 0 findings. Converged.

## Caveats (run-specific compromises)

1. **Final states rerouted.** Spec final j1 (CD-GA-Beh6, CD-MV-Beh9) replaced
   by: GasDetected → Reading after `send stop` (GA may re-emit stop if gas
   stays above threshold — idempotent), and a Found self-loop on `stop`
   (re-fires the flag/halt entry — idempotent).
2. **Odometer event (CD-Evt3) modeled as a Sensors variable**, not an event
   channel (`odometer ? d0` inside entry actions is inexpressible as a Java
   trigger).
3. **Waiting's `during randomWalk()` encoded as a top-of-mode-block statement**
   → extracted as an *entry* action (the pipeline has no during-action route).
4. **`changeDirection` extracted as a typed output event** (`changeDirection : Loc`),
   not an operation call — its internal steering logic (CD-OP4's left→right /
   right→left / front→back mapping) lives only in the Java `Vehicle` and is
   invisible to the formal model. `move` (2-arg) did become an LOperations call.
5. **Invented defaults:** constant values all 1 (within the `{0..1}` CSP
   ranges); `analysis()` classifies "target chemical indicated" as
   `c == Chem.TARGET && i > 0`; `Chem` is a two-literal enum (TARGET, OTHER);
   `angle()` maps 0→Front, 1→Left, 2→Right, ≥3→Back.
6. **Cosmetic:** nat-annotated int constants (evadeTime, stuckPeriod,
   outPeriod) were emitted as `const … : real` in the .rct — did not block.
7. **Determinism not verified** (pipeline strips `:[deterministic]` by design).
8. Local machine tuning (not committed): `pipeline.yaml` dafny win32 path →
   `C:\Users\Will\dafny\dafny\Dafny.exe`, `wsl_isabelle_bin` →
   `/home/will/...`. FDR4 timeout 3600 / memory_limit_mb 78002 were already
   set and match this machine's page file (78001 MB).

## Findings (durable, generalisable)

**F1 — Never call a multi-arg sensor function in a guard predicate; inline
spec-level comparison functions as Java operators.** A 2-arg sensor method
(`goreq(ins, thr)`) used as a named-predicate RHS is mis-extracted *twice*:
the M2M emits the predicate as a zero-arg `Sensors` var defaulted to `nat`
(CSP guard becomes Int-typed → FDR "Couldn't match expected type Bool with
actual type Int"), and the Dafny generator declares the function with one
parameter while emitting 2-arg call sites ("wrong number of arguments").
A tell-tale secondary symptom: any constant referenced *only* inside such a
call silently disappears from the Constants interface. Single-arg
Seq-consuming functions (`analysis(gs)`, `intensity(gs)`, `location(gs)`) and
zero-arg sensor vars extract perfectly. *How to apply:* spec functions whose
job is comparison (goreq-style) should be realised as direct `>=`/`<=`
operators inside the named predicate; keep the Java helper method if a
requirement traces to it, just don't call it from the controller.

**F2 — Complementary predicate pairs (`if (p) … else if (!p)`) give
autonomous-only modes a first-try-provable total guard cover.** Both verifiers
accepted it at the first attempt that reached them: Isabelle's
`deadlock_free` + `metis St.exhaust_disc` closes `P ∨ ¬P` without needing any
enum-exhaustiveness fact, and FDR4 finds no deadlock/divergence. This run
deliberately wrote Analysis's second guard as `!statusNoGas` rather than the
spec's `sts == Status.gasD` — semantically identical for a 2-literal enum, but
propositionally total, which is what the canned closer can discharge. No
self-loops and no bare `else` fallbacks were needed anywhere; the M2M
deadlock-lint advisories ("state without unconditional fallback" on all 10
states) were correctly ignored as the documented too-broad case.

**F3 — In a two-controller study the Isabelle theory went to the diagram
controller (`GasAnalysisController`, the first/diagram-named one), and keeping
BOTH controllers Final-free cost little.** The rerouted terminal transitions
(stop→Reading re-emission loop; Found stop-self-loop) pass FDR4, Dafny and
Isabelle simultaneously. When you cannot predict which controller the theory
generator will pick, make every controller Final-free up front rather than
gambling on discovery order.

**F4 — Named-boolean composition survives extraction, including under the
`since()` rewrite.** `makingProgress = withinStuckPeriod || escapedDistance`
(where `withinStuckPeriod` is a `clock.nowMs() - field < CONST` time
predicate) was inlined into the transition guard as
`since(stuckTimer) < stuckPeriod \/ d1 - d0 > stuckDist`, and its negation
`!makingProgress` as the complementary guard. Composing previously-declared
named booleans with `||`/`!` is safe and keeps the Java readable.

**F5 — (process) The harness git-status snapshot leaks deleted-file *paths*
into a fresh session.** This worktree had the forbidden dirs scrubbed by
uncommitted deletion, so the session-start git status listed
`experiments/cold-baseline/chemical_detector/run-1/...` paths — exposing a
prior run's Java file naming (though no content, counts, or fixes). For full
hygiene, scrub by *committing* the deletions (or base the severed commit on a
tree that never contained the answer material) so the status snapshot is clean.

## Run-level cost

**Claude token total (whole-run, source: summed `usage` blocks from the
driving session's transcript JSONL `742f3111-…ba1.jsonl`; `/cost` is not
exposed to the agent):**

- input tokens (uncached): 5,966
- output tokens (incl. thinking): 365,005
- cache-read input tokens: 17,602,236
- cache-creation input tokens: 533,226
- **total input incl. cache: 18,141,428; grand total in+out: 18,506,433**
- assistant messages with usage: 120

Final session-end figures (updated post-copy-back at the user's request);
they include the trajectory write-up and post-archive Q&A turns. The
archive-time snapshot — i.e. the cost of reaching convergence + archive,
before the Q&A tail — was 324,219 output / 13.5M total input over 97
messages. Per-iter token splits are `null` by design (not measurable
mid-run).

**End-to-end execution time (source: transcript JSONL timestamp span):**

- **end-to-end** = 2,370 s (~40 min) full session span (first ts
  2026-06-05T12:49:27Z → last 13:28:57Z), including write-up and post-archive
  Q&A; the pre-write-up span (cold start → converged + snapshots) was 1,083 s;
- **pipeline** = 109.7 s = Σ pipeline_wall_clock_s (54.91 + 54.82; no hung
  phases — FDR4 finished in ~1.2 s per iter on the {0..1} instantiation);
- **agent** = ~2,260 s (end-to-end − pipeline: codegen, diagnosis, snapshots,
  write-up, post-run Q&A, idle).

A row for `experiments/convergence/execution_times.md` (add during copy-back):
`chemical_detector run-1 | me-as-developer | condition B | 2 iters | end-to-end 2370 s | pipeline 109.7 s | agent ~2260 s`.

## Independence

- **Condition B.** `docs/` never opened (no playbook); the findings
  knowledgebase never read (it is scrubbed from this worktree; per
  RUN_TRAJECTORY §6 the KB append happens at copy-back in the main checkout —
  not recreated here). No prior `run-*`/`iter-*`/cold-baseline/reference-runs
  artefacts read (all scrubbed). No sub-agents dispatched.
- **Iter-1 inputs (complete list):** `CLAUDE.md`,
  `forge.assets/case-studies/chemical_detector/system/system_description.txt`,
  `forge.assets/case-studies/chemical_detector/requirements/requirement_all.json`,
  `forge.assets/prompts/java_codegen_rules.txt`,
  `forge.assets/prompts/chain_of_thought_codegen.txt`,
  `forge.assets/prompts/few_shot_codegen.txt`,
  `experiments/HOWTO_RUN_CONVERGENCE_EXPERIMENT.md`,
  `experiments/RUN_TRAJECTORY.md`. Tool-mechanics reads (the pipeline, not
  answer material): `pipeline.yaml`,
  `forge.dashboard/corrections/type_ranges.json`, and
  `forge.dashboard/web/feedback/coverage.py` (to learn the
  `result_codegen.json` schema). Iter-2 read only the live workspace source +
  iter-1's `post_*` feedback + generated artefacts (`robochart_controller.rct`).
- **Disclosed context exposure (also stated in the session's first response):**
  the harness-injected git-status snapshot listed *deleted* file paths from
  the scrubbed checkout, including
  `experiments/cold-baseline/chemical_detector/run-1/...` Java file names
  (e.g. `actuator/GasAnalysisOutput.java`, `annotation/SensorService.java`,
  `constants/DetectorConstants.java`) and which `post_*` files existed. This
  exposed a prior run's file *naming/structure* — no fix content, failure
  narratives, or iteration counts. Where this run's naming coincides
  (GasAnalysisOutput, DetectorConstants, the annotation trio), each choice is
  independently derivable from CLAUDE.md's conventions table and the
  requirements; nonetheless, judge accordingly (label:
  condition-B-with-structural-path-exposure). No agent-memory content and no
  `.remember` hook output was injected (empty memory namespace; single
  severed-history commit verified — `git log` shows only `eedebcd`).
- **Post-hoc exposure (no influence on the run):** the severed base commit's
  tree already contained a *previous* run-1 attempt under this archive slot
  (this checkout is a run-1 redo; the old files were scrub-deleted in the
  working tree and never read). Their names (e.g. `GasSensorArray.java`,
  `OdometerSensor.java`, `CdConstants.java`, `GaMode.java`, `MvMode.java`,
  `Actuator.java`) surfaced only in the archive commit's rename/delete output,
  *after* convergence and after this write-up was drafted. The slot now
  contains exactly this run's artefacts.

## Reproducibility

1. Check out the severed base (or any tree with the same pipeline), set
   `pipeline.yaml` `agent.active_case_study: chemical_detector`, fix the
   machine-local `dafny_path`/`wsl_isabelle_bin`, keep FDR4
   `timeout: 3600` and `memory_limit_mb` ≈ page-file MB.
2. Stage iter-N: copy `run-1/iter-<N>/java/` →
   `java.generated.project/src/main/java/chemdetector/` and
   `run-1/iter-<N>/traces/result_codegen.json` →
   `java.generated.project/result_codegen.json`.
3. `python experiments/scripts/run_experiment_iteration.py` — iter-1 staging
   reproduces the fdr4 + dafny_verify failures; iter-2 staging converges
   (FDR4 9/9, Isabelle 9 lemmas, vacuity 0).
