#!/usr/bin/env python3
"""Requirements-conformance check for the LRE (paper Section 4.9, RQ7).

Compares the LRE's behavioural requirements, compiled into a transition
relation (requirements_model_lre.json), with the extracted Isabelle model,
and scores the 62 LRE mutants that survive every verifier (Section 4.8).

Inputs, all inside this repository:
  experiments/requirements-conformance/requirements_model_lre.json
      the compiled requirements; its SHA-256 is checked against
      ordering_attestation.txt before anything is run
  reference-runs/lre/formal-artefacts/isabelle/LreController_Beh.thy
      the extracted model of the submitted LRE run
  experiments/mutation-testing/kill_table_all_backends.csv
  experiments/mutation-testing/outputs/<mutant>/isabelle/LreController_Beh.thy
      the mutation-testing results and each mutant's extracted model

Outputs (written to --out, default: a temporary directory):
  archived_conformance_diff.csv    per-cell verdicts, 458,752 rows
  archived_survivor_scoring.csv    one row per surviving mutant, 62 rows

With --check, both outputs are compared byte for byte with the copies
shipped in this directory.

Usage:
  python3 experiments/requirements-conformance/run_reqconf.py [--out DIR] [--check]
Requires Python 3 with numpy and pandas.
"""
import argparse, hashlib, json, os, re, sys, tempfile, itertools, filecmp
import numpy as np, pandas as pd

HERE = os.path.dirname(os.path.abspath(__file__))
REPO = os.path.abspath(os.path.join(HERE, "..", ".."))
ap = argparse.ArgumentParser(description=__doc__.split("\n")[0])
ap.add_argument("--out", default=None, help="output directory (default: a new temporary directory)")
ap.add_argument("--check", action="store_true", help="compare outputs with the shipped CSVs")
args = ap.parse_args()
OUT = args.out or tempfile.mkdtemp(prefix="reqconf-")
os.makedirs(OUT, exist_ok=True)

# ---- the compiled requirements model, checked against the recorded hash ----
MODEL = os.path.join(HERE, "requirements_model_lre.json")
digest = hashlib.sha256(open(MODEL, "rb").read()).hexdigest()
m = re.search(r"sha256\(requirements_model_lre\.json\)\s*=\s*([0-9a-f]{64})", open(os.path.join(HERE, "ordering_attestation.txt")).read())
if not m or m.group(1) != digest:
    sys.exit(f"requirements_model_lre.json does not match the hash in ordering_attestation.txt ({digest})")
print("requirements model hash matches the ordering record:", digest[:16])
_model = json.load(open(MODEL))
ATOMS = {k: {kk: v[kk] for kk in ("variable", "comparator", "bound")} for k, v in _model["atoms"].items()}
TRANSITIONS = _model["transitions"]

# ================= part 1: conformance of the extracted model =================
ATOM_KEY = {"inOpez":"inOpez","vel_le_1":"vel_le_1","odist_cdyn_gt_1":"odc_gt_1","odist_cstc_gt_1":"ocs_gt_1",
 "cda_lt_minSafe":"cda_lt_ms","tcpa_ge_0":"tcpa_ge_0","hvel_ge_1":"hvel_ge_1",
 "hdist_le_horiz":"hdist_le_h","vdist_le_dfltvert":"vdist_le_dv","vvel_ge_1":"vvel_ge_1","vdist_le_vert":"vdist_le_v"}

REQ_TRANS = []
for t in TRANSITIONS:
    if t["kind"] != "transition": continue
    REQ_TRANS.append({"req_id": t["req_id"], "src": t["source_mode"], "ev": t["trigger_event_or_none"],
        "tgt": t["target_mode"], "outputs": t["outputs"],
        "conjs": [(ATOM_KEY[c["atom"]], c["negated"]) for c in t["guard_conjuncts"]]})

