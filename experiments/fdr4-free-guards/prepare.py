#!/usr/bin/env python3
"""Stage the LRE CSP models for FDR4 with the guards left free.

For each model (the reference controller and the converged iteration of every LRE loop run and
cold pass that converged), the recorded csp-gen directory is copied, the pipeline's CSP
corrections are applied as in Phase 6b, and in the controller's state machine every distinct
guard atom (a comparison, or a Boolean variable read as a condition) is replaced by a Boolean
chosen freely at every step. Equal atoms share one Boolean, so a guard and the
negation of the same guard in a later branch stay consistent. This is the abstraction of the
guard-level enumeration of Section 5.5: every combination of guard values is admitted,
including combinations the plant could never produce, and no sensor function or numeric range
decides which mode is entered. The reads of the controller's memory at the head of each step
are then unused and are removed (the memory always offers them, and they are hidden), which
keeps the compiled process small. Since a step depends only on which guards hold, the script
enumerates every assignment of the atoms, keeps the distinct sets of guards that hold, and makes
one internal choice per step over those sets (the same behaviour as a choice over the atoms).

The assertion file checks the pipeline's three counted assertions (deadlock freedom, deadlock
freedom with termination, divergence freedom) and, on the process with visible state entry,
that each mode can be entered.

usage: prepare.py <out-dir> [--control]
  --control: stage the models unchanged (the same assertions on the CSP as Phase 6b checks it)
"""
import glob, json, os, re, shutil, subprocess, sys

HERE = os.path.dirname(os.path.abspath(__file__))
REPO = os.path.abspath(os.path.join(HERE, "..", ".."))
EXP = os.path.join(REPO, "experiments")
BASE = os.path.expanduser("~/Gitee/forge-mutation-a20/experiments/mutation-testing-a20/outputs/baseline-lre/rct-project/csp-gen")
MODES = ["OCM", "MOM", "HCM", "CAM"]


def models():
    out = [("baseline", BASE)]
    for cond in ("v3-cold", "v3-reference"):
        for run in sorted(glob.glob(f"{EXP}/{cond}/lre/run-*"), key=lambda p: int(p.rsplit("-", 1)[1])):
            its = sorted([p for p in glob.glob(f"{run}/iter-*") if re.search(r"iter-\d+$", p)],
                         key=lambda p: int(p.rsplit("-", 1)[1]))
            last = its[-1]
            st = json.load(open(f"{last}/summary.json"))["phase_status"]
            if all(v == "passed" for v in st.values()):
                out.append((f"{cond[3:]}-run{run.rsplit('-',1)[1]}", f"{last}/formal-artefacts/csp-gen"))
    return out


def matching(s, i):
    """Index of the parenthesis closing the one at s[i]."""
    d = 0
    for k in range(i, len(s)):
        if s[k] == "(":
            d += 1
        elif s[k] == ")":
            d -= 1
            if d == 0:
                return k
    raise ValueError("unbalanced")


TERM = r"(?:-?\d+|[A-Za-z_]\w*(?:\([^()]*\))?)"
CMP = re.compile(rf"({TERM})\s*(<=|>=|==|!=|<|>)\s*({TERM})")
KEYWORDS = {"and", "or", "not", "true", "false"}


