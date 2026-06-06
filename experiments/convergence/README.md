# Convergence Experiment

Answers the question: **how many iterations of the feedback loop are needed
for each case study to reach the strict convergence criterion?**

**Convergence criterion** (from [`../experiment-protocol.md`](../experiment-protocol.md)):

> Every one of the 12 pipeline phases reports `passed`/`completed` AND the
> vacuity audit reports zero findings. Concretely: Dafny verifies with 0
> errors; FDR4 proves deadlock- and divergence-freedom (determinism is **not**
> checked — `run_fdr4` strips the `:[deterministic]` assertion); Isabelle
> proves the deadlock-freedom lemma plus all per-operation invariant lemmas;
> and the D1/I1 vacuity signals are non-trivial.

Each iteration is a fresh codegen + full-pipeline pass; from iteration 2 onward
the only differentiator is the prior iteration's `post_*` feedback. See
[`../RUN_TRAJECTORY.md`](../RUN_TRAJECTORY.md) (the single answer-free runbook
for driving one trajectory) and
[`../HOWTO_RUN_CONVERGENCE_EXPERIMENT.md`](../HOWTO_RUN_CONVERGENCE_EXPERIMENT.md).

## Result: fifteen independent runs — median 2 iterations (range 1–3)

The headline result is **fifteen independent runs** (three studies × five runs),
each driven in its own session under Condition B (honest-independent), archived
under `<study>/run-{1..5}/`. **All fifteen converged.**

| Study | run-1 | run-2 | run-3 | run-4 | run-5 | range | median |
|-------|:----:|:----:|:----:|:----:|:----:|:-----:|:-----:|
| LRE | 2 | 2 | 3 | 2 | 3 | 2–3 | **2** |
| chemical_detector | 2 | 2 | 3 | 2 | 1 | 1–3 | **2** |
| sranger | 2 | 2 | 2 | 2 | 2 | 2–2 | **2** |

(Cell = iterations to convergence.) Each run's per-iteration narrative, the
per-phase pass/fail table, and its durable findings/caveats live in that run's
own [`<study>/run-N/trajectory.md`](.); the mechanical per-iter data (phase
wall-clocks, `phase_results`, `converged`) is in each
`<study>/run-N/iter-*/summary.json`.

The deduplicated, provenance-attributed aggregation of every durable
finding/caveat mined across all runs is in
[`CONVERGENCE_FINDINGS_KNOWLEDGEBASE.md`](CONVERGENCE_FINDINGS_KNOWLEDGEBASE.md).

> ⚠️ **Run-numbering note.** The knowledge base uses the **original experiment
> run numbering** — its per-run blocks and cross-references span the full run
> history (including re-runs/redos). In this artifact the published `run-1..5`
> per study correspond to original `run-4..8`; the KB's references to other run
> numbers are historical. It is a write-only findings sink (per
> [`../RUN_TRAJECTORY.md`](../RUN_TRAJECTORY.md) §4/§6) — not to be read or
> injected as context while driving a run.

## Reproducing

From the repo root:

```bash
# Drive one trajectory end-to-end (see RUN_TRAJECTORY.md for the protocol)
python experiments/scripts/run_experiment_iteration.py

# Run the vacuity audit on a study's current Dafny + Isabelle output
python experiments/scripts/run_experiment_iteration.py --phases vacuity
```

See [`../scripts/README.md`](../scripts/README.md) for the full script reference.
To re-stage a converged iteration's source and re-run the pipeline against it,
follow the "Reproducibility" section in the relevant `run-N/trajectory.md`.
