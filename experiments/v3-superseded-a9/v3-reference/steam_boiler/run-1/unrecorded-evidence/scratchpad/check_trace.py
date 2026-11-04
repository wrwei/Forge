import json, re, os, sys
root = "/Users/ranwei/Gitee/forge-v3r7-run1-steam_boiler"
proj = root + "/java.generated.project/"
reqs = [r["id"] for r in json.load(open(root + "/forge.assets/case-studies/steam_boiler/requirements/requirement_all.json"))["requirements"]]
tr = json.load(open(proj + "result_codegen.json"))["codegen_trace"]
covered = {t["requirement_gid"] for t in tr}
print("requirements:", len(reqs), "missing:", [r for r in reqs if r not in covered], "unknown:", sorted(covered - set(reqs)))
files = set()
for d, _, fs in os.walk(proj + "src/main/java"):
    for f in fs:
        if f.endswith(".java"): files.add(os.path.relpath(os.path.join(d, f), proj))
named = {t["java_file"] for t in tr}
print("files:", len(files), "unnamed:", sorted(files - named), "nonexistent:", sorted(named - files))
# public elements
claimed = {(t["java_file"], t["java_element"]) for t in tr}
pub = set()
for f in files:
    src = open(proj + f).read()
    for m in re.finditer(r'public\s+(?:static\s+)?(?:final\s+)?(?:sealed\s+)?(?:@\w+\s+)?(?:class|enum|interface|record|@interface)\s+(\w+)', src): pub.add((f, m.group(1)))
    for m in re.finditer(r'^\s*record\s+(\w+)', src, re.M): pub.add((f, m.group(1)))
    for m in re.finditer(r'public\s+(?:static\s+)?(?:final\s+)?[\w<>\[\]]+\s+(\w+)\s*\(', src): pub.add((f, m.group(1)))
    for m in re.finditer(r'public\s+static\s+final\s+\w+\s+(\w+)\s*=', src): pub.add((f, m.group(1)))
    if "enum " in src:
        body = src.split("{", 1)[1].split("}")[0]
        for c in re.findall(r'\b([A-Z][A-Z0-9_]+)\b', body): pub.add((f, c))
    for m in re.finditer(r'^\s*(\w+)\s*\(\s*\)\s*;', src, re.M): pass
# annotation method value()
pub.add(("src/main/java/steam_boiler/annotation/RoboChartType.java", "value"))
unclaimed = sorted(p for p in pub if p not in claimed)
print("public elements:", len(pub), "unclaimed:", unclaimed)
# every claimed element appears in its file
bad = [c for c in claimed if not re.search(r'\b' + re.escape(c[1]) + r'\b', open(proj + c[0]).read())]
print("claimed but not found:", bad)
