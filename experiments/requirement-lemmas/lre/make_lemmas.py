#!/usr/bin/env python3
"""Requirement-derived Isabelle lemmas for the LRE reference controller.

Source of the obligations: the independent reference specification of Section 4.9.1, as encoded
in Forge's experiments/reference-specification/score.py (SPEC_ROWS, spec_guards), read from that
file, not from the code. Two families:

(A) Mode level. For every mode, every event the mode listens for (or none), every valuation of
    the conditions the specification names in that mode, and every operation:
      H{st = mode & offered = {e} & valuation & Bind & inv} op (nxt) {_. st in admitted}
    admitted = targets of every enabled specification row, or the current mode when none is.
(B) Binding. The theory's guards read three Boolean store variables the Java computes; Bind
    states that each equals the specification condition it stands for:
      camActive  = Collision            (cda < minSafeDist and tcpa >= 0)
      hcmActive  = B_h or B_0 or B_v
      momReturn  = ClearStatic          (not hdist <= horiz and not vdist <= vert)
    Every operation preserves Bind, and InitialToOCM establishes it, so it holds from the first
    step on (the Isabelle Init leaves data free).

The specification's atoms are bound to the theory's expressions by hand (ATOM below).

usage: make_lemmas.py <theory-in> <theory-out> [--control-target | --control-guard]
  --control-target: MOMToHCM takes MOMToOCM's params and update (goes to OCM).
  --control-guard:  in every operation, camActive' tests tcpa > 0 instead of tcpa >= 0."""
import ast, itertools, os, re, sys

SCORE = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "..", "reference-specification", "score.py")
src = open(SCORE).read()
SPEC_ROWS = ast.literal_eval(re.search(r"SPEC_ROWS = (\{.*?\n\})", src, re.S).group(1))

ATOM = {"inOpez": "inOpez", "vel_le_1": "vel\\<le>1.0", "odc_gt_1": "odist(cdyn)>1.0",
        "ocs_gt_1": "odist(cstc)>1.0", "cda_lt_ms": "cda<minsafedist()", "tcpa_ge_0": "tcpa\\<ge>0.0",
        "hvel_ge_1": "hvel\\<ge>1.0", "hdist_le_h": "hdist(cstc)\\<le>staticobshorizdist()",
        "vdist_le_dv": "vdist(cstc)\\<le>staticobsdfltvertdist()", "vvel_ge_1": "vvel\\<ge>1.0",
        "vdist_le_v": "vdist(cstc)\\<le>staticobsvertdist()"}
A = lambda k: f"({ATOM[k]})"
N = lambda x: f"\\<not>{x}"
# specification guards (score.py spec_guards), over the theory's expressions
GUARD = {"P": A("inOpez"),
         "EnterMOM": f"({A('vel_le_1')} \\<and> {N(A('inOpez'))} \\<and> {A('odc_gt_1')} \\<and> {A('ocs_gt_1')})",
         "Collision": f"({A('cda_lt_ms')} \\<and> {A('tcpa_ge_0')})",
         "B": f"(({A('hvel_ge_1')} \\<and> {A('hdist_le_h')}) \\<or> {A('vdist_le_dv')} \\<or> ({A('vvel_ge_1')} \\<and> {A('vdist_le_v')}))",
         # the specification: the distances "exceed" the thresholds
         "ClearStatic": "(hdist(cstc)>staticobshorizdist() \\<and> vdist(cstc)>staticobsvertdist())",
         "SafeCPA": N(A("cda_lt_ms"))}
GROUP = {"B_h": "B", "B_0": "B", "B_v": "B"}    # three rows, one target (HCM): one condition
BIND = (f"(camActive \\<longleftrightarrow> {GUARD['Collision']}) \\<and> "
        f"(hcmActive \\<longleftrightarrow> {GUARD['B']}) \\<and> (momReturn \\<longleftrightarrow> {GUARD['ClearStatic']})")
