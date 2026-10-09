# All-controller experiment (Table 7)

Per-run records of the *All-controller* experiment in Table 7 of the paper:
five Chemical Detector runs in which Dafny and Isabelle check both of the
system's controllers (GasAnalysis and Movement), not only the first.

All 5 runs used one fixed version of the pipeline, development commit
`99952b9b`, with the diagnosis-only feedback configuration applied
(`../ablation-d2-diagnostic/diagnosis-only.patch`). See "Pipeline version per
experiment" in the repository README. The runs are not pooled with any other
experiment.

## Layout

```
chemical_detector/run-<n>/
  actor.json           which agent drove the run, and under which condition
  setup_ok.json        the pre-run checks and the recorded pipeline version
  iter-<k>/
    summary.json       per-phase verdicts for that iteration
    feedback/          the verifier feedback the iteration produced
    traces/            the requirement-to-code traces
    agent_summary.md   the agent's own note on the iteration
    java/              the generated Java at that iteration
    formal-artefacts/  one Dafny program and one Isabelle theory per controller
```

## Results

| Case study | Iterations to converge (runs 1-5) |
| ---------- | --------------------------------- |
| Chemical Detector | 1, 1, 3, 2, 1 |
