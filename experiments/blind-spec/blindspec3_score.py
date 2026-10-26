#!/usr/bin/env python3
"""Score the LRE mutants that survive every verifier against the independently
written LRE reference specification, round 3 (LRE-reference-spec.md; paper Section 4.9.1).

The specification's relation R(mode, event, observation) is the set of (destination,
output sequence) pairs of all enabled rows T02-T18, with a hold row when none is
enabled.  Each mutant's Java step() chain (experiments/mutation-testing/mutants/<id>/java)
is parsed statically and evaluated on a concrete scenario grid (8,928 sensor scenarios
x 4 modes x 7 events = 249,984 cells); the 57,344-cell abstract grid serves as a
self-check of the parser and the relation on the unmutated Java (100% membership).
A mutant is killed when at some cell its outcome is not a member of R while the
unmutated controller's outcome is; thresholds are read in the way that most favours
the mutant, and kills inside the ambiguity ledger's scope are attributed to it.

The pool is experiments/mutation-testing/kill_table.csv (study == lre, disposition ==
survived).  With --check the fresh scoring is compared row by row with the shipped
blindspec3_survivor_scoring.csv.

Usage, from the repository root (needs numpy):
    python3 experiments/blind-spec/blindspec3_score.py [--out FILE] [--check]
"""
import argparse, re, os, csv, sys, itertools
import numpy as np
from itertools import product
from collections import Counter
np.seterr(all="ignore")

ap = argparse.ArgumentParser(description=__doc__.split("\n")[0])
HERE = os.path.dirname(os.path.abspath(__file__)); REPO = os.path.abspath(os.path.join(HERE, "..", ".."))
ap.add_argument("--forge", default=REPO, help="repository root (default: derived from this file)")
ap.add_argument("--pool", default=os.path.join(REPO, "experiments", "mutation-testing", "kill_table.csv"), help="the campaign's kill table (lre rows with disposition == survived)")
ap.add_argument("--exclude", default="")
ap.add_argument("--out", default=os.path.join(HERE, "blindspec3_survivor_scoring.csv"))
ap.add_argument("--check", action="store_true", help="compare the fresh scoring with the shipped blindspec3_survivor_scoring.csv row by row")
args = ap.parse_args()

FORGE = args.forge
BASE = os.path.join(FORGE, "reference-runs", "lre", "java")
MUT = os.path.join(FORGE, "experiments", "mutation-testing", "mutants")
MANIFEST = os.path.join(FORGE, "experiments", "mutation-testing", "mutants_manifest.csv")
SHIPPED = os.path.join(FORGE, "experiments", "blind-spec", "blindspec3_survivor_scoring.csv")
mm = {r["mutant_id"]: r for r in csv.DictReader(open(MANIFEST))}

# ============ spec3 relation: SET-VALUED, no priority ============
ATOMS = ["inOpez","vel_le_1","odc_gt_1","ocs_gt_1","cda_lt_ms","tcpa_ge_0",
         "hvel_ge_1","hdist_le_h","vdist_le_dv","vvel_ge_1","vdist_le_v"]
EVENTS = ["reqVel","reqHdng","reqOCM","reqMOM","reqHCM","endTask","none"]
MODES  = ["OCM","MOM","HCM","CAM"]

SPEC_ROWS = {
 "T02": ("OCM","reqVel", None,"OCM", ("advVel:v",)),
 "T03": ("OCM","reqHdng",None,"OCM", ("advHdng:h",)),
 "T04": ("OCM","reqMOM","EnterMOM","MOM",("advVel:1",)),
 "T05": ("MOM",None,"P","OCM",()),
 "T06": ("MOM","reqOCM",None,"OCM",()),
 "T07": ("MOM","endTask",None,"OCM",("advVel:0",)),
 "T08": ("MOM",None,"Collision","CAM",()),
 "T09": ("MOM",None,"B_h","HCM",("advVel:0",)),
 "T10": ("MOM",None,"B_0","HCM",("advVel:0",)),
 "T11": ("MOM",None,"B_v","HCM",("advVel:0",)),
 "T12": ("MOM","reqHCM",None,"HCM",("advVel:0",)),
 "T13": ("HCM",None,"ClearStatic","MOM",("advVel:1",)),
 "T14": ("HCM",None,"Collision","CAM",()),
 "T15": ("HCM","reqOCM",None,"OCM",()),
 "T16": ("HCM",None,"P","OCM",()),
 "T17": ("CAM","reqOCM",None,"OCM",()),
 "T18": ("CAM",None,"SafeCPA","OCM",("advVel:0",)),
}
def spec_guards(a):
    return dict(
        P=a["inOpez"],
        EnterMOM=a["vel_le_1"] and not a["inOpez"] and a["odc_gt_1"] and a["ocs_gt_1"],
        Collision=a["cda_lt_ms"] and a["tcpa_ge_0"],
        B_h=a["hvel_ge_1"] and a["hdist_le_h"],
        B_0=a["vdist_le_dv"],
        B_v=a["vvel_ge_1"] and a["vdist_le_v"],
        ClearStatic=(not a["hdist_le_h"]) and (not a["vdist_le_v"]),
        SafeCPA=not a["cda_lt_ms"])

