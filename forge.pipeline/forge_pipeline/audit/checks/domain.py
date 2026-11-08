"""Signal E1: an event or parameter domain that is empty or a singleton."""
from __future__ import annotations

import json
import math
import re
from pathlib import Path

from .. import sat
from ..model import Finding, rel as _rel


def check_empty_domain(model, thy_path: Path, repo_root: Path,
                       inst=None, inst_path: Path | None = None) -> list[Finding]:
    """E1 — empty or singleton domains: enumtypes, `params` domains
    (definition ... = {}), and narrowed CSP nametypes. Singleton domains
    are only flagged when a guard/invariant actually compares against
    them (a comparison over a one-value type is constant)."""
    out = []
    # constructors compared against anywhere in preconditions or invariant
    compared_ctors: set[str] = set()
    for atoms in [model.inv_atoms] + [op.atoms for op in model.operations]:
        for a in atoms:
            if (a.kind == "cmp" and a.op in ("=", "!=")
                    and a.rhs and a.rhs[0] == "var"
                    and model.ctor_owner(a.rhs[1]) is not None
                    and a.lhs != ("var", "st")):
                compared_ctors.add(a.rhs[1])
    for tname, ctors in model.enumtypes.items():
        if len(ctors) == 0:
            out.append(Finding(
                signal="E1", kind="empty_domain",
                title=f"enumtype {tname} has an empty domain",
                artifact=_rel(thy_path, repo_root),
                raw=f"enumtype {tname} =",
                fix_directive="A guard quantifying over this type is "
                              "unsatisfiable; a set comprehension over it is "
                              "empty. The type must have at least one value.",
            ))
        elif len(ctors) == 1 and ctors[0] in compared_ctors:
            out.append(Finding(
                signal="E1", kind="singleton_domain",
                title=f"enumtype {tname} has a singleton domain "
                      f"({{{ctors[0]}}})",
                artifact=_rel(thy_path, repo_root),
                raw=f"enumtype {tname} = {ctors[0]}",
                fix_directive=(
                    "Every comparison over this type is constant (always "
                    "true or always false), so guards reading it verify no "
                    "behavioural content. Confirm the Java source really has "
                    "a one-value domain here."
                ),
            ))
    for dname, rhs in model.defs.items():
        if rhs.strip() == "{}" and any(dname == d for op in model.operations
                                       for _, d in op.params):
            out.append(Finding(
                signal="E1", kind="empty_param_domain",
                title=f"Parameter domain {dname} is empty",
                artifact=_rel(thy_path, repo_root),
                raw=f'definition {dname} = "{{}}"',
                fix_directive=(
                    "An operation takes a parameter from this empty set, so "
                    "its enabledness disjunct `\\<exists>x \\<in> {} . ...` "
                    "is False: the operation can never fire and its lemmas "
                    "are vacuous."
                ),
            ))
    if inst is not None and inst_path is not None:
        for nname, (lo, hi) in inst.nametypes.items():
            if lo > hi:
                out.append(Finding(
                    signal="E1", kind="empty_nametype",
                    title=f"CSP nametype {nname} = {{{lo}..{hi}}} is empty",
                    artifact=_rel(inst_path, repo_root),
                    raw=f"nametype {nname} = {{{lo}..{hi}}}",
                    fix_directive="FDR ranges over an empty type: every "
                                  "prefixed communication on it deadlocks and "
                                  "assertions over it are vacuous.",
                ))
    return out
