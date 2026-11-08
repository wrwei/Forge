#!/usr/bin/env python3
"""Link every changed line of every generated artefact (Isabelle theory, Dafny program, RoboChart
model) of each mutant inside the abstraction boundary back to the injection site, through the
pipeline's per-phase trace records.

Injection site S: the Java lines of the mutated file that differ from the unmutated source.
Footprint of S (the elements the trace records say S generates):
  - a line inside a transition's branch (trace_m2m.json: transition -> Java lines) -> that
    transition, plus the later transitions of the same mode block, whose guards carry the negation
    of the earlier branches' guards (the extractor's priority encoding);
  - a mode test (`currentMode == M`) -> every transition of the controller, since the test gates
    the whole dispatch;
  - a controller field assignment before the dispatch (lifted into every transition's update) or a
    field initialiser -> every operation of the controller;
  - a named constant -> every line where its value is used (the RoboChart model, the CSP and the
    Dafny program inline the value; the theory defines it).
A changed artefact line is TRACED if it belongs to an element of the footprint (theory operation
and its lemmas, via trace_isabelle.json; Dafny transition method, via trace_dafny.json; RoboChart
transition block, from the .rct text), or if it mentions an identifier assigned, declared or
compared on a line of S, or (constants) if it differs from its baseline line only in a numeric
literal. Lines are compared after the campaign's normalisation (comments removed, set elements,
disjuncts and enum alternatives sorted); a line that occurs elsewhere in the baseline is a moved
line, not a change.
Usage: trace_changed_lines.py <campaign-dir> <kill_table.csv> <reference-runs dir> <manifest.csv> <out.csv>
"""
import csv, sys, os, re, json, difflib, collections, glob
sys.path.insert(0, "/Users/ranwei/Git/Forge/experiments/mutation-testing/scripts")
from canon import canon_sets, canon_or
CAMP, KT, REF, MAN, OUT = sys.argv[1:6]
kt = [r for r in csv.DictReader(open(KT)) if r["inside_boundary"] == "True"]
man = {r["mutant_id"]: r for r in csv.DictReader(open(MAN))}

def norm(kind, t):
    keep_nl = lambda m: "\n" * m.group(0).count("\n")
    if kind == "thy": t = re.sub(r"\(\*.*?\*\)", keep_nl, t, flags=re.S)
    else: t = re.sub(r"/\*.*?\*/", keep_nl, t, flags=re.S); t = re.sub(r"//[^\n]*", "", t)
    out = []
    for l in t.splitlines():
        l = canon_or(canon_sets(l))
        m = re.match(r"(\s*(?:enumtype|datatype)\s+\w+\s*=\s*)(.*)$", l)
        if m: l = m.group(1) + " | ".join(sorted(x.strip() for x in m.group(2).split("|")))
        mo = re.match(r"(\s*operations\s+)(.*)$", l)
        if mo: l = mo.group(1) + " ".join(sorted(mo.group(2).split()))
        out.append(l)
    return out

NUM = re.compile(r"-?\d+(?:\.\d+)?")
def changed_lines(a, b):
    """mutant-side line numbers that are changed (not moved); with the paired baseline line where one exists"""
    sm = difflib.SequenceMatcher(None, a, b, autojunk=False); out = {}
    aset = {x.strip() for x in a}
    for tag, i1, i2, j1, j2 in sm.get_opcodes():
        if tag == "equal": continue
        for k, j in enumerate(range(j1, j2)):
            if not b[j].strip() or b[j].strip() in aset: continue
            out[j + 1] = a[i1 + k] if (tag == "replace" and i1 + k < i2) else None
    return out

def idents(lines):
    ids = set()
    for l in lines:
        for m in re.finditer(r"this\.(\w+)\s*=[^=]", l): ids.add(m.group(1))
        for m in re.finditer(r"(?:final|static)\s+\w+\s+(\w+)\s*=", l): ids.add(m.group(1))
        for m in re.finditer(r"^\s*(?:private|public|protected)?\s*[\w.<>]+\s+(\w+)\s*=\s", l): ids.add(m.group(1))
        for m in re.finditer(r"\b\w*(?:Event|Mode)\.(\w+)", l): ids.add(m.group(1))
    return {i.lower() for i in ids}