def R_set(mode, e, a):
    g = spec_guards(a)
    S, rows = set(), []
    for rid, (src, trig, gname, dest, eff) in SPEC_ROWS.items():
        if src != mode: continue
        if trig is not None and trig != e: continue
        if gname is not None and not g[gname]: continue
        S.add((dest, eff)); rows.append(rid)
    if not rows:
        return {(mode, ())}, ["hold"]
    return S, sorted(rows)

def A(**kw):
    a = {k: False for k in ATOMS}; a.update(kw); return a
checks = []
S,rows = R_set("CAM","reqOCM",A())
checks.append(("CAM/reqOCM safe", S == {("OCM",()),("OCM",("advVel:0",))} and set(rows)=={"T17","T18"}))
S,rows = R_set("CAM","none",A(cda_lt_ms=True, tcpa_ge_0=False))
checks.append(("CAM stuck tcpa<0", S=={("CAM",())} and rows==["hold"]))
a = A(inOpez=True, cda_lt_ms=True, tcpa_ge_0=True, vdist_le_dv=True)
S,rows = R_set("MOM","none",a)
checks.append(("MOM triple overlap", S=={("OCM",()),("CAM",()),("HCM",("advVel:0",))}))
noobs = A(); noobs.update(odc_gt_1=True, ocs_gt_1=True)
S,_ = R_set("HCM","none",noobs); checks.append(("HCM->MOM clear", S=={("MOM",("advVel:1",))}))
S,_ = R_set("CAM","none",noobs); checks.append(("CAM->OCM clear", S=={("OCM",("advVel:0",))}))
S,r_ = R_set("MOM","none",noobs); checks.append(("MOM holds clear", S=={("MOM",())} and r_==["hold"]))
S,_ = R_set("OCM","none",noobs); checks.append(("OCM holds", S=={("OCM",())}))
a = A(inOpez=True); S,rows = R_set("HCM","none",a)
checks.append(("HCM T13+T16", set(rows)=={"T13","T16"} and S=={("MOM",("advVel:1",)),("OCM",())}))
a = A(vel_le_1=True, ocs_gt_1=True)
S,rows = R_set("OCM","reqMOM",a); checks.append(("T04 dd=1 blocked", rows==["hold"]))
a = A(vdist_le_dv=True)
S,rows = R_set("HCM","none",a); checks.append(("alternation HCM->MOM", S=={("MOM",("advVel:1",))}))
S,rows = R_set("MOM","none",a); checks.append(("alternation MOM->HCM", S=={("HCM",("advVel:0",))}))
assert all(ok for _, ok in checks), [n for n, ok in checks if not ok]
print("spec relation: all", len(checks), "boundary/overlap checks pass")

# ============ Java parser ============
TOKRE = re.compile(r"""
  (?P<num>\d+\.\d+|\d+)
| (?P<op>\|\||&&|<=|>=|==|!=|<|>|!)
| (?P<lp>\() | (?P<rp>\))
| (?P<id>[A-Za-z_][\w.]*(\(\s*[\w.()]*\s*\))?)
""", re.X)

def tokenize(s):
    toks, i = [], 0
    s = s.strip()
    while i < len(s):
        if s[i].isspace(): i += 1; continue
        m = TOKRE.match(s, i)
        if not m: raise ValueError(f"tok fail at {s[i:i+30]!r}")
        toks.append((m.lastgroup, m.group())); i = m.end()
    return toks

class P:
    def __init__(self, toks): self.t = toks; self.i = 0
    def peek(self): return self.t[self.i] if self.i < len(self.t) else (None,None)
    def eat(self): tok = self.t[self.i]; self.i += 1; return tok
    def parse(self):
        n = self.orx(); assert self.i == len(self.t), f"trailing {self.t[self.i:]}"
        return n
    def orx(self):
        n = self.andx()
        while self.peek()[1] == "||": self.eat(); n = ("or", n, self.andx())
        return n
    def andx(self):
        n = self.notx()
        while self.peek()[1] == "&&": self.eat(); n = ("and", n, self.notx())
        return n
    def notx(self):
        if self.peek()[1] == "!": self.eat(); return ("not", self.notx())
        return self.cmpx()
    def cmpx(self):
        n = self.prim()
        if self.peek()[1] in ("<=", ">=", "<", ">", "==", "!="):
            op = self.eat()[1]; return ("cmp", op, n, self.prim())
        return n
    def prim(self):
        k, v = self.peek()
        if k == "lp":
            self.eat(); n = self.orx(); assert self.eat()[0] == "rp"; return n
        if k == "num": self.eat(); return ("num", float(v))
        if k == "id":
            self.eat()
            if v == "event":
                k2, v2 = self.eat(); assert v2 == "instanceof", (k2, v2)
                k3, v3 = self.eat(); assert v3.startswith("InputEvent."), v3
                return ("ev", v3.split(".")[1])
            return ("var", v)
        raise ValueError(f"prim fail at {self.peek()}")

def parse_expr(s): return P(tokenize(s)).parse()

CONSTS = {"LreConstants.minSafeDist":"MS","LreConstants.staticObsHorizDist":"HH",
          "LreConstants.staticObsVertDist":"VV","LreConstants.staticObsDfltVertDist":"DV"}

def canon_var(name):
    if name in CONSTS: return CONSTS[name]
    n = name.replace("this.","")
    if n.startswith("sensor.hdist"): return "hdist"
    if n.startswith("sensor.vdist"): return "vdist"
    if n.startswith("sensor.odist"):
        arg = n[len("sensor.odist("):]
        return "odc" if ("dyn" in arg or "Dyn" in arg) else "ocs"
    if n.startswith("sensor.depth"): return "depth"
    return n

