"""Phase 6b: FDR4 refinement checking of the generated CSP, with the memory monitor and the stderr classifier."""
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


def _parse_framed_json(raw_output: str) -> list:
    """Parse FDR4 framed_json output (one JSON object per line)."""
    results = []
    for line in raw_output.strip().splitlines():
        line = line.strip()
        if not line:
            continue
        try:
            results.append(json.loads(line))
        except json.JSONDecodeError:
            pass
    return results


def _parse_stderr_stats(stderr_text: str) -> dict:
    """Parse FDR4 stderr progress output for per-assertion visit stats."""
    stats: dict[str, dict] = {}
    current_assertion = None
    for line in (stderr_text or "").splitlines():
        line = line.strip()
        m = re.match(r"Checking\s+(.+)", line)
        if m:
            current_assertion = m.group(1).strip().rstrip(".")
            continue
        m = re.search(r"(\d+)\s+states?\b.*?(\d+)\s+transitions?", line)
        if m and current_assertion:
            stats[current_assertion] = {
                "states": int(m.group(1)),
                "transitions": int(m.group(2)),
            }
        m2 = re.search(r"ply\s+(\d+)", line, re.IGNORECASE)
        if m2 and current_assertion and current_assertion in stats:
            stats[current_assertion]["plys"] = int(m2.group(1))
    return stats


def _get_assertion_stats(assertion: dict, stderr_stats: dict) -> dict:
    """Extract visit stats for an assertion from JSON fields or stderr."""
    for container in (
        assertion.get("status", {}),
        assertion.get("statistics", {}),
        assertion,
    ):
        if not isinstance(container, dict):
            continue
        states = (
            container.get("visited_states")
            or container.get("states_visited")
            or container.get("states")
            or container.get("explored_states")
        )
        transitions = (
            container.get("visited_transitions")
            or container.get("transitions_visited")
            or container.get("transitions")
            or container.get("explored_transitions")
        )
        if states is not None and transitions is not None:
            plys = (
                container.get("visited_plys")
                or container.get("plys")
                or container.get("ply")
            )
            return {"states": states, "transitions": transitions, "plys": plys}

    assert_str = assertion.get("assertion_string", "")
    for key, val in stderr_stats.items():
        if key in assert_str or assert_str in key:
            return val

    return {"states": None, "transitions": None, "plys": None}


# Amendment 14 (pre-registration v3): the budget is already the machine's limit
# (Amendment 10), so the directive no longer asks for a pipeline.yaml change.
_FDR4_MEMORY_DIRECTIVE = (
    "The model's state space exceeded the machine's memory, so FDR4 gave no "
    "verdict. The memory budget is already at the machine's limit and cannot be "
    "raised. This is not a parse error or a template defect. You may reduce the "
    "controller's state space in the Java (fewer transitions, fewer or narrower "
    "state variables, merged conditions), but only without changing the behaviour "
    "the requirements specify; otherwise record the FDR4 assertions as unmeasured."
)


