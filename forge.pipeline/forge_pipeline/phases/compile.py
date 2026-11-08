"""Phase 2a: gradle build of the generated Java project."""
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


def _emit_compile_feedback(ctx: PhaseContext, fb_status: str, summary: str,
                           raw_output: str) -> None:
    """Build + write post_compile.{md,json} via the gradle classifier."""
    trace_data = feedback.load_trace(ctx.paths.transformations_output)
    fb = feedback.build_gradle_phase_feedback(
        phase_id="compile",
        label="Compile (gradle build)",
        status="passed" if fb_status == "passed" else "failed",
        summary=summary,
        error_summary=raw_output if fb_status != "passed" else "",
        trace_data=trace_data,
    )
    emit_feedback(ctx, fb)


def run(ctx: PhaseContext) -> PhaseResult:
    """Run `gradlew build` against java.generated.project/. Captures
    stderr, classifies failures via the gradle-phase classifier, and
    emits post_compile.{md,json}."""
    phase_cfg = ctx.config
    label = ctx.phase.label if ctx.phase else "Compile (gradle build)"
    del phase_cfg  # config currently unused; gradle has all it needs

    if not (ctx.paths.generated_project / "build.gradle").exists() and \
       not (ctx.paths.generated_project / "build.gradle.kts").exists():
        msg = (f"No build.gradle in {ctx.paths.generated_project}. "
               "Cannot run compile phase.")
        ctx.sys_msg(msg)
        _emit_compile_feedback(ctx, "failed", msg, msg)
        return "failed", msg

    cmd = [str(ctx.paths.generated_project / ctx.paths.gradlew), "build", "--no-daemon", "-x", "test"]
    ctx.sys_msg(f"{label}: starting...")
    try:
        proc = subprocess.Popen(
            cmd, cwd=str(ctx.paths.generated_project),
            stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
            text=True, encoding="utf-8", errors="replace",
        )
    except (OSError, subprocess.SubprocessError) as exc:
        msg = f"Failed to launch gradle: {exc}"
        ctx.sys_msg(msg)
        _emit_compile_feedback(ctx, "failed", msg, msg)
        return "failed", msg

    output_lines: list[str] = []
    try:
        for line in proc.stdout or []:
            if ctx.stop_requested():
                proc.terminate()
                break
            output_lines.append(line.rstrip())
            ctx.stream(line.rstrip(), phase="compile")
        proc.wait(timeout=30)
    except subprocess.TimeoutExpired:
        proc.kill()
        msg = "gradle build timed out during shutdown"
        _emit_compile_feedback(ctx, "failed", msg, "\n".join(output_lines))
        return "failed", msg

    # A user stop terminates gradle mid-build; the non-zero exit code that
    # produces is not a compile verdict. Return "stopped" without emitting
    # failure feedback (the same exit-status-vs-verdict distinction the
    # verifier runners make).
    if ctx.stop_requested():
        ctx.sys_msg(f"{label} stopped by user.")
        return "stopped", ""

    full_output = "\n".join(output_lines)
    if proc.returncode == 0:
        summary = "Java project compiled successfully."
        _emit_compile_feedback(ctx, "passed", summary, full_output)
        return "completed", ""

    tail = "\n".join(output_lines[-80:]) if output_lines else "(no output)"
    summary = f"gradle build failed (exit {proc.returncode})"
    _emit_compile_feedback(ctx, "failed", summary, tail)
    return "failed", summary
