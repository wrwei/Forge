# Experiments — Companion Artefacts

This directory contains the **experiment data and reproducibility scripts**
for the empirical claims in the FORGE paper. Each experiment has its own folder
with a README.

| Experiment | Paper | Folder |
| ---------- | ----- | ------ |
| Convergence: iterations the feedback loop needs per case study | Section 4.6, Table 7 | [`convergence/`](convergence/) |
| Compile-only ablation of the convergence runs (as collected and scored at submission) | response letter | [`convergence/ablation/`](convergence/ablation/) |
| Compile-only stop points re-scored by the published extractor | Section 4.7 (RQ5), Table 9 | [`convergence/ablation-replay/`](convergence/ablation-replay/) |
| Cold baseline (K=10): the 30 single-shot sources, as collected and scored at submission | response letter | [`cold-baseline/`](cold-baseline/) |
| Cold baseline re-scored by the published extractor; best-of-N selection | Section 4.5 (RQ3), Tables 5 and 6 | [`cold-baseline-replay/`](cold-baseline-replay/) |
| Reference: convergence at one fixed pipeline version | Section 4.6, Table 8 | [`convergence-v3-frozen/`](convergence-v3-frozen/) |
| Diagnosis-only: feedback with every prescriptive fix removed | Section 4.6, Table 8 | [`ablation-d2-diagnostic/`](ablation-d2-diagnostic/) |
| Diagnosis-only feedback, Chemical Detector, both controllers (condition E): five cold runs | Section 4.6, Table 8 (Diagnosis-only Chemical Detector row) | [`multicontroller-e/`](multicontroller-e/) |
| Reference feedback, Chemical Detector, both controllers (condition F): five cold runs | Section 4.6, Table 8 (Reference Chemical Detector row); Section 5.6 (f), (m) | [`reference-multicontroller-f/`](reference-multicontroller-f/) |
| Test-and-static-analysis feedback in place of verifier feedback | Section 4.7 (RQ5) | [`test-feedback/`](test-feedback/) |
| Mutation testing: 186 mutants through every verifier (34 outside the boundary, 3 killed, 15 rejected, 134 survived) | Section 4.8 (RQ6), Table 10 | [`mutation-testing/`](mutation-testing/) |
| Final-mode divergence between the CSP and Isabelle chains | Section 4.4 (RQ2) | [`final-mode-divergence/`](final-mode-divergence/) |
| Requirements-conformance checks: compiled relations for all three studies, survivor scoring | Section 4.9 (RQ7) | [`requirements-conformance/`](requirements-conformance/) |
| Independently authored reference specification (LRE), survivor scoring | Section 4.9.1 | [`blind-spec/`](blind-spec/) |
| Steam Boiler: an independent published specification | Section 4.10 (RQ8) | [`steam-boiler/`](steam-boiler/) |
| Guard-level conformance enumeration | Section 5.5, Figure 2 | [`conformance-tierB/`](conformance-tierB/) |

The headline results of the first two experiments are summarised below; the
other folders report their own.

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
[`convergence/<study>/run-N/`](convergence/).

### Cold baseline — feedback loop is necessary

**0 of 30** single-shot cold codegens converged. Different studies fail at different phases:

| Study              | Compiled | Reached FDR4 | Converged cold |
| ------------------ | -------: | -----------: | -------------: |
| LRE                | 9/10     | 10/10        | **0/10**       |
| Chemical Detector  | 0/10     | 10/10\*      | **0/10**       |
| SRanger            | 10/10    | 10/10        | **0/10**       |
| **Total**          | 19/30    | 30/30        | **0/30**       |

\* At submission time the Chemical Detector cold runs failed Java compile (a JVM memory error on the Windows host that ran them; nine of the ten compile in `cold-baseline-replay/`) and the pipeline continued into FDR4 against the partial output. Per-run logs in [`cold-baseline/<study>/run-N/`](cold-baseline/).

## Layout

```
experiments/
  README.md                              <- this file
  experiment-protocol.md                 <- convergence criterion + hard constraints + baselines
  RUN_TRAJECTORY.md                      <- answer-free runbook for driving one convergence trajectory
  HOWTO_RUN_CONVERGENCE_EXPERIMENT.md     <- convergence run guide (agent or human)
  HOWTO_RUN_COLD_BASELINE_EXPERIMENT.md   <- cold-baseline (K=10) run guide
  convergence/                           <- Table 7 runs; ablation/ holds the Table 9 runs
  cold-baseline/                         <- the 30 cold sources, with the submission-time extractor's verdicts
  cold-baseline-replay/                  <- the same sources re-scored by the published extractor: Tables 5 and 6
  convergence-v3-frozen/                 <- Table 8, Reference
  ablation-d2-diagnostic/                <- Table 8, Diagnosis-only (and diagnosis-only.patch)
  multicontroller-e/                     <- Table 8, Diagnosis-only Chemical Detector row (condition E)
  reference-multicontroller-f/           <- Table 8, Reference Chemical Detector row (condition F)
  test-feedback/                         <- RQ5
  mutation-testing/                      <- RQ6
  requirements-conformance/              <- RQ7
  blind-spec/                            <- Section 4.9.1
  steam-boiler/                          <- RQ8
  conformance-tierB/                     <- Section 5.5, Figure 2
  scripts/                               <- reproducibility scripts (see scripts/README.md)
```

## License

Reviewer evaluation only; see [`../LICENSE`](../LICENSE).
