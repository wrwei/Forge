# LRE Convergence Trajectory — run-3

- **Study:** lre (package `lre`)
- **Actor:** me-as-developer (Claude Code session edits Java directly; no sub-agents)
- **Condition:** B (honest independent — playbook absent)
- **Date:** 2026-06-05
- **Outcome:** **converged at iter 3** (all 12 phases pass, vacuity audit 0 findings)

## Independence

Iter-1 cold codegen read ONLY the canonical inputs: `forge.assets/case-studies/lre/system/system_description.txt`,
`forge.assets/case-studies/lre/requirements/requirement_all.json`, `CLAUDE.md`,
`forge.assets/prompts/java_codegen_rules.txt`, `chain_of_thought_codegen.txt`, `few_shot_codegen.txt`,
plus the process docs (`experiments/RUN_TRAJECTORY.md`, `experiments/HOWTO_RUN_CONVERGENCE_EXPERIMENT.md`).
The session ran in a severed-history worktree (single parentless base commit) with a clean
`CLAUDE_CONFIG_DIR`; no agent memory, no `.remember` hook output, no prior-run content was injected.
Nothing under `docs/` was opened. During iters ≥ 2, reads were limited to the live workspace source,
the immediately-prior `post_*` feedback, and **pipeline implementation sources** consulted as the tool
(`forge.dashboard/web/feedback/coverage.py` for the `result_codegen.json` schema;
`forge.transformations/.../thy_generation_rule.egl` and `java2dafny.egl` to diagnose generator defects;
the generated artefacts under `forge.transformations/output/`).

## Headline

| iter | change | compile | coverage | preflight | t2m | m2m | m2t | dafny_gen | isabelle_gen | fdr4 | dafny_verify | isabelle_verify | vacuity | pipeline wall-clock | converged |
|------|--------|---------|----------|-----------|-----|-----|-----|-----------|--------------|------|--------------|-----------------|---------|--------------------:|-----------|
| 1 | cold codegen (15 files, 633 LOC) | ✓ | ✓ | ✗ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✗ | ✗ | ✓ | 669.8 s¹ | no |
| 2 | Java: +15 `@RoboChartType("real")`; MOM/HCM autonomous-first + negation chaining. Tool: thy closer heuristic → `hasSeqPayloadDomain` | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✗ | ✓ | ✓ | 139.5 s | no |
| 3 | Tool only: `java2dafny.egl` ensures premise wrapped in `old(...)`; no Java change | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | 67.1 s | **yes** |

¹ iter-1's `isabelle_verify` is 616.8 s of that — a 600 s per-goal proof timeout (the broken closer hanging), not productive verification.

FDR4 passed all three iters (3 assertions checked after the `:[deterministic]` strip; deadlock- and divergence-freedom hold from iter 1).

## Iter-by-iter narrative

### Iter 1 — cold codegen
Generated the full tree from the requirements: `annotation/RoboChartType`, `mode/LreMode` (OCM/MOM/HCM/CAM),
`constants/LreConstants` (4 thresholds, requirement-verbatim camelCase names), `event/InputEvent` +
`event/OutputEvent` (sealed interfaces; record names **verbatim** RoboChart event names — `reqVel`, `reqHdng`,
`reqOCM`, `reqMOM`, `reqHCM`, `endTask`, `advVel`, `advHdng`), `sensor/Obstacle` (record) +
`ObstacleRegister` (copy-on-write Map) + `Sensor` (distance functions, accessors with safe defaults,
closest-index selection, and `cpaTime`/`cpaDist` so `CalcCPA.compute()` stays assignment-only),
`actuator/Actuator`, five `operation/` classes named exactly per LRE-OP1..5, and
`controller/LreController` (single-method mode-nested if-else, named predicates, payload capture into
`opVel`/`opHdng` state vars per the typed-trigger convention). `result_codegen.json` traced all 52
requirement ids.

Pre-pipeline environment fix (not an iter): `pipeline.yaml` `dafny_path.win32` and `wsl_isabelle_bin`
pointed at a different user's home (`willr`); both verifiers exited with "not found"/exit 127 — a bridge
failure, not a verdict — so the paths were corrected and the pipeline re-run on identical Java before
snapshotting.

Verdicts: **preflight** — 15 `rule4_double_missing_real_annotation` errors (every un-annotated `double`
counts: constants-class fields, record components, setter parameters, private statics).
**dafny_verify** — 4+ postcondition failures: the generator emits `ensures guard ==> mode == target`
per guarded transition with the guard evaluated post-state and no priority context, so any
higher-priority branch that fires while a lower branch's guard holds violates that branch's clause.
**isabelle_verify** — `deadlock_free` closer timed out at 600 s.

### Iter 2 — annotations, guard exclusivity, Isabelle closer
- Added the 15 annotations (preflight → pass).
- Reordered MOM and HCM blocks **autonomous-first** and added explicit negation chaining
  (`collisionRisk` named predicate; `!collisionRisk`, `!inOpez` conjuncts on lower-priority guards) —
  the `fdr4_system.txt`-sanctioned "redundant in Java, explicit for the extracted model" pattern,
  making sibling guards mutually exclusive.
- Isabelle diagnosis: every state already had a bare-precondition op and there was no Final state — the
  textbook causes didn't apply. The generated theory's own FORK comment revealed the real cause: the
  closer is selected by `hasPayloadDomain`; this machine has scalar payload domains
  (`OpVel`/`OpHdng = UNIV`, from the spec-mandated reqVel/reqHdng pass-through) **and**
  real-arithmetic guards, so it got `using St.exhaust_disc by auto`, which is documented to time out on
  real-arithmetic guards. A scratch-copy experiment (session dir copied, closer hand-patched, `isabelle
  build` run directly in WSL with a 300 s goal timeout) showed `by (metis St.exhaust_disc)` closes this
  machine in 68 s. Since no Java-side fix exists (the payload domain is the spec), the template
  heuristic was refined: discriminator `hasPayloadDomain` → `hasSeqPayloadDomain` (only SeqType/list
  payload domains — the documented metis-hang case — keep `auto`; scalar TypeRef payloads get metis).