def match_block(s, i):
    assert s[i] == "{"
    d = 0
    for j in range(i, len(s)):
        if s[j] == "{": d += 1
        elif s[j] == "}":
            d -= 1
            if d == 0: return s[i+1:j], j+1
    raise ValueError("unbalanced")

def branches(block):
    out, k, n = [], 0, len(block)
    while k < n:
        m = re.compile(r"\bif\s*\(").search(block, k)
        if not m: break
        p = m.end()-1; d = 0
        for j in range(p, n):
            if block[j] == "(": d += 1
            elif block[j] == ")":
                d -= 1
                if d == 0: break
        cond = block[p+1:j]
        b = block.index("{", j)
        inner, after = match_block(block, b)
        out.append((" ".join(cond.split()), inner))
        k = after
    return out

OUTMAP = {"rv.value()":"v","rh.value()":"h","1.0":"1","0.0":"0"}
def body_info(body):
    tgt = re.findall(r"currentMode\s*=\s*LreMode\.(\w+)\s*;", body)
    outs = []
    for kind, arg in re.findall(r"OutputEvent\.(AdvVel|AdvHdng)\((.*?)\)\s*\)\s*;", body):
        outs.append(("advVel" if kind=="AdvVel" else "advHdng") + ":" + OUTMAP.get(arg.strip(), arg.strip()))
    casts = re.findall(r"\(InputEvent\.(\w+)\)\s*event", body)
    return (tgt[-1] if tgt else None, tuple(outs), tuple(casts))

def parse_controller(path):
    src = open(path).read()
    body = src[src.index("public void step(InputEvent event)"):]
    body = body[body.index("{"):]
    body, _ = match_block(body, 0)
    defs = {}
    for name in ("camActive","hcmActive","momReturn"):
        m = re.search(r"this\.%s\s*=\s*(.*?);" % name, body, re.S)
        defs[name] = parse_expr(m.group(1))
    named = {}
    for m in re.finditer(r"boolean\s+(\w+)\s*=\s*(.*?);", body, re.S):
        named[m.group(1)] = parse_expr(m.group(2))
    chain_start = body.index("if (currentMode")
    disp = []
    for cond, blk in branches(body[chain_start:]):
        inner = [(parse_expr(c),) + body_info(b) for c, b in branches(blk)]
        disp.append((parse_expr(cond), inner))
    return dict(defs=defs, named=named, dispatch=disp)

BASE_CTRL = parse_controller(BASE + "/controller/LreController.java")
assert len(BASE_CTRL["dispatch"]) == 4

# ---- abstract atom-level evaluation and the 57,344-cell membership self-check ----
ATOMKEY = {
 ("vel","<=",1.0):"vel_le_1", ("odc",">",1.0):"odc_gt_1", ("ocs",">",1.0):"ocs_gt_1",
 ("cda","<","MS"):"cda_lt_ms", ("tcpa",">=",0.0):"tcpa_ge_0",
 ("hvel",">=",1.0):"hvel_ge_1", ("hdist","<=","HH"):"hdist_le_h",
 ("vdist","<=","DV"):"vdist_le_dv", ("vdist","<=","VV"):"vdist_le_v",
 ("cda",">=","MS"):"!cda_lt_ms",
 ("hdist",">","HH"):"!hdist_le_h", ("vdist",">","VV"):"!vdist_le_v",
 ("vdist",">","DV"):"!vdist_le_dv", ("odc","<=",1.0):"!odc_gt_1",
 ("ocs","<=",1.0):"!ocs_gt_1",
 ("tcpa","<",0.0):"!tcpa_ge_0", ("hvel","<",1.0):"!hvel_ge_1",
 ("vel",">",1.0):"!vel_le_1", ("vvel","<",1.0):"!vvel_ge_1",
 ("vvel",">=",1.0):"vvel_ge_1",
}
def cmp_key(node):
    _, op, a, b = node
    av = canon_var(a[1]) if a[0]=="var" else a[1]
    bv = canon_var(b[1]) if b[0]=="var" else b[1]
    return (av, op, bv)

EV2J = {"reqVel":"ReqVel","reqHdng":"ReqHdng","reqOCM":"ReqOCM","reqMOM":"ReqMOM",
        "reqHCM":"ReqHCM","endTask":"EndTask","none":None}

def eval_atoms(node, atoms, ev, ctx):
    ty = node[0]
    if ty == "or":  return eval_atoms(node[1],atoms,ev,ctx) or eval_atoms(node[2],atoms,ev,ctx)
    if ty == "and": return eval_atoms(node[1],atoms,ev,ctx) and eval_atoms(node[2],atoms,ev,ctx)
    if ty == "not": return not eval_atoms(node[1],atoms,ev,ctx)
    if ty == "cmp":
        k = cmp_key(node)
        a = ATOMKEY[k]
        return (not atoms[a[1:]]) if a.startswith("!") else atoms[a]
    if ty == "ev": return EV2J.get(ev) == node[1]
    if ty == "var":
        v = node[1].replace("this.","")
        if v == "inOpez": return atoms["inOpez"]
        if v in ctx["defs"]:  return eval_atoms(ctx["defs"][v], atoms, ev, ctx)
        if v in ctx["named"]: return eval_atoms(ctx["named"][v], atoms, ev, ctx)
        raise KeyError(v)
    raise ValueError(node)

