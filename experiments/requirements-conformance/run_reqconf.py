#!/usr/bin/env python3
"""Requirements-conformance scoring of the LRE mutants (paper Section 4.9, RQ7).

Scores the LRE mutants that survive every verifier of the mutation campaign
(experiments/mutation-testing, kill_table.csv rows with study == lre and
disposition == survived) against the compiled LRE requirements relation.

The relation code lives in reqconf_relation.py and is executed here unchanged:
its hash check of requirements_model_lre.json against ordering_attestation.txt,
REQ_TRANS (17 transitions), the hybrid concrete/abstract grid, tok_eval,
relation2 and verdicts2.  Only the theory PARSER is defined here, because the
extractor's theories carry three precondition conjunct forms the relation code
does not evaluate itself:
    <ev> \\<in> offered          the triggering event
    \\<not>(<ev> \\<in> offered)   a sibling-priority negation of a trigger
    offered \\<subseteq> listens  always true in the relation's abstraction
The precondition is split at top-level \\<and> only, a conjunct of the form
\\<not>( c1 \\<and> ... \\<and> cn ) is evaluated as the negation of the
conjunction, and the Idle_<State> operations (the Java else branch, "no
transition from State") are excluded, because the relation represents "no
transition" as "no enabled operation".  For every theory the script verifies
that each Idle_<State> precondition equals the complement of the union of the
enabled sets of that state's other operations (idle_check.csv).

Kill rule: a mutant is killed when, at some grid cell, it has a disagreement
with the requirements (requirements demand a transition the theory does not
take, or the theory takes a transition the requirements do not admit) or an
output violation that the unmutated theory does not have.  A mutant whose
theory is identical to the unmutated one cannot be scored (scoreable = False).

Usage (from the repository root; needs numpy and pandas):
  python3 experiments/requirements-conformance/run_reqconf.py [--out FILE] [--check]
With --check the result is compared row by row with the shipped survivor_scoring.csv.
"""
import argparse, hashlib, json, os, re, sys, itertools
import numpy as np, pandas as pd

ap = argparse.ArgumentParser(description=__doc__.split("\n")[0])
HERE = os.path.dirname(os.path.abspath(__file__)); REPO = os.path.abspath(os.path.join(HERE, "..", ".."))
MT = os.path.join(REPO, "experiments", "mutation-testing")
ap.add_argument("--forge", default=REPO, help="repository root (default: derived from this file)")
ap.add_argument("--pool", default=os.path.join(MT, "kill_table.csv"), help="the campaign's kill table (LRE rows with disposition == survived)")
ap.add_argument("--baseline-thy", default=os.path.join(MT, "outputs", "baseline-lre", "isabelle", "LreController_Beh.thy"))
ap.add_argument("--mutant-root", default=os.path.join(MT, "outputs"), help="<root>/<mutant_id>/isabelle/LreController_Beh.thy")
ap.add_argument("--exclude", default="", help="comma-separated mutant ids to drop from the pool")
ap.add_argument("--out", default=os.path.join(HERE, "survivor_scoring.csv"))
ap.add_argument("--idle-check", default=os.path.join(HERE, "idle_check.csv"), help="CSV with the Idle_<State> complement check per theory")
ap.add_argument("--check", action="store_true", help="compare the result with the shipped survivor_scoring.csv row by row")
args = ap.parse_args()

RC = os.path.join(args.forge, "experiments", "requirements-conformance")
SRC = open(os.path.join(RC, "reqconf_relation.py")).read()

def slice_between(s, a, b):
    i = s.index(a); j = s.index(b, i); return s[i:j]

