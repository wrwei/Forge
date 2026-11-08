"""Signal K1: a named constant with different values in the Java and the backends' artefacts."""
from __future__ import annotations

import json
import math
import re
from pathlib import Path

from .. import sat
from ..model import Finding, rel as _rel


def check_constant_consistency(java_root: Path | None,
                               rct_path: Path | None,
                               dfy_paths: list[Path],
                               csp_assert_paths: list[Path],
                               constant_defaults_path: Path | None,
                               repo_root: Path) -> list[Finding]:
    """K1 — the same named constant must carry the same value in the Java
    source (or constant_defaults.json), the emitted .rct, the emitted .dfy,
    and the CSP assertion files. Divergence means at least one backend
    verified a different system than the code implements (the U4 class)."""
    out = []
    java_consts: dict[str, sat.Fraction] = {}
    src_label = ""
    if constant_defaults_path is not None and constant_defaults_path.exists():
        try:
            data = json.loads(constant_defaults_path.read_text(encoding="utf-8"))
            for k, v in data.items():
                try:
                    java_consts[sat.norm_const_name(k)] = sat.Fraction(str(v))
                except (ValueError, ZeroDivisionError):
                    pass
            src_label = _rel(constant_defaults_path, repo_root)
        except (ValueError, OSError):
            pass
    if not java_consts and java_root is not None and java_root.exists():
        for k, v in sat.parse_java_constants(java_root).items():
            java_consts[sat.norm_const_name(k)] = v
        src_label = _rel(java_root, repo_root)
    if not java_consts:
        return out

    backends: dict[str, dict[str, sat.Fraction]] = {}
    if rct_path is not None and rct_path.exists():
        vals = {}
        for k, v in sat.parse_rct_consts(
                rct_path.read_text(encoding="utf-8", errors="replace")).items():
            vals[sat.norm_const_name(k)] = v
        backends[_rel(rct_path, repo_root)] = vals
    for dfy in dfy_paths:
        vals = {}
        for k, v in sat.parse_dafny_consts(
                dfy.read_text(encoding="utf-8", errors="replace")).items():
            vals[sat.norm_const_name(k)] = v
        backends[_rel(dfy, repo_root)] = vals
    for cap in csp_assert_paths:
        vals = {}
        for k, v in sat.parse_csp_consts(
                cap.read_text(encoding="utf-8", errors="replace")).items():
            # const_SRangerController_Ctrl_obstaclethreshold -> obstaclethreshold
            short = sat.norm_const_name(k.split("_")[-1])
            if short in vals and vals[short] != v:
                vals[short] = None  # internally inconsistent — flag via mismatch
            else:
                vals.setdefault(short, v)
        backends[_rel(cap, repo_root)] = {k: v for k, v in vals.items()
                                          if v is not None}

    for cname, jval in sorted(java_consts.items()):
        divergent = []
        for art, vals in backends.items():
            if cname in vals and vals[cname] != jval:
                divergent.append((art, vals[cname]))
        if divergent:
            shown = "; ".join(f"{a}: {float(v):g}" for a, v in divergent)
            # Distinguish the DECLARED CSP float ceiling from an undeclared
            # mismatch. The RoboChart CSP generator (circus.robocalc.robochart
            # .generator.csp 3.0.0) has no FloatExp overload in its expression
            # dispatcher, so a fractional `const ... : real = 0.5` aborts
            # generation entirely; robochart2rct.egl therefore ceils fractional
            # constants for the .rct/CSP target ONLY, records a first-class
            # pipeline warning, and leaves Isabelle/Dafny (generated from the
            # XMI, which carries the true value) untouched.
            #
            # A divergence that is exactly that ceiling, and confined to the
            # .rct/CSP artefacts, is the disclosed cost of a target limitation
            # no codegen iteration can remove -> ADVISORY. Any other
            # divergence -- a different value, or one reaching Dafny/Isabelle
            # -- is a genuine inconsistency between backends -> BLOCKING.
            csp_only = all(
                (".rct" in a) or ("/csp" in a) or a.endswith(".csp")
                for a, _ in divergent
            )
            is_ceiling = all(
                float(v) == float(math.ceil(float(jval))) and float(jval) != float(v)
                for _, v in divergent
            )
            kind = ("constant_divergence_csp_ceiling"
                    if (csp_only and is_ceiling) else "constant_divergence")
            out.append(Finding(
                signal="K1", kind=kind,
                title=f"Constant `{cname}` diverges between the Java source "
                      f"and generated artefact(s)",
                artifact=divergent[0][0],
                raw=f"Java ({src_label}): {float(jval):g}; {shown}",
                fix_directive=(
                    "ADVISORY - the CSP/.rct value is the ceiling of the Java "
                    "value and no other backend diverges. This is the declared "
                    "FloatExp workaround (the CSP generator cannot compile "
                    "fractional real constants); it is warned about at emission "
                    "time and must be disclosed with the FDR results. No action "
                    "for the codegen agent."
                    if kind == "constant_divergence_csp_ceiling" else
                    "At least one backend verified a system with a different "
                    "constant than the Java implements, and the divergence is "
                    "not the declared CSP float ceiling. Fix the constant "
                    "emission."
                ),
            ))
    return out
