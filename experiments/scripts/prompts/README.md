# Driver-internal prompt templates

This directory only holds prompt templates that are specific to the
convergence-experiment harness. Everything else — codegen rules,
chain-of-thought guides, few-shot examples, FDR4 interpretation guide —
lives at its canonical location under
[`../../../forge.assets/prompts/`](../../../forge.assets/prompts/) and
the experiment templates reference those files directly.

| File | Purpose |
| ---- | ------- |
| [`iter-1-cold.md`](iter-1-cold.md)             | Cold-codegen prompt template used by the (deprecated) `replay_convergence.py` headless driver for iter 1 |
| [`iter-n-feedback.md`](iter-n-feedback.md)     | Feedback-driven refinement template used by the same driver for iter ≥ 2 |

These two templates are for the legacy headless driver. The canonical
Agent-tool sub-agent flow (see [`../../HOWTO_RUN_CONVERGENCE_EXPERIMENT.md`](../../HOWTO_RUN_CONVERGENCE_EXPERIMENT.md)
§2 Mode 1) builds prompts from these templates and points each sub-agent
at the live `forge.assets/{case-studies,prompts}/` paths for inputs.
