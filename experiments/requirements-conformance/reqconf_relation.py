#!/usr/bin/env python3
"""The compiled LRE requirements relation (paper Section 4.9, RQ7): library.

This module holds the hash check of requirements_model_lre.json against
ordering_attestation.txt, the compiled transition relation (17 mode-level
transitions over 11 atomic predicates), the hybrid concrete/abstract grid, the
theory parser and the per-cell comparison.  It is not run on its own:

  run_reqconf_conformance.py  compares the extracted model of the unmutated LRE
                              with the relation, cell by cell (conformance_diff.csv)
  run_reqconf.py              scores the LRE mutants that survive every verifier
                              (survivor_scoring.csv)

Both programs execute the code between the markers below unchanged.
"""
import hashlib, json, os, re, sys, itertools
import numpy as np, pandas as pd

HERE = os.path.dirname(os.path.abspath(__file__))
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
# (conformance of the extracted model: see run_reqconf_conformance.py)

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
# (scoring: see run_reqconf.py)
