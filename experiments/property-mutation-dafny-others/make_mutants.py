#!/usr/bin/env python3
"""Dafny obligation mutants for the gas-analysis, movement and Steam Boiler controllers
(pre-registration addendum of 9 November 2026)."""
import os, re, shutil
HERE = os.path.dirname(os.path.abspath(__file__)); B = os.path.join(HERE, "baseline"); OUT = os.path.join(HERE, "mutants")

def parse(src):
    ens, cur = {}, None
    for line in src.splitlines():
        m = re.match(r"\s*method (transitionFrom\w+)\(", line)
        if m: cur = m.group(1)
        m = re.match(r"\s*ensures old\((.*)\) ==> mode == (\w+)\s*$", line)
        if m and cur: ens[(cur, m.group(2))] = (line.strip(), m.group(1))
        m = re.match(r"\s*ensures mode == (\w+)\s*$", line)
        if m and cur: ens[(cur, m.group(1))] = (line.strip(), None)
    return ens
def conj(ant):
    out, d, cur, i = [], 0, "", 0
    while i < len(ant):
        if ant.startswith(" && ", i) and d == 0: out.append(cur); cur = ""; i += 4; continue
        c = ant[i]; d += (c == "("); d -= (c == ")"); cur += c; i += 1
    out.append(cur); return out
def span(src, method):
    i = src.index(f"    method {method}("); j = src.find("\n    method ", i + 10); return i, (j if j > 0 else len(src))
def one_in(src, method, old, new, mid):
    i, j = span(src, method); seg = src[i:j]; n = seg.count(old); assert n == 1, (mid, method, n, old[:80])
    return src[:i] + seg.replace(old, new) + src[j:]

class Prog:
    def __init__(self, name, modes):
        self.name = name; self.src = open(os.path.join(B, name + ".dfy")).read(); self.ens = parse(self.src); self.modes = modes; self.M = {}
    def line(self, m, t): return self.ens[(m, t)][0]
    def ant(self, m, t): return self.ens[(m, t)][1]
    def with_ant(self, m, t, a): return f"ensures old({a}) ==> mode == {t}" if a is not None else f"ensures mode == {t}"
    def flip(self, mid, m, t, nt): self.M[mid] = (m, self.line(m, t), self.line(m, t).replace(f"mode == {t}", f"mode == {nt}"))
    def drop(self, mid, m, t, atom):
        cs = conj(self.ant(m, t)); assert atom in cs, (mid, atom, cs); cs.remove(atom); self.M[mid] = (m, self.line(m, t), self.with_ant(m, t, " && ".join(cs)))
    def subst(self, mid, m, t, old, new):
        a = self.ant(m, t); assert a.count(old) == 1, (mid, old); self.M[mid] = (m, self.line(m, t), self.with_ant(m, t, a.replace(old, new)))
    def add(self, mid, m, t, extra): self.M[mid] = (m, self.line(m, t), self.line(m, t) + "\n        " + extra)
    def droppre(self, mid, m): self.M[mid] = (m, f"    method {m}(event: InputEvent)\n        requires mode == {m[len('transitionFrom'):]}\n", f"    method {m}(event: InputEvent)\n")
    def flips(self, prefix):
        for k, ((m, t), _) in enumerate(sorted(self.ens.items())):
            if (m, t) in self.skip_flip: continue
            self.flip(f"{prefix}{chr(97 + k)}", m, t, self.modes[(self.modes.index(t) + 1) % len(self.modes)])
    skip_flip = set()
    def write(self):
        for mid, (meth, o, n) in self.M.items():
            d = os.path.join(OUT, mid); os.makedirs(d); open(os.path.join(d, self.name + ".dfy"), "w").write(one_in(self.src, meth, o, n, mid))
        return len(self.M)

