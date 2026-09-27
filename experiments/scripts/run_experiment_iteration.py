#!/usr/bin/env python3
"""Drive a single pipeline iteration end-to-end for the LRE convergence experiment.

Runs all 11 deterministic phases (2a-6c) in order using the same PipelineBridge
the dashboard uses, then prints a one-line pass/fail per phase. Feedback files
(forge.assets/corrections/post_*.{md,json}) are written by the bridge as a
side-effect — read them after running for richer diagnostics.

Usage: python experiments/scripts/run_experiment_iteration.py
"""
from __future__ import annotations

import json
import sys
import time
from pathlib import Path

# parents: [0]=scripts/, [1]=experiments/, [2]=REPO_ROOT
REPO_ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(REPO_ROOT / "forge.dashboard"))

# Force UTF-8 stdout so phase labels with arrows (e.g. "2b — Coverage
# (Requirement ↔ Java trace)") don't crash on Windows cp1252.
try:
    sys.stdout.reconfigure(encoding="utf-8")
    sys.stderr.reconfigure(encoding="utf-8")
except Exception:
    pass

from web.bridge import PipelineBridge
from web.state import SessionState


PHASES_IN_ORDER = [
    "compile",
    "coverage",
    "preflight",
    "t2m",
    "m2m",
    "m2t",
    "dafny_gen",
    "isabelle_gen",
    "fdr4",
    "dafny_verify",
    "isabelle_verify",
    "vacuity",
]


_SUCCESS = {"passed", "completed"}


def _authoritative_status(phase_id: str, exec_status: str,
                          started_at: float | None = None) -> tuple[str, str]:
    """Return ``(status, source)`` for a phase, preferring the verification
    verdict in ``forge.assets/corrections/post_<phase>.json`` over the
    in-memory *execution* status.

    The execution status (``state.get_phase_status``) only records that the
    phase finished without throwing — it reports ``completed``/``passed`` even
    when the phase's own feedback verdict is ``failed`` (the classic preflight
    masking that every convergence run had to re-discover). The ``status``
    field in the post-<phase> JSON is the authoritative pass/fail verdict, so
    we trust it whenever it exists. ``source`` is ``"post"`` (authoritative)
    or ``"exec"`` (fallback when no post file was written).
    """
    post = REPO_ROOT / "forge.assets" / "corrections" / f"post_{phase_id}.json"
    # STALE-VERDICT GUARD (2026-09-25). corrections/ is not cleared between
    # iterations (the feedback diff needs the previous file), so a phase that
    # did not execute -- e.g. fdr4 when its dependency m2t failed -- left the
    # PREVIOUS iteration's post file in place, and it was read here as this
    # iteration's verdict: three iterations recorded "FDR4 3 passed" in 0.0 s
    # with no CSP file present. A post file not rewritten since the phase
    # started is not this iteration's verdict. Record the phase as "skipped"
    # (not a success, so it can never count toward convergence) and replace
    # the stale files so neither the actor nor the recorder reads the old pass.
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
    except Exception as e:  # malformed JSON — surface it, don't mask
        print(f"  [warn] could not read post_{phase_id}.json: {e}", flush=True)
    return exec_status, "exec"


