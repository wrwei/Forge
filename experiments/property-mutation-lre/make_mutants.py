#!/usr/bin/env python3
"""Build the LRE obligation mutants of the pre-registration addendum from baseline/."""
import os, re, shutil
HERE = os.path.dirname(os.path.abspath(__file__))
B = os.path.join(HERE, "baseline"); OUT = os.path.join(HERE, "mutants")
dfy0 = open(os.path.join(B, "LreController.dfy")).read()
thy0 = open(os.path.join(B, "isabelle", "LreController_Beh.thy")).read()
root = open(os.path.join(B, "isabelle", "ROOT")).read()

# ---- Dafny: the eight guarded ensures, keyed by (method, target) ----
ENS = {}
cur = None
for line in dfy0.splitlines():
    m = re.match(r"\s*method (transitionFrom\w+)\(", line)
    if m: cur = m.group(1)
    m = re.match(r"\s*ensures old\((.*)\) ==> mode == (\w+)\s*$", line)
    if m and cur: ENS[(cur, m.group(2))] = (line.strip(), m.group(1))
assert len(ENS) == 8, sorted(ENS)
def conj(ant):
    out, d, cur = [], 0, ""
    i = 0
    while i < len(ant):
        if ant.startswith(" && ", i) and d == 0: out.append(cur); cur = ""; i += 4; continue
        c = ant[i]; d += (c == "("); d -= (c == ")"); cur += c; i += 1
    out.append(cur); return out
def ens_line(method, target): return ENS[(method, target)][0]
def span(src, method):
    i = src.index(f"    method {method}("); j = src.find("\n    method ", i + 10); return i, (j if j > 0 else len(src))
def one_in(src, method, old, new, mid):
    i, j = span(src, method); seg = src[i:j]; n = seg.count(old); assert n == 1, (mid, method, n, old[:70])
    return src[:i] + seg.replace(old, new) + src[j:]
def with_ant(method, target, newant): return f"ensures old({newant}) ==> mode == {target}"
def drop(method, target, atom):
    cs = conj(ENS[(method, target)][1]); assert atom in cs, (method, target, atom, cs)
    cs.remove(atom); return with_ant(method, target, " && ".join(cs))
def subst(method, target, old, new):
    line, ant = ENS[(method, target)]; assert ant.count(old) == 1, (method, target, old); return with_ant(method, target, ant.replace(old, new))
MODES = ["OCM", "MOM", "HCM", "CAM"]
D = {}
for k, ((meth, tgt), (line, ant)) in enumerate(sorted(ENS.items())):
    nxt = MODES[(MODES.index(tgt) + 1) % 4]
    D[f"PD1{chr(97+k)}"] = (meth, line, with_ant(meth, tgt, ant).replace(f"mode == {tgt}", f"mode == {nxt}"))
CAM = "!((cda < 1.0 && tcpa >= 0.0))"
HCMACT = "((hvel >= 1.0 && hdist(cstc) <= 1.0) || vdist(cstc) <= 1.0 || (vvel >= 1.0 && vdist(cstc) <= 1.0))"
F = [("transitionFromOCM","MOM","vel <= 1.0"), ("transitionFromMOM","CAM","cda < 1.0"), ("transitionFromMOM","HCM",HCMACT),
     ("transitionFromMOM","OCM","inOpez"), ("transitionFromHCM","CAM","tcpa >= 0.0"), ("transitionFromHCM","OCM","inOpez"),
     ("transitionFromHCM","MOM","(hdist(cstc) > 1.0 && vdist(cstc) > 1.0)"), ("transitionFromCAM","OCM","cda >= 1.0")]
P = [("transitionFromOCM","MOM","!(event == ReqHdng)"), ("transitionFromMOM","HCM",CAM), ("transitionFromMOM","OCM",CAM),
     ("transitionFromHCM","OCM",CAM), ("transitionFromHCM","MOM","!(inOpez)"), ("transitionFromCAM","OCM","!(event == ReqOCM)"),
     ("transitionFromOCM","MOM","!(event == ReqVel)")]
for k, (m, t, a) in enumerate(F):
    if t == "CAM":   # the CAM antecedent is one parenthesised conjunction: drop inside it
        other = "tcpa >= 0.0" if a == "cda < 1.0" else "cda < 1.0"
        D[f"PD2f{k+1}"] = (m, ens_line(m, t), subst(m, t, "(cda < 1.0 && tcpa >= 0.0)", f"({other})"))
    else:
        D[f"PD2f{k+1}"] = (m, ens_line(m, t), drop(m, t, a))