MODES = ["OCM", "MOM", "HCM", "CAM"]
SHORT = {"P": "p", "EnterMOM": "m", "Collision": "c", "B": "b", "ClearStatic": "s", "SafeCPA": "f"}


def rows(mode):
    return [(trig, GROUP.get(g, g) if g else None, dest) for (m, trig, g, dest, _) in SPEC_ROWS.values() if m == mode]


def cells(listens):
    yield "initial", "Init", "True", ["OCM"]
    for mode in MODES:
        rs = rows(mode)
        conds = sorted({g for _, g, _ in rs if g})
        for e in ["none"] + sorted(listens[mode]):
            for vals in itertools.product([True, False], repeat=len(conds)):
                v = dict(zip(conds, vals))
                adm = sorted({d for trig, g, d in rs if (trig is None or trig == e) and (g is None or v[g])}) or [mode]
                off = "offered = {}" if e == "none" else f"offered = {{{e}}}"
                pre = " \\<and> ".join([off] + [GUARD[c] if v[c] else N(GUARD[c]) for c in conds])
                tag = f"{mode}_{e}_" + ("".join(SHORT[c].upper() if v[c] else SHORT[c] for c in conds) or "x")
                yield mode, tag, pre, adm


def main(src_thy, dst, mode):
    s = open(src_thy).read()
    ops = re.search(r"operations\s+(.*)", s).group(1).split()
    listens = {m: re.search(rf"st = {m} \\<longrightarrow> listens = \{{(.*?)\}}", s).group(1).replace(" ", "").split(",")
               for m in MODES}
    if mode == "--control-target":
        get = lambda name: re.search(rf"zoperation {name} =\n  over \w+\n(  params .*?\n)  pre .*?\n(  update \".*?\]\")", s, re.S)
        a, b = get("MOMToHCM"), get("MOMToOCM")
        s = s.replace(a.group(0), a.group(0).replace(a.group(1), b.group(1)).replace(a.group(2), b.group(2)), 1)
    elif mode == "--control-guard":
        def mut(m):
            line = m.group(0); i = line.rfind("\\<ge>0.0")
            assert i > 0, line[:80]
            return line[:i] + ">0.0" + line[i + len("\\<ge>0.0"):]
        s, k = re.subn(r",camActive\\<Zprime> = [^\n]*", mut, s)
        print("camActive updates mutated:", k)
    out = ["", r"subsection \<open> Requirement-derived obligations (reference specification) \<close>", "",
           f'zexpr Bind is "{BIND}"', ""]
    nb = 0
    # disj_assoc: the theory computes hcmActive as ((B_h | B_0) | B_v), the specification reads B_h | B_0 | B_v
    BY = "  by (zpog_full; simp add: disj_assoc)"
    out += [f'lemma Bind_establish: "H{{True}} InitialToOCM (nxt) {{_. Bind}}"', BY]; nb += 1
    for op in ops:
        out += [f'lemma Bind_{op}: "{op} (nxt) preserves Bind"', BY]; nb += 1
    out.append("")
    na = 0
    for m, tag, pre, adm in cells(listens):
        out.append(f'zexpr Pre_{tag} is "st = {m} \\<and> {pre}"')
        out.append(f'zexpr Adm_{tag} is "st \\<in> {{{", ".join(adm)}}}"')
        for op in ops:
            na += 1
            out += [f'lemma Req_{tag}_{op}: "H{{Pre_{tag} \\<and> Bind \\<and> LreController_inv}} {op} (nxt) {{_. Adm_{tag}}}"',
                    "  by (zpog_full; simp)"]
        out.append("")
    i = s.rstrip().rfind("\nend")
    open(dst, "w").write(s[:i] + "\n" + "\n".join(out) + s[i:])
    print(f"{na} mode-level and {nb} binding lemmas; {len(ops)} operations; listens {listens}")


if __name__ == "__main__":
    main(sys.argv[1], sys.argv[2], sys.argv[3] if len(sys.argv) > 3 else None)
