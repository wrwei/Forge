#!/usr/bin/env python3
"""Token usage of every recorded v3 session, read from Claude Code's session logs.

For each run directory under experiments/v3-cold, v3-reference and v3-compile-only, the
worktree named in actor.json gives the log directory ~/.claude/projects/-Users-ranwei-Gitee-<worktree>.
Every assistant message in those logs carries a `usage` record; this sums input, output,
cache-creation and cache-read tokens over the session, and counts the messages.
Writes experiments/v3-analysis/session_tokens.csv and prints a per-condition summary.
Run on the machine that holds the logs:  python3 scripts/session_tokens.py
"""
import csv, glob, json, os, statistics, sys
from pathlib import Path

C = Path(__file__).resolve().parent.parent
E = C / "experiments"
PROJ = Path(os.environ.get("V3_PROJ_ROOT", Path.home() / ".claude/projects"))
COLLECTIONS = {"cold": "v3-cold", "reference": "v3-reference", "compileonly": "v3-compile-only"}

def logdir(worktree):
    return PROJ / ("-" + str(worktree).strip("/").replace("/", "-").replace("_", "-"))

def usage(d):
    tot = {"input": 0, "output": 0, "cache_create": 0, "cache_read": 0, "messages": 0, "files": 0}
    seen = set()
    for f in sorted(glob.glob(str(d / "*.jsonl"))):
        tot["files"] += 1
        for line in open(f, errors="ignore"):
            try: r = json.loads(line)
            except Exception: continue
            m = r.get("message") if isinstance(r.get("message"), dict) else None
            if not m or r.get("type") != "assistant": continue
            u = m.get("usage")
            if not u: continue
            key = (m.get("id"), r.get("requestId"))
            if key in seen: continue          # one usage record per API response
            seen.add(key)
            tot["input"] += u.get("input_tokens", 0) or 0
            tot["output"] += u.get("output_tokens", 0) or 0
            tot["cache_create"] += u.get("cache_creation_input_tokens", 0) or 0
            tot["cache_read"] += u.get("cache_read_input_tokens", 0) or 0
            tot["messages"] += 1
    return tot

rows = []
for cond, coll in COLLECTIONS.items():
    for a in sorted(glob.glob(str(E / coll / "*" / "run-*" / "actor.json"))):
        p = Path(a); study = p.parent.parent.name; run = int(p.parent.name.split("-")[1])
        wt = json.load(open(a)).get("worktree")
        d = logdir(wt) if wt else None
        try: ok = bool(d) and d.exists()
        except PermissionError: ok = False
        if not ok:
            rows.append(dict(condition=cond, study=study, run=run, worktree=wt, log_dir=str(d), found=False)); continue
        u = usage(d)
        iters = len([x for x in glob.glob(str(p.parent / "iter-*")) if os.path.basename(x)[5:].isdigit()])
        rows.append(dict(condition=cond, study=study, run=run, worktree=wt, log_dir=str(d), found=True, iterations=iters, **u,
                         total_in=u["input"] + u["cache_create"] + u["cache_read"]))

out = E / "v3-analysis" / "session_tokens.csv"
cols = ["condition", "study", "run", "worktree", "log_dir", "found", "iterations", "messages", "files", "input", "cache_create", "cache_read", "total_in", "output"]
with open(out, "w", newline="") as f:
    w = csv.DictWriter(f, fieldnames=cols); w.writeheader()
    for r in rows: w.writerow({k: r.get(k, "") for k in cols})
print(f"wrote {out}: {len(rows)} sessions, {sum(1 for r in rows if r['found'])} with logs")
for r in rows:
    if not r["found"]: print("  MISSING", r["condition"], r["study"], r["run"], r["log_dir"])
def med(xs): return f"{statistics.median(xs)/1000:.0f}k" if xs else "-"
def rng(xs): return f"{min(xs)/1000:.0f}k-{max(xs)/1000:.0f}k" if xs else "-"
print(f"{'condition':12s}{'study':18s}{'n':>3s} {'output median (range)':>26s} {'fresh input median (range)':>30s} {'cache read median':>18s} {'msgs':>6s}")
for cond in COLLECTIONS:
    for study in ("sranger", "lre", "chemical_detector", "steam_boiler"):
        rs = [r for r in rows if r["found"] and r["condition"] == cond and r["study"] == study]
        if not rs: continue
        o = [r["output"] for r in rs]; i = [r["input"] + r["cache_create"] for r in rs]; c = [r["cache_read"] for r in rs]
        print(f"{cond:12s}{study:18s}{len(rs):3d} {med(o)+' ('+rng(o)+')':>26s} {med(i)+' ('+rng(i)+')':>30s} {med(c):>18s} {statistics.median([r['messages'] for r in rs]):6.0f}")
