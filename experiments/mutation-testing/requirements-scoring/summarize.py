#!/usr/bin/env python3
"""Combine the three scoring CSVs into one verdict table and print the summary
tables used in README.md (every number in the README comes from here).

Usage (from the repository root):
  python3 experiments/mutation-testing/requirements-scoring/summarize.py
Reads the three scoring CSVs from their homes and writes combined_verdicts.csv beside this file.
"""
import argparse, os, csv
import pandas as pd

ap = argparse.ArgumentParser()
HERE = os.path.dirname(os.path.abspath(__file__)); E = os.path.abspath(os.path.join(HERE, ".."))
ap.add_argument("--dir", default=HERE)
ap.add_argument("--pool", default=os.path.join(E, "kill_table.csv"))
a = ap.parse_args()
D = a.dir; X = os.path.join(E, "..")
lre = pd.read_csv(os.path.join(X, "requirements-conformance", "survivor_scoring.csv"))
multi = pd.read_csv(os.path.join(X, "requirements-conformance", "multi-study", "multi_survivor_scoring.csv"))
bs = pd.read_csv(os.path.join(X, "blind-spec", "blindspec3_survivor_scoring.csv"), keep_default_na=False)
kt = pd.read_csv(a.pool)
surv = kt[kt.disposition == "survived"][["mutant_id", "study", "defect_class"]].copy()
ids_scored = set(lre.mutant_id) | set(multi.mutant_id)
surv = surv[surv.mutant_id.isin(ids_scored)]          # the pool actually scored (excluded ids drop out)

def flag(s): return s.map(lambda v: str(v) == "True")

def md(df):
    """plain markdown table (no tabulate dependency)"""
    df = df.reset_index()
    cols = [str(c) for c in df.columns]
    lines = ["| " + " | ".join(cols) + " |", "|" + "|".join("---" for _ in cols) + "|"]
    for _, r in df.iterrows():
        lines.append("| " + " | ".join(str(r[c]) for c in df.columns) + " |")
    return "\n".join(lines)
surv = surv.merge(lre[["mutant_id", "scoreable", "killed"]].rename(columns={"scoreable": "lre_oracle_scoreable", "killed": "killed_lre_oracle"}), how="left", on="mutant_id")
surv = surv.merge(multi[["mutant_id", "scoreable", "killed"]].rename(columns={"scoreable": "multi_scoreable", "killed": "killed_multi"}), how="left", on="mutant_id")
bs = bs.assign(spec3_scoreable=~bs.attribution.str.startswith("not-scoreable"))
surv = surv.merge(bs[["mutant_id", "spec3_scoreable", "killed"]].rename(columns={"killed": "killed_spec3"}), how="left", on="mutant_id")
for c in ("lre_oracle_scoreable", "multi_scoreable", "spec3_scoreable", "killed_lre_oracle", "killed_multi", "killed_spec3"):
    surv[c] = flag(surv[c])
surv["scoreable_by_any"] = surv[["lre_oracle_scoreable", "multi_scoreable", "spec3_scoreable"]].any(axis=1)
surv["killed_by_any"] = surv[["killed_lre_oracle", "killed_multi", "killed_spec3"]].any(axis=1)
surv["remaining"] = ~surv.killed_by_any
surv.to_csv(os.path.join(D, "combined_verdicts.csv"), index=False)

per = surv.groupby("study").agg(survivors=("mutant_id", "size"), scoreable=("scoreable_by_any", "sum"),
    killed_by_lre_oracle=("killed_lre_oracle", "sum"), killed_by_multi=("killed_multi", "sum"),
    killed_by_spec3=("killed_spec3", "sum"), killed_by_any=("killed_by_any", "sum"), remaining=("remaining", "sum"))
per.loc["total"] = per.sum()
print("## per study\n"); print(md(per))
print("\n## per study and defect class\n")
pc = surv.groupby(["study", "defect_class"]).agg(survivors=("mutant_id", "size"),
    lre_oracle=("killed_lre_oracle", "sum"), multi=("killed_multi", "sum"), spec3=("killed_spec3", "sum"),
    any=("killed_by_any", "sum"), remaining=("remaining", "sum"))
print(md(pc))
l = surv[surv.study == "lre"]
print("\nLRE: killed by both LRE instruments =", int((l.killed_lre_oracle & l.killed_spec3).sum()),
      "| compiled relation only =", int((l.killed_lre_oracle & ~l.killed_spec3).sum()),
      "| specification only =", int((~l.killed_lre_oracle & l.killed_spec3).sum()))
print("LRE scoreable: compiled relation", int(l.lre_oracle_scoreable.sum()), "| specification", int(l.spec3_scoreable.sum()))
print("\nremaining survivors:", ", ".join(surv[surv.remaining].mutant_id))
