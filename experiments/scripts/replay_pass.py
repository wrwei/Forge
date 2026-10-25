#!/usr/bin/env python3
"""Run one pipeline pass of the published extractor over a recorded Java source.

Used to replay the cold baseline (Section 4.5) and the compile-only ablation
(Section 4.7) with the extractor this repository ships, so that every number in
the paper comes from the same pipeline version.

    python experiments/scripts/replay_pass.py <study> <java-package-dir> <out-dir>

<java-package-dir> is the directory holding the package contents (the
sub-packages `controller/`, `mode/`, ... directly under it); its package name is
read from the first `package` declaration found. The source is copied into
java.generated.project/src/main/java/<pkg>/ (any other package there is removed
first), the active case study is switched to <study> for the duration of the
pass, all twelve phases run through experiments/scripts/run_experiment_iteration.py,
and the pass is recorded under <out-dir>:

    phase_status.json      {phase: status} from forge.assets/corrections/post_<phase>.json
    pipeline-results.md    the same, as a table, with the driver's SUMMARY block
    feedback/              post_*.md and post_*.json
    formal-artefacts/      *.dfy, robochart_controller.rct, csp-gen/, isabelle/
    driver.log             the driver's stdout and stderr

Exit code 0 when the pass ran to completion (whatever the verdicts); 1 on a
driver timeout (REPLAY_TIMEOUT seconds, default 3600) or a setup error.
"""
from __future__ import annotations

import json
import os
import re
import shutil
import subprocess
import sys
import time
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]
PHASES = ["compile", "coverage", "preflight", "t2m", "m2m", "m2t",
          "dafny_gen", "isabelle_gen", "fdr4", "dafny_verify", "isabelle_verify", "vacuity"]


def package_name(src: Path) -> str:
    for f in sorted(src.rglob("*.java")):
        m = re.search(r"^\s*package\s+([\w.]+)\s*;", f.read_text(encoding="utf-8", errors="replace"), re.M)
        if m:
            return m.group(1).split(".")[0]
    sys.exit(f"ERROR: no package declaration under {src}")


