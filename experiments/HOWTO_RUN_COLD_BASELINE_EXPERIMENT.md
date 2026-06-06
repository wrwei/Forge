# How to Run a Cold-Baseline Experiment (Agent Guide)

**You are an LLM agent (or a human) that has been asked to run the FORGE
cold-baseline experiment for one or more of the three case studies.**
Read this file first.

A **cold pass** is a single LLM invocation given the case-study spec +
codegen rules + project conventions, with **no prior feedback and no
existing Java source**. The pipeline runs once against that cold source,
verdicts are recorded, and the pass does NOT iterate. This isolates the
question: *is the iteration loop in the convergence experiment doing
real work, or could a single cold codegen converge on its own?*

The published result is **0 of 30** cold passes converging across LRE,
Chemical Detector, and SRanger at K=10 each — see
[`cold-baseline/README.md`](cold-baseline/README.md) for the aggregate.

---

## 0. Prerequisites — Windows users (WSL)

The pipeline's `isabelle_verify` phase (Phase 6c) runs `isabelle build`
against the generated `.thy`. Isabelle/UTP with the CyPhyAssure
distribution is **Linux-only**, so on Windows the dashboard / experiment
driver bridges to WSL via `wsl.exe`. Phases 2a-6b and 6d (vacuity)
run natively on Windows; **only Isabelle proof checking needs WSL**.

**One-time setup** — install WSL2 + Ubuntu + Isabelle/UTP (the full
install, session-heap pre-build, and PATH wiring). Without it, every
`run_baseline.py` invocation will fail the `isabelle_verify` phase even
though the other 11 phases will pass.

**Recurring gotchas you'll hit running batches on Windows:**

- **Docker Desktop hijacks the default WSL distro after a reboot.**
  Symptom: `wsl <command>` lands in `docker-desktop` instead of
  `Ubuntu`, and isabelle isn't on PATH there. Fix once per boot:
  ```powershell
  wsl --set-default Ubuntu
  ```
  Verify with `wsl -l -v` — the starred entry should be Ubuntu.

- **WSL wedges if you run two Isabelle builds concurrently.** A K=10
  batch loops `run_baseline.py <study> $n` 10 times — that's fine
  because the loop is sequential. **Do NOT parallelise** (e.g. with
  `xargs -P` or background `&`); concurrent `wsl.exe` invocations
  against the same distro deadlock the VM and require
  `wsl --shutdown` plus a full Windows reboot to recover.

- **systemd warm-up pollutes stdout on the first `wsl` call after
  boot.** Harmless for the experiment driver (which doesn't parse
  early stdout) but visible in logs. If you're scripting around
  `wslpath`, pipe through `2>/dev/null | tail -1`.

If WSL is wedged or isabelle isn't found, `post_isabelle_verify.md`
will report `theory_load_error` or a process exit with no output.
That's the WSL bridge failing, not the proof failing — fix the WSL
environment first, then re-run just the affected `run-N`.

---

## 1. Pick a path

The cold-baseline experiment has **two distinct workflows**. Pick based
on what you're trying to do.

| Path | What it does | When to use |
|---|---|---|
| **A — Re-run the pipeline against committed K=10 sources** | Replays the deterministic pipeline (phases 2a-6d) against the cold Java already committed under `cold-baseline/<study>/run-N/src/`. No LLM is invoked. | You want to verify the published per-phase outcomes reproduce on your machine, or you've changed a transformation template / vacuity rule and want to see how the existing 30 cold sources fare against it. |
| **B — Generate fresh K=10 cold runs** | Spawns K cold LLM passes per study via the Agent tool (or any other driver of your choice), captures each pass's Java tree under a new `run-N/src/`, then runs Path A's pipeline replay over each. | You're running the experiment with a different LLM, a different prompt bundle, or after a substantive pipeline change you want measured against fresh cold codegen. |

---

## 2. Path A — Reproduce K=10 against committed sources

