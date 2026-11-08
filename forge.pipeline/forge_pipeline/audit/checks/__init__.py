"""The concrete checks, one class per signal family, and the default audit.

Each class adapts one check function (kept in its own module, with the
signal's documentation) to the interface in :mod:`forge_pipeline.audit.check`.
:func:`default_audit` lists them in the order the report has always used.
"""
from __future__ import annotations

from pathlib import Path

from ..audit import Audit
from ..check import ArtefactSet, BehaviourTheory
from ..model import Finding
from . import (antecedent, constants, dafny_placeholders, domain, initial_state,
               isabelle_placeholders, reachability, satisfiability)


# -- Dafny programs ---------------------------------------------------------
class DafnyValidPlaceholder:
    """D1/D2: ``Valid()`` is ``true`` or empty."""
    signal = "D1"

    def run(self, dafny_path: Path, repo_root: Path) -> list[Finding]:
        f = dafny_placeholders.check_dafny_valid_vacuous(dafny_path, repo_root)
        return [f] if f else []


# -- theory files -----------------------------------------------------------
class IsabelleInvariantPlaceholder:
    """I1: the store invariant is ``True``."""
    signal = "I1"

    def run(self, thy_path: Path, repo_root: Path) -> list[Finding]:
        f = isabelle_placeholders.check_isabelle_inv_vacuous(thy_path, repo_root)
        return [f] if f else []


class IsabelleRequirementPlaceholder:
    """I2: a requirement predicate is literally ``True``."""
    signal = "I2"

    def run(self, thy_path: Path, repo_root: Path) -> list[Finding]:
        return isabelle_placeholders.check_isabelle_requirement_placeholder(thy_path, repo_root)


# -- behavioural theories -----------------------------------------------------
class PreconditionSatisfiable:
    """S1: every precondition can be satisfied under the invariant."""
    signal = "S1"

    def run(self, t: BehaviourTheory) -> list[Finding]:
        return satisfiability.check_precondition_unsat(t.model, t.path, t.artefacts.repo_root)


class PreconditionSatisfiableUnderEnvironment:
    """S1 (environment tier): satisfiable under some offer the environment allows."""
    signal = "S1"

    def run(self, t: BehaviourTheory) -> list[Finding]:
        return satisfiability.check_precondition_unsat_env(t.model, t.path, t.artefacts.repo_root)


class AntecedentHolds:
    """A1: no implication has an antecedent that never holds."""
    signal = "A1"

    def run(self, t: BehaviourTheory) -> list[Finding]:
        return antecedent.check_antecedent_vacuity(t.model, t.path, t.artefacts.repo_root)


class DomainsNonDegenerate:
    """E1: no empty or singleton event or parameter domain."""
    signal = "E1"

    def run(self, t: BehaviourTheory) -> list[Finding]:
        a = t.artefacts
        return domain.check_empty_domain(t.model, t.path, a.repo_root, a.instantiation, a.instantiation_path)


class InitialStateConsistent:
    """N1: the initial state satisfies the invariant."""
    signal = "N1"

    def run(self, t: BehaviourTheory) -> list[Finding]:
        return initial_state.check_initial_state_consistency(t.model, t.path, t.artefacts.repo_root)


class Reachability:
    """R1/R2, T1/T2, P1: modes, transitions and offered pairs, symbolic and narrowed.
    Attaches the verdict tables to the theory for the report."""
    signal = "R1"

    def run(self, t: BehaviourTheory) -> list[Finding]:
        findings, details = reachability.check_reachability(
            t.model, t.path, t.artefacts.repo_root, t.artefacts.checked_env)
        t.details = details
        return findings


# -- the artefact set -------------------------------------------------------
class ConstantsAgree:
    """K1: every named constant has the same value in the Java and each backend."""
    signal = "K1"

    def run(self, a: ArtefactSet) -> list[Finding]:
        return constants.check_constant_consistency(
            a.java_root, a.rct_path, a.dfy_files, a.csp_assertions, a.constant_defaults, a.repo_root)


class ArtefactsPresent:
    """An empty artefact set must not pass: zero Dafny and Isabelle files is a finding."""
    signal = "D1"

    def run(self, a: ArtefactSet) -> list[Finding]:
        if a.dfy_files or a.thy_files:
            return []
        return [Finding(
            signal="D1",
            kind="vacuity_no_artefacts",
            title="No Dafny or Isabelle artefacts found to audit",
            artifact=str(a.root),
            raw=f"glob('*.dfy') and isabelle/*.thy both empty under {a.root}",
            fix_directive=(
                "The vacuity audit found nothing to inspect. Run the "
                "dafny_gen / isabelle_gen phases first (or fix their "
                "output paths). A 'passed' vacuity audit over zero "
                "artefacts would itself be vacuous."
            ),
        )]


def default_audit() -> Audit:
    return Audit(
        dafny_checks=[DafnyValidPlaceholder()],
        theory_checks=[IsabelleInvariantPlaceholder(), IsabelleRequirementPlaceholder()],
        behaviour_checks=[PreconditionSatisfiable(), PreconditionSatisfiableUnderEnvironment(),
                          AntecedentHolds(), DomainsNonDegenerate(), InitialStateConsistent(),
                          Reachability()],
        artefact_set_checks=[ConstantsAgree(), ArtefactsPresent()],
    )
