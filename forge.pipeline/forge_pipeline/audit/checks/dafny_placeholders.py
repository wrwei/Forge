"""Signals D1 and D2: the Dafny class invariant ``Valid()`` is ``true`` or empty."""
from __future__ import annotations

import json
import math
import re
from pathlib import Path

from .. import sat
from ..model import Finding, rel as _rel


_VALID_RE = re.compile(
    r"(?:ghost\s+)?predicate\s+Valid\s*\(\s*\)\s*"   # [ghost] predicate Valid()
    r"(?:reads\s+[^{]*?)?"                           # optional reads clause (any frame)
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

    Comments are stripped BEFORE matching: ``ensures true  // note`` used
    to fail the ``true`` exclusion because of the trailing comment and was
    counted as behavioural (false pass — the archived LRE lemmas carry
    exactly that shape). ``this.mode == X`` is excluded like ``mode == X``.
    """
    active = re.findall(r"^\s*ensures\s+(.+)$", _strip_dafny_body(text), re.MULTILINE)
    count = 0
    for body in active:
        body = body.strip().rstrip(";").strip()
        if body == "Valid()":
            continue
        if re.fullmatch(r"true|(?:this\.)?mode\s*==\s*\w+", body):
            continue
        count += 1
    return count


def check_dafny_valid_vacuous(dafny_path: Path, repo_root: Path) -> Finding | None:
    if not dafny_path.exists():
        return None
    text = dafny_path.read_text(encoding="utf-8")
    m = _VALID_RE.search(text)
    if not m:
        # False-pass audit 2026-08: "no Valid() predicate" used to return
        # None (pass). But a template drift that drops Valid() entirely
        # leaves the same vacuity as `Valid() { true }` — unless other
        # behavioural ensures carry the load, which is checked below.
        if _count_behavioural_ensures(text) > 0:
            return None
        try:
            artifact = str(dafny_path.relative_to(repo_root).as_posix())
        except ValueError:
            artifact = str(dafny_path.as_posix())
        return Finding(
            signal="D1",
            kind="dafny_valid_vacuous",
            title="No Valid() predicate found and no behavioural ensures clauses are active",
            artifact=artifact,
            raw="(no `predicate Valid()` matched; no behavioural `ensures` found)",
            fix_directive=(
                "The generated Dafny has neither a Valid() invariant "
                "predicate nor active behavioural `ensures` clauses, so "
                "`0 errors` from the verifier establishes well-formedness "
                "only. Check the M2T-Dafny template (java2dafny.egl) — "
                "either Valid() was dropped or renamed, or the "
                "per-transition ensures clauses are emitted as comments."
            ),
        )
    body = _strip_dafny_body(m.group("body"))
    if body not in {"", "true", "true;"}:
        return None  # Valid() carries non-trivial content — D1 doesn't fire.

    def artifact_of(path: Path) -> str:
        try:
            return str(path.relative_to(repo_root).as_posix())
        except ValueError:
            return str(path.as_posix())
    artifact = artifact_of(dafny_path)

    # Valid() is vacuous — but the verification may still be load-bearing
    # if other behavioural `ensures` clauses are active on the transition
    # methods. Only flag D1 when nothing else carries the load.
    behavioural_count = _count_behavioural_ensures(text)
    if behavioural_count > 0:
        # D1 is moot, but the class invariant is still a placeholder: say so
        # (Amendment 21). Advisory; the obligations live in the ensures clauses.
        return Finding(
            signal="D2",
            kind="dafny_valid_placeholder",
            title="Dafny Valid() body is literally `true`; the obligations are the per-transition ensures clauses",
            artifact=artifact_of(dafny_path),
            raw=f"predicate Valid() {{ {body or '(empty)'} }}  /* {behavioural_count} behavioural ensures clauses active */",
            fix_directive=(
                "The generated class invariant `Valid()` is `true`, so every "
                "`ensures Valid()` is `ensures true` and carries no content. "
                f"The {behavioural_count} behavioural `ensures` clauses on the "
                "transition methods are the obligations Dafny discharges; read "
                "the obligation count as those clauses, not as an invariant proof. "
                "Advisory: a generator template placeholder, not a property of the Java."
            ),
        )

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
