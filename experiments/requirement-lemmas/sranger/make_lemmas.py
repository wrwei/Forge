#!/usr/bin/env python3
"""Requirement-derived Isabelle lemmas for the SRanger reference controller.

Written from the compiled requirement relation of Section 4.9 (requirements_model_sranger.json,
SR-Beh1..SR-Beh7, SR-FR3), not from the code. For every mode, every event the mode listens for
or none (the store invariant allows at most one offered event), every valuation of the
conditions the requirements name in that mode, and every operation, one Hoare triple states that
the operation, if it can fire, takes the controller to a mode the requirements admit:

    H{st = <mode> & offered = {e} & <valuation> & inv} <op> (nxt) {_. st in <admitted modes>}

Admitted modes: the targets of every requirement enabled in the cell, or the current mode when
none is; Final is terminal. Requirement conditions bound to the theory's expressions by hand
(GUARD below): obstacleDetected (SR-GP1) is distance() <= obstaclethreshold();
turnDurationElapsed (SR-GP2) is the store variable of that name.

usage: make_lemmas.py <theory-in> <theory-out> [--control]
  --control: also replace MovingToTurning's params and update with MovingToFinal's (a wrong
             target mode, otherwise as the extractor would generate it)."""
import itertools, re, sys

# requirement conditions bound to the theory's expressions by hand, from the trace
GUARD = {"obstacleDetected": r"(distance()\<le>obstaclethreshold())",      # SR-GP1
         "turnDurationElapsed": r"turnDurationElapsed"}                      # SR-GP2
# (requirement, source mode, trigger event or None, guard or None, target): SR-Beh2..SR-Beh7
ROWS = [("SR-Beh2", "Moving", "obstacle", "obstacleDetected", "Turning"),
        ("SR-Beh3", "Moving", "endTask", None, "Final"),
        ("SR-Beh4", "Moving", "tick", None, "Moving"),
        ("SR-Beh5", "Turning", None, "turnDurationElapsed", "Moving"),
        ("SR-Beh6", "Turning", "endTask", None, "Final"),
        ("SR-Beh7", "Turning", "tick", None, "Turning")]
SHORT = {"obstacleDetected": "o", "turnDurationElapsed": "d"}


def cells(listens):
    """One cell per mode, offered event (the store invariant allows at most one) or none, and
    valuation of the conditions the requirements name in that mode."""
    yield "initial", "Init", "offered = {}", ["Moving"]                          # SR-Beh1
    for mode in ["Moving", "Turning", "Final"]:
        rs = [r for r in ROWS if r[1] == mode]
        conds = sorted({g for *_, g, _ in rs if g})
        for e in ["none"] + sorted(listens[mode]):
            for vals in itertools.product([True, False], repeat=len(conds)):
                v = dict(zip(conds, vals))
                adm = sorted({t for _, _, trig, g, t in rs if (trig is None or trig == e) and (g is None or v[g])}) or [mode]
                off = "offered = {}" if e == "none" else f"offered = {{{e}}}"
                pre = " \\<and> ".join([off] + [GUARD[c] if v[c] else f"\\<not>{GUARD[c]}" for c in conds])
                tag = f"{mode}_{e}_" + ("".join(SHORT[c].upper() if v[c] else SHORT[c] for c in conds) or "x")
                yield mode, tag, pre, adm


def main(src, dst, control):
    s = open(src).read()
    ops = re.search(r"operations\s+(.*)", s).group(1).split()
    if control:
        # params and update both, so the operation is one the extractor could produce for a
        # Final target (offered' drawn from Final's offers) and the invariant still holds
        upd = lambda name: re.search(rf"zoperation {name} =.*?(  params .*?\n)  pre .*?\n(  update \".*?\]\")", s, re.S)
        ma, mb = upd("MovingToTurning"), upd("MovingToFinal")
        blk = lambda m: m.group(0)[m.start(1) - m.start(0):]
        a = ma.group(0); b = a.replace(ma.group(1), mb.group(1)).replace(ma.group(2), mb.group(2))
        s = s.replace(a, b, 1)
        a = b = None
    listens = {m: re.search(r"st = " + m + r" \\<longrightarrow> listens = \{(.*?)\}", s).group(1).replace(" ", "").split(",")
               for m in ["Moving", "Turning", "Final"]}
    out = ["", r"subsection \<open> Requirement-derived obligations (SR-Beh1..SR-Beh7) \<close>", ""]
    n = 0
    for mode, tag, pre, adm in cells(listens):
        out.append(f'zexpr Pre_{tag} is "st = {mode} \\<and> {pre}"')
        out.append(f'zexpr Adm_{tag} is "st \\<in> {{{", ".join(adm)}}}"')
        for op in ops:
            n += 1
            out.append(f'lemma Req_{tag}_{op}: "H{{Pre_{tag} \\<and> SRangerController_inv}} {op} (nxt) {{_. Adm_{tag}}}"')
            out.append("  by (zpog_full; simp)")   # simp, not auto: auto searches without end on a false goal
        out.append("")
    i = s.rstrip().rfind("\nend")
    s = s[:i] + "\n" + "\n".join(out) + s[i:]
    open(dst, "w").write(s)
    print(n, "lemmas;", len(ops), "operations")


if __name__ == "__main__":
    main(sys.argv[1], sys.argv[2], "--control" in sys.argv[3:])
