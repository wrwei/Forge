#!/usr/bin/env python3
"""Replay one case study's convergence trajectory via Claude Code subprocesses.

For each iteration (1 .. max-iters), this driver:
  1. Spawns a fresh `claude -p` with the appropriate prompt
     (`prompts/iter-1-cold.md` for iter 1; `prompts/iter-n-feedback.md` otherwise).
  2. Runs the full pipeline (11 deterministic phases + vacuity audit) via the
     existing `experiments/scripts/run_experiment_iteration.py`.
  3. Snapshots the resulting Java source, per-phase feedback artefacts, and
     formal artefacts (Dafny / CSP / Isabelle) to
     `experiments/convergence/<study>/iter-N/`.
  4. Parses the pipeline summary and stops once every phase reports `passed` +
     the vacuity audit reports clean.

Each `claude -p` invocation uses `--no-session-persistence` so that
iterations have no shared chat memory — they communicate only through the
Java source and the `forge.assets/corrections/post_*.md` artefacts, which
mirrors the paper's "fresh LLM invocation" methodology.

Each iter is also a **fresh sample** — the trajectory will not in general
match the historical trajectory reported in the paper (which used a different
session). The shipped iter counts under `experiments/convergence/<study>/`
should therefore be read as illustrative, not as the headline number.

Usage:
    python experiments/scripts/replay_convergence.py --study sranger
    python experiments/scripts/replay_convergence.py --study lre --max-iters 7
    python experiments/scripts/replay_convergence.py --study chemical_detector --start-iter 4

Prerequisites:
    - `claude` (Claude Code CLI) on PATH or at the location pipeline.yaml
      configures.
    - `pipeline.yaml` `active_case_study` set to the same `--study` value.
    - WSL + Isabelle/UTP available for the Isabelle phase (Windows).
    - Java 21+, Gradle, Dafny, FDR4 reachable via the dashboard config.
"""
from __future__ import annotations

import argparse
import json
import re
import shutil
import subprocess
import sys
import time
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]
CONVERGENCE_DIR = REPO_ROOT / "experiments" / "convergence"
PROMPTS_DIR = Path(__file__).resolve().parent / "prompts"
GENERATED_SRC_ROOT = REPO_ROOT / "java.generated.project" / "src" / "main" / "java"
CORRECTIONS_DIR = REPO_ROOT / "forge.assets" / "corrections"
OUTPUT_DIR = REPO_ROOT / "forge.transformations" / "output"
PIPELINE_DRIVER = REPO_ROOT / "scripts" / "run_experiment_iteration.py"

# Java package directory per study (Java package name often differs from
# the case-study key, e.g. chemical_detector → chemdetector).
JAVA_PACKAGE = {
    "lre": "lre",
    "chemical_detector": "chemdetector",
    "sranger": "sranger",
}

# Phases the pipeline driver runs, in order. All must pass for convergence.
REQUIRED_PHASES = [
    "compile", "coverage", "preflight",
    "t2m", "m2m", "m2t",
    "dafny_gen", "isabelle_gen",
    "fdr4", "dafny_verify", "isabelle_verify",
    "vacuity",
]


# ---------------------------------------------------------------------------
# Claude codegen step
# ---------------------------------------------------------------------------

def build_prompt(study: str, iter_n: int) -> str:
    """Render the iter-1-cold or iter-n-feedback prompt template."""
    template_path = (
        PROMPTS_DIR / "iter-1-cold.md" if iter_n == 1
        else PROMPTS_DIR / "iter-n-feedback.md"
    )
    text = template_path.read_text(encoding="utf-8")
    return text.format(study=study, iter_n=iter_n, prev_iter=iter_n - 1)


def run_claude(prompt_text: str, log_path: Path) -> int:
    """Spawn a fresh `claude -p` subprocess. Returns the exit code."""
    cmd = [
        "claude",
        "--print",
        "--no-session-persistence",
        "--add-dir", str(REPO_ROOT),
        "--permission-mode", "acceptEdits",
        "--dangerously-skip-permissions",
        prompt_text,
    ]
    log_path.parent.mkdir(parents=True, exist_ok=True)
    with log_path.open("w", encoding="utf-8") as logf:
        logf.write(f"=== command ===\n{' '.join(cmd[:-1])} '<PROMPT>'\n\n")
        logf.write(f"=== prompt ===\n{prompt_text}\n\n")
        logf.write("=== stdout ===\n")
        logf.flush()
        proc = subprocess.run(
            cmd,
            cwd=str(REPO_ROOT),
            capture_output=True,
            text=True,
            encoding="utf-8",
            errors="replace",
        )
        logf.write(proc.stdout or "")
        if proc.stderr:
            logf.write("\n\n=== stderr ===\n")
            logf.write(proc.stderr)
        logf.write(f"\n\n=== exit code: {proc.returncode} ===\n")
    return proc.returncode


