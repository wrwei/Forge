"""Phase 6c: Isabelle build of the generated Z-Machine theories (natively or through Docker), with lemma scanning and the obligation context for the report."""
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


# Named obligations: `lemma foo: "..."`. NOTE the companion pattern below —
# the Z-machine generator also emits ANONYMOUS obligations (`lemma "P preserves
# R1 under inv"`), which this regex cannot match because there is no name to
# capture. Counting only named declarations under-reported every run's
# obligation total: chemical_detector has 17 obligations but only 9 named, so
# the success-path message claimed "9 lemmas verified" for a session that
# discharged 17. Both patterns are now used for COUNTS; names come from the
# named pattern alone.
_LEMMA_DECL_RE_RUNNER = re.compile(
    r"^\s*(?:lemma|theorem|corollary)\b\s+(\w+)\s*(?:\[[^\]]*\]\s*)?:")


_LEMMA_ANON_RE_RUNNER = re.compile(
    r"^\s*(?:lemma|theorem|corollary)\b\s+[\"\u201c]")


def _isabelle_obligation_context(session_dir: Path, full_output: str) -> str:
    """Build the per-obligation context block for an isabelle_verify FAILURE.

    A bare "exit 142, 1 theory marker(s) seen" tells a reader nothing about how
    much of the theory was proven. Isabelle aborts at the first failing proof,
    so the run itself cannot establish the status of the obligations after it —
    but it CAN name the inventory and the obligation that failed, and point at
    the measurement that settles the rest.

    Measured on chemical_detector run-1 (2026-09-16): of 17 obligations, the 16
    non-deadlock ones build clean in 55s, while the single
    GasAnalysisController_deadlock_free obligation resists both generated
    tactics (auto OOM-killed at 978s, metis timed out at 1825s). The binary
    verdict had been hiding 16 successful proofs.

    Deliberately does NOT claim the un-reached obligations discharged: that
    requires the arm-C build (scripts/isabelle_tactic_probe.sh), and asserting
    it from a failed run would be the kind of accounting a referee should catch.
    """
    stats = _scan_isabelle_lemmas(session_dir)
    total = stats.get("total_lemmas", 0)
    if not total:
        return ""
    # the failing obligation: Isabelle names the theory, and our .thy line
    # references let us narrow to the lemma when the message carries one
    named = sorted(set(re.findall(r"\b(\w*deadlock_free\w*)\b", full_output)))
    suspect = named[0] if named else (stats.get("deadlock_free_names") or [""])[0]
    lines = [
        "",
        "## Obligation inventory",
        "",
        f"- Obligations in the session: **{total}** across "
        f"{stats.get('theory_count', 0)} theor"
        f"{'y' if stats.get('theory_count', 0) == 1 else 'ies'}",
    ]
    for t in stats.get("by_theory", []):
        lines.append(
            f"- `{t['theory']}`: {t.get('obligation_count', len(t['lemmas']))} "
            f"obligations ({t.get('invariant_count', 0)} invariant-preservation, "
            f"{t.get('anonymous', 0)} requirement-preservation (anonymous), "
            f"{len(t.get('deadlock_free', []))} deadlock-freedom)"
        )
    if suspect:
        lines += [
            "",
            f"Isabelle aborts at the first failing proof, and the failure is in "
            f"`{suspect}`. **The obligations after it were not reached — they are "
            f"unverified in this run, not disproven.**",
        ]
    lines += [
        "",
        "To measure them separately, build the session with the failing lemma "
        "removed (`scripts/isabelle_tactic_probe.sh`, arm C). That is a "
        "measurement of what already discharges — never a substitute for "
        "closing the obligation.",
    ]
    return "\n".join(lines) + "\n"


