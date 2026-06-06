# How to Run a Convergence Experiment (Agent Guide)

**You are an LLM agent that has been asked to drive a FORGE convergence
experiment for one of the three case studies.** Read this file first. It
gives you the workflow, the toolchain assumptions, and the failure modes
to avoid.

If you are a human reading this: the same workflow applies but you can
skim sections labelled *(agent-only)*.

> **Looking for cold-baseline (no iteration)?** See the sibling guide
> [`HOWTO_RUN_COLD_BASELINE_EXPERIMENT.md`](HOWTO_RUN_COLD_BASELINE_EXPERIMENT.md).
> Cold-baseline runs the pipeline against a single cold codegen K times
> per study (no feedback loop); the convergence guide below covers the
> iterative loop that *uses* feedback.

---

## 0. Prerequisites — Windows users (WSL)

The pipeline's `isabelle_verify` phase (Phase 6c) runs `isabelle build`
against the generated `.thy`. Isabelle/UTP with the CyPhyAssure
distribution is **Linux-only**, so on Windows the dashboard / experiment
driver bridges to WSL via `wsl.exe`. Phases 2a-6b and 6d (vacuity)
run natively on Windows; **only Isabelle proof checking needs WSL**.

**One-time setup** — install WSL2 + Ubuntu + Isabelle/UTP.
Without it, every iteration's `isabelle_verify` phase fails even when
the rest of the pipeline passes — and the convergence loop can't
terminate because the convergence criterion requires
`isabelle_verify: completed`.

**Recurring gotchas you'll hit running iterations on Windows:**

- **Docker Desktop hijacks the default WSL distro after a reboot.**
  Symptom: `wsl <command>` lands in `docker-desktop` instead of
  `Ubuntu`, and `isabelle` isn't on PATH there. Fix once per boot:
  ```powershell
  wsl --set-default Ubuntu
  ```
  Verify with `wsl -l -v` — the starred entry should be Ubuntu.

- **WSL wedges if you run two Isabelle builds concurrently.** Each
  iter's `isabelle_verify` is sequential within a trajectory, but
  **don't run two convergence trajectories in parallel** against the
  same WSL distro. Concurrent `wsl.exe` invocations deadlock the VM
  and require `wsl --shutdown` plus a full Windows reboot to recover.

- **First post-boot `wsl` call has systemd warm-up noise on stdout.**
  Harmless for the experiment driver; visible in logs.

If WSL is wedged or `isabelle` isn't found, `post_isabelle_verify.md`
will report `theory_load_error` or a process exit with no output.
That's the bridge failing, not the proof failing — fix the WSL
environment first, then resume the iteration; don't burn a full
trajectory iter chasing it as a Java-side issue.

---

## 1. Pick a study and confirm the workspace

```bash
# Set pipeline.yaml active_case_study to the study you want to drive.
grep "active_case_study" pipeline.yaml
# Edit if needed; valid values: lre | chemical_detector | sranger
```

Wipe any stale Java workspace state from a prior study:

```bash
rm -rf java.generated.project/src/main/java/<study>
mkdir -p java.generated.project/src/main/java/<study>
```

---

## 2. The canonical mode

There is **one canonical mode**: the LLM agent in the user's session
edits Java directly, runs the pipeline, snapshots, and iterates.

### Interactive me-as-developer ⭐ CANONICAL

