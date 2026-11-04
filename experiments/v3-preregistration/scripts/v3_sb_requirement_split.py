#!/usr/bin/env python3
"""Steam Boiler: which requirements each converged loop run's verified machine implements.

    python3 scripts/v3_sb_requirement_split.py   # writes experiments/v3-analysis/sb_requirement_split.{md,json}

Post-hoc analysis (not pre-registered), from the committed records only. For each
converged Reference Steam Boiler run, every trace row of its converged iteration
(traces/result_codegen.json, written by the session) is placed by the Java file it names:
  controller  the class with `void step(` (the extracted state machine);
  outside     sensor services (rules, section 6), whose methods the model represents
              by uninterpreted functions, and every other file with control flow: sensor, monitor and actuator
              bodies, message builders and the wiring program, which the
              extraction leaves out (sensor calls become uninterpreted functions,
              actuator calls output events);
  vocabulary  files with no control flow: records, enums, interfaces, the
              annotation and constants (shared by both sides; they decide neither).
A requirement is INSIDE if it traces to the controller and to nothing outside, PARTLY if
it traces to both, OUTSIDE if it traces to outside code and not to the controller, and
VOCABULARY-ONLY if it traces to vocabulary files only. Requirements with no trace row
are UNTRACED. The traces are the sessions' own, so the split is as accurate as they are.
"""
import glob, json, os, re, collections
ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
E = os.path.join(ROOT, "experiments")
REQ = json.load(open(os.path.join(ROOT, "forge.assets/case-studies/steam_boiler/requirements/requirement_all.json")))
REQ = REQ if isinstance(REQ, list) else REQ.get("requirements", REQ)

def its(r):
    return sorted([d for d in glob.glob(r + "/iter-*") if os.path.basename(d)[5:].isdigit()], key=lambda x: int(x.rsplit("-", 1)[1]))

def place(L, f):
    rel = f.split("src/main/java/", 1)[-1].split("/", 1)[-1]      # drop the base package
    path = os.path.join(L, "java", rel)
    src = open(path).read() if os.path.exists(path) else ""
    if re.search(r"\bvoid\s+step\s*\(", src): return "controller"
    # a sensor service (rules section 6: class name contains "sensor", or @SensorService):
    # its methods are uninterpreted functions in the model, whatever their bodies do
    if re.search(r"sensor", os.path.basename(rel), re.I) or "@SensorService" in src: return "outside"
    # vocabulary: a type with no control flow (record, enum, interface, annotation,
    # or a class of constants); anything else holds logic outside the model
    # (sensor, monitor and actuator bodies, message builders, the wiring program).
    body = re.sub(r"//[^\n]*|/\*.*?\*/", "", src, flags=re.S)
    if not re.search(r"\b(if|for|while|switch|return\s+[^;]*[?<>&|+*/-])\b", body): return "vocabulary"
    if re.search(r"^\s*(public\s+)?(final\s+)?(record|enum|@?interface)\b", body, re.M) and not re.search(r"\b(if|for|while|switch)\b", body):
        return "vocabulary"
    return "outside"

out = {}; md = ["# Steam Boiler: requirements the verified machine implements", "",
                "Post-hoc, from the committed records; method in `scripts/v3_sb_requirement_split.py`.", "",
                "| Run | Inside | Partly | Outside | Vocabulary only | Untraced |", "|---|---|---|---|---|---|"]
for r in sorted(glob.glob(os.path.join(E, "v3-reference/steam_boiler/run-*")), key=lambda x: int(x.rsplit("-", 1)[1])):
    L = its(r)[-1]
    ps = json.load(open(L + "/summary.json"))["phase_status"]
    assert all(v == "passed" for v in ps.values()), r
    rows = json.load(open(L + "/traces/result_codegen.json"))["codegen_trace"]
    where = collections.defaultdict(set)
    for x in rows: where[x["requirement_gid"]].add(place(L, x["java_file"]))
    cls = {}
    for q in REQ:
        w = where.get(q["id"], set())
        cls[q["id"]] = ("untraced" if not w else "inside" if "controller" in w and "outside" not in w else
                        "partly" if "controller" in w else "outside" if "outside" in w else "vocabulary")
    c = collections.Counter(cls.values())
    run = int(r.rsplit("-", 1)[1])
    out[run] = {"iteration": int(L.rsplit("-", 1)[1]), "class": cls, "counts": dict(c)}
    md.append(f"| {run} | {c['inside']} | {c['partly']} | {c['outside']} | {c['vocabulary']} | {c['untraced']} |")
md += ["", "## Per requirement", "", "| Requirement | Kind | " + " | ".join(f"Run {k}" for k in out) + " |", "|---|---|" + "---|" * len(out)]
for q in REQ:
    md.append(f"| {q['id']} {q['name']} | {q['kind']} | " + " | ".join(out[k]["class"][q["id"]] for k in out) + " |")
os.makedirs(os.path.join(E, "v3-analysis"), exist_ok=True)
open(os.path.join(E, "v3-analysis/sb_requirement_split.md"), "w").write("\n".join(md) + "\n")
json.dump(out, open(os.path.join(E, "v3-analysis/sb_requirement_split.json"), "w"), indent=1)
print("\n".join(md[:11]))
