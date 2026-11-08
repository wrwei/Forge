"""Phase 6d: the vacuity audit over the generated artefacts (see forge_pipeline.audit)."""
from __future__ import annotations

import json
import logging
import os
import re
import subprocess
import sys
import threading
import time
from pathlib import Path
from typing import Any

from .. import feedback
from . import PhaseContext, PhaseResult
from ._common import (
    _resolve_os_path, _resolve_tool, _start_watchdog, _get_process_memory_mb,
    _windows_to_wsl_path, _active_requirements_file, emit_feedback,
)
from .. import audit as vacuity_audit

logger = logging.getLogger(__name__)


def run(ctx: PhaseContext) -> PhaseResult:
    """Vacuity audit on generated Dafny + Isabelle + CSP artefacts.

    Runs after ``dafny_gen`` / ``isabelle_gen`` / ``m2t``. Two layers:
    the syntactic D1 / I1 signals ("verifier passes nothing because the
    obligation was True") and the semantic S1/A1/E1/N1/R1/R2/T1/T2/P1/K1
    checks (precondition satisfiability, mode/transition reachability —
    symbolic AND under the checked CSP instantiation — antecedent
    vacuity, empty domains, Init-vs-invariant consistency, cross-backend
    constant consistency). See :mod:`web.vacuity` and
    :mod:`web.vacuity_sat`. Writes ``post_vacuity.{md,json}`` like every
    other phase; ``passed`` iff no signal fires.
    """
    label = ctx.phase.label if ctx.phase else "Vacuity audit"
    ctx.sys_msg(f"{label}: scanning {ctx.paths.transformations_output}...")

    try:
        report = vacuity_audit.audit(
            ctx.paths.transformations_output, ctx.paths.root,
            java_root=ctx.paths.generated_project / "src" / "main" / "java",
        )
    except Exception as exc:
        logger.exception("Vacuity audit raised an exception")
        msg = f"Vacuity audit failed with exception: {exc}"
        ctx.sys_msg(msg)
        emit_feedback(ctx, feedback.Feedback(
            phase="vacuity", label=label, status="failed",
            summary=msg,
        ))
        return "failed", msg

    issues = [
        feedback.Issue(
            # severity is prefixed into the title so it survives into
            # post_vacuity.{md,json} without changing the Issue schema:
            # an advisory finding must never read as an actionable defect.
            kind=f.kind,
            title=f"[{f.severity.upper()}] {f.title}",
            raw=f.raw,
            fix_directive=(
                f.fix_directive if f.severity == "blocking" else
                "ADVISORY — no action for the codegen agent. This follows from "
                "the deliberate CSP domain narrowing (FDR4 finiteness), not from "
                "the Java. Record it with any verdict drawn from the CSP checks."
            ),
        )
        for f in report.findings
    ]

    adv = report.advisory
    if report.status == "passed":
        summary = (
            "No blocking vacuity signals detected (syntactic D1/I1 and semantic "
            "satisfiability/reachability/constant-consistency checks). "
            "Any `0 errors` from Dafny / Isabelle / FDR is at least "
            "potentially load-bearing for the checked signals."
        )
        if adv:
            summary += (
                f" {len(adv)} ADVISORY finding(s) recorded and NOT blocking: "
                + ", ".join(sorted({f"{f.signal}/{f.kind}" for f in adv}))
                + ". These follow from the deliberate narrowing of the CSP data "
                "domains that FDR4's finiteness requirement imposes, not from the "
                "Java under test; they bound what the passing FDR4 assertions are "
                "evidence about and must be reported with any verdict drawn from them."
            )
        next_step = "Nothing to do. Re-run after each iteration to catch regressions."
    else:
        summary = (
            f"{len(report.blocking)} blocking vacuity signal(s) detected"
            + (f" (plus {len(adv)} advisory)" if adv else "")
            + ". The corresponding verifier passes discharge `True` obligations "
            "and do not establish behavioural content."
        )
        next_step = (
            "Strengthen the flagged template(s) so the verifier discharges "
            "behavioural obligations rather than `True`. Until that is done, "
            "do not count the corresponding verifier pass as evidence of "
            "behavioural correctness in the experiment write-up."
        )

    fb = feedback.Feedback(
        phase="vacuity",
        label=label,
        status=report.status,
        summary=summary,
        issues=issues,
        next_step=next_step,
    )
    emit_feedback(ctx, fb)

    ctx.sys_msg(
        f"{label}: {report.status} "
        f"({len(report.blocking)} blocking, {len(adv)} advisory)"
    )
    if report.status == "passed":
        return "completed", ""
    return "failed", summary