def java_step(ctrl, mode, ev, atoms):
    for cond, inner in ctrl["dispatch"]:
        _, op, a, b = cond
        mval = b[1].split(".")[1]
        sel = (mode == mval) if op == "==" else (mode != mval)
        if not sel: continue
        for g, tgt, outs, casts in inner:
            if eval_atoms(g, atoms, ev, ctrl):
                return (tgt if tgt else mode), outs
        return mode, ()
    return mode, ()

n_member = n_cells = 0
for bits in product([False,True], repeat=len(ATOMS)):
    a = dict(zip(ATOMS, bits))
    for m in MODES:
        for e in EVENTS:
            S, rids = R_set(m, e, a)
            n_cells += 1; n_member += (java_step(BASE_CTRL, m, e, a) in S)
print(f"abstract self-check: baseline Java outcome in R on {n_member}/{n_cells} cells")
assert n_cells == 57344 and n_member == 57344, (n_cells, n_member)

# ============ concrete grid ============
MAXD = np.float64(1.7976931348623157e308)

def make_obs(spec_, depth, nsv, ewv):
    oid, ns, ew, z, vn, ve = spec_
    if z == "d": z = depth
    elif z == "d+1": z = depth + 1.0
    if isinstance(vn, tuple): vn = nsv + vn[1]
    if isinstance(ve, tuple): ve = ewv + ve[1]
    return (oid, float(ns), float(ew), float(z), float(vn), float(ve))

CONFIGS = [
 ("empty", []),
 ("s_close", [(0, 0.3, 0.0, 0.5, 0, 0)]),
 ("s_mid",   [(0, 1.0, 0.0, 1.0, 0, 0)]),
 ("s_far",   [(0, 2.2, 0.0, 2.2, 0, 0)]),
 ("s_hclose_vfar", [(0, 0.3, 0.0, 2.5, 0, 0)]),
 ("s_hfar_vclose", [(0, 2.5, 0.0, "d", 0, 0)]),
 ("d_approach", [(0, 2.0, 0.0, 0.5, -1.0, 0.0)]),
 ("d_recede",   [(0, 2.0, 0.0, 0.5,  1.0, 0.0)]),
 ("d_matchvel", [(0, 2.0, 0.0, 0.5, ("rel",0), ("rel",0))]),
 ("d_close",    [(0, 0.5, 0.0, 0.5, -0.5, 0.0)]),
 ("d_diag",     [(0, 3.0, 3.0, 1.0, -1.0, -1.0)]),
 ("sd_pair",    [(0, 1.0, 0.0, 0.5, 0, 0), (1, 2.0, 0.0, 0.5, -1.0, 0.0)]),
 ("ds_pair",    [(0, 2.0, 0.0, 0.5, -1.0, 0.0), (1, 1.0, 0.0, 0.5, 0, 0)]),
 ("ss_two",     [(0, 2.2, 0.0, 2.2, 0, 0), (1, 0.5, 0.0, 0.5, 0, 0)]),
 ("s_bnd_od1",  [(0, 1.0, 0.0, "d", 0, 0)]),
 ("s_bnd_vd1",  [(0, 0.0, 0.0, "d+1", 0, 0)]),
 ("d_side_t0",  [(0, 0.0, 1.0, 0.5, ("rel",-1.0), ("rel",0))]),
 ("d_behind",   [(0,-2.0, 0.5, 0.5, ("rel",-1.0), ("rel",0))]),
 ("d_headon",   [(0, 3.0, 0.0, 0.5, ("rel",-1.0), ("rel",0))]),
 ("d_cda1",     [(0, 3.0, 1.0, 0.5, ("rel",-1.0), ("rel",0))]),
 ("d_cda05",    [(0, 3.0, 0.5, 0.5, ("rel",-1.0), ("rel",0))]),
 ("d_zerorel_close", [(0, 0.5, 0.0, 0.5, ("rel",0), ("rel",0))]),
 ("d_zerorel_far",   [(0, 2.0, 0.0, 0.5, ("rel",0), ("rel",0))]),
 ("d0_only",    [(0, 1.5, 0.0, 0.5, -1.0, 0.0)]),
 ("s0_only",    [(0, 1.5, 0.0, 0.5, 0, 0)]),
 ("d_approach_far", [(0, 5.0, 1.5, 0.5, -1.0, 0.0)]),
 ("d_cda2", [(0, 3.0, 2.0, 0.5, ("rel",-1.0), ("rel",0))]),
 ("s_h2",   [(0, 2.0, 0.0, "d", 0, 0)]),
 ("s_z25",  [(0, 0.0, 0.0, 2.5, 0, 0)]),
 # A05 tie-rule configurations
 ("ss_tie", [(0, 1.0, 0.0, "d", 0, 0), (1, 0.0, 1.0, "d", 0, 0)]),
 ("dd_tie", [(0, 2.0, 0.0, 0.5, -1.0, 0.0), (1, 0.0, 2.0, 0.5, 0.0, -1.0)]),
]
DEPTHS = [-0.5, 0.0, 0.5, 1.0, 1.5, 2.5]
VELS   = [(0,0),(0.5,0),(1,0),(1.5,0),(2.5,0),(1,1),(-1.5,0),(2.0,0)]
ROCS   = [-1.5, 0.0, 1.0, 1.5, 2.5, 2.0]