INPUT_EVTS = {"reqVel","reqHdng","reqMOM","reqOCM","endTask","reqHCM"}
OUTPUT_EVTS = {"advVel","advHdng"}
MODES = ["OCM","MOM","HCM","CAM"]
EVENTS = ["reqVel","reqHdng","reqMOM","reqOCM","endTask","reqHCM","tick"]
MBIT = {m: 1<<i for i,m in enumerate(MODES)}

ATOM14 = ["inOpez","vel_le_1","odc_gt_1","ocs_gt_1","cda_lt_ms","tcpa_ge_0",
           "hvel_ge_1","hdist_le_h","vdist_le_dv","vvel_ge_1","vdist_le_v",
           "camActive","hcmActive","momReturn"]
NV = 1 << len(ATOM14)
idx = np.arange(NV, dtype=np.uint32)
B = {a: ((idx >> i) & 1).astype(bool) for i, a in enumerate(ATOM14)}

cam_def = B["cda_lt_ms"] & B["tcpa_ge_0"]
hcm_def = (B["hvel_ge_1"] & B["hdist_le_h"]) | B["vdist_le_dv"] | (B["vvel_ge_1"] & B["vdist_le_v"])
mom_def = (~B["hdist_le_h"]) & (~B["vdist_le_v"])
consistent = (B["camActive"]==cam_def) & (B["hcmActive"]==hcm_def) & (B["momReturn"]==mom_def)

def req_sets(mode, ev):
    mask = np.zeros(NV, dtype=np.uint8); det = []
    for r in REQ_TRANS:
        if r["src"] != mode: continue
        if r["ev"] is not None and r["ev"] != ev: continue
        en = np.ones(NV, dtype=bool)
        for a, neg in r["conjs"]:
            en &= (~B[a]) if neg else B[a]
        mask |= np.where(en, MBIT[r["tgt"]], 0).astype(np.uint8)
        det.append((r["req_id"], r["tgt"], r["outputs"], en))
    return mask, det

def parse_archived_thy(txt):
    ops = []
    for om in re.finditer(r'zoperation\s+(\w+)\s*=\s*over\s+\w+\s*(?:params[^\n]*\n)?\s*pre\s+"(.*?)"\s*update\s+"\[(.*?)\]"', txt, re.S):
        name, pre, upd = om.group(1), om.group(2), om.group(3)
        pn = re.sub(r'\s+','', pre)
        ms = re.match(r'st=(\w+)', pn)
        if not ms:
            ops.append({"name":name,"src":None,"raw_pre":pn}); continue
        src = ms.group(1)
        rest = pn[ms.end():]
        conjs = [c for c in rest.split("\\<and>") if c]
        mt = re.search(r'st\\<Zprime>=\s*(\w+)', upd)
        tgt = mt.group(1)
        evs = re.findall(r'\[Event\s+(\w+)\]', upd)
        in_evs = [e for e in evs if e in INPUT_EVTS]
        out_evs = {e for e in evs if e in OUTPUT_EVTS}
        ev_bound = in_evs[0] if (not conjs and in_evs) else None
        ops.append({"name":name,"src":src,"tgt":tgt,"conjs":conjs,
                    "ev_bound":ev_bound,"trace_in":in_evs,"outputs":out_evs})
    return ops

GTOK = {
 "vel\\<le>1.0": lambda: B["vel_le_1"],
 "odist(cdyn)>1.0": lambda: B["odc_gt_1"],
 "odist(cstc)>1.0": lambda: B["ocs_gt_1"],
 "cda\\<ge>minsafedist()": lambda: ~B["cda_lt_ms"],
 "inOpez": lambda: B["inOpez"],
 "camActive": lambda: B["camActive"],
 "hcmActive": lambda: B["hcmActive"],
 "momReturn": lambda: B["momReturn"],
}

def op_enabled(o, ev):
    if o["ev_bound"] is not None and o["ev_bound"] != ev:
        return np.zeros(NV, dtype=bool)
    en = np.ones(NV, dtype=bool)
    for c in o["conjs"]:
        neg = c.startswith("\\<not>")
        tok = c[len("\\<not>"):] if neg else c
        arr = GTOK[tok]()
        en &= (~arr) if neg else arr
    return en

