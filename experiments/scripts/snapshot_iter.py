#!/usr/bin/env python3
"""Snapshot one convergence iteration: copy artefacts + emit summary.json
skeleton.

Performs HOWTO_RUN_CONVERGENCE_EXPERIMENT.md §3.4 (the cp recipe) and
§3.5 (the summary.json schema) mechanically. The agent fills in only
the narrative fields and the codegen-cost numbers from the sub-agent's
<usage> block (which this script can't see, since it runs after the
pipeline completes).

Inputs (all read from canonical locations after a pipeline run):
  java.generated.project/src/main/java/<study>/   — converged Java
  forge.assets/corrections/post_*.{md,json}       — per-phase feedback
  forge.assets/corrections/phase_timings.json     — written by run_experiment_iteration.py
  forge.transformations/output/{*.dfy, *.rct, *.xmi, trace_*.json, csp-gen/, isabelle/}
                                                  — formal artefacts + traces
  java.generated.project/result_codegen.json      — requirement -> Java codegen trace

Outputs (written under experiments/convergence/<study>/iter-<N>/):
  java/                  — copy of the converged Java tree
  feedback/              — all post_*.{md,json}
  traces/                — trace_*.json + result_codegen.json + *.xmi
  formal-artefacts/dafny/    — *.dfy
  formal-artefacts/csp/      — robochart_controller.rct + csp-gen/ tree
  formal-artefacts/isabelle/ — *.thy + ROOT
  summary.json           — populated skeleton (see SCHEMA below)

The skeleton populates the mechanical fields (phase results, timings,
LOC, file count, converged-bool) and leaves the narrative + cost
fields as null/empty for the agent to fill in by hand.

Usage:
  python experiments/scripts/snapshot_iter.py <study> <iter_number> [--actor X]

Examples:
  python experiments/scripts/snapshot_iter.py lre 3
  python experiments/scripts/snapshot_iter.py sranger 2 --actor me-as-developer
"""
from __future__ import annotations

import argparse
import json
import shutil
import sys
from pathlib import Path

# parents: [0]=scripts/, [1]=experiments/, [2]=REPO_ROOT
REPO_ROOT = Path(__file__).resolve().parents[2]

GENERATED_SRC = REPO_ROOT / "java.generated.project" / "src" / "main" / "java"
GENERATED_PROJECT = REPO_ROOT / "java.generated.project"
CORRECTIONS_DIR = REPO_ROOT / "forge.assets" / "corrections"
T2M_OUTPUT = REPO_ROOT / "forge.transformations" / "output"
CONVERGENCE_DIR = REPO_ROOT / "experiments" / "convergence"

# The pipeline phases reported by run_experiment_iteration.py, in order.
# Kept here (not imported) so this script can run standalone without
# importing the dashboard package — keeps the snapshot step deployable
# anywhere with just stdlib Python.
PHASES_IN_ORDER = [
    "compile", "coverage", "preflight",
    "t2m", "m2m", "m2t",
    "dafny_gen", "isabelle_gen",
    "fdr4", "dafny_verify", "isabelle_verify",
    "vacuity",
]


def copy_tree_overwrite(src: Path, dst: Path) -> int:
    """copy src/* into dst, replacing dst if it already exists.
    Returns the file count copied. Skips missing src silently."""
    if not src.exists():
        return 0
    if dst.exists():
        shutil.rmtree(dst)
    shutil.copytree(src, dst)
    return sum(1 for _ in dst.rglob("*") if _.is_file())


def copy_glob(src_dir: Path, pattern: str, dst_dir: Path) -> int:
    """Copy src_dir/<pattern> files into dst_dir. Returns count copied."""
    if not src_dir.exists():
        return 0
    dst_dir.mkdir(parents=True, exist_ok=True)
    count = 0
    for f in src_dir.glob(pattern):
        if f.is_file():
            shutil.copy2(f, dst_dir / f.name)
            count += 1
    return count


def copy_file_if_exists(src: Path, dst_dir: Path) -> bool:
    if src.exists() and src.is_file():
        dst_dir.mkdir(parents=True, exist_ok=True)
        shutil.copy2(src, dst_dir / src.name)
        return True
    return False