if os.path.isdir(OUT): shutil.rmtree(OUT)
# gas analysis
g = Prog("GasAnalysisController", ["Reading", "Analysis", "NoGas", "GasDetected", "Final"])
g.flip("PDG1a", "transitionFromAnalysis", "NoGas", "GasDetected"); g.flip("PDG1b", "transitionFromAnalysis", "GasDetected", "NoGas")
g.flip("PDG1c", "transitionFromGasDetected", "Final", "Reading"); g.flip("PDG1d", "transitionFromNoGas", "Reading", "Analysis")
g.drop("PDG2f1", "transitionFromAnalysis", "GasDetected", "sts == gasD"); g.drop("PDG2p1", "transitionFromAnalysis", "GasDetected", "!(sts == noGas)")
g.subst("PDG3a", "transitionFromGasDetected", "Final", "insVal >= 10.0", "insVal >= 5.0"); g.subst("PDG3b", "transitionFromGasDetected", "Final", "insVal >= 10.0", "insVal >= 20.0")
g.subst("PDG3c", "transitionFromGasDetected", "Reading", "insVal >= 10.0", "insVal >= 20.0"); g.subst("PDG3d", "transitionFromGasDetected", "Reading", "insVal >= 10.0", "insVal >= 5.0")
g.M["PDG4"] = ("transitionFromReading", "        requires mode == Reading\n", "        requires mode == Reading\n        ensures old(event == Gas) ==> mode == Analysis\n")
g.droppre("PDG5", "transitionFromAnalysis")
# movement
v = Prog("MovementController", ["Waiting", "Going", "Found", "Avoiding", "TryingAgain", "AvoidingAgain", "GettingOut", "Final"])
v.flip("PDM1a", "transitionFromAvoidingAgain", "Avoiding", "GettingOut"); v.flip("PDM1b", "transitionFromAvoidingAgain", "GettingOut", "Avoiding"); v.flip("PDM1c", "transitionFromFound", "Final", "Waiting")
DIS = "((nowMs() - clockResetTime < 5) || (d1 - d0 > 1.0))"
v.drop("PDM2f1", "transitionFromAvoidingAgain", "Avoiding", "!(event == Stop)"); v.drop("PDM2f2", "transitionFromAvoidingAgain", "Avoiding", DIS)
v.drop("PDM2f3", "transitionFromAvoidingAgain", "GettingOut", "!(event == Resume)")
v.drop("PDM2p1", "transitionFromAvoidingAgain", "GettingOut", "(d1 - d0 <= 1.0)"); v.drop("PDM2p2", "transitionFromAvoidingAgain", "GettingOut", "(nowMs() - clockResetTime >= 5)")
v.drop("PDM2p3", "transitionFromAvoidingAgain", "GettingOut", f"!({DIS})")
v.subst("PDM3a", "transitionFromAvoidingAgain", "Avoiding", "clockResetTime < 5", "clockResetTime < 10"); v.subst("PDM3b", "transitionFromAvoidingAgain", "Avoiding", "d1 - d0 > 1.0", "d1 - d0 > 0.5")
v.subst("PDM3c", "transitionFromAvoidingAgain", "Avoiding", "d1 - d0 > 1.0", "d1 - d0 > 2.0")
v.M["PDM4"] = ("transitionFromWaiting", "        requires mode == Waiting\n", "        requires mode == Waiting\n        ensures old(event == Stop) ==> mode == Found\n")
v.droppre("PDM5", "transitionFromAvoidingAgain")
# steam boiler
b = Prog("SteamBoilerController", ["INITIALISATION", "NORMAL", "DEGRADED", "RESCUE", "EMERGENCY_STOP"])
guarded = sorted((m, t) for (m, t), (l, a) in b.ens.items() if a is not None)
assert len(guarded) == 13, guarded   # the two INITIALISATION self-targets share a key
# the INITIALISATION method has two ensures with target INITIALISATION: parse() keeps the last; recover both from the source
init_lines = [l.strip() for l in b.src.splitlines() if re.match(r"\s*ensures old\(.*\) ==> mode == \w+\s*$", l)]
i0, i1 = span(b.src, "transitionFromINITIALISATION"); init_ens = [l.strip() for l in b.src[i0:i1].splitlines() if l.strip().startswith("ensures old(")]
assert len(init_ens) == 5
k = 0
for m, t in guarded:
    if m == "transitionFromINITIALISATION": continue
    b.flip(f"PDB1{chr(97 + k)}", m, t, b.modes[(b.modes.index(t) + 1) % 5]); k += 1
for l in init_ens:
    t = re.search(r"mode == (\w+)\s*$", l).group(1); nt = b.modes[(b.modes.index(t) + 1) % 5]
    b.M[f"PDB1{chr(97 + k)}"] = ("transitionFromINITIALISATION", l, l.replace(f"mode == {t}", f"mode == {nt}")); k += 1
assert k == 14
def init_drop(mid, lineidx, atom, src_line=None):
    l = init_ens[lineidx]; a = re.match(r"ensures old\((.*)\) ==> mode == (\w+)$", l); cs = conj(a.group(1)); assert atom in cs, (mid, atom, cs); cs.remove(atom)
    b.M[mid] = ("transitionFromINITIALISATION", l, f"ensures old({' && '.join(cs)}) ==> mode == {a.group(2)}")
init_drop("PDB2f1", 0, "startUpAbort()"); init_drop("PDB2f2", 3, "programReadySent"); init_drop("PDB2p3", 1, "!startUpAbort()")
b.drop("PDB2f3", "transitionFromNORMAL", "DEGRADED", "unitFailure()"); b.drop("PDB2f4", "transitionFromDEGRADED", "NORMAL", "!unitFailure()"); b.drop("PDB2f5", "transitionFromRESCUE", "NORMAL", "!(unitFailure())")
b.drop("PDB2p1", "transitionFromNORMAL", "RESCUE", "!estimateUnreliable()"); b.drop("PDB2p2", "transitionFromNORMAL", "RESCUE", "!emergencyCause()")
b.M["PDB4a"] = ("transitionFromEMERGENCY_STOP", "        requires mode == EMERGENCY_STOP\n", "        requires mode == EMERGENCY_STOP\n        ensures old(event == Transmission) ==> mode == EMERGENCY_STOP\n")
b.M["PDB4b"] = ("transitionFromNORMAL", "        requires mode == NORMAL\n", "        requires mode == NORMAL\n        ensures old(event == Transmission && !emergencyCause() && !levelFailure() && !unitFailure()) ==> mode == NORMAL\n")
b.droppre("PDB5", "transitionFromNORMAL")
n = g.write() + v.write() + b.write(); print(n, "mutants:", len(g.M), len(v.M), len(b.M))
