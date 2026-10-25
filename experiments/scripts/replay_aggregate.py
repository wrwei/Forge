#!/usr/bin/env python3
"""Aggregate the replay collections written by replay_published_extractor.sh.

Reads experiments/cold-baseline-replay/<study>/run-N/phase_status.json and
experiments/convergence/ablation-replay/<study>/run-N/phase_status.json and
writes, next to each collection, a RESULTS.md and a results.csv with the
figures the paper's tables use:

  cold baseline (Table 5):   compiled, first failing phase per run (Phase 2b
                             excluded, as in the paper), converged (all phases
                             passed), per study
  best-of-N (Table 6):       disjoint groups in run order within a study;
                             oracle-free = any converged run in the group;
                             verifier-informed = the group's run passing the most
                             phases (2b excluded), converged or not
  ablation (Table 9):        verifier verdicts at the recorded stop point;
                             ships_broken = any of fdr4/dafny_verify/
                             isabelle_verify/vacuity failed

A pass that timed out is listed as "no verdict" and never counted as a failure
or a success. Run with --check to fail (exit 1) if any pass is missing or
without verdict.
"""
from __future__ import annotations

import csv
import json
import sys
from collections import Counter
from pathlib import Path

REPO = Path(__file__).resolve().parents[2]
STUDIES = ["sranger", "lre", "chemical_detector"]
PHASES = ["compile", "coverage", "preflight", "t2m", "m2m", "m2t",
          "dafny_gen", "isabelle_gen", "fdr4", "dafny_verify", "isabelle_verify", "vacuity"]
DECIDING = [p for p in PHASES if p != "coverage"]
LABEL = {"compile": "compile 2a", "preflight": "preflight 2c", "t2m": "T2M 3", "m2m": "M2M 4", "m2t": "M2T 5b",
         "dafny_gen": "Dafny-gen 5a", "isabelle_gen": "Isabelle-gen 5c", "fdr4": "FDR4 6b", "dafny_verify": "Dafny 6a",
         "isabelle_verify": "Isabelle 6c", "vacuity": "vacuity 6d", "coverage": "coverage 2b"}
BEHAV = ["fdr4", "dafny_verify", "isabelle_verify", "vacuity"]


def load(root: Path):
    out = {}
    for s in STUDIES:
        runs = {}
        for d in sorted((root / s).glob("run-*"), key=lambda p: int(p.name.split("-")[1])) if (root / s).exists() else []:
            j = d / "phase_status.json"
            if not j.exists():
                continue
            rec = json.loads(j.read_text(encoding="utf-8"))
            runs[int(d.name.split("-")[1])] = None if rec.get("driver_timeout") else rec["phase_status"]
        out[s] = runs
    return out


def first_fail(ps):
    for p in DECIDING:
        if ps.get(p) != "passed":
            return p
    return None