def java_loc(java_dir: Path) -> int:
    """Total non-blank source lines under java_dir/**/*.java."""
    total = 0
    for f in java_dir.rglob("*.java"):
        try:
            for line in f.read_text(encoding="utf-8", errors="replace").splitlines():
                if line.strip():
                    total += 1
        except OSError:
            pass
    return total


def read_phase_status(corrections_dir: Path, phase: str) -> str | None:
    """Return the status field from post_<phase>.json, or None if absent."""
    p = corrections_dir / f"post_{phase}.json"
    if not p.exists():
        return None
    try:
        data = json.loads(p.read_text(encoding="utf-8"))
        return data.get("status")
    except (json.JSONDecodeError, OSError):
        return None


def read_timings(corrections_dir: Path) -> tuple[dict[str, dict], float]:
    """Read phase_timings.json written by run_experiment_iteration.py.

    Returns ({phase: {"status": ..., "wall_clock_s": ...}}, pipeline_total)
    or ({}, 0.0) if the file is missing.
    """
    p = corrections_dir / "phase_timings.json"
    if not p.exists():
        return {}, 0.0
    try:
        data = json.loads(p.read_text(encoding="utf-8"))
        return data.get("phases", {}), data.get("pipeline_wall_clock_s", 0.0)
    except (json.JSONDecodeError, OSError):
        return {}, 0.0


