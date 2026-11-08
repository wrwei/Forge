"""Helpers shared by the phase runners: tool and path resolution, process watchdog and memory, the active case study, and the feedback emitter that every runner goes through."""
from __future__ import annotations

import json
import logging
import os
import shutil
import re
import subprocess
import sys
import threading
import time
from pathlib import Path
from typing import Any

from .. import feedback
from ..paths import DEFAULT as PATHS
from . import PhaseContext
from .enrichment import apply_llm_residual_translation

logger = logging.getLogger(__name__)


def _active_requirements_file() -> Path:
    """Resolve the requirements file for the currently-active case study.

    Reads ``agent.active_case_study`` from pipeline.yaml on every call so
    a switch via the dashboard's POST /api/active-case-study takes effect
    on the next phase run without a server restart. Falls back to the
    alphabetically-first sub-directory of forge.assets/case-studies
    if the manifest can't be read — no specific case study is hardcoded.
    """
    study: str | None = None
    try:
        from ..manifest import Manifest
        manifest = Manifest.load(PATHS.pipeline_yaml)
        raw_agent = (manifest._raw.get("agent") or {})
        candidate = raw_agent.get("active_case_study")
        if candidate:
            study = candidate
    except Exception:
        logger.exception("Failed to resolve active case study from manifest")
    if study is None:
        try:
            candidates = sorted(
                p.name for p in PATHS.case_studies.iterdir() if p.is_dir()
            )
            if candidates:
                study = candidates[0]
                logger.warning(
                    "Falling back to first case study on disk: %s", study
                )
        except OSError:
            pass
    if study is None:
        raise RuntimeError(
            "No active case study set in pipeline.yaml and no case study "
            f"directories found under {PATHS.case_studies}"
        )
    return PATHS.case_studies / study / "requirements" / "requirement_all.json"


def _active_case_study_dir() -> Path | None:
    """Resolve the active case study directory, or None if unresolvable.

    Mirrors :func:`_active_requirements_file` but returns the directory
    containing both ``requirements/`` and ``system/`` for the active
    study.
    """
    study: str | None = None
    try:
        from ..manifest import Manifest
        manifest = Manifest.load(PATHS.pipeline_yaml)
        raw_agent = (manifest._raw.get("agent") or {})
        candidate = raw_agent.get("active_case_study")
        if candidate:
            study = candidate
    except Exception:
        pass
    if study is None:
        try:
            candidates = sorted(
                p.name for p in PATHS.case_studies.iterdir() if p.is_dir()
            )
            if candidates:
                study = candidates[0]
        except OSError:
            return None
    if study is None:
        return None
    return PATHS.case_studies / study


def _resolve_os_path(value: Any) -> str:
    """Resolve an OS-aware path config value."""
    if isinstance(value, dict):
        return value.get(sys.platform, "")
    if isinstance(value, str):
        return value
    return ""


def _resolve_tool(value: Any) -> str:
    """Resolve a tool path: explicit if it has a path separator, else
    PATH lookup via shutil.which. Returns "" if not found, so the
    caller's "executable not found" branch fires with the original
    unresolved value.

    Lets pipeline.yaml stay portable — entries like `dafny.exe`,
    `refines.exe` resolve to whatever the user has on PATH, while
    legacy absolute paths still work.
    """
    raw = _resolve_os_path(value)
    if not raw:
        return ""
    resolved = shutil.which(raw)
    return resolved or ""


def _start_watchdog(proc: "subprocess.Popen", timeout_s: float) -> tuple[threading.Timer, threading.Event]:
    """Wall-clock timeout enforcement for streaming subprocesses.

    The runners read ``proc.stdout`` to EOF before calling
    ``proc.wait(timeout=...)`` — by that point the process has already
    exited, so the wait timeout can never fire and a hung verifier
    (producing no output) blocks the phase forever. This watchdog kills
    the process after ``timeout_s`` seconds of wall time regardless of
    output activity; callers check the returned Event after the read
    loop and translate a fired watchdog into their existing
    ``subprocess.TimeoutExpired`` handling.
    """
    fired = threading.Event()

    def _kill() -> None:
        fired.set()
        try:
            proc.kill()
        except OSError:
            pass

    timer = threading.Timer(timeout_s, _kill)
    timer.daemon = True
    timer.start()
    return timer, fired


def _darwin_phys_footprint_mb(pid: int):
    """macOS physical footprint of one process in MB (proc_pid_rusage, RUSAGE_INFO_V2), or None."""
    try:
        import ctypes, ctypes.util

        class _RusageInfoV2(ctypes.Structure):
            _fields_ = [("ri_uuid", ctypes.c_uint8 * 16)] + [(n, ctypes.c_uint64) for n in (
                "ri_user_time", "ri_system_time", "ri_pkg_idle_wkups", "ri_interrupt_wkups",
                "ri_pageins", "ri_wired_size", "ri_resident_size", "ri_phys_footprint",
                "ri_proc_start_abstime", "ri_proc_exit_abstime", "ri_child_user_time",
                "ri_child_system_time", "ri_child_pkg_idle_wkups", "ri_child_interrupt_wkups",
                "ri_child_pageins", "ri_child_elapsed_abstime", "ri_diskio_bytesread",
                "ri_diskio_byteswritten")]

        lib = ctypes.CDLL(ctypes.util.find_library("proc") or "/usr/lib/libproc.dylib", use_errno=True)
        info = _RusageInfoV2()
        if lib.proc_pid_rusage(ctypes.c_int(pid), ctypes.c_int(2), ctypes.byref(info)) != 0:
            return None
        total = info.ri_phys_footprint
        # The FDR4 launcher may start `_refines` as a child: add the footprints
        # of child processes, recursively (proc_listchildpids).
        buf = (ctypes.c_int * 256)()
        n = lib.proc_listchildpids(ctypes.c_int(pid), buf, ctypes.sizeof(buf))
        for child in list(buf)[:max(0, n)]:
            if child > 0 and child != pid:
                sub = _darwin_phys_footprint_mb(child)
                if sub is not None:
                    total += sub * 1024 * 1024
        return total / (1024 * 1024)
    except Exception:
        return None


