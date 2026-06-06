# Experiments — Companion Artefacts

This directory contains the **experiment data and reproducibility scripts**
for the empirical claims in the FORGE paper. Two experiments are documented:

| Experiment            | Question answered                                                      | Where                        |
| --------------------- | ---------------------------------------------------------------------- | ---------------------------- |
| **Convergence**       | How many iterations does the feedback loop need per case study?        | [`convergence/`](convergence/)         |
| **Cold baseline (K=10)** | How often does a single-shot cold codegen converge without iteration? | [`cold-baseline/`](cold-baseline/)     |

> **Running the experiments yourself?** Two run guides live next to this
> README — both are written so an LLM agent (or a human) can be pointed
> at one and produce a faithful experiment record end-to-end:
>
> - **Convergence (iterative)** —
>   [`HOWTO_RUN_CONVERGENCE_EXPERIMENT.md`](HOWTO_RUN_CONVERGENCE_EXPERIMENT.md),
>   with the answer-free single-trajectory runbook
>   [`RUN_TRAJECTORY.md`](RUN_TRAJECTORY.md). The per-iteration driver scripts
>   are in [`scripts/`](scripts/) (see [`scripts/README.md`](scripts/README.md)).
> - **Cold baseline (no feedback)** —
>   [`HOWTO_RUN_COLD_BASELINE_EXPERIMENT.md`](HOWTO_RUN_COLD_BASELINE_EXPERIMENT.md).
>   Covers both reproducing the K=30 committed runs and generating fresh
>   cold passes with a different LLM / prompt bundle.

## Headline results

### Convergence — feedback loop terminates for all three studies

**Fifteen independent runs** (three studies × five runs), each driven in its own
session under Condition B (honest-independent). **All fifteen converged**;
median **2** iterations (range **1–3**). Final verdict for every run: Dafny +
FDR4 + Isabelle all pass with a non-vacuous (D1/I1) result.

| Study             | run-1 | run-2 | run-3 | run-4 | run-5 | median |
| ----------------- | :---: | :---: | :---: | :---: | :---: | :----: |
| LRE               |   2   |   2   |   3   |   2   |   3   | **2**  |
| Chemical Detector |   2   |   2   |   3   |   2   |   1   | **2**  |
| SRanger           |   2   |   2   |   2   |   2   |   2   | **2**  |

(Cell = iterations to convergence.) Per-run trajectories are under
[`convergence/<study>/run-N/`](convergence/); the deduplicated findings/caveats
across all runs are in
[`convergence/CONVERGENCE_FINDINGS_KNOWLEDGEBASE.md`](convergence/CONVERGENCE_FINDINGS_KNOWLEDGEBASE.md).

### Cold baseline — feedback loop is necessary

**0 of 30** single-shot cold codegens converged. Different studies fail at different phases:

| Study              | Compiled | Reached FDR4 | Converged cold |
| ------------------ | -------: | -----------: | -------------: |
| LRE                | 9/10     | 10/10        | **0/10**       |
| Chemical Detector  | 0/10     | 10/10\*      | **0/10**       |
| SRanger            | 10/10    | 10/10        | **0/10**       |
| **Total**          | 19/30    | 30/30        | **0/30**       |

\* Chemical Detector cold runs fail Java compile but the pipeline continues into FDR4 against the partial output. Per-run logs in [`cold-baseline/<study>/run-N/`](cold-baseline/).

## Layout

```
experiments/
  README.md                              ← this file
  experiment-protocol.md                 ← convergence criterion + hard constraints + baselines
  RUN_TRAJECTORY.md                      ← answer-free runbook for driving one convergence trajectory
  HOWTO_RUN_CONVERGENCE_EXPERIMENT.md     ← convergence run guide (agent or human)
  HOWTO_RUN_COLD_BASELINE_EXPERIMENT.md   ← cold-baseline (K=10) run guide
  convergence/
    README.md                            ← convergence results + per-run index
    CONVERGENCE_FINDINGS_KNOWLEDGEBASE.md ← deduplicated findings/caveats (original run numbering)
    <study>/run-1 ... run-5/             ← per run: trajectory.md + iter-*/ (summary.json, feedback, formal-artefacts, traces, java)
  cold-baseline/
    README.md
    <study>/run-1 ... run-10/            ← raw artefacts per cold run (Java + per-phase post_*.md)
  scripts/                               ← reproducibility scripts (see scripts/README.md)
```

## License

MIT.