You (the LLM agent running in the user's session) are the developer.
For each iter you read the inputs and produce Java (iter 1) or apply
feedback (iter ≥ 2), run the pipeline, snapshot. Per-iter token cost
is **not separately measured** — the session is the cost. The trade-off
versus a per-iter sub-agent dispatch is intentional and documented in
the section immediately below.

> **DO NOT dispatch `Agent` tool sub-agents from inside a convergence
> trajectory.** Sub-agent dispatch was the previous canonical mode but
> is now **explicitly banned** for this experiment. The trapdoors that
> made it unsafe:
>
> 1. **Worktree-vs-main HEAD drift.** Each sub-agent runs in a temp
>    `.claude/worktrees/agent-<id>/` worktree whose HEAD can lag
>    behind `main`. Sub-agents observed in 2026-05-26 ran `git reset
>    --hard <stale-HEAD>` on the *parent worktree* (i.e. `main`) when
>    they detected their HEAD was wrong — destroying uncommitted
>    deletions and pulling another case study's files back
>    into the active study's workspace. Recovery required reflog
>    archaeology + cherry-picks.
> 2. **Cross-case-study pollution.** When `reset --hard` restores files
>    that should have been deleted (e.g. another study's package under
>    `java.generated.project/src/main/java/<other-study>/**` while
>    running the active study's iter), the M2M picks them up and unifies
>    conflicting event signatures (e.g. one study's `record Foo(Bar payload)`
>    vs another's same-named `record Foo()`) into garbage CSP. A whole
>    pipeline run gets wasted before the contamination is visible.
> 3. **Untracked accidental edits to the parent worktree.** Even when
>    the sub-agent intends to edit inside its own worktree, absolute
>    path arguments to `Edit` / `Write` can land on the parent
>    repository's files. The sub-agent's commit then includes only the
>    worktree-side edits; the parent has unstaged duplicates.
> 4. **Commit-or-lose discipline.** Sub-agents must `git commit` before
>    returning or the worktree auto-cleans and the work is lost — easy
>    to forget under time pressure, expensive to recover from.
>
> Items 1-3 are not fixable from the orchestrator side without
> per-call mitigation that's worth more than the cost savings. The
> only safe rule is: **the orchestrator does all the editing itself**.
> Read, Edit, Write, Bash, Grep, Glob are sufficient — no `Agent` tool
> calls during an iter.

---

## 3. Per-iter workflow

You read these inputs and edit Java directly using your own
Read/Edit/Write tools. No sub-agent dispatch.

### Iter 1 — cold codegen

Inputs live at their canonical repo locations — `forge.assets/...` for
case-study spec and codegen rules, `CLAUDE.md` at the repo root for
project-level conventions. The agent must read from these specific
paths only and must not reach into prior-iter snapshots.

**Read these and only these** (do not look at `experiments/convergence/<study>/iter-*/`,
do not look at `experiments/cold-baseline/<study>/run-*/src/`):

- `forge.assets/case-studies/<study>/system/system_description.txt`
- `forge.assets/case-studies/<study>/requirements/requirement_all.json`
- `CLAUDE.md` (project-level codegen conventions, at the repo root)
- `forge.assets/prompts/java_codegen_rules.txt`
- `forge.assets/prompts/chain_of_thought_codegen.txt`
- `forge.assets/prompts/few_shot_codegen.txt`

Produce the Java source tree to `java.generated.project/src/main/java/<study>/`.
Follow CLAUDE.md's package conventions (`actuator/`, `annotation/`,
`constants/`, `controller/`, `event/`, `mode/`, `operation/`, `sensor/`).

Then run the pipeline (see §3.3 below) and snapshot (§3.4).

### Iter N ≥ 2 — apply feedback

**Read these:**

- `java.generated.project/src/main/java/<study>/**` — the current Java
  source (output of iter N−1; this is the pipeline's *workspace*, not
  under `experiments/`, but the agent has to read it to know what code
  it's editing)
- `forge.assets/corrections/post_*.md` and `post_*.json` — every
  per-phase feedback artefact from iter N−1's pipeline run (the pipeline
  writes feedback to its own canonical location)
- `forge.assets/prompts/fdr4_system.txt` — guide for interpreting
  FDR4 verdicts (which failures are "expected" pass-throughs vs real bugs)
- `CLAUDE.md`, `forge.assets/prompts/java_codegen_rules.txt` —
  the rules that still apply

**Do not read** any of:

- prior iter snapshots in `experiments/convergence/<study>/iter-*/` —
  cross-iter peek would let the agent see what the *next* sub-agent already
  tried; the per-iter discipline requires working only from the immediately
  prior iter's source + feedback
- `experiments/cold-baseline/<study>/run-*/` — cold-baseline artefacts
  that would also leak structure

For each `post_*.md` whose `status` is **not** `passed`:

1. Read the `fix_directive` and `java_trace` entries.
2. Apply the directive minimally — change only what the directive asks
   for. Don't rewrite for style, don't refactor unrelated code, don't
   add new requirements.
3. Respect `forge.assets/prompts/java_codegen_rules.txt` at all times.

**Special cases (agent-only — these are documented patterns):**

- **Determinism is not verified.** `run_fdr4` strips the official
  generator's `:[deterministic]` assertions before invoking FDR (see
  `pipeline.yaml` `phases.fdr4.expected_failures: []` and
  `forge.assets/prompts/fdr4_system.txt`): RoboChart models are
  non-deterministic by construction, so FDR4 checks only deadlock- and
  divergence-freedom. You will NOT see a `:[deterministic]` failure;
  there is nothing to pass through.
- **Isabelle thrashing alert "Previous fix strategies are not working"**
  — this means the cosmetic fix the prior iter applied did not address
  the root cause. CHANGE APPROACH. A common pattern in this codebase is
  to remove a structurally problematic Java class entirely (e.g. the
  `clock` reserved-word collision is resolved by dropping the `Clock`
  class and re-expressing the time semantics around `Tick` events).
- **`post_coverage.md` over-implementation warnings** for structurally
  necessary elements (the controller class itself, the mode enum, the
  sealed-interface event hierarchies) — leave them.
- **Dafny postcondition that fails due to a higher-priority Java branch
  preempting an autonomous transition** — gate the autonomous on `Tick`
  so its premise is event-specific (so EndTask/operator events don't
  silently violate it). See `experiments/convergence/<study>/trajectory.md`
  iter 2 for a worked example.

### §3.3 Running the pipeline

```bash
python experiments/scripts/run_experiment_iteration.py
```

This runs all 11 deterministic phases (compile → coverage → preflight →
T2M → M2M → M2T → Dafny gen → Isabelle gen → FDR4 → Dafny verify →
Isabelle verify) plus the vacuity audit. Each phase writes a pair of
feedback artefacts (`post_<phase>.{md,json}`) into
`forge.assets/corrections/`. The SUMMARY block at the end lists
per-phase status.

Convergence criterion: every phase reports `passed` or `completed`, AND
the vacuity audit reports zero findings.

If FDR4 reports inconclusive (`assertion(s) checked, 0 passed, N inconclusive`),
verify the auto-discovery picked the right CSP file:

```bash
grep "auto-discovered" forge.assets/corrections/post_fdr4.md
```

For single-controller studies, it should pick `<Stm>Controller_coreassertions.csp`
(no `_Module_` infix). The auto-discovery patch in
`forge.dashboard/web/runners.py` `_discover_fdr4_csp_file` enforces
this preference (commit `df6fd82`).

If the type ranges in `forge.transformations/output/csp-gen/instantiations.csp`
look wider than `{0..1}`, the `apply_csp_corrections()` step did not run
— check that the FDR4 phase wraps it (it does in
`experiments/scripts/run_experiment_iteration.py`).

### §3.4 Snapshot the iter

```bash
python experiments/scripts/snapshot_iter.py <study> <iter_number> [--actor agent-tool-claude]
```

The script (`experiments/scripts/snapshot_iter.py`) does everything
mechanical: creates `experiments/convergence/<study>/iter-<N>/` with
the canonical `{java,feedback,traces,formal-artefacts/{dafny,csp,isabelle}}`
layout, then copies in:

- `java.generated.project/src/main/java/<study>/**` → `iter-N/java/`
- `forge.assets/corrections/post_*.{md,json}` → `iter-N/feedback/`
- `forge.transformations/output/*.dfy` → `iter-N/formal-artefacts/dafny/`
- `forge.transformations/output/robochart_controller.rct` +
  `csp-gen/` tree → `iter-N/formal-artefacts/csp/`
- `forge.transformations/output/isabelle/*` →
  `iter-N/formal-artefacts/isabelle/`
- `forge.transformations/output/trace_*.json` + `*.xmi` +
  `java.generated.project/result_codegen.json` → `iter-N/traces/`
- `iter-N/summary.json` populated with the mechanical fields — see
  §3.5.

The agent then fills in **only** the three TODO fields the script
cannot infer (`failure_summary`, `actor_codegen_summary`,
`codegen_cost.*`). Refuses to overwrite an existing `iter-N/`
without `--force`.

For the legacy hand-cp recipe (useful if you need to capture a partial
snapshot mid-pipeline, or against a non-standard layout), see the
script source — it's straightforward `shutil.copytree` / `glob` calls.

### §3.5 `summary.json` schema

`snapshot_iter.py` (§3.4) emits one `summary.json` per iter with the
mechanical fields pre-populated. The agent fills in the three TODO
fields the script can't infer. Canonical schema:

**Auto-populated by `snapshot_iter.py`:**
`iter`, `study`, `actor` (from `--actor` arg, default `me-as-developer`),
`java_file_count`, `java_loc`, `phase_results` (parsed from
`forge.assets/corrections/post_<phase>.json` `status` fields,
authoritative because the runner itself writes them),
`phase_wall_clocks_s` + `pipeline_wall_clock_s` (parsed from
`forge.assets/corrections/phase_timings.json`, written by
`run_experiment_iteration.py` at end-of-run), `converged` (computed
from the phase_results + vacuity).

**You fill by hand:**
`failure_summary` (one line, only when not converged),
`actor_codegen_summary` (what you did this iter),
`codegen_cost.{kind, tokens_in, tokens_out, tool_uses, wall_clock_s, notes}`
(`null` + `kind: "estimated"` is correct — you don't have per-iter
token counts when you do the work yourself).

Full shape:

```jsonc
{
  "iter": N,
  "study": "<study>",
  "actor": "me-as-developer",
  "java_file_count": <int>,
  "java_loc": <int>,
  "phase_results": {                  // status of each pipeline phase
    "compile": "completed" | "failed",
    "coverage": ...,
    "preflight": ...,
    "t2m": ..., "m2m": ..., "m2t": ...,
    "dafny_gen": ..., "isabelle_gen": ...,
    "fdr4": ..., "dafny_verify": ..., "isabelle_verify": ...,
    "vacuity": "passed" | "failed"
  },
  "phase_wall_clocks_s": { ... },     // from the pipeline SUMMARY block
  "pipeline_wall_clock_s": <float>,   // sum of the above
  "converged": <bool>,
  "failure_summary": "<one-line WHY this iter didn't converge>" | null,
  "actor_codegen_summary": "<one-line WHAT you did this iter>",
  "codegen_cost": {
    "kind": "estimated",
    "tokens_in":   null,
    "tokens_out":  null,
    "tool_uses":   null,
    "wall_clock_s": null,
    "notes": "<one-line context>"
  }
}
```

The session-level token/tool-use rollup is captured at the trajectory
level (see `trajectory.md` write-up after convergence/cap), not per
iter. Honest "not measurable here" stays as `null` rather than being
faked.

---

## 4. Stop conditions

- **Converge**: every phase `passed`/`completed`, vacuity audit clean. Stop.
- **Cap reached**: default 7 iters, configurable. Document why convergence didn't happen.
- **Pipeline crash**: if a deterministic phase (T2M, M2M, M2T-gen) fails with a Python/Java exception (not a verifier verdict), the iter is unusable — DON'T snapshot it; fix the root cause and re-run.

---

## 5. Document and commit

After the trajectory finishes (converged or capped), write
`experiments/convergence/<study>/trajectory.md` covering:

- Headline table: per iter, what changed and which verifiers passed/failed.
- Iter-by-iter narrative: what feedback drove the change, what the change was.
- Caveats: any spec compromises made, any tooling workarounds applied.
- Findings: durable lessons from this run that aren't obvious from `CLAUDE.md`,
  the codegen rules, or this HOWTO, and that should help future runs of any
  case study. One paragraph per finding (F1, F2, ...), each naming the rule,
  the *why* (with the supporting incident or diagnostic signal), and *how to
  apply* it next time. Distinguish from caveats: caveats are run-specific
  compromises ("in this run I had to..."); findings are generalisable
  guidance ("in any future run, do/avoid this because..."). Findings sections
  of *other* studies' trajectories are still forbidden reads (RUN_TRAJECTORY
  §4) — do not consult them when writing your own.
- Reproducibility: how a reader can stage iter-N's source and re-run the pipeline.

Reference `experiments/convergence/<study>/trajectory.md` as the template.

Commit + push:

```bash
git add experiments/convergence/<study>
git commit -F <message-file>
git push origin main
```

The commit message should describe the actor (`me-as-developer`), the
iter count, and the convergence outcome.

---

## 6. References

- [`HOWTO_RUN_COLD_BASELINE_EXPERIMENT.md`](HOWTO_RUN_COLD_BASELINE_EXPERIMENT.md) — sibling guide for the no-feedback cold-baseline K=10 experiment
- `experiments/README.md` — top-level experiments index
- `experiments/experiment-protocol.md` — deeper methodology document (convergence criterion, hard constraints)
- `experiments/convergence/<study>/trajectory.md` — per-study trajectory write-up
- `experiments/scripts/README.md` — script-by-script reference
- `CLAUDE.md` — codegen rules, M2M conventions, known gotchas
- `forge.assets/prompts/fdr4_system.txt` — expected-failure patterns in FDR4
