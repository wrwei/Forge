"""The interface every vacuity check implements, and what it is given.

A check is a small object with a ``signal`` (the code the report prints,
``D1``, ``I2``, ``S1`` ...) and a ``run`` method. Checks differ in what they
look at, so there are four kinds of ``run``, one per scope; the
:class:`~forge_pipeline.audit.audit.Audit` calls each kind at the right
point of its loop and never needs to know which concrete checks exist.

* :class:`DafnyCheck`  — one Dafny program at a time;
* :class:`TheoryCheck` — one Isabelle theory file at a time (syntactic);
* :class:`BehaviourCheck` — one parsed behavioural theory at a time, with
  the instantiation the CSP route checks under;
* :class:`ArtefactSetCheck` — the whole artefact set once.

All return a list of :class:`~forge_pipeline.audit.model.Finding`; an
empty list means nothing to report.
"""
from __future__ import annotations

from dataclasses import dataclass, field
from pathlib import Path
from typing import Protocol, runtime_checkable

from . import sat
from .model import Finding


@dataclass
class ArtefactSet:
    """Everything the audit found under one output directory."""
    root: Path
    repo_root: Path
    java_root: Path | None
    dfy_files: list[Path]
    thy_files: list[Path]
    beh_files: list[Path]
    csp_assertions: list[Path]
    instantiation_path: Path | None
    instantiation: sat.Instantiation | None
    rct_path: Path | None
    rct_consts: dict | None
    constant_defaults: Path | None
    checked_env: sat.Env | None


@dataclass
class BehaviourTheory:
    """One parsed ``*_Beh.thy`` with the environment it is checked under."""
    path: Path
    model: sat.TheoryModel
    artefacts: ArtefactSet
    details: dict | None = None   # verdict tables a check may attach for the report


@runtime_checkable
class DafnyCheck(Protocol):
    signal: str
    def run(self, dafny_path: Path, repo_root: Path) -> list[Finding]: ...


@runtime_checkable
class TheoryCheck(Protocol):
    signal: str
    def run(self, thy_path: Path, repo_root: Path) -> list[Finding]: ...


@runtime_checkable
class BehaviourCheck(Protocol):
    signal: str
    def run(self, theory: BehaviourTheory) -> list[Finding]: ...


@runtime_checkable
class ArtefactSetCheck(Protocol):
    signal: str
    def run(self, artefacts: ArtefactSet) -> list[Finding]: ...