# ---------------------------------------------------------------------------
# Pipeline step
# ---------------------------------------------------------------------------

def run_pipeline(log_path: Path) -> tuple[int, dict[str, str]]:
    """Invoke experiments/scripts/run_experiment_iteration.py. Returns (rc, phase results)."""
    cmd = [sys.executable, str(PIPELINE_DRIVER)]
    log_path.parent.mkdir(parents=True, exist_ok=True)
    with log_path.open("w", encoding="utf-8") as logf:
        logf.write(f"=== command ===\n{' '.join(cmd)}\n\n=== stdout ===\n")
        logf.flush()
        proc = subprocess.run(
            cmd,
            cwd=str(REPO_ROOT),
            capture_output=True,
            text=True,
            encoding="utf-8",
            errors="replace",
            timeout=1800,
        )
        logf.write(proc.stdout or "")
        if proc.stderr:
            logf.write("\n\n=== stderr ===\n")
            logf.write(proc.stderr)
    return proc.returncode, parse_summary(proc.stdout or "")


def parse_summary(stdout: str) -> dict[str, str]:
    """Pull the per-phase status from the SUMMARY block."""
    results: dict[str, str] = {}
    in_summary = False
    for line in stdout.splitlines():
        if "===== SUMMARY =====" in line:
            in_summary = True
            continue
        if in_summary:
            stripped = line.strip()
            if not stripped:
                break
            parts = stripped.split()
            if len(parts) >= 2:
                results[parts[0]] = parts[1]
    return results


def is_converged(results: dict[str, str]) -> bool:
    return all(
        results.get(phase) in ("passed", "completed")
        for phase in REQUIRED_PHASES
    )


# ---------------------------------------------------------------------------
# Snapshot step
# ---------------------------------------------------------------------------

def snapshot(study: str, iter_n: int, results: dict[str, str]) -> Path:
    iter_dir = CONVERGENCE_DIR / study / f"iter-{iter_n}"
    iter_dir.mkdir(parents=True, exist_ok=True)

    pkg = JAVA_PACKAGE[study]

    # Java source
    java_src = GENERATED_SRC_ROOT / pkg
    java_dst = iter_dir / "java"
    if java_dst.exists():
        shutil.rmtree(java_dst)
    if java_src.exists():
        shutil.copytree(java_src, java_dst)

    # Per-phase feedback
    feedback_dir = iter_dir / "feedback"
    feedback_dir.mkdir(exist_ok=True)
    for f in CORRECTIONS_DIR.glob("post_*.md"):
        shutil.copy(f, feedback_dir / f.name)
    for f in CORRECTIONS_DIR.glob("post_*.json"):
        shutil.copy(f, feedback_dir / f.name)

    # Traceability artefacts: result_codegen.json (Phase 2 trace, lives in
    # java.generated.project/), trace_*.json (T2M/M2M/M2T/Dafny traces),
    # and the EMF model serialisations (.xmi). Together these let a
    # reviewer follow any verifier failure back to the originating
    # requirement and Java element.
    trace_dir = iter_dir / "traces"
    trace_dir.mkdir(exist_ok=True)
    codegen_trace = REPO_ROOT / "java.generated.project" / "result_codegen.json"
    if codegen_trace.exists():
        shutil.copy(codegen_trace, trace_dir / "result_codegen.json")
    for f in OUTPUT_DIR.glob("trace_*.json"):
        shutil.copy(f, trace_dir / f.name)
    for f in OUTPUT_DIR.glob("*.xmi"):
        shutil.copy(f, trace_dir / f.name)

    # Formal artefacts
    artef = iter_dir / "formal-artefacts"
    (artef / "dafny").mkdir(parents=True, exist_ok=True)
    (artef / "csp").mkdir(parents=True, exist_ok=True)
    (artef / "isabelle").mkdir(parents=True, exist_ok=True)

    for f in OUTPUT_DIR.glob("*.dfy"):
        shutil.copy(f, artef / "dafny" / f.name)

    rct = OUTPUT_DIR / "robochart_controller.rct"
    if rct.exists():
        shutil.copy(rct, artef / "csp" / "robochart_controller.rct")
    csp_gen = OUTPUT_DIR / "csp-gen"
    if csp_gen.exists():
        csp_dst = artef / "csp" / "csp-gen"
        if csp_dst.exists():
            shutil.rmtree(csp_dst)
        shutil.copytree(csp_gen, csp_dst)

    isabelle_src = OUTPUT_DIR / "isabelle"
    if isabelle_src.exists():
        for f in isabelle_src.iterdir():
            if f.is_file():
                shutil.copy(f, artef / "isabelle" / f.name)

    # Per-iter summary as JSON for machine reading
    summary_path = iter_dir / "summary.json"
    summary_path.write_text(
        json.dumps({
            "iter": iter_n,
            "study": study,
            "phase_results": results,
            "converged": is_converged(results),
            "java_file_count": sum(1 for _ in java_dst.rglob("*.java")) if java_dst.exists() else 0,
            "timestamp": time.strftime("%Y-%m-%dT%H:%M:%S"),
        }, indent=2) + "\n",
        encoding="utf-8",
    )

    return iter_dir


