#!/usr/bin/env python3
"""Build the 27 obligation mutants of prereg-property-mutation-sranger.md from baseline/.
Each mutant is one textual edit of one obligation; the script asserts the edit applies once."""
import os, re, shutil, sys
HERE = os.path.dirname(os.path.abspath(__file__))
B = os.path.join(HERE, "baseline"); OUT = os.path.join(HERE, "mutants")
dfy0 = open(os.path.join(B, "SRangerController.dfy")).read()
thy0 = open(os.path.join(B, "isabelle", "SRangerController_Beh.thy")).read()
root = open(os.path.join(B, "isabelle", "ROOT")).read()

MOV = 'ensures old(!(event == EndTask) && event == Obstacle && distance() <= 0.5) ==> mode == Turning'
TRN = 'ensures old(turnDurationElapsed) ==> mode == Moving'
D = {
 "PD1a": (MOV, MOV.replace("mode == Turning", "mode == Moving")),
 "PD1b": (MOV, MOV.replace("mode == Turning", "mode == Final")),
 "PD1c": (TRN, TRN.replace("mode == Moving", "mode == Turning")),
 "PD1d": (TRN, TRN.replace("mode == Moving", "mode == Final")),
 "PD2a": (MOV, MOV.replace("!(event == EndTask) && ", "")),
 "PD2b": (MOV, MOV.replace("event == Obstacle && ", "")),
 "PD2c": (MOV, MOV.replace(" && distance() <= 0.5", "")),
 "PD2d": (TRN, "ensures mode == Moving"),
 "PD3a": (MOV, MOV.replace("mode == Turning", "mode != Turning")),
 "PD3b": (TRN, TRN.replace("mode == Moving", "mode != Moving")),
 "PD4a": ("        requires mode == Final\n        requires Valid()\n        modifies this\n        ensures Valid()",
          "        requires mode == Final\n        requires Valid()\n        modifies this\n        ensures mode == Final\n        ensures Valid()"),
 "PD4b": (MOV, MOV + "\n        ensures old(event == EndTask) ==> mode == Final"),
 "PD5":  ("    method transitionFromMoving(event: InputEvent)\n        requires mode == Moving\n",
          "    method transitionFromMoving(event: InputEvent)\n"),
}
INV_MOV = "(st = Moving \\<longrightarrow> listens = {tick, obstacle, endTask})"
INV_TRN = "(st = Turning \\<longrightarrow> listens = {endTask, tick})"
INV_FIN = "(st = Final \\<longrightarrow> listens = {tick})"
OFF_TRN = "(st = Turning \\<longrightarrow> (offered = {} \\<or> offered = {endTask} \\<or> offered = {tick}))"
I = {
 "PI1a": (INV_MOV, INV_MOV.replace("{tick, obstacle, endTask}", "{obstacle, endTask}")),
 "PI1b": (INV_MOV, INV_MOV.replace("{tick, obstacle, endTask}", "{tick, endTask}")),
 "PI1c": (INV_MOV, INV_MOV.replace("{tick, obstacle, endTask}", "{tick, obstacle}")),
 "PI1d": (INV_TRN, INV_TRN.replace("{endTask, tick}", "{tick}")),
 "PI1e": (INV_TRN, INV_TRN.replace("{endTask, tick}", "{endTask}")),
 "PI1f": (INV_FIN, INV_FIN.replace("{tick}", "{}")),
 "PI2a": (INV_TRN, INV_TRN.replace("{endTask, tick}", "{endTask, tick, obstacle}")),
 "PI2b": (INV_FIN, INV_FIN.replace("{tick}", "{tick, endTask}")),
 "PI3":  (OFF_TRN, OFF_TRN.replace("offered = {tick}))", "offered = {tick} \\<or> offered = {obstacle}))")),
 "PI4":  (" \\<or> offered = {obstacle}", ""),   # Moving's offer alternatives (the only state listing {obstacle})
 "PI5":  ("[st\\<leadsto> initial", "[st\\<leadsto> Moving"),
 "PI6a": ('zexpr R1 is "True"', 'zexpr R1 is "st \\<noteq> Final"'),
 "PI6b": ('zexpr R1 is "True"', 'zexpr R1 is "st = Final \\<longrightarrow> listens = {tick}"'),
 "PI7":  ("FinalToFinal Idle_Moving Idle_Turning Idle_Final\n", "FinalToFinal\n"),
}
def one(src, old, new, mid):
    n = src.count(old); assert n == 1, (mid, n, old[:60]); return src.replace(old, new)
if os.path.isdir(OUT): shutil.rmtree(OUT)
for mid, (o, n) in D.items():
    d = os.path.join(OUT, mid); os.makedirs(d)
    open(os.path.join(d, "SRangerController.dfy"), "w").write(one(dfy0, o, n, mid))
for mid, (o, n) in I.items():
    d = os.path.join(OUT, mid, "isabelle"); os.makedirs(d)
    open(os.path.join(d, "SRangerController_Beh.thy"), "w").write(one(thy0, o, n, mid))
    open(os.path.join(d, "ROOT"), "w").write(root)
print(len(D), "Dafny and", len(I), "Isabelle mutants under", OUT)