ARCH = os.path.join(REPO, "reference-runs/lre/formal-artefacts/isabelle/LreController_Beh.thy")
arch_ops = [o for o in parse_archived_thy(open(ARCH).read()) if o.get("src") not in (None, "initial")]

rows = []
counts = {}
for mode in MODES:
    for ev in EVENTS:
        rmask, rdet = req_sets(mode, ev)
        tmask = np.zeros(NV, dtype=np.uint8)
        opnames_enabled = {}
        for o in arch_ops:
            if o["src"] != mode: continue
            en = op_enabled(o, ev)
            if en.any():
                tmask |= np.where(en, MBIT[o["tgt"]], 0).astype(np.uint8)
                opnames_enabled[o["name"]] = (en, o)
        both_empty = (rmask==0) & (tmask==0)
        exact = (rmask==tmask) & (rmask!=0)
        subset = ((tmask & ~rmask)==0)
        refin = subset & (tmask!=0) & (tmask!=rmask)
        req_only = (tmask==0) & (rmask!=0)
        code_extra = (tmask & ~rmask) != 0
        verdict = np.full(NV, "", dtype=object)
        verdict[both_empty] = "agree_stay"
        verdict[exact] = "agree_exact"
        verdict[refin] = "agree_refinement"
        verdict[req_only] = "REQ_transition_CODE_none"
        verdict[code_extra] = "CODE_transition_REQ_none"
        dis = req_only | code_extra
        mech = np.full(NV, "", dtype=object)
        mech[dis & ~consistent] = "frozen_data_plane"
        cons_dis = dis & consistent
        if cons_dis.any():
            c2 = np.zeros(NV, dtype=bool)
            if mode=="OCM" and ev!="reqMOM" and "OCMToMOM" in opnames_enabled:
                en_o = opnames_enabled["OCMToMOM"][0]
                only_mom_extra = ((tmask & ~rmask) == MBIT["MOM"])
                c2 = cons_dis & en_o & only_mom_extra
            mech[cons_dis & c2] = "C2_trigger_not_in_pre"
            mech[cons_dis & ~c2] = "UNMAPPED"
        outviol = np.zeros(NV, dtype=bool)
        for (rid, tgt, routs, en) in rdet:
            if not routs: continue
            same_tgt = [d for d in rdet if d[1]==tgt]
            if len(same_tgt) != 1: continue
            need = {re.match(r'(\w+)\(', o).group(1) for o in routs}
            got_ok = np.zeros(NV, dtype=bool); any_tgt = np.zeros(NV, dtype=bool)
            for nm,(eno,o) in opnames_enabled.items():
                if o["tgt"]==tgt:
                    any_tgt |= eno
                    if need <= o["outputs"]: got_ok |= eno
            outviol |= en & any_tgt & ~got_ok & ~dis
        counts.setdefault("outviol", 0); counts["outviol"] += int(outviol.sum())
        for v in ("agree_stay","agree_exact","agree_refinement","REQ_transition_CODE_none","CODE_transition_REQ_none"):
            counts[v] = counts.get(v,0) + int((verdict==v).sum())
        for m in ("frozen_data_plane","C2_trigger_not_in_pre","UNMAPPED"):
            counts[m] = counts.get(m,0) + int((mech==m).sum())
        dfp = pd.DataFrame({a: B[a] for a in ATOM14})
        dfp.insert(0,"mode",mode); dfp.insert(1,"event",ev)
        dfp["flags_consistent"]=consistent
        dfp["req_targets"]=[ "|".join(m2 for m2 in MODES if rm & MBIT[m2]) for rm in rmask ]
        dfp["thy_targets"]=[ "|".join(m2 for m2 in MODES if tm & MBIT[m2]) for tm in tmask ]
        dfp["verdict"]=verdict; dfp["mechanism"]=mech; dfp["output_violation"]=outviol
        rows.append(dfp)

