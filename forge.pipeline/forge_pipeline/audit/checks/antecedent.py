"""Signal A1: an implication whose antecedent can never hold."""
from __future__ import annotations

import json
import math
import re
from pathlib import Path

from .. import sat
from ..model import Finding, rel as _rel


def check_antecedent_vacuity(model, thy_path: Path, repo_root: Path) -> list[Finding]:
    """A1 — an implication (in the invariant or a precondition) whose
    antecedent is UNSAT: the implication is then vacuously true."""
    out = []
    env = sat.Env(checked=False)
    seen: set[str] = set()
    sources = [("invariant", model.inv_atoms)] + [
        (f"pre of {op.name}", op.atoms) for op in model.operations]
    for where, atoms in sources:
        for a in atoms:
            if a.kind != "implies" or a.raw in seen:
                continue
            seen.add(a.raw)
            ante_atoms = sat.parse_conjunction(a.ante)
            r = sat.sat_conjunction(ante_atoms, model, env)
            if r.verdict == sat.UNSAT:
                out.append(Finding(
                    signal="A1",
                    kind="antecedent_unsat",
                    title=f"Implication antecedent can never hold ({where})",
                    artifact=_rel(thy_path, repo_root),
                    raw=f"{a.raw}  -- antecedent UNSAT: {r.reason}",
                    fix_directive=(
                        "The antecedent of this implication is "
                        f"unsatisfiable ({r.reason}); the implication is "
                        "vacuously true and contributes no verification "
                        "content. Repair or remove it in the template."
                    ),
                ))
    return out
