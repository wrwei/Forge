#!/usr/bin/env python3
"""Run one cold-baseline pipeline pass.

Takes a (study, run_number) and:
1. Wipes java.generated.project/src/main/java/<study>/
2. Copies experiments/cold-baseline/<study>/run-N/src/main/java/<study>/* into it
3. Runs the full pipeline (11 phases) via the existing experiment driver
4. Copies forge.assets/corrections/post_*.md into the baseline run dir
5. Writes a pipeline-results.md summarising per-phase outcomes

Usage: python experiments/cold-baseline/scripts/run_baseline.py <study> <run_number>
"""
from __future__ import annotations

import shutil
import subprocess
import sys
from pathlib import Path

# parents: [0]=scripts/, [1]=cold-baseline/, [2]=experiments/, [3]=REPO_ROOT
REPO_ROOT = Path(__file__).resolve().parents[3]


def main() -> int:
    if len(sys.argv) != 3:
        print(f"usage: {sys.argv[0]} <study> <run_number>", file=sys.stderr)
        return 2

    study = sys.argv[1]
    run_n = int(sys.argv[2])

    baseline_dir = REPO_ROOT / "experiments" / "cold-baseline" / study / f"run-{run_n}"
    if not baseline_dir.exists():
        print(f"ERROR: {baseline_dir} does not exist", file=sys.stderr)
        return 1

    # The Java package name may differ from the case-study name (e.g.,
    # `chemical_detector` → `chemdetector`). Discover the actual package by
    # listing the single top-level subdirectory under src/main/java/.
    src_java_root = baseline_dir / "src" / "main" / "java"
    if not src_java_root.exists():
        print(f"ERROR: {src_java_root} does not exist", file=sys.stderr)
        return 1
    pkg_dirs = [d for d in src_java_root.iterdir() if d.is_dir()]
    if not pkg_dirs:
        print(f"ERROR: no package dir under {src_java_root}", file=sys.stderr)
        return 1
    if len(pkg_dirs) > 1:
        print(f"ERROR: multiple package dirs under {src_java_root}: {pkg_dirs}", file=sys.stderr)
        return 1
    src_src = pkg_dirs[0]
    pkg_name = src_src.name

    target_root = REPO_ROOT / "java.generated.project" / "src" / "main" / "java"
    target_dir = target_root / pkg_name

    # 1. Wipe any existing target source. Strip ALL packages under
    #    src/main/java/ to avoid cross-study pollution between back-to-back
    #    baseline runs.
    if target_root.exists():
        for d in target_root.iterdir():
            if d.is_dir():
                shutil.rmtree(d)
    target_root.mkdir(parents=True, exist_ok=True)

    # 2. Copy cold source in.
    shutil.copytree(src_src, target_dir)
    print(f"[baseline] copied {src_src} -> {target_dir}", flush=True)

    # 3. Run the pipeline. The existing driver writes post_*.md to
    #    forge.assets/corrections/ and prints a SUMMARY block we can scrape.
    corrections_dir = REPO_ROOT / "forge.assets" / "corrections"
    # Capture pre-existing post_*.md so we can detect which were rewritten.
    for old in corrections_dir.glob("post_*.md"):
        # Don't delete; just record mtime via Path.stat. Pipeline rewrites
        # everything anyway.
        pass

    # The pipeline driver (forge.dashboard.web.runners._active_requirements_file)
    # resolves the active case study from pipeline.yaml's agent.active_case_study
    # on every call. If that doesn't match the study we're processing, the
    # coverage phase validates THIS study's Java against the OTHER study's
    # requirements — silent and misleading. Set it to the study we're
    # running, then restore on exit so back-to-back invocations across
    # different studies don't trip each other up.
    sys.path.insert(0, str(REPO_ROOT / "forge.dashboard"))
    from web.manifest import Manifest  # noqa: E402 — depends on sys.path mutation above
    manifest_path = REPO_ROOT / "pipeline.yaml"
    manifest = Manifest.load(manifest_path)
    raw = manifest._raw
    original_study = (raw.get("agent") or {}).get("active_case_study", "")
    must_restore = original_study != study
    if must_restore:
        raw.setdefault("agent", {})["active_case_study"] = study
        manifest.dump(manifest_path)
        print(f"[baseline] set active_case_study: {original_study or '(unset)'} -> {study}", flush=True)

    try:
        try:
            result = subprocess.run(
                [sys.executable, str(REPO_ROOT / "experiments" / "scripts" / "run_experiment_iteration.py")],
                cwd=str(REPO_ROOT),
                capture_output=True,
                text=True,
                encoding="utf-8",
                errors="replace",
                timeout=420,
            )
            stdout = result.stdout or ""
            stderr = result.stderr or ""
            returncode = result.returncode
            print(f"[baseline] pipeline exit={returncode}", flush=True)
        except subprocess.TimeoutExpired as exc:
            # text=True means exc.stdout/stderr are already str, not bytes.
            raw_out = exc.stdout if exc.stdout else ""
            raw_err = exc.stderr if exc.stderr else ""
            stdout = raw_out if isinstance(raw_out, str) else raw_out.decode("utf-8", errors="replace")
            stderr = raw_err if isinstance(raw_err, str) else raw_err.decode("utf-8", errors="replace")
            returncode = -1  # convention: -1 = pipeline timeout
            stderr += f"\n[baseline] TIMEOUT after {exc.timeout}s — pipeline killed.\n"
            print(f"[baseline] pipeline TIMEOUT after {exc.timeout}s", flush=True)
    finally:
        if must_restore:
            # Restore on every exit path (success, failure, timeout, ctrl-c).
            raw.setdefault("agent", {})["active_case_study"] = original_study
            manifest.dump(manifest_path)
            print(f"[baseline] restored active_case_study -> {original_study or '(unset)'}", flush=True)
    # Wrap result-shaped access in a tuple so later code can reference uniformly.
    class _R:
        pass
    result = _R()
    result.returncode = returncode

    # 4. Copy resulting post_*.md into the baseline run dir.
    for md in corrections_dir.glob("post_*.md"):
        shutil.copy(md, baseline_dir / md.name)

    # 5. Scrape the SUMMARY block from stdout and write pipeline-results.md.
    summary_lines: list[str] = []
    in_summary = False
    for line in stdout.splitlines():
        if "===== SUMMARY =====" in line:
            in_summary = True
            continue
        if in_summary:
            stripped = line.strip()
            if not stripped:
                break
            summary_lines.append(stripped)

    md_lines = [
        f"# Cold-baseline pipeline results — {study} run-{run_n}",
        "",
        "Per-phase outcomes from one cold-baseline pipeline pass. The cold",
        "codegen Java source under `src/` was copied into",
        "`java.generated.project/src/main/java/" + study + "/`, then",
        "`experiments/scripts/run_experiment_iteration.py` was invoked once.",
        "",
        "## Phase outcomes",
        "",
        "| Phase | Status | Time |",
        "|---|---|---|",
    ]
    for line in summary_lines:
        parts = line.split()
        if len(parts) >= 3:
            phase = parts[0]
            status = parts[1]
            timing = " ".join(parts[2:])
            md_lines.append(f"| {phase} | {status} | {timing} |")

    md_lines.append("")
    md_lines.append("## Pipeline exit code")
    md_lines.append("")
    md_lines.append(f"`{result.returncode}`")
    md_lines.append("")
    md_lines.append("## Stdout (last 80 lines)")
    md_lines.append("")
    md_lines.append("```")
    tail = stdout.splitlines()[-80:]
    md_lines.extend(tail)
    md_lines.append("```")
    if stderr.strip():
        md_lines.append("")
        md_lines.append("## Stderr")
        md_lines.append("")
        md_lines.append("```")
        md_lines.extend(stderr.splitlines()[-40:])
        md_lines.append("```")

    (baseline_dir / "pipeline-results.md").write_text(
        "\n".join(md_lines) + "\n", encoding="utf-8"
    )
    print(f"[baseline] wrote {baseline_dir / 'pipeline-results.md'}", flush=True)

    return 0


if __name__ == "__main__":
    sys.exit(main())