def main() -> int:
    import argparse
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--phases", nargs="*", default=None,
        help="Subset of phases to run (space-separated). Default: all 11 in order.",
    )
    args = parser.parse_args()
    phase_list = args.phases if args.phases else PHASES_IN_ORDER

    state = SessionState()
    bridge = PipelineBridge(str(REPO_ROOT / "pipeline.yaml"), state)

    def _send(msg: dict) -> None:
        t = msg.get("type", "")
        if t == "phase_complete":
            print(f"  -> phase_complete: status={msg.get('status')}", flush=True)
        elif t == "agent_message" and msg.get("agent") == "system":
            text = msg.get("content", "").strip()
            if text:
                print(f"  [sys] {text}", flush=True)

    bridge._send = _send

    results: dict[str, tuple[str, float]] = {}

    for phase_id in phase_list:
        print(f"\n===== {phase_id} =====", flush=True)
        # FDR4 needs instantiations.csp's type ranges flattened to {0..1}
        # before refines.exe runs, otherwise the model-derived ranges
        # (e.g. union({-2..2}, ...)) blow the Windows pagefile commit.
        # The dashboard does this via WebSocket; the CLI driver must do
        # it explicitly, since the bridge's _run_command for FDR4
        # doesn't include this step.
        if phase_id == "fdr4":
            from web.csp_corrections import apply_csp_corrections
            try:
                result = apply_csp_corrections(str(REPO_ROOT / "pipeline.yaml"))
                if result:
                    print(f"  [pre-fdr4] applied csp corrections to {result.name}", flush=True)
                else:
                    print("  [pre-fdr4] no csp corrections applied (file missing?)", flush=True)
            except Exception as e:
                print(f"  [pre-fdr4] csp corrections failed: {e}", flush=True)
        bridge._stop_event.clear()
        bridge._current_phase = phase_id
        state.set_phase_status(phase_id, "running")
        t0 = time.time()
        try:
            bridge._run_command(phase_id)
        except Exception as e:
            print(f"  EXCEPTION: {e}", flush=True)
            state.set_phase_status(phase_id, "failed")
        elapsed = time.time() - t0
        exec_status = state.get_phase_status(phase_id)
        status, source = _authoritative_status(phase_id, exec_status, started_at=t0)
        results[phase_id] = (status, elapsed)
        # If the authoritative verdict disagrees with the execution status,
        # call it out — this is exactly the masking that hid failed preflight
        # behind a "completed" CLI line in every convergence run.
        if source == "post" and status != exec_status:
            print(
                f"===== {phase_id}: {status} ({elapsed:.1f}s) "
                f"[authoritative post_{phase_id}.json; execution status was '{exec_status}'] =====",
                flush=True,
            )
        else:
            tag = {"post": "", "stale": " [not run -- previous verdict discarded]"}.get(
                source, " [execution status — no post file]")
            print(f"===== {phase_id}: {status} ({elapsed:.1f}s){tag} =====", flush=True)

    print("\n===== SUMMARY (authoritative post_<phase>.json verdicts) =====", flush=True)
    width = max(len(p) for p in results) + 2
    for phase_id, (status, elapsed) in results.items():
        mark = "    " if status in _SUCCESS else "FAIL"
        print(f"  {mark} {phase_id:<{width}} {status:<12} {elapsed:>6.1f}s", flush=True)

    # Persist per-phase wall-clock + status for snapshot_iter.py to consume.
    # The post_<phase>.json corrections files only have status; timing is
    # derived in this driver and lost after the process exits unless we
    # write it somewhere durable. Land it alongside the other corrections
    # so it follows the same gitignore rule. The status recorded here is the
    # authoritative post_<phase>.json verdict (see _authoritative_status), so
    # snapshot_iter.py's `converged` flag reflects real verification results.
    corrections_dir = REPO_ROOT / "forge.assets" / "corrections"
    corrections_dir.mkdir(parents=True, exist_ok=True)
    timings_path = corrections_dir / "phase_timings.json"
    timings_path.write_text(json.dumps({
        "phases": {
            phase_id: {"status": status, "wall_clock_s": round(elapsed, 2)}
            for phase_id, (status, elapsed) in results.items()
        },
        "pipeline_wall_clock_s": round(sum(e for _, e in results.values()), 2),
    }, indent=2), encoding="utf-8")

    failures = [p for p, (s, _) in results.items() if s not in _SUCCESS]
    if failures:
        print(f"\nFAILED phases (authoritative): {', '.join(failures)}", flush=True)
    return 0 if not failures else 1


if __name__ == "__main__":
    sys.exit(main())