adf = pd.concat(rows, ignore_index=True)
adf.to_csv(os.path.join(OUT, "archived_conformance_diff.csv"), index=False)
print("wrote archived_conformance_diff.csv:", len(adf), "rows")

# ================= part 2: scoring the surviving mutants =================
ATOM_KEY = {"inOpez":"inOpez","vel_le_1":"vel_le_1","odist_cdyn_gt_1":"odc_gt_1","odist_cstc_gt_1":"ocs_gt_1",
 "cda_lt_minSafe":"cda_lt_ms","tcpa_ge_0":"tcpa_ge_0","hvel_ge_1":"hvel_ge_1",
 "hdist_le_horiz":"hdist_le_h","vdist_le_dfltvert":"vdist_le_dv","vvel_ge_1":"vvel_ge_1","vdist_le_vert":"vdist_le_v"}

REQ_TRANS = []
for t in TRANSITIONS:
    if t["kind"] != "transition": continue
    REQ_TRANS.append({"req_id": t["req_id"], "src": t["source_mode"], "ev": t["trigger_event_or_none"],
        "tgt": t["target_mode"], "outputs": t["outputs"],
        "conjs": [(ATOM_KEY[c["atom"]], c["negated"]) for c in t["guard_conjuncts"]]})
assert len(REQ_TRANS) == 17

INPUT_EVTS = {"reqVel","reqHdng","reqMOM","reqOCM","endTask","reqHCM"}
OUTPUT_EVTS = {"advVel","advHdng"}
MODES = ["OCM","MOM","HCM","CAM"]
EVENTS = ["reqVel","reqHdng","reqMOM","reqOCM","endTask","reqHCM","tick"]
MBIT = {m: 1<<i for i,m in enumerate(MODES)}

# ---- hybrid concrete/abstract space ----
GVALS = np.array([0.5,1.0,1.5,2.0,2.5])
BITNAMES = ["inOpez","cda_lt_ms","tcpa_ge_0","hvel_ge_1","hdist_le_h","vdist_le_dv","vvel_ge_1","vdist_le_v",
            "camActive","hcmActive","momReturn"]
g = np.meshgrid(GVALS, GVALS, GVALS, np.arange(1<<len(BITNAMES)), indexing="ij")
VELc, ODCc, OCSc, bidx = [x.ravel() for x in g]
NV2 = VELc.size
Bh = {a: ((bidx.astype(np.uint32) >> i) & 1).astype(bool) for i, a in enumerate(BITNAMES)}

REQ_ATOM = {
 "inOpez": lambda: Bh["inOpez"], "vel_le_1": lambda: VELc<=1.0,
 "odc_gt_1": lambda: ODCc>1.0, "ocs_gt_1": lambda: OCSc>1.0,
 "cda_lt_ms": lambda: Bh["cda_lt_ms"], "tcpa_ge_0": lambda: Bh["tcpa_ge_0"],
 "hvel_ge_1": lambda: Bh["hvel_ge_1"], "hdist_le_h": lambda: Bh["hdist_le_h"],
 "vdist_le_dv": lambda: Bh["vdist_le_dv"], "vvel_ge_1": lambda: Bh["vvel_ge_1"],
 "vdist_le_v": lambda: Bh["vdist_le_v"],
}

def req_sets2(mode, ev):
    mask = np.zeros(NV2, dtype=np.uint8); det = []
    for r in REQ_TRANS:
        if r["src"] != mode: continue
        if r["ev"] is not None and r["ev"] != ev: continue
        en = np.ones(NV2, dtype=bool)
        for a, neg in r["conjs"]:
            arr = REQ_ATOM[a]()
            en &= (~arr) if neg else arr
        mask |= np.where(en, MBIT[r["tgt"]], 0).astype(np.uint8)
        det.append((r["req_id"], r["tgt"], r["outputs"], en))
    return mask, det

CMP = {"\\<le>": np.less_equal, "\\<ge>": np.greater_equal, "<": np.less, ">": np.greater, "=": np.equal}

