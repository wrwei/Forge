# Experiment Scripts

Reproducibility scripts for the cold-baseline and convergence experiments.
All scripts read paths and tool locations from the parent code repository's
`pipeline.yaml` and `forge.dashboard/config.yaml`, so they need to be
run from a clone of the parent repository, not from this directory in
isolation.

## Scripts

| Script                          | Purpose                                                                                 |
| ------------------------------- | --------------------------------------------------------------------------------------- |
| [`replay_convergence.py`](replay_convergence.py)  | **Deprecated headless driver.** Spawns `claude -p` subprocesses per iter. Two documented problems (no cross-iter context recognition + no `<usage>` reporting back to the parent) make this unfit for real trajectories. Use the **Agent tool sub-agent driver** documented in [`../HOWTO_RUN_CONVERGENCE_EXPERIMENT.md`](../HOWTO_RUN_CONVERGENCE_EXPERIMENT.md) §2 Mode 1 instead. Retained here for one-shot smoke tests only. |
| [`run_experiment_iteration.py`](run_experiment_iteration.py) | Run one iteration of the convergence experiment for a study. Uses the existing iteration source plus the previous iteration's feedback. Also writes `forge.assets/corrections/phase_timings.json` for `snapshot_iter.py` to consume. |
| [`snapshot_iter.py`](snapshot_iter.py)            | Snapshot one convergence iter to `experiments/convergence/<study>/iter-<N>/`: copies Java + per-phase feedback + formal artefacts + traces, then emits a `summary.json` skeleton with phase statuses, wall-clock timings, LOC, and converged-bool pre-populated. The agent fills in only the three narrative TODO fields (`failure_summary`, `actor_codegen_summary`, `codegen_cost.*`). |
| ~~`audit_vacuity.py`~~ — **promoted to pipeline phase `vacuity`.** | Implementation moved to [`../../forge.dashboard/web/vacuity.py`](../../forge.dashboard/web/vacuity.py) (D1 + I1 checks) and wired into `pipeline.yaml` as phase 6d (`depends_on: [dafny_gen, isabelle_gen]`). Run from the dashboard, from the convergence driver (`run_experiment_iteration.py` now includes it in `PHASES_IN_ORDER`), or by name (`python experiments/scripts/run_experiment_iteration.py --phases vacuity`). Writes `post_vacuity.{md,json}` like every other phase. |

**Baseline scripts live elsewhere:** `run_baseline.py` and
`aggregate_baseline.py` are colocated with the baseline result tree at
[`../cold-baseline/scripts/`](../cold-baseline/scripts/).

## Typical invocations

```bash
# === End-to-end convergence trajectory ===
# Drives the full loop until convergence or cap; spawns claude -p per iter.
# Requires: pipeline.yaml active_case_study matches --study; claude CLI on PATH.
python experiments/scripts/replay_convergence.py --study sranger
python experiments/scripts/replay_convergence.py --study lre --max-iters 7
python experiments/scripts/replay_convergence.py --study chemical_detector --start-iter 4

# === Cold-baseline reproduction (scripts live under experiments/cold-baseline/scripts/) ===
# Reproduce one cold-baseline run for SRanger
python experiments/cold-baseline/scripts/run_baseline.py --study sranger --runs 1

# Reproduce the full K=10 cold-baseline batch for one study
python experiments/cold-baseline/scripts/run_baseline.py --study lre --runs 10

# Re-aggregate after a re-run
python experiments/cold-baseline/scripts/aggregate_baseline.py --study lre

# Run the next convergence iteration for a study from its current state
python experiments/scripts/run_experiment_iteration.py --study chemical_detector

# Vacuity audit the current pipeline output (now phase 6d — runs by name)
python experiments/scripts/run_experiment_iteration.py --phases vacuity
```

## Known limitation: `replay_convergence.py` cannot replace a developer in the loop

Each iter the headless driver spawns a **fresh** `claude -p` subprocess
(`--no-session-persistence`). The intent was that this matches the paper's
methodology of treating every iter as a fresh LLM invocation. In practice,
two reproductions of the SRanger trajectory showed that **the headless
agent cannot productively iterate against verifier feedback**:

- **v1 reproduction** (`8f235fa`): the agent achieved functional convergence
  on Dafny and Isabelle at iter 4 but FDR4 remained inconclusive because a
  separate dashboard auto-discovery bug pinned the verifier to the wrong
  CSP file. That bug is fixed (`df6fd82`).
- **v2 reproduction** (`8f235fa`, headless): the agent **never converged**.
  All 7 iters got stuck on the same `clock` reserved-word collision in the
  Isabelle theory. The thrashing detector in the feedback parser fired
  ("recurring x7 — fix strategy failing"), but the agent did not act on
  it. Most telling: every iter's `feedback/post_codegen.md` was **byte-identical**
  to iter 1's — the fresh sessions had no notion of which iter they were
  on and kept emitting the same self-report against the same broken code.

The root cause is structural: `claude -p --no-session-persistence`
discards the cross-iter conversational context that lets a developer
recognise "I tried this fix three iters ago and it didn't work; I need a
new approach". The thrashing-detection signal in `post_*.md` is necessary
but not sufficient — the agent has to **recognise** the signal and
**escalate** the response, and a stateless one-shot prompt can't do that
reliably on a small case study.

What the headless driver IS good for:

- A one-shot smoke test: "does the pipeline + claude end-to-end execute
  without errors on a fresh box?" — answer comes within ~5 minutes.
- Recording the cold-codegen output for one specific case study against
  the current claude model, for comparison with the cold-baseline batches.

What the headless driver is NOT good for:

- A reliable reproduction of a full convergence trajectory. For that, use
  the interactive Claude Code chat with the same prompts the script
  generates (the contents of `prompts/iter-1-cold.md` and
  `prompts/iter-n-feedback.md`), and let the developer mediate. The
  manual SRanger reproduction at `experiments/convergence/sranger/`
  (commit `df6fd82`) follows this path and converges in 4 iters.

For agents (LLMs) being asked to drive a convergence experiment, the
file [`experiments/HOWTO_RUN_CONVERGENCE_EXPERIMENT.md`](../HOWTO_RUN_CONVERGENCE_EXPERIMENT.md)
is the canonical entry point.

## Toolchain requirements

These scripts shell out to the same toolchain the dashboard uses:

- **Java 21+** and **Gradle 8.12+** — for the Spoon/EMF/Epsilon transformations
- **Dafny 4.x** — for the Dafny verifier (Windows install via `install-dafny-windows.ps1` in the parent repo's `scripts/`)
- **FDR4 4.2.7** — for CSP refinement checking
- **Isabelle/UTP via CyPhyAssure-2023 distribution** — for Z-machine theorem proving (Linux-only; on Windows runs under WSL Ubuntu)
- **Anthropic Claude** (Claude Code CLI) — the LLM that produces the cold and iteration codegen