def _get_process_memory_mb(pid: int) -> float:
    """Get total RSS memory usage of a process tree in MB."""
    try:
        if sys.platform == "win32":
            result = subprocess.run(
                ["tasklist", "/FI", f"PID eq {pid}", "/FO", "CSV", "/NH"],
                capture_output=True, text=True, timeout=5,
            )
            if result.returncode == 0 and result.stdout.strip():
                for line in result.stdout.strip().splitlines():
                    parts = line.strip().strip('"').split('","')
                    if len(parts) >= 5:
                        mem_str = (parts[4].replace('"', '').replace(',', '')
                                   .replace(' K', '').replace(' ', ''))
                        return int(mem_str) / 1024
        elif sys.platform == "darwin":
            # Amendment 14 (pre-registration v3): resident memory (`ps -o rss`)
            # leaves out what macOS has compressed, so the budget never fired
            # while FDR4 grew to 79-98 GB and the system's memory-pressure
            # killer ended it. The physical footprint (resident + compressed,
            # macOS's own accounting) is read first; `ps` stays as a fallback.
            fp = _darwin_phys_footprint_mb(pid)
            if fp is not None:
                return fp
            result = subprocess.run(
                ["ps", "-o", "rss=", "-p", str(pid)],
                capture_output=True, text=True, timeout=5,
            )
            if result.returncode == 0 and result.stdout.strip():
                return int(result.stdout.strip()) / 1024
        elif sys.platform == "linux":
            status_file = Path(f"/proc/{pid}/status")
            if status_file.exists():
                for line in status_file.read_text().splitlines():
                    if line.startswith("VmRSS:"):
                        return int(line.split()[1]) / 1024
    except Exception:
        pass
    return 0


def _windows_to_wsl_path(p: str) -> str:
    """Translate a Windows-style path to a WSL /mnt/<drive>/... path."""
    if not p or p.startswith("/"):
        return p
    if len(p) >= 2 and p[1] == ":":
        drive = p[0].lower()
        rest = p[2:].replace("\\", "/")
        if rest.startswith("/"):
            rest = rest[1:]
        return f"/mnt/{drive}/{rest}"
    return p.replace("\\", "/")


def _amplify_with_requirements(fb: feedback.Feedback) -> None:
    """Fix 1: populate TraceRef.requirement_ids and amplify fix_directives.

    For each Issue, look up the requirement IDs that the codegen agent
    traced to each TraceRef's (file, element), then append a one-line
    description of each requirement to the issue's ``fix_directive``.
    Best-effort: silently no-ops if the codegen trace or requirement
    catalogue are unavailable.
    """
    codegen_trace = feedback.load_codegen_trace(PATHS.generated_project)
    if not codegen_trace:
        return
    case_dir = _active_case_study_dir()
    requirements = (
        feedback.load_requirements(case_dir) if case_dir else None
    )
    for issue in fb.issues:
        feedback.populate_trace_refs_with_requirements(
            issue.java_trace, codegen_trace
        )
        # Aggregate requirement_ids across the issue's TraceRefs.
        aggregated: list[str] = []
        seen: set[str] = set()
        for ref in issue.java_trace:
            for rid in ref.requirement_ids:
                if rid not in seen:
                    seen.add(rid)
                    aggregated.append(rid)
        if aggregated:
            issue.fix_directive = feedback.amplify_directive_with_requirements(
                issue.fix_directive, aggregated, requirements
            )


def emit_feedback(ctx: PhaseContext, fb: feedback.Feedback) -> None:
    """Emit feedback via ctx.emit_feedback if set, else write directly."""
    # Fix 1: requirement-trace amplification. Joins the codegen agent's
    # requirement-to-Java trace with the verifier's Java-locating
    # failure pointers, then surfaces the requirement IDs and a
    # one-line restatement in each issue's fix_directive.
    try:
        _amplify_with_requirements(fb)
    except Exception:
        logger.exception("Requirement amplification failed for phase %s", fb.phase)

    # Fix 3: optional LLM-mediated translation of Isabelle residual goals
    # into Java-level fix suggestions. Off by default; enable via
    # agent.llm_residual_translation in pipeline.yaml. Clearly labels its
    # output as "LLM-suggested (not verified)".
    try:
        apply_llm_residual_translation(fb)
    except Exception:
        logger.exception("LLM residual translation failed for phase %s", fb.phase)

    if ctx.emit_feedback is not None:
        ctx.emit_feedback(fb)
    else:
        try:
            md_path, _ = feedback.write_feedback(PATHS.feedback_dir, fb)
            rel = md_path.relative_to(PATHS.root) if md_path.is_absolute() else md_path
            ctx.sys_msg(f"Feedback written: {rel.as_posix()}")
        except Exception:
            logger.exception("Failed to write feedback for phase %s", fb.phase)
