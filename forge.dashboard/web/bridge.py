"""Pipeline bridge: runs deterministic phases in background threads with WebSocket output."""
from __future__ import annotations

import asyncio
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

import yaml

from web import feedback
from web.manifest import Manifest
from web.state import SessionState

logger = logging.getLogger(__name__)

# Paths relative to repo root
_DASHBOARD_DIR = Path(__file__).resolve().parent.parent


def _resolve_os_path(value) -> str:
    """Resolve an OS-aware path config value.

    Accepts either a plain string or a dict mapping platform keys
    (``darwin``, ``win32``, ``linux``) to paths.  Returns the path
    for the current platform, or ``""`` if not configured.
    """
    if isinstance(value, dict):
        return value.get(sys.platform, "")
    if isinstance(value, str):
        return value
    return ""
_REPO_ROOT = _DASHBOARD_DIR.parent
_T2M_DIR = _REPO_ROOT / "forge.transformations"
_GENERATED_PROJECT = _REPO_ROOT / "java.generated.project"
_GENERATED_SRC = _GENERATED_PROJECT / "src" / "main" / "java"
_T2M_OUTPUT = _T2M_DIR / "output"
_CODEGEN_TRACE_FILE = _GENERATED_PROJECT / "result_codegen.json"
# Unified verification-feedback directory (phase post_*.md / post_*.json).
# NOTE: forge.dashboard/corrections/ is separate — it holds user CSP
# config (type_ranges.json, csp_overrides.csp) and is unchanged.
_FEEDBACK_DIR = _REPO_ROOT / "forge.assets" / "corrections"
_GRADLEW = "gradlew.bat" if sys.platform == "win32" else "./gradlew"

# Interactive phases not in pipeline.yaml (handled outside dashboard).
_INTERACTIVE_PHASES = [
    {"id": "requirements",  "label": "1 — Requirements Elicitation"},
    {"id": "code_synthesis", "label": "2 — Java Code Generation (Interactive)"},
    {"id": "refinement",    "label": "7 — Closed-Loop Refinement (Interactive)"},
]



def _count_lint_errors(lint_report: Path) -> int:
    """Count error-severity violations in a lint_report.json. Warnings
    do not count (preflight passes with warnings, fails only on errors)."""
    if not lint_report.exists():
        return 0
    try:
        data = json.loads(lint_report.read_text(encoding="utf-8"))
    except (json.JSONDecodeError, OSError):
        return 0
    return sum(
        1 for v in data.get("violations", [])
        if v.get("severity") == "error"
    )


def _update_csp_file_in_config(config_path: str) -> None:
    """Discover the generated *_coreassertions.csp and update config.yaml."""
    config_file = Path(config_path)
    with open(config_file, "r", encoding="utf-8") as f:
        config = yaml.safe_load(f)

    defs_dir = _T2M_OUTPUT / "csp-gen" / "defs"
    if not defs_dir.exists():
        return

    candidates = sorted(defs_dir.glob("*_coreassertions.csp"))
    if not candidates:
        logger.warning("No *_coreassertions.csp found in %s", defs_dir)
        return

    controller_candidates = [
        c for c in candidates
        if "Controller_coreassertions" in c.name
        and "_Ctrl_" not in c.name
        and "_Module_" not in c.name
    ]
    csp_file = controller_candidates[0] if controller_candidates else candidates[0]
    rel_path = os.path.relpath(csp_file, config_file.parent).replace("\\", "/")

    old_path = config.get("phases", {}).get("fdr4", {}).get("csp_file", "")
    if old_path == rel_path:
        return

    config_text = config_file.read_text(encoding="utf-8")
    if old_path:
        config_text = config_text.replace(
            f'csp_file: "{old_path}"', f'csp_file: "{rel_path}"',
        )
    else:
        config_text = config_text.replace(
            "  fdr4:\n", f'  fdr4:\n    csp_file: "{rel_path}"\n',
        )
    config_file.write_text(config_text, encoding="utf-8")
    logger.info("Updated fdr4.csp_file in config: %s", rel_path)


