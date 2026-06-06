# Cold Baseline Experiment (K=10 per study)

Answers the question: **how often does a single-shot cold codegen converge without iteration?**

A *cold pass* is identical to iteration 1 of the convergence experiment:
fresh LLM invocation given the case-study specification, codegen prompt
assets, project conventions, and RAG-corpus access; no prior verifier
feedback and no existing iteration source. Java output is run through
the pipeline plus the vacuity audit **exactly once** — no iteration,
no second LLM invocation.

Protocol: [`../experiment-protocol.md`](../experiment-protocol.md).

## Aggregate result

**0 of 30** cold runs converged. The feedback loop is doing real work.

| Study              | Compiled | Reached FDR4 | Converged cold |
| ------------------ | -------: | -----------: | -------------: |
| [LRE](lre/)                              | 9/10  | 10/10  | **0/10** |
| [Chemical Detector](chemical_detector/)  | 0/10  | 10/10\* | **0/10** |
| [SRanger](sranger/)                      | 10/10 | 10/10  | **0/10** |

\* Chemical Detector cold runs fail Java compile but the pipeline continues
into FDR4 against the partial output.

## Where the cold runs fail (finer-grained finding)

Different case studies fail at different phases — useful evidence that
the feedback signal carries real content beyond "the code didn't compile":

- **LRE**: 9/10 compile, all reach Dafny, but **Dafny verification fails on all 10** (every per-mode postcondition is violated by the cold output). A compile-only feedback loop would be useless here.
- **SRanger**: 10/10 compile, all reach every verifier; Dafny and Isabelle fail on all 10 (cold runs consistently produce a `clock` reserved-word collision), but FDR4 passes on 10/10. Different verifiers catch different defects.
- **Chemical Detector**: All 10 fail Java compile (cold codegen consistently mis-handles the multi-controller dispatch). Even the basic compile-error loop would only get this study off the floor.

Per-run detail is in `<study>/results-summary.md` and the
per-phase `post_*.md` artefacts under `<study>/run-N/`.

## Reproducing

See [`../HOWTO_RUN_COLD_BASELINE_EXPERIMENT.md`](../HOWTO_RUN_COLD_BASELINE_EXPERIMENT.md)
for the full agent-facing run guide. It covers two paths: re-running
the pipeline against the K=30 committed sources here, and generating
fresh K=10 batches with a different LLM / prompt bundle.

The two scripts in this directory's `scripts/` subdir, used by the
HOWTO and invoked from the repo root:

```bash
# Replay the pipeline against one already-committed cold source.
python experiments/cold-baseline/scripts/run_baseline.py <study> <run_number>

# Re-aggregate results-summary.md per study + this README's headline table.
python experiments/cold-baseline/scripts/aggregate_baseline.py
```

Convergence experiment scripts live separately at
[`../scripts/`](../scripts/); see [`../scripts/README.md`](../scripts/README.md).