SCEN = []
for d in DEPTHS:
    for nsv, ewv in VELS:
        for roc in ROCS:
            for cname, obs in CONFIGS:
                SCEN.append((d, nsv, ewv, roc, cname,
                             tuple(make_obs(o, d, nsv, ewv) for o in obs)))
NS = len(SCEN)
assert NS == 8928, NS
print("concrete grid:", NS, "scenarios x 28 mode/event pairs =", NS*28, "cells")

def derive_java(s, F=frozenset(), consts=(1.0,1.0,1.0,1.0)):
    depth, nsv, ewv, roc, cname, obs = s
    MS, HH, VV, DV = consts
    reg = {o[0]: o[1:] for o in sorted(obs)}
    def is_static(t):
        vn, ve = t[3], t[4]
        if "cs003" in F: return (vn != 0.0) and (ve == 0.0)
        return vn == 0.0 and ve == 0.0
    def get(idx):
        if "cs004" in F: return None
        return reg.get(idx)
    def hdist(i):
        if i == -1: return MAXD
        o = get(i)
        if o is None: return MAXD
        return np.sqrt(np.float64(o[0])**2 + np.float64(o[1])**2)
    def vdist(i):
        if "cs005" in F: return MAXD
        if i == -1: return MAXD
        o = get(i)
        if o is None: return MAXD
        return abs(np.float64(depth) - o[2])
    def odist(i):
        if "cs006" in F: return MAXD
        if i == -1: return MAXD
        o = get(i)
        if o is None: return MAXD
        h = hdist(i); v = vdist(i)
        return np.sqrt(h*h + v*v)
    def nsRel(i):
        if "cs007" in F: return MAXD
        if i == -1: return MAXD
        o = get(i)
        return MAXD if o is None else np.float64(o[0])
    def ewRel(i):
        if "cs008" in F: return MAXD
        if i == -1: return MAXD
        o = get(i)
        return MAXD if o is None else np.float64(o[1])
    def obsNsVel(i):
        if i == -1: return np.float64(0.0)
        o = get(i)
        return np.float64(0.0) if o is None else np.float64(o[3])
    def obsEwVel(i):
        if "cs009" in F: return np.float64(0.0)
        if i == -1: return np.float64(0.0)
        o = get(i)
        return np.float64(0.0) if o is None else np.float64(o[4])
    static_idxs = [i for i in sorted(reg) if is_static(reg[i])]
    dyn_idxs    = [i for i in sorted(reg) if not is_static(reg[i])]
    if "dn007" in F: dyn_idxs = static_idxs[:]
    def closest_of(idxs, best0):
        best = 0 if best0 else -1
        bestD = MAXD
        for i in idxs:
            dd = odist(i)
            if dd < bestD: bestD = dd; best = i
        return best
    cstc = closest_of(static_idxs, "si006" in F)
    cdyn = closest_of(dyn_idxs,    "si007" in F)
    hvel = np.sqrt(np.float64(nsv)**2 + np.float64(ewv)**2)
    vvel = np.float64(roc)
    vel  = np.sqrt(hvel*hvel + vvel*vvel)
    if "rf009" in F:   inOpez = (odist(cstc) <= MS) or (depth > 0.0)
    elif "ob009" in F: inOpez = (odist(cstc) <= MS) or (depth <= 1.0)
    else:              inOpez = (odist(cstc) <= MS) or (depth <= 0.0)
    rn, re_ = nsRel(cdyn), ewRel(cdyn)
    un, ue  = obsNsVel(cdyn) - nsv, obsEwVel(cdyn) - ewv
    tcpa = -(rn*un + re_*ue) / (un*un + ue*ue)
    cda  = np.sqrt((rn + un*tcpa)**2 + (re_ + ue*tcpa)**2)
    return dict(vel=vel, hvel=hvel, vvel=vvel, cda=cda, tcpa=tcpa,
                inOpez=bool(inOpez), hdist=hdist(cstc), vdist=vdist(cstc),
                odc=odist(cdyn), ocs=odist(cstc),
                MS=MS, HH=HH, VV=VV, DV=DV)

def derive_spec3(s, consts=(1.0,1.0,1.0,1.0)):
    depth, nsv, ewv, roc, cname, obs = s
    MS, HH, VV, DV = consts
    Fs = 1.0 + max(0.0, 1.0, MS, HH, VV, DV)
    reg = {o[0]: o[1:] for o in obs}
    stat = {i for i,o in reg.items() if o[3]==0.0 and o[4]==0.0}
    dyn  = set(reg) - stat
    def hd(i): return np.sqrt(reg[i][0]**2 + reg[i][1]**2) if i!=-1 else Fs
    def vd(i): return abs(depth - reg[i][2]) if i!=-1 else Fs
    def od(i): return np.sqrt(hd(i)**2 + vd(i)**2) if i!=-1 else Fs
    def sel(cls):
        if not cls: return -1
        return min(cls, key=lambda i: (od(i), i))
    cstc, cdyn = sel(stat), sel(dyn)
    hvel = np.sqrt(nsv**2 + ewv**2); vvel = roc; vel = np.sqrt(hvel**2 + vvel**2)
    inOpez = (od(cstc) <= MS) or (depth <= 0.0)
    domedge = False
    if cdyn == -1:
        cda, tcpa = Fs, 0.0
    else:
        rn, re_ = reg[cdyn][0], reg[cdyn][1]
        un, ue  = reg[cdyn][3]-nsv, reg[cdyn][4]-ewv
        uu = un*un + ue*ue
        if uu > 0:
            tcpa = -(rn*un + re_*ue)/uu
            cda  = np.sqrt((rn+un*tcpa)**2 + (re_+ue*tcpa)**2)
        else:
            tcpa = 0.0; cda = np.sqrt(rn*rn + re_*re_)
            domedge = True
    atoms = dict(inOpez=bool(inOpez), vel_le_1=bool(vel<=1.0),
        odc_gt_1=bool(od(cdyn)>1.0), ocs_gt_1=bool(od(cstc)>1.0),
        cda_lt_ms=bool(cda<MS), tcpa_ge_0=bool(tcpa>=0.0),
        hvel_ge_1=bool(hvel>=1.0), hdist_le_h=bool(hd(cstc)<=HH),
        vdist_le_dv=bool(vd(cstc)<=DV), vvel_ge_1=bool(vvel>=1.0),
        vdist_le_v=bool(vd(cstc)<=VV))
    return atoms, domedge

