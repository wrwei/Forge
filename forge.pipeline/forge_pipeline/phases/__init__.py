"""Phase runners implemented in Python.

A phase runner is any callable ``run(ctx: PhaseContext) -> PhaseResult``.
``pipeline.yaml`` names it as ``"<module>:<function>"`` and the
:class:`Registry` resolves that name. One module per phase:

======================  ==========================================
``phases.compile``      2a  gradle build of the generated project
``phases.coverage``     2b  requirement-to-Java trace coverage
``phases.fdr4``         6b  FDR4 refinement checking
``phases.dafny``        6a  Dafny verification
``phases.isabelle``     6c  Isabelle build
``phases.vacuity``      6d  vacuity audit
======================  ==========================================

The runners share only what is in :mod:`._common`; nothing in them knows
about the engine, the command line or a user interface. They report
through the :class:`PhaseContext` they are given.
"""
from __future__ import annotations

import importlib
from dataclasses import dataclass, field
from typing import Any, Callable, Protocol

from ..manifest import Phase
from ..paths import RepoPaths
from ..sink import ProgressSink
from ..state import SessionState
from .. import feedback

PhaseResult = tuple[str, str]   # (status, error_msg)


class PhaseRunner(Protocol):
    def __call__(self, ctx: "PhaseContext") -> PhaseResult: ...


@dataclass
class PhaseContext:
    """Everything a runner may use. Built by the engine, one per phase run."""
    phase: Phase
    config: dict[str, Any]
    state: SessionState
    paths: RepoPaths
    sys_msg: Callable[[str], None]
    stream: Callable[[str], None]
    stop_requested: Callable[[], bool]
    emit_feedback: Callable[[feedback.Feedback], None] | None = None
    sink: ProgressSink = field(default=None)  # type: ignore[assignment]

    def __post_init__(self) -> None:
        if self.sink is None:
            from ..sink import NullSink
            self.sink = NullSink()


class Registry:
    """Resolves the ``function:`` string of a ``python`` phase to a runner.

    Names of the form ``package.module:function`` are imported on demand,
    so a new phase is a new module and a line in ``pipeline.yaml``, not an
    edit here. Aliases keep older manifests working: the names the
    pipeline used before this package existed resolve to the same runners.
    """

    def __init__(self) -> None:
        self._aliases: dict[str, str] = {}

    def alias(self, old: str, new: str) -> None:
        self._aliases[old] = new

    def resolve(self, name: str) -> PhaseRunner:
        name = self._aliases.get(name, name)
        if ":" not in name:
            raise KeyError(f"phase function must be 'module:function', got {name!r}")
        mod_name, fn_name = name.split(":", 1)
        module = importlib.import_module(mod_name)
        fn = getattr(module, fn_name, None)
        if fn is None:
            raise KeyError(f"{mod_name} has no function {fn_name!r}")
        return fn


REGISTRY = Registry()
for _old, _new in {
    "forge.dashboard.web.runners:run_compile": "forge_pipeline.phases.compile:run",
    "forge.dashboard.web.runners:run_coverage": "forge_pipeline.phases.coverage:run",
    "forge.dashboard.web.runners:run_fdr4": "forge_pipeline.phases.fdr4:run",
    "forge.dashboard.web.runners:run_dafny_verify": "forge_pipeline.phases.dafny:run",
    "forge.dashboard.web.runners:run_isabelle_verify": "forge_pipeline.phases.isabelle:run",
    "forge.dashboard.web.runners:run_vacuity": "forge_pipeline.phases.vacuity:run",
}.items():
    REGISTRY.alias(_old, _new)

__all__ = ["PhaseContext", "PhaseResult", "PhaseRunner", "Registry", "REGISTRY"]