def cold(root: Path, check: bool) -> bool:
    data = load(root)
    ok = True
    lines = ["# Cold baseline, replayed with the published extractor", "",
             "One pipeline pass per recorded cold source. Phase 2b (coverage) is excluded from the failing-phase",
             "column, as in the paper. A pass without verdict (driver timeout) is excluded from every count.", "",
             "| Study | Passes | With verdict | Compiled (2a) | Failing phase (runs) | Converged |", "|---|---:|---:|---:|---|---:|"]
    rows = []
    totals = Counter()
    for s in STUDIES:
        runs = data[s]
        verd = {k: v for k, v in runs.items() if v}
        comp = sum(1 for v in verd.values() if v.get("compile") == "passed")
        conv = sum(1 for v in verd.values() if all(v.get(p) == "passed" for p in DECIDING))
        ff = Counter(first_fail(v) for v in verd.values() if first_fail(v))
        ffs = "; ".join(f"{LABEL[p]} ({n})" for p, n in sorted(ff.items(), key=lambda x: -x[1]))
        lines.append(f"| {s} | {len(runs)} | {len(verd)} | {comp} | {ffs or '—'} | **{conv}** |")
        totals.update(passes=len(runs), verd=len(verd), comp=comp, conv=conv)
        for k in sorted(runs):
            v = runs[k]
            rows.append({"study": s, "run": k, "verdict": "yes" if v else "no", "compiled": "" if not v else int(v.get("compile") == "passed"),
                         "first_failing_phase": "" if not v else (first_fail(v) or ""), "converged": "" if not v else int(all(v.get(p) == "passed" for p in DECIDING)),
                         **{p: (v or {}).get(p, "") for p in PHASES}})
        if check and (len(runs) < 10 or len(verd) < len(runs)):
            ok = False
    lines.append(f"| **Total** | {totals['passes']} | {totals['verd']} | {totals['comp']} | — | **{totals['conv']}** |")
    # best-of-N
    lines += ["", "## Best-of-N over the replayed cold runs", "",
              "Disjoint groups in run order within each study. Oracle-free: the group converges if any run in it converged.",
              "Verifier-informed: the group's representative is the run passing the most deciding phases; the group converges if that run converged.", "",
              "| Selector | N | Groups per study | Groups in all | Converged |", "|---|---:|---:|---:|---:|"]
    bon = []
    for sel in ("oracle-free", "verifier-informed"):
        for N in (2, 3, 5, 10):
            if sel == "oracle-free" and N == 10:
                continue
            g_all = c_all = 0
            per = []
            for s in STUDIES:
                ks = sorted(k for k, v in data[s].items() if v)
                groups = [ks[i:i + N] for i in range(0, len(ks) - N + 1, N)]
                per.append(len(groups))
                for g in groups:
                    vs = [data[s][k] for k in g]
                    convd = [all(v.get(p) == "passed" for p in DECIDING) for v in vs]
                    if sel == "oracle-free":
                        c_all += any(convd)
                    else:
                        best = max(range(len(vs)), key=lambda i: sum(1 for p in DECIDING if vs[i].get(p) == "passed"))
                        c_all += convd[best]
                g_all += len(groups)
            lines.append(f"| {sel} | {N} | {'/'.join(map(str, per))} | {g_all} | {c_all} of {g_all} |")
            bon.append({"selector": sel, "N": N, "groups_per_study": "/".join(map(str, per)), "groups": g_all, "converged": c_all})
    (root / "RESULTS.md").write_text("\n".join(lines) + "\n", encoding="utf-8")
    with open(root / "results.csv", "w", newline="", encoding="utf-8") as fh:
        w = csv.DictWriter(fh, fieldnames=["study", "run", "verdict", "compiled", "first_failing_phase", "converged"] + PHASES)
        w.writeheader()
        w.writerows(rows)
    with open(root / "best_of_n.csv", "w", newline="", encoding="utf-8") as fh:
        w = csv.DictWriter(fh, fieldnames=["selector", "N", "groups_per_study", "groups", "converged"])
        w.writeheader()
        w.writerows(bon)
    print("\n".join(lines[5:5 + len(STUDIES) + 1]))
    return ok


def ablation(root: Path, check: bool) -> bool:
    data = load(root)
    ok = True
    lines = ["# Compile-only ablation stop points, replayed with the published extractor", "",
             "One pipeline pass per recorded stop-point Java (the last iteration of each archived run).",
             "`ships broken` = at least one of fdr4 / dafny_verify / isabelle_verify / vacuity failed at the stop point.", "",
             "| Study | Run | Verdict | fdr4 | dafny_verify | isabelle_verify | vacuity | Ships broken |", "|---|---:|---|---|---|---|---|---|"]
    rows = []
    broken = Counter()
    total = Counter()
    for s in STUDIES:
        for k in sorted(data[s]):
            v = data[s][k]
            if not v:
                lines.append(f"| {s} | {k} | no verdict | | | | | |")
                ok = ok and not check
                continue
            sb = any(v.get(p) != "passed" for p in BEHAV)
            lines.append(f"| {s} | {k} | yes | " + " | ".join(v.get(p, "absent") for p in BEHAV) + f" | {'yes' if sb else 'no'} |")
            rows.append({"study": s, "run": k, **{p: v.get(p, "") for p in PHASES}, "ships_broken": int(sb)})
            broken[s] += sb
            total[s] += 1
        if check and len(data[s]) < 5:
            ok = False
    lines += ["", "| Study | Ships broken |", "|---|---:|"] + [f"| {s} | {broken[s]} of {total[s]} |" for s in STUDIES] + \
             [f"| **Total** | **{sum(broken.values())} of {sum(total.values())}** |"]
    (root / "RESULTS.md").write_text("\n".join(lines) + "\n", encoding="utf-8")
    with open(root / "results.csv", "w", newline="", encoding="utf-8") as fh:
        w = csv.DictWriter(fh, fieldnames=["study", "run"] + PHASES + ["ships_broken"])
        w.writeheader()
        w.writerows(rows)
    print("\n".join(lines[-5:]))
    return ok


def main() -> int:
    check = "--check" in sys.argv
    ok = True
    cb = REPO / "experiments" / "cold-baseline-replay"
    ab = REPO / "experiments" / "convergence" / "ablation-replay"
    if cb.exists():
        print("cold baseline:")
        ok &= cold(cb, check)
    if ab.exists():
        print("ablation:")
        ok &= ablation(ab, check)
    return 0 if ok else 1


if __name__ == "__main__":
    sys.exit(main())
