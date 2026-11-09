#!/usr/bin/env python3
"""Requirements-stated values check: compare every value the requirements state (a table per
case study, compiled from the requirements text) with the Java sources.

  usage: check_stated_values.py <table.json> <java-root>     -> JSON report on stdout

An entry fails when a named constant is declared with a different value, or when a comparison
whose one side contains the entry's quantity has, on its other side, a numeric literal or a
named constant whose value is not one of the values the requirements state for that quantity.
Operators are not compared. Comment text is ignored."""
import json, os, re, sys

NUM = r"-?\d+(?:\.\d+)?(?:[eE][-+]?\d+)?[dDfF]?"
CMP = re.compile(r"(<=|>=|(?<![<>=!-])<(?![<=])|(?<![-<>=])>(?![>=]))")
STOP = re.compile(r"(&&|\|\||[;{}?:,]|\breturn\b|(?<![<>=!])=(?!=))")

def norm(name):
    return name.replace("_", "").lower()

def strip_comments(s):
    s = re.sub(r"/\*.*?\*/", lambda m: "\n" * m.group(0).count("\n"), s, flags=re.S)
    return re.sub(r"//[^\n]*", " ", s)

def java_sources(root):
    out = {}
    for d, _, fs in os.walk(root):
        for f in fs:
            if f.endswith(".java"):
                p = os.path.join(d, f); out[os.path.relpath(p, root)] = strip_comments(open(p, errors="replace").read())
    return out

def declared_constants(src):
    consts = {}
    for rel, s in src.items():
        for m in re.finditer(r"\bstatic\s+final\s+(?:double|float|int|long|short)\s+(\w+)\s*=\s*(" + NUM + r")\s*;", s):
            consts.setdefault(norm(m.group(1)), []).append((rel, float(m.group(2).rstrip("dDfF"))))
    return consts

def operand(text, i, direction):
    """The operand of a comparison: scan from i to the left (-1) or right (+1) to the nearest
    unbalanced bracket or a boolean/statement boundary."""
    depth = 0; j = i
    rng = range(i - 1, -1, -1) if direction < 0 else range(i, len(text))
    for j in rng:
        c = text[j]
        if c in ")]" : depth += 1 if direction < 0 else -1
        elif c in "([": depth += -1 if direction < 0 else 1
        if depth < 0: break
        if depth == 0:
            seg = text[j:i] if direction < 0 else text[i:j + 1]
            if direction < 0 and STOP.match(text, j): break
            if direction > 0 and STOP.match(text, j): break
    return (text[j + 1:i] if direction < 0 else text[i:j]).strip().lstrip("|&").strip()

def resolve(expr, consts):
    e = expr.strip().strip("()").strip()
    if re.fullmatch(NUM, e): return float(e.rstrip("dDfF"))
    name = norm(e.split(".")[-1])
    if re.fullmatch(r"\w+", name) and name in consts and len({v for _, v in consts[name]}) == 1:
        return consts[name][0][1]
    return None

def check(table, root):
    src = java_sources(root); consts = declared_constants(src); rep = {"study": table["study"], "entries": []}
    for c in table["constants"]:
        found = consts.get(norm(c["name"]), [])
        bad = [(f, v) for f, v in found if abs(v - c["value"]) > 1e-9]
        rep["entries"].append({"kind": "constant", "name": c["name"], "req": c["req"], "stated": c["value"],
                               "located": bool(found), "found": found, "holds": bool(found) and not bad})
    for e in table["comparisons"]:
        tab = {norm(c["name"]): c["value"] for c in table["constants"]}
        stated = [float(v) if isinstance(v, (int, float)) else tab[norm(v)] for v in e["values"]]
        pat = re.compile(e["pattern"]); hits = []
        for rel, s in src.items():
            for m in CMP.finditer(s):
                l = operand(s, m.start(), -1); r = operand(s, m.end(), +1)
                for side, other in ((l, r), (r, l)):
                    if pat.search(side) and not pat.search(other):
                        v = resolve(other, consts)
                        if v is not None:
                            line = s[:m.start()].count("\n") + 1
                            hits.append({"file": rel, "line": line, "expr": f"{l} {m.group(0)} {r}"[:160], "value": v,
                                         "ok": any(abs(v - x) < 1e-9 for x in stated)})
        rep["entries"].append({"kind": "comparison", "quantity": e["quantity"], "req": e["req"], "stated": stated,
                               "located": bool(hits), "found": hits, "holds": bool(hits) and all(h["ok"] for h in hits)})
    rep["failing"] = [x.get("name") or x.get("quantity") for x in rep["entries"] if x["located"] and not x["holds"]]
    rep["unlocated"] = [x.get("name") or x.get("quantity") for x in rep["entries"] if not x["located"]]
    return rep

if __name__ == "__main__":
    print(json.dumps(check(json.load(open(sys.argv[1])), sys.argv[2]), indent=1))
