"""Finding and Report, and the severity table.

Vacuity audit for generated Dafny + Isabelle artefacts.

A verifier "pass" only means something if the obligation it discharged was
non-trivial. The M2T-Dafny and M2T-Isabelle templates can emit boilerplate
obligations as TRUE, so "Dafny verifies; Isabelle proves 24 lemmas" can be
entirely vacuous even on broken Java.

Two specific signals (chosen during the LRE iter-1 audit):

  D1. Dafny `ghost predicate Valid()` body is literally `true` (or empty)
      AND no other behavioural `ensures` clauses are active on the
      transition methods. Every `ensures Valid()` clause is then
      equivalent to `ensures true`, so the verifier discharges no
      behavioural content.

  I1. Isabelle zstore invariant clause `where inv: "True"`. Every
      `xxx preserves <Store>_inv` lemma then proves `True is preserved`,
      which `zpog_full` discharges mechanically.

Pure-logic module: takes ``Path`` arguments, returns a ``Report``. Has no
dependency on ``REPO_ROOT`` or any process-wide state. ``run_vacuity`` in
``runners.py`` wraps this for the pipeline-phase runner contract."""
from __future__ import annotations

import re
from dataclasses import dataclass, field
from pathlib import Path


# Severity classification.
#
# BLOCKING  — the finding means a discharged obligation carries no content, or
#             that the backends verified mutually inconsistent systems. Either
#             way a "pass" is not evidence, so the iteration has not converged.
#
# ADVISORY  — the finding is a consequence of a DELIBERATE design decision of
#             the pipeline, not a property of the Java under test, and no
#             amount of codegen iteration can remove it. The CSP data domains
#             are narrowed (`core_int = {0..1}` and similar) because FDR4
#             requires finite types for tractable refinement checking; modes
#             and transitions that are reachable in the symbolic model but not
#             under that narrowing (R2/T2), and domains that are singletons
#             only because of it (E1 narrowed kinds), are the disclosed cost of
#             that choice. They are ALWAYS recorded in the run report — they
#             bound what a passing FDR4 assertion is evidence about — but they
#             do not block convergence, because blocking on them would make the
#             criterion unsatisfiable by the codegen agent.
#
# Symbolic-tier unreachability (R1/T1) is BLOCKING: it holds in the unnarrowed
# model, so it is a property of the extracted machine, not of the abstraction.
ADVISORY_KINDS = frozenset({
    "idle_unreachable",                # S1 variant: the Java `else` of a mode whose
                                       # branches are already exhaustive -- dead in the
                                       # Java too; mirrors it, not a defect
    "mode_unreachable_checked",        # R2
    "transition_unfireable_checked",   # T2
    "singleton_domain",                # E1, when induced by narrowing
    "constant_divergence_csp_ceiling", # K1, declared FloatExp workaround
    # P1, empty-offer half. A state with nothing offered is waiting (reactive
    # controller) or absorbing (specified terminal mode); neither is a defect.
    # The non-empty half (dead_state_offered_pair) stays BLOCKING — there the
    # state advertises an event it cannot act on, which is a real gap.
    "dead_state_no_offer",
    # P1, checked-tier-only half (VAC-2). A non-empty-offer pair that fires
    # symbolically but is dead at the narrowed/stubbed instantiation is the
    # narrowing's cost (R2/T2's class), not a Java gap. Blocking on it would
    # push requirement-free edits to satisfy an instrument bound.
    # dead_state_offered_pair (dead in BOTH tiers) remains BLOCKING.
    "dead_state_offered_pair_checked",
    # D2 / I2 (Amendment 21, 8 Nov 2026). Placeholders the generator templates
    # emit whatever the Java: `Valid() { true }` while the behavioural
    # obligations are the per-transition `ensures` clauses, and the
    # requirement predicate `zexpr R1 is "True"` with its `preserves R1`
    # lemmas. Reported so the obligation and lemma counts can be read for
    # what they contain; not blocking, because no code-generation iteration
    # can remove a template placeholder.
    "dafny_valid_placeholder",
    "isabelle_requirement_placeholder",
})


def classify_severity(signal: str, kind: str) -> str:
    """Return "blocking" | "advisory" for a (signal, kind) pair."""
    return "advisory" if kind in ADVISORY_KINDS else "blocking"


@dataclass
class Finding:
    signal: str            # e.g. "D1", "I1"
    kind: str              # vacuity kind
    title: str
    artifact: str          # file path, repo-root-relative posix style
    raw: str               # the offending snippet
    fix_directive: str
    severity: str = ""     # "blocking" | "advisory"; filled by Report.add()


@dataclass
class Report:
    status: str = "passed"   # "passed" | "failed"
    findings: list[Finding] = field(default_factory=list)
    # Semantic-check verdict tables (per behavioural theory): reachable mode
    # sets and per-operation fireability for both evaluation tiers. Populated
    # by audit(); serialised by the runner into post_vacuity.json so a
    # "passed" is auditable, not just asserted.
    details: dict = field(default_factory=dict)

    def add(self, f: Finding) -> None:
        if not f.severity:
            f.severity = classify_severity(f.signal, f.kind)
        self.findings.append(f)
        if f.severity == "blocking":
            self.status = "failed"

    @property
    def blocking(self) -> list[Finding]:
        return [f for f in self.findings if f.severity == "blocking"]

    @property
    def advisory(self) -> list[Finding]:
        return [f for f in self.findings if f.severity == "advisory"]


def rel(path: Path, repo_root: Path) -> str:
    try:
        return str(path.relative_to(repo_root).as_posix())
    except ValueError:
        return str(path.as_posix())


def find_first(root: Path, patterns: list[str],
                exclude_substr: str = "/timed/") -> Path | None:
    for pat in patterns:
        for p in sorted(root.rglob(pat)):
            if exclude_substr and exclude_substr in p.as_posix():
                continue
            return p
    return None