The 30 cold-codegen Java trees are checked in under
`experiments/cold-baseline/<study>/run-{1..10}/src/main/java/<pkg>/`.
`run_baseline.py` copies one of them into `java.generated.project/`,
runs the pipeline, copies the resulting `post_*.md` artefacts back into
the run directory, and writes a `pipeline-results.md` summary.

### Single run

```bash
python experiments/cold-baseline/scripts/run_baseline.py <study> <run_number>
```

`<study>` is `lre`, `chemical_detector`, or `sranger`.
`<run_number>` is `1..10`.

The script wipes `java.generated.project/src/main/java/*` first, so it
is safe to invoke back-to-back across studies and run numbers. Each
invocation takes ~5-7 minutes (compile + 11 deterministic phases + the
vacuity audit). A 7-minute hard timeout is enforced — runs that exceed
it are recorded as `pipeline_timeout` rather than crashing the driver.

### Full K=10 batch (one study)

```bash
for n in $(seq 1 10); do
    python experiments/cold-baseline/scripts/run_baseline.py lre $n
done
```

Repeat for `chemical_detector` and `sranger` for the full K=30. End to
end this is ~3.5 hours per study, ~10 hours total.

### Aggregate the results

After all 30 runs complete:

```bash
python experiments/cold-baseline/scripts/aggregate_baseline.py
```

This walks every `run-N/post_*.md`, classifies the per-phase outcomes,
and rewrites two artefacts:

- `cold-baseline/<study>/results-summary.md` — per-run pass/fail matrix
  for that study (10 rows × 12 phase columns)
- `cold-baseline/README.md` — the top-level aggregate table is updated
  in place (compiled / reached-fdr4 / converged counts per study)

Convergence count of zero is the expected outcome — see §5 below.

> **Heads up: Path A overwrites the committed `post_*.md` artefacts.**
> Each `run_baseline.py` invocation copies the run-N's cold Java
> source into `java.generated.project/`, runs the current pipeline,
> and copies the resulting `forge.assets/corrections/post_*.md` back
> into `cold-baseline/<study>/run-N/`. If the pipeline has evolved
> since the K=10 batch was originally captured (new phases like
> vacuity, M2T template changes, etc.), the overwritten artefacts
> will differ from the committed canonical results. The published
> K=30 results were captured at a specific pipeline-infrastructure
> commit SHA; treat the committed `post_*.md` as a frozen historical
> record. If you want to verify reproducibility without mutating the
> record, use `git stash` after the run and inspect the diff. If
> your re-run was for a deliberate "what does the current pipeline
> say about the old cold sources?" question, then commit the diff
> as a *new* dated experiment record under
> `cold-baseline/<study>/cold-experiment-<date>.md` (see §6).

---

## 3. Path B — Generate fresh K=10 cold runs

Use this when you want to measure cold-codegen performance for a
different LLM, a different prompt bundle, or after a substantive
pipeline change. Each cold pass is one fresh LLM invocation + one
pipeline run, repeated K times per study.

### Step 1 — Spawn a cold codegen sub-agent

**Use the Agent tool with `subagent_type: claude`, one sub-agent per
cold pass. Do NOT use `claude -p` (headless mode).**

Why this matters: a single cold pass involves many tool calls — Read
the spec, Read the requirements, Read the prompt assets, Write Java
files, possibly Edit and re-Read them. The sub-agent must maintain
its own context across those tool calls. `claude -p
--no-session-persistence` discards context between calls and produces
incoherent output.

Each cold pass is a *fresh* sub-agent (no context shared across cold
passes — that is the cold-baseline definition), but within one pass
the sub-agent's session persists across its tool calls — this is
exactly what the Agent tool provides and exactly what `claude -p` does
not.

See [`HOWTO_RUN_CONVERGENCE_EXPERIMENT.md`](HOWTO_RUN_CONVERGENCE_EXPERIMENT.md)
§2 Mode 1 for the invocation shape (fresh session per pass, `<usage>`
block captured for cost data).