def free_guards(path):
    s = open(path).read()
    m = re.search(r"Trans = share__choice\(((?:get_\w+\?\w+ -> )*)\(", s)
    assert m, "no Trans in " + path
    open_i = m.end() - 1
    close_i = matching(s, open_i)
    body = s[open_i + 1:close_i]
    atoms = {}

    def var(text):
        key = re.sub(r"\s+", "", text)
        if key not in atoms:
            atoms[key] = f"g{len(atoms)+1}__"
        return atoms[key]

    # The top-level branches of Trans, in order.
    spans, i = [], 0
    while True:
        j = body.find("dbisim((", i)
        if j < 0:
            break
        e = matching(body, j + len("dbisim") )
        spans.append((j, e + 1)); i = e + 1
    for (a0, a1), (b0, b1) in zip(spans, spans[1:]):
        assert body[a1:b0].strip() == "[]", repr(body[a1:b0])
    head, tail = body[:spans[0][0]], body[spans[-1][1]:]
    groups, order, plain = {}, [], []
    for a, b in spans:
        br = body[a:b]
        g0 = len("dbisim(")
        g1 = matching(br, g0)
        assert br[g1 + 1] == "&", br[g1:g1 + 20]
        guard = br[g0 + 1:g1]
        if guard.strip() == "true":
            plain.append(br); continue
        new = CMP.sub(lambda mm: var(mm.group(0)), guard)
        new = re.sub(r"\b([A-Za-z_]\w*)\b(?!\s*\()",
                     lambda mm: mm.group(1) if (mm.group(1) in KEYWORDS or re.fullmatch(r"g\d+__", mm.group(1)) or mm.group(1).startswith("const_")) else var(mm.group(1)),
                     new)
        src = re.search(r"!NID_(\w+?)(?:\.in|\.out)?\b", br[g1:g1 + 200]).group(1)
        if src not in groups:
            groups[src] = []; order.append(src)
        groups[src].append((br[:g0 + 1], new, br[g1:]))
    # Which guards hold depends only on the atoms; a step depends only on which guards hold.
    # Enumerate every assignment of the atoms and keep the distinct sets of guards that hold;
    # one internal choice over those sets replaces the choice over the atoms (same behaviour,
    # far fewer compiled copies of the step).
    guarded = [(src, pre, g, post) for src in order for (pre, g, post) in groups[src]]
    used = sorted(set(re.findall(r"g\d+__", " ".join(g for _, _, g, _ in guarded))), key=lambda x: int(x[1:-2]))
    def py(g):
        return re.sub(r"\btrue\b", "True", re.sub(r"\bfalse\b", "False", g))
    code = [compile(py(g), "<guard>", "eval") for _, _, g, _ in guarded]
    vecs = set()
    for k in range(2 ** len(used)):
        env = {v: bool(k >> i & 1) for i, v in enumerate(used)}
        vecs.add(frozenset(i for i, c in enumerate(code) if eval(c, {}, env)))
    vecs = sorted(vecs, key=lambda x: (len(x), sorted(x)))
    group_atoms = {src: sorted(set(re.findall(r"g\d+__", " ".join(g for s_, _, g, _ in guarded if s_ == src))),
                               key=lambda x: int(x[1:-2])) for src in order}
    parts = list(plain) + [pre + f"(member({i}, gv__))" + post for i, (_, pre, _, post) in enumerate(guarded)]
    body2 = head + "\n\t\t\t \t [] ".join(parts) + tail
    if guarded:
        sets = ", ".join("{" + ", ".join(str(i) for i in sorted(v)) + "}" for v in vecs)
        body2 = f"|~| gv__ : {{{sets}}} @ (" + body2 + ")"
    atoms["__vectors__"] = len(vecs)
    n = len(spans)
    atoms["__groups__"] = group_atoms
    reads = m.group(1)
    kept = "".join(r for r in re.findall(r"get_\w+\?\w+ -> ", reads)
                   if re.search(r"\b%s\b" % re.escape(r.split("?")[1].split(" ")[0]), body2))
    atoms["__dropped_reads__"] = len(re.findall(r"get_\w+\?", reads)) - len(re.findall(r"get_\w+\?", kept))
    pre = s[:m.start()] + "Trans = share__choice(" + kept + "("
    s2 = pre + body2 + s[close_i:]
    open(path, "w").write(s2)
    return n, atoms


def assertions(defs):
    ca = os.path.join(defs, "LreController_coreassertions.csp")
    s = open(ca).read()
    s = re.sub(r"^(\s*assert.*:\[deterministic\].*)$", r"-- determinism not counted: \1", s, flags=re.M)
    m = re.search(r"P_LreController = let(.*?)\n\)", s, re.S) or re.search(r"P_LreController = let(.*?within.*?\n\t\))", s, re.S)
    blk = s[s.index("P_LreController = let"):s.index("assert P_LreController :[deadlock-free]")]
    vs = blk.replace("P_LreController = let", "P_LreController_VS = let").replace("LreController::O__(", "LreController::VS__(")
    reach = "\n".join(
        f"assert P_LreController_VS \\ diff(Events, {{LreController::{m}::entered}}) :[has trace [T]]: <LreController::{m}::entered>"
        for m in MODES)
    s = s.rstrip() + "\n\n-- the same machine with state entry visible\n" + vs.rstrip() + "\n\n-- each mode can be entered\n" + reach + "\n"
    open(os.path.join(defs, "free_guards_assertions.csp"), "w").write(s)


def main(out, control=False):
    os.makedirs(out, exist_ok=True)
    rows = []
    for label, src in models():
        st = os.path.join(out, label)
        shutil.rmtree(st, ignore_errors=True)
        shutil.copytree(src, st)
        subprocess.run(["/usr/bin/python3", os.path.join(REPO, "scripts", "apply_corrections_standalone.py"), st],
                       check=True, capture_output=True)
        if control:   # the model as Phase 6b checks it: guards read the stubs and the narrowed ranges
            n, atoms = 0, {"__groups__": {}, "__dropped_reads__": 0, "__vectors__": 0}
        else:
            n, atoms = free_guards(os.path.join(st, "defs", "LreController.csp"))
        assertions(os.path.join(st, "defs"))
        grp = atoms.pop("__groups__"); dropped = atoms.pop("__dropped_reads__"); nvec = atoms.pop("__vectors__")
        rows.append(dict(model=label, source=os.path.relpath(src, os.path.expanduser("~")), branches=n, atoms=len(atoms),
                         atom_list=list(atoms), atoms_per_mode={k: len(v) for k, v in grp.items()}, memory_reads_dropped=dropped, guard_value_sets=nvec))
        print(label, n, "branches,", len(atoms), "atoms,", nvec, "sets of guards that hold,", dropped, "reads dropped")
    json.dump(rows, open(os.path.join(out, "models.json"), "w"), indent=1)


if __name__ == "__main__":
    main(sys.argv[1], control="--control" in sys.argv[2:])
