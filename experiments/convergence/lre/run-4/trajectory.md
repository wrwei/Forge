# LRE Convergence Trajectory — run-4

- **Study:** lre (package dir `lre`)
- **Run:** run-4
- **Condition:** B (honest independent — playbook absent and not read)
- **Actor:** me-as-developer (interactive session edits Java directly; no `Agent`-tool sub-agents dispatched)
- **Date:** 2026-06-05
- **Base:** severed single-commit worktree `94e2b4d9` ("isolated run base (history severed; answer material excluded from tree)")
- **Converged:** YES, at **iter 2** (all 12 phases passed, vacuity audit 0 findings)

## Independence

Iter-1 cold codegen read ONLY the canonical inputs:
`forge.assets/case-studies/lre/system/system_description.txt`,
`forge.assets/case-studies/lre/requirements/requirement_all.json`, `CLAUDE.md`,
`forge.assets/prompts/java_codegen_rules.txt`,
`forge.assets/prompts/chain_of_thought_codegen.txt`,
`forge.assets/prompts/few_shot_codegen.txt`,
`experiments/HOWTO_RUN_CONVERGENCE_EXPERIMENT.md`, and
`experiments/RUN_TRAJECTORY.md`. The session's injected context contained the
canonical CLAUDE.md/prompt files only — no agent auto-memory content, no
`.remember` SessionStart output, no study-specific prior-run narratives. Git
history was the single severed base commit. Nothing under `docs/`, no
`experiments/convergence/run-*`, no `reference-runs/`, no playbook, and no
findings knowledgebase was read (the scrubbed worktree did not contain them).
Iter 2 read only the live workspace source and iter-1's
`forge.assets/corrections/post_*` feedback, plus pipeline *tool* sources
(`forge.dashboard/web/runners.py` + `feedback/coverage.py` for the
`result_codegen.json` schema; `thy_generation_rule.egl` and the generated
`.dfy`/`.thy` artefacts for failure diagnosis), which RUN_TRAJECTORY explicitly
permits ("the tool, not the answer key").

## Headline

| Iter | Change | compile | coverage | preflight | t2m | m2m | m2t | dafny_gen | isabelle_gen | fdr4 | dafny_verify | isabelle_verify | vacuity | Pipeline wall-clock | Converged |
|------|--------|---------|----------|-----------|-----|-----|-----|-----------|--------------|------|--------------|-----------------|---------|--------------------:|-----------|
| 1 | Cold codegen: 15 files, 592 LOC, all 51 requirement IDs traced | ✅ | ✅ | ❌ 21 lints | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ 3/3 | ❌ 3 postconds | ❌ deadlock_free timeout | ✅ 0 | 695.1 s | no |
| 2 | `@RoboChartType("real")` ×21; `Tick` event + gated autonomous branches with cross-target exclusions; EGL closer-selection fix (tooling) | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ 3/3 | ✅ | ✅ deadlock-freedom proved | ✅ 0 | 175.8 s | **yes** |

## Iter-by-iter narrative

### Iter 1 — cold codegen

From the canonical inputs alone, produced 15 Java files (592 LOC) under
`java.generated.project/src/main/java/lre/` in sub-packages `annotation`,
`mode`, `constants`, `event`, `sensor`, `actuator`, `operation`, `controller`:
mode-nested if-else `LreController` (4 modes OCM/MOM/HCM/CAM, named boolean
predicates, all 18 behavioural transitions Beh1–Beh18, controller state vars
LRE-Var1–8 assigned from the five operations each step), operations `CalcVel`,
`CalcCStc`, `CalcCDyn`, `CalcCPA`, `CheckOPEZ` (single-expression `this.field =`
compute() bodies), `Sensor` with `odist/hdist/vdist`, obstacle field accessors
and closest-static/dynamic selection over an immutable `ObstacleRegister`, and
sealed-interface events. `result_codegen.json` traced all 51 requirement IDs.
Every mode block was given a bare-precondition event branch (ReqVel in OCM,
ReqOCM elsewhere) per the CLAUDE.md pattern (b); no `Final` state.

**Mid-iter environment fix (not a code iter):** the first pipeline invocation
failed `dafny_verify` ("executable not found") and `isabelle_verify` (exit 127)
because `pipeline.yaml` carried another machine's user paths
(`C:\Users\willr\...`, `/home/willr/...`). Located the real installs
(`C:\Users\Will\dafny\dafny\Dafny.exe`,
`/home/will/isabelle/Isabelle2023-CyPhyAssure/bin/isabelle`), patched the two
path entries locally, and re-ran the pipeline on **unchanged** code so iter-1's
verdicts are genuine (HOWTO §0: bridge failure, not proof failure — don't burn
an iter).

Genuine iter-1 verdicts — 9/12 passed; FDR4 passed 3/3 first time:

1. **preflight failed (21 × `rule4_double_missing_real_annotation`)** — every
   `double` field/parameter in *every* class needs `@RoboChartType("real")`,
   including constants, record components, actuator fields, sensor raw fields
   and `update(...)` parameters.
