"""Vacuity audit for generated Dafny + Isabelle artefacts.

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
``runners.py`` wraps this for the pipeline-phase runner contract.
"""
from __future__ import annotations

import re
from dataclasses import dataclass, field
from pathlib import Path


@dataclass
class Finding:
    signal: str            # e.g. "D1", "I1"
    kind: str              # vacuity kind
    title: str
    artifact: str          # file path, repo-root-relative posix style
    raw: str               # the offending snippet
    fix_directive: str


@dataclass
class Report:
    status: str = "passed"   # "passed" | "failed"
    findings: list[Finding] = field(default_factory=list)

    def add(self, f: Finding) -> None:
        self.findings.append(f)
        self.status = "failed"


# ---------------------------------------------------------------------------
# Signal D1 — Dafny Valid() body is `true` / empty
# ---------------------------------------------------------------------------

_VALID_RE = re.compile(
    r"ghost\s+predicate\s+Valid\s*\(\s*\)\s*"      # ghost predicate Valid()
    r"(?:reads\s+this\s*)?"                          # optional reads this
    r"\{(?P<body>[^}]*)\}",                          # { ... } body
    re.DOTALL,
)


def _strip_dafny_body(text: str) -> str:
    # Drop line comments (`//...`) and block comments (`/* ... */`).
    text = re.sub(r"//[^\n]*", "", text)
    text = re.sub(r"/\*.*?\*/", "", text, flags=re.DOTALL)
    return text.strip()


def _count_behavioural_ensures(text: str) -> int:
    """Count active ``ensures`` clauses on transitionFromX methods that
    reference something other than ``Valid()``. Constructor
    ``ensures mode == X`` is excluded — initial-state assertions don't
    constitute behavioural verification of the controller's stepping logic.
    """
    active = re.findall(r"^\s*ensures\s+(.+)$", text, re.MULTILINE)
    count = 0
    for body in active:
        body = body.strip().rstrip(";")
        if body == "Valid()":
            continue
        if re.fullmatch(r"true|mode\s*==\s*\w+", body):
            continue
        count += 1
    return count


def check_dafny_valid_vacuous(dafny_path: Path, repo_root: Path) -> Finding | None:
    if not dafny_path.exists():
        return None
    text = dafny_path.read_text(encoding="utf-8")
    m = _VALID_RE.search(text)
    if not m:
        return None
    body = _strip_dafny_body(m.group("body"))
    if body not in {"", "true", "true;"}:
        return None  # Valid() carries non-trivial content — D1 doesn't fire.

    # Valid() is vacuous — but the verification may still be load-bearing
    # if other behavioural `ensures` clauses are active on the transition
    # methods. Only flag D1 when nothing else carries the load.
    behavioural_count = _count_behavioural_ensures(text)
    if behavioural_count > 0:
        return None  # Active behavioural obligations exist; D1 is moot.

    try:
        artifact = str(dafny_path.relative_to(repo_root).as_posix())
    except ValueError:
        artifact = str(dafny_path.as_posix())

    return Finding(
        signal="D1",
        kind="dafny_valid_vacuous",
        title="Dafny Valid() body is literally `true` (or empty) and no other behavioural ensures clauses are active",
        artifact=artifact,
        raw=m.group(0).strip(),
        fix_directive=(
            "Every `ensures Valid()` clause in the generated Dafny is "
            "equivalent to `ensures true` while Valid() returns true, AND "
            "no per-transition behavioural `ensures` clauses are active "
            "(the template emits them as comments). The Dafny verifier "
            "therefore discharges only `ensures true` obligations — "
            "`0 errors` does not establish behavioural correctness, only "
            "well-formedness.\n\n"
            "To make Dafny verification load-bearing, the M2T-Dafny "
            "template (forge.transformations/src/main/resources/"
            "transformations/java2dafny.egl) must either: "
            "(a) derive a non-trivial Valid() body from the requirements, "
            "or (b) emit per-transition `ensures` clauses as ACTIVE "
            "obligations rather than comments. (b) is the more direct "
            "route to behavioural verification."
        ),
    )


# ---------------------------------------------------------------------------
# Signal I1 — Isabelle zstore invariant is "True"
# ---------------------------------------------------------------------------

_ZSTORE_INV_RE = re.compile(
    r"zstore\s+\w+\s*=.*?where\s+inv\s*:\s*"
    r"\"(?P<inv>[^\"]*)\"",
    re.DOTALL,
)


def check_isabelle_inv_vacuous(thy_path: Path, repo_root: Path) -> Finding | None:
    if not thy_path.exists():
        return None
    text = thy_path.read_text(encoding="utf-8")
    m = _ZSTORE_INV_RE.search(text)
    if not m:
        return None
    inv = m.group("inv").strip()
    if inv != "True":
        return None

    try:
        artifact = str(thy_path.relative_to(repo_root).as_posix())
    except ValueError:
        artifact = str(thy_path.as_posix())

    return Finding(
        signal="I1",
        kind="isabelle_inv_vacuous",
        title="Isabelle zstore invariant `where inv:` is `\"True\"`",
        artifact=artifact,
        raw=f'where inv: "{inv}"',
        fix_directive=(
            "The zstore's invariant clause is `\"True\"`, which means "
            "every `xxx preserves <Store>_inv` lemma in the theory "
            "proves `True is preserved`. Isabelle's `zpog_full` tactic "
            "discharges these mechanically; the lemmas verify no "
            "behavioural content.\n\n"
            "To make Isabelle's invariant proofs load-bearing, the "
            "M2T-Isabelle template (forge.transformations/src/main/"
            "resources/transformations/thy_generation_rule.egl) should "
            "derive a non-trivial invariant from the requirements (e.g. "
            "the disjoint mode partition, `cstc >= -1`, post-CalcCPA "
            "invariants on `tcpa`/`cda`). The deadlock-freedom proof "
            "remains substantive even with `inv: True`, but the per-"
            "operation invariant lemmas do not."
        ),
    )


# ---------------------------------------------------------------------------
# Audit entry point
# ---------------------------------------------------------------------------

def audit(t2m_output: Path, repo_root: Path) -> Report:
    """Scan every ``.dfy`` under ``t2m_output`` and every ``.thy`` under
    ``t2m_output/isabelle`` for the D1 / I1 signals.

    Pure: opens files, runs regexes, returns a Report. No side effects.
    """
    report = Report()
    for dfy in sorted(t2m_output.glob("*.dfy")):
        f = check_dafny_valid_vacuous(dfy, repo_root)
        if f:
            report.add(f)
    thy_dir = t2m_output / "isabelle"
    if thy_dir.exists():
        for thy in sorted(thy_dir.glob("*.thy")):
            f = check_isabelle_inv_vacuous(thy, repo_root)
            if f:
                report.add(f)
    return report
