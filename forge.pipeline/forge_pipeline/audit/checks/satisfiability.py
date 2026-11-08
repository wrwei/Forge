"""Signal S1: an operation's precondition cannot be satisfied under the store invariant, symbolically or under the environment model."""
from __future__ import annotations

import json
import math
import re
from pathlib import Path

from .. import sat
from ..model import Finding, rel as _rel


def check_precondition_unsat(model, thy_path: Path, repo_root: Path) -> list[Finding]:
    """S1 — a zoperation whose `pre` is unsatisfiable under the invariant
    pins in the SYMBOLIC (unnarrowed) model. The obligation `Op preserves
    inv` for such an operation is vacuously discharged."""
    out = []
    env = sat.Env(checked=False)
    em = sat.environment_model(model)
    for op in model.operations:
        if em == "B" and op.name.startswith("Idle_"):
            # The idle operation (the Java's implicit `else`) is reported by
            # check_precondition_unsat_env, as advisory, when the mode's
            # branches are exhaustive; reporting it here as well would block
            # the iteration on a condition that is not a defect.
            continue
        r = sat.sat_conjunction(op.atoms, model, env)
        if r.verdict == sat.UNSAT:
            out.append(Finding(
                signal="S1",
                kind="precondition_unsat",
                title=f"Precondition of zoperation {op.name} is unsatisfiable",
                artifact=_rel(thy_path, repo_root),
                raw=f'pre "{op.pre_raw}"  -- {r.reason}',
                fix_directive=(
                    f"The precondition of `{op.name}` can never hold "
                    f"({r.reason}), so every proof obligation about this "
                    "operation is vacuously true and the operation can never "
                    "fire. The extractor prepends the negations of every "
                    "EARLIER branch guard in the same mode (faithful else-if "
                    "semantics), so a contradiction here almost always means "
                    "the SOURCE JAVA TRANSITION IS DEAD CODE: an earlier "
                    "branch in the same mode has a guard that subsumes or "
                    "equals part of this branch's guard, so this branch can "
                    "never be reached. FIX IN THE JAVA: find this operation's "
                    "source branch via the traceability entry, compare its "
                    "guard with the earlier branches in the same mode, and "
                    "reorder the branches (most-specific first) or merge them "
                    "per the requirements' intent. Do NOT touch any .egl "
                    "template. Only if the named atoms are NOT equivalent in "
                    "the Java (i.e. the contradiction was introduced by "
                    "extraction, not by branch ordering) is this an "
                    "instrument issue — report it to the operator instead of "
                    "editing anything."
                ),
            ))
    return out


def check_precondition_unsat_env(model, thy_path: Path, repo_root: Path) -> list[Finding]:
    """S1 (environment-aware, 2026-10-20) -- a zoperation whose `pre` is
    unsatisfiable under EVERY offer the theory's own invariant allows at its
    source state. `check_precondition_unsat` leaves `offered` free, so it cannot
    see an operation killed by the environment model; this is the check that
    would have reported the model-A dead operations (LRE: 9 of 18, incl.
    MOMToCAM) in September. Under model B (at most one listened event per
    step) a hit means the branch can never be taken in the extracted model."""
    out = []
    em = sat.environment_model(model)
    if em == "free":
        return out
    env = sat.Env(checked=False)
    for op in model.operations:
        verdict, offers, reasons = sat.op_sat_under_environment(op, model, env)
        if verdict != sat.UNSAT or offers is None:
            continue
        if em == "B" and op.name.startswith("Idle_"):
            # The idle operation is the Java's implicit `else`. It is unsatisfiable
            # exactly when the mode's branches are exhaustive (e.g. one branch per
            # value of a two-valued status plus every listened event), so the Java
            # `else` is unreachable too. Report it, but as advisory.
            out.append(Finding(
                signal="S1",
                kind="idle_unreachable",
                title=f"Idle operation {op.name} is unreachable: the mode's branches are exhaustive",
                artifact=_rel(thy_path, repo_root),
                raw=f'pre "{op.pre_raw}"  -- ' + "; ".join(reasons),
                fix_directive=(
                    f"`{op.name}` stands for the Java `else` of this mode and can never "
                    "fire because the mode's branches cover every case. The Java `else` is "
                    "unreachable for the same reason. Nothing to fix; recorded so the "
                    "obligation count for this mode is read correctly."
                ),
            ))
            continue
        if em == "A":
            directive = (
                f"`{op.name}` can never fire in the verified theory: its precondition "
                "is false under the invariant's environment model `offered = listens` "
                "(every event the state listens for is always offered, so every "
                "absence conjunct on a listened event is false). This is an "
                "INSTRUMENT issue, not a Java defect: the theory was generated with "
                "the superseded maximal-cooperation encoding . Regenerate with "
                "the current generator (environment model B). Do NOT edit the Java "
                "for this finding."
            )
        else:
            directive = (
                f"`{op.name}` can never fire in the verified theory: under every offer "
                "the invariant allows at its source state (at most one listened "
                "event per step) its precondition is false. Either the source branch "
                "needs two events in one step (not expressible in the Java step "
                "function either -- check the extraction), or it is dead code in the "
                "Java (an earlier branch in the same mode subsumes it). Find the "
                "branch via the traceability entry and compare it with the earlier "
                "branches in the same mode; reorder or merge per the requirements' "
                "intent. Do NOT touch any .egl template."
            )
        out.append(Finding(
            signal="S1",
            kind="precondition_unsat_env",
            title=f"Precondition of zoperation {op.name} is unsatisfiable under the environment model ({em})",
            artifact=_rel(thy_path, repo_root),
            raw=f'pre "{op.pre_raw}"  -- ' + "; ".join(reasons),
            fix_directive=directive,
        ))
    return out