def tok_eval(tok):
    if tok in ("inOpez","camActive","hcmActive","momReturn"): return Bh[tok]
    m = re.match(r'cda(\\<ge>|\\<le>|<|>)minsafedist\(\)$', tok)
    if m:
        op = m.group(1)
        if op in ("\\<ge>",">"): return ~Bh["cda_lt_ms"]
        else: return Bh["cda_lt_ms"]
    m = re.match(r'(vel|odist\(cdyn\)|odist\(cstc\))(\\<le>|\\<ge>|<|>)([\d.]+)$', tok)
    if m:
        var = {"vel":VELc,"odist(cdyn)":ODCc,"odist(cstc)":OCSc}[m.group(1)]
        return CMP[m.group(2)](var, float(m.group(3)))
    raise KeyError(tok)

def parse_archived_thy(txt):
    ops = []
    for om in re.finditer(r'zoperation\s+(\w+)\s*=\s*over\s+\w+\s*(?:params[^\n]*\n)?\s*pre\s+"(.*?)"\s*update\s+"\[(.*?)\]"', txt, re.S):
        name, pre, upd = om.group(1), om.group(2), om.group(3)
        pn = re.sub(r'\s+','', pre)
        ms = re.match(r'st=(\w+)', pn)
        if not ms:
            ops.append({"name":name,"src":None,"raw_pre":pn}); continue
        src = ms.group(1)
        rest = pn[ms.end():]
        conjs = [c for c in rest.split("\\<and>") if c]
        mt = re.search(r'st\\<Zprime>=\s*(\w+)', upd)
        tgt = mt.group(1)
        evs = re.findall(r'\[Event\s+(\w+)\]', upd)
        in_evs = [e for e in evs if e in INPUT_EVTS]
        out_evs = {e for e in evs if e in OUTPUT_EVTS}
        ev_bound = in_evs[0] if (not conjs and in_evs) else None
        ops.append({"name":name,"src":src,"tgt":tgt,"conjs":conjs,
                    "ev_bound":ev_bound,"trace_in":in_evs,"outputs":out_evs})
    return ops

def op_enabled2(o, ev):
    if o["ev_bound"] is not None and o["ev_bound"] != ev: return np.zeros(NV2, dtype=bool)
    en = np.ones(NV2, dtype=bool)
    for c in o["conjs"]:
        neg = c.startswith("\\<not>")
        tok = c[len("\\<not>"):] if neg else c
        arr = tok_eval(tok)
        en &= (~arr) if neg else arr
    return en

def relation2(ops):
    rel = {}
    for mode in MODES:
        for ev in EVENTS:
            tmask = np.zeros(NV2, dtype=np.uint8); ens = []
            for o in ops:
                if o["src"] != mode: continue
                en = op_enabled2(o, ev)
                if en.any():
                    tmask |= np.where(en, MBIT[o["tgt"]], 0).astype(np.uint8)
                    ens.append((en, o))
            rel[(mode,ev)] = (tmask, ens)
    return rel

def verdicts2(rel):
    out = {}
    for mode in MODES:
        for ev in EVENTS:
            rmask, rdet = req_sets2(mode, ev)
            tmask, ens = rel[(mode,ev)]
            dis = ((tmask==0) & (rmask!=0)) | ((tmask & ~rmask) != 0)
            outviol = np.zeros(NV2, dtype=bool)
            for (rid, tgt, routs, en) in rdet:
                if not routs: continue
                if sum(1 for d in rdet if d[1]==tgt) != 1: continue
                need = {re.match(r'(\w+)\(', o).group(1) for o in routs}
                got_ok = np.zeros(NV2, dtype=bool); any_tgt = np.zeros(NV2, dtype=bool)
                for eno, o in ens:
                    if o["tgt"]==tgt:
                        any_tgt |= eno
                        if need <= o["outputs"]: got_ok |= eno
                outviol |= en & any_tgt & ~got_ok & ~dis
            out[(mode,ev)] = (dis, outviol)
    return out