The prompt is the **cold codegen prompt only** — cold runs by
definition have no feedback to apply:

```
prompt = build_cold_prompt(study)  # the cold codegen prompt; no feedback section
result = spawn_agent(subagent_type="claude", prompt=prompt)
# result.usage has {tokens_in, tokens_out, tool_uses, duration_s}
```

The cold prompt directs the sub-agent to read **only**:

- `forge.assets/case-studies/<study>/system/system_description.txt`
- `forge.assets/case-studies/<study>/requirements/requirement_all.json`
- `CLAUDE.md`
- `forge.assets/prompts/{java_codegen_rules,chain_of_thought_codegen,few_shot_codegen}.txt`

And explicitly **not** read any prior cold-baseline output, any
convergence-experiment iter snapshot, or any converged reference
source. The cold prompt must enforce this "do NOT read" list.

### Step 2 — Capture the Java tree

The sub-agent writes Java to `java.generated.project/src/main/java/<pkg>/`.
After it returns, snapshot the source into a new run directory:

```bash
STUDY=lre
N=11   # next unused run number for this study
RUN=experiments/cold-baseline/$STUDY/run-$N
mkdir -p "$RUN/src/main/java"
cp -r java.generated.project/src/main/java/* "$RUN/src/main/java/"
# Optional: capture the sub-agent's own summary alongside.
echo "<sub-agent summary>" > "$RUN/agent-summary.md"
```

### Step 3 — Run the pipeline against the new source

Now Path A applies:

```bash
python experiments/cold-baseline/scripts/run_baseline.py $STUDY $N
```

### Step 4 — Repeat K times per study

K=10 per study × 3 studies = 30 fresh cold passes. Reset
`java.generated.project/` between passes (the driver does this
automatically — no manual cleanup needed).

### Step 5 — Aggregate

Same `aggregate_baseline.py` invocation as Path A.

---

## 4. Pipeline operational notes

These are handled automatically by `run_baseline.py` and the pipeline
driver it invokes — listed here so an agent reading this file knows
they are *covered*, not as steps to apply manually.

### `agent.active_case_study` is auto-synced

The pipeline's coverage phase reads `pipeline.yaml`'s
`agent.active_case_study` to locate the requirements file to validate
the generated Java against. If that value doesn't match the study
`run_baseline.py` was invoked for, coverage compares the wrong pair
of (Java, requirements) and post_coverage.md becomes meaningless.

`run_baseline.py` handles this on every invocation: it reads the
current `active_case_study`, sets it to the `<study>` arg before
spawning the pipeline subprocess, and restores the original value
on every exit path (success, failure, timeout, ctrl-c). No manual
edit to `pipeline.yaml` is needed.

### FDR4 CSP type ranges are auto-flattened to `[0..1]`

FDR4 inherits the model-derived channel type ranges from `instantiations.csp`
(typically wider than `[0..1]`, e.g. `union({-2..2}, ...)`). Wider
ranges either fail to compile or blow the Windows page-file commit
during compilation. The driver therefore runs `web.csp_corrections.
apply_csp_corrections(pipeline.yaml)` immediately before invoking
`refines.exe` — this rewrites `instantiations.csp` in place to use
`{0..1}` for every event-payload channel.

This is invoked automatically by `experiments/scripts/
run_experiment_iteration.py` (which `run_baseline.py` calls), so no
manual flattening is needed when running through these scripts. If
you ever invoke FDR4 standalone via `refines.exe` outside this
driver, you must flatten the type ranges yourself first.

### FDR4 picks the controller-only assertions, not the module-level one