def jconv_atoms(env, MS=1.0, HH=1.0, VV=1.0, DV=1.0):
    return dict(inOpez=env["inOpez"], vel_le_1=bool(env["vel"]<=1.0),
        odc_gt_1=bool(env["odc"]>1.0), ocs_gt_1=bool(env["ocs"]>1.0),
        cda_lt_ms=bool(env["cda"]<MS), tcpa_ge_0=bool(env["tcpa"]>=0.0),
        hvel_ge_1=bool(env["hvel"]>=1.0), hdist_le_h=bool(env["hdist"]<=HH),
        vdist_le_dv=bool(env["vdist"]<=DV), vvel_ge_1=bool(env["vvel"]>=1.0),
        vdist_le_v=bool(env["vdist"]<=VV))

# ============ vectorised machinery ============
def envs_to_arrays(envs):
    keys = ["vel","hvel","vvel","cda","tcpa","hdist","vdist","odc","ocs"]
    Aa = {k: np.array([e[k] for e in envs], dtype=np.float64) for k in keys}
    Aa["inOpez"] = np.array([e["inOpez"] for e in envs])
    for c in ("MS","HH","VV","DV"): Aa[c] = envs[0][c]
    Aa["_T"] = np.ones(len(envs), bool); Aa["_F"] = np.zeros(len(envs), bool)
    return Aa

def eval_c(node, Aa, ev, ctrl):
    ty = node[0]
    if ty == "or":  return eval_c(node[1],Aa,ev,ctrl) | eval_c(node[2],Aa,ev,ctrl)
    if ty == "and": return eval_c(node[1],Aa,ev,ctrl) & eval_c(node[2],Aa,ev,ctrl)
    if ty == "not": return ~eval_c(node[1],Aa,ev,ctrl)
    if ty == "cmp":
        op = node[1]; a = eval_c(node[2],Aa,ev,ctrl); b = eval_c(node[3],Aa,ev,ctrl)
        return a<=b if op=="<=" else a>=b if op==">=" else a<b if op=="<" else \
               a>b if op==">" else a==b if op=="==" else a!=b
    if ty == "num": return node[1]
    if ty == "ev":  return Aa["_T"] if EV2J.get(ev) == node[1] else Aa["_F"]
    if ty == "var":
        v = node[1].replace("this.","")
        if v in ctrl["defs"]:  return eval_c(ctrl["defs"][v], Aa, ev, ctrl)
        if v in ctrl["named"]: return eval_c(ctrl["named"][v], Aa, ev, ctrl)
        return Aa[canon_var(node[1])]
    raise ValueError(node)

RES = {}
CODE2RES = {}
def rcode(dest, outs):
    key = (dest, tuple(outs))
    if key not in RES:
        RES[key] = len(RES); CODE2RES[RES[key]] = key
    return RES[key]

EXC = rcode("EXC", ("EXC",))

def java_codes_x(ctrl, Aa, n):
    out = {}
    for m in MODES:
        blk = None
        for cond, inner in ctrl["dispatch"]:
            _, op, _, b = cond
            mv = b[1].split(".")[1]
            if (m == mv) if op == "==" else (m != mv): blk = inner; break
        for e in EVENTS:
            stay = rcode(m, ())
            if blk is None:
                out[(m,e)] = np.full(n, stay); continue
            masks, codes = [], []
            for g, tgt, outs, casts in blk:
                gm = eval_c(g, Aa, e, ctrl)
                if isinstance(gm, (bool, np.bool_)): gm = np.full(n, bool(gm))
                bad_cast = any(c != EV2J.get(e) for c in casts)
                masks.append(gm)
                codes.append(EXC if bad_cast else rcode(tgt if tgt else m, outs))
            out[(m,e)] = np.select(masks, codes, default=stay)
    return out