# ---- load baseline theory ----
ARCH = os.path.join(REPO, "reference-runs/lre/formal-artefacts/isabelle/LreController_Beh.thy")
base_ops = [o for o in parse_archived_thy(open(ARCH).read()) if o.get("src") not in (None,"initial")]
base_ver2 = verdicts2(relation2(base_ops))

# ---- the 62 LRE mutants that survive every verifier, and their extracted models ----
kt = pd.read_csv(os.path.join(REPO, "experiments/mutation-testing/kill_table_all_backends.csv"))
lre_surv = kt[(kt["study"]=="lre") & ~kt["any_killed"] & ~kt["isabelle_killed"]]
surv_ids = lre_surv["mutant_id"].tolist()
assert len(lre_surv) == 62, len(lre_surv)

# ---- score each survivor ----
rows = []
for _, mrow in lre_surv.iterrows():
    mid = mrow["mutant_id"]
    thy_path = os.path.join(REPO, "experiments/mutation-testing/outputs", mid, "isabelle", "LreController_Beh.thy")
    if mrow["model_changed_thy"] != "yes":
        rows.append(dict(mutant_id=mid, defect_class=mrow["defect_class"], file=mrow["file"], line=mrow["line"],
            thy_changed="no", scoreable=False, killed=False, new_disagree=0, new_outviol=0,
            # reason text kept verbatim so the output matches the shipped CSV byte for byte
            reason="extraction-invisible on shipped extractor: .thy identical to baseline (frozen data plane / value-only / below abstraction)")); continue
    if not os.path.exists(thy_path):
        rows.append(dict(mutant_id=mid, defect_class=mrow["defect_class"], file=mrow["file"], line=mrow["line"],
            thy_changed=mrow["model_changed_thy"], scoreable=False, killed=False, new_disagree=0, new_outviol=0,
            reason="mutant theory missing from evidence tree")); continue
    try:
        mops = [o for o in parse_archived_thy(open(thy_path).read()) if o.get("src") not in (None,"initial")]
        mver = verdicts2(relation2(mops))
        nd = nov = 0; where = []
        for key in mver:
            d_m, o_m = mver[key]; d_b, o_b = base_ver2[key]
            new_d = d_m & ~d_b; new_o = o_m & ~o_b
            if new_d.any() or new_o.any():
                nd += int(new_d.sum()); nov += int(new_o.sum())
                where.append(f"{key[0]}/{key[1]}:{int(new_d.sum())}d+{int(new_o.sum())}o")
        killed = (nd + nov) > 0
        rows.append(dict(mutant_id=mid, defect_class=mrow["defect_class"], file=mrow["file"], line=mrow["line"],
            thy_changed="yes", scoreable=True, killed=killed, new_disagree=nd, new_outviol=nov,
            reason=("KILLED: " + "; ".join(where[:5])) if killed else "thy changed but relation verdicts vs requirements unchanged"))
    except Exception as e:
        rows.append(dict(mutant_id=mid, defect_class=mrow["defect_class"], file=mrow["file"], line=mrow["line"],
            thy_changed="yes", scoreable=False, killed=False, new_disagree=0, new_outviol=0,
            reason=f"PARSE FAILURE: {e}"))

asc = pd.DataFrame(rows)
asc.to_csv(os.path.join(OUT, "archived_survivor_scoring.csv"), index=False)
print("wrote archived_survivor_scoring.csv:", len(asc), "rows,", int(asc["scoreable"].sum()), "scoreable,", int(asc["killed"].sum()), "killed")

# ---- optional byte-for-byte check against the shipped outputs ----
if args.check:
    bad = [f for f in ("archived_conformance_diff.csv", "archived_survivor_scoring.csv")
           if not filecmp.cmp(os.path.join(OUT, f), os.path.join(HERE, f), shallow=False)]
    if bad:
        sys.exit("differs from the shipped copy: " + ", ".join(bad))
    print("both outputs are byte-identical to the shipped copies")
print("outputs in", OUT)