2. **dafny_verify failed (3 unproved postconditions, `LreController.dfy`
   :100/:104/:140)** — the Dafny generator emits one unconditional
   `ensures <guard> ==> mode' == <target>` per *autonomous* Java branch, with
   the bare guard as premise. Event-triggered branches (EndTask, ReqHCM) and
   earlier autonomous branches (inOpez) that target a different mode preempt
   the guard while it still holds, violating the implication.
3. **isabelle_verify failed (tactic timeout in `LreController_deadlock_free`,
   618.9 s ≈ the 600 s per-goal timeout)** — see iter 2 diagnosis.

### Iter 2 — three fixes

1. **Preflight:** added `@RoboChartType("real")` to all 21 flagged
   fields/params (Actuator ×2, LreConstants ×4, Obstacle record components ×6,
   Sensor fields ×5 incl. the static default, Sensor.update params ×4).
2. **Dafny:** added `record Tick()` to `InputEvent` and gated every autonomous
   branch on `event instanceof InputEvent.Tick`, conjoining cross-target
   exclusion predicates so each generated `ensures` holds on every return
   path: MOM's CAM-branch gains `!inOpez`, MOM's three HCM-branches gain
   `!inOpez && !collisionCourse`, HCM symmetric, CAM's exit gains the Tick
   gate (new named predicate `collisionCourse = cdaBelowMinSafe &&
   tcpaNonNegative`). Same-target branches need no mutual exclusion (their
   ensures are compatible). Verified all return paths on paper before the run.