# ---- execute Forge's own relation code, unchanged, in a private namespace ----
hash_block = slice_between(SRC, "# ---- the compiled requirements model", "# ================= part 1")
part2 = slice_between(SRC, "# ================= part 2", "# ---- load baseline theory ----")
ns = dict(HERE=RC, np=np, pd=pd, re=re, json=json, hashlib=hashlib, os=os, sys=sys, itertools=itertools)
exec(hash_block, ns)     # verifies sha256(requirements_model_lre.json) against ordering_attestation.txt
exec(part2, ns)          # REQ_TRANS, grid, req_sets2, tok_eval, parse_archived_thy, op_enabled2, relation2, verdicts2
print("relation code executed from", os.path.join(RC, "reqconf_relation.py"),
      "(part 2: %d lines)" % part2.count("\n"), "| REQ_TRANS =", len(ns["REQ_TRANS"]))

NV2, tok_eval, parse_archived_thy = ns["NV2"], ns["tok_eval"], ns["parse_archived_thy"]
relation2, verdicts2, MODES, EVENTS = ns["relation2"], ns["verdicts2"], ns["MODES"], ns["EVENTS"]

# ---- adaptation 1: the published precondition conjunct forms ----
RE_IN = re.compile(r"(\w+)\\<in>offered$")
RE_NOT_IN = re.compile(r"\\<not>\((\w+)\\<in>offered\)$")
SUBSET = "offered\\<subseteq>listens"

# ---- precondition evaluation over the grid (published conjunct forms) ----
def split_top(s, sep="\\<and>"):
    parts, depth, cur, i = [], 0, "", 0
    while i < len(s):
        if s.startswith(sep, i) and depth == 0:
            parts.append(cur); cur = ""; i += len(sep); continue
        ch = s[i]
        depth += (ch == "("); depth -= (ch == ")")
        cur += ch; i += 1
    parts.append(cur)
    return [p for p in parts if p]

def balanced_wrap(s):
    """True when s is `(...)` with the outer parens matching each other."""
    if not (s.startswith("(") and s.endswith(")")): return False
    depth = 0
    for k, ch in enumerate(s):
        depth += (ch == "("); depth -= (ch == ")")
        if depth == 0 and k < len(s) - 1: return False
    return True

def eval_conj(c, ev):
    """One whitespace-free precondition conjunct over the grid for event ev."""
    if c.startswith("\\<not>") and balanced_wrap(c[len("\\<not>"):]):
        inner = c[len("\\<not>") + 1:-1]
        return ~eval_conjs(split_top(inner), ev)
    m = RE_IN.match(c)
    if m: return np.full(NV2, m.group(1) == ev)          # the triggering event, now in `pre`
    m = RE_NOT_IN.match(c)
    if m: return np.full(NV2, m.group(1) != ev)          # sibling-priority negation of a trigger
    if c == SUBSET: return np.ones(NV2, dtype=bool)      # always true in this abstraction
    neg = c.startswith("\\<not>")
    tok = c[len("\\<not>"):] if neg else c
    if balanced_wrap(tok): tok = tok[1:-1]
    arr = tok_eval(tok)
    return (~arr) if neg else arr

def eval_conjs(conjs, ev):
    en = np.ones(NV2, dtype=bool)
    for c in conjs: en &= eval_conj(c, ev)
    return en

def op_enabled2_published(o, ev):
    if o["ev_bound"] is not None and o["ev_bound"] != ev:
        return np.zeros(NV2, dtype=bool)
    en = np.ones(NV2, dtype=bool)
    for c in o["conjs"]:
        en &= eval_conj(c, ev)
        if not en.any(): break
    return en

ns["op_enabled2"] = op_enabled2_published      # relation2 looks op_enabled2 up in its globals

# ---- adaptation 2: Idle_<State> operations are "no transition" and leave the operation set ----
def is_idle(o):
    return o["name"].startswith("Idle_")