def build_summary(study: str, iter_n: int, actor: str,
                  java_dir: Path, corrections_dir: Path) -> dict:
    """Assemble the summary.json dict per HOWTO §3.5."""
    timings, pipeline_total = read_timings(corrections_dir)
    phase_results: dict[str, str] = {}
    phase_wall_clocks: dict[str, float] = {}

    for phase in PHASES_IN_ORDER:
        # Prefer status from post_<phase>.json (authoritative — written
        # by the phase runner itself). Fall back to timings file if the
        # phase didn't produce a corrections artefact.
        status = read_phase_status(corrections_dir, phase)
        if status is None and phase in timings:
            status = timings[phase].get("status")
        phase_results[phase] = status or "not_run"
        if phase in timings:
            phase_wall_clocks[phase] = timings[phase].get("wall_clock_s", 0.0)

    java_files = list(java_dir.rglob("*.java")) if java_dir.exists() else []
    converged = (
        all(s in ("completed", "passed") for s in phase_results.values())
        and phase_results.get("vacuity") == "passed"
    )

    return {
        "iter": iter_n,
        "study": study,
        "actor": actor,
        "java_file_count": len(java_files),
        "java_loc": java_loc(java_dir),
        "phase_results": phase_results,
        "phase_wall_clocks_s": phase_wall_clocks,
        "pipeline_wall_clock_s": pipeline_total,
        "converged": converged,
        # The agent fills these in by hand from sub-agent <usage> + own
        # narrative. Left as null/empty placeholders, not faked.
        "failure_summary": None if converged else "TODO: one-line WHY this iter didn't converge",
        "actor_codegen_summary": "TODO: one-line WHAT the actor did this iter",
        "codegen_cost": {
            "kind": "TODO",  # "measured" | "estimated"
            "tokens_in": None,
            "tokens_out": None,
            "tool_uses": None,
            "wall_clock_s": None,
            "notes": "TODO: one-line context (e.g. 'Agent-tool sub-agent, <usage> block')",
        },
    }


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("study", help="Case study: lre | chemical_detector | sranger")
    parser.add_argument("iter_number", type=int, help="Iteration number (1, 2, 3, ...)")
    parser.add_argument("--actor", default="me-as-developer",
                        help="Who drove this iter (default: me-as-developer — the "
                             "canonical mode, edits done by the LLM agent in the user's "
                             "session). Other historical values present in the existing "
                             "trajectories: agent-tool-claude (now BANNED — see HOWTO §2 "
                             "for the rationale) | claude -p (deprecated headless mode).")
    parser.add_argument("--force", action="store_true",
                        help="Overwrite iter-N/ if it already exists.")
    args = parser.parse_args()

    iter_dir = CONVERGENCE_DIR / args.study / f"iter-{args.iter_number}"
    if iter_dir.exists() and not args.force:
        print(f"ERROR: {iter_dir} already exists. Use --force to overwrite.", file=sys.stderr)
        return 1

    if iter_dir.exists():
        shutil.rmtree(iter_dir)
    iter_dir.mkdir(parents=True)

    # Java package name may differ from the case-study name (e.g.
    # chemical_detector study → chemdetector package). Try the study
    # name first; on miss, fall back to the single subdir under
    # src/main/java/ (the same convention run_baseline.py uses).
    java_src = GENERATED_SRC / args.study
    if not java_src.exists():
        if GENERATED_SRC.exists():
            pkg_dirs = [d for d in GENERATED_SRC.iterdir() if d.is_dir()]
            if len(pkg_dirs) == 1:
                java_src = pkg_dirs[0]
                print(f"[snapshot] Java package '{java_src.name}' (auto-discovered; "
                      f"differs from study arg '{args.study}')", flush=True)
            elif len(pkg_dirs) > 1:
                names = ", ".join(d.name for d in pkg_dirs)
                print(f"ERROR: multiple package dirs under {GENERATED_SRC}: {names}",
                      file=sys.stderr)
                return 1
        if not java_src.exists():
            print(f"ERROR: {java_src} does not exist — was the pipeline run for this study?",
                  file=sys.stderr)
            return 1

    print(f"[snapshot] study={args.study} iter={args.iter_number} -> {iter_dir}", flush=True)

    # 1. Java source
    java_count = copy_tree_overwrite(java_src, iter_dir / "java")
    print(f"  copied {java_count} Java files into iter-{args.iter_number}/java/", flush=True)

    # 2. Per-phase feedback (post_*.{md,json}) — both extensions in one go.
    fb_dir = iter_dir / "feedback"
    fb_md = copy_glob(CORRECTIONS_DIR, "post_*.md", fb_dir)
    fb_json = copy_glob(CORRECTIONS_DIR, "post_*.json", fb_dir)
    print(f"  copied {fb_md} post_*.md + {fb_json} post_*.json into feedback/", flush=True)

    # 3. Formal artefacts
    fa = iter_dir / "formal-artefacts"
    n_dfy = copy_glob(T2M_OUTPUT, "*.dfy", fa / "dafny")
    n_rct = copy_glob(T2M_OUTPUT, "robochart_controller.rct", fa / "csp")
    n_csp = copy_tree_overwrite(T2M_OUTPUT / "csp-gen", fa / "csp" / "csp-gen")
    n_thy = copy_glob(T2M_OUTPUT / "isabelle", "*", fa / "isabelle")
    print(f"  copied {n_dfy} .dfy + {n_rct} .rct + {n_csp} csp-gen files + {n_thy} isabelle files",
          flush=True)

    # 4. Traces — trace_*.json + result_codegen.json + the two .xmi
    tr = iter_dir / "traces"
    n_tr_json = copy_glob(T2M_OUTPUT, "trace_*.json", tr)
    n_xmi = copy_glob(T2M_OUTPUT, "*.xmi", tr)
    has_codegen = copy_file_if_exists(GENERATED_PROJECT / "result_codegen.json", tr)
    print(f"  copied {n_tr_json} trace_*.json + {n_xmi} .xmi + result_codegen.json={has_codegen} into traces/",
          flush=True)

    # 5. summary.json skeleton (the agent fills in narrative + cost)
    summary = build_summary(args.study, args.iter_number, args.actor,
                            iter_dir / "java", CORRECTIONS_DIR)
    (iter_dir / "summary.json").write_text(
        json.dumps(summary, indent=2) + "\n", encoding="utf-8"
    )
    print(f"  wrote summary.json (converged={summary['converged']}, "
          f"pipeline_wall_clock_s={summary['pipeline_wall_clock_s']})",
          flush=True)
    print(f"\n[snapshot] DONE. Fill in TODO fields in {iter_dir / 'summary.json'}:",
          flush=True)
    print(f"  - failure_summary (if not converged)", flush=True)
    print(f"  - actor_codegen_summary", flush=True)
    print(f"  - codegen_cost.{{kind, tokens_in, tokens_out, tool_uses, wall_clock_s, notes}}",
          flush=True)
    return 0


if __name__ == "__main__":
    sys.exit(main())