Each study's `csp-gen/defs/` directory contains multiple
`_coreassertions.csp` files (one per generated state machine + one per
module). The composed-system Module-level assertions explode the state
space at `[0..1]` ranges and report `inconclusive` rather than
`pass`/`fail`. `forge.dashboard/web/runners.py
_discover_fdr4_csp_file` therefore prefers
`<Stm>Controller_coreassertions.csp` (controller-only) over
`*_Module_coreassertions.csp` for single-controller studies.

If `post_fdr4.md` reports inconclusive verdicts, verify the
auto-discovery picked the controller-only file:

```bash
grep "auto-discovered" forge.assets/corrections/post_fdr4.md
```

For single-controller studies (LRE, SRanger) it should match
`<Stm>Controller_coreassertions.csp` with no `_Module_` infix. For
the multi-controller Chemical Detector study it picks the unified
`GasAnalysisController_System_Module_coreassertions.csp`.

---

## 5. Convergence criterion (per cold pass)

A cold pass *converges* iff **all** of the following hold (identical to
the convergence experiment's per-iter criterion, just measured once
without iteration):

1. All twelve deterministic pipeline phases (compile, coverage,
   preflight, t2m, m2m, m2t, dafny_gen, isabelle_gen, fdr4,
   dafny_verify, isabelle_verify, vacuity) report `completed` or
   `passed`.
2. The vacuity audit (phase 6d) reports zero findings.
3. FDR4 (deadlock- and divergence-freedom) passes. Determinism is not
   checked — `run_fdr4` strips the official generator's
   `:[deterministic]` assertion before invoking FDR, and
   `pipeline.yaml`'s `phases.fdr4.expected_failures` is `[]`.

If any of (1)-(3) fails, the cold pass has **not** converged. The pass
is still recorded — partial progress (e.g. "compiled but failed at
m2m") is one of the things the cold-baseline experiment is meant to
quantify.

The published K=30 result is **0 converged**. The headline message is
"single-shot cold codegen does not reach a verifiable controller — the
feedback loop is doing real work." Per-study failure breakdowns live in
[`cold-baseline/README.md`](cold-baseline/README.md) and the
per-study `cold-baseline/<study>/results-summary.md` matrices.

---

## 6. Document and commit

After a fresh K=10 batch (Path B), record the experiment context in
a dated write-up under `cold-baseline/<study>/cold-experiment-<date>.md`
covering:

- The LLM (model + version) used for the cold passes.
- The prompt bundle SHA (the `forge.assets/prompts/*.txt` plus the
  cold codegen prompt version) at the start of the batch.
- The pipeline-infrastructure commit SHA — see
  [`experiment-protocol.md`](experiment-protocol.md) §5 "Record the
  infrastructure version" for why this matters.
- The aggregate counts (compiled / reached-fdr4 / converged per study).
- Notable failure patterns surfaced by the per-run `post_*.md`
  artefacts.

For Path A (pure reproducibility check), commit the regenerated
`results-summary.md` and `README.md` aggregate-table updates only if
they actually changed — otherwise the run was a no-op.

Commit + push:

```bash
git add experiments/cold-baseline
git commit -F <message-file>
git push origin main
```

---

## 7. References

- [`HOWTO_RUN_CONVERGENCE_EXPERIMENT.md`](HOWTO_RUN_CONVERGENCE_EXPERIMENT.md)
  — the sibling guide for the convergence (iterative) experiment.
- [`experiment-protocol.md`](experiment-protocol.md) — the deeper
  methodology document (convergence criterion, hard constraints, what
  the experiment is meant to measure and rule out).
- [`cold-baseline/README.md`](cold-baseline/README.md) — published
  aggregate result + per-study breakdown.
- [`cold-baseline/scripts/run_baseline.py`](cold-baseline/scripts/run_baseline.py),
  [`cold-baseline/scripts/aggregate_baseline.py`](cold-baseline/scripts/aggregate_baseline.py)
  — the driver and aggregator.
- [`../CLAUDE.md`](../CLAUDE.md) — codegen rules, M2M conventions,
  known gotchas.