def eligible_sets(atomdicts):
    n = len(atomdicts)
    AT = {k: np.array([d[k] for d in atomdicts]) for k in ATOMS}
    G = dict(P=AT["inOpez"],
        EnterMOM=AT["vel_le_1"] & ~AT["inOpez"] & AT["odc_gt_1"] & AT["ocs_gt_1"],
        Collision=AT["cda_lt_ms"] & AT["tcpa_ge_0"],
        B_h=AT["hvel_ge_1"] & AT["hdist_le_h"], B_0=AT["vdist_le_dv"],
        B_v=AT["vvel_ge_1"] & AT["vdist_le_v"],
        ClearStatic=~AT["hdist_le_h"] & ~AT["vdist_le_v"], SafeCPA=~AT["cda_lt_ms"])
    TRUE = np.ones(n, bool)
    ES = {}
    for m in MODES:
        for e in EVENTS:
            entries = []
            any_elig = np.zeros(n, bool)
            for rid, (src, trig, gn, dest, eff) in SPEC_ROWS.items():
                if src != m: continue
                if trig is not None and trig != e: continue
                msk = G[gn] if gn else TRUE
                entries.append((msk, rcode(dest, eff), rid))
                any_elig |= msk
            entries.append((~any_elig, rcode(m, ()), "hold"))
            ES[(m,e)] = entries
    return ES

def member_mask(ES, me, codes):
    ok = np.zeros(len(codes), bool)
    for msk, c, rid in ES[me]:
        ok |= msk & (codes == c)
    return ok

def member_one(ES, me, i, code):
    return any(bool(msk[i]) and c == code for msk, c, rid in ES[me])

BASE_ENVS  = [derive_java(s) for s in SCEN]
sp = [derive_spec3(s) for s in SCEN]
SPEC_ATOMS = [x[0] for x in sp]
DOMEDGE    = np.array([x[1] for x in sp])
SPEC_JCONV = [jconv_atoms(e) for e in BASE_ENVS]
NANCELL    = np.array([bool(np.isnan(e["cda"]) or np.isnan(e["tcpa"])) for e in BASE_ENVS])
BASE_A     = envs_to_arrays(BASE_ENVS)
BASE_CODES = java_codes_x(BASE_CTRL, BASE_A, NS)
ES_SPEC    = eligible_sets(SPEC_ATOMS)
ES_JC      = eligible_sets(SPEC_JCONV)
BASE_MEMBER = {me: member_mask(ES_SPEC, me, BASE_CODES[me]) for me in BASE_CODES}
nonmem = sum(int((~BASE_MEMBER[me]).sum()) for me in BASE_CODES)
print(f"baseline on the concrete grid: {NS*28 - nonmem}/{NS*28} member cells, {nonmem} non-member (all NaN/CPA-edge cells: "
      f"{all(NANCELL[i] or DOMEDGE[i] for me in BASE_CODES for i in np.where(~BASE_MEMBER[me])[0])})")

# ============ data-plane mutant models ============
DATA_FLAG = {"lre_relop_flip_009":"rf009","lre_cmp_swap_003":"cs003","lre_cmp_swap_004":"cs004",
 "lre_cmp_swap_005":"cs005","lre_cmp_swap_006":"cs006","lre_cmp_swap_007":"cs007",
 "lre_cmp_swap_008":"cs008","lre_cmp_swap_009":"cs009","lre_sentinel_init_006":"si006",
 "lre_sentinel_init_007":"si007","lre_off_by_one_009":"ob009","lre_drop_negation_007":"dn007"}
CONST_MUT = {"lre_off_by_one_000":(2.0,1.0,1.0,1.0),"lre_off_by_one_001":(1.0,2.0,1.0,1.0),
             "lre_off_by_one_002":(1.0,1.0,2.0,1.0),"lre_off_by_one_003":(1.0,1.0,1.0,2.0)}
NOOP_DATA = {"lre_sentinel_init_000","lre_sentinel_init_001","lre_sentinel_init_002",
             "lre_sentinel_init_003","lre_sentinel_init_004","lre_sentinel_init_005"}
ACTUATOR = {"lre_branch_swap_000","lre_trigger_swap_000","lre_trigger_swap_001"}

