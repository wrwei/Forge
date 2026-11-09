#!/usr/bin/env python3
"""Isabelle obligation mutants for the two Chemical Detector theories (addendum of 9 Nov 2026).
Each mutant directory holds the mutated theory only, with a ROOT naming it, so a build checks one theory."""
import os, re, shutil
HERE = os.path.dirname(os.path.abspath(__file__)); B = os.path.join(HERE, "baseline", "isabelle"); OUT = os.path.join(HERE, "mutants")
L = "\\<longrightarrow>"; A = "\\<and>"; O = "\\<or>"
def listens(st, evs): return f"(st = {st} {L} listens = {{{evs}}})"
def one(src, old, new, mid):
    n = src.count(old); assert n == 1, (mid, n, old[:80]); return src.replace(old, new)
def root(theory): return f'session {theory}_Check = "Z_Machines" +\n  theories\n    {theory}\n'
def emit(prefix, theory, muts):
    src = open(os.path.join(B, theory + ".thy")).read()
    for mid, (o, n) in muts.items():
        d = os.path.join(OUT, f"{prefix}{mid}", "isabelle"); os.makedirs(d)
        open(os.path.join(d, theory + ".thy"), "w").write(one(src, o, n, mid)); open(os.path.join(d, "ROOT"), "w").write(root(theory))
    return len(muts)
if os.path.isdir(OUT): shutil.rmtree(OUT)
G = {
 "PI1a": (listens("Reading", "tick, gas"), listens("Reading", "gas")), "PI1b": (listens("Reading", "tick, gas"), listens("Reading", "tick")),
 "PI1c": (listens("Analysis", "tick"), listens("Analysis", "")), "PI1d": (listens("GasDetected", "tick"), listens("GasDetected", "")), "PI1e": (listens("Final", "tick"), listens("Final", "")),
 "PI2a": (listens("Analysis", "tick"), listens("Analysis", "tick, gas")), "PI2b": (listens("Final", "tick"), listens("Final", "tick, gas")),
 "PI3": (f"(st = Analysis {L} (offered = {{}} {O} offered = {{tick}}))", f"(st = Analysis {L} (offered = {{}} {O} offered = {{tick}} {O} offered = {{gas}}))"),
 "PI4": (f"(st = Reading {L} (offered = {{}} {O} offered = {{tick}} {O} offered = {{gas}}))", f"(st = Reading {L} (offered = {{}} {O} offered = {{tick}}))"),
 "PI5": ("[st\\<leadsto> initial", "[st\\<leadsto> Reading"),
 "PI6a": ('zexpr R1 is "True"', 'zexpr R1 is "st \\<noteq> Final"'), "PI6b": ('zexpr R1 is "True"', f'zexpr R1 is "st = Analysis {L} listens = {{tick}}"'),
 "PI7": (" Idle_Reading Idle_Analysis Idle_Final\n", "\n"),
}
V = {
 "PI1a": (listens("Waiting", "resume, stop, turn"), listens("Waiting", "resume, stop")), "PI1b": (listens("Going", "obstacle, resume, turn, stop"), listens("Going", "resume, turn, stop")),
 "PI1c": (listens("Avoiding", "resume, turn, stop"), listens("Avoiding", "resume, stop")), "PI1d": (listens("TryingAgain", "resume, obstacle, turn, stop"), listens("TryingAgain", "resume, turn, stop")),
 "PI1e": (listens("AvoidingAgain", "resume, stop"), listens("AvoidingAgain", "resume")), "PI1f": (listens("GettingOut", "resume, stop, turn"), listens("GettingOut", "resume, stop")), "PI1g": (listens("Final", "tick"), listens("Final", "")),
 "PI2a": (listens("Waiting", "resume, stop, turn"), listens("Waiting", "resume, stop, turn, obstacle")), "PI2b": (listens("AvoidingAgain", "resume, stop"), listens("AvoidingAgain", "resume, stop, turn")),
 "PI3": (f"(st = Waiting {L} (offered = {{}} {O} offered = {{resume}} {O} offered = {{stop}} {O} offered = {{turn}}))", f"(st = Waiting {L} (offered = {{}} {O} offered = {{resume}} {O} offered = {{stop}} {O} offered = {{turn}} {O} offered = {{obstacle}}))"),
 "PI4": (f"(st = Waiting {L} (offered = {{}} {O} offered = {{resume}} {O} offered = {{stop}} {O} offered = {{turn}}))", f"(st = Waiting {L} (offered = {{}} {O} offered = {{resume}} {O} offered = {{stop}}))"),
 "PI5": ("[st\\<leadsto> initial", "[st\\<leadsto> Waiting"),
 "PI6a": ('zexpr R1 is "True"', 'zexpr R1 is "st \\<noteq> Final"'), "PI6b": ('zexpr R1 is "True"', f'zexpr R1 is "st = AvoidingAgain {L} listens = {{resume, stop}}"'),
 "PI7": (" Idle_Waiting Idle_Going Idle_Avoiding Idle_TryingAgain Idle_AvoidingAgain Idle_GettingOut Idle_Final\n", "\n"),
}
n = emit("G", "GasAnalysisController_Beh", G) + emit("M", "MovementController_Beh", V); print(n, "mutants")
