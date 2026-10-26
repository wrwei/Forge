# Diagnosis-only feedback configuration (Table 8)

Per-run records of the *Diagnosis-only* experiment. The runs used the
diagnosis-only feedback configuration, which is shipped here as
`diagnosis-only.patch` against the full-feedback code on `main`:

```
git apply experiments/ablation-d2-diagnostic/diagnosis-only.patch      # switch to diagnosis-only
git apply -R experiments/ablation-d2-diagnostic/diagnosis-only.patch   # back to full feedback
```

The patch removes every prescriptive fix instruction from the structural linter,
the code-generation rules the model reads and the three Phase 7 feedback
compilers (`gradle.py`, `fdr4.py`, `isabelle.py`), and keeps each diagnosis. The
condition-E Chemical Detector runs (`../multicontroller-e/`) ran with the same patch
applied, together with the per-controller generators on `main`.

With the patch applied, linting the recorded Java of
`../multicontroller-e/chemical_detector/run-4/iter-1/java` reproduces the
directives recorded in that iteration's `feedback/post_preflight.json`; without
it, linting `../convergence-v3-frozen/sranger/run-4/iter-1/java` reproduces the
Reference directives recorded there.

## Isabelle theories

In each run's converged iteration, `formal-artefacts/isabelle/` holds the theory
the published generator produces from that iteration's `robochart_model.xmi`,
with its Isabelle build log under `build-logs/` (all verify; see
`../environment-model/results/2026-10-26-tables7-8-regenerated/`). The theory
collected while the run was in progress is kept beside it in
`formal-artefacts/isabelle-collected/`; the run's recorded verdict in
`feedback/post_isabelle_verify.md` refers to that collected theory.
