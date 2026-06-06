# Experiment Scripts

Reproducibility scripts for the convergence experiment. They read paths and
tool locations from the repository's `pipeline.yaml` and
`forge.dashboard/config.yaml`, so they run from a clone of the full pipeline
repository (this one) — not from `experiments/scripts/` in isolation.

## Scripts

| Script | Purpose |
| ------ | ------- |
| [`run_experiment_iteration.py`](run_experiment_iteration.py) | Run one iteration of the convergence pipeline (all 12 phases) for the active case study, against the current `java.generated.project` source plus the prior iteration's feedback. Writes the per-phase `forge.assets/corrections/post_*.{md,json}` and `phase_timings.json`. Run a single phase with `--phases <name>` (e.g. `--phases vacuity`). |
| [`snapshot_iter.py`](snapshot_iter.py) | Snapshot one iteration to `experiments/convergence/<study>/iter-<N>/`: copies the Java source + per-phase feedback + formal artefacts + traces, and emits a `summary.json` with phase statuses, wall-clocks, LOC, and the `converged` flag pre-populated (the developer fills the three narrative fields). |
| [`attribute_iter_tokens.py`](attribute_iter_tokens.py) | Post-hoc helper: split a session's token usage across iters on `snapshot_iter` boundaries to populate each `summary.json`'s `codegen_cost`. |

The canonical way to drive a trajectory is the interactive *me-as-developer*
mode described in [`../RUN_TRAJECTORY.md`](../RUN_TRAJECTORY.md) and
[`../HOWTO_RUN_CONVERGENCE_EXPERIMENT.md`](../HOWTO_RUN_CONVERGENCE_EXPERIMENT.md):
the developer edits the Java under `java.generated.project/`, runs
`run_experiment_iteration.py`, then `snapshot_iter.py`, and repeats until the
convergence criterion is met.

The **vacuity audit** is pipeline phase 6d (implementation in
[`../../forge.dashboard/web/vacuity.py`](../../forge.dashboard/web/vacuity.py),
D1 + I1 checks); run it standalone via `run_experiment_iteration.py --phases vacuity`.

**Cold-baseline scripts live elsewhere:** `run_baseline.py` and
`aggregate_baseline.py` are colocated with the baseline result tree at
[`../cold-baseline/scripts/`](../cold-baseline/scripts/).

## Typical invocations

```bash
# Run the next convergence iteration for the active study (pipeline.yaml active_case_study)
python experiments/scripts/run_experiment_iteration.py

# Run just the vacuity audit on the current pipeline output
python experiments/scripts/run_experiment_iteration.py --phases vacuity

# Snapshot the current iteration into the convergence archive
python experiments/scripts/snapshot_iter.py <study> <iter> --actor me-as-developer
```
