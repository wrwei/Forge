# forge.pipeline

The deterministic half of Forge: given generated Java, run the extraction,
generation and verification phases that `pipeline.yaml` declares, and write
one report per phase (`forge.assets/corrections/post_<phase>.{md,json}`) for
the next generation step to read.

```bash
pip install -r requirements.txt
python -m forge_pipeline run-iteration            # every phase in order
python -m forge_pipeline run-phase fdr4           # one phase
python -m forge_pipeline audit <artefact-dir> --java-root <java-dir>
python -m forge_pipeline csp-corrections          # narrow instantiations.csp's types
python -m pytest tests
```

Run from this directory, or add it to `PYTHONPATH`; `experiments/scripts/run_experiment_iteration.py`
at the repository root is a thin wrapper around `run-iteration`.

## Layout

| Module | Responsibility |
|---|---|
| `paths.py` | the repository layout, in one place (`RepoPaths`) |
| `manifest.py` | `pipeline.yaml` parsing and validation (schema shipped alongside) |
| `state.py` | per-phase status and the message log |
| `sink.py` | `ProgressSink`: where progress goes (console, recording, nothing) |
| `engine.py` | `PipelineEngine`: runs one phase of any kind (`python`, `java`, `gradle`, `sequence`) and writes its report |
| `iteration.py` | the phase order, the pre-FDR4 correction, and which verdict counts |
| `cli.py` | the command line |
| `phases/` | one module per Python phase (`compile`, `coverage`, `fdr4`, `dafny`, `isabelle`, `vacuity`); `PhaseContext`, `Registry` |
| `feedback/` | the report schema, per-backend classifiers and the Markdown/JSON writer |
| `audit/` | the vacuity audit: `model` (Finding, Report), `check` (the check interfaces), `checks/` (one module per signal family), `sat` (the bounded evaluator) |
| `csp/` | type-range corrections for `instantiations.csp`, with the default ranges in `csp/data/` |
| `trace/` | consolidation of the per-stage trace files |

## Extending

* A new phase: a module under `phases/` with `run(ctx: PhaseContext) -> (status, error)`,
  and a `function: "forge_pipeline.phases.<name>:run"` line in `pipeline.yaml`.
* A new vacuity signal: a module under `audit/checks/`, a class implementing one of the
  interfaces in `audit/check.py`, and a line in `audit/checks/default_audit()`.
* A new front end: a class implementing `ProgressSink`.