def _scan_isabelle_lemmas(session_dir: Path) -> dict:
    """Scan .thy files in ``session_dir`` for lemma declarations.

    Returns::

        {
          "theory_count": int,
          "total_lemmas": int,
          "deadlock_free_names": [str, ...],
          "by_theory": [{
              "theory": "<stem>",
              "lemmas": [str, ...],
              "deadlock_free": [str, ...],
              "invariant_count": int,
          }, ...],
        }

    Used on the success path so the chat message and feedback body can
    name exactly what was proven.
    """
    out = {
        "theory_count": 0,
        "total_lemmas": 0,
        "anonymous_count": 0,
        "deadlock_free_names": [],
        "by_theory": [],
    }
    try:
        thy_files = sorted(session_dir.glob("*.thy"))
    except OSError:
        return out
    for thy in thy_files:
        try:
            text = thy.read_text(encoding="utf-8")
        except OSError:
            continue
        names = [
            m.group(1)
            for line in text.splitlines()
            for m in [_LEMMA_DECL_RE_RUNNER.match(line)]
            if m
        ]
        # anonymous obligations carry no name but are real proof obligations;
        # count them so totals match the theory rather than the named subset
        anon = sum(
            1 for line in text.splitlines()
            if _LEMMA_ANON_RE_RUNNER.match(line)
        )
        if not names and not anon:
            continue
        deadlock_free = [n for n in names if n.endswith("_deadlock_free")]
        invariants = [n for n in names if n.endswith("_inv")]
        out["theory_count"] += 1
        # total = named + anonymous. Reporting only the named subset understated
        # every run (chem: 9 of 17). `lemmas` keeps the NAMES for display;
        # `obligation_count` is the honest total.
        out["total_lemmas"] += len(names) + anon
        out["anonymous_count"] += anon
        out["deadlock_free_names"].extend(deadlock_free)
        out["by_theory"].append({
            "theory": thy.stem,
            "lemmas": names,
            "anonymous": anon,
            "obligation_count": len(names) + anon,
            "deadlock_free": deadlock_free,
            "invariant_count": len(invariants),
        })
    return out


def _emit_isabelle_feedback(ctx: PhaseContext, fb_status: str, summary: str,
                            raw_output: str,
                            success_stats: dict | None = None) -> None:
    """Build + write the unified feedback record for isabelle_verify."""
    trace_data = feedback.load_trace(ctx.paths.transformations_output)
    # Mirror the session_dir resolution from run_isabelle_verify so the
    # feedback builder can enrich Timeout/Interrupt issues with the .thy
    # line number of the lemma Isabelle was working on (and use it to
    # count lemmas on the success path).
    session_dir_cfg = (ctx.config or {}).get("session_dir", "")
    if session_dir_cfg:
        session_dir = (ctx.paths.root / session_dir_cfg).resolve()
    else:
        session_dir = (ctx.paths.transformations_output / "isabelle").resolve()
    fb = feedback.build_isabelle_feedback(
        label="Isabelle Verification",
        status=fb_status,
        summary=summary,
        raw_output=raw_output,
        trace_data=trace_data,
        session_dir=session_dir if session_dir.exists() else None,
        success_stats=success_stats,
    )
    emit_feedback(ctx, fb)