def _consolidate_traces(t2m_output: Path, codegen_output: Path) -> None:
    """Merge per-stage trace files into a single trace_full.json."""
    def _load(path: Path) -> dict | None:
        if path.exists():
            return json.loads(path.read_text(encoding="utf-8"))
        return None

    codegen = _load(codegen_output / "result_codegen.json")
    t2m = _load(t2m_output / "trace_t2m.json")
    m2m = _load(t2m_output / "trace_m2m.json")
    m2t = _load(t2m_output / "trace_m2t.json")
    m2t_rct = _load(t2m_output / "trace_m2t_rct.json")

    if not m2m:
        logger.warning("Missing trace_m2m.json, skipping consolidation")
        return

    # Build lookups
    codegen_by_name: dict[str, list[dict]] = {}
    if codegen:
        for entry in codegen.get("codegen_trace", []):
            name = entry.get("java_element", "")
            codegen_by_name.setdefault(name, []).append(entry)

    t2m_by_suffix: dict[str, list] = {}
    if t2m:
        for entry in t2m.get("source_positions", []):
            qn = entry.get("qualified_name", "")
            simple = qn.rsplit(".", 1)[-1] if "." in qn else qn
            t2m_by_suffix.setdefault(simple, []).append(entry)

    m2t_by_name: dict[str, dict] = {}
    if m2t:
        for entry in m2t.get("mappings", []):
            m2t_by_name[entry.get("robochart_element", "")] = entry

    rct_by_name: dict[str, dict] = {}
    if m2t_rct:
        for entry in m2t_rct.get("mappings", []):
            rct_by_name[entry.get("robochart_element", "")] = entry

    traces = []
    for m2m_entry in m2m.get("mappings", []):
        rc_type = m2m_entry.get("robochart_type", "")
        rc_name = m2m_entry.get("robochart_element", "")

        trace: dict[str, Any] = {
            "robochart_type": rc_type,
            "robochart_element": rc_name,
        }

        if rc_type == "Transition":
            for key in ("source_state", "target_state", "trigger_event"):
                if key in m2m_entry:
                    trace[key] = m2m_entry[key]

        if rc_name in m2t_by_name:
            m2t_entry = m2t_by_name[rc_name]
            trace["csp_line_start"] = m2t_entry.get("csp_line_start")
            trace["csp_line_end"] = m2t_entry.get("csp_line_end")

        if rc_name in rct_by_name:
            rct_entry = rct_by_name[rc_name]
            trace["rct_line_start"] = rct_entry.get("rct_line_start")
            trace["rct_line_end"] = rct_entry.get("rct_line_end")

        java_name = rc_name
        m2m_java_elem = m2m_entry.get("java_element", "")
        cg_entries = codegen_by_name.get(java_name) or codegen_by_name.get(m2m_java_elem)
        if cg_entries:
            trace["requirement_ids"] = list({e["requirement_gid"] for e in cg_entries})
            trace["java_file"] = cg_entries[0].get("java_file", "")

        if "java_file" not in trace and m2m_entry.get("java_file"):
            trace["java_file"] = m2m_entry["java_file"]
        if "java_line_start" not in trace and m2m_entry.get("java_line_start"):
            trace["java_line_start"] = m2m_entry["java_line_start"]
            trace["java_line_end"] = m2m_entry.get("java_line_end")

        t2m_entries = (t2m_by_suffix or {}).get(java_name) or (t2m_by_suffix or {}).get(m2m_java_elem)
        if t2m_entries and "java_line_start" not in trace:
            trace["java_line_start"] = t2m_entries[0].get("line_start")
            trace["java_line_end"] = t2m_entries[0].get("line_end")
            trace["java_source_file"] = t2m_entries[0].get("file", "")

        traces.append(trace)

    out_path = t2m_output / "trace_full.json"
    out_path.write_text(
        json.dumps({"traces": traces}, indent=2, ensure_ascii=False),
        encoding="utf-8",
    )
    logger.info("Consolidated trace: %s (%d entries)", out_path, len(traces))


