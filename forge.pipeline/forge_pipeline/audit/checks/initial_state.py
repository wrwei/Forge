"""Signal N1: the initial state does not satisfy the invariant."""
from __future__ import annotations

import json
import math
import re
from pathlib import Path

from .. import sat
from ..model import Finding, rel as _rel


def check_initial_state_consistency(model, thy_path: Path,
                                    repo_root: Path) -> list[Finding]:
    """N1 — Init's assignments vs the invariant: is `Init establishes inv`
    non-vacuously satisfiable? Evaluates each invariant conjunct under the
    literal Init substitution."""
    out = []
    if not model.init:
        return out
    init_st = model.init.get("st")
    for a in model.inv_atoms:
        verdict = None
        reason = ""
        if a.kind == "neq_empty_list":
            rhs = model.init.get(a.var, "")
            if rhs.startswith("[") and rhs.strip() != "[]":
                verdict = True
            elif rhs.strip() == "[]":
                verdict, reason = False, f"Init sets {a.var} = [] but the " \
                                         f"invariant requires {a.var} != []"
        elif a.kind == "implies":
            ante = sat.parse_conjunction(a.ante)
            if (len(ante) == 1 and ante[0].kind == "cmp"
                    and ante[0].lhs == ("var", "st") and ante[0].op == "="
                    and ante[0].rhs and ante[0].rhs[0] == "var"):
                pin_state = ante[0].rhs[1]
                if init_st is not None and pin_state != init_st:
                    verdict = True  # antecedent false at Init
                else:
                    cons = sat.parse_conjunction(a.cons)
                    if len(cons) == 1 and cons[0].kind == "set_eq":
                        lens_rhs = model.init.get(cons[0].var)
                        if lens_rhs is not None:
                            init_set = sat._parse_term(lens_rhs)
                            if init_set and init_set[0] == "set":
                                verdict = init_set[1] == cons[0].set_value
                                if not verdict:
                                    reason = (
                                        f"Init sets {cons[0].var} = "
                                        f"{{{', '.join(sorted(init_set[1]))}}} but the "
                                        f"invariant pins it to "
                                        f"{{{', '.join(sorted(cons[0].set_value))}}} "
                                        f"in state {pin_state}")
        if verdict is False:
            out.append(Finding(
                signal="N1", kind="init_inv_unsat",
                title="Initial state contradicts the invariant",
                artifact=_rel(thy_path, repo_root),
                raw=f"{a.raw}  -- {reason}",
                fix_directive=(
                    f"`Init establishes inv` cannot hold: {reason}. Either "
                    "the Init defaults or the invariant conjunct is wrong in "
                    "thy_generation_rule.egl."
                ),
            ))
    return out