def rct_blocks(lines):
    """(stm name, transition name, start, end) for every transition block, and stm spans"""
    stms = []; trans = []; cur = None; depth = 0; stack = []
    for i, l in enumerate(lines, 1):
        ms = re.match(r"\s*stm\s+(\w+)\s*\{", l)
        if ms: cur = ms.group(1); stms.append([cur, i, None])
        mt = re.match(r"\s*transition\s+(\w+)\s*\{", l)
        if mt: stack.append((mt.group(1), i))
        if stack and l.strip() == "}": name, s0 = stack.pop(); trans.append((cur, name, s0, i))
        if cur and l.startswith("}"): stms[-1][2] = i; cur = None
    return stms, trans

rows = []
for r in kt:
    mid = r["mutant_id"]; st = r["study"]; m = man[mid]; f = m["file"]; cls = os.path.basename(f)[:-5]
    base_java = open(f"{REF}/{st}/java/{f}").read().splitlines()
    mut_java = open(f"{CAMP}/mutants/{mid}/java/{f}").read().splitlines()
    S = [i + 1 for i, (x, y) in enumerate(zip(base_java, mut_java)) if x != y] or [int(m["line"])]
    S_text = [mut_java[i - 1] for i in S] + [base_java[i - 1] for i in S if i <= len(base_java)]
    ids = idents(S_text)
    O = f"{CAMP}/outputs/{mid}"; B = f"{CAMP}/outputs/baseline-{st}"
    m2m = json.load(open(f"{O}/trace_m2m.json"))["mappings"]
    trans = [x for x in m2m if x["robochart_type"] == "Transition"]
    def in_cls(x): return x.get("java_element", "").split(".")[0] == cls
    ctrl_trans = [x for x in trans if in_cls(x)]           # the mutated controller's transitions, in order
    rct_lines = norm("rct", open(glob.glob(f"{O}/*.rct")[0]).read())
    stms, rblocks = rct_blocks(rct_lines)
    stm_name = next((s[0] for s in stms if s[0].lower().startswith(cls.lower()[:12])), None)
    rtrans = [b for b in rblocks if b[0] == stm_name and re.match(r"t\d+$", b[1])]   # (stm, tK, s, e) in order
    src_of = {}
    for (_, name, s0, e0) in rtrans:
        mf = re.search(r"from\s+(\w+)", " ".join(rct_lines[s0 - 1:e0])); src_of[name] = mf.group(1) if mf else None
    # ---- footprint ----
    hit = [x["robochart_element"] for x in ctrl_trans if any(x["java_line_start"] <= s <= x["java_line_end"] for s in S)]
    mode_ids = {i for i in ids if any((src or "").lower() == i for src in src_of.values())}
    is_mode_test = any(re.search(r"currentMode\s*[!=]=", t) for t in S_text)
    if hit and not is_mode_test:
        site = "branch"; modes = {src_of.get(t) for t in hit}
        for x in ctrl_trans:
            if x["robochart_element"] in hit: ids |= idents(mut_java[x["java_line_start"] - 1:x["java_line_end"]])
        names = [x["robochart_element"] for x in ctrl_trans]
        T = set(hit)
        for t in hit:                                   # later siblings of the same mode carry the negated guard
            for u in names[names.index(t) + 1:]:
                if src_of.get(u) == src_of.get(t): T.add(u)
    elif ctrl_trans and mode_ids:
        site = "mode-dispatch"; T = set(src_of); modes = set(src_of.values())
    elif ctrl_trans:
        site = "controller-field"; T = set(src_of); modes = set(src_of.values())
    elif "onstants" in f:
        site = "constant"; T = set(); modes = set()
    else:
        site = "operation-class"; T = set(src_of); modes = set(src_of.values())
    whole_ctrl = site in ("mode-dispatch", "controller-field", "operation-class")
    lits = set(NUM.findall(m["before"])) | set(NUM.findall(m["after"]))
    for kind, pat in (("thy", "isabelle/*_Beh.thy"), ("dfy", "*.dfy"), ("rct", "*.rct")):
        for mp in sorted(glob.glob(f"{O}/{pat}")):
            bp = f"{B}/{os.path.relpath(mp, O)}"
            if not os.path.exists(bp): continue
            a = norm(kind, open(bp).read()); b = norm(kind, open(mp).read())
            ch = changed_lines(a, b)
            if not ch: continue
            this_ctrl = (kind != "thy" or os.path.basename(mp).startswith(cls)) and (kind != "dfy" or os.path.basename(mp).startswith(cls)) if st == "chemical_detector" else True
            owner = {}
            if kind == "thy":
                tr = json.load(open(f"{O}/trace_isabelle.json"))["mappings"]
                ops = sorted([x for x in tr if x["isabelle_type"] == "zoperation" and x.get("theory", os.path.basename(mp)) == os.path.basename(mp)], key=lambda x: x["thy_line_start"])
                ctrl_ops = [x["isabelle_element"] for x in ops if not x["isabelle_element"].startswith(("Initial", "Idle_"))]
                op2t = dict(zip(ctrl_ops, [x["robochart_element"] for x in ctrl_trans] if this_ctrl else []))
                first_op = ops[0]["thy_line_start"] if ops else 10**9
                for l in ch:
                    own = None
                    for x in ops:
                        if x["thy_line_start"] <= l: own = x["isabelle_element"]
                    lem = re.match(r"\s*lemma\s+(\w+?)_(?:inv|wf|enabled|total)\b", b[l - 1])
                    if lem: own = lem.group(1)
                    lem2 = re.match(r"\s*lemma\s+\"(\w+?)\s*\(", b[l - 1])
                    if lem2: own = lem2.group(1)
                    if own and own.startswith("Idle_"): owner[l] = ("idle", own[5:])
                    else: owner[l] = ("decl", None) if l < first_op else ("op", op2t.get(own, own))
            elif kind == "dfy":
                tr = json.load(open(f"{O}/trace_dafny.json"))["mappings"]
                els = [x for x in tr if x.get("dafny_line_start") and x["controller"] + ".dfy" == os.path.basename(mp)]
                els = sorted([x for x in els if x["dafny_type"] != "Controller"], key=lambda x: int(x["dafny_line_start"]))
                for l in ch:
                    own = ("decl", None)
                    for x in els:
                        if int(x["dafny_line_start"]) <= l:
                            own = ("method", x["dafny_element"]) if x["dafny_type"] in ("TransitionMethod", "Lemma", "StepMethod") else ("other", x["dafny_element"])
                    owner[l] = own
            else:
                for l in ch:
                    own = ("decl", None)
                    for (sname, name, s0, e0) in rblocks:
                        if s0 <= l <= e0: own = ("op", name if sname == stm_name else f"{sname}:{name}")
                    owner[l] = own
            traced = []; untraced = []
            for l, base_line in ch.items():
                kind_o, el = owner[l]; text = b[l - 1].lower(); ok = False
                if kind_o == "op" and el in T and this_ctrl: ok = True
                if kind_o == "idle" and this_ctrl and (whole_ctrl or el in modes): ok = True
                if kind_o == "method" and this_ctrl and (whole_ctrl or any(md and (el.endswith(md) or el.startswith(md)) for md in modes)): ok = True
                if kind_o in ("op", "decl", "other") and whole_ctrl and this_ctrl: ok = True
                if ids and any(re.search(r"\b" + re.escape(i) + r"\b", text) for i in ids): ok = True
                if site == "constant" and base_line is not None and NUM.sub("#", base_line.strip()) == NUM.sub("#", b[l - 1].strip()):
                    fl = lambda xs: {float(x) for x in xs}
                    if (fl(NUM.findall(base_line)) ^ fl(NUM.findall(b[l - 1]))) <= fl(lits): ok = True
                (traced if ok else untraced).append(l)
            rows.append(dict(mutant_id=mid, study=st, defect_class=r["defect_class"], site=site, java_lines=" ".join(map(str, S)),
                             identifiers=" ".join(sorted(ids)), artefact=os.path.relpath(mp, O), changed=len(ch), traced=len(traced),
                             untraced=len(untraced), untraced_sample=" | ".join(b[l - 1].strip()[:80] for l in untraced[:3])))
w = csv.DictWriter(open(OUT, "w", newline=""), fieldnames=list(rows[0].keys())); w.writeheader(); w.writerows(rows)
tot = collections.Counter(); mut = collections.defaultdict(lambda: True)
for x in rows:
    tot[(x["artefact"].split(".")[-1], "changed")] += int(x["changed"]); tot[(x["artefact"].split(".")[-1], "traced")] += int(x["traced"])
    if int(x["untraced"]): mut[x["mutant_id"]] = False
    else: mut.setdefault(x["mutant_id"], True)
print("lines:", {k: v for k, v in sorted(tot.items())})
print("mutants with artefact changes:", len(mut), "| every changed line traced:", sum(mut.values()))
print("untraced by class:", collections.Counter(x["defect_class"] for x in rows if int(x["untraced"])))