# ============ score_mutant ============
def score_mutant(sid):
    rel = mm[sid]["file"]; cls = mm[sid]["defect_class"]
    if sid in ACTUATOR:
        return dict(mutant_id=sid, defect_class=cls, killed=False,
            attribution="not-scoreable-by-R",
            mechanism="below-relation: actuator-internal storage; spec3's mode relation does not depend on lastVel/lastHdng records (A09), and R's outputs (emitted advice events) are unchanged",
            witness="")
    if sid in NOOP_DATA:
        return dict(mutant_id=sid, defect_class=cls, killed=False,
            attribution="not-scoreable-by-R",
            mechanism="no-op on R: constructor/field initial overwritten by compute()/step() refresh before any guard read (T01 fixes only the initial mode; initial record values unspecified per A09)",
            witness="")
    if rel.startswith("controller/") and sid not in CONST_MUT:
        ctrl = parse_controller(f"{MUT}/{sid}/java/controller/LreController.java")
        MC = java_codes_x(ctrl, BASE_A, NS)
        ESs, ESj = ES_SPEC, ES_JC
        MNAN = NANCELL; MDOM = DOMEDGE
    elif sid in CONST_MUT:
        consts = CONST_MUT[sid]
        envs = [derive_java(s, frozenset(), consts) for s in SCEN]
        Aa = envs_to_arrays(envs)
        MC = java_codes_x(BASE_CTRL, Aa, NS)
        spx = [derive_spec3(s, consts) for s in SCEN]
        atoms = [x[0] for x in spx]
        MDOM = np.array([x[1] for x in spx])
        jc = [jconv_atoms(e, *consts) for e in envs]
        ESs, ESj = eligible_sets(atoms), eligible_sets(jc)
        MNAN = np.array([bool(np.isnan(e["cda"]) or np.isnan(e["tcpa"])) for e in envs])
    elif sid in DATA_FLAG:
        F = frozenset([DATA_FLAG[sid]])
        envs = [derive_java(s, F) for s in SCEN]
        Aa = envs_to_arrays(envs)
        MC = java_codes_x(BASE_CTRL, Aa, NS)
        ESs, ESj = ES_SPEC, ES_JC
        MNAN = np.array([bool(np.isnan(e["cda"]) or np.isnan(e["tcpa"])) for e in envs])
        MDOM = DOMEDGE
    else:
        return dict(mutant_id=sid, defect_class=cls, killed=False, attribution="not-scoreable",
            mechanism=f"edit in {rel} has no data-plane model in the restored machinery (not a controller edit; not in DATA_FLAG/CONST_MUT/NOOP_DATA/ACTUATOR)",
            witness="")
    kill_cells = []
    for me in MC:
        mmem = member_mask(ESs, me, MC[me])
        mask = (~mmem) & BASE_MEMBER[me]
        for i in np.where(mask)[0]: kill_cells.append((me, int(i)))
    if not kill_cells:
        return dict(mutant_id=sid, defect_class=cls, killed=False, attribution="miss",
            mechanism=f"0 membership-violation cells (where baseline is a member) on the {NS}-scenario grid",
            witness="")
    unamb=[]; ledger=0; ledger_kinds=Counter()
    for me,i in kill_cells:
        code = int(MC[me][i])
        if MNAN[i] or NANCELL[i] or MDOM[i] or DOMEDGE[i]:
            ledger+=1; ledger_kinds["A07/A08/A14_CPA_edge"]+=1
        elif member_one(ESj, me, i, code):
            ledger+=1; ledger_kinds["A07/A08_convention"]+=1
        else: unamb.append((me,i))
    if unamb:
        me,i = unamb[0]; s = SCEN[i]
        witness = (f"{me[0]}/{me[1]}; depth={s[0]}, v=({s[1]},{s[2]}), roc={s[3]}, obstacles={s[4]}: "
                   f"mutant->{CODE2RES[int(MC[me][i])]} NOT IN R; baseline->{CODE2RES[int(BASE_CODES[me][i])]} IN R")
        return dict(mutant_id=sid, defect_class=cls, killed=True, attribution="unambiguous",
            mechanism=f"{len(kill_cells)} membership-violation cells ({len(unamb)} unambiguous, {ledger} CPA-edge)",
            witness=witness)
    me,i = kill_cells[0]; s = SCEN[i]
    witness = (f"{me[0]}/{me[1]}; depth={s[0]}, v=({s[1]},{s[2]}), roc={s[3]}, obstacles={s[4]}: "
               f"mutant->{CODE2RES[int(MC[me][i])]} outside R only under the spec's disclosed CPA completion")
    return dict(mutant_id=sid, defect_class=cls, killed=True,
        attribution="ledger-attributed:" + "+".join(sorted(ledger_kinds)),
        mechanism=f"{len(kill_cells)} membership-violation cells, all in ledger scope {dict(ledger_kinds)}",
        witness=witness)

# ============ the pool: score every survivor fresh ============
FIELDS = ["mutant_id", "defect_class", "file", "line", "killed", "attribution", "mechanism", "witness"]
kt = list(csv.DictReader(open(args.pool)))
excl = {x for x in args.exclude.split(",") if x}
ids = [r["mutant_id"] for r in kt if r["study"] == "lre" and r["disposition"] == "survived"]
dropped = [i for i in ids if i in excl]; ids = [i for i in ids if i not in excl]
print(f"pool: {len(ids)} LRE survivors ({len(dropped)} excluded: {', '.join(dropped) or 'none'})")
out_rows = []
for sid in ids:
    r = score_mutant(sid)
    out_rows.append(dict(mutant_id=sid, defect_class=mm[sid]["defect_class"], file=mm[sid]["file"], line=mm[sid]["line"],
                         killed=r["killed"], attribution=r["attribution"], mechanism=r["mechanism"], witness=r["witness"]))
nk = sum(str(r["killed"]) == "True" for r in out_rows)
if args.check:
    ship = {r["mutant_id"]: r for r in csv.DictReader(open(SHIPPED))}
    common = [r for r in out_rows if r["mutant_id"] in ship]
    dk = [r["mutant_id"] for r in common if str(r["killed"]) != ship[r["mutant_id"]]["killed"]]
    dm = [r["mutant_id"] for r in common if r["mechanism"] != ship[r["mutant_id"]]["mechanism"]]
    only = sorted(set(ship) ^ {r["mutant_id"] for r in out_rows})
    print(f"CHECK vs shipped blindspec3_survivor_scoring.csv: {len(common)} common rows; killed differs on {len(dk)}; "
          f"mechanism differs on {len(dm)}; rows only on one side: {len(only)}")
    for m_ in dk: print("  DIFF", m_, ship[m_]["killed"], "->", [r["killed"] for r in out_rows if r["mutant_id"] == m_][0])
    sys.exit(0 if (not dk and not only) else 1)
os.makedirs(os.path.dirname(os.path.abspath(args.out)), exist_ok=True)
with open(args.out, "w", newline="") as f:
    w = csv.DictWriter(f, fieldnames=FIELDS); w.writeheader(); [w.writerow(r) for r in out_rows]
print(f"wrote {args.out}: {len(out_rows)} rows; killed={nk}")
