#!/usr/bin/env python3
"""Secondary: apply the stated-values check to the Java of the converged iteration of every loop
run on the three studies; write sessions.csv (one line per run and entry)."""
import csv, glob, json, os, re, sys
HERE = os.path.dirname(os.path.abspath(__file__)); sys.path.insert(0, HERE)
from check_stated_values import check
EXP = sys.argv[1]
rows = []
for s in ("sranger", "lre", "chemical_detector"):
    t = json.load(open(f"{HERE}/tables/{s}.json"))
    for run in sorted(glob.glob(f"{EXP}/v3-reference/{s}/run-*")):
        its = sorted(glob.glob(f"{run}/iter-*"), key=lambda p: int(re.search(r"iter-(\d+)$", p).group(1)) if re.search(r"iter-(\d+)$", p) else -1)
        its = [p for p in its if re.search(r"iter-\d+$", p)]
        rep = check(t, f"{its[-1]}/java")
        for e in rep["entries"]:
            rows.append({"study": s, "run": os.path.basename(run), "iteration": os.path.basename(its[-1]), "entry": e.get("name") or e.get("quantity"),
                         "req": e["req"], "located": e["located"], "holds": e["holds"],
                         "found": "; ".join(f"{h['expr'][:70]}={h['value']}" if isinstance(h, dict) else f"{h[0]}={h[1]}" for h in e["found"])[:300]})
with open(f"{HERE}/sessions.csv", "w", newline="") as f:
    w = csv.DictWriter(f, fieldnames=list(rows[0])); w.writeheader(); w.writerows(rows)
