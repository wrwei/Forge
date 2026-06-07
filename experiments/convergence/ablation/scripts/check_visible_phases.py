#!/usr/bin/env python3
"""Stop-gate for a compile-only ablation run.

Prints ONLY the eight visible (compile-and-extraction) phase statuses from
forge.assets/corrections/post_<phase>.json, so a running session can decide
the stop condition WITHOUT ever seeing the four withheld behavioural-verifier
results (which snapshot_iter.py also records in summary.json). Exits 0 when all
eight visible phases are green, 1 otherwise.

Usage (from the repo / worktree root):
    python experiments/convergence/ablation/scripts/check_visible_phases.py
"""
import json, sys
from pathlib import Path

VISIBLE = ["compile", "coverage", "preflight",
           "t2m", "m2m", "m2t", "dafny_gen", "isabelle_gen"]
GREEN = {"passed", "completed"}

REPO = Path(__file__).resolve().parents[4]
CORR = REPO / "forge.assets" / "corrections"


def status(phase: str) -> str:
    p = CORR / f"post_{phase}.json"
    if not p.exists():
        return "absent"
    try:
        return json.loads(p.read_text(encoding="utf-8")).get("status", "?")
    except Exception:
        return "unreadable"


def main() -> int:
    rows = [(ph, status(ph)) for ph in VISIBLE]
    width = max(len(p) for p in VISIBLE)
    failed = []
    for ph, st in rows:
        mark = "ok " if st in GREEN else "XX "
        print(f"  {mark}{ph.ljust(width)}  {st}")
        if st not in GREEN:
            failed.append(ph)
    print("-" * (width + 18))
    if failed:
        print(f"CONTINUE: fix visible phase(s): {', '.join(failed)}")
        print("(do NOT open post_fdr4/_dafny_verify/_isabelle_verify/_vacuity "
              "or summary.json)")
        return 1
    print("STOP: all 8 visible phases green -> this is the compile-only stop "
          "point. Archive + record; do not iterate further.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