3. **Isabelle (tooling fix):** scratch diagnosis in a copied session dir —
   with the closer `sorry`'d (under `-o quick_and_dirty`) the theory builds in
   76 s, proving the hog was the *closer*, not the reduction; the residual
   after `apply deadlock_free` is already param-free because the scalar
   payload-domain sets (`OpVel`/`OpHdng`, from the reqVel/reqHdng
   payload-capture pattern) are `[simp] "= UNIV"` definitions whose bounded
   existentials the method's internal simp discharges. `by (metis
   St.exhaust_disc)` closed it in ~88 s, while the template-selected
   `using St.exhaust_disc by auto` exceeds 600 s on LRE's real-arithmetic
   guard disjuncts. Patched `thy_generation_rule.egl` to key the `auto`
   closer on **Seq**-typed payload domains only (the documented gas-analysis
   regression case); machines with no payload domain or only scalar
   (TypeRef/ProductType) payload domains get `metis`. (`apply simp` between
   the two fails with no-progress — the residual is already simp-normal.)

Result: all 12 phases passed, vacuity 0 findings. Isabelle proved 20 lemmas
including `LreController_deadlock_free` in 101.8 s.

## Caveats (run-specific compromises / workarounds)

- **`Tick` input event added** — not in LRE-DM6's six operator events; it is a
  control-cycle artefact required so the Dafny generator's per-branch ensures
  premises are event-specific. Spec deviation, HOWTO-sanctioned pattern.
- **Priority-encoding negations** (`!inOpez`, `!collisionCourse`) added to
  autonomous guards: Java behaviour is unchanged (they were implicit in the
  else-if chain) but the extracted RoboChart/Dafny/Isabelle guards now encode
  priority explicitly; the model transitions are no longer the verbatim
  one-guard-per-requirement reading of Beh8–Beh14.
- **Pipeline template edited mid-run** (`thy_generation_rule.egl` closer
  selection, see iter 2 §3). A tool fix, not a Java fix — committed separately
  from the run archive so it can be reviewed/cherry-picked independently.
- **Machine-local `pipeline.yaml` edits not committed:** `dafny_path.win32`,
  `wsl_isabelle_bin` (user-path corrections), plus the §2 kill-policy tuning
  (`fdr4.timeout: 3600`, `memory_limit_mb: 78002`) which was pre-set by the
  human.
- Iter-1 design choices/ambiguities (in `iter-1/feedback/post_codegen.*`):
  `cstc`/`cdyn` as signed `int` (−1 sentinel; matches `csp_overrides.csp`
  `core_int = {-1..1}`), invented `NO_OBSTACLE_DIST = 1.0e6`, CalcCPA computed
  in the 2-D horizontal plane with obstacle velocities read as already
  relative (division yields NaN when no dynamic obstacle exists — NaN guard
  semantics are safe: no spurious CAM entry, CAM exits), velocity thresholds
  as literal `1.0`, CAM has no entry action.
- **KB append and `execution_times.md` row deferred to copy-back** — both
  files are scrubbed out of this worktree (correct per RUN_TRAJECTORY §6.3);
  the findings below are the source for the KB block.

## Run-level cost

### Claude token total (RUN_TRAJECTORY §6.2a)

Source: sum of `usage` blocks in the driving session's transcript JSONL
(`48e11366-…f.jsonl` under the clean `CLAUDE_CONFIG_DIR`); `/cost` is not
exposed to the agent as a tool. Captured at archive time (excludes the final
part of this write-up):

- input tokens (non-cache): **12,075**
- cache-creation input tokens: **839,488**
- cache-read input tokens: **35,727,072**
- output tokens: **401,417**

### End-to-end execution time (RUN_TRAJECTORY §6.2b)

- **end-to-end** (session span, first→last transcript timestamp at archive
  time): **2,723 s ≈ 45.4 min**
- **pipeline** (Σ `pipeline_wall_clock_s` over the two snapshotted iters,
  i.e. the productive runs: 695.1 + 175.8): **870.9 s ≈ 14.5 min**. (A
  further ~3 min env-broken first pipeline invocation of iter 1 is excluded;
  its dafny/isabelle phases failed instantly on missing tool paths.)
- **agent** (end-to-end − pipeline; codegen + diagnosis + 3 scratch Isabelle
  builds of ~90 s each + write-up + idle): **≈ 1,852 s ≈ 30.9 min**

## Findings (durable, generalizable — KB append source)

**F1 — Annotate every `double` everywhere at cold-codegen time.** The
preflight `rule4_double_missing_real_annotation` lint requires
`@RoboChartType("real")` on *every* double field and parameter in *every*
class — constants classes, record components, actuator state, sensor raw
fields, even private statics and plain setter parameters — not just the
controller/operation fields that CLAUDE.md's examples show. 21 of iter-1's
findings were this one mechanical rule. Apply it exhaustively during cold
codegen; it costs nothing and removes a whole iteration's worth of lint noise.

**F2 — The Dafny generator's autonomous-branch contracts need event-gating
plus cross-target exclusions.** It emits `ensures <bare-guard> ==> mode' ==
<target>` for each autonomous branch, with no event context and no negation
of preceding branches. Two-part recipe: (a) gate every autonomous branch on a
`Tick` input event so every event-triggered return path falsifies the premise;
(b) conjoin negations of earlier same-mode branches **only when they target a
different mode** (same-target branches have compatible ensures and need no
exclusions — this keeps guards much shorter than full mutual exclusion).
Verify per return path: each premise must be false or its target reached.

**F3 — Isabelle deadlock_free closer: scalar payload domains want `metis`,
not `auto`.** The generated `[simp] "= UNIV"` payload-set definitions mean
`apply deadlock_free` internally discharges the `∃x∈Set.` enabledness
existentials, so the residual is param-free and `by (metis St.exhaust_disc)`
closes it (~88 s here) — while `using St.exhaust_disc by auto` dies (>600 s)
on real-arithmetic guard disjuncts. The template's closer selection keyed on
*any* payload domain; machines combining scalar payload captures (e.g.
reqVel/reqHdng pass-through) with real-arithmetic guards hit the gap. Fixed by
keying `auto` on Seq-typed payload domains only.

**F4 — Diagnose Isabelle timeouts in a scratch session, not through the
pipeline.** Copy `output/isabelle/{ROOT,*.thy}` to a temp dir and run
`isabelle build -D .` in WSL directly: replacing the suspect closer with
`sorry` under `-o quick_and_dirty` separates "reduction hangs" from "closer
hangs" in one ~76 s build, and each candidate closer costs ~90 s to test —
versus ~12 min per full-pipeline attempt. The failed `apply simp` attempt also
printed the residual goal for free, confirming its shape before choosing the
closer.

**F5 — Tool-path failures have unmistakable signatures; fix env, re-run the
same iter.** `dafny_verify` failing in 0.0 s with "executable not found", or
`isabelle_verify` exiting 127 with "`…/bin/isabelle`: No such file or
directory", means `pipeline.yaml`'s per-machine paths (`dafny_path.win32`,
`wsl_isabelle_bin`) point at another machine's user dirs. Patch locally, rerun
the pipeline on unchanged code, and treat the second run's verdicts as the
iter's verdicts; don't snapshot the env-broken run or count it as an iter.

## Reproducibility

1. Stage iter-N source: copy `run-4/iter-<N>/java/` over
   `java.generated.project/src/main/java/lre/` (wipe the dir first) and
   `run-4/iter-<N>/traces/result_codegen.json` to
   `java.generated.project/result_codegen.json`.
2. Ensure machine-local `pipeline.yaml` entries point at real installs
   (`phases.dafny_verify.dafny_path`, `phases.isabelle_verify.wsl_isabelle_bin`),
   FDR4 kill policy per RUN_TRAJECTORY §2 (timeout 3600, memory cap = page
   file), type ranges `{0..1}` in `forge.dashboard/corrections/type_ranges.json`.
3. For iter-2 results the `thy_generation_rule.egl` closer-selection fix must
   be present (separate commit alongside this archive).
4. `python experiments/scripts/run_experiment_iteration.py` — authoritative
   per-phase verdicts in `forge.assets/corrections/post_<phase>.json`.
   Iter-2 expected: 12/12 passed, vacuity 0 findings.
