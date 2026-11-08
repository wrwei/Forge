"""The vacuity audit (pipeline Phase 6d).

``audit(t2m_output, repo_root, java_root=..., type_ranges_path=...)`` returns a
:class:`Report`. The check functions are re-exported here so callers and tests
can address them by their original names.
"""
from __future__ import annotations

from .model import ADVISORY_KINDS, Finding, Report, classify_severity
from .audit import Audit, audit, collect_artefacts
from .check import ArtefactSet, BehaviourTheory
from .checks.dafny_placeholders import check_dafny_valid_vacuous
from .checks.isabelle_placeholders import (check_isabelle_inv_vacuous,
                                           check_isabelle_requirement_placeholder)
from .checks.satisfiability import check_precondition_unsat, check_precondition_unsat_env
from .checks.antecedent import check_antecedent_vacuity
from .checks.domain import check_empty_domain
from .checks.initial_state import check_initial_state_consistency
from .checks.reachability import check_reachability
from .checks.constants import check_constant_consistency

__all__ = [
    "ADVISORY_KINDS", "Finding", "Report", "classify_severity", "Audit", "audit",
    "collect_artefacts", "ArtefactSet", "BehaviourTheory",
    "check_dafny_valid_vacuous", "check_isabelle_inv_vacuous",
    "check_isabelle_requirement_placeholder", "check_precondition_unsat",
    "check_precondition_unsat_env", "check_antecedent_vacuity", "check_empty_domain",
    "check_initial_state_consistency", "check_reachability", "check_constant_consistency",
]
