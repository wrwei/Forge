#!/usr/bin/env python3
"""best_of_n_selection.py -- the verifier-selected best-of-N control (R1-M3).

Protocol: the cold-baseline runs the FULL verification chain on every cold
candidate (one pipeline pass per run, per-phase verdicts in post_*.md), so a
resampling selector WITH verifier access is a selection over these recorded
verdicts. This script recomputes the selection from the run records.

Selection rule: lexicographic (converged, number of passing phases), i.e. the
selector prefers a fully verifying candidate and otherwise the one passing the
most phases. Any monotone rule gives the same convergence outcome, since no
candidate fully verifies.

Output: best_of_n_verifier_selection.csv -- one row per (study, N):
disjoint groups in run order, how many selections converge, which run each
group selects, and what the selected run still fails.
"""
import re, os, sys, glob, csv, collections

CB = sys.argv[1] if len(sys.argv) > 1 else os.path.dirname(os.path.abspath(__file__))
POST = {p: f"post_{p}.md" for p in
        ("compile","preflight","t2m","m2m","m2t","dafny_gen","isabelle_gen",
         "fdr4","dafny_verify","isabelle_verify","vacuity","coverage")}
DEC = [p for p in POST if p != "coverage"]

def verdicts(run):
    out = {}
    for ph, fn in POST.items():
        head = open(os.path.join(run, fn), errors="ignore").readline()
        m = re.search(r"\u2014\s*(PASSED|FAILED|SKIPPED|ERROR|INCONCLUSIVE)", head) or \
            re.search(r"-\s*(PASSED|FAILED|SKIPPED|ERROR|INCONCLUSIVE)", head)
        assert m, (run, fn, head)
        out[ph] = m.group(1).lower()
    return out

def score(v):
    return (all(v[p] == "passed" for p in DEC), sum(1 for p in DEC if v[p] == "passed"))

rows = collections.defaultdict(list)
for run in sorted(glob.glob(os.path.join(CB, "*", "run-*")),
                  key=lambda p: (p.split(os.sep)[-2], int(p.split("-")[-1]))):
    st = run.split(os.sep)[-2]
    v = verdicts(run)
    rows[st].append((int(run.split("-")[-1]), score(v), sorted(p for p in DEC if v[p] != "passed")))

with open(os.path.join(CB, "best_of_n_verifier_selection.csv"), "w", newline="") as fh:
    w = csv.writer(fh)
    w.writerow(["study","N","groups","converging_selections","selected_runs","selected_still_failing"])
    for st, scored in sorted(rows.items()):
        for N in (2, 3, 5, 10):
            groups = [scored[i:i+N] for i in range(0, len(scored), N) if len(scored[i:i+N]) == N]
            sel = [max(g, key=lambda x: x[1]) for g in groups]
            wins = sum(1 for s in sel if s[1][0])
            w.writerow([st, N, len(groups), wins,
                        " ".join(f"run-{s[0]}" for s in sel),
                        " | ".join(";".join(s[2][:4]) for s in sel)])
            print(f"{st} best-of-{N}: {wins}/{len(groups)} converge")
