"""Signals I1 and I2: the Isabelle store invariant is ``True``; a requirement predicate is literally ``True``."""
from __future__ import annotations

import json
import math
import re
from pathlib import Path

from .. import sat
from ..model import Finding, rel as _rel


# Split the theory into per-zstore blocks so the inv clause is checked
# WITHIN its own zstore. The old single `zstore ... .*? where inv` DOTALL
# regex could pair zstore A (no inv) with zstore B's inv clause, and a
# zstore with NO inv clause at all was never flagged even though
# Z-Machines defaults a missing inv to True (same vacuity).
_ZSTORE_BLOCK_RE = re.compile(
    r"zstore\s+(?P<name>\w+)\s*=(?P<block>.*?)"
    r"(?=\bzstore\s|\bzoperation\s|\bzmachine\s|\bsubsection\b|\bsection\b|\Z)",
    re.DOTALL,
)


_INV_CLAUSE_RE = re.compile(r"where\s+inv\s*:\s*\"(?P<inv>[^\"]*)\"", re.DOTALL)


def check_isabelle_inv_vacuous(thy_path: Path, repo_root: Path) -> Finding | None:
    if not thy_path.exists():
        return None
    text = thy_path.read_text(encoding="utf-8")

    try:
        artifact = str(thy_path.relative_to(repo_root).as_posix())
    except ValueError:
        artifact = str(thy_path.as_posix())

    for zm in _ZSTORE_BLOCK_RE.finditer(text):
        inv_m = _INV_CLAUSE_RE.search(zm.group("block"))
        if inv_m is None:
            return Finding(
                signal="I1",
                kind="isabelle_inv_vacuous",
                title=f"Isabelle zstore {zm.group('name')} has no `where inv:` clause (defaults to True)",
                artifact=artifact,
                raw=f"zstore {zm.group('name')} = ... (no inv clause)",
                fix_directive=(
                    "The zstore declares no invariant clause; Z-Machines "
                    "defaults the invariant to True, so every "
                    "`preserves <Store>_inv` lemma proves `True is "
                    "preserved`. Derive a non-trivial invariant in the "
                    "M2T-Isabelle template."
                ),
            )
        inv = inv_m.group("inv").strip()
        if inv == "True":
            break  # vacuous inv found — fall through to the Finding below
    else:
        return None

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
            "For the invariant lemmas to check anything, the theory "
            "generator (forge.transformations/src/main/resources/"
            "transformations/thy_generation_rule.egl) has to emit a "
            "non-trivial invariant derived from the requirements (for "
            "example, that exactly one mode holds, or a range a state "
            "variable must stay in). The deadlock-freedom proof still "
            "checks behaviour with `inv: True`; the per-operation "
            "invariant lemmas do not."
        ),
    )


_ZEXPR_TRUE_RE = re.compile(r'^\s*zexpr\s+(?P<name>\w+)\s+is\s+"\s*True\s*"', re.MULTILINE)


def check_isabelle_requirement_placeholder(thy_path: Path, repo_root: Path) -> list[Finding]:
    """I2: a named requirement predicate declared literally ``True``.

    The Isabelle generator emits ``zexpr R1 is "True"`` as a slot for a
    user-stated safety requirement, with ``Init establishes R1`` and one
    ``<op> preserves R1 under <Store>_inv`` lemma per operation. Those lemmas
    close by ``zpog_full`` and prove nothing. I1 does not see this: it checks
    the *store* invariant, which is not literally ``True``. I2 reports each
    such predicate with the number of lemmas that reference it, so the
    theory's lemma count can be read for what it contains. Advisory.
    """
    if not thy_path.exists():
        return []
    text = thy_path.read_text(encoding="utf-8")
    try:
        artifact = str(thy_path.relative_to(repo_root).as_posix())
    except ValueError:
        artifact = str(thy_path.as_posix())
    out: list[Finding] = []
    total_lemmas = len(re.findall(r"^\s*lemma\b", text, re.MULTILINE))
    for m in _ZEXPR_TRUE_RE.finditer(text):
        name = m.group("name")
        n_ref = len(re.findall(
            r'^\s*lemma\b[^\n]*"[^"\n]*\b(?:establishes|preserves)\s+' + re.escape(name) + r'\b',
            text, re.MULTILINE))
        out.append(Finding(
            signal="I2",
            kind="isabelle_requirement_placeholder",
            title=f"Isabelle requirement predicate {name} is literally `True`; "
                  f"{n_ref} of {total_lemmas} lemmas establish or preserve it",
            artifact=artifact,
            raw=m.group(0).strip(),
            fix_directive=(
                f"`zexpr {name} is \"True\"` is the generator's placeholder for a "
                f"user-stated safety requirement. The {n_ref} lemmas that establish "
                f"or preserve {name} prove that True is preserved and have no "
                f"behavioural content; the theory's remaining {total_lemmas - n_ref} "
                "lemmas (store-invariant preservation, deadlock freedom) are the "
                "ones that do. Read the lemma count accordingly. Advisory: a "
                "generator template placeholder, not a property of the Java."
            ),
        ))
    return out
