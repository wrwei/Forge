"""Phase 2b: requirement-to-Java trace coverage check."""
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

logger = logging.getLogger(__name__)


def run(ctx: PhaseContext) -> PhaseResult:
    """Run the bidirectional coverage check: requirements ↔
    result_codegen.json ↔ Java source. Pure Python, no subprocess."""
    phase_cfg = ctx.config
    label = ctx.phase.label if ctx.phase else "Coverage (Requirement ↔ Java)"
    del phase_cfg  # paths come from module-level constants

    ctx.sys_msg(f"{label}: starting...")

    trace_data = feedback.load_trace(ctx.paths.transformations_output)
    requirements_path = _active_requirements_file()
    if not requirements_path.exists():
        msg = (f"Requirements file not found: {requirements_path}. "
               "Check pipeline.yaml's agent.active_case_study points at "
               "a real case study under forge.assets/case-studies/.")
        ctx.sys_msg(msg)
        emit_feedback(ctx, feedback.Feedback(
            phase="coverage", label="Coverage", status="failed",
            summary=msg,
        ))
        return "failed", msg
    try:
        fb = feedback.build_coverage_feedback(
            label="Coverage (Requirement ↔ Java)",
            requirements_path=requirements_path,
            codegen_trace_path=ctx.paths.codegen_trace,
            java_source_root=ctx.paths.generated_src,
            trace_data=trace_data,
        )
    except Exception as exc:
        logger.exception("Coverage check raised an exception")
        msg = f"Coverage check failed with exception: {exc}"
        ctx.sys_msg(msg)
        emit_feedback(ctx, feedback.Feedback(
            phase="coverage", label="Coverage", status="failed",
            summary=msg,
        ))
        return "failed", msg

    emit_feedback(ctx, fb)
    if fb.status == "passed":
        return "completed", ""
    return "failed", fb.summary