def _classify_fdr4_stderr(stderr_text: str, memory_limit_mb: int) -> dict:
    """Classify FDR4 stderr to distinguish memory/runtime errors from parse errors.

    Returns a dict with chat (`headline`), feedback-summary (`summary`),
    and feedback-issue (`kind`, `title`, `fix_directive`, `error`) fields.
    The feedback-issue fields are consumed by
    feedback.fdr4.build_fdr4_feedback's errors loop — when `kind` is
    present it bypasses the default parse_error builder.
    """
    low = stderr_text.lower()

    # Amendment 14 (pre-registration v3): FDR4 ended by a signal with no verdict
    # (the operating system's memory-pressure killer sends SIGKILL). It used to
    # fall through to the parse_error builder below.
    if low.startswith("fdr4 was killed by signal"):
        return {
            "headline": "FDR4 was killed before giving a verdict.",
            "summary": (
                "FDR4 loaded the CSP and was exploring the state space when it was "
                "killed by a signal (on macOS, the system's memory-pressure killer "
                "ends the largest process when memory runs out). Not a parse error."
            ),
            "kind": "fdr4_signal_kill",
            "title": "FDR4 killed — verdict unmeasured",
            "fix_directive": _FDR4_MEMORY_DIRECTIVE,
            "error": stderr_text[:2000],
        }

    # Timeout is NOT a parse error: the CSP loaded and FDR4 was still
    # exploring the state space when the subprocess budget expired.
    # Labelling it parse_error (the old fallthrough) sends a repair
    # iteration hunting for a template bug that does not exist — observed
    # on chem run-1 v2, whose seq-typed payload (LSeq(GasSensor,2))
    # genuinely needs more than the default budget.
    # Python-side memory-monitor kill — same reporting rule as the timeout:
    # the CSP loaded and FDR4 was mid-exploration when the RSS monitor
    # killed it. Not a parse error, not a code defect.
    if "memory usage exceeded" in low:
        return {
            "headline": "FDR4 killed by the memory monitor (state space exceeded the RSS budget).",
            "summary": (
                "FDR4 loaded the CSP and was exploring when its RSS crossed "
                "the configured memory_limit_mb. This is a state-space "
                "measurement limit, not a code or template defect."
            ),
            "kind": "fdr4_memory_kill",
            "title": "FDR4 memory kill — verdict unmeasured",
            "fix_directive": _FDR4_MEMORY_DIRECTIVE,   # Amendment 14
            "error": stderr_text[:2000],
        }

    if "timed out after" in low:
        return {
            "headline": "FDR4 timed out (state-space exploration, not a defect).",
            "summary": (
                "FDR4 loaded the CSP and was still checking when the time "
                "budget expired. This is a measurement limit, not a code or "
                "template defect."
            ),
            "kind": "fdr4_timeout",
            "title": "FDR4 timeout — verdict unmeasured",
            "fix_directive": (
                "Do NOT edit Java or templates for this issue. Either raise "
                "phases.fdr4.timeout in pipeline.yaml and re-run, or record "
                "the assertion set as unmeasured under the time budget. If "
                "the model has sequence- or record-typed channels, their "
                "instantiated domains are the usual state-space driver."
            ),
            "error": stderr_text[:2000],
        }

    # Windows page-file exhaustion: GHC RTS tries to commit address
    # space up-front and fails when the page file can't grow.
    if ("virtualalloc" in low and "mem_commit" in low) or "paging file is too small" in low:
        return {
            "headline": "FDR4 ran out of address space (Windows page file too small).",
            "summary": (
                "FDR4 could not commit memory: Windows page file is smaller "
                "than what the GHC runtime in refines.exe wants up-front."
            ),
            "kind": "memory_error",
            "title": "Windows page file too small for refines.exe",
            "fix_directive": (
                "Fix any one:\n"
                "  • Grow the system page file: System Properties → Advanced → "
                "Performance Settings → Advanced → Virtual memory. Set the page "
                "file to at least 8 GB on the drive that hosts refines.exe.\n"
                f"  • Lower phases.fdr4.memory_limit_mb in pipeline.yaml "
                f"(currently {memory_limit_mb}) — the python-side monitor will "
                "stop the run earlier, but doesn't shrink the up-front commit.\n"
                "  • Run on a machine with more physical RAM."
            ),
            "error": stderr_text[:2000],
        }

    if "most rts options are disabled" in low:
        return {
            "headline": "FDR4 rejected +RTS arguments (build was compiled with -rtsopts=some).",
            "summary": "FDR4 rejected the +RTS heap-cap flag.",
            "kind": "runtime_config_error",
            "title": "refines.exe rejected +RTS arguments",
            "fix_directive": (
                "refines.exe was built with restricted RTS options. Remove "
                "any +RTS arguments from the run_fdr4 command in "
                "forge.dashboard/web/runners.py."
            ),
            "error": stderr_text[:2000],
        }

    if "out of memory" in low or "getmblocks" in low or "heap exhausted" in low:
        return {
            "headline": "FDR4 ran out of memory.",
            "summary": "FDR4 exhausted the GHC heap before producing results.",
            "kind": "memory_error",
            "title": "FDR4 out of memory",
            "fix_directive": (
                "Reduce the state space (shrink phases.fdr4 type_ranges in "
                "pipeline.yaml or the corrections/type_ranges.json overrides) "
                "or grow the Windows page file / RAM and retry."
            ),
            "error": stderr_text[:2000],
        }

    # Fall through: no special classification, let the default
    # parse_error builder format the error.
    return {
        "headline": "CSP-M parse error:",
        "summary": "FDR4 could not parse the generated CSP-M.",
        "error": stderr_text[:2000],
    }


