"""The pipeline engine: runs the phases that ``pipeline.yaml`` declares.

One class, one job: given a manifest, a session state and a progress sink,
run a phase by its id and write its feedback report. Phase *kinds* are
dispatched to small strategies:

* ``python`` phases are looked up in :data:`forge_pipeline.phases.REGISTRY`
  and run in-process through a :class:`~forge_pipeline.phases.PhaseContext`;
* ``java`` and ``gradle`` phases run one gradle invocation in
  ``forge.transformations``;
* ``sequence`` phases run several java/gradle steps in order.

The engine does not know about threads, sockets or the command line; the
:mod:`~forge_pipeline.cli` adds the iteration loop and the console sink.
"""
from __future__ import annotations

import json
import logging
import re
import shutil
import subprocess
from pathlib import Path
from typing import Callable

from . import feedback
from .manifest import Manifest, Phase
from .paths import RepoPaths
from .sink import NullSink, ProgressSink
from .state import SessionState

logger = logging.getLogger(__name__)


# Lines of gradle output that carry no information for the reader.
GRADLE_NOISE = re.compile(
    r"^("
    r"(> Task |:)\S+"
    r"|Downloading\s+http"
    r"|Download\s+http"
    r"|BUILD SUCCESSFUL"
    r"|\d+ actionable tasks?:"
    r"|Starting a Gradle Daemon"
    r"|Deprecated Gradle features"
    r"|You can use '--warning-mode all'"
    r"|For more on this, please refer to"
    r"|Reusing configuration cache"
    r"|Configuration cache entry"
    r")"
)

_ERROR_KEYWORDS = ("error", "exception", "failed", "fatal", "caused by", "case not treated")


def count_lint_errors(lint_report: Path) -> int | None:
    """Count error-severity violations in a ``lint_report.json``.

    Returns ``None`` when the report is missing or unreadable; the caller
    treats that as a failure, not a pass (a linter that crashed after the
    gradle wrapper exited 0 must not count as "no structural errors").
    """
    if not lint_report.exists():
        return None
    try:
        data = json.loads(lint_report.read_text(encoding="utf-8"))
    except (json.JSONDecodeError, OSError):
        return None
    return sum(1 for v in data.get("violations", []) if v.get("severity") == "error")


def consolidate_traces(t2m_output: Path, codegen_output: Path | None = None) -> None:
    """Merge the per-stage trace files into ``trace_full.json``.

    ``codegen_output`` is the directory holding ``result_codegen.json``. The
    pipeline as evaluated passed a directory that did not exist, so every
    recorded ``trace_full.json`` was consolidated without the codegen trace;
    the engine keeps that behaviour by passing ``None``. Supplying the
    generated project here is a change to the pipeline, not a refactoring.
    """
    from .trace.postprocess import consolidate, parse_csp_trace

    def _load(path: Path) -> dict | None:
        if path.exists():
            return json.loads(path.read_text(encoding="utf-8"))
        return None

    m2m = _load(t2m_output / "trace_m2m.json")
    if not m2m:
        logger.warning("Missing trace_m2m.json, skipping consolidation")
        return

    csp_file = None
    csp_defs = t2m_output / "csp-gen" / "defs"
    if csp_defs.exists():
        csp_file = (next(iter(sorted(csp_defs.glob("*_Module_coreassertions.csp"))), None)
                    or next(iter(sorted(csp_defs.glob("*_coreassertions.csp"))), None))

    out = consolidate(
        codegen=_load(codegen_output / "result_codegen.json") if codegen_output else None,
        t2m=_load(t2m_output / "trace_t2m.json"),
        m2m=m2m,
        m2t_rct=_load(t2m_output / "trace_m2t_rct.json"),
        dafny=_load(t2m_output / "trace_dafny.json"),
        csp=parse_csp_trace(csp_file),
        isa=_load(t2m_output / "trace_isabelle.json"),
    )
    out_path = t2m_output / "trace_full.json"
    out_path.write_text(json.dumps(out, indent=2, ensure_ascii=False), encoding="utf-8")
    logger.info("Consolidated trace: %s (%d entries)", out_path, len(out.get("traces", [])))