class PipelineBridge:
    """Runs deterministic pipeline phases in background threads."""

    def __init__(self, config_path: str, state: SessionState) -> None:
        self.config_path = config_path
        self.state = state
        self.manifest = Manifest.load(_REPO_ROOT / "pipeline.yaml")
        self._thread: threading.Thread | None = None
        self._stop_event = threading.Event()
        self._current_phase: str | None = None
        self._ws = None
        self._loop = None

    @property
    def is_running(self) -> bool:
        return self._thread is not None and self._thread.is_alive()

    def _send(self, msg: dict) -> None:
        """Send a JSON message to the WebSocket (thread-safe)."""
        if self._ws and self._loop:
            asyncio.run_coroutine_threadsafe(
                self._ws.send_json(msg), self._loop,
            )

    def _sys_msg(self, text: str, phase: str = "") -> None:
        """Send a system message to the chat."""
        msg = {
            "type": "agent_message",
            "phase": phase or self._current_phase or "",
            "agent": "system",
            "content": text,
        }
        self._send(msg)
        self.state.add_message(msg)

    def _emit_feedback(self, fb: feedback.Feedback) -> None:
        """Persist a unified feedback record and announce it to the chat."""
        try:
            md_path, _ = feedback.write_feedback(_FEEDBACK_DIR, fb)
        except Exception:
            logger.exception("Failed to write feedback for phase %s", fb.phase)
            return
        rel = md_path.relative_to(_REPO_ROOT) if md_path.is_absolute() else md_path
        self._sys_msg(f"Feedback written: {rel.as_posix()}")

    def _stream_line(self, text: str, phase: str = "") -> None:
        """Stream a single output line to the chat."""
        self._send({
            "type": "agent_stream",
            "phase": phase or self._current_phase or "",
            "agent": "pipeline",
            "content": text,
        })

    def get_pipeline_structure(self) -> list[dict]:
        """Return the ordered pipeline phase list for the UI tree."""
        structure = list(_INTERACTIVE_PHASES[:2])  # requirements + code_synthesis
        for phase in self.manifest.ordered():
            entry: dict = {"id": phase.id, "label": phase.label, "command": phase.id}
            if phase.depends_on:
                entry["depends_on"] = phase.depends_on
            structure.append(entry)
        structure.append(_INTERACTIVE_PHASES[2])  # refinement
        return structure

    def start_command(self, command_id: str, ws, loop: asyncio.AbstractEventLoop) -> None:
        """Launch a pipeline phase (dispatched via manifest)."""
        if self.is_running:
            asyncio.run_coroutine_threadsafe(
                ws.send_json({"type": "error", "message": "A phase is already running"}),
                loop,
            )
            return

        try:
            self.manifest.phase(command_id)
        except KeyError:
            asyncio.run_coroutine_threadsafe(
                ws.send_json({"type": "error", "message": f"Unknown command: {command_id}"}),
                loop,
            )
            return

        self._stop_event.clear()
        self._current_phase = command_id
        self._ws = ws
        self._loop = loop
        self.state.set_phase_status(command_id, "running")

        self._thread = threading.Thread(
            target=self._run_command,
            args=(command_id,),
            daemon=True,
        )
        self._thread.start()

    # Patterns for Gradle noise lines that should be suppressed
    _GRADLE_NOISE = re.compile(
        r"^("
        r"(> Task |:)\S+"
        r"|Downloading\s+http"
        r"|Download\s+http"
        r"|\s*$"
        r"|[\[\]]{1,2}"
        r"|BUILD SUCCESSFUL in"
        r"|[0-9]+ actionable task"
        r"|> Configure project"
        r"|Picked up JAVA_TOOL_OPTIONS"
        r"|WARNING:.*--args"
        r")",
        re.IGNORECASE,
    )

    def _run_command(self, command_id: str) -> None:
        """Dispatch a phase by runner kind and stream output to WebSocket."""
        phase = self.manifest.phase(command_id)
        kind = phase.runner["kind"]

        status = "completed"
        error_msg = ""
        try:
            if kind == "python":
                from web import runners
                runner_fn = runners.PYTHON_RUNNERS[phase.runner["function"]]
                ctx = runners.RunnerContext(
                    phase=phase,
                    config=phase.config,
                    state=self.state,
                    send=self._send,
                    sys_msg=self._sys_msg,
                    stop_requested=self._stop_event.is_set,
                    current_phase=command_id,
                    emit_feedback=self._emit_feedback,
                )
                status, error_msg = runner_fn(ctx)
            elif kind in ("java", "gradle"):
                status, error_msg = self._run_gradle_phase(phase)
            elif kind == "sequence":
                status, error_msg = self._run_sequence_phase(phase)
            else:
                status = "failed"
                error_msg = f"Unknown runner kind: {kind}"
                self._sys_msg(error_msg)
        except Exception as e:
            if self._stop_event.is_set():
                status = "stopped"
            else:
                logger.exception("Command %s raised an exception", command_id)
                status = "failed"
                error_msg = f"[{phase.label}] Error: {e}"
                self.state.set_phase_error(command_id, str(e))
                self._sys_msg(error_msg)
        finally:
            self._current_phase = None

        self.state.set_phase_status(command_id, status)
        self.state.save()
        event: dict[str, Any] = {"type": "phase_complete", "status": status, "phase": command_id}
        if status == "failed" and error_msg:
            event["error"] = error_msg
        self._send(event)

    def _invoke_gradle(self, args: list[str], label: str, cwd: "Path | None" = None) -> tuple[str, str]:
        """Invoke gradlew with the given args list; stream stdout; return (status, error_msg)."""
        gradlew = str(_T2M_DIR / _GRADLEW)
        work_dir = cwd or _T2M_DIR
        cmd = [gradlew] + args
        logger.info("Running command: %s", " ".join(cmd))

        status = "completed"
        error_msg = ""
        error_lines: list[str] = []
        in_error_block = False
        # Linter warnings emitted by the ETL (e.g. `[deadlock-lint]
        # GasAnalysisController.Reading: ...`) appear on stdout but
        # aren't errors — capture them separately so the post-phase
        # feedback can surface them as advisory issues without flipping
        # status to failed.
        lint_lines: list[str] = []

        proc = subprocess.Popen(
            cmd, cwd=str(work_dir),
            stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
            text=True, encoding="utf-8", errors="replace",
        )

        for line in proc.stdout:
            if self._stop_event.is_set():
                proc.terminate()
                break
            stripped = line.rstrip("\r\n")
            logger.debug("[gradle] %s", stripped)

            if "[deadlock-lint]" in stripped:
                lint_lines.append(stripped)

            is_error_line = any(kw in stripped.lower() for kw in
                   ("error", "exception", "failed", "fatal",
                    "caused by", "case not treated"))
            if is_error_line:
                in_error_block = True
                error_lines.append(stripped)
            elif in_error_block and stripped.strip() and not stripped.strip().startswith(">"):
                error_lines.append(stripped)

            clean = stripped.strip()
            if clean and not self._GRADLE_NOISE.match(clean):
                self._stream_line(clean)

        try:
            proc.wait(timeout=10)
        except subprocess.TimeoutExpired:
            logger.warning("gradle child did not exit within 10s; killing.")
            proc.kill()
            try:
                proc.wait(timeout=5)
            except subprocess.TimeoutExpired:
                pass

        exit_code = proc.returncode

        # Some gradle tasks (notably the RoboChart CSP generator) are
        # registered with ``ignoreExitValue = true``. They print
        # ``ERROR:Couldn't resolve...`` to stdout but still exit 0, so
        # the returncode check below would miss them. Detect those
        # explicitly: any line starting with ``ERROR:`` (with optional
        # whitespace) flips the status to failed.
        csp_gen_errors = [l for l in error_lines
                          if l.lstrip().startswith("ERROR:")]

        if self._stop_event.is_set():
            status = "stopped"
            self._sys_msg(f"{label} stopped by user.")
        elif (proc.returncode != 0
              or csp_gen_errors
              or any("case not treated" in l.lower() for l in error_lines)):
            status = "failed"
            error_summary = "\n".join(error_lines[:20])
            if proc.returncode != 0:
                msg = f"{label} failed with exit code {proc.returncode}."
            else:
                msg = (f"{label} reported errors to stdout while still "
                       f"exiting 0 (silent failure — gradle task ignored "
                       f"its own exit value).")
            if error_summary:
                msg += "\n" + error_summary
            self._sys_msg(msg)
            error_msg = msg

        # Stash linter lines on the bridge so the caller's
        # _emit_gradle_feedback path can pick them up.
        self._last_lint_lines = lint_lines
        return status, error_msg

    def _verify_phase_outputs(self, phase) -> tuple[bool, str]:
        """After a phase runs, verify its declared ``output_files``
        actually exist. Returns (ok, error_msg).

        Catches the case where a gradle task exits 0 and prints nothing
        recognisable as an error to stdout — yet still produces no
        artefacts. The RoboChart CSP generator with model-level errors
        is the canonical case this guards against.
        """
        out_dir = phase.output_dir
        if not out_dir or not phase.output_files:
            return True, ""
        missing: list[str] = []
        for pattern in phase.output_files:
            matches = list(out_dir.glob(pattern))
            if not matches:
                missing.append(pattern)
        if not missing:
            return True, ""
        msg = (f"{phase.label} reported completion but produced no files "
               f"matching: {', '.join(missing)}. Check whether the gradle "
               f"task silently no-op'd (e.g. RoboChart CSP generator "
               f"errors with ignoreExitValue=true).")
        self._sys_msg(msg)
        return False, msg

    def _clean_phase_outputs(self, phase) -> None:
        """Wipe a phase's declared outputs *before* it runs.

        Consults the manifest's per-phase ``output_files`` (glob patterns
        relative to ``output_dir``) and ``clear_dirs`` (absolute dirs to
        rmtree). This is the same data the dashboard's "Clear output"
        button uses; without auto-invocation, gradle phases write fresh
        artifacts alongside stale ones from previous runs (e.g. after a
        case-study switch the previous controller's *.dfy / *.thy /
        defs/*.csp files stay on disk).

        Only called for gradle and sequence phases — verifier phases
        (FDR4, dafny_verify, isabelle_verify) *consume* gradle output
        and must not pre-clean.
        """
        import shutil
        removed = 0

        out_dir = phase.output_dir
        if out_dir and out_dir.exists():
            for pattern in (phase.output_files or []):
                for fp in out_dir.glob(pattern):
                    if fp.is_file():
                        try:
                            fp.unlink()
                            removed += 1
                        except OSError as exc:
                            logger.warning("pre-clean: cannot delete %s: %s", fp, exc)

        for cdir in (phase.clear_dirs or []):
            cdir = Path(cdir)
            if cdir.exists() and cdir.is_dir():
                try:
                    shutil.rmtree(cdir)
                    removed += 1  # count the dir as one entry
                except OSError as exc:
                    logger.warning("pre-clean: cannot rmtree %s: %s", cdir, exc)

        if removed:
            self._sys_msg(f"Pre-cleaned {removed} stale output entr"
                          f"{'y' if removed == 1 else 'ies'} for {phase.id}.")

    def _run_gradle_phase(self, phase) -> tuple[str, str]:
        """Run a single java or gradle kind phase."""
        runner = phase.runner
        label = phase.label
        kind = runner["kind"]
        if kind == "java":
            args_str = " ".join(f"{k}={v}" for k, v in phase.args.items())
            run_arg = f"{phase.id} {args_str}".strip()
            gradle_args = ["run", f"--args={run_arg}"]
        else:  # gradle
            task = runner["task"]
            props = runner.get("props", {})
            prop_args = [f"-P{k}={v}" for k, v in props.items()]
            gradle_args = [task] + prop_args

        self._sys_msg(f"{label} starting...")
        self._clean_phase_outputs(phase)
        status, error_msg = self._invoke_gradle(gradle_args, label=label)

        # Postcondition: declared outputs must actually exist.
        if status == "completed":
            ok, verr = self._verify_phase_outputs(phase)
            if not ok:
                status = "failed"
                error_msg = verr

        if status == "completed":
            self._sys_msg(f"{label} completed successfully.")
            self._list_output_files(phase)
            self._emit_gradle_feedback(phase, fb_status="passed", error_summary="")
        elif status == "failed":
            self._emit_gradle_feedback(phase, fb_status="failed",
                                       error_summary=error_msg or "")
        return status, error_msg

    def _run_sequence_phase(self, phase) -> tuple[str, str]:
        """Run a sequence of java/gradle steps for one phase."""
        label = phase.label
        status = "completed"
        error_msg = ""
        error_summary = ""

        self._sys_msg(f"{label} starting...")
        # Wipe declared outputs once at phase start (not per step) — the
        # csp-gen dir is a single artefact produced collectively by all
        # the steps, and clearing it between steps would delete an
        # earlier step's output that later steps depend on.
        self._clean_phase_outputs(phase)
        for step in phase.runner["steps"]:
            if self._stop_event.is_set():
                status = "stopped"
                break
            step_kind = step["kind"]
            step_label = step.get("label", label)
            if step_label:
                self._sys_msg(f"{step_label}...")

            if step_kind == "java":
                step_args = step.get("args", {})
                args_str = " ".join(f"{k}={v}" for k, v in step_args.items())
                cls = step.get("class", "")
                run_arg = f"{cls} {args_str}".strip() if cls else args_str
                gradle_args = ["run", f"--args={run_arg}"]
            elif step_kind == "gradle":
                task = step["task"]
                props = step.get("props", {})
                prop_args = [f"-P{k}={v}" for k, v in props.items()]
                gradle_args = [task] + prop_args
            else:
                status = "failed"
                error_msg = f"Unsupported sequence step kind: {step_kind}"
                self._sys_msg(error_msg)
                break

            step_status, step_err = self._invoke_gradle(gradle_args, label=step_label)
            if step_status != "completed":
                status = step_status
                error_msg = step_err
                error_summary = step_err
                break

        # Postcondition: declared outputs must actually exist. Sequence
        # phases (m2t) have output_files that span the whole pipeline of
        # steps, so we verify once after the last step.
        if status == "completed":
            ok, verr = self._verify_phase_outputs(phase)
            if not ok:
                status = "failed"
                error_msg = verr
                error_summary = verr

        if status == "completed":
            self._sys_msg(f"{label} completed successfully.")
            # After M2T: consolidate traces
            if phase.id == "m2t":
                try:
                    _consolidate_traces(_T2M_OUTPUT, _DASHBOARD_DIR / "output")
                    self._sys_msg("End-to-end trace consolidated: trace_full.json")
                except Exception as e:
                    logger.warning("Trace consolidation failed: %s", e)
            self._list_output_files(phase)
            self._emit_gradle_feedback(phase, fb_status="passed", error_summary="")
        elif status == "failed":
            # When M2T fails, delete trace_full.json so downstream phases'
            # feedback parsers don't read stale data from a prior run. The
            # bridge's consolidation only OVERWRITES on success, so without
            # this the prior controller's trace leaks into the next
            # iteration's "files to review" lists.
            if phase.id == "m2t":
                stale_trace = _T2M_OUTPUT / "trace_full.json"
                if stale_trace.exists():
                    try:
                        stale_trace.unlink()
                        self._sys_msg(
                            "Removed stale trace_full.json (m2t failed; "
                            "downstream feedback should not read prior-run trace).")
                    except OSError as e:
                        logger.warning("Could not remove stale trace_full.json: %s", e)
            self._emit_gradle_feedback(phase, fb_status="failed", error_summary=error_summary)

        return status, error_msg

    def _list_output_files(self, phase) -> None:
        """Emit a message listing output files for a completed phase."""
        out_dir = phase.output_dir
        if out_dir and Path(out_dir).exists():
            files = sorted(f for f in Path(out_dir).rglob("*") if f.is_file())
            if files:
                self._sys_msg(
                    f"Output ({len(files)} file(s) in {Path(out_dir).name}/):\n" +
                    "\n".join(
                        f"  {f.relative_to(out_dir)} ({f.stat().st_size / 1024:.1f} KB)"
                        for f in files
                    )
                )

    def _emit_gradle_feedback(self, phase, fb_status: str, error_summary: str) -> None:
        """Build and emit unified feedback for a gradle/java/sequence phase."""
        label = phase.label
        command_id = phase.id
        summary = (
            f"{label} completed successfully."
            if fb_status == "passed"
            else f"{label} failed."
        )
        trace_data = feedback.load_trace(_T2M_OUTPUT)
        if command_id == "preflight":
            lint_report = _T2M_OUTPUT / "lint_report.json"
            error_count = _count_lint_errors(lint_report)
            if fb_status == "passed" and error_count > 0:
                fb_status = "failed"
                summary = (
                    f"{label}: {error_count} structural error(s) "
                    f"found. Downstream phases would produce a "
                    f"wrong formal model."
                )
            elif fb_status == "passed":
                summary = f"{label}: no structural errors."
            fb = feedback.build_preflight_feedback(
                label=label,
                status=fb_status,
                summary=summary,
                lint_report_path=lint_report,
                error_summary=error_summary,
                trace_data=trace_data,
            )
        else:
            lint_lines = getattr(self, "_last_lint_lines", []) or []
            fb = feedback.build_gradle_phase_feedback(
                phase_id=command_id,
                label=label,
                status=fb_status,
                summary=summary,
                error_summary=error_summary,
                trace_data=trace_data,
                lint_lines=lint_lines,
            )
        self._emit_feedback(fb)

    @staticmethod
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

    @staticmethod
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

    @staticmethod
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

    @staticmethod
    def _get_process_memory_mb(pid: int) -> float:
        """Get total RSS memory usage of a process tree in MB."""
        try:
            if sys.platform == "win32":
                # Windows: use tasklist to get working set of the process
                result = subprocess.run(
                    ["tasklist", "/FI", f"PID eq {pid}", "/FO", "CSV", "/NH"],
                    capture_output=True, text=True, timeout=5,
                )
                if result.returncode == 0 and result.stdout.strip():
                    # Output: "name","pid","session","session#","mem usage"
                    for line in result.stdout.strip().splitlines():
                        parts = line.strip().strip('"').split('","')
                        if len(parts) >= 5:
                            mem_str = parts[4].replace('"', '').replace(',', '').replace(' K', '').replace(' ', '')
                            return int(mem_str) / 1024  # KB -> MB
            elif sys.platform == "darwin":
                # macOS: use ps to get RSS of process and children
                result = subprocess.run(
                    ["ps", "-o", "rss=", "-p", str(pid)],
                    capture_output=True, text=True, timeout=5,
                )
                if result.returncode == 0 and result.stdout.strip():
                    return int(result.stdout.strip()) / 1024  # KB -> MB
            elif sys.platform == "linux":
                status_file = Path(f"/proc/{pid}/status")
                if status_file.exists():
                    for line in status_file.read_text().splitlines():
                        if line.startswith("VmRSS:"):
                            return int(line.split()[1]) / 1024  # kB -> MB
        except Exception:
            pass
        return 0

    def _windows_to_wsl_path(p: str) -> str:
        """Translate a Windows-style path to a WSL /mnt/<drive>/... path.

        e.g. "C:\\Users\\Will\\foo" → "/mnt/c/Users/Will/foo".
        Pass through if the path already starts with "/" or doesn't have
        a Windows drive letter prefix.
        """
        if not p or p.startswith("/"):
            return p
        if len(p) >= 2 and p[1] == ":":
            drive = p[0].lower()
            rest = p[2:].replace("\\", "/")
            if rest.startswith("/"):
                rest = rest[1:]
            return f"/mnt/{drive}/{rest}"
        return p.replace("\\", "/")

    def stop_phase(self) -> None:
        """Request the running phase to stop.

        Signals the worker thread via ``_stop_event`` and waits for it to
        exit (bounded). The worker thread is the canonical emitter of
        ``phase_complete`` and ``set_phase_status`` — racing it from here
        produced two events and a status flip.

        If the worker doesn't exit within the timeout we fall back to
        emitting "stopped" ourselves so the UI doesn't get wedged, but
        the common case is the worker observing ``_stop_event`` and
        emitting its own "stopped" event.
        """
        if not self.is_running:
            return

        phase_name = self._current_phase
        thread = self._thread
        self._stop_event.set()

        # Give the worker a chance to observe the stop event and emit its
        # own phase_complete. Bound the wait so a wedged worker can't
        # deadlock the WebSocket loop.
        if thread is not None and thread.is_alive():
            thread.join(timeout=5)

        # Worker exited cleanly -> it has already emitted phase_complete
        # and set the status. Just clear our own bookkeeping.
        if thread is None or not thread.is_alive():
            self._thread = None
            self._current_phase = None
            self._ws = None
            self._loop = None
            return

        # Worker is wedged. Force the status + event ourselves so the
        # UI unsticks; the daemon worker will eventually exit on its own.
        if phase_name:
            self.state.set_phase_status(phase_name, "stopped")
            self.state.save()
            self._send({
                "type": "phase_complete",
                "status": "stopped",
                "phase": phase_name,
            })
        self._thread = None
        self._current_phase = None
        self._ws = None
        self._loop = None