def _discover_fdr4_csp_file(ctx: PhaseContext) -> "Path | None":
    """Auto-discover the FDR4 input CSP file under <output>/csp-gen/defs/.

    Preference order:
      1. *_System_Module_coreassertions.csp  — multi-controller aggregate
      2. *<Stm>Controller_coreassertions.csp — single-controller controller-only
                                              (excludes *_Module_*, *_Ctrl_*,
                                              *_InputEnv_*, *_OutputEnv_*)
      3. *_Module_coreassertions.csp         — single-controller module level
                                              (wraps controller + InputEnv +
                                              OutputEnv; larger state space)
      4. exactly one *_coreassertions.csp    — fallback when only one exists

    Tier 2 was added after the SRanger convergence-replay experiment
    (experiments/convergence/sranger/) showed that Module-level assertions
    over the composed (controller || InputEnv || OutputEnv) system can
    exceed FDR4's state-space budget at [0..1] type ranges on Windows,
    returning "inconclusive" for every assertion. The controller-only
    coreassertions cover the same deadlock-freedom / divergence-freedom /
    determinism properties on the controller itself with a much smaller
    exploration cost — when that variant is present (which it always is
    for single-controller studies), it should be preferred.

    Returns the chosen file path, or None if none qualifies. The function
    is deliberately quiet on ambiguity (multiple candidates at the same
    preference tier) — the caller can still pin csp_file explicitly in
    pipeline.yaml if the default heuristic picks the wrong one.
    """
    defs_dir = ctx.paths.transformations_output / "csp-gen" / "defs"
    if not defs_dir.exists():
        return None

    candidates = sorted(defs_dir.glob("*_coreassertions.csp"))
    if not candidates:
        return None

    # Tier 1: multi-controller aggregate
    for p in candidates:
        if "_System_Module_coreassertions.csp" in p.name:
            return p
    # Tier 2: single-controller controller-only (no Module / Ctrl / env infix)
    for p in candidates:
        name = p.name
        if (
            "Controller_coreassertions.csp" in name
            and "_Module_" not in name
            and "_Ctrl_" not in name
            and "_InputEnv_" not in name
            and "_OutputEnv_" not in name
        ):
            return p
    # Tier 3: single-controller module level
    for p in candidates:
        if "_Module_coreassertions.csp" in p.name:
            return p
    # Tier 4: unique fallback
    if len(candidates) == 1:
        return candidates[0]
    # Multiple controller-level files and no module-level aggregate —
    # surface the ambiguity rather than picking arbitrarily.
    names = ", ".join(p.name for p in candidates)
    ctx.sys_msg(
        f"FDR4: multiple *_coreassertions.csp candidates and no "
        f"*_Module_coreassertions.csp aggregate — auto-discovery cannot "
        f"choose. Pin one in pipeline.yaml phases.fdr4.csp_file. "
        f"Candidates: {names}"
    )
    return None