def main() -> int:
    if len(sys.argv) != 4:
        print(__doc__, file=sys.stderr)
        return 2
    study, src, out = sys.argv[1], Path(sys.argv[2]).resolve(), Path(sys.argv[3]).resolve()
    if not src.is_dir():
        print(f"ERROR: {src} is not a directory", file=sys.stderr)
        return 1
    pkg = package_name(src)
    try:
        src_shown = str(src.relative_to(REPO_ROOT))
    except ValueError:
        src_shown = str(src)
    target_root = REPO_ROOT / "java.generated.project" / "src" / "main" / "java"
    if target_root.exists():
        for d in target_root.iterdir():
            if d.is_dir():
                shutil.rmtree(d)
    target_root.mkdir(parents=True, exist_ok=True)
    shutil.copytree(src, target_root / pkg)
    n_java = len(list((target_root / pkg).rglob("*.java")))
    print(f"[replay] {study}: copied {n_java} Java files from {src} -> {target_root / pkg}", flush=True)

    corrections = REPO_ROOT / "forge.assets" / "corrections"
    corrections.mkdir(parents=True, exist_ok=True)
    for stale in list(corrections.glob("post_*.md")) + list(corrections.glob("post_*.json")):
        stale.unlink()
    t2m_out = REPO_ROOT / "forge.transformations" / "output"
    if t2m_out.exists():
        shutil.rmtree(t2m_out)

    sys.path.insert(0, str(REPO_ROOT / "forge.dashboard"))
    from web.manifest import Manifest  # noqa: E402
    manifest_path = REPO_ROOT / "pipeline.yaml"
    manifest = Manifest.load(manifest_path)
    raw = manifest._raw
    original = (raw.get("agent") or {}).get("active_case_study", "")
    if original != study:
        raw.setdefault("agent", {})["active_case_study"] = study
        manifest.dump(manifest_path)
        print(f"[replay] active_case_study: {original or '(unset)'} -> {study}", flush=True)

    timeout = int(os.environ.get("REPLAY_TIMEOUT", "3600"))
    t0 = time.time()
    timed_out = False
    try:
        try:
            r = subprocess.run([sys.executable, str(REPO_ROOT / "experiments" / "scripts" / "run_experiment_iteration.py")],
                               cwd=str(REPO_ROOT), capture_output=True, text=True, encoding="utf-8", errors="replace", timeout=timeout)
            stdout, stderr, rc = r.stdout or "", r.stderr or "", r.returncode
        except subprocess.TimeoutExpired as exc:
            stdout = exc.stdout if isinstance(exc.stdout, str) else (exc.stdout or b"").decode("utf-8", "replace")
            stderr = exc.stderr if isinstance(exc.stderr, str) else (exc.stderr or b"").decode("utf-8", "replace")
            rc, timed_out = -1, True
    finally:
        if original != study:
            raw.setdefault("agent", {})["active_case_study"] = original
            manifest.dump(manifest_path)
    elapsed = time.time() - t0

    out.mkdir(parents=True, exist_ok=True)
    fb = out / "feedback"
    if fb.exists():
        shutil.rmtree(fb)
    fb.mkdir()
    for f in list(corrections.glob("post_*.md")) + list(corrections.glob("post_*.json")):
        shutil.copy(f, fb / f.name)
    fa = out / "formal-artefacts"
    if fa.exists():
        shutil.rmtree(fa)
    (fa / "dafny").mkdir(parents=True)
    (fa / "csp").mkdir()
    for f in t2m_out.glob("*.dfy") if t2m_out.exists() else []:
        shutil.copy(f, fa / "dafny" / f.name)
    for f in t2m_out.glob("*.rct") if t2m_out.exists() else []:
        shutil.copy(f, fa / "csp" / f.name)
    if (t2m_out / "csp-gen").exists():
        shutil.copytree(t2m_out / "csp-gen", fa / "csp" / "csp-gen")
    if (t2m_out / "isabelle").exists():
        shutil.copytree(t2m_out / "isabelle", fa / "isabelle")

    status = {}
    for p in PHASES:
        j = corrections / f"post_{p}.json"
        try:
            status[p] = json.loads(j.read_text(encoding="utf-8")).get("status", "unknown") if j.exists() else "absent"
        except Exception:
            status[p] = "unreadable"
    record = {"study": study, "package": pkg, "source": src_shown, "java_files": n_java,
              "driver_exit": rc, "driver_timeout": timed_out, "elapsed_s": round(elapsed, 1),
              "phase_status": status, "all_passed": all(v == "passed" for v in status.values())}
    (out / "phase_status.json").write_text(json.dumps(record, indent=2), encoding="utf-8")
    (out / "driver.log").write_text(stdout + "\n--- stderr ---\n" + stderr, encoding="utf-8")

    summary = []
    seen = False
    for line in stdout.splitlines():
        if "===== SUMMARY =====" in line:
            seen = True
            continue
        if seen:
            if not line.strip():
                break
            summary.append(line.strip())
    md = [f"# Replay pass ({study}) with the published extractor", "",
          f"Source: `{src_shown}` ({n_java} Java files, package `{pkg}`).",
          f"Driver exit `{rc}`{' (TIMEOUT)' if timed_out else ''}, {elapsed:.0f} s.", "",
          "| Phase | Status |", "|---|---|"] + [f"| {p} | {status[p]} |" for p in PHASES]
    if summary:
        md += ["", "## Driver summary", "", "```"] + summary + ["```"]
    (out / "pipeline-results.md").write_text("\n".join(md) + "\n", encoding="utf-8")
    verdict = "ALL PASSED" if record["all_passed"] else "failed: " + ", ".join(p for p in PHASES if status[p] != "passed")
    print(f"[replay] {study} -> {out.name}: {verdict} ({elapsed:.0f} s){' TIMEOUT' if timed_out else ''}", flush=True)
    return 1 if timed_out else 0


if __name__ == "__main__":
    sys.exit(main())
