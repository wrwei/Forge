#!/usr/bin/env python3
"""Apply the stated-values check to every inside-boundary mutant of the mutation campaign and
to the unmutated references; write results.csv (one line per mutant) and summary.json."""
import csv, json, os, sys, collections
HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
from check_stated_values import check
KT, MUT, REF = sys.argv[1:4]     # kill_table.csv, mutants dir, reference-runs dir
tables = {s: json.load(open(f"{HERE}/tables/{s}.json")) for s in ("sranger", "lre", "chemical_detector")}
base = {s: check(tables[s], f"{REF}/{s}/java") for s in tables}
assert all(not b["failing"] for b in base.values()), {s: b["failing"] for s, b in base.items()}
rows = []
for r in csv.DictReader(open(KT)):
    if r["inside_boundary"] != "True": continue
    rep = check(tables[r["study"]], f"{MUT}/{r['mutant_id']}/java")
    rows.append({"mutant_id": r["mutant_id"], "study": r["study"], "defect_class": r["defect_class"],
                 "verifier_disposition": r["disposition"], "caught": bool(rep["failing"]),
                 "failing_entries": ";".join(rep["failing"])})
with open(f"{HERE}/results.csv", "w", newline="") as f:
    w = csv.DictWriter(f, fieldnames=list(rows[0])); w.writeheader(); w.writerows(rows)
g = collections.defaultdict(lambda: [0, 0])
for x in rows:
    k = f"{x['study']}/{x['defect_class']}"; g[k][0] += 1; g[k][1] += x["caught"]
summary = {"mutants": len(rows), "caught": sum(x["caught"] for x in rows), "by_group": {k: {"mutants": v[0], "caught": v[1]} for k, v in sorted(g.items())},
           "caught_not_killed_by_verifiers": sum(x["caught"] and x["verifier_disposition"] != "killed" for x in rows)}
json.dump(summary, open(f"{HERE}/summary.json", "w"), indent=1); print(json.dumps(summary, indent=1))