for k, (m, t, a) in enumerate(P): D[f"PD2p{k+1}"] = (m, ens_line(m, t), drop(m, t, a))
D["PD3a"] = ("transitionFromOCM", ens_line("transitionFromOCM","MOM"), subst("transitionFromOCM","MOM","vel <= 1.0","vel <= 2.0"))
D["PD3b"] = ("transitionFromMOM", ens_line("transitionFromMOM","CAM"), subst("transitionFromMOM","CAM","cda < 1.0","cda < 0.5"))
D["PD3c"] = ("transitionFromMOM", ens_line("transitionFromMOM","CAM"), subst("transitionFromMOM","CAM","cda < 1.0","cda < 2.0"))
D["PD3d"] = ("transitionFromCAM", ens_line("transitionFromCAM","OCM"), subst("transitionFromCAM","OCM","cda >= 1.0","cda >= 0.5"))
D["PD3e"] = ("transitionFromCAM", ens_line("transitionFromCAM","OCM"), subst("transitionFromCAM","OCM","cda >= 1.0","cda >= 2.0"))
D["PD4a"] = ("transitionFromOCM", ens_line("transitionFromOCM","MOM"), ens_line("transitionFromOCM","MOM") + "\n        ensures old(event == ReqVel) ==> mode == OCM")
D["PD4b"] = ("transitionFromCAM", ens_line("transitionFromCAM","OCM"), ens_line("transitionFromCAM","OCM") + "\n        ensures old(event == ReqOCM) ==> mode == OCM")
D["PD5"]  = ("transitionFromOCM", "    method transitionFromOCM(event: InputEvent)\n        requires mode == OCM\n", "    method transitionFromOCM(event: InputEvent)\n")
assert len(D) == 31, len(D)

# ---- Isabelle ----
L = {"OCM": "(st = OCM \\<longrightarrow> listens = {reqVel, reqHdng, reqMOM})",
     "MOM": "(st = MOM \\<longrightarrow> listens = {endTask, reqOCM, reqHCM})",
     "HCM": "(st = HCM \\<longrightarrow> listens = {reqOCM})",
     "CAM": "(st = CAM \\<longrightarrow> listens = {reqOCM})"}
def lset(mode, new): return L[mode].replace(L[mode][L[mode].index("{"):], "{" + new + "})")
I = {
 "PI1a": (L["OCM"], lset("OCM", "reqHdng, reqMOM")), "PI1b": (L["OCM"], lset("OCM", "reqVel, reqMOM")), "PI1c": (L["OCM"], lset("OCM", "reqVel, reqHdng")),
 "PI1d": (L["MOM"], lset("MOM", "reqOCM, reqHCM")), "PI1e": (L["MOM"], lset("MOM", "endTask, reqHCM")), "PI1f": (L["MOM"], lset("MOM", "endTask, reqOCM")),
 "PI1g": (L["HCM"], lset("HCM", "")), "PI1h": (L["CAM"], lset("CAM", "")),
 "PI2a": (L["OCM"], lset("OCM", "reqVel, reqHdng, reqMOM, endTask")), "PI2b": (L["HCM"], lset("HCM", "reqOCM, reqHCM")),
 "PI3": ("(st = HCM \\<longrightarrow> (offered = {} \\<or> offered = {reqOCM}))", "(st = HCM \\<longrightarrow> (offered = {} \\<or> offered = {reqOCM} \\<or> offered = {reqHCM}))"),
 "PI4": ("offered = {reqHdng} \\<or> offered = {reqMOM}))", "offered = {reqHdng}))"),
 "PI5": ("[st\\<leadsto> initial", "[st\\<leadsto> OCM"),
 "PI6a": ('zexpr R1 is "True"', 'zexpr R1 is "st \\<noteq> CAM"'),
 "PI6b": ('zexpr R1 is "True"', 'zexpr R1 is "st = HCM \\<longrightarrow> listens = {reqOCM}"'),
 "PI7": (" Idle_OCM Idle_MOM Idle_HCM Idle_CAM\n", "\n"),
}
def one(src, old, new, mid):
    n = src.count(old); assert n == 1, (mid, n, old[:70]); return src.replace(old, new)
if os.path.isdir(OUT): shutil.rmtree(OUT)
for mid, (meth, o, n) in D.items():
    d = os.path.join(OUT, mid); os.makedirs(d); open(os.path.join(d, "LreController.dfy"), "w").write(one_in(dfy0, meth, o, n, mid))
for mid, (o, n) in I.items():
    d = os.path.join(OUT, mid, "isabelle"); os.makedirs(d)
    open(os.path.join(d, "LreController_Beh.thy"), "w").write(one(thy0, o, n, mid)); open(os.path.join(d, "ROOT"), "w").write(root)
print(len(D), "Dafny and", len(I), "Isabelle mutants")