# ---------------------------------------------------------------------------
# Main loop
# ---------------------------------------------------------------------------

def check_active_study(study: str) -> bool:
    """Warn if pipeline.yaml's active_case_study doesn't match."""
    pipeline_yaml = REPO_ROOT / "pipeline.yaml"
    text = pipeline_yaml.read_text(encoding="utf-8")
    m = re.search(r"^\s*active_case_study:\s*(\S+)", text, re.MULTILINE)
    if not m:
        print("[warn] could not find active_case_study in pipeline.yaml")
        return False
    actual = m.group(1).strip()
    if actual != study:
        print(f"[warn] pipeline.yaml active_case_study='{actual}', but --study='{study}'")
        print(f"       fix with: sed -i 's/active_case_study: {actual}/active_case_study: {study}/' pipeline.yaml")
        return False
    return True


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--study", required=True, choices=list(JAVA_PACKAGE.keys()))
    parser.add_argument("--max-iters", type=int, default=10,
                        help="Stop after this many iterations even if not converged (default: 10)")
    parser.add_argument("--start-iter", type=int, default=1,
                        help="Resume from this iteration (assumes prior iters already snapshotted; default: 1)")
    parser.add_argument("--skip-active-study-check", action="store_true",
                        help="Don't refuse to run if pipeline.yaml active_case_study disagrees")
    args = parser.parse_args()

    if args.start_iter == 1 and not args.skip_active_study_check:
        if not check_active_study(args.study):
            print("[abort] active_case_study mismatch (override with --skip-active-study-check)")
            return 2

    print(f"=== replay convergence: {args.study}, iters {args.start_iter}..{args.max_iters} ===")

    for iter_n in range(args.start_iter, args.max_iters + 1):
        print(f"\n========== ITER {iter_n} ==========")
        iter_dir = CONVERGENCE_DIR / args.study / f"iter-{iter_n}"
        iter_dir.mkdir(parents=True, exist_ok=True)
        log_dir = iter_dir / "_logs"
        log_dir.mkdir(exist_ok=True)

        # 1. Codegen via claude -p
        print(f"[iter-{iter_n}] codegen via claude -p ...", flush=True)
        prompt = build_prompt(args.study, iter_n)
        t0 = time.time()
        rc = run_claude(prompt, log_dir / "codegen.log")
        codegen_secs = time.time() - t0
        print(f"[iter-{iter_n}] codegen: rc={rc}, {codegen_secs:.1f}s", flush=True)
        if rc != 0:
            print(f"[iter-{iter_n}] codegen FAILED — aborting trajectory", flush=True)
            return 1

        # 2. Pipeline
        print(f"[iter-{iter_n}] pipeline ...", flush=True)
        t0 = time.time()
        pipeline_rc, results = run_pipeline(log_dir / "pipeline.log")
        pipeline_secs = time.time() - t0
        print(f"[iter-{iter_n}] pipeline: rc={pipeline_rc}, {pipeline_secs:.1f}s", flush=True)
        print(f"[iter-{iter_n}] phase results: {results}", flush=True)

        # 3. Snapshot
        snap = snapshot(args.study, iter_n, results)
        print(f"[iter-{iter_n}] snapshotted to {snap.relative_to(REPO_ROOT)}", flush=True)

        # 4. Check convergence
        if is_converged(results):
            print(f"\n=== CONVERGED at iter {iter_n} ===")
            return 0

    print(f"\n=== hit cap of {args.max_iters} iterations without converging ===")
    return 2


if __name__ == "__main__":
    sys.exit(main())