def run(ctx: PhaseContext) -> PhaseResult:
    """Run `isabelle build` on the session at output/isabelle/.

    On macOS/Linux: invokes the configured isabelle binary directly.
    On Windows: invokes via wsl.exe and translates the session path
    to /mnt/c/... form. The CyPhyAssure distribution is Linux-only.
    """
    phase_cfg = ctx.config
    label = ctx.phase.label if ctx.phase else "Isabelle Verification"

    raw_isabelle = _resolve_os_path(phase_cfg.get("isabelle_path", ""))
    isabelle_path = _resolve_tool(phase_cfg.get("isabelle_path", ""))
    wsl_isabelle_bin = phase_cfg.get("wsl_isabelle_bin", "")
    wsl_distro = phase_cfg.get("wsl_distro", "")
    timeout = phase_cfg.get("timeout", 1800)
    proof_timeout = phase_cfg.get("proof_timeout", 600)
    memory_limit_mb = phase_cfg.get("memory_limit_mb", 16384)

    session_dir_cfg = phase_cfg.get("session_dir", "")
    if session_dir_cfg:
        session_dir = (ctx.paths.root / session_dir_cfg).resolve()
    else:
        session_dir = (ctx.paths.transformations_output / "isabelle").resolve()

    if not session_dir.exists():
        msg = (f"Isabelle session directory not found: {session_dir}. "
               "Run phase 5c (Isabelle Theory Generation) first.")
        ctx.sys_msg(msg)
        _emit_isabelle_feedback(ctx, "failed", msg, msg)
        return "failed", msg

    # Decide branch from the *raw* (user-written) value so a missing
    # wsl.exe still produces a wsl-flavoured error, not a generic one.
    is_wsl = sys.platform == "win32" and "wsl" in raw_isabelle.lower()

    if is_wsl:
        if not isabelle_path:
            msg = (f"wsl.exe not found on PATH (configured as {raw_isabelle!r}). "
                   "Install WSL or set phases.isabelle_verify.isabelle_path "
                   "to an absolute path.")
            ctx.sys_msg(msg)
            _emit_isabelle_feedback(ctx, "failed", msg, msg)
            return "failed", msg
        if not wsl_isabelle_bin:
            msg = ("isabelle_path is wsl.exe but wsl_isabelle_bin is "
                   "not configured. Set phases.isabelle_verify.wsl_isabelle_bin "
                   "to the path of `isabelle` inside WSL.")
            ctx.sys_msg(msg)
            _emit_isabelle_feedback(ctx, "failed", msg, msg)
            return "failed", msg
        wsl_session = _windows_to_wsl_path(str(session_dir))
        # `:; ` no-op prefix prevents wsl.exe from path-translating the
        # first token: with Git Bash installed, `/bin/echo` would be
        # rewritten to `C:/Program Files/Git/usr/bin/echo` (and the
        # same for /home/... mappings), breaking the bash -lc command.
        # A leading `:;` keeps the first token benign.
        inner = (
            f':; {wsl_isabelle_bin} build -D {wsl_session} '
            f'-v -o timeout={proof_timeout}'
        )
        # When the default WSL distro is something other than the one
        # holding Isabelle (e.g. docker-desktop on machines with Docker
        # Desktop installed), -d <distro> forces the right target.
        cmd = [isabelle_path]
        if wsl_distro:
            cmd += ["-d", wsl_distro]
        cmd += ["--", "bash", "-lc", inner]
    else:
        if not isabelle_path:
            msg = (f"Isabelle executable not found: {raw_isabelle or '(empty)'}. "
                   "Add the install dir to PATH or set "
                   "phases.isabelle_verify.isabelle_path to an absolute path. "
                   "See docs/design/isabelle_wsl_setup.md.")
            ctx.sys_msg(msg)
            _emit_isabelle_feedback(ctx, "failed", msg, msg)
            return "failed", msg
        cmd = [isabelle_path, "build", "-D", str(session_dir),
               "-v", "-o", f"timeout={proof_timeout}"]

    logger.info("Running Isabelle: %s", " ".join(cmd))
    ctx.sys_msg(f"{label}: starting (this can take minutes — "
                f"first run cold loads the Z_Machine heap)...")

    try:
        proc = subprocess.Popen(
            cmd, stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
            text=True, encoding="utf-8", errors="replace",
        )
    except FileNotFoundError as exc:
        msg = f"Failed to launch Isabelle: {exc}"
        ctx.sys_msg(msg)
        _emit_isabelle_feedback(ctx, "failed", msg, msg)
        return "failed", msg

    peak_memory_mb = [0]
    killed_for_memory = threading.Event()
    # Wall-clock timeout: the stdout read loop below blocks until EOF, so
    # proc.wait(timeout=...) alone cannot stop a hung isabelle build.
    watchdog, watchdog_fired = _start_watchdog(proc, timeout)

    def monitor_memory():
        while proc.poll() is None and not ctx.stop_requested():
            mem = _get_process_memory_mb(proc.pid)
            if mem > peak_memory_mb[0]:
                peak_memory_mb[0] = mem
            if mem > memory_limit_mb:
                logger.error("Isabelle exceeded memory limit: %.0f MB", mem)
                killed_for_memory.set()
                proc.kill()
                return
            time.sleep(2)

    mem_thread = threading.Thread(target=monitor_memory, daemon=True)
    mem_thread.start()

    output_lines: list[str] = []
    try:
        for line in proc.stdout:
            if ctx.stop_requested():
                proc.terminate()
                return "stopped", ""
            stripped = line.rstrip("\r\n")
            output_lines.append(stripped)
            if stripped.strip():
                ctx.stream(stripped)
        proc.wait(timeout=timeout)
        if watchdog_fired.is_set() and not killed_for_memory.is_set():
            raise subprocess.TimeoutExpired(cmd, timeout)
    except subprocess.TimeoutExpired:
        proc.kill()
        msg = f"Isabelle timed out after {timeout}s"
        ctx.sys_msg(msg)
        # Append the obligation inventory: a bare timeout message gave the
        # reader no idea how much of the theory was proven.
        _out = "\n".join(output_lines)
        _emit_isabelle_feedback(
            ctx, "failed", msg,
            _out + _isabelle_obligation_context(session_dir, _out))
        return "failed", msg
    finally:
        watchdog.cancel()
        mem_thread.join(timeout=2)

    full_output = "\n".join(output_lines)

    if killed_for_memory.is_set():
        msg = (f"Isabelle killed: memory exceeded {memory_limit_mb} MB "
               f"(peak {peak_memory_mb[0]:.0f} MB)")
        ctx.sys_msg(msg)
        _emit_isabelle_feedback(
            ctx, "failed", msg,
            full_output + _isabelle_obligation_context(session_dir, full_output))
        return "failed", msg

    # Isabelle 2023 `build -v` emits per-theory markers as
    # `<session>: theory <session>.<theory>` — one per theory processed.
    # No per-theory "Finished (Xs)" line is printed, so count from the
    # session-theory markers; total elapsed comes from the closing line.
    theory_markers = re.findall(
        r"^\s*(\S+):\s+theory\s+\S+\.(\w+)\s*$",
        full_output,
        re.MULTILINE,
    )
    elapsed = re.search(r"^([\d:]+) elapsed time", full_output, re.MULTILINE)
    failed_session = re.search(r"Session\s+(\S+)\s+FAILED", full_output)
    any_star_error = "***" in full_output

    if proc.returncode == 0 and not failed_session and not any_star_error:
        elapsed_str = elapsed.group(1) if elapsed else ""
        # Scan the .thy files in session_dir for lemma counts + names so
        # both the chat message and the feedback body can be specific
        # about what was actually proven (not just "all proofs closed").
        # NOTE: reuse the session_dir resolved from phase config above —
        # recomputing the default here silently ignored a configured
        # session_dir and scanned the wrong directory.
        lemma_stats = _scan_isabelle_lemmas(session_dir)
        # Compact chat message: theory count + total lemmas + named
        # deadlock-freedom result (the user-facing safety property).
        chat_bits = [
            f"{lemma_stats['theory_count']} "
            f"theor{'y' if lemma_stats['theory_count'] == 1 else 'ies'}",
            f"{lemma_stats['total_lemmas']} "
            f"lemma{'' if lemma_stats['total_lemmas'] == 1 else 's'} verified",
        ]
        if lemma_stats["deadlock_free_names"]:
            dfs = ", ".join(f"`{n}`" for n in lemma_stats["deadlock_free_names"])
            chat_bits.append(f"deadlock-freedom proved for {dfs}")
        if elapsed_str:
            chat_bits.append(f"elapsed {elapsed_str}")
        ctx.sys_msg(f"{label} — " + "; ".join(chat_bits) + ".")
        summary = "Isabelle: " + ", ".join([
            f"{lemma_stats['theory_count']} theor"
            f"{'y' if lemma_stats['theory_count'] == 1 else 'ies'}, "
            f"{lemma_stats['total_lemmas']} lemma(s) verified",
            f"elapsed {elapsed_str}" if elapsed_str else "",
            f"peak {peak_memory_mb[0]:.0f} MB",
        ]).replace(", ,", ",") + "."
        _emit_isabelle_feedback(
            ctx, "passed", summary, full_output,
            success_stats={
                "theory_markers": theory_markers,
                "elapsed": elapsed_str,
                "peak_memory_mb": int(peak_memory_mb[0]),
                "lemma_stats": lemma_stats,
            },
        )
        return "completed", ""

    summary_parts = [f"Isabelle: exit {proc.returncode}"]
    if theory_markers:
        summary_parts.append(f"{len(theory_markers)} theory marker(s) seen before failure")
    if failed_session:
        summary_parts.append(f"session {failed_session.group(1)} FAILED")
    summary = ", ".join(summary_parts) + "."
    ctx.sys_msg(f"{label} failed — see Feedback tab.")
    # This is the path chem run-1 took (exit 142, tactic timeout). The summary
    # alone — "1 theory marker(s) seen before failure" — says nothing about how
    # many obligations the session carries or which one aborted it, so the
    # feedback body now carries the inventory and names the failing obligation.
    _emit_isabelle_feedback(
        ctx, "failed", summary,
        full_output + _isabelle_obligation_context(session_dir, full_output))
    return "failed", full_output[:2000]
