#!/usr/bin/env python3
"""Aggregate the cold-baseline experiment results.

For each <study>/run-N/pipeline-results.md, scrape:
- which phases completed (any status)
- which phases passed (status=completed/passed)
- whether the pipeline timed out

Write per-study results-summary.md and a top-level aggregate.
"""
from __future__ import annotations

import re
import sys
from pathlib import Path

# parents: [0]=scripts/, [1]=cold-baseline/, [2]=experiments/, [3]=REPO_ROOT
REPO_ROOT = Path(__file__).resolve().parents[3]
BASE = REPO_ROOT / "experiments" / "cold-baseline"

PHASES = [
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


# Map post_*.md file stem to canonical phase name.
POST_FILE_MAP = {
    "post_compile.md": "compile",
    "post_coverage.md": "coverage",
    "post_preflight.md": "preflight",
    "post_t2m.md": "t2m",
    "post_m2m.md": "m2m",
    "post_m2t.md": "m2t",
    "post_dafny_gen.md": "dafny_gen",
    "post_isabelle_gen.md": "isabelle_gen",
    "post_fdr4.md": "fdr4",
    "post_dafny_verify.md": "dafny_verify",
    "post_isabelle_verify.md": "isabelle_verify",
    "post_vacuity.md": "vacuity",
}


def parse_run(path: Path) -> dict:
    """Parse a baseline run directory.

    Reads:
    - pipeline-results.md (for timeout flag)
    - post_*.md (for canonical per-phase pass/fail)

    Returns {timed_out: bool, statuses: dict[phase, "passed"|"failed"|""]}.
    """
    run_dir = path.parent
    text = path.read_text(encoding="utf-8", errors="replace") if path.exists() else ""
    timed_out = "TIMEOUT after" in text
    statuses: dict[str, str] = {}
    for filename, phase in POST_FILE_MAP.items():
        f = run_dir / filename
        if not f.exists():
            continue
        head = f.read_text(encoding="utf-8", errors="replace").splitlines()[:5]
        joined = " ".join(head).upper()
        if "PASSED" in joined or "— PASSED" in joined or "PASSED\n" in joined:
            statuses[phase] = "passed"
        elif "FAILED" in joined:
            statuses[phase] = "failed"
        else:
            statuses[phase] = "unknown"
    return {
        "timed_out": timed_out,
        "statuses": statuses,
    }


def is_converged(run: dict) -> bool:
    s = run["statuses"]
    # All 11 deterministic phases + vacuity must report a success outcome.
    for phase in PHASES:
        st = s.get(phase, "")
        if st not in ("passed", "completed"):
            return False
    return True


def summarise_study(study: str) -> dict:
    study_dir = BASE / study
    runs = []
    for k in range(1, 11):
        results_md = study_dir / f"run-{k}" / "pipeline-results.md"
        if not results_md.exists():
            runs.append({"k": k, "missing": True, "timed_out": False, "statuses": {}})
        else:
            r = parse_run(results_md)
            r["k"] = k
            r["missing"] = False
            runs.append(r)
    return runs


def write_study_summary(study: str, runs: list[dict]) -> None:
    out = BASE / study / "results-summary.md"
    lines: list[str] = []
    lines.append(f"# Cold-baseline results — {study}")
    lines.append("")
    lines.append("Each row is one cold codegen run + one pipeline pass.")
    lines.append("Convergence = every phase reports success AND vacuity audit clean.")
    lines.append("")
    lines.append("## Per-phase outcomes")
    lines.append("")
    header = "| Run | " + " | ".join(PHASES) + " | Timeout | Converged? |"
    sep = "|---|" + "|".join(["---"] * len(PHASES)) + "|---|---|"
    lines.append(header)
    lines.append(sep)
    converged_count = 0
    for r in runs:
        if r["missing"]:
            row = f"| {r['k']} |" + " - |" * len(PHASES) + " - | (missing) |"
            lines.append(row)
            continue
        cells = []
        for p in PHASES:
            st = r["statuses"].get(p, "")
            if st in ("passed", "completed"):
                cells.append("PASS")
            elif st == "failed":
                cells.append("FAIL")
            else:
                cells.append("-")
        timeout = "yes" if r["timed_out"] else "no"
        converged = is_converged(r)
        if converged:
            converged_count += 1
        conv_label = "**YES**" if converged else "no"
        row = f"| {r['k']} | " + " | ".join(cells) + f" | {timeout} | {conv_label} |"
        lines.append(row)
    lines.append("")
    lines.append(f"## Convergence count")
    lines.append("")
    lines.append(f"**{converged_count} of 10** cold runs converged.")
    out.write_text("\n".join(lines) + "\n", encoding="utf-8")
    print(f"[aggregate] wrote {out}  (converged: {converged_count}/10)")
    return converged_count


def main() -> int:
    studies = ["lre", "chemical_detector", "sranger"]
    total = {}
    for s in studies:
        runs = summarise_study(s)
        c = write_study_summary(s, runs)
        total[s] = c

    # Update top-level README.md aggregate table.
    readme = BASE / "README.md"
    text = readme.read_text(encoding="utf-8")

    # Re-render the aggregate table block. Build new lines:
    table_lines = [
        "| Study | Runs | Compiled (2a) | Reached m2t (5b) | Reached fdr4 (6b) | Converged cold |",
        "|---|---:|---:|---:|---:|---:|",
    ]
    for s in studies:
        runs = summarise_study(s)
        compiled = sum(1 for r in runs if r["statuses"].get("compile") in ("passed", "completed"))
        reached_m2t = sum(1 for r in runs if r["statuses"].get("m2t") in ("passed", "completed", "failed"))
        reached_fdr4 = sum(1 for r in runs if r["statuses"].get("fdr4") in ("passed", "completed", "failed"))
        # Count attempts that returned ANY status for those phases (i.e. the pipeline reached them).
        converged = total[s]
        pretty = {"lre": "LRE", "chemical_detector": "Chemical Detector", "sranger": "SRanger"}[s]
        table_lines.append(
            f"| {pretty} | 10 | {compiled} | {reached_m2t} | {reached_fdr4} | {converged} |"
        )

    # Replace the aggregate table in README.md (matches the previous table by
    # its header line and absorbs all rows until a blank line or EOF).
    new_table = "\n".join(table_lines)
    pat = re.compile(
        r"\| Study \| Runs \| Compiled[^\n]*\|\n\|---[^\n]*\|\n(?:\|[^\n]*\|\n)+",
        re.MULTILINE,
    )
    if pat.search(text):
        text = pat.sub(new_table + "\n", text)
    readme.write_text(text, encoding="utf-8")
    print(f"[aggregate] updated {readme}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
