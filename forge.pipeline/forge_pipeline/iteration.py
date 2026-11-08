"""One pipeline iteration: every phase in order, with the authoritative verdicts.

This is the loop the evaluation's sessions ran (``run_claude_iteration.sh``
called it as the headless driver). It owns three decisions the engine does
not make:

* the phase order;
* the CSP type-range correction applied before FDR4 runs;
* which verdict counts. The engine's status says a phase *finished*; the
  phase's own report (``post_<phase>.json``) says whether it *passed*, and
  the report wins. A report not rewritten since the phase started belongs to
  an earlier iteration and is replaced by a ``skipped`` verdict.

It writes ``phase_timings.json`` beside the reports, which the recorder reads.
"""
from __future__ import annotations

import json
import logging
import time
from dataclasses import dataclass
from pathlib import Path

from .engine import PipelineEngine
from .manifest import Manifest
from .paths import RepoPaths
from .sink import ConsoleSink, ProgressSink
from .state import SessionState

logger = logging.getLogger(__name__)

PHASES_IN_ORDER = [
    "compile", "coverage", "preflight", "t2m", "m2m", "m2t",
    "dafny_gen", "isabelle_gen", "fdr4", "dafny_verify", "isabelle_verify", "vacuity",
]
SUCCESS = frozenset({"passed", "completed"})


@dataclass
class PhaseOutcome:
    phase: str
    status: str        # the authoritative verdict
    source: str        # "post" (report), "stale" (report discarded), "exec" (no report)
    exec_status: str
    seconds: float

    @property
    def ok(self) -> bool:
        return self.status in SUCCESS


def authoritative_status(feedback_dir: Path, phase_id: str, exec_status: str,
                         started_at: float | None = None) -> tuple[str, str]:
    """``(status, source)`` for a phase, preferring the report's verdict."""
    post = feedback_dir / f"post_{phase_id}.json"
    if started_at is not None and post.exists() and post.stat().st_mtime < started_at - 0.5:
        stale = {
            "phase": phase_id, "status": "skipped",
            "summary": (f"{phase_id} did not run in this iteration (an upstream "
                        "phase failed or the phase did not execute). The previous "
                        "iteration's verdict was discarded."),
            "issues": [], "files_to_review": [],
            "new_count": 0, "recurring_count": 0, "resolved_count": 0,
            "resolved_issue_titles": [],
        }
        post.write_text(json.dumps(stale, indent=2), encoding="utf-8")
        post.with_suffix(".md").write_text(
            f"# {phase_id}: skipped\n\n{stale['summary']}\n", encoding="utf-8")
        return "skipped", "stale"
    try:
        data = json.loads(post.read_text(encoding="utf-8"))
        verdict = data.get("status")
        if isinstance(verdict, str) and verdict:
            return verdict, "post"
    except FileNotFoundError:
        pass
    except Exception as e:
        logger.warning("could not read post_%s.json: %s", phase_id, e)
    return exec_status, "exec"


def write_timings(feedback_dir: Path, outcomes: list[PhaseOutcome]) -> Path:
    feedback_dir.mkdir(parents=True, exist_ok=True)
    path = feedback_dir / "phase_timings.json"
    path.write_text(json.dumps({
        "phases": {o.phase: {"status": o.status, "wall_clock_s": round(o.seconds, 2)} for o in outcomes},
        "pipeline_wall_clock_s": round(sum(o.seconds for o in outcomes), 2),
    }, indent=2), encoding="utf-8")
    return path


class Iteration:
    def __init__(self, paths: RepoPaths | None = None, sink: ProgressSink | None = None) -> None:
        self.paths = paths or RepoPaths.from_root()
        self.sink = sink or ConsoleSink()
        self.state = SessionState()
        self.engine = PipelineEngine(Manifest.load(self.paths.pipeline_yaml), self.state,
                                     self.sink, self.paths)

    def _before(self, phase_id: str) -> None:
        if phase_id == "fdr4":
            # instantiations.csp's type ranges are narrowed before FDR4 runs.
            from .csp.corrections import apply_csp_corrections
            try:
                result = apply_csp_corrections(str(self.paths.pipeline_yaml))
                self.sink.system(f"[pre-fdr4] applied csp corrections to {result.name}" if result
                                 else "[pre-fdr4] no csp corrections applied (file missing?)", phase_id)
            except Exception as e:
                self.sink.system(f"[pre-fdr4] csp corrections failed: {e}", phase_id)

    def run_phase(self, phase_id: str) -> PhaseOutcome:
        self._before(phase_id)
        t0 = time.time()
        try:
            self.engine.run_phase(phase_id)
        except Exception as e:
            self.sink.system(f"EXCEPTION: {e}", phase_id)
            self.state.set_phase_status(phase_id, "failed")
        elapsed = time.time() - t0
        exec_status = self.state.get_phase_status(phase_id)
        status, source = authoritative_status(self.paths.feedback_dir, phase_id, exec_status, started_at=t0)
        return PhaseOutcome(phase_id, status, source, exec_status, elapsed)

    def run(self, phases: list[str] | None = None) -> list[PhaseOutcome]:
        outcomes = [self.run_phase(p) for p in (phases or PHASES_IN_ORDER)]
        write_timings(self.paths.feedback_dir, outcomes)
        return outcomes
