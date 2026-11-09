#!/usr/bin/env python3
"""Totals per subject from results/*.csv -> summary.json."""
import csv, glob, gzip, io, json, os, collections
HERE = os.path.dirname(os.path.abspath(__file__)); out = {}
for f in sorted(glob.glob(f"{HERE}/results/*.csv.gz")):
    R = list(csv.DictReader(io.TextIOWrapper(gzip.open(f), encoding="utf-8"))); tag = os.path.basename(f)[:-7]
    dis = [r for r in R if r["verdict"] == "disagree"]
    att = [r for r in dis if r.get("double_model_agrees") == "True" or r.get("cause", "").startswith("double-arithmetic")]
    nonfin = [r for r in dis if r.get("java_nonfinite") == "True" or "non-finite" in r.get("cause", "")]
    out[tag] = {"steps": len(R), "agree": sum(r["verdict"] == "agree" for r in R), "disagree": len(dis),
                "undecided": sum(r["verdict"] == "undecided" for r in R),
                "disagree_explained_by_double_arithmetic": len(att), "disagree_with_nonfinite_java_values": len(nonfin),
                "boundary_steps": sum(r["set"] == "boundary" for r in R), "random_steps": sum(r["set"] == "random" for r in R)}
json.dump(out, open(f"{HERE}/summary.json", "w"), indent=1)
for k, v in out.items(): print(k, v)
