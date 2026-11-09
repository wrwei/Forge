#!/usr/bin/env python3
"""Isabelle obligation mutants for the Steam Boiler theory (addendum of 9 Nov 2026)."""
import os, shutil
HERE = os.path.dirname(os.path.abspath(__file__)); B = os.path.join(HERE, "baseline", "isabelle"); OUT = os.path.join(HERE, "mutants")
L = "\\<longrightarrow>"; O = "\\<or>"
src = open(os.path.join(B, "SteamBoilerController_Beh.thy")).read(); root = open(os.path.join(B, "ROOT")).read()
T6 = ", ".join(["transmission"] * 6); T4 = ", ".join(["transmission"] * 4)
def inv_line(st, evs): return f"(st = {st} {L} listens = {{{evs}}})"
def one(s, old, new, mid):
    n = s.count(old); assert n == 1, (mid, n, old[:90]); return s.replace(old, new)
M = {
 "PI1a": (inv_line("INITIALISATION", T6), inv_line("INITIALISATION", "")), "PI1b": (inv_line("NORMAL", T4), inv_line("NORMAL", "")),
 "PI1c": (inv_line("DEGRADED", T4), inv_line("DEGRADED", "")), "PI1d": (inv_line("RESCUE", T4), inv_line("RESCUE", "")), "PI1e": (inv_line("EMERGENCY_STOP", "transmission"), inv_line("EMERGENCY_STOP", "")),
 "PI2a": (inv_line("NORMAL", T4), inv_line("NORMAL", T4 + ", mode")), "PI2b": (inv_line("INITIALISATION", T6), inv_line("INITIALISATION", T6 + ", programReady")),
 "PI3": (f"(st = INITIALISATION {L} (offered = {{}} {O} offered = {{transmission}}))", f"(st = INITIALISATION {L} (offered = {{}} {O} offered = {{transmission}} {O} offered = {{programReady}}))"),
 "PI4": (f"(st = INITIALISATION {L} (offered = {{}} {O} offered = {{transmission}}))", f"(st = INITIALISATION {L} (offered = {{}}))"),
 "PI5": ("[st\\<leadsto> initial", "[st\\<leadsto> INITIALISATION"),
 "PI6a": ('zexpr R1 is "True"', 'zexpr R1 is "st \\<noteq> EMERGENCY_STOP"'), "PI6b": ('zexpr R1 is "True"', f'zexpr R1 is "st = EMERGENCY_STOP {L} listens = {{transmission}}"'),
 "PI7": (" Idle_INITIALISATION Idle_NORMAL Idle_DEGRADED Idle_RESCUE Idle_EMERGENCY_STOP\n", "\n"),
}
if os.path.isdir(OUT): shutil.rmtree(OUT)
for mid, (o, n) in M.items():
    d = os.path.join(OUT, mid, "isabelle"); os.makedirs(d); open(os.path.join(d, "SteamBoilerController_Beh.thy"), "w").write(one(src, o, n, mid)); open(os.path.join(d, "ROOT"), "w").write(root)
print(len(M), "mutants")