class PipelineEngine:
    """Runs one phase at a time and writes its feedback report."""

    def __init__(self, manifest: Manifest, state: SessionState | None = None,
                 sink: ProgressSink | None = None, paths: RepoPaths | None = None,
                 stop_requested: Callable[[], bool] | None = None) -> None:
        self.manifest = manifest
        self.state = state or SessionState()
        self.sink = sink or NullSink()
        self.paths = paths or RepoPaths.from_root()
        self._stop_requested = stop_requested or (lambda: False)
        self._current_phase: str | None = None
        self._last_lint_lines: list[str] = []

    # -- messages ---------------------------------------------------------
    def sys_msg(self, text: str, phase: str = "") -> None:
        phase = phase or self._current_phase or ""
        self.sink.system(text, phase)
        self.state.add_message({"type": "agent_message", "phase": phase,
                                "agent": "system", "content": text})

    def stream_line(self, text: str, phase: str = "") -> None:
        self.sink.stream(text, phase or self._current_phase or "")

    def emit_feedback(self, fb: feedback.Feedback) -> None:
        """Persist a feedback record and announce it."""
        try:
            md_path, _ = feedback.write_feedback(self.paths.feedback_dir, fb)
        except Exception:
            logger.exception("Failed to write feedback for phase %s", fb.phase)
            return
        rel = md_path.relative_to(self.paths.root) if md_path.is_absolute() else md_path
        self.sys_msg(f"Feedback written: {rel.as_posix()}")

    # -- running a phase --------------------------------------------------
    def run_phase(self, phase_id: str) -> tuple[str, str]:
        """Run one phase; return ``(status, error_msg)`` and record it in the state."""
        phase = self.manifest.phase(phase_id)
        kind = phase.runner["kind"]
        self._current_phase = phase_id
        self.state.set_phase_status(phase_id, "running")
        status, error_msg = "completed", ""
        try:
            if kind == "python":
                status, error_msg = self._run_python_phase(phase)
            elif kind in ("java", "gradle"):
                status, error_msg = self._run_gradle_phase(phase)
            elif kind == "sequence":
                status, error_msg = self._run_sequence_phase(phase)
            else:
                status, error_msg = "failed", f"Unknown runner kind: {kind}"
                self.sys_msg(error_msg)
        except Exception as e:
            if self._stop_requested():
                status = "stopped"
            else:
                logger.exception("Phase %s raised an exception", phase_id)
                status = "failed"
                error_msg = f"[{phase.label}] Error: {e}"
                self.state.set_phase_error(phase_id, str(e))
                self.sys_msg(error_msg)
        finally:
            self._current_phase = None

        self.state.set_phase_status(phase_id, status)
        self.state.save()
        self.sink.phase_complete(phase_id, status, error_msg if status == "failed" else "")
        return status, error_msg

    def _run_python_phase(self, phase: Phase) -> tuple[str, str]:
        from .phases import REGISTRY, PhaseContext
        runner = REGISTRY.resolve(phase.runner["function"])
        ctx = PhaseContext(
            phase=phase, config=phase.config, state=self.state, paths=self.paths,
            sys_msg=self.sys_msg, stream=self.stream_line,
            stop_requested=self._stop_requested, emit_feedback=self.emit_feedback,
            sink=self.sink,
        )
        return runner(ctx)

    # -- gradle -----------------------------------------------------------
    def invoke_gradle(self, args: list[str], label: str, cwd: Path | None = None) -> tuple[str, str]:
        """Run ``gradlew`` with ``args`` in ``forge.transformations``; stream its
        output; return ``(status, error_msg)``."""
        work_dir = cwd or self.paths.transformations
        cmd = [str(self.paths.transformations / self.paths.gradlew)] + args
        logger.info("Running command: %s", " ".join(cmd))

        status, error_msg = "completed", ""
        error_lines: list[str] = []
        in_error_block = False
        lint_lines: list[str] = []

        proc = subprocess.Popen(cmd, cwd=str(work_dir), stdout=subprocess.PIPE,
                                stderr=subprocess.STDOUT, text=True,
                                encoding="utf-8", errors="replace")
        for line in proc.stdout:
            if self._stop_requested():
                proc.terminate()
                break
            stripped = line.rstrip("\r\n")
            logger.debug("[gradle] %s", stripped)
            if "[deadlock-lint]" in stripped:
                lint_lines.append(stripped)
            is_error_line = any(kw in stripped.lower() for kw in _ERROR_KEYWORDS)
            if is_error_line:
                in_error_block = True
                error_lines.append(stripped)
            elif in_error_block and stripped.strip() and not stripped.strip().startswith(">"):
                error_lines.append(stripped)
            clean = stripped.strip()
            if clean and not GRADLE_NOISE.match(clean):
                self.stream_line(clean)

        try:
            proc.wait(timeout=10)
        except subprocess.TimeoutExpired:
            logger.warning("gradle child did not exit within 10s; killing.")
            proc.kill()
            try:
                proc.wait(timeout=5)
            except subprocess.TimeoutExpired:
                pass

        # Tasks registered with ignoreExitValue (the RoboChart CSP generator)
        # print ``ERROR:`` lines and still exit 0.
        csp_gen_errors = [l for l in error_lines if l.lstrip().startswith("ERROR:")]

        if self._stop_requested():
            status = "stopped"
            self.sys_msg(f"{label} stopped by user.")
        elif (proc.returncode != 0 or csp_gen_errors
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
            self.sys_msg(msg)
            error_msg = msg

        self._last_lint_lines = lint_lines
        return status, error_msg

    def _verify_phase_outputs(self, phase: Phase) -> tuple[bool, str]:
        """A phase that completes must have produced its declared outputs."""
        out_dir = phase.output_dir
        if not out_dir or not phase.output_files:
            return True, ""
        missing = [p for p in phase.output_files if not list(out_dir.glob(p))]
        if not missing:
            return True, ""
        msg = (f"{phase.label} reported completion but produced no files "
               f"matching: {', '.join(missing)}. Check whether the gradle "
               f"task silently no-op'd (e.g. RoboChart CSP generator "
               f"errors with ignoreExitValue=true).")
        self.sys_msg(msg)
        return False, msg

    def _clean_phase_outputs(self, phase: Phase) -> None:
        """Remove a phase's declared outputs before it runs, so fresh
        artefacts are not mixed with stale ones."""
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
                    removed += 1
                except OSError as exc:
                    logger.warning("pre-clean: cannot rmtree %s: %s", cdir, exc)
        if removed:
            self.sys_msg(f"Pre-cleaned {removed} stale output entr"
                         f"{'y' if removed == 1 else 'ies'} for {phase.id}.")

    @staticmethod
    def _gradle_args(kind: str, spec: dict, phase_or_class: str, args: dict) -> list[str]:
        if kind == "java":
            args_str = " ".join(f"{k}={v}" for k, v in args.items())
            run_arg = f"{phase_or_class} {args_str}".strip() if phase_or_class else args_str
            return ["run", f"--args={run_arg}"]
        props = spec.get("props", {})
        return [spec["task"]] + [f"-P{k}={v}" for k, v in props.items()]

    def _run_gradle_phase(self, phase: Phase) -> tuple[str, str]:
        runner = phase.runner
        label = phase.label
        gradle_args = self._gradle_args(runner["kind"], runner, phase.id, phase.args)

        self.sys_msg(f"{label} starting...")
        self._clean_phase_outputs(phase)
        status, error_msg = self.invoke_gradle(gradle_args, label=label)

        if status == "completed":
            ok, verr = self._verify_phase_outputs(phase)
            if not ok:
                status, error_msg = "failed", verr

        if status == "completed":
            self.sys_msg(f"{label} completed successfully.")
            self._list_output_files(phase)
            self._emit_gradle_feedback(phase, fb_status="passed", error_summary="")
        elif status == "failed":
            self._emit_gradle_feedback(phase, fb_status="failed", error_summary=error_msg or "")
        return status, error_msg

    def _run_sequence_phase(self, phase: Phase) -> tuple[str, str]:
        label = phase.label
        status, error_msg, error_summary = "completed", "", ""

        self.sys_msg(f"{label} starting...")
        # Clear once at phase start: the steps build one artefact together.
        self._clean_phase_outputs(phase)
        for step in phase.runner["steps"]:
            if self._stop_requested():
                status = "stopped"
                break
            step_kind = step["kind"]
            step_label = step.get("label", label)
            if step_label:
                self.sys_msg(f"{step_label}...")
            if step_kind not in ("java", "gradle"):
                status, error_msg = "failed", f"Unsupported sequence step kind: {step_kind}"
                self.sys_msg(error_msg)
                break
            gradle_args = self._gradle_args(step_kind, step, step.get("class", ""), step.get("args", {}))
            step_status, step_err = self.invoke_gradle(gradle_args, label=step_label)
            if step_status != "completed":
                status, error_msg, error_summary = step_status, step_err, step_err
                break

        if status == "completed":
            ok, verr = self._verify_phase_outputs(phase)
            if not ok:
                status, error_msg, error_summary = "failed", verr, verr

        t2m_output = self.paths.transformations_output
        if status == "completed":
            self.sys_msg(f"{label} completed successfully.")
            if phase.id == "m2t":
                try:
                    consolidate_traces(t2m_output)
                    self.sys_msg("End-to-end trace consolidated: trace_full.json")
                except Exception as e:
                    logger.warning("Trace consolidation failed: %s", e)
            self._list_output_files(phase)
            self._emit_gradle_feedback(phase, fb_status="passed", error_summary="")
        elif status == "failed":
            if phase.id == "m2t":
                # Do not let a prior run's trace leak into this iteration's reports.
                stale_trace = t2m_output / "trace_full.json"
                if stale_trace.exists():
                    try:
                        stale_trace.unlink()
                        self.sys_msg("Removed stale trace_full.json (m2t failed; "
                                     "downstream feedback should not read prior-run trace).")
                    except OSError as e:
                        logger.warning("Could not remove stale trace_full.json: %s", e)
            self._emit_gradle_feedback(phase, fb_status="failed", error_summary=error_summary)
        return status, error_msg

    def _list_output_files(self, phase: Phase) -> None:
        out_dir = phase.output_dir
        if out_dir and Path(out_dir).exists():
            files = sorted(f for f in Path(out_dir).rglob("*") if f.is_file())
            if files:
                self.sys_msg(
                    f"Output ({len(files)} file(s) in {Path(out_dir).name}/):\n"
                    + "\n".join(f"  {f.relative_to(out_dir)} ({f.stat().st_size / 1024:.1f} KB)"
                                for f in files))

    def _emit_gradle_feedback(self, phase: Phase, fb_status: str, error_summary: str) -> None:
        label, command_id = phase.label, phase.id
        summary = f"{label} completed successfully." if fb_status == "passed" else f"{label} failed."
        t2m_output = self.paths.transformations_output
        trace_data = feedback.load_trace(t2m_output)
        if command_id == "preflight":
            lint_report = t2m_output / "lint_report.json"
            error_count = count_lint_errors(lint_report)
            if fb_status == "passed" and error_count is None:
                fb_status = "failed"
                summary = (f"{label}: lint_report.json is missing or unreadable "
                           f"— the linter's result cannot be confirmed, so "
                           f"preflight is treated as failed.")
            elif fb_status == "passed" and error_count > 0:
                fb_status = "failed"
                summary = (f"{label}: {error_count} structural error(s) "
                           f"found. Downstream phases would produce a "
                           f"wrong formal model.")
            elif fb_status == "passed":
                summary = f"{label}: no structural errors."
            fb = feedback.build_preflight_feedback(
                label=label, status=fb_status, summary=summary,
                lint_report_path=lint_report, error_summary=error_summary,
                trace_data=trace_data)
        else:
            fb = feedback.build_gradle_phase_feedback(
                phase_id=command_id, label=label, status=fb_status, summary=summary,
                error_summary=error_summary, trace_data=trace_data,
                lint_lines=self._last_lint_lines or [])
        self.emit_feedback(fb)