def run(ctx: PhaseContext) -> PhaseResult:
    """Run FDR4 verification as a subprocess."""
    phase_cfg = ctx.config
    label = ctx.phase.label if ctx.phase else "FDR4 Verification"

    raw_fdr4 = _resolve_os_path(phase_cfg.get("fdr4_path", ""))
    fdr4_path = _resolve_tool(phase_cfg.get("fdr4_path", ""))
    csp_file = phase_cfg.get("csp_file", "")
    timeout = phase_cfg.get("timeout", 600)
    expected = phase_cfg.get("expected_failures", [])
    memory_limit_mb = phase_cfg.get("memory_limit_mb", 8192)

    if not fdr4_path:
        msg = (f"FDR4 executable not found: {raw_fdr4 or '(empty)'}. "
               "Add its install dir to PATH or set "
               "phases.fdr4.fdr4_path to an absolute path in pipeline.yaml.")
        ctx.sys_msg(msg)
        return "failed", msg

    # Resolve CSP file. Two paths:
    # (a) pipeline.yaml's csp_file points at an existing file → use it
    #     (preserves explicit pin; useful when the user wants to verify a
    #     specific controller rather than the top-level module).
    # (b) otherwise auto-discover under <output_dir>/csp-gen/defs/ in
    #     preference order:
    #       1. <Diagram>_System_Module_coreassertions.csp  (multi-controller)
    #       2. <Stm>_Module_coreassertions.csp             (single-controller)
    #       3. unique *_coreassertions.csp file            (fallback)
    # The auto-discovery keeps pipeline.yaml case-study-independent — the
    # original hardcoded "LreController_coreassertions.csp" path broke as
    # soon as the active_case_study switched.
    csp_path = ctx.paths.root / csp_file if csp_file else None
    if csp_path is None or not csp_path.exists():
        csp_path = _discover_fdr4_csp_file(ctx)
        if csp_path is None:
            msg = (
                "FDR4 CSP file not found. Auto-discovery looked under "
                f"{ctx.paths.transformations_output / 'csp-gen' / 'defs'} for "
                "*_System_Module_coreassertions.csp, *_Module_coreassertions.csp, "
                "or *_coreassertions.csp; nothing matched. Either the m2t phase "
                "did not produce csp-gen output, or pin a path explicitly in "
                "phases.fdr4.csp_file."
            )
            ctx.sys_msg(msg)
            return "failed", msg
        ctx.sys_msg(f"FDR4: auto-discovered CSP file {csp_path.name}")

    # Determinism is not a property the pipeline verifies. RoboChart gives a
    # state's outgoing transitions as concurrent choices (not a prioritised
    # list), and an autonomous guard-only transition compiles to a hidden
    # internal event, so the extracted models are non-deterministic by
    # construction even though the generated Java is deterministic. We
    # therefore strip the official generator's `:[deterministic]` assertions
    # before invoking FDR, so the property is neither checked nor reported.
    # (We cannot suppress emission: the assertions come from the vendored
    # RoboChart CSP generator, not our templates.)
    try:
        original_text = csp_path.read_text(encoding="utf-8")
        det_lines = [ln for ln in original_text.splitlines()
                     if ln.lstrip().startswith("assert") and ":[deterministic]" in ln]
        if det_lines:
            filtered = "\n".join(
                ("-- determinism not verified (see runners.run_fdr4): " + ln)
                if (ln.lstrip().startswith("assert") and ":[deterministic]" in ln)
                else ln
                for ln in original_text.splitlines()
            )
            nodet_path = csp_path.with_name(
                csp_path.stem + "_nodet" + csp_path.suffix)
            nodet_path.write_text(filtered + "\n", encoding="utf-8")
            ctx.sys_msg(
                f"FDR4: determinism not verified; commented out "
                f"{len(det_lines)} :[deterministic] assertion(s).")
            csp_path = nodet_path
    except OSError as exc:
        logger.warning("Could not filter determinism assertions: %s", exc)

    # refines.exe is built with restricted RTS options ("-rtsopts=some"
    # or similar), so +RTS -M<N>m -RTS to cap GHC's heap is rejected
    # ("Most RTS options are disabled"). The only knobs we have are
    # the python-side memory monitor below and the user's Windows
    # page-file size. We detect the page-file failure from stderr and
    # surface an actionable message.
    cmd = [fdr4_path, "--format", "framed_json", str(csp_path)]
    logger.info("Running FDR4: %s", " ".join(cmd))
    env = os.environ.copy()
    env["LC_ALL"] = "C"

    try:
        proc = subprocess.Popen(
            cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE,
            text=True, encoding="utf-8", errors="replace", env=env,
        )
        # Wall-clock timeout: proc.wait(timeout=...) below only runs after
        # stdout EOF (process exit), so it alone cannot stop a hung FDR4.
        watchdog, watchdog_fired = _start_watchdog(proc, timeout)

        # Memory monitoring thread
        peak_memory_mb = [0]
        killed_for_memory = threading.Event()

        def monitor_memory():
            while proc.poll() is None and not ctx.stop_requested():
                mem = _get_process_memory_mb(proc.pid)
                if mem > peak_memory_mb[0]:
                    peak_memory_mb[0] = mem
                ctx.sink.event({
                    "type": "fdr4_memory",
                    "memory_mb": round(mem),
                    "peak_mb": round(peak_memory_mb[0]),
                    "limit_mb": memory_limit_mb,
                })
                if mem > memory_limit_mb:
                    logger.error("FDR4 exceeded memory limit: %.0f MB > %d MB", mem, memory_limit_mb)
                    killed_for_memory.set()
                    proc.kill()
                    return
                time.sleep(1)

        mem_thread = threading.Thread(target=monitor_memory, daemon=True)
        mem_thread.start()

        stderr_chunks: list[str] = []

        def drain_stderr():
            if proc.stderr is None:
                return
            try:
                for chunk in iter(lambda: proc.stderr.read(4096), ""):
                    stderr_chunks.append(chunk)
            except (OSError, ValueError):
                pass

        stderr_thread = threading.Thread(target=drain_stderr, daemon=True)
        stderr_thread.start()

        output_lines = []
        for line in proc.stdout:
            if ctx.stop_requested():
                proc.terminate()
                stderr_thread.join(timeout=2)
                return "stopped", ""
            stripped = line.rstrip("\r\n")
            output_lines.append(stripped)

        proc.wait(timeout=timeout)
        watchdog.cancel()
        if watchdog_fired.is_set() and not killed_for_memory.is_set():
            # Killed by the wall-clock watchdog: route through the same
            # handling as a wait() timeout.
            raise subprocess.TimeoutExpired(cmd, timeout)
        stderr_thread.join(timeout=5)
        stderr_text = "".join(stderr_chunks)

        mem_thread.join(timeout=2)
        ctx.sink.event({
            "type": "fdr4_memory",
            "memory_mb": 0,
            "peak_mb": round(peak_memory_mb[0]),
            "limit_mb": memory_limit_mb,
        })

        if killed_for_memory.is_set():
            msg = (f"FDR4 killed: memory usage exceeded {memory_limit_mb} MB limit "
                   f"(peak: {peak_memory_mb[0]:.0f} MB)")
            # SALVAGE: same rule as the timeout path — verdicts FDR4
            # completed before the RSS kill are real and already in
            # output_lines; report them rather than discarding.
            partial = []
            try:
                partial = _parse_framed_json("\n".join(output_lines))
            except Exception:
                partial = []
            n_done = sum(len(r.get("results", [])) for r in partial)
            if n_done:
                msg += f" — {n_done} assertion verdict(s) completed before the kill (salvaged)"
            ctx.sys_msg(msg)
            trace_data = feedback.load_trace(ctx.paths.transformations_output)
            # Route through the classifier (kind=fdr4_memory_kill) — a bare
            # {"error": msg} fell through to the parse_error builder,
            # mislabelling a state-space memory kill as a template defect
            # (observed on chem run-1 v2 at the 3600s re-measure).
            classified = _classify_fdr4_stderr(msg, memory_limit_mb)
            if n_done:
                classified["salvaged_results"] = partial
            fb = feedback.build_fdr4_feedback(
                label=label, status="failed", summary=msg,
                failed_assertions=[],
                errors=[classified],
                csp_file=csp_path,
                trace_data=trace_data,
                expected_failures=[],
            )
            emit_feedback(ctx, fb)
            return "failed", msg

        # Parse framed JSON results
        full_output = "\n".join(output_lines)
        raw_results = _parse_framed_json(full_output)
        stderr_stats = _parse_stderr_stats(stderr_text)

        if not raw_results and proc.returncode is not None and proc.returncode < 0:
            # Amendment 14: a signal ended FDR4 (returncode -N) with no verdict.
            stderr_text = (f"FDR4 was killed by signal {-proc.returncode} before giving a verdict "
                           f"(peak memory seen by the monitor: {peak_memory_mb[0]:.0f} MB).\n"
                           + (stderr_text or "") + "\n" + "\n".join(output_lines[-20:]))
        if not raw_results and stderr_text:
            classified = _classify_fdr4_stderr(stderr_text, memory_limit_mb)
            ctx.sys_msg(f"{classified['headline']}\n{stderr_text[:2000]}")
            trace_data = feedback.load_trace(ctx.paths.transformations_output)
            err_entry: dict[str, str] = {
                "error": classified["error"],
                "raw": stderr_text[:2000],
            }
            for key in ("kind", "title", "fix_directive"):
                if key in classified:
                    err_entry[key] = classified[key]
            fb = feedback.build_fdr4_feedback(
                label=label, status="failed",
                summary=classified["summary"],
                failed_assertions=[],
                errors=[err_entry],
                csp_file=csp_path,
                trace_data=trace_data,
                expected_failures=[],
            )
            emit_feedback(ctx, fb)
            return "failed", classified["summary"]

        assertions = []
        errors = []
        # Inverse of the exit-status-vs-verdict conflation: the verdict
        # below is computed purely from parsed frames, so a refines crash
        # AFTER emitting some passing frames (nonzero exit, partial
        # results) would otherwise report "completed". Surface the crash
        # as an error so has_issues flips the phase to failed.
        if proc.returncode not in (0, None):
            errors.append({
                "error": (f"FDR4 exited with code {proc.returncode}; "
                          "parsed results may be partial."),
            })
        for frame in raw_results:
            if "errors" in frame and isinstance(frame["errors"], list):
                for err_msg in frame["errors"]:
                    if err_msg:
                        errors.append({"error": err_msg})
            for ar in frame.get("results", []):
                assertions.append(ar)
            if "error" in frame:
                errors.append(frame)

        passed = [a for a in assertions if a.get("result") in (1, True)]
        failed = [a for a in assertions if a.get("result") in (0, False)]
        inconclusive = [a for a in assertions if a not in passed and a not in failed]

        expected_failed = []
        unexpected_failed = []
        for a in failed:
            astr = a.get("assertion_string", "")
            if any(pat in astr for pat in expected):
                expected_failed.append(a)
            else:
                unexpected_failed.append(a)

        table_lines = [
            "| Assertion | Result | States | Transitions |",
            "|---|---|---|---|",
        ]
        for a in assertions:
            assert_str = a.get("assertion_string", "?")
            stats = _get_assertion_stats(a, stderr_stats)
            states_str = str(stats["states"]) if stats.get("states") is not None else "-"
            trans_str = str(stats["transitions"]) if stats.get("transitions") is not None else "-"

            if a in passed:
                result_str = "PASS"
            elif a in expected_failed:
                plys = stats.get("plys")
                result_str = f"EXPECTED FAIL (ply {plys})" if plys else "EXPECTED FAIL"
            elif a in failed:
                plys = stats.get("plys")
                result_str = f"FAIL (ply {plys})" if plys else "FAIL"
            else:
                result_str = "INCONCLUSIVE"

            table_lines.append(
                f"| {assert_str} | {result_str} | {states_str} | {trans_str} |"
            )

        summary_parts = [
            f"FDR4: {len(assertions)} assertion(s) checked",
            f"{len(passed)} passed",
        ]
        if unexpected_failed:
            summary_parts.append(f"{len(unexpected_failed)} failed")
        if expected_failed:
            summary_parts.append(f"{len(expected_failed)} expected failure(s)")
        if inconclusive:
            summary_parts.append(f"{len(inconclusive)} inconclusive")
        summary_parts.append(f"{len(errors)} error(s)")
        ctx.sys_msg(", ".join(summary_parts))

        table_msg = {
            "type": "agent_message",
            "phase": ctx.phase.id or "",
            "agent": "fdr4",
            "content": "\n".join(table_lines),
        }
        ctx.sink.event(table_msg)
        ctx.state.add_message(table_msg)

        for a in failed:
            assert_str = a.get("assertion_string", "?")
            counterexamples = a.get("counterexamples", [])
            traces = []
            for ce in counterexamples:
                trace = ce.get("trace", [])
                if trace:
                    traces.append(" -> ".join(str(ev) for ev in trace))
            if traces:
                trace_desc = "; ".join(traces)
                ctx.sys_msg(f"Counterexample for {assert_str}: {trace_desc}")

        for e in errors:
            ctx.sys_msg(f"ERROR: {e.get('error', '?')}")

        has_issues = bool(unexpected_failed) or bool(errors) or bool(inconclusive)

        event_map: dict[str, str] = {}
        for frame in raw_results:
            fmap = frame.get("event_map", {}) or {}
            for eid, ename in fmap.items():
                event_map[str(eid)] = str(ename)

        trace_data = feedback.load_trace(ctx.paths.transformations_output)
        fb_status = "failed" if has_issues else "passed"
        summary_parts_fb = [
            f"{len(assertions)} assertion(s) checked",
            f"{len(passed)} passed",
        ]
        if unexpected_failed:
            summary_parts_fb.append(f"{len(unexpected_failed)} failed")
        if expected_failed:
            summary_parts_fb.append(f"{len(expected_failed)} expected failure(s)")
        if inconclusive:
            summary_parts_fb.append(f"{len(inconclusive)} inconclusive")
        if errors:
            summary_parts_fb.append(f"{len(errors)} error(s)")
        fb = feedback.build_fdr4_feedback(
            label=label,
            status=fb_status,
            summary="FDR4: " + ", ".join(summary_parts_fb) + ".",
            failed_assertions=unexpected_failed,
            errors=errors,
            csp_file=csp_path,
            trace_data=trace_data,
            expected_failures=expected_failed,
            event_map=event_map,
        )
        emit_feedback(ctx, fb)

        if has_issues:
            return "failed", full_output[:2000]
        else:
            if expected_failed:
                ctx.sys_msg(
                    f"{label} completed — failures are expected "
                    f"({', '.join(expected)})."
                )
            else:
                ctx.sys_msg(f"{label} completed — all assertions passed.")
            return "completed", ""

    except subprocess.TimeoutExpired:
        proc.kill()
        msg = f"FDR4 timed out after {timeout}s"
        # SALVAGE (2026-09-14). FDR4 emits framed-JSON verdicts PER
        # ASSERTION as it completes them, and the read loop above has
        # already banked them in output_lines — discarding them threw
        # away every cheap verdict each time the composed-module
        # assertion blew the budget (chem run-1 v2: three attempts,
        # zero verdicts reported, while the per-controller assertions
        # had almost certainly finished). Parse what completed and
        # report it; only the assertions FDR4 never reached are
        # unmeasured.
        partial = []
        try:
            # output_lines may be unbound if the kill hit before the read
            # loop started (e.g. Popen failure routed here).
            partial = _parse_framed_json("\n".join(locals().get("output_lines") or []))
        except Exception:
            partial = []
        n_done = sum(len(r.get("results", [])) for r in partial)
        if n_done:
            msg += f" — {n_done} assertion verdict(s) completed before the kill (salvaged)"
        ctx.sys_msg(msg)
        trace_data = feedback.load_trace(ctx.paths.transformations_output)
        # Route through the classifier so the issue carries kind
        # "fdr4_timeout" — a bare {"error": msg} fell through to the
        # default parse_error builder, mislabelling a state-space
        # timeout as a template defect (observed on chem run-1 v2).
        classified = _classify_fdr4_stderr(msg, memory_limit_mb=0)
        if n_done:
            classified["salvaged_results"] = partial
        fb = feedback.build_fdr4_feedback(
            label=label, status="failed", summary=msg,
            failed_assertions=[],
            errors=[classified],
            csp_file=csp_path,
            trace_data=trace_data,
            expected_failures=[],
        )
        emit_feedback(ctx, fb)
        return "failed", msg
