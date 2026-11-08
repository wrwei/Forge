"""Forge pipeline: the deterministic phases that turn generated Java into
verified formal artefacts, and the reports that feed the next generation.

Entry points: ``python -m forge_pipeline run-iteration`` (every phase in
order, as the evaluation's sessions ran it), ``run-phase <id>``, and
``audit <dir>``. Programmatic use: :class:`forge_pipeline.engine.PipelineEngine`.
"""
__version__ = "1.0.0"