- Result: only dafny_verify still failing (7 errors) — the negations alone did not help, because the
  clauses' premises are evaluated in the **post-state**: the abstracted sensor functions are bodyless
  with `reads this`, so after any real `mode` write their post-state values are unconstrained and no
  pre-state path condition can discharge a sibling clause. Dafny's own "related location" output
  (run directly for full detail) confirmed exactly this shape.

### Iter 3 — Dafny contract fixed with `old(...)`
Tool fix only, no Java change: `java2dafny.egl` now emits
`ensures old(condition) ==> mode == target`. Pre-state premises are decided by the if-else path
conditions, which the iter-2 negation chaining makes mutually exclusive; identity-write paths are
unaffected (`old(e) == e` on an unchanged heap). Validated standalone (`dafny verify`: 12 verified,
0 errors) before the pipeline run. Full pipeline: **all 12 phases pass, vacuity 0 findings — converged.**

## Caveats (run-specific compromises / workarounds)

- **Two toolchain edits were required for convergence** (documented inline with `FORK run-3` comments):
  1. `forge.transformations/src/main/resources/transformations/thy_generation_rule.egl` — deadlock_free
     closer selection `hasPayloadDomain` → `hasSeqPayloadDomain`.
  2. `forge.transformations/src/main/resources/transformations/java2dafny.egl` — transition `ensures`
     premise wrapped in `old(...)`.
  Both are generator-defect fixes, not Java codegen: no Java edit could have made iter-2's model verify.
  The full diff is archived as [`toolchain-fixes.patch`](toolchain-fixes.patch) in this run dir (the
  worktree's uncommitted template edits would otherwise be lost at reconciliation — apply it to the main
  checkout alongside the run copy-back).
  Risk note: neither could be regression-tested against the other studies' Isabelle/Dafny runs from this
  worktree (only CSP regression is scripted); the SeqType discriminator matches both documented data
  points (gas list-payload needs `auto`; real-guard machines need metis).
- `pipeline.yaml` machine-path edits (dafny_path win32, wsl_isabelle_bin user) — local tuning, not
  committed, per RUN_TRAJECTORY §2/§6. FDR4 timeout 3600 / memory_limit 78002 were pre-set by the human.
- **Priority design choice:** MOM/HCM evaluate autonomous safety transitions before operator event
  branches (iter-2 reorder). The requirements do not specify cross-category priority; safety-first is
  defensible, but an operator reqOCM arriving while a CAM guard holds is consumed by the CAM transition.
- **CAM entry action:** LRE-FR4 names no actuator output for CAM entry; none was invented.
- **cstc/cdyn typing:** left as plain `int` (no `@RoboChartType("nat")`) because the spec's −1 sentinel
  is not a natural number; preflight accepts this (rule 4 covers doubles only).
- **CPA semantics:** 2-D horizontal CPA computed in Sensor (`cpaTime`/`cpaDist`), treating
  `obs_ns_vel`/`obs_ew_vel` as relative velocity; no-dynamic-obstacle / zero-velocity default is
  `tcpa = −1`, `cda = current odist` (or 1000 when absent) — invented defaults consistent with LRE-OP5's
  "Sensor layer handles the no-obstacle case".
- iter-1 was re-run once on identical Java after fixing the tool paths (bridge failure, not a verdict);
  the snapshot reflects the genuine-verdict run.

## Cost

- **Run-level Claude token total** (source: session transcript JSONL `usage` blocks, summed over the
  whole driving session — `/cost` is not agent-readable): **≈ 0.35 M output tokens + 15 k fresh input
  tokens** (plus 29.6 M cache-read / 1.06 M cache-write input tokens), measured after the trajectory
  write-up; only the commit tail follows. Per-iter splits are intentionally `null`
  (`codegen_cost.kind = "estimated"`) — not measurable mid-run.
- **Run-level end-to-end execution time** (source: transcript timestamp span):
  - **end-to-end ≈ 43.8 min** (2625 s, first→last message after write-up; commit tail excluded);
  - **pipeline = 876.5 s (14.6 min)** = Σ pipeline_wall_clock_s (669.8 + 139.5 + 67.1); the productive
    figure discounting iter-1's 600 s closer hang is ≈ 276 s;
  - **agent = end-to-end − pipeline ≈ 29 min** (codegen + diagnosis incl. the scratch Isabelle
    experiment (~80 s) and a direct Dafny validation, + write-up + idle).
  - Add to `experiments/convergence/execution_times.md` at copy-back (file scrubbed from this worktree).

## Reproducibility

1. Stage iter-N: `rm -rf java.generated.project/src/main/java/lre && cp -r
   experiments/convergence/lre/run-3/iter-N/java java.generated.project/src/main/java/lre`
   and `cp experiments/convergence/lre/run-3/iter-N/traces/result_codegen.json java.generated.project/`.
2. Ensure `pipeline.yaml` per-machine paths are valid (`dafny_path.win32`, `wsl_isabelle_bin`,
   `fdr4_path.win32`) and `agent.active_case_study: lre`.
3. For iter-2 semantics the thy-template closer fix must be present; for iter-3 (converged) the
   java2dafny `old()` fix must also be present (both carry `FORK run-3` comments in
   `forge.transformations/src/main/resources/transformations/`).
4. `python experiments/scripts/run_experiment_iteration.py` — authoritative per-phase verdicts in
   `forge.assets/corrections/post_<phase>.json`.
