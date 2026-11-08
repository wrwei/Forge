"""Phase 6a: Dafny verification of the generated program."""
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
    """Run Dafny verification as a subprocess."""
    phase_cfg = ctx.config
    label = ctx.phase.label if ctx.phase else "Dafny Verification"

    raw_dafny = _resolve_os_path(phase_cfg.get("dafny_path", ""))
    dafny_path = _resolve_tool(phase_cfg.get("dafny_path", ""))
    timeout = phase_cfg.get("timeout", 120)

    if not dafny_path:
        msg = (f"Dafny executable not found: {raw_dafny or '(empty)'}. "
               "Add its install dir to PATH or set "
               "phases.dafny_verify.dafny_path to an absolute path in pipeline.yaml.")
        ctx.sys_msg(msg)
        return "failed", msg

    dfy_files = sorted(ctx.paths.transformations_output.glob("*.dfy"))
    if not dfy_files:
        msg = "No .dfy files found in output"
        ctx.sys_msg(msg)
        return "failed", msg

    dafny_mappings = feedback.load_dafny_trace(ctx.paths.transformations_output)
    trace_data = feedback.load_trace(ctx.paths.transformations_output)

    all_passed = True
    error_output = []
    results_table = []
    combined_raw_output_parts: list[str] = []

    for dfy_file in dfy_files:
        ctx.sys_msg(f"Verifying {dfy_file.name}...")
        cmd = [dafny_path, "verify", str(dfy_file)]

        try:
            proc = subprocess.Popen(
                cmd, stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
                text=True, encoding="utf-8", errors="replace",
            )
            # Wall-clock timeout: the read loop below blocks until stdout
            # EOF, so wait(timeout=...) alone cannot stop a hung dafny.
            watchdog, watchdog_fired = _start_watchdog(proc, timeout)

            file_output_lines = []
            for line in proc.stdout:
                if ctx.stop_requested():
                    proc.terminate()
                    return "stopped", ""
                stripped = line.rstrip("\r\n")
                file_output_lines.append(stripped)
                ctx.stream(stripped)

            proc.wait(timeout=timeout)
            watchdog.cancel()
            if watchdog_fired.is_set():
                raise subprocess.TimeoutExpired(cmd, timeout)
            file_output = "\n".join(file_output_lines)
            combined_raw_output_parts.append(file_output)

            verified = 0
            errors = 0
            match = re.search(r"(\d+)\s+verified,\s+(\d+)\s+error", file_output)
            if match:
                verified = int(match.group(1))
                errors = int(match.group(2))

            status = "PASS" if errors == 0 and proc.returncode == 0 else "FAIL"
            if status == "PASS":
                detail = f"All {verified} conditions verified" if verified else "No conditions found"
            else:
                all_passed = False
                detail = ""
                err_lines = [l for l in file_output.split("\n") if "Error" in l or "error" in l.lower()]
                if err_lines:
                    detail = err_lines[0].strip()[:120]
                error_output.append(f"{dfy_file.name}: {detail or f'exit code {proc.returncode}'}")

            results_table.append({
                "file": dfy_file.name,
                "status": status,
                "verified": verified,
                "errors": errors,
                "detail": detail,
            })

        except subprocess.TimeoutExpired:
            proc.kill()
            all_passed = False
            timeout_msg = f"{dfy_file.name}: timed out after {timeout}s"
            error_output.append(timeout_msg)
            combined_raw_output_parts.append(
                f"{dfy_file.name}(0,0): Error: verifier timed out after {timeout}s"
            )
            results_table.append({
                "file": dfy_file.name,
                "status": "FAIL",
                "verified": 0,
                "errors": 0,
                "detail": f"Timeout after {timeout}s",
            })

    table_lines = [
        "| File | Status | Verified | Errors | Details |",
        "|------|--------|----------|--------|---------|",
    ]
    for r in results_table:
        table_lines.append(
            f"| {r['file']} | {r['status']} | {r['verified']} | "
            f"{r['errors']} | {r.get('detail', '')} |"
        )
    table_msg = {
        "type": "agent_message",
        "phase": ctx.phase.id or "",
        "agent": "dafny_verify",
        "content": "\n".join(table_lines),
    }
    ctx.sink.event(table_msg)
    ctx.state.add_message(table_msg)

    fb_status = "passed" if all_passed else "failed"
    total_verified = sum(r["verified"] for r in results_table)
    total_errors = sum(r["errors"] for r in results_table)
    summary = (
        f"Dafny: {len(dfy_files)} file(s), "
        f"{total_verified} verified, {total_errors} errors."
    )
    fb = feedback.build_dafny_feedback(
        label=label,
        status=fb_status,
        summary=summary,
        raw_output="\n".join(combined_raw_output_parts),
        dafny_mappings=dafny_mappings,
        trace_data=trace_data,
    )
    emit_feedback(ctx, fb)

    if all_passed:
        ctx.sys_msg(f"{label} completed — all files verified.")
        return "completed", ""
    else:
        msg = f"{label} failed:\n" + "\n".join(error_output)
        ctx.sys_msg(msg)
        return "failed", msg
