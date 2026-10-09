# Reference experiment (Table 7)

Per-run records of the *Reference* experiment in Table 7 of the paper: five
runs per case study, each driven from a cold start until every verifier
passed or the iteration cap was reached.

All 15 runs used one fixed version of the pipeline, development commit
`b808424c` (see "Pipeline version per experiment" in the repository README for
how it differs from `main`). The runs are not pooled with any other experiment.

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