def parse_published_thy(txt):
    """parse_archived_thy (reqconf_relation.py) with ONE change: the precondition is split
    at top-level \\<and> only (depth-aware), so a sibling-priority negation of a compound
    guard, \\<not>(a \\<and> b), stays one conjunct instead of being fragmented."""
    ops = []
    for om in re.finditer(r'zoperation\s+(\w+)\s*=\s*over\s+\w+\s*(?:params[^\n]*\n)?\s*pre\s+"(.*?)"\s*update\s+"\[(.*?)\]"', txt, re.S):
        name, pre, upd = om.group(1), om.group(2), om.group(3)
        pn = re.sub(r'\s+', '', pre)
        ms = re.match(r'st=(\w+)', pn)
        if not ms:
            ops.append({"name": name, "src": None, "raw_pre": pn}); continue
        src = ms.group(1)
        rest = pn[ms.end():]
        conjs = [c for c in split_top(rest) if c]          # <- was: rest.split("\\<and>")
        mt = re.search(r'st\\<Zprime>=\s*(\w+)', upd)
        tgt = mt.group(1)
        evs = re.findall(r'\[Event\s+(\w+)\]', upd)
        in_evs = [e for e in evs if e in ns["INPUT_EVTS"]]
        out_evs = {e for e in evs if e in ns["OUTPUT_EVTS"]}
        ev_bound = in_evs[0] if (not conjs and in_evs) else None
        ops.append({"name": name, "src": src, "tgt": tgt, "conjs": conjs,
                    "ev_bound": ev_bound, "trace_in": in_evs, "outputs": out_evs, "raw_pre": pn})
    return ops

def load_ops(path):
    allops = parse_published_thy(open(path).read())
    ops = [o for o in allops if o.get("src") not in (None, "initial") and not is_idle(o)]
    idle = [o for o in allops if o.get("src") not in (None, "initial") and is_idle(o)]
    return ops, idle

def idle_check(thy_id, path, ops, idle):
    rows = []
    for o in idle:
        pn = o["raw_pre"]
        conjs = split_top(pn)
        assert conjs[0] == "st=" + o["src"], conjs[0]
        ok = True; bad = []
        for ev in EVENTS:
            try:
                idle_en = eval_conjs(conjs[1:], ev)
            except KeyError as e:
                ok = False; bad.append(f"{ev}:unevaluable({e})"); continue
            union = np.zeros(NV2, dtype=bool)
            for s in ops:
                if s["src"] == o["src"]: union |= op_enabled2_published(s, ev)
            if not np.array_equal(idle_en, ~union):
                ok = False; bad.append(f"{ev}:{int((idle_en != ~union).sum())}cells")
        rows.append(dict(theory=thy_id, idle_op=o["name"], state=o["src"], target=o["tgt"],
                         self_loop=(o["tgt"] == o["src"]), complement_of_siblings=ok, detail=";".join(bad)))
    return rows

# ---- baseline ----
base_ops, base_idle = load_ops(args.baseline_thy)
base_ver = verdicts2(relation2(base_ops))
idle_rows = idle_check("baseline", args.baseline_thy, base_ops, base_idle)
nb_dis = sum(int(d.sum()) for d, _ in base_ver.values()); nb_ov = sum(int(o.sum()) for _, o in base_ver.values())
print(f"baseline: {len(base_ops)} transition operations, {len(base_idle)} Idle operations excluded; "
      f"baseline disagreement cells={nb_dis} output-violation cells={nb_ov}")

# ---- the pool ----
kt = pd.read_csv(args.pool)
pool = kt[(kt["study"] == "lre") & (kt["disposition"] == "survived")].copy()
pool["model_changed_thy"] = pool["theory_changed"].map(lambda v: "yes" if str(v).strip().lower() in ("true", "yes", "1") else "no")
excl = {x for x in args.exclude.split(",") if x}
dropped = [m for m in pool["mutant_id"] if m in excl]
pool = pool[~pool["mutant_id"].isin(excl)]
print(f"pool: {len(pool)} LRE survivors ({len(dropped)} excluded: {', '.join(dropped) or 'none'})")

