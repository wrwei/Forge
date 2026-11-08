"""The audit: a list of checks run over one artefact set, producing a Report.

The order is the order the report has always had: for each Dafny program
the Dafny checks; for each theory the syntactic theory checks; for each
behavioural theory the behaviour checks; then the artefact-set checks.
Adding a signal means adding a class to the right list in
:func:`default_audit`; nothing in :class:`Audit` changes.
"""
from __future__ import annotations

from dataclasses import dataclass, field
from pathlib import Path

from . import sat
from .check import (ArtefactSet, ArtefactSetCheck, BehaviourCheck, BehaviourTheory,
                    DafnyCheck, TheoryCheck)
from .model import Finding, Report, find_first, rel

DEFAULT_TYPE_RANGES = Path(__file__).resolve().parent.parent / "csp" / "data" / "type_ranges.json"


def collect_artefacts(t2m_output: Path, repo_root: Path, java_root: Path | None,
                      type_ranges_path: Path | None) -> ArtefactSet:
    """Scan ``t2m_output`` recursively; both the pipeline layout
    (``output/``, ``output/isabelle/``, ``output/csp-gen/``) and the archive
    layout (``formal-artefacts/{dafny,isabelle,csp}``) work."""
    dfy_files = sorted(t2m_output.rglob("*.dfy"))
    thy_files = sorted(t2m_output.rglob("*.thy"))
    inst_path = find_first(t2m_output, ["instantiations.csp"])
    inst = (sat.parse_instantiations(inst_path.read_text(encoding="utf-8", errors="replace"))
            if inst_path else None)
    rct_path = find_first(t2m_output, ["robochart_controller.rct", "*.rct"])
    grid = sat.load_type_ranges(type_ranges_path or DEFAULT_TYPE_RANGES)
    rct_consts = (sat.parse_rct_consts(rct_path.read_text(encoding="utf-8", errors="replace"))
                  if rct_path else None)
    checked_env = (sat.build_checked_env(inst, rct_consts, grid)
                   if (inst is not None or rct_consts) else None)
    return ArtefactSet(
        root=t2m_output, repo_root=repo_root, java_root=java_root,
        dfy_files=dfy_files, thy_files=thy_files,
        beh_files=[p for p in thy_files if p.name.endswith("_Beh.thy")],
        csp_assertions=sorted(t2m_output.rglob("file_*_coreassertions.csp")),
        instantiation_path=inst_path, instantiation=inst,
        rct_path=rct_path, rct_consts=rct_consts,
        constant_defaults=find_first(t2m_output, ["constant_defaults.json"]),
        checked_env=checked_env,
    )


@dataclass
class Audit:
    dafny_checks: list[DafnyCheck] = field(default_factory=list)
    theory_checks: list[TheoryCheck] = field(default_factory=list)
    behaviour_checks: list[BehaviourCheck] = field(default_factory=list)
    artefact_set_checks: list[ArtefactSetCheck] = field(default_factory=list)

    def run(self, artefacts: ArtefactSet) -> Report:
        report = Report()
        for dfy in artefacts.dfy_files:
            for check in self.dafny_checks:
                for f in check.run(dfy, artefacts.repo_root):
                    report.add(f)
        for thy in artefacts.thy_files:
            for check in self.theory_checks:
                for f in check.run(thy, artefacts.repo_root):
                    report.add(f)

        report.details = {}
        for thy in artefacts.beh_files:
            model = sat.parse_theory(thy.read_text(encoding="utf-8", errors="replace"))
            if not model.operations:
                continue
            theory = BehaviourTheory(path=thy, model=model, artefacts=artefacts)
            for check in self.behaviour_checks:
                for f in check.run(theory):
                    report.add(f)
            if theory.details is not None:
                report.details[rel(thy, artefacts.repo_root)] = theory.details

        for check in self.artefact_set_checks:
            for f in check.run(artefacts):
                report.add(f)
        return report


def audit(t2m_output: Path, repo_root: Path, java_root: Path | None = None,
          type_ranges_path: Path | None = None) -> Report:
    """Full vacuity audit over one study's generated artefacts.

    Pure: opens files, runs parsers and the bounded evaluator, returns a
    Report. The semantic verdict tables are attached as ``report.details``
    for the runner to serialise.
    """
    from .checks import default_audit
    artefacts = collect_artefacts(t2m_output, repo_root, java_root, type_ranges_path)
    return default_audit().run(artefacts)
