# Reference experiment (Table 8)

Per-run records of the *Reference* experiment in Table 8 of the paper: five
runs per case study, each driven from a cold start until every verifier
passed or the iteration cap was reached.

All 15 runs used one fixed version of the pipeline, development commit
`b808424c` (see "Pipeline version per experiment" in the repository README for
how it differs from `main`). The runs are not pooled with any other experiment.

Of these, the SRanger and LRE runs are the Reference rows of Table 8. The
Chemical Detector runs here verified the first controller only; Table 8's
Reference Chemical Detector row is instead `../reference-multicontroller-f/`,
collected on the published pipeline with both controllers verified. The five
Chemical Detector runs here are kept as recorded and are not reported in the
paper.

## Layout

```
<study>/run-<n>/
  actor.json           which agent drove the run, and under which condition
  iter-<k>/
    summary.json       per-phase verdicts for that iteration
    feedback/          the verifier feedback the iteration produced
    traces/            the requirement-to-code traces
    agent_summary.md   the agent's own note on the iteration
    java/              the generated Java at that iteration
    formal-artefacts/  the extracted Dafny, CSP and Isabelle artefacts
```

A run converged at the first iteration whose `summary.json` records every
decisive phase (preflight, vacuity, Dafny, FDR4, Isabelle) as passed.

## Results

| Case study | Iterations to converge (runs 1-5) |
| ---------- | --------------------------------- |
| SRanger | 1, 1, 1, 1, 1 |
| LRE | 4, 2, 2, 1, 1 |
| Chemical Detector | 1, 1, 1, 1, 1 |

## Isabelle theories

In each run's converged iteration, `formal-artefacts/isabelle/` holds the theory
the published generator produces from that iteration's `robochart_model.xmi`,
with its Isabelle build log under `build-logs/` (all verify; see
`../environment-model/results/2026-10-26-tables7-8-regenerated/`). The theory
collected while the run was in progress is kept beside it in
`formal-artefacts/isabelle-collected/`; the run's recorded verdict in
`feedback/post_isabelle_verify.md` refers to that collected theory.