REASON_UNCHANGED = "not scoreable: theory identical to the unmutated one (the defect changes only the Dafny program or the RoboChart model)"
rows = []
for _, mrow in pool.iterrows():
    mid = mrow["mutant_id"]
    thy_path = os.path.join(args.mutant_root, mid, "isabelle", "LreController_Beh.thy")
    if mrow["model_changed_thy"] != "yes":
        rows.append(dict(mutant_id=mid, defect_class=mrow["defect_class"], thy_changed="no", scoreable=False,
                         killed=False, new_disagree=0, new_outviol=0, reason=REASON_UNCHANGED)); continue
    if not os.path.exists(thy_path):
        rows.append(dict(mutant_id=mid, defect_class=mrow["defect_class"], thy_changed=mrow["model_changed_thy"],
                         scoreable=False, killed=False, new_disagree=0, new_outviol=0,
                         reason="mutant theory missing from evidence tree")); continue
    try:
        mops, midle = load_ops(thy_path)
        idle_rows += idle_check(mid, thy_path, mops, midle)
        mver = verdicts2(relation2(mops))
        nd = nov = 0; where = []
        for key in mver:
            d_m, o_m = mver[key]; d_b, o_b = base_ver[key]
            new_d = d_m & ~d_b; new_o = o_m & ~o_b
            if new_d.any() or new_o.any():
                nd += int(new_d.sum()); nov += int(new_o.sum())
                where.append(f"{key[0]}/{key[1]}:{int(new_d.sum())}d+{int(new_o.sum())}o")
        killed = (nd + nov) > 0
        rows.append(dict(mutant_id=mid, defect_class=mrow["defect_class"], thy_changed="yes", scoreable=True,
                         killed=killed, new_disagree=nd, new_outviol=nov,
                         reason=("KILLED: " + "; ".join(where[:5])) if killed
                                else "thy changed but relation verdicts vs requirements unchanged"))
    except Exception as e:
        rows.append(dict(mutant_id=mid, defect_class=mrow["defect_class"], thy_changed="yes", scoreable=False,
                         killed=False, new_disagree=0, new_outviol=0, reason=f"PARSE FAILURE: {e}"))

out = pd.DataFrame(rows, columns=["mutant_id", "defect_class", "thy_changed", "scoreable", "killed",
                                  "new_disagree", "new_outviol", "reason"])
os.makedirs(os.path.dirname(os.path.abspath(args.out)), exist_ok=True)
out.to_csv(args.out, index=False)
print(f"wrote {args.out}: {len(out)} rows, {int(out.scoreable.sum())} scoreable, {int(out.killed.sum())} killed")

if args.idle_check:
    idf = pd.DataFrame(idle_rows, columns=["theory", "idle_op", "state", "target", "self_loop", "complement_of_siblings", "detail"])
    idf.to_csv(args.idle_check, index=False)
    print(f"Idle check: {len(idf)} Idle operations over {idf.theory.nunique()} theories; "
          f"complement property holds for {int(idf.complement_of_siblings.sum())}/{len(idf)}")

if args.check:
    SHIP = os.path.join(HERE, "survivor_scoring.csv")
    ship = pd.read_csv(SHIP)
    m = ship.merge(out, on="mutant_id", how="outer", suffixes=("_shipped", "_new"), indicator=True)
    only = m[m["_merge"] != "both"]
    diff = m[(m["_merge"] == "both") & (m["killed_shipped"] != m["killed_new"])]
    diff2 = m[(m["_merge"] == "both") & ((m["new_disagree_shipped"] != m["new_disagree_new"]) |
                                         (m["new_outviol_shipped"] != m["new_outviol_new"]) |
                                         (m["scoreable_shipped"] != m["scoreable_new"]))]
    print(f"CHECK vs {SHIP}: {len(m[m['_merge']=='both'])} common rows; "
          f"killed differs on {len(diff)}; counts/scoreable differ on {len(diff2)}; rows only on one side: {len(only)}")
    for _, r in diff.iterrows():
        print("  DIFF", r["mutant_id"], "shipped killed =", r["killed_shipped"], "new killed =", r["killed_new"])
    for _, r in only.iterrows():
        print("  ONLY", r["_merge"], r["mutant_id"])
    sys.exit(0 if (len(diff) == 0 and len(only) == 0) else 1)
